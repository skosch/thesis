// ---------------------------------------------------------------*- Java -*-
// File: ./examples/src/java/SchedJobShop.java
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

Problem Description
-------------------

This example is an extension of the classical Job-Shop Scheduling
problem (see sched_jobshop.cpp) with a learning effect on machines:
because of experience acquired by the machine, executing an
operation at position i on the machine will require less time than
if it were executed earlier at a position k < i.

More formally, each machine M_j has a learning factor alpha_j in [0,1]
such that the actual processing time of the operation executed at the
ith position on machine M_j is the decreasing function
d_j(i) = D * pow(alpha_j,i) where D is the nominal processing time of
operation.

The model for a resource, except for the classical no-overlap constraint,
consists of a chain of intervals of unknown size that forms a one-to-one
correspondance with the actual operations. The correspondance (made using
an isomorphism constraint) associates an integer variable (the position)
with each operation of the resource.  The position variable is used to
define the processing time of an operation subject to the learning effect.

This example illustrates the typical usage of the isomorphism constraint
to express relations according to the rank order of operations and to
get the position of interval variables in a sequence.

------------------------------------------------------------ */

import ilog.concert.*;
import ilog.cp.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SchedLearningEffect {

    static class DataReader {

        private StreamTokenizer st;

        public DataReader(String filename) throws IOException {
            FileInputStream fstream = new FileInputStream(filename);
            Reader r = new BufferedReader(new InputStreamReader(fstream));
            st = new StreamTokenizer(r);
        }

        public int next() throws IOException {
            st.nextToken();
            return (int) st.nval;
        }
    }

    static class IntervalVarList extends ArrayList<IloIntervalVar> {
        public IloIntervalVar[] toArray() {
            return (IloIntervalVar[]) this.toArray(new IloIntervalVar[this.size()]);
        }
    }

    static IloIntExpr[] arrayFromList(List<IloIntExpr> list) {
        return (IloIntExpr[]) list.toArray(new IloIntExpr[list.size()]);
    }

    public static void main(String[] args) throws IOException {

        String filename = "../../../examples/data/learningeffect_default.data";
        int failLimit = 10000;
        int nbJobs, nbMachines;

        if (args.length > 0)
            filename = args[0];
        if (args.length > 1)
            failLimit = Integer.parseInt(args[1]);

        IloCP cp = new IloCP();
        DataReader data = new DataReader(filename);
        try {
            nbJobs = data.next();
            nbMachines = data.next();
            IloIntervalVar[][] machines =  new IloIntervalVar[nbMachines][nbJobs];
            int[][] sizes = new int[nbMachines][nbJobs];
            IloIntExpr[] ends = new IloIntExpr[nbJobs];
            for (int i = 0; i < nbJobs; i++) {
                IloIntervalVar prec = cp.intervalVar();
                for (int j = 0; j < nbMachines; j++) {
                    int m, d;
                    m = data.next();
                    d = data.next();
                    IloIntervalVar ti = cp.intervalVar();
                    machines[m][i] = ti;
                    sizes[m][i] = d;
                    if (j > 0) {
                        cp.add(cp.endBeforeStart(prec, ti));
                    }
                    prec = ti;
                }
                ends[i] = cp.endOf(prec);
            }
            
            
            for (int j = 0; j < nbMachines; j++) {
                double alpha = data.next() / 100.;
                IloIntervalVar[] chain = new IloIntervalVar[nbJobs];
                IloIntExpr[] indices = new IloIntVar[nbJobs];
                IloIntervalVar prec = cp.intervalVar();
                for (int i = 0; i < nbJobs; i++) {
                    IloIntervalVar syncti = cp.intervalVar();
                    if (i > 0)
                        cp.add(cp.endBeforeStart(prec, syncti));
                    prec = syncti;
                    chain[i] = syncti;
                    IloIntVar index = cp.intVar(0, nbJobs -1);
                    indices[i] = index;
                    // Learning effect captured by the decreasing function
                    // of the position (0 <= alpha <= 1).
                    // At first position, in the sequence index = 0; there is no
                    // learning effect and duration of the task is its nominal duration
                    cp.add(cp.eq(cp.sizeOf(machines[j][i]), 
                                 cp.floor(cp.prod(sizes[j][i], 
                                                  cp.power(alpha, index)))));
                }
                cp.add(cp.isomorphism(chain, machines[j], indices, nbJobs));
                // The no-overlap is in a redundant constraint in this quite simple model,
                // but it is used to provide stronger inference. In a practical model, 
                // there were exist constraints on the sequence that would require the 
                // no-overlap constraint.
                cp.add(cp.noOverlap(machines[j]));
            }
            IloObjective objective = cp.minimize(cp.max(ends));
            cp.add(objective);
            
            cp.setParameter(IloCP.IntParam.FailLimit, failLimit);
            System.out.println("Instance \t: " + filename);
            if (cp.solve()) {
                System.out.println("Makespan \t: " + cp.getObjValue());
            } else {
                System.out.println("No solution found.");
            }
        } catch (IloException e) {
            System.err.println("Error: " + e);
        }
    }
}
