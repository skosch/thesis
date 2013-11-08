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

package samples.multicostregular.asap;

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;
import samples.multicostregular.asap.data.ASAPItemHandler;
import samples.multicostregular.asap.data.ASAPShiftOnRequest;
import samples.multicostregular.asap.data.base.*;
import samples.multicostregular.asap.parser.ASAPParser;

import java.awt.*;
import java.io.BufferedWriter;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 3, 2009
 * Time: 2:17:53 PM
 */
public class ASAPCPModel extends CPModel {

    ASAPItemHandler handler;
    HashMap<ASAPContract, FiniteAutomaton> rules;
    HashMap<String, Color> colormap;
    public IntegerVariable[][] shifts;
    TIntHashSet alpha;
    int nbDays;
    int nbEmployees;

    public int lowb[][];
    public int uppb[][];

    public ASAPCPModel(String file)
    {
        this.handler = new ASAPParser(file).getHandler();
        this.rules = new HashMap<ASAPContract, FiniteAutomaton>();
        this.alpha = new TIntHashSet();
        this.nbDays = ASAPDate.getDaysBetween(handler.getStart(), handler.getEnd())+1;
        this.nbEmployees = handler.employees.values().size();
        this.colormap = new HashMap<String,Color>();
        this.lowb = new int[nbDays][];
        this.uppb= new int[nbDays][];
    }


    public int getNbDays() {
        return this.nbDays;
    }
    public int getNbEmployees()
    {
        return this.nbEmployees;
    }


    public ASAPItemHandler getHandler()
    {
        return this.handler;
    }

    private void makeShiftVariables()
    {
        int[] dom =  alpha.toArray();

        shifts = makeIntVarArray("x",nbEmployees,nbDays,dom, Options.V_ENUM);


    }

    static int WE = 0;



