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

package parser.absconparseur.components;


import parser.absconparseur.InstanceTokens;

import java.util.Arrays;
import java.util.BitSet;

public class PDomain {
	private final String name;

	private final int[] values;

    private final int index;

    public String getName() {
		return name;
	}

	public int[] getValues() {
		return values;
	}

	public int getNbValues() {
		return values.length;
	}

	public BitSet getBitSetDomain() {
		if (values[0] < 0) return null;
		BitSet b = new BitSet(values.length);
		for (int i = 0; i < values.length; i++) {
			b.set(values[i]);
		}
		return b;
	}

	public int getIntersectionSize(PDomain dom2) {
		if (getMaxValue() < dom2.getMinValue() ||
			dom2.getMinValue() > getMaxValue()) {
			return 0;
		} else {
			BitSet b1 = getBitSetDomain();
			BitSet b2 = dom2.getBitSetDomain();
			if (b1 != null && b2 != null) {
				b1.and(b2);
				return b1.cardinality();
			} else return -1;

		}
	}

	//assume the values are sorted from the min to the max
	public int getMinValue() {
		return values[0];
	}

	public int getMaxValue() {
		return values[values.length - 1];
	}

	public int getMaxAbsoluteValue() {
		return Math.max(Math.abs(values[0]), Math.abs(values[values.length - 1]));
	}

	public PDomain(String name, int[] values) {
		this.name = name;
		this.values = values;
        this.index = Integer.parseInt(name.substring(1));
    }

	public boolean contains(int value) {
		return Arrays.binarySearch(values, value) >= 0;
	}

	public String toString() {
		int displayLimit = 5;
		String s = "  domain " + name + " with " + values.length + " values : ";
		for (int i = 0; i < Math.min(values.length, displayLimit); i++)
			s += values[i] + " ";
		return s + (values.length > displayLimit ? "..." : "");
	}

	public String getStringListOfValues() {
		int previousValue = values[0];
		boolean startedInterval = false;
        StringBuilder sb = new StringBuilder(16);
		for (int i = 1; i < values.length; i++) {
			int currentValue = values[i];
			if (currentValue != previousValue + 1) {
				if (startedInterval) {
                    sb.append(previousValue).append(InstanceTokens.DISCRETE_INTERVAL_END);
					startedInterval = false;
				} else
					sb.append(previousValue);
				sb.append(InstanceTokens.VALUE_SEPARATOR);
			} else {
				if (!startedInterval) {
                    sb.append(InstanceTokens.DISCRETE_INTERVAL_START).append(previousValue).append(InstanceTokens.DISCRETE_INTERVAL_SEPARATOR);
					startedInterval = true;
				}
			}
			previousValue = currentValue;
		}
		if (startedInterval)
            sb.append(previousValue).append(InstanceTokens.DISCRETE_INTERVAL_END);
		else
			sb.append(previousValue);
		return sb.toString();
	}

	public boolean controlValueRanging(int min, int max) {
		for (int v : values)
			if (v < min || v > max)
				return false;
		return true;
	}

    public int hashCode() {
        return index;
    }
}
