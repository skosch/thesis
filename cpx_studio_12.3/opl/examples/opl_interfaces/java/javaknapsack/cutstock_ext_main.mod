// This example illustrates how to use an external function
// implemented in Java. It works only if you compile Knapsack.java 
// in examples\java\javaknapsack\src\javaknapsack first. 
include "javaknapsack.mod";
int RollWidth = ...;
int NbItems = ...;

range Items = 1..NbItems;
int Size[Items] = ...;
int Amount[Items] = ...;


tuple  pattern {
   key int id;
   int cost;
   int fill[Items];
}


{pattern} Patterns = ...;


// for knapsack
float Duals[Items] = ...;
int NewPattern[i in Items] = 0;

dvar float Cut[Patterns] in 0..1000000;
     
minimize
  sum( p in Patterns ) 
    p.cost * Cut[p];

subject to {
  forall( i in Items ) 
    ctFill:
      sum( p in Patterns ) 
        p.fill[i] * Cut[p] >= Amount[i];
}
tuple r {
   pattern p;
   float cut;
};

{r} Result = {<p,Cut[p]> | p in Patterns : Cut[p] > 1e-3};

execute DISPLAY_RESULT {
   writeln(Result);
}
main {
   var masterOpl = thisOplModel;
   masterOpl.generate();

  var RC_EPS = 1.0e-6;
  
  var masterDef = masterOpl.modelDefinition;
  var masterCplex = cplex;
  var masterData = masterOpl.dataElements; 
   // Create a subproblem instance:
   var knapsack = new Knapsack();
   var best;
   var curr = Infinity;

    while ( best != curr ) {
      best = curr;
      writeln("Solve master.");
        if ( masterCplex.solve() ) {
        curr = masterCplex.getObjValue();
          writeln();
          writeln("MASTER OBJECTIVE: ",curr);
       } else {
          writeln("No solution!");
        break;
       }
        

      for(var i in masterOpl.Items) {
         masterOpl.Duals[i] = masterOpl.ctFill[i].dual;
      }

      
      writeln("Solve sub.");
      knapsack.updateInputs(masterOpl.Size, masterOpl.Duals);
      var solutionValue = knapsack.solve(masterOpl.NewPattern, masterOpl.RollWidth);
        if ( solutionValue > 1+RC_EPS) {
          writeln();
          writeln("SUB OBJECTIVE: ",solutionValue);
        } else {
          writeln("No solution!");
        break;
        }

      // Prepare the next iteration:
      masterData = masterOpl.dataElements;
      masterData.Patterns.add(masterData.Patterns.size,1,masterOpl.NewPattern);

      if ( masterOpl!=thisOplModel ) {
         masterOpl.end();
      }
      masterOpl = new IloOplModel(masterDef,masterCplex);
      masterOpl.addDataSource(masterData);
      masterOpl.generate();
   }
   masterOpl.postProcess();
   if ( masterOpl!=thisOplModel ) {
      masterOpl.end();
   }
 
   
   0;
}
