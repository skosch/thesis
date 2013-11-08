/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples.multicostregular.asap.parser;

import samples.multicostregular.asap.data.*;
import samples.multicostregular.asap.data.base.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Dec 16, 2008
 * Time: 6:00:43 PM
 */
public class ASAPParser {

    public static boolean LET_INFEASABILITY = true;

    String id;


    ASAPItemHandler handler;


    public ASAPParser(String filename)
    {
        this.handler = new ASAPItemHandler();

        this.parseXMLFile(filename);
    }


    public ASAPItemHandler getHandler() {
        return handler;
    }

    public void parseXMLFile(String filename)  {
        String[] tmp  = filename.split("/");
        this.handler.setProblemName(tmp[tmp.length-1]);

        DOMParser parser = new DOMParser();
        try {
            parser.parse(filename);
        } catch (SAXException e) {
            System.err.println("ERROR PARSING XML FILE");

        } catch (IOException e) {
            System.err.println("ERROR READING XML FILE");

        }

        Document d = parser.getDocument();
        parseXMLNode(d.getDocumentElement());



    }


    private void parseXMLNode(Node n)
    {

        String name = n.getNodeName();
        short type = n.getNodeType();
        if (type == Node.ELEMENT_NODE) {

            if (name.equals("SchedulingPeriod"))
            {
                this.id = n.getAttributes().item(0).getNodeValue();
                expand(n);
            }
            else if (name.equals("StartDate"))
                this.handler.setStart(ASAPDate.parseDate(n.getFirstChild().getNodeValue()));
            else if (name.equals("EndDate"))
                this.handler.setEnd(ASAPDate.parseDate(n.getFirstChild().getNodeValue()));
            else if (name.equals("Skill"))
            {
                parseSkill(n);
            }
            else if (name.equals("Shift"))
            {
                parseShift(n);
            }
            else if (name.equals("ShiftGroup"))
            {
                parseShiftGroup(n);
            }
            else if (name.equals("Contract"))
            {
                parseContract(n);

            }
            else if (name.equals("Employee"))
            {
                String a = getAttr(n);
                ASAPEmployee e = handler.makeEmployee(a);
                NodeList l = n.getChildNodes();
                for (int i = 0 ; i < l.getLength() ; i++)
                {
                    Node o = l.item(i);
                    if (o.getNodeName().equals("Name")) e.setName(o.getFirstChild().getNodeValue());
                    else if (o.getNodeName().equals("ContractID")) e.setContract(handler.getContract(o.getFirstChild().getNodeValue()));
                    else if (o.getNodeName().equals("Skills"))
                    {
                        NodeList l2 = o.getChildNodes();
                        for (int j = 0 ; j < l2.getLength() ; j++)
                        {
                            Node o2 = l2.item(j);
                            if (o2.getNodeName().equals("Skill"))
                                e.addSkill(o2.getFirstChild().getNodeValue());
                        }
                    }
                }
            }
            else if (name.equals("DayOfWeekCover"))
            {
                NodeList l = n.getChildNodes();
                ASAPCover d = handler.makeCover();
                for (int i =0 ; i < l.getLength() ; i++)
                {
                    Node o = l.item(i);

                    if (o.getNodeName().equals("Day")) d.setDay(o.getFirstChild().getNodeValue());
                    else if (o.getNodeName().equals("Cover"))
                    {
                        NodeList l2 = o.getChildNodes();
                        String shi = null;
                        String ski = null;
                        Integer pre = null;
                        Integer min = null;
                        Integer max = null;
                        for (int j = 0 ; j < l2.getLength() ; j++)
                        {
                            Node o2 = l2.item(j);
                            if (o2.getNodeName().equals("Shift")) shi = (o2.getFirstChild().getNodeValue());
                            else if (o2.getNodeName().equals("Skill")) ski = (o2.getFirstChild().getNodeValue());
                            else if (o2.getNodeName().equals("Preferred")) pre = Integer.parseInt(o2.getFirstChild().getNodeValue());
                            else if (o2.getNodeName().equals("Min")) min = Integer.parseInt(o2.getFirstChild().getNodeValue());
                            else if (o2.getNodeName().equals("Max")) max = Integer.parseInt(o2.getFirstChild().getNodeValue());

                        }
                        d.push(handler.getSkill(ski), handler.getShift(shi),pre,min,max);
                    }
                }

            }
            else if (name.equals("DateSpecificCover"))
            {
	            NodeList l = n.getChildNodes();
	            ASAPCover d = handler.makeCover();
	            for (int i =0 ; i < l.getLength() ; i++)
	            {
		            Node o = l.item(i);

		            if (o.getNodeName().equals("Date")) {
			            d.setDay(o.getFirstChild().getNodeValue());
		            }
		            else if (o.getNodeName().equals("Cover"))
		            {
			            NodeList l2 = o.getChildNodes();
			            String shi = null;
			            String ski = null;
			            Integer pre = null;
			            Integer min = null;
			            Integer max = null;
			            for (int j = 0 ; j < l2.getLength() ; j++)
			            {
				            Node o2 = l2.item(j);
				            if (o2.getNodeName().equals("Shift")) shi = (o2.getFirstChild().getNodeValue());
				            else if (o2.getNodeName().equals("Skill")) ski = (o2.getFirstChild().getNodeValue());
				            else if (o2.getNodeName().equals("Preferred")) pre = Integer.parseInt(o2.getFirstChild().getNodeValue());
				            else if (o2.getNodeName().equals("Min")) min = Integer.parseInt(o2.getFirstChild().getNodeValue());
				            else if (o2.getNodeName().equals("Max")) max = Integer.parseInt(o2.getFirstChild().getNodeValue());

			            }
			            d.push(handler.getSkill(ski), handler.getShift(shi),pre,min,max);
		            }
	            }

            }
            else if (name.equals("MasterWeights"))
            {
                NodeList l = n.getChildNodes();
                for (int i = 0 ; i < l.getLength() ; i++)
                {
                    Node o = l.item(i);
                    if (o.getNodeName().equals("PrefOverStaffing")) this.handler.masterWeights.setPrefOverStaffing(Integer.parseInt(o.getFirstChild().getNodeValue()));
                    else if (o.getNodeName().equals("PrefUnderStaffing")) this.handler.masterWeights.setPrefUnderStaffing(Integer.parseInt(o.getFirstChild().getNodeValue()));
                    else if (o.getNodeName().equals("MaxOverStaffing")) this.handler.masterWeights.setMaxOverStaffing(Integer.parseInt(o.getFirstChild().getNodeValue()));
                    else if (o.getNodeName().equals("MinUnderStaffing")) this.handler.masterWeights.setMinUnderStaffing(Integer.parseInt(o.getFirstChild().getNodeValue()));
                    else if (o.getNodeName().equals("MaxShiftsPerDay")) this.handler.masterWeights.setMaxShiftsPerDay(Integer.parseInt(o.getFirstChild().getNodeValue()));

                }


            }
            else if (name.equals("ShiftOn") || name.equals("ShiftOff"))
            {
                String a = getAttr(n);
                ASAPShiftOn s = handler.makeShiftOn(Integer.parseInt(a));
                NodeList l = n.getChildNodes();
                for (int i = 0 ; i < l.getLength() ; i++)
                {
                    Node o = l.item(i);
                    if (o.getNodeName().equals("ShiftTypeID")) { s.setShift(o.getFirstChild().getNodeValue()); if (name.equals("ShiftOff")) s.setOff(); }
                    else if (o.getNodeName().equals("EmployeeID")) s.setEmployee(o.getFirstChild().getNodeValue());
                    else if (o.getNodeName().equals("Date")) s.setDate(ASAPDate.parseDate(o.getFirstChild().getNodeValue()));
                }

            }
            else if (name.equals("DayOff"))
            {
	            int weight = Integer.parseInt(n.getAttributes().getNamedItem("weight").getNodeValue());
	            if (n.getAttributes().getNamedItem("working") == null || n.getAttributes().getNamedItem("working").getNodeValue().equals("false")) {
		            ASAPShiftOn s = handler.makeShiftOn(weight);
		            NodeList l = n.getChildNodes();
		            for (int i = 0 ; i < l.getLength() ; i++)
		            {
			            Node o = l.item(i);
			            s.setShift(null); s.setOff();
			            if (o.getNodeName().equals("EmployeeID")) s.setEmployee(o.getFirstChild().getNodeValue());
			            else if (o.getNodeName().equals("Date")) s.setDate(ASAPDate.parseDate(o.getFirstChild().getNodeValue()));
		            }
	            } else
		            System.out.println("not parsed XML tag DayOff with 'working=true' attribute");

            }
            else if (name.equals("Skills")||name.equals("SkillGroups")||name.equals("ShiftTypes")||name.equals("ShiftGroups")||name.equals("Contracts")||name.equals("Employees")
	            ||name.equals("CoverRequirements")||name.equals("DayOffRequests")||name.equals("ShiftOffRequests")||name.equals("ShiftOnRequests")) {
	        expand(n);
            }
            else if (name.equals("SkillGroup")) {
	            System.out.println("through XML tag " + name);
            }
            else if (name.equals("SchedulingHistory")) {
	            System.out.println("not parsed XML tag " + name);
            }
            else {
	            System.out.println("unknown XML tag " + name);
            }
        }

    }

