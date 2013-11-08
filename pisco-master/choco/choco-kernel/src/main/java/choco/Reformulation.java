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

package choco;

import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.model.variables.set.SetVariable;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 24 f�vr. 2010<br/>
 * Since : Choco 2.1.1<br/>
 *
 * A class for reformulated constraints.
 */
public class Reformulation {

    /**
     * AMONG constraint reformulated like: <br/>
     * <ul>
     * <li>introducing BOOL variable for each VARIABLE,</li>
     * <li>adding following constraints:</li>
     * <ul>
     * <li>for each VARIABLE : REIFIED(BOOL_i, MEMBER(VARIABLE_i, S)),</li>
     * <li>EQ(SUM(BOOL), N),</li>
     * </ul>
     * </ul>
     * @param variables scope variable
     * @param s set variable, containing values to count
     * @param nvar integer variable counter
     * @return AMONG constraint reformulated
     */
    public static Constraint[] among(IntegerVariable[] variables, SetVariable s, IntegerVariable nvar) {
        IntegerVariable[] bools = new IntegerVariable[variables.length];
        for(int i = 0; i< variables.length; i++){
            bools[i] = Choco.makeBooleanVar(StringUtils.randomName());
        }
        Constraint[] cs = new Constraint[variables.length + 1];
        for (int i = 0; i < variables.length; i++) {
            cs[i] = Choco.reifiedConstraint(bools[i], Choco.member(variables[i], s), Choco.notMember(variables[i], s));
        }
        cs[variables.length] = Choco.eq(nvar, Choco.sum(bools));
        return cs;
    }

    /**
     * AMONG constraint reformulated like: <br/>
     * <ul>
     * <li>introducing BOOL variable for each VARIABLE,</li>
     * <li>adding following constraints:</li>
     * <ul>
     * <li>for each VARIABLE : REIFIED(BOOL_i, AMONG(VARIABLE_i, VALUES)),</li>
     * <li>EQ(SUM(BOOL), N),</li>
     * </ul>
     * </ul>
     * @param nvar counter variable
     * @param variables counted variables
     * @param values array of values
     * @return AMONG constraint reformulated
     */
    public static Constraint[] among(IntegerVariable nvar, IntegerVariable[] variables, int[]values){
        IntegerVariable[] bools = new IntegerVariable[variables.length];
        for(int i = 0; i< variables.length; i++){
            bools[i] = Choco.makeBooleanVar(StringUtils.randomName());
        }
        Constraint[] cs = new Constraint[variables.length + 1];
        for(int j = 0; j < bools.length; j++){
            cs[j] = Choco.reifiedConstraint(bools[j], Choco.member(variables[j], values));
        }
        cs[variables.length] = Choco.eq(Choco.sum(bools), nvar);
        return cs;
    }
    
    /**
    * AMONG constraint reformulated like: <br/>
    * <ul>
    * <li>introducing BOOL variable for each VARIABLE,</li>
    * <li>adding following constraints:</li>
    * <ul>
    * <li>for each VARIABLE : REIFIED(BOOL_i, AMONG(VARIABLE_i, VALUES)),</li>
    * <li>EQ(SUM(BOOL), N),</li>
    * </ul>
    * </ul>
    */
    public static Constraint[] disjunctive(TaskVariable[] clique, String... boolvarOptions) {
		final int n = clique.length;
		Constraint[] cstr = new Constraint[ (n * (n-1) )/2];
		int idx = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i+1; j < n; j++) {
				cstr[idx++] = Choco.precedenceDisjoint(clique[i], clique[j], VariableUtils.createDirVariable(clique[i], clique[j], boolvarOptions));
			}
		}
		return cstr;
	}

}
