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

package choco.cp.solver.constraints.global;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;


/**
 * Created by IntelliJ IDEA.
 * User: Ashish
 * Date: Jun 23, 2008
 * Time: 9:12:41 AM
 * Solver constraint of the LexChain constraint.
 * Allows to sort lexical chain with strict lexicographic ordering or not.
 */

public final class LexChain extends AbstractLargeIntSConstraint {


    /**
     * the number of variables in each vector of the chain - v1 <= lexChainEq/lexChain <= v2 .....
     */

    public int n;

    /**
     * array of vectors in the lex chain constraint
     */

    public IntDomainVar [][] x;

    /**
     *    array for holding lexicographically largest feasible  upper bound of each vector
     */


    public int  [][] upperBoundVector;

    /**
     *     array for holding lexicographically smallest  feasible  lower bound of each vector
     */

    public int  [][] lowerBoundVector;

    /**
     * If strict's value is true then  lexChain  is implemented  , if false lexChainEq
     */

    public boolean strict ;

    /**
     * total number of vector in the  lex chain constraint 
     */

    public int numOfVectors;


    /**
     * Constructor for the lex_chain constraint
     * @param vars     an array containing all the variables in all the vectors of variables in the chain of vectors
     * @param n        the number of variables in each vector of the chain - v1 <= lexChainEq/lexChain <= v2 .....
     * @param strict   whether strict lexicographic ordering or not .True value means lexChain { < }  else lexChainEq { <= }
     */

