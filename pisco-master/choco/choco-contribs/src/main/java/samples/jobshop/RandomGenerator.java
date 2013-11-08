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

package samples.jobshop;

public final class RandomGenerator {
    private final static int A = 16807;
    private final static int B = 127773;
    private final static int C = 2836;
    private final static int M = (0x1 << 31) - 1;
    private static int seed = 1;

    private RandomGenerator() {
    }

    private static double nextRand() {
        int rand = A * (seed % B) - (seed / B) * C;
        if (rand < 0) {
            rand += M;
        }
        seed = rand;
        return (double) rand / M;
    }

    private static int nextRand(int a, int b) {
        return (int) Math.floor(a + nextRand() * (b - a + 1));
    }

    public static int[][] randMatrix(int seed, int x, int y) {
        RandomGenerator.seed = seed;
        int[][] matrix = new int[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                matrix[i][j] = nextRand(1, 99);
            }
        }
        return matrix;
    }

    public static int[][] randShuffle(int seed, int x, int y) {
        RandomGenerator.seed = seed;
        int[][] matrix = new int[x][y];

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                matrix[i][j] = j;
            }
            for (int j = 0; j < y; j++) {
                final int s = nextRand(j, y - 1);
                final int t = matrix[i][j];
                matrix[i][j] = matrix[i][s];
                matrix[i][s] = t;
            }
        }
        return matrix;
    }
}
