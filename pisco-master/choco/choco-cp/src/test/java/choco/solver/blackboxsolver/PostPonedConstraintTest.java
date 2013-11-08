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

package choco.solver.blackboxsolver;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import org.junit.Assert;
import org.junit.Test;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 9 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class PostPonedConstraintTest {

    @Test
    public void test1(){
        final IntegerVariable[] vars = Choco.makeBooleanVarArray("b", 2);

        final Constraint c1 = Choco.eq(vars[1], 1);
        final Constraint c2 = Choco.eq(vars[0], vars[1]);

        final Model m = new CPModel();

        m.addConstraint(Options.C_POST_PONED, c1);
        m.addConstraint(c2);

        final Solver s= new CPSolver();
        s.read(m);
        final SConstraint sc1 = s.getCstr(c1);
        final SConstraint sc2 = s.getCstr(c2);

        DisposableIterator<SConstraint> it = s.getConstraintIterator();
        Assert.assertTrue("no constraint", it.hasNext());
        Assert.assertEquals("wrong order", sc2, it.next());
        Assert.assertTrue("no constraint", it.hasNext());
        Assert.assertEquals("wrong order", sc1, it.next());
        Assert.assertFalse("still constraint", it.hasNext());


    }
}
