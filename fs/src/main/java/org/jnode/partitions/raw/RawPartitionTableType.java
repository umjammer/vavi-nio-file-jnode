/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.partitions.raw;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableException;
import org.jnode.partitions.PartitionTableType;


/**
 * RawPartitionTableType (dummy for virtual device).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/06 umjammer initial version <br>
 */
public class RawPartitionTableType implements PartitionTableType {

    @Override
    public PartitionTable<?> create(byte[] firstSector, Device device) throws PartitionTableException {
        return new RawPartitionTable();
    }

    @Override
    public String getName() {
        return "RAW";
    }

    @Override
    public String getScheme() {
        return "raw";
    }

    @Override
    public boolean supports(byte[] first16KiB, BlockDeviceAPI devApi) {
        throw new UnsupportedOperationException();
    }
}