    public  LexChain(IntDomainVar[] vars, int n, boolean strict ) {

        super(ConstraintEvent.LINEAR, vars);
        this.strict = strict;
        this.n = n;
        numOfVectors = vars.length / n;
        x = new IntDomainVar[numOfVectors][n];
        upperBoundVector = new int[numOfVectors][n];
        lowerBoundVector = new int [numOfVectors][n];
        for(int i = 0 ; i< numOfVectors ; i++){
            System.arraycopy(vars, n * i, x[i], 0, n);

        }

    }


    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }

    /**
     * Filtering algorithm for between(a,x,b)
     * Ensures that x is  lexicographically greater than  a and less than  b if strict is false
     * otherwise  x is  lexicographically greater than or equal to    a and less than or equal to   b
     * @param a  lexicographically smallest feasible lower bound
     * @param x  the vector of variables among other vectors in the chain of vectors
     * @param b  lexicographically largest feasible  upper bound
     * @param j  index of the vector x  in  the chain
     * @throws ContradictionException
     */

    public void  boundsLex(int [] a , IntDomainVar [] x , int [] b,int j) throws ContradictionException{

        int i =0;


        while( i< n &&  a[i]==b[i]){


            if((x[i].getInf()==a[i] || x[i].updateInf(a[i], this, false)) &&
                    (x[i].getSup()==b[i] || x[i].updateSup(b[i], this, false))){
                i++;
            }else{
                this.fail();
            }

        }


        if(i<n )
            if ((x[i].getInf()==a[i] || x[i].updateInf(a[i], this, false)) &&
                    (x[i].getSup()==b[i] || x[i].updateSup(b[i], this, false))){
            }else{
                this.fail();
            }


        if(i==n || x[i].getNextDomainValue(a[i])<b[i]){
            return ;
        }

        i+=1;

        while(i<n && (b[i]+1 <= a[i]-1) && x[i].getInf()==b[i] && x[i].getSup()==a[i]){
            if(x[i].removeInterval(b[i]+1,a[i]-1, this, false)){
                i++;
            }else{
                this.fail();

            }
        }

        if(i<n) {
            if (b[i] + 1 <= a[i] - 1 && x[i].getInf() <= b[i] &&
                    b[i] <= x[i].getSup() && x[i].getSup() >= a[i] && a[i] >= x[i].getInf()) {
                if (!x[i].removeInterval(b[i] + 1, a[i] - 1, this, false)) {
                    this.fail();
                }
            }
        }

    }


    /** computes alpha for use in computing lexicographically largest feasible upper  bound  of x in
     * {@link  LexChain#computeUB(choco.kernel.solver.variables.integer.IntDomainVar[], int[], int[]) computUB}
     * @param x   the vector of  variables whose  lexicographically largest feasible  upper bound is to be computed
     * @param b   the vector of integers claimed  to be the feasible upper  bound
     * @return    an integer greater than or equal to  -1 which is used in the computation of   lexicographically smallest  feasible upper bound vector of integers of x
     * @throws ContradictionException
     */


    public int computeAlpha(IntDomainVar [] x ,int [] b )throws ContradictionException {
        int i =0;


        int alpha = -1;


        while(i< n &&  x[i].getInf()<= b[i] && x[i].getSup()>= b[i] && x[i].getDomain().contains(b[i])){

            if(b[i] > x[i].getInf()){
                alpha = i;
            }
            i++;

        }
        if(!strict){
            if( i==n || b[i] > x[i].getInf()){
                alpha = i;
            }
        }else{
            if(i< n && b[i] > x[i].getInf()){
                alpha = i;
            }
        }

        return alpha;
    }


    /** computes beta for use in computing lexicographically smallest feasible lower bound  of x in
     * {@link  LexChain#computeLB(choco.kernel.solver.variables.integer.IntDomainVar[], int[], int[]) computeLB}
     * @param x    the vector of  variables whose  lexicographically smallest feasible lower bound is to be computed
     * @param a    the vector of integers claimed  to be the feasible lower bound
     * @return    an integer greater than or equal to  -1  which is used in the computation of   lexicographically smallest  feasible upper bound vector of integers of x
     * @throws ContradictionException
     */

    public int  computeBeta(IntDomainVar [] x ,int [] a)throws ContradictionException {
        int i =0;
        int beta = -1;
        while(i< n && x[i].getInf()<= a[i] && x[i].getSup()>= a[i] && x[i].getDomain().contains(a[i])){
            if(a[i] < x[i].getSup()){
                beta= i;
            }
            i++;

        }
        if(!strict){
            if( i==n || a[i] < x[i].getSup()){
                beta = i;
            }
        }else{
            if(i<n && a[i] < x[i].getSup()){
                beta = i;
            }
        }
        return beta;



    }


    /**
     * Computes the   lexicographically largest  feasible upper bound vector of integers of x .
     * if aplha computed in  {@link  LexChain#computeAlpha(choco.kernel.solver.variables.integer.IntDomainVar[], int[]) computeAlpha} is -1 then
     * the current domain values  can't satisfy the constraint .So the current intantiations if any are dropped and fresh search is continued.
     * @param x      the vector of  variables whose  lexicographically largest feasible  upper bound is to be computed
     * @param b      the vector of integers claimed  to be the feasible upper  bound
     * @param u       lexicographically largest  feasible upper  bound  of x
     * @throws ContradictionException
     */

    public  void   computeUB(IntDomainVar [] x ,int [] b,int  [] u)throws ContradictionException {

        int  alpha = computeAlpha(x,b);
        if(alpha==-1) this.fail();
        for(int i =0; i<n;i++){
            if(i<alpha){
                u[i]=b[i];
            }else if(i==alpha){
                u[i] = x[i].getPrevDomainValue(b[i]);

            }else {
                u[i] = x[i].getSup();

            }
        }

    }

    /**  Computes the   lexicographically smallest feasible  lower bound vector of integers of x .
     * if beta computed in  {@link  LexChain#computeBeta(choco.kernel.solver.variables.integer.IntDomainVar[], int[]) computeBeta} is -1 then
     * the current domain values  can't satisfy the constraint .So the current intantiations if any are dropped and fresh search is continued.
     *  @param x    the vector of  variables whose  lexicographically smallest feasible
     *                  lower bound is to be computed
     * @param a       the vector of integers claimed  to be the feasible lower bound
     * @param lower   lexicographically smallest feasible lower bound   of x
     * @throws ContradictionException
     */


    public void  computeLB(IntDomainVar [] x ,int [] a,int  [] lower)throws ContradictionException {

        int beta = computeBeta(x,a);
        if(beta==-1) this.fail();
        for(int i =0; i<n;i++){
            if(i<beta){
                lower[i] = a[i];
            }else if(i==beta){
                lower[i] = x[i].getNextDomainValue(a[i]);

            }else {
                lower[i]=  x[i].getInf();

            }
        }

    }

    /**
     * Implements the main filtering algorithm by calling
     * {@link  LexChain#boundsLex(int[], choco.kernel.solver.variables.integer.IntDomainVar[], int[], int) boundsLex} for
     * each vector in the chain.
     * If there is no variable aliasing then the fixed-point is reached in one run.
     * @throws ContradictionException
     */


    public void filter() throws ContradictionException {

        for(int i =0; i< n ;i++)
            upperBoundVector[numOfVectors-1][i] = x[numOfVectors-1][i].getSup();




        for(int i = numOfVectors -2 ; i>=0 ; i--)
            computeUB(x[i],upperBoundVector[i+1],upperBoundVector[i]);



        for(int i =0 ;i< n ;i++)
            lowerBoundVector[0][i] = x[0][i].getInf();




        for(int i =1 ;i<numOfVectors ;i++)
            computeLB(x[i],lowerBoundVector[i-1],lowerBoundVector[i]);



        for(int  i =0 ; i< numOfVectors ; i++)
            boundsLex(lowerBoundVector[i],x[i],upperBoundVector[i],i);





    }


    public void awake() throws ContradictionException {
        filter();
    }

    public Boolean isEntailed() {
        throw new SolverException("isEntailed not yet implemented on choco.cp.cpsolver.constraints.global.LexChain");
    }



    public void propagate() throws ContradictionException {
        filter();
    }


