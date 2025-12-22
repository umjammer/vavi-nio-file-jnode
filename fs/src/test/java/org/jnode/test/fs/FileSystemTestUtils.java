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

package org.jnode.test.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import org.jnode.util.FileUtils;

import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * File system test utilities.
 */
public class FileSystemTestUtils {

    /**
     * Gets a file system test file.
     *
     * @param testFile the test file to get. E.g. "ntfs/test.ntfs".
     * @return the file.
     * @throws IOException if an error occurs.
     */
    public static File getTestFile(String testFile) throws IOException {
        Path dir = Paths.get("tmp/images");
        Path file = dir.resolve(testFile);
Debug.println("result out: " + file);
        if (Files.exists(file)) {
            return file.toFile();
        }

        Path srcDir = Paths.get("src/test/resources");
        Path normalFile = srcDir.resolve(testFile);
        if (Files.exists(normalFile)) {
            return normalFile.toFile();
        }

        // Look for the gzip file.
        Path gzipFile = srcDir.resolve(testFile + ".gz");
Debug.println("source gz: " + file);
        if (Files.exists(gzipFile)) {
            explodeGzip(gzipFile, file);
            return file.toFile();
        }

        fail("Expected a gzipped file: " + gzipFile.toAbsolutePath());
        return null;
    }

    /**
     * Explodes a GZIP file to a file.
     *
     * @param gzipFile   the source GZIP file.
     * @param outputFile the destination file.
     * @throws java.io.IOException if there was an error exploding the GZIP file.
     */
    private static synchronized void explodeGzip(Path gzipFile, Path outputFile) throws IOException {
        Path tempFile = outputFile.getParent().resolve(outputFile.getFileName() + ".tmp");

        if (!Files.exists(outputFile) || Files.getLastModifiedTime(gzipFile).compareTo(Files.getLastModifiedTime(outputFile)) > 0) {
            if (Files.exists(outputFile)) {
                // Force deletion if it's out of date or the renameTo further down will fail.
                Files.delete(outputFile);
            }
            if (!Files.exists(outputFile.getParent())) {
                Files.createDirectories(outputFile.getParent());
            }

            try (InputStream in = new GZIPInputStream(Files.newInputStream(gzipFile))) {
                try (OutputStream out = Files.newOutputStream(tempFile)) {
                    FileUtils.copy(in, out, new byte[0x10000], false);
                }
            }

            Files.move(tempFile, outputFile);
            assertTrue(Files.exists(outputFile),
                       "Temp data file couldn't be renamed.\nOld name: %s\nNew name: %s".formatted(tempFile, outputFile));
        }
    }
}
