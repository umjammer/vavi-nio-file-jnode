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

package org.jnode.net.arp;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.HardwareAddress;
import org.jnode.net.InvalidLayerException;
import org.jnode.net.LayerAlreadyRegisteredException;
import org.jnode.net.NetworkLayer;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.ProtocolAddressInfo;
import org.jnode.net.SocketBuffer;
import org.jnode.net.TransportLayer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.util.TimeoutException;
import org.jnode.vm.objects.Statistics;

/**
 * @author epr
 */
public class ARPNetworkLayer implements NetworkLayer {

    private static final int IPv4_PROTOCOL_SIZE = 4;

    /**
     * Delay between ARP requests in millisecond
     */
    public static final int ARP_REQUEST_DELAY = 1500;

    /**
     * My logger
     */
    private static final Logger log = System.getLogger(ARPNetworkLayer.class.getName());

    private static final boolean DEBUG = false;

    /**
     * My statistics
     */
    private final ARPStatistics stat = new ARPStatistics();

    /**
     * ARP cache
     */
    private static final ARPCache cache = new ARPCache();

    /**
     * Create a new instance
     */
    public ARPNetworkLayer() {
    }

    /**
     * Gets the name of this type
     */
    @Override
    public String getName() {
        return "arp";
    }

    /**
     * Gets the protocol ID this packet-type handles
     */
    @Override
    public int getProtocolID() {
        return EthernetConstants.ETH_P_ARP;
    }

    /**
     * Can this packet type process packets received from the given device?
     */
    @Override
    public boolean isAllowedForDevice(Device dev) {
        // For all devices
        return true;
    }

    /**
     * Process a packet that has been received and matches getType()
     *
     * @param skbuf the socket buffer
     * @param deviceAPI the device API
     * @throws SocketException when an error occurs
     */
    @Override
    public void receive(SocketBuffer skbuf, NetDeviceAPI deviceAPI) throws SocketException {

        // Update statistics
        stat.ipackets.inc();

        final ARPHeader hdr = new ARPHeader(skbuf);
        skbuf.setNetworkLayerHeader(hdr);
        skbuf.pull(hdr.getLength());

        // Update the cache
        cache.set(hdr.getSrcHWAddress(), hdr.getSrcPAddress(), true);

        // Should we reply?
        switch (hdr.getOperation()) {
            case ARP_REQUEST:
                processARPRequest(skbuf, hdr, deviceAPI);
                break;
            case ARP_REPLY:
                processARPReply(skbuf, hdr, deviceAPI);
                break;
            case RARP_REQUEST:
                processRARPRequest(skbuf, hdr, deviceAPI);
                break;
            case RARP_REPLY:
                processRARPReply(skbuf, hdr, deviceAPI);
                break;
            default: {
                log.log(Level.DEBUG, "Unknown ARP operation " + hdr.getOperation());
            }
        }
    }

    /**
     * Process and ARP request.
     *
     * @param skbuf the socket buffer
     * @param hdr the hdr
     * @param deviceAPI the deviceAPI
     * @throws NetworkException when an error occurs
     */
    private void processARPRequest(SocketBuffer skbuf, ARPHeader hdr, NetDeviceAPI deviceAPI)
        throws SocketException {

        final ProtocolAddressInfo addrInfo = deviceAPI.getProtocolAddressInfo(hdr.getPType());
        if ((addrInfo != null) && (addrInfo.contains(hdr.getTargetPAddress()))) {
            // log.log(Level.DEBUG, "Sending ARP reply");
            stat.arpreply.inc();
            stat.opackets.inc();
            hdr.swapAddresses();
            hdr.setSrcHWAddress(deviceAPI.getAddress());
            hdr.setOperation(ARPOperation.ARP_REPLY);
            skbuf.clear();
            skbuf.setProtocolID(getProtocolID());
            hdr.prefixTo(skbuf);
            deviceAPI.transmit(skbuf, hdr.getTargetHWAddress());
        } else {
            // log.log(Level.DEBUG, "ARP request, not my IP-address");
        }
    }

    /**
     * Process and ARP reply
     *
     * @param skbuf the socket buffer
     * @param hdr the hdr
     * @param deviceAPI the deviceAPI
     * @throws NetworkException when an error occurs
     */
    private void processARPReply(SocketBuffer skbuf, ARPHeader hdr, NetDeviceAPI deviceAPI)
        throws SocketException {
        // Nothing further todo
    }

    /**
     * Process and RARP request
     *
     * @param skbuf the socket buffer
     * @param hdr the hdr
     * @param deviceAPI the deviceAPI
     * @throws NetworkException when an error occurs
     */
    private void processRARPRequest(SocketBuffer skbuf, ARPHeader hdr, NetDeviceAPI deviceAPI)
        throws SocketException {
        stat.rarpreq.inc();
        log.log(Level.DEBUG, "GOT RARP Request");
    }

