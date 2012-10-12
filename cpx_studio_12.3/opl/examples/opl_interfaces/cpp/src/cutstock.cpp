// -------------------------------------------------------------- -*- C++ -*-
// File: cutstock.cpp
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

    IloInt nbItems = 5;
    IloIntRange items(env, 1, 5);

    int status = 127;
    try {
        IloNum best;
        IloNum curr = IloInfinity;


	IloOplErrorHandler errHandler(env,cout);
	IloOplSettings settings(env,errHandler);
	IloOplModelSource masterSource(env, DATADIR "cutstock" DIRSEP "cutstock.mod");
	IloOplModelDefinition masterDef(masterSource,settings);
	IloOplDataSource masterDataSource(env, DATADIR "cutstock" DIRSEP "cutstock.dat");
	IloOplDataElements masterDataElements(masterDef,masterDataSource);
        IloCplex masterCplex(env);
        masterCplex.setOut(env.getNullStream());

	IloOplModelSource subSource(env, DATADIR "cutstock" DIRSEP "cutstock-sub.mod");
	IloOplModelDefinition subDef(subSource,settings);
	IloCplex subCplex(env);

        IloOplModel oldSubOpl;
        do {
            best = curr;

            masterCplex.clearModel();

            IloOplRunConfiguration masterRC(masterDef,masterDataElements);
            masterRC.setCplex(masterCplex);
            masterRC.getOplModel().generate();

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
            if ( oldSubOpl.getImpl()!=0 ) {
                oldSubOpl.end();
            }

			subCplex.clearModel();
			subCplex.setOut(env.getNullStream());
			IloOplModel subOpl(subDef,subCplex);
			IloOplDataElements subDataElements(env);
			subDataElements.addElement(masterRC.getOplModel().getElement("RollWidth"));
			subDataElements.addElement(masterRC.getOplModel().getElement("Size"));
			subDataElements.addElement(masterRC.getOplModel().getElement("Duals"));

            IloNumMap duals = subDataElements.getElement("Duals").asNumMap();
            IloConstraintMap FillCt = masterRC.getOplModel().getElement("ctFill").asConstraintMap();
            for (IloInt i=1; i<nbItems+1; i++) {
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

            // Add variable in master model by simply adding a new tuple in Patterns
            IloIntMap newFill = subOpl.getElement("Use").asIntMap().copy();

            IloTupleBuffer buf = masterDataElements.getElement("Patterns").asTupleSet().makeTupleBuffer();
            buf.setIntValue("id", masterDataElements.getElement("Patterns").asTupleSet().getSize());
            buf.setIntValue("cost", 1);
            buf.setIntMapValue("fill", newFill);
            buf.commit();

            masterRC.end();
			oldSubOpl = subOpl;
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
    }

    env.end();

    cout << endl << "--Press <Enter> to exit--" << endl;
    getchar();
    
    return status;
}
