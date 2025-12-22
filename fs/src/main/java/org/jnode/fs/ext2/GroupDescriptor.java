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

package org.jnode.fs.ext2;

import java.io.IOException;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;

import vavi.util.ByteUtil;


/**
 * @author Andras Nagy
 * 
 */
public class GroupDescriptor {

    private static final Logger log = System.getLogger(GroupDescriptor.class.getName());

    public static final int GROUPDESCRIPTOR_LENGTH = 32;

    private final byte[] data;
    private Ext2FileSystem fs;
    private int groupNr;
    private boolean dirty;

    public GroupDescriptor() {
        data = new byte[GROUPDESCRIPTOR_LENGTH];
    }

    /*
     * create() and read() precedes any access to the inners of the group
     * descriptor, so no synchronization is needed
     */
    public void read(int groupNr, Ext2FileSystem fs) throws IOException {
        // read the group descriptors from the main copy in block group 0
        long baseBlock = fs.getSuperblock().getFirstDataBlock() + 1;
        long blockOffset = ((long) groupNr * GROUPDESCRIPTOR_LENGTH) / fs.getBlockSize();
        long offset = ((long) groupNr * GROUPDESCRIPTOR_LENGTH) % fs.getBlockSize();
        byte[] blockData = fs.getBlock(baseBlock + blockOffset);
        System.arraycopy(blockData, (int) offset, data, 0, GROUPDESCRIPTOR_LENGTH);
        this.groupNr = groupNr;
        this.fs = fs;
        setDirty(false);
    }

    /*
     * create() and read() precedes any access to the inners of the group
     * descriptor, so no synchronization is needed
     */
    public void create(int groupNr, Ext2FileSystem fs) {
        this.fs = fs;
        this.groupNr = groupNr;

        long desc; // the length of the superblock and group descriptor copies
                    // in the block group
        if (!fs.groupHasDescriptors(groupNr))
            desc = 0;
        else
            desc = 1 + /* superblock */
                    Ext2Utils.ceilDiv((long) fs.getGroupCount() * GroupDescriptor.GROUPDESCRIPTOR_LENGTH,
                            fs.getBlockSize()); /* GDT */
        Superblock superblock = fs.getSuperblock();
        setBlockBitmap(superblock.getFirstDataBlock() + groupNr * superblock.getBlocksPerGroup() + desc);

        setInodeBitmap(getBlockBitmap() + 1);
        setInodeTable(getBlockBitmap() + 2);

        int iNodeSize = fs.getSuperblock().getINodeSize();
        long inodeTableSize = Ext2Utils.ceilDiv(superblock.getINodesPerGroup() * iNodeSize, fs.getBlockSize());
        long blockCount;
        if (groupNr == fs.getGroupCount() - 1)
            blockCount = superblock.getBlocksCount() -
                superblock.getBlocksPerGroup() * (fs.getGroupCount() - 1) - 
                superblock.getFirstDataBlock();
        else
            blockCount = superblock.getBlocksPerGroup();

        setFreeBlocksCount((int) (blockCount - desc /* superblock copy, GDT copies */
                - 2 /* block and inode bitmaps */
                - inodeTableSize)); /* inode table */

        if (groupNr == 0)
            setFreeInodesCount((int) (superblock.getINodesPerGroup() -
                    superblock.getFirstInode() + 1));
        else
            setFreeInodesCount((int) (superblock.getINodesPerGroup()));

        setUsedDirsCount(0);
    }

    /**
     * Update all copies of a single group descriptor. (GroupDescriptors are
     * duplicated in some (or all) block groups: if a GroupDescriptor changes,
     * all copies have to be changed.)
     * 
     * The method is synchronized with all methods that modify the group
     * descriptor (to "this") to ensure that it is not modified until all copies
     * are written to disk
     */
    protected synchronized void updateGroupDescriptor() throws IOException {
        if (isDirty()) {
            log.log(Level.DEBUG, "Updating groupDescriptor copies");
            Superblock superblock = fs.getSuperblock();
            for (int i = 0; i < fs.getGroupCount(); i++) {
                // check if there is a group descriptor table copy in the block group
                if (!fs.groupHasDescriptors(i))
                    continue;

                long block = superblock.getFirstDataBlock() + 1 + superblock.getBlocksPerGroup() * i; 
                long pos = (long) groupNr * GROUPDESCRIPTOR_LENGTH;
                block += pos / fs.getBlockSize();
                long offset = pos % fs.getBlockSize();
                byte[] blockData = fs.getBlock(block);
                // update the block with the new group descriptor
                System.arraycopy(data, 0, blockData, (int) offset, GROUPDESCRIPTOR_LENGTH);
                fs.writeBlock(block, blockData, true);
            }
            setDirty(false);
        }
    }

    public int size() {
        return GROUPDESCRIPTOR_LENGTH;
    }

    // this field is only written during format (so no synchronization issues here)
    public long getBlockBitmap() {
        return ByteUtil.readLeInt(data, 0) & 0xffff_ffffL;
    }

    public void setBlockBitmap(long l) {
        Ext2Utils.set32(data, 0, l);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)
    public long getInodeBitmap() {
        return ByteUtil.readLeInt(data, 4) & 0xffff_ffffL;
    }

    public void setInodeBitmap(long l) {
        Ext2Utils.set32(data, 4, l);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)
    public long getInodeTable() {
        return ByteUtil.readLeInt(data, 8) & 0xffff_ffffL;
    }

    public void setInodeTable(long l) {
        Ext2Utils.set32(data, 8, l);
        setDirty(true);
    }

    public synchronized int getFreeBlocksCount() {
        return ByteUtil.readLeShort(data, 12) & 0xffff;
    }

    public synchronized void setFreeBlocksCount(int count) {
        ByteUtil.writeLeShort((short) count, data, 12);
        setDirty(true);
    }

    public synchronized int getFreeInodesCount() {
        return ByteUtil.readLeShort(data, 14) & 0xffff;
    }

    public synchronized void setFreeInodesCount(int count) {
        ByteUtil.writeLeShort((short) count, data, 14);
        setDirty(true);
    }

    public synchronized int getUsedDirsCount() {
        return ByteUtil.readLeShort(data, 16) & 0xffff;
    }

    public synchronized void setUsedDirsCount(int count) {
        ByteUtil.writeLeShort((short) count, data, 16);
        setDirty(true);
    }

    /**
     * @return the dirty flag for the descriptor
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * @param b the b
     */
    public void setDirty(boolean b) {
        dirty = b;
    }
}
