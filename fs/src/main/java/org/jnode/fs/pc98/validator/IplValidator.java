/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.fs.pc98.validator;

import java.lang.System.Logger.Level;

import org.jnode.fs.pc98.PC98FileSystemType.Validator;


/**
 * NecValidator.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-11-30 nsano initial version <br>
 */
public class IplValidator implements Validator {

    @Override
    public int weight() {
        return 30;
    }

    @Override
    public boolean enabled() {
        return Boolean.parseBoolean(System.getProperty("org.jnode.fs.pc98.validator.ipl", "true"));
    }

    @Override
    public boolean validate(byte[] firstSectors) {
        if (firstSectors[0x4] != 'I' ||
                firstSectors[0x5] != 'P' ||
                firstSectors[0x6] != 'L' ||
                firstSectors[0x7] != '1') {
logger.log(Level.TRACE, "Missing magic number 'IPL1': %c%c%c%c".formatted(firstSectors[0x4] & 0xff, firstSectors[0x5] & 0xff, firstSectors[0x6] & 0xff, firstSectors[0x7] & 0xff));
            return false;
        } else {
logger.log(Level.TRACE, "validation (IPL1) passed");
            return true;
        }
    }
}