    private void parseContract(Node n) {
        int days = ASAPDate.getDaysBetween(handler.getStart(),handler.getEnd())+1;
        int weeks = days/7;

        if (handler.map.get("R") == null) handler.makeRestShift();
        String a = getAttr(n);
        ASAPContract c = handler.makeContract(a);
        NodeList l = n.getChildNodes();
        for (int i = 0 ; i < l.getLength() ; i++)
        {
            Node o = l.item(i);
            String na = o.getNodeName();
            if (na.equals("MaxShiftsPerDay"))
                c.setMaxShiftsPerDay(Integer.parseInt(o.getFirstChild().getNodeValue()));
            else if (na.equals("MaxNumAssignments"))
            {
                c.setMaxNumAssignmentPenalty(Integer.parseInt(getAttr(o)));
                c.setMaxNumAssignment(Integer.parseInt(o.getFirstChild().getNodeValue()));
            }
            else if (na.equals("MinConsecutiveFreeDays"))
            {
                parseMinConsFreeDays(o,c,days);
            }
            else if (na.equals("MinConsecutiveWorkingDays"))
            {
                parseMinConsWE(o,c,days);

            }
            else if (na.equals("MaxConsecutiveWorkingWeekends"))
            {
                c.setMaxConsecutiveWorkingWeekEndsPenalty(Integer.parseInt(getAttr(o)));
                int val = Integer.parseInt(o.getFirstChild().getNodeValue());
                c.setMaxConsecutiveWorkingWeekEnds(val);

                makePatternForConsecutiveWeekEnds(c,weeks,val);



            }
            else if (na.equals("MaxShiftTypes"))
            {
                String b = getAttr(o);
                c.setMaxShiftTypesPenalty(Integer.parseInt(b));
                NodeList l2 = o.getChildNodes();
                for (int j = 0 ; j < l2.getLength() ; j++)
                {
                    Node o2 = l2.item(j);
                    if (o2.getNodeName().equals("MaxShiftType"))
                    {
                        c.addMaxShiftType(getInSons(o2,"ShiftType"),
                                Integer.parseInt(getInSons(o2,"Value")));

                    }
                }
            }
            else if (na.equals("MaxWorkingWeekends"))
            {
                String b = getAttr(o);
                c.setMaxWorkingWeekEndsPenalty(Integer.parseInt(b));
                int val = Integer.parseInt(o.getFirstChild().getNodeValue());
                c.setMaxWorkingWeekEnds(val);
                makePatternForMaxWeekEnds(c,weeks,val);
            }
            else if (na.equals("MinWorkingWeekends"))
            {
                String b = getAttr(o);
                c.setMinWorkingWeekEndsPenalty(Integer.parseInt(b));
                c.setMinWorkingWeekEnds(Integer.parseInt(o.getFirstChild().getNodeValue()));
            }


            else if (na.equals("MinShiftTypes"))
            {
                String b = getAttr(o);
                c.setMinShiftTypesPenalty(Integer.parseInt(b));
                NodeList l2 = o.getChildNodes();
                for (int j = 0 ; j < l2.getLength() ; j++)
                {
                    Node o2 = l2.item(j);
                    if (o2.getNodeName().equals("MinShiftType"))
                    {
                        c.addMinShiftType(getInSons(o2,"ShiftType"),
                                Integer.parseInt(getInSons(o2,"Value")));

                    }
                }
            }
            else if (na.equals("MinDaysOff"))
            {
                c.setMinDaysOffPenalty(Integer.parseInt(getAttr(o)));
                c.setMinDaysOff(Integer.parseInt(o.getFirstChild().getNodeValue()));
            }
            else if (na.equals("MaxDaysOff"))
            {
                c.setMaxDaysOffPenalty(Integer.parseInt(getAttr(o)));
                c.setMaxDaysOff(Integer.parseInt(o.getFirstChild().getNodeValue()));
            }
            else if (na.equals("MaxWeekendDays"))
            {
                c.setMaxWeekEndDaysPenalty(Integer.parseInt(getAttr(o)));
                c.setMaxWeekEndDays(Integer.parseInt(o.getFirstChild().getNodeValue()));
            }
            else if (na.equals("MinWeekendDays"))
            {
                c.setMinWeekEndDaysPenalty(Integer.parseInt(getAttr(o)));
                c.setMinWeekEndDays(Integer.parseInt(o.getFirstChild().getNodeValue()));
            }
            else if (na.equals("MinShiftsPerWeek"))
            {
                c.setMinShiftsPerWeekPenalty(Integer.parseInt(getAttr(o)));
                c.setMinShiftsPerWeek(Integer.parseInt(o.getFirstChild().getNodeValue()));
            }
            else if (na.equals("MaxShiftsPerWeek"))
            {
                c.setMaxShiftsPerWeekPenalty(Integer.parseInt(getAttr(o)));
                c.setMaxShiftsPerWeek(Integer.parseInt(o.getFirstChild().getNodeValue()));
            }
            else if (na.equals("GoodPatterns"))
            {
                treatPattern(o,c,false);
            }
            else if (na.equals("BadPatterns"))
            {
                treatPattern(o,c,true);
            }

        }
    }

