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

package choco.kernel.memory.copy;

import gnu.trove.TIntObjectHashMap;

/*
 * Created by IntelliJ IDEA.
 * User: Julien
 * Date: 29 mars 2007
 * Since : Choco 2.0.0
 *
 */
public final class RcSave implements RecomputableElement {

    public RecomputableElement[][] currentElement;
    private int lastSavedWorldIndex;



    private static TIntObjectHashMap<int []> saveInt;
    private static TIntObjectHashMap<Object[][]> saveVector;
    private static TIntObjectHashMap<int[][]> saveIntVector;
    private static TIntObjectHashMap<boolean[]> saveBool;
    private static TIntObjectHashMap<long []> saveLong;
    private static TIntObjectHashMap<double []> saveDouble;
    private static TIntObjectHashMap<Object []> saveObject;

    static {
        saveInt = new TIntObjectHashMap<int []>();
        saveVector = new TIntObjectHashMap<Object[][]>();
        saveIntVector = new TIntObjectHashMap<int[][]>();
        saveBool = new TIntObjectHashMap<boolean[]>();
        saveLong = new TIntObjectHashMap<long[]>();
        saveDouble = new TIntObjectHashMap<double[]>();
        saveObject = new TIntObjectHashMap<Object[]>();
    }


    public RcSave(EnvironmentCopying env) {
        lastSavedWorldIndex = env.getWorldIndex();

        clear();
    }

    public void clear(){

        saveInt.clear();
        saveVector.clear();
        saveIntVector.clear();
        saveBool.clear();
        saveLong.clear();
        saveDouble.clear();
        saveObject.clear();
    }

    public void save(int worldIndex) {
        if (lastSavedWorldIndex >= worldIndex)
            lastSavedWorldIndex = 0;

        boolean [] tmpbool =  new boolean[currentElement[BOOL].length];
        for (int i = currentElement[BOOL].length ; --i>=0; ) {
            tmpbool[i] = ((RcBool) currentElement[BOOL][i]).deepCopy();
        }
        saveBool.put(worldIndex,tmpbool);

        int [] tmpint = new int [currentElement[INT].length];
        for (int i = currentElement[INT].length ; --i>=0; ) {
            tmpint[i] = ((RcInt) currentElement[INT][i]).deepCopy();
        }
        saveInt.put(worldIndex,tmpint);

        Object[][] tmpvec = new Object[currentElement[VECTOR].length][];
        for (int i = currentElement[VECTOR].length ; --i>=0; ) {
            if (worldIndex != 0 && lastSavedWorldIndex >= (currentElement[VECTOR][i]).getTimeStamp() )
                tmpvec[i] = saveVector.get(lastSavedWorldIndex)[i];
            else
                tmpvec[i] = ((RcVector) currentElement[VECTOR][i]).deepCopy();
        }
        saveVector.put(worldIndex,tmpvec);

        int[][] tmpintvec = new int [currentElement[INTVECTOR].length][];
        for (int i = currentElement[INTVECTOR].length ; --i>=0; ) {
            if (worldIndex != 0 && lastSavedWorldIndex >= (currentElement[INTVECTOR][i]).getTimeStamp() )
                tmpintvec[i] = saveIntVector.get(lastSavedWorldIndex)[i];
            else
                tmpintvec[i] = ((RcIntVector) currentElement[INTVECTOR][i]).deepCopy();
        }
        saveIntVector.put(worldIndex,tmpintvec);

        long [] tmplong = new long [currentElement[LONG].length];
        for (int i = currentElement[LONG].length ; --i>=0; ) {
            tmplong[i] = ((RcLong) currentElement[LONG][i]).deepCopy();
        }
        saveLong.put(worldIndex,tmplong);

        double[] tmpdouble = new double [currentElement[DOUBLE].length];
        for (int i = currentElement[DOUBLE].length ; --i>=0; ) {
            tmpdouble[i] = ((RcDouble) currentElement[DOUBLE][i]).deepCopy();
        }
        saveDouble.put(worldIndex,tmpdouble);

        Object[] tmpobject = new Object[currentElement[OBJECT].length];
        for (int i = currentElement[OBJECT].length ; --i>=0; ) {
            if (worldIndex != 0 && lastSavedWorldIndex >= (currentElement[OBJECT][i]).getTimeStamp() )
                tmpobject[i] = saveObject.get(lastSavedWorldIndex)[i];
            else
                tmpobject[i] = ((RcObject) currentElement[OBJECT][i]).deepCopy();
        }
        saveObject.put(worldIndex,tmpobject);


        lastSavedWorldIndex = worldIndex;


    }

    public void restore(int worldIndex) {
        boolean[] tmpbool = saveBool.get(worldIndex);
        int[] tmpint = saveInt.get(worldIndex);
        Object[][] tmpvec = saveVector.get(worldIndex);
        int[][] tmpintvec = saveIntVector.get(worldIndex);
        long[] tmplong = saveLong.get(worldIndex);
        double[] tmpdouble = saveDouble.get(worldIndex);
        Object[] tmpobject = saveObject.get(worldIndex);
        //saveVector.remove(worldIndex);

        for (int i = tmpbool.length ; --i>=0; )
            ((RcBool) currentElement[BOOL][i])._set(tmpbool[i], worldIndex);
        for (int i = tmpint.length ; --i >=0 ; )
            ((RcInt) currentElement[INT][i])._set(tmpint[i], worldIndex);
        for (int i = tmpvec.length ; --i>=0;)
            ((RcVector) currentElement[VECTOR][i])._set(tmpvec[i], worldIndex);
        for (int i = tmpintvec.length ; --i>=0;)
            ((RcIntVector) currentElement[INTVECTOR][i])._set(tmpintvec[i], worldIndex);
        for (int i = tmplong.length ; --i>=0;)
            ((RcLong) currentElement[LONG][i])._set(tmplong[i], worldIndex);
        for (int i = tmpdouble.length ; --i>=0;)
            ((RcDouble) currentElement[DOUBLE][i])._set(tmpdouble[i], worldIndex);
        for (int i = tmpobject.length ; --i>=0;)
            ((RcObject) currentElement[OBJECT][i])._set(tmpobject[i], worldIndex);

        if (worldIndex == 0)
            clearMaps();
        else
            remove(worldIndex+1);
  //          removeLast();


    }

    public void remove(int worldIndex) {
        saveInt.remove(worldIndex);
        saveVector.remove(worldIndex);
        saveIntVector.remove(worldIndex);
        saveBool.remove(worldIndex);
        saveLong.remove(worldIndex);
        saveDouble.remove(worldIndex);
        saveObject.remove(worldIndex);
    }


    private void clearMaps() {
        saveInt.clear();
        saveVector.clear();
        saveIntVector.clear();
        saveBool.clear();
        saveLong.clear();
        saveDouble.clear();
        saveObject.clear();
    }


    public int getType() {
        return -1;
    }

    public int getTimeStamp() {
        return 0;
    }
}
