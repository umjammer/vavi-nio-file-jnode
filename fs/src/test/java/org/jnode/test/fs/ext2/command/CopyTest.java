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

package org.jnode.test.fs.ext2.command;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Sorry, this is not a proper JNode command...
 * @author Andras Nagy
 */
public class CopyTest {

    static void copyTest(String fname, String fname2) throws IOException {
        byte[] bbuf = new byte[1024];

        FileInputStream fis = new FileInputStream(fname);
        FileOutputStream fos = new FileOutputStream(fname2, false);
        while (fis.available() > 0) {
            System.out.print(".");
            int len = fis.read(bbuf);
            fos.write(bbuf, 0, len);
        }

        fis.close();
        fos.close();
    }

    public static void main(String[] args) throws IOException {
        String fname, fname2;
        if (args.length >= 2) {
            fname = args[0];
            fname2 = args[1];
        } else {
            System.out.println("copyTest fromFile toFile");
            return;
        }

        copyTest(fname, fname2);
    }
}
