/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.partitions.pc98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.jnode.driver.Device;
import org.jnode.driver.block.VirtualDiskDevice;
import org.jnode.partitions.PartitionTableEntry;
import vavix.io.partition.PC98PartitionEntry;

import static java.lang.System.getLogger;
import static vavix.io.fat.PC98BiosParameterBlock.toLBA;


/**
 * PC98PartitionTableEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/06 umjammer initial version <br>
 */
public class PC98PartitionTableEntry implements PartitionTableEntry {

    private static final Logger logger = getLogger(PC98PartitionTableEntry.class.getName());

    private final PC98PartitionEntry pe;

    // disk geometries
    private int heads = 0, secs = 0;

    /**
     * Creates a new entry.
     */
    public PC98PartitionTableEntry(PC98PartitionEntry pe, Device device) {
        this.pe = pe;
        if (device instanceof VirtualDiskDevice) {
            heads = ((VirtualDiskDevice) device).getHeads();
            secs = ((VirtualDiskDevice) device).getSectors();
        }
logger.log(Level.DEBUG, "heads: %d, secs: %d, device: %s".formatted(heads, secs, device.getClass().getName()));
    }

    @Override
    public boolean isValid() {
        return pe.isValid();
    }

    @Override
    public PC98PartitionTable getChildPartitionTable() {
        throw new UnsupportedOperationException("No child partitions.");
    }

    @Override
    public boolean hasChildPartitionTable() {
        return false;
    }

    @Override
    public long getStartOffset(int sectorSize) {
logger.log(Level.DEBUG, "s.c: %d, s.h: %d, s.s: %d, heads: %d, secs: %d, bps: %d".formatted(pe.startCylinder, pe.startHeader, pe.startSector, heads, secs, sectorSize));
        if (heads != 0 && secs != 0) {
            return (long) toLBA(pe.startCylinder, pe.startHeader, pe.startSector + 1, heads, secs) * sectorSize;
        } else {
            // when device is not VirtualDiskDevice
logger.log(Level.WARNING, "@@@@@@@@@@@@@@@@@@@@@@@@ magic number is used @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            return 0x20000;
        }
    }

    @Override
    public long getEndOffset(int sectorSize) {
logger.log(Level.DEBUG, "e.c: %d, e.h: %d, e.s: %d, heads: %d, secs: %d, bps: %d".formatted(pe.endCylinder, pe.endHeader, pe.endSector, heads, secs, sectorSize));
        return (long) toLBA(pe.endCylinder, pe.endHeader, pe.endSector + 1, heads, secs) * sectorSize;
    }
}
