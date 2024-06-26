package org.jnode.fs.jfat;

import java.util.Iterator;
import org.jnode.fs.FSEntry;

public class FatEntriesIterator extends FatEntriesFactory implements Iterator<FSEntry> {

    private final FatTable fatTable;

    public FatEntriesIterator(FatTable fatTable, FatDirectory directory, boolean includeDeleted) {
        super(directory, includeDeleted);

        this.fatTable = fatTable;
    }

    @Override
    public boolean hasNext() {
        return hasNextEntry();
    }

    @Override
    public FSEntry next() {
        if (includeDeleted) {
            return createNextEntry();
        } else {
            return fatTable.look(createNextEntry());
        }
    }

}