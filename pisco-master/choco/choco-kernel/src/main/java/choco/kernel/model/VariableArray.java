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

package choco.kernel.model;

import choco.Choco;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.IteratorUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;

import java.io.Serializable;
import java.util.Iterator;

public class VariableArray implements IVariableArray, Serializable {

	private static final long serialVersionUID = -4465055351650054924L;

	protected final static Constraint[] NO_CONSTRAINTS = {};

	private Variable[] variables;
	private Variable[] extractedVariables;


	public VariableArray() {
		super();
	}

	public VariableArray(final Variable[] variables) {
		super();
		this.variables = variables.clone();
	}

	@Override
	public final int getNbVars() {
		return variables.length;
	}

	@Override
	public final Variable getVariable(final int i) {
		return variables[i];
	}

	@Override
	public final DisposableIterator<Variable> getVariableIterator() {
		return IteratorUtils.iterator(extractVariables());
	}

	@Override
	public final Variable[] getVariables() {
		return variables;
	}

	protected final void setVariables(final Variable variable) {
		this.variables = new Variable[]{variable};
		cancelExtractVariables();
	}

	protected final void setVariables(final Variable[] variables) {
		this.variables = variables;
		cancelExtractVariables();
	}

	public final void replaceBy(final Variable outVar, final Variable inVar){
		//if(inVar.getIndex() != outVar.getIndex()) { ??
		final long idx = outVar.getIndex();
		for(int i = 0; i < variables.length; i++){
			if(variables[i].getIndex() == idx){
				variables[i] = inVar;
			}
		}
		cancelExtractVariables();
	}

	protected final void replaceByConstantAt(final int outVarIndex, final int val){
		variables[outVarIndex] = Choco.constant(val);
		cancelExtractVariables(); 
	}


	protected Variable[] doExtractVariables() {
		return ArrayUtils.getNonRedundantObjects(Variable.class, variables);
	}
	
	protected final void cancelExtractVariables() {
		extractedVariables = null;
	}
	
	protected final void forceExtractVariables() {
		cancelExtractVariables();
		extractedVariables = doExtractVariables();
	}

	@Override
	public final Variable[] extractVariables() {
		if(extractedVariables == null){
			extractedVariables = doExtractVariables();
		}
		return extractedVariables;
	}

	protected final class VConstraintsDataStructure implements IConstraintList {



		public VConstraintsDataStructure() {
			super();
		}

		@Override
		public void _addConstraint(final Constraint c) {
			for(final Variable v : variables){
				v._addConstraint(c);
			}
		}

		@Override
		public void _removeConstraint(final Constraint c) {
			for(final Variable v : variables){
				v._removeConstraint(c);
			}			
		}

		@Override
		public boolean _contains(final Constraint c) {
			for(final Variable v : variables){
				if(!v._contains(c)){
					return false;
				}
			}
			return true;
		}

		@Override
		public void removeConstraints() {
			for(final Variable v : variables){
				v.removeConstraints();
			}
		}

		@Override
		public Constraint getConstraint(final int i) {
			return null;

		}

		@Override
		public Iterator<Constraint> getConstraintIterator(final Model m) {
			return new Iterator<Constraint>(){
				int n = 0;
				Iterator<Constraint> it = (variables.length > 0? variables[n].getConstraintIterator(m):null);
				Constraint c = null;

				public boolean hasNext() {
					while(true){
						if(it == null){
							return false;
						}else
							if(it.hasNext()){
								c = it.next();
								if(Boolean.TRUE.equals(m.contains(c))){
									return true;
								}
							}else
								if(n < variables.length){
									n++;
									if (n < variables.length) {
										it = variables[n].getConstraintIterator(m);
									}
								}else{
									return false;
								}
					}
				}

				public Constraint next() {
					return c;
				}

				public void remove() {
					it.remove();
				}
			};
		}

		@Override
		public Constraint[] getConstraints() {
			return NO_CONSTRAINTS;
		}

		@Override
		public int getNbConstraint(final Model m) {
			int sum = 0;
			for(final Variable v : variables){
				sum += v.getNbConstraint(m);
			}	
			return sum;
		}
	}

	@Override
	public String pretty() {
		return StringUtils.pretty(variables);
	}



}
