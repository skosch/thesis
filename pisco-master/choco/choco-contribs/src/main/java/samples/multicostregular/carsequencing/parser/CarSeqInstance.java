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

package samples.multicostregular.carsequencing.parser;


import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jan 28, 2009
 * Time: 3:12:14 PM
 */
public class CarSeqInstance {

    public String name;

    public int nbCars;
    public int nbOptions;
    public int nbClasses;

    public int[] maxPerBlock;
    public int[] blockSize;

    public int[][] optionRequirement;
    public int[] nbOptionsPerClasse;


    public CarSeqInstance(String filename)
    {
        try {
            this.parse(filename);
        } catch (IOException e) {
            System.err.println("Unable to parse file");
        }
    }

    private void parse(String filename) throws IOException {
        BufferedReader read = new BufferedReader(new FileReader(filename));

        String r;
        int i = 0;

        while ((r= read.readLine()) != null)
        {
            if (r.length() < 3) break;
            if (i < 3)
            {
                if (i == 1)
                {
                    this.name = r.substring(2,r.length());
                }
            }
            else
            {
                String[] stmp = r.split(" ");
                int[] tmp = new int[stmp.length];
                for (int l = 0 ; l < stmp.length;l++) tmp[l] = Integer.parseInt(stmp[l]);

                if (i == 3)
                {
                    this.nbCars = tmp[0];
                    this.nbOptions = tmp[1];
                    this.nbClasses = tmp[2];
                    this.optionRequirement = new int[this.nbClasses][];
                }
                else if (i == 4)
                {
                    this.maxPerBlock = new int[tmp.length];
                    System.arraycopy(tmp,0,this.maxPerBlock,0,tmp.length);
                }
                else if (i == 5)
                {
                    this.blockSize = new int[tmp.length];
                    System.arraycopy(tmp,0,this.blockSize,0,tmp.length);
                }
                else
                {
                    int[] or = new int[this.nbOptions+2];
                    System.arraycopy(tmp,0,or,0,tmp.length);
                    this.optionRequirement[i-6] = or;
                }
            }
            i++;
        }
    }

    public static void main(String[] args) {

        int nb = 10;

        try {
            BufferedWriter buff = new BufferedWriter(new FileWriter("monFichier.txt"));

            buff.write(nb+"");
            buff.newLine();
            buff.write((nb+1)+"");
            buff.newLine();
            buff.close();



        } catch (IOException e) {
            System.err.println("impossible d'ouvrir le fichier");
        }


    }


}