    private void parseMinConsWE(Node o, ASAPContract c, int days) {
        c.setMinConsecutiveWorkingDaysPenalty(Integer.parseInt(getAttr(o)));
        c.setMinConsecutiveWorkingDays(Integer.parseInt(o.getFirstChild().getNodeValue()));
        ASAPPattern pat = handler.makePattern(c.getMinConsecutiveWorkingDaysPenalty(),true);
        ASAPPattern pat2 = handler.makePattern(c.getMinConsecutiveWorkingDaysPenalty(),true);
        ASAPPattern pat3 = handler.makePattern(c.getMinConsecutiveWorkingDaysPenalty(),true);

        ASAPShiftSet allW = handler.makeShiftSet();
        ASAPShiftSet any = handler.makeShiftSet();

        ASAPShift rest = handler.getShift("R");
        for (ASAPShift s : handler.getShifts()){
            if (!s.equals(rest))
                allW.add(s);
            any.add(s);
        }
        pat.add(rest);
        for (int m = 0 ; m < c.getMinConsecutiveWorkingDays()-1 ; m++)
            pat.add(allW);
        pat.add(rest);
        c.addPattern(pat);

        pat2.setComplete(true);
        pat3.setComplete(true);



        for (int m = 0 ; m < c.getMinConsecutiveWorkingDays()-1 ; m++)
        {
            pat2.add(allW);
        }

        pat2.add(rest);

        int remain = days - c.getMinConsecutiveWorkingDays();
        for (int m = 0 ; m < remain ; m++)
        {
            pat2.add(any);
            pat3.add(any);
        }
        pat3.add(rest);
        for (int m = 0 ; m < c.getMinConsecutiveWorkingDays()-1 ; m++)
        {
            pat3.add(allW);
        }

        c.addPattern(pat2);
        c.addPattern(pat3);





    }

