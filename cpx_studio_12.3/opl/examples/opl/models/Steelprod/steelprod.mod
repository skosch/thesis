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

/* ---------------------------------------------------------------
   OPL Model for Steel Production Example 
   
This model is described in the documentation. 
See "Samples" in the Documentation home page.
 
--------------------------------------------------------------------*/

{string} Products = ...;
int      T = ...;
{string} Resources = ...;
range TimePeriods = 1..T;

float ResourceReq[Products][Resources] = ...;  //Bills of materials   
float Avail[Resources][TimePeriods]=...;  //Available resources in each period 
float Inv0[Products] = ...;     // Initial inventories
float Backorder0[Products]=...; // Initial backorders
float EndBlg[Products]=...; //Bounds on ending backorder levels
float EndInv[Products]=...;//Target ending inventory levels

float Demand[Products][TimePeriods] = ...;
float Prodcost[Products][TimePeriods] = ...; // Variable production cost
float Invcost[Products][TimePeriods] = ...;  // Unit inventory holding cost

float Backlogcost[Products][TimePeriods]=...;// Unit backorder cost

dvar float+ Backorder[Products][0..T];
dvar float+ Make[Products][TimePeriods]; // Production quantities in each period
dvar float+ Inv[Products][0..T];

dexpr float TotalCost =
   sum(p in Products,t in TimePeriods)
       (Backlogcost[p,t] * Backorder[p,t]
       + Prodcost[p,t]*Make[p,t] + Invcost[p,t]*Inv[p,t]);

minimize TotalCost;

subject to{
   
  // Resource capacity constraints
  forall(r in Resources, t in TimePeriods) 
    ctAvail: 
      sum(p in Products) 
        ResourceReq[p][r]*Make[p][t] <= Avail[r][t];
   
  // Initial inventories and backorders
  forall(p in Products) 
    Inv[p][0] == Inv0[p];
  forall(p in Products)
    Backorder[p][0] == Backorder0[p];
   
  // Inventory flow balance constraints
  // The left side represents the history:
  //    the prior inventory less the backorders plus the current production
  // The right side represents the future:
  //    the current demand plus the carryover inventory less the backorders
  // (If this seems confusing, think about the case when there are no backorders)
  forall(p in Products,t in TimePeriods)
    Inv[p][t-1] - Backorder[p][t-1] + Make[p][t] == Demand[p][t] + Inv[p][t] - Backorder[p][t];
   
  // Bounds on ending backorder levels
  forall(p in Products) 
    ctEndBlg: Backorder[p][T] <= EndBlg[p];
  // Target ending inventory levels 
  forall(p in Products) 
    Inv[p][T] == EndInv[p];      
    
}
