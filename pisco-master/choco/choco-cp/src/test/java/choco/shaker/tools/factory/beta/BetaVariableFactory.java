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

package choco.shaker.tools.factory.beta;

import static choco.Options.V_BLIST;
import static choco.Options.V_BOUND;
import static choco.Options.V_BTREE;
import static choco.Options.V_ENUM;
import static choco.Options.V_LINK;

import java.lang.reflect.Array;
import java.util.Random;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.shaker.tools.factory.VariableFactory.V;

abstract class AbstractIntVariableFactory extends AbstractVariableFactory<IntegerVariable> {

	public AbstractIntVariableFactory(String... defaultScope) {
		super(defaultScope);
	}

	@Override
	protected Class<IntegerVariable> getComponentClass() {
		return IntegerVariable.class;
	}
}

class BoolVariableFactory extends AbstractIntVariableFactory {


	public BoolVariableFactory() {
		super(BetaVariableFactory.V_BOOL);
	}

	@Override
	public IntegerVariable create(String option, Random r) {
		return Choco.makeBooleanVar(StringUtils.randomName());
	}

}

class ConstVariableFactory extends AbstractIntVariableFactory {

	public ConstVariableFactory() {
		super(BetaVariableFactory.V_CONSTANT);
	}

	@Override
	public IntegerVariable create(String option, Random r) {
		final int v = r.nextInt(maxDomainSize);
		return Choco.constant( valueOffset == null ?  v - maxDomainSize/2 : valueOffset.intValue() + v);
	}
}

class IntVariableFactory extends AbstractIntVariableFactory {

	protected final static String[] INTSCOPE = {V_ENUM, V_BOUND, V_BTREE, V_BLIST, V_LINK, BetaVariableFactory.V_UNBOUNDED};

	public IntVariableFactory() {
		super(INTSCOPE);
	}

	@Override
	public IntegerVariable create(String option, Random r) {
		int lowB, uppB;
		if(option == BetaVariableFactory.V_UNBOUNDED) {
			return Choco.makeIntVar(StringUtils.randomName());
		} else {
			if(valueOffset == null ) {
				uppB = r.nextInt(maxDomainSize);
				lowB = uppB - r.nextInt(maxDomainSize);
			} else {
				lowB = valueOffset.intValue() + r.nextInt(maxDomainSize);
				uppB = lowB + r.nextInt(maxDomainSize);
			}
			return Choco.makeIntVar(StringUtils.randomName(), lowB, uppB, option);
		}
	}
}



/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 12 mars 2009
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 */
public class BetaVariableFactory extends AbstractVariablePool<IntegerVariable> {

	public final static String V_CONSTANT = "cp:constant";

	public final static String V_BOOL = "cp:boolean";

	public final static String[] GSCOPE =  createScope();

	public final static String V_UNBOUNDED = "cp:unbounded";

	public final static String T_CONST_DUR ="cp:task:constant_duration";

	public final static String T_VAR_DUR ="cp:task:variable_duration";

	private static String[] createScope() {
		final int n = IntVariableFactory.INTSCOPE.length;
		final String[] scope = new String[n + 2];
		scope[0] = V_CONSTANT;
		scope[1] = V_BOOL;
		System.arraycopy(IntVariableFactory.INTSCOPE, 0, scope, 2, n);
		return scope;
	}

	public final ConstVariableFactory constF = new ConstVariableFactory();

	public final BoolVariableFactory boolF = new BoolVariableFactory();

	public final IntVariableFactory intF = new IntVariableFactory();

	public final TaskVariableFactory taskF = new TaskVariableFactory();


	public BetaVariableFactory() {
		super(GSCOPE);
	}

	@Override
	protected Class<IntegerVariable> getComponentClass() {
		return IntegerVariable.class;
	}

	

	public final IVariableFactory<IntegerVariable> getConstFactory() {
		return constF;
	}

	public final IVariableFactory<IntegerVariable> getBoolFactory() {
		return boolF;
	}

	public final IVariableFactory<IntegerVariable> getIntFactory() {
		return intF;
	}

	public final IVariableFactory<TaskVariable> getTaskFactory() {
		return taskF;
	}


	@Override
	public void cancelValueOffset() {
		constF.cancelValueOffset();
		boolF.cancelValueOffset();
		intF.cancelValueOffset();
		taskF.cancelValueOffset();

	}


