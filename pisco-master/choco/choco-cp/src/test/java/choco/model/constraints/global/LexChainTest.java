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

package choco.model.constraints.global;

/**
 * Created by IntelliJ IDEA.
 * User: Ashish
 * Date: Jun 26, 2008
 * Time: 1:31:37 PM
 * LexChain test file
 */

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valiterator.DecreasingDomain;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.cp.solver.search.integer.valselector.MidVal;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Test;

import static java.lang.System.arraycopy;
import java.text.MessageFormat;
import java.util.Random;
import java.util.logging.Logger;

public class LexChainTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    public void lexChainTest1(){
        Model m = new CPModel();
        Solver s = new CPSolver();

        IntegerVariable[] ar1 = makeIntVarArray("v1", 3, 0, 10);
        IntegerVariable[] ar2 = makeIntVarArray("v2", 3, -1, 9);

        Constraint c = lexChain(ar1, ar2);
        m.addConstraint(c);

        s.read(m);
        if(s.solve()){
            do{
                s.getCstr(c).isSatisfied();
            }while(s.nextSolution());
        }

    }

    @Test
    public void lexChainTest2(){

        IntegerVariable[] ar1 = makeIntVarArray("v1", 1, 0, 10);
        IntegerVariable[] ar2 = makeIntVarArray("v2", 3, -1, 9);

        try{
            Constraint c = lexChain(ar1, ar2);
            Assert.fail("No exception thrown for the incorrect size of parameters");
        }catch (ModelException e){

        }
    }



    static final int numOfVectors = 3;
    static final int numOfVarInVectors = 2;
    static int minValue[] = new int[numOfVectors];
    static int maxValue[] = new int[numOfVectors];
    static int lowerbound, upperbound;
    static int numberOfSolutions = 0;
    static final int minSeed = 2;
    static final int maxSeed = 4;


    public static int compare(Solver s, IntegerVariable[] array1, IntegerVariable[] array2, String string, IntegerVariable[]... variables) throws Exception {

        numberOfSolutions = 0;
        int j = 0;

        do {
            StringBuffer st = new StringBuffer();
            st.append("{");
            for (IntegerVariable anArray1 : array1) {
                st.append(MessageFormat.format(" {0}", s.getVar(anArray1).getVal()));
            }

            st.append(MessageFormat.format("}   {0}  ", string));

            st.append("{");
            for (IntegerVariable anArray2 : array2) {
                st.append(MessageFormat.format(" {0}", s.getVar(anArray2).getVal()));
            }

            if (variables.length == 0) {
                st.append("}");
            } else {
                st.append(MessageFormat.format("}   {0}  ", string));

            }


            for (IntegerVariable[] array : variables) {
                ++j;
                st.append("{");
                for (IntegerVariable v : array) {
                    st.append(MessageFormat.format(" {0}", s.getVar(v).getVal()));
                }
                if (j != variables.length) {
                    st.append(MessageFormat.format("}   {0}  {1}", string, j));
                } else {
                    j = 0;
                    st.append("}");
                }
            }
            LOGGER.info(st.toString());

            ++numberOfSolutions;

        } while (s.nextSolution() == Boolean.TRUE);


        LOGGER.info("Number of solutions " + numberOfSolutions);
        LOGGER.info("*********************************************************************************");

        return numberOfSolutions;

    }


    public static void compareDiffStrategy(IntegerVariable[][] array, IntegerVariable[][] variableVector) {

        LOGGER.info("\n\n\n**********************Comparing lex_chain on different strategy********************* \n\n\n");
        Model model = new CPModel();

        Solver[] solver = new Solver[5];                    // error SOlver [] solver ;

        for (int i = 0; i < 5; i++) {
            solver[i] = new CPSolver();
        }

        IntegerVariable[][] tmp = new IntegerVariable[variableVector.length + 2][];
        tmp[0] = array[0];
        tmp[1] = array[1];
        arraycopy(variableVector, 0, tmp, 2, variableVector.length);

        model.addConstraint(lexChain(tmp));//array[0],array[1],variableVector));


        solver[0].read(model);
        solver[0].setValIntIterator(new IncreasingDomain());
        solver[0].setValIntSelector(new MaxVal());
        solver[0].solveAll();

        solver[1].read(model);
        solver[1].setValIntIterator(new DecreasingDomain());
        solver[1].setValIntSelector(new RandomIntValSelector());
        solver[1].solveAll();

        solver[2].read(model);
        solver[2].setValIntIterator(new IncreasingDomain());
        solver[2].setValIntSelector(new MidVal());
        solver[2].solveAll();


//        out.println(" number of solutions of lexChain - increasing domain & maxVal is  = " + solver[0].getNbSolutions());
//        out.print("increasing domain & maxVal   ");
//        solver[0].printRuntimeSatistics();
//        out.println(" number of solutions of lexChain - decreasing domain & random selector is  = " + solver[1].getNbSolutions());
//        out.print("decreasing domain & random selector   ");
//       solver[1].printRuntimeSatistics();

//        out.println(" number of solutions of lexChain - increasing  domain &  mid Value is  = " + solver[1].getNbSolutions());
//        out.print("increasing  domain & mid Value   ");
        solver[2].printRuntimeStatistics();
        Assert.assertEquals("Not same number of solution", solver[0].getNbSolutions(), solver[1].getNbSolutions());
        Assert.assertEquals("Not same number of solution", solver[1].getNbSolutions(), solver[2].getNbSolutions());

        LOGGER.info("**********************Comparing lex_chain on different strategy*********************\n\n\n ");
    }


    @Test
    public void main() throws Exception {

        for (int seed = 0; seed < 20; seed++) {
            Model m = new CPModel(),
                    m2 = new CPModel(),
                    m3 = new CPModel(),
                    m4 = new CPModel();

            Solver s = new CPSolver(),
                    s2 = new CPSolver(),
                    s3 = new CPSolver(),
                    s4 = new CPSolver();


            Random r1 = new Random(seed);
            minValue[0] = r1.nextInt(minSeed) + 4;
            for (int i = 1; i < numOfVectors; i++) {
                minValue[i] = minValue[i - 1] + r1.nextInt(minSeed);
            }
            Random r2 = new Random(seed);

            maxValue[0] = r2.nextInt(maxSeed) + minValue[0];
            for (int i = 1; i < numOfVectors; i++) {
                maxValue[i] = maxValue[i - 1] + r1.nextInt(maxSeed) + 1;
            }

            IntegerVariable[][] array = new IntegerVariable[2][numOfVarInVectors];
            IntegerVariable[][] variableVector = new IntegerVariable[numOfVectors - 2][numOfVarInVectors];
            for (int i = 0; i < numOfVectors; i++) {
                for (int j = 0; j < numOfVarInVectors; j++) {
                    if (j == 0) {

                        if (i >= 2) {
                            variableVector[i - 2][j] = makeIntVar("X" + i + j, minValue[i], maxValue[i]);
                        } else {
                            array[i][j] = makeIntVar("X" + i + j, minValue[i], maxValue[i]);
                        }
                    } else {

                        lowerbound = r1.nextInt(minSeed);
                        upperbound = r2.nextInt(maxSeed) + lowerbound;                        // incase both are zero
                        if (i >= 2) {
                            variableVector[i - 2][j] = makeIntVar("X" + i + j, lowerbound, upperbound);
                        } else {
                            array[i][j] = makeIntVar("X" + i + j, lowerbound, upperbound);
                        }

                    }

                }
            }


            m.addConstraint(lexChainEq(array[0], array[1]));
            m2.addConstraint(lexEq(array[0], array[1]));
            m3.addConstraint(lexChain(array[0], array[1]));
            m4.addConstraint(lex(array[0], array[1]));

            s.read(m);
            s2.read(m2);
            s3.read(m3);
            s4.read(m4);


            s.solveAll();

            //compare(s, array[0], array[1], " < lexChainEq");

            s2.solveAll();
            //compare(s2, array[0], array[1], "  <lexeq ");


            s3.solveAll();
            //compare(s3, array[0], array[1], " < lexChain");
            s4.solveAll();
            //compare(s4, array[0], array[1], "  <lex ");
            Assert.assertEquals("Not same number of solution (seed="+seed+")", s.getNbSolutions(), s2.getNbSolutions());
            Assert.assertEquals("Not same number of solution (seed="+seed+")", s3.getNbSolutions(), s4.getNbSolutions());


            Model mmm = new CPModel();
            Solver sss = new CPSolver();
            IntegerVariable[][] tmp = new IntegerVariable[variableVector.length + 2][];
            tmp[0] = array[0];
            tmp[1] = array[1];
            arraycopy(variableVector, 0, tmp, 2, variableVector.length);
            mmm.addConstraint(lexChainEq(tmp));
            sss.read(mmm);
            sss.solve();
//            compare(sss, array[0], array[1], " <= lexChainEq ", variableVector);                             // no printing of solution

            compareDiffStrategy(array, variableVector);
        }
    }
}

