/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package choco.kernel.model.constraints.cnf;

import choco.kernel.model.ModelException;
import choco.kernel.model.variables.integer.IntegerVariable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21/12/10
 */
public final class Literal extends ALogicTree implements Comparable<Literal> {

    private IntegerVariable var;
    private IntegerVariable[] varAsArray;

    protected Literal(IntegerVariable var, Type type) {
        super(type);
        if (!var.isBoolean()) {
            throw new ModelException("Literal cannot be declared with non boolean variable! (" + var.pretty() + ")");
        }
        this.var = var;
        this.varAsArray = new IntegerVariable[]{var};
    }

    public static Literal pos(IntegerVariable var) {
        return new Literal(var, Type.POSITIVE);
    }

    public static Literal[] pos(IntegerVariable[] vars) {
        Literal[] lits = new Literal[vars.length];
        for(int i = 0 ; i< vars.length; i++){
            lits[i] = new Literal(vars[i], Type.POSITIVE);
        }
        return lits;
    }

    public static Literal neg(IntegerVariable var) {
        return new Literal(var, Type.NEGATIVE);
    }

    public static Literal[] neg(IntegerVariable[] vars) {
        Literal[] lits = new Literal[vars.length];
        for(int i = 0 ; i< vars.length; i++){
            lits[i] = new Literal(vars[i], Type.NEGATIVE);
        }
        return lits;
    }


    @Override
    public boolean is(Operator op) {
        return false;
    }

    @Override
    public boolean isNot() {
        return false;
    }

    @Override
    public boolean isLit() {
        return true;
    }

    @Override
    int getNbChildren() {
        return 0;
    }

    @Override
    public boolean hasOrChild() {
        return false;
    }

    @Override
    public boolean hasAndChild() {
        return false;
    }

    @Override
    public void addChild(ALogicTree child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeChild(ALogicTree child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ALogicTree[] getChildren() {
        return null;
    }

    @Override
    public ALogicTree getAndChild() {
        return null;
    }

    @Override
    public ALogicTree getChildBut(ALogicTree child) {
        return null;
    }

    @Override
    void deny() {
        type = Type.flip(type);
    }

    @Override
    void flip() {
        type = Type.flip(type);
    }

    @Override
    public String toString() {
        return (Type.POSITIVE.equals(type) ? "" : "not ") + var.getName();
    }

    @Override
    public int compareTo(Literal o) {
        if (type.equals(o.type)) {
            return 0;
        } else if (Type.POSITIVE.equals(type)) {
            return -1;
        }
        return 1;
    }

    @Override
    public Literal clone() throws CloneNotSupportedException {
        Literal lit = (Literal) super.clone();
        lit.var = this.var;
        lit.type = this.type;
        return lit;
    }

    @Override
    public IntegerVariable[] flattenBoolVar() {
        return varAsArray;
    }

    @Override
    public int getNbPositiveLiterals() {
        return Type.POSITIVE.equals(type) ? 1 : 0;
    }
}
