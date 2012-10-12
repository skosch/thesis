// ---------------------------------------------------------------*- Java -*-
// File: ./examples/src/java/SearchPhases.java
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5725-A06 5725-A29
// Copyright IBM Corporation 1990, 2011. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
// --------------------------------------------------------------------------

/* ------------------------------------------------------------

How to write custom search phases
---------------------------------

------------------------------------------------------------ */

import ilog.cp.*;
import ilog.concert.*;

public class SearchPhases {
    public static void main(String[] args) throws Exception {
        try {
            IloCP cp = new IloCP();
            IloIntVar[] x = new IloIntVar[10];
            for (int i = 0; i < 10; i++) {
                String name = "X" + Integer.toString(i);
                x[i] = cp.intVar(0, 100 - 2*(i / 2), name);
            }
            cp.add(cp.allDiff(x));

            IloIntVarChooser varChooser   = new SearchPhases.ChooseSmallestCentroid(cp);
            IloIntValueChooser valChooser = new SearchPhases.ChooseSmallestDistanceFromCentroid(cp);
            IloSearchPhase sp1 = cp.searchPhase(x, varChooser, valChooser);

            IloIntVarEval   varEval       = new SearchPhases.Centroid(cp);
            IloIntValueEval valEval       = new SearchPhases.DistanceFromCentroid(cp);
            IloSearchPhase sp2 = cp.searchPhase(x,
                                                cp.intVarChooser(cp.selectSmallest(varEval)),
                                                cp.intValueChooser(cp.selectSmallest(valEval)));

            // sp2 can have ties as two variable or values could evaluate
            // to the same values.  sp3 shows how to break these ties
            // choosing, for equivalent centroid and distance-to-centroid
            // evaluations, the lowest indexed variable in x and the 
            // lowest value.
            IloVarSelector[] selVar = new IloVarSelector[1];
            selVar[0] = cp.selectSmallest(varEval);

            IloValueSelector[] selValue = new IloValueSelector[2];
            selValue[0] = cp.selectSmallest(valEval);
            selValue[1] = cp.selectSmallest(cp.value()); // break ties on smallest

            IloSearchPhase sp3 = cp.searchPhase(x,
                                                cp.intVarChooser(selVar),
                                                cp.intValueChooser(selValue));

            cp.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.DepthFirst);
            cp.setParameter(IloCP.IntParam.LogPeriod, 1);

            System.out.println("Choosers");
            cp.solve(sp1);
            printIntVars(cp, x, "X");
            System.out.println("Evaluators");
            cp.solve(sp2);
            printIntVars(cp, x, "X");
            System.out.println("Evaluators (with tie-break)");
            cp.solve(sp3);
            printIntVars(cp, x, "X");

        } catch (IloException e) {
            System.err.println("Error " + e);
        }
    }

    // Example: variable is evaluated according to the average of
    //          the bounds of the variable
    public static double CalcCentroid(IloCP cp, IloIntVar var) {
        return 0.5 * (cp.getMin(var) + cp.getMax(var));
    };
    
    // Custom variable evaluator
    static public class Centroid extends IloCustomIntVarEval {
        public Centroid(IloCP cp) throws IloException {
            super(cp);
        }
        public double eval(IloCP cp, IloIntVar var) {
            return SearchPhases.CalcCentroid(cp, var);
        };
    }

    // Custom variable chooser
    static public class ChooseSmallestCentroid extends IloCustomIntVarChooser {
        public ChooseSmallestCentroid(IloCP cp) throws IloException {
            super(cp);
        };
        // Example: choose the variable with the smallest centroid
        //          Note that this class is merely for instruction
        //          In reality, it would be easier to use the above
        //          evaluator class and write:
        //            cp.selectSmallest(new Centroid(cp))
        //          rather than:
        //            ChooseSmallestCentroid(cp)
        public int choose(IloCP cp, IloIntVar[] vars) {
            double best = Double.MAX_VALUE;
            int bestIndex = -1;
            int n = vars.length;
            for (int i = 0; i < n; i++) {
                if (!cp.isFixed(vars[i])) {
                    double c = SearchPhases.CalcCentroid(cp, vars[i]);
                    if (c < best) {
                        best = c;
                        bestIndex = i;
                    }
                }
            } 
            return bestIndex;
        }
    }

    // Custom value evaluator
    static public class DistanceFromCentroid extends IloCustomIntValueEval {
        public DistanceFromCentroid(IloCP cp) throws IloException {
            super(cp);
        }
        // Example: value's evaluation is its distance
        //          from the centroid
        public double eval(IloCP cp, IloIntVar var, int value) {
            return Math.abs(SearchPhases.CalcCentroid(cp, var) - value);
        }
    }


    // Custom value chooser
    static public class ChooseSmallestDistanceFromCentroid extends IloCustomIntValueChooser {
        public ChooseSmallestDistanceFromCentroid(IloCP cp) throws IloException {
            super(cp);
        }
        // Example: choose the value with the smallest distance to
        //          the centroid.
        public int choose(IloCP cp, IloIntVar[] vars, int i) {
            IloIntVar var = vars[i];
            double best = Double.MAX_VALUE;
            int bestValue = (int)cp.getMin(var);
            double centroid = SearchPhases.CalcCentroid(cp, var);
            java.util.Iterator it = cp.iterator(var); 
            while (it.hasNext()) {
                int curr = ((Integer)(it.next())).intValue();
                double eval = Math.abs(centroid - curr);
                if (eval < best) {
                    best      = eval;
                    bestValue = curr;
                }
            }
            return bestValue;
        }
    }

    public static void printIntVars(IloCP cp, IloIntVar[] x, String name) throws Exception {
        System.out.print("[");
        for (int i = 0; i < x.length; i++) {
            if (i>0)
                System.out.print(" ");
            int value = cp.getIntValue(x[i]);
            System.out.print(name + i + "[" + value + "]");
        }
        System.out.println("]");
    }
        
        
}

