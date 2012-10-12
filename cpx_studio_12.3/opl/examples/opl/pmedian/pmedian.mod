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

//Data
int P = ...;
{string} Customers = ...;
{string} Warehouses = ...;
int Demand[Customers] = ...;
float Distance[Customers][Warehouses] = ...;

//Variables
dvar boolean OpenWarehouse[Warehouses];
dvar boolean ShipToCustomer[Customers][Warehouses];

//Objective
minimize 
  sum( c in Customers , w in Warehouses ) 
    Demand[c]*Distance[c][w]*ShipToCustomer[c][w];

//Constraints
subject to {
  forall( c in Customers )
    ctShip:
      sum( w in Warehouses ) 
        ShipToCustomer[c][w] == 1;

  ctOpen:
    sum( w in Warehouses ) 
      OpenWarehouse[w] == P;

  forall( c in Customers , w in Warehouses )
    ctShipOpen:
      ShipToCustomer[c][w] <= OpenWarehouse[w];
}




