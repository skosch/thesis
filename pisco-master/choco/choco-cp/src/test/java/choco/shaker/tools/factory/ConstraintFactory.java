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

import static choco.kernel.common.util.tools.StringUtils.*;
import static choco.Choco.*;
import static choco.Options.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import choco.Choco;
import choco.Options;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.shaker.tools.factory.beta.BetaVariableFactory;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 12 mars 2009
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 */
public class ConstraintFactory {

	OperatorFactory of;
	VariableFactory vf;
	BetaVariableFactory bvf;


	public enum C {
		ALLDIFFERENT, BALLDIFFERENT,
		GCC,
		BGCC_1, //BGCC_2, BGCC_3,
		DISJUNCTIVE_ALL, DISJUNCTIVE_OC, DISJUNCTIVE_NFNL,DISJUNCTIVE_DP, DISJUNCTIVE_EF,
		DISJUNCTIVE_ALT_ALL, DISJUNCTIVE_ALT_OC, DISJUNCTIVE_ALT_NFNL,DISJUNCTIVE_ALT_DP, DISJUNCTIVE_ALT_EF,
		CUMULATIVE, CUMULATIVE_TI, CUMULATIVE_EF, CUMULATIVE_VH, CUMULATIVE_ALL, CUMULATIVE_ALT, CUMULATIVE_ALT_VH, 
		PREC_DISJOINT, PREC_DISJOINT_0, PREC_DISJOINT_1,
		PREC_IMPLIED, PREC_IMPLIED_1,
		PREC_REIFIED, PREC_REIFIED_0, PREC_REIFIED_1,
		//        CUMULATIVE_6, CUMULATIVE_7, CUMULATIVE_8, CUMULATIVE_9,
		//        DIJUNCTIVE_1,  DIJUNCTIVE_2, DIJUNCTIVE_3, DIJUNCTIVE_4,  DIJUNCTIVE_5,
		//        ELEMENT_1, ELEMENT_2, ELEMENT_3, ELEMENT_4, ELEMENT_5,
		LEXLESS, LEXLESSEQ,
		DISTANCE_EQ_1, DISTANCE_EQ_2, DISTANCE_EQ_3,
		DISTANCE_NEQ,
		DISTANCE_GT_1, DISTANCE_GT_2, DISTANCE_GT_3,
		DISTANCE_LT_1, DISTANCE_LT_2, DISTANCE_LT_3,
		MIN_1, MIN_2, MIN_3, MIN_4,
		MAX_1, MAX_2, MAX_3, MAX_4,
		EQ, GEQ, GT, LEQ, LT, NEQ,
		REIFIEDINTCONSTRAINT_1, REIFIEDINTCONSTRAINT_2,
		SIGNOPP, SAMESIGN,
		TRUE, FALSE
	}


	public final static C[] TEMPORAL_SCOPE = new C[] {
		C.PREC_DISJOINT, C.PREC_DISJOINT_0, C.PREC_DISJOINT_1,
		C.PREC_IMPLIED, C.PREC_IMPLIED_1,
		C.PREC_REIFIED, C.PREC_REIFIED_0, C.PREC_REIFIED_1
	};

	public final static C[] DISJ_SCOPE = new C[] {
		C.DISJUNCTIVE_OC, C.DISJUNCTIVE_NFNL, C.DISJUNCTIVE_DP, C.DISJUNCTIVE_EF, C.DISJUNCTIVE_ALL,			
		C.DISJUNCTIVE_ALT_OC, C.DISJUNCTIVE_ALT_NFNL, C.DISJUNCTIVE_ALT_DP //, C.DISJUNCTIVE_ALT_EF, C.DISJUNCTIVE_ALT_ALL
	};

	public final static C[] CUMUL_SCOPE = new C[] {
		C.CUMULATIVE, C.CUMULATIVE_TI, C.CUMULATIVE_EF, C.CUMULATIVE_VH, 
		C.CUMULATIVE_ALT, C.CUMULATIVE_ALT_VH	
	};

	/**
	 * Declare factory dependencies
	 * @param of
	 */
	public void depends(OperatorFactory of, VariableFactory vf){
		this.of = of;
		this.vf = vf;
	}

	public void depends(BetaVariableFactory bvf) {
		this.bvf = bvf;
	}

	public ArrayList<C> scope = new ArrayList<C>();


