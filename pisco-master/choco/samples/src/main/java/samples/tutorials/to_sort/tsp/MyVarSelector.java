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

package samples.tutorials.to_sort.tsp;

import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.LinkedList;


public class MyVarSelector extends AbstractIntVarSelector {

    protected IntDomainVar[] vars;
    protected IntDomainVar objective;
    protected int src;
    protected int dest;

    public MyVarSelector(IntDomainVar objective, IntDomainVar[] vars, int src, int dest) {
        super(null, vars);
    	this.objective = objective;
    	this.src = src;
        this.dest = dest;
    }

    public MyVarSelector(IntDomainVar[] vars, int src, int dest) {
    	super(null, vars);
        this.src = src;
        this.dest = dest;
        this.objective = null;
    }

    public IntDomainVar selectVar() {
        int next = dfs();
        if (next == dest && vars[dest].isInstantiated()) {
            if (objective == null) return null;
            else {
                if (objective.isInstantiated()) return null;
                else return objective;
            }
        } else {
            return vars[next];
        }
    }

    private int dfs() {
        LinkedList toVisit = new LinkedList();
        LinkedList visited = new LinkedList();
        visited.offer(String.valueOf(src));
        toVisit.addFirst(String.valueOf(src));
        int lastVisited = src;
        while (!toVisit.isEmpty()) {
            int current = Integer.parseInt((String) toVisit.poll());
            lastVisited = current;
            if (vars[current].isInstantiated()) {
                int j = vars[current].getVal();
                if (!visited.contains(String.valueOf(j))) {
                    visited.offer(String.valueOf(j));
                    toVisit.addFirst(String.valueOf(j));
                }
            }
        }
        return lastVisited;
    }

}
