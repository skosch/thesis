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
import choco.kernel.common.logging.Verbosity;
import samples.tutorials.puzzles.Queen;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingExample implements Example {


    @Override
    public void execute(String... args) {
        if (args.length == 0) {
            for (Verbosity v : Verbosity.values()) {
                execute(v.toString());
            }
        } else {
            Verbosity verb = Verbosity.valueOf(args[0]);
            ChocoLogging.flushLogs();
            ChocoLogging.setVerbosity(verb);
            ChocoLogging.getMainLogger().log(Level.SEVERE, "verbosity: {0}", verb);
            new Queen().execute();
            ChocoLogging.flushLogs();
            for (Logger logger : ChocoLogging.CHOCO_LOGGERS) {
                final Level l = logger.getLevel();
                logger.log(l, "{1}: {2}", new Object[]{-1, logger.getName(), l});
            }
        }
    }

    public static void main(String[] args) {
    	ChocoLogging.recordXmlLogs(null);
    	ChocoLogging.recordLogs(null);
    	ChocoLogging.recordErrorLogs(null);
    	new LoggingExample().execute("SEARCH");
    }

}
