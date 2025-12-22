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

package org.jnode.fs.ext4;

import vavi.util.ByteUtil;


/**
 * An ext4 extent object.
 *
 * @author Luke Quinane
 */
public class Extent {

    /**
     * The length of an extent.
     */
    public static final int EXTENT_LENGTH = 12;

    /**
     * The data for the extent.
     */
    private final byte[] data;

    /**
     * Create an extent object.
     *
     * @param data the data for the extent.
     */
    public Extent(byte[] data) {
        this.data = new byte[EXTENT_LENGTH];
        System.arraycopy(data, 0, this.data, 0, EXTENT_LENGTH);

        // Safety check
        if (getStartHigh() != 0) {
            throw new UnsupportedOperationException("Extents that use the high bits aren't supported yet");
        }
    }

    public long getBlockIndex() {
        return ByteUtil.readLeInt(data, 0) & 0xffff_ffffL;
    }

    public int getBlockCount() {
        return ByteUtil.readLeShort(data, 4) & 0xffff;
    }

    public long getStartLow() {
        return ByteUtil.readLeInt(data, 8) & 0xffff_ffffL;
    }

    public int getStartHigh() {
        return ByteUtil.readLeShort(data, 6) & 0xffff;
    }

    @Override
    public String toString() {
        return "Extent: blockindex:%d count:%d start(low:%d high:%d)".formatted(getBlockIndex(), getBlockCount(),
                             getStartLow(), getStartHigh());
    }
}
