// ---------------------------------------------------------------*- Java -*-
// File: ./examples/src/java/SchedSequence.java
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5725-A06 5725-A29
// Copyright IBM Corporation 1990, 2011. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
// ----------------------------------------------------------------------------

/* ------------------------------------------------------------

Problem Description
-------------------

This is a problem of building five houses in different locations. The
masonry, roofing, painting, etc. must be scheduled. Some tasks must
necessarily take place before others and these requirements are
expressed through precedence constraints.

There are two workers, and each task requires a specific worker.  The
time required for the workers to travel between houses must be taken
into account.  

Moreover, there are tardiness costs associated with some tasks as well
as a cost associated with the length of time it takes to build each
house.  The objective is to minimize these costs.

------------------------------------------------------------ */

//$doc:ALL
//$doc:HEADERS
import ilog.concert.*;
import ilog.cp.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
//end:HEADERS

public class SchedSequence {

//$doc:TASKS
    static final int nbTasks = 10;

    static final int masonry   = 0;
    static final int carpentry = 1;
    static final int plumbing  = 2;
    static final int ceiling   = 3;
    static final int roofing   = 4;
    static final int painting  = 5;
    static final int windows   = 6;
    static final int facade    = 7;
    static final int garden    = 8;
    static final int moving    = 9;

    static final String[] taskNames = {
        "masonry  ",
        "carpentry",
        "plumbing ",
        "ceiling  ",
        "roofing  ",
        "painting ",
        "windows  ",
        "facade   ",
        "garden   ",
        "moving   "
    };

    static final int[] taskDurations = {
        35,
        15,
        40,
        15,
        05,
        10,
        05,
        10,
        05,
        05,
    };
//end:TASKS

    static IloCP cp = new IloCP();
    static IloNumExpr cost;

//$doc:TARDYCOST
    public static IloNumExpr tardinessCost(IloIntervalVar task, int dd, double weight) throws IloException {
        return cp.prod(weight, cp.max(0, cp.diff(cp.endOf(task), dd)));
    }
//end:TARDYCOST

//$doc:MAKEHOUSEARG

    public static void makeHouse (List<IloIntervalVar> allTasks,
                                  List<IloIntervalVar> joeTasks,
                                  List<IloIntervalVar> jimTasks,
                                  List<Integer> joeLocations,
                                  List<Integer> jimLocations,
                                  int loc,
                                  int rd,
                                  int dd,
                                  double weight)
//end:MAKEHOUSEARG
        throws IloException {

        /* CREATE THE INTERVAL VARIABLES. */
//$doc:TASKVAR
        String name = "H" + loc;

        IloIntervalVar[] tasks = new IloIntervalVar[nbTasks];
        for (int i = 0; i < nbTasks; i++ ) {
            name = "H" + loc + "-" + taskNames[i];
            tasks[i] = cp.intervalVar(taskDurations[i], name);
            allTasks.add(tasks[i]);
        }
//end:TASKVAR

        /* SPAN CONSTRAINT. */
//$doc:HOUSEVAR
        IloIntervalVar house = cp.intervalVar(name);
        cp.add(cp.span(house, tasks));
//end:HOUSEVAR

        /* ADDING PRECEDENCE CONSTRAINTS. */
//$doc:CSTS
        house.setStartMin(rd);
        cp.add(cp.endBeforeStart(tasks[masonry],   tasks[carpentry]));
        cp.add(cp.endBeforeStart(tasks[masonry],   tasks[plumbing]));
        cp.add(cp.endBeforeStart(tasks[masonry],   tasks[ceiling]));
        cp.add(cp.endBeforeStart(tasks[carpentry], tasks[roofing]));
        cp.add(cp.endBeforeStart(tasks[ceiling],   tasks[painting]));
        cp.add(cp.endBeforeStart(tasks[roofing],   tasks[windows]));
        cp.add(cp.endBeforeStart(tasks[roofing],   tasks[facade]));
        cp.add(cp.endBeforeStart(tasks[plumbing],  tasks[facade]));
        cp.add(cp.endBeforeStart(tasks[roofing],   tasks[garden]));
        cp.add(cp.endBeforeStart(tasks[plumbing],  tasks[garden]));
        cp.add(cp.endBeforeStart(tasks[windows],   tasks[moving]));
        cp.add(cp.endBeforeStart(tasks[facade],    tasks[moving]));
        cp.add(cp.endBeforeStart(tasks[garden],    tasks[moving]));
        cp.add(cp.endBeforeStart(tasks[painting],  tasks[moving]));
//end:CSTS

        /* ALLOCATING TASKS TO WORKERS. */
//$doc:WORKERTASKS
        joeTasks.add(tasks[masonry]);
        joeLocations.add(loc);
        joeTasks.add(tasks[carpentry]);
        joeLocations.add(loc);
        jimTasks.add(tasks[plumbing]);
        jimLocations.add(loc);
        jimTasks.add(tasks[ceiling]);
        jimLocations.add(loc);
        joeTasks.add(tasks[roofing]);
        joeLocations.add(loc);
        jimTasks.add(tasks[painting]);
        jimLocations.add(loc);
        jimTasks.add(tasks[windows]);
        jimLocations.add(loc);
        joeTasks.add(tasks[facade]);
        joeLocations.add(loc);
        joeTasks.add(tasks[garden]);
        joeLocations.add(loc);
        jimTasks.add(tasks[moving]);
        jimLocations.add(loc);
//end:WORKERTASKS

        /* DEFINING MINIMIZATION OBJECTIVE. */
//$doc:MAKECOST
        cost = cp.sum(cost, tardinessCost(house, dd, weight));
        cost = cp.sum(cost, cp.lengthOf(house));
//end:MAKECOST
    }

