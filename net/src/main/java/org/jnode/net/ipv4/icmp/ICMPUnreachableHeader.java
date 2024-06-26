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

package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public class ICMPUnreachableHeader extends ICMPHeader {

    /**
     * @param code the code
     */
    public ICMPUnreachableHeader(int code) {
        super(ICMPType.ICMP_DEST_UNREACH, code);
    }

    /**
     * @param skBuf the socket buffer
     */
    public ICMPUnreachableHeader(SocketBuffer skBuf) {
        super(skBuf);
        final ICMPType type = getType();
        if (type != ICMPType.ICMP_DEST_UNREACH) {
            throw new IllegalArgumentException("Invalid type " + type);
        }
    }

    @Override
    protected void doPrefixTo(SocketBuffer skBuf) {
        skBuf.set16(4, 0); // Unused, must be 0
    }

    @Override
    public int getLength() {
        return 8;
    }
}
