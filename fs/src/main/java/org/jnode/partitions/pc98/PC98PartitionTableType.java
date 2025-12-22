/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.partitions.pc98;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableException;
import org.jnode.partitions.PartitionTableType;
import vavi.util.ByteUtil;
import vavi.util.StringUtil;
import vavi.util.serdes.Serdes;
import vavix.io.partition.PC98PartitionEntry;

import static java.lang.System.getLogger;


/**
 * PC98PartitionTableType.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/06 umjammer initial version <br>
 */
public class PC98PartitionTableType implements PartitionTableType {

    private static final Logger logger = getLogger(PC98PartitionTableType.class.getName());

    @Override
    public PartitionTable<?> create(byte[] firstSector, Device device) throws PartitionTableException {
        return new PC98PartitionTable(firstSector, device);
    }

    @Override
    public String getName() {
        return "PC98";
    }

    @Override
    public String getScheme() {
        return "pc98";
    }

    // TODO out source
    private static final String[] iplSignatures = {
        "IPL1", "Linux 98", "GRUB/98 "
    };

    // works don't touch
    @Override
    public boolean supports(byte[] bootSector, BlockDeviceAPI devApi) {
logger.log(Level.TRACE, "bootSector: \n" + StringUtil.getDump(bootSector));
        if (bootSector.length < 0x400) {
logger.log(Level.TRACE, "Not enough data for detection: %04x/%04x".formatted(bootSector.length, 0x400));
            return false;
        }

        if ((ByteUtil.readLeShort(bootSector, 510) & 0xffff) != 0xaa55) {
logger.log(Level.TRACE, "No aa55 magic: %04x".formatted(ByteUtil.readLeShort(bootSector, 510) & 0xffff));
            return false;
        }

        if (Arrays.stream(iplSignatures).noneMatch(s ->
            new String(bootSector, 4, s.length(), StandardCharsets.US_ASCII).equals(s)
        )) {
logger.log(Level.TRACE, "no matching signature is found: " + new String(bootSector, 4, 4, StandardCharsets.US_ASCII));
            return false;
        }

        if (new String(bootSector, 0x36, 3, StandardCharsets.US_ASCII).equals("FAT")) {
logger.log(Level.TRACE, "strings FAT is found, this partition might be for AT");
            return false;
        }

        int count = 0;
        ByteArrayInputStream baos = new ByteArrayInputStream(bootSector, 512, 512);
        for (int i = 0; i < 16; i++) {
            PC98PartitionEntry pe = new PC98PartitionEntry();
            try {
                Serdes.Util.deserialize(baos, pe);
                if (!pe.isValid()) {
                    continue;
                }
            } catch (IOException e) {
                continue;
            }
logger.log(Level.DEBUG, "[" + count + "]: " + pe);
            count++;
        }

        return count > 0;
    }
}