    private void parseMinConsFreeDays(Node o, ASAPContract c, int days)
    {

        c.setMinConsecutiveFreeDaysPenalty(Integer.parseInt(getAttr(o)));
        c.setMinConsecutiveFreeDays(Integer.parseInt(o.getFirstChild().getNodeValue()));
        ASAPPattern pat = handler.makePattern(c.getMinConsecutiveFreeDaysPenalty(),true);
        ASAPPattern pat2 = handler.makePattern(c.getMinConsecutiveFreeDaysPenalty(),true);
        ASAPPattern pat3 = handler.makePattern(c.getMinConsecutiveFreeDaysPenalty(),true);

        ASAPShiftSet allW = handler.makeShiftSet();
        ASAPShiftSet any = handler.makeShiftSet();
        ASAPShift rest = handler.getShift("R");
        for (ASAPShift s : handler.getShifts()){
            if (!s.equals(rest))
                allW.add(s);
            any.add(s);
        }
        pat.add(allW);
        for (int m = 0 ; m < c.getMinConsecutiveFreeDays()-1 ; m++)
            pat.add(rest);
        pat.add(allW);

        if (LET_INFEASABILITY)
            c.addPattern(pat);

        pat2.setComplete(true);
        pat3.setComplete(true);



        for (int m = 0 ; m < c.getMinConsecutiveFreeDays()-1 ; m++)
        {
            pat2.add(rest);
        }

        pat2.add(allW);

        int remain = days - c.getMinConsecutiveFreeDays();
        for (int m = 0 ; m < remain ; m++)
        {
            pat2.add(any);
            pat3.add(any);
        }
        pat3.add(allW);
        for (int m = 0 ; m < c.getMinConsecutiveFreeDays()-1 ; m++)
        {
            pat3.add(rest);
        }

        c.addPattern(pat2);
        c.addPattern(pat3);


    }

