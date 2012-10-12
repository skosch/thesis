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
//Java version of cutstock.cpp of OPL distrib
//--------------------------------------------------------------------------
package cutstock;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.opl.*;

public class Cutstock {
    static final String DATADIR = ".";

    static final double RC_EPS = 1.0e-6;

    static public void main(String[] args) throws Exception {
        int status = 127;
        try {
            IloOplFactory.setDebugMode(true);
            IloOplFactory oplF = new IloOplFactory();
            IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
            IloOplSettings settings = oplF.createOplSettings(errHandler);

            // make master model
            IloCplex masterCplex = oplF.createCplex();
            masterCplex.setOut(null);

            IloOplRunConfiguration masterRC0 = oplF.createOplRunConfiguration(
                    DATADIR + "/cutstock.mod", DATADIR + "/cutstock.dat");
            masterRC0.setCplex(masterCplex);
            IloOplDataElements masterDataElements = masterRC0.getOplModel()
                    .makeDataElements();

            // prepare sub-model source, definition and engine
            IloOplModelSource subSource = oplF.createOplModelSource(DATADIR + "/cutstock-sub.mod");
            IloOplModelDefinition subDef = oplF.createOplModelDefinition(subSource, settings);
            IloCplex subCplex = oplF.createCplex();
            subCplex.setOut(null);

            int nbItems = masterRC0.getOplModel().getElement("NbItems").asInt();
            IloIntRange items = masterRC0.getOplModel().getElement("Items").asIntRange();

            double best;
            double curr = Double.MAX_VALUE;
            do {
                best = curr;

                masterCplex.clearModel();

                IloOplRunConfiguration masterRC = oplF.createOplRunConfiguration(
                        masterRC0.getOplModel().getModelDefinition(),
                        masterDataElements);
                masterRC.setCplex(masterCplex);
                masterRC.getOplModel().generate();

                System.out.println("Solve master.");
                if (masterCplex.solve()) {
                    curr = masterCplex.getObjValue();
                    System.out.println("OBJECTIVE: " + curr);
                    status = 0;
                } else {
                    System.out.println("No solution!");
                    status = 1;
                }

                // prepare sub-model data
                IloOplDataElements subDataElements = oplF.createOplDataElements();
                subDataElements.addElement(masterDataElements.getElement("RollWidth"));
                subDataElements.addElement(masterDataElements.getElement("Size"));
                subDataElements.addElement(masterDataElements.getElement("Duals"));
                // get reduced costs and set them in sub problem
                IloNumMap duals = subDataElements.getElement("Duals").asNumMap();
                for (int i = 1; i < nbItems + 1; i++) {
                    IloForAllRange forAll = (IloForAllRange) masterRC.getOplModel()
                            .getElement("ctFill").asConstraintMap().get(i);
                    duals.set(i, masterCplex.getDual(forAll));
                }
                // make sub-model
                IloOplModel subOpl = oplF.createOplModel(subDef, subCplex);
                subOpl.addDataSource(subDataElements);
                subOpl.generate();

                System.out.println("Solve sub.");
                if (subCplex.solve()) {
                    System.out.println("OBJECTIVE: "
                            + subCplex.getObjValue());
                    status = 0;
                } else {
                    System.out.println("No solution!");
                    status = 1;
                }
                if (subCplex.getObjValue() > -RC_EPS)
                    break;

                // Add variable in master model
                IloIntMap newFill = masterCplex.intMap(items);
                for (int i = 1; i < nbItems + 1; i++) {
                    newFill.set(i, (int) subCplex.getValue(
                            subOpl.getElement("Use").asIntVarMap()
                                    .get(i)));
                }
                IloTupleBuffer buf = masterDataElements.getElement("Patterns")
                        .asTupleSet().makeTupleBuffer(-1);
                buf.setIntValue("id", masterDataElements.getElement("Patterns")
                        .asTupleSet().getSize());
                buf.setIntValue("cost", 1);
                buf.setIntMapValue("fill", newFill);
                buf.commit();

                subOpl.end();
                masterRC.end();
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
