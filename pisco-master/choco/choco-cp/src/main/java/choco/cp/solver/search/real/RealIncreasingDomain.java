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

package choco.cp.solver.search.real;

import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.variables.real.RealVar;

/**
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 2 nov. 2004
 */
public class RealIncreasingDomain implements ValIterator<RealVar> {

    /**
     * testing whether more branches can be considered after branch i, on the alternative associated to variable x
     *
     * @param x the variable under scrutiny
     * @param i the index of the last branch explored
     * @return true if more branches can be expanded after branch i
     */
    public boolean hasNextVal(RealVar x, int i) {
        return i < 2;
    }

    /**
     * Accessing the index of the first branch for variable x
     *
     * @param x the variable under scrutiny
     * @return the index of the first branch (such as the first value to be assigned to the variable)
     */
    public int getFirstVal(RealVar x) {
        return 1;
    }

    /**
     * generates the index of the next branch after branch i, on the alternative associated to variable x
     *
     * @param x the variable under scrutiny
     * @param i the index of the last branch explored
     * @return the index of the next branch to be expanded after branch i
     */
    public int getNextVal(RealVar x, int i) {
    return 2;
  }
}
