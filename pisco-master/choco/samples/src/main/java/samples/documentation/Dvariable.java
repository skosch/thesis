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

package samples.documentation;

import static choco.Choco.*;
import choco.Options;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 9 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public class Dvariable {

    public static void vintegervariable() {
        //totex vintegervariable
        IntegerVariable ivar1 = makeIntVar("ivar1", -10, 10);
        IntegerVariable ivar2 = makeIntVar("ivar2", 0, 10000, Options.V_BOUND, Options.V_NO_DECISION);
        IntegerVariable bool = makeBooleanVar("bool");
        //totex
    }

    public static void vsetvariable() {
        //totex vsetvariable
        SetVariable svar1 = makeSetVar("svar1", -10, 10);
        SetVariable svar2 = makeSetVar("svar2", 0, 10000, Options.V_BOUND, Options.V_NO_DECISION);
        //totex
    }

    public static void vrealvariable() {
        //totex vprecision
        Solver m = new CPSolver();
        m.setPrecision(0.01);
        //totex

        //totex vrealvariable
        RealVariable rvar1 = makeRealVar("rvar1", -10.0, 10.0);
        RealVariable rvar2 = makeRealVar("rvar2", 0.0, 100.0, Options.V_NO_DECISION, Options.V_OBJECTIVE);
        //totex
    }

    public static void vtaskvariable() {
        //totex vtaskvariable
        TaskVariable tvar1 = makeTaskVar("tvar1", 0, 123, 18, Options.V_ENUM);
        IntegerVariable start = makeIntVar("start", 0, 30);
        IntegerVariable end = makeIntVar("end", 10, 60);
        IntegerVariable duration = makeIntVar("duration", 7, 13);
        TaskVariable tvar2 = makeTaskVar("tvar2", start, end, duration);
        //totex
    }
}
