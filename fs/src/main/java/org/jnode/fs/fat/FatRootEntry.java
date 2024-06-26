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
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;

/**
 * @author epr
 */
public class FatRootEntry extends FatObject implements FSEntry {

    /**
     * The actual root directory
     */
    private final FatDirectory rootDir;

    public FatRootEntry(FatDirectory rootDir) {
        super(rootDir.getFatFileSystem());
        this.rootDir = rootDir;
    }

    @Override
    public String getId() {
        return "2";
    }

    /**
     * Gets the name of this entry.
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * Gets the directory this entry is a part of.
     */
    @Override
    public FSDirectory getParent() {
        return null;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    /**
     * Is this entry referring to a file?
     */
    @Override
    public boolean isFile() {
        return false;
    }

    /**
     * Is this entry referring to a (sub-)directory?
     */
    @Override
    public boolean isDirectory() {
        return true;
    }

    /**
     * Sets the name of this entry.
     */
    @Override
    public void setName(String newName) throws IOException {
        throw new IOException("Cannot change name of root directory");
    }

    /**
     * Sets the last modification time of this entry.
     *
     * @throws IOException when an error occurs
     */
    @Override
    public void setLastModified(long lastModified) throws IOException {
        throw new IOException("Cannot change last modified of root directory");
    }

    /**
     * Gets the file this entry refers to. This method can only be called if
     * <code>isFile</code> returns true.
     */
    @Override
    public FSFile getFile() throws IOException {
        throw new IOException("Not a file");
    }

    /**
     * Gets the directory this entry refers to. This method can only be called
     * if <code>isDirectory</code> returns true.
     */
    @Override
    public FSDirectory getDirectory() {
        return rootDir;
    }

    /**
     * Gets the access rights for this entry.
     *
     * @throws IOException when an error occurs
     */
    @Override
    public FSAccessRights getAccessRights() throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Indicate if the entry has been modified in memory (ie need to be saved)
     *
     * @return true if the entry need to be saved
     * @throws IOException when an error occurs
     */
    @Override
    public boolean isDirty() throws IOException {
        return true;
    }
}