    private void parseShiftGroup(Node n) {
        String a = getAttr(n);
        ASAPShiftGroup g = handler.makeShiftGroup(a);
        NodeList l = n.getChildNodes();
        for (int i = 0 ; i < l.getLength() ; i++)
        {
            if (l.item(i).getNodeName().equals("Shift"))
                g.add(handler.getShift(l.item(i).getFirstChild().getNodeValue()));

        }
    }

    private void parseShift(Node n) {
        String a = getAttr(n);
        ASAPShift tmp = handler.makeShift(a);
        NodeList l = n.getChildNodes();
        for (int i = 0 ; i < l.getLength() ; i++)
        {
            Node o = l.item(i);
            if (o.getNodeName().equals("Label")) tmp.setLabel(o.getFirstChild().getNodeValue());
            else if (o.getNodeName().equals("Colour")) tmp.setColour(o.getFirstChild().getNodeValue());
            else if (o.getNodeName().equals("Description")) tmp.setDescription(o.getFirstChild().getNodeValue());
            else if (o.getNodeName().equals("StartTime")) tmp.setStart(o.getFirstChild().getNodeValue());
            else if (o.getNodeName().equals("EndTime")) tmp.setEnd(o.getFirstChild().getNodeValue());
            else if (o.getNodeName().equals("HoursWorked")) tmp.setDuration(o.getFirstChild().getNodeValue());
        }

    }

    private void parseSkill(Node n) {
        String a = getAttr(n);
        ASAPSkill tmp = handler.makeSkill(a);
        NodeList l = n.getChildNodes();
        for (int i = 0 ; i < l.getLength() ; i++)
        {
            Node o = l.item(i);
            if (o.getNodeName().equals("Label")) tmp.setLabel(o.getFirstChild().getNodeValue());
        }
    }

