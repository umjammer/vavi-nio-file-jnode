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

package org.jnode.fs.jfat;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FileSystemException;

import static java.lang.System.getLogger;


/**
 * @author gvt
 */
public abstract class Fat {

    private static final Logger logger = getLogger(Fat.class.getName());

    private final BlockDeviceAPI api;
    private final BootSector bs;

    private final FatCache cache;

    private int lastFree;

    private final ByteBuffer clearBuf;

    protected Fat(BootSector bs, BlockDeviceAPI api) {
        this.bs = bs;
        this.api = api;

        /*
         * create a suitable cache
         */
        cache = new FatCache(this, 8192, 512);

        /*
         * set last-free
         */
        rewindFree();

        /*
         * and blank the clear buffer
         */
        byte[] clearData = new byte[getClusterSize()];
        Arrays.fill(clearData, 0, clearData.length, (byte) 0x00);

        /*
         * setup the clear buffer
         */
        clearBuf = ByteBuffer.wrap(clearData).asReadOnlyBuffer();
    }

    public static Fat create(BlockDeviceAPI api, BootSector bs) throws IOException {

        bs.read(api);

        if (bs.isFat32()) {
            return new Fat32(bs, api);
        } else if (bs.isFat16()) {
            return new Fat16(bs, api);
        } else if (bs.isFat12()) {
            return new Fat12(bs, api);
        }

        throw new FileSystemException("FAT not recognized");
    }

    public final BootSector getBootSector() {
        return bs;
    }

    public final BlockDeviceAPI getApi() {
        return api;
    }

    public final int getClusterSize() {
        return getBootSector().getBytesPerSector() * getBootSector().getSectorsPerCluster();
    }

    public final long getFirstSector(int fatNum) {
        if (fatNum < 0 || fatNum >= getBootSector().getNrFats()) {
            throw new IndexOutOfBoundsException("illegal fat: " + fatNum);
        }
        return (long) getBootSector().getNrReservedSectors() + getBootSector().getSectorsPerFat() * (long) fatNum;
    }

    public final boolean isFirstSector(int fatNum, long sector) {
        return (sector == getFirstSector(fatNum));
    }

    public final long getLastSector(int fatNum) {
        return getFirstSector(fatNum) + getBootSector().getSectorsPerFat() - 1;
    }

    public final boolean isLastSector(int fatNum, long sector) {
        return (sector == getLastSector(fatNum));
    }

    public final long getFirst(int fatNum) {
        return getFirstSector(fatNum) * (long) getBootSector().getBytesPerSector();
    }

    public final long getLast(int fatNum) {
        return getLastSector(fatNum) + offset(size() - 1);
    }

    protected final long position(int fatNum, int index) throws IOException {
        if (index < 0 || index >= size()) {
            throw new IllegalArgumentException("illegal entry: " + index);
        }
        return getFirst(fatNum) + offset(index);
    }

    public void readCluster(int cluster, int offset, ByteBuffer dst) throws IOException {
        if (offset < 0) {
            throw new IllegalArgumentException("offset<0");
        }
logger.log(Logger.Level.TRACE, "cluster: " + cluster);
        if ((offset + dst.remaining()) > getClusterSize()) {
            throw new IllegalArgumentException("length[" + (offset + dst.remaining()) + "] " +
                "exceed clusterSize[" + getClusterSize() + "]");
        }

        getApi().read(getClusterPosition(cluster) + offset, dst);
    }

    public void writeCluster(int cluster, int offset, ByteBuffer src) throws IOException {
        if (offset < 0) {
            throw new IllegalArgumentException("offset<0");
        }

        if ((offset + src.remaining()) > getClusterSize()) {
            throw new IllegalArgumentException("length[" + (offset + src.remaining()) + "] " +
                "exceed clusterSize[" + getClusterSize() + "]");
        }

        getApi().write(getClusterPosition(cluster) + offset, src);
    }

