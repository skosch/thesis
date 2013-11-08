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

package parser.chocogen;

import static choco.Choco.makeIntVar;
import choco.Options;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import parser.absconparseur.components.PConstraint;
import parser.absconparseur.components.PExtensionConstraint;
import parser.absconparseur.components.PVariable;
import parser.absconparseur.tools.InstanceParser;

import java.util.Iterator;

/*
 * User:    hcambaza
 * Date:    15 avr. 2008
 */
public class VariableFactory extends ObjectFactory {

	public VariableFactory(Model m, InstanceParser parser) {
		super(m, parser);
	}

	/**
	 * Create a variable for pvar except if it is a fake variable
	 * (equal to another)
	 *
	 * @param pvar
	 * @return
	 */
	public IntegerVariable makeVariable(PVariable pvar) {
		IntegerVariable var;
		if (pvar.getChocovar() == null) {
			var = createVar(pvar);
			pvar.setChocovar(var);
		} else var = pvar.getChocovar();
		return var;
	}

	/**
	 * Make the internal decision to create Bound, Enum, LinkedList var
	 *
	 * @param pvar
	 * @return
	 */
	public IntegerVariable createVar(PVariable pvar) {
		IntegerVariable var;
		int nbvalues = pvar.getDomain().getValues().length;
		int span = pvar.getDomain().getMaxValue() - pvar.getDomain().getMinValue() + 1;
        if (span > nbvalues || nbvalues < 300) { //there are some holes
			var = makeIntVar(pvar.getName(), pvar.getDomain().getValues());
            //the second condition is for very sparse variables !
            if (isVarOnlyInvolvedInExtConstraint(pvar) || 20 * span > 100 * nbvalues) {
                m.addVariable(Options.V_BLIST, var);
            } else {
                m.addVariable(Options.V_ENUM, var);
            }
        } else {
			if (isVarOnlyInvolvedInExtConstraint(pvar)) {
				var = makeIntVar(pvar.getName(), pvar.getDomain().getMinValue(), pvar.getDomain().getMaxValue());					
                m.addVariable(Options.V_BLIST, var);
            } else {
				var = makeIntVar(pvar.getName(), pvar.getDomain().getMinValue(), pvar.getDomain().getMaxValue());
                m.addVariable(Options.V_BOUND, var);
            }
		}
		return var;
	}

	public boolean isVarOnlyInvolvedInExtConstraint(PVariable pvar) {
		if (parser.getNbExtensionConstraints() != 0) {
			Iterator it = parser.getMapOfConstraints().values().iterator();
			for (; it.hasNext();) {
				PConstraint pc = (PConstraint) it.next();
				if (pc instanceof PExtensionConstraint) {
					PVariable[] scope = pc.getScope();
					for (int i = 0; i < scope.length; i++) {
						if (scope[i] == pvar) return true;
					}
				}
			}
		}
		return false;
	}

    public int getNbvar() {
		return parser.getNbVariables();
	}

}
