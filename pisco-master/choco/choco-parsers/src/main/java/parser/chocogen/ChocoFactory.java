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

import choco.Options;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import parser.absconparseur.components.*;
import parser.absconparseur.tools.InstanceParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * User:    hcambaza
 * Date:    15 avr. 2008
 */
public class ChocoFactory {

	protected static float ratioHole = 0.7f;

	protected InstanceParser parser;

	//status of restart mode (false if extension problems)
	//true otherwise depending the initial propagation time
	protected Boolean restartMode = null;

    //The model we are building according to the parser
    protected Model m;


	protected RelationFactory relfactory;

    //can be redondant variables if they are linked by
	//an equality constraint
	protected IntegerVariable[] vars;

	protected List cstrs;

	public ChocoFactory(InstanceParser parser, Model m) {
		this.m = m;
		this.parser = parser;
		restartMode = null;
	}

	public Model getM() {
		return m;
	}


//******************************************************************//
//***************** Factories **************************************//
//******************************************************************//


	public void createVariables() {
		VariableFactory factory = new VariableFactory(m, parser);
		vars = new IntegerVariable[factory.getNbvar()];
		PVariable[] pvars = parser.getVariables();
		for (int i = 0; i < pvars.length; i++) {
			vars[i] = factory.makeVariable(pvars[i]);
		}
		m.addVariables(vars);
	}


	public void createRelations() {
		relfactory = new RelationFactory(m, parser);
		Iterator<PRelation> maprel = parser.getMapOfRelations().values().iterator();
		for (; maprel.hasNext();) {
			PRelation prel = maprel.next();
			if (relfactory.isSatDecomposable(prel)) {
               RelationFactory.makeClausesEncoding(prel);
            } else {
                if (prel.getArity() == 2) {
                    BinRelation brel = relfactory.makeBinRelation(prel);
                    prel.setBrel(brel);
                } else {
                    //DFA lrel = relfactory.makeDFA(prel);
                    //prel.setDfa(lrel);
                    LargeRelation lrel = relfactory.makeLargeRelation(prel);
                    prel.setLrel(lrel);
                }
            }
        }
	}

    public void createConstraints(boolean forceExp){
        ExtConstraintFactory extFact = new ExtConstraintFactory(m, parser);
        extFact.preAnalyse();

        Map pcstr = parser.getMapOfConstraints();
		Iterator it = pcstr.keySet().iterator();
        cstrs = new ArrayList(16);
        String options = (forceExp? Options.E_DECOMP:"");
        while (it.hasNext()) {
			PConstraint pc = (PConstraint) pcstr.get(it.next());
			makeModelConstraint(pc, options);
		}

    }

    public void makeModelConstraint(PConstraint pc, String options) {
		Constraint[] c = null;
        if (pc instanceof PExtensionConstraint) {
			c = ExtConstraintFactory.makeExtConstraint((PExtensionConstraint) pc);
		} else
		if (pc instanceof PIntensionConstraint) {
            c = ModelConstraintFactory.makeIntensionConstraint((PIntensionConstraint) pc);
		}else
		if (pc instanceof PGlobalConstraint) {
            c = GloConstraintFactory.makeGlobalConstraint((PGlobalConstraint) pc);
		}
        if (c != null)
            m.addConstraints(options, c);
	}

}
