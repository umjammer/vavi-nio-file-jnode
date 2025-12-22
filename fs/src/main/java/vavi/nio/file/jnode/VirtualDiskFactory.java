/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jnode.driver.block.VirtualDisk;
import org.jnode.fs.emu.EmuFileSystemType;
import vavi.emu.disk.Disk;
import vavi.emu.disk.LogicalDisk;
import vavi.emu.disk.phisical.D88;

import static java.lang.System.getLogger;


/**
 * VirtualDiskFactory.
 * <p>
 * virtual disk detector using "vavi-nio-file-emu".
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/12 umjammer initial version <br>
 */
public class VirtualDiskFactory {

    private static final Logger logger = getLogger(VirtualDiskFactory.class.getName());

    private VirtualDiskFactory() {}

    /** */
    private static final VirtualDiskFactory instance = new VirtualDiskFactory();

    /** */
    public static VirtualDiskFactory getInstance() {
        return instance;
    }

    /** */
    public VirtualDisk createVirtualDiskFactory(Path path) throws IOException {
        Disk disk;
        try {
            disk = Disk.read(path);
logger.log(Logger.Level.DEBUG, "disk: " + disk.getClass().getSimpleName() + ", bps: " + disk.getSectorSize() + ", offset: " + disk.getOffset());
            // TODO basically jnode has capability of logical disk detection,
            //  but it's for only solid image or header + solid image (= BlockDeviceAPI).
            //  so image that has other info among disk data (e.g. sector info) like "d88"
            //  is available currently using ad-hoc way by Disk#search()
            if (disk.getSectorSize() == -1) {
logger.log(Logger.Level.DEBUG, "no sector size, try to post read");
                try {
                    LogicalDisk logicalDisk = LogicalDisk.read(path, disk);
logger.log(Logger.Level.DEBUG, "logicalDisk: " + logicalDisk.getClass().getSimpleName() + ", bps: " + disk.getSectorSize() + ", offset: " + disk.getOffset());
                    EmuFileSystemType.logicalDisk.set(logicalDisk);
                } catch (IllegalArgumentException e) { // not found for logicalDisk
                    EmuFileSystemType.logicalDisk.set(null);
logger.log(Logger.Level.DEBUG, "no logicalDisk: " + e);
                }
            }
            assert disk.getSectorSize() != -1 : "bytes per sector should be defined";
        } catch (IllegalArgumentException e) { // not found for disk
logger.log(Logger.Level.DEBUG, "raw disk?: " + e);
            disk = createNullDisk();
        }

        return createByPhysical(disk, Files.newByteChannel(path));
    }

    /** for jnode existing code (for solid disk only, so only {@link Disk#bytesPerSector} is used) */
    private Disk createNullDisk() {
        return new Disk() {
            {
                headerSize = 0;
                bytesPerSector = 512;
            }
            @Override protected String imageTypeText() {return null;}
            @Override public void read(SeekableByteChannel sbc) throws IOException {}
            @Override public void save(SeekableByteChannel sbc) throws IOException {}
            @Override public String imageDescText() { return null; }
            @Override public String filterDesc() { return null; }
            @Override public String filterExt() { return null; }
            @Override public Type getType() { return null; }
        };
    }

    /** */
    private VirtualDisk createByPhysical(Disk disk, SeekableByteChannel sbc) {
        return new VirtualDisk() {
            @Override
            public void write(long offset, ByteBuffer buffer) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setLength(long length) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void read(long offset, ByteBuffer buffer) throws IOException {
logger.log(Level.TRACE, () -> "offset: given: %08x, (+o:%08x o:%08x), sceSize: %08x".formatted(offset, disk.getOffset() + offset, disk.getOffset(), getSectorSize()));
                if (offset != 0 && disk instanceof D88) {
                    // for NOT solid disk (TODO this is ad-hoc because VirtualDisk is for solid disk)
                    int sectorOffset = (((int) offset / getSectorSize()) * getSectorSize()) + (int) disk.getOffset() - 16;
                    int[] chs = disk.search(sectorOffset);
                    if (chs == null) {
logger.log(Level.TRACE, "no such sector of offset: argument: %08x, actual: %08x".formatted(sectorOffset, sectorOffset + disk.getOffset() - 16));
                        throw new IOException("no such sector of offset: %08x".formatted(sectorOffset));
                    }
//logger.log(Level.TRACE, "hit sector of offset: argument: %08x, actual: %08x".formatted(sectorOffset, sectorOffset + disk.getOffset() - 16));
                    byte[] sectorData = disk.getSector(chs[0], chs[1], chs[2]).data;
                    buffer.put(sectorData, (int) (offset % getSectorSize()), buffer.capacity());
//logger.log(Level.TRACE, "sector[c: %d, h: %d, s: %s] ofs: %08x, len: %08x%n%s".formatted(chs[0], chs[1], chs[2], (int) (offset % getSectorSize()), buffer.capacity(), StringUtil.getDump(buffer.array(), buffer.capacity())));
                } else {
                    // for solid disk (both jnode and vavi-nio-file-emu)
                    sbc.position(disk.getOffset() + offset);
                    sbc.read(buffer);
                }
            }

            @Override
            public int getSectorSize() {
                return disk.getSectorSize();
            }

            @Override
            public long getLength() {
                return disk.getLength();
            }

            @Override
            public int getSectors() {
                return disk.getGeometry().sectors;
            }

            @Override
            public int getHeads() {
                return disk.getGeometry().heads;
            }

            @Override
            public void close() throws IOException {
                sbc.close();
            }
        };
    }
}
