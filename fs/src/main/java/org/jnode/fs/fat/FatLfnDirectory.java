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

package org.jnode.fs.fat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ReadOnlyFileSystemException;

/**
 * @author gbin
 */
public class FatLfnDirectory extends FatDirectory {
    private final HashMap<String, LfnEntry> shortNameIndex = new HashMap<>();
    private final HashMap<String, LfnEntry> longFileNameIndex = new HashMap<>();

    /**
     * @param fs the fs
     * @param file the file
     * @throws IOException when an error occurs
     */
    public FatLfnDirectory(FatFileSystem fs, FatFile file) throws IOException {
        super(fs, file);
    }

    // for root
    public FatLfnDirectory(FatFileSystem fs, int nrEntries) {
        super(fs, nrEntries);
    }

    @Override
    public FSEntry addFile(String name) throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException("addFile in readonly filesystem");
        }

        name = name.trim();
        String shortName = generateShortNameFor(name);
        FatDirEntry realEntry = new FatDirEntry(this, splitName(shortName), splitExt(shortName));
        LfnEntry entry = new LfnEntry(this, realEntry, name);
        shortNameIndex.put(shortName, entry);
        longFileNameIndex.put(name, entry);
        setDirty();
        flush();
        return entry;
    }

    @Override
    public FSEntry addDirectory(String name) throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException("addDirectory in readonly filesystem");
        }

        name = name.trim();
        String shortName = generateShortNameFor(name);
        FatDirEntry realEntry = new FatDirEntry(this, splitName(shortName), splitExt(shortName));

        final long parentCluster;
        if (file == null) {
            parentCluster = 0;
        } else {
            parentCluster = file.getStartCluster();
        }

        final int clusterSize = getFatFileSystem().getClusterSize();
        realEntry.setFlags(FatConstants.F_DIRECTORY);
        final FatFile file = realEntry.getFatFile();
        file.setLength(clusterSize);

        // TODO optimize it also to use ByteBuffer at lower level
        // final byte[] buf = new byte[clusterSize];
        final ByteBuffer buf = ByteBuffer.allocate(clusterSize);

        // Clean the contents of this cluster to avoid reading strange data
        // in the directory.
        // file.write(0, buf, 0, buf.length);
        file.write(0, buf);

        file.getDirectory().initialize(file.getStartCluster(), parentCluster);

        LfnEntry entry = new LfnEntry(this, realEntry, name);
        shortNameIndex.put(shortName, entry);
        longFileNameIndex.put(name, entry);
        setDirty();
        flush();
        return entry;
    }

    @Override
    public FSEntry getEntry(String name) {
        // System.out.println("Search : " + name);
        name = name.trim();
        FSEntry entry;
        // try first as a long file name
        entry = longFileNameIndex.get(name);
        if (entry == null)
            return shortNameIndex.get(name.toUpperCase());
        else
            return entry;

    }

    @Override
    protected synchronized void read(byte[] src) {
        super.read(src);
        readLFN();
    }

    private void readLFN() {
        // System.out.println("Read LFN");
        int i = 0;
        int size = entries.size();

        while (i < size) {
            // jump over empty entries
            while (i < size && entries.get(i) == null) {
                i++;
            }

            if (i >= size) {
                break;
            }

            int offset = i; // beginning of the entry
            // check when we reach a real entry
            while (entries.get(i) instanceof FatLfnDirEntry) {
                // System.out.println(" Jumped over : " + entries.get(i));
                i++;
                if (i >= size) {
                    // This is a cut entry, forgive it
                    break;
                }
            }
            i++;
            if (i >= size) {
                // This is a cut entry, forgive it
                break;
            }

            LfnEntry current = new LfnEntry(this, entries, offset, i - offset);

            if (!current.isDeleted() && current.isValid()) {
                shortNameIndex.put(current.getRealEntry().getName(), current);
                longFileNameIndex.put(current.getName(), current);
            }
        }

    }

    private void updateLFN() throws IOException {
        Vector<FatBasicDirEntry> destination = new Vector<>();

        for (LfnEntry currentEntry : shortNameIndex.values()) {
            FatBasicDirEntry[] encoded = currentEntry.compactForm();
            Collections.addAll(destination, encoded);
        }

        final int size = destination.size();
        if (entries.size() < size) {
            if (!canChangeSize(size)) {
                throw new IOException("Directory is full");
            }
        }

        boolean useAdd = false;
        for (int i = 0; i < size; i++) {
            if (!useAdd) {
                try {
                    entries.set(i, destination.get(i));
                } catch (ArrayIndexOutOfBoundsException aEx) {
                    useAdd = true;
                }
            }
            if (useAdd) {
                entries.add(i, destination.get(i));
            }
        }

        final int entireSize = entries.size();
        for (int i = size; i < entireSize; i++) {
            entries.set(i, null); // remove stale entries
        }

    }

    @Override
    public void flush() throws IOException {
        updateLFN();
        super.flush();
    }

    @Override
    public Iterator<FSEntry> iterator() {
        return new Iterator<>() {
            final Iterator<LfnEntry> it = shortNameIndex.values().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public FSEntry next() {
                return it.next();
            }

            /**
             * @see java.util.Iterator#remove()
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /*
     * It's in the DOS manual!(DOS 5: page 72) Valid: A..Z 0..9 _ ^ $ ~ ! # % & - {} () @ ' `
     * 
     * Invalid: spaces/periods,
     */

    public String generateShortNameFor(String longFullName) {
        int dotIndex = longFullName.lastIndexOf('.');

        String longName;
        String longExt;

        if (dotIndex == -1) {
            // No dot in the name
            longName = longFullName;
            longExt = ""; // so no extension
        } else {
            // split it at the dot
            longName = longFullName.substring(0, dotIndex);
            longExt = longFullName.substring(dotIndex + 1);
        }

        String shortName = longName;
        String shortExt = longExt;

        // make the extension short
        if (shortExt.length() > 3) {
            shortExt = shortExt.substring(0, 3);
        }

        // make the ~n name short
        if (shortName.length() > 8) {
            // trim it
            char[] shortNameChar = shortName.substring(0, 7).toUpperCase().toCharArray();

            // epurate it from alien characters
        loop:
            for (int i = 0; i < shortNameChar.length; i++) {
                char toTest = shortNameChar[i];
            valid:
                {
                    if (toTest > 255)
                        break valid;
                    if (toTest == ' ')
                        break valid;
                    if (toTest >= 'A' && toTest <= 'Z')
                        continue;
                    if (toTest >= '0' && toTest <= '9')
                        continue;
                    if (toTest == '_' || toTest == '^' || toTest == '$' || toTest == '~' ||
                        toTest == '!' || toTest == '#' || toTest == '%' || toTest == '&' ||
                        toTest == '-' || toTest == '{' || toTest == '}' || toTest == '(' ||
                        toTest == ')' || toTest == '@' || toTest == '\'' || toTest == '`')
                        continue;

                }
                shortNameChar[i] = '_';

            }

            // name range from "nnnnnn~1" to "~9999999"
            for (int i = 1; i <= 99999999; i++) {
                String tildeStuff = "~" + i;
                int tildeStuffLength = tildeStuff.length();
                System.arraycopy(tildeStuff.toCharArray(), 0, shortNameChar, 7 - tildeStuffLength,
                    tildeStuffLength);
                shortName = new String(shortNameChar);
                if (!shortNameIndex.containsKey(shortName + "." + shortExt))
                    break;
            }
        }

        String shortFullName = shortName + "." + shortExt;
        return shortFullName.toUpperCase();
    }

    /**
     * Remove the entry with the given name from this directory.
     *
     * @param name the name
     * @throws IOException when an error occurs
     */
    @Override
    public void remove(String name) throws IOException {
        name = name.trim();
        LfnEntry byLongName = longFileNameIndex.get(name);

        if (byLongName != null) {
            longFileNameIndex.remove(name);
            shortNameIndex.remove(byLongName.getRealEntry().getName());
            return;
        }

        String uppedName = name.toUpperCase();
        LfnEntry byShortName = shortNameIndex.get(uppedName);

        if (byShortName != null) {
            longFileNameIndex.remove(byShortName.getName());
            shortNameIndex.remove(uppedName);
        }
        throw new FileNotFoundException(name);
    }

}
