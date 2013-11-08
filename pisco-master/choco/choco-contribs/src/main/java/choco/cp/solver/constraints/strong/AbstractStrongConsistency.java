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

package choco.cp.solver.constraints.strong;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractStrongConsistency<MyVariable extends SCVariable<? extends SCConstraint>>
		extends AbstractLargeIntSConstraint {

	protected final SCConstraint[] constraints;
	private final List<MyVariable> variables;
	protected final Map<IntDomainVar, MyVariable> variablesMap;

	public AbstractStrongConsistency(IntDomainVar[] vars,
			ISpecializedConstraint[] constraints,
			Class<? extends MyVariable> myVariable,
			Class<? extends SCConstraint> myConstraint) {
		super(vars);
		// Création des variables encapsulantes + map avec variables
		// originales
		this.variables = new ArrayList<MyVariable>(getNbVars());

		variablesMap = new HashMap<IntDomainVar, MyVariable>(getNbVars());

		final Constructor<? extends MyVariable> variableConstructor;
		try {
			variableConstructor = myVariable.getConstructor(IntDomainVar.class,
					Integer.class);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
		for (int i = 0; i < getNbVars(); i++) {
			final MyVariable var;
			try {
				var = variableConstructor.newInstance(getVar(i), i);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
			this.variables.add(var);
			variablesMap.put(getVar(i), var);
		}

		// Initialisation du réseau interne
		this.constraints = new SCConstraint[constraints.length];

		final Constructor<? extends SCConstraint> constraintConstructor;
		try {
			constraintConstructor = myConstraint.getConstructor(
					ISpecializedConstraint.class, Map.class);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}

		for (int i = constraints.length; --i >= 0;) {

			try {
				this.constraints[i] = constraintConstructor.newInstance(
						constraints[i], variablesMap);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
			// for (int j = 2; --j >= 0;) {
			// this.constraints[i].getVariable(j).addConstraint(
			// this.constraints[i]);
			// }

		}

	}

	public MyVariable getVariable(int id) {
		return variables.get(id);
	}
	
	public boolean isSatisfied(int[] tuple) {
		final int[] subTuple = new int[2];
		for (SCConstraint c: constraints) {
			for (int i = c.getArity(); --i>=0;) {
				subTuple[i] = tuple[c.getVariable(i).getId()];
			}
			if (!c.check(subTuple)) {
				return false;
			}
		}
		return true;
	}
	
	public void awake() throws ContradictionException {
		for (MyVariable v: variables) {
			v.setCId(this);
		}
	}
}