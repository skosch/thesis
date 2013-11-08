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

package choco.cp.solver.constraints.global.tree;

import choco.cp.solver.constraints.global.tree.deduction.DeductionsAdvisor;
import choco.cp.solver.constraints.global.tree.filtering.FilteringAdvisor;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.StructuresAdvisor;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.logging.Level;


/**
 * <p> A global <b> tree partitioning constraint </b> that deals with several restrictions: </p>
 * <blockquote> - The number of <b> trees </b>  allowed to partition the graph (assuming an isolated node forms
 * a tree), </blockquote>
 * <blockquote> - The number of <b> proper trees </b> allowed to partition the graph (assuming a proper tree
 * contains at least 2 nodes), </blockquote>
 * <blockquote> - <b> Partial order </b>  between the nodes of the digraph, </blockquote>
 * <blockquote> - <b> Incomparability relations </b>  between the nodes of the digraph, </blockquote>
 * <blockquote> - <b> Degree restrictions </b>  related to the number of incoming arc of each node, </blockquote>
 * <blockquote> - <b> Time windows associated </b>  with each node that represent the starting time from each
 * node. </blockquote>
 * <p> All these attributes are embedded in a specific data structure, the description of the <b> input
 * data structure </b> is available in the <code> structure.inputStructure </code> package.</p>
 * 
 */
public final class TreeSConstraint extends AbstractLargeIntSConstraint {

    /**
     * boolean for debug and show a trace of the execution
     */
    protected final static boolean AFFICHE = false;

    /**
     * Choco problem embedding the tree constraint
     */
    protected Solver solver;

    /**
     * attributes
     */
    protected TreeParameters tree;

    /**
     * total number of nodes involved in the graph
     */
    protected int nbNodes;

    /**
     * internal structure manager
     */
    protected StructuresAdvisor structure;

    /**
     * deduction manager
     */
    protected DeductionsAdvisor deduction;

    /**
     * filtering manager
     */
    protected FilteringAdvisor filtering;


    /**
     * constructor: allocates the data util for a Choco constraint
     *
     * @param allVars   the set of variable related to the description of the tree constraint
     * @param tree  the input data structure available in the <code> structure.inputStructure </code> package
     */
    public TreeSConstraint(IntDomainVar[] allVars, TreeParameters tree){
        // allVars = ntree, nproper, ojectif, s[nbNodes], o[nbNodes], d[nbNodes], p[nbNodes] ==> |allVars| = (4*n)+3
        super(ConstraintEvent.VERY_SLOW, allVars);
        this.tree = tree;
    }

    public boolean isSatisfied(int[] tuple) {
        LOGGER.severe("TreeSConstraint#isSatisfied(int[] tuple) is not implemented and always returns true!");
        return true;
    }
    public boolean isSatisfied() {
        LOGGER.severe("TreeSConstraint#isSatisfied() is not implemented and always returns true!");
        return true;
    }

    /**
     * Initial awake of the tree constraint. <b> Internal data structures </b> are created from the input structure
     * <code> TreeParameters </code>. Each manager of filtering, deduction and update are initiliazed.
     *
     * @throws ContradictionException
     */
    public void awake() throws ContradictionException {
        this.solver = tree.getSolver();
        this.nbNodes = tree.getNbNodes();
        this.structure = new StructuresAdvisor(this.solver, tree);
        this.deduction = new DeductionsAdvisor(this.solver, tree, this.structure, AFFICHE);
        this.filtering = new FilteringAdvisor(this.solver, this, this.tree, this.structure, AFFICHE);
        if (AFFICHE) {
        	LOGGER.log(Level.INFO, "*********************************");
            structure.getInputGraph().showGlobal();
            LOGGER.log(Level.INFO, "-------------------------");
            structure.getPrecs().showPrecGraph();
            LOGGER.log(Level.INFO, "-------------------------");
            structure.getDoms().showDoms(0);
        }
        propagate();
    }

