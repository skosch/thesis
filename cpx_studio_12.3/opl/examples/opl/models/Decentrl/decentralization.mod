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

// Problem 10 from Model Building in Mathematical Programming, 3rd ed.
//   by HP Williams
//
//This model is described in the documentation. 
//See "Samples" in the Documentation home page.

{string} Cities = ...;
{string} Depts = ...;

float Benefit[Depts][Cities] = ...;
float CostComm[Cities][Cities] = ...;
float Comm[Depts][Depts] = ...;
int   LimDeptsLoc = ...;

dvar int IsIn[Depts][Cities] in 0..1;
dvar int Link[Depts][Cities][Depts][Cities] in 0..1; 

dexpr float Cost =
  sum(ordered d1, d2 in Depts, c1, c2 in Cities)
        CostComm[c1][c2] * Comm[d1][d2] * Link[d1][c1][d2][c2]; 
dexpr float Benef =
  sum(d in Depts, c in Cities) Benefit[d][c] * IsIn[d][c];
  
minimize Cost - Benef;

subject to {
   // Each department must be in one city
   forall(d in Depts) 
      sum(c in Cities) IsIn[d][c] == 1;
   
   // No city can be the location of more than the specified
   // number of departments
   forall(c in Cities)
      sum(d in Depts) IsIn[d][c] <= LimDeptsLoc;

   // Link the linearized quadratic terms to the binaries
   forall(ordered d1,d2 in Depts, c1, c2 in Cities) {
      Link[d1][c1][d2][c2] - IsIn[d1][c1] <= 0;
      Link[d1][c1][d2][c2] - IsIn[d2][c2] <= 0;
      IsIn[d1][c1] + IsIn[d2][c2] - Link[d1][c1][d2][c2] <= 1;
   }
};
