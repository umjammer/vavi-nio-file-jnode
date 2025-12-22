/*
 * https://github.com/VivekDudani/java-fs
 */

package org.jnode.fs.xfs;


/**
 * Created by luke on 8/12/2016.
 */
public class UnixFSConstants {

    // i_mode masks and values
    public static final int S_IFMT = 0xf000; // format mask
    public static final int S_IFSOCK = 0xc000; // socket
    public static final int S_IFLNK = 0xA000; // symbolic link
    public static final int S_IFREG = 0x8000; // regular file
    public static final int S_IFBLK = 0x6000; // block device
    public static final int S_IFDIR = 0x4000; // directory
    public static final int S_IFCHR = 0x2000; // character device
    public static final int S_IFIFO = 0x1000; // fifo
}
