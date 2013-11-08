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

import choco.kernel.model.variables.integer.IntegerVariable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21/12/10
 */
public class Singleton extends ALogicTree {

    public static final Singleton TRUE = new Singleton(Type.POSITIVE);
    public static final Singleton FALSE = new Singleton(Type.NEGATIVE);

    protected Singleton(Type type) {
        super(type);
    }

    @Override
    public boolean is(Operator op) {
        throw new UnsupportedOperationException();
    }

    @Override
    boolean isNot() {
        throw new UnsupportedOperationException();
    }

    @Override
    boolean isLit() {
        throw new UnsupportedOperationException();
    }

    @Override
    int getNbChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    boolean hasOrChild() {
        throw new UnsupportedOperationException();
    }

    @Override
    boolean hasAndChild() {
        throw new UnsupportedOperationException();
    }

    @Override
    void addChild(ALogicTree child) {
        throw new UnsupportedOperationException();
    }

    @Override
    void removeChild(ALogicTree child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ALogicTree[] getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    ALogicTree getAndChild() {
        throw new UnsupportedOperationException();
    }

    @Override
    ALogicTree getChildBut(ALogicTree child) {
        throw new UnsupportedOperationException();
    }

    @Override
    void flip() {
        throw new UnsupportedOperationException();
    }

    @Override
    void deny() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntegerVariable[] flattenBoolVar() {
        return new IntegerVariable[0];
    }

    @Override
    public String toString() {
        return Type.POSITIVE.equals(type) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    @Override
    public int getNbPositiveLiterals() {
        return 0;
    }
}
