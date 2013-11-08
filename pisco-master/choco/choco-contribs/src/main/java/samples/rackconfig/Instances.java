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

package samples.rackconfig;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 24, 2009
 * Time: 1:11:38 PM
 */
public class Instances
{


    public static ArrayList<Instances> instances = new ArrayList<Instances>();

    public static int getNbInstances() { return instances.size(); }

    public static Instances getInstance(int idx) { return instances.get(idx); }
    static
    {
        Instances i1 = new Instances(2,4,5,new int[][]{{150,8,150},{200,16,200}},new int[][]{{20,10},{40,4},{50,2},{75,1}});
        Instances i2 = new Instances(2,4,10,new int[][]{{150,8,150},{200,16,200}},new int[][]{{20,20},{40,8},{50,4},{75,2}});
        Instances i3 = new Instances(2,6,12,new int[][]{{150,8,150},{200,16,200}},new int[][]{{10,20},{20,10},{40,8},{50,4},{75,3},{100,1}});



        Instances i4 = new Instances(6,6,9,new int[][]{{50,2,50},
                                                       {100,4,100},
                                                       {150,8,150},
                                                       {200,16,200},
                                                       {250,32,250},
                                                       {300,64,300}
                                                      },new int[][]{{20,10},
                                                                    {40,6},
                                                                    {50,4},
                                                                    {75,2},
                                                                    {100,2},
                                                                    {150,1}
                                                                    });

        instances.add(i1);
        instances.add(i2);
        instances.add(i3);
        instances.add(i4);


    }





    int nbRackModels;
    int nbCardTypes;
    int nbRacks;
    int[][] rackModels;
    int[][] cardTypes;


    private Instances(int nbRackModels, int nbCardTypes, int nbRacks, int[][] rackModels, int[][] cardTypes)
    {
        this.nbRackModels = nbRackModels;
        this.nbCardTypes = nbCardTypes;
        this.nbRacks = nbRacks;
        this.rackModels = rackModels;
        this.cardTypes = cardTypes;
    }


    public final int getNbRackModels()
    {
        return this.nbRackModels;
    }
    public final int getNbCardTypes()
    {
        return this.nbCardTypes;
    }
    public final int getNbRacks()
    {
        return this.nbRacks;
    }



    public final int getRackMaxPower(int rackModel)
    {
        return this.rackModels[rackModel][0];
    }
    public final int getRackCapacity(int rackModel)
    {
        return this.rackModels[rackModel][1];
    }
    public final int getRackPrice(int rackModel)
    {
        return this.rackModels[rackModel][2];
    }


    public final int getCardPower(int cardType)
    {
        return this.cardTypes[cardType][0];
    }
    public final int getCardNeed(int cardType)
    {
        return this.cardTypes[cardType][1];
    }

    public int getNbCard()
    {
        int out = 0;
        for (int i  = 0 ; i < this.getNbCardTypes() ;i++)
            out+=this.getCardNeed(i);
        return out;
    }
    



}