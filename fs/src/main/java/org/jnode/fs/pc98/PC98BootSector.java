/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.fs.pc98;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.jfat.BootSector;
import vavi.util.serdes.Serdes;
import vavix.io.fat.PC98BiosParameterBlock;

import static java.lang.System.getLogger;


/**
 * PC98BootSector.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/08 umjammer initial version <br>
 */
public class PC98BootSector implements BootSector {

    private static final Logger logger = getLogger(PC98BootSector.class.getName());

    private PC98BiosParameterBlock bpb;

    private boolean dirty;

    public PC98BootSector() {
        dirty = false;
    }

    @Override
    public boolean isaValidBootSector() {
logger.log(Level.DEBUG, "bpb.fileSystem: " + bpb.fileSystem);
        boolean r = validate(bpb);
if (!r) { logger.log(Level.DEBUG, "validation failed: " + bpb); }
        return r;
    }

    /**
     * true: do default validation,
     * false: no validation,
     * else: validation function name "class#method", the method must return boolean and
     *       w/ an argument PC98BiosParameterBlock and static.
     */
    public static final String VALIDATION_KEY = "org.jnode.fs.pc98.PC98BootSector.validation";

    /** @see #VALIDATION_KEY */
    private static boolean validate(PC98BiosParameterBlock bpb) {
        String validation = System.getProperty(VALIDATION_KEY, "true");
        if (Boolean.parseBoolean(validation)) {
            logger.log(Level.DEBUG, "default validation");
            return bpb.fileSystem.contains("FAT");
        } else if (validation.equalsIgnoreCase("false")) {
            logger.log(Level.DEBUG, "no validation, accepting anyway");
            return true;
        } else {
            try {
                String[] parts = validation.split("#");
                Class<?> clazz = Class.forName(parts[0]);
                Method method = clazz.getDeclaredMethod(parts[1], PC98BiosParameterBlock.class);
                if (method.getReturnType() != Boolean.TYPE) {
                    throw new IllegalArgumentException("method %s return type is not boolean but %s".formatted(method.getName(), method.getReturnType().getName()));
                }
                boolean r = method.invoke(null, bpb).equals(Boolean.TRUE);
                logger.log(Level.DEBUG, "do user bpb validation %s#%s: %s".formatted(clazz.getSimpleName(), method.getName(), r));
                return r;
            } catch (Exception e) {
                logger.log(Level.WARNING, "validation function error, accepting anyway", e);
                return true;
            }
        }
    }

    @Override
    public void read(BlockDeviceAPI device) throws IOException {
        byte[] sector = new byte[1024];
        device.read(0, ByteBuffer.wrap(sector));
        ByteArrayInputStream bais = new ByteArrayInputStream(sector);

        this.bpb = new PC98BiosParameterBlock();
        Serdes.Util.deserialize(bais, bpb);
logger.log(Level.DEBUG, "■ bootRecord ----\n" + bpb);
        bpb.compute();
logger.log(Level.DEBUG, "■ bootRecord ----\n" + bpb);

        dirty = false;
    }

    @Override
    public void write(BlockDeviceAPI device, long offset) throws IOException {
        device.write(offset, ByteBuffer.wrap(null)); // TODO
        dirty = false;
    }

    @Override
    public String fatType() {
        return bpb.getFatType().toString().toUpperCase().replaceFirst("FAT$", "");
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public boolean isFat12() {
        return bpb.getFatType() == vavix.io.fat.FatType.Fat12Fat;
    }

    @Override
    public boolean isFat16() {
        return bpb.getFatType() == vavix.io.fat.FatType.Fat16Fat;
    }

    @Override
    public boolean isFat32() {
        return bpb.getFatType() == vavix.io.fat.FatType.Fat32Fat;
    }

    @Override
    public int fatSize() {
        return Integer.parseInt(fatType().replaceFirst("^FAT", ""));
    }

    @Override
    public int getMediumDescriptor() {
        return bpb.mediaDescriptor;
    }

    @Override
    public long getSectorsPerFat() {
        return bpb.numberOfFATSector;
    }

    @Override
    public int getBytesPerSector() {
        return bpb.getBytesPerSector();
    }

    @Override
    public int getClusterSize() {
        return getSectorsPerCluster() * getBytesPerSector();
    }

    @Override
    public int getSectorsPerCluster() {
        return bpb.getSectorsPerCluster();
    }

    @Override
    public int getNrReservedSectors() {
        return bpb.reservedSectors;
    }

    @Override
    public int getNrFats() {
        return bpb.numberOfFAT;
    }

    @Override
    public long getRootDirectoryStartCluster() {
        return bpb.getStartClusterOfRootDirectory();
    }

    @Override
    public long getCountOfClusters() {
        return bpb.countOfClusters;
    }

    @Override
    public long getFirstDataSector() {
        return bpb.firstDataSector;
    }

    @Override
    public long getNrRootDirEntries() {
        return bpb.maxRootDirectoryEntries;
    }

    @Override
    public String getVolumeLabel() {
        return bpb.volumeLabel;
    }

    @Override
    public long getVolumeId() {
        return bpb.volumeSerialID;
    }

    @Override
    public String getOEMName() {
        return bpb.oemLabel;
    }

    @Override
    public void setBS_JmpBoot(byte[] BS_jmpBoot) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBS_OemName(String BS_OEMName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_BytesPerSector(int BPB_BytsPerSec) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_SecPerCluster(int BPB_SecPerClus) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_RsvSecCount(int BPB_RsvdSecCnt) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_NoFATs(int BPB_NumFATs) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_RootEntCnt(int BPB_RootEntCnt) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_TotSec16(int BPB_TotSec16) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_MediumDescriptor(int BPB_Media) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_FATSz16(int BPB_FATSz16) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_SecPerTrk(int BPB_SecPerTrk) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_NumHeads(int BPB_NumHeads) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_HiddSec(long BPB_HiddSec) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_TotSec32(long BPB_TotSec32) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_FATSz32(long BPB_FATSz32) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_ExtFlags(int BPB_ExtFlags) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_FSVer(int BPB_FSVer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_RootClus(long BPB_RootClus) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_FSInfo(int BPB_FSInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_BkBootSec(int BPB_BkBootSec) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBPB_Reserved(byte[] BPB_Reserved) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBS_DrvNum(int BS_DrvNum) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBS_Reserved1(int BS_Reserved1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBS_BootSig(int BS_BootSig) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBS_VolID(long BS_VolID) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBS_VolLab(String BS_VolLab) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBS_FilSysType(String BS_FilSysType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBS_Identifier(byte[] ident) {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        return bpb.toString();
    }
}