//         public void awakeOnInf(int idx) throws ContradictionException {
//
//         }
//
//         public void awakeOnSup(int idx) throws ContradictionException {
//
//        }
//
//         public void awakeOnRem(int idx, int x) throws ContradictionException {
//
//         }


    public void awakeOnInst(int idx) throws ContradictionException {
        filter();

    }



    public boolean isSatisfied(int[] tuple)
    {
        return checkTuple(0,tuple);
    }
    /**
     * check the feasibility of a tuple, recursively on each pair of consecutive vectors.
     * Compare vector xi with vector x(i+1):
     * return false if xij > x(i+1)j or if (strict && xi=x(i+1)), and checkTuple(i+1, tuple) otherwise.
     * @param i the index of the first vector to be considered
     * @param tuple the instantiation [[x11,..,x1n],[x21..x2n],..,[xk1..xkn]] to be checked
     * @return true iff lexChain(xi,x(i+1)) && lexChain(x(i+1),..,xk)
     */
    private boolean checkTuple(int i, int[] tuple)
    {
        if (i == x.length-1) return true;
        int index = n*i;
        for (int j=0; j < n; j++, index++) {
            if(tuple[index] > tuple[index+n])
                return false;
            if(tuple[index] < tuple[index+n])
                return checkTuple(i+1,tuple);
        }
        return (!strict) && checkTuple(i + 1, tuple);
    }


    /**
     *  Method for printing the constraint in a readable form
     * @return     a readable representation of the constraint as a String
     */

    public String pretty() {
        StringBuffer sb = new StringBuffer();
        for(int i =0;i<numOfVectors;i++){
            for (int j = 0; j < n; j++) {
                sb = j>0?sb.append(", "):sb.append("{");
                sb.append(x[i][j].pretty());
            }
            sb= i+1==numOfVectors?sb.append("} "):strict?sb.append("} < lex "):sb.append("} <= lex ");
        }
        return sb.toString();
    }

}