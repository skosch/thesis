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

package choco.kernel.solver.constraints.global.scheduling;

import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;

public final class ResourceParameters implements IResourceParameters {

	private static final long serialVersionUID = 3665641934423408763L;

	private final String name;
	
	private final int nbReg;
	
	private final int nbOpt;
	
	private final int nbTot;
	
	private final boolean isHorizonDefined;
		
	public ResourceParameters(String name, TaskVariable[] tasks,IntegerVariable[] usages, IntegerVariable uppBound) {
		super();
		if(tasks == null || tasks.length == 0) {
			throw new ModelException("Empty resource ?");
		}
		nbTot = tasks.length;
		if(usages == null) {
			nbOpt= 0;
		}else {
			nbOpt = usages.length;
			if(nbOpt>nbTot) {
				throw new ModelException("Invalid resource dimensions.");
			}
			for (int i = 0; i < usages.length; i++) {
				if( ! usages[i].isBoolean()) {
					throw new ModelException("Resource usage variable: "+usages[i].pretty()+" is not boolean.");
				}
			}
		}
		nbReg = nbTot - nbOpt;
		this.name = name == null ? StringUtils.randomName()+"-RSC" : name;
		this.isHorizonDefined = (uppBound != null);
	}

	public boolean isRegular() {
		return nbOpt == 0;
	}

	public boolean isAlternative() {
		return nbOpt > 0;
	}
	@Override
	public int getNbOptionalTasks() {
		return nbOpt;
	}

	@Override
	public int getNbRegularTasks() {
		return nbReg;
	}

	@Override
	public int getNbTasks() {
		return nbTot;
	}

	@Override
	public String getRscName() {
		return name;
	}

	public boolean isHorizonDefined() {
		return isHorizonDefined;
	}

	@Override
	public String toString() {
		return name +"("+nbReg+", "+nbOpt+")";
	}

	public final int getUsagesOffset() {
		return nbTot;
	}
	
	public final int getHeightsOffset() {
		return nbTot + nbOpt;
	}
	
	public final int getConsOffset() {
		return nbTot + nbOpt + nbTot;
	}
	
	public final int getCapaOffset() {
		return nbTot + nbOpt + nbTot + 1;
	}
		
}
