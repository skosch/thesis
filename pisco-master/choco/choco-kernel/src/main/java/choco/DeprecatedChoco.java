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

package choco;

import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.ConstantFactory;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealConstantVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.model.variables.set.SetConstantVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.variables.real.RealMath;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 6 août 2008
 * Time: 16:08:55
 *
 * This class must contain every deprecated method of the Choco class.
 * The idea is that we can easily unbranch deprecated method to update tests.
 */
public class DeprecatedChoco extends Choco{

    private DeprecatedChoco() {
        super();
    }


    @Deprecated
    public static IntegerVariable makeIntVar(String name, VariableType type, int binf, int bsup) {
        if(binf>bsup) {
			throw new ModelException("makeIntVar : binf > bsup");
		}
		return new IntegerVariable(name, binf, bsup);
	}

    @Deprecated
    protected static IntegerVariable[] makeIntVarArray(String name,VariableType type, int n,int binf, int bsup) {
		if(binf>bsup) {
			throw new ModelException("makeIntVarArray : binf > bsup");
		}
		IntegerVariable[] vars=new IntegerVariable[n];
		for (int i = 0; i < n; i++) {
			vars[i]=makeIntVar(name+"_"+i, type, binf, bsup);
		}
		return vars;
	}

    @Deprecated
    protected static IntegerVariable[][] makeIntVarArray(String name,VariableType type, int n,int m,int binf, int bsup) {
        if(binf>bsup) {
			throw new ModelException("makeIntVarArray : binf > bsup");
		}
		IntegerVariable[][] vars=new IntegerVariable[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				vars[i][j]=makeIntVar(name+"_"+i+"_"+j, type, binf, bsup);
			}
		}
		return vars;
	}

    @Deprecated
    public static IntegerVariable makeEnumIntVar(String name, int binf, int bsup) {
		return makeIntVar(name, binf, bsup, "cp:enum");
	}

	/**
	 * Creates a new search variable with an enumerated domain
	 *
	 * @param name   the name of the variable
	 * @param values allowed in the domain (may be unsorted, but not with duplicates !)
	 * @return the variable
     * @Deprecated
	 */
	public static IntegerVariable makeEnumIntVar(String name, int[] values) {
		int[] values2 = new int[values.length];
		System.arraycopy(values, 0, values2, 0, values.length);
		Arrays.sort(values2);
		return makeIntVar(name, values2, "cp:enum");
	}

	/**
	 * Creates a new search variable with an enumerated domain
	 *
	 * @param name   the name of the variable
	 * @param values allowed in the domain (may be unsorted, but not with duplicates !)
	 * @return the variable
     * @Deprecated
	 */
	public static IntegerVariable makeEnumIntVar(String name, ArrayList<Integer> values) {
		int[] values2 = new int[values.size()];
		for (int i = 0; i < values.size(); i++) {
			values2[i] = values.get(i);
		}
		Arrays.sort(values2);
		return makeIntVar(name, values2, "cp:enum");
	}

    @Deprecated
    public static IntegerVariable[] makeEnumIntVarArray(String name, int size, int binf, int bsup) {
		return makeIntVarArray(name, size, binf, bsup, "cp:enum");
	}

    @Deprecated
    public static IntegerVariable[][] makeEnumIntVarArray(String name, int dim, int dim2, int binf, int bsup) {
		return makeIntVarArray(name, dim,dim2, binf, bsup, "cp:enum");
	}

	/******** BINARY_TREE ***********/
	/**
	 * Create a binary tree domain for integer variable
	 * @param name the name of the variable
	 * @param binf the lower bound of the variable
	 * @param bsup the upper bound of the variables
	 * @return an IntegerVariable object
     * @Deprecated
	 */
	public static IntegerVariable makeBinTreeIntVar(String name, int binf, int bsup){
		return makeIntVar(name, binf, bsup, "cp:btree");
	}

	/**
	 * Create a binary tree domain for integer variable
	 * @param name the name of the variable
	 * @param values array of allowed values
	 * @return an IntegerVariable object
     * @Deprecated
	 */
	public static IntegerVariable makeBinTreeIntVar(String name, int[] values) {
		int[] values2 = new int[values.length];
		System.arraycopy(values, 0, values2, 0, values.length);
		Arrays.sort(values2);
		IntegerVariable v = makeIntVar(name, values2, "cp:enum");
		return v;
	}

	/**
	 * Create a binary tree domain for integer variable
	 * @param name the name of the variable
	 * @param values list of allowed values
	 * @return an IntegerVariable object
     * @Deprecated
	 */
	public static IntegerVariable makeBinTreeIntVar(String name, ArrayList<Integer> values) {
		int[] values2 = new int[values.size()];
		for(int i =0; i < values.size(); i++){
			values2[i] = values.get(i);
		}
		Arrays.sort(values2);
		return makeIntVar(name, values2, "cp:btree");
	}

	/**
	 * Create an array of integer variable with a binary tree domain
	 * @param name the name of the variable
	 * @param size size of the array
	 * @param binf the lower bound of every  variable
	 * @param bsup the upper bound of every variables
	 * @return an IntegerVariable array
     * @Deprecated
	 */
	public static IntegerVariable[] makeBinTreeIntVarArray(String name, int size, int binf, int bsup){
		return makeIntVarArray(name, size, binf, bsup, "cp:btree");
	}

	/**
	 * Create an double array of integer variable with a binary tree domain
	 * @param name the name of the variable
	 * @param dim size of the array
	 * @param dim2 size of the array
	 * @param binf the lower bound of every  variable
	 * @param bsup the upper bound of every variables
	 * @return an IntegerVariable array
     * @Deprecated
	 */
	public static IntegerVariable[][] makeBinTreeIntVarArray(String name, int dim, int dim2, int binf, int bsup){
		return makeIntVarArray(name, dim,dim2, binf, bsup, "cp:btree");
	}

	/******** INTEGER_BOUNDED ***********/
    @Deprecated
    public static IntegerVariable makeBoundIntVar(String name, int binf, int bsup) {
		return makeIntVar(name, binf, bsup, "cp:bound");
	}

    @Deprecated
    public static IntegerVariable[] makeBoundIntVarArray(String name, int size, int binf, int bsup) {
		return makeIntVarArray(name, size, binf, bsup, "cp:bound");
	}

    @Deprecated
    public static IntegerVariable[][] makeBoundIntVarArray(String name, int dim1, int dim2, int binf, int bsup) {
		return makeIntVarArray(name, dim1,dim2, binf, bsup, "cp:bound");
	}

    @Deprecated
    public static IntegerVariable makeLinkedListIntVar(String name, int binf, int bsup) {
		return makeIntVar(name, binf, bsup, "cp:link");
	}

    @Deprecated
    public static IntegerVariable makeLinkedListIntVar(String name, int[] values) {
		return makeIntVar(name, values, "cp:link");
	}

    /**************REAL*********/


		/**
		* Arounds a double d to <code>[d - epsilon, d + epilon]</code>.
		*/
        @Deprecated
        public RealConstantVariable around(double d) {
		return cst(RealMath.prevFloat(d), RealMath.nextFloat(d));
		}

		/**
		* Makes a constant interval from a double d ([d,d]).
		*/
        @Deprecated
        public RealConstantVariable cst(double d) {
		    return new RealConstantVariable(d, d);
		}

		/**
		* Makes a constant interval between two doubles [a,b].
		*/
        @Deprecated
        public RealConstantVariable cst(double a, double b) {
		return new RealConstantVariable(a, b);
	}

