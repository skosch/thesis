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

import choco.kernel.common.util.tools.IteratorUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.ModelException;
import choco.kernel.model.ModelObject;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.THashSet;

import java.util.Iterator;
import java.util.Properties;


/**
 * @author Arnaud Malapert
 */
public abstract class AbstractConstraint extends ModelObject implements Constraint, Comparable {

    private ConstraintType type;

    //null by default until it has been loaded
    protected String manager;

    public AbstractConstraint(final ConstraintType type, final Variable[] variables) {
        super(variables, true);
        this.type = type;
    }

    public AbstractConstraint(final String consMan, final Variable[] variables) {
        this(ConstraintType.NONE, variables);
        this.manager = consMan;
    }


    void variablesPrettyPrint(final StringBuilder buffer) {
        buffer.append(StringUtils.pretty(this.getVariableIterator()));
    }

    public Object getParameters() {
        return null;
    }

    @Override
    public String getName() {
        return type == null || type.equals(ConstraintType.NONE) ? manager : type.getName();
    }

    @Override
    public String pretty() {
        final StringBuilder st = new StringBuilder(getName());
        st.append(" ( ");
        variablesPrettyPrint(st);
        st.append(" )");
        return st.toString();
    }

    @Override
    public final ConstraintType getConstraintType() {
        return type;
    }


    /**
     * get rid of the constants within the returned scopes !
     *
     * @return scope of integervariable of the constraint
     */
    public IntegerVariable[] getIntVariableScope() {
        final Iterator<Variable> itvs = getVariableIterator();
        final THashSet<IntegerVariable> vs = new THashSet<IntegerVariable>();
        while (itvs.hasNext()) {
            final Variable v1 = itvs.next();
            if (v1.getVariableType() == VariableType.INTEGER &&
                    !vs.contains(v1)) {
                vs.add((IntegerVariable) v1);
            } else if (v1.getVariableType().equals(VariableType.INTEGER_EXPRESSION)) {
                final THashSet<Variable> tmp = extractEveryvariables((IntegerExpressionVariable) v1);
                for (final Variable aTmp : tmp) {
                    if (aTmp.getVariableType() == VariableType.INTEGER &&
                            !vs.contains(aTmp)) {
                        vs.add((IntegerVariable) aTmp);
                    }
                }
            }
        }
        final IntegerVariable[] vars = new IntegerVariable[vs.size()];
        int cpt = 0;
        for (final IntegerVariable v : vs) {
            vars[cpt++] = v;

        }
        return vars;
    }

    /**
     * Extract every sub variables of an IntegerExpressionVariable
     *
     * @param iev integer expression variable
     * @return set of variable
     */
    private static THashSet<Variable> extractEveryvariables(final IntegerExpressionVariable iev) {
        final THashSet<Variable> vs = new THashSet<Variable>();
        if (iev.getVariableType().equals(VariableType.INTEGER)) {
            if (!vs.contains(iev)) vs.add(iev);
        } else if (iev.getVariableType().equals(VariableType.INTEGER_EXPRESSION)) {
            final Variable[] tmp = iev.extractVariables();
            final Iterator<Variable> it = IteratorUtils.iterator(tmp);
            while (it.hasNext()) {
                vs.addAll(extractEveryvariables((IntegerExpressionVariable) it.next()));
            }
        }
        return vs;
    }


    /**
     * @return a list of domains accepted by the constraint and sorted
     *         by order of preference
     */
    public int[] getFavoriteDomains() {
        return new int[]{IntDomainVar.BITSET,
                IntDomainVar.LINKEDLIST,
                IntDomainVar.BIPARTITELIST,
                IntDomainVar.BINARYTREE,
                IntDomainVar.BOUNDS,
        };
    }


    public ConstraintManager getConstraintManager() {
        return ManagerFactory.loadConstraintManager(getManager());
    }

    public ExpressionManager getExpressionManager() {
        return ManagerFactory.loadExpressionManager(getManager());
    }

    public final String getManager() {
        return manager;
    }


    public void findManager(final Properties propertiesFile) {
        if (manager == null) {
            if (type.property == null) throw new ModelException("Empty property, can not read it!");
            manager = propertiesFile.getProperty(type.property);
            if (manager == null)
                throw new ModelException("No property found for " + type.property + " in application.properties");
        }
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p/>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p/>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p/>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p/>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p/>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(final Object o) {
        if (this.equals(o)) {
            return 0;
        } else {
            return 1;
        }
    }
}
