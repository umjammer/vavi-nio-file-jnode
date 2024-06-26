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

package org.jnode.util;

/**
 * @deprecated use {@link DecimalScaleFactor} or {@link BinaryScaleFactor} instead.
 */
@Deprecated
public enum SizeUnit {
    B(1L, "B"),
    K(1024L, "K"),
    M(1024L * 1024L, "M"),
    G(1024L * 1024L * 1024L, "G"),
    T(1024L * 1024L * 1024L * 1024L, "T"),
    P(1024L * 1024L * 1024L * 1024L * 1024L, "P"),
    E(1024L * 1024L * 1024L * 1024L * 1024L * 1024L, "E");
    // these units have too big multipliers to fit in a long
    // (aka they are greater than 2^64) :
    // Z(1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Z"),
    // Y(1024l*1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Y");

    public static final SizeUnit MIN = B;
    public static final SizeUnit MAX = E;

    private final long multiplier;
    private final String unit;

    SizeUnit(long multiplier, String unit) {
        this.multiplier = multiplier;
        this.unit = unit;
    }

    public long getMultiplier() {
        return multiplier;
    }

    public String getUnit() {
        return unit;
    }

    public String toString() {
        return multiplier + ", " + unit;
    }
}
