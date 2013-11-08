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

package samples.random;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter {

    private final static String fl = "\\d*\\.?\\d+E?-?\\d*";

    private Interpreter() {

    }

    public static void main(String[] arg) throws IOException {
        final BufferedReader fr = new BufferedReader(new FileReader(arg[0]));

        final Matcher tghtM = Pattern.compile("(" + fl + ")\\ tightness")
                .matcher("");
        final Matcher secsM = Pattern.compile("(" + fl + ")\\ sec").matcher("");
        final Matcher nodesM = Pattern.compile("(" + fl + ")\\ nodes").matcher(
                "");
        final Matcher memM = Pattern.compile("(" + fl + ")\\ mem").matcher("");

        final DecimalFormat f = (DecimalFormat) DecimalFormat.getInstance();
        f.setGroupingSize(0);

        do {
            if (fr.readLine() == null) {
                break;
            }
            tghtM.reset(fr.readLine());
            tghtM.find();
            final float tght = Float.parseFloat(tghtM.group(1));
            fr.readLine();
            fr.readLine();
            fr.readLine();
            fr.readLine();

            // secsM.reset(fr.readLine());
            // secsM.find();
            // final float lightSecs = Float.parseFloat(secsM.group(1));
            // nodesM.reset(fr.readLine());
            // nodesM.find();
            // final float lightNodes = Float.parseFloat(nodesM.group(1));
            // fr.readLine();
            // memM.reset(fr.readLine());
            // memM.find();
            // final float lightMem = Float.parseFloat(memM.group(1));
            // fr.readLine();
            // fr.readLine();
            //
            // secsM.reset(fr.readLine());
            // secsM.find();
            // final float maxSecs = Float.parseFloat(secsM.group(1));
            // nodesM.reset(fr.readLine());
            // nodesM.find();
            // final float maxNodes = Float.parseFloat(nodesM.group(1));
            // fr.readLine();
            // memM.reset(fr.readLine());
            // memM.find();
            // final float maxMem = Float.parseFloat(memM.group(1));
            // fr.readLine();
            // fr.readLine();

            secsM.reset(fr.readLine());
            secsM.find();
            final float acSecs = Float.parseFloat(secsM.group(1));
            nodesM.reset(fr.readLine());
            nodesM.find();
            final float acNodes = Float.parseFloat(nodesM.group(1));
            fr.readLine();
            memM.reset(fr.readLine());
            memM.find();
            final float acMem = Float.parseFloat(memM.group(1));

            // System.out.println(f.format(tght) + "\t" + f.format(lightSecs)
            // + "\t" + f.format(lightNodes) + "\t" + f.format(lightMem)
            // + "\t" + f.format(maxSecs) + "\t" + f.format(maxNodes)
            // + "\t" + f.format(maxMem) + "\t" + f.format(acSecs) + "\t");
            System.out.println(f.format(acSecs) + "\t" + f.format(acMem));
        }

        while (true);

    }
}
