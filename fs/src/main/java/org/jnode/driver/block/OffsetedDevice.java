/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.driver.block;

import org.jnode.driver.Device;


/**
 * Device with some header part.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-11-29 nsano initial version <br>
 */
public abstract class OffsetedDevice extends Device {

    /**
     * Create a new instance
     *
     * @param id the new id
     */
    public OffsetedDevice(String id) {
        super(id);
    }

    public abstract void addOffset(long offset);
}
