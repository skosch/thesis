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

package choco.cp.model.managers.constraints.global;

import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.Geost_Constraint;
import choco.cp.solver.constraints.global.geost.externalConstraints.*;
import choco.cp.solver.constraints.global.geost.geometricPrim.Obj;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.geost.GeostOptions;
import choco.kernel.model.constraints.geost.externalConstraints.*;
import choco.kernel.model.variables.geost.GeostObject;
import choco.kernel.model.variables.geost.ShiftedBox;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 8 août 2008
 * Time: 19:38:51
 */
public final class GeostManager extends IntConstraintManager {

    /**
     * Build a constraint for the given solver and "model variables"
     *
     * @param solver
     * @param variables
     * @param parameters : a "hook" to attach any kind of parameters to constraints
     * @param options
     * @return
     */
    public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables/*variables model*/, Object parameters, List<String> options) {
        if (solver instanceof CPSolver) {
            if(parameters instanceof Object[]){
                Object[] params = (Object[])parameters;
                int dim = (Integer)params[0];
                List<ShiftedBox> shiftedBoxes = (List<ShiftedBox>)params[1];
                List<IExternalConstraint> ectr = (List<IExternalConstraint>)params[2];
                List<GeostObject> vgo = (List<GeostObject>)params[3];
                List<int[]> ctrlVs = (List<int[]>)params[4];
                GeostOptions opt = (GeostOptions) params[5];
                if (opt==null) { opt=new GeostOptions(); }

                //Transformation of Geost Objects (model) to interval geost object (constraint)
                List<Obj> vo = new ArrayList<Obj>(vgo.size());
                for (int i = 0; i < vgo.size(); i++) {
                    GeostObject g = vgo.get(i);
                    vo.add(i, new Obj(g.getDim(),
                            g.getObjectId(),
                            solver.getVar(g.getShapeId()),
                            solver.getVar(g.getCoordinates()),
                            solver.getVar(g.getStartTime()),
                            solver.getVar(g.getDurationTime()),
                            solver.getVar(g.getEndTime()),
                            g.getRadius())
                            );
                }

                List<ExternalConstraint> ectrs = new ArrayList<ExternalConstraint>();

                for (IExternalConstraint iectr : ectr) {
                    
                    if (iectr instanceof DistLeqModel) {
                        DistLeqModel dlm = (DistLeqModel) iectr;
                        if (dlm.hasDistanceVar()) 
                            ectrs.add(new DistLeq(dlm.getEctrID(), dlm.getDim(), dlm.getObjectIds(), dlm.D, dlm.q, solver.getVar(dlm.getDistanceVar() )));
                        else
                            ectrs.add(new DistLeq(dlm.getEctrID(), dlm.getDim(), dlm.getObjectIds(), dlm.D, dlm.q));
                    }

                    if (iectr instanceof DistGeqModel) {
                        DistGeqModel dgm = (DistGeqModel) iectr;
                        if (dgm.hasDistanceVar())
                            ectrs.add(new DistGeq(dgm.getEctrID(), dgm.getDim(), dgm.getObjectIds(), dgm.D, dgm.q, solver.getVar(dgm.getDistanceVar() )));
                        else
                            ectrs.add(new DistGeq(dgm.getEctrID(), dgm.getDim(), dgm.getObjectIds(), dgm.D, dgm.q));
                    }

                    if (iectr instanceof NonOverlappingModel) {
                         NonOverlappingModel ctr = (NonOverlappingModel) iectr;
                         ectrs.add(new NonOverlapping(ctr.getEctrID(),ctr.getDim(),ctr.getObjectIds()));
                    }

                    if (iectr instanceof DistLinearModel) {
                         DistLinearModel ctr = (DistLinearModel) iectr;
                         ectrs.add(new DistLinear(ctr.getEctrID(),ctr.getDim(),ctr.getObjectIds(), ctr.a, ctr.b));
                    }

                }


                if (ctrlVs == null) {
                    return new Geost_Constraint(solver.getVar((IntegerVariable[])variables)/*solver variables*/, dim, vo, shiftedBoxes, ectrs,false, opt.included, solver);
                } else {
                    return new Geost_Constraint(solver.getVar((IntegerVariable[])variables), dim, vo, shiftedBoxes, ectrs, ctrlVs,opt.memoisation, opt.included,opt.increment, solver);
                }
                        }
                    }
       throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
    }

}
