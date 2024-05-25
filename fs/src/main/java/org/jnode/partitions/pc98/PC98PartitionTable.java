/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.partitions.pc98;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.jnode.driver.Device;
import org.jnode.partitions.PartitionTable;

import vavi.util.Debug;
import vavi.util.serdes.Serdes;

import vavix.io.partition.PC98PartitionEntry;


/**
 * PC98PartitionTableEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/06 umjammer initial version <br>
 */
public class PC98PartitionTable implements PartitionTable<PC98PartitionTableEntry> {

    /**
     * The partition entries
     */
    private final List<PC98PartitionTableEntry> partitions = new ArrayList<>();

    /**
     * Create a new instance
     */
    public PC98PartitionTable(byte[] bootSector, Device device) {

        ByteArrayInputStream baos = new ByteArrayInputStream(bootSector, 512, 512);
        for (int i = 0; i < 16; i++) {
            try {
                PC98PartitionEntry pe = new PC98PartitionEntry();
                Serdes.Util.deserialize(baos, pe);
                if (!pe.isValid()) {
                    continue;
                }
//Debug.println("[" + i + "]: " + pe);
                partitions.add(new PC98PartitionTableEntry(pe, device));
            } catch (IOException e) {
                Debug.println(Level.FINEST, e);
            }
        }
    }

    @Override
    public Iterator<PC98PartitionTableEntry> iterator() {
        return Collections.unmodifiableList(partitions).iterator();
    }
}
