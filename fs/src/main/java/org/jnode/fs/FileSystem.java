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

package org.jnode.fs;

import java.io.IOException;

/**
 * The <tt>FileSystem<tt> interface provide methods common to file system implementations.
 * *
 *
 * @param <T> {@link FSEntry} sub-type
 * @author epr
 */
public interface FileSystem<T extends FSEntry> {

    /**
     * Gets the root entry of this filesystem. This is usually a directory, but this is not required.
     *
     * @return {@link FSEntry} corresponding to root entry.
     * @throws IOException if an I/O error occurs
     */
    T getRootEntry() throws IOException;

    /**
     * Returns <tt>true</tt> if the file system is mounted in read-only mode.
     *
     * @return <tt>true</tt> if it's a read-only file system.
     */
    boolean isReadOnly();

    /**
     * Close this file system. After a close, all invocations of method of this file system or objects created by this
     * file system will throw an IOException.
     *
     * @throws IOException if an I/O error occurs
     */
    void close() throws IOException;

    /**
     * Returns <tt>true</tt> if this file system is close.
     *
     * @return <tt>true</tt> if file system is closed.
     */
    boolean isClosed();

    /**
     * Return The total size in bytes of this file system.
     *
     * @return total size in bytes or -1 if this feature is unsupported.
     * @throws IOException if an I/O error occurs
     */
    long getTotalSpace() throws IOException;

    /**
     * The total free space in bytes of this file system.
     *
     * @return total free space in bytes or -1 if this feature is unsupported
     * @throws IOException if an I/O error occurs
     */
    long getFreeSpace() throws IOException;

    /**
     * The usable space of this file system.
     *
     * @return usable space in bytes or -1 if this feature is unsupported
     * @throws IOException if an I/O error occurs
     */
    long getUsableSpace() throws IOException;

    /**
     * Gets the volume name for this file system.
     *
     * @return the volume name.
     * @throws IOException if an error occurs reading the volume name.
     */
    String getVolumeName() throws IOException;
}
