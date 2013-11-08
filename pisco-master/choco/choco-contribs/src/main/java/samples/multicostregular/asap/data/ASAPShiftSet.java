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


import samples.multicostregular.asap.data.base.ASAPPatternElement;
import samples.multicostregular.asap.data.base.ASAPShift;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;

import gnu.trove.TIntHashSet;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Dec 17, 2008
 * Time: 3:28:18 PM
 */
public class ASAPShiftSet extends AbstractSet<ASAPPatternElement> implements ASAPPatternElement {

    public HashSet<ASAPPatternElement> elem;


    public ASAPShiftSet()
    {
        this.elem = new HashSet<ASAPPatternElement>();
    }

    public boolean add(ASAPPatternElement pe)
    {
        return elem.add(pe);
    }

    public Iterator<ASAPPatternElement> iterator() {
        return elem.iterator();
    }

    public int size() {
        return elem.size();
    }

    public boolean isInPattern(ASAPShift s) {
        return elem.contains(s);
    }

    public String toRegExp() {
        StringBuffer b = new StringBuffer("(");
        for (ASAPPatternElement e : this)
        {
            b.append(e.toRegExp()).append("|");
        }
        b.deleteCharAt(b.length()-1).append(")");
        return b.toString();
    }

    @Override
    public int[] getElementValues() {
        TIntHashSet set = new TIntHashSet();
        for (ASAPPatternElement pat : this)
        {
            set.addAll(pat.getElementValues());
        }
        return set.toArray();

    }
}