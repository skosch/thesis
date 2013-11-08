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

package samples.tutorials;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.solver.Solver;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.logging.Level;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 9 janv. 2009
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 */
public abstract class PatternExample implements Example {

    public Model model;

    public Solver solver;

    public void printDescription() {
    }

    public abstract void buildModel();

    public abstract void buildSolver();

    public abstract void solve();

    public abstract void prettyOut();

    public final void readArgs(String... args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(160);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java " + this.getClass() + " [options...]");
            parser.printUsage(System.err);
            System.err.println();
            System.exit(-1);
        }
    }

    public final void execute(String... args) {
        this.readArgs(args);

        LOGGER.log(Level.INFO, ChocoLogging.START_MESSAGE);
        LOGGER.log(Level.INFO, "* Sample library: executing {0}.java ... \n", getClass().getName());
        this.printDescription();
        this.buildModel();
        this.buildSolver();
        this.solve();
        this.prettyOut();

        LOGGER.log(Level.INFO, "* Choco usage statistics");
        if (LOGGER.isLoggable(Level.INFO)) {
            if (solver == null) {
                LOGGER.info(" - solver object is null. No statistics available.");
            } else {
                LOGGER.log(Level.INFO, " - #sol : {0}\n - {1}", new Object[]{solver.getSolutionCount(), solver.runtimeStatistics()});
            }
            ChocoLogging.flushLogs();
        }
    }

    public final void execute() {
        this.execute(new String[0]);
    }


}
