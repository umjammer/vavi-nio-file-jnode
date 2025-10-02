/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

package org.jnode.partitions;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ServiceLoader;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.raw.RawPartitionTableType;

import static java.lang.System.getLogger;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PartitionTableType {

    Logger logger = getLogger(PartitionTableType.class.getName());

    /**
     * Gets the unique name of this partition table type.
     */
    String getName();

    /**
     * Can this partition table type be used on the given first sector of a
     * block-device?
     *
     * @param devApi the device api
     * @param firstSectors first sectors
     */
    boolean supports(byte[] firstSectors, BlockDeviceAPI devApi);

    /**
     * Create a partition table for a given device.
     *
     * @param device the device api
     * @param firstSectors first sectors
     */
    PartitionTable<?> create(byte[] firstSectors, Device device) throws PartitionTableException;

    /** */
    String getScheme();

    /** factory */
    @SuppressWarnings({ "unchecked" })
    static <T extends PartitionTableType> T lookup(Class<T> clazz) {
        ServiceLoader<PartitionTableType> sl = ServiceLoader.load(PartitionTableType.class);
        for (PartitionTableType ptt : sl) {
            if (clazz.isInstance(ptt)) {
                return (T) ptt;
            }
        }
        return (T) new RawPartitionTableType();
    }

    /** factory */
    @SuppressWarnings({ "unchecked" })
    static <T extends PartitionTableType> T lookup(String scheme) {
        ServiceLoader<PartitionTableType> sl = ServiceLoader.load(PartitionTableType.class);
        for (PartitionTableType ptt : sl) {
            if (ptt.getScheme().equals(scheme)) {
                return (T) ptt;
            }
        }
        return (T) new RawPartitionTableType();
    }

    /** factory */
    @SuppressWarnings({ "unchecked" })
    static <T extends PartitionTableType> T lookup(byte[] firstSectors, Device device) {
        ServiceLoader<PartitionTableType> sl = ServiceLoader.load(PartitionTableType.class);
        for (PartitionTableType ptt : sl) {
logger.log(Level.TRACE, "partition table type: " + ptt);
            if (ptt.supports(firstSectors, device.getAPI(BlockDeviceAPI.class))) {
                return (T) ptt;
            }
        }
        return (T) new RawPartitionTableType();
    }
}
