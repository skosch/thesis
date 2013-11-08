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

package choco.cp.solver.constraints.strong.maxrpcrm;

import java.util.Arrays;

public class Clique {
    private final MaxRPCVariable variable;
    private final AbstractMaxRPCConstraint c0;
    private final int c0Position;
    private final AbstractMaxRPCConstraint c1;
    private final int c1Position;

    private final AbstractMaxRPCConstraint homeConstraint;
    private final static int[] t0 = new int[2];
    private final static int[] t1 = new int[2];

    private final int[] lastPCSupports0;
    private final int[] lastPCSupports1;

    private final int offset0;
    private final int offset1;

    /**
     * Crée la 3-clique et met à jour la liste des 3-cliques de la 3e variable.
     * c0[c0Position] et c1[c1Position] doivent identifier la même variable
     * 
     * @param homeConstraint
     *            Contrainte « principale » de la 3-clique
     * @param c0
     *            Contrainte gauche
     * @param c0Position
     *            Position de la 3e variable dans la contrainte gauche
     * @param c1
     *            Contrainte droite
     * @param c1Position
     *            Position de la 3e variable dans la contrainte droite
     */
    public Clique(AbstractMaxRPCConstraint homeConstraint,
            AbstractMaxRPCConstraint c0, int c0Position,
            AbstractMaxRPCConstraint c1, int c1Position, boolean useSupports) {
        this.variable = c0.getVariable(c0Position);
        this.c0 = c0;
        this.c0Position = c0Position;
        this.c1 = c1;
        this.c1Position = c1Position;
        this.variable.addClique(this);
        this.homeConstraint = homeConstraint;

        if (useSupports) {
            offset0 = c0.getVariable(1 - c0Position).getOffset();
            offset1 = c1.getVariable(1 - c1Position).getOffset();

            lastPCSupports0 = new int[1
                    + c0.getVariable(1 - c0Position).getSVariable().getSup()
                    - offset0];
            Arrays.fill(lastPCSupports0, Integer.MAX_VALUE);
            lastPCSupports1 = new int[1
                    + c1.getVariable(1 - c1Position).getSVariable().getSup()
                    - offset1];
            Arrays.fill(lastPCSupports1, Integer.MAX_VALUE);
        } else {
            offset0 = offset1 = 0;
            lastPCSupports0 = lastPCSupports1 = null;
        }

    }

    public MaxRPCVariable getVariable() {
        return variable;
    }

    public AbstractMaxRPCConstraint getC0() {
        return c0;
    }

    public AbstractMaxRPCConstraint getC1() {
        return c1;
    }

    public int getC0Position() {
        return c0Position;
    }

    public int getC1Position() {
        return c1Position;
    }

    public AbstractMaxRPCConstraint getHomeConstraint() {
        return homeConstraint;
    }

    /**
     * Cherche un support chemin-consistant pour le couple (v0, v1). v0
     * correspond à la contrainte c0 indiquée à la construction de la 3-clique
     * 
     * @param v0
     * @param v1
     * @return Le premier support trouvé
     */
//     public int findPCSupport(int position, int v0, int v1) {
//        
//     if (position == 0) {
//     t0[1 - c0Position] = v0;
//     t1[1 - c1Position] = v1;
//     } else {
//     t0[1 - c0Position] = v1;
//     t1[1 - c1Position] = v0;
//     }
//        
//     //
//     // i = c0.last[c0Position][v0];
//     // if (variable.getOriginalVariable().canBeInstantiatedTo(i)
//     // && checkPCSupport1(i)) {
//     // return i;
//     // }
//     //
//     // i = c1.last[c0Position][v1];
//     // if (variable.getOriginalVariable().canBeInstantiatedTo(i)
//     // && checkPCSupport0(i)) {
//     // return i;
//     // }
//        
//     final DisposableIntIterator itr = variable.getSVariable().getDomain()
//     .getIterator();
//        
//     while (itr.hasNext()) {
//     final int i = itr.next();
//     if (checkPCSupport0(i) && checkPCSupport1(i)) {
//     itr.dispose();
//     return i;
//     }
//     }
//        
//     itr.dispose();
//     return Integer.MAX_VALUE;
//     }
//     
//
//     private boolean checkPCSupport0(int s) {
//        t0[c0Position] = s;
//        return c0.check(t0);
//    }
//
//    private boolean checkPCSupport1(int s) {
//        t1[c1Position] = s;
//        return c1.check(t1);
//    }
    
    public int findPCSupport(int position, int v0, int v1) {
        final int vv0;
        final int vv1;
        if (position == 0) {
            vv0 = v0;
            vv1 = v1;
        } else {
            vv0 = v1;
            vv1 = v0;
        }
        final AbstractMaxRPCConstraint c0 = this.c0;
        final AbstractMaxRPCConstraint c1 = this.c1;
        final int otherC0Position = 1 - c0Position;
        final int otherC1Position = 1 - c1Position;
        int v21 = c0.firstSupport(otherC0Position, vv0);
        int v22 = c1.firstSupport(otherC1Position, vv1);

        while (v21!= Integer.MAX_VALUE && v22 != Integer.MAX_VALUE)  {
            if (v21 == v22) {
                return v21;
            }
            if (v21 < v22) {
                v21 = c0.nextSupport(otherC0Position, vv0, v22 - 1);
                // if (v21 == Integer.MAX_VALUE) {
                // return Integer.MAX_VALUE;
                // }
            } else {
                v22 = c1.nextSupport(otherC1Position, vv1, v21 - 1);
//                if (v22 == Integer.MAX_VALUE) {
//                    return Integer.MAX_VALUE;
//                }
            }
        } 
        return Integer.MAX_VALUE;

    }


    /**
     * Contrôle si le dernier support de value est présent dans le domaine de la
     * variable et que la valeur est compatible avec la seconde contrainte
     * 
     * @param position
     * @param value
     * @return
     */
    public boolean checkLast(int position, int value) {
        final int last = getLast(position, value);

        return variable.getSVariable().canBeInstantiatedTo(last);
    }

    public int getLast(int position, int value) {
        switch (position) {
        case 0:
            return lastPCSupports0[value - offset0];
        default:
            return lastPCSupports1[value - offset1];
        }
    }

    public void setLast(int position, int value, int support) {
        switch (position) {
        case 0:
            lastPCSupports0[value - offset0] = support;
            break;
        default:
            lastPCSupports1[value - offset1] = support;
        }
    }

    /**
     * Contrôle si le tuple (value, pcSupport) satisfait la contrainte numérotée
     * position
     * 
     * @param position
     * @param value
     * @param pcSupport
     * @return
     */
    public boolean check(int position, int value, int pcSupport) {
        if (position == 0) {
            t0[1 - c0Position] = value;
            t0[c0Position] = pcSupport;
            return c0.check(t0);
        }
        t1[1 - c1Position] = value;
        t1[c1Position] = pcSupport;
        return c1.check(t1);
    }
}
