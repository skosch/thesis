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

package choco.kernel.common;

import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.ConstantSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 8 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 *
 * Class for constant declaration.
 */
public class Constant {

    private Constant() {}

    /**
     * Initial capacity for array containinc static elements.
     */
    public static final int INITIAL_STATIC_CAPACITY = 16;

    /**
     * Initial capacity for array containinc dynamic elements.
     */
    public static final int INITIAL_STORED_CAPACITY = 16;

    /**
     * Offset of the first dynamic element.
     */
    public static final int STORED_OFFSET = 1000000;

    /**
     * Initial capacity of bipartite set
     */
    public static final int SET_INITIAL_CAPACITY = 8;
    
    /**
     * A constant denoting the true constraint (always satisfied)
     */
    public static final ConstantSConstraint TRUE = new ConstantSConstraint(true) {
        /**
         * Get the opposite constraint
         *
         * @return the opposite constraint  @param solver
         */
        @Override
        public AbstractSConstraint<IntDomainVar> opposite(final Solver solver) {
            return Constant.FALSE;
        }
    };

    /**
     * A constant denoting the false constraint (never satisfied)
     */
    public static final ConstantSConstraint FALSE = new ConstantSConstraint(false) {
        /**
         * Get the opposite constraint
         *
         * @return the opposite constraint  @param solver
         */
        @Override
        public AbstractSConstraint<IntDomainVar> opposite(final Solver solver) {
            return Constant.TRUE;
        }
    };

    /**
     * Defines the rounding precision for multicostregular algorithm
     */
    public static final int MCR_PRECISION = 4; // MUST BE < 13 as java messes up the precisions starting from 10E-12 (34.0*0.05 == 1.70000000000005)

      /**
     * Defines the smallest used double for multicostregular
     */
    public static final double MCR_DECIMAL_PREC = Math.pow(10.0,-MCR_PRECISION);

}
