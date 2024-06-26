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

package org.jnode.fs.ftpfs;

import com.enterprisedt.net.ftp.FTPFile;
import java.io.IOException;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSEntry;

/**
 * @author Levente Sántha
 */
public abstract class FTPFSEntry implements FSEntry {

    final FTPFileSystem fileSystem;
    final FTPFile ftpFile;
    FTPFSDirectory parent;

    FTPFSEntry(FTPFileSystem fileSystem, FTPFile ftpFile) {
        this.fileSystem = fileSystem;
        this.ftpFile = ftpFile;
    }

    public void setParent(FTPFSDirectory parent) {
        this.parent = parent;
    }

    /**
     * Gets the access-rights for this entry.
     */
    @Override
    public FSAccessRights getAccessRights() throws IOException {
        return null;
    }

    /**
     * Gets the directory this entry refers to. This method can only be called
     * if <code>isDirectory</code> returns true.
     *
     * @return The directory described by this entry
     */
    @Override
    public FTPFSDirectory getDirectory() throws IOException {
        return (FTPFSDirectory) this;
    }

    /**
     * Gets the file this entry refers to. This method can only be called
     * if <code>isFile</code> returns true.
     *
     * @return The file described by this entry
     */
    @Override
    public FTPFSFile getFile() throws IOException {
        return (FTPFSFile) this;
    }

    /**
     * Gets the last modification time of this entry.
     *
     * @return the last modification time, in milliseconds since January 1, 1970 UTC.
     * @throws java.io.IOException when an error occurs
     */

    @Override
    public long getLastModified() throws IOException {
        return ftpFile.lastModified().getTime();
        // return ftpFile.getTimestamp().getTimeInMillis();
    }

    /**
     * <p>Gets the last access time of this entry.</p>
     * <p/>
     * <p>This implementation returns <code>0</code> as the FTP library has no means of
     * obtaining the access time.</p>
     *
     * @return the last access time, in milliseconds since January 1, 1970 UTC.
     * @throws IOException when an error occurs
     */
    public long getLastAccessed() throws IOException {
        return 0;
    }

    /**
     * Gets the name of this entry.
     */
    @Override
    public String getName() {
        return ftpFile.getName();
    }

    /**
     * Gets the directory this entry is a part of.
     */
    @Override
    public FTPFSDirectory getParent() {
        return null;
    }

    /**
     * Is this entry referring to a (sub-)directory?
     */
    @Override
    public boolean isDirectory() {
        return ftpFile.isDir();
    }

    /**
     * Indicate if the entry has been modified in memory (ie need to be saved)
     *
     * @return true if the entry need to be saved
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public boolean isDirty() throws IOException {
        return false;
    }

    /**
     * Is this entry referring to a file?
     */
    @Override
    public boolean isFile() {
        return !ftpFile.isDir() && !ftpFile.isLink();
    }

    /**
     * Sets the last modification time of this entry.  This implementation does nothing.
     *
     * @param lastModified the new last modification time.
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public void setLastModified(long lastModified) throws IOException {

    }

    /**
     * Sets the last access time of this entry.  This implementation does nothing.
     *
     * @param lastAccessed the new last access time.
     */
    public void setLastAccessed(long lastAccessed) {
    }

    /**
     * Sets the name of this entry.
     */
    @Override
    public void setName(String newName) throws IOException {

    }

    /**
     * Gets the filesystem to which this object belongs.
     */
    @Override
    public FTPFileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * Is this object still valid.
     * <p/>
     * An object is not valid anymore if it has been removed from the filesystem.
     * All invocations on methods (exception this method) of invalid objects
     * must throw an IOException.
     */
    @Override
    public boolean isValid() {
        return true;
    }
}
