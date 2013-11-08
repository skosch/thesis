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
 * Time: 5:46:37 PM
 */
public class ASAPShiftOn {

    int weight;
    ASAPShift shift;
    ASAPEmployee employee;
    ASAPDate date;
    ASAPItemHandler handler;
    boolean isOn;

    public ASAPShiftOn(ASAPItemHandler handler)
    {
        this(handler,0);
    }

    public ASAPShiftOn(ASAPItemHandler handler, int weight, ASAPShift shift, ASAPEmployee employee, ASAPDate date) {
        this(handler,0);
        this.weight = weight;
        this.shift = shift;
        this.employee = employee;
        this.date = date;
    }
    public ASAPShiftOn(ASAPItemHandler handler, int weight)
    {
        this.handler = handler;
        this.weight = weight;
	this.isOn = true;
        handler.requestOn.add(this);
    }


    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public ASAPShift getShift() {
        return shift;
    }

    public void setShift(String sid) {
        this.shift = handler.getShift(sid);
    }

    public ASAPEmployee getEmployee() {
        return employee;
    }

    public void setEmployee(String eid) {
        this.employee = handler.getEmployee(eid);
    }

    public ASAPDate getDate() {
        return date;
    }

    public void setDate(ASAPDate date) {
        this.date = date;
    }

public boolean isOn() {return isOn; }
public boolean isOff() {return !isOn; }
public void setOn() { isOn = true; }
public void setOff() { isOn = false; }

}