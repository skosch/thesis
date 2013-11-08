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

package choco.kernel.solver.constraints.global;

import choco.kernel.common.util.tools.IteratorUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MetaSConstraint implements SConstraint, IResource<TaskVar> {

    private final static TaskVar[] EMPTY_TASK_ARRAY = new TaskVar[0];

	private final static IntDomainVar[] EMPTY_INTVAR_ARRAY = new IntDomainVar[0];

	
	public final IntDomainVar[] vars;

	public final TaskVar[] tasks;

	public final SConstraint[] constraints;

    public final SConstraintType type;

	protected String name;


	public MetaSConstraint(String name, SConstraint[] constraints, TaskVar[] tasks, IntDomainVar[] vars) {
		this(constraints, tasks, vars);
		this.name = name;
	}
	
	public MetaSConstraint(SConstraint[] constraints, TaskVar[] tasks, IntDomainVar[] vars) {
		if(constraints == null || constraints.length == 0) {
			throw new SolverException("Empty MetaConstraint !?");
		}
		this.constraints = constraints;
		this.vars = vars == null ? EMPTY_INTVAR_ARRAY : vars;
		this.tasks = tasks == null ? EMPTY_TASK_ARRAY : tasks ;
        this.type = computeType(constraints);
	}

    /**
     * Compute the global type of the constraint, based on the sub-constraints.
     * @param constraints list of implied constraints
     * @return a SConstraintType 
     */
    private static SConstraintType computeType(final SConstraint[] constraints) {
        SConstraintType type = constraints[0].getConstraintType();
        if(SConstraintType.EXPRESSION.equals(type)){
            return SConstraintType.EXPRESSION;
        }else{
            for(int i = 1; i < constraints.length; i++ ){
                if(SConstraintType.EXPRESSION.equals(type)){
                    return SConstraintType.EXPRESSION;
                }else if(!constraints[i].getConstraintType().equals(type)){
                    type = SConstraintType.MIXED;
                }
            }
        }
        return type;
    }

    public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * does not really add a listener as it is useless for propagation.
	 * it only records a list of constraints for a task variables.
	 * @param dynamicAddition
	 */
	public void addListener(final boolean dynamicAddition) {
	}


	@Override
	public final int getConstraintIdx(int idx) {
		return -1;
	}

	@Override
	public final int getNbVars() {
		return vars.length + tasks.length;
	}

	@Override
	public final Var getVar(int i) {
		return i < tasks.length ? tasks[i] : vars[i];
	}

	@Override
	public final Var getVarQuick(int i) {
		return getVar(i);
	}

	public final TaskVar getTask(int i) {
		return tasks[i];
	}

	public final int getNbSubConstraints() {
		return constraints.length;
	}

	public final SConstraint getSubConstraints(int i) {
		return constraints[i];
	}

	@Override
	public boolean isSatisfied() {
		for (SConstraint c : constraints) {
			if( ! c.isSatisfied() ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public AbstractSConstraint opposite(Solver solver) {
		throw new UnsupportedOperationException("opposite is not supported");
	}

	@Override
	public final void setConstraintIndex(int i, int idx) {
		throw new UnsupportedOperationException("index is useless");
	}


	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public final void setVar(int i, Var v) {
		throw new UnsupportedOperationException("cant change the scope of a meta constraint.");
	}

	@Override
	public String pretty() {
		return "intvars"+StringUtils.pretty(vars)+"\ntasks"+StringUtils.pretty(tasks)+"\nsubconstraints"+StringUtils.pretty(constraints);
	}

	@Override
	public List<TaskVar> asTaskList() {
		return Arrays.asList(tasks);
	}

	@Override
	public int getNbTasks() {
		return tasks.length;
	}

	@Override
	public String getRscName() {
		return name;
	}

	@Override
	public IRTask getRTask(int idx) {
		//FIXME
		return null;
	}


	@Override
	public List<IRTask> asRTaskList() {
		return Collections.emptyList();
	}

	@Override
	public Iterator<IRTask> getRTaskIterator() {
		return asRTaskList().iterator();
	}
	
	@Override
	public Iterator<TaskVar> getTaskIterator() {
		return IteratorUtils.iterator(tasks);
	}

    /**
     * Return the type of constraint, ie the type of variable involved in the constraint
     *
     * @return
     */
    @Override
    public SConstraintType getConstraintType() {
        return type;
    }

	@Override
	public int getNbOptionalTasks() {
		return 0;
	}

	@Override
	public int getNbRegularTasks() {
		return getNbTasks();
	}
    
	@Override
	public Extension getExtension(int extensionNumber) {
		return null;
	}

    /**
     * Adds a new extension.
     *
     * @param extensionNumber should use the number returned by getAbstractSConstraintExtensionNumber
     */
    @Override
    public void addExtension(final int extensionNumber) {}

	@Override
	public int getFineDegree(int idx) {
		return constraints.length;
	}


}
