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

import choco.Choco;
import static choco.Choco.*;
import choco.Options;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import choco.kernel.solver.constraints.integer.extension.TuplesTable;
import parser.absconparseur.components.PConstraint;
import parser.absconparseur.components.PExtensionConstraint;
import parser.absconparseur.components.PRelation;
import parser.absconparseur.components.PVariable;
import parser.absconparseur.tools.InstanceParser;

import java.util.List;


/**
 *  The factory to create Extensionnal constraints
 **/
public class ExtConstraintFactory extends ObjectFactory {

    public ExtConstraintFactory(Model m, InstanceParser parser) {
		super(m, parser);
    }

	// add the neq into the difference graph
	public void preAnalyse() {
        for (final PConstraint pConstraint : parser.getMapOfConstraints().values()) {
            if (pConstraint instanceof PExtensionConstraint) {
                if (pConstraint.getArity() == 2) {
                    RelationFactory.detectIntensionConstraint((PExtensionConstraint) pConstraint);
                }
            }
        }
	}

	public static Constraint[] makeExtConstraint(PExtensionConstraint pec) {
		Constraint[] extct = new Constraint[1];
		PRelation prel = pec.getRelation();
		if (prel.getNbTuples() == 0) {
			if (prel.getSemantics().equals("supports"))
				extct[0] = FALSE;
			else extct[0] = TRUE;
			return extct;
		} else {
            if (prel.getSatEncoding() != null) {
                PVariable[] sc = pec.getScope();
                List<XmlClause> lcls = prel.getSatEncoding();
                extct = new Constraint[lcls.size()];
                for (int i = 0; i < lcls.size(); i++) {
                    XmlClause xcl = lcls.get(i);
                    IntegerVariable[] pos = new IntegerVariable[xcl.poslits.length];
                    IntegerVariable[] neg = new IntegerVariable[xcl.neglits.length];
                    for (int k = 0; k < pos.length; k++) {
                        pos[k] = sc[xcl.poslits[k]].getChocovar();
                    }
                    for (int k = 0; k < neg.length; k++) {
                        neg[k] = sc[xcl.neglits[k]].getChocovar();
                    }
                    extct[i] = Choco.clause(pos,neg);
                }
                return extct;
            } else {
                if (pec.getArity() == 2) {
                    PVariable[] sc = pec.getScope();
                    ModelConstraintFactory.ConstExp ctexp = pec.getIntensionCts();
                    if (ctexp != null) {
                        switch (ctexp) {
                            case eq:
                                extct[0] = eq(sc[0].getChocovar(), sc[1].getChocovar());
                                break;
                            case ne:
                                extct[0] = neq(sc[0].getChocovar(), sc[1].getChocovar());
                                break;
                            default:
                                return null;
                        }
                    } else {
                        extct[0] = relationPairAC(sc[0].getChocovar(), sc[1].getChocovar(), prel.getBrel());
                    }
                } else {
                    PVariable[] sc = pec.getScope();
                    IntegerVariable[] intvars = new IntegerVariable[sc.length];
                    for (int i = 0; i < intvars.length; i++) {
                        intvars[i] = sc[i].getChocovar();
                    }
                    LargeRelation lrel = prel.getLrel();
                    DFA dfa = prel.getDfa();
                    if (lrel != null) {
                        if (prel.getLrel() instanceof TuplesTable)
                            extct[0] = relationTupleAC(Options.C_EXT_AC32, intvars, prel.getLrel());
                        else
                            extct[0] = relationTupleAC("cp:ac"+algorithmAC, intvars, prel.getLrel());
                    } else {
                        extct[0] = regular(intvars, dfa);
                    }
                }
            }
        }
		return extct;
	}

    

}
