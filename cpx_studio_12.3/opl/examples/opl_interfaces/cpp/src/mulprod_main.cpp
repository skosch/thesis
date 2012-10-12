// -------------------------------------------------------------- -*- C++ -*-
// File: mulprod_main.cpp
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
// 
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 
// Copyright IBM Corporation 1998, 2011. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
/////////////////////////////////////////////////////////////////////////////// 

#include <ilopl/iloopl.h>

#include <sstream>

#ifdef ILO_WINDOWS
#define DIRSEP "\\"
#else
#define DIRSEP "/"
#endif
#ifndef DATADIR
#define DATADIR ".." DIRSEP ".."  DIRSEP ".." DIRSEP ".." DIRSEP "opl" DIRSEP
#endif

int main(int argc,char* argv[]) {
    IloEnv env;


    int status = 127;
    try {
        IloInt capFlour = 20; 
        IloNum best;
        IloNum curr = IloInfinity;

        IloOplRunConfiguration rc0(env,
            DATADIR "mulprod" DIRSEP "mulprod.mod",
            DATADIR "mulprod" DIRSEP "mulprod.dat");
        IloOplDataElements dataElements = rc0.getOplModel().makeDataElements();

        do {
          best = curr;

          IloOplRunConfiguration rc(rc0.getOplModel().getModelDefinition(),
              dataElements);

          rc.getCplex().setOut(env.getNullStream());
          rc.getOplModel().generate();

          cout << "Solve with capFlour = " << capFlour << endl;
          if ( rc.getCplex().solve() ) {
              curr = rc.getCplex().getObjValue();
              cout << endl
                  << "OBJECTIVE: " << fixed << setprecision(2) << curr
                  << endl;
              status = 0;
          } else {
              cout << "No solution!" << endl;
              status = 1;
          }

          capFlour++;
          // Change the value of Capacity["flour"] in dataElements
          dataElements.getElement("Capacity").asNumMap().set("flour", capFlour);

          rc.end();
        } while ( best != curr && status == 0);
    } catch (IloOplException & e) {
        cout << "### OPL exception: " << e.getMessage() << endl;
    } catch( IloException & e ) {
        cout << "### CONCERT exception: ";
        e.print(cout);
        status = 2;
    } catch (...) {
        cout << "### UNEXPECTED ERROR ..." << endl;
        status = 3;
        return status;
    }

    env.end();

    cout << endl << "--Press <Enter> to exit--" << endl;
    getchar();
    
    return status;
}
