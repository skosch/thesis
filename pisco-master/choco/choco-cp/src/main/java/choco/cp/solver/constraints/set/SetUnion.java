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

package choco.cp.solver.constraints.set;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.cp.solver.variables.set.SetVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractTernSetSConstraint;
import choco.kernel.solver.variables.set.SetVar;

/**
 * A constraint stating that a set is the union of two others
 * There are seven propagation rules for the constraint sv3 = union(sv1, sv2)
 * Ker(sv1) contains Ker(sv3)
 * Ker(sv2) contains Ker(sv3)
 * Ker(sv3) contains (Ker(sv1) inter Ker(sv2))
 * Env(v3)  disjoint Complement(Env(v1))
 * Env(v3)  disjoint Complement(Env(v2))
 * Env(v2)  disjoint Ker(v1) inter Complement(Env(v3))
 * Env(v1)  disjoint Ker(v2) inter Complement(Env(v3))
 */
public final class SetUnion extends AbstractTernSetSConstraint {

    /**
     * Enforce sv3 to be the union of sv1 and sv2
     *
     * @param sv1 the first set
     * @param sv2 the second set
     * @param sv3 the union set
     */

    public SetUnion(SetVar sv1, SetVar sv2, SetVar sv3) {
        super(sv1, sv2, sv3);
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx == 0) {
            return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK + IntVarEvent.REMVAL_MASK;
        }
        return SetVarEvent.ADDKER_MASK + SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void awakeOnKer(int varIdx, int x) throws ContradictionException {
        switch (varIdx) {
            case 0:
                v2.addToKernel(x, this, false);
                break;
            case 1:
                v2.addToKernel(x, this, false);
                break;
            case 2:
                if (!v0.isInDomainEnveloppe(x)) v1.addToKernel(x, this, false);
                if (!v1.isInDomainEnveloppe(x)) v0.addToKernel(x, this, false);
                break;
            default:
                break;
        }

    }

    public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
        switch (varIdx) {
            case 0:
                if (!v1.isInDomainEnveloppe(x))
                    v2.remFromEnveloppe(x, this, false);
                break;
            case 1:
                if (!v0.isInDomainEnveloppe(x))
                    v2.remFromEnveloppe(x, this, false);
                break;
            case 2:
                v0.remFromEnveloppe(x, this, false);
                v1.remFromEnveloppe(x, this, false);
                break;
            default:
                break;
        }
    }

    public void awakeOnInst(int varIdx) throws ContradictionException {
        switch (varIdx) {
            case 0:
                DisposableIntIterator it1 = v0.getDomain().getKernelIterator();
                try {
                    while (it1.hasNext()) {
                        int val = it1.next();
                        v2.addToKernel(val, this, false);
                    }
                } finally {
                    it1.dispose();
                }
                it1 = v2.getDomain().getEnveloppeIterator();
                try {
                    while (it1.hasNext()) {
                        int val = it1.next();
                        if (!v0.isInDomainEnveloppe(val) && !v1.isInDomainEnveloppe(val))
                            v2.remFromEnveloppe(val, this, false);
                    }
                } finally {
                    it1.dispose();
                }
                break;
            case 1:
                DisposableIntIterator it2 = v1.getDomain().getKernelIterator();
                try {
                    while (it2.hasNext()) {
                        int val = it2.next();
                        v2.addToKernel(val, this, false);
                    }
                } finally {
                    it2.dispose();
                }

                it2 = v2.getDomain().getEnveloppeIterator();
                try {
                    while (it2.hasNext()) {
                        int val = it2.next();
                        if (!v0.isInDomainEnveloppe(val) && !v1.isInDomainEnveloppe(val))
                            v2.remFromEnveloppe(val, this, false);
                    }
                } finally {
                    it2.dispose();
                }
                break;
            case 2:
                DisposableIntIterator it3 = v2.getDomain().getKernelIterator();
                try {
                    while (it3.hasNext()) {
                        int val = it3.next();
                        if (!v0.isInDomainEnveloppe(val)) v1.addToKernel(val, this, false);
                        if (!v1.isInDomainEnveloppe(val)) v0.addToKernel(val, this, false);
                    }
                } finally {
                    it3.dispose();
                }
                break;
            default:
                break;
        }
    }

    public void propagate() throws ContradictionException {

        DisposableIntIterator it1 = v0.getDomain().getKernelIterator();
        try {
            while (it1.hasNext()) {
                int val = it1.next();
                v2.addToKernel(val, this, false);
            }
        } finally {
            it1.dispose();
        }
        it1 = v1.getDomain().getKernelIterator();
        try {
            while (it1.hasNext()) {
                int val = it1.next();
                v2.addToKernel(val, this, false);
            }
        } finally {
            it1.dispose();
        }

        it1 = v2.getDomain().getKernelIterator();
        try {
            while (it1.hasNext()) {
                int val = it1.next();
                if (!v0.isInDomainEnveloppe(val)) v1.addToKernel(val, this, false);
                if (!v1.isInDomainEnveloppe(val)) v0.addToKernel(val, this, false);
            }
        } finally {
            it1.dispose();
        }

        it1 = v2.getDomain().getEnveloppeIterator();
        try {
            while (it1.hasNext()) {
                int val = it1.next();
                if (!v0.isInDomainEnveloppe(val) && !v1.isInDomainEnveloppe(val)) {
                    v2.remFromEnveloppe(val, this, false);
                }
            }
        } finally {
            it1.dispose();
        }
    }

    public String toString() {
        return v0 + " UNION " + v1 + " = " + v2;
    }

    public String pretty() {
        return v0.pretty() + " UNION " + v1.pretty() + " = " + v2.pretty();
    }

    public boolean isSatisfied() {
        boolean allin = true;
        DisposableIntIterator it = v2.getDomain().getKernelIterator();
        try {
            while (it.hasNext()) {
                int val = it.next();
                if (!v0.isInDomainKernel(val) && !v1.isInDomainKernel(val)) {
                    allin = false;
                }
            }
        } finally {
            it.dispose();
        }
        if (!allin) return false;
        it = v1.getDomain().getKernelIterator();
        try {
            while (it.hasNext()) {
                int val = it.next();
                if (!v2.isInDomainKernel(val)) {
                    allin = false;
                }
            }
        } finally {
            it.dispose();
        }
        if (!allin) return false;
        it = v0.getDomain().getKernelIterator();
        try {
            while (it.hasNext()) {
                int val = it.next();
                if (!v2.isInDomainKernel(val)) {
                    allin = false;
                }
            }
        } finally {
            it.dispose();
        }
        return allin;
    }

    public boolean isConsistent() {
        // TODO
        return false;
    }
}