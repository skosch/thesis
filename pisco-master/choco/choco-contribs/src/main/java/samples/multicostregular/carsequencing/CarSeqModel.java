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

package samples.multicostregular.carsequencing;

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import gnu.trove.TIntHashSet;
import samples.multicostregular.carsequencing.parser.CarSeqInstance;
import samples.multicostregular.carsequencing.parser.GraphGenerator;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jan 28, 2009
 * Time: 4:25:06 PM
 */
public class CarSeqModel extends CPModel {

    public IntegerVariable[] seqVars;
    public IntegerVariable[] occVars;

    private CarSeqInstance instance;


    public CarSeqModel(String description,boolean unique)
    {

        this.instance = new CarSeqInstance(description);
        this.buildModelFromInstance(this.instance,unique);
    }

    public CarSeqInstance getInstance()
    {
        return this.instance;
    }

    public void buildModelFromInstance(CarSeqInstance make,boolean unique)
    {


        FiniteAutomaton auto = new FiniteAutomaton();
        ArrayList<dk.brics.automaton.Automaton> list = new ArrayList<dk.brics.automaton.Automaton>();

        IntegerVariable[] vs = makeIntVarArray("pos",make.nbCars,0,make.nbClasses-1);
        IntegerVariable[] z = new IntegerVariable[make.nbClasses];
        this.seqVars = vs;
        this.occVars = z;
        for (int i = 0 ; i < z.length ; i++)
        {
            int nb = make.optionRequirement[i][1];
            z[i] = makeIntVar("nb",nb,nb);
        }

        this.addVariables(Options.V_ENUM,vs);
        this.addVariables(Options.V_BOUND,z);




        TIntHashSet alphabet = new TIntHashSet();
        for (int i = 0; i < make.nbClasses ; i++) alphabet.add(i);

        for (int i = 0 ; i < make.blockSize.length ; i++)
        {

            TIntHashSet cin = new TIntHashSet();
            TIntHashSet cout = new TIntHashSet();

            for (int j = 0 ; j < make.nbClasses ; j++)
            {
                if (make.optionRequirement[j][i+2] == 1)
                    cin.add(j);
                else
                    cout.add(j);
            }



            GraphGenerator gg = GraphGenerator.make(make.maxPerBlock[i],make.blockSize[i]);
            dk.brics.automaton.Automaton aa = gg.toBricsAutomaton(cin.toArray(),cout.toArray());
            aa.minimize();
            list.add(aa);
        }






        int[][][] csts = new int[vs.length][make.nbClasses][make.nbClasses];
        for (int i = 0 ; i < csts.length ; i++)
            for (int j = 0 ; j < csts[i].length ; j++)
                for (int k = 0 ; k < csts[i][j].length ;k++)
                {
                    if (j == k) csts[i][j][k] = 1;
                }



        if (unique)
        {

            dk.brics.automaton.Automaton inter = list.get(0);
            for (dk.brics.automaton.Automaton a : list)
            {
                inter = inter.intersection(a);
                inter.minimize();
            }

            auto.fill(inter,alphabet);


            Constraint cons = multiCostRegular(z, vs, auto,csts);
            this.addConstraint(cons);
        }
        else
        {
            int i = 0;
            for (dk.brics.automaton.Automaton a : list)
            {
                if (true || i == 0)
                {
                    FiniteAutomaton tomate = new FiniteAutomaton();
                    tomate.fill(a,alphabet);
                    Constraint cons = multiCostRegular(z, vs, tomate,csts);
                    this.addConstraint(cons);

                    i++;
                }
            }

        }






    }

}