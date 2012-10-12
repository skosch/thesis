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

/******
 * This model is described in the documentation. 
 * See "Samples" in the Documentation home page.
 *
 */


int NumNodes = ...;   // Number of nodes
range Nodes = 1..NumNodes;

// Get the supply (positive) and demand (negative)
// at each node

int SupDem[Nodes] = ...;

// Create a record to hold information about each arc
tuple arc {
   key int fromnode;
   key int tonode;
   float cost;
   float ub;
}

// Get the set of arcs

{arc} Arcs = ...;

// The network flow model has decision variables indexed on 
// the arcs.

dvar float+ Flow[a in Arcs] in 0 .. a.ub;

dexpr float TotalFlow = sum (a in Arcs) a.cost * Flow[a];
minimize TotalFlow;
subject to {
   // Preserve flows at each node.  Note the use of slicing
   forall (i in Nodes)
     ctNodeFlow:
      sum (<i,j,c,ub> in Arcs) Flow[<i,j,c,ub>]
    - sum (<j,i,c,ub> in Arcs) Flow[<j,i,c,ub>] == SupDem[i];
}


execute DISPLAY {
   writeln("\n<from node,to node,Flow[a]>\n");
   for(var a in Arcs)
      if(Flow[a] > 0)
         writeln("<",a.fromnode,",",a.tonode,",",Flow[a],">");
}
