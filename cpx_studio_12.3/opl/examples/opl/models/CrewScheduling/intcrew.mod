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
 * OPL Model for Airline Crew Scheduling (Set Partitioning)
 * Integer program
 * 
******************************************************************************/


/******************************************************************************
 * DATA DECLARATIONS
 ******************************************************************************/

// Enumerated lists
{string} City = ...;
{string} Flight = ...;
range Flights = 0..card(Flight)-1;

// Array data
int Org[Flights] = ...;
int Dst[Flights] = ...;
tuple pairCst_T {
   int i;
   int v;
};
{pairCst_T} pairCst = ...; 

int pairCstSize = card(pairCst);
tuple pairIdx_T {
   int i;
   int j;
   int v;
};
{pairIdx_T} pairIdx = ...; 

tuple T {
   int j;
   int v;
};
{T} pairIdx2[f in Flights] = {<j,v> | <f,j,v> in pairIdx};
// Range
range Columns = 0..pairCstSize-1;


/******************************************************************************
 * MODEL DECLARATIONS
 ******************************************************************************/

dvar boolean pair[Columns];  // Amount for each pairing


/******************************************************************************
 * MODEL
 ******************************************************************************/

minimize
    sum (i in Columns) item(pairCst,i).v*pair[i];

subject to {
    cvr:
    forall (f in Flights : Org[f] != Dst[f])
            sum (t in pairIdx2[f]) pair[t.v] >= 1;
};
