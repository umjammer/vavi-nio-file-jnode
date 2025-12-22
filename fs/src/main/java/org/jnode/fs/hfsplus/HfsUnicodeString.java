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


public class HfsUnicodeString {

    /**
     * Length of string in characters.
     */
    private int length;

    private final String string;

    /**
     * Create HFSUnicodeString from existing data.
     *
     * @param src    byte array contains data.
     * @param offset start of data in the array.
     */
    public HfsUnicodeString(final byte[] src, final int offset) {
        length = ByteUtil.readBeShort(src, offset) & 0xffff;
        byte[] data = new byte[2 + (length * 2)];
        System.arraycopy(src, offset, data, 0, 2);
        length = ByteUtil.readBeShort(data, 0) & 0xffff;
        data = new byte[length * 2];
        System.arraycopy(src, offset + 2, data, 0, length * 2);
        char[] result = new char[length];
        for (int i = 0; i < length; ++i) {
            result[i] = (char) (ByteUtil.readBeShort(data, i * 2) & 0xffff);
        }
        string = new String(result);
    }

    /**
     * @param string the string
     */
    public HfsUnicodeString(String string) {
        this.string = string;
        this.length = string.length();
    }

    public final int getLength() {
        return length;
    }

    public final String getUnicodeString() {
        return string;
    }

    public final byte[] getBytes() {
        char[] result = new char[length];
        string.getChars(0, length, result, 0);
        byte[] name = new byte[length * 2];
        for (int i = 0; i < length; ++i) {
            ByteUtil.writeBeShort((short) result[i], name, i * 2);
        }
        byte[] data = new byte[(length * 2) + 2];
        ByteUtil.writeBeShort((short) length, data, 0);
        System.arraycopy(name, 0, data, 2, name.length);
        return data;
    }

    @Override
    public String toString() {
        return string;
    }
}
