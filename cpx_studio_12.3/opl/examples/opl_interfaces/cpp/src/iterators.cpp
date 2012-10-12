// -------------------------------------------------------------- -*- C++ -*-
// File: iterators.cpp
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

#ifndef DATADIR
#ifdef ILO_WINDOWS
#define DIRSEP "\\"
#else
#define DIRSEP "/"
#endif
#define DATADIR ".." DIRSEP ".."  DIRSEP ".." DIRSEP ".." DIRSEP "opl" DIRSEP
#endif

// The purpose of this sample is to check the result of filtering by iterating on the generated data element.
//
// The data element is an array of strings that is indexed by a set of strings.
// It is filled as the result of an iteration on a set of tuples that filters out the duplicates.
// It is based on the model used in "Sparsity" run configuration of the "transp" example.
//
//
// The simplified model is:
//
// {string} Products = ...;
// tuple Route { string p; string o; string d; }
// {Route} Routes = ...;
// {string} orig[p in Products] = { o | <p,o,d> in Routes };
//
int sample1() {
    IloEnv env;
    int status = 127;
    try {
        IloOplRunConfiguration rc(env,
            DATADIR "transp" DIRSEP "transp2.mod",
            DATADIR "transp" DIRSEP "transp2.dat");
        IloOplModel opl = rc.getOplModel();
        opl.generate();

        cout << "Verification of the computation of orig: \n";

        // Get the orig, Routes, Product elements from the OplModel.
        IloSymbolSetMap orig = opl.getElement("Orig").asSymbolSetMap();
        IloTupleSet Routes = opl.getElement("Routes").asTupleSet();
        IloSymbolSet Products = opl.getElement("Products").asSymbolSet();

        // Iterate through the orig to see the result of the data element filtering.
        for (IloSymbolSetIterator it2(Products); it2.ok(); ++it2){
            const char* p = *it2;
            // This is the last dimension of the array (as it is a one-dimensional array), so you can use the get method directly.
            cout << "for p = " << p << " we have " << orig.get(p) << "\n";
        }
        cout << "---------------------\n";

        // Iterate through the TupleSet.
        for (IloTupleIterator it1(Routes); it1.ok(); ++it1){
            IloTuple t = *it1;
            // Get the string "p" from the tuple.
            const char* p = t.getStringValue("p");
            // if "p" is in the indexer, we will try to add the "o" string to the array.
            if (Products.contains(p)) cout << "for p = " << p << " we will have " << t.getStringValue("o") << " from " << t << "\n";
        }
        cout << "---------------------\n";
        status = 0;
    } catch (IloOplException & e) {
        status = 1;
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

// The purpose of this sample is to output a multidimensional array x[i][j] to illustrate how arrays and subarrays are managed.
// To access the elements of an array, you must first access the subarrays up to  the last dimension, then you can get the values.
//  Here, as there are two dimensions, you have to get one subarray from which you can directly get the values.
//
// The array of integers is indexed by two sets of strings..
//
// The simplified model is:
//
// {string} s1 = ...;
// {string} s2 = ...;
// {int} x[s1][s2] = ...;
//
int sample2() {
    IloEnv env;
    int status = 0;
    try {
        IloOplRunConfiguration rc(env,DATADIR "iterators" DIRSEP "iterators.mod");
        IloOplModel opl = rc.getOplModel();
        opl.generate();

        // Get the x, s1 and s2 elements from the OplModel.
        IloIntMap x = opl.getElement("x").asIntMap();
        IloSymbolSet s1 = opl.getElement("s1").asSymbolSet();
        IloSymbolSet s2 = opl.getElement("s2").asSymbolSet();

        // Iterate on the first indexer.
        for (IloSymbolSetIterator it1(s1); it1.ok(); ++it1){
            // Get the second dimension array from the first dimension.
            IloIntMap sub = x.getSub(*it1);
            // Iterate on the second indexer of x (that is the indexer of the subarray).
            for (IloSymbolSetIterator it2(s2); it2.ok(); ++it2){
                // This is the last dimension of the array, so you can directly use the get method.
                cout << *it1 << " " << *it2 << " " << sub.get(*it2) << "\n";
            }
        }
        cout << "---------------------\n";
    } catch (IloOplException & e) {
        status = 1;
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
    return 0;
}

// The purpose of this sample is to output an array of tuples arrayT[i],
// to illustrate how tuple elements can be accessed.
// The simplified model is:
// tuple t
// {
//   int a;
//   int b;
// }
// {string} ids={"id1","id2","id3"};
// t arrayT[ids]=[<1,2>,<2,3>,<1,3>];

static char* getModelTextSample3 () {
        return (char*)"tuple t{int a;int b;} \
                  {string} ids = {\"id1\",\"id2\", \"id3\"};\
                  t arrayT[ids] = [<1,2>,<2,3>,<1,3>];";
}
int sample3() {
    int status = 0;
    IloEnv env;
    try {
        std::istringstream ins( getModelTextSample3() );
        IloOplModelSource src (env,ins,"tuple array iterator");
        IloOplErrorHandler errHandler(env, cout);
        IloOplSettings settings(env,errHandler);
        IloOplModelDefinition def(src, settings);
        IloCplex cplex(env);
        IloOplModel opl(def, cplex);
        opl.generate();

        // get the string set used to index the array of tuples
        IloTupleMap arrayT = opl.getElement("arrayT").asTupleMap();
        IloSymbolSet ids = IloAdvCollectionHelper::asSymbolSet(arrayT.getIndexer());
        // iterate on the index set to retrieve the tuples stored in the array
        for (IloSymbolSetIterator it(ids); it.ok(); ++it){
            cout << "arrayT[" << *it << "] = ";
            IloMapIndexArray id(env,0);
            id.add(*it);
            IloTuple t = arrayT.makeTuple();
            arrayT.getAt(id,t);
            cout << t << endl;
        }
    } catch (IloOplException & e) {
        status = 1;
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
    return status;
}

int main(int argc,char* argv[]) {
    int status = 0;
    status = status + sample1();
    status = status + sample2();
    status = status + sample3();
    return status;
}


