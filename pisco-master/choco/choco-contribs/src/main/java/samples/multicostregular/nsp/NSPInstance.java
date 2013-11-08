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

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Dec 4, 2008
 * Time: 5:12:08 PM
 */
public  class NSPInstance
{
    public int nbNurses;
    public int nbShifts;
    public int nbDays;

    public int[][] coverages;
    public int[][] prefs;

    public String toString()
    {
        StringBuffer b = new StringBuffer();
        b.append(nbNurses).append("\t").append(nbDays).append("\t").append(nbShifts).append((char) Character.LINE_SEPARATOR).append((char) Character.LINE_SEPARATOR);
        for (int[] coverage : coverages) {
            for (int aCoverage : coverage) {
                b.append(aCoverage).append("\t");
            }
            b.append((char) Character.LINE_SEPARATOR);
        }
        b.append((char) Character.LINE_SEPARATOR);
        for (int[] pref : prefs) {
            for (int aPref : pref) {
                b.append(aPref).append("\t");
            }
            b.append("\n");
        }
        return b.toString();
    }



}
