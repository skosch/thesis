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

package samples.multicostregular.asap.data.base;


import samples.multicostregular.asap.data.ASAPItemHandler;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Dec 16, 2008
 * Time: 4:44:46 PM
 */
public class ASAPCover {

    String day;


    ArrayList<ASAPSkill> skills;
    ArrayList<ASAPShift> shifts;
    ArrayList<Integer> prefs;
    ArrayList<Integer> mins;
    ArrayList<Integer> maxs;


    public ASAPCover(ASAPItemHandler handler) {
        this.skills = new ArrayList<ASAPSkill>();
        this.shifts = new ArrayList<ASAPShift>();
        this.prefs = new ArrayList<Integer>();
        this.mins = new ArrayList<Integer>();
        this.maxs = new ArrayList<Integer>();
        handler.cover.addDayOfWeekCover(this);
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void push(ASAPSkill skill, ASAPShift shift, Integer pref, Integer min, Integer max)
    {
        this.skills.add(skill);
        this.shifts.add(shift);
        this.prefs.add(pref);
        this.mins.add(min);
        this.maxs.add(max);
    }

    public ArrayList<ASAPSkill> getSkills() {
        return skills;
    }

    public ArrayList<ASAPShift> getShifts() {
        return shifts;
    }

    public ArrayList<Integer> getPrefs() {
        return prefs;
    }

    public ArrayList<Integer> getMins() {
        return mins;
    }

    public ArrayList<Integer> getMaxs() {
        return maxs;
    }
}