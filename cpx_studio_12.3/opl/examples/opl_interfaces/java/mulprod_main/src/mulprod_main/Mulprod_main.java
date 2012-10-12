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
//Java version of mulprod_main.cpp of OPL distrib
//--------------------------------------------------------------------------
package mulprod_main;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.opl.*;

public class Mulprod_main {

    static final String DATADIR = ".";

    static public void main(String[] args) throws Exception {
        int status = 127;
        try {
            int capFlour = 20;
            double best;
            double curr = Double.MAX_VALUE;

            IloOplFactory.setDebugMode(true);
            IloOplFactory oplF = new IloOplFactory();

            IloOplRunConfiguration rc0 = oplF.createOplRunConfiguration(DATADIR
                    + "/mulprod.mod", DATADIR + "/mulprod.dat");
            IloOplDataElements dataElements = rc0.getOplModel().makeDataElements();

            do {
                best = curr;

                IloOplRunConfiguration rc = oplF.createOplRunConfiguration(rc0
                        .getOplModel().getModelDefinition(), dataElements);
                rc.getCplex().setOut(null);
                rc.getOplModel().generate();

                System.out.println("Solve with capFlour = " + capFlour);
                if (rc.getCplex().solve()) {
                    curr = rc.getOplModel().getCplex().getObjValue();
                    System.out.println("OBJECTIVE: " + curr);
                    status = 0;
                } else {
                    System.out.println("No solution!");
                    status = 1;
                }
                capFlour++;
                dataElements.getElement("Capacity").asNumMap().set("flour",
                        capFlour);

                rc.end();
            } while (best != curr && status == 0);
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
}
