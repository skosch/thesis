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


import choco.cp.solver.constraints.global.geost.Constants;
import choco.cp.solver.constraints.global.geost.Setup;
import choco.cp.solver.constraints.global.geost.externalConstraints.ExternalConstraint;
import choco.cp.solver.constraints.global.geost.geometricPrim.Obj;
import choco.cp.solver.constraints.global.geost.internalConstraints.InternalConstraint;
import choco.cp.solver.constraints.global.geost.layers.ExternalLayer;
import choco.cp.solver.constraints.global.geost.layers.GeometricKernel;
import choco.cp.solver.constraints.global.geost.layers.IntermediateLayer;
import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import com.sun.tools.javac.util.Pair;
import choco.kernel.memory.IStateInt;
import choco.kernel.model.variables.geost.ShiftedBox;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solution;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.HashMap;
import java.util.List;

public final class Geost_Constraint extends AbstractLargeIntSConstraint {

    /**
     * Array of objects ids.
     * initial order is not preserved in greedy mode, due to iteration over fixed objects.
     */
    int[] oIDs;
    /**
     * Index of the last non fixed object id (used with greedy mode)
     */
    IStateInt lastNonFixedO;

	Constants cst;
	Setup stp;
	ExternalLayer externalLayer;
	GeometricKernel geometricKernel;
	IntermediateLayer intermediateLayer;
    protected Solver s;
    private int greedyMode = 0;
    boolean increment = false;
    List<int[]> ctrlVs ;

	/**
	 * Creates a geost constraint with the given parameters.
	 * @param vars Array of Variables for choco
     * @param k Dimension of the problem we are working with
     * @param objects A vector containing the objects (obj)
     * @param shiftedBoxes A vector containing the shifted boxes
     * @param ectr A vector containing the External Constraints in our problem
     * @param ctrlVs A list of controlling vectors used in the greedy mode
     * @param solver
     */

	public Geost_Constraint(IntDomainVar[] vars, int k, List<Obj> objects, List<ShiftedBox> shiftedBoxes, List<ExternalConstraint> ectr, List<int[]> ctrlVs, boolean memo_active, HashMap<Pair<Integer, Integer>, Boolean> included,
                            boolean increment_, Solver solver)
	{

        super(ConstraintEvent.VERY_SLOW, vars);

        cst = new Constants();
		stp = new Setup(cst, solver.getPropagationEngine(), this);
		intermediateLayer = new IntermediateLayer();
		externalLayer = new ExternalLayer(cst, stp);
		geometricKernel = new GeometricKernel(cst, stp, externalLayer, intermediateLayer,memo_active,included, solver, this);

		cst.setDIM(k);
        this.ctrlVs = ctrlVs;

		stp.SetupTheProblem(objects, shiftedBoxes, ectr);

		//this should be changed and be provided globally to the system
		oIDs = new int[stp.getNbOfObjects()];
		for(int i = 0; i< stp.getNbOfObjects(); i++){
			oIDs[i] = objects.get(i).getObjectId();
        }
        lastNonFixedO = solver.getEnvironment().makeInt(oIDs.length);

        this.s = solver;
        this.greedyMode = 1;
        this.increment=increment_;


        IntDomainVarImpl D = new IntDomainVarImpl(s,"D",IntDomainVar.BOUNDS,0,100);


	}


	/**
	 * Creates a geost constraint with the given parameters.
	 * @param vars Array of Variables for choco
     * @param k Dimension of the problem we are working with
     * @param objects A vector containing the objects (obj)
     * @param shiftedBoxes A vector containing the shifted boxes
     * @param ectr A vector containing the External Constraints in our problem
     * @param solver
     */

	public Geost_Constraint(IntDomainVar[] vars, int k, List<Obj> objects, List<ShiftedBox> shiftedBoxes, List<ExternalConstraint> ectr, boolean memo, HashMap<Pair<Integer, Integer>, Boolean> included, Solver solver)
	{
        super(ConstraintEvent.VERY_SLOW, vars);

        cst = new Constants();
		stp = new Setup(cst, solver.getPropagationEngine(), this);
		intermediateLayer = new IntermediateLayer();
		externalLayer = new ExternalLayer(cst, stp);
		geometricKernel = new GeometricKernel(cst, stp, externalLayer, intermediateLayer, memo, included, solver, this);

		cst.setDIM(k);

		stp.SetupTheProblem(objects, shiftedBoxes, ectr);

		//this should be changed and be provided globally to the system
		oIDs = new int[stp.getNbOfObjects()];
		for(int i = 0; i< stp.getNbOfObjects(); i++){
			oIDs[i] = objects.get(i).getObjectId();
        }
        lastNonFixedO = solver.getEnvironment().makeInt(oIDs.length);

        this.s = solver;

	}
    
    public void filter() throws ContradictionException{
      if(this.greedyMode == 0)  {
		  filterWithoutGreedyMode();
      }
	  else {          
        long tmpTime = (System.nanoTime() / 1000000);
        filterWithGreedyMode();
        stp.opt.timefilterWithGreedyMode += ((System.nanoTime()/1000000) - tmpTime);
      }

	}

