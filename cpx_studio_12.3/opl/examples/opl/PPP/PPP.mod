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


//
// For a description of the problem and resolution methods:
//
//    The Progressive Party Problem: Integer Linear Programming
//    and Constraint Programming Compared
//
//    Proceedings of the First International Conference on Principles
//    and Practice of Constraint Programming table of contents
//
//    Lecture Notes In Computer Science; Vol. 976, pages 36-52, 1995
//    ISBN:3-540-60299-2
//

// This model is greater than the size allowed in trial mode. 
// You therefore need a commercial edition of CPLEX Studio to run this example.
// If you are a student or teacher, you can also get a full version through
// the IBM Academic Initiative.

using CP;

int numBoats = ...;
range Boats = 0..numBoats - 1;
int boatSize[Boats] = ...;
int crewSize[Boats] = ...;

int numPeriods = 6;
range Periods = 0..numPeriods - 1;
int maxCapacity = max(b in Boats) boatSize[b];

dvar boolean host[Boats];
dvar int numHosts in numPeriods..numBoats;
dvar int goWhere[Periods,Boats] in Boats;
dvar int load[Periods,Boats] in 0..maxCapacity;
dvar int meets[Boats,Boats] in Periods;

execute {
   		cp.setSearchPhases(cp.factory.searchPhase(goWhere));
}

minimize numHosts;
subject to {
   // Number of hosts
   numHosts == sum (b in Boats) host[b];
   
   // Capacity of hosts, non-hosts have zero capacity
   forall (b in Boats, p in Periods)
     load[p,b] <= host[b] * boatSize[b];
   
   // Capacities respected
   forall (p in Periods)
     pack(all (b in Boats) load[p,b], all (b in Boats) goWhere[p,b], crewSize, numHosts);   

   // Hosts are always in their boat, guests are never in their boat
   forall (b in Boats)
     count(all(p in Periods) goWhere[p,b], b) == host[b] * numPeriods;
     
   // No two crews ever meet
   forall (p in Periods, b1, b2 in Boats : b1 < b2)
      (goWhere[p,b1] == goWhere[p,b2]) => (meets[b1,b2] == p);
   
   // Asserted hosts and guests (in spec)
   host[0] == true;
   host[1] == true;
   host[2] == true;
   host[39] == false;
   host[40] == false;
   host[41] == false;
}
