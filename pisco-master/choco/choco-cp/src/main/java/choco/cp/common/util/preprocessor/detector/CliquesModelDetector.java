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

package choco.cp.common.util.preprocessor.detector;

import choco.Choco;
import static choco.Choco.allDifferent;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.preprocessor.SymetryBreakingModelDetector;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.Iterator;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 2 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 * <p/>
 * A class dedicated to detect clique of differences
 * and state the corresponding global constraints
 */
public class CliquesModelDetector extends AbstractGraphBasedDetector {

    private final SymetryBreakingModelDetector symbreakD;

    public CliquesModelDetector(final CPModel model, final boolean breakSymetries) {
        super(model);
        if(breakSymetries){
            symbreakD = new SymetryBreakingModelDetector(model);
        }else{
            symbreakD = new SymetryBreakingModelDetector.EmptySymetryBreakingModelDetector(model);
        }
    }

    /**
     * Apply the detection defined within the detector.
     */
    @Override
    public void apply() {
        if (addAllNeqEdges()) {
            final CliqueIterator it = cliqueIterator();
            while (it.hasNext()) {
                final IntegerVariable[] cl = it.next();
                if (cl.length > 2) {
                    add(allDifferent(Options.C_ALLDIFFERENT_BC, cl));
                    symbreakD.setMaxClique(cl);
                    it.remove();
                } else {
                    add(Choco.neq(cl[0], cl[1]));
                }
            }
            symbreakD.applyThenCommit();
        }
    }

    /**
     * Build the constraint graph of differences
     *
     * @return boolean
     */
    public boolean addAllNeqEdges() {
        Iterator<Constraint> itneq = model.getConstraintByType(ConstraintType.NEQ);
        while (itneq.hasNext()) {
            Constraint neq = itneq.next();
            Variable[] vars = neq.getVariables();
            if (isRealBinaryNeq(vars)) {
                //the NEQ can take a constant...
                addEdge(neq.getVariables()[0], neq.getVariables()[1], neq);
            }
        }
        return diffs.nbEdges > 0;
    }

    public static boolean isRealBinaryNeq(Variable[] vars) {
        if (vars.length != 2) return false;
        for (Variable var : vars) {
            if (var.getVariableType() != VariableType.INTEGER)
                return false;
        }
        return true;
    }

}
