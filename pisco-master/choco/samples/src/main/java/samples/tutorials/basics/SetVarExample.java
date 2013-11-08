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

package samples.tutorials.basics;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import samples.tutorials.PatternExample;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 8 mai 2010
 * Time: 13:15:02
 * <p/>
 * //TODO : does not work
 */
public class SetVarExample extends PatternExample {

    int[][] whoknowswho =  // incidence matrix
            {
                    {0, 1, 2, 7, 8, 9},
                    {0, 1, 2, 8, 9},
                    {2, 8, 9},
                    {1, 2, 3, 4, 5, 8, 9},
                    {2, 3, 4, 5, 8, 9},
                    {2, 3, 4, 5, 6, 7, 8, 9},
                    {2, 5, 6, 7, 8, 9},
                    {2, 5, 6, 7, 8, 9},
                    {9},
                    {2, 8, 9}
            };
    int n;               // size of the instance

    SetVariable celebs;  // what we are looking for


    public String prettyArray(int[] vals) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < vals.length; i++) {
            ret.append(vals[i]);
            ret.append(" ");
        }
        return ret.toString();
    }

    public void printInstance() {
        StringBuffer ret = new StringBuffer();

        for (int i = 0; i < n; i++) {
            ret.append(MessageFormat.format("\nP{0} knows {1}", i, prettyArray(whoknowswho[i])));
        }


        LOGGER.info(ret.toString());
    }

    @Override
    public void printDescription() {
        super.printDescription();
        LOGGER.info("A simple example involving set variables in choco. ");
        LOGGER.info("Problem: given a list of people at a party and for each person");
        LOGGER.info("the list of people they know at the party, we want to find");
        LOGGER.info("the celebrities at the party. A celebrity is a person that");
        LOGGER.info("everybody knows but that only knows celebrities. At least one");
        LOGGER.info("celebrity is present at the party.");

        printInstance();
    }

    @Override
    public void buildModel() {
        model = new CPModel();
        n = whoknowswho.length;
        // create the variables
        SetVariable[] party = new SetVariable[n];

        // the celebs variable
        celebs = Choco.makeSetVar("celebs", 1, n);
        // the people at the party (the set represents the set of people they know)

        for (int i = 0; i < n; i++) {
            party[i] = Choco.makeSetVar("P" + i, whoknowswho[i]);

        }
        model.addVariables(party);
        model.addVariable(celebs);


        // there is at least one celeb here
        model.addConstraint(Choco.geq(celebs.getCard(), 1));

        for (int i = 0; i < n; i++) {
            // everybody knows the celebs
            model.addConstraint(Choco.isIncluded(celebs, party[i]));

            // celebs only know celebs
            IntegerVariable b1 = Choco.makeIntVar("b1", 0, 1);
            IntegerVariable b2 = Choco.makeIntVar("b2", 0, 1);
            model.addConstraint(Choco.reifiedConstraint(b1, Choco.member(celebs, i)));
            model.addConstraint(Choco.reifiedConstraint(b2, Choco.isIncluded(party[i], celebs)));
            model.addConstraint(Choco.implies(Choco.eq(b1, 1), Choco.eq(b2, 1)));
        }

    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
    }

    @Override
    public void solve() {
        solver.solve();
    }

    @Override
    public void prettyOut() {
        StringBuffer ret = new StringBuffer();

        ret.append(MessageFormat.format("\nThe celebs are {0}", Arrays.toString(
                solver.getVar(celebs).getValue())));

        ret.append("\n");

        LOGGER.info(ret.toString());

    }


    public static void main(String[] args) {
        new SetVarExample().execute(args);
    }

    ////////////////////////////////////////////////////

}
