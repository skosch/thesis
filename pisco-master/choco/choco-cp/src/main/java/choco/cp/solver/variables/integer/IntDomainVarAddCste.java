/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package choco.cp.solver.variables.integer;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Declare a variable Y, based on a variable X and a constante c,
 * such as Y = X + c
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/02/11
 */
public class IntDomainVarAddCste extends AbstractBijectiveVar {

    final int constante;

    /**
     * Build a variable Y such as Y = X + c.
     *
     * @param solver   The model this variable belongs to
     * @param name     The name of the variable
     * @param variable constraints stored specific structure
     */
    public IntDomainVarAddCste(final Solver solver, String name, IntDomainVar variable, int constante) {
        super(solver, name, variable);
        this.constante = constante;
    }

    @Override
    public void remVal(int x) throws ContradictionException {
        variable.remVal(x - constante);
    }

    @Override
    public void setInf(int x) throws ContradictionException {
        variable.setInf(x - constante);
    }

    @Override
    @Deprecated
    public void setMin(int x) throws ContradictionException {
        variable.setMin(x - constante);
    }

    @Override
    public void setSup(int x) throws ContradictionException {
        variable.setSup(x - constante);
    }

    @Override
    @Deprecated
    public void setMax(int x) throws ContradictionException {
        variable.setMax(x - constante);
    }

    @Override
    public boolean canBeInstantiatedTo(int x) {
        return variable.canBeInstantiatedTo(x - constante);
    }

    @Override
    public boolean fastCanBeInstantiatedTo(int x) {
        return variable.fastCanBeInstantiatedTo(x - constante);
    }

    @Override
    public int getRandomDomainValue() {
        return variable.getRandomDomainValue() + constante;
    }

    @Override
    public int getNextDomainValue(int i) {
        return variable.getNextDomainValue(i - constante);
    }

    @Override
    public int getPrevDomainValue(int i) {
        return variable.getPrevDomainValue(i - constante);
    }


    @Override
    public int getInf() {
        return variable.getInf() + constante;
    }

    @Override
    public int getSup() {
        return variable.getSup() + constante;
    }

    @Override
    public int getValue() {
        return variable.getValue() + constante;
    }

    @Override
    public boolean updateInf(int x, SConstraint cause, boolean forceAwake) throws ContradictionException {
        return variable.updateInf(x - constante, cause, forceAwake);
    }

    @Override
    public boolean updateInf(int x, int idx) throws ContradictionException {
        return variable.updateInf(x - constante, idx);
    }

    @Override
    public boolean updateSup(int x, SConstraint cause, boolean forceAwake) throws ContradictionException {
        return variable.updateSup(x - constante, cause, forceAwake);
    }

    @Override
    public boolean updateSup(int x, int idx) throws ContradictionException {
        return variable.updateSup(x - constante, idx);
    }

    @Override
    public boolean removeVal(int x, SConstraint cause, boolean forceAwake) throws ContradictionException {
        return variable.removeVal(x - constante, cause, forceAwake);
    }

    @Override
    public boolean removeVal(int x, int idx) throws ContradictionException {
        return variable.removeVal(x - constante, idx);
    }

    @Override
    public boolean removeInterval(int a, int b, SConstraint cause, boolean forceAwake) throws ContradictionException {
        return variable.removeInterval(a - constante, b - constante, cause, forceAwake);
    }

    @Override
    public boolean removeInterval(int a, int b, int idx) throws ContradictionException {
        return variable.removeInterval(a - constante, b - constante, idx);
    }

    @Override
    public boolean instantiate(int x, SConstraint cause, boolean forceAwake) throws ContradictionException {
        return variable.instantiate(x - constante, cause, forceAwake);
    }

    @Override
    public boolean instantiate(int x, int idx) throws ContradictionException {
        return variable.instantiate(x - constante, idx);
    }

    @Override
    public void setVal(int x) throws ContradictionException {
        variable.setVal(x - constante);
    }

    @Override
    public int getVal() {
        return variable.getVal() + constante;
    }

    @Override
    public boolean isInstantiatedTo(int x) {
        return variable.isInstantiatedTo(x - constante);
    }

    @Override
    public String pretty() {
        return String.format("(%s + %d)", variable.getName(), constante);
    }
}
