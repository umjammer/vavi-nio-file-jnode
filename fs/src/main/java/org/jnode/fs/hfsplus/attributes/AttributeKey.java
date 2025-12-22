package org.jnode.fs.hfsplus.attributes;

import org.jnode.fs.hfsplus.HfsUnicodeString;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import vavi.util.ByteUtil;


/**
 * An attributes file key (HFSPlusAttrKey).
 *
 * @author Luke Quinane
 */
public class AttributeKey extends AbstractKey {

    /**
     * The key length.
     */
    public static final int KEY_LENGTH = 14 + 127 + 1;

    /**
     * The padding value.
     */
    private int pad;

    /**
     * The file ID.
     */
    private final CatalogNodeId fileId;

    /**
     * The start block.
     */
    private long startBlock;

    /**
     * The attribute name.
     */
    private final HfsUnicodeString attributeName;

    /**
     * Reads in an attribute key.
     *
     * @param src the source buffer.
     * @param offset the offset to read from.
     */
    public AttributeKey(byte[] src, int offset) {
        keyLength = (ByteUtil.readBeShort(src, offset) & 0xffff) + 2;
        pad = ByteUtil.readBeShort(src, offset + 2) & 0xffff;
        fileId = new CatalogNodeId(src, offset + 4);
        startBlock = ByteUtil.readBeInt(src, offset + 8) & 0xffff_ffffL;
        attributeName = new HfsUnicodeString(src, offset + 0xc);
    }

    /**
     * Creates a new attribute key.
     *
     * @param fileId the file ID.
     * @param attributeName the attribute name.
     */
    public AttributeKey(CatalogNodeId fileId, String attributeName) {
        this.fileId = fileId;
        this.attributeName = new HfsUnicodeString(attributeName);
    }

    public CatalogNodeId getFileId() {
        return fileId;
    }

    public HfsUnicodeString getAttributeName() {
        return attributeName;
    }

    @Override
    public int compareTo(Key key) {
        int result = -1;
        if (key instanceof AttributeKey otherKey) {
            result = getFileId().compareTo(otherKey.getFileId());
            if (result == 0) {
                // Note: this is unlikely to be correct. See TN1150 section "Unicode Subtleties" for details
                // For reading in data is should be safe since the B-Tree will be pre-sorted, but for adding new entries
                // it will cause the order to be wrong.
                result = this.getAttributeName().getUnicodeString()
                    .compareTo(otherKey.getAttributeName().getUnicodeString());
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return 789 ^ fileId.hashCode() + attributeName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AttributeKey otherKey)) {
            return false;
        }

        return
            fileId.getId() == otherKey.fileId.getId() &&
            attributeName.getUnicodeString().equals(otherKey.getAttributeName().getUnicodeString());
    }

    @Override
    public byte[] getBytes() {
        int length = this.getKeyLength();
        byte[] data = new byte[length];
        ByteUtil.writeBeShort((short) length, data, 0);
        ByteUtil.writeBeShort((short) pad, data, 2);
        System.arraycopy(fileId.getBytes(), 0, data, 4, 4);
        ByteUtil.writeBeInt((int) startBlock, data, 8);
        System.arraycopy(attributeName.getBytes(), 0, data, 0xc, (attributeName.getLength() * 2) + 2);
        return data;
    }

    @Override
    public String toString() {
        return "[length: %d, file-id: %d, attribute-name: '%s']".formatted(getKeyLength(), getFileId().getId(),
            getAttributeName());
    }
}
