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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.jnode.driver.Device;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class NetConfigurationData {

    /** My logger */
    static final Logger log = System.getLogger(NetConfigurationData.class.getName());

    /** The preferences that contain my data */
    private final Preferences prefs;

    /** The preferences that contain the per device data*/
    private final Preferences devConfigsPrefs;

    // Preference keys
    private static final String AUTO_CONFIGURE_DHCP_KEY = "auto-configure-dhcp";
    private static final String DEVICE_CONFIG_NODE = "device-configs";
    private static final String CONFIG_CLASS_NAME_KEY = "class-name";

    /**
     * Initialize this instance.
     * @param prefs the prefs
     */
    public NetConfigurationData(Preferences prefs) {
        this.prefs = prefs;
        this.devConfigsPrefs = prefs.node(DEVICE_CONFIG_NODE);
    }

    /**
     * Set the configuration data for the given device.
     * @param device the device
     * @param config the config
     */
    public void setConfiguration(Device device, NetDeviceConfig config) {
        final Preferences devPrefs = devConfigsPrefs.node(device.getId());
        devPrefs.put(CONFIG_CLASS_NAME_KEY, config.getClass().getName());
        config.store(devPrefs);
    }

    /**
     * Gets the configuration data for the device, or null if not found.
     */
    public NetDeviceConfig getConfiguration(Device device) {
        NetDeviceConfig cfg = null;
        try {
            if (devConfigsPrefs.nodeExists(device.getId())) {
                final Preferences devPrefs = devConfigsPrefs.node(device.getId());
                final String clsName = devPrefs.get(CONFIG_CLASS_NAME_KEY, null);
                if (clsName != null) {
                    try {
                        final Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(clsName);
                        return (NetDeviceConfig) cls.getDeclaredConstructor().newInstance();
                    } catch (ClassNotFoundException ex) {
                        log.log(Level.WARNING, "NetDeviceConfig class not found", ex);
                        return null;
                    } catch (InstantiationException ex) {
                        log.log(Level.WARNING, "Cannot instantiate NetDeviceConfig class", ex);
                        return null;
                    } catch (IllegalAccessException ex) {
                        log.log(Level.WARNING, "Cannot access NetDeviceConfig class", ex);
                        return null;
                    } catch (InvocationTargetException | NoSuchMethodException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        } catch (BackingStoreException ex) {
            log.log(Level.WARNING, "BackingStore error while loading NetDeviceConfig preferences", ex);
            // Ignore
        }

        if ((cfg == null) && isAutoConfigureUsingDhcp()) {
            return new NetDhcpConfig();
        } else {
            if (cfg != null) {
                cfg.load(devConfigsPrefs.node(device.getId()));
            }
            return cfg;
        }
    }    

    /**
     * @return Returns the autoConfigureUsingDhcp.
     */
    public boolean isAutoConfigureUsingDhcp() {
        return prefs.getBoolean(AUTO_CONFIGURE_DHCP_KEY, false);
    }

    /**
     * @param autoConfigureUsingDhcp The autoConfigureUsingDhcp to set.
     */
    public void setAutoConfigureUsingDhcp(boolean autoConfigureUsingDhcp) {
        prefs.putBoolean(AUTO_CONFIGURE_DHCP_KEY, autoConfigureUsingDhcp);
    }
}
