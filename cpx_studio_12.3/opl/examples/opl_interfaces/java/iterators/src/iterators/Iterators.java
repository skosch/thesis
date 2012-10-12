/*
* Licensed Materials - Property of IBM
* 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
* Copyright IBM Corporation 1998, 2011. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/

// -------------------------------------------------------------- -*- Java -*-
//Java version of iterators.cpp of OPL distrib
//--------------------------------------------------------------------------
package iterators;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.opl.*;

public class Iterators {
    static final String DATADIR = ".";

    // The purpose of this sample is to check the result of filtering by
    // iterating on the generated data element.
    //
    // The data element is an array of strings that is indexed by a set of
    // strings.
    // It is filled as the result of an iteration on a set of tuples by
    // filtering out the duplicates.
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
static public void sample1() throws Exception
    {
        int status = 127;
        try {
            IloOplFactory.setDebugMode(true);
            IloOplFactory oplF = new IloOplFactory();

            IloOplRunConfiguration rc = oplF.createOplRunConfiguration(DATADIR
                    + "/transp2.mod",DATADIR
                    + "/transp2.dat");
            IloOplModel opl = rc.getOplModel();
            opl.generate();

            System.out.println("Verification of the computation of orig:");

            // Get the orig, Routes, Product elements from the OplModel.
            IloSymbolSetMap orig = opl.getElement("Orig").asSymbolSetMap();
            IloTupleSet Routes = opl.getElement("Routes").asTupleSet();
            IloSymbolSet Products = opl.getElement("Products").asSymbolSet();

            // Iterate through the orig to see the result of the data element
            // filtering.
            for (java.util.Iterator it2 = Products.iterator(); it2.hasNext(); )
            {
                String p = (String)it2.next();
                // We are in the last dimension of the array (as it is a 1
                // dimensional array), so we can use the get method directly.
                System.out.println("for p = " + p + " we have " + (IloSymbolSet)orig.get(p));
            }
            System.out.println("---------------------");

            // Iterate through the TupleSet.
            for (java.util.Iterator it1 = Routes.iterator(); it1.hasNext(); )
            {
                IloTuple t = (IloTuple)it1.next();
                // Get the string "p" from the tuple.
                String p = t.getStringValue("p");
                // if "p" is in the indexer, we will try to add the "o" string to
                // the array.
                if (Products.contains(p))
                    System.out.println("for p = " + p + " we will have " + t.getStringValue("o") + " from " + t.toString());
            }

            System.out.println("---------------------");
            status = 0;
        } catch (IloOplException ex) {
            System.err.println("### OPL exception: " + ex.getMessage());
            ex.printStackTrace();
            status = 2;
        } catch (IloException ex) {
            System.err.println("### CONCERT exception: " + ex.getMessage());
            ex.printStackTrace();
            status = 3;
        } catch (Exception ex) {
            System.err.println("### UNEXPECTED UNKNOWN ERROR ...");
            ex.printStackTrace();
            status = 4;
        }
        System.exit(status);
    }
    // The purpose of this sample is to output a multidimensional array x[i][j]
    // to illustrate how arrays and sub-arrays are managed.
    // To access the elements of an array, you must first access the sub-arrays
    // until the last dimension, then you can get the values.
    // Here, as there are two dimensions, you have to get one sub-array from
    // which you can directly get the values.
    //
    // The array of integers is indexed by two sets of strings..
    //
    // The simplified model is:
    //
    // {string} s1 = ...;
    // {string} s2 = ...;
    // {int} x[s1][s2] = ...;
    //
    static public void sample2() throws Exception {
        int status = 127;
        try {
        IloOplFactory.setDebugMode(true);
        IloOplFactory oplF = new IloOplFactory();

        IloOplRunConfiguration rc = oplF.createOplRunConfiguration(DATADIR
                + "/iterators.mod");
        IloOplModel opl = rc.getOplModel();
        opl.generate();

        // Get the x, s1 and s2 elements from the OplModel.
        IloIntMap x = opl.getElement("x").asIntMap();
        IloSymbolSet s1 = opl.getElement("s1").asSymbolSet();
        IloSymbolSet s2 = opl.getElement("s2").asSymbolSet();

        // Iterate on the first indexer.
        for (java.util.Iterator it1 = s1.iterator(); it1.hasNext();) {
            String sub1 = (String) it1.next();
            // Get the 2nd dimension array from the 1st dimension.
            IloIntMap sub = x.getSub(sub1);
            // Iterate on the second indexer of x (that is the indexer of the
            // sub array).
            for (java.util.Iterator it2 = s2.iterator(); it2.hasNext();) {
                String sub2 = (String) it2.next();
                // We are in the last dimension of the array, so we can directly
                // use the get method.
                System.out.println(sub1 + " " + sub2 + " " + sub.get(sub2));
            }
        }
        System.out.println("---------------------");
        status = 0;
        }
        catch (IloOplException e) {
        e.printStackTrace();
        status = 2;
        }
        catch (IloException e) {
        e.printStackTrace();
        status = 3;
        }
        catch (Exception e) {
        e.printStackTrace();
        status = 4;
        }
        System.exit(status);
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

    static String getModelTextSample3 () {
        String model = "";
        model += "tuple t{int a;int b;}";
        model += " {string} ids = {\"id1\",\"id2\", \"id3\"};";
        model += " t arrayT[ids] = [<1,2>,<2,3>,<1,3>];";
        return model;
    }
    static public void sample3() throws Exception {
        int status = 127;
            try {
        IloOplFactory.setDebugMode(true);
        IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelSource src = oplF.createOplModelSourceFromString(getModelTextSample3(),"tuple array iterator");
        IloOplModelDefinition def = oplF.createOplModelDefinition(src, settings);
        IloCplex cplex = oplF.createCplex();
        IloOplModel opl = oplF.createOplModel(def, cplex);
        opl.generate();
        // get the string set used to index the array of tuples
        IloSymbolSet ids = opl.getElement("ids").asSymbolSet();
        IloTupleMap arrayT = opl.getElement("arrayT").asTupleMap();
        // iterate on the index set to retrieve the tuples stored in the array
        for (java.util.Iterator it = ids.iterator(); it.hasNext();) {
            String s = (String)it.next();
            System.out.print("arrayT[" + s + "] = ");
            IloMapIndexArray id = oplF.mapIndexArray(0);
            id.add(s);
            IloTuple t = arrayT.makeTuple();
            arrayT.getAt(id,t);
            System.out.println(t);
        }
        oplF.end();
        status = 0;
        }
        catch (IloOplException e) {
        e.printStackTrace();
        status = 2;
        }
        catch (IloException e) {
        e.printStackTrace();
        status = 3;
        }
        catch (Exception e) {
        e.printStackTrace();
        status = 4;
        }
        System.exit(status);
    }


    static public void main(String[] args) throws Exception {
        sample1();
        sample2();
        sample3();
    }
}
