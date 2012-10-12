// -------------------------------------------------------------- -*- C++ -*-
// File: cutstock_change.cpp
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

#define RC_EPS 1.0e-6

#ifdef ILO_WINDOWS
#define DIRSEP "\\"
#else
#define DIRSEP "/"
#endif
#ifndef DATADIR
#define DATADIR ".." DIRSEP ".."  DIRSEP ".." DIRSEP ".." DIRSEP "opl" DIRSEP
#endif

ILOSTLBEGIN
int main(int argc,char* argv[]) {
    IloEnv env;
    IloInt i;

    int status = 127;
    try {
        IloNum best;
        IloNum curr = IloInfinity;

        IloOplErrorHandler handler(env,cout);

        IloOplRunConfiguration masterRC(env,
            DATADIR "cutstock" DIRSEP "cutstock_change.mod",
            DATADIR "cutstock" DIRSEP "cutstock_change.dat");
        masterRC.setErrorHandler(handler);
        IloCplex masterCplex(env);
        masterCplex.setOut(env.getNullStream());
        masterRC.setCplex(masterCplex);
        IloOplModel masterOpl = masterRC.getOplModel();
        masterOpl.generate();

        IloOplDataElements masterDataElements = masterOpl.makeDataElements();
        IloInt nWdth = masterDataElements.getElement("Amount").asIntMap().getSize();
        IloNumVarArray masterVars(env);
        IloNumVarMap cuts = masterOpl.getElement("Cut").asNumVarMap();
        for (i=1; i<=nWdth; i++) {
            masterVars.add(cuts.get(i));
        }

		IloOplSettings settings(env,handler);
   		IloOplModelSource subSource(env, DATADIR "cutstock" DIRSEP "cutstock-sub.mod");
		IloOplModelDefinition subDef(subSource,settings);
		IloCplex subCplex(env);

        do {
            best = curr;

            cout << "Solve master." << endl;
            if ( masterCplex.solve() ) {
                curr = masterCplex.getObjValue() ;
                cout << endl
                    << "MASTER OBJECTIVE: " << fixed << setprecision(2) << curr
                    << endl;
                status = 0;
            } else {
                cout << "No solution!" << endl;
                status = 1;
            }

            // get reduced costs and set them in sub problem

            // make sub model
			subCplex.setOut(env.getNullStream());
			IloOplModel subOpl(subDef,subCplex);
			IloOplDataElements subDataElements(env);
			subDataElements.addElement(masterRC.getOplModel().getElement("RollWidth"));
			subDataElements.addElement(masterRC.getOplModel().getElement("Size"));
			subDataElements.addElement(masterRC.getOplModel().getElement("Duals"));

            IloConstraintMap FillCt = masterOpl.getElement("ctFill").asConstraintMap();
            IloNumMap duals = subDataElements.getElement("Duals").asNumMap();
            for (i=1; i<nWdth+1; i++) {
                IloForAllRange far = FillCt.get(i);
                duals.set(i, masterCplex.getDual(far));
            }

			subOpl.addDataSource(subDataElements);
            subOpl.generate();

            cout << "Solve sub." << endl;
            if ( subCplex.solve() ) {
                cout << endl
                    << "SUB OBJECTIVE: " << fixed << setprecision(2)
                    << subCplex.getObjValue()
                    << endl;
                status = 0;
            } else {
                cout << "No solution!" << endl;
                status = 1;
            }

            if (subCplex.getObjValue() > -RC_EPS) break;

            // Add variable in master model

            IloNumVar newVar(env, 0, IloIntMax);
            IloObjective masterObj = masterOpl.getObjective();
            masterObj.setLinearCoef(newVar, 1);
            IloIntVarMap Use = subOpl.getElement("Use").asIntVarMap();
            for (i=1; i<nWdth+1; i++) {
                IloNum coef = subCplex.getValue(Use.get(i));
                IloForAllRange far = FillCt.get(i);
                far.setLinearCoef(newVar, coef);
            }
            masterVars.add(newVar);

            subOpl.end();
        } while ( best != curr && status == 0);

        masterOpl.getModel().add(IloConversion(env, masterVars, ILOINT));
        if ( masterCplex.solve() ) {
            cout << endl
                << "OBJECTIVE: " << fixed << setprecision(2) << masterCplex.getObjValue()
                << endl;
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
