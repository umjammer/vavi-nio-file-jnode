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

package org.jnode.fs;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ServiceLoader;

import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.partitions.PartitionTableEntry;

import static java.lang.System.getLogger;


/**
 * Specific kind of FileSystemType for block devices
 *
 * @author epr
 */
public interface BlockDeviceFileSystemType<T extends FileSystem<?>> extends FileSystemType<T> {

    Logger logger = getLogger(BlockDeviceFileSystemType.class.getName());

    /**
     * Can this file system type be used on the given first sector of a
     * blockdevice?
     *
     * @param pte The partition table entry, if any. If null, there is no
     *            partition table entry.
     * @param firstSector the firstSector
     */
    boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi);

    /** factory */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <T extends FileSystemType> T lookup(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
        ServiceLoader<FileSystemType> sl = ServiceLoader.load(FileSystemType.class);
        for (FileSystemType fst : sl) {
            if (fst instanceof BlockDeviceFileSystemType bdfst) {
logger.log(Level.TRACE, "try filesystem type: " + fst);
                if (bdfst.supports(pte, firstSector, devApi)) {
logger.log(Level.DEBUG, "FILESYSTEM TYPE: " + fst.getClass().getName());
                    return (T) fst;
                }
            }
        }
        throw new IllegalArgumentException("no suitable file system type for particular parameters.");
    }
}
