// -------------------------------------------------------------- -*- C++ -*-
// File: warehouse.cpp
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

/* --------------------------------------------------------------------------
 * USAGE EXAMPLE:
 * warehouse fixed 30 nbWarehouses 5 nbStores 10 disaggregate 1
 * --------------------------------------------------------------------------
 */

#include <sstream>
class MyParams: public IloOplDataSourceBaseI {
    int _nbWarehouses;
    int _nbStores;
    int _fixed;
    IloBool _disaggregate;

    void usage();
public:
    MyParams(IloEnv& env, int argc, char* argv[]);
    void read() const;
};
static char* getModelText();
int main(int argc,char* argv[]) {
	IloEnv env;
    

    int status = 127;
    try {
        IloOplErrorHandler handler(env,cout);
        std::istringstream in( getModelText() );
        IloOplModelSource modelSource(env,in,"warehouse");
        IloOplSettings settings(env,handler);
        IloOplModelDefinition def(modelSource,settings);
        IloCplex cplex(env);
        IloOplModel opl(def,cplex);
        MyParams params(env,argc,argv);
        IloOplDataSource dataSource(&params);
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
static char* getModelText() {
    return (char*)"\
           int   fixed        = ...; \
           int   nbWarehouses = ...; \
           int   nbStores     = ...; \
           int   disaggregate = ...; \
           \
           assert nbStores > nbWarehouses; \
           \
           range Warehouses = 1..nbWarehouses; \
           range Stores     = 1..nbStores; \
           \
           int capacity[w in Warehouses] = nbStores div nbWarehouses + w mod (nbStores div nbWarehouses); \
           int supplyCost[s in Stores][w in Warehouses] = 1+((s+10*w)%100); \
           \
           dvar boolean open[Warehouses]; \
           dvar boolean supply[Stores][Warehouses]; \
           \
           minimize \
           sum(w in Warehouses) fixed * open[w] + \
           sum(w in Warehouses, s in Stores) supplyCost[s][w] * supply[s][w]; \
           \
           constraints { \
             forall(s in Stores) \
               sum(w in Warehouses) supply[s][w] == 1; \
             forall(w in Warehouses) \
               sum(s in Stores) supply[s][w] <= open[w]*capacity[w]; \
             if (disaggregate == 1) { \
               forall(w in Warehouses, s in Stores) \
                 supply[s][w] <= open[w]; \
             } \
           } \
           ";
}

MyParams::MyParams(IloEnv& env, int argc, char* argv[]):IloOplDataSourceBaseI(env) {
    _nbWarehouses = 5;
    _nbStores = 10;
    _fixed = 30;
    _disaggregate = 1;

    for (int i=1; i<argc; i++) {
        if ( strcmp("-h",argv[i])==0 ) {
            usage();
        } else if ( strcmp("fixed",argv[i])==0 ) {
            if ( i==argc ) {
                usage();
            }
            _fixed=atoi(argv[++i]);
        } else if ( strcmp("nbWarehouses",argv[i])==0 ) {
            if ( i==argc ) {
                usage();
            }
            _nbWarehouses=atoi(argv[++i]);
        } else if ( strcmp("nbStores",argv[i])==0 ) {
            if ( i==argc ) {
                usage();
            }
            _nbStores=atoi(argv[++i]);
        } else if ( strcmp("disaggregate",argv[i])==0 ) {
            if ( i==argc ) {
                usage();
            }
            _disaggregate=atoi(argv[++i]);
        } else {
            break;
        }
    }

	cout << "Using parameters: " << endl
		<< "    nbWarehouses " << _nbWarehouses << endl
		<< "    nbStores     " << _nbStores << endl
		<< "    fixed        " << _fixed << endl
		<< "    disaggregate " << _disaggregate << endl
		<< endl;
}

void MyParams::usage() {
	cerr << endl
		<< "Usage: warehouse [-h] parameters" << endl
		<< "  -h " << "this help message" << endl
		<< "  parameters " << endl
		<< "    nbWarehouses <value> " << endl
		<< "    nbStores     <value> " << endl
		<< "    fixed        <value> " << endl
		<< "    disaggregate <value> " << endl
		<< endl;
    exit(0);
}
void MyParams::read() const {
    IloOplDataHandler handler = getDataHandler();

    handler.startElement("nbWarehouses");
    handler.addIntItem(_nbWarehouses);
    handler.endElement();

    handler.startElement("nbStores");
    handler.addIntItem(_nbStores);
    handler.endElement();

    handler.startElement("fixed");
    handler.addIntItem(_fixed);
    handler.endElement();

    handler.startElement("disaggregate");
    handler.addIntItem(_disaggregate);
    handler.endElement();
}
