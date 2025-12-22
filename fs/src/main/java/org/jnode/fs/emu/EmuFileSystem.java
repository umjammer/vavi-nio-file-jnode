/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.fs.emu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.github.fge.filesystem.exceptions.UncaughtIOException;
import org.jnode.driver.Device;
import org.jnode.driver.block.VirtualDiskDevice;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import vavi.emu.disk.FileEntry;
import vavi.emu.disk.FolderEntry;
import vavi.emu.disk.LogicalDisk;


/**
 * EmuFileSystem.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-12-05 nsano initial version <br>
 */
public class EmuFileSystem implements FileSystem<FSEntry> {

    private final LogicalDisk logicalDisk;
    private final Device device;
    private final boolean readOnly;
    private boolean isOpened;

    static abstract class EmuEntry implements FSEntry {

        EmuFileSystem fileSystem;
        Device device;
        FileEntry entry;

        EmuEntry(EmuFileSystem fileSystem, Device device, FileEntry entry) {
            this.fileSystem = fileSystem;
            this.device = device;
            this.entry = entry;
        }

        @Override
        public String getId() {
            return super.toString();
        }

        @Override
        public FSDirectory getParent() {
            return null;
        }

        @Override
        public long getLastModified() throws IOException {
            return 0;
        }

        @Override
        public boolean isFile() {
            return !entry.isDirectory();
        }

        @Override
        public boolean isDirectory() {
            return entry.isDirectory();
        }

        @Override
        public String getName() {
            return entry.getName();
        }

        @Override
        public void setName(String newName) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLastModified(long lastModified) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public FSFile getFile() throws IOException {
            return !isDirectory() && entry != null ? new EmuFile(fileSystem, device, entry) : null;
        }

        @Override
        public FSDirectory getDirectory() throws IOException {
            return isDirectory() && entry != null ? new EmuDirectory(fileSystem, device, (FolderEntry) entry) : null;
        }

        @Override
        public FSAccessRights getAccessRights() throws IOException {
            return null;
        }

        @Override
        public boolean isDirty() throws IOException {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public FileSystem<?> getFileSystem() {
            return fileSystem;
        }
    }

    static class EmuDirectory extends EmuEntry implements FSDirectory {

        FolderEntry folderEntry;

        EmuDirectory(EmuFileSystem fileSystem, Device device, FolderEntry folderEntry) {
            super(fileSystem, device, folderEntry);
            this.folderEntry = folderEntry;
        }

        @Override
        public Iterator<? extends FSEntry> iterator() throws IOException {
            return folderEntry.entries().stream().map(f -> {
                if (f instanceof FolderEntry fe)
                    return new EmuDirectory(fileSystem, device, fe);
                else
                    return new EmuFile(fileSystem, device, f);
            }).iterator();
        }

        @Override
        public FSEntry getEntry(String name) throws IOException {
            return folderEntry.entries().stream().filter(f -> f.getName().equals(name)).map(f -> {
                        if (f instanceof FolderEntry fe)
                            return new EmuDirectory(fileSystem, device, fe);
                        else
                            return new EmuFile(fileSystem, device, f);
                    }
            ).findFirst().orElseThrow(FileNotFoundException::new);
        }

        @Override
        public FSEntry getEntryById(String id) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public FSEntry addFile(String name) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public FSEntry addDirectory(String name) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(String name) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    static class EmuFile extends EmuEntry implements FSFile {

        FileEntry fileEntry;

        EmuFile(EmuFileSystem fileSystem, Device device, FileEntry fileEntry) {
            super(fileSystem, device, fileEntry);
            this.fileEntry = fileEntry;
        }

        @Override
        public long getLength() {
            try {
                return fileEntry.getSize();
            } catch (IOException e) {
                throw new UncaughtIOException(e);
            }
        }

        @Override
        public void setLength(long length) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void read(long fileOffset, ByteBuffer dest) throws IOException {
            // TODO
        }

        @Override
        public void write(long fileOffset, ByteBuffer src) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    EmuFileSystem(Device device, LogicalDisk logicalDisk, boolean readOnly) {
        if (!(device instanceof VirtualDiskDevice))
            throw new IllegalArgumentException("device must be VirtualDiskDevice");

        this.device = device;
        this.logicalDisk = logicalDisk;
        this.readOnly = readOnly;
        this.isOpened = true;
    }

    @Override
    public FSEntry getRootEntry() throws IOException {
        return new EmuDirectory(this, device, logicalDisk.getRoot());
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void close() throws IOException {
        isOpened = false;
    }

    @Override
    public boolean isClosed() {
        return !isOpened;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return 0;
    }

    @Override
    public long getFreeSpace() throws IOException {
        return 0;
    }

    @Override
    public long getUsableSpace() throws IOException {
        return 0;
    }

    @Override
    public String getVolumeName() throws IOException {
        return "";
    }
}
