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

package choco.shaker.tools.factory;

import choco.Choco;
import choco.kernel.model.constraints.Constraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 12 mars 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class MetaConstraintFactory {

    ConstraintFactory cf;

    public enum MC {AND, IFTHENELSE, IFONLYIF, IMPLIES, NOT, NONE, OR}

    public ArrayList<MC> scope = new ArrayList<MC>();


    /**
     * Declare factory dependencies
     * @param cf constraint factory
     */
    public void depends(ConstraintFactory cf){
        this.cf = cf;
    }

    /**
     * Define a specific scope of metaconstraint type to pick up in
     * @param mcs metaconstraint types
     */
    public void scopes(MC... mcs){
        scope.clear();
        scope.addAll(Arrays.asList(mcs));
    }

    /**
     * Select randomly (among scope if defined)
     * and return a metaconstraint type
     * @param r random
     * @return metaconstraint type
     */
    public MC any(Random r){
        if(scope.size()>0){
            return scope.get(r.nextInt(scope.size()));
        }
        MC[] values = MC.values();
        return values[r.nextInt(values.length)];
    }

    /**
     * Make a metaconstraint
     * @param r random
     * @return Constraint
     */
    public Constraint make(Random r) {
        return make(any(r), r);
    }


    /**
     * Make a specific metaconstraint
     * @param mc metaconstraint
     * @param r random
     * @return Constraint
     */
    public Constraint make(MC mc, Random r) {
        Constraint[] cs;
        switch (mc){
            case AND:
                return Choco.and(cf.make(r.nextInt(5), r));
            case IFTHENELSE:
                cs = cf.make(3, r);
                return  Choco.ifThenElse(cs[0], cs[1], cs[2]);
            case IFONLYIF:
                cs = cf.make(2, r);
                return  Choco.ifOnlyIf(cs[0], cs[1]);
            case IMPLIES:
                cs = cf.make(2, r);
                return  Choco.implies(cs[0], cs[1]);
            case NOT :
                cs = cf.make(1, r);
                return  Choco.not(cs[0]);
            case NONE:
                return cf.make(r);
            case OR :
                return Choco.or(cf.make(r.nextInt(5), r));
        }
        return null;
    }

}