    private void makePatternForMaxWeekEnds(ASAPContract c, int weeks, int val)
    {
        ASAPShiftSet set = handler.makeShiftSet();
        ASAPShiftSet all = handler.makeShiftSet();
        for (ASAPPatternElement pe : handler.getShifts())
        {
            if (!pe.equals(handler.getShift("R")))
            {
                set.add(pe);
            }
            all.add(pe);
        }

        ASAPSubPattern s1 = handler.makeSubPattern();
        s1.addPatternElement(handler.getShift("R"));
        s1.addPatternElement(set);

        ASAPSubPattern s2 = handler.makeSubPattern();
        s2.addPatternElement(set);
        s2.addPatternElement(handler.getShift("R"));

        ASAPSubPattern s3 = handler.makeSubPattern();
        s3.addPatternElement(set);
        s3.addPatternElement(set);



        ASAPShiftSet wwe = handler.makeShiftSet();
        wwe.add(s1);
        wwe.add(s2);
        wwe.add(s3);
        for (int nbW = val +1 ; nbW <= weeks ; nbW++)
        {
            int[][] tuples = getTuples(nbW,weeks);
            for (int[] a :tuples)
            {
                ASAPPattern p = handler.makePattern(1000,true);
                p.setComplete(true);
                for (int i = 0 ; i < weeks ; i++)
                {
                    for (int j = 0 ; j < 5 ; j++)
                        p.add(all);
                    if (Arrays.binarySearch(a,i) >=0)
                    {
                        p.add(wwe);
                    }
                    else
                    {
                        p.add(all);
                        p.add(all);
                    }
                }
                System.out.println(p.toRegExp());
                System.out.println("############");
                c.addPattern(p);
            }

        }



    }

