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
 * OPL Model for Airline Crew Pairing
 * optpair: Find best pairing with respect to dual values
 * 
******************************************************************************/

using CP;

/******************************************************************************
 * DATA DECLARATIONS
 ******************************************************************************/

execute{
	}

// Parameters
int nSeq    = ...;
int minStop = ...;
int hubStop = ...;
int maxStop = ...;
int minPay  = ...;
int maxDuty = ...;

// Enumerated lists
{string} City = ...;
range Cities = 0..card(City)-1;
{string} Flight = ...;
range Flights = 0..card(Flight)-1;

// Array data
int    Org[Flights] = ...; //City
int    Dst[Flights] = ...; //City
int     Dep[Flights] = ...;
int     Arr[Flights] = ...;
float     fltCst[Flights] = ...;

// Set data
{string}  Hub0 = ...;
{int} Hub = {ord(City,h) | h in Hub0};

// Ranges for the flights in a sequence
range cityRng = 0..nSeq;
range fltRng = 1..nSeq;

// Pay data
int     nPay    = 3;
int     maxPay  = maxDuty;
float     minObj  = nSeq*(min (f in Flights) fltCst[f]) - maxPay;
float     maxObj  = nSeq*(max (f in Flights) fltCst[f]);



/******************************************************************************
 * MODEL DECLARATIONS
 ******************************************************************************/

// Variables
dvar int    citySeq[cityRng];                   // sequence of cities
dvar int  fltSeq[fltRng];                     // sequence of flights
dvar int+    start   in 0..1440;                 // start time
dvar int+    end     in 0..1440;                 // end time
dvar int+    duty    in 0..1440;                 // duty period
dvar int+    flight  in 0..1440;                 // time spent flying
dvar int+    payForm[1..nPay] in 0..maxPay;      // pay formulas
dvar int+    pay in minPay..maxPay;              // overall pay
//dexpr float  obj = sum (i in fltRng) fltCst[fltSeq[i]];              // objective value
dexpr float obj = sum (i in Flights) fltCst[i]*(count(fltSeq,i) >=1);

// allowed assignments for cities and flights
tuple allowed {
   int f;
   int o;
   int d;
};
{allowed} allowedSet = {<f,Org[f],Dst[f]> | f in Flights};


/******************************************************************************
 * MODEL
 ******************************************************************************/

maximize
    obj - pay;
subject to {
    
    // Define objective function
    obj - pay >= minObj;
    obj - pay <= maxObj;
      
    
    /******************************************************************************
     * STRUCTURAL CONSTRAINTS
     ******************************************************************************/

    // Define times
    start   == Dep[fltSeq[1]];
    end     == max (i in fltRng) Arr[fltSeq[i]];
    duty    == end - start;
    flight  == sum (i in fltRng)
              ((citySeq[i-1] != citySeq[i])*(Arr[fltSeq[i]]-Dep[fltSeq[i]]));

    // Define pay
    payForm[1]  == minPay;
    payForm[2]  == duty/2;
    payForm[3]  == flight;
    pay         == max (i in 1..nPay) payForm[i];

    // Link flights and cities using predicate
    forall (i in fltRng)
         allowedAssignments(allowedSet,fltSeq[i], citySeq[i-1], citySeq[i]);

    
    
    /******************************************************************************
     * MODEL CONSTRAINTS
     ******************************************************************************/

    // Constrain layovers; use double index for better propagation
    forall (i,j in fltRng : i < j)
              Arr[fltSeq[i]] + minStop + (hubStop-minStop) * (citySeq[i] in Hub)
              <= Dep[fltSeq[j]];
    forall (i in fltRng : i > 1)
              (Dep[fltSeq[i]] < Arr[fltSeq[i-1]] + maxStop)
              || (citySeq[i] == citySeq[i-1]);
    
    // Constrain dummy flights to end of sequence; use double index for better propagation
    citySeq[0] != citySeq[1];
    forall (i,j in fltRng : 1 < i < j)
              (citySeq[i-1] == citySeq[i]) => (citySeq[j-1] == citySeq[j]);
    
    // Limit duty period
    duty <= maxDuty;
   
    // Sequence must be a rotation: crews start and end in same city
    citySeq[0] == citySeq[nSeq];
    
};



/******************************************************************************
 * DISPLAY
 ******************************************************************************/
execute DISPLAY {
    writeln( pay );
    writeln( duty );
    writeln( flight );
    writeln( 100*pay/flight );
    writeln( citySeq[0] );
}
