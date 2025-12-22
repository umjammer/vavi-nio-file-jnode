/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.partitions.raw;

import java.util.Collections;
import java.util.Iterator;

import org.jnode.partitions.PartitionTable;


/**
 * RawPartitionTable (dummy for virtual device).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/06 umjammer initial version <br>
 */
public class RawPartitionTable implements PartitionTable<RawPartitionTableEntry> {

    /** dummy */
    private final RawPartitionTableEntry entry;

    /**
     * Create a new instance
     */
    public RawPartitionTable() {
        this.entry = new RawPartitionTableEntry();
    }

    @Override
    public Iterator<RawPartitionTableEntry> iterator() {
        return Collections.singletonList(entry).iterator();
    }
}
