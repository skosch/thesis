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

package choco.cp.solver.constraints.global.matching;

import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Simple implementation of global cardinality constraint with occurrence constrained by
 * variables and not only integer bounds.
 */
public final class GlobalCardinalityVar extends GlobalCardinality {

    public GlobalCardinalityVar(IntDomainVar[] values,
                                IntDomainVar[] occurences, IEnvironment environment) {
        this(values, 1, occurences.length, occurences, environment);
    }

    public GlobalCardinalityVar(IntDomainVar[] values,
                                int minValue, int maxValue,
                                IntDomainVar[] occurences, IEnvironment environment) {
        super(values, minValue, maxValue, new int[occurences.length], new int[occurences.length], environment);
        int nbVarsTotal = values.length + occurences.length;
        vars = new IntDomainVar[nbVarsTotal];
        System.arraycopy(values, 0, vars, 0, values.length);
        System.arraycopy(occurences, 0, vars, values.length, occurences.length);
        cIndices = new int[nbVarsTotal];
        //minOccurence = getProblem().getEnvironment().makeIntVector(occurencesMin.length, -1);
        //maxOccurence = getProblem().getEnvironment().makeIntVector(occurencesMin.length, -1);
    }

    /*public GlobalCardinalityVar(IntDomainVar[] values,
                                int[] thevalues,
                                IntDomainVar[] occurences, IEnvironment environment) {
        super(values, minValue, maxValue, new int[occurences.length], new int[occurences.length], environment);
        int nbVarsTotal = values.length + occurences.length;
        vars = new IntDomainVar[nbVarsTotal];
        System.arraycopy(values, 0, vars, 0, values.length);
        System.arraycopy(occurences, 0, vars, values.length, occurences.length);
        cIndices = new int[nbVarsTotal];
        //minOccurence = getProblem().getEnvironment().makeIntVector(occurencesMin.length, -1);
        //maxOccurence = getProblem().getEnvironment().makeIntVector(occurencesMin.length, -1);
    }*/


    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx < nbLeftVertices) super.awakeOnInf(idx);
        else {
            checkSumInfs();
            deleteSupport();
            this.constAwake(false);
        }
    }

    private void checkSumInfs() throws ContradictionException {
        int sum = 0;
        for (int j = 0; j < nbRightVertices; j++) {
            sum += vars[j + nbLeftVertices].getInf();
        }
        if (sum > nbLeftVertices) this.fail();
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        if (idx < nbLeftVertices) super.awakeOnRem(idx, x);
        else {
            deleteSupport();
            this.constAwake(false);
        }
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx < nbLeftVertices) super.awakeOnSup(idx);
        else {
            deleteSupport();
            this.constAwake(false);
        }
    }

    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx < nbLeftVertices) super.awakeOnInst(idx);
        else {
            checkSumInfs();
            deleteSupport();
            this.constAwake(false);
        }
    }

    public void awake() throws ContradictionException {
        super.awake();
    }

    public void deleteSupport() {
        for (int i = 0; i < nbLeftVertices; i++) {
            refMatch.set(i, -1);
        }
        for (int j = 0; j < nbRightVertices; j++) {
            flow.set(j, 0);
        }
        matchingSize.set(0);
    }

    protected int getMinFlow(int j) {
        return vars[nbLeftVertices + j].getInf();
    }

    protected int getMaxFlow(int j) {
        return vars[nbLeftVertices + j].getSup();
    }

    public boolean isSatisfied(int[] tuple) {
        int[] occurrences = new int[this.maxValue - this.minValue + 1];
        int nbvar = tuple.length - occurrences.length;
        for (int i = 0; i < nbvar; i++) {
            occurrences[tuple[i] - this.minValue]++;
        }
        for (int i = 0; i < occurrences.length; i++) {
            int occurrence = occurrences[i];
            if (tuple[i + nbvar] != occurrence)
                return false;
        }
        return true;
    }

}