    private void postBadPatternConstraints()
    {
        int[] alpha = this.alpha.toArray();
        int m = 0 ;
        int nbWeek = nbDays/7;
        HashMap<ASAPContract,HashSet<IntegerVariable[]>> map = new HashMap<ASAPContract,HashSet<IntegerVariable[]>>();

 


        for (ASAPEmployee e : this.handler.orderedEmployees)
        {
            ASAPContract c = e.getContract();
            if (map.get(c) == null)
                map.put(c,new HashSet<IntegerVariable[]>());


            ArrayList<IntegerVariable> vs = new ArrayList<IntegerVariable>();
            int maxSPW = c.getMaxShiftsPerWeek();
            int minSPW = c.getMinShiftsPerWeek();


            //IntegerVariable[] z = new IntegerVariable[nbdim];



            if (maxSPW < 7 || minSPW > 0)
            {
                for (int i = 0 ; i < nbWeek ; i++)
                    vs.add(makeIntVar("z"+vs.size(),minSPW,maxSPW, Options.V_BOUND));
            }
            for (Integer anAlpha1 : alpha) {
                int tmp = c.getMaxShiftType(handler.getShift(handler.inverseMap.get(anAlpha1)));
                int tmp2 = c.getMinShiftType(handler.getShift(handler.inverseMap.get(anAlpha1)));

                if (tmp != Integer.MAX_VALUE || tmp2 > 0) {
                    if (tmp == Integer.MAX_VALUE) tmp /= 1000;
                    vs.add(makeIntVar("z" + vs.size(), tmp2, tmp, Options.V_BOUND));
                }

            }
            if (c.getMaxNumAssignment() != -1)
                vs.add(makeIntVar("z"+(nbWeek+alpha.length),c.getMinShiftsPerWeek()*nbWeek,c.getMaxNumAssignment(), Options.V_BOUND));

            if (c.getMaxWorkingWeekEnds() >= 0 || c.getMinWorkingWeekEnds() >0)
            {
                vs.add(makeIntVar("z"+vs.size(),c.getMinWorkingWeekEnds(),c.getMaxWorkingWeekEnds(), Options.V_BOUND));
            }

            if (c.getMaxDaysOff() > -1 || c.getMinDaysOff() > 0)
            {
                if (c.getMaxDaysOff() == -1) c.setMaxDaysOff(nbDays);
                vs.add(makeIntVar("z"+vs.size(),c.getMinDaysOff(),c.getMaxDaysOff(), Options.V_BOUND));
            }
            if (c.getMaxWeekEndDays() > -1  || c.getMinWeekEndDays() > 0)
            {
                vs.add(makeIntVar("z"+vs.size(),c.getMinWeekEndDays(),c.getMaxWeekEndDays(), Options.V_BOUND));
            }







            IntegerVariable[] v =  shifts[m++];
            map.get(c).add(v);
            IntegerVariable[] z = vs.toArray(new IntegerVariable[vs.size()]);
            IAutomaton a = rules.get(c);
            //  IntegerVariable z = makeIntVar("z",0,1000000,CPOptions.V_BOUND);
            this.addVariables(z);
            this.addVariables(v);
            int csts[][][] = new int[nbDays][FiniteAutomaton.max(this.alpha)+1][vs.size()];
            for (int i  = 0 ;i < csts.length ; i++)
                for (int j = 0 ; j < csts[i].length ; j++)
                {
                    int where =0;

                    if (maxSPW < 7 || minSPW > 0)
                        for (int k = 0 ; k < nbWeek ; k++)
                        {
                            if (i/7 == where && j != handler.map.get("R"))
                            {
                                csts[i][j][where] = 1;
                            }
                            where++;
                        }
                    for (Integer anAlpha : alpha) {
                        int tmp = c.getMaxShiftType(handler.getShift(handler.inverseMap.get(anAlpha)));
                        int tmp2 = c.getMinShiftType(handler.getShift(handler.inverseMap.get(anAlpha)));

                        if (tmp != Integer.MAX_VALUE || tmp2 > 0) {
                            if (j == anAlpha)
                                csts[i][j][where] = 1;
                            where++;
                        }

                    }
                    if (c.getMaxNumAssignment() != -1)
                    {
                        if (j != handler.map.get("R"))
                            csts[i][j][where] = 1;
                        where++;
                    }
                    if (c.getMaxWorkingWeekEnds() >= 0 || c.getMinWorkingWeekEnds() >0)
                    {
                        //System.out.println(this.data.getStart().getDayOfWeekName());
                        int d = (handler.getStart().getDayOfWeek()+i)%7;
                        if (d == 6 && j != handler.map.get("R")){
                            csts[i][j][where] = 1;

                        }
                        where++;
                    }
                    if (c.getMaxDaysOff() > -1 || c.getMinDaysOff() > 0)
                    {
                        if (j == handler.map.get("R"))
                            csts[i][j][where] = 1;
                        where++;
                    }
                    if (c.getMaxWeekEndDays() > -1  || c.getMinWeekEndDays() > 0)
                    {
                        if (j != handler.map.get("R") && ( i%7 == 5 || i%7 == 6))
                            csts[i][j][where] = 1;
                        where++;
                    }

                }





            

            Constraint cons = multiCostRegular(z, v, a,csts);
            this.addConstraint(cons);
        }

        for (HashSet<IntegerVariable[]> h : map.values())
        {

            IntegerVariable[][] chain = h.toArray(new IntegerVariable[h.size()][]);
            IntegerVariable[][] copy  = new IntegerVariable[chain.length][chain[0].length];
            for (int i = 0 ; i < chain.length ;i++)
            {
                System.arraycopy(chain[i], 0, copy[i], 0, chain[i].length);
            }


            for (IntegerVariable[] cc : copy)
            {
                ArrayUtils.reverse(cc);
            }

          //  this.addConstraint(lexChainEq(chain));   //TODO Ceci n'est pas correct !
        }
    }

    private void postVerticalGcc()
    {
        IntegerVariable[][] v = ArrayUtils.transpose(shifts);

        for (ASAPCover c: handler.getCover())
        {
            String d = c.getDay();
            int[] idxs = indexesForDay(d);
            int[] lowb = new int[10];
            int[] uppb = new int[10];

            uppb[handler.map.get("R")] = this.nbEmployees;

            for (int i =0 ; i < c.getSkills().size() ; i++)
            {
                ASAPSkill sk = c.getSkills().get(i);
                ASAPShift sh = c.getShifts().get(i);
                Integer min = c.getMins().get(i);
                Integer max = c.getMaxs().get(i);
                Integer pre = c.getPrefs().get(i);

                if (sk == null)
                {
                    uppb[handler.map.get(sh.getID())] = this.nbEmployees;
                    if (min != null)
                    {
                        lowb[handler.map.get(sh.getID())] = min;
                    }
                    if (max != null)
                    {
                        uppb[handler.map.get(sh.getID())] = max;

                    }
                    if (pre != null)
                    {
                        lowb[handler.map.get(sh.getID())] = pre;
                        uppb[handler.map.get(sh.getID())] = pre;
                    }

                }
                else
                {
                    uppb[handler.map.get(sh.getID())] = this.nbEmployees;
                    Collection<ASAPEmployee> emp = handler.getEmployeeBySkill(sk);
                    if (min != null)
                    {
                        for (int idx :idxs)
                        {
                            IntegerVariable[] subset = makeSubSet(emp,v[idx]);
                            this.addConstraint(occurrenceMin(makeIntVar("occ",min,Integer.MAX_VALUE/100, Options.V_BOUND),subset,handler.map.get(sh.getID())));
                        }
                    }
                    if (max != null)
                    {
                        for (int idx :idxs)
                        {
                            IntegerVariable[] subset = makeSubSet(emp,v[idx]);
                            this.addConstraint(occurrenceMax(makeIntVar("occ",0,max, Options.V_BOUND),subset,handler.map.get(sh.getID())));
                        }
                    }
                    if (pre != null)
                    {
                        for (int idx :idxs)
                        {
                            IntegerVariable[] subset = makeSubSet(emp,v[idx]);
                            this.addConstraint(occurrence(constant(pre),subset, handler.map.get(sh.getID())));
                        }
                    }
                }

            }
            for (int idx : idxs)
            {
                IntegerVariable[] col = v[idx];
                this.lowb[idx] = lowb;
                this.uppb[idx] = uppb;
                this.addConstraint(globalCardinality(Options.C_GCC_AC,col,0,9,lowb,uppb));
            }

        }





        //System.out.println(v);
    }

