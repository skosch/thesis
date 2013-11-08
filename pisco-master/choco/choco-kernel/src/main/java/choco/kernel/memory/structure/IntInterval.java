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

package choco.kernel.memory.structure;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 22 juin 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class IntInterval {

    private IStateInt inf;
    private IStateInt sup;

    private final IEnvironment environment;

    public IntInterval(IEnvironment environment, int inf, int sup) {
        this.environment = environment;
        this.inf = environment.makeInt(inf);
        this.sup = environment.makeInt(sup);
    }

    public int getInf() {
        return inf.get();
    }

    public void setInf(int inf) {
        this.inf.set(inf);
    }

    public void addInf(int delta) {
        this.inf.add(delta);
    }

    public int getSup() {
        return sup.get();
    }

    public void setSup(int sup) {
        this.sup.set(sup);
    }

    public void addSup(int delta) {
        this.sup.add(delta);
    }

    public IEnvironment getEnvironment() {
        return environment;
    }

    public boolean contains(int x){
        return x <= sup.get() && x >= inf.get();
    }

}
