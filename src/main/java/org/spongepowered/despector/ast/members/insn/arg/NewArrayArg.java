/*
 * The MIT License (MIT)
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.despector.ast.members.insn.arg;

import org.spongepowered.despector.ast.members.insn.InstructionVisitor;
import org.spongepowered.despector.util.TypeHelper;

public class NewArrayArg implements Instruction {

    private final String type;
    private final Instruction size;
    private final Instruction[] values;

    public NewArrayArg(String type, Instruction size, Instruction[] values) {
        this.type = type;
        this.size = size;
        this.values = values;
    }

    public String getType() {
        return this.type;
    }

    public Instruction getSize() {
        return this.size;
    }

    public Instruction[] getInitializer() {
        return this.values;
    }

    @Override
    public String inferType() {
        return "[" + this.type;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visitNewArrayArg(this);
        this.size.accept(visitor);
        for (Instruction value : this.values) {
            value.accept(visitor);
        }
    }

    @Override
    public String toString() {
        if (this.values == null) {
            return "new " + TypeHelper.descToType(this.type) + "[" + this.size.toString() + "]";
        }
        String result = "new " + TypeHelper.descToType(this.type) + "[] {";
        for (int i = 0; i < this.values.length; i++) {
            result += this.values[i].toString();
            if (i < this.values.length - 1) {
                result += ", ";
            }
        }
        result += "}";
        return result;
    }

}