    private IntegerVariable[] makeSubSet(Collection<ASAPEmployee> emp, IntegerVariable[] iv) {
        IntegerVariable[] out = new IntegerVariable[emp.size()];
        int k = 0 ;
        for (int i = 0 ; i < this.nbEmployees ; i++)
        {
            if (emp.contains(handler.orderedEmployees.get(i)))
                out[k++] = iv[i];
        }
        return out;
    }

    private  int[] indexesForDay(String day)
    {
        int[] out = new int[nbDays/7];
        int idx = (ASAPDate.indexes.get(day)+6)%7;
        for (int i = 0 ; i < nbDays/7 ;i++)
        {
            out[i] = idx +(7*i);

        }
        return out;

    }


    private void solveModel(boolean all)
    {

        Solver s = new CPSolver();


        s.read(this);
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(ArrayUtils.flatten(ArrayUtils.transpose(shifts)))));
        s.monitorFailLimit(true);
        //s.setValIntSelector(new MaxVal());


        System.out.println("########## SOLVING... ##########");
        long t1 = System.currentTimeMillis();
        if (s.solve())
        {
            do {
            int i = 0;
            for(IntegerVariable[] ivt : shifts)
            {
                System.out.print(handler.orderedEmployees.get(i++)+" : ");
                for (IntDomainVar iv : s.getVar(ivt))
                {
                    System.out.print(StringUtils.pad(handler.inverseMap.get(iv.getVal()),4," "));
                }
                System.out.println("");
            }
            }
            while (all && s.nextSolution());
        }
        else System.out.println("NO SOLUTION");
        s.printRuntimeStatistics();





    }


    private String makeRegExpOfPattern(ASAPPattern p)
    {
        if (p.isWeekPattern() && !p.isComplete())
        {
            StringBuffer b = new StringBuffer();
            //String all = "(0|2|9)";//ShiftGroup.getFromId("All").toRegExp();
            String all = "(";
            for (ASAPShift s : handler.getShifts())
                all+= handler.map.get(s.getID())+"|";
            all = all.substring(0,all.length()-1)+")";


            if (p.getStartDay() == null)
            {


                b.append("((").append(all).append("*)").append(p.toRegExp()).append("(").append(all).append("*))");
            }
            else
            {
                StringBuffer b2 = new StringBuffer("(");
                int nd = p.size();
                int nbWeek = nbDays/7;
                int sd = ASAPDate.indexes.get(p.getStartDay());

                for (int w = 0 ; w < nbWeek ; w++)
                {
                    b2.append("(");
                    for (int i = 1; i < sd+(7*w) ;i++)
                        b2.append(all);
                    b2.append(p.toRegExp());
                    for (int i = (sd+7*w)+nd ; i <=nbDays ; i++)
                        b2.append(all);
                    b2.append(")|");
                }
                b2.deleteCharAt(b2.length()-1).append(")");
                b = b2;

                /* for (int i =0 ;i < nbWeek ;i++ )
               {
                   b.append(b2);
               } */

            }


            return b.toString();
        }
        else
            return p.toRegExp();

    }

    public void makeSoftPatternRule(ASAPPattern pat)
    {
        FiniteAutomaton auto = new FiniteAutomaton();
        ArrayList<ASAPPatternElement> elem = pat.pattern;

        int start = auto.addState();
        ASAPPatternElement first = elem.get(0);



    }

    public void instantiateMandatoryShift()
    {
        ASAPShiftOnRequest req = handler.getRequestOn();
        for (ASAPShiftOn so : req)
        {
            int didx = ASAPDate.getDaysBetween(handler.getStart(),so.getDate());
            int val = handler.map.get((so.getShift().getID()));
            int eidx = handler.orderedEmployees.indexOf(so.getEmployee());
            Constraint c = eq(shifts[eidx][didx],val);
            this.addConstraint(c);
        }
    }


    private void generateAutomatons()
    {
        int k = 0;
        for (ASAPContract c : handler.contracts.values())
        {
            int l = 0;
            HashSet<dk.brics.automaton.Automaton> hs = new HashSet<dk.brics.automaton.Automaton>();
            for (ASAPPattern p : c.getPatterns())
            {
                //System.out.println(makeRegExpOfPattern(p));
                String reg = makeRegExpOfPattern(p);
                System.out.println(reg);
                for (int i = 0 ; i < reg.length() ; i++)
                {
                    try {
                        alpha.add(Integer.parseInt(reg.charAt(i)+""));
                    }
                    catch (NumberFormatException ignored) {}
                }
                // System.out.println(reg);
                dk.brics.automaton.Automaton aa = new dk.brics.automaton.RegExp(StringUtils.toCharExp(reg)).toAutomaton();

                BufferedWriter out = null;

                if (!p.isBad())
                    aa = aa.complement();

                hs.add(aa);

            }
            k++;

            int nbStates = 0 ;
            int nbTra = 0;
            Iterator<dk.brics.automaton.Automaton> it = hs.iterator();
            dk.brics.automaton.Automaton a;
            if (it.hasNext())
            {
                a = it.next();
                while (it.hasNext())
                {
                    dk.brics.automaton.Automaton t = a.complement();
                    t.minimize();
                    nbStates+=t.getNumberOfStates();
                    nbTra+=t.getNumberOfTransitions();


                    a = a.union(it.next());
                    a.minimize();
                }
            }
            else
            {
              a = dk.brics.automaton.Automaton.makeEmpty();
                for (ASAPShift s : handler.getShifts())
                    alpha.add(handler.map.get(s.getID()));
            }
            a = a.complement();
            a.minimize();
           // System.out.println(a.getNumberOfStates()+"\t"+a.getNumberOfTransitions());
            //System.out.println(nbStates+"\t"+nbTra);


            FiniteAutomaton myA = new FiniteAutomaton();
            myA.fill(a,alpha);
            //myA.toDotty("myGreatAutomaton.dot");
            rules.put(c,myA);
        }



    }



    public void buildModel()
    {
        this.generateAutomatons();
        this.makeShiftVariables();
        this.instantiateMandatoryShift();
        this.postBadPatternConstraints();
        this.postVerticalGcc();
        this.makeColorMap();
    }

    private void makeColorMap() {
        Color[] all = new Color[]{Color.YELLOW,Color.BLUE,Color.RED,Color.LIGHT_GRAY,Color.ORANGE,Color.MAGENTA,Color.PINK,Color.CYAN};
        int i=0;
        for (String s : handler.map.keySet())
        {
            if (!s.equals("R"))
            {
                colormap.put(s,all[(i)%all.length]);
                i++;
            }

        }
        colormap.put("R",Color.LIGHT_GRAY);
        colormap.put("NO",Color.BLACK);

    }

    public Color getColor(String s)
    {
        return colormap.get(s);
    }


    public static void main(String[] args) {
       // String filename =   "/Users/julien/These/benchs/RosteringPbs/Azaiez.ros";
        //String filename = "/Users/julien/These/benchs/RosteringPbs/Millar-2Shift-DATA1.ros";
         String filename = "/Users/julien/These/benchs/RosteringPbs/GPost.ros";


       // String filename =   "/Users/julien/These/benchs/RosteringPbs/Ozkarahan.ros";
       // String filename =   "/Users/julien/These/benchs/RosteringPbs/Ikegami-2Shift-DATA1.ros";


        ASAPCPModel sched = new ASAPCPModel(filename);
        // sched.addWEPatterns();

        sched.generateAutomatons();
        sched.makeShiftVariables();
        sched.instantiateMandatoryShift();
        sched.postBadPatternConstraints();
        //sched.postBadPatternAndGccConstraints();
        sched.postVerticalGcc();

        sched.solveModel(false);


    }


}