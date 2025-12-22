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

import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import vavi.util.ByteUtil;


public class ExtentKey extends AbstractKey {

    public static final byte DATA_FORK = (byte) 0x00;
    public static final byte RESOURCE_FORK = (byte) 0xFF;
    public static final int KEY_LENGTH = 12;
    public static final int MAXIMUM_KEY_LENGTH = 10;

    private final int forkType;
    private final int pad;
    private final CatalogNodeId fileId;
    private final long startBlock;

    /**
     * @param src the src
     * @param offset the offset
     */
    public ExtentKey(final byte[] src, final int offset) {
        byte[] ek = new byte[KEY_LENGTH];
        System.arraycopy(src, offset, ek, 0, KEY_LENGTH);
        // TODO Understand why the +2 is necessary
        keyLength = (ByteUtil.readBeShort(ek, 0) & 0xffff) + 2;
        forkType = ek[2] & 0xff;
        pad = ek[3] & 0xff;
        fileId = new CatalogNodeId(ek, 4);
        startBlock = ByteUtil.readBeInt(ek, 8) & 0xffff_ffffL;
    }

    /**
     * @param forkType the forkType
     * @param pad the pad
     * @param fileId the fileId
     * @param startBlock the startBlock
     */
    public ExtentKey(int forkType, int pad, CatalogNodeId fileId, int startBlock) {
        super();
        this.forkType = forkType;
        this.pad = pad;
        this.fileId = fileId;
        this.startBlock = startBlock;
    }

    @Override
    public final int compareTo(final Key key) {
        int res = -1;
        if (key instanceof ExtentKey compareKey) {
            res = fileId.compareTo(compareKey.getFileId());
            if (res == 0) {
                res = compareForkType(compareKey.getForkType());
                if (res == 0) {
                    return compareStartBlock(compareKey.getStartBlock());
                }
            }
        }
        return res;
    }

    @Override
    public int hashCode() {
        return 73 ^ fileId.hashCode() * forkType + (int) startBlock;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExtentKey otherKey)) {
            return false;
        }

        return
            fileId.getId() == otherKey.fileId.getId() &&
                forkType == otherKey.forkType;
    }

    @Override
    public byte[] getBytes() {
        byte[] data = new byte[this.getKeyLength()];
        return data;
    }

    private int compareForkType(int fork) {
        Integer currentForkType = forkType;
        Integer forkType = fork;
        return currentForkType.compareTo(forkType);
    }

    private int compareStartBlock(long block) {
        Long currentStartBlock = startBlock;
        Long startBlock = block;
        return currentStartBlock.compareTo(startBlock);
    }

    public int getForkType() {
        return forkType;
    }

    public int getPad() {
        return pad;
    }

    public CatalogNodeId getFileId() {
        return fileId;
    }

    public long getStartBlock() {
        return startBlock;
    }

    @Override
    public final String toString() {
        return "[%s type:%s start:%d]".formatted(fileId, forkType == DATA_FORK ? "data" : "resource", startBlock);
    }
}
