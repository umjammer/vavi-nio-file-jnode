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

package org.jnode.net.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.driver.Device;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.util.Queue;

/**
 * @author epr
 */
public abstract class AbstractDatagramSocketImpl extends DatagramSocketImpl {

    private static final Logger bootlog = System.getLogger("bootlog");

    /** The receive queue of SocketBuffer instances */
    private final Queue<SocketBuffer> receiveQueue = new Queue<>();

    /** Have I been closed? */
    private boolean closed;

    /** Time to live */
    private int ttl = 0xFF;

    /** Type of service */
    private int tos = 0;

    /** Timeout of network operations */
    private int timeout = 0;

    /** Local address */
    private InetAddress laddr;

    /** Send using broadcast addresses? */
    private boolean broadcast = true;

    /** Device used for transmission (can be null) */
    private Device device;

    /**
     * Create a new instance
     */
    public AbstractDatagramSocketImpl() {
        this.closed = false;
    }

    @Override
    protected final synchronized void bind(int lport, InetAddress laddr) throws SocketException {
        this.localPort = lport;
        this.laddr = laddr;
        doBind(lport, laddr);
    }

    protected abstract void doBind(int lport, InetAddress laddr) throws SocketException;

    @Override
    protected final synchronized void close() {
        if (!closed) {
            this.closed = true;
            doClose();
            receiveQueue.close();
        }
    }

    protected abstract void doClose();

    @Override
    protected void create() throws SocketException {
        // Nothing todo here
    }

    @Override
    public final synchronized Object getOption(int option_id) throws SocketException {
        if (closed) {
            throw new SocketException("DatagramSocket closed");
        }
        return switch (option_id) {
            case IP_TOS -> tos;
            case SO_BINDADDR -> laddr;
            case SO_BROADCAST -> broadcast;
            case SO_RCVBUF, SO_SNDBUF -> EthernetConstants.ETH_FRAME_LEN;
            case SO_TIMEOUT -> timeout;
            default -> doGetOption(option_id);
        };
    }

    protected Object doGetOption(int option_id) throws SocketException {
        throw new SocketException("Unknown option " + option_id);
    }

    @Override
    public final synchronized void setOption(int option_id, Object val) throws SocketException {
        if (closed) {
            throw new SocketException("DatagramSocket closed");
        }
        try {
            switch (option_id) {
                case IP_TOS:
                    tos = (Integer) val;
                    break;
                case SO_BINDADDR:
                    throw new SocketException("Get only option: SO_BINDADDR");
                case SO_BROADCAST:
                    broadcast = (Boolean) val;
                    break;
                case SO_RCVBUF, SO_SNDBUF: /* ignore */
                    break;
                case SO_TIMEOUT:
                    timeout = (Integer) val;
                    break;
                case SO_REUSEADDR:
                    // Ignored for now
                    break;
                default:
                    doSetOption(option_id, val);
            }
        } catch (ClassCastException ex) {
            throw (SocketException) new SocketException("Invalid option type").initCause(ex);
        }
    }

    protected void doSetOption(int option_id, Object val) throws SocketException {
        bootlog.log(Level.ERROR, "Unknown option " + option_id);
    }

    @Override
    protected final int getTimeToLive() throws IOException {
        return ttl;
    }

    @Override
    protected void join(InetAddress inetaddr) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    protected void leave(InetAddress inetaddr) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    protected int peek(InetAddress i) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected int peekData(DatagramPacket p) throws IOException {
        throw new IOException("Not implemented");
    }

    @Override
    protected final void receive(DatagramPacket p) throws IOException {
        if (closed) {
            throw new SocketException("DatagramSocket has been closed");
        }
        final SocketBuffer skbuf = receiveQueue.get(timeout);
        if (skbuf == null) {
            if (closed) {
                throw new SocketException("DatagramSocket has been closed");
            } else {
                throw new SocketTimeoutException("Timeout in receive");
            }
        } else {
            onReceive(p, skbuf);
        }
    }

    protected abstract void onReceive(DatagramPacket p, SocketBuffer skbuf) throws IOException;

    /**
     * Deliver a packet to this socket. This will put the packet in the
     * receive queue if this socket has not been closed.
     * @param skbuf the socket buffer
     */
    public final boolean deliverReceived(SocketBuffer skbuf) {
        if (!closed) {
            receiveQueue.add(skbuf);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see java.net.DatagramSocketImpl#setTimeToLive(int)
     */
    @Override
    protected final void setTimeToLive(int ttl) {
        this.ttl = ttl;
    }

    /**
     * Gets the local port of this socket 
     * @see java.net.DatagramSocketImpl#getLocalPort()
     */
    @Override
    public final int getLocalPort() {
        return super.getLocalPort();
    }

    /**
     * Gets the local port of this socket 
     */
    public final InetAddress getLocalAddress() {
        return laddr;
    }

    /**
     * Gets the device used to send/receive packets.
     */
    protected Device getDevice() {
        return device;
    }

    /**
     * Gets the timeout used in receive
     */
    protected int getTimeout() {
        return timeout;
    }

    /**
     * Gets the Type of Service, used in send
     */
    protected int getTos() {
        return tos;
    }
}
