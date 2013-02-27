// -------------------------------------------------------------- -*- C++ -*-
// File: ./examples/src/cpp/sched_cumul.cpp
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5725-A06 5725-A29
// Copyright IBM Corporation 1990, 2010. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
// --------------------------------------------------------------------------

/* ------------------------------------------------------------

Problem Description
-------------------

This is a problem of building five houses in different locations. The
masonry, roofing, painting, etc. must be scheduled. Some tasks must
necessarily take place before others and these requirements are
expressed through precedence constraints.

There are three workers, and each task requires a worker.  There is
also a cash budget which starts with a given balance.  Each task costs
a given amount of cash per day which must be available at the start of
the task.  A cash payment is received periodically.  The objective is
to minimize the overall completion date.

------------------------------------------------------------ */

#include <ilcp/cp.h>

const IloInt NbWorkers = 3;
const IloInt NbTasks = 10;

enum Tasks {
  masonry   = 0,
  carpentry = 1,
  plumbing  = 2,
  ceiling   = 3,
  roofing   = 4,
  painting  = 5,
  windows   = 6,
  facade    = 7,
  garden    = 8,
  moving    = 9
};

const char* TaskNames [] = {
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

const IloInt TaskDurations [] = {
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

void MakeHouse(IloModel model,
                IloInt id,
                IloInt rd,
                IloCumulFunctionExpr& workersUsage,
                IloCumulFunctionExpr& cash,
                IloIntExprArray ends,
                IloIntervalVarArray allTasks) {
  IloEnv env = model.getEnv();

  /* CREATE THE INTERVAL VARIABLES. */
  char name[128];
  IloIntervalVarArray tasks(env, NbTasks);
  for (IloInt i=0; i<NbTasks; ++i) {
    sprintf(name, "H%ld-%s", id, TaskNames[i]);
    IloIntervalVar task(env, TaskDurations[i], name);
    tasks[i] = task;
    allTasks.add(task);
    workersUsage += IloPulse(task, 1);
    cash -= IloStepAtStart(task, 200 * TaskDurations[i]);
  }

  /* ADDING TEMPORAL CONSTRAINTS. */
  tasks[masonry].setStartMin(rd);
  model.add(IloEndBeforeStart(env, tasks[masonry], tasks[carpentry]));
  model.add(IloEndBeforeStart(env, tasks[masonry], tasks[plumbing]));
  model.add(IloEndBeforeStart(env, tasks[masonry], tasks[ceiling]));
  model.add(IloEndBeforeStart(env, tasks[carpentry], tasks[roofing]));
  model.add(IloEndBeforeStart(env, tasks[ceiling], tasks[painting]));
  model.add(IloEndBeforeStart(env, tasks[roofing], tasks[windows]));
  model.add(IloEndBeforeStart(env, tasks[roofing], tasks[facade]));
  model.add(IloEndBeforeStart(env, tasks[plumbing], tasks[facade]));
  model.add(IloEndBeforeStart(env, tasks[roofing], tasks[garden]));
  model.add(IloEndBeforeStart(env, tasks[plumbing], tasks[garden]));
  model.add(IloEndBeforeStart(env, tasks[windows], tasks[moving]));
  model.add(IloEndBeforeStart(env, tasks[facade], tasks[moving]));
  model.add(IloEndBeforeStart(env, tasks[garden], tasks[moving]));
  model.add(IloEndBeforeStart(env, tasks[painting], tasks[moving]));

  /* DEFINING MINIMIZATION OBJECTIVE. */
  ends.add(IloEndOf(tasks[moving]));
}

int main(int , const char * []) {
  IloEnv env;
  try {
    IloModel model(env);

    IloCumulFunctionExpr workersUsage(env);
    IloCumulFunctionExpr cash(env);
    IloIntExprArray ends(env);
    IloIntervalVarArray allTasks(env);

    /* CASH PAYMENTS. */
    for (IloInt p=0; p<5; ++p)
      cash += IloStep(env, 60*p, 30000);

    MakeHouse(model, 0,  31, workersUsage, cash, ends, allTasks);
    MakeHouse(model, 1,   0, workersUsage, cash, ends, allTasks);
    MakeHouse(model, 2,  90, workersUsage, cash, ends, allTasks);
    MakeHouse(model, 3, 120, workersUsage, cash, ends, allTasks);
    MakeHouse(model, 4,  90, workersUsage, cash, ends, allTasks);

    model.add(0 <= cash);

    model.add(workersUsage <= NbWorkers);

    model.add(IloMinimize(env, IloMax(ends)));

    /* EXTRACTING THE MODEL AND SOLVING. */
    IloCP cp(model);
    cp.setParameter(IloCP::FailLimit, 10000);
    if (cp.solve()) {
      cp.out() << "Solution with objective " << cp.getObjValue() << ":" << std::endl;
      for (IloInt i=0; i<allTasks.getSize(); ++i) {
        cp.out() << cp.domain(allTasks[i]) << std::endl;
      }
      IloInt segs = cp.getNumberOfSegments(cash);
      for (IloInt i = 0; i < segs; i++) {
        cp.out() << "Cash is " << cp.getSegmentValue(cash, i) <<
                    " from " << cp.getSegmentStart(cash, i) <<
                    " to " << cp.getSegmentEnd(cash, i) - 1 << std::endl;
      }
    } else {
      cp.out() << "No solution found. " << std::endl;
    }
  } catch (IloException& ex) {
    env.out() << "Error: " << ex << std::endl;
  }
  env.end();
  return 0;
}

/*
Solution with objective 285:
H0-masonry  [1: 31 -- 35 --> 66]
H0-carpentry[1: 66 -- 15 --> 81]
H0-plumbing [1: 81 -- 40 --> 121]
H0-ceiling  [1: 70 -- 15 --> 85]
H0-roofing  [1: 85 -- 5 --> 90]
H0-painting [1: 110 -- 10 --> 120]
H0-windows  [1: 95 -- 5 --> 100]
H0-facade   [1: 255 -- 10 --> 265]
H0-garden   [1: 240 -- 5 --> 245]
H0-moving   [1: 270 -- 5 --> 275]
H1-masonry  [1: 0 -- 35 --> 35]
H1-carpentry[1: 35 -- 15 --> 50]
H1-plumbing [1: 50 -- 40 --> 90]
H1-ceiling  [1: 35 -- 15 --> 50]
H1-roofing  [1: 50 -- 5 --> 55]
H1-painting [1: 60 -- 10 --> 70]
H1-windows  [1: 55 -- 5 --> 60]
H1-facade   [1: 100 -- 10 --> 110]
H1-garden   [1: 90 -- 5 --> 95]
H1-moving   [1: 280 -- 5 --> 285]
H2-masonry  [1: 120 -- 35 --> 155]
H2-carpentry[1: 155 -- 15 --> 170]
H2-plumbing [1: 195 -- 40 --> 235]
H2-ceiling  [1: 205 -- 15 --> 220]
H2-roofing  [1: 195 -- 5 --> 200]
H2-painting [1: 265 -- 10 --> 275]
H2-windows  [1: 270 -- 5 --> 275]
H2-facade   [1: 240 -- 10 --> 250]
H2-garden   [1: 250 -- 5 --> 255]
H2-moving   [1: 275 -- 5 --> 280]
H3-masonry  [1: 121 -- 35 --> 156]
H3-carpentry[1: 180 -- 15 --> 195]
H3-plumbing [1: 195 -- 40 --> 235]
H3-ceiling  [1: 180 -- 15 --> 195]
H3-roofing  [1: 200 -- 5 --> 205]
H3-painting [1: 255 -- 10 --> 265]
H3-windows  [1: 250 -- 5 --> 255]
H3-facade   [1: 255 -- 10 --> 265]
H3-garden   [1: 265 -- 5 --> 270]
H3-moving   [1: 275 -- 5 --> 280]
H4-masonry  [1: 90 -- 35 --> 125]
H4-carpentry[1: 125 -- 15 --> 140]
H4-plumbing [1: 140 -- 40 --> 180]
H4-ceiling  [1: 180 -- 15 --> 195]
H4-roofing  [1: 156 -- 5 --> 161]
H4-painting [1: 245 -- 10 --> 255]
H4-windows  [1: 161 -- 5 --> 166]
H4-facade   [1: 240 -- 10 --> 250]
H4-garden   [1: 265 -- 5 --> 270]
H4-moving   [1: 275 -- 5 --> 280]
*/
