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

package choco.kernel.model.variables.scheduling;

import java.util.Properties;

import choco.kernel.common.IDotty;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.common.util.tools.TaskUtils;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ManagerFactory;
import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.VariableManager;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerVariable;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 23 janv. 2009
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 */
public class TaskVariable extends MultipleVariables implements ITaskVariable<IntegerVariable>, IDotty {

	protected String variableManager;

	public TaskVariable(String name, IntegerVariable start, IntegerVariable end, IntegerVariable duration) {
		super(true, true, start,end,duration);
		this.setName(name);
	}


	@Override
	public VariableManager<?> getVariableManager() {
		return ManagerFactory.loadVariableManager(variableManager);
	}

	/**
	 * Get the duration of the task
	 * @return
	 */
	public final IntegerVariable duration() {
		return (IntegerVariable) getVariable(2);
	}


	/**
	 * Get the end time of the task
	 * @return
	 */
	public final IntegerVariable end() {
		return (IntegerVariable) getVariable(1);
	}


	/**
	 * Get the start time of the task
	 * @return
	 */
	public final IntegerVariable start() {
		return (IntegerVariable)getVariable(0);
	}


	@Override
	public boolean isEquivalentTo(MultipleVariables mv) {
		if (mv instanceof TaskVariable) {
			TaskVariable t = (TaskVariable) mv;
			boolean r = (t.start().getIndex() == this.start().getIndex() &&
					t.duration().getIndex() == this.duration().getIndex());
			/*r &= (t.end() == null ||
                    this.end() == null ||
                    t.end().getIndex() == this.end().getIndex());*/
			return r;

		}
		return false;
	}

	@Override
	public String pretty() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append(name).append(" {").append(start().pretty());
		buffer.append(" + ").append(duration().pretty());
		buffer.append(" = ").append(end().pretty());
		buffer.append('}');
		return buffer.toString();
	}


	@Override
	public String toDotty() {
		final StringBuilder b = new StringBuilder(); 	
		b.append(getHook()).append("[ shape=record,");
		b.append("label=\"{");
		b.append("{").append(start().getLowB());
		b.append("|").append(StringUtils.format(duration()));
		b.append("|").append(end().getUppB());
		b.append("}|").append(getName());
		b.append("}\"];");
		return b.toString();
	}

	@Override
	public final void findManager(Properties propertiesFile) {
		if (variableManager == null) {
			variableManager = propertiesFile.getProperty(VariableType.TASK.property);
		}
		if (variableManager == null) {
			throw new ModelException("Can not find " + type.property + " in application.properties");
		}
		super.findManager(propertiesFile);
	}

}
