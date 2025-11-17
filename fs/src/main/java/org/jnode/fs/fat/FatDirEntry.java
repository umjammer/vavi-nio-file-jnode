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

import java.io.IOException;
import java.util.Date;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryCreated;
import org.jnode.fs.FSEntryLastAccessed;
import org.jnode.fs.FSFile;
import org.jnode.fs.spi.UnixFSAccessRights;
import org.jnode.fs.util.DosUtils;
import org.jnode.util.NumberUtils;
import vavi.util.ByteUtil;


/**
 * @author epr
 */
public class FatDirEntry extends FatBasicDirEntry implements FSEntry, FSEntryCreated, FSEntryLastAccessed {

    /**
     * The ID for this entry.
     */
    private final String id;

    /**
     * Name of this entry
     */
    private String name;

    /**
     * Extension of this entry
     */
    private String ext;

    /**
     * Has this entry been deleted?
     */
    private boolean deleted;

    /**
     * Is this entry not used?
     */
    private boolean unused;

    /**
     * Flags of this entry
     */
    private int flags;

    /**
     * Time of creation.
     */
    private long created;

    /**
     * Time of last modification.
     */
    private long lastModified;

    /**
     * Time of last access.
     */
    private long lastAccessed;

    /**
     * First cluster of the data of this entry
     */
    private int startCluster;

    /**
     * Length in bytes of the data of this entry
     */
    private long length;

    /**
     * Has this entry been changed and not yet flushed to disk?
     */
    private boolean _dirty;

    /**
     * Directory this entry is a part of
     */
    private final AbstractDirectory parent;

    /**
     * access rights of the entry
     */
    private final FSAccessRights rights;

    public static FatBasicDirEntry fatDirEntryFactory(AbstractDirectory dir, byte[] src, int offset) {
        int flags = src[offset + 0x0b] & 0xff;
        boolean r = (flags & F_READONLY) != 0;
        boolean h = (flags & F_HIDDEN) != 0;
        boolean s = (flags & F_SYSTEM) != 0;
        boolean v = (flags & F_LABEL) != 0;

        if (r && h && s && v) {
            // this is a LFN entry, don't need to parse it!
            return new FatLfnDirEntry(dir, src, offset);
        }
        FatDirEntry entry = new FatDirEntry(dir, src, offset);
        return entry;

    }

    /**
     * Create a new entry
     *
     * @param dir the dir
     */
    public FatDirEntry(AbstractDirectory dir) {
        this(dir, "", "");
    }

    /**
     * Create a new entry
     *
     * @param dir the dir
     * @param name the name
     * @param ext the ext
     */
    public FatDirEntry(AbstractDirectory dir, String name, String ext) {
        super(dir);
        this.parent = dir;
        setName(name);
        setExt(ext);
        this.flags = F_ARCHIVE;
        this.created = this.lastModified = this.lastAccessed = System.currentTimeMillis();
        this._dirty = false;
        this.rights = new UnixFSAccessRights(getFileSystem());
        id = name;
    }

    /**
     * Create a new entry from a FAT directory image.
     *
     * @param dir the dir
     * @param src the src
     * @param offset the offset
     */
    public FatDirEntry(AbstractDirectory dir, byte[] src, int offset) {
        super(dir, src, offset);

        this.parent = dir;
        id = Integer.toString(offset / FatConstants.DIR_ENTRY_SIZE);
        unused = (src[offset] == 0);
        deleted = ((src[offset] & 0xff) == 0xe5);

        char[] nameArr = new char[8];
        for (int i = 0; i < nameArr.length; i++) {
            nameArr[i] = (char) (src[offset + i] & 0xff);
        }
        if ((src[offset] & 0xff) == 0x05) {
            nameArr[0] = (char) 0xe5;
        }
        setName(new String(nameArr).trim());

        char[] extArr = new char[3];
        for (int i = 0; i < extArr.length; i++) {
            extArr[i] = (char) (src[offset + 0x08 + i] & 0xff);
        }
        setExt(new String(extArr).trim());

        this.flags = src[offset + 0x0b] & 0xff;
        this.created = DosUtils.decodeDateTime(ByteUtil.readLeShort(src, offset + 0x10) & 0xffff,
                ByteUtil.readLeShort(src, offset + 0x0e) & 0xffff);
        this.lastModified = DosUtils.decodeDateTime(ByteUtil.readLeShort(src, offset + 0x18) & 0xffff,
                ByteUtil.readLeShort(src, offset + 0x16) & 0xffff);
        this.lastAccessed = DosUtils.decodeDateTime(ByteUtil.readLeShort(src, offset + 0x12) & 0xffff,
                0); // time not stored
        this.startCluster = ByteUtil.readLeShort(src, offset + 0x1a) & 0xffff;
        this.length = ByteUtil.readLeInt(src, offset + 0x1c) & 0xffff_ffffL;
        this._dirty = false;
        this.rights = new UnixFSAccessRights(getFileSystem());
    }

