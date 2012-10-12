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

{string} Products = ...;

int NbMonths   = ...;
range Months = 1..NbMonths;
float Cost[Months][Products] = ...;

dvar float+ Produce[Months];
dvar float+ Use[Months][Products];
dvar float+ Buy[Months][Products];
dvar float Store[Months][Products] in 0..1000;


maximize 
  sum( m in Months ) 
    (150 * Produce[m] 
    - sum( p in Products ) 
      Cost[m][p] * Buy[m][p] 
    - 5 * sum( p in Products ) 
      Store[m][p]);
subject to {
  forall( p in Products )
    ctStore:
      Store[NbMonths][p] == 500;
  forall( m in Months ) {
    ctUse1:
      Use[m]["v1"] + Use[m]["v2"] <= 200;
    ctUse2:                
      Use[m]["o1"] + Use[m]["o2"] + Use[m]["o3"] <= 250;
    ctUse3:
      3 * Produce[m] <=
            8.8 * Use[m]["v1"] + 6.1 * Use[m]["v2"] +
            2   * Use[m]["o1"] + 4.2 * Use[m]["o2"] + 5 * Use[m]["o3"];
     ctUse4:
       8.8 * Use[m]["v1"] + 6.1 * Use[m]["v2"] +
            2   * Use[m]["o1"] + 4.2 * Use[m]["o2"] + 5 * Use[m]["o3"]
            <= 6 * Produce[m];
            
     ctUse5:
       Produce[m] == sum( p in Products ) Use[m][p];
   }
  forall( m in Months )
    forall( p in Products ) {
      ctUse6:  
        if (m == 1) {
          500 + Buy[m][p] == Use[m][p] + Store[m][p];
        }
        else {
          Store[m-1][p] + Buy[m][p] == Use[m][p] + Store[m][p];
        }
    }
    
};

// Expected result : 107843

execute DISPLAY {   
  writeln(" Maximum profit = " , cplex.getObjValue());
  for (var i in Months) {
    writeln(" Month ", i, " ");
    write("  . Buy   ");
    for (var p in Products)
      write(Buy[i][p], "\t ");
    writeln();            
    write("  . Use   ");
    for (p in Products) 
      write(Use[i][p], "\t ");
    writeln();
    write("  . store ");
    for (p in Products) 
      write(Store[i][p], "\t ");
    writeln();
  }
}
 
