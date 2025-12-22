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

package org.jnode.fs.fat;

import vavi.util.ByteUtil;


/**
 * @author gbin
 */
public class FatLfnDirEntry extends FatBasicDirEntry {
    /**
     * @param dir the dir
     */
    public FatLfnDirEntry(AbstractDirectory dir) {
        super(dir);
    }

    /**
     * @param dir the dir
     * @param src the src
     * @param offset the offset
     */
    public FatLfnDirEntry(AbstractDirectory dir, byte[] src, int offset) {
        super(dir, src, offset);
    }

    public FatLfnDirEntry(AbstractDirectory dir, String subName, int ordinal, byte checkSum,
            boolean isLast) {
        super(dir);
        char[] unicodeChar = new char[13];
        subName.getChars(0, subName.length(), unicodeChar, 0);
        if (isLast) {
            rawData[0] = (byte) (ordinal + (1 << 6)); // set the 6th security ending bit
        } else {
            rawData[0] = (byte) ordinal;
        }

        ByteUtil.writeLeShort((short) unicodeChar[0], rawData, 1);
        ByteUtil.writeLeShort((short) unicodeChar[1], rawData, 3);
        ByteUtil.writeLeShort((short) unicodeChar[2], rawData, 5);
        ByteUtil.writeLeShort((short) unicodeChar[3], rawData, 7);
        ByteUtil.writeLeShort((short) unicodeChar[4], rawData, 9);
        rawData[11] = (byte) 0x0f; // this is the hidden attribute tag for lfn
        rawData[12] = (byte) 0; // reserved
        rawData[13] = (byte) (int) checkSum; // checksum
        ByteUtil.writeLeShort((short) unicodeChar[5], rawData, 14);
        ByteUtil.writeLeShort((short) unicodeChar[6], rawData, 16);
        ByteUtil.writeLeShort((short) unicodeChar[7], rawData, 18);
        ByteUtil.writeLeShort((short) unicodeChar[8], rawData, 20);
        ByteUtil.writeLeShort((short) unicodeChar[9], rawData, 22);
        ByteUtil.writeLeShort((short) unicodeChar[10], rawData, 24);
        // sector... unused
        ByteUtil.writeLeShort((short) 0, rawData, 26);
        ByteUtil.writeLeShort((short) unicodeChar[11], rawData, 28);
        ByteUtil.writeLeShort((short) unicodeChar[12], rawData, 30);

    }

    public byte getOrdinal() {
        return (byte) (rawData[0] & 0xff);
    }

    public byte getCheckSum() {
        return (byte) (rawData[13] & 0xff);
    }

    public String getSubstring() {
        char[] unicodechar = new char[13];
        unicodechar[0] = (char) (ByteUtil.readLeShort(rawData, 1) & 0xffff);
        unicodechar[1] = (char) (ByteUtil.readLeShort(rawData, 3) & 0xffff);
        unicodechar[2] = (char) (ByteUtil.readLeShort(rawData, 5) & 0xffff);
        unicodechar[3] = (char) (ByteUtil.readLeShort(rawData, 7) & 0xffff);
        unicodechar[4] = (char) (ByteUtil.readLeShort(rawData, 9) & 0xffff);
        unicodechar[5] = (char) (ByteUtil.readLeShort(rawData, 14) & 0xffff);
        unicodechar[6] = (char) (ByteUtil.readLeShort(rawData, 16) & 0xffff);
        unicodechar[7] = (char) (ByteUtil.readLeShort(rawData, 18) & 0xffff);
        unicodechar[8] = (char) (ByteUtil.readLeShort(rawData, 20) & 0xffff);
        unicodechar[9] = (char) (ByteUtil.readLeShort(rawData, 22) & 0xffff);
        unicodechar[10] = (char) (ByteUtil.readLeShort(rawData, 24) & 0xffff);
        unicodechar[11] = (char) (ByteUtil.readLeShort(rawData, 28) & 0xffff);
        unicodechar[12] = (char) (ByteUtil.readLeShort(rawData, 30) & 0xffff);
        int index = 0;
        while (index < 13 && unicodechar[index] != '\0')
            index++;
        return new String(unicodechar).substring(0, index);
    }

    public String toString() {
        return "LFN ordinal " + getOrdinal() + " subString = " + getSubstring() + "CheckSum = " +
                getCheckSum();
    }
}
