/*
 * https://github.com/VivekDudani/java-fs
 */

package org.jnode.fs.xfs.directory;

import java.nio.charset.StandardCharsets;

import org.jnode.fs.xfs.XfsObject;


/**
 * A directory data entry ('xfs_dir2_data_entry').
 *
 * @author Luke Quinane
 */
public class DirectoryDataEntry extends XfsObject {

    /**
     * Creates a new directory data entry.
     *
     * @param data   the data.
     * @param offset the offset to read from.
     */
    public DirectoryDataEntry(byte[] data, int offset) {
        super(data, offset);
    }

    /**
     * Gets the inode number.
     *
     * @return the inode number.
     */
    public long getINumber() {
        return getInt64(0x0);
    }

    /**
     * Gets the name length.
     *
     * @return the name length.
     */
    public int getNameLength() {
        return getUInt8(0x8);
    }

    /**
     * Gets the name of the entry.
     *
     * @return the entry name.
     */
    public String getName() {
        return new String(getData(), getOffset() + 0x9, getNameLength(), StandardCharsets.UTF_8);
    }

    public int getTag() {
        return getUInt16(getNameLength() + 0x9);
    }
}