	private void filterWithGreedyMode() throws ContradictionException{
        if (stp.opt.debug) LOGGER.info("Geost_Constraint:filterWithGreedyMode()");
        s.worldPush();    //Starts a new branch in the search tree
        boolean result = false;

        if (!increment) {
            long tmpTimeFixAllObj = System.nanoTime() / 1000000;
            result=geometricKernel.fixAllObjs(cst.getDIM(), oIDs, stp.getConstraints(), this.ctrlVs, lastNonFixedO);
            stp.opt.timeFixAllObj += ((System.nanoTime() / 1000000) - tmpTimeFixAllObj);
        }
        else {
            long tmpTimeFixAllObj = System.nanoTime() / 1000000;
            result=geometricKernel.fixAllObjs_incr(cst.getDIM(), oIDs, stp.getConstraints(), this.ctrlVs, lastNonFixedO);
            stp.opt.timeFixAllObj += ((System.nanoTime() / 1000000) - tmpTimeFixAllObj);
        }
        if (!result){
			s.worldPop();
            long tmpTime = (System.nanoTime() / 1000000);
			filterWithoutGreedyMode();
            stp.opt.timefilterWithoutGreedyMode += ((System.nanoTime()/1000000) - tmpTime);

		}
		else{

           long tmpTime = (System.nanoTime() / 1000000);
            //s.getSearchStrategy().recordSolution();
			Solution sol = new Solution(s);

            for (int i=0; i<s.getNbIntVars(); i++) {
				//int idx = s.getIntVarIndex(var);
                // idx = -1 means/**/ that it is not a variable but a constant
                // and we do not need to record it
                //if(idx != -1){
                    sol.recordIntValue(i, s.getIntVar(i).getVal());

                //}
            }
            stp.opt.handleSolution1 += ((System.nanoTime()/1000000) - tmpTime);
            tmpTime = (System.nanoTime() / 1000000);
			s.worldPop();  //Come back to the state before propagation
            stp.opt.handleSolution2 += ((System.nanoTime()/1000000) - tmpTime);
            tmpTime = (System.nanoTime() / 1000000);
            //s.getSearchStrategy().restoreBestSolution();

            s.restoreSolution(sol);//Restore the solution
            stp.opt.handleSolution3 += ((System.nanoTime()/1000000) - tmpTime);
		}
	}


	private void filterWithoutGreedyMode() throws ContradictionException{
        if (stp.opt.debug) LOGGER.info("Geost_Constraint:filterWithoutGreedyMode()");
        if(!geometricKernel.filterCtrs(cst.getDIM(), oIDs, stp.getConstraints()))
			this.fail();
	}

	public boolean isSatisfied() {
        boolean b = false;
        s.worldPushDuringPropagation();
        try {
            b = geometricKernel.filterCtrs(cst.getDIM(), oIDs,
                                    stp.getConstraints());
        } catch (ContradictionException e) {
            b = false;
        }
        s.worldPopDuringPropagation();
        return b;
}


	public void propagate() throws ContradictionException {
//        int l=vars.length;
//        for (int i=0; i<l; i++)
//            LOGGER.info("Geost_Constraint:propagate():vars["+i+"]:"+vars[i]+","+vars[i].getInf()+","+vars[i].getSup());
//        LOGGER.info("----propagate");          ^
        if (stp.opt.debug) LOGGER.info("GeostConstraint:propagate()");
		filter();
	}

	public void awake() throws ContradictionException {
		this.constAwake(false);
		//filter();
	}


	public void awakeOnInst(int idx) throws ContradictionException {
		this.constAwake(false);
		//filter();
	}



	public void awakeOnInf(int idx) throws ContradictionException {
		this.constAwake(false);
		//filter();
	}

	public void awakeOnSup(int idx) throws ContradictionException {
		this.constAwake(false);
		//filter();
	}

    public void awakeOnBounds(int varIndex) throws ContradictionException {
    	this.constAwake(false);
		//filter();
	 }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
		this.constAwake(false);
		//filter();
	 }

	public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
		this.constAwake(false);
		//filter();
	}

	public Constants getCst() {
		return cst;
	}

	public Setup getStp() {
		return stp;
	}

	public void setCst(Constants cst) {
		this.cst = cst;
	}

	public void setStp(Setup stp) {
		this.stp = stp;
	}

    public ExternalLayer getExternalLayer() {
        return externalLayer;
    }

    public List<InternalConstraint> getForbiddenRegions(Obj o) {

        //Should be set up only once during a single fixpoint
        List<ExternalConstraint> ectrs  = stp.getConstraints();
        for (int i = 0; i < ectrs.size(); i++)
		{
			ectrs.get(i).setFrame(externalLayer.InitFrameExternalConstraint(ectrs.get(i), oIDs));
		}
        
        //TODO: Holes should be generated here

        for (int i = 0; i < o.getRelatedExternalConstraints().size(); i++)
        {
            List<InternalConstraint> v = externalLayer.genInternalCtrs(o.getRelatedExternalConstraints().get(i), o);
            for (int j = 0; j < v.size(); j++)
            {
                o.addRelatedInternalConstraint(v.get(j));
            }
        }

        return o.getRelatedInternalConstraints();
    }

    public void setGreedy(boolean greedy){
        this.greedyMode = greedy?1:0;
    }

    public boolean isGreedy(){
        return greedyMode != 0;
    }

}
