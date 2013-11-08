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

package choco.cp.common.util.preprocessor.merger;

import choco.Options;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import gnu.trove.THashSet;

import java.util.Set;

public final class IntegerVariableMerger {
    public int low;
    public int upp;
    // values is null if domain is bounded
    public int[] values;
    public Set<String> optionsSet;

    public IntegerVariableMerger() {
        optionsSet = new THashSet<String>();
    }

    public IntegerVariableMerger(final IntegerVariable v) {
        this();
        low = v.getLowB();
        upp = v.getUppB();
        optionsSet.addAll(v.getOptions());
    }

    public void copy(final IntegerVariableMerger d) {
        low = d.low;
        upp = d.upp;
        if (d.values != null) {
            values = new int[d.values.length];
            System.arraycopy(d.values, 0, values, 0, values.length);
        }
        optionsSet = d.optionsSet;
    }

    public IntegerVariable create(){
        if(values != null){
           return new IntegerVariable(StringUtils.randomName(), values);
        }else{
            return new IntegerVariable(StringUtils.randomName(), low, upp);
        }
    }

    private int[] enumVal() {
        if (values != null) {
            if (values.length == 2 && values[0] == values[1]) {
                return new int[]{values[0]};
            }
            return values;
        } else {
            final int[] val = new int[upp - low + 1];
            for (int i = 0; i < val.length; i++) {
                val[i] = low + i;
            }
            return val;
        }
    }

    /**
     * intersection of the current domain and v
     * @param v the variable to intersect with
     * @return true if the two domains intersect
     */
    public boolean intersection(final IntegerVariable v) {
        if (v.getValues() == null && this.values == null) {
            this.low = Math.max(this.low, v.getLowB());
            this.upp = Math.min(this.upp, v.getUppB());
            if (low > upp) {
                return false;
            }
        } else {
            final int[] val = new int[Math.min((this.upp - this.low + 1), v.getDomainSize())];
            int size = 0;
            final int[] ev1 = this.enumVal();
            final int[] ev2 = v.enumVal();
            for (final int anEv1 : ev1) {
                for (final int anEv2 : ev2) {
                    if (anEv1 == anEv2) {
                        val[size++] = anEv1;
                        break;
                    }
                }
            }
            //<cpru> bidouille...
            if (size > 0) {
                values = new int[size];
                System.arraycopy(val, 0, values, 0, size--);
                this.low = values[0];
                this.upp = values[size];
            } else {
                return false;
            }
        }
        this.optionsSet = mergeOptions(v);
        return true;
    }

    private THashSet<String> mergeOptions(final IntegerVariable v) {
        final THashSet<String> toptionsSet = new THashSet<String>();
        if (v.getOptions().contains(Options.V_NO_DECISION)
                || optionsSet.contains(Options.V_NO_DECISION)) {
            toptionsSet.add(Options.V_NO_DECISION);
        }
        if (v.getOptions().contains(Options.V_OBJECTIVE)
                || optionsSet.contains(Options.V_OBJECTIVE)) {
            toptionsSet.add(Options.V_OBJECTIVE);
        }
        // Type copy
        if (v.getOptions().contains(Options.V_BTREE)
                || optionsSet.contains(Options.V_BTREE)) {
            toptionsSet.add(Options.V_BTREE);
        } else if (v.getOptions().contains(Options.V_ENUM)
                || optionsSet.contains(Options.V_ENUM)) {
            toptionsSet.add(Options.V_ENUM);
        } else if (v.getOptions().contains(Options.V_BLIST)
                || optionsSet.contains(Options.V_BLIST)) {
            toptionsSet.add(Options.V_BLIST);
        } else if (v.getOptions().contains(Options.V_LINK)
                || optionsSet.contains(Options.V_LINK)) {
            toptionsSet.add(Options.V_LINK);
        } else if (v.getOptions().contains(Options.V_BOUND)
                || optionsSet.contains(Options.V_BOUND)) {
            toptionsSet.add(Options.V_BOUND);
        }
        return toptionsSet;
    }
}