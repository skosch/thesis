// -------------------------------------------------------------- -*- C++ -*-
// File: carseq.cpp
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
class MyData: public IloOplDataSourceBaseI {
public:
	MyData(IloEnv& env);
    void read() const;
};
static char* getModelText();
int main(int argc,char* argv[]) {
	IloEnv env;
    

    int status = 127;
    try {
        IloOplErrorHandler handler(env,cout);
        std::istringstream in( getModelText() );
        IloOplModelSource modelSource(env,in,"carseq");
	IloOplSettings settings(env,handler);
        IloOplModelDefinition def(modelSource,settings);
        IloCP cp(env);
        IloOplModel opl(def,cp);
        MyData data(env);
        IloOplDataSource dataSource(&data);
        opl.addDataSource(dataSource);
        opl.generate();

        if ( cp.solve()) {
            cout << endl 
                << "OBJECTIVE: "  << opl.getCP().getObjValue() 
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
        cout << "### exception: ";
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
using CP;\
\
int  nbConfs    = ...; \
int   nbOptions = ...;\
\
range Confs=1..nbConfs;\
range Options=1..nbOptions;\
\
int demand[Confs] = ...;\
tuple CapacitatedWindow { \
  int l;\
  int u;\
};\
CapacitatedWindow capacity[Options] = ...; \
\
range AllConfs = 0..nbConfs;\
int nbCars = sum (c in Confs) demand[c];\
int nbSlots = ftoi(floor(nbCars * 1.1 + 5)); \
int nbBlanks = nbSlots - nbCars;\
range Slots = 1..nbSlots;\
int option[Options,Confs] = ...; \
int allOptions[o in Options, c in AllConfs] = (c == 0) ? 0 : option[o][c];\
\
dvar int slot[Slots] in AllConfs;\
dvar int lastSlot in nbCars..nbSlots;\
\
minimize lastSlot - nbCars; \
subject to { \
  count(slot, 0) == nbBlanks;\
  forall (c in Confs)\
    count(slot, c) == demand[c];\
\
  forall(o in Options, s in Slots : s + capacity[o].u - 1 <= nbSlots) \
    sum(j in s .. s + capacity[o].u - 1) allOptions[o][slot[j]] <= capacity[o].l; \
\
  forall (s in nbCars + 1 .. nbSlots) \
    (s > lastSlot) => slot[s] == 0; \
};\
";
}

MyData::MyData(IloEnv& env):IloOplDataSourceBaseI(env) {
 
}

void MyData::read() const {
	const int _nbConfs = 7;
	const int _nbOptions = 5;

    IloOplDataHandler handler = getDataHandler();

    handler.startElement("nbConfs");
    handler.addIntItem(_nbConfs);
    handler.endElement();
    handler.startElement("nbOptions");
    handler.addIntItem(_nbOptions);
    handler.endElement();

	int _demand[_nbConfs] = {5, 5, 10, 10, 10, 10, 5};
	handler.startElement("demand");
	handler.startArray();
	for (int i=0; i<_nbConfs; i++)
		handler.addIntItem(_demand[i]);
	handler.endArray();
	handler.endElement();
    
	int _option[_nbOptions][_nbConfs] = {{1, 0, 0, 0, 1, 1, 0},
									   {0, 0, 1, 1, 0, 1, 0},
									   {1, 0, 0, 0, 1, 0, 0},
									   {1, 1, 0, 1, 0, 0, 0},
									   {0, 0, 1, 0, 0, 0, 0}};
	handler.startElement("option");
	handler.startArray();
	for (int i=0; i< _nbOptions; i++) {
		handler.startArray();
		for (int j=0; j<_nbConfs;j++)
			handler.addIntItem(_option[i][j]);
		handler.endArray();
	}
	handler.endArray();
	handler.endElement();

	int _capacity[_nbOptions][2] = {{1,2},{2,3},{1,3},{2,5},{1,5}};
	handler.startElement("capacity");
	handler.startArray();
	for (int i=0; i<_nbOptions;i++) {
		handler.startTuple();
		handler.addIntItem(_capacity[i][0]);
		handler.addIntItem(_capacity[i][1]);
		handler.endTuple();
	}
	handler.endArray();
	handler.endElement();
}
