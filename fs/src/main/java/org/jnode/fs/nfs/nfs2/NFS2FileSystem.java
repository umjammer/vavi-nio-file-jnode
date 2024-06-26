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

package org.jnode.fs.nfs.nfs2;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.net.nfs.nfs2.FileAttribute;
import org.jnode.net.nfs.nfs2.FileSystemAttribute;
import org.jnode.net.nfs.nfs2.NFS2Client;
import org.jnode.net.nfs.nfs2.NFS2Exception;
import org.jnode.net.nfs.nfs2.mount.Mount1Client;
import org.jnode.net.nfs.nfs2.mount.MountException;
import org.jnode.net.nfs.nfs2.mount.MountResult;

import static java.lang.System.getLogger;


/**
 * @author Andrei Dore
 */
public class NFS2FileSystem implements FileSystem<NFS2RootEntry> {

    private static final Logger logger = getLogger(NFS2FileSystem.class.getName());

    private final NFS2Device device;

    private final NFS2RootEntry root;

    private final Mount1Client mountClient;

    private final NFS2Client nfsClient;

    private boolean closed;

    private final boolean readOnly;

    private final NFS2FileSystemType type;

    public NFS2FileSystem(final NFS2Device device, boolean readOnly, NFS2FileSystemType type)
        throws FileSystemException {
        this.device = device;
        this.readOnly = readOnly;
        this.type = type;

        mountClient = new Mount1Client(device.getHost(), device.getProtocol(), device.getUid(), device.getGid());
        nfsClient = new NFS2Client(device.getHost(), device.getProtocol(), device.getUid(), device.getGid());

        // Mount the file system
        MountResult result;
        FileAttribute fileAttribute;
        try {
            result = mountClient.mount(device.getRemoteDirectory());
            fileAttribute = nfsClient.getAttribute(result.getFileHandle());
        } catch (IOException | MountException | NFS2Exception e) {
            try {
                close();
            } catch (IOException e1) {
                logger.log(Level.TRACE, e1.getMessage(), e1);
            }
            throw new FileSystemException(e.getMessage(), e);
        }
        root = new NFS2RootEntry(this, result.getFileHandle(), fileAttribute);
    }

    public final NFS2FileSystemType getType() {
        return type;
    }

    /**
     * Close this filesystem. After a close, all invocations of method of this filesystem or objects created by this
     * filesystem will throw an IOException.
     *
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public void close() throws IOException {
        // FIXME ... we squash exceptions though the signature says they can be thrown.
        if (mountClient != null) {
            try {
                mountClient.unmount(device.getRemoteDirectory());
            } catch (MountException e) {
                logger.log(Level.TRACE, e.getMessage(), e);
            }

            try {
                mountClient.close();
            } catch (IOException e) {
                // ignore
            }
        }

        if (nfsClient != null) {
            try {
                nfsClient.close();
            } catch (IOException e) {
                logger.log(Level.TRACE, e.getMessage(), e);
            }
        }
        closed = true;
    }

    /**
     * Gets the device this FS driver operates on.
     */
    public NFS2Device getDevice() {
        return device;
    }

    /**
     * Gets the root entry of this filesystem. This is usually a directory, but this is not required.
     */
    @Override
    public NFS2RootEntry getRootEntry() throws IOException {
        return root;
    }

    /**
     * Is this filesystem closed.
     */
    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * Is the filesystem mounted in readonly mode ?
     */
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public long getFreeSpace() {
        FileSystemAttribute fileSystemAttribute = getFileSystemAttribute();
        return fileSystemAttribute.getBlockSize() * fileSystemAttribute.getFreeBlockCount();
    }

    @Override
    public long getTotalSpace() {
        FileSystemAttribute fileSystemAttribute = getFileSystemAttribute();
        return fileSystemAttribute.getBlockSize() * fileSystemAttribute.getBlockCount();
    }

    @Override
    public long getUsableSpace() {
        FileSystemAttribute fileSystemAttribute = getFileSystemAttribute();
        return fileSystemAttribute.getBlockSize() * fileSystemAttribute.getFreeBlockCount();
    }

    @Override
    public String getVolumeName() throws IOException {
        return device.getHost() + ":" + device.getRemoteDirectory();
    }

    private FileSystemAttribute getFileSystemAttribute() {
        try {
            return nfsClient.getFileSystemAttribute(root.getFileHandle());
        } catch (NFS2Exception | IOException e) {
            logger.log(Level.TRACE, e.getMessage(), e);
        }
        return null;
    }

    NFS2Client getNFSClient() {
        return nfsClient;
    }
}
