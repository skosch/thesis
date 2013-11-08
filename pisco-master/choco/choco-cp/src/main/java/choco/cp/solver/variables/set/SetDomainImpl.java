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

package choco.cp.solver.variables.set;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.variables.set.SetDomain;
import choco.kernel.solver.variables.set.SetSubDomain;
import choco.kernel.solver.variables.set.SetVar;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 6 juin 2004
 * Time: 14:20:54
 */
public final class SetDomainImpl implements SetDomain {

    private final PropagationEngine propagationEngine;

    private final SetVar variable;

    private final BitSetEnumeratedDomain kernel;

    private final BitSetEnumeratedDomain enveloppe;

    private SetDomainIterator _kiterator, _eiterator;

    protected SetOpenDomainIterator _oiterator;

    public SetDomainImpl(final SetVar v, final int a, final int b, final IEnvironment environment, final PropagationEngine propagationEngine) {
        variable = v;
        kernel = new BitSetEnumeratedDomain(v, a, b, false, environment);
        enveloppe = new BitSetEnumeratedDomain(v, a, b, true, environment);
        this.propagationEngine = propagationEngine;
    }

    public SetDomainImpl(final SetVar v, final int[] sortedValues, final IEnvironment environment, final PropagationEngine propagationEngine) {
        variable = v;
        kernel = new BitSetEnumeratedDomain(v, sortedValues, false, environment);
        enveloppe = new BitSetEnumeratedDomain(v, sortedValues, true, environment);
        this.propagationEngine = propagationEngine;
    }

    /**
     * Constructor of set var, allow creation of constant set var and empty set var.
     *
     * @param v
     * @param sortedValues      values of the set var. If null or lenght=0 => empty set
     * @param constant          if true, build a constant set var
     * @param environment
     * @param propagationEngine
     */
    public SetDomainImpl(final SetVar v, final int[] sortedValues, final boolean constant, final IEnvironment environment, final PropagationEngine propagationEngine) {
        variable = v;
        if (sortedValues.length > 0) {
            kernel = new BitSetEnumeratedDomain(v, sortedValues, constant, environment);
            enveloppe = new BitSetEnumeratedDomain(v, sortedValues, true, environment);
        } else {
            kernel = BitSetEnumeratedDomain.empty(v, environment);
            enveloppe = BitSetEnumeratedDomain.empty(v, environment);
        }
        this.propagationEngine = propagationEngine;
    }


    public SetSubDomain getKernelDomain() {
        return kernel;
    }

    public SetSubDomain getEnveloppeDomain() {
        return enveloppe;
    }

    public boolean addToKernel(final int x) {
        kernel.add(x);
        return true;
    }

    public boolean isInstantiated() {
        return kernel.getSize() == enveloppe.getSize();
    }

