package org.jnode.fs.jfat;

import java.io.IOException;
import java.util.NoSuchElementException;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;

public class FatEntriesFactory {

    private static final Logger logger = System.getLogger(FatEntriesFactory.class.getName());

    private boolean label;
    private int index;
    private int next;
    private FatEntry entry;
    protected final boolean includeDeleted;
    private final FatDirectory directory;

    protected FatEntriesFactory(FatDirectory directory, boolean includeDeleted) {
        label = false;
        index = 0;
        next = 0;
        entry = null;
        this.includeDeleted = includeDeleted;
        this.directory = directory;
    }

    protected boolean hasNextEntry() {
        int i;
        FatDirEntry e;
        FatRecord v = new FatRecord();

        if (index > FatDirectory.MAXENTRIES)
            logger.log(Level.TRACE, "Full Directory: invalid index " + index);

        for (i = index;; ) {
                //
                // create a new entry from the chain
                //
            try {
                e = directory.getFatDirEntry(i, includeDeleted);
                i++;
            } catch (NoSuchElementException ex) {
                entry = null;
logger.log(Level.TRACE, ex.getMessage(), ex);
                return false;
            } catch (IOException ex) {
                logger.log(Level.INFO, "cannot read entry " + i + ", " + ex.getMessage());
                logger.log(Level.TRACE, ex.getMessage(), ex);
                i++;
                continue;
            }

            if (e.isFreeDirEntry() && e.isLongDirEntry() && includeDeleted) {
                // Ignore damage on deleted long directory entries
                ((FatLongDirEntry) e).setDamaged(false);
            }

            if (e.isFreeDirEntry() && !includeDeleted) {
                v.clear();
            } else if (e.isLongDirEntry()) {
                FatLongDirEntry l = (FatLongDirEntry) e;
                if (l.isDamaged()) {
                    logger.log(Level.DEBUG, "Damaged entry at " + (i - 1));
                    v.clear();
                } else {
                    v.add(l);
                }
            } else if (e.isShortDirEntry()) {
                FatShortDirEntry s = (FatShortDirEntry) e;
                if (s.isLabel()) {
                    if (directory.isRoot()) {
                        FatRootDirectory r = (FatRootDirectory) directory;
                        if (label) {
                            logger.log(Level.DEBUG, "Duplicated label in root directory");
                        } else {
                            r.setEntry(s);
                            label = true;
                        }
                    } else {
                        logger.log(Level.DEBUG, "Volume label in non root directory");
                    }
                } else {
                    break;
                }
            } else if (e.isLastDirEntry()) {
                entry = null;
logger.log(Level.TRACE, "entry is last entry");
                return false;
            } else
                throw new UnsupportedOperationException("FatDirEntry is of unknown type, shouldn't happen");
        }

        if (!e.isShortDirEntry())
            throw new UnsupportedOperationException("shouldn't happen");

        v.close((FatShortDirEntry) e);

        //
        // here recursion is in action for the entries factory it creates
        // directory nodes and file leafs
        //
        if (((FatShortDirEntry) e).isDirectory())
            this.entry = new FatDirectory(directory.getFatFileSystem(), directory, v);
        else
            this.entry = new FatFile(directory.getFatFileSystem(), directory, v);

        this.next = i;

        return true;
    }

    protected FatEntry createNextEntry() {
        if (index == next)
            hasNextEntry();
        if (entry == null)
            throw new NoSuchElementException();
        index = next;
        return entry;
    }
}