	/**
	 * Define a specific scope of constraint type to pick up in
	 * @param cs
	 */
	public void scopes(C... cs){
		scope.clear();
		for(int i = 0; i < cs.length; i++){
			scope.add(cs[i]);
		}
	}

	/**
	 * Select randomly (among scope if defined)
	 * and return a constraint type
	 * @param r
	 * @return
	 */
	public C any(Random r) {
		if(scope.size()>0){
			return scope.get(r.nextInt(scope.size()));
		}
		C[] values = C.values();
		return values[r.nextInt(values.length)];
	}

	/**
	 * Make a constraint
	 * @param r
	 * @return
	 */
	public Constraint make(Random r) {
		return make(any(r), r);
	}

	/**
	 * Make a specific constraint
	 * @param c type of constraint
	 * @param r Random
	 * @return a constraint
	 */
	public Constraint make(C c, Random r) {
		//System.out.println(c);
		switch (c) {
		case ALLDIFFERENT:
			return Choco.allDifferent( C_ALLDIFFERENT_AC, of.make(7, r));
		case BALLDIFFERENT:
			return Choco.allDifferent( C_ALLDIFFERENT_BC, of.make(7, r));
		case BGCC_1:
			return makeGcc1(r, C_GCC_BC);
		case CUMULATIVE : 
			return makeCumulative(r, true, false, true);
		case CUMULATIVE_TI : 
			return makeCumulative(r, true, true, true, C_CUMUL_TI);
		case CUMULATIVE_EF : 
			return makeCumulative(r, true, true, true, C_CUMUL_EF);
		case CUMULATIVE_VH:
			return makeCumulative(r, true, false, false);
		case CUMULATIVE_ALL: 
			return makeCumulative(r, false, false, false, C_CUMUL_TI, C_CUMUL_EF);
		case CUMULATIVE_ALT: 
			return makeCumulative(r, false, true, false);
		case CUMULATIVE_ALT_VH:
			return makeCumulative(r, false, false, false);
		case DISJUNCTIVE_OC: 
			return disjunctive(bvf.makeTask(nbTasks(r), r), C_DISJ_OC); 
		case DISJUNCTIVE_NFNL : 
			return disjunctive(bvf.makeTask(nbTasks(r), r), C_DISJ_NFNL); 
		case DISJUNCTIVE_DP : 
			return disjunctive(bvf.makeTask(nbTasks(r), r), C_DISJ_DP);
		case DISJUNCTIVE_EF : 
			return disjunctive(bvf.makeTask(nbTasks(r), r), C_DISJ_EF);
		case DISJUNCTIVE_ALL : 
			return disjunctive(bvf.makeTask(nbTasks(r), r), C_DISJ_NFNL, C_DISJ_DP, C_DISJ_EF, r.nextBoolean() ? C_DISJ_VF : NO_OPTION);
		case DISJUNCTIVE_ALT_OC: { 
			final int n = nbTasks(r);
			return disjunctive(bvf.makeTask(n, r),bvf.makeBool(r.nextInt(n), r), C_DISJ_OC);
		}
		case DISJUNCTIVE_ALT_NFNL : { 
			final int n = nbTasks(r);
			return disjunctive(bvf.makeTask(n, r),bvf.makeBool(r.nextInt(n), r), C_DISJ_NFNL);
		}
		case DISJUNCTIVE_ALT_DP :{ 
			final int n = nbTasks(r);
			return disjunctive(bvf.makeTask(n, r),bvf.makeBool(r.nextInt(n), r), C_DISJ_DP);
		}
		case DISJUNCTIVE_ALT_EF : { 
			final int n = nbTasks(r);
			return disjunctive(bvf.makeTask(n, r),bvf.makeBool(r.nextInt(n), r), C_DISJ_EF);
		}
		case DISJUNCTIVE_ALT_ALL : { 
			final int n = nbTasks(r);
			return disjunctive(bvf.makeTask(n, r),bvf.makeBool(r.nextInt(n), r), C_DISJ_NFNL, C_DISJ_DP, C_DISJ_EF, r.nextBoolean() ? C_DISJ_VF : NO_OPTION);
		}
		case DISTANCE_EQ_1:
			return Choco.distanceEQ(vf.make(r), vf.make(r), r.nextInt(vf.dsize)-vf.dsize/2);
		case DISTANCE_EQ_2:
			return Choco.distanceEQ(vf.make(r), vf.make(r), vf.make(r));
		case DISTANCE_EQ_3:
			return Choco.distanceEQ(vf.make(r), vf.make(r), vf.make(r), r.nextInt(vf.dsize)-vf.dsize/2);
		case DISTANCE_GT_1:
			return Choco.distanceGT(vf.make(r), vf.make(r), r.nextInt(vf.dsize)-vf.dsize/2);
		case DISTANCE_GT_2:
			return Choco.distanceGT(vf.make(r), vf.make(r), vf.make(r));
		case DISTANCE_GT_3:
			return Choco.distanceGT(vf.make(r), vf.make(r), vf.make(r), r.nextInt(vf.dsize)-vf.dsize/2);
		case DISTANCE_LT_1:
			return Choco.distanceLT(vf.make(r), vf.make(r), r.nextInt(vf.dsize)-vf.dsize/2);
		case DISTANCE_LT_2:
			return Choco.distanceLT(vf.make(r), vf.make(r), vf.make(r));
		case DISTANCE_LT_3:
			return Choco.distanceLT(vf.make(r), vf.make(r), vf.make(r), r.nextInt(vf.dsize)-vf.dsize/2);
		case DISTANCE_NEQ:
			return Choco.distanceNEQ(vf.make(r), vf.make(r), r.nextInt(vf.dsize)-vf.dsize/2);
		case EQ:
			return Choco.eq(of.make(r), of.make(r));
		case FALSE:
			return Choco.FALSE;
		case GCC:
			return makeGcc1(r, C_GCC_AC);
		case GEQ:
			return Choco.geq(of.make(r), of.make(r));
		case GT:
			return Choco.gt(of.make(r), of.make(r));
		case LEQ:
			return Choco.leq(of.make(r), of.make(r));
		case LEXLESS:
			return Choco.lex(of.make(5, r), of.make(5, r));
		case LEXLESSEQ:
			return Choco.lexEq(of.make(5, r), of.make(5, r));
		case LT:
			return Choco.lt(of.make(r), of.make(r));
		case MAX_1:
			return Choco.max(vf.make(7, r), vf.make(r));
		case MAX_2:
			return Choco.max(vf.make(r), vf.make(r), vf.make(r));
		case MAX_3:
			return Choco.max(r.nextInt(vf.dsize)-vf.dsize/2, vf.make(r), vf.make(r));
		case MAX_4:
			return Choco.max(vf.make(r), r.nextInt(vf.dsize)-vf.dsize/2, vf.make(r));
		case MIN_1:
			return Choco.min(vf.make(7, r), vf.make(r));
		case MIN_2:
			return Choco.min(vf.make(r), vf.make(r), vf.make(r));
		case MIN_3:
			return Choco.min(r.nextInt(vf.dsize)-vf.dsize/2, vf.make(r), vf.make(r));
		case MIN_4:
			return Choco.min(vf.make(r), r.nextInt(vf.dsize)-vf.dsize/2, vf.make(r));
		case NEQ:
			return Choco.neq(of.make(r), of.make(r));
		case PREC_DISJOINT : 
			return makePrecDisjoint(r, bvf.makeBool(r));
		case PREC_DISJOINT_0 : 
			return makePrecDisjoint(r, Choco.ZERO);
		case PREC_DISJOINT_1 : 
			return makePrecDisjoint(r, Choco.ONE);
		case PREC_IMPLIED : 
			return makePrecImplied(r, bvf.makeBool(r));
		case PREC_IMPLIED_1 : 
			return makePrecImplied(r, Choco.ONE);
		case PREC_REIFIED : 
			return makePrecReified(r, bvf.makeBool(r));
		case PREC_REIFIED_0 : 
			return makePrecReified(r, Choco.ZERO);
		case PREC_REIFIED_1 : 
			return makePrecReified(r, Choco.ONE);
		case
		REIFIEDINTCONSTRAINT_1:
			return Choco.reifiedConstraint(vf.make(VariableFactory.V.BOOLVAR, r), make(r));
		case REIFIEDINTCONSTRAINT_2:
			return Choco.reifiedConstraint(vf.make(VariableFactory.V.BOOLVAR, r), make(r), make(r));
		case SIGNOPP:
			return Choco.oppositeSign(of.make(r), of.make(r));
		case SAMESIGN:
			return Choco.sameSign(of.make(r), of.make(r));
		case TRUE:
			return Choco.TRUE;
		}
		return null;
	}

