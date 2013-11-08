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

package choco.kernel.solver.constraints.reified;

import choco.IPretty;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TLongHashSet;

import java.util.ArrayList;
import java.util.logging.Logger;

/*
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 23 avr. 2008
 * Since : Choco 2.0.0
 *
 */
public abstract class INode implements IPretty {

    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * To speed up union operation
     */
    private static final IntDomainVar[] emptyIntArray = new IntDomainVar[0];

    /**
     * reference to branches below this node
     */
    protected INode[] subtrees;

    private final NodeType type;

    public INode(NodeType type) {
        this.type = type;
    }

    public INode(INode[] subt, NodeType type) {
        this(type);
        subtrees = subt;
    }


    public int getNbSubTrees() {
        return subtrees.length;
    }

    public INode getSubtree(int i) {
        return subtrees[i];
    }

    /**
     * Compute the set of solver variable involved in this predicat
     *
     * @param s container solver
     * @return array of variable in the scope
     */
//    public IntDomainVar[] getScope(Solver s) {
//        try {
//            IntDomainVar[] scope = subtrees[0].getScope(s);
//            for (int i = 1; i < subtrees.length; i++) {
//                scope = union(scope, subtrees[i].getScope(s));
//            }
//            return scope;
//        } catch (NullPointerException e) {
//            return null;
//        }
//    }
    public IntDomainVar[] getScope(Solver s) {
        try {
            if(subtrees.length>0){
                final IntDomainVar[][] scopes = new IntDomainVar[subtrees.length][];
                for(int i = 0; i< subtrees.length; i++){
                    scopes[i] = subtrees[i].getScope(s);
                }
                return union(scopes);
            }else{
                return emptyIntArray;
            }
        } catch (NullPointerException e) {
            return null;
        }
    }


    /**
     * Compute the set of model variable involved in this predicat
     *
     * @return
     */
    public IntegerVariable[] getModelScope() {
        if (subtrees == null) {
            return null;
        }
        if (subtrees.length == 1) {
            return subtrees[0].getModelScope();
        } else {
            IntegerVariable[] vars = union(subtrees[0].getModelScope(), subtrees[1].getModelScope());
            for (int i = 2; i < subtrees.length; i++) {
                vars = union(vars, subtrees[i].getModelScope());
            }
            return vars;
        }
    }

    /**
     * set the indexes of each variables in the leaves of the tree
     *
     * @param vs
     */
    public void setIndexes(IntDomainVar[] vs) {
        if (subtrees != null) {
            for (int i = 0; i < subtrees.length; i++) {
                subtrees[i].setIndexes(vs);
            }
        }
    }

    /**
     * Return a table being the union of the two tables given in argument
     *
     * @param arrays first array
     * @return array representing union of the two arrays in parameters
     */
    private IntDomainVar[] union(final IntDomainVar[]... arrays) {
        final TLongHashSet indexes = new TLongHashSet();
        final ArrayList<IntDomainVar> unionset = new ArrayList<IntDomainVar>();
        for (IntDomainVar[] array : arrays) {
            if (array != null) {
                for (IntDomainVar var : array) {
                    if (!indexes.contains(var.getIndex())) {
                        indexes.add(var.getIndex());
                        unionset.add(var);
                    }
                }
            }
        }
        if(unionset.size()>0){
            return unionset.toArray(new IntDomainVar[unionset.size()]);
        }else{
            return emptyIntArray;
        }
    }

    /**
     * Return a table being the union of the two tables given in argument
     *
     * @param t1
     * @param t2
     * @return
     */
    private IntegerVariable[] union(IntegerVariable[] t1, IntegerVariable[] t2) {
        final TLongHashSet indexes = new TLongHashSet((t1==null?0:t1.length) + (t2==null?0:t2.length));
        final IntegerVariable[] unionset = new IntegerVariable[(t1==null?0:t1.length) + (t2==null?0:t2.length)];
        int indice = 0;
        if(t1!=null){
            for (IntegerVariable var : t1) {
                if (!indexes.contains(var.getIndex())) {
                    indexes.add(var.getIndex());
                    unionset[indice++] = var;
                }
            }
        }
        if(t2!=null){
            for (IntegerVariable var : t2) {
                if (!indexes.contains(var.getIndex())) {
                    indexes.add(var.getIndex());
                    unionset[indice++] = var;
                }
            }
        }
        IntegerVariable[] uniontab = new IntegerVariable[indice];
        System.arraycopy(unionset, 0, uniontab, 0, indice);
        return uniontab;
    }


    /**
     * @return a variable that represents the true/false value of the tree belows
     *         this node if it is a reified node or its value if it is an arithmetic node
     */
    public IntDomainVar extractResult(Solver s) {
        return null;
    }

    /**
     * check if this node can be decomposed
     *
     * @return
     */
    public boolean isDecompositionPossible() {
        if (subtrees == null) return true;
        for (int i = 0; i < subtrees.length; i++) {
            if (!subtrees[i].isDecompositionPossible()) {
                return false;
            }
        }
        return true;
    }

    /**
     * check if this expression is reified (involve or, and, not, ...)
     *
     * @return
     */
    public boolean isReified() {
        if (subtrees == null) return false;
        for (int i = 0; i < subtrees.length; i++) {
            if (subtrees[i].isReified()) {
                return true;
            }
        }
        return false;
	}

    public boolean hasOnlyVariablesLeaves() {
        for (int i = 0; i < subtrees.length; i++) {
            if (!subtrees[i].isAVariable())
               return false;
        }
        return true;
    }

    public int countNbVar() {
        int cv = 0;
        for (int i = 0; i < subtrees.length; i++) {
            cv += subtrees[i].countNbVar();
        }
        return cv;
    }

    //*************************************************//
    //**************  identifing nodes ****************//    
    //*************************************************//

    public boolean isAVariable() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isCsteEqualTo(int a) {
		return false;
	}

    public boolean isAConstant() {
        for (int i = 0; i < subtrees.length; i++) {
            if (!(subtrees[i].isAConstant()))
               return false;
        }
        return true;
    }

    public boolean isALinearTerm() {
         return false;
    }

    /**
     * @return the set of coefficients encoding a linear equation
     */
    public int[] computeLinearExpr(int scope) {
        return null;
    }

    //replace all the instanceof of ExpressionDetector by proper types
    public NodeType getType() {
        return type;
    }
}
