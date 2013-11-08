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

package samples.multicostregular.asap.hci.abstraction;

import choco.cp.solver.CPSolver;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import samples.multicostregular.asap.ASAPCPModel;
import samples.multicostregular.asap.heuristics.CoverVarSelector;

import java.util.Observable;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 9, 2009
 * Time: 1:14:21 PM
 */
public class ASAPDataHandler extends Observable {

    ASAPCPModel model;
    Solver solver;
    String file;
    Thread solving;
    public final static String DAYS[] = new String[]{"M ","Tu","W ","Th","F ","Sa","Su"};
    public final static Integer MODEL_FED       = 0;
    public final static Integer SOLVING         = 1;
    public final static Integer SOLUTION_FOUND  = 2;
    public final static Integer NO_SOLUTION     = 3;
    private boolean solved = false;


    public ASAPDataHandler()
    {
    }

    public void feed(String name)
    {
        this.file = name;
        this.solved =false;
        model = new ASAPCPModel(name);
        model.buildModel();
        this.solving = new ASAPResolutionThread(this);
        this.setChanged();
        this.notifyObservers(MODEL_FED);

    }


    public void solve()
    {
        this.solver = new CPSolver();
        this.solver.monitorFailLimit(true);
        this.solver.read(this.model);
      //  ((CPSolver)this.solver).setGeometricRestart(1500,1.0);
      //  ((CPSolver)this.solver).setRecordNogoodFromRestart(true);
        IntegerVariable[][] trans = ArrayUtils.transpose(this.model.shifts);
        IntDomainVar[][] vs = new IntDomainVar[trans.length][];
        for (int i  = 0 ; i < vs.length ; i++)
        {
            vs[i] = solver.getVar(trans[i]);
        }

        CoverVarSelector vsel = new CoverVarSelector(vs,this.model.lowb, solver);
        this.solver.setVarIntSelector(vsel);

        this.solver.setValIntSelector(vsel);

    /*    this.solver.setVarIntSelector(
                new StaticVarOrder(
                        this.solver.getVar(ArrayUtils.flatten(trans))
                )
        );  */
      //  this.solver.setVarIntSelector(new ASAPVarSelector(this.solver,ArrayUtils.transpose(this.model.shifts)));
        //this.solver.setValIntSelector(new ASAPValSelector(this.solver,ArrayUtils.transpose(this.model.shifts),this));

        solving.start();
        
        this.setChanged();
        this.notifyObservers(SOLVING);

    }

    public ASAPCPModel getCPModel()
    {
        return model;
    }

    public Solver getCPSolver()
    {                
        return solver;
    }


    public void setSolved(boolean b) {
        this.setChanged();
        //System.out.println(solving.getState());

        if (b)
        {
            solved = true;
            this.notifyObservers(SOLUTION_FOUND);
        }
        else
        {
            this.notifyObservers(NO_SOLUTION);
        }
        solving = new ASAPResolutionThread(this);
    }

    public void next() {
        solving.start();
        solving = new ASAPResolutionThread(this);
    }

    public boolean isSolved() {
        return solved;
    }
}