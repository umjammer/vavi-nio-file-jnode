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

package org.jnode.net.wireless;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;

/**
 * Wrapper class for the IEEE 802.11 header.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class p80211Header {

    private final int frameControl;

    private final int durationId;

    private final EthernetAddress address1;

    private final EthernetAddress address2;

    private final EthernetAddress address3;

    private final int sequenceControl;

    private final EthernetAddress address4;

    /**
     * Initialize this instance.
     * 
     * @param frameControl the frameControl
     * @param durationId the durationId
     * @param address1 the address1
     * @param address2 the address2
     * @param address3 the address3
     * @param sequenceControl the sequenceControl
     * @param address4 the address4
     */
    public p80211Header(int frameControl, int durationId, EthernetAddress address1,
            EthernetAddress address2, EthernetAddress address3, int sequenceControl,
            EthernetAddress address4) {
        this.frameControl = frameControl;
        this.durationId = durationId;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.sequenceControl = sequenceControl;
        this.address4 = address4;
    }

    /**
     * Create a new instance
     * 
     * @param skBuf the socket buffer
     */
    public p80211Header(SocketBuffer skBuf) {
        this.frameControl = skBuf.get16(0);
        this.durationId = skBuf.get16(2);
        this.address1 = new EthernetAddress(skBuf, 4);
        this.address2 = new EthernetAddress(skBuf, 10);
        this.address3 = new EthernetAddress(skBuf, 16);
        this.sequenceControl = skBuf.get16(22);
        this.address4 = new EthernetAddress(skBuf, 24);
    }

    /**
     * Gets the length of this header in bytes
     */
    public int getLength() {
        return WirelessConstants.WLAN_HDR_A4_LEN;
    }

    /**
     * Prefix this header to the front of the given buffer
     * 
     * @param skBuf the socket buffer
     */
    public void prefixTo(SocketBuffer skBuf) {
        skBuf.insert(getLength());
        skBuf.set16(0, frameControl);
        skBuf.set16(2, durationId);
        address1.writeTo(skBuf, 4);
        address2.writeTo(skBuf, 10);
        address3.writeTo(skBuf, 16);
        skBuf.set16(22, sequenceControl);
        address4.writeTo(skBuf, 24);
    }

    /**
     * Finalize the header in the given buffer. This method is called when all
     * layers have set their header data and can be used e.g. to update checksum
     * values.
     * 
     * @param skbuf The buffer
     * @param offset The offset to the first byte (in the buffer) of this header
     *            (since low layer headers are already prefixed)
     */
    public void finalizeHeader(SocketBuffer skbuf, int offset) {
        // Do nothing
    }

    /**
     * @return Returns the address1.
     */
    public final EthernetAddress getAddress1() {
        return address1;
    }

    /**
     * @return Returns the address2.
     */
    public final EthernetAddress getAddress2() {
        return address2;
    }

    /**
     * @return Returns the address3.
     */
    public final EthernetAddress getAddress3() {
        return address3;
    }

    /**
     * @return Returns the address4.
     */
    public final EthernetAddress getAddress4() {
        return address4;
    }

    /**
     * @return Returns the durationId.
     */
    public final int getDurationId() {
        return durationId;
    }

    /**
     * @return Returns the frameControl.
     */
    public final int getFrameControl() {
        return frameControl;
    }

    /**
     * @return Returns the sequenceControl.
     */
    public final int getSequenceControl() {
        return sequenceControl;
    }

}
