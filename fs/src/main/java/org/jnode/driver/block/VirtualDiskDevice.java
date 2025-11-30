/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.driver.block;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;

import org.jnode.partitions.PartitionTableEntry;


/**
 * VirtualDiskDevice.
 * <p>
 * this device is an alternative of {@link FileDevice}
 * that can treat offset of virtual disk.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/09 umjammer initial version <br>
 */
public class VirtualDiskDevice extends OffsetedDevice implements FSBlockDeviceAPI {

    private static final Logger logger = System.getLogger(VirtualDiskDevice.class.getName());

    /** for partition entry */
    private long offset = 0;

    /** for partition entry */
    @Override
    public void addOffset(long offset) {
        this.offset += offset;
logger.log(Level.DEBUG, "OFFSET: diff: %08x + add: %08x -> this: %08x".formatted((this.offset - offset), offset, this.offset));
    }

    /** virtual offset */
    private final VirtualDisk virtualDisk;

    /**
     * Create a new VirtualDiskDevice
     *
     * @param virtualDisk the virtual disk
     * @throws FileNotFoundException when an error occurs
     */
    public VirtualDiskDevice(VirtualDisk virtualDisk) throws IOException {
        super("virtualdisk" + System.currentTimeMillis());
        this.virtualDisk = virtualDisk;
        registerAPI(FSBlockDeviceAPI.class, this);
    }

    @Override
    public long getLength() throws IOException {
        return virtualDisk.getLength();
    }

    @Override
    public void read(long devOffset, ByteBuffer destBuf) throws IOException {
        virtualDisk.read(devOffset + offset, destBuf);
    }

    @Override
    public void write(long devOffset, ByteBuffer srcBuf) throws IOException {
        virtualDisk.write(devOffset + offset, srcBuf);
    }

    @Override
    public void flush() {
        // Nothing to flush
    }

    /**
     * change the length of the underlying file
     *
     * @param length real file length
     */
    public void setLength(long length) throws IOException {
        virtualDisk.setLength(length);
    }

    /**
     * close the underlying file
     */
    public void close() throws IOException {
        virtualDisk.close();
    }

    @Override
    public PartitionTableEntry getPartitionTableEntry() {
        return null;
    }

    @Override
    public int getSectorSize() throws IOException {
        return virtualDisk.getSectorSize();
    }

    public int getSectors() {
        return virtualDisk.getSectors();
    }

    public int getHeads() {
        return virtualDisk.getHeads();
    }
}
