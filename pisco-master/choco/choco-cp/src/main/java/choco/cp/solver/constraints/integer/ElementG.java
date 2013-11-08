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

package choco.cp.solver.constraints.integer;


import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

public final class ElementG extends AbstractBinIntSConstraint {
    int[] lval;
    protected IStateInt lastIndexInf, lastIndexSup, lastVarInf, lastVarSup;
    protected IStateInt[] domainSize;
    private final IEnvironment environment;


    public ElementG(final IntDomainVar index, final int[] values, final IntDomainVar var, final IEnvironment environment) {
        super(index, var);
        this.environment = environment;
        this.lval = values;
        this.domainSize = new IStateInt[2];
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String toString() {
        return "ElementG";
    }

    public String pretty() {
        return (this.v1.toString() + " = nth(" + this.v0.toString() + ", " + StringUtils.pretty(this.lval) + ')');
    }

    /* we never know from what value we come .. so we store it in lastIndex or lastVar to propagate exactly the right removed values
because we are in a pure removeValue environnement */
    public void awakeOnInf(final int i) throws ContradictionException {
        if (i == 0) {
            final int minIndex = this.v0.getInf();
            for (int index = this.lastIndexInf.get(); index < minIndex; index++) {
                this.updateValueFromIndex(index);
            }
            this.lastIndexInf.set(minIndex);
        } else {
            final int minVar = this.v1.getInf();
            for (int index = this.lastVarInf.get(); index < minVar; index++) {
                this.updateIndexFromValue(index);
            }
            this.lastVarInf.set(minVar);
        }
    }

    /* same idea than for awakeOnInf */
    public void awakeOnSup(final int i) throws ContradictionException {
        if (i == 0) {
            int maxIndex = this.lastIndexSup.get();
            //[EBo] bug on LastIndexSup=lval.length = maxIndexVal + 1 !
            if (maxIndex >= this.lval.length) {
                maxIndex = this.lval.length - 1;
            }
            for (int index = this.v0.getSup() + 1; index <= maxIndex; index++) {
                this.updateValueFromIndex(index);
            }
            this.lastIndexSup.set(this.v0.getSup());
        } else {
            final int maxVar = this.lastVarSup.get();
            for (int index = this.v1.getSup() + 1; index <= maxVar; index++) {
                this.updateIndexFromValue(index);
            }
            this.lastVarSup.set(this.v1.getSup());
        }
    }

    public void awakeOnBounds(final int varIdx) throws ContradictionException {
        //Change if necessary
        awakeOnInf(varIdx);
        awakeOnSup(varIdx);
    }

    public void awakeOnInst(final int i) throws ContradictionException {
        if (i == 0) {
            if (this.v0.getVal() - 1 < this.lval.length)
                this.v1.instantiate(this.lval[this.v0.getVal() - 1], this, false);
        } else {
            final int maxVar = this.lastVarSup.get();
            for (int index = this.v1.getVal(); index < maxVar; index++) {
                this.updateIndexFromValue(index + 1);
            }
            for (int index = this.lastVarInf.get(); index < this.v1.getVal(); index++) {
                this.updateIndexFromValue(index);
            }
            this.lastVarInf.set(this.v1.getVal());
            this.lastVarSup.set(this.v1.getVal());
        }
    }

    public void awakeOnRem(final int i, final int x) throws ContradictionException {
        if (i == 0)
            this.updateValueFromIndex(x);
        else
            this.updateIndexFromValue(x);
    }

    public Boolean isEntailed() {
        if (this.v1.isInstantiated()) {
            boolean b = true;
            final DisposableIntIterator iter = this.v0.getDomain().getIterator();
            for (; iter.hasNext();) {
                final int val = iter.next();
                b &= (val) >= 0;
                b &= (val) < this.lval.length;
                b &= this.lval[val] == this.v1.getVal();
            }
            iter.dispose();
            if (b) return Boolean.TRUE;
        } else {
            boolean b = false;
            final DisposableIntIterator iter = this.v0.getDomain().getIterator();
            while (iter.hasNext() && !b) {
                final int val = iter.next();
                b = (val) >= 0;
                b &= (val) < this.lval.length;
                b &= this.v1.canBeInstantiatedTo(this.lval[val]);
            }
            iter.dispose();
            if (b) return null;
        }
        return Boolean.FALSE;
    }

    public boolean isSatisfied() {
        return this.lval[this.v0.getVal()] == this.v1.getVal();
    }

    public void propagate() throws ContradictionException {

        awakeOnInf(0);
        awakeOnInf(1);
        awakeOnSup(0);
        awakeOnSup(1);
        // je ne sais pas trop la nuance du propagate et des awake specifique donc je fais pas la GAC complete !
    }

    /* je raisonne toujours avec la contrainte element(Var,Index,Tableau) ou Index commence à 1 !
      Nous allons utiliser 4 structures de données LOCALES
      -	un tableau value recensant la liste des valeurs rencontrées lors du balayage de Tableau.
        Le i ième rang du tableau possède la i ième valeur observée lors du balayage,
      -	Un tableau occur maintenant le nombre d’occurrence de la i ième valeur observée dans Tableau,
      -	Un tableau firstPos maintenant l’indice de la première position de la i ième valeur observée dans Tableau,
      -	Un tableau redirect englobant le domaine possible de Variable indiquant l’indice de redirection de la valeur v
        dans le tableau de recensement des valeurs observées. Ce tableau étant initialisé à 0, si la valeur n’a pas été
        observée, son indice de redirection reste 0.
      Aprés une première passe pour alimenter tout cela en O(2 x size(Index) + 1 x size(Var)), nous cherchons les cas
      d'incohérences afin de mettre à jour les variables index et var :
          Cas 0-1-1
            Une valeur v, d’indice i est dans Tableau, i est dans le domaine de Index mais v n’est pas dans
            le domaine de Var => il faut retirer i du domaine de Index
        Cas 1-0-1
            Une valeur v, d’indice i est dans Tableau, v est aussi dans le domaine de Var mais i n’est pas
            dans le domaine de Index => il faut retirer v du domaine de Var
        Cas 1-0-0
            Une valeur v est dans le domaine de Var mais n’apparaît pas dans le Tableau (donc n’est pas d’indice
            dans Index) => il faut retirer v du domaine de Var
        (les autres cas ne sont pas intéressants : 0-0-0 ne conduit à aucune modification, le cas 0-0-1 ne conduit
         à aucune mise à jour, le cas 0-1-0 est impossible car si i est dans Index alors il existe une valeur dans
         Tableau, le cas 1-1-0 est impossible et le cas 1-1-1 ne conduit à aucune mise à jour)
    */
    public void awake() throws ContradictionException {
        final int[] value;
        final int[] occur;
        final int[] firstPos;
        final int[] redirect;

        /* On précalcule la taille minimale du tableau redirect */
        int heigth = lval[0]; // length of redirect (use first as a max value)
        int offset = lval[0]; // starting value in redirect
        for (int i = 1; i < this.lval.length; i++) {
            if (heigth < lval[i]) {
                heigth = lval[i];
            }
            if (offset > lval[i]) {
                offset = lval[i];
            }
        }

        heigth = heigth - offset + 1; // take the right heigth

        value = new int[this.lval.length];
        occur = new int[this.lval.length];
        firstPos = new int[this.lval.length];
        redirect = new int[heigth];

        /*Initialisation de redirect */
        for (int i = 0; i < heigth; i++) {
            redirect[i] = -1;
        }
        /* comptage de chaque occurrence de chaque valeur dans Tableau */
        int nbVal = 0;
        for (int i = 0; i < this.lval.length; i++) {
            if (redirect[this.lval[i] - offset] == -1) { /* nouvelle valeur */
                value[nbVal] = this.lval[i];
                redirect[this.lval[i] - offset] = nbVal;
                occur[nbVal] = 1;
                firstPos[nbVal] = i + 1;
                nbVal = nbVal + 1;
            } else {    /* valeur existante */
                occur[redirect[this.lval[i] - offset]] = occur[redirect[this.lval[i] - offset]] + 1;
            }
        }

        /* Update Index via Var = cas 0-1-1 :
          v in Tableau, i in Index but v not in Var => remove I from Index */
        final DisposableIntIterator iter = this.v0.getDomain().getIterator();
        int left = Integer.MIN_VALUE;
        int right = left;
        try {
            for (; iter.hasNext();) {
                final int index = iter.next();
                // bug "index - 1 > 0" change to "index - 1 >= 0"  [EBo 6/4/8]
                if ((index - 1 < this.lval.length) && (index - 1 >= 0) && (!this.v1.canBeInstantiatedTo(this.lval[index - 1]))) {
                    if (index == right + 1) {
                        right = index;
                    } else {
                        v0.removeInterval(left, right, this, false);
                        left = index;
                        right = index;
                    }
//                    this.v0.removeVal(index, this, false);
                }
            }
            v0.removeInterval(left, right, this, false);
        } finally {
            iter.dispose();
        }

        /* update Var via Index = cas 1-0-1 :
          v in Tableau, i not in Index => remove v from Var si occur = 1 sinon update FirstPos */
        left = right = Integer.MIN_VALUE;
        for (int i = 0; i < nbVal; i++) {
            int val = value[i];
            if (this.v1.canBeInstantiatedTo(val)) {
                while ((occur[i] > 1) && (!this.v0.canBeInstantiatedTo(firstPos[i]))) {
                    occur[i] = occur[i] - 1;
                    int j = firstPos[i] + 1;
                    while (this.lval[j - 1] != val) {
                        j = j + 1;
                    }
                    firstPos[i] = j;
                }
                if ((occur[i] == 1) && (!this.v0.canBeInstantiatedTo(firstPos[i]))) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        v0.removeInterval(left, right, this, false);
                        left = val;
                        right = val;
                    }
//                    this.v1.removeVal(val, this, false);

                }       /* sinon, firstPos est mis a une véritable première position de v , cas 1-1-1 */
            }
        }
        v0.removeInterval(left, right, this, false);

