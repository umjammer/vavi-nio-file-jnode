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
import org.jnode.fs.FileSystemException;

/**
 * This class represents a part of the inode table (that which is contained
 * in one block group).
 * <p>
 * It provides methods for reading and writing the (already allocated) inodes.
 * </p>
 * An inode table contains just the inodes, with no extra metadata.
 * 
 * @author Andras Nagy
 */
public class INodeTable {

    private final int blockSize;
    final int blockCount;
    final Ext2FileSystem fs;
    /** the first block of the inode table */
    final int firstBlock;

    public INodeTable(Ext2FileSystem fs, int firstBlock) {
        this.fs = fs;
        this.firstBlock = firstBlock;
        blockSize = fs.getBlockSize();
        blockCount =
                (int) Ext2Utils.ceilDiv(
                        fs.getSuperblock().getINodesPerGroup() * fs.getSuperblock().getINodeSize(), blockSize);
    }

    public static int getSizeInBlocks(Ext2FileSystem fs) {
        return (int) Ext2Utils.ceilDiv(
                fs.getSuperblock().getINodesPerGroup() * fs.getSuperblock().getINodeSize(),
                fs.getBlockSize());
    }

    /**
     * get the <code>blockNo</code>th block from the beginning of the inode table
     * 
     * @param blockNo the blockNo
     * @return the contents of the block as a byte[]
     * @throws FileSystemException when an error occurs
     * @throws IOException when an error occurs
     */
    private byte[] getINodeTableBlock(long blockNo) throws FileSystemException, IOException {
        if (blockNo < blockCount)
            return fs.getBlock(firstBlock + blockNo);
        else
            throw new FileSystemException("Trying to get block #" + blockNo +
                    "of an inode table that only has " + blockCount + " blocks");
    }

    /**
     * Write the <code>blockNo</code>th block (from the beginning of the inode table)
     * 
     * @param data the data
     * @param blockNo the blockNo
     * @throws FileSystemException when an error occurs
     * @throws IOException when an error occurs
     */
    private void writeINodeTableBlock(byte[] data, int blockNo)
        throws FileSystemException, IOException {
        if (blockNo < blockCount)
            fs.writeBlock(firstBlock + blockNo, data, false);
        else
            throw new FileSystemException("Trying to write block #" + blockNo +
                    "of an inode table that only has " + blockCount + " blocks");
    }

    /**
     * Get the index-th inode from the inode table. (index is not an inode
     * number, it is just an index in the inode table)
     * <p>
     * For each inode table, only one instance of INodeTable exists, so it is
     * safe to synchronize to it
     */
    public synchronized byte[] getInodeData(int index) throws IOException {
        int iNodeSize = fs.getSuperblock().getINodeSize();
        byte[] data = new byte[iNodeSize];

        int indexCopied = 0;
        while (indexCopied < iNodeSize) {
            long blockNo = ((long) index * iNodeSize + indexCopied) / blockSize;
            int blockOffset = (index * iNodeSize + indexCopied) % blockSize;
            int copyLength = Math.min(blockSize - blockOffset, iNodeSize);
            System.arraycopy(getINodeTableBlock(blockNo), blockOffset, data, indexCopied, copyLength);
            indexCopied += copyLength;
        }
        return data;
    }

    /*
     * For each inode table, only one instance of INodeTable exists, so it is
     * safe to synchronize to it
     */
    public synchronized void writeInodeData(int index, byte[] data) throws IOException {
        int iNodeSize = fs.getSuperblock().getINodeSize();

        int indexCopied = 0;
        while (indexCopied < iNodeSize) {
            int blockNo = (index * iNodeSize + indexCopied) / blockSize;
            int blockOffset = (index * iNodeSize + indexCopied) % blockSize;
            int copyLength = Math.min(blockSize - blockOffset, iNodeSize);
            byte[] originalBlock = getINodeTableBlock(blockNo);
            System.arraycopy(data, indexCopied, originalBlock, blockOffset, copyLength);
            indexCopied += copyLength;
            writeINodeTableBlock(originalBlock, blockNo);
        }
    }
}
