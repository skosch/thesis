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

package choco.cp.solver.constraints.global.tree.filtering.structuralFiltering.precedences;


import choco.kernel.memory.IStateBitSet;

import java.util.BitSet;


public class TopologicSort {

    protected BitSet[] RGS;
    protected int[] numTable;
    protected BitSet sorted;

    public TopologicSort(IStateBitSet[] rgs) {
        this.RGS = new BitSet[rgs.length];
        for (int i = 0; i < rgs.length; i++) {
            RGS[i] = rgs[i].copyToBitSet();
        }
        this.numTable = new int[RGS.length];
        for (int i = 0; i < numTable.length; i++) numTable[i] = -1;
        this.sorted = new BitSet(RGS.length);
    }

    public int[] sort() {
        exec_sort(0);
        return numTable;
    }

    protected void exec_sort(int lvl) {
        BitSet sources = getSources();
        if (sources.cardinality() > 0) {
            for (int i = sources.nextSetBit(0); i >= 0; i = sources.nextSetBit(i + 1)) {
                numTable[i] = lvl;
                sorted.set(i, true);
                updateRGS(i);
            }
            lvl++;
            exec_sort(lvl);
        }
    }

    protected void updateRGS(int v) {
        for (int i = 0; i < RGS.length; i++) {
            if (i != v) {
                for (int j = RGS[i].nextSetBit(0); j >= 0; j = RGS[i].nextSetBit(j + 1)) {
                    if (j == v) RGS[i].set(j, true);
                }
            }
        }
        RGS[v].clear();
    }

    protected BitSet getSources() {
        BitSet res = new BitSet(RGS.length);
        for (int i = 0; i < RGS.length; i++) {
            if (!sorted.get(i)) {
                boolean src = true;
                for (BitSet aRGS : RGS) {
                    if (aRGS.get(i)) src = false;
                }
                if (src) res.set(i, true);
            }
        }
        return res;
    }
}
