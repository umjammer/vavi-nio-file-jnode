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

import java.lang.System.Logger.Level;
import java.lang.System.Logger;

import vavi.util.ByteUtil;


/**
 * <description>
 * 
 * @author epr
 * @author Fabien DUMINY
 */
public class FatUtils {

    private static final Logger log = System.getLogger(FatUtils.class.getName());

    public static final int FIRST_CLUSTER = 2;

    /**
     * Gets the offset (in bytes) of the fat with the given index
     * 
     * @param bs the bs
     * @param fatNr (0..)
     * @return long
     */
    public static long getFatOffset(BootSector bs, int fatNr) {
        long sectSize = bs.getBytesPerSector();
        long sectsPerFat = bs.getSectorsPerFat();
        long resSects = bs.getNrReservedSectors();

        long offset = resSects * sectSize;
        long fatSize = sectsPerFat * sectSize;

        offset += fatNr * fatSize;

        return offset;
    }

    /**
     * Gets the offset (in bytes) of the root directory with the given index
     * 
     * @param bs the bs
     * @return long
     */
    public static long getRootDirOffset(BootSector bs) {
        long sectSize = bs.getBytesPerSector();
        long sectsPerFat = bs.getSectorsPerFat();
        int fats = bs.getNrFats();

        long offset = getFatOffset(bs, 0);

        offset += fats * sectsPerFat * sectSize;

        return offset;
    }

    /**
     * Gets the offset of the data (file) area
     * 
     * @param bs the bs
     * @return long
     */
    public static long getFilesOffset(BootSector bs) {
        long offset = getRootDirOffset(bs);

        offset += bs.getNrRootDirEntries() * 32L;

        return offset;
    }

    /**
     * Return the name (without extension) of a full file name
     * 
     * @param nameExt the nameExt
     * @return the name part
     */
    public static String splitName(String nameExt) {
        int i = nameExt.indexOf('.');
        if (i < 0) {
            return nameExt;
        } else {
            return nameExt.substring(0, i);
        }
    }

    /**
     * Return the extension (without name) of a full file name
     * 
     * @param nameExt the nameExt
     * @return the extension part
     */
    public static String splitExt(String nameExt) {
        int i = nameExt.indexOf('.');
        if (i < 0) {
            return "";
        } else {
            return nameExt.substring(i + 1);
        }
    }

    /**
     * Normalize full file name in DOS 8.3 format from the name and the ext
     * 
     * @param name a DOS 8 name
     * @param ext a DOS 3 extension
     * @return the normalized DOS 8.3 name
     */
    public static String normalizeName(String name, String ext) {
        if (!ext.isEmpty()) {
            return (name + "." + ext).toUpperCase();
        } else {
            return name.toUpperCase();
        }
    }

    /**
     * Normalize full file name in DOS 8.3 format from the given full name
     * 
     * @param nameExt a DOS 8.3 name + extension
     * @return the normalized DOS 8.3 name
     */
    public static String normalizeName(String nameExt) {
        if (nameExt.equals("."))
            return nameExt;

        if (nameExt.equals(".."))
            return nameExt;

        return normalizeName(splitName(nameExt), splitExt(nameExt));
    }

    public static void checkValidName(String name) {
        checkString(name, "name", 1, 8);
    }

    public static void checkValidExt(String ext) {
        checkString(ext, "extension", 0, 3);
    }

    private static void checkString(String str, String strType, int minLength, int maxLength) {
        if (str == null)
            throw new IllegalArgumentException(strType + " is null");
        if (str.length() < minLength)
            throw new IllegalArgumentException(strType + " must have at least " + maxLength +
                    " characters: " + str);
        if (str.length() > maxLength)
            throw new IllegalArgumentException(strType + " has more than " + maxLength +
                    " characters: " + str);
    }

    public static final int SUBNAME_SIZE = 13;

