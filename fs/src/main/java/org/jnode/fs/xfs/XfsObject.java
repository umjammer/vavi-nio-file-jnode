/*
 * https://github.com/VivekDudani/java-fs
 */

package org.jnode.fs.xfs;

import vavi.util.ByteUtil;


/**
 * An object in a XFS file system.
 */
public class XfsObject {

    /**
     * The data for this record.
     */
    private byte[] data;

    /**
     * The offset into the data.
     */
    private int offset;

    /**
     * Creates a new object.
     */
    public XfsObject() {
    }

    /**
     * Creates a new object.
     *
     * @param data   the data.
     * @param offset the offset into the data for this object.
     */
    public XfsObject(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    /**
     * Gets the data.
     *
     * @return the data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the offset for this object into the data.
     *
     * @return the offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets a uint-8.
     *
     * @param relativeOffset the offset to read from.
     * @return the value.
     */
    public int getUInt8(int relativeOffset) {
        return data[offset + relativeOffset] & 0xff;
    }

    /**
     * Gets a uint-16.
     *
     * @param relativeOffset the offset to read from.
     * @return the value.
     */
    public int getUInt16(int relativeOffset) {
        return ByteUtil.readBeShort(data, offset + relativeOffset) & 0xffff;
    }

    /**
     * Gets a uint-32.
     *
     * @param relativeOffset the offset to read from.
     * @return the value.
     */
    public long getUInt32(int relativeOffset) {
        return ByteUtil.readBeInt(data, offset + relativeOffset) & 0xffff_ffffL;
    }

    /**
     * Gets an int-64.
     *
     * @param relativeOffset the offset to read from.
     * @return the value.
     */
    public long getInt64(int relativeOffset) {
        return ByteUtil.readBeLong(data, offset + relativeOffset);
    }
}
