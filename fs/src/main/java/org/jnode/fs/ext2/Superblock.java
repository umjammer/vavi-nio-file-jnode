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
import org.jnode.fs.FileSystemException;
import vavi.util.ByteUtil;


/**
 * Ext2fs superblock
 * 
 * @author Andras Nagy
 */
public class Superblock {

    private static final Logger log = System.getLogger(Superblock.class.getName());

    public static final int SUPERBLOCK_LENGTH = 1024;

    // some constants for the fs creation

    /** one inode for 4KBs of data */
    private static final long BYTES_PER_INODE = 4096;
    /** 5% reserved for the superuser */
    private static final double RESERVED_BLOCKS_RATIO = 0.05;
    /** create the new fs with the sparse_super option */
    private static final boolean CREATE_WITH_SPARSE_SUPER = true;
    /** number of times to mount before check (check not yet implemented) */
    private static final int MAX_MOUNT_COUNT = 256;
    /** check every year (check not yet implemented) */
    private static final int CHECK_INTERVAL = 365 * 24 * 60 * 60;
    /** whatever */
    private static final long JNODE = 42;

    private final byte[] data;
    private boolean dirty;
    private Ext2FileSystem fs;

    public Superblock() {
        data = new byte[SUPERBLOCK_LENGTH];
    }

    public void read(byte[] src, Ext2FileSystem fs) throws FileSystemException {
        System.arraycopy(src, 0, data, 0, SUPERBLOCK_LENGTH);

        this.fs = fs;

        // check the magic :)
        if (getMagic() != 0xef53)
            throw new FileSystemException("Not ext2 superblock (" + getMagic() + ": bad magic)");

        setDirty(false);
    }

    public void create(BlockSize blockSize, Ext2FileSystem fs) throws IOException {
        this.fs = fs;
        setRevLevel(Ext2Constants.EXT2_DYNAMIC_REV);
        setMinorRevLevel(0);
        setMagic(0xef53);
        setCreatorOS(JNODE);

        // the number of inodes has to be <= than the number of blocks
        long bytesPerInode = (BYTES_PER_INODE >= blockSize.getSize()) ? BYTES_PER_INODE : blockSize.getSize();
        long size = fs.getApi().getLength();
        long blocks = size / blockSize.getSize();
        long inodes = size / bytesPerInode;
        setINodesCount(inodes);
        setBlocksCount(blocks);
        setRBlocksCount((long) (RESERVED_BLOCKS_RATIO * blocks));
        setDefResgid(0);
        setDefResuid(0);
        // actually sets the S_LOG_BLOCK_SIZE
        setBlockSize(blockSize);
        // set S_LOG_FRAG_SIZE
        setFragSize(blockSize);
        setFirstDataBlock(blockSize.getSize() == 1024 ? 1 : 0);

        // a block bitmap is 1 block long, so blockSize*8 blocks can be indexed by a bitmap
        // and thus be in a group
        long blocksPerGroup = (long) blockSize.getSize() << 3;
        setBlocksPerGroup(blocksPerGroup);
        setFragsPerGroup(blocksPerGroup);
        long groupCount = Ext2Utils.ceilDiv(blocks, blocksPerGroup);

        long inodesPerGroup = Ext2Utils.ceilDiv(inodes, groupCount);
        setINodesPerGroup(inodesPerGroup);

        // calculate the number of blocks reserved for metadata
        // first, set the sparse_super option (it affects this value)
        if (CREATE_WITH_SPARSE_SUPER)
            setFeatureROCompat(getFeatureROCompat() | Ext2Constants.EXT2_FEATURE_RO_COMPAT_SPARSE_SUPER);
        long sbSize = 1; // superblock is 1 block fixed
        long gdtSize = Ext2Utils.ceilDiv(groupCount * GroupDescriptor.GROUPDESCRIPTOR_LENGTH, blockSize
                        .getSize());
        long bbSize = 1; // block bitmap is 1 block fixed
        long ibSize = 1; // inode bitmap is 1 block fixed
        long inodeTableSize =
                Ext2Utils.ceilDiv(inodesPerGroup * INode.EXT2_GOOD_OLD_INODE_SIZE, blockSize.getSize());
        int groupsWithMetadata = 0;
        for (int i = 0; i < groupCount; i++)
            if (fs.groupHasDescriptors(i))
                groupsWithMetadata++;
        long metadataSize = (bbSize + ibSize + inodeTableSize) * groupCount + (sbSize + gdtSize) *
                        groupsWithMetadata;
        setFreeBlocksCount(blocks - metadataSize);
        setFirstInode(11);
        setFreeInodesCount(inodes - getFirstInode() + 1);

        setMTime(0);
        setWTime(0);
        setLastCheck(0);
        setCheckInterval(CHECK_INTERVAL);
        setMntCount(0);
        setMaxMntCount(MAX_MOUNT_COUNT);

        setState(Ext2Constants.EXT2_VALID_FS);
        setErrors(Ext2Constants.EXT2_ERRORS_DEFAULT);

        setINodeSize(INode.EXT2_GOOD_OLD_INODE_SIZE);

        setBlockGroupNr(0);

        // set the options SPARSE_SUPER and FILETYPE
        setFeatureCompat(0);
        setFeatureROCompat(Ext2Constants.EXT2_FEATURE_RO_COMPAT_SPARSE_SUPER);
        setFeatureIncompat(Ext2Constants.EXT2_FEATURE_INCOMPAT_FILETYPE);

        byte[] uuid = new byte[16];
        for (int i = 0; i < uuid.length; i++)
            uuid[i] = (byte) (Math.random() * 255);
        setUUID(uuid);

        setPreallocBlocks(8);
        setPreallocDirBlocks(0);

        log.log(Level.DEBUG, "SuperBlock.create(): getBlockSize(): " + getBlockSize());
    }

