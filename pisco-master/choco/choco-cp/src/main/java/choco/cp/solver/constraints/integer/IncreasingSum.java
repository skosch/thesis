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
package choco.cp.solver.constraints.integer;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;


/**
 * A sum constraint with increasing variables
 * s = x_0 + x_1 + ... + x_(n-1)
 * and x_0 <= x_1 <= ... <= x_(n-1)
 * Bounds-consistency algorithm linear in n (the number of variables)
 * <p/>
 * See "A O(n) Bound-Consistency Algorithm for the Increasing Sum Constraint",
 * <br/>T. Petit, J.C. Regin and N. Beldiceanu
 *
 * User: tpetit
 * thierry.petit(a)mines-nantes.fr
 */

public class IncreasingSum extends AbstractLargeIntSConstraint {

    // Decision variables
    IntDomainVar[] dec;
    // Number of decsision variables
    int n;
    // Sum variable
    IntDomainVar s;

    // initialization
    protected static IntDomainVar[] buildVars(final IntDomainVar[] dec, final IntDomainVar s) {
        final IntDomainVar[] res = new IntDomainVar[dec.length + 1];
        System.arraycopy(dec, 0, res, 0, dec.length);
        res[dec.length] = s;
        return res;
    }

    // creation
    public IncreasingSum(final IntDomainVar[] dec, final IntDomainVar s) {
        super(buildVars(dec, s));
        this.dec = dec;
        this.s = s;
        this.n = dec.length;
    }

    // shrink mins according to <=
    public void updateMin() throws ContradictionException {
        int minSum = dec[0].getInf();
        for (int i = 1; i < n; i++) {
            if (dec[i - 1].getInf() > dec[i].getInf()) {
                dec[i].updateInf(dec[i - 1].getInf(), this, false);
            }
            minSum += dec[i].getInf();
        }
        if (minSum > s.getInf()) {
            s.updateInf(minSum, this, false);
        }
    }

    // shrink max according to <=
    public void updateMax() throws ContradictionException {
        int maxSum = dec[n - 1].getSup();
        for (int i = n - 2; i >= 0; i--) {
            if (dec[i + 1].getSup() < dec[i].getSup()) {
                dec[i].updateSup(dec[i + 1].getSup(), this, false);
            }
            maxSum += dec[i].getSup();
        }
        if (maxSum < s.getSup()) {
            s.updateSup(maxSum, this, false);
        }
    }


    // BC on max
    // Assumes that min and max are consistent with xi <= xi+1 and bounds of s are updated
    // Therefore, it should NEVER fail
    public void filterMax() throws ContradictionException {
        int minSum = 0;
        for (int i = 0; i < n; i++) {
            minSum += dec[i].getInf();
        }
        int margin = s.getSup() - minSum;
        int i = n - 1; // index of the current variable
        int j = i; // last index of i
        int delta_i = dec[i].getSup() - dec[i].getInf(); // increase in sum if we chose the max of xi
        while (i >= 0) {
            if (delta_i <= margin) { // no pruning of dec[i]
                int oldmax = dec[i].getSup();
                i--;
                if (i >= 0) {
                    while (dec[j].getInf() >= dec[i].getSup() && j > i) {
                        delta_i -= (oldmax - dec[j].getInf());
                        j--;
                    }
                    delta_i += dec[i].getSup() - dec[i].getInf() - (j - i) * (oldmax - dec[i].getSup());
                }
            } else { // prune dec[i]
                while (delta_i > margin) {
                    int cut = (delta_i - margin) / (j - i + 1);
                    if ((delta_i - margin) % (j - i + 1) > 0) {
                        cut++;
                    }
                    int steps = Math.min(cut, dec[i].getSup() - dec[j].getInf());
                    dec[i].setSup(dec[i].getSup() - steps);
                    delta_i -= (j - i + 1) * (steps);
                    while (dec[j].getInf() >= dec[i].getSup() && j > i) {
                        j--;
                    }
                }
            }
            if (i > 0 && dec[i - 1].getSup() > dec[i].getSup()) {
                dec[i - 1].setSup(dec[i].getSup());
            }
        }
    }

    // BC on min
    // Assumes that min and max are consistent with xi <= xi+1 and bounds of s are updated
    // Therefore, it should NEVER fail
    public void filterMin() throws ContradictionException {
        int maxSum = 0;
        for (int i = 0; i < n; i++) {
            maxSum += dec[i].getSup();
        }
        int margin = s.getInf() - maxSum; // <=0
        int i = 0;
        int j = i;
        int delta_i = dec[i].getInf() - dec[i].getSup(); // <=0
        while (i < n) {
            if (delta_i >= margin) { // no pruning of dec[i]
                int oldmin = dec[i].getInf();
                i++;
                if (i < n) {
                    while (dec[j].getSup() <= dec[i].getInf() && j < i) {
                        delta_i += (dec[j].getSup() - oldmin);
                        j++;
                    }
                    delta_i -= dec[i].getSup() - dec[i].getInf() + (i - j) * (oldmin - dec[i].getInf());
                }
            } else { // prune dec[i]
                while (delta_i < margin) {
                    int cut = (margin - delta_i) / (i - j + 1);
                    if ((margin - delta_i) % (i - j + 1) > 0) {
                        cut++;
                    }
                    int steps = Math.min(cut, dec[j].getSup() - dec[i].getInf());
                    dec[i].setInf(dec[i].getInf() + steps);
                    delta_i += (i - j + 1) * (steps);
                    while (dec[j].getSup() <= dec[i].getInf() && j < i) {
                        j++;
                    }
                }
            }
            if (i < n - 1 && dec[i + 1].getInf() < dec[i].getInf()) {
                dec[i + 1].setInf(dec[i].getInf());
            }
        }
    }

    public void propagate() throws ContradictionException {
        updateMax(); // <= coherence
        updateMin(); // <= coherence
        filterMax();
        filterMin();
    }

    public boolean isSatisfied() {
        if (isCompletelyInstantiated()) {
            int res = dec[0].getVal();
            for (int i = 1; i < dec.length; i++) {
                if (dec[i - 1].getVal() > dec[i].getVal()) {
                    return false;
                }
                res += dec[i].getVal();
            }
            return res == s.getVal();
        }
        return false;
    }
}
