// -------------------------------------------------------------- -*- C++ -*-
// File: ./examples/tutorial/cpp/sched_optional_partial.cpp
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

#include <ilcp/cp.h>

IloInt NbWorkers = 3;
IloInt NbTasks = 10;

enum Workers {
  joe       = 0,
  jack      = 1,
  jim       = 2
};

char* WorkerNames [] = {
  "Joe",
  "Jack",
  "Jim"
};

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

char* TaskNames [] = {
  "masonry",
  "carpentry",
  "plumbing",
  "ceiling",
  "roofing",
  "painting",
  "windows",
  "facade",
  "garden",
  "moving"
};

IloInt TaskDurations [] = {
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

IloInt SkillsMatrix [] = {
  // Joe, Jack, Jim
  9, 5, 0, // masonry
  7, 0, 5, // carpentry
  0, 7, 0, // plumbing
  5, 8, 0, // ceiling
  6, 7, 0, // roofing
  0, 9, 6, // painting
  8, 0, 5, // windows
  5, 5, 0, // facade
  5, 5, 9, // garden
  6, 0, 8  // moving
};
//end:TASKS

IloBool HasSkill(IloInt w, IloInt i) {
  return (0<SkillsMatrix[NbWorkers*i + w]);
}
IloInt SkillLevel(IloInt w, IloInt i) {
  return SkillsMatrix[NbWorkers*i + w];
}

  //Create the MakeHouse function
  IloEnv env = model.getEnv();

  //Create the interval variables
  //Add the temporal constraints
  //Add same worker constraints
}

int main(int argc, const char * argv[]) {
  IloEnv env;
  try {

  //Declare the objects needed for MakeHouse

  //Create the houses
  //Add the no overlap constraints
  //Add the objective

  //Create an instance of IloCP
  //Search for a solution

      cp.out() << "Solution with objective " << cp.getObjValue() << ":" << std::endl;
      for (IloInt i=0; i<allTasks.getSize(); ++i) {
        if (cp.isPresent(allTasks[i]))
          cp.out() << cp.domain(allTasks[i]) << std::endl;
      }
    } else {
      cp.out() << "No solution found. " << std::endl;
    }
    cp.printInformation();
  } catch (IloException& ex) {
    env.out() << "Error: " << ex << std::endl;
  }
  env.end();
  return 0;
}
