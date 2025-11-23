/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * SPI (jnode) Test.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/20 umjammer initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file://${user.dir}/local.properties")
class SpiTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String avdsdcard;
    @Property
    String exfat;
    @Property
    String dmg;
    @Property
    String d88;
    @Property
    String fdi;
    @Property
    String qcow2;

    @BeforeEach
    void before() throws IOException {
        PropsEntity.Util.bind(this);
    }

    @Test
    @DisplayName("by scheme, raw disk")
    void test() throws Exception {
        Path exfatPath = Paths.get(exfat);
Debug.println("disc: " + exfatPath + ", " + Files.exists(exfatPath));
        URI uri = URI.create("jnode:exfat:" + exfatPath.toUri());
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
//        Files.list(fs.getRootDirectories().iterator().next()).forEach(System.err::println);
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        });
        fs.close();
    }

    @Test
    @DisplayName("by partition, parted disk")
    void test2() throws Exception {
        Path dmgPath = Paths.get(dmg);
Debug.println("disc: " + dmgPath + ", " + Files.exists(dmgPath));
        URI uri = URI.create("jnode:" + dmgPath.toUri());
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        });
        fs.close();
    }

    @Test
    @DisplayName("by partition, raw disk")
    void test3() throws Exception {
        Path exfatPath = Paths.get(exfat);
Debug.println("disc: " + exfatPath + ", " + Files.exists(exfatPath));
        URI uri = URI.create("jnode:" + exfatPath.toUri());
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        });
        fs.close();
    }

    @Test
    @DisplayName("dmg jfat:fat16")
    void test4() throws Exception {
        Path dmgPath = Paths.get(dmg);
Debug.println("disc: " + dmgPath + ", " + Files.exists(dmgPath));
        URI uri = URI.create("jnode:" + dmgPath.toUri());
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        });
        fs.close();
    }

    // TODO not (header +) solid image
    @Test
//    @Disabled("wip d88")
    void test5() throws Exception {
        Path d88Path = Paths.get(d88);
Debug.println("disc: " + d88Path + ", " + Files.exists(d88Path));
        URI uri = URI.create("jnode:" + d88Path.toUri());
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        });
        fs.close();
    }

    @Test
    @DisplayName("fdi fat12")
    void test6() throws Exception {
        Path fidPath = Paths.get(fdi);
Debug.println("disc: " + fidPath + ", " + Files.exists(fidPath));
        URI uri = URI.create("jnode:" + fidPath.toUri());
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
//                if (!Files.isDirectory(p)) // newInputStream is not supported mark
//                    System.err.println(StringUtil.getDump(new BufferedInputStream(Files.newInputStream(p)), 0, (int) Math.min(64, Files.size(p))));
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        });
        fs.close();
    }

    @Test
    @DisplayName("by partition, raw disk")
    void test7() throws Exception {
        Path exfatPath = Paths.get(avdsdcard);
Debug.println("disc: " + exfatPath + ", " + Files.exists(exfatPath));
        URI uri = URI.create("jnode:" + exfatPath.toUri());
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        });
        fs.close();
    }

    @Test
    @DisplayName("qcow2")
    @Disabled("not implemented yet")
    void test8() throws Exception {
        Path qcow2 = Paths.get(this.qcow2);
Debug.println("disc: " + qcow2 + ", " + Files.exists(qcow2));
        URI uri = URI.create("jnode:" + qcow2.toUri());
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        });
        fs.close();
    }
}
