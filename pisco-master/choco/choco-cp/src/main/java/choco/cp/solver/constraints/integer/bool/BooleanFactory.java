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

package choco.cp.solver.constraints.integer.bool;

import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 18 mai 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class BooleanFactory {

    /**
     * Builder for AND constraint over integer variables
     *
     * @param booleans booleans variables
     * @return AND constraint
     */
    public static AbstractSConstraint<IntDomainVar> and(IntDomainVar... booleans) {
        if (booleans.length == 2) {
            return new BinAnd(booleans[0], booleans[1]);
        } else {
            return new LargeAnd(booleans);
        }
    }

    /**
     * Builder for NAND constraint over integer variables
     *
     * @param booleans booleans variables
     * @return NAND constraint
     */
    public static AbstractSConstraint<IntDomainVar> nand(final IEnvironment environment, IntDomainVar... booleans) {
        if (booleans.length == 2) {
            return new BinNand(booleans[0], booleans[1]);
        } else {
            return new LargeNand(booleans, environment);
        }
    }

    /**
     * Builder for OR constraint over integer variables
     *
     * @param environment
     * @param booleans    booleans variables
     * @return OR constraint
     */
    public static AbstractSConstraint<IntDomainVar> or(final IEnvironment environment, IntDomainVar... booleans) {
        if (booleans.length == 2) {
            return new BinOr(booleans[0], booleans[1]);
        } else {
            return new LargeOr(booleans, environment);
        }
    }

    /**
     * Builder for NOR constraint over integer variables
     *
     * @param environment
     * @param booleans    booleans variables
     * @return NOR constraint
     */
    public static AbstractSConstraint<IntDomainVar> nor(IntDomainVar... booleans) {
        if (booleans.length == 2) {
            return new BinNor(booleans[0], booleans[1]);
        } else {
            return new LargeNor(booleans);
        }
    }

    /**
     * Builder for XOR constraint over integer variables
     *
     * @param booleans    booleans variables
     * @return XOR constraint
     */
    public static AbstractSConstraint<IntDomainVar> xor(IntDomainVar... booleans) {
        if (booleans.length == 2) {
            return new BinXor(booleans[0], booleans[1]);
        } else {
            return new LargeXor(booleans);
        }
    }

    /**
     * Builder for XOR constraint over integer variables
     *
     * @param booleans    booleans variables
     * @return XOR constraint
     */
    public static AbstractSConstraint<IntDomainVar> xnor(IntDomainVar... booleans) {
        if (booleans.length == 2) {
            return new BinXnor(booleans[0], booleans[1]);
        } else {
            return new LargeXnor(booleans);
        }
    }

    /**
     * Builder for NOT constraint over an integer variable
     *
     * @param bool    boolean variable
     * @return NOT constraint
     */
    public static AbstractSConstraint<IntDomainVar> not(IntDomainVar bool) {
        return new Not(bool);
    }

    /**
     * Builder for NOT constraint over an integer variable
     *
     * @param bool    boolean variable
     * @return NOT constraint
     */
    public static AbstractSConstraint<IntDomainVar> identity(IntDomainVar bool) {
        return new Identity(bool); 
    }

}
