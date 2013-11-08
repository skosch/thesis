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


/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Dec 16, 2008
 * Time: 3:14:30 PM
 */
public class ASAPShift implements ASAPPatternElement {

    String id;
    String label;
    String colour;
    String description;
    int start;
    int end;
    int duration;
    boolean not;

    ASAPItemHandler handler;



    public ASAPShift(ASAPItemHandler handler, String ID, String label, String colour, String description, int start, int end, int duration)
    {
        this.id             =   ID;
        this.label          =   label;
        this.colour         =   colour;
        this.description    =   description;
        this.start          =   start;
        this.end            =   end;
        this.duration       =   duration;
        this.handler        =   handler;

        handler.putShift(this.id,this);
        this.not = true;
        addToMap(handler);
    }

    public ASAPShift(ASAPItemHandler handler, String id)
    {
        this.not = true;
        this.id = id;
        this.handler = handler;

        handler.putShift(this.id,this);
        addToMap(handler);
    }

    private void addToMap(ASAPItemHandler handler){
        int a = handler.map.size();
        handler.map.put(this.getID(),a);
        handler.inverseMap.put(a,this.getID());

    }

    public String getID() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getColour() {
        return colour;
    }

    public String getDescription() {
        return description;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getDuration() {
        return duration;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStart(int start) {
        this.start = start;
    }
    public void setStart(String time)
    {
        this.start = Integer.parseInt(time.split(":")[0]);
    }

    public void setEnd(int end) {
        this.end = end;
    }
    public void setEnd(String end)
    {
       this.end = Integer.parseInt(end.split(":")[0]);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDuration(String dur)
    {
        this.duration = (int) Float.parseFloat(dur);
    }

    public boolean equals(Object o)
    {
        if (o instanceof ASAPShift)
        {
            ASAPShift os = (ASAPShift) o;
            return os.id.equals(id) && os.label.equals(label) && os.duration == duration && os.start == start;
        }
        return false;       
    }

    public boolean isInPattern(ASAPShift s) {
        return s.equals(this);
    }

    

    public String toRegExp() {
        return ""+ handler.map.get(this.id);
    }

    @Override
    public int[] getElementValues() {
        return new int[]{handler.map.get(this.id)};
    }
}