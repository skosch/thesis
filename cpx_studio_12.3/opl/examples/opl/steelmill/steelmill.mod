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

// This problem is based on "prob038: Steel mill slab design problem" from
// CSPLib (www.csplib.org). It is a simplification of an industrial problem
// described in J. R. Kalagnanam, M. W. Dawande, M. Trumbo, H. S. Lee.
// "Inventory Matching Problems in the Steel Industry," IBM Research
// Report RC 21171, 1998.



using CP;

int nbOrders   = ...;
int nbSlabs = ...;
int nbColors   = ...;
int nbCap      = ...;
int capacities[1..nbCap] = ...;
int weight[1..nbOrders] = ...;
int colors[1..nbOrders] = ...;

int maxLoad = sum(i in 1..nbOrders) weight[i];
int maxCap  = max(i in 1..nbCap) capacities[i];

int loss[c in 0..maxCap] = min(i in 1..nbCap : capacities[i] >= c) capacities[i] - c; 
dvar int where[1..nbOrders] in 1..nbSlabs;
dvar int load[1..nbSlabs] in 0..maxLoad;

execute{
   writeln("loss = ", loss);   
   writeln("maxLoad = ", maxLoad);   
   writeln("maxCap = ", maxCap);

}
execute {
		cp.param.LogPeriod = 50;
}
execute {
   var f = cp.factory;
   cp.setSearchPhases(f.searchPhase(where));
}
dexpr int totalLoss = sum(s in 1..nbSlabs) loss[load[s]];

minimize totalLoss;
subject to {  
  packCt: pack(load, where, weight);
  forall(s in 1..nbSlabs)
    colorCt: sum (c in 1..nbColors) (or(o in 1..nbOrders : colors[o] == c) (where[o] == s)) <= 2; 
}
