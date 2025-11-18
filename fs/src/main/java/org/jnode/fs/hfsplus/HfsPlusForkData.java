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

package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.fs.hfsplus.extent.ExtentKey;
import vavi.util.ByteUtil;


public class HfsPlusForkData {

    public static final int FORK_DATA_LENGTH = 80;
    private static final int EXTENT_OFFSET = 16;

    /**
     * The size in bytes of the valid data in the fork.
     */
    private final long totalSize;
    /** */
    private final long clumpSize;
    /**
     * The total of allocation blocks use by the extents in the fork.
     */
    private final long totalBlock;
    /**
     * The first eight extent descriptors for the fork.
     */
    private final ExtentDescriptor[] extents;
    /**
     * Overflow extents.
     */
    private ExtentDescriptor[] overflowExtents;

    /**
     * The catalog node ID that owns this fork.
     */
    private final CatalogNodeId cnid;

    /**
     * Indicates whether this is a data fork, or a resource fork.
     */
    private final boolean dataFork;

    /**
     * Create fork data from existing information.
     *
     * @param cnid the catalog node ID that owns this fork.
     * @param dataFork indicates whether this is a data fork, or a resource fork.
     * @param src the src
     * @param offset the offset
     */
    public HfsPlusForkData(CatalogNodeId cnid, boolean dataFork, final byte[] src, final int offset) {
        this.cnid = cnid;
        this.dataFork = dataFork;
        byte[] data = new byte[FORK_DATA_LENGTH];
        System.arraycopy(src, offset, data, 0, FORK_DATA_LENGTH);
        totalSize = ByteUtil.readBeLong(data, 0);
        clumpSize = ByteUtil.readBeInt(data, 8) & 0xffff_ffffL;
        totalBlock = ByteUtil.readBeInt(data, 12) & 0xffff_ffffL;
        extents = new ExtentDescriptor[8];
        for (int i = 0; i < 8; i++) {
            extents[i] = new ExtentDescriptor(data, EXTENT_OFFSET +
                    (i * ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH));
        }
    }

    /**
     * Create a new empty data-fork data.
     *
     * @param totalSize the total size
     * @param clumpSize the clump size
     * @param totalBlock the total block
     */
    public HfsPlusForkData(CatalogNodeId cnid, long totalSize, int clumpSize, int totalBlock) {
        this.cnid = cnid;
        this.dataFork = true;
        this.totalSize = totalSize;
        this.clumpSize = clumpSize;
        this.totalBlock = totalBlock;
        this.extents = new ExtentDescriptor[8];
        for (int i = 0; i < extents.length; i++) {
            extents[i] = new ExtentDescriptor();
        }
    }

    public byte[] write(byte[] dest, int destOffSet) {
        byte[] data = new byte[FORK_DATA_LENGTH];
        ByteUtil.writeBeLong(totalSize, data, 0);
        ByteUtil.writeBeInt((int) clumpSize, data, 8);
        ByteUtil.writeBeInt((int) totalBlock, data, 12);
        for (int i = 0; i < extents.length; i++) {
            extents[i].write(data, EXTENT_OFFSET + (i * ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH));
        }
        System.arraycopy(data, 0, dest, destOffSet, FORK_DATA_LENGTH);
        return dest;
    }

    public final String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Total size : ").append(totalSize).append("\n");
        s.append("Clump size : ").append(clumpSize).append("\n");
        s.append("Total Blocks : ").append(totalBlock).append("\n");
        for (int i = 0; i < extents.length; i++) {
            s.append("Extent[").append(i).append("]: ").append(extents[i].toString());
        }
        return s.toString();
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getClumpSize() {
        return clumpSize;
    }

    public long getTotalBlocks() {
        return totalBlock;
    }

    public ExtentDescriptor getExtent(int index) {
        return extents[index];
    }

    /**
     * Gets all extents to read data from.
     *
     * @param fileSystem the current file system.
     * @return the collection of extents.
     * @throws IOException if an error occurs.
     */
    public Collection<ExtentDescriptor> getAllExtents(HfsPlusFileSystem fileSystem) throws IOException {
        List<ExtentDescriptor> allExtents = new ArrayList<>();
        Collections.addAll(allExtents, extents);

        // Only check for overflow extents if the last non-overflow extent is in use
        if (!extents[7].isEmpty() && overflowExtents == null) {
            int forkType = dataFork ? ExtentKey.DATA_FORK : ExtentKey.RESOURCE_FORK;
            overflowExtents = fileSystem.getExtentOverflow().getOverflowExtents(new ExtentKey(forkType, 0, cnid, 0));
        }

        // Add the overflow extents if to exist
        if (overflowExtents != null) {
            Collections.addAll(allExtents, overflowExtents);
        }

        return allExtents;
    }

    /**
     * Read a block of data
     *
     * @param fileSystem the associated file system.
     * @param offset the offset to read from.
     * @param buffer the buffer to read into.
     * @throws java.io.IOException if an error occurs.
     */
    public void read(HfsPlusFileSystem fileSystem, long offset, ByteBuffer buffer) throws IOException {
        int blockSize = fileSystem.getVolumeHeader().getBlockSize();
        int limit = buffer.limit();
        int remaining = buffer.remaining();

        Collection<ExtentDescriptor> allExtents = getAllExtents(fileSystem);

        for (ExtentDescriptor extentDescriptor : allExtents) {
            if (remaining > 0 && !extentDescriptor.isEmpty()) {
                long length = extentDescriptor.getSize(blockSize);

                if (offset != 0 && length < offset) {
                    offset -= length;
                } else {
                    long firstOffset = extentDescriptor.getStartOffset(blockSize);

                    while (remaining > 0 && offset < length) {
                        int byteCount = Math.min(remaining, blockSize);
                        byteCount = Math.min(byteCount, (int) (length - offset));

                        buffer.limit(buffer.position() + byteCount);
                        fileSystem.getApi().read(firstOffset + offset, buffer);

                        offset += byteCount;
                        remaining -= byteCount;
                    }

                    offset = 0;
                }
            }
        }

        if (remaining > 0) {
            throw new IOException("Failed to read in all the data. cnid: %s offset: %d extents: %s".formatted(
                cnid, offset, allExtents));
        }

        // Reset the limit
        buffer.limit(limit);
    }

    /**
     *
     * @param index the index
     * @param desc the desc
     */
    public final void addDescriptor(int index, ExtentDescriptor desc) {
        extents[index] = desc;
    }

    public ExtentDescriptor[] getExtents() {
        return extents;
    }
}