	/**
	 * Create a globalcardinality constraint of type 1
	 * @param r Random
	 * @param option "cp:ac" or "cp:bc"
	 * @return gcc
	 */
	private Constraint makeGcc1(Random r, String option) {
		IntegerVariable[] vars = vf.make(5, r);
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for(IntegerVariable v : vars){
			min = Math.min(min, v.getLowB());
			max = Math.max(max, v.getUppB());
		}
		int[] low = new int[max-min+1];
		int[] up = new int[max-min+1];

		Arrays.fill(low, 0);
		Arrays.fill(up, vars.length);

		// Fill low array with value that do not exceed n
		int n = vars.length;
		while(n>0){
			int i = next(low, 0, r);
			int v = r.nextInt(n+1);
			low[i] = v;
			n-= v;
		}
		n = vars.length;
		while(n>0){
			int i = next(up, vars.length, r);
			int v = Math.max(low[i], r.nextInt(n+1));
			up[i] = v;
			n-= v;
		}
		//        if(minMax){
		//            return Choco.globalCardinality(option, vars, min, max, low, up);
		//        }
		return Choco.globalCardinality(option, vars, low, up, min);
	}

	private Constraint makeCumulative(Random r, boolean regular, boolean constHeight, boolean noCons, String... options) {
		final int n = 3 + r.nextInt(10);
		final TaskVariable[] tasks = bvf.makeTask(n, r);
		final IntegerVariable[] heights = constHeight ? bvf.makeConst(n, r) : bvf.make(n, r);
		final IntegerVariable[] usages = regular ? null : bvf.makeBool(r.nextInt(n), r);
		int capa = Integer.MIN_VALUE;
		for (IntegerVariable h : heights) {
			if(h.getUppB() > capa) capa = h.getUppB();
		}
		return Choco.cumulative(randomName(), 
				tasks, heights, usages,
				constant(noCons ? 0 : r.nextInt(MAX_CONS)), constant(capa), options
		);
	}

