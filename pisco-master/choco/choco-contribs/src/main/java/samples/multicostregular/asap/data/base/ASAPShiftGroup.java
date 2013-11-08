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

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Dec 16, 2008
 * Time: 3:23:02 PM
 */
public class ASAPShiftGroup extends AbstractList<ASAPShift> implements ASAPPatternElement {

    String id;
    ArrayList<ASAPShift> shifts;



    public void init(String id, ASAPItemHandler handler)
    {
        this.id = id;
        this.shifts = new ArrayList<ASAPShift>();
        handler.putShiftGroup(id,this);
    }

    public ASAPShiftGroup(ASAPItemHandler handler, String id, ASAPShift... shift)
    {
        this.init(id,handler);
        this.shifts.addAll(Arrays.asList(shift));
    }
    public ASAPShiftGroup(ASAPItemHandler handler, String id, String... sids)
    {
        this.init(id, handler);
        for (String s : sids)
        {
            this.shifts.add(handler.getShift(s));
        }
    }

    public ASAPShiftGroup(ASAPItemHandler handler, String id)
    {
        this.init(id, handler);
    }

    public boolean add(ASAPShift s)
    {
        return this.shifts.add(s);
    }

    public String getId() {
        return id;
    }

    public ASAPShift get(int i) {
        return this.shifts.get(i);
    }

    public Iterator<ASAPShift> iterator() {
        return this.shifts.iterator();
    }

    public int size() {
        return this.shifts.size();
    }

    public boolean isInPattern(ASAPShift s) {
        return this.shifts.contains(s);
    }

    public String toRegExp() {
        StringBuffer b = new StringBuffer("(");
        for (ASAPPatternElement e : shifts)
        {
            b.append(e.toRegExp()).append("|");
        }
        b.deleteCharAt(b.length()-1).append(")");
        //System.out.println(b.toString());
        return b.toString();

    }

    @Override
    public int[] getElementValues() {
        int[] ret = new int[this.size()];
        Iterator<ASAPShift> it = this.iterator();
        int i  = 0;
        while (it.hasNext())
        {
            ret[i++] = it.next().getElementValues()[0];
        }
        return ret;

    }
}