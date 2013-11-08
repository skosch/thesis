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

package choco.cp.solver.constraints.global;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 22 févr. 2010
 * Since : Choco 2.1.1
 *
 * GCCAT:
 * NVAR is the number of variables of the collection VARIABLES that take their value in VALUES.
 * <br/>
 * <a href="http://www.emn.fr/x-info/sdemasse/gccat/Cexactly.html">gccat exactly</a>
 * <br/>
 * Propagator :
 * C. Bessière, E. Hebrard, B. Hnich, Z. K?z?ltan, T. Walsh,
 * Among, common and disjoint Constraints
 * CP-2005
 *
 * Could be improved by defining awakes on XX... but required storable data structures and clever management of LB and UB
 * during INST + INF + SUP + REM.
 * So not sure it will be that interesting.
 */
public final class Exactly extends AbstractLargeIntSConstraint {


    private final int value;
    private final int nb_vars;
    private final int N;
    private final List<IntDomainVar> BOTH;

    /**
     * Constructs a constraint with the specified priority.
     *
     * The last variables of {@code vars} is the counter.
     * @param vars (n-1) variables + N as counter
     * @param N counter
     * @param value counted values
     */
    @SuppressWarnings({"unchecked"})
    public Exactly(IntDomainVar[] vars, int N, int value) {
        super(ConstraintEvent.LINEAR, vars);
        nb_vars = vars.length;
        this.value = value;
        this.N = N;
        BOTH  = new ArrayList<IntDomainVar>(nb_vars);
    }

    /**
     * <i>Propagation:</i>
     * Propagating the constraint until local consistency is reached.
     *
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction exception
     */
    @Override
    public void propagate() throws ContradictionException {
        BOTH.clear();
        int lb = 0;
        int ub = nb_vars;
        for(int i = 0 ; i < nb_vars; i++){
            IntDomainVar var = vars[i];
            if(var.canBeInstantiatedTo(value)){
                if(var.isInstantiatedTo(value)){
                    lb++;
                }else{
                    BOTH.add(var);
                }
            }else{
                ub--;
            }
        }
        int min = Math.max(N, lb);
        int max = Math.min(N, ub);

        if(max < min) this.fail();

        if(lb == min && lb == max){
            for(IntDomainVar var : BOTH){
                var.removeVal(value, this, false);
            }
            setEntailed();
        }

        if(ub == min && ub == max){
            for(IntDomainVar var : BOTH){
                var.instantiate(value, this, false);
            }
            setEntailed();
        }
    }

    /**
     * Default implementation of the isSatisfied by
     * delegating to the isSatisfied(int[] tuple)
     *
     * @return
     */
    @Override
    public boolean isSatisfied() {
        int nbToValue  = 0;
        int nbNotToValue = 0;

        for(int i = 0; i < nb_vars; i++){
            if(vars[i].isInstantiatedTo(value)){
                nbToValue++;
            }else if(!vars[i].canBeInstantiatedTo(value)){
                nbNotToValue++;
            }
        }
        return nbToValue == N && (nbToValue + nbNotToValue) == nb_vars;
    }

    /**
     * TEMPORARY: if not overriden by the constraint, throws an error
     * to avoid bug using reified constraints in constraints
     * that have not been changed to fulfill this api yet !
     *
     * @param tuple
     * @return
     */
    @Override
    public boolean isSatisfied(int[] tuple) {
        int nb = 0;
        for(int tu : tuple){
            if(tu == value){
                nb++;
            }
        }
        return nb == N;
    }

    @Override
    public String pretty() {
        StringBuffer sb = new StringBuffer("EXACTLY(");
        sb.append(N).append(",[");
        for(int i = 0; i < nb_vars; i++){
            if(i>0)sb.append(",");
            sb.append(vars[i].pretty());
        }
        sb.append("],").append(value).append(")");
        return sb.toString();
    }
}