main {

   IloOplImportJava("./classes");
  
   // Create a new model using this model definition and cplex.
   var src=new IloOplModelSource("externaldataread.mod");
   var def=new IloOplModelDefinition(src);
   var opl = new IloOplModel(def);
   opl.addDataSource(new IloOplDataSource("externaldataread.dat"));
  
   // Create the custom data source.
   var customDataSource = IloOplCallJava("externaldataread.ExternalDataRead",
      "<init>", "(Lilog.opl.IloOplModel;)V", opl);
        
   // Pass it to the model (notice that you can do this from a script because the custom data source
   // was converted to a script data source upon return of the Java call).
   opl.addDataSource(customDataSource);
   opl.generate();
  
   // Some output to show that data has been correctly initialized.
   writeln(opl.a);
   writeln(opl.strings);

   // Check that everything went fine and the results are correct.
   var Ok=1;
   if (opl.a!=1) Ok=0;
   var n=0;
   for(var s in opl.strings)
   {
      n++;
      if ((n==1) && (s!="val1")) Ok=0;
      if ((n==2) && (s!="val2")) Ok=0;
      if ((n==3) && (s!="val3")) Ok=0;
   }
   if (n!=3) Ok=0;
   if (Ok==0) -1;
}
