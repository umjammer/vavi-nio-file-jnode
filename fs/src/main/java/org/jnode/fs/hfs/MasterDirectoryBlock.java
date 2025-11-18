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

package org.jnode.fs.hfs;

import vavi.util.ByteUtil;


/**
 * An HFS master directory block (MDB).
 *
 * @author Luke Quinane
 */
public class MasterDirectoryBlock {
    /**
     * The length of this structure.
     */
    public static final int LENGTH = 162;

    /**
     * The HFS MDB signature 'BD'.
     */
    public static final int HFS_MDB_SIGNATURE = 0x4244;

    /**
     * The HFS+ embedded volume signature 'H+'.
     */
    public static final int HFSPLUS_EMBEDDED_SIGNATURE = 0x482B;

    /**
     * The mask for MDB attributes.
     */
    public static final int ATTRIBUTES_MASK = 0x8380;

    /**
     * The data.
     */
    private final byte[] data;

    /**
     * Creates a new MDB.
     *
     * @param data the data to read from.
     */
    public MasterDirectoryBlock(byte[] data) {
        this.data = data;
    }

    /**
     * Gets the volume signature (drSigWord)
     *
     * @return the signature.
     */
    public int getSignature() {
        return ByteUtil.readBeShort(data, 0) & 0xffff;
    }

    /**
     * Gets the volume attributes (drAtrb)
     *
     * @return the attributes.
     */
    public int getAttributes() {
        return ByteUtil.readBeShort(data, 0xa) & 0xffff;
    }

    /**
     * Gets the allocation block size (drAlBlkSiz)
     *
     * @return the allocation block size.
     */
    public long getAllocationBlockSize() {
        return ByteUtil.readBeInt(data, 0x14) & 0xffff_ffffL;
    }

    /**
     * Gets the first allocation block (drAlBlSt)
     *
     * @return the first allocation block.
     */
    public int getAllocationBlockStart() {
        return ByteUtil.readBeShort(data, 0x1c) & 0xffff;
    }

    /**
     * Gets the embedded signature (drEmbedSigWord)
     *
     * @return the signature.
     */
    public int getEmbeddedSignature() {
        return ByteUtil.readBeShort(data, 0x7c) & 0xffff;
    }

    /**
     * Gets the embedded volume start block (drEmbedExtent)
     *
     * @return the start block.
     */
    public int getEmbeddedVolumeStartBlock() {
        return ByteUtil.readBeShort(data, 0x7e) & 0xffff;
    }

    /**
     * Gets the embedded volume block count (drEmbedExtent)
     *
     * @return the block count.
     */
    public int getEmbeddedVolumeBlockCount() {
        return ByteUtil.readBeShort(data, 0x80) & 0xffff;
    }
}
