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

package choco.cp.solver.constraints.strong.xmlmodel;

import choco.Options;
import choco.cp.solver.constraints.strong.StrongConsistencyManager;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import parser.absconparseur.components.PConstraint;
import parser.absconparseur.components.PExtensionConstraint;
import parser.absconparseur.components.PGlobalConstraint;
import parser.absconparseur.components.PIntensionConstraint;
import parser.absconparseur.tools.InstanceParser;
import parser.chocogen.ChocoFactory;
import parser.chocogen.ExtConstraintFactory;
import parser.chocogen.GloConstraintFactory;
import parser.chocogen.ModelConstraintFactory;

import java.util.*;

public class ChocoFactoryRPC extends ChocoFactory {
	public ChocoFactoryRPC(InstanceParser parser, Model m) {
		super(parser, m);
	}

	public void createConstraints(boolean forceExp, boolean light) {
		ExtConstraintFactory extFact = new ExtConstraintFactory(m, parser);
		extFact.preAnalyse();

		final Collection<Constraint> maxRPCConstraints = new ArrayList<Constraint>(16);

		Map<String, PConstraint> pcstr = parser.getMapOfConstraints();
		String options = (forceExp ? Options.E_DECOMP : "");
		for (PConstraint pc : pcstr.values()) {
			for (Constraint c : makeModelConstraint(pc)) {
				if (nbVariables(c) == 2) {
					maxRPCConstraints.add(c);
				} else {
					m.addConstraint(options, c);
				}
			}
		}
		if (maxRPCConstraints.size() > 2) {
			final Set<Variable> variables = new HashSet<Variable>(16);
			for (Constraint c : maxRPCConstraints) {
				for (Variable v : c.extractVariables()) {
					if (v instanceof IntegerVariable
							&& !(v instanceof IntegerConstantVariable)) {
						variables.add(v);
					}
				}
				// variables.addAll(Arrays.asList(c.getVariables()));
			}
			final Constraint c = new ComponentConstraint(StrongConsistencyManager.class,
					maxRPCConstraints.toArray(new Constraint[maxRPCConstraints
							.size()]), variables.toArray(new Variable[variables
							.size()]));

			if (light) {
				c.addOption("light");
			}

			m.addConstraint(c);
		} else {
			for (Constraint c : maxRPCConstraints) {
				m.addConstraint(options, c);
			}
		}
	}

	private static int nbVariables(Constraint c) {
		int nb = 0;
		for (Variable v : c.extractVariables()) {
			if (v instanceof IntegerVariable
					&& !(v instanceof IntegerConstantVariable)) {
				nb++;
			}
		}
		return nb;
	}

	public static Constraint[] makeModelConstraint(PConstraint pc) {
		Constraint[] c = null;
		if (pc instanceof PExtensionConstraint) {
			c = ExtConstraintFactory
					.makeExtConstraint((PExtensionConstraint) pc);
		} else if (pc instanceof PIntensionConstraint) {
			c = ModelConstraintFactory.makeIntensionConstraint((PIntensionConstraint) pc);
		} else if (pc instanceof PGlobalConstraint) {
			c = GloConstraintFactory.makeGlobalConstraint((PGlobalConstraint) pc);
		}
		return c;
	}
}
