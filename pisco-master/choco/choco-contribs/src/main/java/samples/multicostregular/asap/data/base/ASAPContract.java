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
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Dec 16, 2008
 * Time: 3:21:30 PM
 */
public class ASAPContract {
    String id;
    int maxShiftsPerDay;
    int maxNumAssignmentPenalty;
    int maxNumAssignment = -1;
    int minConsecutiveFreeDaysPenalty;
    int minConsecutiveFreeDays;
    int minConsecutiveWorkingDaysPenalty;
    int minConsecutiveWorkingDays;

    int maxConsecutiveWorkingWeekEndsPenalty;
    int maxConsecutiveWorkingWeekEnds;
    int maxShiftTypesPenalty;

    public int getMinShiftTypesPenalty() {
        return minShiftTypesPenalty;
    }

    int minShiftTypesPenalty;
    int maxWorkingWeekEndsPenalty;
    int maxWorkingWeekEnds = -1;
    int minWorkingWeekEndsPenalty;
    int minWorkingWeekEnds;
    int minDaysOffPenalty;
    int minDaysOff;
    int maxDaysOffPenalty;
    int maxDaysOff = -1;

    int maxWeekEndDaysPenalty;
    int maxWeekEndDays = -1;
    int minWeekEndDaysPenalty;
    int minWeekEndDays;

    HashMap<ASAPShift,Integer> maxShiftType;
    HashMap<ASAPShift,Integer> minShiftType;


    int minShiftsPerWeekPenalty;
    int minShiftsPerWeek = 0;
    int maxShiftsPerWeekPenalty;
    int maxShiftsPerWeek = 7;

    ArrayList<ASAPPattern> patterns;
    ASAPItemHandler handler;




    public ASAPContract(ASAPItemHandler handler, String id)
    {
        this.id = id;
        this.handler = handler;
        this.maxShiftType = new HashMap<ASAPShift,Integer>();
        this.minShiftType = new HashMap<ASAPShift,Integer>();

        this.patterns = new ArrayList<ASAPPattern>();
        handler.putContract(id,this);
    }

    public void addMaxShiftType(String s, int val)
    {
        this.maxShiftType.put(handler.getShift(s),val);
    }
    public void addMinShiftType(String s, int val)
    {
        this.minShiftType.put(handler.getShift(s),val);
    }

    public int getMaxShiftType(ASAPShift s)
    {
        Integer i = this.maxShiftType.get(s);
        if (i == null)
            return Integer.MAX_VALUE;
        else
            return i;
    }

    public int getMinShiftType(ASAPShift s)
    {
        Integer i = this.minShiftType.get(s);
        if (i == null)
            return 0;
        else
            return i;
    }


    public ArrayList<ASAPPattern> getPatterns() {
        return patterns;
    }

    public void addPattern(ASAPPattern badPattern) {
        this.patterns.add(badPattern);
    }

    public String getId() {
        return id;
    }

    public int getMaxShiftsPerDay() {
        return maxShiftsPerDay;
    }

    public void setMaxShiftsPerDay(int maxShiftsPerDay) {
        this.maxShiftsPerDay = maxShiftsPerDay;
    }

    public int getMaxNumAssignmentPenalty() {
        return maxNumAssignmentPenalty;
    }

    public void setMaxNumAssignmentPenalty(int maxNumAssignmentPenalty) {
        this.maxNumAssignmentPenalty = maxNumAssignmentPenalty;
    }

    public int getMaxNumAssignment() {
        return maxNumAssignment;
    }

    public void setMaxNumAssignment(int maxNumAssignment) {
        this.maxNumAssignment = maxNumAssignment;
    }

    public int getMinConsecutiveFreeDaysPenalty() {
        return minConsecutiveFreeDaysPenalty;
    }

    public void setMinConsecutiveFreeDaysPenalty(int minConsecutiveFreeDaysPenalty) {
        this.minConsecutiveFreeDaysPenalty = minConsecutiveFreeDaysPenalty;
    }

    public int getMinConsecutiveFreeDays() {
        return minConsecutiveFreeDays;
    }

    public void setMinConsecutiveFreeDays(int minConsecutiveFreeDays) {
        this.minConsecutiveFreeDays = minConsecutiveFreeDays;
    }

    public int getMaxConsecutiveWorkingWeekEndsPenalty() {
        return maxConsecutiveWorkingWeekEndsPenalty;
    }

    public void setMaxConsecutiveWorkingWeekEndsPenalty(int maxConsecutiveWorkingWeekEndsPenalty) {
        this.maxConsecutiveWorkingWeekEndsPenalty = maxConsecutiveWorkingWeekEndsPenalty;
    }

    public int getMaxConsecutiveWorkingWeekEnds() {
        return maxConsecutiveWorkingWeekEnds;
    }

    public void setMaxConsecutiveWorkingWeekEnds(int maxConsecutiveWorkingWeekEnds) {
        this.maxConsecutiveWorkingWeekEnds = maxConsecutiveWorkingWeekEnds;
    }

    public int getMinShiftsPerWeekPenalty() {
        return minShiftsPerWeekPenalty;
    }

