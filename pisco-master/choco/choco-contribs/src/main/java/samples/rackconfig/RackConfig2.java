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

package samples.rackconfig;

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.ArrayList;
import java.util.Arrays;
/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 25, 2009
 * Time: 8:10:35 PM
 */
public class RackConfig2 extends CPModel {

    Instances inst;
    IntegerVariable cost;
    IntegerVariable[] w;

    public RackConfig2(int idx)
    {
        this.inst = Instances.getInstance(idx);
    }

    IntegerVariable[][] rel;
    public void makePrimalModel()
    {
        rel = makeIntVarArray("rel",inst.getNbRackModels()*inst.getNbRacks(),inst.getNbCard(),0,1);


        for (int i  = 0 ; i < inst.getNbRackModels() ; i++)
        {
            for (int j = 0 ; j < inst.getNbRacks() ; j++)
            {
                this.addConstraint(leq(sum(rel[i*inst.getNbRacks()+j]),inst.getRackCapacity(i)));

                ArrayList<IntegerExpressionVariable> iee = new ArrayList<IntegerExpressionVariable>();
                int l = 0;
                for (int k = 0 ; k < inst.getNbCardTypes() ; k++)
                {
                    IntegerVariable[] tmp = Arrays.copyOfRange(rel[i*inst.getNbRacks()+j],l,inst.getCardNeed(k)+l);
                    l+= inst.getCardNeed(k);
                    iee.add(mult(sum(tmp),inst.getCardPower(k)));
                }

                this.addConstraint(leq(sum(iee.toArray(new IntegerExpressionVariable[iee.size()])),inst.getRackMaxPower(i)));

            }
        }

        //demand constraint

        IntegerVariable[][] relTrans = ArrayUtils.transpose(rel);
        int idx = 0;
        for (int i = 0 ; i < inst.getNbCardTypes();  i++)
        {
            for (int j = 0 ;j < inst.getCardNeed(i) ; j++)
            {
                this.addConstraint(eq(sum(relTrans[idx++]),1));

            }
        }

        //cardinality constraint
        w = makeIntVarArray("w",inst.getNbRackModels()*inst.getNbRacks(),0,1);

        this.addConstraint(leq(sum(w),inst.getNbRacks()));

        //linking constraint


        for (int i  = 0 ; i < inst.getNbRackModels() ; i++)
        {
            for (int j = 0 ; j < inst.getNbRacks() ; j++)
            {
                int sofar = 0;
                for (int k = 0 ; k < inst.getNbCardTypes() ; k++)
                {
                    for (int l = 0 ; l < inst.getCardNeed(k) ; l++)
                    {
                        this.addConstraint(implies(eq(rel[i*inst.getNbRacks()+j][sofar++],1),eq(w[i*inst.getNbRacks()+j],1)));
                    }
                }
            }
        }



        cost  = makeIntVar("cost",0,Integer.MAX_VALUE/1000, Options.V_BOUND);

        ArrayList<IntegerExpressionVariable> ie = new ArrayList<IntegerExpressionVariable>();
        for (int i = 0 ; i < inst.getNbRackModels() ; i++)
        {
            IntegerVariable[] tmp = Arrays.copyOfRange(w,i*inst.getNbRacks(),(i+1)*inst.getNbRacks());
            ie.add(mult(sum(tmp),inst.getRackPrice(i)));
        }
        this.addConstraint(eq(cost,sum(ie.toArray(new IntegerExpressionVariable[ie.size()]))));


    }


    public static void main(String[] args) {
        RackConfig2 rc = new RackConfig2(0);

        rc.makePrimalModel();


        CPSolver s = new CPSolver();

        s.read(rc);
        System.out.println(s.minimize(s.getVar(rc.cost),false));
        System.out.println(s.getVar(rc.cost).getVal());

        for (int i = 0 ; i < rc.rel.length ;i++)
        {
            for (int j = 0 ; j < rc.rel[i].length ; j++)
            {
                System.out.print(s.getVar(rc.rel[i][j]).getVal()+" ");
            }
            System.out.println("");
        }
        for (int i = 0 ; i < rc.w.length ; i++)
        {
            System.out.print(s.getVar(rc.w[i]).getVal());
        }
        System.out.println("");
        s.printRuntimeStatistics();

    }

}