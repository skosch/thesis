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

package choco.kernel.model.constraints;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.Variable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

public abstract class ConstraintManager <V extends Variable> implements ExpressionManager{


    /**
     * Build a constraint for the given solver and "model variables"
     * @param solver solver to build constraint in
     * @param variables array of variables
     * @param parameters Object defining the paramaters
     * @param options set of options
     * @return One SConstraint
     */
    public abstract SConstraint makeConstraint(Solver solver, V[] variables, Object parameters, List<String> options);

    /**
     * Build a constraint and its opposite for the given solver and "model variables"
     * @param solver solver to build constraint in
     * @param variables array of variables
     * @param parameters Object defining the paramaters
     * @param options set of options
     * @return array of 2 SConstraint object, the constraint and its opposite
     */
    public abstract SConstraint[] makeConstraintAndOpposite(Solver solver, V[] variables, Object parameters, List<String> options);

    /**
     * @param options : the set of options on the constraint (Typically the level of consistency)
     * @return a list of domains accepted by the constraint and sorted
     *         by order of preference
     */
    public abstract int[] getFavoriteDomains(List<String> options);

    protected static int[] getACFavoriteIntDomains() {
        return new int[]{IntDomainVar.BITSET,
                IntDomainVar.LINKEDLIST,
                IntDomainVar.BIPARTITELIST,
                IntDomainVar.BINARYTREE,
                IntDomainVar.BOUNDS,
        };
    }

    protected static int[] getBCFavoriteIntDomains() {
        return new int[]{IntDomainVar.BOUNDS,
                IntDomainVar.BINARYTREE,
                IntDomainVar.BITSET,
                IntDomainVar.BIPARTITELIST,
                IntDomainVar.LINKEDLIST,
        };
    }
    
	protected static boolean checkParameter(Object[] o, int idx) {
		return o.length>idx && o[idx] != null;
	}
	
    protected static SConstraint fail() {
    	return fail("?");
    }

    protected static SConstraint fail(String cname) {
    	LOGGER.severe("Could not found an implementation of "+cname+".");
    	ChocoLogging.flushLogs();
    	return null;
    }


}
