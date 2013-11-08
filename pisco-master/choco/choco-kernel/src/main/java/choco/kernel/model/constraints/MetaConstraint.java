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

import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.variables.Variable;

import java.util.Properties;

/* 
 * Created by IntelliJ IDEA.
 * User: GROCHART
 * Date: 5 aout 2008
 * Since : Choco 2.0.0
 *
 */
public final class MetaConstraint<E extends Constraint> extends AbstractConstraint {

	private final static Variable[] EMPTY_ARRAY = {};
	
    protected E[] constraints;

    public MetaConstraint(final ConstraintType type, final E... constraints) {
        super(type, EMPTY_ARRAY);
        this.constraints = constraints.clone();
    }

    public MetaConstraint(final Class metaManager, final E... constraints) {
        super(metaManager.getName(),EMPTY_ARRAY);
        this.constraints = constraints.clone();
    }

    public MetaConstraint(final String metaManager, final E... constraints) {
        super(metaManager,EMPTY_ARRAY);
        this.constraints = constraints.clone();
    }

    public E[] getConstraints() {
        return constraints;
    }

    public final E getConstraint(final int idx) {
        return constraints[idx];
    }

    /**
     * Extract variables of a constraint
     * and return an array of variables.
     * @return an array of every variables contained in the Constraint.
     */
    @Override
	public Variable[] doExtractVariables() {
        Variable[] listVars = new Variable[0];
        for (Constraint c : constraints) {
            listVars = ArrayUtils.append(listVars, c.extractVariables());
        }
        return ArrayUtils.getNonRedundantObjects(Variable.class, listVars);
    }

    @Override
    public void findManager(Properties propertiesFile) {
        super.findManager(propertiesFile);
        for (int i = 0; i < constraints.length; i++) {
            E constraint = constraints[i];
            constraint.findManager(propertiesFile);
        }
    }


	private void constraintsPrettyPrint(StringBuilder buffer)
	{
		buffer.append(StringUtils.pretty(getConstraints(), 0, getConstraints().length));
}

	@Override
	public String pretty()
	{
		final StringBuilder st = new StringBuilder(getName());
//		st.append(" [ ");
//		variablesPrettyPrint(st);
//		st.append(" ]: (");
		st.append(" ( ");

		constraintsPrettyPrint(st);
		st.append(" ) ");
		return st.toString();
	}


}
