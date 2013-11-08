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

package choco.kernel.model.variables;

import choco.kernel.model.IConstraintList;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintManager;
import choco.kernel.model.constraints.ExpressionManager;
import choco.kernel.model.constraints.ManagerFactory;

import java.util.*;

/*
 * User:    charles
 * Date: 8 août 2008
 */
public abstract class ComponentVariable extends AbstractVariable {


	public final static IConstraintList NO_CONSTRAINTS_DS = new NoConstraintDataStructure();
	
	protected final Object parameters;
	protected String variableManager;
	protected String expressionManager;
	protected Operator operator;
	
	/**
	 * For IntegerVariable, RealVariable, SetVariable.
	 */
	protected ComponentVariable(final VariableType variableType, boolean enableOption, final Object parameters,IConstraintList constraints) {
		super(variableType,enableOption,constraints);
		this.parameters = parameters;
		this.operator = Operator.NONE;
	}
	
	/**
	 * For expressions 
	 */
	protected ComponentVariable(final VariableType variableType, final Object parameters, final ComponentVariable... vars) {
		super(variableType, vars, false); //disable options
		this.parameters=parameters;
	}
	
	/**
	 * For expressions 
	 */
	public ComponentVariable(final VariableType variableType, final Operator operator, final Object parameters, final ComponentVariable... vars) {
		this(variableType, parameters, vars);
		this.operator = operator;
	}

	/**
	 * For Expressions 
	 */
	public ComponentVariable(final VariableType variableType, final String operatorManager, final Object parameters, final ComponentVariable... vars) {
		this(variableType, parameters, vars);
		this.expressionManager = operatorManager;
	}


	protected final String getComponentClass() {
		return variableManager;
	}

	protected final String getOperatorClass(){
		if(expressionManager!=null){
			return expressionManager;
		}
		return variableManager;
	}


	public final Object getParameters() {
		return parameters;
	}

	public final Operator getOperator() {
		return operator;
	}

//	public final void setOperator(Operator operator) {
//		this.operator = operator;
//	}

	



	public void findManager(Properties propertiesFile) {
		if (variableManager == null && !type.equals(VariableType.NONE)){
			variableManager = propertiesFile.getProperty(type.property);
		}
		if(expressionManager == null && !operator.equals(Operator.NONE)){
			expressionManager = propertiesFile.getProperty(operator.property);
		}
		if(variableManager == null && expressionManager == null){
			throw new ModelException("Can not find "+type.property+" or "
					+ operator.property+" in application.properties");
		}
	}

	public VariableManager<?> getVariableManager() {
		return ManagerFactory.loadVariableManager(getComponentClass());
	}


	public ExpressionManager getExpressionManager() {
		return ManagerFactory.loadExpressionManager(getOperatorClass());
	}
	
	@Override
	public ConstraintManager<?> getConstraintManager() {
		return ManagerFactory.loadConstraintManager(getOperatorClass());
	}





	protected final static class ConstraintsDataStructure implements IConstraintList {

		List<Constraint> constraints;
		Constraint[] reuseConstraints;

		public ConstraintsDataStructure() {
			super();
			constraints = new ArrayList<Constraint>(10);
		}

		@Override
		public void _addConstraint(Constraint c) {
			reuseConstraints=null;
            constraints.add(c);
		}

		@Override
		public void _removeConstraint(Constraint c) {
			if(constraints.remove(c)) reuseConstraints=null;			
		}

        @Override
        public boolean _contains(final Constraint c) {
            return constraints.contains(c);
        }

        @Override
		public void removeConstraints() {
			constraints.clear();
			reuseConstraints=null;

		}

		@Override
		public Constraint getConstraint(int i) {
			return constraints.get(i);

		}

		@Override
		public Iterator<Constraint> getConstraintIterator(final Model m) {
			  return new Iterator<Constraint>(){
		            Constraint c;
		            Iterator<Constraint> it = constraints.iterator();

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
		                    }else{
		                        return false;
		                    }
		                }
		            }

		            @Override
		            public Constraint next() {
		                return c;
		            }

		            @Override
		            public void remove() {
		                it.remove();
		            }
		    };
		    
		}

		@Override
		public Constraint[] getConstraints() {
			if(reuseConstraints == null) {
				reuseConstraints = constraints.toArray(new Constraint[constraints.size()]);
			}
			return reuseConstraints;
		}

		@Override
		public int getNbConstraint(Model m) {
			int sum = 0;
			for(int i = 0; i < constraints.size(); i++){
				if(Boolean.TRUE.equals(m.contains(constraints.get(i)))){
					sum++;
				}
			}
			return sum;
		}
	}

	private final static class NoConstraintDataStructure implements IConstraintList {

		@Override
		public void _addConstraint(Constraint c) {}

		@Override
		public void _removeConstraint(Constraint c) {}

        @Override
        public boolean _contains(final Constraint c) {
            return false;
        }

        @Override
		public void removeConstraints() {}

		@Override
		public Constraint getConstraint(int i) {
			return null;
		}

		@Override
		public Iterator<Constraint> getConstraintIterator(Model m) {
			return Collections.<Constraint>emptyList().iterator();
		}

		@Override
		public Constraint[] getConstraints() {
			return NO_CONSTRAINTS;
		}

		@Override
		public int getNbConstraint(Model m) {
			return 0;
		}


	}

}
