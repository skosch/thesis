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

import java.util.Collection;
import java.util.Iterator;

import choco.IPretty;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.iterators.EmptyIterator;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.ITaskVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.search.limit.Limit;
import choco.kernel.solver.search.measure.ISearchMeasures;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.ITask;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 3 juil. 2009
 * Since : Choco 2.1.0
 * Update : Choco 2.1.0
 *
 * Provides some short and usefull methods to deal with String object
 * and pretty print of IPretty objects.
 *
 */
public class StringUtils {
	private StringUtils() {}

	
	
	/**
	 * Pads out a string upto padlen with pad chars
	 *
	 * @param str    string to be padded
	 * @param padlen length of pad (+ve = pad on right, -ve pad on left)
	 * @param pad    character
	 * @return padded string
	 */
	public static String pad(String str, int padlen, String pad) {
		final StringBuilder padding = new StringBuilder(32);
		final int len = Math.abs(padlen) - str.length();
		if (len < 1) {
			return str;
		}
		for (int i = 0; i < len; ++i) {
			padding.append(pad);
		}
		return (padlen < 0 ? padding.append(str).toString() : padding.insert(0,str).toString());
	}


	public static DisposableIterator<String> getOptionIterator(final String options) {
		if(options != null && options.length() > 0) {
			return new DisposableIterator<String>() {

				int b, e = -1;

				@Override
				public boolean hasNext() {
					b = e + 1;
					while( (e = options.indexOf(' ', b)) >= 0) {
						if(e > b) return true;
						b=e+1;
					}
					e = options.length();
					return e > b; 
				}

				@Override
				public String next() {
					return options.substring(b, e);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Immutable");
				}

				/**
				 * This method allows to declare that an object is not used anymore. It
				 * can be reused by another object.
				 */
				@Override
				public void dispose() {}
			};
		} else {
			return EmptyIterator.get();
		}

	}

	//*****************************************************************//
	//*******************  Pretty  ********************************//
	//***************************************************************//
	public static String pretty(final IPretty[] elems, int begin, int end) {
		final StringBuilder buffer = new StringBuilder(32);
		buffer.append("{ ");
		for (int i = begin; i < end; i++) {
			buffer.append(elems[i].pretty()).append(", ");
		}
		if(begin < end) buffer.deleteCharAt(buffer.length() - 2);
		buffer.append('}');
		return new String(buffer);
	}

	public static String pretty(final IPretty... elems) {
		return pretty(elems, 0, elems.length);
	}

	public static String prettyOnePerLine(final Collection<? extends IPretty> elems) {
		return prettyOnePerLine(elems.iterator());
	}

	/**
	 * Pretty print of elements on 1 line 
	 * @param iter iterator over element
	 * @return pretty print of elements into a String
	 */
	public static String prettyOnePerLine(Iterator<? extends IPretty> iter) {
		final StringBuilder buffer = new StringBuilder(32);
		while (iter.hasNext()) {
			buffer.append(iter.next().pretty()).append('\n');
		}
		return new String(buffer);
	}

	public static String pretty(final Collection<? extends IPretty> elems) {
		return pretty(elems.iterator());
	}

	public static String pretty(final Iterator<? extends IPretty> iter) {
		final StringBuilder buffer = new StringBuilder(32);
		buffer.append("{ ");
		if (iter.hasNext()) {
			while (iter.hasNext()) {
				buffer.append(iter.next().pretty()).append(", ");
			}
			buffer.deleteCharAt(buffer.length() - 2);
		}
		buffer.append('}');
		return new String(buffer);
	}

	public static String pretty(int[] lval) {
		StringBuilder sb = new StringBuilder(32);
		sb.append('{');
		for (int i = 0; i < lval.length - 1; i++) {
			sb.append(lval[i]);
			sb.append(',');
		}
		sb.append(lval[lval.length - 1]);
		sb.append('}');
		return sb.toString();
	}

	public static String pretty(int[][] lvals) {
		StringBuilder sb = new StringBuilder(32);
		sb.append('{');
		for (int i = 0; i < lvals.length; i++) {
			if (i > 0) sb.append(", ");
			int[] lval = lvals[i];
			sb.append('{');
			for (int j = 0; j < lval.length; j++) {
				if (j > 0) sb.append(',');
				int val = lval[j];
				sb.append(val);
			}
			sb.append('}');
		}
		sb.append('}');
		return sb.toString();
	}

	public static String pretty(int c) {
		if (c > 0) return " + "+c;
		else if (c < 0) return " - "+Math.abs(c);
		else return "";
	}



	public static String prettyOnePerLine(ISearchMeasures measures) {
		StringBuilder b = new StringBuilder(32);
		for (Limit type : Limit.values()) {
			final int val = type.getValue(measures);
			if(val != Integer.MIN_VALUE) {
				b.append("\n  ").append(type.getUnit()).append(": ").append(val);
			}
		}
		if(b.length() > 0) b.deleteCharAt(0);
		return new String(b);
	}

