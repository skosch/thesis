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

package samples.tutorials.to_sort.scheduling;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.scheduling.TaskVariable;

import java.util.logging.Logger;

import static choco.Choco.neq;


/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: Nov 28, 2008
 * Time: 2:23:37 PM
 * A simple example 
 */
public class MeetingScheduling {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

    protected int nbP;          // number of proposals
    protected int nbE;          // number of evaluators

    protected int[][] reva;     // reva[i][j] = 1 if reviewer i is assigned to meeting j
    protected int[] numberOfEv; //the number of evaluators for each proposal

    protected int horizon;
    
    protected CPModel m;
    protected TaskVariable[] proposals;

    public MeetingScheduling(int nbP, int nbE, int[][] revAssignment) {
        this.nbP = nbP;
        this.nbE = nbE;
        this.reva = revAssignment;
        numberOfEv = new int[nbE];
        for (int i = 0; i < nbE; i++) {
            for (int j = 0; j < nbP; j++) {
                if (reva[i][j] == 1) numberOfEv[j]++;
            }
        }
    }

    public void buildModel() {
        horizon = 2*nbP;
        m = new CPModel();

        proposals = new TaskVariable[nbP];
        for (int i = 0; i < nbP; i++) {
            proposals[i] = Choco.makeTaskVar("p_" + i, horizon, 1);
        }

        //each reviewer is a unary resource
        Constraint[] rsc = new Constraint[nbE];
//        for (int i = 0; i < nbE; i++) {
//            rsc[i] = Scheduling.makeUnaryResource();           
//        }
//        for (int i = 0; i < nbE; i++) {
//            for (int j = 0; j < nbP; j++) {
//                if (reva[i][j] == 1) rsc[i].addTask(proposals[j]);                
//            }
//        }

        //evaluator 1 is not available at timeslots 1 and 2
        for (int i = 0; i < nbP; i++) {
            if (reva[0][i] == 1) { //evaluator 2 is needed for proposal i
                //m.addConstraint(neq(proposals[i].start(),0));
                m.addConstraint(neq(proposals[i].start(),1));
                m.addConstraint(neq(proposals[i].start(),2));
            }
        }

        //proposals are ordered by number of reviewers
        for (int i = 0; i < nbP; i++) {
            for (int j = i + 1; j < nbP; j++) {
                if (numberOfEv[i] < numberOfEv[j]) {
                   m.addConstraint(Choco.startsAfterEnd(proposals[j],proposals[i]));
                }
            }
        }
        m.addConstraints(rsc);
    }

    public void solve() {
        CPSolver solver=new CPSolver();
        solver.setHorizon(horizon);
		solver.read(m);
        solver.minimize(solver.getMakespan(),false);

        //print solution
        if(solver.isFeasible()) {
        for (int i = 0; i < nbP; i++) {
            LOGGER.info("P" + i + ": " + solver.getVar(proposals[i]));
        }
        } else LOGGER.info("no solution");
    }


    public static void main(String[] args) {
        MeetingScheduling mt = new MeetingScheduling(3,3,new int[][]{{1,1,0},
                                                                     {1,0,1},
                                                                     {0,0,1}});
        mt.buildModel();
        mt.solve();
    }
}
