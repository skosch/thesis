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
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.AbstractTernIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import static java.lang.Math.*;
/*
 * User : Charles Prud'homme
 * Mail : cprudhom(a)emn.fr
 * Date : 15/12/2008
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 *
 * A constraint modelling X / Y = Z
 * It is based on the following reasonning:
 *  z = x/y - r/y   where 0<=r<|y|
 *
 * So:
 * x = y.z + r
 * y = x/z + r/z
 *
 */
public final class EuclideanDivisionXYZ extends AbstractTernIntSConstraint {

    private boolean changeX, changeY, changeZ = false;


    public EuclideanDivisionXYZ(IntDomainVar x, IntDomainVar y, IntDomainVar z) {
        super(x, y, z);
    }

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.BOUNDS_MASK;
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        return (tuple[0] / tuple[1] == tuple[2]);
    }

    @Override
    public String pretty() {
        return v0.pretty() + " / " + v1.pretty() + " = " + v2.pretty();
    }

    /**
     * <i>Propagation:</i>
     * Propagating the constraint until local consistency is reached.
     *
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction exception
     */
    @Override
    public void propagate() throws ContradictionException {
        if(v1.isInstantiatedTo(0)){
            fail();
        }
        if(v0.isInstantiated() && v1.isInstantiated() && v2.isInstantiated()){
            if(v2.getVal() != v0.getVal() / v1.getVal()){
                fail();
            }
        }else{
            do{
                filterOnX();
                filterOnY();
                filterOnZ();
            }while(changeX || changeY || changeZ);
        }
    }

    /**
     * Default propagation on improved lower bound: propagation on domain revision.
     */
    @Override
    public void awakeOnInf(int varIdx) throws ContradictionException {
        filter(varIdx);
    }

    /**
     * Default propagation on improved upper bound: propagation on domain revision.
     */
    @Override
    public void awakeOnSup(int varIdx) throws ContradictionException {
        filter(varIdx);
    }

    public void awakeOnBounds(int varIndex) throws ContradictionException {
        filter(varIndex);
    }

    private void filter(int ind) throws ContradictionException {
        switch (ind){
            case 0:
                 do{
                    filterOnY();
                    filterOnZ();
                }while(changeY || changeZ);
                break;
            case 1:
                do{
                    filterOnX();
                    filterOnZ();
                }while(changeX || changeZ);
                break;
            case 2:
                do{
                    filterOnX();
                    filterOnY();
                }while(changeX || changeY);
                break;
            default:
                throw new SolverException("Unknown case for IntDiv");
        }

    }

    private void filterOnX() throws ContradictionException {
        changeX = false;
        changeX = v0.updateInf(getLowerBoundX(), this, false);
        changeX = changeX || v0.updateSup(getUpperBoundX(), this, false);
    }

    /**
     * Compute the new lower bound of X with
     * the domain of Y and Z.
     * @return the new lower bound of X
     */
    private int getLowerBoundX() {
        if(v2.isInstantiatedTo(0)){
            if(v1.getInf()>=0){
                return -v1.getSup()+1;
            }else
            if(v1.getSup()<=0){
                return v1.getInf()+1;
            }else
            if(v1.getInf()<0 && v1.getSup() > 0){
                return -max(abs(v1.getInf()), v1.getSup())+1;
            }
        }else
        if(v2.getInf() >= 0){
            if(v1.getInf()>=0){
                return -v1.getSup();
            }else
            if(v1.getSup()<=0){
                return v1.getInf()*(v2.getSup()+1)+1;
            }else
            if(v1.getInf()<0 && v1.getSup() > 0){
                return -max(abs(v1.getInf()), v1.getSup())*(v2.getSup()+1)+1;
            }
        }else
        if(v2.getSup() <= 0){
             if(v1.getInf()>=0){
                return v1.getSup()*(v2.getInf()-1)+1;
            }else
            if(v1.getSup()<=0){
               return -v1.getInf()*(v2.getInf()-1)+1;
            }else
            if(v1.getInf()<0 && v1.getSup() > 0){
               return max(abs(v1.getInf()), v1.getSup())*(v2.getInf()-1)+1;
            }
        }else
        if(v2.getInf() < 0 && v2.getSup() > 0){
            if(v1.getInf()>=0){
                return -v1.getSup()*(max(abs(v2.getInf()), v2.getSup())+1)+1;
            }else
            if(v1.getSup()<=0){
               return v1.getInf()*(max(abs(v2.getInf()), v2.getSup())+1)+1;
            }else
            if(v1.getInf()<0 && v1.getSup() > 0){
               return -max(abs(v1.getInf()), v1.getSup())*(max(abs(v2.getInf()), v2.getSup())+1)+1;
            }
        }
        throw new SolverException("Could not compute lower bound for X");
    }


    /**
     * Compute the new upper bound of X with
     * the domain of Y and Z.
     * @return the new upper bound of X
     */
    private int getUpperBoundX() {
        if(v2.isInstantiatedTo(0)){
            if(v1.getInf()>=0){
                return v1.getSup()-1;
            }else
            if(v1.getSup()<=0){
                return -v1.getInf()-1;
            }else
            if(v1.getInf()<0 && v1.getSup() > 0){
                return max(abs(v1.getInf()), v1.getSup())-1;
            }
        }else
        if(v2.getInf() >= 0){
            if(v1.getInf()>=0){
                return v1.getSup()*(v2.getSup()+1)-1;
            }else
            if(v1.getSup()<=0){
                return -v1.getInf()*(v2.getSup()+1)-1;
            }else
            if(v1.getInf()<0 && v1.getSup() > 0){
                return max(abs(v1.getInf()), v1.getSup())*(v2.getSup()+1)-1;
            }
        }else
        if(v2.getSup() <= 0){
             if(v1.getInf()>=0){
                return v1.getSup()-1;
            }else
            if(v1.getSup()<=0){
               return v1.getInf()*(v2.getInf()-1)-1;
            }else
            if(v1.getInf()<0 && v1.getSup() > 0){
               return -max(abs(v1.getInf()), v1.getSup())*(v2.getInf()-1)-1;
            }
        }else
        if(v2.getInf() < 0 && v2.getSup() > 0){
            if(v1.getInf()>=0){
                return v1.getSup()*(max(abs(v2.getInf()), v2.getSup())+1)-1;
            }else
            if(v1.getSup()<=0){
               return -v1.getInf()*(max(abs(v2.getInf()), v2.getSup())+1)-1;
            }else
            if(v1.getInf()<0 && v1.getSup() > 0){
               return max(abs(v1.getInf()), v1.getSup())*(max(abs(v2.getInf()), v2.getSup())+1)-1;
            }
        }
//        throw new SolverException("Could not compute upper bound for X");
        return v0.getSup();
    }

    private void filterOnY() throws ContradictionException {
        changeY = false;
        if(v1.getInf()==0){
                changeY = v1.updateInf(1, this, false);
            }else if(v1.getSup()==0){
                changeY = v1.updateSup(-1, this, false);
            }
        if(!v0.isInstantiatedTo(0)){
            changeY = changeY || v1.updateInf(getLowerBoundY(), this, false);
            changeY =  changeY || v1.updateSup(getUpperBoundY(), this, false);
        }
    }

    /**
     * Compute the new lower bound of Y with
     * the domain of X and Z.
     * @return the new lower bound of Y
     */
    private int getLowerBoundY() {
        if(v2.isInstantiatedTo(0)){
            if (v1.getInf()>=0){
                if(v0.getInf()>=0 ){
                    return v0.getInf()+1;
                }else if(v0.getSup()<=0 ){
                    return -(v0.getSup()-1);
                }
            }
        }
        if (v2.getInf() > 0) {
            if (v0.getInf() >= 0) {
                return 1;
            } else if (v0.getSup() <= 0) {
                return min(-1, v0.getInf()/v2.getInf());
            }else
            if (v0.getInf() < 0 && v0.getSup() > 0) {
                return -max(abs(v0.getInf()),v0.getSup())/v2.getInf();
            }
        } else if (v2.getSup() < 0) {
            if (v0.getInf() >= 0) {
                return min(-1, v0.getSup()/v2.getSup());
            } else if (v0.getSup() <= 0) {
                return max(-v0.getInf()/v2.getSup(),1);
            }else
            if (v0.getInf() < 0 && v0.getSup() > 0) {
               return max(abs(v0.getInf()),v0.getSup())/v2.getSup();
            }
        }
        return v1.getInf();
    }

    /**
     * Compute the new upper bound of Y with
     * the domain of X and Z.
     * @return the new upper bound of Y
     */
    private int getUpperBoundY() {
        if(v2.isInstantiatedTo(0)){
            if (v1.getSup()<=0){
                if(v0.getInf()>=0 ){
                    return -(v0.getInf()+1);
                }else if(v0.getSup()<=0 ){
                    return v0.getSup()-1    ;
                }
            }
        }
        if (v2.getInf() > 0) {
            if (v0.getInf() >= 0) {
                return v0.getSup() / v2.getInf();
            } else if (v0.getSup() <= 0) {
                return min(-v0.getInf() / v2.getInf(), -1);
            } else if (v0.getInf() < 0 && v0.getSup() > 0) {
                return max(abs(v0.getInf()), v0.getSup()) / v2.getInf();
            }
        } else if (v2.getSup() < 0) {
            if (v0.getInf() >= 0) {
                return min(-1,-v0.getSup()/(v2.getSup()-1));
            } else if (v0.getSup() <= 0) {
                return v0.getInf() / v2.getSup();
            } else if (v0.getInf() < 0 && v0.getSup() > 0) {
                return -max(abs(v0.getInf()), v0.getSup()) / v2.getSup();
            }
        }
        return v1.getSup();
    }


    private void filterOnZ() throws ContradictionException {
        changeZ = false;
        if(v0.isInstantiatedTo(0)){
            changeZ = v2.instantiate(0, this, false);
        }else{
            changeZ = v2.updateInf(getLowerBoundZ(), this, false);
            changeZ = changeZ || v2.updateSup(getUpperBoundZ(), this, false);
        }
    }

    private int getLowerBoundZ() {
        if(v0.getInf() >= 0){
            if(v1.getInf() > 0){
                return v0.getInf() / v1.getSup();
            }else
            if(v1.getSup() < 0){
                return v0.getSup() / v1.getSup();
            }else
            if(v1.getInf() < 0 && v1.getSup() >0){
                return - v0.getSup();
            }
        }else
        if(v0.getSup() <= 0){
            if(v1.getInf() > 0){
                return v0.getInf() / v1.getInf();
            }else
            if(v1.getSup() < 0){
                return v0.getSup() / v1.getInf();
            }else
            if(v1.getInf() < 0 && v1.getSup() >0){
                return  v0.getInf();
            }
        }else
        if(v0.getInf() < 0 && v0.getSup() > 0){
            if(v1.getInf() > 0){
                return v0.getInf() / v1.getInf();
            }else
            if(v1.getSup() < 0){
                return v0.getSup()/v1.getSup();
            }else
            if(v1.getInf() < 0 && v1.getSup() >0){
                return  -max(abs(v0.getInf()), v0.getSup());
            }
        }
        return v2.getInf();
    }

    private int getUpperBoundZ() {
        if(v0.getInf() >= 0){
            if(v1.getInf() > 0){
                return v0.getSup() / v1.getInf();
            }else
            if(v1.getSup() < 0){
                return v0.getInf() / v1.getInf();
            }else
            if(v1.getInf() < 0 && v1.getSup() >0){
                return v0.getSup();
            }
        }else
        if(v0.getSup() <= 0){
            if(v1.getInf() > 0){
                return v0.getSup() / v1.getSup();
            }else
            if(v1.getSup() < 0){
                return v0.getInf() / v1.getSup();
            }else
            if(v1.getInf() < 0 && v1.getSup() >0){
                return -v0.getInf();
            }
        }else
        if(v0.getInf() < 0 && v0.getSup() > 0){
            if(v1.getInf() > 0){
                return v0.getSup() / v1.getInf();
            }else
            if(v1.getSup() < 0){
                return v0.getInf()/v1.getSup();
            }else
            if(v1.getInf() < 0 && v1.getSup() >0){
                return  max(abs(v0.getInf()), v0.getSup());
            }
        }
        return v2.getSup();
    }
}