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
import java.io.FileInputStream;
import java.io.IOException;
import org.jnode.util.NumberUtils;
import vavi.util.Debug;


/**
 * @author gbin
 */
public class LfnTest {

    private static final String[] fileNames =
        new String[]{
            "This is a long filename test.ext",
            "With a long.extension",
            "With.a.multiple.extensions",
            "short.one",
            ".ext"};

    public static void main(String[] args) throws Exception {
        String directory = args[0];
        System.out.println("Create some files in directory = " + directory);

        // create files
        for (String fileName : fileNames) {
            args[0] = directory + "/" + fileName;
            FileTest.main(args);
        }

        // list files
        listFiles(directory);

        // create a directory
        System.out.println("------------ create dir");
        File directoryToCreate = new File(directory + "/this is a long file name directory");
        directoryToCreate.mkdir();

        listFiles(directory);

        // delete a directory
//        System.out.println("------------ delete dir");
//        File directoryToDelete = new File(directory + "/this is a long file name directory");
//        directoryToCreate.delete();

//        listFiles(directory);

        // delete files
//        System.out.println("------------ remove Files dir");
//        for (int i = 0; i < fileNames.length; i++) {
//            System.out.println("remove file entry = " + directory + "/" + fileNames[i]);
//            new File(directory + "/" + fileNames[i]).delete();
//        }

//        listFiles(directory);
    }

    private static void listFiles(String directory) {
        File dir = new File(directory);
        String[] all = dir.list();
        for (String s : all) {
            File toTest = new File(s);
            System.out.println("dir entry = " + s + " isDirectory = " + toTest.isDirectory());
            if (!toTest.isDirectory()) {
                System.out.print("MiniDump content :");
                byte[] raw = new byte[4];
                try (FileInputStream fis = new FileInputStream(directory + "/" + s)) {
                    fis.read(raw);
                } catch (IOException | SecurityException e1) {
                    Debug.printStackTrace(e1);
                }

                System.out.println("hexdata = " + NumberUtils.hex(raw));
            }
        }
    }
}
