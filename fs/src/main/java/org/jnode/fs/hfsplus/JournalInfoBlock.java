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

import vavi.util.ByteUtil;


public class JournalInfoBlock {

    /** Flag indicate that the journal is located in the volume itself. */
    public static final int IN_FS_MASK = 0x00000001;
    /**
     * Flag indicate that the journal located in another device. This flag is
     * not currently supported.
     */
    public static final int ION_OTHER_DEVICE_MASK = 0x00000002;
    /** Flag indicate that the journal header is invalid and must be initialized. */
    public static final int NEED_INITIALIZATION = 0x00000004;
    /** One-bits flag. See constants */
    private final int flag;
    /** Device where the journal is located if it is not in the volume itself. */
    private final int deviceSignature;
    /** journal start position on the volume */
    private final long offset;
    /** Size of the journal included header and buffer. */
    private final long size;

    public JournalInfoBlock(final byte[] src) {
        byte[] data = new byte[180];
        System.arraycopy(src, 0, data, 0, 180);
        flag = ByteUtil.readBeInt(data, 0);
        deviceSignature = ByteUtil.readBeInt(data, 4);
        offset = ByteUtil.readBeLong(data, 36);
        size = ByteUtil.readBeLong(data, 44);
    }

    public final String toString() {
        return "Journal : " + offset + "::" + size;
    }

    public int getFlag() {
        return flag;
    }

    public int getDeviceSignature() {
        return deviceSignature;
    }

    public long getOffset() {
        return offset;
    }

    public long getSize() {
        return size;
    }
}
