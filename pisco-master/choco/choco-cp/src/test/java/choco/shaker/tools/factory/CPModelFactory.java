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

package choco.shaker.tools.factory;

import java.util.Random;

import choco.cp.model.CPModel;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.shaker.tools.factory.ConstraintFactory.C;
import choco.shaker.tools.factory.beta.BetaVariableFactory;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 12 mars 2009
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 */
public class CPModelFactory {

	VariableFactory vf = new VariableFactory();

	BetaVariableFactory bvf = new BetaVariableFactory();

	ConstraintFactory cf = new ConstraintFactory();
	OperatorFactory of = new OperatorFactory();
	MetaConstraintFactory mcf = new MetaConstraintFactory();

	//********************************************
	//********* METACONSTRAINTS ******************
	//********************************************


	public void setSchedulingVarFactory() {
		bvf.setValueOffset(1);
	}

	/**
	 * Declare specific metaconstraints to use
	 * @param mcs metaconstraint types
	 */
	public void uses(MetaConstraintFactory.MC... mcs){
		mcf.scopes(mcs);
	}

	/**
	 * Force use of metacontraints
	 */
	public void includesMetaconstraints(){
		mcf.scopes(MetaConstraintFactory.MC.values());
	}

	//********************************************
	//************* CONSTRAINTS ******************
	//********************************************
	/**
	 * Declare specific constraints to use
	 * @param cs constraint types
	 */
	public void uses(ConstraintFactory.C... cs){
		cf.scopes(cs);
	}

	//********************************************
	//*************** OPERATORS ******************
	//********************************************
	/**
	 * Declare specific operators to use
	 * @param os operator types
	 */
	public void uses(OperatorFactory.O... os){
		of.scopes(os);
	}

	/**
	 * Force use of operators
	 */
	public void includesOperators(){
		of.scopes(OperatorFactory.O.values());
	}

	//********************************************
	//**************** VARIABLES *****************
	//********************************************

	/**
	 * Declare specific variables to use
	 * @param vs variable types
	 */
	public void uses(VariableFactory.V... vs){
		vf.scopes(vs);
		bvf.scopes(vs);
	}

	/**
	 * Define the pool of variables to use
	 * @param vars variables
	 */
	public void defines(IntegerVariable... vars){
		vf.definePool(vars);
		bvf.definePool(vars);
	}

	/**
	 * Limit the number of variables created to nb
	 * @param nb max number of created variables
	 */
	public void limits(int nb){
		vf.setMaxCreated(nb);
		bvf.setMaxCreated(nb);
	}

	/**
	 * Set the maximum domain size
	 * @param size domain size
	 */
	public void domain(int size){
		vf.setMaxDomSize(size);
		bvf.setMaxDomSize(size);
	}

	//********************************************
	//************** PARAMATERS ******************
	//********************************************

	/**
	 * Declare a specific depth for expressions
	 * default = 2
	 * @param d max depth
	 */
	public void depth(int d){
		of.depth(d);
	}

	/**
	 * Initialize data structures
	 */
	private void init(){
		// declare dependencies
		mcf.depends(cf);
		cf.depends(of, vf);
		cf.depends(bvf);
		of.depends(vf, cf);

		// If no metaconstraints must be used...
		if(mcf.scope.size()==0){
			mcf.scope.add(MetaConstraintFactory.MC.NONE);
		}
		// If no operators must be used...
		if(of.scope.size()==0){
			of.scope.add(OperatorFactory.O.NONE);
		}
	}

	/**
	 * Create a random model
	 * @param r random
	 * @return a CPModel
	 */
	public CPModel model(Random r){
		init();
		CPModel m = new CPModel();
		Constraint c = mcf.make(r);
		m.addConstraint(c);
		return m;
	}

	public CPModel model(int n, Random r){
		init();
		CPModel m = new CPModel();
		m.addConstraints( cf.make(n, r));
		return m;
	}

}
