// -------------------------------------------------------------- -*- C++ -*-
// File: mulprod.cpp
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
        IloOplErrorHandler handler(env,cout);
        IloOplModelSource modelSource(env, DATADIR "mulprod" DIRSEP "mulprod.mod");
        IloOplSettings settings(env,handler);
        IloOplModelDefinition def(modelSource,settings);
    	IloCplex cplex(env);
        IloOplModel opl(def,cplex);
        IloOplDataSource dataSource(env, DATADIR "mulprod" DIRSEP "mulprod.dat");
        opl.addDataSource(dataSource);
        opl.generate();

        if ( cplex.solve() ) {
          cout << endl
                << "OBJECTIVE: " << fixed << setprecision(2) << opl.getCplex().getObjValue()
                << endl;
            opl.postProcess();
            opl.printSolution(cout);
            status = 0;
        } else {
            cout << "No solution!" << endl;
            status = 1;
        }
    } catch (IloOplException & e) {
        cout << "### OPL exception: " << e.getMessage() << endl;
    } catch( IloException & e ) {
        cout << "### CONCERT exception: ";
        e.print(cout);
        status = 2;
    } catch (...) {
        cout << "### UNEXPECTED ERROR ..." << endl;
        status = 3;
    }

    env.end();

    cout << endl << "--Press <Enter> to exit--" << endl;
    getchar();

    return status;
}
