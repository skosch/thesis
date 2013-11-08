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

package samples.documentation;

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.solver.Solver;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 16 oct. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*
* the code example of documentation.
* Every method represents a sample for a specific chapter/part of the documentation.
* It ensures the code presented in the documentation is closed to the trunk source.
* Before creating the documentation, one has to run the j2tex jar to export code between
* commentary slashes // and the 'totex' or 'apptex' tags.
* The opening tag must be followed with the file name without extension
* and the closing tag must be empty.
 *
 * Create a new file (remove '_' character to make it work)
 * //_totex filename    (<- opening tag)
 * ...                  (<- code to export)
 * //_totex             (<- closing tag)
 *
 * Append to an existing file (remove '_' character to make it work):
 * //_apptex filename    (<- opening tag)
 * ...                  (<- code to export)
 * //_totex             (<- closing tag)
 *
 * Tags can be overlaped, it removes automatically totex tags in the exported file.
*/
public class Code4Doc1 {

    public static void main(String[] args) {
        new Code4Doc1().osin();
    }

    public void oabs() {
        //totex oabs
        Model m = new CPModel();
        IntegerVariable x = makeIntVar("x", 1, 5, Options.V_ENUM);
        IntegerVariable y = makeIntVar("y", -5, 5, Options.V_ENUM);
        m.addConstraint(eq(abs(x), y));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void ocos() {
        //totex ocos
        Model m = new CPModel();
        RealVariable x = makeRealVar("x", -Math.PI/2, Math.PI);
        m.addConstraint(eq(cos(x), 2/3));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void odiv() {
        //totex odiv
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", 1, 10);
        IntegerVariable w = makeIntVar("w", 22, 44);
        IntegerVariable z = makeIntVar("z", 12, 21);
        m.addConstraint(eq(z, div(w, x)));
        s.read(m);
        s.solve();
        //totex
    }

    public void oifthenelse() {
        //totex oifthenelse
        Model m = new CPModel();
        IntegerVariable x = makeIntVar("x", 1, 5);
        IntegerVariable y = makeIntVar("y", 0, 10);
        m.addConstraint(eq(y, ifThenElse(gt(x,2), mult(x,x), x)));
        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //totex
    }

    public void omax() {
        //totex omax
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 3, -3, 3);
        IntegerVariable maxv = makeIntVar("max", -3, 3);
        Constraint c = eq(maxv, max(v));
        m.addConstraint(c);
        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //totex
    }

    public void omin() {
        //totex omin
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] v = makeIntVarArray("v", 3, -3, 3);
        IntegerVariable minv = makeIntVar("min", -3, 3);
        Constraint c = eq(minv, min(v));
        m.addConstraint(c);
        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //totex
    }

    public void ominus() {
        //totex ominus
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable a = makeIntVar("a", 0, 4);
        m.addConstraint(eq(minus(a, 1), 2));
        s.read(m);
        s.solve();
        //totex
    }

    public void omod() {
        //totex omod
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", 1, 10);
        IntegerVariable w = makeIntVar("w", 22, 44);
        m.addConstraint(eq(1, mod(w, x)));
        s.read(m);
        s.solve();
        //totex
    }

    public void omult() {
        //totex omult
        CPModel m = new CPModel();
        IntegerVariable x = makeIntVar("x", -10, 10);
        IntegerVariable z = makeIntVar("z", -10, 10);
        IntegerVariable w = makeIntVar("w", -10, 10);
        m.addVariables(x, z, w);
        CPSolver s = new CPSolver();
        // x >= z * w
        Constraint exp = geq(x, mult(z, w));
        m.setDefaultExpressionDecomposition(true);
        m.addConstraint(exp);
        s.read(m);
        s.solveAll();

        //totex        
    }

    public void oneg() {
        //totex oneg
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", -10, 10);
        IntegerVariable w = makeIntVar("w", -10, 10);
        // -x = w - 20
        m.addConstraint(eq(neg(x), minus(w, 20)));
        s.read(m);
        s.solve();
        //totex
    }

    public void oplus() {
        //totex oplus
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable a = makeIntVar("a", 0, 4);
        // a + 1 = 2
        m.addConstraint(eq(plus(a, 1), 2));
        s.read(m);
        s.solve();
        //totex
    }

    public void opower() {
        //totex opower
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", 0, 10);
        IntegerVariable y = makeIntVar("y", 2, 4);
        IntegerVariable z = makeIntVar("z", 28, 80);
        m.addConstraint(eq(z, power(x, y)));
        s.read(m);
        s.solve();
        //totex
    }


    public void oscalar() {
        //totex oscalar
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("C", 9, 1, 10);
        int[] coefficients = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        m.addConstraint(eq(165, scalar(coefficients, vars)));
        
        s.read(m);
        s.solve();
        System.out.print("165 = (" + coefficients[0] + "*" + s.getVar(vars[0]).getVal()+")");
        for (int i = 1; i < vars.length; i++) {
            System.out.print(" + (" + coefficients[i] + "*" + s.getVar(vars[i]).getVal()+")");
        }
        System.out.println();
        //totex
    }

    public void osin() {
        //totex osin
        Model m = new CPModel();
        RealVariable x = makeRealVar("x", 0, Math.PI);
        m.addConstraint(eq(sin(x), 1));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void osum(){
        //totex osum
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] vars = makeIntVarArray("C", 10, 1, 10);
        m.addConstraint(eq(99, sum(vars)));

        s.read(m);
        s.solve();
        if(s.isFeasible()){
            System.out.print("99 = " + s.getVar(vars[0]).getVal());
            for (int i = 1; i < vars.length; i++) {
                System.out.print(" + "+s.getVar(vars[i]).getVal());
            }
            System.out.println();
        }
        //totex
    }
}