        /* Elegage des bornes d'Index et enregistrement des premieres valeurs pour LastIndex */
        if (this.v0.getInf() < 1) {
            this.v0.updateInf(1, this, false);
            this.lastIndexInf = environment.makeInt(1);
        } else {
            this.lastIndexInf = environment.makeInt(this.v0.getInf());
        }
        if (this.lval.length < this.v0.getSup()) {
            this.v0.updateSup(this.lval.length, this, false);
            this.lastIndexSup = environment.makeInt(this.lval.length);
        } else {
            this.lastIndexSup = environment.makeInt(this.v0.getSup());
        }

        /* Elegage des bornes de Var et enregistrement des premieres valeurs pour LastVar */
        if (offset > this.v1.getInf()) {
            this.v1.updateInf(offset, this, false);
            this.lastVarInf = environment.makeInt(offset);
        } else {
            this.lastVarInf = environment.makeInt(this.v1.getInf());
        }
        if (this.v1.getSup() > (heigth + offset - 1)) {
            this.v1.updateSup(heigth + offset - 1, this, false);
            this.lastVarSup = environment.makeInt(heigth + offset - 1);
        } else {
            this.lastVarSup = environment.makeInt(this.v1.getSup());
        }

        /* update Var via Tableau = cas 1-0-0 :
                v in Var but not in Tableau => remove v from Var */
        left = right = Integer.MIN_VALUE;
        for (int i = offset; i < heigth + offset - 1; i++) { /* on balaie les valeurs restantes de Var */
            if (this.v1.canBeInstantiatedTo(i) && (redirect[i - offset] == -1)) {
                if (i == right + 1) {
                    right = i;
                } else {
                    v0.removeInterval(left, right, this, false);
                    left = i;
                    right = i;
                }
//                this.v1.removeVal(i, this, false);
            }
        }
        v0.removeInterval(left, right, this, false);

