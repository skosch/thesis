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

package choco.cp.solver.constraints.integer.intlincomb.policy;

import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 11 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public final class ForSum extends CoeffPolicy {

    static CoeffPolicy get(final IntDomainVar[] vars, final int[] coeffs, final int nbPosVars, final int cste) {
        return new ForSum(vars, coeffs, nbPosVars, cste);
    }

    private ForSum(final IntDomainVar[] vars, final int[] coeffs, final int nbPosVars, final int cste) {
        super(vars, coeffs, nbPosVars, cste);
    }

    @Override
    public int getInfNV(int i, int mylb) {
        return vars[i].getSup() + mylb;
    }

    @Override
    public int getInfPV(int i, int myub) {
        return vars[i].getSup() - myub;
    }

    @Override
    public int getSupNV(int i, int myub) {
        return vars[i].getInf() + myub;
    }

    @Override
    public int getSupPV(int i, int mylb) {
        return vars[i].getInf() - mylb;
    }

    @Override
    public int computeLowerBound() {
        int lb = cste;
        for (int i = 0; i < nbPosVars; i++) {
            lb += vars[i].getInf();
        }
        for (int i = nbPosVars; i < vars.length; i++) {
            lb -= vars[i].getSup();
        }
        return lb;
    }


    @Override
    public int computeUpperBound() {
        int ub = cste;
        for (int i = 0; i < nbPosVars; i++) {
            ub += vars[i].getSup();
        }
        for (int i = nbPosVars; i < vars.length; i++) {
            ub -= vars[i].getInf();
        }
        return ub;
    }
}