    /**
     * Returns the attribute.
     *
     * @return int
     */
    public int getFlags() {
        return flags;
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public long getLastAccessed() {
        return lastAccessed;
    }

    /**
     * Returns the deleted.
     *
     * @return boolean
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Returns the ext.
     *
     * @return String
     */
    public String getExt() {
        return ext;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        if (!ext.isEmpty()) {
            return name + "." + ext;
        } else {
            return name;
        }
    }

    /**
     * Returns the length.
     *
     * @return long
     */
    public long getLength() {
        return length;
    }

    /**
     * Returns the name.
     *
     * @return String
     */
    public String getNameOnly() {
        return name;
    }

    /**
     * Returns the startCluster.
     *
     * @return int
     */
    public int getStartCluster() {
        return startCluster;
    }

    /**
     * Returns the unused.
     *
     * @return boolean
     */
    public boolean isUnused() {
        return unused;
    }

    /**
     * Sets the flags.
     *
     * @param flags the flags
     */
    public void setFlags(int flags) {
        this.flags = flags;
        setDirty();
    }

    public void setCreated(long created) {
        this.created = created;
        setDirty();
    }

    @Override
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
        setDirty();
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
        setDirty();
    }

    /**
     * Sets the deleted.
     *
     * @param deleted The deleted to set
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        setDirty();
    }

    /**
     * Sets the ext.
     *
     * @param ext The ext to set
     */
    public void setExt(String ext) {
        FatUtils.checkValidExt(ext);
        this.ext = ext;
        setDirty();
    }

    /**
     * Updates the length of the entry. This method is called by
     * FatFile.setLength.
     *
     * @param newLength The length to set
     */
    public synchronized void updateLength(long newLength) {
//        System.out.println("updateLength(" + newLength + ") on " + getName());
        this.length = newLength;
        setDirty();
    }

    @Override
    public FSFile getFile() throws IOException {
        if (isFile()) {
            return getFatFile();
        } else {
            throw new IOException("Not a file");
        }
    }

    @Override
    public FSDirectory getDirectory() throws IOException {
        if (isDirectory()) {
            return getFatFile().getDirectory();
        } else {
            throw new IOException("Not a directory");
        }
    }

    /**
     * Gets the single instance of the file connected to this entry. Returns
     * null if the file is 0 bytes long
     *
     * @return File
     */
    public FatFile getFatFile() {
        return getFatFileSystem().getFile(this);
    }

    @Override
    public void setName(String name) {
        FatUtils.checkValidName(name);
        this.name = name;
        setDirty();
    }

    /**
     * Sets the startCluster.
     *
     * @param startCluster The startCluster to set
     */
    protected void setStartCluster(int startCluster) {
        this.startCluster = startCluster;
        setDirty();
    }

    /**
     * Sets the unused.
     *
     * @param unused The unused to set
     */
    public void setUnused(boolean unused) {
        this.unused = unused;
        setDirty();
    }

    public boolean isReadonly() {
        return ((flags & F_READONLY) != 0);
    }

