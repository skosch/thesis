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

package choco.cp.solver.constraints.integer.bool.sat;

import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A literal is a boolean variable or its negation
 * This structure stores the lists of watched literals to ensure
 * propagation of clauses
 */
public final class Lits {

    //number of boolean variables
    protected int nblits;

    protected IntDomainVar[] boolvars;

    // watches for assignments x_i = 1
    protected Vec<WLClause>[] poswatches;

    // watches for assignments x_i = 0
    protected Vec<WLClause>[] negwatches;


    
    @SuppressWarnings({"unchecked"})
    public void init(IntDomainVar[] vars) {
        boolvars = new IntDomainVar[vars.length+1];
        for (int i = 1; i < vars.length+1; i++) {
            if(vars[i-1].hasBooleanDomain()){
                boolvars[i] = vars[i-1];
            }else{
                throw new SolverException(vars[i-1].getName()+" is not a boolean variable");
            }
        }
        nblits = boolvars.length + 1;
        poswatches = new Vec[nblits];
        negwatches = new Vec[nblits];
    }

    public boolean isFree(int lit) {
        //negative indexes denote negative literals,
        //similarly positive indexes denote positive literal
        if (lit < 0) {
            return !boolvars[-lit].isInstantiated();
        } else {
            return !boolvars[lit].isInstantiated();
        }
    }

    public boolean isFalsified(int lit) {
        //negative indexes denote negative literals,
        //similarly positive indexes denote positive literal
        if (lit < 0) {
            return boolvars[-lit].isInstantiatedTo(1);
        } else {
            return boolvars[lit].isInstantiatedTo(0);
        }
    }

    public boolean isSatisfied(int lit) {
        if (lit < 0) {
            return boolvars[-lit].isInstantiatedTo(0);
        } else {
            return boolvars[lit].isInstantiatedTo(1);
        }
    }

    public Boolean isEntailed(int lit) {
        if (lit < 0) {
            if (!boolvars[-lit].isInstantiated())
                return null;
            return boolvars[-lit].isInstantiatedTo(0);
        } else {
            if (!boolvars[lit].isInstantiated())
                return null;
            return boolvars[lit].isInstantiatedTo(1);
        }
    }

    public static boolean isSatisfied(int lit, int val) {
        if (lit < 0) {
            return val == 0;
        } else {
            return val == 1;
        }
    }

    public static boolean isPositive(int lit) {
        return lit > 0;
    }

    public void watch(int lit, WLClause c) {
        if (lit < 0) {
            int rlit = -lit;
            if (poswatches[rlit] == null) {
                poswatches[rlit] = new Vec<WLClause>();
            }
            poswatches[rlit].push(c);
        } else {
            if (negwatches[lit] == null) {
                negwatches[lit] = new Vec<WLClause>();
            }
            negwatches[lit].push(c);
        }
    }

    public void unwatch(int lit, WLClause c) {
        if (lit < 0) {
            poswatches[-lit].remove(c);
        } else {
            negwatches[lit].remove(c);
        }
    }

    public void unwatch(int lit, int idxClause) {
        if (lit < 0) {
            poswatches[-lit].delete(idxClause);
        } else {
            negwatches[lit].delete(idxClause);
        }
    }

    public Vec<WLClause> pos_watches(int idx) {
        return poswatches[idx];// - offsets[idx]];
    }

    public Vec<WLClause> neg_watches(int idx) {
        return negwatches[idx];// - offsets[idx]];
    }

    public Vec<WLClause> watches(int lit) {
        if (lit < 0) {
            return poswatches[-lit];
        } else return negwatches[lit];
    }

    public void reset() {
        for (int i = 0; i < nblits; i++) {
            poswatches[i] = null;
            negwatches[i] = null;
        }
    }

}
