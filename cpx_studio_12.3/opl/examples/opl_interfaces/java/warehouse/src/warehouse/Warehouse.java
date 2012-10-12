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
//Java version of warehouse.cpp of OPL distrib
//--------------------------------------------------------------------------
package warehouse;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.opl.*;

public class Warehouse
{
    static class MyParams extends IloCustomOplDataSource
    {
        int _nbWarehouses;
        int _nbStores;
        int _fixed;
        int _disaggregate;

        MyParams(IloOplFactory oplF,int nbWarehouses,int nbStores,int fixed,int disaggregate)
        {
            super(oplF);
            _nbWarehouses = nbWarehouses;
            _nbStores = nbStores;
            _fixed = fixed;
            _disaggregate = disaggregate;
        }

        public void customRead()
        {
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
    };

    static public void main(String[] args) throws Exception
    {
      int status = 127;
      try {
        int nbWarehouses=-1;
        int nbStores=-1;
        int fixed=-1;
        int disaggregate=-1;
        for (int i=0; i<args.length; i++)
        {
            if ( "-h".equals(args[i]) ) {
                usage();
            } else if ("fixed".equals(args[i])) {
                if (i==args.length) {
                    usage();
                }
                fixed=Integer.parseInt(args[++i]);
            } else if ( "nbWarehouses".equals(args[i]) ) {
                if ( i==args.length ) {
                    usage();
                }
                nbWarehouses=Integer.parseInt(args[++i]);
            } else if ( "nbStores".equals(args[i]) ) {
                if ( i==args.length ) {
                    usage();
                }
                nbStores=Integer.parseInt(args[++i]);
            } else if ( "disaggregate".equals(args[i]) ) {
                if ( i==args.length ) {
                    usage();
                }
                disaggregate=Integer.parseInt(args[++i]);
            } else {
                break;
            }
        }

        if ( fixed==-1 || nbWarehouses==-1 || nbStores==-1 || disaggregate==-1 ) {
            usage();
        }

        IloOplFactory.setDebugMode(true);
        IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
        IloOplModelSource modelSource=oplF.createOplModelSourceFromString(getModelText(),"warehouse");
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
        IloCplex cplex = oplF.createCplex();
        IloOplModel opl=oplF.createOplModel(def,cplex);

        IloOplDataSource dataSource=new MyParams(oplF,nbWarehouses,nbStores,fixed,disaggregate);
        opl.addDataSource(dataSource);
        opl.generate();
        if ( cplex.solve() )
        {
            System.out.println("OBJECTIVE: " + opl.getCplex().getObjValue());
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
        model+="int   fixed        = ...;";
        model+="int   nbWarehouses = ...;";
        model+="int   nbStores     = ...;";
        model+="int   disaggregate = ...;";
        model+="assert nbStores > nbWarehouses;";

        model+="range Warehouses = 1..nbWarehouses;";
        model+="range Stores     = 1..nbStores;";

        model+="int capacity[w in Warehouses] = nbStores div nbWarehouses + w mod (nbStores div nbWarehouses);";
        model+="int supplyCost[s in Stores][w in Warehouses] = 1+((s+10*w) mod 100);";

        model+="dvar boolean open[Warehouses];";
        model+="dvar boolean supply[Stores][Warehouses];";

        model+="minimize ";
        model+="sum(w in Warehouses) fixed * open[w] +";
        model+="sum(w in Warehouses, s in Stores) supplyCost[s][w] * supply[s][w];";

        model+="constraints {";
        model+="  forall(s in Stores)";
        model+="    sum(w in Warehouses) supply[s][w] == 1;";
        model+="  forall(w in Warehouses)";
        model+="    sum(s in Stores) supply[s][w] <= open[w]*capacity[w];";
        model+="  if (disaggregate == 1) {";
        model+="   forall(w in Warehouses, s in Stores)";
        model+="      supply[s][w] <= open[w];";
        model+="  }";
        model+="}";
        return model;
    }

    static void usage() {
        System.err.println();
        System.err.println("Usage: warehouse [-h] parameters");
        System.err.println("  -h  this help message");
        System.err.println("  parameters ");
        System.err.println("    nbWarehouses <value> ");
        System.err.println("    nbStores <value> ");
        System.err.println("    fixed <value> ");
        System.err.println("    disaggregate <value> ");
        System.err.println();
        System.exit(0);
    }
}

