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
//Java version of cutstock_change.cpp of OPL distrib
//--------------------------------------------------------------------------
package cutstock_change;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.opl.*;
import java.util.*;

public class Cutstock_change {
    static final String DATADIR = ".";

    static final double RC_EPS = 1.0e-6;

    static public void main(String[] args) throws Exception {
        int status = 127;
        try {
            IloOplFactory.setDebugMode(true);
            IloOplFactory oplF = new IloOplFactory();

            // make master model
            IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
            IloOplSettings settings = oplF.createOplSettings(errHandler);

            IloOplRunConfiguration masterRC = oplF.createOplRunConfiguration(
                    DATADIR + "/cutstock_change.mod", DATADIR
                            + "/cutstock_change.dat");
            masterRC.setErrorHandler(errHandler);
            IloCplex masterCplex = oplF.createCplex();
            masterCplex.setOut(null);

            masterRC.setCplex(masterCplex);
            IloOplModel masterOpl = masterRC.getOplModel();
            masterOpl.generate();
            IloOplDataElements masterDataElements = masterOpl.makeDataElements();

            int nWdth = masterDataElements.getElement("Amount").asIntMap()
                    .getSize();
            ArrayList<IloNumVar> masterVars = new ArrayList<IloNumVar>();
            IloNumVarMap cuts = masterOpl.getElement("Cut").asNumVarMap();
            for (int i = 1; i <= nWdth; i++) {
                masterVars.add(cuts.get(i));
            }

            // prepare sub model source, definition and engine
            IloOplModelSource subSource = oplF.createOplModelSource(DATADIR + "/cutstock-sub.mod");
            IloOplModelDefinition subDef = oplF.createOplModelDefinition(subSource, settings);
            IloCplex subCplex = oplF.createCplex();
            subCplex.setOut(null);

            double best;
            double curr = Double.MAX_VALUE;
            do {
                best = curr;

                // Make master model
                System.out.println("Solve master.");
                if (masterCplex.solve()) {
                    curr = masterCplex.getObjValue();
                    System.out.println("OBJECTIVE: " + curr);
                    status = 0;
                } else {
                    System.out.println("No solution!");
                    status = 1;
                }

                // prepare data for sub model
                IloOplDataElements subDataElements = oplF.createOplDataElements();
                subDataElements.addElement(masterDataElements.getElement("RollWidth"));
                subDataElements.addElement(masterDataElements.getElement("Size"));
                subDataElements.addElement(masterDataElements.getElement("Duals"));
                // get reduced costs and set them in sub problem
                IloNumMap duals = subDataElements.getElement("Duals").asNumMap();
                for (int i = 1; i < nWdth + 1; i++) {
                    IloForAllRange forAll = (IloForAllRange) masterOpl.getElement(
                            "ctFill").asConstraintMap().get(i);
                    duals.set(i, masterCplex.getDual(forAll));
                }
                // make sub model
                IloOplModel subOpl = oplF.createOplModel(subDef, subCplex);
                subOpl.addDataSource(subDataElements);
                subOpl.generate();

                System.out.println("Solve sub.");
                if (subCplex.solve()) {
                    System.out.println("OBJECTIVE: " + subCplex.getObjValue());
                    status = 0;
                } else {
                    System.out.println("No solution!");
                    status = 1;
                }
                if (subCplex.getObjValue() > -RC_EPS)
                    break;

                // Add variable in master model
                IloNumVar newVar = masterCplex.numVar(0, Double.MAX_VALUE);
                IloObjective masterObj = masterOpl.getObjective();
                masterCplex.setLinearCoef(masterObj, newVar, 1);
                for (int i = 1; i < nWdth + 1; i++) {
                    double coef = subCplex.getValue(subOpl.getElement("Use")
                            .asIntVarMap().get(i));
                    IloForAllRange forAll = (IloForAllRange) masterOpl.getElement(
                            "ctFill").asConstraintMap().get(i);
                    masterCplex.setLinearCoef(forAll, newVar, coef);
                }
                masterVars.add(newVar);

                subOpl.end();
            } while (best != curr && status == 0);

            IloNumVar[] masterVarsA = new IloNumVar[masterVars.size()];
            masterVars.toArray(masterVarsA);
            masterCplex.add(masterCplex.conversion(masterVarsA, IloNumVarType.Int));
            if (masterCplex.solve()) {
                System.out.println("OBJECTIVE: " + masterCplex.getObjValue());
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
}
