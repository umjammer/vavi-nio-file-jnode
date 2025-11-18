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

package org.jnode.fs.hfsplus.catalog;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.jnode.fs.hfsplus.HfsPlusBSDInfo;
import org.jnode.fs.hfsplus.HfsUtils;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


public class CatalogFolder {

    private static final Logger logger = getLogger(CatalogFolder.class.getName());

    // Types
    public static final int RECORD_TYPE_FOLDER = 0x0001;
    public static final int RECORD_TYPE_FOLDER_THREAD = 0x0003;

    /**
     * The folder type set on hardlinks - 'fdrp'.
     */
    public static final int HARDLINK_FOLDER_TYPE = 0x66647270;

    /**
     * The creator set on hardlinks - 'MACS'.
     */
    public static final int HARDLINK_CREATOR = 0x4d414353;

    public static final int CATALOG_FOLDER_SIZE = 88;

    private int recordType;
    private int flags;
    private long valence;
    private CatalogNodeId folderId;
    private long createDate;
    private long contentModDate;
    private long attrModDate;
    private long accessDate;
    private long backupDate;
    private HfsPlusBSDInfo permissions;

    /**
     * @param src the src
     */
    public CatalogFolder(final byte[] src) {
        byte[] data = new byte[88];
logger.log(Level.TRACE, "src: " + src.length);
        System.arraycopy(src, 0, data, 0, Math.min(CATALOG_FOLDER_SIZE, src.length)); // TODO check
        recordType = ByteUtil.readBeShort(data, 0);
        flags = ByteUtil.readBeShort(data, 2) & 0xffff;
        valence = ByteUtil.readBeInt(data, 4) & 0xffff_ffffL;
        folderId = new CatalogNodeId(data, 8);
        createDate = ByteUtil.readBeInt(data, 12) & 0xffff_ffffL;
        contentModDate = ByteUtil.readBeInt(data, 16) & 0xffff_ffffL;
        attrModDate = ByteUtil.readBeInt(data, 20) & 0xffff_ffffL;
        accessDate = ByteUtil.readBeInt(data, 24) & 0xffff_ffffL;
        backupDate = ByteUtil.readBeInt(data, 28) & 0xffff_ffffL;
        permissions = new HfsPlusBSDInfo(data, 32);
    }

    /**
     * @param valence the valence
     * @param folderID the folderID
     */
    public CatalogFolder(int valence, CatalogNodeId folderID) {
        this.recordType = RECORD_TYPE_FOLDER;
        this.valence = valence;
        this.folderId = folderID;
        this.createDate = HfsUtils.getNow();
        this.contentModDate = HfsUtils.getNow();
        this.attrModDate = HfsUtils.getNow();
    }

    /**
     * Return bytes representation of the catalog folder.
     *
     * @return byte array representation.
     */
    public byte[] getBytes() {
        byte[] data = new byte[88];
        ByteUtil.writeBeShort((short) recordType, data, 0);
        ByteUtil.writeBeInt((int) valence, data, 4);
        System.arraycopy(folderId.getBytes(), 0, data, 8, folderId.getBytes().length);
        ByteUtil.writeBeInt((int) createDate, data, 12);
        ByteUtil.writeBeInt((int) contentModDate, data, 16);
        ByteUtil.writeBeInt((int) attrModDate, data, 20);
        return data;
    }

    @Override
    public String toString() {
        String s = "Record type: " + recordType + "\n" +
                "Valence: " + valence + "\n" +
                "Folder ID: " + folderId.getId() + "\n" +
                "Creation Date :" +
                HfsUtils.printDate(createDate, "EEE MMM d HH:mm:ss yyyy") + "\n" +
                "Content Mod Date  :" +
                HfsUtils.printDate(contentModDate, "EEE MMM d HH:mm:ss yyyy") + "\n" +
                "Attr Mod Date  :" +
                HfsUtils.printDate(attrModDate, "EEE MMM d HH:mm:ss yyyy") + "\n";
        return s;
    }

    public int getRecordType() {
        return recordType;
    }

    public int getFlags() {
        return flags;
    }

    public long getValence() {
        return valence;
    }

    public CatalogNodeId getFolderId() {
        return folderId;
    }

    public long getCreateDate() {
        return HfsUtils.getDate(createDate & 0xffff_ffffL, false) * 1000L;
    }

    public long getContentModDate() {
        return HfsUtils.getDate(contentModDate & 0xffff_ffffL, false) * 1000L;
    }

    public long getAttrModDate() {
        return HfsUtils.getDate(attrModDate & 0xffff_ffffL, false) * 1000L;
    }

    public long getAccessDate() {
        return HfsUtils.getDate(accessDate & 0xffff_ffffL, false) * 1000L;
    }

    public long getBackupDate() {
        return HfsUtils.getDate(backupDate & 0xffff_ffffL, false) * 1000L;
    }

    public HfsPlusBSDInfo getPermissions() {
        return permissions;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }

    public void setValence(long valence) {
        this.valence = valence;
    }

    public void setFolderId(CatalogNodeId folderId) {
        this.folderId = folderId;
    }

    public void setCreateDate(long createDate) {
        this.createDate = HfsUtils.getDate(createDate / 1000L, true);
    }

    public void setContentModDate(long contentModDate) {
        this.contentModDate = HfsUtils.getDate(contentModDate / 1000L, true);
    }

    public void setAttrModDate(long attrModDate) {
        this.attrModDate = HfsUtils.getDate(attrModDate / 1000L, true);
    }

    public void incrementValence() {
        this.setValence(this.getValence() + 1);
    }
}
