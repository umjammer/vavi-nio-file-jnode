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

import java.io.IOException;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ISO9660FileSystem extends AbstractFileSystem<ISO9660Entry> {

    private final ISO9660Volume volume;
    private ISO9660Entry rootEntry;

    /** */
    public ISO9660FileSystem(Device device, boolean readOnly) throws FileSystemException {
        super(device, readOnly);

        try {
            volume = new ISO9660Volume(getFSApi());
        } catch (IOException e) {
            throw new FileSystemException(e);
        } catch (ApiNotFoundException ex) {
            throw new FileSystemException("Need FSBlockDeviceAPI for ISO9660 filesystem");
        }
    }

    @Override
    public ISO9660Entry getRootEntry() throws IOException {
        if (rootEntry == null) {
            rootEntry = new ISO9660Entry(this, volume.getRootDirectoryEntry());
        }
        return rootEntry;
    }

    /**
     * @return Returns the volume.
     */
    public ISO9660Volume getVolume() {
        return this.volume;
    }

    @Override
    public void flush() throws IOException {
        if (isReadOnly()) {
            // Do nothing, since readonly
        } else {
            // TODO not implemented yet
        }
    }

    @Override
    protected FSFile createFile(FSEntry entry) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected FSDirectory createDirectory(FSEntry entry) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ISO9660Entry createRootEntry() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getFreeSpace() {
        return 0;
    }

    @Override
    public long getTotalSpace() {
        return volume.getSize();
    }

    @Override
    public long getUsableSpace() {
        return volume.getSize();
    }

    @Override
    public String getVolumeName() throws IOException {
        return getVolume().getPrimaryVolumeDescriptor().getVolumeIdentifier();
    }
}
