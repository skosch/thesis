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

package choco.kernel.model.constraints;

import java.util.HashMap;
import java.util.Map;

import choco.kernel.model.ModelException;
import choco.kernel.model.variables.VariableManager;
/**
 * Handle all object's managers referenced by property name.
 * The class ensures that there exists at most one instance of each manager.
 * @author Arnaud Malapert</br> 
 * @since 9 févr. 2010 version 2.1.1</br>
 * @version 2.1.1</br>
 */
		
public final class ManagerFactory {

	private static final Map<String, VariableManager<?>> VM_MAP = new HashMap<String, VariableManager<?>>();

	private static final Map<String, ExpressionManager> EM_MAP = new HashMap<String, ExpressionManager>();

	private static final Map<String, ConstraintManager<?>> CM_MAP = new HashMap<String, ConstraintManager<?>>();

	private static final String ERROR_MSG="Cant load manager by reflection: ";
	
	private static Object loadManager(String name) {
		//We get it by reflection !
		try {
			return Class.forName(name).newInstance();
		} catch (ClassNotFoundException e) {
			throw new ModelException(ERROR_MSG+name);
		} catch (InstantiationException e) {
			throw new ModelException(ERROR_MSG+name);
		} catch (IllegalAccessException e) {
			throw new ModelException(ERROR_MSG+name);
		}
	}

	public static VariableManager<?> loadVariableManager(String name) {
		VariableManager<?> vm = VM_MAP.get(name);
		if( vm == null) {
			vm = (VariableManager<?>) loadManager(name);
			VM_MAP.put(name, vm);
		}
		return vm;
	}

	public static ExpressionManager loadExpressionManager(String name) {
		ExpressionManager em = EM_MAP.get(name);
		if( em == null) {
			em = (ExpressionManager) loadManager(name);
			EM_MAP.put(name, em);
		}
		return em;
	}

	public static ConstraintManager<?> loadConstraintManager(String name) {
		ConstraintManager<?> cm = CM_MAP.get(name);
		if( cm == null) {
			cm = (ConstraintManager<?>) loadManager(name);
			CM_MAP.put(name, cm);
		}
		return cm;	
	}
	
	public static void clear() {
		VM_MAP.clear();
		EM_MAP.clear();
		CM_MAP.clear();
	}
}
