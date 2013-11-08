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

package choco.cp.solver.variables.integer;

import choco.cp.common.util.iterators.IntDomainIterator;
import choco.cp.solver.variables.delta.StackDeltaDomain;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBinaryTree;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.propagation.PropagationEngine;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 25, 2008
 * Time: 9:34:40 AM
 * <p/>
 * A choco domain class represented by a binary tree of int interval
 */
public final class IntervalBTreeDomain extends AbstractIntDomain {

    /**
     * static instance of Random to get random number
     */
    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * The binary tree representing the domain
     */
    private final IStateBinaryTree btree;

    /**
     * Backtrackable int to avoid recalculating the number of value in the domain
     */
    private final IStateInt size;

    /**
     * the initial size of the domain (never increases)
     */
    private final int capacity;

    protected IntDomainIterator _iterator;

    /**
     * Construct a new domain represented by a Binary Tree of Interval
     *
     * @param v                 the associatede variable
     * @param a                 the lower bound
     * @param b                 the upper bound
     * @param environment
     * @param propagationEngine
     */
    public IntervalBTreeDomain(IntDomainVarImpl v, int a, int b, IEnvironment environment, PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        btree = environment.makeBinaryTree(a, b);
        capacity = b - a + 1;
        size = environment.makeInt(capacity);
        deltaDom = new StackDeltaDomain();
    }

    /**
     * Construct a new domain represented by a Binary Tree of Interval
     *
     * @param v                 the associatede variable
     * @param sortedValues      array of values
     * @param environment
     * @param propagationEngine
     */
    public IntervalBTreeDomain(IntDomainVarImpl v, int[] sortedValues, IEnvironment environment, PropagationEngine propagationEngine) {
        super(v, propagationEngine);
        int a = sortedValues[0];
        btree = environment.makeBinaryTree(a, a);
        capacity = sortedValues.length;
        IStateBinaryTree.Node n = btree.getRoot();
        for (int i = 1; i < capacity; i++) {
            int b = sortedValues[i];
            if (b == a + 1) {
                n.setSup(b);
            } else {
                n = new IStateBinaryTree.Node(btree, b, b);
                btree.add(n, false);
            }
            a = b;
        }
        size = environment.makeInt(btree.getSize());
        deltaDom = new StackDeltaDomain();
    }

    /**
     * Gets the lowest value of the domain
     *
     * @return the lower bound of the domain
     */
    public int getInf() {
        return btree.getFirstNode().inf;
    }

    /**
     * Gets the greatest value of the domain
     *
     * @return the greater bound of the domain
     */
    public int getSup() {
        return btree.getLastNode().sup;
    }

    /**
     * Updates the inf bound of the domain to the given value
     * TODO: Must exist a better way than removing all values until the new inf is reached
     *
     * @param x integer value
     * @return the new lower bound
     */
    public int updateInf(int x) {
        for (int i = this.getInf(); i < x; i++) {
            this.remove(i);
        }
        return this.getInf();
    }

    /**
     * Updates the sup of the domain to the given value
     * TODO: Must exist a better way than removing all values (e.g. removing all the node but the one containing the sup)
     *
     * @param x integer value
     * @return the new greater bound
     */
    public int updateSup(int x) {
        for (int i = this.getSup(); i > x; i--) {
            this.remove(i);
        }
        return this.getSup();
    }

    /**
     * Checks wether a value is in the domain
     *
     * @param x the value to be checked
     * @return true if x is in the domain, false otherwise
     */
    public boolean contains(int x) {
        return btree.find(x) != null;
    }

    /**
     * Removes a value from the domain
     *
     * @param x the value to be removed
     * @return true if removal is a success, false otherwise
     */
    public boolean remove(int x) {
        boolean b = btree.remove(x);
        if (b) {
            removeIndex(x);
            this.size.add(-1);
        }
        return b;
    }

//    private void updateStack(int i){
//        if(removedvalues==null)removedvalues=new Stack<Integer>();
//        removedvalues.push(i);
//    }

    private void removeIndex(int i) {
        deltaDom.remove(i);
    }

    /**
     * Restrict the domain to one value
     * TODO: must existe a better way to remove all the other Node in the tree
     *
     * @param x integer value
     */
    public void restrict(int x) {
        IStateBinaryTree.Node current = btree.getRoot();
        while (current.leftNode != null) {
            btree.remove(current.leftNode);
        }
        while (current.rightNode != null) {
            btree.remove(current.rightNode);
        }
        btree.getRoot().setInf(x);
        btree.getRoot().setSup(x);
        this.size.set(1);
    }

    /**
     * Indicates the number of value in the domain
     *
     * @return the size of the domain
     */
    public int getSize() {
        return this.size.get();
    }

    public DisposableIntIterator getIterator() {
        if (_iterator == null) {
            _iterator = new IntDomainIterator();
        }else if (!_iterator.reusable()) {
            //assert false;
            _iterator = new IntDomainIterator();
        }
        _iterator.init(this);
        return _iterator;
    }

    /**
     * gets the next value of x in this domain
     *
     * @param x integer value
     * @return -infinity if not found, the next value otherwise
     */
    public int getNextValue(int x) {
        IStateBinaryTree.Node n = btree.nextNode(x);
        if (n == null) {
            return Integer.MAX_VALUE;
        } else if (n.contains(x + 1)) {
            return x + 1;
        } else {
            return n.getInf();
        }
    }

    /**
     * gets the previous value of x in this domain
     *
     * @param x integer value
     * @return -infinity if not found, the previous value otherwise
     */
    public int getPrevValue(int x) {
        IStateBinaryTree.Node n = btree.prevNode(x);
        if (n == null) {
            return Integer.MIN_VALUE;
        } else if (n.contains(x - 1)) {
            return x - 1;
        } else {
            return n.getSup();
        }

    }

    /**
     * Has this domain a value greater than the parameter
     * TODO: getSup is to slow for that kind of operation
     *
     * @param x integer value
     * @return wether the domain contains a greater value
     */
    public boolean hasNextValue(int x) {
        return x < getSup();
    }

    /**
     * Has this domain a value lower than the parameter
     * TODO: it is not efficient to call getInf
     *
     * @param x integer value
     * @return wether this domain contains a lower value
     */
    public boolean hasPrevValue(int x) {
        return x > getInf();
    }

    /**
     * Easy way to get a random value in the domain
     * Definitely not selected through an uniform distribution
     * TODO: Find a better way to select a value really at random
     *
     * @return a value selected at random in the domain
     */
    public int getRandomValue() {
        ArrayList<IStateBinaryTree.Node> tmp = new ArrayList<IStateBinaryTree.Node>(16);
        IStateBinaryTree.Node current = btree.getRoot();
        while (current != null) {
            tmp.add(current);
            if (random.nextBoolean()) {
                current = current.leftNode;

            } else {
                current = current.rightNode;
            }
        }
        IStateBinaryTree.Node selected = tmp.get(random.nextInt(tmp.size()));
        int val = random.nextInt(selected.sup - selected.inf + 1);
        return val + selected.inf;

    }


    /**
     * Check wether this domain is Enumerated or not
     *
     * @return true
     */
    public boolean isEnumerated() {
        return true;
    }

    /**
     * pretty print of the domain
     *
     * @return a new string
     */
    public String pretty() {
        return btree.toString();
    }

    public String toString() {
        return btree.toString();
    }
}