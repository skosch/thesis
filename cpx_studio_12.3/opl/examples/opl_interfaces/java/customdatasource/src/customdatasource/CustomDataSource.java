/*
* Licensed Materials - Property of IBM
* 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
* Copyright IBM Corporation 1998, 2011. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/

//-------------------------------------------------------------- -*- Java -*-
//Java version of customdatasource.cpp of OPL distrib
//--------------------------------------------------------------------------
package customdatasource;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.opl.*;

public class CustomDataSource
{
  static final String DATADIR = ".";

  static class MyCustomDataSource extends IloCustomOplDataSource
  {
    MyCustomDataSource(IloOplFactory oplF)
    {
      super(oplF);
    }

    public void customRead()
    {
      IloOplDataHandler handler = getDataHandler();
      // initialize the int 'simpleInt'
      handler.startElement("anInt");
      handler.addIntItem(3);
      handler.endElement();

      // initialize the int array 'simpleIntArray'
      handler.startElement("anIntArray");
      handler.startArray();
      handler.addIntItem(1);
      handler.addIntItem(2);
      handler.addIntItem(3);
      handler.endArray();
      handler.endElement();

      // initialize int array indexed by float 'anArrayIndexedByFloat'
      handler.startElement("anArrayIndexedByFloat");
      handler.startIndexedArray();
      handler.setItemNumIndex(2.0);
      handler.addIntItem(1);
      handler.setItemNumIndex(2.5);
      handler.addIntItem(2);
      handler.setItemNumIndex(1.0);
      handler.addIntItem(3);
      handler.setItemNumIndex(1.5);
      handler.addIntItem(4);
      handler.endIndexedArray();
      handler.endElement();

      // initialize int array indexed by string 'anArrayIndexedByString'
      handler.startElement("anArrayIndexedByString");
      handler.startIndexedArray();
      handler.setItemStringIndex("idx1");
      handler.addIntItem(1);
      handler.setItemStringIndex("idx2");
      handler.addIntItem(2);
      handler.endIndexedArray();
      handler.endElement();

      // initialize a tuple in the order the components are declared
      handler.startElement("aTuple");
      handler.startTuple();
      handler.addIntItem(1);
      handler.addNumItem(2.3);
      handler.addStringItem("not named tuple");
      handler.endTuple();
      handler.endElement();

      // initialize a tuple using tuple component names.
      handler.startElement("aNamedTuple");
      handler.startNamedTuple();
      handler.setItemName("f");
      handler.addNumItem(3.45);
      handler.setItemName("s");
      handler.addStringItem("named tuple");
      handler.setItemName("i");
      handler.addIntItem(99);
      handler.endNamedTuple();
      handler.endElement();

      // initialize the tuple set 'simpleTupleSet'
      handler.startElement("aTupleSet");
      handler.startSet();
      // first tuple
      handler.startTuple();
      handler.addIntItem(1);
      handler.addNumItem(2.5);
      handler.addStringItem("a");
      handler.endTuple();
      // second element
      handler.startTuple();
      handler.addIntItem(3);
      handler.addNumItem(4.1);
      handler.addStringItem("b");
      handler.endTuple();
      handler.endSet();
      handler.endElement();

      // initialize element 3 and 2 of the tuple array 'simpleTupleArray' in that particular order
      handler.startElement("aTupleArray");
      handler.startIndexedArray();
      // initialize 3rd cell
      handler.setItemIntIndex(3);
      handler.startTuple();
      handler.addIntItem(1);
      handler.addNumItem(2.5);
      handler.addStringItem("a");
      handler.endTuple();
      // initialize second cell
      handler.setItemIntIndex(2);
      handler.startTuple();
      handler.addIntItem(3);
      handler.addNumItem(4.1);
      handler.addStringItem("b");
      handler.endTuple();
      handler.endIndexedArray();
      handler.endElement();

      // initialize int array indexed by tuple set 'anArrayIndexedByTuple'
      handler.startElement("anArrayIndexedByTuple");
      handler.startIndexedArray();
      handler.startItemTupleIndex();
      handler.addIntItem(3);
      handler.addNumItem(4.1);
      handler.addStringItem("b");
      handler.endItemTupleIndex();
      handler.addIntItem(1);
      handler.endIndexedArray();
      handler.endElement();

      // initialize a 2-dimension int array 'a2DIntArray'
      handler.startElement("a2DIntArray");
      handler.startArray();
      for (int i=1;i<=2;i++) {
    handler.startArray();
    for (int j=1;j<=3;j++)
      handler.addIntItem(i * 10 + j);
    handler.endArray();
      }
      handler.endArray();
      handler.endElement();
    }
  };

  static public void main(String[] args) throws Exception
  {
    int status = 127;
    try {
        IloOplFactory.setDebugMode(true);
        IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
        IloOplModelSource modelSource = oplF.createOplModelSource(DATADIR + "/customDataSource.mod");
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
        IloCplex cplex = oplF.createCplex();
        IloOplModel opl=oplF.createOplModel(def,cplex);

        IloOplDataSource dataSource=new MyCustomDataSource(oplF);
        opl.addDataSource(dataSource);
        opl.generate();
        oplF.end();
        status = 0;
    } catch (IloOplException ex) {
        System.err.println("### OPL exception: " + ex.getMessage());
        ex.printStackTrace();
        status = 2;
    } catch (IloException ex) {
        System.err.println("### CONCERT exception: " + ex.getMessage());
        ex.printStackTrace();
        status = 3;
    } catch (Exception ex) {
        System.err.println("### UNEXPECTED UNKNOWN ERROR ...");
        ex.printStackTrace();
        status = 4;
    }
    System.exit(status);

  }
}
