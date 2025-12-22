/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.fs.hfsplus.attributes;

import java.io.IOException;
import java.nio.ByteBuffer;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusForkData;
import org.jnode.fs.hfsplus.SuperBlock;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.BTHeaderRecord;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;
import org.jnode.util.ByteBufferUtils;
import vavi.util.ByteUtil;


/**
 * The attributes file in the HFS+ volume.
 *
 * @author Luke Quinane
 */
public class Attributes {
    /**
     * The logger.
     */
    private static final Logger log = System.getLogger(Attributes.class.getName());

    /**
     * B-Tree Header record
     */
    private BTHeaderRecord bthr;

    /**
     * The current file system.
     */
    private final HfsPlusFileSystem fs;

    /**
     * The attributes file data fork.
     */
    private final HfsPlusForkData attributesFile;

    /**
     * Creates a new attributes file for the give file system.
     *
     * @param fs the file system.
     * @throws IOException if an error occurs.
     */
    public Attributes(HfsPlusFileSystem fs) throws IOException {
        log.log(Level.TRACE, "Loading the attributes file B-Tree");
        this.fs = fs;
        SuperBlock sb = fs.getVolumeHeader();
        attributesFile = sb.getAttributesFile();

        if (!attributesFile.getExtent(0).isEmpty()) {
            ByteBuffer buffer = ByteBuffer.allocate(NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH +
                BTHeaderRecord.BT_HEADER_RECORD_LENGTH);
            attributesFile.read(fs, 0, buffer);
            buffer.rewind();
            byte[] data = ByteBufferUtils.toArray(buffer);
            log.log(Level.TRACE, "Load attributes node descriptor.");

            NodeDescriptor btnd = new NodeDescriptor(data, 0);
            log.log(Level.TRACE, btnd.toString());
            log.log(Level.TRACE, "Load attributes header record.");
            bthr = new BTHeaderRecord(data, NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH);
            log.log(Level.TRACE, bthr.toString());
        }
    }

    /**
     * Looks up an attribute in the attributes file.
     *
     * @param fileId the ID of the file to look up the attribute on.
     * @param attributeName the name of the attribute to lookup.
     * @return the leaf record, or possibly {code null}.
     * @throws IOException if an error occurs.
     */
    public AttributeData getAttribute(CatalogNodeId fileId, String attributeName) throws IOException {
        if (bthr == null) {
            return null;
        }

        return getAttribute(fileId, attributeName, bthr.getRootNode());
    }

    /**
     * Looks up an attribute in the attributes file.
     *
     * @param fileId the ID of the file to look up the attribute on.
     * @param attributeName the name of the attribute to lookup.
     * @param nodeNumber the index of node where the search begin.
     * @return the leaf record, or possibly {code null}.
     * @throws IOException if an error occurs.
     */
    public AttributeData getAttribute(CatalogNodeId fileId, String attributeName, long nodeNumber) throws IOException {
        if (attributesFile.getExtent(0).isEmpty()) {
            // No attributes
            return null;
        }

        LeafRecord leafRecord = null;
        int nodeSize = bthr.getNodeSize();
        ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
        attributesFile.read(fs, (nodeNumber * nodeSize), nodeData);
        nodeData.rewind();
        byte[] data = ByteBufferUtils.toArray(nodeData);
        NodeDescriptor nodeDescriptor = new NodeDescriptor(data, 0);

        if (nodeDescriptor.isIndexNode()) {
            AttributeIndexNode node = new AttributeIndexNode(data, nodeSize);
            IndexRecord[] records = node.findAll(new AttributeKey(fileId, attributeName));

            for (IndexRecord indexRecord : records) {
                AttributeData attributeData = getAttribute(fileId, attributeName, indexRecord.getIndex());
                if (attributeData != null) {
                    return attributeData;
                }
            }

        } else if (nodeDescriptor.isLeafNode()) {
            AttributeLeafNode node = new AttributeLeafNode(data, nodeSize);
            leafRecord = node.find(new AttributeKey(fileId, attributeName));
        }

        if (leafRecord == null) {
            return null;
        }

        long type = ByteUtil.readBeInt(leafRecord.getData(), 0) & 0xffff_ffffL;

        if (type == AttributeData.ATTRIBUTE_INLINE_DATA) {
            return new AttributeInlineData(leafRecord.getData(), 0);
        } else if (type == AttributeData.ATTRIBUTE_FORK_DATA) {
            return new AttributeForkData(fileId, leafRecord.getData(), 0);
        } else if (type == AttributeData.ATTRIBUTE_EXTENTS) {
            throw new UnsupportedOperationException();
        } else {
            log.log(Level.WARNING, "Invalid attribute record type: %d for leaf: %s".formatted(type, leafRecord));
            return null;
        }
    }
}
