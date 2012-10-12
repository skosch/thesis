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


/****************************************************************** 
 OPL Model for Multi-Product Deployment Example
 
This model is described in the documentation. 
See "Samples" in the Documentation home page.


 ******************************************************************/

// The network configuration
{string} Sites = ...;
{string} Articles = ...;

tuple link {
   string org;
   string dst;
}

{link} Links = ...;

// Standard unit cost of transporting goods using a link 
// for the amount of flows within the link capacity. 
float Cost[Links] = ...;

// Unit cost of flows in excess of link capacities 
float LinkExtraCost[Links] = ...;  

// Storage capacity at a site (for all the articles cumulated)
float SiteCapacity[Sites] = ...;

// Demand for each article at each node at the begining of the period.
float Demand[Sites][Articles] = ...;

// Total transportation capacity on a link (for all the articles cumulated)
float LinkCapacity[Links] = ...;

// Stocks on hand at a node at the begining of the period
float OnHand[Sites][Articles] = ...;

// Extra storage capacity required at a node
dvar float+ SiteExtra[Sites];

// Unit cost of renting external storage at the sites
float SiteExtraCost[Sites] = ...;
  
// Extra transportation capacity required on the links 
dvar float+ LinkExtra[Links];

// Flow for each article on each link 
dvar float+ Flow[Links][Articles];

// Total transportation costs and extra storage costs
dexpr float TotalLinkCost = 
  sum(l in Links, a in Articles) 
    (Cost[l] * Flow[l][a] + LinkExtraCost[l] * LinkExtra[l]);

dexpr float TotalSiteCost =
  sum(i in Sites) SiteExtraCost[i]*SiteExtra[i];

  // Standard cost of flows  + Extra cost paid for flows over capacity + Cost of extra storage capacity  
minimize TotalLinkCost + TotalSiteCost;
   
subject to {

  // The transportation capacity constraint. It computes how much extra
  // transportation capacity is required   
  forall(l in Links) 
    ctLinkCapa: sum(a in Articles) Flow[l][a] <= LinkCapacity[l] + LinkExtra[l];

  // The storage capacity constraint. It computes how much extra storage
  // capacity is required
  forall(s in Sites) 
    ctSiteCapa: sum(a in Articles)   (  sum(l in Links: s == l.dst) Flow[l][a] -
                        sum(l in Links: s == l.org) Flow[l][a] +
                        OnHand[s][a] -
                        Demand[s][a] ) <= SiteCapacity[s] + SiteExtra[s];


  // At each node, the incomming quantities and the stock should cover the
  // demand plus the outgoing quantities
  forall(s in Sites, a in Articles)
    ctDemand: Demand[s][a] <= OnHand[s][a] + sum(l in Links: s == l.dst) Flow[l][a] -
                                   sum(l in Links: s == l.org) Flow[l][a];           
                                   
}

execute DISPLAY {
   writeln("LinkExtra = ", LinkExtra);
   writeln("SiteExtra = ", SiteExtra);
   writeln("Flow = ", Flow);
}
