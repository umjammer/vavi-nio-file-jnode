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

package org.jnode.fs.hfsplus.tree;

import java.util.LinkedList;
import java.util.List;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;

public abstract class AbstractLeafNode<K extends Key> extends AbstractNode<K, LeafRecord> {

    private static final Logger log = System.getLogger(AbstractLeafNode.class.getName());

    /**
     * Create a new node.
     *
     * @param descriptor the descriptor
     * @param nodeSize the nodeSize
     */
    public AbstractLeafNode(NodeDescriptor descriptor, final int nodeSize) {
        super(descriptor, nodeSize);
    }

    /**
     * Create node from existing data.
     *
     * @param nodeData the nodeData
     * @param nodeSize the nodeSize
     */
    public AbstractLeafNode(final byte[] nodeData, final int nodeSize) {
        super(nodeData, nodeSize);
    }

    @Override
    protected LeafRecord createRecord(Key key, byte[] nodeData, int offset, int recordSize) {
        return new LeafRecord(key, nodeData, offset, recordSize);
    }

    public final LeafRecord[] findAll(K key) {
        List<LeafRecord> list = new LinkedList<>();
        for (LeafRecord record : records) {
            log.log(Level.DEBUG, "Record: " + record.toString() + " Key: " + key);
            @SuppressWarnings("unchecked")
            K recordKey = (K) record.getKey();
            if (recordKey != null && recordKey.equals(key)) {
                list.add(record);
            }
        }
        return list.toArray(new LeafRecord[0]);
    }
}
