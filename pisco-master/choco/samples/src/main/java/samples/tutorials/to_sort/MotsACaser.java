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

package samples.tutorials.to_sort;

/**
 * A tutorial on a simple crossword puzzle
 */
public class MotsACaser {


    /*// each letter is indexed by a number between 0--25
    // access from a number to a letter and from a letter to a number is
    // given by "numberToletters" and "lettersToNumbers"
    protected static char[] numberToletters = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    protected static HashMap lettersToNumbers = new HashMap();

    public static void loadCharMapping() {
        lettersToNumbers.put('a', 0);
        lettersToNumbers.put('b', 1);
        lettersToNumbers.put('c', 2);
        lettersToNumbers.put('d', 3);
        lettersToNumbers.put('e', 4);
        lettersToNumbers.put('f', 5);
        lettersToNumbers.put('g', 6);
        lettersToNumbers.put('h', 7);
        lettersToNumbers.put('i', 8);
        lettersToNumbers.put('j', 9);
        lettersToNumbers.put('k', 10);
        lettersToNumbers.put('l', 11);
        lettersToNumbers.put('m', 12);
        lettersToNumbers.put('n', 13);
        lettersToNumbers.put('o', 14);
        lettersToNumbers.put('p', 15);
        lettersToNumbers.put('q', 16);
        lettersToNumbers.put('r', 17);
        lettersToNumbers.put('s', 18);
        lettersToNumbers.put('t', 19);
        lettersToNumbers.put('u', 20);
        lettersToNumbers.put('v', 21);
        lettersToNumbers.put('w', 22);
        lettersToNumbers.put('x', 23);
        lettersToNumbers.put('y', 24);
        lettersToNumbers.put('z', 25);
    }


    protected int[][] grid = new int[][]{
            {1, 1, 1, 1, 0, 1, 0, 0},
            {1, 1, 0, 1, 1, 1, 1, 1},
            {1, 0, 0, 1, 0, 1, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 0, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1, 1}};

    protected String[] words = new String[]{
            "au", "ru", "su", "ut", "cou", "mer", "son",
            "sur", "use", "dent", "tags", "tris",
            "stop", "tiree", "prince", "suspens"};


    public void solvePuzzle() {
        int n = 6;    // number of lines
        int m = 8; // number of columns

        //1-  Choco Model
        //1a- Model and variables
        Solver pb = new CPSolver();
        IntDomainVar[][] vs = new IntDomainVar[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (grid[i][j] == 1)
                    vs[i][j] = pb.createEnumIntVar("x[" + i + "][" + j + "]", 0, 25);
            }
        }

        //1b- Constraints
        //Create a DFA corresponding to the words of a given size;
        DFA[] automata = new DFA[8];
        for (int i = 2; i < 8; i++) {
            automata[i] = new DFA(getWordsOfSize(i));
        }

        //posts constraint on words
        ArrayList<IntVar[]> wordOfSize2 = new ArrayList<IntVar[]>();
        wordOfSize2.add(new IntVar[]{vs[1][0], vs[1][1]});
        wordOfSize2.add(new IntVar[]{vs[4][1], vs[4][2]});
        wordOfSize2.add(new IntVar[]{vs[0][1], vs[1][1]});
        wordOfSize2.add(new IntVar[]{vs[4][7], vs[5][7]});

        for (Iterator<IntVar[]> it = wordOfSize2.iterator(); it.hasNext();) {
            pb.post(pb.regular(automata[2], it.next()));
        }

        ArrayList<IntVar[]> wordOfSize3 = new ArrayList<IntVar[]>();
        wordOfSize3.add(new IntVar[]{vs[4][5], vs[4][6], vs[4][7]});
        wordOfSize3.add(new IntVar[]{vs[5][0], vs[5][1], vs[5][2]});
        wordOfSize3.add(new IntVar[]{vs[3][2], vs[4][2], vs[5][2]});
        wordOfSize3.add(new IntVar[]{vs[3][1], vs[4][1], vs[5][1]});
        wordOfSize3.add(new IntVar[]{vs[3][6], vs[4][6], vs[5][6]});

        for (Iterator<IntVar[]> it = wordOfSize3.iterator(); it.hasNext();) {
            pb.post(pb.regular(automata[3], it.next()));
        }

        ArrayList<IntVar[]> wordOfSize4 = new ArrayList<IntVar[]>();
        wordOfSize4.add(new IntVar[]{vs[0][0], vs[0][1], vs[0][2], vs[0][3]});
        wordOfSize4.add(new IntVar[]{vs[5][4], vs[5][5], vs[5][6], vs[5][7]});
        wordOfSize4.add(new IntVar[]{vs[0][0], vs[1][0], vs[2][0], vs[3][0]});
        wordOfSize4.add(new IntVar[]{vs[0][3], vs[1][3], vs[2][3], vs[3][3]});

        for (Iterator<IntVar[]> it = wordOfSize4.iterator(); it.hasNext();) {
            pb.post(pb.regular(automata[4], it.next()));
        }

        pb.post(pb.regular(automata[5], new IntVar[]{vs[1][3], vs[1][4], vs[1][5], vs[1][6], vs[1][7]}));
        pb.post(pb.regular(automata[6], new IntVar[]{vs[0][5], vs[1][5], vs[2][5], vs[3][5], vs[4][5], vs[5][5]}));
        pb.post(pb.regular(automata[7], new IntVar[]{vs[3][0], vs[3][1], vs[3][2], vs[3][3], vs[3][4], vs[3][5], vs[3][6]}));

        //enforce all words of same same size to be distinct
        enforceDistincWords(pb, 2, wordOfSize2);
        enforceDistincWords(pb, 3, wordOfSize3);
        enforceDistincWords(pb, 4, wordOfSize4);

        try {
            pb.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        //pb.solve();
        printSol(vs);
    }


    public void enforceDistincWords(Model pb, int k, ArrayList<IntVar[]> wordOfSizek) {
        for (int i = 0; i < wordOfSizek.size(); i++) {
            for (int j = i + 1; j < wordOfSizek.size(); j++) {
                IntVar[] w1 = wordOfSizek.get(i);
                IntVar[] w2 = wordOfSizek.get(j);
                SConstraint[] ct = new SConstraint[k];
                for (int l = 0; l < ct.length; l++) {
                    ct[l] = pb.neq(w1[l], w2[l]);
                }
                pb.post(pb.or(ct));
            }
        }
    }


    public void printSol(IntDomainVar[][] vs) {
        for (int i = 0; i < vs.length; i++) {
            StringBuffer st = new StringBuffer();
            for (int j = 0; j < vs[0].length; j++) {
                if (vs[i][j] == null) {
                    st.append("* ");
                } else if (!vs[i][j].isInstantiated()) {
                    st.append("? ");
                } else st.append(numberToletters[vs[i][j].getVal()] + " ");
            }
            LOGGER.info(st.toString());
        }
    }

    public List<int[]> getWordsOfSize(int s) {
        ArrayList lwords = new ArrayList();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() == s) {
                char[] chartuple = word.toLowerCase().toCharArray();
                int[] tuple = new int[chartuple.length];
                for (int j = 0; j < chartuple.length; j++) {
                    tuple[j] = (Integer) lettersToNumbers.get(chartuple[j]);
                }
                lwords.add(tuple);
            }
        }
        return lwords;
    }

    public static void main(String[] args) {
        loadCharMapping();
        MotsACaser mots = new MotsACaser();
        mots.solvePuzzle();
    }*/


}

   