    public void clearCluster(int cluster, int start, int end) throws IOException {
        if (start < 0) {
            throw new IllegalArgumentException("start<0");
        }

        if (end < start) {
            throw new IllegalArgumentException("end<start " + start + " " + end);
        }

        if (end > getClusterSize()) {
            throw new IllegalArgumentException("end[" + end + "] " + "exceed clusterSize[" +
                getClusterSize() + "]");
        }

        clearBuf.clear();
        clearBuf.limit(end - start);

        writeCluster(cluster, start, clearBuf);
    }

    public void clearCluster(int cluster) throws IOException {
        clearCluster(cluster, 0, getClusterSize());
    }

    public final int firstCluster() {
        return 2;
    }

    public final long getClusterSector(int index) {
        if (index < firstCluster() || index >= size()) {
            throw new IllegalArgumentException("illegal cluster # : " + index);
        }

logger.log(Level.TRACE, "sector: " + (long) (index - firstCluster()) * (long) bs.getSectorsPerCluster() +
              getBootSector().getFirstDataSector());
        return (long) (index - firstCluster()) * (long) bs.getSectorsPerCluster() +
            getBootSector().getFirstDataSector();
    }

    public abstract long getClusterPosition(int index);

    /** count of clusters (w/ first cluster) */
    public final int size() {
        return (int) (bs.getCountOfClusters() + firstCluster());
    }

    protected abstract long offset(int index);

    public abstract boolean isEofChain(int entry);

    public abstract int eofChain();

    public boolean hasNext(int entry) {
        // cluster 0(zero) and 1(one) are EndOfChains!
        if ((entry == 0) || (entry == 1)) {
            return false;
        }
        return !isEofChain(entry);
    }

    public final int freeEntry() {
        return 0;
    }

    public final boolean isFree(int entry) {
        return (entry == freeEntry());
    }

    public long getUInt16(int index) throws IOException {
        return cache.getUInt16(index);
    }

    public long getUInt32(int index) throws IOException {
        return cache.getUInt32(index);
    }

    public void setInt16(int index, int element) throws IOException {
        cache.setInt16(index, element);
    }

    public void setInt32(int index, int element) throws IOException {
        cache.setInt32(index, element);
    }

    public abstract int get(int index) throws IOException;

    public abstract int set(int index, int element) throws IOException;

    public void flush() throws IOException {
        cache.flush();
    }

    public final boolean isFreeEntry(int entry) throws IOException {
        return isFree(get(entry));
    }

    public final int getLastFree() {
        return lastFree;
    }

    public final void setLastFree(int value) {
        lastFree = value;
    }

    public final void rewindFree() {
        lastFree = firstCluster();
    }

    public final int freeEntries() throws IOException {
        int count = 0;
        for (int i = 0; i < size(); i++) {
            if (isFreeEntry(i)) {
                count++;
            }
        }
        return count;
    }

    public final boolean isFat32() {
        return getBootSector().isFat32();
    }

    public final boolean isFat16() {
        return getBootSector().isFat16();
    }

    public final boolean isFat12() {
        return getBootSector().isFat12();
    }

    public String getCacheStat() {
        try (StrWriter out = new StrWriter()) {
            out.println("Access: " + cache.getAccess() + " Hits: " + cache.getHit() + " Ratio: " +
                cache.getRatio() * 100 + "%");
            return out.toString();
        }
    }

    public String toString() {
        return "FAT cluster:%d\nboot sector:\n%s".formatted(getClusterSize(), getBootSector());
    }

    public String toDebugString() {
        try (StrWriter out = new StrWriter()) {

            out.println("***************************  Fat   **************************");
            out.println(getBootSector());
            out.println("ClusterSize\t" + getClusterSize());
            out.println("Size\t\t" + size());
            out.print("FirstSector");
            for (int i = 0; i < getBootSector().getNrFats(); i++)
                out.print("\t" + getFirstSector(i));
            out.println();
            out.print("LastSector");
            for (int i = 0; i < getBootSector().getNrFats(); i++)
                out.print("\t" + getLastSector(i));
            out.println();
            // out.println ( "FreeEntries\t" + freeEntries() );
            out.print("*************************************************************");
            return out.toString();
        }
    }
}
