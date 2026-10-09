/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.github.fge.filesystem.driver.CachedFileSystemDriver;
import vavi.net.fuse.Base;
import vavi.net.fuse.Fuse;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


/**
 * Fuse (jnode) Test.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/20 umjammer initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file://${user.dir}/local.properties")
public class FuseTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String discImageForFuse;
    @Property
    String mountPoint;

    FileSystem fs;
    Map<String, Object> options;

    @BeforeEach
    public void before() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        URI uri = URI.create("jnode:" + Paths.get(discImageForFuse).toUri());

        Map<String, Object> env = new HashMap<>();
        env.put(CachedFileSystemDriver.ENV_IGNORE_APPLE_DOUBLE, true); // mandatory

        fs = FileSystems.newFileSystem(uri, env);
//Files.list(fs.getRootDirectories().iterator().next()).forEach(System.err::println);

        options = new HashMap<>();
        options.put("fsname", "jnode_fs" + "@" + System.currentTimeMillis());
        options.put("noappledouble", null);
//        options.put("noapplexattr", null);
        options.put(vavi.net.fuse.javafs.JavaFSFuse.ENV_DEBUG, false);
        options.put(vavi.net.fuse.javafs.JavaFSFuse.ENV_READ_ONLY, false);
    }

    @Disabled
    @ParameterizedTest
    @ValueSource(strings = {
        "vavi.net.fuse.javafs.JavaFSFuseProvider",
        "vavi.net.fuse.jnrfuse.JnrFuseFuseProvider",
        "vavi.net.fuse.fusejna.FuseJnaFuseProvider",
    })
    public void test01(String providerClassName) throws Exception {
        System.setProperty("vavi.net.fuse.FuseProvider.class", providerClassName);

        Base.testFuse(fs, mountPoint, options);

        fs.close();
    }

    // nhd pc98 fat16 ok
    public static void main(String[] args) throws Exception {
//        System.setProperty("vavi.net.fuse.FuseProvider.class", "vavi.net.fuse.javafs.JavaFSFuseProvider");
        System.setProperty("vavi.net.fuse.FuseProvider.class", "vavi.net.fuse.jnrfuse.JnrFuseFuseProvider");
//        System.setProperty("vavi.net.fuse.FuseProvider.class", "vavi.net.fuse.fusejna.FuseJnaFuseProvider");

        FuseTest app = new FuseTest();
        app.before();

Files.list(app.fs.getRootDirectories().iterator().next()).forEach(System.err::println);

        Fuse fuse = Fuse.getFuse();
        fuse.mount(app.fs, app.mountPoint, app.options);

        CountDownLatch cdl = new CountDownLatch(1);
        cdl.await();
Debug.println("EXIT");
    }
}