    private static int[][] getTuples(int b, int n)
    {
        int [][] a = null;
        if (b == 1 && n == 1) a = new int[][]{{0}};
        else if (b ==1 && n==2) a = new int[][]{{0},{1}};
        else if (b ==2 && n==2) a = new int[][]{{0,1}};

        else if (b ==1 && n == 3) a = new int[][]{{0},{1},{2}};
        else if (b ==2 && n == 3) a = new int[][]{{0,1},{0,2},{1,2}};
        else if (b ==3 && n == 3) a = new int[][]{{0,1,2}};

        else if (b ==1 && n == 4) a = new int[][]{{0},{1},{2},{3}};
        else if (b ==2 && n == 4) a = new int[][]{{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        else if (b ==3 && n == 4) a = new int[][]{{0,1,2},{0,1,3},{0,2,3},{1,2,3}};
        else if (b ==4 && n == 4) a = new int[][]{{0,1,2,3}};

        else if (b ==1 && n == 5) a = new int[][]{{0},{1},{2},{3},{4}};
        else if (b ==2 && n == 5) a = new int[][]{{0,1},{0,2},{0,3},{0,4},{1,2},{1,3},{1,4},{2,3},{2,4},{3,4}};
        else if (b ==3 && n == 5) a = new int[][]{{0,1,2},{0,1,3},{0,1,4},{0,2,3},{0,2,4},{0,3,4},{1,2,3},{1,2,4},{1,3,4},{2,3,4}};
        else if (b ==4 && n == 5) a = new int[][]{{0,1,2,3},{0,1,2,4},{0,1,3,4},{0,2,3,4},{1,2,3,4}};
        else if (b ==5 && n == 5) a = new int[][]{{0,1,2,3,4}};

        return a;
    }

    private void makePatternForConsecutiveWeekEnds(ASAPContract c, int weeks, int val) {
        ASAPShiftSet set = handler.makeShiftSet();
        ASAPShiftSet all = handler.makeShiftSet();
        for (ASAPPatternElement pe : handler.getShifts())
        {
            if (!pe.equals(handler.getShift("R")))
            {
                set.add(pe);
            }
            all.add(pe);
        }

        ASAPSubPattern s1 = handler.makeSubPattern();
        s1.addPatternElement(handler.getShift("R"));
        s1.addPatternElement(set);

        ASAPSubPattern s2 = handler.makeSubPattern();
        s2.addPatternElement(set);
        s2.addPatternElement(handler.getShift("R"));

        ASAPSubPattern s3 = handler.makeSubPattern();
        s3.addPatternElement(set);
        s3.addPatternElement(set);



        ASAPShiftSet wwe = handler.makeShiftSet();
        wwe.add(s1);
        wwe.add(s2);
        wwe.add(s3);

        for (int nbW = val +1 ; nbW <= weeks ; nbW++)
        {
            int offset = 0;
            while (offset/7+nbW <= weeks)
            {
                ASAPPattern p = handler.makePattern(1000,true);
                p.setComplete(true);
                int d = 0;
                for (int i = 0 ; i < offset ;i++)
                {
                    p.add(all);
                    d++;
                }
                for (int i = 0 ; i < nbW ;i++)
                {
                    for (int j = 0 ; j < 5 ; j++)
                        p.add(all);
                    p.add(wwe);
                    d+=7;
                }
                for (int i =d ; i < weeks*7 ; i++)
                    p.add(all);

                c.addPattern(p);
                offset+=7;
            }

        }







    }

    private void treatPattern(Node o, ASAPContract c,  boolean b)
    {
        NodeList l2 = o.getChildNodes();
        for (int j = 0 ; j < l2.getLength() ; j++)
        {
            Node o2 = l2.item(j);
            if (o2.getNodeName().equals("Pattern"))
            {
                String d = getAttr(o2);
                ASAPPattern tmp = handler.makePattern(Integer.parseInt(d),b);
                NodeList l3 = o2.getChildNodes();
                for (int k = 0 ; k < l3.getLength() ; k++)
                {
                    Node o3 = l3.item(k);
                    if (o3.getNodeName().equals("ShiftGroup")) tmp.add(handler.getShiftGroup(o3.getFirstChild().getNodeValue()));
                    else if (o3.getNodeName().equals("Shift")){
                        Node child = o3.getFirstChild();
                        if (child == null) tmp.add(handler.getShift("R"));
                        else if (child.getNodeValue().equals("$"))
                        {
                            ASAPShiftSet set = handler.makeShiftSet();
                            for (ASAPPatternElement pe : handler.getShifts())
                            {
                                if (!pe.equals(handler.getShift("R")))
                                {
                                    set.add(pe);
                                }
                            }
                            tmp.add(set);
                        }
                        else if (child.getNodeValue().equals("*"))
                        {
                            ASAPShiftSet set = handler.makeShiftSet();
                            for (ASAPPatternElement pe : handler.getShifts())
                            {

                                set.add(pe);

                            }
                            tmp.add(set);
                        }
                        else tmp.add(handler.getShift(child.getNodeValue()));
                        //tmp.add(Shift.getFromId(o3.getFirstChild().getNodeValue()));
                    }
                    else if (o3.getNodeName().equals("NotShift")) {
                        String t = o3.getFirstChild().getNodeValue();
                        ASAPShift not = handler.getShift(t);
                        ASAPShiftSet set = handler.makeShiftSet();
                        for (ASAPPatternElement pe : handler.getShifts()/*handler.getShiftGroup("All")*/)
                        {
                            if (!pe.equals(not))
                                set.add(pe);
                        }
                        tmp.add(set);

                    }
                    else if (o3.getNodeName().equals("StartDay")) tmp.setStartDay(o3.getFirstChild().getNodeValue());
                }
                c.addPattern(tmp);



            }

        }

    }

    private void expand(Node n)
    {
        NodeList l = n.getChildNodes();
        for (int i = 0 ; i < l.getLength() ; i++)
            parseXMLNode(l.item(i));
    }



    private String getInSons(Node n, String pat)
    {
        String s = null;
        NodeList l = n.getChildNodes();
        for (int i = 0 ; i < l.getLength() ;i++)
        {
            if (l.item(i).getNodeName().equals(pat))
                return l.item(i).getFirstChild().getNodeValue();
            else
                getInSons(l.item(i),pat);
        }
        return s;
    }

    private String getAttr(Node n)
    {
        if (n.getAttributes().item(0) != null)
            return n.getAttributes().item(0).getNodeValue();
        else
            return "0";
    }


    public String getId() {
        return id;
    }





}