    public void setMinShiftsPerWeekPenalty(int minShiftsPerWeekPenalty) {
        this.minShiftsPerWeekPenalty = minShiftsPerWeekPenalty;
    }

    public int getMinShiftsPerWeek() {
        return minShiftsPerWeek;
    }

    public void setMinShiftsPerWeek(int minShiftsPerWeek) {
        this.minShiftsPerWeek = minShiftsPerWeek;
    }

    public int getMaxShiftsPerWeekPenalty() {
        return maxShiftsPerWeekPenalty;
    }

    public void setMaxShiftsPerWeekPenalty(int maxShiftsPerWeekPenalty) {
        this.maxShiftsPerWeekPenalty = maxShiftsPerWeekPenalty;
    }

    public int getMaxShiftsPerWeek() {
        return maxShiftsPerWeek;
    }

    public void setMaxShiftsPerWeek(int maxShiftsPerWeek) {
        this.maxShiftsPerWeek = maxShiftsPerWeek;
    }

    public int getMaxShiftTypesPenalty() {
        return maxShiftTypesPenalty;
    }

    public void setMaxShiftTypesPenalty(int maxShiftTypesPenalty) {
        this.maxShiftTypesPenalty = maxShiftTypesPenalty;
    }
      public void setMinShiftTypesPenalty(int maxShiftTypesPenalty) {
        this.minShiftTypesPenalty = maxShiftTypesPenalty;
    }

    public int getMaxWorkingWeekEndsPenalty() {
        return maxWorkingWeekEndsPenalty;
    }

    public void setMaxWorkingWeekEndsPenalty(int maxWorkingWeekEndsPenalty) {
        this.maxWorkingWeekEndsPenalty = maxWorkingWeekEndsPenalty;
    }

    public int getMaxWorkingWeekEnds() {
        return maxWorkingWeekEnds;
    }

    public void setMaxWorkingWeekEnds(int maxWorkingWeekEnds) {
        this.maxWorkingWeekEnds = maxWorkingWeekEnds;
    }

    public int getMinWorkingWeekEndsPenalty() {
        return minWorkingWeekEndsPenalty;
    }

    public void setMinWorkingWeekEndsPenalty(int minWorkingWeekEndsPenalty) {
        this.minWorkingWeekEndsPenalty = minWorkingWeekEndsPenalty;
    }

    public int getMinWorkingWeekEnds() {
        return minWorkingWeekEnds;
    }

    public void setMinWorkingWeekEnds(int minWorkingWeekEnds) {
        this.minWorkingWeekEnds = minWorkingWeekEnds;
    }

    public int getMinConsecutiveWorkingDaysPenalty() {
        return minConsecutiveWorkingDaysPenalty;
    }

    public void setMinConsecutiveWorkingDaysPenalty(int minConsecutiveWorkingDaysPenalty) {
        this.minConsecutiveWorkingDaysPenalty = minConsecutiveWorkingDaysPenalty;
    }

    public int getMinConsecutiveWorkingDays() {
        return minConsecutiveWorkingDays;
    }

    public void setMinConsecutiveWorkingDays(int minConsecutiveWorkingDays) {
        this.minConsecutiveWorkingDays = minConsecutiveWorkingDays;
    }

    public int getMinDaysOffPenalty() {
        return minDaysOffPenalty;
    }

    public void setMinDaysOffPenalty(int minDaysOffPenalty) {
        this.minDaysOffPenalty = minDaysOffPenalty;
    }

    public int getMinDaysOff() {
        return minDaysOff;
    }

    public void setMinDaysOff(int minDaysOff) {
        this.minDaysOff = minDaysOff;
    }

    public int getMaxDaysOffPenalty() {
        return maxDaysOffPenalty;
    }

    public void setMaxDaysOffPenalty(int maxDaysOffPenalty) {
        this.maxDaysOffPenalty = maxDaysOffPenalty;
    }

    public int getMaxDaysOff() {
        return maxDaysOff;
    }

    public void setMaxDaysOff(int maxDaysOff) {
        this.maxDaysOff = maxDaysOff;
    }

    public int getMaxWeekEndDaysPenalty() {
        return maxWeekEndDaysPenalty;
    }

    public void setMaxWeekEndDaysPenalty(int maxWeekEndDaysPenalty) {
        this.maxWeekEndDaysPenalty = maxWeekEndDaysPenalty;
    }

    public int getMaxWeekEndDays() {
        return maxWeekEndDays;
    }

    public void setMaxWeekEndDays(int maxWeekEndDays) {
        this.maxWeekEndDays = maxWeekEndDays;
    }

    public int getMinWeekEndDaysPenalty() {
        return minWeekEndDaysPenalty;
    }

    public void setMinWeekEndDaysPenalty(int minWeekEndDaysPenalty) {
        this.minWeekEndDaysPenalty = minWeekEndDaysPenalty;
    }

    public int getMinWeekEndDays() {
        return minWeekEndDays;
    }

    public void setMinWeekEndDays(int minWeekEndDays) {
        this.minWeekEndDays = minWeekEndDays;
    }
}