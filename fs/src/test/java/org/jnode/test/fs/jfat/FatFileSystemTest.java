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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.jfat.FatFileSystem;
import org.jnode.fs.jfat.FatFileSystemType;
import org.jnode.partitions.PartitionTable;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@PropsEntity(url = "file://${user.dir}/local.properties")
public class FatFileSystemTest {

    @Property
    String dmg;

    @Property
    String nhd;

    @BeforeEach
    void before() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Test
    public void testReadFat32Disk() throws Exception {

        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/jfat/test.fat32"), "r");
        FatFileSystemType type = FileSystemType.lookup(FatFileSystemType.class);
        FatFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol: total:-1 free:-1
                          ;\s
                            dir1;\s
                              test.txt; 18; 80aeb09eb86de4c4a7d1f877451dc2a2
                            dir2;\s
                              test.txt; 18; 1b20f937ce4a3e9241cc907086169ad7
                            test.txt; 18; fd99fcfc86ba71118bd64c2d9f4b54a4
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadFat16Disk() throws Exception {

        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/jfat/test.fat16"), "r");
        FatFileSystemType type = FileSystemType.lookup(FatFileSystemType.class);
        FatFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol: total:-1 free:-1
                          ;\s
                            dir1;\s
                              test.txt; 18; 80aeb09eb86de4c4a7d1f877451dc2a2
                            dir2;\s
                              test.txt; 18; 1b20f937ce4a3e9241cc907086169ad7
                            test.txt; 18; fd99fcfc86ba71118bd64c2d9f4b54a4
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    @EnabledIf("localPropertiesExists")
    public void testReadFromPartition() throws Exception {
Debug.println(dmg);
//        File file = new File(nhd);
        File file = new File(dmg);
//        File file = FileSystemTestUtils.getTestFile("org/jnode/test/fs/jfat/test.fat32");
        FileDevice device = new FileDevice(file, "r");

        FileSystem<?> fs = PartitionTable.getFileSystem(device, 0);
Debug.println("\nfile system: " + fs);

        StringBuilder actual = new StringBuilder();
        DataStructureAsserts.buildStructure(fs.getRootEntry(), actual, "");
Debug.println("\n: " + actual);

        assertNotNull(fs);
    }
}