    /**
     * Write the part of a long file name to the given byte array
     * 
     * @param src the src
     * @param srcOffset the srcOffset
     * @param ordinal the ordinal
     * @param checkSum the checkSum
     * @param isLast the isLast
     * @param dest the dest
     * @param destOffset the destOffset
     */
    public static void writeSubString(char[] src, int srcOffset, int ordinal, byte checkSum,
            boolean isLast, byte[] dest, int destOffset) {
        if (isLast) {
            dest[destOffset] = (byte) (ordinal + (1 << 6)); // set the 6th security ending bit
        } else {
            dest[destOffset] = (byte) ordinal;
        }

        ByteUtil.writeLeShort((short) src[srcOffset + 0], dest, destOffset + 1);
        ByteUtil.writeLeShort((short) src[srcOffset + 1], dest, destOffset + 3);
        ByteUtil.writeLeShort((short) src[srcOffset + 2], dest, destOffset + 5);
        ByteUtil.writeLeShort((short) src[srcOffset + 3], dest, destOffset + 7);
        ByteUtil.writeLeShort((short) src[srcOffset + 4], dest, destOffset + 9);
        dest[destOffset + 11] = (byte) 0x0f; // this is the hidden attribute tag for lfn
        dest[destOffset + 12] = (byte) 0; // reserved
        dest[destOffset + 13] = (byte) (int) checkSum; // checksum
        ByteUtil.writeLeShort((short) src[srcOffset + 5], dest, destOffset + 14);
        ByteUtil.writeLeShort((short) src[srcOffset + 6], dest, destOffset + 16);
        ByteUtil.writeLeShort((short) src[srcOffset + 7], dest, destOffset + 18);
        ByteUtil.writeLeShort((short) src[srcOffset + 8], dest, destOffset + 20);
        ByteUtil.writeLeShort((short) src[srcOffset + 9], dest, destOffset + 22);
        ByteUtil.writeLeShort((short) src[srcOffset + 10], dest, destOffset + 24);
        // sector... unused
        ByteUtil.writeLeShort((short) 0, dest, destOffset + 26);
        ByteUtil.writeLeShort((short) src[srcOffset + 11], dest, destOffset + 28);
        ByteUtil.writeLeShort((short) src[srcOffset + 12], dest, destOffset + 30);

        log.log(Level.DEBUG, "<<< END writeSubString dest=\n" /* + FSUtils.toString(dest) */ + ">>>");
    }

    public static byte getOrdinal(byte[] rawData, int offset) {
        return (byte) (rawData[offset] & 0xff);
    }

    public static byte getCheckSum(byte[] rawData, int offset) {
        return (byte) (rawData[offset + 13] & 0xff);
    }

    /**
     * Read a part of the long filename from the given byte array and append the
     * result to the given StringBuffer
     * 
     * @param sb the sb
     * @param rawData the rawData
     * @param offset the offset
     */
    public static void appendSubstring(StringBuffer sb, byte[] rawData, int offset) {
        log.log(Level.DEBUG, "<<< BEGIN appendSubstring buffer=" + sb.toString() + ">>>");

        int index = 12;
        char[] unicodeChar = getUnicodeChars(rawData, offset);

        log.log(Level.DEBUG, "appendSubstring: unicodeChar=" + new String(unicodeChar));

        while (unicodeChar[index] == 0)
            index--;

        sb.append(unicodeChar, 0, index + 1);

        log.log(Level.DEBUG, "<<< END appendSubstring buffer=" + sb + ">>>");
    }

    /**
     * Return a part of a long file name read from the given byte array
     */
    public static String getSubstring(byte[] rawData, int offset) {
log.log(Level.DEBUG, "<<< BEGIN getSubString: rawData=" /* + FSUtils.toString(rawData, offset, 12) */ + " >>>");

//log.log(Level.DEBUG, "getSubString: rawData as chars=" + FSUtils.toStringAsChars(rawData, offset, 12));
        int index = 12;
        char[] unicodechar = getUnicodeChars(rawData, offset);
        while (unicodechar[index] == 0)
            index--;

//log.log(Level.DEBUG, "getSubString: rawData.length="+rawData.length + "offset=" + offset + " nbChars(index)=" + index);
        String str = new String(unicodechar, 0, index);

log.log(Level.DEBUG, "<<< END getSubString: return=" + str + " >>>");

        return str;
    }

    /**
     * convert an array of bytes to an array of chars (unicode)
     */
    static char[] getUnicodeChars(byte[] rawData, int offset) {
        char[] unicodechar = new char[SUBNAME_SIZE];
        unicodechar[0] = (char) (ByteUtil.readLeShort(rawData, offset + 1) & 0xffff);
        unicodechar[1] = (char) (ByteUtil.readLeShort(rawData, offset + 3) & 0xffff);
        unicodechar[2] = (char) (ByteUtil.readLeShort(rawData, offset + 5) & 0xffff);
        unicodechar[3] = (char) (ByteUtil.readLeShort(rawData, offset + 7) & 0xffff);
        unicodechar[4] = (char) (ByteUtil.readLeShort(rawData, offset + 9) & 0xffff);
        unicodechar[5] = (char) (ByteUtil.readLeShort(rawData, offset + 14) & 0xffff);
        unicodechar[6] = (char) (ByteUtil.readLeShort(rawData, offset + 16) & 0xffff);
        unicodechar[7] = (char) (ByteUtil.readLeShort(rawData, offset + 18) & 0xffff);
        unicodechar[8] = (char) (ByteUtil.readLeShort(rawData, offset + 20) & 0xffff);
        unicodechar[9] = (char) (ByteUtil.readLeShort(rawData, offset + 22) & 0xffff);
        unicodechar[10] = (char) (ByteUtil.readLeShort(rawData, offset + 24) & 0xffff);
        unicodechar[11] = (char) (ByteUtil.readLeShort(rawData, offset + 28) & 0xffff);
        unicodechar[12] = (char) (ByteUtil.readLeShort(rawData, offset + 30) & 0xffff);
        return unicodechar;
    }
}
