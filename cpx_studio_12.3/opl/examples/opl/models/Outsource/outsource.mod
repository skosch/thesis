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

/**************************************************************
Outscourcing Example

This model is described in the documentation. 
See "Samples" in the Documentation home page.
   
**************************************************************/

int NumAmts = ...; // # of discount ranges
{string} Items = ...;
{string} Suppliers = ...;
{string} AUDsuppliers = ...; // AUD (All-Units-Discount) suppliers
{string} CQDsuppliers = ...; // ICD (Incremental-Quantity-Discount) suppliers 

assert card(CQDsuppliers union AUDsuppliers) == card(Suppliers);

range Amts = 1..NumAmts; // Indices of the discount ranges

int ItemDemand[Items] = ...; 
int TotalSupplierCap[Suppliers] = ...; // Supply capacity for all items at each supplier
int BreakAmts[1..NumAmts+1] = ...; // Discount Breaks
int SetupCost[Suppliers] = ...;

//float+ basicCost[Items][Suppliers] = ...;
float CostInRanges[Items][Suppliers][Amts] = ...; //Cost difference between consecutive discount ranges

int MaxN = sum(i in Suppliers) TotalSupplierCap[i];


dvar int Quantity[Items][Suppliers][Amts] in 0..MaxN;
dvar int SupAmt[Items][Suppliers][Amts] in 0..1; // For AUD
dvar int TotalQuantity[Items][Suppliers] in 0..MaxN;
dvar int Setup[Suppliers] in 0..1;

dexpr float TotalVariableCost =
  sum(i in Items, s in Suppliers, a in Amts) CostInRanges[i][s][a] 
                                                 * Quantity[i][s][a] ;
dexpr float TotalSetupCost =
  sum(s in Suppliers) SetupCost[s]*Setup[s];                                            
  
minimize TotalVariableCost + TotalSetupCost;
                                                 
subject to {
  // Capacity Constraints
  forall(s in Suppliers)
    ctCaps: sum(i in Items, a in Amts) Quantity[i][s][a] <= TotalSupplierCap[s];
       
  // Satisfy Demand
  forall(i in Items)
    ctDem: sum(s in Suppliers, a in Amts) Quantity[i][s][a] >= ItemDemand[i];
      
  // supAmt in one range only for each item and each All-Unit-Discount supplier
  forall(i in Items, s in AUDsuppliers)
    sum(a in Amts) SupAmt[i][s][a] == 1;
        
  // Force AUD quantity to be in correct range
  forall(i in Items, s in AUDsuppliers, a in Amts) {
    Quantity[i][s][a] <= (BreakAmts[a+1]-1) * SupAmt[i][s][a];
    Quantity[i][s][a] >= (BreakAmts[a]) * SupAmt[i][s][a];
  }
   
  //Setup indicator variables: If a supplier orders any positive quantity of any items, 
  // a setup cost occurs.
  forall(s in Suppliers) 
    ctSetup: Setup[s]*MaxN >= sum(i in Items, a in Amts) Quantity[i][s][a];

  //Force quantity in discount range at CQD supplier to be incremental 
  forall(i in Items, s in CQDsuppliers, a in Amts) {
    forall(k in 1..a-1) {
      // Because the "quantity" for CQDs is incremental, if CQD order quantity lies 
      // in discount interval a, namely, sup[i,s,a]=1, then
      // the quantities in interval 1 to a-1, should be the length of those ranges.  
      Quantity[i][s][k] >= (BreakAmts[k+1]-BreakAmts[k])*SupAmt[i][s][a];
    }
    // quantity in each range is no greater than the width of the range      
    Quantity[i][s][a] <= (BreakAmts[a+1]-BreakAmts[a])*SupAmt[i][s][a];             
  }

  // Total Quantity ordered for each item from each supplier
  forall(i in Items, s in Suppliers)
    sum(a in Amts) Quantity[i][s][a] == TotalQuantity[i][s];
      
}

execute DISPLAY {
   writeln("TotalQuantity = ", TotalQuantity);
}
