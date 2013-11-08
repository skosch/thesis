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

package choco.kernel.solver.constraints;

import java.util.Collection;
import java.util.Iterator;

import choco.kernel.solver.propagation.Propagator;

/**
 * An interface for handling collections (sets/sequences) of constraints.
 * This is useful for representing explanations, states, paths in a search tree, and so on.
 */
public interface SConstraintCollection {
  /**
   * Merges an explain with the current one.
   *
   * @param collection The collection of constraints that must be added to this
   */
  void merge(SConstraintCollection collection);

  /**
   * Clones the collection as a new one.
   */
  SConstraintCollection copy();

  /**
   * Adds a new constraint in the explain.
   *
   * @param constraint The constraint that should be added to the explain.
   *                   It must be a <code>PalmConstraint</code>.
   */

  void add(Propagator constraint);

  /**
   * Deletes a constraint from the explain.
   *
   * @param constraint The constraint that must be removed.
   */

  void delete(Propagator constraint);

  /**
   * Adds several constraints at a time
   *
   * @param collection The set of constraints
   */

  void addAll(Collection collection);

  /**
   * Checks if the explain is empty (that is wether the size of the set is null).
   */

  boolean isEmpty();

  /**
   * return the size of the bitSet
   */

  int size();

  /**
   * Deletes all indirect constraints.
   */

  void clear();

  /**
   * currentElement if a constraint is in the collection
   */
  boolean contains(Propagator ct);

  /**
   * currentElement inclusion
   */
  boolean containsAll(SConstraintCollection collec);
  
  /**
   * get an iterator over the collection of constraint.
   */
  Iterator<Propagator> iterator();
}
