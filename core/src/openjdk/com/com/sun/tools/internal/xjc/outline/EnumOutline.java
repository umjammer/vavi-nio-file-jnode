/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.internal.xjc.outline;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.internal.JDefinedClass;
import com.sun.tools.internal.xjc.model.CEnumLeafInfo;

/**
 * Outline object that provides per-{@link CEnumLeafInfo} information
 * for filling in methods/fields for a bean.
 *
 * This object can be obtained from {@link Outline}
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class EnumOutline {

    /**
     * This {@link EnumOutline} holds information about this {@link CEnumLeafInfo}.
     */
    public final CEnumLeafInfo target;

    /**
     * The generated enum class.
     */
    public final JDefinedClass clazz;

    /**
     * Constants.
     */
    public final List<EnumConstantOutline> constants = new ArrayList<EnumConstantOutline>();

    protected EnumOutline(CEnumLeafInfo target, JDefinedClass clazz) {
        this.target = target;
        this.clazz = clazz;
    }
}