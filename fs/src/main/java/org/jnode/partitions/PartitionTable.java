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

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.block.FileDevice;
import org.jnode.driver.block.OffsetedDevice;
import org.jnode.driver.block.VirtualDiskDevice;
import org.jnode.fs.FileSystem;

import static java.lang.System.getLogger;


/**
 * @author epr
 */
public interface PartitionTable<PTE extends PartitionTableEntry> extends Iterable<PTE> {

    Logger logger = getLogger(PartitionTable.class.getName());

    /**
     * @param device only {@link OffsetedDevice} is acceptable
     * @param n partition number 0 origin
     * @throws IllegalStateException when the specified number's partition is invalid.
     * @throws IndexOutOfBoundsException partition number is wrong
     */
    static FileSystem<?> getFileSystem(OffsetedDevice device, int n) throws IOException {
        byte[] bytes = new byte[1024];
        device.getAPI(FSBlockDeviceAPI.class).read(0, ByteBuffer.wrap(bytes));

        PartitionTableType type = PartitionTableType.lookup(bytes, device);
        PartitionTable<?> table = type.create(bytes, device);
logger.log(Level.DEBUG, "PARTITION: " + table.getClass().getSimpleName());
        int i = 0;
        for (PartitionTableEntry entry : table) {
logger.log(Level.DEBUG, "partition entry[" + i + "]: " + entry.getClass().getSimpleName());
            if (i == n) {
                if (entry.isValid()) {
                    return entry.getFileSystem(device);
                } else {
                    throw new IllegalStateException(entry + "[" + i + "] is not valid");
                }
            }
            i++;
        }
        throw new IndexOutOfBoundsException(n + "/" + i);
    }
}
