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

import vavi.util.ByteUtil;


public class BTHeaderRecord {

    public static final int KEY_COMPARE_TYPE_CASE_FOLDING = 0xCF;
    /**
     * B-Tree was not closed correctly and need check for consistency.
     */
    public static final int BT_BAD_CLOSE_MASK = 0x00000001;
    public static final int BT_BIG_KEYS_MASK = 0x00000002;
    public static final int BT_VARIABLE_INDEX_KEYS_MASK = 0x00000004;

    public static final int BT_TYPE_HFS = 0;
    public static final int BT_TYPE_USER = 128;
    public static final int BT_TYPE_RESERVED = 256;

    public static final int BT_HEADER_RECORD_LENGTH = 106;
    /**
     * The depth of the current B-Tree.
     */
    private final int treeDepth;
    /**
     * The root node number
     */
    private final long rootNode;
    /**
     * The number of records contains in all leaf nodes.
     */
    private final long leafRecords;
    /**
     * The number of the first leaf node. This may be zero.
     */
    private final long firstLeafNode;
    /**
     * The number of the last leaf node. This may be zero.
     */
    private final long lastLeafNode;
    /**
     * The size in bytes of a node.
     */
    private final int nodeSize;
    /**
     * The maximum length of a key.
     */
    private final int maxKeyLength;
    /**
     * The total number of free or used nodes in the B-Tree.
     */
    private final long totalNodes;
    /**
     * The number of free node in the B-Tree.
     */
    private final long freeNodes;
    /**
     * Ignore for HFS+, clumpSize field from {@code HFSPlusForkData} used
     * instead.
     */
    private final long clumpSize;
    /**
     * The type of the B-Tree.
     */
    private final int treeType;
    /**
     * Ignore in HFS+, should be threat as reserved.
     */
    private final int keyCompareType;
    /**
     * Various attributes of the B-Tree.
     */
    private final long attributes;

    public BTHeaderRecord(int treeDepth, int rootNode, int leafRecords, int firstLeafNode,
                          int lastLeafNode, int nodeSize, int maxKeyLength, int totalNodes, int freeNodes,
                          int clumpSize, int treeType, int keyCompareType, int attributes) {
        this.treeDepth = treeDepth;
        this.rootNode = rootNode;
        this.leafRecords = leafRecords;
        this.firstLeafNode = firstLeafNode;
        this.lastLeafNode = lastLeafNode;
        this.nodeSize = nodeSize;
        this.maxKeyLength = maxKeyLength;
        this.totalNodes = totalNodes;
        this.freeNodes = freeNodes;
        this.clumpSize = clumpSize;
        this.treeType = treeType;
        this.keyCompareType = keyCompareType;
        this.attributes = attributes;
    }

    public BTHeaderRecord(final byte[] src, int offset) {
        byte[] data = new byte[BT_HEADER_RECORD_LENGTH];
        System.arraycopy(src, offset, data, 0, BT_HEADER_RECORD_LENGTH);
        treeDepth = ByteUtil.readBeShort(data, 0) & 0xffff;
        rootNode = ByteUtil.readBeInt(data, 2) & 0xffff_ffffL;
        leafRecords = ByteUtil.readBeInt(data, 6) & 0xffff_ffffL;
        firstLeafNode = ByteUtil.readBeInt(data, 10) & 0xffff_ffffL;
        lastLeafNode = ByteUtil.readBeInt(data, 14) & 0xffff_ffffL;
        nodeSize = ByteUtil.readBeShort(data, 18) & 0xffff;
        maxKeyLength = ByteUtil.readBeShort(data, 20) & 0xffff;
        totalNodes = ByteUtil.readBeInt(data, 22) & 0xffff_ffffL;
        freeNodes = ByteUtil.readBeInt(data, 26) & 0xffff_ffffL;
        // UInt16 reserved1 - offset 30
        clumpSize = ByteUtil.readBeInt(data, 32) & 0xffff_ffffL;
        treeType = data[36] & 0xff;
        keyCompareType = data[37] & 0xff;
        attributes = ByteUtil.readBeInt(data, 38) & 0xffff_ffffL;
    }

    public byte[] getBytes() {
        byte[] data = new byte[BT_HEADER_RECORD_LENGTH];
        ByteUtil.writeBeShort((short) treeDepth, data, 0);
        ByteUtil.writeBeInt((int) rootNode, data, 2);
        ByteUtil.writeBeInt((int) leafRecords, data, 6);
        ByteUtil.writeBeInt((int) firstLeafNode, data, 10);
        ByteUtil.writeBeInt((int) lastLeafNode, data, 14);
        ByteUtil.writeBeShort((short) nodeSize, data, 18);
        ByteUtil.writeBeShort((short) maxKeyLength, data, 20);
        ByteUtil.writeBeInt((int) totalNodes, data, 22);
        ByteUtil.writeBeInt((int) freeNodes, data, 26);
        ByteUtil.writeBeInt((int) clumpSize, data, 32);
        data[36] = (byte) treeType;
        data[37] = (byte) keyCompareType;
        ByteUtil.writeBeInt((int) attributes, data, 38);
        return data;
    }

    public final String toString() {
        return ("Root node:  " + getRootNode() + "\n" + "First leaf: " + getFirstLeafNode() + "\n" +
            "Last leaf:  " + getLastLeafNode() + "\n" + "node size:  " + getNodeSize() + "\n");
    }

    public int getTreeDepth() {
        return treeDepth;
    }

    public long getRootNode() {
        return rootNode;
    }

    public long getLeafRecords() {
        return leafRecords;
    }

    public long getFirstLeafNode() {
        return firstLeafNode;
    }

    public long getLastLeafNode() {
        return lastLeafNode;
    }

    public int getNodeSize() {
        return nodeSize;
    }

    public int getMaxKeyLength() {
        return maxKeyLength;
    }

    public long getTotalNodes() {
        return totalNodes;
    }

    public long getFreeNodes() {
        return freeNodes;
    }

    public long getClumpSize() {
        return clumpSize;
    }

    public int getTreeType() {
        return treeType;
    }

    public int getKeyCompareType() {
        return keyCompareType;
    }

    public long getAttributes() {
        return attributes;
    }
}
