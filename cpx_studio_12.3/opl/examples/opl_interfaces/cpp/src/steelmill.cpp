// -------------------------------------------------------------- -*- C++ -*-
// File: steelmill.cpp
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

/*
This problem is based on "prob038: Steel mill slab design problem" from
CSPLib (www.csplib.org). It is a simplification of an industrial problem
described in J. R. Kalagnanam, M. W. Dawande, M. Trumbo, H. S. Lee.
"Inventory Matching Problems in the Steel Industry," IBM Research
Report RC 21171, 1998.
*/

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
        IloOplModelSource modelSource(env,in,"steelmill");
	    IloOplSettings settings(env,handler);
        IloOplModelDefinition def(modelSource,settings);
        IloCP cp(env);
        IloOplModel opl(def,cp);
        MyData data(env);
        IloOplDataSource dataSource(&data);
        opl.addDataSource(dataSource);
        opl.generate();
        if ( cp.solve() ) {
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
    } catch( IloOplException & e ) {
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
using CP;\
\
int nbOrders   = ...;\
int nbSlabs = ...;\
int nbColors   = ...;\
int nbCap      = ...;\
int capacities[1..nbCap] = ...;\
int weight[1..nbOrders] = ...;\
int colors[1..nbOrders] = ...;\
int maxLoad = sum(i in 1..nbOrders) weight[i];\
int maxCap  = max(i in 1..nbCap) capacities[i];\
int loss[c in 0..maxCap] = min(i in 1..nbCap : capacities[i] >= c) capacities[i] - c; \
execute {\
writeln(\"loss = \", loss);\
writeln(\"maxLoad = \", maxLoad);\
writeln(\"maxCap = \", maxCap);\
};\
dvar int where[1..nbOrders] in 1..nbSlabs;\
dvar int load[1..nbSlabs] in 0..maxLoad;\
execute {\
  cp.param.LogPeriod = 50;\
  var f = cp.factory;\
  cp.setSearchPhases(f.searchPhase(where));\
}\
dexpr int totalLoss = sum(s in 1..nbSlabs) loss[load[s]];\
\
minimize totalLoss;\
subject to {  \
  packCt: pack(load, where, weight);\
  forall(s in 1..nbSlabs)\
    colorCt: sum (c in 1..nbColors) (or(o in 1..nbOrders : colors[o] == c) (where[o] == s)) <= 2; \
}\
           ";
}

MyData::MyData(IloEnv& env):IloOplDataSourceBaseI(env) {
 
}

void MyData::read() const {
    const int _nbOrders = 12;
    const int _nbSlabs = 12;
    const int _nbColors = 8;
    const int _nbCap = 20;
	
    IloOplDataHandler handler = getDataHandler();

    handler.startElement("nbOrders");
    handler.addIntItem(_nbOrders);
    handler.endElement();
    handler.startElement("nbSlabs");
    handler.addIntItem(_nbSlabs);
    handler.endElement();
    handler.startElement("nbColors");
    handler.addIntItem(_nbColors);
    handler.endElement();
    handler.startElement("nbCap");
    handler.addIntItem(_nbCap);
    handler.endElement();

	int _capacity[_nbCap] = { 0, 11, 13, 16, 17, 19, 20,
                                  23, 24, 25, 26, 27, 28, 29,
                                  30, 33, 34, 40, 43, 45 };
	handler.startElement("capacities");
	handler.startArray();
	for (int i=0; i<_nbCap; i++)
		handler.addIntItem(_capacity[i]);
	handler.endArray();
	handler.endElement();

	int _weight[_nbOrders] = { 22, 9, 9, 8, 8, 6, 5, 3, 3, 3, 2, 2};
	handler.startElement("weight");
	handler.startArray();
	for (int i=0; i<_nbOrders; i++)
		handler.addIntItem(_weight[i]);
	handler.endArray();
	handler.endElement();

	int _colors[_nbOrders] = { 5, 3, 4, 5, 7, 3, 6, 0, 2, 3, 1, 5 };
	handler.startElement("colors");
	handler.startArray();
	for (int i=0; i<_nbOrders; i++)
		handler.addIntItem(_colors[i]);
	handler.endArray();
	handler.endElement();
}