    /**
     * The main propagation method. A fix point for the filetring and the deductions is reached by a basic saturation
     * loop that iteratively call the update, deduction and filtering managers until each one does not leads to an
     * update of the internal structure, or a contradiction over the domain variables is detected.
     *
     * @throws ContradictionException
     */
    public void propagate() throws ContradictionException {
        if (AFFICHE) {
        	ChocoLogging.flushLogs();
        	LOGGER.log(Level.INFO, "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            LOGGER.log(Level.INFO, "propagate {0}", solver.getEnvironment().getWorldIndex());
        }
        boolean update;
        do {
            // update the internal data structure
            structure.applyStructure();
            // try to apply new deductions that may update the internal structures
            update = deduction.applyDeduction();
            // feasibility
            if (!deduction.isCompatible()) {
                if (AFFICHE) LOGGER.log(Level.INFO, "\t update fail()");
                update = false;
                this.fail();
            } else {
                // apply the filtering manager and consequently the different filtering rules
                update = filtering.applyFiltering() || update;
            }
        } while (update);
        if (AFFICHE) {
            LOGGER.log(Level.INFO, "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            ChocoLogging.flushLogs();
        }
    }

    /**
     * Event based propagation related to the instanciation of a variable. Only variables depicting a node of the graph
     * to partition are treated here.
     *
     * @param idx   the index of the variable instanciated
     * @throws ContradictionException
     */
    public void awakeOnInst(int idx) throws ContradictionException {
        // events over successor variables
        if ((2 < idx) && (idx < nbNodes + 3)) {
            if (AFFICHE) LOGGER.log(Level.INFO, "awakeOnInst");
            structure.getInputGraph().updateOnInst(idx - 3);
        }
        this.constAwake(false);
    }

    /**
     * Event based propagation related to the update of the lower bound of a variable. Only variables depicting a node of the graph
     * to partition are treated here.
     *
     * @param idx   the variable index for which the lower bound is updated
     * @throws ContradictionException
     */
    public void awakeOnInf(int idx) throws ContradictionException {
        // events over successor variables
        if ((2 < idx) && (idx < nbNodes + 3)) {
            if (AFFICHE) LOGGER.log(Level.INFO, "awakeOnInf");
            structure.getInputGraph().updateOnInf(idx - 3);
        }
        this.constAwake(false);
    }

    /**
     * Event based propagation related to the update of the upper bound of a variable. Only variables depicting a node of the graph
     * to partition are treated here.
     *
     * @param idx   the variable index for which the upper bound is updated
     * @throws ContradictionException
     */
    public void awakeOnSup(int idx) throws ContradictionException {
        // events over successor variables
        if ((2 < idx) && (idx < nbNodes + 3)) {
            if (AFFICHE) LOGGER.log(Level.INFO, "awakeOnSup");
            structure.getInputGraph().updateOnSup(idx - 3);
        }
        this.constAwake(false);
    }

    /**
     * Event based propagation related to the update of the bounds of a variable. Only variables depicting a node of the graph
     * to partition are treated here.
     *
     * @param idx   the variable index for which the bounds are updated
     * @throws ContradictionException
     */
    public void awakeOnBounds(int idx) throws ContradictionException {
        // events over successor variables
        if ((2 < idx) && (idx < nbNodes + 3)) {
            if (AFFICHE) LOGGER.log(Level.INFO, "awakeOnBounds");
            structure.getInputGraph().updateOnBounds(idx - 3);
        }
        this.constAwake(false);
    }

    /**
     * Event based propagation related to the removal of a value in the domain of a variable. Only variables depicting a node of the graph
     * to partition are treated here.
     *
     * @param idx   the variable index for which a value is removed
     * @param i     the value removed
     * @throws ContradictionException
     */
    public void awakeOnRem(int idx, int i) throws ContradictionException {
        // events over successor variables
        if ((2 < idx) && (idx < nbNodes + 3)) {
            if (AFFICHE) LOGGER.log(Level.INFO, "awakeOnRem");
            structure.getInputGraph().updateOnRem(idx - 3, i);
        }
        this.constAwake(false);
    }

    /**
     * Event based propagation related to the removal of a set of values in the domain of a variable. Only variables depicting a node of the graph
     * to partition are treated here.
     *
     * @param idx   the variable index for which a set of values is removed
     * @param deltaDomain   an iterator over the removed values
     * @throws ContradictionException
     */
    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        // events over successor variables
        if ((2 < idx) && (idx < nbNodes + 3)) {
            if (AFFICHE) LOGGER.log(Level.INFO, "awakeOnRemovals");
            structure.getInputGraph().updateOnRemovals(idx - 3, deltaDomain);
        }
        this.constAwake(false);
    }

}
