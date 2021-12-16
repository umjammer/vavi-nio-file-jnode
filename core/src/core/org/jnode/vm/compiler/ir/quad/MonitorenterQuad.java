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

package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;

/**
 * @author Levente S\u00e1ntha
 */
public class MonitorenterQuad<T> extends Quad<T> {
    private Operand<T> refs[];

    /**
     * @param address
     */
    public MonitorenterQuad(int address, IRBasicBlock<T> block, int varIndex) {
        super(address, block);
        refs = new Operand[]{getOperand(varIndex)};
    }

    public Operand<T> getDefinedOp() {
        return null;
    }

    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    /**
     * @return the operand for the 'putstatic'
     */
    public Operand<T> getOperand() {
        return refs[0];
    }

    public String toString() {
        return getAddress() + ": monitorenter " + refs[0];
    }

    public void doPass2() {
        refs[0] = refs[0].simplify();
    }

    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }
}
