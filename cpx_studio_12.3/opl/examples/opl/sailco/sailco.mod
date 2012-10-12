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

int NbPeriods = ...;
range Periods = 1..NbPeriods;

float Demand[Periods] = ...;
float RegularCost = ...;
float ExtraCost = ...;
float Capacity = ...;
float Inventory = ...;
float InventoryCost = ...;

dvar float+ RegulBoat[Periods];
dvar float+ ExtraBoat[Periods];
dvar float+ Inv[0..NbPeriods];


minimize
   RegularCost * 
   ( sum( t in Periods ) RegulBoat[t] ) +
   ExtraCost * 
   ( sum( t in Periods ) ExtraBoat[t] ) +
   InventoryCost * 
   ( sum(t in Periods ) Inv[t] );
   
subject to {
  ctInit:  
    Inv[0] == Inventory;
  forall( t in Periods ) 
    ctCapacity:
      RegulBoat[t] <= Capacity;
  forall( t in Periods )
    ctBoat: 
      RegulBoat[t] + ExtraBoat[t] + Inv[t-1] == Inv[t] + Demand[t];
}
