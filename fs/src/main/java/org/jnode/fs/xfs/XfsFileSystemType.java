/*
 * https://github.com/VivekDudani/java-fs
 */

package org.jnode.fs.xfs;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.partitions.PartitionTableEntry;
import vavi.util.ByteUtil;


/**
 * The file system type for an XFS file system.
 *
 * @author Luke Quinane
 */
public class XfsFileSystemType implements BlockDeviceFileSystemType<XfsFileSystem> {

    /**
     * The ID.
     */
    public static final Class<XfsFileSystemType> ID = XfsFileSystemType.class;

    @Override
    public XfsFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        XfsFileSystem fs = new XfsFileSystem(device);
        fs.read();
        return fs;
    }

    @Override
    public String getScheme() {
        return "xfs";
    }

    @Override
    public String getName() {
        return "XFS";
    }

    @Override
    public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {

        ByteBuffer magic = ByteBuffer.allocate(4);

        try {
            devApi.read(0, magic);
        } catch (IOException e) {
            return false;
        }

        return (ByteUtil.readBeInt(magic.array(), 0) & 0xffff_ffffL) == Superblock.XFS_SUPER_MAGIC;
    }
}
