// -------------------------------------------------------------- -*- C++ -*-
// File: ./examples/tutorial/cpp/sched_cumul_partial.cpp
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
//end:TASKS

//Create the MakeHouse function
  IloEnv env = model.getEnv();

  //Create the interval variables
  //Add the temporal constraints
  //Add the objective expression
}

int main(int argc, const char * argv[]) {
  IloEnv env;
  try {
    IloModel model(env);

    //Declare the objects needed for MakeHouse
    //Add the cash payment expression
    //Create the houses
    //Add the cash balance constraint
    //Add the worker usage constraint
    //Add the objective

    //Create an instance of IloCP
    //Search for a solution
      cp.out() << "Solution with objective " << cp.getObjValue() << ":" << std::endl;
      for (IloInt i=0; i<allTasks.getSize(); ++i) {
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
}
