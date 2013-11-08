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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Dec 16, 2008
 * Time: 5:49:20 PM
 */
public class ASAPDate {

    public static String[] names = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
    public static HashMap<String,Integer> indexes = new HashMap<String,Integer>();
    static
    {
        for (int i = 0 ;i < names.length ;i++)
        {
            indexes.put(names[i],i);
        }
    }

    int year;
    int month;
    int day;
    private Calendar calendar;


    public ASAPDate(int year,int month, int day)
    {
        this.year = year;
        this.month = month-1;
        this.day = day;
        this.setCalendar();
    }

    private void setCalendar()
    {
        calendar = new GregorianCalendar(year,month,day);

    }
    public int getDayOfWeek()
    {
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
    public String getDayOfWeekName()
    {
        return names[getDayOfWeek()-1];
    }


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month+1;
    }

    public void setMonth(int month) {
        this.month = month-1;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public static ASAPDate parseDate(String s)
    {
        String[] tmp = s.split("-");
        return new ASAPDate(Integer.parseInt(tmp[0]),Integer.parseInt(tmp[1]),Integer.parseInt(tmp[2]));
    }

    public String toString()
    {
        return this.year+"-"+(this.month+1)+"-"+this.day;
    }

    public static int getDaysBetween (ASAPDate da1, ASAPDate da2) {
        Calendar d1 = da1.calendar;
        Calendar d2 = da2.calendar;
        if (d1.after(d2)) {  // swap dates so that d1 is start and d2 is end
            java.util.Calendar swap = d1;
            d1 = d2;
            d2 = swap;
        }
        int days = d2.get(java.util.Calendar.DAY_OF_YEAR) -
                d1.get(java.util.Calendar.DAY_OF_YEAR);
        int y2 = d2.get(java.util.Calendar.YEAR);
        if (d1.get(java.util.Calendar.YEAR) != y2) {
            d1 = (java.util.Calendar) d1.clone();
            do {
                days += d1.getActualMaximum(java.util.Calendar.DAY_OF_YEAR);
                d1.add(java.util.Calendar.YEAR, 1);
            } while (d1.get(java.util.Calendar.YEAR) != y2);
        }
        return days;
    } // getDaysBetween()


}