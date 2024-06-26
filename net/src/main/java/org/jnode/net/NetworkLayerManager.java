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

package org.jnode.net;

import java.io.IOException;
import java.util.Collection;

/**
 * This interface must be implemented by the network service of the JNode
 * kernel. It contains methods to register/unregister and obtain NetworkLayers,
 * and it is used by Network drivers to deliver receive packets. <p/> The
 * implementation of this interface must be obtained by invoking a lookup of
 * {@link #NAME}.
 * 
 * @author epr
 * @see org.jnode.driver.net.NetDeviceAPI
 */
public interface NetworkLayerManager {

    /** Name used to bind the ptm in the InitialNaming namespace */
    Class<NetworkLayerManager> NAME = NetworkLayerManager.class;

    /**
     * Get all register packet types.
     * 
     * @return A collection of PacketType instances
     */
    Collection<NetworkLayer> getNetworkLayers();

    /**
     * Gets the packet type for a given protocol ID
     * 
     * @param protocolID the protocolID
     * @throws NoSuchProtocolException when an error occurs
     */
    NetworkLayer getNetworkLayer(int protocolID) throws NoSuchProtocolException;

    /**
     * Process a packet that has been received. The {@code receive} method of all those
     * packet-types that have a matching type and allow the device(of the packet)
     * is called. The packet is cloned if more than 1 packet-types want to
     * receive the packet.
     * 
     * @param skbuf the skbuf
     */
    void receive(SocketBuffer skbuf) throws IOException;
}
