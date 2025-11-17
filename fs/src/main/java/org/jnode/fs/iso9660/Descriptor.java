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

package org.jnode.fs.iso9660;

import java.io.UnsupportedEncodingException;

import org.jnode.util.BigEndian;
import vavi.util.ByteUtil;


/**
 * Base class for descriptors. All helper methods in this class used to read the
 * descriptor are using that the bp (see ISO9660 spec) start at 1.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Descriptor implements ISO9660Constants {

    /**
     * See section 7.1.1.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @return an unsigned byte
     */
    protected static int getUInt8(byte[] buffer, int bp) {
        return buffer[bp - 1] & 0xff;
    }

    /**
     * See section 7.1.2.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @return a signed byte
     */
    protected static int getInt8(byte[] buffer, int bp) {
        return buffer[bp - 1];
    }

    /**
     * Gets an unsigned 16-bit value LSB first. See section 7.2.1.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @return an unsigned little-endian short
     */
    protected static int getUInt16LE(byte[] buffer, int bp) {
        return ByteUtil.readLeShort(buffer, bp - 1) & 0xffff;
    }

    /**
     * Gets an unsigned 16-bit value MSB first. See section 7.2.2.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @return an unsigned big-endian short
     */
    protected static int getUInt16BE(byte[] buffer, int bp) {
        return BigEndian.getUInt16(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 16-bit value in both byteorders. See section 7.2.3.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @return an unsigned short
     */
    protected static int getUInt16Both(byte[] buffer, int bp) {
        return ByteUtil.readLeShort(buffer, bp - 1) & 0xffff;
    }

    /**
     * Gets an unsigned 32-bit value LSB first. See section 7.3.1.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @return an unsigned little-endian int
     */
    protected static long getUInt32LE(byte[] buffer, int bp) {
        return ByteUtil.readLeInt(buffer, bp - 1) & 0xffff_ffffL;
    }

    /**
     * Gets an unsigned 32-bit value MSB first. See section 7.3.2.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @return an unsigned big-endian int
     */
    protected static long getUInt32BE(byte[] buffer, int bp) {
        return BigEndian.getUInt32(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 32-bit value in both byteorders. See section 7.3.3.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @return an unsigned int
     */
    protected static long getUInt32Both(byte[] buffer, int bp) {
        return ByteUtil.readLeInt(buffer, bp - 1) & 0xffff_ffffL;
    }

    /**
     * Gets a string of a-characters. See section 7.4.1.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @param length the length
     * @return the String representation
     */
    protected static String getAChars(byte[] buffer, int bp, int length) {
        return new String(buffer, bp - 1, length).trim();
    }

    /**
     * Gets a string of d-characters. See section 7.4.1.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @param length the length
     * @return the String representation
     */
    protected static String getDChars(byte[] buffer, int bp, int length) {
        return new String(buffer, bp - 1, length).trim();
    }

    /**
     * Gets a string of a-characters. See section 7.4.1.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @param length the length
     * @param encoding the encoding
     * @return the String representation
     */
    protected static String getAChars(byte[] buffer, int bp, int length, String encoding)
        throws UnsupportedEncodingException {
        return new String(buffer, bp - 1, length, encoding).trim();
    }

    /**
     * Gets a string of d-characters. See section 7.4.1.
     * 
     * @param buffer the buffer
     * @param bp the bp
     * @param length the length
     * @return the String representation
     */
    protected static String getDChars(byte[] buffer, int bp, int length, String encoding)
        throws UnsupportedEncodingException {
        return new String(buffer, bp - 1, length, encoding).trim();
    }
}
