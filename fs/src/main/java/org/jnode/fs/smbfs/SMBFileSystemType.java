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

package org.jnode.fs.smbfs;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;

/**
 * @author Levente Sántha
 */
public class SMBFileSystemType implements FileSystemType<SMBFileSystem> {
    public static final Class<SMBFileSystemType> ID = SMBFileSystemType.class;

    static {
        System.setProperty("jcifs.smb.client.attrExpirationPeriod", "10");
        System.setProperty("jcifs.smb.client.responseTimeout", "10000");
    }

    /**
     * Create a filesystem from a given device.
     *
     * @param device the device
     * @param readOnly the readOnly
     */
    @Override
    public SMBFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        return new SMBFileSystem((SMBFSDevice) device, this);
    }

    /**
     * Gets the unique name of this file system type.
     */
    @Override
    public String getName() {
        return "SMBFS";
    }

    @Override
    public String getScheme() {
        return "smb";
    }
}