	@Override
	public void setMaxCreated(int n) {
		constF.setMaxCreated(n);
		boolF.setMaxCreated(n);
		intF.setMaxCreated(n);
		taskF.setMaxCreated(n);
	}


	@Override
	public void setMaxDomSize(int maxDomSize) {
		constF.setMaxDomSize(maxDomSize);
		boolF.setMaxDomSize(maxDomSize);
		intF.setMaxDomSize(maxDomSize);
		taskF.setMaxDomSize(maxDomSize);
	}


	@Override
	public void setValueOffset(int valOffset) {
		constF.setValueOffset(valOffset);
		boolF.setValueOffset(valOffset);
		intF.setValueOffset(valOffset);
		taskF.setValueOffset(valOffset);
	}



	@Override
	public void cancelScope() {
		super.cancelScope();
		intF.cancelScope();
		taskF.cancelScope();
	}

	@Override
	public void setScope(String... options) {
		super.setScope(options);
		intF.setScope(options);
		intF.remScope(V_CONSTANT, V_BOOL);
	}

	@Override
	public void remScope(String... options) {
		super.remScope(options);
		intF.remScope(options);
	}


	protected String getOption(V v) {
		switch (v) {
		case BOOLVAR:return V_BOOL;
		case ENUMVAR:return V_ENUM;
		case BOUNDVAR:return V_BOUND;
		case BTREEVAR:return V_BTREE;
		case BLISTVAR:return V_BLIST;
		case LINKVAR:return V_LINK;
		case UNBOUNDED:return V_UNBOUNDED;
		case CST:return V_CONSTANT;
		default : return V_BOUND;
		}
	}
	/**
	 * Define a specific scope of variable tupe to pick up in
	 * @param vs the scope of variables
	 */
	public void scopes(V... vs){
		String[] opts = new String[vs.length];
		for (int i = 0; i < opts.length; i++) {
			opts[i] = getOption(vs[i]);
		}
		setScope(opts);
	}

	/**
	 * Create and return the corresponding variable
	 * @param v the type of variable
	 * @param r random
	 * @return IntegerVariable
	 */
	public IntegerVariable make(V v, Random r) {
		return make(getOption(v), r);
	}

	

	/**
	 * Get an array of variables
	 * @param nb number of variables to create
	 * @param v the type of variable
	 * @param r random
	 * @return array of variables
	 */
	public IntegerVariable[] make(int nb, V v, Random r){
		IntegerVariable[] variables = new IntegerVariable[nb];
		for (int i = 0; i < variables.length; i++) {
			variables[i] = make(v, r);
		}
		return variables;
	}

	@Override
	public IntegerVariable make(String option, Random r) {
		IntegerVariable var = super.make(option, r);
		if(var == null) {
			var = option == V_CONSTANT ? constF.make(r) :
				option == V_BOOL ?  boolF.make(r) : intF.make(r);
		}
		return var;
	}

	public final IntegerVariable makeBool(Random r) {
		return boolF.make(r);
	}
	
	public final IntegerVariable[] makeBool(int nb, Random r) {
		return boolF.make(nb, r);
	}

	public final TaskVariable makeTask(Random r) {
		return taskF.make(r);
	}
	
	public final TaskVariable[] makeTask(int nb, Random r) {
		return taskF.make(nb, r);
	}
	
	public final IntegerVariable makeConst(Random r) {
		return constF.make(r);
	}
	
	public final IntegerVariable[] makeConst(int nb, Random r) {
		return constF.make(nb, r);
	}
	final class TaskVariableFactory extends AbstractVariableFactory<TaskVariable> {

		public TaskVariableFactory() {
			super(T_CONST_DUR, T_VAR_DUR);
		}


		@Override
		protected Class<TaskVariable> getComponentClass() {
			return TaskVariable.class;
		}


		@Override
		public TaskVariable create(String option, Random r) {
			return Choco.makeTaskVar(
					StringUtils.randomName()+"-Task", 
					intF.make(r),
					option == T_VAR_DUR ? intF.make(r) : constF.make(r)
			);
		}
	}

	public static void main(String[] args) {
		BetaVariableFactory f = new BetaVariableFactory();
		f.setMaxCreated(5);
		f.setMaxDomSize(5);
		f.setScope(V_BOOL, V_BOUND, V_CONSTANT);
		CPModel m = new CPModel();
		m.addVariables(f.make(25, new Random(0)));
		m.addVariables(f.taskF.make(25, new Random(0)));
		//System.out.println(m.pretty());
	}

}