    /**
     * Update the superblock copies on the disk
     */
    public synchronized void update() throws IOException {
        if (isDirty()) {
            log.log(Level.DEBUG, "Updating superblock copies");
            byte[] oldData;

            // update the main copy
            if (getFirstDataBlock() == 0) {
                oldData = fs.getBlock(0);
                // the block size is an integer multiply of 1024, and if
                // getFirstDataBlock==0, it's
                // at least 2048 bytes
                System.arraycopy(data, 0, oldData, 1024, SUPERBLOCK_LENGTH);
            } else {
                oldData = fs.getBlock(getFirstDataBlock());
                System.arraycopy(data, 0, oldData, 0, SUPERBLOCK_LENGTH);
            }
            fs.writeBlock(getFirstDataBlock(), oldData, true);

            // update the other copies
            for (int i = 1; i < fs.getGroupCount(); i++) {
                // check if there is a superblock copy in the block group
                if (!fs.groupHasDescriptors(i))
                    continue;

                long blockNr = getFirstDataBlock() + i * getBlocksPerGroup();
                oldData = fs.getBlock(blockNr);
                setBlockGroupNr(i);
                // update the old contents with the new superblock
                System.arraycopy(data, 0, oldData, 0, SUPERBLOCK_LENGTH);
                fs.writeBlock(blockNr, oldData, true);
            }

            setBlockGroupNr(0);
            setDirty(false);
        }

    }

    // this field is only written during format (so no synchronization issues here)

    public long getINodesCount() {
        return ByteUtil.readLeInt(data, 0) & 0xffff_ffffL;
    }

