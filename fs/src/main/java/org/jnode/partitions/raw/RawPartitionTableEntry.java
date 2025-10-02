/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.partitions.raw;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.jnode.partitions.PartitionTableEntry;

import static java.lang.System.getLogger;


/**
 * RawPartitionTableEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/06 umjammer initial version <br>
 */
public class RawPartitionTableEntry implements PartitionTableEntry {

    private static final Logger logger = getLogger(RawPartitionTableEntry.class.getName());

    /**
     * Creates a new entry.
     */
    public RawPartitionTableEntry() {
logger.log(Level.TRACE, "virtual raw partition");
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public RawPartitionTable getChildPartitionTable() {
        throw new UnsupportedOperationException("No child partitions.");
    }

    @Override
    public boolean hasChildPartitionTable() {
        return false;
    }

    @Override
    public long getStartOffset(int sectorSize) {
        return 0;
    }

    @Override
    public long getEndOffset(int sectorSize) {
        // TODO Auto-generated method stub
        return 0;
    }
}
