// -------------------------------------------------------------- -*- C++ -*-
// File: ./examples/src/cpp/intro.cpp
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

The problem is to find values for x and y from the following information:
x + y = 17
x - y = 5
x can be any integer from 5 through 12
y can be any integer from 2 through 17

------------------------------------------------------------ */

#include <ilcp/cp.h>

int main(int, const char * []){
  IloEnv env;
  try {
    IloModel model(env);
    IloIntVar x(env, 5, 12, "x");
    IloIntVar y(env, 2, 17, "y");
    model.add(x + y == 17);
    model.add(x - y == 5);
    IloCP cp(model);
    if (cp.solve()){
      cp.out() << std::endl << "Solution:" << std::endl;
      cp.out() << "x = " << cp.getValue(x) << std::endl;
      cp.out() << "y = " << cp.getValue(y) << std::endl;
    }
  }
  catch (IloException& ex) {
    env.out() << "Error: " << ex << std::endl;
  }
  env.end();
  return 0;
}
/*
 ! ----------------------------------------------------------------------------
 ! Satisfiability problem - 2 variables, 2 constraints
 ! Initial process time : 0.00s (0.00s extraction + 0.00s propagation)
 !  . Log search space  : 7.0 (before), 3.2 (after)
 !  . Memory usage      : 315.4 Kb (before), 315.4 Kb (after)
 ! ----------------------------------------------------------------------------
 !   Branches  Non-fixed                Branch decision
 *          2      0.03s                      y !=    5
 ! ----------------------------------------------------------------------------
 ! Solution status        : Terminated normally, solution found
 ! Number of branches     : 2
 ! Number of fails        : 1
 ! Total memory usage     : 328.3 Kb (315.4 Kb CP Optimizer + 12.9 Kb Concert)
 ! Time spent in solve    : 0.03s (0.03s engine + 0.00s extraction)
 ! Search speed (br. / s) : 64.0
 ! ----------------------------------------------------------------------------

Solution:
x = 11
y = 6
*/