    static IloIntervalVar[] intervalVarsToArray(List<IloIntervalVar> list) {
        return (IloIntervalVar[]) list.toArray(new IloIntervalVar[list.size()]);
    }

    static int[] integersToArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++)
            array[i] = list.get(i);
        return array;
    }

//$doc:MAIN
    public static void main(String[] args) {
        try {
//end:MAIN

//$doc:VARS
            cost = cp.numExpr();
            List<IloIntervalVar> allTasks = new ArrayList<IloIntervalVar>();
            List<IloIntervalVar> joeTasks = new ArrayList<IloIntervalVar>();
            List<IloIntervalVar> jimTasks = new ArrayList<IloIntervalVar>();
            List<Integer> joeLocations = new ArrayList<Integer>();
            List<Integer> jimLocations = new ArrayList<Integer>();
//end:VARS

//$doc:MAKEHOUSE
            makeHouse(allTasks, joeTasks, jimTasks, joeLocations, jimLocations, 0, 0,   120, 100.0);
            makeHouse(allTasks, joeTasks, jimTasks, joeLocations, jimLocations, 1, 0,   212, 100.0);
            makeHouse(allTasks, joeTasks, jimTasks, joeLocations, jimLocations, 2, 151, 304, 100.0);
            makeHouse(allTasks, joeTasks, jimTasks, joeLocations, jimLocations, 3, 59,  181, 200.0);
            makeHouse(allTasks, joeTasks, jimTasks, joeLocations, jimLocations, 4, 243, 425, 100.0);
//end:MAKEHOUSE

//$doc:TT
            IloTransitionDistance tt = cp.transitionDistance(5);
            for (int i = 0; i < 5; ++i)
                for (int j = 0; j < 5; ++j)
                    tt.setValue(i, j, java.lang.Math.abs(i - j));
//end:TT

//$doc:SEQVAR
            IloIntervalSequenceVar joe = cp.intervalSequenceVar(intervalVarsToArray(joeTasks), integersToArray(joeLocations), "Joe");
            IloIntervalSequenceVar jim = cp.intervalSequenceVar(intervalVarsToArray(jimTasks), integersToArray(jimLocations), "Jim");
//end:SEQVAR

//$doc:NOOVERLAP
            cp.add(cp.noOverlap(joe, tt));
            cp.add(cp.noOverlap(jim, tt));
//end:NOOVERLAP

//$doc:OBJ
            cp.add(cp.minimize(cost));
//end:OBJ

//$doc:SOLVE
            cp.setParameter(IloCP.IntParam.FailLimit, 50000);
            /* EXTRACTING THE MODEL AND SOLVING. */
            if (cp.solve()) {
//end:SOLVE
//$doc:SOLN
                System.out.println("Solution with objective " + cp.getObjValue() + ":");
                for (int i = 0; i < allTasks.size(); ++i)
                    System.out.println(cp.getDomain(allTasks.get(i)));
//end:SOLN
            } else {
                System.out.println("No solution found.");
            }
        } catch (IloException e) {
            System.err.println("Error: " + e);
        }
    }
}
//end:ALL