    public void setINodesCount(long count) {
        Ext2Utils.set32(data, 0, count);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getBlocksCount() {
        return ByteUtil.readLeInt(data, 4) & 0xffff_ffffL;
    }

    public void setBlocksCount(long count) {
        Ext2Utils.set32(data, 4, count);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getRBlocksCount() {
        return ByteUtil.readLeInt(data, 8) & 0xffff_ffffL;
    }

    public void setRBlocksCount(long count) {
        Ext2Utils.set32(data, 8, count);
        setDirty(true);
    }

    public synchronized long getFreeBlocksCount() {
        return ByteUtil.readLeInt(data, 12) & 0xffff_ffffL;
    }

    public synchronized void setFreeBlocksCount(long count) {
        Ext2Utils.set32(data, 12, count);
        setDirty(true);
    }

    public synchronized long getFreeInodesCount() {
        return ByteUtil.readLeInt(data, 16) & 0xffff_ffffL;
    }

    public synchronized void setFreeInodesCount(long count) {
        Ext2Utils.set32(data, 16, count);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getFirstDataBlock() {
        return ByteUtil.readLeInt(data, 20) & 0xffff_ffffL;
    }

    public void setFirstDataBlock(long i) {
        Ext2Utils.set32(data, 20, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    private long getLogBlockSize() {
        return ByteUtil.readLeInt(data, 24) & 0xffff_ffffL;
    }

    private void setLogBlockSize(long i) {
        Ext2Utils.set32(data, 24, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getBlockSize() {
        return 1024 << getLogBlockSize();
    }

    public void setBlockSize(BlockSize size) {
        // setLogBlockSize((long) (Math.log(size) / Math.log(2) - 10));
        // Math.log() is buggy
        // TODO should we handle all these values for size or not ? from mke2fs
        //  man page, it seems NO.
        if (size.getSize() == 1024)
            setLogBlockSize(0);
        if (size.getSize() == 2048)
            setLogBlockSize(1);
        if (size.getSize() == 4096)
            setLogFragSize(2);
        if (size.getSize() == 8192)
            setLogFragSize(3);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    private long getLogFragSize() {
        return ByteUtil.readLeInt(data, 28) & 0xffff_ffffL;
    }

    private void setLogFragSize(long i) {
        Ext2Utils.set32(data, 28, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getFragSize() {
        if (getLogFragSize() > 0)
            return 1024 << getLogFragSize();
        else
            return 1024 >> -getLogFragSize();
    }

    public void setFragSize(BlockSize size) {
        // setLogFragSize((long) (Math.log(size) / Math.log(2)) - 10);
        // Math.log() is buggy
        // TODO should we handle all these values for size or not ? from mke2fs
        // man page, it seems NO.
        if (size.getSize() == 64)
            setLogFragSize(-4);
        if (size.getSize() == 128)
            setLogFragSize(-3);
        if (size.getSize() == 256)
            setLogBlockSize(-2);
        if (size.getSize() == 512)
            setLogBlockSize(-1);
        if (size.getSize() == 1024)
            setLogFragSize(0);
        if (size.getSize() == 2048)
            setLogFragSize(1);
        if (size.getSize() == 4096)
            setLogBlockSize(2);
        if (size.getSize() == 8192)
            setLogBlockSize(3);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getBlocksPerGroup() {
        return ByteUtil.readLeInt(data, 32) & 0xffff_ffffL;
    }

    public void setBlocksPerGroup(long i) {
        Ext2Utils.set32(data, 32, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getFragsPerGroup() {
        return ByteUtil.readLeInt(data, 36) & 0xffff_ffffL;
    }

    public void setFragsPerGroup(long i) {
        Ext2Utils.set32(data, 36, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getINodesPerGroup() {
        return ByteUtil.readLeInt(data, 40) & 0xffff_ffffL;
    }

    public void setINodesPerGroup(long i) {
        Ext2Utils.set32(data, 40, i);
        setDirty(true);
    }

    // this field is only written during mounting (so no synchronization issues here)

    public long getMTime() {
        return ByteUtil.readLeInt(data, 44) & 0xffff_ffffL;
    }

    public void setMTime(long time) {
        Ext2Utils.set32(data, 44, time);
        setDirty(true);
    }

    public synchronized long getWTime() {
        return ByteUtil.readLeInt(data, 48) & 0xffff_ffffL;
    }

    public synchronized void setWTime(long time) {
        Ext2Utils.set32(data, 48, time);
        setDirty(true);
    }

    // this field is only written during mounting (so no synchronization issues here)

    public int getMntCount() {
        return ByteUtil.readLeShort(data, 52) & 0xffff;
    }

    public void setMntCount(int i) {
        ByteUtil.writeLeShort((short) i, data, 52);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getMaxMntCount() {
        return ByteUtil.readLeShort(data, 54) & 0xffff;
    }

    public void setMaxMntCount(int i) {
        ByteUtil.writeLeShort((short) i, data, 54);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getMagic() {
        return ByteUtil.readLeShort(data, 56) & 0xffff;
    }

    public void setMagic(int i) {
        ByteUtil.writeLeShort((short) i, data, 56);
        setDirty(true);
    }

    public synchronized int getState() {
        return ByteUtil.readLeShort(data, 58) & 0xffff;
    }

    public synchronized void setState(int state) {
        ByteUtil.writeLeShort((short) state, data, 58);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getErrors() {
        return ByteUtil.readLeShort(data, 60) & 0xffff;
    }

    public void setErrors(int i) {
        ByteUtil.writeLeShort((short) i, data, 60);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getMinorRevLevel() {
        return ByteUtil.readLeShort(data, 62) & 0xffff;
    }

    public void setMinorRevLevel(int i) {
        ByteUtil.writeLeShort((short) i, data, 62);
        setDirty(true);
    }

    // this field is only written during filesystem check (so no synchronization issues here)

    public long getLastCheck() {
        return ByteUtil.readLeInt(data, 64) & 0xffff_ffffL;
    }

    public void setLastCheck(long i) {
        Ext2Utils.set32(data, 64, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getCheckInterval() {
        return ByteUtil.readLeInt(data, 68) & 0xffff_ffffL;
    }

    public void setCheckInterval(long i) {
        Ext2Utils.set32(data, 68, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getCreatorOS() {
        return ByteUtil.readLeInt(data, 72) & 0xffff_ffffL;
    }

    public void setCreatorOS(long i) {
        Ext2Utils.set32(data, 72, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getRevLevel() {
        return ByteUtil.readLeInt(data, 76) & 0xffff_ffffL;
    }

    public void setRevLevel(long i) {
        Ext2Utils.set32(data, 76, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getDefResuid() {
        return ByteUtil.readLeShort(data, 80) & 0xffff;
    }

    public void setDefResuid(int i) {
        ByteUtil.writeLeShort((short) i, data, 80);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getDefResgid() {
        return ByteUtil.readLeShort(data, 82) & 0xffff;
    }

    public void setDefResgid(int i) {
        ByteUtil.writeLeShort((short) i, data, 82);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getFirstInode() {
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            return ByteUtil.readLeInt(data, 84) & 0xffff_ffffL;
        else
            return 11;
    }

    public void setFirstInode(long i) {
        Ext2Utils.set32(data, 84, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getINodeSize() {
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            return ByteUtil.readLeShort(data, 88) & 0xffff;
        else
            return INode.EXT2_GOOD_OLD_INODE_SIZE;
    }

    public void setINodeSize(int i) {
        ByteUtil.writeLeShort((short) i, data, 88);
        setDirty(true);
    }

    // TODO what to return for old versions?

    public synchronized long getBlockGroupNr() {
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            return ByteUtil.readLeShort(data, 90) & 0xffff;
        else
            return 0;
    }

    public synchronized void setBlockGroupNr(int i) {
        ByteUtil.writeLeShort((short) i, data, 90);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getFeatureCompat() {
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            return ByteUtil.readLeInt(data, 92) & 0xffff_ffffL;
        else
            return 0;
    }

    public void setFeatureCompat(long i) {
        Ext2Utils.set32(data, 92, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getFeatureIncompat() {
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            return ByteUtil.readLeInt(data, 96) & 0xffff_ffffL;
        else
            return 0;
    }

    public void setFeatureIncompat(long i) {
        Ext2Utils.set32(data, 96, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public long getFeatureROCompat() {
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            return ByteUtil.readLeInt(data, 100) & 0xffff_ffffL;
        else
            return 0;
    }

    public void setFeatureROCompat(long i) {
        Ext2Utils.set32(data, 100, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public byte[] getUUID() {
        byte[] result = new byte[16];
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            System.arraycopy(data, 104, result, 0, 16);
        return result;
    }

    public void setUUID(byte[] uuid) {
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            System.arraycopy(uuid, 0, data, 104, 16);
        setDirty(true);
    }

    public String getVolumeName() {
        StringBuilder result = new StringBuilder();
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            for (int i = 0; i < 16; i++) {
                char c = (char) data[120 + i];
                if (c != 0)
                    result.append(c);
                else
                    break;
            }
        return result.toString();
    }

    public String getLastMounted() {
        StringBuilder result = new StringBuilder();
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            for (int i = 0; i < 64; i++) {
                char c = (char) data[136 + i];
                if (c != 0)
                    result.append(c);
                else
                    break;
            }
        return result.toString();
    }

    // not sure this is the correct byte-order for this field

    public long getAlgoBitmap() {
        if (getRevLevel() == Ext2Constants.EXT2_DYNAMIC_REV)
            return ByteUtil.readLeInt(data, 200) & 0xffff_ffffL;
        else
            return 11;
    }

    // this field is only written during format (so no synchronization issues here)

    public int getPreallocBlocks() {
        return data[204] & 0xff;
    }

    public void setPreallocBlocks(int i) {
        Ext2Utils.set8(data, 204, i);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues here)

    public int getPreallocDirBlocks() {
        return data[205] & 0xff;
    }

    public void setPreallocDirBlocks(int i) {
        Ext2Utils.set8(data, 205, i);
        setDirty(true);
    }

    public byte[] getJournalUUID() {
        byte[] result = new byte[16];
        System.arraycopy(data, 208, result, 0, 16);
        return result;
    }

    public long getJournalINum() {
        return ByteUtil.readLeInt(data, 224) & 0xffff_ffffL;
    }

    public long getJournalDev() {
        return ByteUtil.readLeInt(data, 228) & 0xffff_ffffL;
    }

    public long getLastOrphan() {
        return data[232] & 0xff;
    }

    /**
     * Gets the block number that contains the multi-mount protection (MMP) data.
     *
     * @return the block number.
     */
    public long getMultiMountProtectionBlock() {
        return ByteUtil.readLeLong(data, 360);
    }

    public long getBlocksPerFlex() {
        int logBlocksPerFlex = data[372] & 0xff;
        return 1L << logBlocksPerFlex;
    }

    /**
     * Checks whether the file system is using flexible block groups.
     *
     * @return {@code true} if using flexible block groups.
     */
    public boolean isUsingFlexibleBlockGroups() {
        return (getFeatureIncompat() & Ext2Constants.EXT4_FEATURE_INCOMPAT_FLEX_BG) ==
            Ext2Constants.EXT4_FEATURE_INCOMPAT_FLEX_BG;
    }

    /**
     * @return the SuperBlock's dirty flag
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * @param b is dirty or not
     */
    public void setDirty(boolean b) {
        dirty = b;
    }
}
