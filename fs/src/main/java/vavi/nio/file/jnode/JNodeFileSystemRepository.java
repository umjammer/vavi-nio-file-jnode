/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import org.jnode.driver.block.VirtualDisk;
import org.jnode.driver.block.VirtualDiskDevice;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemType;
import org.jnode.partitions.PartitionTable;

import com.github.fge.filesystem.driver.FileSystemDriver;
import com.github.fge.filesystem.provider.FileSystemRepositoryBase;

import static java.lang.System.getLogger;


/**
 * JNodeFileSystemRepository.
 * <p>
 * env
 * <ul>
 * </ul>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/19 umjammer initial version <br>
 */
public final class JNodeFileSystemRepository extends FileSystemRepositoryBase {

    private static final Logger logger = getLogger(JNodeFileSystemRepository.class.getName());

    /** */
    public JNodeFileSystemRepository() {
        super("jnode", new JNodeFileSystemFactoryProvider());
    }

    /**
     * @param uri "jnode:scheme:sub_url", sub url (after "jnode:") parts will be replaced by properties.
     */
    @Override
    public FileSystemDriver createDriver(final URI uri, final Map<String, ?> env) throws IOException {
        String uriString = uri.toString();
        URI subUri = URI.create(uriString.substring(uriString.indexOf(':') + 1));
        String scheme = subUri.getScheme();
logger.log(Level.DEBUG, "scheme: " + scheme);
logger.log(Level.DEBUG, "subUri: " + subUri);

        FileSystem<?> fs;
        if ("file".equals(scheme)) {

            // scheme not specified

            // add emu type by special way
            Path path = Paths.get(subUri);
logger.log(Level.DEBUG, "path: " + path + ", " + Files.exists(path));
            VirtualDisk virtualDisk = VirtualDiskFactory.getInstance().createVirtualDiskFactory(path);
            VirtualDiskDevice device = new VirtualDiskDevice(virtualDisk);

            // partition detection by jnode
            fs = PartitionTable.getFileSystem(device, 0); // TODO partition number

        } else {

            // scheme specified

            URI subSubUri = URI.create(subUri.toString().substring(scheme.length() + 1));
logger.log(Level.DEBUG, "subSubUri: " + subSubUri);
            if (!subSubUri.getScheme().equals("file")) {
                throw new IllegalArgumentException("only file is supported: " + subSubUri);
            }

            // add emu type by special way
            Path path = Paths.get(subSubUri);
logger.log(Level.DEBUG, "path: " + path + ", " + Files.exists(path));
            VirtualDisk virtualDisk = VirtualDiskFactory.getInstance().createVirtualDiskFactory(path);
            VirtualDiskDevice device = new VirtualDiskDevice(virtualDisk);

            // filesystem detection by jnode
            FileSystemType<?> type = FileSystemType.lookup(scheme);
            fs = type.create(device, true); // TODO read only
        }
logger.log(Level.DEBUG, "FILESYSTEM: " + fs.getClass().getSimpleName());

        final JNodeFileStore fileStore = new JNodeFileStore(fs, factoryProvider.getAttributesFactory());
        return new JNodeFileSystemDriver<>(fileStore, factoryProvider, fs, env);
    }

    // ad-hoc hack for ignoring checking opacity
    @Override
    protected void checkURI(URI uri) {
        Objects.requireNonNull(uri);
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("uri is not absolute");
        }
        if (!getScheme().equals(uri.getScheme())) {
            throw new IllegalArgumentException("bad scheme");
        }
    }
}
