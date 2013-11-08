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

package parser.absconparseur.components;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.constraints.Constraint;

import static java.lang.Integer.parseInt;
import java.util.logging.Logger;

public abstract class PConstraint {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

    private final int index;

    protected String name;

	protected PVariable[] scope;

    protected Constraint chocoCstr;

    public String getName() {
		return name;
	}

	public PVariable[] getScope() {
		return scope;
	}

	public int getPositionInScope(PVariable variable) {
		for (int i = 0; i < scope.length; i++)
			if (variable == scope[i])
				return i;
		return -1;
	}

	public int getArity() {
		return scope.length;
	}

	public PConstraint(String name, PVariable[] scope) {
		this.name = name;
		this.scope = scope;
        this.index = parseInt(name.substring(1).replaceAll("_", "00"));
    }

	public int getMaximalCost() {
		return 1;
	}

	/**
	 * For CSP, returns 0 is the constraint is satified and 1 if the constraint is violated. <br>
	 * For WCSP, returns the cost for the given tuple.
	 */
	public abstract long computeCostOf(int[] tuple);

	public String toString() {
		StringBuilder s = new StringBuilder(128);
        s.append("  constraint ").append(name).append(" with arity = ").append(scope.length).append(", scope = ");
		s.append(scope[0].getName());
		for (int i = 1; i < scope.length; i++)
            s.append(' ').append(scope[i].getName());
		return s.toString();
	}

	public boolean isGuaranteedToBeDivisionByZeroFree() {
		return true;
	}

	public boolean isGuaranteedToBeOverflowFree() {
		return true;
	}

    public Constraint getChocoCstr() {
        return chocoCstr;
    }

    public void setChocoCstr(Constraint chocoCstr) {
        this.chocoCstr = chocoCstr;
    }

    public int hashCode() {
        return index;
    }
}
