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

package choco.cp.solver.constraints.global;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : xlorca
 * Mail : xlorca(a)emn.fr
 * Date : 29 janv. 2010
 * Since : Choco 2.1.1
 */
public final class OrderedSparseArray {

    int[][] values;
    int[] nbVals;
    int[][] infos;
    int[] indices;
    boolean[] dirs; // dirs[i] true => increasing order, otherwise decreasing order
    int[] defaults;
    int[] previous;
    int n;
    boolean type; // true si minS maxS ou minP maxP otherwise false

    public OrderedSparseArray(final int n) {
        this(n, false);
    }

    public OrderedSparseArray(final int n, final boolean type) {
        this.n = n;
        this.type = type;
        this.values = new int[n][];
        this.nbVals = new int[n];
        this.infos = new int[n][];
        this.indices = new int[n];
        this.dirs = new boolean[n];
        this.defaults = new int[n];
        this.previous = new int[n];
    }

    public void allocate(final IntDomainVar[] vars, final int def) {
        for (int i = 0; i < n; i++) {
            values[i] = new int[vars[i].getDomainSize()];
            nbVals[i] = vars[i].getDomainSize();
            final DisposableIntIterator it = vars[i].getDomain().getIterator();
            int j = 0;
            while (it.hasNext()) {
                values[i][j] = it.next();
                j++;
            }
            it.dispose();
            infos[i] = new int[vars[i].getDomainSize()];
            for (j = 0; j < nbVals[i]; j++) {
                infos[i][j] = def;
            }
            defaults[i] = def;
        }
    }

    public void scanInit(final int i, final boolean dir) {
        dirs[i] = dir;
        previous[i] = defaults[i];
        if (dir) {
            indices[i] = -1;
        } else {
            indices[i] = nbVals[i];
        }
    }

    public int get(final int i, final int v) {
        if (dirs[i]) {
            while (indices[i] == -1 || (indices[i] <= nbVals[i] - 1 && v > values[i][indices[i]])) {
                if (type && indices[i] >= 0) {
                    previous[i] = infos[i][indices[i]];
                }
                indices[i]++;
            }
        } else {
            while (indices[i] == nbVals[i] || (indices[i] >= 0 && v < values[i][indices[i]])) {
                if (type && indices[i] < nbVals[i]) {
                    previous[i] = infos[i][indices[i]];
                }
                indices[i]--;
            }
        }
        if ((dirs[i] && (indices[i] > nbVals[i] - 1 || v < values[i][indices[i]])) || (!dirs[i] && (indices[i] < 0 || v > values[i][indices[i]]))) {
            if (!type) { // cas infSuffix supSuffix infPrefix supPrefix
                previous[i] = defaults[i];
                return defaults[i];
            } else {
                return previous[i];
            }
        } else {
            previous[i] = infos[i][indices[i]];
            return infos[i][indices[i]];
        }
    }

    public void set(final int i, final int v, final int info) {
        if (dirs[i]) {
            //assert(!(indices[i] >= 0 && values[i][indices[i]] > v));
            if (indices[i] == -1 || values[i][indices[i]] < v) {
                if (type && indices[i] >= 0) {
                    previous[i] = infos[i][indices[i]];
                }
                indices[i]++;
                //assert(indices[i] <= nbVals[i]+1);
                values[i][indices[i]] = v;
            }
        } else {
            //assert(!(indices[i] < nbVals[i]-1 && values[i][indices[i]] < v));
            if (indices[i] == nbVals[i] || values[i][indices[i]] > v) {
                if (type && indices[i] < nbVals[i]) {
                    previous[i] = infos[i][indices[i]];
                }
                indices[i]--;
                //assert(indices[i] >= 0);
                values[i][indices[i]] = v;
            }
        }
        infos[i][indices[i]] = info;
        previous[i] = info;
    }

    public String printer(final String name) {
        final StringBuilder s = new StringBuilder();
        s.append("name | coords | value | info\n");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < values[i].length; j++) {
                s.append(name).append(" | [").append(i).append("][").append(j).append("] |   ")
                        .append(values[i][j]).append("   | ").append(infos[i][j]).append('\n');
            }
        }
        return s.toString();
    }

}