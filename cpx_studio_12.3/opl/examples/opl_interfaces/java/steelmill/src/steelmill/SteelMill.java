/*
* Licensed Materials - Property of IBM
* 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 
* Copyright IBM Corporation 1998, 2011. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/ 


//-------------------------------------------------------------- -*- Java -*-
//Java version of steelmill.cpp of OPL distrib
//--------------------------------------------------------------------------
package steelmill;

import ilog.concert.*;
import ilog.cp.*;
import ilog.opl.*;

public class SteelMill
{
    static class MyData extends IloCustomOplDataSource
    {
        MyData(IloOplFactory oplF)
        {
            super(oplF);
        }

        public void customRead()
        {
            int _nbOrders = 12;
            int _nbSlabs = 12;
            int _nbColors = 8;
            int _nbCap = 20;

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

            int[] _capacity = {0, 11, 13, 16, 17, 19, 20, 23, 24, 25, 26, 27, 28, 29, 30, 33, 34, 40, 43, 45};
            handler.startElement("capacities");
            handler.startArray();
            for (int i=0; i<_nbCap; i++)
                handler.addIntItem(_capacity[i]);
            handler.endArray();
            handler.endElement();

            int[] _weight = {22, 9, 9, 8, 8, 6, 5, 3, 3, 3, 2, 2};
            handler.startElement("weight");
            handler.startArray();
            for (int i=0; i<_nbOrders; i++)
                handler.addIntItem(_weight[i]);
            handler.endArray();
            handler.endElement();

            int[] _colors = {5, 3, 4, 5, 7, 3, 6, 0, 2, 3, 1, 5};
            handler.startElement("colors");
            handler.startArray();
            for (int i=0; i<_nbOrders; i++)
                handler.addIntItem(_colors[i]);
            handler.endArray();
            handler.endElement();
        }
    };

    static public void main(String[] args) throws Exception
    {
        int status = 127;
        try {
            IloOplFactory.setDebugMode(true);
            IloOplFactory oplF = new IloOplFactory();
            IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
            IloOplModelSource modelSource=oplF.createOplModelSourceFromString(getModelText(),"steelmill");
            IloOplSettings settings = oplF.createOplSettings(errHandler);
            IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
            IloCP cp = oplF.createCP();
            IloOplModel opl=oplF.createOplModel(def,cp);

            IloOplDataSource dataSource=new MyData(oplF);
            opl.addDataSource(dataSource);
            opl.generate();
            if ( cp.solve() )
            {
                System.out.println("OBJECTIVE: " + opl.getCP().getObjValue());
                opl.postProcess();
                opl.printSolution(System.out);
                status = 0;
            } else {
                System.out.println("No solution!");
                status = 1;
            }

            oplF.end();
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

    static String getModelText()
    {
        String model="";
        model+="using CP;";
        model+="int nbOrders   = ...;";
        model+="int nbSlabs = ...;";
        model+="int nbColors   = ...;";
        model+="int nbCap      = ...;";
        model+="int capacities[1..nbCap] = ...;";
        model+="int weight[1..nbOrders] = ...;";
        model+="int colors[1..nbOrders] = ...;";
        model+="int maxLoad = sum(i in 1..nbOrders) weight[i];";
        model+="int maxCap  = max(i in 1..nbCap) capacities[i];";
        model+="int loss[c in 0..maxCap] = min(i in 1..nbCap : capacities[i] >= c) capacities[i] - c; ";
        model+="execute {";
        model+="writeln(\"loss = \", loss);";
        model+="writeln(\"maxLoad = \", maxLoad);";
        model+="writeln(\"maxCap = \", maxCap);";
        model+="};";
        model+="dvar int where[1..nbOrders] in 1..nbSlabs;";
        model+="dvar int load[1..nbSlabs] in 0..maxLoad;";
        model+="execute {";
        model+="  cp.param.LogPeriod = 50;";
        model+="  var f = cp.factory;";
        model+="  cp.setSearchPhases(f.searchPhase(where));";
        model+="}";
        model+="dexpr int totalLoss = sum(s in 1..nbSlabs) loss[load[s]];";
        model+="minimize totalLoss;";
        model+="subject to {  ";
        model+="  packCt: pack(load, where, weight);";
        model+="  forall(s in 1..nbSlabs)";
        model+="    colorCt: sum (c in 1..nbColors) (or(o in 1..nbOrders : colors[o] == c) (where[o] == s)) <= 2; ";
        model+="}";
        return model;
    }
}

