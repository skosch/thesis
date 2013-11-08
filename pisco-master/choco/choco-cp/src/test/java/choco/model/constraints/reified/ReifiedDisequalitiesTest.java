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

package choco.model.constraints.reified;

import static choco.Choco.and;
import static choco.Choco.makeIntVar;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import static choco.kernel.model.constraints.ConstraintType.*;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
/* 
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 29 oct. 2008
 */

public class ReifiedDisequalitiesTest {

    private ConstraintType[] types;

    @Before
    public void before(){
        types = new ConstraintType[]{EQ, NEQ, GEQ, LEQ, GT, LT};
    }


    @Test
    public void testDisequalities(){
        Model m1;
        Model m2;
        Solver s1;
        Solver s2;
        IntegerVariable x = makeIntVar("x", 1, 3);
        IntegerVariable y = makeIntVar("y", 1, 3);
        IntegerVariable z = makeIntVar("z", 1, 3);
        Random r = new Random();

        for(int seed = 0; seed < 60; seed++){
            int op = r.nextInt(6);
            Constraint c1 = new ComponentConstraint(types[op], types[op], new Variable[]{x, y});
            op = r.nextInt(6);
            Constraint c2 = new ComponentConstraint(types[op], types[op], new Variable[]{y, z});
    
            m1 = new CPModel();
            m2 = new CPModel();

            m1.addConstraints(c1, c2);
            m2.addConstraint(and(c1, c2));

            s1 = new CPSolver();
            s2 = new CPSolver();

            s1.read(m1);
            s2.read(m2);

            s1.solveAll();
            s2.solveAll();

            Assert.assertEquals("Same number of solution", s1.getNbSolutions(), s2.getNbSolutions());
        }
    }


}
