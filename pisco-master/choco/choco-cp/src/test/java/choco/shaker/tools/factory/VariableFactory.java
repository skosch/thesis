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

package choco.shaker.tools.factory;

import choco.Choco;
import choco.Options;
import choco.kernel.model.ModelException;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.shaker.tools.factory.beta.IVariableFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 12 mars 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class VariableFactory implements IVariableFactory<IntegerVariable> {

    public ArrayList<IntegerVariable> pool;

    public ArrayList<IntegerVariable> created = new ArrayList<IntegerVariable>();

    public ArrayList<V> scope = new ArrayList<V>();

    int id = 0;

    /**
     * specify the maximum number of created variables
     */
    int maxcreation=30;
    public int dsize = 10;

    public enum V {
        BOOLVAR, CST, ENUMVAR, BOUNDVAR, BTREEVAR, BLISTVAR, LINKVAR, UNBOUNDED
    }

    
    /**
     * Define a specific scope of variable to pick up in
     * @param variables the pool of variables
     */
    public void definePool(IntegerVariable... variables){
        this.pool = new ArrayList<IntegerVariable>();
        this.pool.addAll(Arrays.asList(variables));
    }

    /**
     * Set a maximum number of created variables
     * @param n the maximum number of created variables
     */
    public void setMaxCreated(int n){
        maxcreation = n;
    }

    /**
     * Set a maximum domain size
     * @param n the domain size
     */
    public void setMaxDomSize(int n){
        dsize = n;
    }

    /**
     * Define a specific scope of variable tupe to pick up in
     * @param vs the scope of variables
     */
    public void scopes(V... vs){
        scope.clear();
        scope.addAll(Arrays.asList(vs));
    }

    /**
     * Select randomly (among scope if defined)
     * and return a variable type
     * @param r random
     * @return type of variable
     */
    public V any(Random r) {
        if(scope.size()>0){
            return scope.get(r.nextInt(scope.size()));
        }
        V[] values = V.values();
        return values[r.nextInt(values.length)];
    }


    /**
     * Get one variable among all
     * @param r random
     * @return IntegerVariable
     */
    public IntegerVariable make(Random r){
        return make(any(r), r);
    }


    /**
     * Create and return the corresponding variable
     * @param v the type of variable
     * @param r random
     * @return IntegerVariable
     */
    public IntegerVariable make(V v, Random r) {
        // If there is a restricted pre-defined pool of variables
        // return one of them
        if(this.pool!=null){
            return this.pool.get(r.nextInt(this.pool.size()));
        }
        // If the number of new variable has been reached
        // return one of them
        if(created.size() >= maxcreation){
            return created.get(r.nextInt(created.size()));
        }
        //Otherwise, create a new variable
        IntegerVariable var = null;
        id++;
        int low, upp;
        switch (v) {
            case BOOLVAR:
                var = Choco.makeBooleanVar("b_"+id);
                break;
            case ENUMVAR:
                upp = r.nextInt(dsize);
                low = upp - r.nextInt(dsize);
                var = Choco.makeIntVar("v_"+id, low, upp, Options.V_ENUM);
                break;
            case BOUNDVAR:
                upp = r.nextInt(dsize);
                low = upp - r.nextInt(dsize);
                var = Choco.makeIntVar("v_"+id, low, upp, Options.V_BOUND);
                break;
            case BTREEVAR:
                upp = r.nextInt(dsize);
                low = upp - r.nextInt(dsize);
                var = Choco.makeIntVar("v_"+id, low, upp, Options.V_BTREE);
                break;
            case BLISTVAR:
                upp = r.nextInt(dsize);
                low = upp - r.nextInt(dsize);
                var = Choco.makeIntVar("v_"+id, low, upp, Options.V_BLIST);
                break;
            case LINKVAR:
                upp = r.nextInt(dsize);
                low = upp - r.nextInt(dsize);
                var = Choco.makeIntVar("v_"+id, low, upp, Options.V_LINK);
                break;
            case UNBOUNDED:
                var = Choco.makeIntVar("v_"+id);
                break;
            case CST:
                int val = r.nextInt(dsize)-dsize/2;
                var = Choco.constant(val);
                break;
        }
        created.add(var);
        return var;
    }


    /**
     * Get an array of variables
     * @param nb number of variables to create
     * @param r random
     * @return array of IntegerVariables
     */
    public IntegerVariable[] make(int nb, Random r){
        IntegerVariable[] variables = new IntegerVariable[nb];
        for (int i = 0; i < variables.length; i++) {
            variables[i] = make(r);
        }
        return variables;
    }

    /**
     * Get an array of variables
     * @param nb number of variables to create
     * @param v the type of variable
     * @param r random
     * @return array of variables
     */
    public IntegerVariable[] make(int nb, V v, Random r){
        IntegerVariable[] variables = new IntegerVariable[nb];
        for (int i = 0; i < variables.length; i++) {
            variables[i] = make(v, r);
        }
        return variables;
    }

	@Override
	public void addScope(String... options) {
		throw new ModelException("Not Implemented");
		
	}

	@Override
	public void cancelScope() {
		scope.clear();
		
	}

	@Override
	public void cancelValueOffset() {
		throw new ModelException("Not Implemented");
	}

	@Override
	public void clearPool() {
		pool.clear();
		
	}

	@Override
	public IntegerVariable[] make(String option, int nb, Random r) {
		throw new ModelException("Not Implemented");
	}

	@Override
	public IntegerVariable make(String option, Random r) {
		throw new ModelException("Not Implemented");
	}

	@Override
	public void remScope(String... options) {
		throw new ModelException("Not Implemented");
	}

	@Override
	public void setScope(String... options) {
		throw new ModelException("Not Implemented");
		
	}

	@Override
	public void setValueOffset(int valOffset) {
		throw new ModelException("Not Implemented");
	}

}
