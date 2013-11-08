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
import choco.Options;
import choco.cp.common.util.preprocessor.AbstractDetector;
import choco.cp.model.CPModel;
import choco.cp.model.managers.variables.IntegerVariableManager;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/01/11
 */
public class AnalysisModelDetector extends AbstractDetector {


    static int NB_STATS = 0;
    static final int EXT_CSTR = NB_STATS++;
    static final int BIG_DOM = NB_STATS++;
    static final int DOM_TYPE = NB_STATS++;
    static final int FREE_VAR = NB_STATS++;


    static long[] STATS = new long[NB_STATS];
    static String[] DEF_STATS = new String[NB_STATS];

    static final String ROW_SEP = "-";

    static {
        DEF_STATS[EXT_CSTR] = "Number of variables involved in more constraints than those of the current model: ";
        DEF_STATS[BIG_DOM] = "Number of integer variables with very large domain: ";
        DEF_STATS[DOM_TYPE] = "Number of integer variables with inappropriate domain type: ";
        DEF_STATS[FREE_VAR] = "Number of free variables: ";
    }

    public AnalysisModelDetector(CPModel model) {
        super(model);
        Arrays.fill(STATS, 0);
    }

    @Override
    public void apply() {
        if (LOGGER.getLevel().intValue() > Level.INFO.intValue()) {
            LOGGER.severe("AnalysisModelDetector: LOGGER verbosity is not sufficient to print all messages (minimum required : INFO)");
        }
        analyseIntegerVariables(model);
        analyseRealVariables(model);
        analyseSetVariables(model);
        analyseConstants(model);
        analyseMultipleVariables(model);

        print();
    }

    @Override
    public void commit() {
    }

    public void analyseIntegerVariables(final CPModel model) {
        IntegerVariable i;
        final Iterator<IntegerVariable> it = model.getIntVarIterator();
        while (it.hasNext()) {
            i = it.next();
            analyseNbConstraint(i);
            analyseDomainSize(i);
            analyseDomainType(i);
        }
    }

    public void analyseRealVariables(final CPModel model) {
        RealVariable r;
        final Iterator<RealVariable> it = model.getRealVarIterator();
        while (it.hasNext()) {
            r = it.next();
            analyseNbConstraint(r);
        }
    }

    public void analyseSetVariables(final CPModel model) {
        SetVariable s;
        final Iterator<SetVariable> it = model.getSetVarIterator();
        while (it.hasNext()) {
            s = it.next();
            analyseNbConstraint(s);
        }
    }

    public void analyseConstants(final CPModel model) {
        Variable v;
        final Iterator<Variable> it = model.getConstVarIterator();
        while (it.hasNext()) {
            v = it.next();
            analyseNbConstraint(v);
        }
    }

    public void analyseMultipleVariables(final CPModel model) {
        MultipleVariables mv;
        final Iterator<MultipleVariables> it = model.getMultipleVarIterator();
        while (it.hasNext()) {
            mv = it.next();
            analyseNbConstraint(mv);
        }
    }

    //****************************************************************************************************************//
    private void print() {
        LOGGER.info("\n");
        LOGGER.info(StringUtils.pad("", 5, ROW_SEP));
        LOGGER.info("Relevant statistics on Model @" + model.hashCode() + ":\n");
        for (int i = 0; i < NB_STATS; i++) {
            if (STATS[i] > 0) {
                LOGGER.info("> " + DEF_STATS[i] + STATS[i]);
            }
        }
        LOGGER.info(StringUtils.pad("", 5, ROW_SEP));
        LOGGER.info("\n");
    }

    private void analyseNbConstraint(Variable v) {
        if (v.getConstraints().length > model.getNbConstraints()) {
            STATS[EXT_CSTR]++;
        }
    }

    private void analyseDomainSize(IntegerVariable v) {
        if (v.getLowB() < Choco.MIN_LOWER_BOUND
                || v.getUppB() > Choco.MAX_UPPER_BOUND) {
            STATS[BIG_DOM]++;
        }
    }

    private void analyseDomainType(IntegerVariable v) {
        int actualDom = -1;
        if (v.containsOption(Options.V_BOUND)) {
            actualDom = IntDomainVar.BOUNDS;
        } else if (v.containsOption(Options.V_ENUM)) {
            actualDom = IntDomainVar.BITSET;
        } else if (v.containsOption(Options.V_LINK)) {
            actualDom = IntDomainVar.LINKEDLIST;
        } else if (v.containsOption(Options.V_BLIST)) {
            actualDom = IntDomainVar.BIPARTITELIST;
        } else if (v.containsOption(Options.V_BTREE)) {
            actualDom = IntDomainVar.BINARYTREE;
        }
        if (actualDom > -1) {
            int bestDom = IntegerVariableManager.getIntelligentDomain(model, v);
            if (actualDom != bestDom) {
                STATS[DOM_TYPE]++;
            }
        }
    }

    private void analyseFreeVar(Variable v) {
        if (v.getNbConstraint(model) == 0) {
            STATS[FREE_VAR]++;
        }
    }
}
