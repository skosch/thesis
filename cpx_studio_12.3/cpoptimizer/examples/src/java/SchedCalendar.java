// ---------------------------------------------------------------*- Java -*-
// File: ./examples/src/java/SchedCalendar.java
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

This is a problem of building five houses. The masonry, roofing,
painting, etc. must be scheduled.  Some tasks must necessarily take
place before others and these requirements are expressed through
precedence constraints.

There are two workers and each task requires a specific worker.  The
worker has a calendar of days off that must be taken into account. The
objective is to minimize the overall completion date.

------------------------------------------------------------ */

//$doc:ALL
//$doc:HEADERS
import ilog.concert.*;
import ilog.cp.*;
import java.util.List;
import java.util.ArrayList;
//end:HEADERS

public class SchedCalendar {
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

//$doc:MAKEHOUSEARG
    public static void makeHouse(IloCP cp,
                                 int id,
                                 List<IloIntExpr> ends,
                                 List<IloIntervalVar> allTasks,
                                 List<IloIntervalVar> joeTasks,
                                 List<IloIntervalVar> jimTasks)
        throws IloException {
//end:MAKEHOUSEARG

        /* CREATE THE INTERVAL VARIABLES. */
//$doc:TASKVAR
        String name;
        IloIntervalVar[] tasks = new IloIntervalVar[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            name = "H" + id + "-" + taskNames[i];
            tasks[i] = cp.intervalVar(taskDurations[i], name);
            allTasks.add(tasks[i]);
        }
//end:TASKVAR

        /* ADDING PRECEDENCE CONSTRAINTS. */
//$doc:CSTS
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

        /* ADDING WORKER TASKS. */
//$doc:WORKERTASKS
        joeTasks.add(tasks[masonry]);
        joeTasks.add(tasks[carpentry]);
        jimTasks.add(tasks[plumbing]);
        jimTasks.add(tasks[ceiling]);
        joeTasks.add(tasks[roofing]);
        jimTasks.add(tasks[painting]);
        jimTasks.add(tasks[windows]);
        joeTasks.add(tasks[facade]);
        joeTasks.add(tasks[garden]);
        jimTasks.add(tasks[moving]);
//end:WORKERTASKS

        /* DEFINING MINIMIZATION OBJECTIVE. */
//$doc:ENDCOST
        ends.add(cp.endOf(tasks[moving]));
//end:ENDCOST
    }

    static IloIntExpr[] arrayFromList(List<IloIntExpr> list) {
        return (IloIntExpr[]) list.toArray(new IloIntExpr[list.size()]);
    }

//$doc:MAIN
    public static void main(String[] args) {
        try {
//$doc:CP
            IloCP cp = new IloCP();
//end:CP
//end:MAIN

//$doc:VARS
            int nbHouses = 5;
            List<IloIntExpr> ends = new ArrayList<IloIntExpr>();
            List<IloIntervalVar> allTasks = new ArrayList<IloIntervalVar>();
            List<IloIntervalVar> joeTasks = new ArrayList<IloIntervalVar>();
            List<IloIntervalVar> jimTasks = new ArrayList<IloIntervalVar>();
//end:VARS

//$doc:MAKEHOUSE
            for (int h = 0; h < nbHouses; h++) {
                makeHouse(cp, h, ends, allTasks, joeTasks, jimTasks);
            }
//end:MAKEHOUSE

//$doc:NOOVERLAP
            cp.add(cp.noOverlap((IloIntervalVar[]) joeTasks.toArray(new IloIntervalVar[joeTasks.size()])));
            cp.add(cp.noOverlap((IloIntervalVar[]) jimTasks.toArray(new IloIntervalVar[jimTasks.size()])));
//end:NOOVERLAP

//$doc:EFFFN
            IloNumToNumStepFunction joeCalendar = cp.numToNumStepFunction(0, 2 * 365, 100);
            IloNumToNumStepFunction jimCalendar = cp.numToNumStepFunction(0, 2 * 365, 100);
//end:EFFFN
//$doc:FNVAL
            /* WEEK ENDS. */
            for (int w = 0; w < 2 * 52; w++) {
                joeCalendar.setValue(5 + (7 * w), 7 + (7 * w), 0);
                jimCalendar.setValue(5 + (7 * w), 7 + (7 * w), 0);
            }

            /* HOLIDAYS. */
            joeCalendar.setValue(5, 12, 0);
            joeCalendar.setValue(124, 131, 0);
            joeCalendar.setValue(215, 236, 0);
            joeCalendar.setValue(369, 376, 0);
            joeCalendar.setValue(495, 502, 0);
            joeCalendar.setValue(579, 600, 0);
            jimCalendar.setValue(26, 40, 0);
            jimCalendar.setValue(201, 225, 0);
            jimCalendar.setValue(306, 313, 0);
            jimCalendar.setValue(397, 411, 0);
            jimCalendar.setValue(565, 579, 0);
//end:FNVAL

//$doc:FORBID
            for (int i = 0; i < joeTasks.size(); i++) {
                joeTasks.get(i).setIntensity(joeCalendar);
                cp.add(cp.forbidStart(joeTasks.get(i), joeCalendar));
                cp.add(cp.forbidEnd(joeTasks.get(i), joeCalendar));
            }
            for (int i = 0; i < jimTasks.size(); i++) {
                jimTasks.get(i).setIntensity(jimCalendar);
                cp.add(cp.forbidStart(jimTasks.get(i), jimCalendar));
                cp.add(cp.forbidEnd(jimTasks.get(i), jimCalendar));
            }
//end:FORBID

//$doc:OBJ
            cp.add(cp.minimize(cp.max(arrayFromList(ends))));
//end:OBJ

            /* EXTRACTING THE MODEL AND SOLVING. */
//$doc:SOLVE
            cp.setParameter(IloCP.IntParam.FailLimit, 10000);
            if (cp.solve()) {
//end:SOLVE
//$doc:SOLN
                for (int i = 0; i < allTasks.size(); i++) {
                    System.out.println(cp.getDomain(allTasks.get(i)));
                }
//end:SOLN
            } else {
                System.out.println("No Solution found.");
            }
        } catch (IloException e) {
            System.err.println("Error " + e );
        }
    }
}
//end:ALL
