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

package choco.cp.solver.goals.choice;

import choco.cp.solver.goals.GoalHelper;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.goals.Goal;
import choco.kernel.solver.goals.GoalType;
import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;


/*
 * Created by IntelliJ IDEA.
 * User: GROCHART
 * Date: 11 janv. 2008
 * Since : Choco 2.0.0
 *
 */
public class Instantiate implements Goal {

	protected IntDomainVar var;
	protected ValSelector<IntDomainVar> valSelector;
  protected ValIterator<IntDomainVar> valIterator;
  protected int previousVal = Integer.MAX_VALUE;

  public Instantiate(IntDomainVar var, ValSelector<IntDomainVar> s) {
		this.var = var;
		this.valSelector = s;
	}

  public Instantiate(IntDomainVar var, ValIterator<IntDomainVar> valIterator) {
    this.var = var;
		this.valIterator = valIterator;
  }

  public Instantiate(IntDomainVar var) {
		this.var = var;
		this.valSelector = new MinVal();
	}

  public String pretty() {
    return "Instantiate " + var.pretty();
  }

  public Goal execute(Solver s) throws ContradictionException {
		if (var.isInstantiated()) return null;
    int val = -1;
    if (valIterator != null) {
      if (previousVal == Integer.MAX_VALUE) {
        val = valIterator.getFirstVal(var);
      } else {
        if (valIterator.hasNextVal(var, previousVal))
          val = valIterator.getNextVal(var, previousVal);
      }
      previousVal = val;
    } else {
      val = valSelector.getBestVal(var);
    }
    return GoalHelper.or(GoalHelper.setVal(var, val),
				GoalHelper.and(GoalHelper.remVal(var, val), this));
	}

    @Override
    public GoalType getType() {
        return GoalType.INST;
    }
}
