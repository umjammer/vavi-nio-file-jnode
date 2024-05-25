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

package org.jnode.net.ipv4.bootp;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.nanoxml.XMLElement;
import org.jnode.net.ipv4.IPv4Address;

/**
 * @author markhale
 */
public class BOOTPServer {

    private static final Logger log = System.getLogger(BOOTPServer.class.getName());

    public static final int SERVER_PORT = 67;
    public static final int CLIENT_PORT = 68;

    private DatagramSocket socket;
    private final Map<String, TableEntry> table = new HashMap<>();

    public static void main(String[] args) {
        String filename = "bootptab.xml";
        if (args.length > 0)
            filename = args[0];
        BOOTPServer server = new BOOTPServer();
        try {
            server.loadTable(filename);
            server.run();
        } catch (IOException ex) {
            log.log(Level.DEBUG, "I/O exception", ex);
        }
    }

    private static class TableEntry {
        final Inet4Address address;
        final String bootFileName;

        public TableEntry(XMLElement xml) {
            try {
                address = (Inet4Address) InetAddress.getByName(xml.getStringAttribute("ipAddress"));
            } catch (UnknownHostException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
            bootFileName = xml.getStringAttribute("bootFileName");
        }
    }

    private void loadTable(String filename) throws IOException {
        try (FileReader reader = new FileReader(filename)) {
            XMLElement xml = new XMLElement();
            xml.parseFromReader(reader);
            List<XMLElement> children = xml.getChildren();
            for (XMLElement aChildren : children) {
                XMLElement child = aChildren;
                try {
                    table.put(child.getStringAttribute("ethernetAddress").toUpperCase(),
                            new TableEntry(child));
                } catch (IllegalArgumentException ex) {
                    log.log(Level.DEBUG, "Invalid IP address", ex);
                }
            }
        }
    }

    private void run() throws SocketException {
        System.out.println("JNode BOOTP Server");
        socket = new DatagramSocket(SERVER_PORT);
        try {
            socket.setBroadcast(true);

            final byte[] buffer = new byte[BOOTPMessage.SIZE];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            boolean doShutdown = false;
            while (!doShutdown) {
                try {
                    socket.receive(packet);
                    processRequest(packet);
                } catch (IOException ex) {
                    log.log(Level.DEBUG, "I/O exception", ex);
                }
            }
        } finally {
            socket.close();
        }
    }

    private void processRequest(DatagramPacket packet) throws IOException {
        log.log(Level.DEBUG, "Received packet: " + packet.getAddress() + ':' + packet.getPort() + ' ' +
                new String(packet.getData(), packet.getOffset(), packet.getLength()));
        BOOTPHeader hdr = new BOOTPHeader(packet);
        if (hdr.getOpcode() != BOOTPHeader.BOOTREQUEST) {
            // Not a request
            return;
        }

        log.log(Level.DEBUG, "Got Client IP address  : " + hdr.getClientIPAddress());
        log.log(Level.DEBUG, "Got Your IP address    : " + hdr.getYourIPAddress());
        log.log(Level.DEBUG, "Got Server IP address  : " + hdr.getServerIPAddress());
        log.log(Level.DEBUG, "Got Gateway IP address : " + hdr.getGatewayIPAddress());
        log.log(Level.DEBUG, "Got Hardware address   : " + hdr.getClientHwAddress());

        TableEntry entry =
                table.get(hdr.getClientHwAddress().toString().toUpperCase());
        if (entry == null) {
            // no entry in table
            log.log(Level.DEBUG, "No match for hardware address found in table");
            return;
        }
        Inet4Address yourIP = entry.address;
        hdr = new BOOTPHeader(
                BOOTPHeader.BOOTREPLY, hdr.getTransactionID(), hdr.getTimeElapsedSecs(), 
                hdr.getClientIPAddress(), yourIP, (Inet4Address) InetAddress.getLocalHost(), 
                hdr.getClientHwAddress());
        hdr.setBootFileName(entry.bootFileName);
        BOOTPMessage msg = new BOOTPMessage(hdr);
        packet = msg.toDatagramPacket();
        packet.setAddress(IPv4Address.BROADCAST_ADDRESS);
        packet.setPort(CLIENT_PORT);
        socket.send(packet);
    }
}
