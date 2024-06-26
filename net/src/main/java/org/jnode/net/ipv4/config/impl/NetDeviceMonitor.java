/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.net.ipv4.config.impl;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;

/**
 * Monitor the startup/shutdown of network devices.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class NetDeviceMonitor {

    /** My logger */
    private static final Logger log = System.getLogger(NetDeviceMonitor.class.getName());
    private final ConfigurationProcessor processor;
    private final NetConfigurationData config;

    /**
     * @param config the config
     */
    public NetDeviceMonitor(ConfigurationProcessor processor, NetConfigurationData config) {
        this.processor = processor;
        this.config = config;
    }

    /** */
    public void deviceStarted(Device device) {
        if (device.implementsAPI(NetDeviceAPI.class)) {
            configureDevice(device);
        }
    }

    /** */
    public void deviceStop(Device device) {
    }

    private void configureDevice(Device dev) {
        log.log(Level.INFO, "Configuring " + dev.getId());
        final NetDeviceConfig cfg = config.getConfiguration(dev);
        if (cfg != null) {
            processor.apply(dev, cfg, false);
        }
    }
}
