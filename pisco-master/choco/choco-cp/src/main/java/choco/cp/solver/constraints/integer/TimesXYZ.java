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

package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.AbstractTernIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * a constraint modelling X * Y = Z
 *
 * [BUG 3297805]: fix
 */
public final class TimesXYZ extends AbstractTernIntSConstraint {

    private static final int MAX = Integer.MAX_VALUE - 1, MIN = Integer.MIN_VALUE + 1;


    public TimesXYZ(IntDomainVar x, IntDomainVar y, IntDomainVar z) {
        super(x, y, z);
    }

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        return (tuple[0] * tuple[1] == tuple[2]);
    }

    @Override
    public String pretty() {
        return v0.pretty() + " * " + v1.pretty() + " = " + v2.pretty();
    }

    @Override
    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx == 0) {
            awakeOnX();
        } else if (idx == 1) {
            awakeOnY();
        } else if (idx == 2) {
            awakeOnZ();
            if (!(v2.canBeInstantiatedTo(0))) {
                int r = (int) Math.min(getZmax(), MAX);
                v2.updateSup(r, this, false);
            }
        }
    }

    @Override
    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx == 0) {
            awakeOnX();
        } else if (idx == 1) {
            awakeOnY();
        } else if (idx == 2) {
            awakeOnZ();
            if (!(v2.canBeInstantiatedTo(0))) {
                int r = (int) Math.max(getZmin(), MIN);
                v2.updateInf(r, this, false);
            }
        }
    }

    public void filter(int idx) throws ContradictionException {
        if (idx == 0) {
            awakeOnX();
        } else if (idx == 1) {
            awakeOnY();
        } else if (idx == 2) {
            awakeOnZ();
        }
    }

    @Override
    public void awakeOnInst(int vIdx) throws ContradictionException {
        filter(vIdx);
    }

    /**
     * reaction when X (v0) is updated
     *
     * @throws ContradictionException
     */
    protected void awakeOnX() throws ContradictionException {
        if (v0.isInstantiatedTo(0)) {
            v2.instantiate(0, this, false);
        }
        if ((v2.isInstantiatedTo(0)) && (!v0.canBeInstantiatedTo(0))) {
            v1.instantiate(0, this, false);
        } else if (!v2.canBeInstantiatedTo(0)) {
            updateYandX();
        } else if (!(v2.isInstantiatedTo(0))) {
            shaveOnYandX();
        }
        if (!(v2.isInstantiatedTo(0))) {
            int r = (int) Math.max(getZmin(), MIN);
            v2.updateInf(r, this, false);
            r = (int) Math.min(getZmax(), MAX);
            v2.updateSup(r, this, false);
        }
    }

    protected void awakeOnY() throws ContradictionException {
        if (v1.isInstantiatedTo(0)) {
            v2.instantiate(0, this, false);
        }
        if ((v2.isInstantiatedTo(0)) && (!v1.canBeInstantiatedTo(0))) {
            v0.instantiate(0, this, false);
        } else if (!v2.canBeInstantiatedTo(0)) {
            updateXandY();
        } else if (!(v2.isInstantiatedTo(0))) {
            shaveOnXandY();
        }
        if (!(v2.isInstantiatedTo(0))) {
            int r = (int) Math.max(getZmin(), MIN);
            v2.updateInf(r, this, false);
            r = (int) Math.min(getZmax(), MAX);
            v2.updateSup(r, this, false);
        }
    }

    protected void awakeOnZ() throws ContradictionException {
        if (!(v2.canBeInstantiatedTo(0))) {
            updateX();
            if (updateY()) {
                updateXandY();
            }
        } else if (!(v2.isInstantiatedTo(0))) {
            shaveOnX();
            if (shaveOnY()) {
                shaveOnXandY();
            }
        }
        if (v2.isInstantiatedTo(0)) {
            propagateZero();
        }
    }

    @Override
    public Boolean isEntailed() {
        if (this.isCompletelyInstantiated() && this.isSatisfied()) {
            return Boolean.TRUE;
        } else if (v2.isInstantiatedTo(0)) {
            if (v0.isInstantiatedTo(0) || v1.isInstantiatedTo(0)) {
                return Boolean.TRUE;
            } else if (!(v0.canBeInstantiatedTo(0)) && !(v1.canBeInstantiatedTo(0))) {
                return Boolean.FALSE;
            } else {
                return null;
            }
        } else if (!(v2.canBeInstantiatedTo(0))) {
            if (v0.getSup() < getXminIfNonZero()) {
                return Boolean.FALSE;
            } else if (v0.getInf() > getXmaxIfNonZero()) {
                return Boolean.FALSE;
            } else if (v1.getSup() < getYminIfNonZero()) {
                return Boolean.FALSE;
            } else if (v1.getInf() > getYmaxIfNonZero()) {
                return Boolean.FALSE;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    protected long getXminIfNonZero() {
        if ((v2.getInf() >= 0) && (v1.getInf() >= 0)) {
            return infCeilmM(v2, v1);
        } else if ((v2.getSup() <= 0) && (v1.getSup() <= 0)) {
            return infCeilMm(v2, v1);
        } else if ((v2.getInf() >= 0) && (v1.getSup() <= 0)) {
            return infCeilMM(v2, v1);
        } else if ((v2.getSup() <= 0) && (v1.getInf() >= 0)) {
            return infCeilmm(v2, v1);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getSup() <= 0)) {
            return infCeilMM(v2, v1);
        } else if ((v2.getSup() <= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return infCeilmP(v2);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getInf() >= 0)) {
            return infCeilmm(v2, v1);
        } else if ((v2.getInf() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return infCeilMN(v2);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return infCeilxx(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getXmaxIfNonZero() {
        if ((v2.getInf() >= 0) && (v1.getInf() >= 0)) {
            return supCeilMm(v2, v1);
        } else if ((v2.getSup() <= 0) && (v1.getSup() <= 0)) {
            return supCeilmM(v2, v1);
        } else if ((v2.getInf() >= 0) && (v1.getSup() <= 0)) {
            return supCeilmm(v2, v1);
        } else if ((v2.getSup() <= 0) && (v1.getInf() >= 0)) {
            return supCeilMM(v2, v1);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getSup() <= 0)) {
            return supCeilmM(v2, v1);
        } else if ((v2.getSup() <= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return supCeilmN(v2);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getInf() >= 0)) {
            return supCeilMm(v2, v1);
        } else if ((v2.getInf() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return supCeilMP(v2);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return supCeilEq(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getYminIfNonZero() {
        if ((v2.getInf() >= 0) && (v0.getInf() >= 0)) {
            return infCeilmM(v2, v0);
        } else if ((v2.getSup() <= 0) && (v0.getSup() <= 0)) {
            return infCeilMm(v2, v0);
        } else if ((v2.getInf() >= 0) && (v0.getSup() <= 0)) {
            return infCeilMM(v2, v0);
        } else if ((v2.getSup() <= 0) && (v0.getInf() >= 0)) {
            return infCeilmm(v2, v0);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getSup() <= 0)) {
            return infCeilMM(v2, v0);
        } else if ((v2.getSup() <= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
            return infCeilmP(v2);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getInf() >= 0)) {
            return infCeilmm(v2, v0);
        } else if ((v2.getInf() >= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
            return infCeilMN(v2);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
            return infCeilxx(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getYmaxIfNonZero() {
        if ((v2.getInf() >= 0) && (v0.getInf() >= 0)) {
            return supCeilMm(v2, v0);
        } else if ((v2.getSup() <= 0) && (v0.getSup() <= 0)) {
            return supCeilmM(v2, v0);
        } else if ((v2.getInf() >= 0) && (v0.getSup() <= 0)) {
            return supCeilmm(v2, v0);
        } else if ((v2.getSup() <= 0) && (v0.getInf() >= 0)) {
            return supCeilMM(v2, v0);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getSup() <= 0)) {
            return supCeilmM(v2, v0);
        } else if ((v2.getSup() <= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
            return supCeilmN(v2);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getInf() >= 0)) {
            return supCeilMm(v2, v0);
        } else if ((v2.getInf() >= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
            return supCeilMP(v2);
        } else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
            return supCeilEq(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getZmin() {
        if ((v0.getInf() >= 0) && (v1.getInf() >= 0)) {
            return infFloormm(v0, v1);
        } else if ((v0.getSup() <= 0) && (v1.getSup() <= 0)) {
            return infFloorMM(v0, v1);
        } else if ((v0.getInf() >= 0) && (v1.getSup() <= 0)) {
            return infFloorMm(v0, v1);
        } else if ((v0.getSup() <= 0) && (v1.getInf() >= 0)) {
            return infFloormM(v0, v1);
        } else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getSup() <= 0)) {
            return infFloorMm(v0, v1);
        } else if ((v0.getSup() <= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return infFloormM(v0, v1);
        } else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getInf() >= 0)) {
            return infFloormM(v0, v1);
        } else if ((v0.getInf() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return infFloorMm(v0, v1);
        } else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return infFloorxx(v0, v1);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getZmax() {
        if ((v0.getInf() >= 0) && (v1.getInf() >= 0)) {
            return supFloorMM(v0, v1);
        } else if ((v0.getSup() <= 0) && (v1.getSup() <= 0)) {
            return supFloormm(v0, v1);
        } else if ((v0.getInf() >= 0) && (v1.getSup() <= 0)) {
            return supFloormM(v0, v1);
        } else if ((v0.getSup() <= 0) && (v1.getInf() >= 0)) {
            return supFloorMm(v0, v1);
        } else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getSup() <= 0)) {
            return supFloormm(v0, v1);
        } else if ((v0.getSup() <= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return supFloormm(v0, v1);
        } else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getInf() >= 0)) {
            return supFloorMM(v0, v1);
        } else if ((v0.getInf() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return supFloorMM(v0, v1);
        } else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
            return supFloorEq(v0, v1);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    private long infFloormm(IntDomainVar b, IntDomainVar c) {
        return b.getInf() * c.getInf();
    }

    private long infFloormM(IntDomainVar b, IntDomainVar c) {
        return b.getInf() * c.getSup();
    }

    private long infFloorMm(IntDomainVar b, IntDomainVar c) {
        return b.getSup() * c.getInf();
    }

    private long infFloorMM(IntDomainVar b, IntDomainVar c) {
        return b.getSup() * c.getSup();
    }

    private long supFloormm(IntDomainVar b, IntDomainVar c) {
        return b.getInf() * c.getInf();
    }

    private long supFloormM(IntDomainVar b, IntDomainVar c) {
        return b.getInf() * c.getSup();
    }

    private long supFloorMm(IntDomainVar b, IntDomainVar c) {
        return b.getSup() * c.getInf();
    }

    private long supFloorMM(IntDomainVar b, IntDomainVar c) {
        return b.getSup() * c.getSup();
    }

    private int getNonZeroSup(IntDomainVar v) {
        return Math.min(v.getSup(), -1);
    }

    private int getNonZeroInf(IntDomainVar v) {
        return Math.max(v.getInf(), 1);
    }

    private long infCeilmm(IntDomainVar b, IntDomainVar c) {
        return MathUtils.divCeil(b.getInf(), getNonZeroInf(c));
    }

    private long infCeilmM(IntDomainVar b, IntDomainVar c) {
        return MathUtils.divCeil(getNonZeroInf(b), c.getSup());
    }

    private long infCeilMm(IntDomainVar b, IntDomainVar c) {
        return MathUtils.divCeil(getNonZeroSup(b), c.getInf());
    }

    private long infCeilMM(IntDomainVar b, IntDomainVar c) {
        return MathUtils.divCeil(b.getSup(), getNonZeroSup(c));
    }

    private long infCeilmP(IntDomainVar b) {
        return MathUtils.divCeil(b.getInf(), 1);
    }

    private long infCeilMN(IntDomainVar b) {
        return MathUtils.divCeil(b.getSup(), -1);
    }

    private long supCeilmm(IntDomainVar b, IntDomainVar c) {
        return MathUtils.divFloor(getNonZeroInf(b), c.getInf());
    }

    private long supCeilmM(IntDomainVar b, IntDomainVar c) {
        return MathUtils.divFloor(b.getInf(), getNonZeroSup(c));
    }

    private long supCeilMm(IntDomainVar b, IntDomainVar c) {
        return MathUtils.divFloor(b.getSup(), getNonZeroInf(c));
    }

    private long supCeilMM(IntDomainVar b, IntDomainVar c) {
        return MathUtils.divFloor(getNonZeroSup(b), c.getSup());
    }

    private long supCeilmN(IntDomainVar b) {
        return MathUtils.divFloor(b.getInf(), -1);
    }

    private long supCeilMP(IntDomainVar b) {
        return MathUtils.divFloor(b.getSup(), 1);
    }

    private long infFloorxx(IntDomainVar b, IntDomainVar c) {
        long s1 = b.getInf() * c.getSup();
        long s2 = b.getSup() * c.getInf();
        if (s1 < s2) {
            return b.getInf() * c.getSup();
        } else {
            return b.getSup() * c.getInf();
        }
    }

    private long supFloorEq(IntDomainVar b, IntDomainVar c) {
        if (b.getInf() * c.getInf() > b.getSup() * c.getSup()) {
            return b.getInf() * c.getInf();
        } else {
            return b.getSup() * c.getSup();
        }
    }

    private long infCeilxx(IntDomainVar b) {
        return Math.min(MathUtils.divCeil(b.getInf(), 1), MathUtils.divCeil(b.getSup(), -1));
    }  //v0.18

    private long supCeilEq(IntDomainVar b) {
        return Math.max(MathUtils.divFloor(b.getInf(), -1), MathUtils.divFloor(b.getSup(), 1));
    }   //v0.18

    @Override
    public void awake() throws ContradictionException {
        propagate();
    }

    @Override
    public void propagate() throws ContradictionException {
        filter(0);
        filter(1);
        filter(2);
    }

    /**
     * propagate the fact that v2 (Z) is instantiated to 0
     *
     * @throws ContradictionException
     */
    public void propagateZero() throws ContradictionException {
        if (!(v1.canBeInstantiatedTo(0))) {
            v0.instantiate(0, this, false);
        }
        if (!(v0.canBeInstantiatedTo(0))) {
            v1.instantiate(0, this, false);
        }
    }

    /**
     * Updating X and Y when Z cannot be 0
     */
    protected boolean updateX() throws ContradictionException {
        int r = (int) Math.max(getXminIfNonZero(), MIN);
        boolean infChange = v0.updateInf(r, this, false);
        r = (int) Math.min(getXmaxIfNonZero(), MAX);
        boolean supChange = v0.updateSup(r, this, false);
        return (infChange || supChange);
    }

    protected boolean updateY() throws ContradictionException {
        int r = (int) Math.max(getYminIfNonZero(), MIN);
        boolean infChange = v1.updateInf(r, this, false);
        r = (int) Math.min(getYmaxIfNonZero(), MAX);
        boolean supChange = v1.updateSup(r, this, false);
        return (infChange || supChange);
    }

    /**
     * loop until a fix point is reach (see testProd14)
     */
    protected void updateXandY() throws ContradictionException {
        while (updateX() && updateY()) {
            ;
        }
    }

    protected void updateYandX() throws ContradictionException {
        while (updateY() && updateX()) {
            ;
        }
    }

    /**
     * Updating X and Y when Z can  be 0
     */

    protected boolean shaveOnX() throws ContradictionException {
        int xmin = (int) Math.max(getXminIfNonZero(), MIN);
        int xmax = (int) Math.min(getXmaxIfNonZero(), MAX);
        if ((xmin > v0.getSup()) || (xmax < v0.getInf())) {
            v2.instantiate(0, this, false);
            propagateZero();    // make one of X,Y be 0 if the other cannot be
            return false;       //no more shaving need to be performed
        } else {
            boolean infChange = (!(v1.canBeInstantiatedTo(0)) && v0.updateInf(Math.min(0, xmin), this, false));
            boolean supChange = (!(v1.canBeInstantiatedTo(0)) && v0.updateSup(Math.max(0, xmax), this, false));
            return (infChange || supChange);
        }
    }

    protected boolean shaveOnY() throws ContradictionException {
        int ymin = (int) Math.max(getYminIfNonZero(), MIN);
        int ymax = (int) Math.min(getYmaxIfNonZero(), MAX);
        if ((ymin > v1.getSup()) || (ymax < v1.getInf())) {
            v2.instantiate(0, this, false);
            propagateZero();    // make one of X,Y be 0 if the other cannot be
            return false;       //no more shaving need to be performed
        } else {
            boolean infChange = (!(v0.canBeInstantiatedTo(0)) && v1.updateInf(Math.min(0, ymin), this, false));
            boolean supChange = (!(v0.canBeInstantiatedTo(0)) && v1.updateSup(Math.max(0, ymax), this, false));
            return (infChange || supChange);
        }
    }

    protected void shaveOnXandY() throws ContradictionException {
        while (shaveOnX() && shaveOnY()) {
        }
    }

    protected void shaveOnYandX() throws ContradictionException {
        while (shaveOnY() && shaveOnX()) {
        }
    }

}
