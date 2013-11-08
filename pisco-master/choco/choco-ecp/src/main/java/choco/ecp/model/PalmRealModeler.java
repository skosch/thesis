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

package choco.ecp.model;

import choco.ecp.solver.constraints.real.PalmEquation;
import choco.ecp.solver.constraints.real.exp.PalmRealIntervalConstant;
import choco.ecp.solver.constraints.real.exp.PalmRealMinus;
import choco.ecp.solver.constraints.real.exp.PalmRealMult;
import choco.ecp.solver.constraints.real.exp.PalmRealPlus;
import choco.kernel.model.RealModeler;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealVar;

import java.util.HashSet;
import java.util.Set;

/**
 * J-CHOCO
 * Copyright (C) F. Laburthe, 1999-2003
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 24 juin 2004
 */
public class PalmRealModeler implements RealModeler {

    final Solver solver;


    public PalmRealModeler(Solver s) {
        solver = s;
    }

    /**
   * Makes an equation from an expression and a constantt interval. It is used by all methods building
   * constraints. This is  useful for subclassing this modeller  for another kind of problem (like PaLM).
   * @param exp The expression
   * @param cst The interval this expression should be in
   */
  public SConstraint makeEquation(RealExp exp, RealIntervalConstant cst) {
    // Collect the variables
    Set collectedVars = new HashSet();
    exp.collectVars(collectedVars);
    RealVar[] tmpVars = (RealVar[]) collectedVars.toArray();
    return new PalmEquation(solver, tmpVars, exp, cst);

  }

  /**
   * Addition of two expressions.
   */
  public RealExp plus(RealExp exp1, RealExp exp2) {
    return new PalmRealPlus(solver, exp1, exp2);
  }

  /**
   * Substraction of two expressions.
   */
  public RealExp minus(RealExp exp1, RealExp exp2) {
    return new PalmRealMinus(solver, exp1, exp2);
  }

  /**
   * Multiplication of two expressions.
   */
  public RealExp mult(RealExp exp1, RealExp exp2) {
    return new PalmRealMult(solver, exp1, exp2);
  }

  /**
   * Power of an expression.
   */
  public RealExp power(RealExp exp, int power) {
    if (power == 2) {
      return mult(exp, exp);
    }
    throw new UnsupportedOperationException();
  }

  /**
   * Cosinus of an expression.
   */
  public RealExp cos(RealExp  exp) {
    throw new UnsupportedOperationException();
  }

  /**
   * Sinus of an expression.
   */
  public RealExp sin(RealExp exp) {
    throw new UnsupportedOperationException();
  }

  /**
   * Makes a constant interval from a double d ([d,d]).
   */
  public RealIntervalConstant cst(double d) {
    return new PalmRealIntervalConstant(d, d);
  }

  /**
   * Makes a constant interval between two doubles [a,b].
   */
  public RealIntervalConstant cst(double a, double b) {
    return new PalmRealIntervalConstant(a, b);
  }
}
