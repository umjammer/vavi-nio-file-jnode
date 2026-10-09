/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.github.fge.filesystem.driver.CachedFileSystemDriver;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-03-08 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String nhd;

    FileSystem fs;
    Map<String, Object> options;

    @BeforeEach
    public void before() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        URI uri = URI.create("jnode:" + Paths.get(nhd).toUri());

        Map<String, Object> env = new HashMap<>();
        env.put(CachedFileSystemDriver.ENV_IGNORE_APPLE_DOUBLE, true); // mandatory

        fs = FileSystems.newFileSystem(uri, env);
//Files.list(fs.getRootDirectories().iterator().next()).forEach(System.err::println);
    }

    @Test
    @Disabled("because of just setting a wrong disk")
    @DisplayName("Files#copy stops in the mid way")
    void test1() throws Exception {

        Path output = Path.of("tmp", "fmdsp.com");

        Path root = fs.getRootDirectories().iterator().next();
        Path usr = root.resolve("USR");
        System.out.println("Contents of /USR:");
        try {
            Files.list(usr).forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Path target = root.resolve("USR").resolve("FMDSP").resolve("FMDSP.COM");
        assertTrue(Files.exists(target), "FMDSP.COM not found under /USR/FMDSP in " + nhd);

        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.copy(target, output, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Extracted: " + target);
        System.out.println("Output   : " + output.toAbsolutePath());
        System.out.println("Size     : " + Files.size(output) + " bytes");
    }
}