    public void setReadonly() {
        setFlags(flags | F_READONLY);
    }

    public boolean isHidden() {
        return ((flags & F_HIDDEN) != 0);
    }

    public void setHidden() {
        setFlags(flags | F_HIDDEN);
    }

    public boolean isSystem() {
        return ((flags & F_SYSTEM) != 0);
    }

    public void setSystem() {
        setFlags(flags | F_SYSTEM);
    }

    public boolean isLabel() {
        return ((flags & F_LABEL) != 0);
    }

    public void setLabel() {
        setFlags(F_LABEL);
    }

    @Override
    public boolean isFile() {
        return (!(isDirectory() || isLabel()));
    }

    @Override
    public boolean isDirectory() {
        return ((flags & F_DIRECTORY) != 0);
    }

    public void setDirectory() {
        setFlags(F_DIRECTORY);
    }

    public boolean isArchive() {
        return ((flags & F_ARCHIVE) != 0);
    }

    public void setArchive() {
        setFlags(flags | F_ARCHIVE);
    }

    @Override
    public void write(byte[] dest, int offset) {
        // System.out.println("FatDir entry write at" + offset);
        if (unused) {
            dest[offset] = 0;
        } else if (deleted) {
            dest[offset] = (byte) 0xe5;
        }

        for (int i = 0; i < 8; i++) {
            char ch;
            if (i < name.length()) {
                ch = Character.toUpperCase(name.charAt(i));
                if (ch == 0xe5) {
                    ch = (char) 0x05;
                }
            } else {
                ch = ' ';
            }
            dest[offset + i] = (byte) ch;
        }

        for (int i = 0; i < 3; i++) {
            char ch;
            if (i < ext.length()) {
                ch = Character.toUpperCase(ext.charAt(i));
            } else {
                ch = ' ';
            }
            dest[offset + 0x08 + i] = (byte) ch;
        }

        dest[offset + 0x0b] = (byte) flags;
        ByteUtil.writeLeShort((short) DosUtils.encodeTime(created), dest, offset + 0x0e);
        ByteUtil.writeLeShort((short) DosUtils.encodeDate(created), dest, offset + 0x10);
        ByteUtil.writeLeShort((short) DosUtils.encodeDate(lastAccessed), dest, offset + 0x12);
        ByteUtil.writeLeShort((short) DosUtils.encodeTime(lastModified), dest, offset + 0x16);
        ByteUtil.writeLeShort((short) DosUtils.encodeDate(lastModified), dest, offset + 0x18);
        ByteUtil.writeLeShort((short) startCluster, dest, offset + 0x1a);
        ByteUtil.writeLeInt((int) length, dest, offset + 0x1c);
        this._dirty = false;
    }

    public String toString() {
        StringBuilder b = new StringBuilder(64);

        b.append(getName());

        b.append(" attr=");
        if (isReadonly()) {
            b.append('R');
        }
        if (isHidden()) {
            b.append('H');
        }
        if (isSystem()) {
            b.append('S');
        }
        if (isLabel()) {
            b.append('L');
        }
        if (isDirectory()) {
            b.append('D');
        }
        if (isArchive()) {
            b.append('A');
        }
        b.append("(0x");
        b.append(NumberUtils.hex(flags, 2));
        b.append(")");

        b.append(" created=");
        b.append(new Date(getCreated()));
        b.append(" lastModified=");
        b.append(new Date(getLastModified()));
        b.append(" lastAccessed=");
        b.append(new Date(getLastAccessed()));
        b.append(" startCluster=");
        b.append(getStartCluster());
        b.append(" length=");
        b.append(getLength());
        if (deleted) {
            b.append(" deleted");
        }

        return b.toString();
    }

    @Override
    public final boolean isDirty() {
        return _dirty;
    }

    protected final void setDirty() {
        this._dirty = true;
        parent.setDirty();
    }

    @Override
    public FSDirectory getParent() {
        return parent;
    }

    @Override
    public FSAccessRights getAccessRights() throws IOException {
        return rights;
    }
}
