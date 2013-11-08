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

package samples.multicostregular.asap.data;


import samples.multicostregular.asap.data.base.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Mar 18, 2009
 * Time: 2:17:50 PM
 */
public class ASAPItemHandler
{

    public ASAPDate start;
    public ASAPDate end;

    public HashMap<String, ASAPContract> contracts;
    public HashMap<String, ASAPEmployee> employees;
    public ArrayList<ASAPEmployee> orderedEmployees;
    public HashMap<String,Integer> map;
    public HashMap<Integer,String> inverseMap;
    public HashMap<String, ASAPShift> shifts;
    public HashMap<String, ASAPShiftGroup> shiftgroups;
    public HashMap<String, ASAPSkill> skills;

    public ASAPCoverRequirements cover;
    public ASAPMasterWeights masterWeights;
    public ASAPShiftOnRequest requestOn;


    public String pbName;



    public ASAPItemHandler()
    {
        contracts = new HashMap<String, ASAPContract>();
        employees = new HashMap<String, ASAPEmployee>();
        orderedEmployees = new ArrayList<ASAPEmployee>();
        map = new HashMap<String,Integer>();
        inverseMap = new HashMap<Integer,String>();
        shifts = new HashMap<String, ASAPShift>();
        shiftgroups = new HashMap<String, ASAPShiftGroup>();
        skills = new HashMap<String, ASAPSkill>();
        cover = new ASAPCoverRequirements();
        masterWeights = new ASAPMasterWeights();
        requestOn = new ASAPShiftOnRequest();
    }


    public ASAPDate getStart() {
        return start;
    }

    public void setStart(ASAPDate start) {
        this.start = start;
    }

    public ASAPDate getEnd() {
        return end;
    }

    public void setEnd(ASAPDate end) {
        this.end = end;
    }

    public ASAPEmployee getEmployee(String id)
    {
        return employees.get(id);
    }

    public ASAPContract getContract(String id)
    {
        return contracts.get(id);
    }

    public ASAPShift getShift(String id)
    {
        return shifts.get(id);
    }
    public Collection<ASAPShift> getShifts()
    {
        return shifts.values();
    }

    public ASAPShiftGroup getShiftGroup(String id)
    {
        return shiftgroups.get(id);
    }

    public ASAPSkill getSkill(String id)
    {
        return skills.get(id);
    }
    public Collection<ASAPSkill> getSkills()
    {
        return skills.values();
    }


    public Collection<ASAPEmployee> getEmployeeBySkill(ASAPSkill s)
    {
        if (s == null)
            return orderedEmployees;
        else
        {
            HashSet<ASAPEmployee> hs = new HashSet<ASAPEmployee>();
            for (ASAPEmployee e : orderedEmployees)
            {
                if (e.getSkills().contains(s))
                    hs.add(e);
            }
            return hs;
        }
    }



    public void putContract(String id, ASAPContract c)
    {
        contracts.put(id,c);
    }


    public void putEmployee(String id, ASAPEmployee e)
    {
        employees.put(id,e);
        orderedEmployees.add(e);
    }
    public void putShift(String id, ASAPShift s)
    {
        shifts.put(id,s);
    }

    public void putShiftGroup(String id, ASAPShiftGroup a) {
        shiftgroups.put(id,a);
    }

    public void putSkill(String id, ASAPSkill s) {
        skills.put(id,s);
    }


    public ASAPSkill makeSkill(String id)
    {
        return new ASAPSkill(this,id);
    }

    public ASAPShift makeShift(String ID, String label, String colour, String description, int start, int end, int duration)
    {
        return new ASAPShift(this, ID, label, colour, description, start, end, duration);
    }

    public ASAPShift makeShift(String id)
    {
        return new ASAPShift(this, id);
    }


    public ASAPShiftGroup makeShiftGroup(String id, ASAPShift... shift)
    {
        return new ASAPShiftGroup(this,id, shift);
    }
    public ASAPShiftGroup makeShiftGroup(String id, String... sids)
    {
        return new ASAPShiftGroup(this,id, sids);
    }

    public ASAPShiftGroup makeShiftGroup(String id)
    {
        return new ASAPShiftGroup(this,id);
    }

    public ASAPRestShift makeRestShift()
    {
        return new ASAPRestShift(this);
    }

    public ASAPEmployee makeEmployee(String id)
    {
        return new ASAPEmployee(this,id);

    }

    public ASAPContract makeContract(String id)
    {
        return new ASAPContract(this,id);
    }

    public ASAPCover makeCover()
    {
        return new ASAPCover(this);
    }

    public ASAPDate makeDate(int year, int month, int day)
    {
        return new ASAPDate(year,month,day);
    }

    public ASAPMasterWeights makeMasterWeights()
    {
        return new ASAPMasterWeights();
    }

    public ASAPPattern makePattern(int weight, boolean bad)
    {
        return new ASAPPattern(weight,bad);
    }

    public ASAPShiftOn makeShiftOn()
    {
        return new ASAPShiftOn(this);
    }

    public ASAPShiftOn makeShiftOn(int weight, ASAPShift shift, ASAPEmployee employee, ASAPDate date)
    {
        return new ASAPShiftOn(this,weight,shift,employee, date);
    }
    public ASAPShiftOn makeShiftOn(int weight)
    {
        return new ASAPShiftOn(this,weight);
    }

    public ASAPSubPattern makeSubPattern()
    {
        return new ASAPSubPattern();
    }


    public ASAPShiftSet makeShiftSet() {
        return new ASAPShiftSet();
    }

    public ASAPCoverRequirements makeCoverRequirements() {
        return new ASAPCoverRequirements();
    }

    public ASAPShiftOnRequest makeShiftOnRequest() {
        return new ASAPShiftOnRequest();
    }

    public ASAPCoverRequirements getCover() {
        return cover;
    }

    public ASAPShiftOnRequest getRequestOn() {
        return requestOn;
    }

    public void setProblemName(String s) {
        this.pbName = s;
    }
    public String getProblemName()
    {
        return this.pbName;
    }
}