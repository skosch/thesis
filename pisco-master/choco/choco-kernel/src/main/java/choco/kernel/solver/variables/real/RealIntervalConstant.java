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

package choco.kernel.solver.variables.real;

import choco.kernel.common.HashCoding;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.propagation.event.VarEvent;

import java.util.List;
import java.util.Set;

/**
 * A constant real interval.
 */
public class RealIntervalConstant implements RealConstant {
  protected final double inf;
  protected final double sup;

    private long index;

    Solver solver;


  public RealIntervalConstant(RealInterval interval) {
    this.inf = interval.getInf();
    this.sup = interval.getSup();
  }

  public RealIntervalConstant(double inf, double sup) {
    this.inf = inf;
    this.sup = sup;
  }

  public String toString() {
    return "[" + inf + "," + sup + "]";
  }

    @Override
	public int hashCode() {
		return HashCoding.hashCodeMe(new Object[]{index});
	}

    /**
     * Unique index
     * (Different from hashCode, can change from one execution to another one)
     *
     * @return the indice of the objet
     */
    @Override
    public long getIndex() {
        return index;
    }

    public double getInf() {
    return inf;
  }

  public double getSup() {
    return sup;
  }

  public void intersect(RealInterval interval) throws ContradictionException {
  }

  public void tighten() {
  }

  public void project() {
  }

  public String pretty() {
    return toString();
  }

  public List<RealExp> subExps(List<RealExp> l) {
    l.add(this);
    return l;
  }

  public Set<RealVar> collectVars(Set<RealVar> s) {
    return s;
  }

  public boolean isolate(RealVar var, List<RealExp> wx, List<RealExp> wox) {
    return false;
  }

  /**
   * Retrieves the solver of the entity
   */

  public Solver getSolver() {
    return solver;
  }

  public void setSolver(Solver solver) {
    this.solver = solver;
      index = solver.getIndexfactory().getIndex();
  }


    /**
     * Returns the number of listeners involving the variable.
     *
     * @return the numbers of listeners involving the variable
     */
    @Override
    public int getNbConstraints() {
        return 0;
    }

    /**
     * Returns the <code>i</code>th constraint. <code>i</code>
     * should be more than or equal to 0, and less or equal to
     * the number of constraint minus 1.
     *
     * @param i number of constraint to be returned
     * @return the ith constraint
     */
    @Override
    public SConstraint getConstraint(int i) {
        return null;
    }

    /**
     * returns the index of the variable in it i-th constraint
     *
     * @param constraintIndex the index of the constraint (among all constraints linked to the variable)
     * @return the index of the variable (0 if this is the first variable of that constraint)
     */
    @Override
    public int getVarIndex(int constraintIndex) {
        return 0;
    }

    /**
     * access the data structure storing constraints involving a given variable
     *
     * @return the vector of constraints
     */
    @Override
    public PartiallyStoredVector<SConstraint> getConstraintVector() {
        return null;
    }

    /**
     * access the data structure storing indices associated to constraints involving a given variable
     *
     * @return the vector of index
     */
    @Override
    public PartiallyStoredIntVector getIndexVector() {
        return null;
    }

    /**
     * <b>Public user API:</b>
     * <i>Domains :</i> testing whether a variable is instantiated or not.
     *
     * @return a boolean giving if a variable is instanciated or not
     */
    @Override
    public boolean isInstantiated() {
        return true;
    }

    /**
     * Adds a new listener for the variable, that is a constraint which
     * should be informed as soon as the variable domain is modified.
     * The addition can be dynamic (undone upon backtracking) or not
     *
     * @param c               the constraint to add
     * @param varIdx          index of the variable
     * @param dynamicAddition dynamical addition or not
     * @return the number of the listener
     */
    @Override
    public int addConstraint(SConstraint c, int varIdx, boolean dynamicAddition) {
        return 0;
    }

    /**
     * returns the object used by the propagation engine to model a propagation event associated to the variable
     * (an update to its domain)
     *
     * @return the propagation event
     */
    @Override
    public VarEvent getEvent() {
        return null;
    }

    /**
     * This methods should be used if one want to access the different constraints
     * currently posted on this variable.
     * <p/>
     * Indeed, since indices are not always
     * consecutive, it is the only simple way to achieve this.
     *
     * @return an iterator over all constraints involving this variable
     */
    @Override
    public DisposableIterator<SConstraint> getConstraintsIterator() {
        return null;
    }

    @Override
    public int getPriority() {
        return ConstraintEvent.UNARY;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public RealDomain getDomain() {
        return null;
    }

    /**
     * Modifies bounds silently (does not propagate modifications). This is usefull for box cosistency.
     *
     * @param i
     */
    @Override
    public void silentlyAssign(RealInterval i) {
    }

    @Override
    public RealInterval getValue() {
        return this;
    }
    

	@Override
	public Extension getExtension(int extensionNumber) {
		return null;
	}

    @Override
    public void addExtension(final int extensionNumber) {}

	
    
}
