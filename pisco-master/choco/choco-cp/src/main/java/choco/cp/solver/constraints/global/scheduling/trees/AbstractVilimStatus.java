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

package choco.cp.solver.constraints.global.scheduling.trees;

import choco.cp.solver.constraints.global.scheduling.trees.AbstractVilimTree.NodeType;
import choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode;
import choco.kernel.common.opres.graph.INodeLabel;
import choco.kernel.solver.variables.scheduling.ITask;

/**
 * @author Arnaud Malapert</br> 
 * @since 10 févr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 * @param <E>
 */
public abstract class AbstractVilimStatus<E> implements INodeLabel {

	protected NodeType type;

	protected ITask task;

	protected final E status;

	public AbstractVilimStatus(NodeType type, E status) {
		super();
		this.status = status;
		this.type = type;
	}

	public final NodeType getType() {
		return type;
	}

	public final void setType(NodeType type) {
		this.type = type;
	}

	public final ITask getTask() {
		return task;
	}

	public void setTask(ITask task) {
		this.task = task;
	}

	public final E getStatus() {
		return status;
	}
	private String getDotStyle() {
		switch ( getType()) {
		case LAMBDA: 
		case NIL: return "filled";
		default: return "solid";
		}
	}
	
	private String getFillColor() {
		switch ( getType()) {
		case LAMBDA: return "gray";
		case NIL: return "black";
		default: return "white";
		}
	}

	private String getFontColor() {
		switch ( getType() ) {
		case NIL: return "white";
		default: return "black";
		}
	}
	
	private String getBorderColor() {
		switch ( getType() ) {
		case INTERNAL: return "green";
		default: return "black";
		}
	}
	
	protected int getResetIntValue(TreeMode mode) {
		return mode.value() ? Integer.MIN_VALUE : Integer.MAX_VALUE;
	}
	
	protected long getResetLongValue(TreeMode mode) {
		return mode.value() ? Long.MIN_VALUE : Long.MAX_VALUE;
	}

	protected void writeRow(StringBuilder buffer, String label1, String str1,String label2, String str2) {
		buffer.append('{').append(label1).append('=').append(str1);
		buffer.append('|');
		buffer.append(label2).append('=').append(str2).append('}');
	}
	
	protected String format(int value) {
		return value == Integer.MIN_VALUE ? "-inf" : value == Integer.MAX_VALUE ? "+inf" : String.valueOf(value);
	}
	
	protected String format(long value) {
		return value == Long.MIN_VALUE ? "-inf" : value == Long.MAX_VALUE ? "+inf" : String.valueOf(value);
	}
	
	@Override
	public String toDotty() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("shape=Mrecord,");
		buffer.append("style=").append(getDotStyle());
		buffer.append(", fillcolor=").append(getFillColor());
		buffer.append(", fontcolor=").append(getFontColor());
		buffer.append(", color=").append(getBorderColor());
		buffer.append(", label=\"{");
		if( getTask() != null) {
			buffer.append(getTask().getName());
			buffer.append('|');
		}
		writeDotStatus(buffer);
		buffer.append("}\"");
		return new String(buffer);
	}

	protected abstract void writeDotStatus(StringBuilder buffer);

	public abstract void reset();

	@Override
	public int getNbParameters() {
		return 3;
	}

	@Override
	public Object getParameter(int idx) {
		switch (idx) {
		case 0: return type;
		case 1: return status;
		case 2: return task;
		default:return null;
		}
	}

	@Override
	public void setParameter(int idx, Object parameter) {
		switch (idx) {
		case 0: this.setType((NodeType) parameter);break;
		case 1: throw new UnsupportedOperationException("cant change status");
		case 2: this.setTask((ITask) parameter);break;
		default:
			throw new ArrayIndexOutOfBoundsException("index out of range");
		}

	}

}