//    @Deprecated
//    public static RealVariable makeRealVar(String name, double binf, double bsup) {
//		return new RealVariable(name, binf, bsup);
//	}

	/**
		 * @see {@link Choco#precedence(TaskVariable, TaskVariable)}
		 */
		@Deprecated 
		public static Constraint preceding(TaskVariable t1, TaskVariable t2) {
			return Choco.precedenceDisjoint(t1, t2, Choco.constant(1));
		}


		/**
		 * @see {@link Choco#precedenceDisjoint(IntegerVariable, int, IntegerVariable, int, IntegerVariable)}
		 */
		@Deprecated 
		public static Constraint preceding(IntegerVariable v1, int dur1, IntegerVariable v2, int dur2, IntegerVariable bool) {
			return Choco.precedenceDisjoint(v1, dur1, v2, dur2, bool);
		}


		/**
		 * @see {@link Choco#precedenceDisjoint(TaskVariable, TaskVariable, IntegerVariable)}
		 */
		@Deprecated 
		public static Constraint preceding(TaskVariable t1, TaskVariable t2, IntegerVariable direction) {
			return Choco.precedenceDisjoint(t1, t2, direction);
		}


	@Deprecated
		public static Constraint cumulative(String name, TaskVariable[] tasks, IntegerVariable[] heights, IntegerVariable capa, String... options) {
			return Choco.cumulative(name, tasks, heights,null, Choco.constant(0),  capa, null, options);
		}


		@Deprecated
		public static Constraint cumulative(TaskVariable[] tasks, int[] heights, int capa, String... options) {
			return Choco.cumulative(null, tasks, Choco.constantArray(heights), null, Choco.constant(0),Choco.constant(capa), null,options);
		}


		@Deprecated
		public static Constraint cumulative(String name, IntegerVariable[] starts, IntegerVariable[] ends, IntegerVariable[] durations, IntegerVariable[] heights, IntegerVariable capa, String... options) {
			final TaskVariable[] tasks = Choco.makeTaskVarArray("t", starts, ends, durations);
			return Choco.cumulative(name, tasks, heights, Choco.constant(0), capa, options);
		
		}


		@Deprecated
		public static Constraint cumulative(IntegerVariable[] starts, IntegerVariable[] ends, IntegerVariable[] durations, IntegerVariable[] heights, IntegerVariable capa, String... options) {
			TaskVariable[] t = new TaskVariable[starts.length];
			for(int i = 0; i < starts.length; i++){
				t[i] = Choco.makeTaskVar("", starts[i], ends[i], durations[i]);
			}
			return Choco.cumulative(null, t, heights, Choco.constant(0), capa, options);
		}


		@Deprecated
		public static Constraint cumulative(IntegerVariable[] starts, IntegerVariable[] ends, IntegerVariable[] durations, int[] heights, int capa, String... options) {
			TaskVariable[] t = new TaskVariable[starts.length];
			for(int i = 0; i < starts.length; i++){
				t[i] = Choco.makeTaskVar("", starts[i], ends[i], durations[i]);
			}
			return Choco.cumulative(null, t, Choco.constantArray(heights), Choco.constant(0), Choco.constant(capa), options);
		}


		@Deprecated
		public static Constraint cumulative(IntegerVariable[] starts, IntegerVariable[] durations, IntegerVariable[] heights, IntegerVariable capa, String... options) {
			TaskVariable[] t = new TaskVariable[starts.length];
			for(int i = 0; i < starts.length; i++){
				t[i] = Choco.makeTaskVar("", starts[i], durations[i]);
			}
			return Choco.cumulative(null, t, heights, Choco.constant(0),capa, options);
		}


	/**
		 * Returns a disjunctive constraint
		 *
		 * @param starts to schedule
		 * @param durations of each task
		 * @param options options of the variable
		 * @return the disjunctive constraint
		 */
		@Deprecated
		public static Constraint disjunctive(IntegerVariable[] starts, int[] durations,String...options) {
			TaskVariable[] t = new TaskVariable[starts.length];
			for(int i = 0; i < starts.length; i++){
				t[i] = Choco.makeTaskVar("", starts[i], Choco.constant(durations[i]));
			}
			return Choco.disjunctive(null, t,  null, options);
		}


		@Deprecated
		public static Constraint disjunctive(IntegerVariable[] starts, IntegerVariable[] durations, String... options) {
			TaskVariable[] t = new TaskVariable[starts.length];
			for(int i = 0; i < starts.length; i++){
				t[i] = Choco.makeTaskVar("", starts[i], durations[i]);
			}
			return Choco.disjunctive(null, t,  null, options);
		}


		@Deprecated
		public static Constraint disjunctive(IntegerVariable[] starts, IntegerVariable[] ends, IntegerVariable[] durations,String... options) {
			TaskVariable[] t = new TaskVariable[starts.length];
			for(int i = 0; i < starts.length; i++){
				t[i] = Choco.makeTaskVar("", starts[i], ends[i], durations[i]);
			}
			return Choco.disjunctive(null, t,  null, options);
		}


		@Deprecated
		public static Constraint disjunctive(String name, IntegerVariable[] starts, IntegerVariable[] ends, IntegerVariable[] durations,IntegerVariable uppBound, String... options) {
			final TaskVariable[] tasks = Choco.makeTaskVarArray("task-", starts, ends, durations);
			return Choco.disjunctive(name, tasks,null, uppBound, options);
		}


	/******** SET ***********/
    @Deprecated
    public static SetVariable[] makeSetVarArray(String name,int n,int binf,int bsup) {
		SetVariable[] vars=new SetVariable[n];
		for (int i = 0; i < vars.length; i++) {
			vars[i]=makeSetVar(name+"-"+i, binf, bsup, "cp:enum");
		}
		return vars;
	}


    @Deprecated
    public static SetVariable makeBoundSetVar(String name, int binf, int bsup) {
		return makeSetVar(name,binf, bsup, "cp:bound");
	}

    @Deprecated
    public static SetVariable[] makeBoundSetVarArray(String name,int n,int binf, int bsup) {
		return makeSetVarArray(name, n, binf, bsup, "cp:bound");
	}


    @Deprecated
    public static SetVariable makeEnumSetVar(String name, int binf, int bsup) {
		return makeSetVar(name,binf, bsup,"cp:enum");
	}

    @Deprecated
    public static SetVariable[] makeEnumSetVarArray(String name,int n,int binf, int bsup) {
		return makeSetVarArray(name, n, binf, bsup,"cp:enum");
	}

    /**
	 * Ensures that the lower bound of occurrence is at least equal to the number of occurences
	 * size{forall v in vars | v = value} <= occurence
     * @see Choco#occurrenceMin(int, choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable[])
	 */
    @Deprecated
    public static Constraint occurenceMin(int value, IntegerVariable occurence, IntegerVariable... vars) {
		IntegerVariable[] variables = new IntegerVariable[vars.length + 2];
		variables[0] = constant(value);
		variables[1] = occurence;
		System.arraycopy(vars, 0, variables, 2, vars.length);
		return new ComponentConstraint(ConstraintType.OCCURRENCE, -1, variables);
	}

	/**
	 * Ensures that the upper bound of occurrence is at most equal to the number of occurences
	 * size{forall v in vars | v = value} >= occurence
     * @see Choco#occurrenceMax(int, choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable[])
	 */
    @Deprecated
    public static Constraint occurenceMax(int value, IntegerVariable occurence, IntegerVariable... vars) {
		IntegerVariable[] variables = new IntegerVariable[vars.length + 2];
		variables[0] = constant(value);
		variables[1] = occurence;
		System.arraycopy(vars, 0, variables, 2, vars.length);
		return new ComponentConstraint(ConstraintType.OCCURRENCE, 1, variables);
	}

    
    @Deprecated
    public static SetVariable makeConstantSetVar(String name, int... value) {
		return new SetConstantVariable(constant(value.length), value);
	}

    @Deprecated
	public static RealConstantVariable makeConstantVar(String name, double value) {
		return new RealConstantVariable(value);
	}

    @Deprecated
	public static IntegerConstantVariable makeConstantVar(String name, int value) {
		return new IntegerConstantVariable(value);
	}

    @Deprecated
    public static SetVariable constant(String name, int... value) {
		return ConstantFactory.getConstant(value);
	}

    @Deprecated
	public static RealConstantVariable constant(String name, double value) {
		return ConstantFactory.getConstant(value);
	}

    @Deprecated
	public static IntegerConstantVariable constant(String name, int value) {
		return ConstantFactory.getConstant(value);
	}
}
