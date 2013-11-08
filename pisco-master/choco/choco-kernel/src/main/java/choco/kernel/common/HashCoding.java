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

package choco.kernel.common;

import java.lang.reflect.Array;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 13 févr. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*
*
* Compute the hashCode of a given object
*/
public class HashCoding {
    /**
     * An initial value for a <code>hashCode</code>, to which is added contributions
     * from fields. Using a non-zero value decreases collisons of <code>hashCode</code>
     * values.
     */
    public static final int SEED = 23;

    /**
     * booleans.
     */
    private static int hash(int aSeed, boolean aBoolean) {
        return firstTerm(aSeed) + (aBoolean ? 1 : 0);
    }

    /**
     * chars.
     */
    private static int hash(int aSeed, char aChar) {
        return firstTerm(aSeed) + (int) aChar;
    }

    /**
     * ints.
     */
    private static int hash(int aSeed, int aInt) {
        /*
        * Implementation Note
        * Note that byte and short are handled by this method, through
        * implicit conversion.
        */
        return firstTerm(aSeed) + aInt;
    }

    /**
     * longs.
     */
    private static int hash(int aSeed, long aLong) {
        return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
    }

    /**
     * floats.
     */
    private static int hash(int aSeed, float aFloat) {
        return hash(aSeed, Float.floatToIntBits(aFloat));
    }

    /**
     * doubles.
     */
    private static int hash(int aSeed, double aDouble) {
        return hash(aSeed, Double.doubleToLongBits(aDouble));
    }

    /**
     * <code>aObject</code> is a possibly-null object field, and possibly an array.
     * <p/>
     * If <code>aObject</code> is an array, then each element may be a primitive
     * or a possibly-null object.
     */
    private static int hash(int aSeed, Object aObject) {
        int result = aSeed;
        if (aObject == null) {
            result = hash(result, 0);
        } else if (!isArray(aObject)) {
            result = hash(result, aObject.hashCode());
        } else {
            int length = Array.getLength(aObject);
            for (int idx = 0; idx < length; ++idx) {
                Object item = Array.get(aObject, idx);
                //recursive call!
                result = hash(result, item);
            }
        }
        return result;
    }


    /// PRIVATE ///
    private static final int fODD_PRIME_NUMBER = 37;

    private static int firstTerm(int aSeed) {
        return fODD_PRIME_NUMBER * aSeed;
    }

    private static boolean isArray(Object aObject) {
        return aObject.getClass().isArray();
    }

    public static int hashCodeMe(Object field) {
    	return hashCodeMe(new Object[]{field});
    }

    public static int hashCodeMe(Object[] fields) {
        int result = SEED;
        for(int field = 0; field < fields.length; field++){
            result = hash(result, field);
        }
        return result;
    }

    public static int hashCodeMe(long field) {
    	return hash(SEED, field);
    }

}
