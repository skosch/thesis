// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
// Copyright IBM Corporation 1998, 2011. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
// --------------------------------------------------------------------------

/******************************************************************************
 * 
 * ILOG Script for 1-Day Crew Scheduling
 * 
******************************************************************************/
/*** This example is forced on 1 single Workers as it can allocate a lot of memory with multiple Workers. ***/

tuple pairCst_T {
   int i;
   int v;
};
{pairCst_T} pairCst = ...;

tuple pairIdx_T {
   int i;
   int j;
   int v;
};
{pairIdx_T} pairIdx = ...;

{pairIdx_T} solIdx = ...;

main {
   thisOplModel.settings.mainEndEnabled = true;
   //System constants
   var NCVR  = 10;   // Initial pairings to cover each flight
   var NPRT  = 20;   // Print frequency
   var PayCr = 1.005;// Pay & Credit tolerance

   
   var time  = 0;          // Procedure time
   var date0;
   var totTime;            // Total computation time
   var colCnt;   

   /******************************************************************************
    * INITIALIZATION:
    * Generate initial pairings that cover all flights
    ******************************************************************************/
   
   // Load pairing cover model
   var cpConfig0 = new IloOplRunConfiguration("cvrpair.mod",
      "CrewScheduling.dat","City.dat","Flights.dat","Destin.dat","Origin.dat","Depart.dat","Arrive.dat","coverFlt.dat");
   var cpModel0 = cpConfig0.oplModel;
   var cpData0 = cpModel0.dataElements;
   cpModel0.generate();
   
   var nbFlights = cpData0.Flight.size;
   
   writeln("Starting CP search for initial pairings that cover every flight at least ",
         NCVR , " times");
   date0 = new Date();
   // Indices for master problem
   var pairIdx = thisOplModel.pairIdx;
   var pairCst = thisOplModel.pairCst;

   // Now, generate pairings for every flight
   for (var f=0; f<nbFlights; f++) {
      if (cpData0.Org[f]!=cpData0.Dst[f]) {
         var cpConfig = new IloOplRunConfiguration("cvrpair.mod");    
         var cpModel = cpConfig.oplModel;
         cpData0.coverFlt = f;
         cpModel.addDataSource(cpData0);
         cpModel.generate();         
         colCnt = 0;

         // Find pairings
		 cpConfig.cp.param.Workers = 1;
         cpConfig.cp.startNewSearch();
         while (cpConfig.cp.next()) {
            // Add the pairing to the list
            var size = pairCst.size;
            pairCst.add(size,cpModel.pay);
            // Add the flights to the pairing
            for (var i=1; i<= cpData0.nSeq; i++){ 
               if (cpData0.Org[cpModel.fltSeq[i]] != cpData0.Dst[cpModel.fltSeq[i]]) {
                   var g = cpModel.fltSeq[i];
                   var j = 0;
                   for (var z in pairIdx) {
                      if (z.i==g) {
                         j = j+1;
                      }
                   }
                   pairIdx.add(g,j,size);
               }
            }
            colCnt = colCnt + 1;
            if (colCnt == NCVR) 
               break;
         }
         
         if (colCnt < NCVR) {
             writeln("Only " , colCnt , " pairings found for flight " , f );
         }
               
       }
       if (((1+f) % NPRT) == 0) {
           writeln(100*(1+f)/nbFlights, "% flights covered");
       }
   }
   var date1 = new Date();
   time = date1 - date0;   
   writeln(pairCst.size, " INITIAL PAIRINGS, TIME: " , time);
   totTime = time;
   


   /******************************************************************************
    * COLUMN GENERATION:
    * Solve LP relaxation of crew set covering
    ******************************************************************************/

   // Setup LP data
   var LPerr     = 0; // Boolean for LP errors
   var totFlight = 0; // Total scheduled flight time
   for (f=0;f<nbFlights;f++) {
      if (cpData0.Org[f] != cpData0.Dst[f]) {
         totFlight = totFlight + cpData0.Arr[f] - cpData0.Dep[f];
      }
   }
   
   var lpConfig0 = new IloOplRunConfiguration("lincrew.mod","City.dat","Flights.dat","Origin.dat","Destin.dat","pairInit.dat");
   var lpModel0 = lpConfig0.oplModel;
   var lpData0 = lpModel0.dataElements;
   
   // Iterate until pay & credit is low enough (see below)
   while (true) {
    // Solve LP
      writeln("Solving LP with ",pairCst.size," total columns");
      date0 = new Date();
      var lpConfig = new IloOplRunConfiguration("lincrew.mod");
      var lpModel = lpConfig.oplModel;
      lpData0.pairCst = pairCst;
      lpData0.pairIdx = pairIdx;
      lpModel.addDataSource(lpData0);
      lpModel.generate();
      if (lpConfig.cplex.solve() == false) {
         writeln("Error in LP relaxation: no solution found!");
         LPerr = 1;
         break;
      }
      lpModel.postProcess();
      // Process objective value; exit loop when pay/credit is low enough
      var lObj = lpConfig.cplex.getObjValue();
      date1 = new Date();
      time = date1-date0;
      totTime = totTime + time;
      writeln("OPTIMUM LP SOLUTION = " , lObj, ", PAY/CREDIT = " , lObj / totFlight, ", TIME: ", time);
      if (lObj / totFlight <= PayCr)  {
          writeln("COLUMN GENERATION COMPLETE: PAY/CREDIT TARGET REACHED");
          break;
      }
      
      //optimal pairing model
      var opSrc = new IloOplModelSource("optpair.mod");
      var opDef = new IloOplModelDefinition(opSrc);
      var opEngine = new IloCP();
      var op = new IloOplModel(opDef,opEngine);
      var dataFlights = new IloOplDataSource("Flights.dat");
      var dataCrew = new IloOplDataSource("CrewScheduling.dat");
      var dataCity = new IloOplDataSource("City.dat");
      var dataArrive = new IloOplDataSource("Arrive.dat");
      var dataDepart = new IloOplDataSource("Depart.dat");
      var dataOrigin = new IloOplDataSource("Origin.dat");
      var dataDestin = new IloOplDataSource("Destin.dat");
      op.addDataSource(dataFlights);
      op.addDataSource(dataCrew);
      op.addDataSource(dataCity);
      op.addDataSource(dataArrive);
      op.addDataSource(dataDepart);
      op.addDataSource(dataOrigin);
      op.addDataSource(dataDestin);
      
      //copy dual values to the op model
      var dataDuals = new IloOplDataElements();
      dataDuals.fltCst = lpModel.fltCst;
      op.addDataSource(dataDuals);
      
      // Find (nearly) optimal pairing
      writeln("Starting partial CP search for best new pairing");
      var pObj;
      op.generate();
      
      date0 = new Date();
      opEngine.startNewSearch();
      while (opEngine.next()) {
        pObj = opEngine.getObjValue();
        writeln("Found pairing in ", opEngine.info.NumberOfFails, " fails with LP reduced cost = ", pObj);
        opEngine.param.FailLimit = opEngine.info.NumberOfFails+250;
    }
    opEngine.endSearch();
    //op.restore();
    date1 = new Date();
    time = date1-date0;
    totTime = totTime + time;
    if (pObj <= 0) {
       writeln("COLUMN GENERATION COMPLETE: NO MORE ENTERING COLUMNS, TIME: ",time);
        break;
    }
    //entering pairing model
    var epSrc = new IloOplModelSource("entpair.mod");
    var epDef = new IloOplModelDefinition(epSrc);
    var epEngine = new IloCP();
    var ep = new IloOplModel(epDef,epEngine);
    ep.addDataSource(dataCrew);
    ep.addDataSource(dataFlights);
    ep.addDataSource(dataCity);
    ep.addDataSource(dataArrive);
    ep.addDataSource(dataDepart);
    ep.addDataSource(dataOrigin);
    ep.addDataSource(dataDestin);
    ep.addDataSource(dataDuals);
     
    // Find all pairings within 2/3 of best known pairing
    var epData = new IloOplDataElements();
    epData.minCost = 2*pObj/3;
    writeln("epData.minCost = ",2*pObj/3); 
    ep.addDataSource(epData);
    colCnt = 0;
    writeln("Starting full CP search for pairings with LP reduced cost >= ",epData.minCost);
    date0 = new Date();
    ep.generate();
    epEngine.startNewSearch();
    while (epEngine.next()) {
        // Add pairing to the list
            size = pairCst.size;
            pairCst.add(size,ep.pay);
            // Add the flights to the pairing
            for (i=1; i<= cpData0.nSeq; i++){ 
               if (cpData0.Org[cpModel.fltSeq[i]] != cpData0.Dst[cpModel.fltSeq[i]]) {
                   g = cpModel.fltSeq[i];
                   j = 0;
                   for (z in pairIdx) {
                      if (z.i==g) {
                         j = j+1;
                      }
                   }
                   pairIdx.add(g,j,size);
               }
            } 
            
        colCnt = colCnt + 1;
        if (colCnt % NPRT == 0) { 
            writeln("Generated ", colCnt, " pairings");
        }
    }
    epEngine.endSearch();
    date1=new Date();
    time = date1-date0;
    totTime = totTime+time;
    writeln(colCnt, " NEW PAIRINGS WITH REDUCED COST >= ",epData.minCost, ", TIME: ", time);
    
    ep.end();
    op.end();
    lpModel.end();
   }//end while     
   /******************************************************************************
    * CREW SCHEDULING:
    * Solve IP with the given set of columns
    ******************************************************************************/
    
   
   // Load and solve IP crew covering
   date0 = new Date();
   var ipConfig = new IloOplRunConfiguration("intcrew.mod");
   var ipModel = ipConfig.oplModel;
   lpData0.pairCst = pairCst;
   lpData0.pairIdx = pairIdx;
   ipModel.addDataSource(lpData0);   
   ipModel.generate();
   writeln( "Solving IP with ", pairCst.size, " total columns.  Please be patient.")
   if (ipConfig.cplex.solve() == false) {
       writeln ("Error in IP: no solution found!");
   } else {
     // Process objective value
     var Obj = ipConfig.cplex.getObjValue();
     writeln ("OPTIMUM SOLUTION = ", Obj, ", TIME: ", time);
   }
   date1=new Date();
   totTime = totTime + date1-date0;
   


   /******************************************************************************
    * OUTPUT:
    * Print solution
    ******************************************************************************/
   // First, we have to be clever to create an index back to crews (columns)
   var solIdx = thisOplModel.solIdx;
   var solIdx_up = -1;
   for (f=0; f < nbFlights; f++) {
      for (j in pairIdx) {//
         if (j.i==f && ipModel.pair[j.v] == 1) {
            solIdx_up = solIdx_up + 1;
            solIdx.add(j.v, solIdx_up, f);
         }
       }
   }
   var payArr = new Array(pairCst.size);
   for (var p in pairCst)
      payArr[p.i] = p.v;
   
   // Now print the crew schedule for each crew
   writeln();
   writeln("CREW SCHEDULE:");
   var nCrew = 0;
   var FlightArray = new Array(nbFlights);
   var nFlight = -1;
   for (var flight in cpData0.Flight) {
      nFlight = nFlight+1;
      FlightArray[nFlight] = flight;
   }
   
   for (j=0; j<pairCst.size;j++) {
      if (ipModel.pair[j] == 1) {
         nCrew = nCrew + 1;
         writeln();
         writeln("Crew ", nCrew, " uses pairing ", j+1);
         var pay = Math.round(payArr[j]);
         var maxSolIdx = 0;
         var minSolIdx = 0;
         var totalflight = 0;
         for (var s in solIdx) {
            if (s.i == j) {
              write (FlightArray[s.v]," ");
              var dep     = cpModel0.Dep[s.v];
              var arr     = cpModel0.Arr[s.v];
              var depH    = Math.floor(dep/60) % 12;
              var arrH    = Math.floor(arr/60) % 12;
              var depM    = dep % 60;
              var  arrM   = arr % 60;
              var  depZ   = "";
              var  arrZ   = "";
              var  depAP  = " pm";
              var  arrAP  = " pm";
              
              if (depH == 0)  depH = 12;
              if (arrH == 0)  arrH = 12;
              if (depM < 10)  depZ = "0";
              if (arrM < 10)  arrZ = "0";
              if (dep < 720)  depAP = " am";
              if (arr < 720)  arrAP = " am";
              
              write(depH, ":", depZ, depM, depAP);
              write(" -> ");
              writeln(arrH , ":" , arrZ , arrM , arrAP);
              totalflight = totalflight + cpData0.Arr[s.v] - cpData0.Dep[s.v];
              maxSolIdx = Math.max(maxSolIdx, cpData0.Arr[s.v]);
              minSolIdx = Math.min(minSolIdx, cpData0.Dep[s.v]);
            }
         }
         //publish summary data
         var duty = maxSolIdx - minSolIdx;
         writeln("Pay = ", pay, ", Duty = ",duty,", Flight = ", totalflight);
            
      }
   }
   writeln();
   writeln();    
   writeln("OPTIMUM SOLUTION = ", Obj, ", PAY/CREDIT = ", Obj/totFlight);
   writeln("TOTAL COMPUTATION TIME: ", totTime);


   // release memory
   ipConfig.end();

   lpConfig0.end();
   cpConfig0.end();    
}
