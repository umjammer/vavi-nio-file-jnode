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

package org.jnode.test.partitions.ibm;

import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import vavi.util.ByteUtil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IBMPartitionTableEntryTest {

    @Test
    public void testHasChildPartitionTable() {
        byte[] bootSector = getBootSector();
        bootSector[450] = (byte) 0x85;
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, bootSector, 0);
        assertTrue(pte.hasChildPartitionTable());
    }

    @Test
    public void testHasNoChildPartitionTable() {
        byte[] bootSector = getBootSector();
        bootSector[450] = (byte) 0x84;
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, bootSector, 0);
        assertFalse(pte.hasChildPartitionTable());
    }

    @Test
    public void testIsValid() {
        byte[] bootSector = getBootSector();
        ByteUtil.writeLeInt(0x80, bootSector, 446); // bootable
        ByteUtil.writeLeInt(0x85, bootSector, 450); // valid system id
        ByteUtil.writeLeInt(1, bootSector, 458); // has sectors
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, bootSector, 0);
        assertTrue(pte.isValid());
    }

    @Test
    public void testIsNotValidEmptyBootSector() {
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, getBootSector(), 0);
        assertFalse(pte.isValid());
    }

    @Test
    public void testIsEmpty() {
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, getBootSector(), 0);
        assertTrue(pte.isEmpty());
    }

    private byte[] getBootSector() {
        byte[] bs = new byte[500];
        return bs;
    }
}
