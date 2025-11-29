/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

package org.jnode.partitions;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.block.FileDevice;
import org.jnode.driver.block.OffsetedDevice;
import org.jnode.driver.block.VirtualDiskDevice;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystem;
import vavi.util.StringUtil;

import static java.lang.System.getLogger;


/**
 * @author epr
 */
public interface PartitionTableEntry {

    Logger logger = getLogger(PartitionTableEntry.class.getName());

    /**
     * Is this a valid entry, if not it must be ignored.
     */
    boolean isValid();

    /**
     * Does this partition actually is a set of partitions with a partition
     * table of itself.
     */
    boolean hasChildPartitionTable();

    /**
     * Gets the partition table that describes the partitions within this
     * partition.
     *
     * @return null of {{@link #hasChildPartitionTable()} is false.
     */
    PartitionTable<?> getChildPartitionTable();

    /** */
    long getStartOffset(int sectorSize);

    /** */
    long getEndOffset(int sectorSize);

    /**
     * works! don't touch
     * @param device TODO only {@link OffsetedDevice}
     */
    default FileSystem<?> getFileSystem(OffsetedDevice device) throws IOException {
        try {
            int sectorSize = device.getAPI(FSBlockDeviceAPI.class).getSectorSize();

            long offset = getStartOffset(sectorSize);
logger.log(Level.DEBUG, "entry offset: %08x".formatted(offset));
            device.addOffset(offset);

            byte[] bytes = new byte[sectorSize];
            device.getAPI(FSBlockDeviceAPI.class).read(0, ByteBuffer.wrap(bytes));
logger.log(Level.DEBUG, "entry heads\n" + StringUtil.getDump(bytes, 128));

            BlockDeviceFileSystemType<?> bdfst = BlockDeviceFileSystemType.lookup(this, bytes, device.getAPI(FSBlockDeviceAPI.class));
            return bdfst.create(device, true);
        } catch (ApiNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
