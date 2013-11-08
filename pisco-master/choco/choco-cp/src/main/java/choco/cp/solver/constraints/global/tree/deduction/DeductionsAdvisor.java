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

package choco.cp.solver.constraints.global.tree.deduction;

import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.global.tree.structure.internalStructure.StructuresAdvisor;
import choco.kernel.solver.Solver;



public class DeductionsAdvisor {

    /**
     * boolean for debug and show a trace of the execution
     */
    protected boolean affiche;

    /**
     * check the potential update of the internal structures related to the precedences and the incomparabilities
     */
    protected boolean update;

    /**
     * check the compatibility of the udpate according to the constraint itself
     */
    protected boolean compatible;

    /**
     * Choco solver embedding the tree constraint
     */
    protected Solver solver;

    /**
     * attributes
     */
    protected TreeParameters tree;

    /**
     * structure advisor
     */
    protected StructuresAdvisor struct;

    /**
     * structure that compute new precedence constraints from the other structures
     */
    protected OrderedGraphDeduction precs;

    /**
     * structure that compute new incomparability constraints from the other structures
     */
    protected IncompGraphDeduction incomp;

    /**
     * constructor: allocates the data util for the deduction manager
     *
     * @param solver    the Choco solver who uses the current tree constraint.
     * @param tree  the input data structure available in the <code> structure.inputStructure </code> package.
     * @param struct    the advisor of the internal data structures
     * @param affiche   a boolean that allow to display the main actions done by the filtering manager
     */
    public DeductionsAdvisor(Solver solver, TreeParameters tree, StructuresAdvisor struct, boolean affiche) {
        this.solver = solver;
        this.tree = tree;
        this.struct = struct;
        this.affiche = affiche;

        this.update = false;
        this.compatible = true;

        // initialiaze structures related to the deductions
        Object[] params = new Object[]{this.solver, this.tree, this.struct, this.update, this.compatible, this.affiche};
        this.precs = new OrderedGraphDeduction(params);
        this.incomp = new IncompGraphDeduction(params);
    }

    /**
     * the main method of the deduction advisor that updates each part of the tree constraint according to the other.
     *
     * @return   <code> true </code> iff a deduction modified the structures (precedences, incomparabilites or dominators)
     */
    public boolean applyDeduction() {
        boolean update;
        do {
            struct.getDoms().updateDominators();
            precs.updateOrderedGraphWithDeductions();
            incomp.updateIncompGraphWithDeductions();
            compatible = precs.isCompatible() && incomp.isCompatible();
            update = precs.isUpdate() || incomp.isUpdate() || struct.getDoms().isUpdate();
        }
        while (update && compatible);
        return update;
    }

    public boolean isCompatible() {
        return compatible;
    }
}