	private final static int MAX_CONS = 4;
	
	private final static int MAX_SETUP = 4;

	private final static int MIN_NB_TASKS= 3;
	
	private final static int MAX_NB_TASKS= 10;


	private int setup(Random r) {
		return r.nextInt(MAX_SETUP);
	}

	private int nbTasks(Random r) {
		return MIN_NB_TASKS + r.nextInt(MAX_NB_TASKS - MIN_NB_TASKS);
	}


	private Constraint makePrecDisjoint(Random r, IntegerVariable dir) {
		return Choco.precedenceDisjoint(bvf.makeTask(r), bvf.makeTask(r), dir, setup(r), setup(r));
	}

	private Constraint makePrecImplied(Random r, IntegerVariable dir) {
		return Choco.precedenceImplied(bvf.makeTask(r), setup(r), bvf.makeTask(r), dir);
	}

	private Constraint makePrecReified(Random r, IntegerVariable dir) {
		return Choco.precedenceReified(bvf.makeTask(r), setup(r), bvf.makeTask(r), dir);
	}



	/**
	 * Retrieve the next value in an array, where the cell is different from diffn
	 * @param vals array of values
	 * @param diffn the value to avoid
	 * @param r Random
	 * @return the next int value
	 */
	private int next(int[] vals, int diffn, Random r){
		int i = r.nextInt(vals.length);
		while(vals[i]!=diffn){
			i = r.nextInt(vals.length);
		}
		return i;
	}

	/**
	 * Make an array of constraints
	 * @param nb number of constraints to create
	 * @param r Random
	 * @return array of constraints
	 */
	public Constraint[] make(int nb, Random r) {
		Constraint[] cs = new Constraint[nb];
		for(int i = 0 ; i < nb; i++){
			cs[i] = make(r);
		}
		return cs;
	}

	/**
	 * Make an array of specific constraints
	 * @param nb number of constraints to create
	 * @param c type of constraint to create
	 * @param r Random
	 * @return array of specific constraint
	 */
	public Constraint[] make(int nb, C c, Random r) {
		Constraint[] cs = new Constraint[nb];
		for(int i = 0 ; i < nb; i++){
			cs[i] = make(c, r);
		}
		return cs;
	}




}
