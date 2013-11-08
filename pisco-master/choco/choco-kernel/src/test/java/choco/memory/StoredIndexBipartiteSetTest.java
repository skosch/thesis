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

package choco.memory;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 23 févr. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class StoredIndexBipartiteSetTest {

    StoredIndexedBipartiteSet sibs;
    IEnvironment env;

    @Before
    public void before(){
        env = new EnvironmentTrailing();
    }

    @Test
    public void test0() {
        for(int seed = 0; seed < 20; seed++ ){
            Random r = new Random(seed);

            sibs = (StoredIndexedBipartiteSet)env.makeBipartiteSet(10);
            int[] v = new int[20];
            for(int i = 0; i <4; i++){
                int n = r.nextInt(10);
                if(v[n]==0){
                    v[n] = 1;
                    sibs.remove(n);
                }
            }
            sibs.increaseSize(10);
            for(int i = 0; i <4; i++){
                int n = 10 + r.nextInt(10);
                if(v[n]==0){
                    v[n] = 1;
                    sibs.remove(n);
                }
            }
            for(int i = 0; i< 20; i++){
                Assert.assertEquals("i:"+i, v[i]==0, sibs.contain(i));
            }
        }
    }

}
