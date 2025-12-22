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

package org.jnode.partitions.apm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.driver.Device;
import org.jnode.partitions.PartitionTable;
import vavi.util.ByteUtil;


/**
 * The main Apple Partition Map (APM) partition table class.
 *
 * @author Luke Quinane
 */
public class ApmPartitionTable implements PartitionTable<ApmPartitionTableEntry> {

    /** The partition entries */
    private final List<ApmPartitionTableEntry> partitions = new ArrayList<>();

    /** My logger */
    private static final Logger log = System.getLogger(ApmPartitionTable.class.getName());

    /**
     * Create a new instance
     *
     * @param first16KiB the first 16,384 bytes of the disk.
     * @param device the drive device.
     */
    public ApmPartitionTable(byte[] first16KiB, Device device) {

        long entries = ByteUtil.readBeInt(first16KiB, 0x204) & 0xffff_ffffL;

        for (int partitionNumber = 0; partitionNumber < entries; partitionNumber++) {
            log.log(Level.DEBUG, "try part " + partitionNumber);

            int offset = 0x200 + (partitionNumber * 0x200);

            ApmPartitionTableEntry entry = new ApmPartitionTableEntry(this, first16KiB, offset);

            if (entry.isValid()) {
                partitions.add(entry);
            }
        }
    }

    @Override
    public Iterator<ApmPartitionTableEntry> iterator() {
        return Collections.unmodifiableList(partitions).iterator();
    }
}
