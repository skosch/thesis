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

// Problem 3 from Model Building in Mathematical Programming, 3rd ed.
//   by HP Williams
// Factory Planning 
// This model is described in the documentation. 
//See "Samples" in the Documentation home page.

{string} Prod = ...;
{string} Process = ...;

int NbMonths = ...;
range Months = 1..NbMonths;

float ProfitProd[Prod] = ...;
float ProcessProd[Process][Prod] = ...;
float MarketProd[Months][Prod] = ...;
float HoursMonth = ...;
float NumProcess[Process][Months] = ...;

float CostHold = ...;
float StartHold = ...;
float EndHold = ...;
float MaxHold = ...;

dvar float+ Make[Prod][Months];
dvar float+ Hold[Prod][0..NbMonths] in 0..MaxHold;
dvar float+ Sell[j in Prod][m in Months] in 0..MarketProd[m,j];

dexpr float Profit = 
  sum (j in Prod, m in Months) ProfitProd[j] * Sell[j][m];
dexpr float Cost = 
  sum (j in Prod, m in Months) CostHold * Hold[j][m];
  
maximize Profit - Cost;
        
subject to {
  // Limits on process capacity 
  forall(m in Months, i in Process)
    ctCapacity: sum(j in Prod) ProcessProd[i][j] * Make[j][m]
           <= NumProcess[i][m] * HoursMonth;

  // Inventory balance
  forall(j in Prod, m in Months)
    ctInvBal: Hold[j][m-1] + Make[j][m] == Sell[j][m] + Hold[j][m];

  // Starting and ending inventories are fixed
  forall(j in Prod) {
    ctStartInv: Hold[j][0] == StartHold;    
    ctEndInv: Hold[j][NbMonths] == EndHold;
  }
}

// display dual values of processes at capacity
execute DISPLAY {
   //plan[m][j] describes how much to make, sell, and hold of each product j in each month m
   for(var m in Months)
      for(var j in Prod)
         writeln("plan[",m,"][",j,"] = <Make:",Make[j][m],", Sell:",Sell[j][m],", Hold:",Hold[j][m],">");
}
