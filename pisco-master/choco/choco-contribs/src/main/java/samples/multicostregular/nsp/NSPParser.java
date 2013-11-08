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

package samples.multicostregular.nsp;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Dec 4, 2008
 * Time: 3:15:58 PM
 */
public class NSPParser {




    public static NSPInstance parseNSPFile(String filename)
    {

        NSPInstance out = new NSPInstance();
        FileReader f = null;
        try {
            f = new FileReader(filename);
        } catch (FileNotFoundException e) {
            System.err.println("Unable to open file");
            System.exit(-1);
        }
        BufferedReader r = new BufferedReader(f);

        String line;
        Pattern p = Pattern.compile("(\\d+\\t?)+");

        try {
            int count = 0;
            String[] l;
            while ((line = r.readLine()) != null) {

                Matcher m = p.matcher(line);
                if (m.matches())
                {
                    l = line.split("\t");
                    if (count == 0)
                    {

                        out.nbNurses = Integer.parseInt(l[0]);
                        out.nbDays = Integer.parseInt(l[1]);
                        out.nbShifts = Integer.parseInt(l[2]);
                        out.coverages = new int[out.nbDays][out.nbShifts];
                        out.prefs = new int[out.nbNurses][out.nbDays*out.nbShifts];
                    }
                    else if (count > 0 && count < out.nbDays+1)
                    {
                        for (int i = 0 ; i < l.length ; i++)
                        {
                            out.coverages[count-1][i] = Integer.parseInt(l[i]);
                        }
                    }
                    else if (count >= out.nbDays+1)
                    {
                        for (int i = 0 ; i < l.length ; i++)
                        {
                            out.prefs[count-out.nbDays-1][i] = Integer.parseInt(l[i]);
                        }

                    }
                    count++;
                }

            }
        } catch (IOException e) {
            System.err.println("Error reading file");
            System.exit(-1);
        }

        return out;
    }

    public static void main(String[] args) {
        NSPInstance nsp = NSPParser.parseNSPFile("/Users/julien/These/NSP/NSPLib/N25/1.nsp");
        System.out.println(nsp);


    }

}
