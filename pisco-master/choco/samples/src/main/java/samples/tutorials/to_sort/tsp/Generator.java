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

package samples.tutorials.to_sort.tsp;

import choco.kernel.common.logging.ChocoLogging;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;


public class Generator {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

    protected Random rand;

    protected int n;

    protected int maxDist;
    protected int[][] dist;

    protected int[] hamPath;

    public Generator(Random rand, int n, int maxDist) {
        this.rand = rand;
        this.n = n;
        this.maxDist= maxDist;
        this.dist = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                this.dist[i][j] = -1;
            }
        }
        this.hamPath = new int[n];
        generateHamCycle();
    }

    public int[][] generateMatrix() {  // matrix with splitted depot node
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = rand.nextInt(maxDist);
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j || (i == n - 1 && j == 0) ) {
                    dist[i][j] = 0;
                } else {
                    if (hamPath[i] == j) dist[i][j] = 1;
                }
                //LOGGER.info("dist["+i+"]["+j+"] = "+dist[i][j]);
            }
        }
        return dist;
    }

    private void generateHamCycle() {
        for (int i = 0; i < n; i++) hamPath[i] = -1;
        ArrayList<Integer> dispo = new ArrayList<Integer>(n-2);
        for (int i = 1; i < n-1; i++) dispo.add(i);
        //LOGGER.info(dispo.toString());
        int next = 0;
        while(!dispo.isEmpty()) {
            hamPath[next] = dispo.remove(rand.nextInt(dispo.size()));
            next = hamPath[next];
        }
        for (int i = 1; i < n-1; i++) {
            if (hamPath[i] == -1) hamPath[i] = n-1;
        }
        hamPath[n-1] = 0;
        LOGGER.info(showHamPath());
    }

    public String showHamPath() {
        String s = ""+hamPath[0];
        int i = 1;
        while(i < hamPath.length) {
            s += " "+hamPath[i];
            i++;
        }
        return s;
    }

}
