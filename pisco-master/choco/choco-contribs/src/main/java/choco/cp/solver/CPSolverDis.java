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

package choco.cp.solver;

import choco.cp.solver.propagation.BlockingVarEventQueue;
import choco.cp.solver.propagation.VariableEventQueue;

import java.lang.reflect.Field;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 4 août 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*/
public class CPSolverDis extends CPSolver{

    public CPSolverDis() {
        super();
         try {
             Field field = propagationEngine.getClass().getDeclaredField("varEventQueue");
             field.setAccessible(true);
             VariableEventQueue[] old_veq = (VariableEventQueue[]) field.get(propagationEngine);
             VariableEventQueue[] new_veq = new VariableEventQueue[old_veq.length];
             for(int i = 0 ; i < old_veq.length; i++){
                 new_veq[i] = new BlockingVarEventQueue();
             }
             field.set(propagationEngine, new_veq);
             field.setAccessible(false);
         } catch (NoSuchFieldException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
    }
}