        this.domainSize[0] = environment.makeInt(v0.getDomainSize());
        this.domainSize[1] = environment.makeInt(v1.getDomainSize());
    }


    /* update the Var variable when i was remove from Index */
    /* first catch the potential value "val" to remove and then search if Tableau[i] was the only occurence of val ... then remove */
    protected void updateValueFromIndex(final int i) throws ContradictionException {
        if ((v0.getDomainSize() == this.domainSize[0].get())) {
            // already test in a previous awake !
        } else if (i < this.lval.length) {  // [Ebo] Bug when maxIndex = length = maxValIndex + 1 ! (due to start at 1 insteand of 0)
            int val = 0;
            if (i > 0) {
                val = this.lval[i - 1];
            } else {
                fail();
            }
            int j = this.v0.getInf();

//		  if ((i == 13) && (val == 4) && (v1DomainSize == 4) && (v0DomainSize == 36) && (j == 2) && (this.domainSize[0].get() == 52)) {
//			  val = val;
//		  };
            // bug (as in awake)
//	      while ((j <= this.v0.getSup()) && (j <= this.lval.length) && ((j == i) || ((j-1 > 0) && (this.lval[j-1] != val)) || (this.v0.canBeInstantiatedTo(j) == false))) {			  
            while ((j <= this.v0.getSup()) && (j <= this.lval.length) && ((j == i) || ((j - 1 >= 0) && (this.lval[j - 1] != val)) || (!this.v0.canBeInstantiatedTo(j)))) {
                j = j + 1;
            }
//	      if (j > this.lval.length) {  // Bug EBo 6/4/8 on testait max de lval mais pas max de dom(Index)
            if ((j > this.lval.length) || (j > this.v0.getSup())) {
                if (this.v1.canBeInstantiatedTo(val)) {
                    this.v1.removeVal(val, this, false);
                    this.domainSize[1].set(v1.getDomainSize());
                }
            }
        }
    }

    /* update Index from the value v removed from Var */
    /* remove all the index where v was the value un Tableau */
    protected void updateIndexFromValue(final int v) throws ContradictionException {
        if ((v1.getDomainSize() == this.domainSize[1].get())) {
            // already test in a previous awake !
        } else {
            final DisposableIntIterator iter = this.v0.getDomain().getIterator();
            int left = Integer.MIN_VALUE;
            int right = left;
            try {
                for (; iter.hasNext();) {
                    final int index = iter.next();
//		      if ((index - 1 < this.lval.length) && (index - 1 > 0) && (this.lval[index-1] == v)) {				  
                    if ((index - 1 < this.lval.length) && (index - 1 >= 0) && (this.lval[index - 1] == v)) {
                        if (this.v0.canBeInstantiatedTo(index)) {
                            if (index == right + 1) {
                                right = index;
                            } else {
                                v0.removeInterval(left, right, this, false);
                                left = index;
                                right = index;
                            }
//                            this.v0.removeVal(index, this, false);
//                            this.domainSize[0].set(v0.getDomainSize());
                        }
                    }
                }
                v0.removeInterval(left, right, this, false);
                this.domainSize[0].set(v0.getDomainSize());
            } finally {
                iter.dispose();
            }
        }
    }
}
