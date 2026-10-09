/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * streams of {@link JNodeFileSystemDriver}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-08 nsano initial version <br>
 */
class JNodeStreamsTest {

    /** on memory jnode file */
    static class MemoryFile implements FSFile {
        byte[] data = new byte[0];
        int flushed;

        @Override public long getLength() { return data.length; }
        @Override public void setLength(long length) { data = Arrays.copyOf(data, (int) length); }
        @Override public void read(long fileOffset, ByteBuffer dest) throws IOException {
            if (fileOffset + dest.remaining() > data.length) throw new IOException("out of range");
            dest.put(data, (int) fileOffset, dest.remaining());
        }
        @Override public void write(long fileOffset, ByteBuffer src) {
            int end = (int) fileOffset + src.remaining();
            if (end > data.length) setLength(end);
            src.get(data, (int) fileOffset, src.remaining());
        }
        @Override public void flush() { flushed++; }
        @Override public boolean isValid() { return true; }
        @Override public FileSystem<?> getFileSystem() { return null; }
    }

    @Test
    void writeThenRead() throws Exception {
        byte[] expected = new byte[100_000];
        new Random(1).nextBytes(expected);

        MemoryFile file = new MemoryFile();
        try (OutputStream os = JNodeFileSystemDriver.newOutputStream(file)) {
            os.write(expected[0]);
            os.write(expected, 1, 8191);
            os.write(expected, 8192, expected.length - 8192);
        }
        assertArrayEquals(expected, file.data);
        assertEquals(1, file.flushed);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = JNodeFileSystemDriver.newInputStream(file)) {
            assertEquals(expected[0] & 0xff, is.read());
            is.transferTo(baos);
            assertEquals(-1, is.read());
            assertEquals(-1, is.read(new byte[10], 0, 10));
        }
        assertArrayEquals(Arrays.copyOfRange(expected, 1, expected.length), baos.toByteArray());
    }

    @Test
    void readEmpty() throws Exception {
        try (InputStream is = JNodeFileSystemDriver.newInputStream(new MemoryFile())) {
            assertEquals(-1, is.read());
            assertEquals(0, is.read(new byte[0], 0, 0));
        }
    }

    @Test
    void readHighByte() throws Exception {
        MemoryFile file = new MemoryFile();
        file.data = new byte[] {(byte) 0xff, 0};
        try (InputStream is = JNodeFileSystemDriver.newInputStream(file)) {
            assertEquals(0xff, is.read());
            assertEquals(0, is.read());
            assertEquals(-1, is.read());
        }
    }
}
