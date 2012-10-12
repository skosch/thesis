/*
* Licensed Materials - Property of IBM
* 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 
* Copyright IBM Corporation 1998, 2011. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/ 

package externaldataread;
import ilog.opl.*;

// This is a very simple text reader used to demonstrate how
// OPL can use an externally defined data source.
// See documentation on externaldataread project

 public class ExternalDataRead extends IloCustomOplDataSource
{
    public ExternalDataRead(IloOplModel oplModel)
    {
        super(IloOplFactory.getOplFactoryFrom(oplModel));
    }
    public void customRead()
    {
        IloOplDataHandler handler = getDataHandler();
        handler.startElement("a");
        handler.addIntItem(1);
        handler.endElement();
    }
}
