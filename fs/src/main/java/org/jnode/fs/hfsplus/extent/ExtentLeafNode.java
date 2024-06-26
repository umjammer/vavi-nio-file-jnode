/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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

package org.jnode.fs.hfsplus.extent;

import java.util.LinkedList;
import java.util.List;

import org.jnode.fs.hfsplus.tree.AbstractLeafNode;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class ExtentLeafNode extends AbstractLeafNode<ExtentKey> {

    /**
     * Create a new node.
     *
     * @param descriptor the descriptor
     * @param nodeSize the nodeSize
     */
    public ExtentLeafNode(NodeDescriptor descriptor, final int nodeSize) {
        super(descriptor, nodeSize);
    }

    /**
     * Create node from existing data.
     *
     * @param nodeData the nodeData
     * @param nodeSize the nodeSize
     */
    public ExtentLeafNode(final byte[] nodeData, final int nodeSize) {
        super(nodeData, nodeSize);

    }

    @Override
    protected ExtentKey createKey(byte[] nodeData, int offset) {
        return new ExtentKey(nodeData, offset);
    }

    /**
     * Gets all overflow extents that match the given key.
     *
     * @param key the key to match.
     * @return the overflow extents.
     */
    public ExtentDescriptor[] getOverflowExtents(ExtentKey key) {
        List<ExtentDescriptor> overflowExtents = new LinkedList<>();

        for (LeafRecord record : findAll(key)) {
            for (
                int offset = 0; offset < record.getData().length; offset += ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH) {
                ExtentDescriptor descriptor = new ExtentDescriptor(record.getData(), offset);
                overflowExtents.add(descriptor);
            }
        }

        return overflowExtents.toArray(new ExtentDescriptor[0]);
    }
}
