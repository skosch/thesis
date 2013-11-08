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

package choco.cp.model.managers;

import choco.kernel.common.util.tools.IteratorUtils;
import choco.kernel.model.variables.ComponentVariable;
import choco.kernel.model.variables.Variable;

import java.util.Iterator;

/**
 * @author Arnaud Malapert
 *
 */
public final class VariableIterator implements Iterator<Variable> {

	public final Variable[] variables;

	private int n=0;

	Iterator<Variable> it;

	public VariableIterator(Variable[] variables) {
		super();
		this.variables = variables;
		it = (variables != null && variables.length > 0 ? getIterator(variables[n]) : null);
	}

	protected Iterator<Variable> getIterator(Variable v) {
		if (v instanceof ComponentVariable) {
			return v.getVariableIterator();
		} else {
			return IteratorUtils.iterator(v);
		}
	}




	public boolean hasNext() {
		if (it == null) {
			return false;
		}
		while (n < variables.length && !it.hasNext()) {
			n++;
			if (n < variables.length) {
				it = getIterator(variables[n]);
			}
		}
		return n < variables.length && it.hasNext();
	}

	public Variable next() {
		return it.next();
	}

	public void remove() {
		it.remove();
	}




}
