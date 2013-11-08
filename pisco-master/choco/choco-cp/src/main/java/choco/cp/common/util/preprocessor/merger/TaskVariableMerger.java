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

package choco.cp.common.util.preprocessor.merger;

import choco.Options;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.variables.scheduling.TaskVariable;
import gnu.trove.THashSet;

import java.util.Set;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 1 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public class TaskVariableMerger {

    public IntegerVariableMerger start;
    public IntegerVariableMerger duration;
    public IntegerVariableMerger end;

    private Set<String> optionsSet;

    public TaskVariableMerger() {
        optionsSet = new THashSet<String>();
    }

    public TaskVariableMerger(final TaskVariable v) {
        this();
        start = new IntegerVariableMerger(v.start());
        duration = new IntegerVariableMerger(v.duration());
        end = new IntegerVariableMerger(v.end());
        optionsSet.addAll(v.getOptions());
    }

    public void copy(final TaskVariableMerger toCopy) {
        this.start = toCopy.start;
        this.duration = toCopy.duration;
        this.end = toCopy.end;
        this.optionsSet = toCopy.optionsSet;
    }

    public TaskVariable create(){
        return new TaskVariable(StringUtils.randomName(), start.create(), duration.create(), end.create());
    }

    public boolean intersection(final TaskVariable d) {
        if (start == null) {
            start = new IntegerVariableMerger(d.start());
        } else if (!start.intersection(d.start())) {
            return false;
        }
        if (duration == null) {
            duration = new IntegerVariableMerger(d.duration());
        } else if (!duration.intersection(d.duration())) {
            return false;
        }
        if (end == null) {
            end = new IntegerVariableMerger(d.end());
        } else if (!end.intersection(d.end())) {
            return false;
        }
        final THashSet<String> toptionsSet = new THashSet<String>();
        if (d.getOptions().contains(Options.V_DECISION)
                || optionsSet.contains(Options.V_DECISION)) {
            toptionsSet.add(Options.V_DECISION);
        }
        if (d.getOptions().contains(Options.V_NO_DECISION)
                || optionsSet.contains(Options.V_NO_DECISION)) {
            toptionsSet.add(Options.V_NO_DECISION);
        }
        if (d.getOptions().contains(Options.V_OBJECTIVE)
                || optionsSet.contains(Options.V_OBJECTIVE)) {
            toptionsSet.add(Options.V_OBJECTIVE);
        }
        this.optionsSet = toptionsSet;
        return true;
    }
}
