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

package parser.absconparseur.components;

import choco.kernel.model.variables.integer.IntegerVariable;


public class PVariable {
	private final int index;

	private final String name;

	private final PDomain domain;

	private PVariable representative;

	protected IntegerVariable chocovar;

	public String getName() {
		return name;
	}

	public PDomain getDomain() {
		return domain;
	}

	public PVariable(String name, PDomain domain) {
		this.name = name;
		this.domain = domain;
		this.index = name.hashCode(); //Integer.parseInt(name.substring(1,name.length()));
	}

	public int getIdx() {
		return index;
	}

	public String toString() {
		return "  variable " + name + " with associated domain " + domain.getName();
	}

	public PVariable getRepresentative() {
		if (representative == null)
			return this;
		else return representative;
	}

	public void setRepresentative(PVariable representative) {
		this.representative = representative;
	}

	public boolean isFake() {
		return representative != null;
	}

	public IntegerVariable getChocovar() {
		return chocovar;
	}

	public void setChocovar(IntegerVariable chocovar) {
		this.chocovar = chocovar;
	}

	public int hashCode() {
		return index;
	}
}
