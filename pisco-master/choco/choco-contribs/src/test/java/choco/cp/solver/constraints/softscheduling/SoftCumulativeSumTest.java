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

package choco.cp.solver.constraints.softscheduling;

import static choco.Choco.makeIntVar;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.softscheduling.SoftCumulativeSumManager;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: thierry
 * Date: 9 nov. 2009
 * Time: 15:33:01
 * To change this template use File | Settings | File Templates.
 */
public class SoftCumulativeSumTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();
    public static boolean debug = false;

    // Todo : implement isSatisfied methods of SofCumulative and SoftCumulativeSum (remove from abstract class)

    public static int unitTest1(int seed) {
		// parametres
        int makespan =          5;
      	int nbTasks =           4;
      	int maxDuration =       2;
      	int maxHeight = 	    2;
      	int maxCost = 			5;
      	int wishCapa =          2;
        Random r = new Random(seed);
      	int[] durations = new int[nbTasks];
      	int[] heights = new int[nbTasks];
      	for (int i = 0; i < nbTasks; i++) {
			durations[i] = (Math.abs(r.nextInt()))%(maxDuration)+1;
			heights[i] = (Math.abs(r.nextInt()))%(maxHeight)+1;
		}
        Object[] params = new Object[3];
        params[0] = durations;
        params[1] = heights;
        params[2] = wishCapa;

        // variables
        Model m = new CPModel();
		IntegerVariable[] vars = new IntegerVariable[nbTasks+makespan+1];
        for(int i=0; i<nbTasks; i++) { // starts
      		vars[i] = makeIntVar("start[i]",0,makespan-durations[i]);
      	}
		for(int i   = nbTasks; i< (nbTasks+makespan); i++) { // costs
			vars[i] = makeIntVar("cost " +(i-nbTasks),0,maxCost);
		}
        vars[nbTasks+makespan] = makeIntVar("obj ", 0, makespan*maxCost); // objectif

        // conntrainte
        ComponentConstraint c = new ComponentConstraint(SoftCumulativeSumManager.class,(Object) params,vars);
        m.addConstraint(c);

        // solve
        CPSolver s = new CPSolver();
        s.read(m);
        s.minimize(s.getVar(vars[nbTasks+makespan]),true);

        // display

        if(debug) {
		    String res = "";
		    for(int i = 0; i<nbTasks; i++) {
			    int val = s.getVar(vars[i]).getVal();
			    res += "[";
			    res += val;
			    res += ", ";
			    res += val+durations[i];
			    res += "[";
                res += "(" + heights[i] +  ")  ";
			    if(i!= 0 && i%8==0) {
				   res += '\n';
			    }
		    }
            res+="  costs =";
            for(int i=nbTasks; i<nbTasks+makespan; i++) {
                res += " " + s.getVar(vars[i]).getVal();
            }
            res += ", obj = " + s.getVar(vars[nbTasks+makespan]).getVal();
		    //System.out.println(res);   
	    }

        return s.getVar(vars[nbTasks+makespan]).getVal();
    }

    @Test
    public void test() {
        int obj = unitTest1(1986);
        assertTrue(obj==1);
        obj = unitTest1(1987);
        assertTrue(obj==2);
        obj = unitTest1(1988);
        assertTrue(obj==1);
        obj = unitTest1(1989);
        assertTrue(obj==0);
        obj = unitTest1(1990);
        assertTrue(obj==1);
    }
}
