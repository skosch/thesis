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
// Java version of concurrent processing with OPL
//--------------------------------------------------------------------------

package ConcurrentProcessing;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.IntParam;
import ilog.opl.IloOplDataElements;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;

public class PortfolioProblem {

  // class to stock the request data
  static class Request {
    private final double _rho;

    public Request(double rho) {
      _rho = rho;
    }

    public double getRho() {
      return _rho;
    }
  }

  // class to stock the result of the OPL thread execution
  static class Result {
    private final double _totalReturn;
    private final double _totalVariance;
    private final double _rho;

    public Result(double r, double v, double rho) {
      _totalReturn = r;
      _totalVariance = v;
      _rho = rho;
    }

    public double getTotalReturn() {
      return _totalReturn;
    }

    public double getTotalVariance() {
      return _totalVariance;
    }

    public double getRho() {
      return _rho;
    }
  }

  static final String DATADIR = ".";

  public PortfolioProblem() {
  }

  public Result solve(Request request) throws IloException {
    double rho = request.getRho();

    IloOplFactory.setDebugMode(true);
    // Create one OPL factory by thread.
    // The OPL factory will create and handle all the needed OPL objects.
    IloOplFactory oplF = new IloOplFactory();
    try {
      // Create the OPL model source based on the .mod file.
      IloOplModelSource source = oplF.createOplModelSource(DATADIR + "/portfolio.mod");
      // Create the OPL data source based on the .dat file.
      IloOplDataSource dataSource = oplF.createOplDataSource(DATADIR + "/portfolio.dat");

      // Create an error handler.
      IloOplErrorHandler handler = oplF.createOplErrorHandler();
      // Create the default settings.
      IloOplSettings settings = oplF.createOplSettings(handler);
      // Create the OPL model definition by linking the source and the settings.
      IloOplModelDefinition def = oplF.createOplModelDefinition(source, settings);
      // Gets the algorithm.
      IloCplex cplex = oplF.createCplex();
      // Create the OPL model from the OPL defition and the algorithm.
      IloOplModel opl = oplF.createOplModel(def, cplex);

      // Create the OPL data elements.
      // we will use them to update the missing data from the .dat file
      // in this sample, we will update rho.
      IloOplDataElements dataElements = oplF.createOplDataElements();
      dataElements.addElement(dataElements.makeElement("Rho", rho));

      // Add the the different data sources to the OPL model.
      // In this sample, we have a .dat file giving all values except rho
      // and an OPL element giving the value of rho.
      opl.addDataSource(dataElements);
      opl.addDataSource(dataSource);

      // Generate the model.
      opl.generate();

      cplex.setOut(null);
      // Here we force CPLEX to use only 1 thread to get only the OPL multithreading efficiency
      // without interference from CPLEX.
      cplex.setParam(IntParam.Threads, 1);
      if (!cplex.solve()) {
        throw new IloException("solve failed");
      }

      double totalReturn = opl.getElement("TotalReturn").asNum();
      double totalVariance = opl.getElement("TotalVariance").asNum();
      // Create the result handler to return.
      Result result = new Result(totalReturn, totalVariance, rho);
      return result;
    } finally {
      oplF.end();
    }
  }
}
