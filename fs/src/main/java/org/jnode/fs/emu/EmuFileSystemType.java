/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.fs.emu;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.partitions.PartitionTableEntry;
import vavi.emu.disk.LogicalDisk;


/**
 * EmuFileSystemType.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-12-05 nsano initial version <br>
 */
public class EmuFileSystemType implements BlockDeviceFileSystemType<EmuFileSystem>, FileSystemType<EmuFileSystem> {

    /** TODO using thread local */
    public static ThreadLocal<LogicalDisk> logicalDisk = new InheritableThreadLocal<>();

    @Override
    public String getName() {
        return "EMU";
    }

    @Override
    public String getScheme() {
        return "emu";
    }

    @Override
    public EmuFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        return new EmuFileSystem(device, logicalDisk.get(), readOnly);
    }

    @Override
    public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
        LogicalDisk logicalDisk = EmuFileSystemType.logicalDisk.get();
        return logicalDisk != null;
    }
}
