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

package choco.kernel.model.constraints;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 18 mars 2008
 * Time: 09:30:58
 * Define every type of constraint that exist in Choco API.
 */
public enum ConstraintType {
    ABS("abs", "constraint.abs", false),
    ALLDIFFERENT("allDifferent", "constraint.allDifferent", false),
    AMONGSET("among_set", "constraint.amongset", false),
    AND("and", "constraint.and", true),
    AROUND("around"),
    ATMOSTNVALUE("atMostNValue", "constraint.atMostNValue", false),
    //BATCH("batchresource", "constraint.batchresource"),
    CHANNELING("channeling", "constraint.channeling", false),
    CLAUSES("clauses", "constraint.clauses", false),
    COMPLEMENTSET("complementSet", "constraint.complementset", false),
    CST("cst"),
    COSTKNAPSACK("costknapsack", "constraint.costknapsack", false),
    COSTREGULAR("costregular", "constraint.costregular", false),
    CUMULATIVE("cumulative", "constraint.cumulative", false),
    DISJUNCTIVE("disjunctive", "constraint.disjunctive", false),
    DISTANCE("distance", "constraint.distance", true),
    EQ("eq", "constraint.eq", true),
    EUCLIDEANDIVISION("div", "constraint.div", false),
    EXACTLY("exactly", "constraint.exactly", false),
    EXPRESSION("expression"),
    FALSE("false", "constraint.false", false),
    FASTREGULAR("fastregular", "constraint.fastregular", false),
    FORBIDDEN_INTERVALS("forbidden intervals", "constraint.forbiddenIntervals", false),
    GEOST("geost", "constraint.geost", false),
    GEQ("geq", "constraint.geq", true),
    GLOBALCARDINALITY("globalCardinaly", "constraint.globalCardinaly", false),
    GLOBALCARDINALITYMAX("globalCardinaly max", "constraint.globalCardinaly", false),
    GLOBALCARDINALITYVALUES("globalCardinaly values", "constraint.globalCardinaly", false),
    GLOBALCARDINALITYVAR("globalCardinaly var", "constraint.globalCardinaly", false),
    GLOBALCARDINALITYVARVALUES("globalCardinaly var", "constraint.globalCardinaly", false),
    GT("gt", "constraint.gt", true),
    IFONLYIF("ifonlyif", "constraint.ifonlyif", false),
    IFTHENELSE("ifthenelse", "constraint.ifthenelse", true),
    IMPLIES("implies", "constraint.implies", false),
    INVERSECHANNELING("inverse channeling", "constraint.channeling", false),
    INVERSECHANNELINGWITHINRANGE("inverse_channeling_with_range", "constraint.channeling", false),
    INCREASINGNVALUE("increasing n value", "constraint.increasingnvalue", false),
    INCREASINGSUM("increasing_sum", "constraint.increasingsum", false),
    DOMAIN_CHANNELING("domain channeling", "constraint.channeling", false),
    INVERSE_SET("inverse set", "constraint.inverseset", false),
    ISINCLUDED("isIncluded", "constraint.isIncluded", false),
    ISNOTINCLUDED("isNotIncluded", "constraint.isNotIncluded", false),
    LEQ("leq", "constraint.leq", true),
    LEX("lex", "constraint.lex", false),
    LEXEQ("lexeq", "constraint.lex", false),
    LEXCHAIN("lexChain", "constraint.lexChain", false),
    LEXIMIN("leximin", "constraint.leximin", false),
    LT("lt", "constraint.lt", true),
    MAX("max", "constraint.max", false),
    MEMBER("member", "constraint.member", false),
    INTMEMBER("member", "constraint.intmember", false),
    INTNOTMEMBER("not member", "constraint.intnotmember", false),
    MIN("min", "constraint.min", false),
    MOD("mod", "constraint.mod", false),
    MULTICOSTREGULAR("fast_multicostregular", "constraint.multicostregular", false),
    NAND("nand", "constraint.nand", true),
    NEQ("neq", "constraint.neq", true),
    NONE("none"),
    NOT("not", "constraint.not", true),
    NOTMEMBER("notMember", "constraint.notMember", false),
    NOR("nor", "constraint.nor", true),
    NTH("nth", "constraint.nth", false),
    OCCURRENCE("occurence", "constraint.occurence", false),
    OR("or", "constraint.or", true),
    PACK("binpacking1D", "constraint.binpacking1D", false),
    //PERT("pertprecedence", "constraint.pertprecedence"),
    PRECEDENCE_REIFIED("precedence reified", "constraint.precedencereified", false),
    PRECEDENCE_IMPLIED("precedence implied", "constraint.precedenceimplied", false),
    PRECEDENCE_DISJOINT("precedence disjoint", "constraint.precedencedisjoint", false),
    REGULAR("regular", "constraint.regular", false),
    REIFIEDAND("reifiedAnd", "constraint.reifiedAnd", false),
    REIFIEDIMPLICATION("reifiedImplication", "constraint.reifiedImplication", false),
    REIFIEDCONSTRAINT("reifiedconstraint", "constraint.reifiedconstraint", false),
    REIFIEDNAND("reifiedNand", "constraint.reifiedNand", false),
    REIFIEDNOR("reifiedNor", "constraint.reifiedNor", false),
    REIFIEDOR("reifiedOr", "constraint.reifiedOr", false),
    REIFIEDXNOR("reifiedXnor", "constraint.reifiedXnor", false),
    REIFIEDXOR("reifiedXor", "constraint.reifiedXor", false),
    SETDISJOINT("setDisjoint", "constraint.setDisjoint", false),
    SETINTER("setInter", "constraint.setInter", false),
    SETLEXICOGRAPHICORDERING("setLex", "constraint.setlex", false),
    SETUNION("union", "constraint.union", false),
    SETVALUEPRECEDE("set value precede", "constraint.setvalueprecede", false),
    SIGNOP("signop", "constraint.signop", true),
    SOFTMULTICOSTREGULAR("soft_multicostregular", "constraint.softmulticostregular", false),
    SORTING("sorting", "constraint.sorting", false),
    STRETCHPATH("stretchPath", "constraint.stretchPath", false),
    TABLE("table", "constraint.table", false),
    METATASKCONSTRAINT("Meta Task Constraint", "constraint.metaTaskConstraint", false),
    TIMES("times", "constraint.times", false),
    TREE("tree", "constraint.tree", false),
    TRUE("true", "constraint.true", false),
    //USE_RESOURCES("useResources", "constraint.useResources", false),
    XNOR("xnor", "constraint.xnor", true),
    XOR("xor", "constraint.xor", true),;

    public final String name;
    public final String property;
    public final boolean canContainExpression;


    ConstraintType(String name, String property, boolean canContainExpression) {
        this.property = property;
        this.name = name;
        this.canContainExpression = canContainExpression;
    }

    ConstraintType(String name) {
        this(name, "", false);
    }

    public final String getName() {
        return name;
    }


}
