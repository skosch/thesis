
// This is not a standalone example.
// This model contains definition of a new script class and 
// its method to call an external knapsack algorithm 
// implemented in Java.

execute {
/**
 * Knapsack solver
 */
function Knapsack() {
  IloOplImportJava("../../java/javaknapsack/classes");
 
  this.object = IloOplCallJava("javaknapsack.Knapsack","<init>","");

  this.updateInputs = __Knapsack_updateInputs;
  this.solve = __Knapsack_solve;
};
function __Knapsack_updateInputs(weights,values) {
   this.object.updateInputs(weights,values);
  // The call above is a shortcut as there is no risk of ambiguity.
  // In the general case, if several methods have the same name, you can use:
  //IloOplCallJava(this.object,"updateInputs", "(Lilog.opl.IloOplElement;Lilog.opl.IloOplElement;)V", weights, values);   
};
function __Knapsack_solve(solution, size) {
   return this.object.solve(solution, size);
  // The call above is a shortcut as there is no risk of ambiguity.
  // In the general case, if several methods have the same name, you can use:
  //return IloOplCallJava(this.object,"solve", "(Lilog.opl.IloOplElement;I)D", solution, size);   
};
}