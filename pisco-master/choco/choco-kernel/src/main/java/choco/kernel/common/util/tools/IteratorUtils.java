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

package choco.kernel.common.util.tools;

import choco.kernel.common.util.iterators.*;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 3 juil. 2009
 * Since : Choco 2.1.0
 * Update : Choco 2.1.0
 */
public class IteratorUtils {
	//****************************************************************//
	//********* Iterator *******************************************//
	//****************************************************************//
	public static <E> ListIterator<E> setImmutableIterator(final ListIterator<E> iter) {
		return new ImmutableListIterator<E>(iter);
	}

	public static <E> ListIterator<E> getImmutableIterator(final List<E> list) {
		return setImmutableIterator(list.listIterator());
	}

	public static <E> DisposableIterator<E> iterator(final E elem) {
		return SingleElementIterator.getIterator(elem);
	}

	public static <E> DisposableIterator<E> iterator(final E[] array) {
		return ArrayIterator.getIterator(array, array.length);
	}

	public static <E> Iterator<E> append(final Iterator<E>... iters) {
		return new AppendIterator<E>(iters);
	}

	@SuppressWarnings("unchecked")
	public static <E> Iterator<E> iterator(final List<E>... lists) {
		Iterator<E>[] iters= (Iterator<E>[]) java.lang.reflect.Array.newInstance(Iterator.class, lists.length);
		for (int i = 0; i < lists.length; i++) {
			iters[i]= getImmutableIterator(lists[i]);
		}
		return append(iters);
	}

	public static Iterator<Variable> variableIterator(final Iterator<? extends Variable>... iters) {
		return new AppendIterator<Variable>(iters);
	}

	public static <E> Iterator<E> appendAndCast(final Iterator<? extends E>... iters) {
		return new AppendIterator<E>(iters);
	}

	@SuppressWarnings("unchecked")
	public static <E> Iterator<E> append(final E[]... arrays) {
		Iterator<E>[] iters= (Iterator<E>[]) java.lang.reflect.Array.newInstance(Iterator.class, arrays.length);
		for (int i = 0; i < arrays.length; i++) {
			iters[i]= iterator(arrays[i]);
		}
		return append(iters);
	}

	public static Iterator<Constraint> iterator(final Model m, final Collection<Constraint> constraints) {
		return new Iterator<Constraint>(){
			Constraint c;
			final Iterator<Constraint> it = constraints.iterator();
			public boolean hasNext() {
				while(true){
					if(it == null){
						return false;
					}else
						if(it.hasNext()){
							c = it.next();
							if(Boolean.TRUE.equals(m.contains(c))){
								return true;
							}
						}else{
							return false;
						}
				}
			}

			@Override
			public Constraint next() {
				return c;
			}


			@Override
			public void remove() {
				it.remove();
			}
		};
	}
}
