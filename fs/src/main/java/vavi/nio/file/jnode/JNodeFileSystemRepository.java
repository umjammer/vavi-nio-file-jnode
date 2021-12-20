/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemType;

import com.github.fge.filesystem.driver.FileSystemDriver;
import com.github.fge.filesystem.provider.FileSystemRepositoryBase;

import vavi.util.Debug;


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
Debug.println("scheme: " + scheme);
Debug.println("subUri: " + subUri);

        URI subSubUri = URI.create(subUri.toString().substring(scheme.length() + 1));
Debug.println("subSubUri: " + subSubUri);
        if (!subSubUri.getScheme().equals("file")) {
            throw new IllegalArgumentException("only file is supported: " + subSubUri);
        }
        Path path = Paths.get(subSubUri);
Debug.println("path: " + path + ", " + Files.exists(path));
        Device device = new FileDevice(path.toFile(), "r");

        FileSystemType<?> type = FileSystemType.lookup(scheme);
        FileSystem<?> fs = type.create(device, true);
        final JNodeFileStore fileStore = new JNodeFileStore(fs, factoryProvider.getAttributesFactory());
        return new JNodeFileSystemDriver<>(fileStore, factoryProvider, fs, env);
    }

    /* ad-hoc hack for ignoring checking opacity */
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
