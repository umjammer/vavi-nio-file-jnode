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

package org.jnode.driver;

/**
 * Generic exception to devices in the device framework.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceException extends RuntimeException {

    /** */
    public DeviceException() {
        super();
    }

    /**
     * @param message the message
     * @param cause the cause
     */
    public DeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause
     */
    public DeviceException(Throwable cause) {
        super(cause);
    }

    /**
     * @param s the s
     */
    public DeviceException(String s) {
        super(s);
    }
}
