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

package org.jnode.test.driver.bus.ide;

import org.jnode.driver.bus.ide.IDEDriveDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IDEDriveDescriptorTest {
    // The ide descriptor.
    private IDEDriveDescriptor ideDescriptor;
    private IDEDriveDescriptor cdromIdeDescriptor;
    // IDE Descriptor datas obtain from command 'hdparm --Istdout /dev/sda' on a linux machine.
    private int[] ide = new int[]{0x0c5a, 0x3fff, 0xc837, 0x0010, 0x0000,
        0x0000, 0x003f, 0x0000, 0x0000, 0x0000, 0x2020, 0x2020, 0x2020,
        0x2020, 0x2020, 0x2020, 0x354c, 0x5339, 0x4b37, 0x4346, 0x0000,
        0x4000, 0x0004, 0x332e, 0x4144, 0x4a20, 0x2020, 0x5354, 0x3331,
        0x3630, 0x3831, 0x3241, 0x5320, 0x2020, 0x2020, 0x2020, 0x2020,
        0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020,
        0x2020, 0x2020, 0x8010, 0x0000, 0x2f00, 0x4000, 0x0200, 0x0200,
        0x0007, 0x3fff, 0x0010, 0x003f, 0xfc10, 0x00fb, 0x0108, 0xffff,
        0x0fff, 0x0000, 0x0007, 0x0003, 0x0078, 0x0078, 0x00f0, 0x0078,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x001f, 0x0506,
        0x0000, 0x0040, 0x0040, 0x00fe, 0x0000, 0x346b, 0x7701, 0x4023,
        0x3469, 0x3401, 0x4023, 0x407f, 0x0000, 0x0000, 0xfefe, 0xfffe,
        0x0000, 0xd000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x5f20,
        0x12a0, 0x0000, 0x0000, 0x0000, 0x0000, 0x4000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0100, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0009, 0x5f20, 0x12a0, 0x5f20, 0x12a0,
        0x2020, 0x0002, 0x02b6, 0x0002, 0x008a, 0x3c06, 0x3c0a, 0x0000,
        0x07c6, 0x0100, 0x0800, 0x1314, 0x1200, 0x0002, 0x0080, 0x0000,
        0x0000, 0x00a0, 0x0202, 0x0000, 0x0404, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0c00, 0x000b, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x8aa5};

    private int[] cdrom = new int[]{0x8580, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020,
        0x2020, 0x2020, 0x2020, 0x2020, 0x0000, 0x0000, 0x0000, 0x3130,
        0x3543, 0x2020, 0x2020, 0x5f4e, 0x4543, 0x2044, 0x5644, 0x2b2f,
        0x2d52, 0x5720, 0x4e44, 0x2d33, 0x3635, 0x3041, 0x2020, 0x2020,
        0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x0000,
        0x0000, 0x0b00, 0x0000, 0x0200, 0x0200, 0x0006, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0007,
        0x0003, 0x0078, 0x0078, 0x0078, 0x0078, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0602, 0x0000, 0x0000, 0x0000,
        0x0080, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0407, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};

    @BeforeEach
    public void setUp() {
        ideDescriptor = new IDEDriveDescriptor(ide, true);
        cdromIdeDescriptor = new IDEDriveDescriptor(cdrom, true);
    }

    @Test
    public void testConstructorDataWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            int[] data = new int[125];
            IDEDriveDescriptor wrongIdeDescriptor = new IDEDriveDescriptor(data, true);
        });
    }

    @Test
    public void testGetSerialNumber() {
        String result = ideDescriptor.getSerialNumber();
        assertEquals("5LS9K7CF", result);
    }

    @Test
    public void testGetModel() {
        String result = ideDescriptor.getModel();
        assertEquals("ST3160812AS", result);
    }

    @Test
    public void testGetFirmware() {
        String result = ideDescriptor.getFirmware();
        assertEquals("3.ADJ", result);
    }

    @Test
    public void testGetSectorsAddressable() {
        long result = ideDescriptor.getSectorsAddressable();
        //Get actually the LBA48 user addressable sectors
        assertEquals(312500000, result);
    }

    @Test
    public void testSupports48bitAddressing() {
        boolean result = ideDescriptor.supports48bitAddressing();
        assertTrue(result, "Must support 48bits addressing");
    }

    @Test
    public void testSupportsLBA() {
        boolean result = ideDescriptor.supportsLBA();
        assertTrue(result, "Must support LBA");
    }

    @Test
    public void testDMA() {
        boolean result = ideDescriptor.supportsDMA();
        assertTrue(result, "Must support DMA");
    }

    @Test
    public void testIsATA() {
        boolean result = ideDescriptor.isAta();
        assertTrue(result, "Must be ATA drive");
    }

    @Test
    public void testIsRemovable() {
        boolean result = ideDescriptor.isRemovable();
        assertFalse(result, "Must not be a removable device");
        result = cdromIdeDescriptor.isRemovable();
        assertTrue(result, "Must be a removable device");
    }

}
