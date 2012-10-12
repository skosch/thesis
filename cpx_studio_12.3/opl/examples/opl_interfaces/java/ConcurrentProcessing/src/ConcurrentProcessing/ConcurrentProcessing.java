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
//Java version of concurrent processing with OPL
//--------------------------------------------------------------------------

package ConcurrentProcessing;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ConcurrentProcessing.PortfolioProblem.Request;
import ConcurrentProcessing.PortfolioProblem.Result;

// This example aims at showing OPL multithreading capabilities in a real application.
// In this financial sample, we need to solve the same model N times, 
// only the data are changing from one instance to the other.
// From the results of the N OPL runs, we will calculate the tangental portfolio.
//
// To improve the OPL solving time, we will generate and solve multiple OPL instances at the same time  
// by using m threads, each of them will contain one and only one instance of IloOplFactory.
// The IloOplFactory is the OPL object that certify the multithreading integrity of OPL.
//
// This example simulates a client/server architecture: for each of the OPL run, 
// we will ask a pseudo server to create and solve an OPL model.

public class ConcurrentProcessing {
  private static int DEFAULT_THREADS = 4;
  private static final int DEFAULT_SAMPLES = 150;
  private static final double DEFAULT_RFR = 0.02;

  public static void main(String[] args) {
    int threads = DEFAULT_THREADS;
    int samples = DEFAULT_SAMPLES;
    double rfr = DEFAULT_RFR;

    if (args.length >= 1) {
      threads = Integer.parseInt(args[0]);
    }
    if (args.length >= 2) {
      samples = Integer.parseInt(args[1]);
    }
    if (args.length >= 3) {
      rfr = Double.parseDouble(args[2]);
    }

    System.out.println("Using " + threads + " threads for " + samples + " samples.");

    // warming for accurate efficiencies
    calculate(1, samples, rfr);


    // solve using 1 thread to get a reference value
    long t1 = calculate(1, samples, rfr);
    double throughput1 = 1000.0 * samples / t1;
    System.out.println("Reference duration for 1 thread = " + t1);
    System.out.println("Reference thoughput was " + throughput1 + " problems per second.");
    System.out.println();

    // solve using several threads (as specified in the arguments, 4 by default)
    long tn = calculate(threads, samples, rfr);
    double parallelEfficiency = 100.0 * t1 / (threads * tn);
    System.out.println("Efficiency for " + threads + " threads = " + parallelEfficiency
        + "%, duration = " + tn);
    double throughput = 1000.0 * samples / tn;
    System.out.println("Thoughput was " + throughput + " problems per second.");
    System.out.println();

    // solve using maximum number of threads available and suggesting best number of threads
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    if (threads != availableProcessors) {
      System.out.println("Recommended number of threads is " + availableProcessors);
      tn = calculate(availableProcessors, samples, rfr);
      parallelEfficiency = 100.0 * t1 / (availableProcessors * tn);
      System.out.println("Efficiency for recommended " + availableProcessors + " threads = "
          + parallelEfficiency + "%, duration = " + tn);
      throughput = 1000.0 * samples / tn;
      System.out.println("Thoughput was " + throughput + " problems per second.");
    }
  }

  private static long calculate(int threads, int samples, double rfr) {
    try {
      // Create the request to sumbit to the pseudo OPL server.
      List<Request> requests = makeRequests(samples);
      long t1 = System.currentTimeMillis();
      List<Result> results = submitRequests(requests, threads);
      long t2 = System.currentTimeMillis();
      processResults(results, rfr);
      return t2 - t1;
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (IloException e) {
      e.printStackTrace();
    }
    return -1;
  }

  private static List<Request> makeRequests(int samples) {
    // We solve the same model N times, only the data is changing from 1 instance to the other.
    // Here only rho is changing, the rest of the data is fixed and stored in an OPL .dat file
    List<Request> tasks = new ArrayList<Request>();
    for (int i = 0; i < samples; i++) {
      final double rho = i / (samples - 1.0);
      // Create a request criteria.
      tasks.add(new Request(rho));
    }
    return tasks;
  }

  private static List<Result> submitRequests(List<Request> requests, int threads)
      throws InterruptedException, ExecutionException, IloException {
    // Transformation of the request criteria (rho values) into real requests.
    List<Callable<Result>> tasks = getCallables(requests);

    ExecutorService service = Executors.newFixedThreadPool(threads);
    List<Future<Result>> result = service.invokeAll(tasks);
    service.shutdownNow();
    List<Result> ret = getResults(result);
    return ret;
  }

  private static List<Callable<Result>> getCallables(Collection<Request> reqs) {
    List<Callable<Result>> tasks = new ArrayList<Callable<Result>>();
    final Iterator<Request> it = reqs.iterator();
    while (it.hasNext()) {
      final Request m = it.next();
      tasks.add(new Callable<Result>() {
        public Result call() throws Exception {
          return (new PortfolioProblem()).solve(m);
        }
      });
    }
    return tasks;
  }

  private static List<Result> getResults(List<Future<Result>> result) throws InterruptedException,
      ExecutionException, IloException {
    List<Result> ret = new ArrayList<Result>();
    Iterator<Future<Result>> it = result.iterator();
    while (it.hasNext()) {
      Future<Result> future = it.next();
      if (future.isDone())
        ret.add(future.get());
      else
        throw new IloException("Some request returned empty result");
    }
    return ret;
  }

  private static void processResults(List<Result> results, double rfr) throws InterruptedException,
      ExecutionException {

    double optimum = Double.MIN_VALUE;
    double optimumR = 0.0;
    double optimumV = 0.0;
    double optimumRho = 0.0;
    Iterator<Result> it = results.iterator();
    while (it.hasNext()) {
      Result curr = it.next();
      double totalR = curr.getTotalReturn();
      double totalV = curr.getTotalVariance();
      double rho = curr.getRho();
      double tmp = (totalR - rfr) / totalV;
      if (tmp > optimum) {
        optimum = tmp;
        optimumR = totalR;
        optimumV = totalV;
        optimumRho = rho;
      }
    }
    System.out.println("Tangental portfolio for rfr " + rfr + " is at rho=" + optimumRho
        + " with total return of " + optimumR + ", variance is " + optimumV);
  }
}
