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

package org.jnode.fs.hfsplus.catalog;

import java.lang.System.Logger;
import org.jnode.fs.hfsplus.tree.AbstractLeafNode;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class CatalogLeafNode extends AbstractLeafNode<CatalogKey> {
    private static final Logger log = System.getLogger(CatalogLeafNode.class.getName());

    /**
     * Create a new node.
     *
     * @param descriptor the descriptor
     * @param nodeSize the nodeSize
     */
    public CatalogLeafNode(NodeDescriptor descriptor, final int nodeSize) {
        super(descriptor, nodeSize);
    }

    /**
     * Create node from existing data.
     *
     * @param nodeData the nodeData
     * @param nodeSize the nodeSize
     */
    public CatalogLeafNode(final byte[] nodeData, final int nodeSize) {
        super(nodeData, nodeSize);

    }

    @Override
    protected CatalogKey createKey(byte[] nodeData, int offset) {
        return new CatalogKey(nodeData, offset);
    }

}
