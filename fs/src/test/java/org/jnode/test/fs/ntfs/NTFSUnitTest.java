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

package org.jnode.test.fs.ntfs;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ntfs.NTFSFileSystemType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests to ensure sanity of Jnode's NTFS implementation.
 *
 * @author Daniel Noll (daniel@nuix.com.au)
 */
public class NTFSUnitTest {

    private final String TEST_IMAGE_PATH = System.getProperty("user.home");
    private final String TEST_IMAGE_FILENAME_2 = TEST_IMAGE_PATH + "/ntfs-sparse-files.001";
    private final String TEST_IMAGE_FILENAME_1 = TEST_IMAGE_PATH + "/ntfs-extreme-fragmentation.001";

//    /**
//     * Run tests invocation method.
//     * @param args the args
//     */
//    public static void main(String[] args){
//         junit.textui.TestRunner.run(NTFSUnitTest.class);
//    }

    /**
     * Tests handling of extreme fragmentation, which results in attribute lists being
     * split among multiple MFT records.
     * <p>The image was created by writing 120 small files, and then creating a big file.
     * Then each small file was deleted individually and randomly.  After each deletion,
     * the size of the big file was grown by 1MB.  Eventually this results in a single
     * 120MB file fragmented into up to 120 chunks (depending on the random numbers.)</p>
     */
    public void testExtremeFragmentation() {
        System.out.println("NTFS : Test extreme fragmentation (" + TEST_IMAGE_FILENAME_1 + ").");
        try {
            File file = new File(TEST_IMAGE_FILENAME_1);
            Device device = new FileDevice(file, "r");
            FileSystem<?> fileSystem = new NTFSFileSystemType().create(device, true);
            FSDirectory root = fileSystem.getRootEntry().getDirectory();

            // Check the big file.  Every byte should be readable as zero, hopefully.
            FSFile bigFile = root.getEntry("bigfile.dat").getFile();
            int increment = 1024 * 1024;
            assertEquals(120 * increment, bigFile.getLength(), "Wrong file length for big file");
            byte[] actual = new byte[increment];
            for (int i = 0; i < 120 * increment; i += increment) {
                bigFile.read(i, ByteBuffer.wrap(actual));
            }

            fileSystem.close();
        } catch (IOException e) {
            fail("Unexpected exception : " + e.getMessage());
        }
    }

    /**
     * Tests sparse file handling.
     * <p>FWIW, the files were created like this on Windows XP:</p>
     * <pre>
     *   fsutil file createnew E:\sparsefile1.dat 10240
     *   fsutil sparse setflag E:\sparsefile1.dat
     *   fsutil file setzerodata offset=0 length=10240 E:\sparsefile1.dat
     * </pre>
     * <p>The data written into the first file was done from Java and a RandomAccessFile.</p>
     */
    public void testSparseFiles() {
        System.out.println("NTFS : Test sparse file (" + TEST_IMAGE_FILENAME_2 + ").");
        try {
            File file = new File(TEST_IMAGE_FILENAME_2);
            Device device = new FileDevice(file, "r");
            FileSystem<?> fileSystem = new NTFSFileSystemType().create(device, true);
            FSDirectory root = fileSystem.getRootEntry().getDirectory();

            // The first file has 256 bytes of real data at the front, and the rest is sparse.
            byte[] expectedContents = new byte[10240];
            for (int i = 0; i < 256; i++) {
                expectedContents[i] = (byte) i;
            }
            FSFile sparseFile1 = root.getEntry("sparsefile1.dat").getFile();
            assertEquals(expectedContents.length, sparseFile1.getLength(), "Wrong length for sparse file 2");
            byte[] actualContents = new byte[expectedContents.length];
            sparseFile1.read(0, ByteBuffer.wrap(actualContents));
            Arrays.fill(actualContents, 256, 4096, (byte) 0); // slack space contains garbage, so wipe it.
            assertEquals(expectedContents, actualContents, "Wrong contents for sparse file 1");
            // The second file is 100% sparse.
            expectedContents = new byte[10240];
            FSFile sparseFile2 = root.getEntry("sparsefile2.dat").getFile();
            assertEquals(expectedContents.length, sparseFile2.getLength(), "Wrong length for sparse file 2");
            actualContents = new byte[expectedContents.length];
            sparseFile2.read(0, ByteBuffer.wrap(actualContents));
            assertEquals(expectedContents, actualContents, "Wrong contents for sparse file 2");
            fileSystem.close();
        } catch (IOException e) {
            fail("Unexpected exception : " + e.getMessage());
        }
    }
}
