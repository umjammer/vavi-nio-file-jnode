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

package org.jnode.test.fs.jfat;

import java.io.File;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.jfat.FatFileSystem;
import org.jnode.fs.jfat.FatFileSystemType;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FatFileSystemTest {

    private Device device;

    @Test
    public void testReadFat32Disk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/jfat/test.fat32"), "r");
        FatFileSystemType type = FileSystemType.lookup(FatFileSystemType.class);
        FatFileSystem fs = type.create(device, true);

        String expectedStructure =
            "vol: total:-1 free:-1\n" +
                "  ; \n" +
                "    dir1; \n" +
                "      test.txt; 18; 80aeb09eb86de4c4a7d1f877451dc2a2\n" +
                "    dir2; \n" +
                "      test.txt; 18; 1b20f937ce4a3e9241cc907086169ad7\n" +
                "    test.txt; 18; fd99fcfc86ba71118bd64c2d9f4b54a4\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadFat16Disk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/jfat/test.fat16"), "r");
        FatFileSystemType type = FileSystemType.lookup(FatFileSystemType.class);
        FatFileSystem fs = type.create(device, true);

        String expectedStructure =
            "vol: total:-1 free:-1\n" +
                "  ; \n" +
                "    dir1; \n" +
                "      test.txt; 18; 80aeb09eb86de4c4a7d1f877451dc2a2\n" +
                "    dir2; \n" +
                "      test.txt; 18; 1b20f937ce4a3e9241cc907086169ad7\n" +
                "    test.txt; 18; fd99fcfc86ba71118bd64c2d9f4b54a4\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    @Disabled
    public void testReadFat32Disk2() throws Exception {

        device = new FileDevice(new File("/Users/nsano/src/vavi/vavi-nio-file-fat/src/test/resources/fat32.dmg"), "r");


        FatFileSystemType type = FileSystemType.lookup(FatFileSystemType.class);
        FatFileSystem fs = type.create(device, true);

        String expectedStructure =
            "tvol: total:-1 free:-1\n" +
                "  ; \n" +
                "    dir1; \n" +
                "      test.txt; 18; 80aeb09eb86de4c4a7d1f877451dc2a2\n" +
                "    dir2; \n" +
                "      test.txt; 18; 1b20f937ce4a3e9241cc907086169ad7\n" +
                "    test.txt; 18; fd99fcfc86ba71118bd64c2d9f4b54a4\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }
}
