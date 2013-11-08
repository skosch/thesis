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

package shaker.tools.search;

import choco.cp.solver.search.integer.valiterator.DecreasingDomain;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.kernel.solver.search.ValIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 5 août 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*/
public class IntValIteratorFactory {

    public ArrayList<V> scope = new ArrayList<V>();

    public enum V {
        DECDOM, INCDOM
    }

    /**
     * Define a specific scope of value iterator tuple to pick up in
     * @param vs the scope of value iterator
     */
    public void scopes(V... vs){
        scope.clear();
        scope.addAll(Arrays.asList(vs));
    }

    /**
     * Select randomly (among scope if defined)
     * and return a value iterator type
     * @param r random
     * @return type of value iterator
     */
    public V any(Random r) {
        if(scope.size()>0){
            return scope.get(r.nextInt(scope.size()));
        }
        V[] values = V.values();
        return values[r.nextInt(values.length)];
    }


    /**
     * Get one value iterator among all
     * @param r random
     * @return value iterator
     */
    public ValIterator make(Random r){
        return make(any(r), r);
    }

    /**
     * Create and return the corresponding value iterator
     * @param v the type of value iterator
     * @param r random
     * @return value iterator
     */
    public ValIterator make(V v, Random r) {
        // Select a new val iterator
        ValIterator vi = null;

        switch (v) {
            case DECDOM:
                vi = new DecreasingDomain();
            break;
            case INCDOM:
                vi = new IncreasingDomain();
            break;

        }
        return vi;
    }
}