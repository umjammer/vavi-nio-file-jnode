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
public class NecValidator implements Validator {

    @Override
    public int weight() {
        return 10;
    }

    @Override
    public boolean enabled() {
        return Boolean.parseBoolean(System.getProperty("org.jnode.fs.pc98.validator.nec", "true"));
    }

    @Override
    public boolean validate(byte[] firstSectors) {
        if (firstSectors[0x3] != 'N' ||
                firstSectors[0x4] != 'E' ||
                firstSectors[0x5] != 'C') {
            // Missing magic number
logger.log(Level.DEBUG, "Missing magic number 'NEC': %c%c%c".formatted(firstSectors[0x3] & 0xff, firstSectors[0x4] & 0xff, firstSectors[0x5] & 0xff));
            return false;
        } else {
logger.log(Level.DEBUG, "validation (NEC) passed");
            return true;
        }
    }
}