    /**
     * Process and RARP reply
     *
     * @param skbuf the socket buffer
     * @param hdr the hdr
     * @param deviceAPI the deviceAPI
     * @throws NetworkException when an error occurs
     */
    private void processRARPReply(SocketBuffer skbuf, ARPHeader hdr, NetDeviceAPI deviceAPI)
        throws SocketException {
        log.log(Level.DEBUG, "GOT RARP Reply");
    }

    /**
     * Gets the ARP cache.
     */
    public ARPCache getCache() {
        return cache;
    }

    @Override
    public Statistics getStatistics() {
        return stat;
    }

    @Override
    public void registerTransportLayer(TransportLayer layer)
        throws LayerAlreadyRegisteredException, InvalidLayerException {
        throw new InvalidLayerException("ARP cannot register transportlayers");
    }

    @Override
    public void unregisterTransportLayer(TransportLayer layer) {
        // Just ignore
    }

    /**
     * Gets all registered transport-layers
     */
    @Override
    public Collection<TransportLayer> getTransportLayers() {
        return new ArrayList<>(0);
    }

    /**
     * Gets a registered transportlayer by its protocol ID.
     *
     * @param protocolID the protocolID
     */
    @Override
    public TransportLayer getTransportLayer(int protocolID) throws NoSuchProtocolException {
        throw new NoSuchProtocolException("protocol " + protocolID);
    }

    /**
     * Gets the hardware address for a given protocol address.
     *
     * @param address the address
     * @param myAddress the myAddress
     * @param device the device
     * @param timeout the timeout
     * @throws TimeoutException when an error occurs
     * @throws NetworkException when an error occurs
     */
    public HardwareAddress getHardwareAddress(ProtocolAddress address, ProtocolAddress myAddress,
                                              Device device, long timeout) throws TimeoutException, NetworkException {
        final long start = System.currentTimeMillis();
        long lastReq = 0;

        if (DEBUG) {
            log.log(Level.DEBUG, "getHardwareAddress(" + address + ", " + myAddress + ", " + device.getId() +
                    ", " + timeout + ')');
        }

        if (address.equals(myAddress)) {
            // This is simple, just return my address
            return getAPI(device).getAddress();
        }

        while (true) {
            final HardwareAddress hwAddress = cache.get(address);
            if (hwAddress != null) {
                return hwAddress;
            }
            final long now = System.currentTimeMillis();
            if ((now - start) >= timeout) {
                // Still not correct response
                throw new TimeoutException("Timeout in ARP request");
            }
            // Try to send a request every few seconds
            if ((now - lastReq) >= ARP_REQUEST_DELAY) {
                lastReq = now;
                request(address, myAddress, device);
            } else {
                cache.waitForChanges(Math.min(timeout, ARP_REQUEST_DELAY));
            }
        }

    }

    /**
     * Gets the protocol addresses for a given name, or null if not found.
     *
     * @param hostname the hostname
     * @return the addresses or {@code null}
     */
    @Override
    public ProtocolAddress[] getHostByName(String hostname) {
        return null;
    }

    /**
     * Create and transmit an ARP request
     *
     * @param address the address
     * @param myAddress the myAddress
     * @param device the device
     */
    private void request(ProtocolAddress address, ProtocolAddress myAddress, Device device)
        throws NetworkException {
        // Not found in the cache, make a request
        final NetDeviceAPI api = getAPI(device);
        final HardwareAddress srcHwAddr = api.getAddress();
        final HardwareAddress trgHwAddr = srcHwAddr.getDefaultBroadcastAddress();
        final ARPOperation op = ARPOperation.ARP_REQUEST;
        final int hwtype = srcHwAddr.getType();
        final int ptype = address.getType();

        final ARPHeader hdr =
            new ARPHeader(srcHwAddr, myAddress, trgHwAddr, address, op, hwtype, ptype, EthernetConstants.ETH_ALEN,
                IPv4_PROTOCOL_SIZE);
        final SocketBuffer skbuf = new SocketBuffer();
        skbuf.setProtocolID(EthernetConstants.ETH_P_ARP);
        hdr.prefixTo(skbuf);

        api.transmit(skbuf, trgHwAddr);
    }

    /**
     * Gets the NetDeviceAPI for a given device
     *
     * @param device the device
     */
    private NetDeviceAPI getAPI(Device device) {
        try {
            return device.getAPI(NetDeviceAPI.class);
        } catch (ApiNotFoundException ex) {
            throw new IllegalArgumentException("Not a network device " + device.getId());
        }
    }
}
