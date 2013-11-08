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

package choco.kernel.common.util.tools;

import static choco.Choco.makeBooleanVar;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;
import choco.kernel.solver.variables.real.RealVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.solver.variables.set.SetVar;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public final class VariableUtils {


    private VariableUtils() {
        super();
    }
    
    public static Var[] getVar(Solver solver, Variable[] variables, int begin, int end) {
        if (end > begin && begin >= 0 && end <= variables.length) {
            Var[] vars = new Var[end - begin];
            for (int i = begin; i < end; i++) {
                vars[i - begin] = solver.getVar(variables[i]);
            }
            return vars;
        }
        return null;
    }


    public static IntDomainVar[] getVar(Solver solver, IntegerVariable[] variables, int begin, int end) {
        if (end > begin && begin >= 0 && end <= variables.length) {
            IntDomainVar[] vars = new IntDomainVar[end - begin];
            for (int i = begin; i < end; i++) {
                vars[i - begin] = solver.getVar(variables[i]);
            }
            return vars;
        }
        return null;
    }

    public static SetVar[] getVar(Solver solver, SetVariable[] variables, int begin, int end) {
        if (end > begin && begin >= 0 && end <= variables.length) {
            SetVar[] vars = new SetVar[end - begin];
            for (int i = begin; i < end; i++) {
                vars[i - begin] = solver.getVar(variables[i]);
            }
            return vars;
        }
        return null;
    }

    public static TaskVar[] getVar(Solver solver, TaskVariable[] variables, int begin, int end) {
        if (end > begin && begin >= 0 && end <= variables.length) {
            TaskVar[] vars = new TaskVar[end - begin];
            for (int i = begin; i < end; i++) {
                vars[i - begin] = solver.getVar(variables[i]);
            }
            return vars;
        }
        return null;
    }

    public static IntDomainVar[] getIntVar(Solver solver, Variable[] variables, int begin, int end) {
        if (end > begin && begin >= 0 && end <= variables.length) {
            IntDomainVar[] vars = new IntDomainVar[end - begin];
            for (int i = begin; i < end; i++) {
                vars[i - begin] = solver.getVar((IntegerVariable) variables[i]);
            }
            return vars;
        }
        return null;
    }

    public static SetVar[] getSetVar(Solver solver, Variable[] variables, int begin, int end) {
        if (end > begin && begin >= 0 && end <= variables.length) {
            SetVar[] vars = new SetVar[end - begin];
            for (int i = begin; i < end; i++) {
                vars[i - begin] = solver.getVar((SetVariable) variables[i]);
            }
            return vars;
        }
        return null;
    }

    public static TaskVar[] getTaskVar(Solver solver, Variable[] variables, int begin, int end) {
        if (end > begin && begin >= 0 && end <= variables.length) {
            TaskVar[] vars = new TaskVar[end - begin];
            for (int i = begin; i < end; i++) {
                vars[i - begin] = solver.getVar((TaskVariable) variables[i]);
            }
            return vars;
        }
        return null;
    }


    //****************************************************************//
    //********* TYPE *******************************************//
    //****************************************************************//


    public static boolean checkInteger(VariableType v) {
        return v == VariableType.INTEGER || v == VariableType.CONSTANT_INTEGER;
    }

    public static boolean checkSet(VariableType v) {
        return v == VariableType.SET || v == VariableType.CONSTANT_SET;
    }

    public static boolean checkReal(VariableType v) {
        return v == VariableType.REAL || v == VariableType.CONSTANT_DOUBLE || v == VariableType.REAL_EXPRESSION;
    }

    /**
     * Check the type of each variable and compute a int value
     *
     * @param v1 type of the first variable
     * @param v2 type of he second variable
     * @return a value corresponding to the whole type
     *         <p/>
     *         if the type is integer return 1 * position
     *         if the type is set return 2 * position
     *         if the type is real return 3 * position
     *         <p/>
     *         where position is 10 for v1 and 1 for v2
     */
    public static int checkType(VariableType v1, VariableType v2) {
        int t1 = 0;
        int t2 = 0;
        if (checkInteger(v1)) {
            t1 = 1;
        } else if (checkSet(v1)) {
            t1 = 2;
        } else if (checkReal(v1)) {
            t1 = 3;
        }
        if (checkInteger(v2)) {
            t2 = 1;
        } else if (checkSet(v2)) {
            t2 = 2;
        } else if (checkReal(v2)) {
            t2 = 3;
        }
        return 10 * t1 + t2;

    }

    /**
     * A quickSort algorithm for sorting a table of variable according
     * to a table of integers.
     *
     * @param a     : the integer table to be sorted
     * @param vs    : the intvar table to be sorted according a
     * @param left
     * @param right
     */
    public static void quicksort(int[] a, IntDomainVar[] vs, int left, int right) {
        if (right <= left) {
            return;
        }
        int i = partition(a, vs, left, right);
        quicksort(a, vs, left, i - 1);
        quicksort(a, vs, i + 1, right);
    }

    public static int partition(int[] a, IntDomainVar[] vs, int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (a[++i] < a[right]) {
// a[right] acts as sentinel
            }
            while (a[right] < a[--j]) {
                if (j == left) {
                    break;           // don't go out-of-bounds
                }
            }
            if (i >= j) {
                break;                  // check if pointers cross
            }
            exch(a, vs, i, j);                    // swap two elements into place
        }
        exch(a, vs, i, right);                      // swap with partition element
        return i;
    }

    public static void exch(int[] a, IntDomainVar[] vs, int i, int j) {
        int swap = a[i];
        IntDomainVar vswap = vs[i];
        a[i] = a[j];
        vs[i] = vs[j];
        a[j] = swap;
        vs[j] = vswap;
    }

    /**
     * Reverse a table of integer and variables (use for api on linear combination)
     *
     * @param tab array of integer to reverse
     * @param vs  array of variables to reverse
     */
    public static void reverse(int[] tab, IntDomainVar[] vs) {
        int[] revtab = new int[tab.length];
        IntDomainVar[] revvs = new IntDomainVar[vs.length];
        for (int i = 0; i < revtab.length; i++) {
            revtab[i] = tab[revtab.length - 1 - i];
            revvs[i] = vs[revtab.length - 1 - i];
        }
        for (int i = 0; i < revtab.length; i++) {
            tab[i] = revtab[i];
            vs[i] = revvs[i];
        }
    }


    //*****************************************************************//
    //*******************  TaskVariable  ********************************//
    //***************************************************************//


    public static IntegerVariable createDirVariable(TaskVariable t1, TaskVariable t2, String... boolOptions) {
        return makeBooleanVar("dir-" + t1.getName() + '-' + t2.getName(), boolOptions);
    }

    public static IntegerVariable[] getStartVariables(TaskVariable... tasks) {
        final IntegerVariable[] vars = new IntegerVariable[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            vars[i] = tasks[i].start();
        }
        return vars;
    }

    public static IntegerVariable[] getDurationVariables(TaskVariable... tasks) {
        final IntegerVariable[] vars = new IntegerVariable[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            vars[i] = tasks[i].duration();
        }
        return vars;
    }

    public static IntegerVariable[] getEndVariables(TaskVariable... tasks) {
        final IntegerVariable[] vars = new IntegerVariable[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            vars[i] = tasks[i].end();
        }
        return vars;
    }

    public static IntegerVariable[] getStartVariables(List<TaskVariable> tasks) {
        final IntegerVariable[] vars = new IntegerVariable[tasks.size()];
        ListIterator<TaskVariable> iter = tasks.listIterator();
        while (iter.hasNext()) {
            vars[iter.nextIndex()] = iter.next().start();
        }
        return vars;
    }

    public static IntegerVariable[] getDurationVariables(List<TaskVariable> tasks) {
        final IntegerVariable[] vars = new IntegerVariable[tasks.size()];
        ListIterator<TaskVariable> iter = tasks.listIterator();
        while (iter.hasNext()) {
            vars[iter.nextIndex()] = iter.next().duration();
        }
        return vars;
    }

    public static IntegerVariable[] getEndVariables(List<TaskVariable> tasks) {
        final IntegerVariable[] vars = new IntegerVariable[tasks.size()];
        ListIterator<TaskVariable> iter = tasks.listIterator();
        while (iter.hasNext()) {
            vars[iter.nextIndex()] = iter.next().end();
        }
        return vars;
    }


    //*****************************************************************//
    //*******************  TaskVar  ********************************//
    //***************************************************************//

    public static IntDomainVar createDirVar(Solver solver, TaskVar t1, TaskVar t2) {
    	return solver.createBooleanVar(StringUtils.randomName(t1, t2));
    }

    public static IntDomainVar[] getStartVars(TaskVar... tasks) {
        final IntDomainVar[] vars = new IntDomainVar[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            vars[i] = tasks[i].start();
        }
        return vars;
    }

    public static IntDomainVar[] getDurationVars(TaskVar... tasks) {
        final IntDomainVar[] vars = new IntDomainVar[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            vars[i] = tasks[i].duration();
        }
        return vars;
    }

    public static IntDomainVar[] getEndVars(TaskVar... tasks) {
        final IntDomainVar[] vars = new IntDomainVar[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            vars[i] = tasks[i].end();
        }
        return vars;
    }

    public static IntDomainVar[] getStartVars(List<TaskVar> tasks) {
        final IntDomainVar[] vars = new IntDomainVar[tasks.size()];
        ListIterator<TaskVar> iter = tasks.listIterator();
        while (iter.hasNext()) {
            vars[iter.nextIndex()] = iter.next().start();
        }
        return vars;
    }

    public static IntDomainVar[] getDurationVars(List<TaskVar> tasks) {
        final IntDomainVar[] vars = new IntDomainVar[tasks.size()];
        ListIterator<TaskVar> iter = tasks.listIterator();
        while (iter.hasNext()) {
            vars[iter.nextIndex()] = iter.next().duration();
        }
        return vars;
    }

    public static IntDomainVar[] getEndVars(List<TaskVar> tasks) {
        final IntDomainVar[] vars = new IntDomainVar[tasks.size()];
        ListIterator<TaskVar> iter = tasks.listIterator();
        while (iter.hasNext()) {
            vars[iter.nextIndex()] = iter.next().end();
        }
        return vars;
    }

    //*****************************************************************//
    //*******************  Branching Utils ***************************//
    //***************************************************************//
   
    public static IntDomainVar[] getIntVars(Solver solver) {
        final int n = solver.getNbIntVars();
        final IntDomainVar[] vars = new IntDomainVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.getIntVarQuick(i);
        }
        return vars;
    }

    public static SetVar[] getSetVars(Solver solver) {
        final int n = solver.getNbSetVars();
        final SetVar[] vars = new SetVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.getSetVarQuick(i);
        }
        return vars;
    }

    public static RealVar[] getRealVars(Solver solver) {
        final int n = solver.getNbRealVars();
        RealVar[] vars = new RealVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.getRealVar(i);
        }
        return vars;
    }

    public static TaskVar[] getTaskVars(Solver solver) {
        final int n = solver.getNbTaskVars();
        final TaskVar[] vars = new TaskVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = solver.getTaskVarQuick(i);
        }
        return vars;
    }

    /**
     * Return an array of Variables, from {@code decisions} if not empty, otherwise from {@code all}.
     * {@code clazz} is mandatory for array creation.
     * @param decisions list of decisions variables (can be empty)
     * @param all list of all variables of the solver
     * @param clazz class of objects in {@code decisions} and {@code all}
     * @param <E>
     * @return an array of E
     */
    @SuppressWarnings({"unchecked"})
    public static <E> E[] getDecisionList(List<E> decisions, List<E> all, Class<E> clazz) {
        if (decisions.isEmpty()) {
            E[] tmp = (E[])Array.newInstance(clazz, all.size());
            return all.toArray(tmp);
        } else {
            E[] tmp = (E[])Array.newInstance(clazz, decisions.size());
            return decisions.toArray(tmp);
        }
    }
    
    public static int[] getConstantValues(IntVar... vars) {
    	final int[] cards =new int[vars.length];
		for (int i = 0; i < vars.length; i++) {
			if(vars[i].isInstantiated()) cards[i]= vars[i].getVal();
			else throw new SolverException(vars[i].pretty()+" is not an integer constant");
		}
		return cards;
	}
    
    public static IntegerVariable[] getCardinalities(SetVariable... vars) {
    	final IntegerVariable[] cards =new IntegerVariable[vars.length];
		for (int i = 0; i < vars.length; i++) {
			cards[i]=vars[i].getCard();
		}
		return cards;
	}
    

    public static boolean checkConstant(IntegerVariable v, int value) {
        return v.isConstant() && v.canBeEqualTo(value);
    }

	public static IntDomainVar[] getBoolDecisionVars(Solver solver) {
		IntDomainVar[] ivs = solver.getIntDecisionVars();
		int i = 0;
		while( i < ivs.length && ivs[i].hasBooleanDomain()) {
			i++;
		}
		if(i < ivs.length) {
			LinkedList<IntDomainVar> bvs = new LinkedList<IntDomainVar>();
			for (IntDomainVar v : ivs) {
				if(v.hasBooleanDomain()) {
					bvs.add(v);
				}
			}
			ivs =  bvs.toArray(new IntDomainVar[bvs.size()]);
		}
		return ivs;
	}

}
