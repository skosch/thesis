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

import ilog.concert.IloSymbolSet;
import ilog.opl.IloOplElement;
import ilog.opl.IloOplModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;


// This is a very simple text reader used to demonstrate how
// OPL can run Java functions from a .dat file during data initialization.
// See documentation on externaldataread project

public class SimpleTextReader
{
    private BufferedReader br;
    private String token = " ";

    public SimpleTextReader(IloOplModel opl, String fileName, String token) throws IOException
    {
        opl.getSettings().getConsoleOutput().println("SimpleTextReader : " + fileName);
        this.br = new BufferedReader(new FileReader(fileName));
        this.token = token;
    }

    public void fillOplElement(IloOplModel opl, IloOplElement element) throws IOException
    {
        opl.getSettings().getConsoleOutput().println("Filling element : " + element.getName());
        IloSymbolSet set = element.asSymbolSet();
        String[] values = readNext();
        for (int i=0; i<values.length; i++)
            set.add(values[i]);
    }
    public String[] readNext() throws IOException
    {
        return parseLine(br.readLine());
    }

    private String[] parseLine(String nextLine) throws IOException
    {

        if (nextLine == null)
        {
            return null;
        }

        ArrayList<String> tokensOnThisLine = new ArrayList<String>();
        StringTokenizer tokens=new StringTokenizer(nextLine, token);
        while(tokens.hasMoreTokens())
        {
            tokensOnThisLine.add((tokens.nextToken()));
        }

        return tokensOnThisLine.toArray(new String[0]);

    }

    public void close() throws IOException
    {
        br.close();
    }

}