	public static String pretty(ISearchMeasures measures) {
		StringBuilder b = new StringBuilder(32);
		for (Limit type : Limit.values()) {
			final int val = type.getValue(measures);
			if(val != Integer.MIN_VALUE) {
				b.append(", ").append(val).append(' ').append(type.getUnit());
			}
		}
		if(b.length() > 1) b.delete(0, 2);
		return new String(b);
	}


	/**
	 * Convert a regexp formed with integer charachter into a char formed regexp
	 * for instance, "12%12%" which stands for 1 followed by 2 followed by 12 would be misinterpreted by regular
	 * regular expression parser. We use here the asci code to encode everything as a single char.
	 * Due to char encoding limits, we cannot parse int greater than 2^16-1
	 * @param strRegExp a regexp of integer
	 * @return a char regexp
	 */
	public static String toCharExp(String strRegExp) {
		StringBuilder b = new StringBuilder(32);
		for (int i =0 ;i < strRegExp.length() ;i++)
		{
			char c = strRegExp.charAt(i);
			if (c == '<')
			{
				int out = strRegExp.indexOf('>',i+1);
				int tmp = Integer.parseInt(strRegExp.substring(i+1,out));
				b.append(FiniteAutomaton.getCharFromInt(tmp));
				i = out;
			}
			else if (Character.isDigit(c))
			{
				b.append(FiniteAutomaton.getCharFromInt(Character.getNumericValue(c)));

			}
			else if (c == '{')
			{
				int out = strRegExp.indexOf('}',i+1);
				b.append(c);
				for (int d = i+1; d <= out ; d++)
					b.append(strRegExp.charAt(d));
				i = out;
			}
			else
			{
				b.append(c);
			}
		}

		return b.toString();

	}

	/**
	 * Transform a char regexp into an int regexp w.r.t. the asci code of each character.
	 * @param charExp a char regexp
	 * @return an int regexp
	 */
	public static String toIntExp (String charExp)
	{
		StringBuilder b = new StringBuilder(32);
		for (int i = 0 ; i < charExp.length() ; i++)
		{
			char c = charExp.charAt(i);
			if (c == '(' || c == ')' || c == '*' || c == '+' || c == '|')
			{
				b.append(c);
			}
			else
			{
				int n = (int) c;
				if (n >= 35) n--;
				if (n < 10) b.append(n);
				else b.append('<').append(n).append('>');
			}
		}

		return b.toString();
	}

	private static long next;


	/**
	 * Return a generated short, random string
	 * @return String
	 */
	public static String randomName(){
		return "TMP_" + next++ ;
	}

	public static String format(int lb, int ub) {
		return lb == ub ? String.valueOf(lb) : lb + ".."+ub ;	
	}

	public static String format(IntegerVariable iv) {
		return format(iv.getLowB(), iv.getUppB());	
	}

	public static String format(IntDomainVar iv) {
		return format(iv.getInf(), iv.getSup());	
	}

	public static String pretty(ITask t) {
		final StringBuilder  b = new StringBuilder(32);
		b.append(t.getName()).append(":[");
		b.append(format(t.getEST(), t.getLST())).append(" + ");
		b.append(format(t.getMinDuration(), t.getMaxDuration())).append(" -> ");
		b.append(format(t.getECT(), t.getLCT())).append(']');
		return new String(b);
	}

	/**
	 * convert a task into .dot format.
	 * @param label  information appended to the default label
	 * @param format if <code>true</code> then format the node, else do nothing
	 * @param options the options passed to the .dot node.
	 * @return
	 */
	public static String toDotty(ITask t, String label,boolean format,String... options) {
		StringBuilder b= new StringBuilder(32);
		b.append(t.getID()).append("[ shape=record,");
		//label
		b.append("label=\"{ ");
		b.append('{').append(t.getEST()).append('|');
		b.append(format(t.getMinDuration(), t.getMaxDuration()));
		b.append('|').append(t.getECT()).append('}');
		b.append('|').append(t.getName());
		if(!t.isScheduled()) {
			b.append('|');
			b.append('{').append(t.getLST()).append('|').append(TaskUtils.getSlack(t)).append('|').append(t.getLCT()).append('}');
		}
		b.append(" }");
		if(label!=null) {b.append(label);}
		b.append(" \"");
		if(format){
			if(t.isScheduled()) {
				b.append(", style=bold");
			}else {
				b.append(", style=\"dashed,bold\"");
			}
		}
		if(options!=null) {
			for (int i = 0; i < options.length; i++) {
				b.append(", ").append(options[i]);
			}
		}
		b.append(" ];");
		return new String(b);
	}

	
	public static String randomName(ITask t1,ITask t2){
		return randomName()+"_" + t1.getName() + "_" +t2.getName();
	}
	
	public static String randomName(TaskVariable t1,TaskVariable t2){
		return randomName()+"_" + t1.getName() + "_" +t2.getName();
	}
}
