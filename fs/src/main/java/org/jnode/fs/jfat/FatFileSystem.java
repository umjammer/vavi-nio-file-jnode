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

package org.jnode.fs.jfat;

import java.io.IOException;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * @author gvt
 */
public class FatFileSystem extends AbstractFileSystem<FatRootDirectory> {

    private static final Logger log = System.getLogger(FatFileSystem.class.getName());

    private final Fat fat;
    private final CodePage cp;

    public FatFileSystem(Device device, BootSector bs, String codePageName, boolean readOnly)
        throws FileSystemException {
        super(device, readOnly);

        try {
            fat = Fat.create(getApi(), bs);
        } catch (Exception ex) {
            throw new FileSystemException(ex);
        }

        cp = CodePage.forName(codePageName);
    }

    public FatFileSystem(Device device, BootSector bs, boolean readOnly) throws FileSystemException {
        this(device, bs, "ISO_8859_1", readOnly);
    }

    public int getClusterSize() {
        return fat.getClusterSize();
    }

    public Fat getFat() {
        return fat;
    }

    public BootSector getBootSector() {
        return fat.getBootSector();
    }

    public CodePage getCodePage() {
        return cp;
    }

    @Override
    protected FSFile createFile(FSEntry entry) throws IOException {
        return entry.getFile();
    }

    @Override
    protected FSDirectory createDirectory(FSEntry entry) throws IOException {
        return entry.getDirectory();
    }

    @Override
    protected FatRootDirectory createRootEntry() throws IOException {
        return new FatRootDirectory(this);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        fat.flush();
        log.log(Level.DEBUG, getFat().getCacheStat());
    }

    @Override
    public String toString() {
        return "FAT File System: %s".formatted(fat);
    }

    @Override
    public long getFreeSpace() {
        // TODO implement me
        return -1;
    }

    @Override
    public long getTotalSpace() {
        // TODO implement me
        return -1;
    }

    @Override
    public long getUsableSpace() {
        // TODO implement me
        return -1;
    }

    @Override
    public String getVolumeName() throws IOException {
        return getRootEntry().getLabel();
    }
}
