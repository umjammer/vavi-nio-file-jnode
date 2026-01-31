/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.fs.pc98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.jfat.BootSector;
import org.jnode.fs.jfat.FatFileSystem;
import org.jnode.partitions.PartitionTableEntry;
import vavi.util.StringUtil;
import vavix.io.partition.Validator;

import static java.lang.System.getLogger;


/**
 * PC98FileSystemType.
 * <p>
 * nhd, hdi etc. are detected by {@link org.jnode.driver.block.VirtualDiskDevice}
 * </p>
 * <p>
 * system property
 * <li>{@code "org.jnode.file.encoding"} ... filename encoding for {@link Charset#forName(String)}, default is {@code "MS932"}</li>
 * <li>{@code "vavix.io.partition.validator.fat"} ... , validator for finding fat literal default is {@code false}</li>
 * <li>{@code "vavix.io.partition.validator.ipl"} ... , validator for finding ipl literal default is {@code true}</li>
 * <li>{@code "vavix.io.partition.validator.nec"} ... , validator for finding nec literal, default is {@code true}</li>
 * </p>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/08 umjammer initial version <br>
 */
public class PC98FileSystemType implements BlockDeviceFileSystemType<FatFileSystem> {

    private static final Logger logger = getLogger(PC98FileSystemType.class.getName());

    /** */
    private static final List<Validator> validators;

    static {
        validators = ServiceLoader.load(Validator.class).stream().map(Provider::get).sorted(Comparator.comparingInt(Validator::weight)).toList();
logger.log(Level.TRACE, validators.stream().map(v -> v.getClass().getSimpleName()).toList());
    }

    @Override
    public String getName() {
        return "PC98";
    }

    @Override
    public String getScheme() {
        return "pc98";
    }

    // TODO
    // @see vavi-nio-file-discutils:discUtils.core.pc98.Pc98FileSystemFactory
    @Override
    public boolean supports(PartitionTableEntry pte, byte[] firstSectors, FSBlockDeviceAPI devApi) {
logger.log(Level.TRACE, "firstSectors:\n" + StringUtil.getDump(firstSectors));
        boolean matches = validators.stream().filter(Validator::enabled).anyMatch(v -> v.validate(firstSectors));
logger.log(Level.TRACE, "validators any match: " + matches + "\n" + String.join("\n", validators.stream().filter(Validator::enabled).map(v ->  v.getClass().getSimpleName() + ": " + v.validate(firstSectors)).toList()));
        return matches;
    }

    @Override
    public FatFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        BootSector bs = new PC98BootSector();
        return new FatFileSystem(device, bs, System.getProperty("org.jnode.file.encoding", "MS932"), readOnly);
    }
}