    boolean isInstantiatedTo(final int[] setVal) {
        if (setVal.length == kernel.getSize() && setVal.length == enveloppe.getSize()) {
            for (int i = 0; i < setVal.length; i++) {
                if (!kernel.contains(setVal[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    //TODO : a bitset instead of a int[] ?

    boolean canBeInstantiatedTo(final int[] setVal) {
        if (kernel.getSize() <= setVal.length && enveloppe.getSize() >= setVal.length) {
            Arrays.sort(setVal);   // TODO : can we suppose that the table is sorted ?
            for (int i = 0; i < setVal.length; i++) {
                if (!enveloppe.contains(setVal[i])) {
                    return false;
                }
            }
            for (int i = kernel.getFirstVal(); i >= 0; i = kernel.getNextValue(i)) {
                if (Arrays.binarySearch(setVal, i) < 0) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }

    }

    public String toString() {
        final StringBuilder buf = new StringBuilder("{Env[");
        int count = 0;
        DisposableIntIterator it = this.getEnveloppeIterator();
        while (it.hasNext()) {
            final int val = it.next();
            count += 1;
            if (count > 1) {
                buf.append(',');
            }
            buf.append(val);
        }
        it.dispose();
        buf.append("], Ker[");
        count = 0;
        it = this.getKernelIterator();
        while (it.hasNext()) {
            final int val = it.next();
            count += 1;
            if (count > 1) {
                buf.append(',');
            }
            buf.append(val);
        }
        it.dispose();
        buf.append("]}");
        return buf.toString();
    }

    public String pretty() {
        return toString();
    }
    // ============================================
    // methods for posting propagation events and
    // maintaining the domain.
    // ============================================

    // Si promotion, il faut annuler la cause

    public boolean remFromEnveloppe(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        if (_remFromEnveloppe(x, cause)) {
            if (isInstantiated()) {
                propagationEngine.postInstSet(variable, cause, forceAwake);
            } else {
                propagationEngine.postRemEnv(variable, cause, forceAwake);
            }
            return true;
        }
        return false;
    }

    // Si promotion, il faut annuler la cause

    public boolean addToKernel(final int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        if (_addToKernel(x, cause)) {
            if (isInstantiated()) {
                propagationEngine.postInstSet(variable, cause, forceAwake);
            } else {
                propagationEngine.postAddKer(variable, cause, forceAwake);
            }
            return true;
        }
        return false;
    }

    public boolean instantiate(final int[] x, final SConstraint cause, final boolean forceAwake) throws ContradictionException {
        if (_instantiate(x, cause)) {
            propagationEngine.postInstSet(variable, cause, forceAwake);
            return true;
        } else {
            return false;
        }
    }

    // Si promotion, il faut annuler la cause

    boolean _remFromEnveloppe(final int x, final SConstraint cause) throws ContradictionException {
        if (kernel.contains(x)) {
            propagationEngine.raiseContradiction(cause);
            return true; // just for compilation
        } else if (enveloppe.contains(x)) {
            enveloppe.remove(x);
            return true;
        }
        return false;
    }

    // Si promotion, il faut annuler la cause

    boolean _addToKernel(final int x, final SConstraint cause) throws ContradictionException {
        if (!enveloppe.contains(x)) {
            propagationEngine.raiseContradiction(cause);
            return true; // just for compilation
        } else if (!kernel.contains(x)) {
            kernel.add(x);
            return true;
        }
        return false;
    }

    boolean _instantiate(final int[] values, final SConstraint cause) throws ContradictionException {
        if (isInstantiated()) {
            if (!isInstantiatedTo(values)) {
                propagationEngine.raiseContradiction(cause);
                return true; // just for compilation
            } else {
                return false;
            }
        } else {
            if (!canBeInstantiatedTo(values)) {
                propagationEngine.raiseContradiction(cause);
                return true; // just for compilation
            } else {
                for (int i = 0; i < values.length; i++) { // TODO: ajouter un restrict(int[] val) dans le BitSetEnumeratedDomain
                    kernel.add(values[i]);
                }
                for (int i = enveloppe.getFirstVal(); i >= 0; i = enveloppe.getNextValue(i)) {
                    if (!kernel.contains(i)) {
                        enveloppe.remove(i);
                    }
                }
                return true;
            }
        }
    }

    // ============================================
    // Iterators on kernel and enveloppe.
    // ============================================

    public DisposableIntIterator getKernelIterator() {
//        return SetDomainIterator.getIterator(this.kernel);
        if (_kiterator == null) {
            _kiterator = new SetDomainIterator();
        }else if (!_kiterator.reusable()) {
//            assert false;
            _kiterator = new SetDomainIterator();
        }
        _kiterator.init(kernel);
        return _kiterator;

    }

    public DisposableIntIterator getEnveloppeIterator() {
        if (_eiterator == null) {
            _eiterator = new SetDomainIterator();
        }else if (!_eiterator.reusable()) {
//            assert false;
            _eiterator = new SetDomainIterator();
        }
        _eiterator.init(enveloppe);
        return _eiterator;
    }

    public DisposableIntIterator getOpenDomainIterator() {
        if (_oiterator == null) {
            _oiterator = new SetOpenDomainIterator();
        }else if (!_oiterator.reusable()) {
//            assert false;
            _oiterator = new SetOpenDomainIterator();
        }
        _oiterator.init(enveloppe, kernel);
        return _oiterator;

    }

    protected static final class SetOpenDomainIterator extends DisposableIntIterator {

        private BitSetEnumeratedDomain envdomain;
        private BitSetEnumeratedDomain kerdomain;
        private int currentValue = Integer.MIN_VALUE;
        private int nbValueToBeIterated = Integer.MAX_VALUE;

        private SetOpenDomainIterator() {
        }

        public void init(final BitSetEnumeratedDomain dom1, final BitSetEnumeratedDomain dom2) {
            super.init();
            envdomain = dom1;
            kerdomain = dom2;
            currentValue = Integer.MIN_VALUE;
            nbValueToBeIterated = dom1.getSize() - dom2.getSize();
        }

        public boolean hasNext() {
            return nbValueToBeIterated > 0;
        }

        public int next() {
            int i = (currentValue == Integer.MIN_VALUE) ? envdomain.getFirstVal() : envdomain.getNextValue(currentValue);
            for (; i >= 0; i = envdomain.getNextValue(i)) {
                if (!kerdomain.contains(i)) {
                    currentValue = i;
                    break;
                }
            }
            nbValueToBeIterated -= 1;
            return currentValue;
        }

        public void remove() {
            if (currentValue == Integer.MIN_VALUE) {
                throw new IllegalStateException();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
