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

package choco.cp.model;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.iterators.EmptyIterator;
import choco.kernel.common.util.iterators.TIHIterator;
import choco.kernel.common.util.objects.DeterministicIndicedList;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.IOptions;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.ComponentConstraintWithSubConstraints;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealExpressionVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetExpressionVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;
import gnu.trove.*;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;


/**
 * A model is a global structure containing variables bound by listeners
 * as well as solutions or strategy parameters
 */
public class CPModel implements Model {

    private final static String NO_OPTIONS = "";
    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * All the constraint of the model
     */
    protected final DeterministicIndicedList<Constraint> constraints;

    /**
     * All the search intVars in the model.
     */
    protected final DeterministicIndicedList<IntegerVariable> intVars;

    protected int nbBoolVar;
    /**
     * All the set intVars in the model.
     */
    protected final DeterministicIndicedList<SetVariable> setVars;
    /**
     * All the float vars in the model.
     */
    protected final DeterministicIndicedList<RealVariable> floatVars;

    /**
     * All the constant vars in the model
     */
    protected final DeterministicIndicedList<Variable> constantVars;
    /**
     * All the search intVars in the model.
     */
    protected final DeterministicIndicedList<IntegerExpressionVariable> expVars;

    protected final DeterministicIndicedList<MultipleVariables> storedMultipleVariables;

    /**
     * Map that gives for type of contraints, a list of contraints of that type
     */
    private final THashMap<ConstraintType, TIntHashSet> constraintsByType;

    /**
     * Decomposed expression
     */
    protected Boolean defDecExp;

    protected ComponentConstraintWithSubConstraints clausesStore = null;

    protected TIHIterator<Constraint> _iterator;

    /**
     * Properties file
     */
    public static final Properties properties;

    static {
        properties = new Properties();
        try {
            final InputStream is = CPModel.class.getResourceAsStream("/application.properties");
            properties.load(is);
        } catch (IOException e) {
            LOGGER.severe("Could not open application.properties");
        }
    }

    /**
     * Constructor.
     * Create srtuctures for a model of constraint programming.
     */
    public CPModel() {
        this(32, 32, 32, 32, 32, 32, 32);
    }

    /**
     * Constructor.
     * Create srtuctures for a model of constraint programming.
     *
     * @param nbCstrs    estimated number of constraints
     * @param nbIntVars  estimated number of integer variables
     * @param nbSetVars  estimated number of set variables
     * @param nbRealVars estimated number of real variables
     * @param nbCsts     estimated number of constants
     * @param nbExpVars  estimated number of expression variables
     * @param nbMultVars estimated number of multiples variables
     */
    public CPModel(final int nbCstrs, final int nbIntVars, final int nbSetVars, final int nbRealVars, final int nbCsts, final int nbExpVars, final int nbMultVars) {
        intVars = new DeterministicIndicedList<IntegerVariable>(IntegerVariable.class, nbIntVars);
        setVars = new DeterministicIndicedList<SetVariable>(SetVariable.class, nbSetVars);
        floatVars = new DeterministicIndicedList<RealVariable>(RealVariable.class, nbRealVars);
        constantVars = new DeterministicIndicedList<Variable>(Variable.class, nbCsts);
        expVars = new DeterministicIndicedList<IntegerExpressionVariable>(IntegerExpressionVariable.class, nbExpVars);
        storedMultipleVariables = new DeterministicIndicedList<MultipleVariables>(MultipleVariables.class, nbMultVars);
        constraints = new DeterministicIndicedList<Constraint>(Constraint.class, nbCstrs);

        constraintsByType = new THashMap<ConstraintType, TIntHashSet>();
    }

    /**
     * Empty <code>this</code> by removing everry added constraints. A consequence is that every variables declared
     * will also be removed properly.
     * <br/>
     * This method must be called when <code>this</code> becomes obsolet and variables or constraints are shared
     * between mutliple models. This prevents from alive references of useless constraints within variables which
     * consequences can be slowing down execution and large memory usage with useless calls to gc.
     */
    public void removeConstraints() {
        DisposableIterator<Constraint> itc = constraints.iterator();
        while (itc.hasNext()) {
            this.remove(itc.next());
        }
        itc.dispose();
        this.remove(clausesStore);
    }

    public String pretty() {
        final StringBuffer buf = new StringBuffer("Pb[" + getNbTotVars() + " vars, " + getNbStoredMultipleVars() + " multiple vars, " + getNbConstraints() + " cons]\n");
        buf.append(this.varsToString());
        buf.append(this.constraintsToString());
        return new String(buf);
    }

    public String varsToString() {
        final StringBuffer buf = new StringBuffer("==== VARIABLES ====\n");
        buf.append(StringUtils.prettyOnePerLine(intVars.iterator()));
        buf.append(StringUtils.prettyOnePerLine(floatVars.iterator()));
        buf.append(StringUtils.prettyOnePerLine(setVars.iterator()));
        buf.append(StringUtils.prettyOnePerLine(constantVars.iterator()));

        buf.append("==== MULTIPLE VARIABLES ====\n");
        buf.append(StringUtils.prettyOnePerLine(storedMultipleVariables.iterator()));

        return new String(buf);
    }

    public String constraintsToString() {
        final StringBuffer buf = new StringBuffer("==== CONSTRAINTS ====\n");
        buf.append(StringUtils.prettyOnePerLine(constraints.iterator()));
        return new String(buf);
    }


    public String solutionToString() {
        final StringBuffer buf = new StringBuffer(24);
        buf.append(StringUtils.prettyOnePerLine(intVars.iterator()));
        buf.append(StringUtils.prettyOnePerLine(floatVars.iterator()));
        buf.append(StringUtils.prettyOnePerLine(setVars.iterator()));
        buf.append(StringUtils.prettyOnePerLine(constantVars.iterator()));
        return new String(buf);
    }


    @Deprecated
    public int getIntVarIndex(final IntDomainVar c) {
        throw new ModelException("CPModel: ?");
    }

    @Deprecated
    public int getIntVarIndex(final IntVar c) {
        throw new ModelException("CPModel: ?");
    }

    public Boolean getDefaultExpressionDecomposition() {
        return defDecExp;
    }

    public void setDefaultExpressionDecomposition(final Boolean defDecExp) {
        this.defDecExp = defDecExp;
    }

    /**
     * <i>Network management:</i>
     * Retrieve a variable by its index (all integer variables of
     * the model are numbered in sequence from 0 on)
     *
     * @param i index of the variable in the model
     */

    public final IntegerVariable getIntVar(final int i) {
        return intVars.get(i);
    }

    /**
     * retrieving the total number of variables
     *
     * @return the total number of variables in the model
     */
    public final int getNbIntVars() {
        return intVars.size();
    }

    /**
     * Returns a real variable.
     *
     * @param i index of the variable
     * @return the i-th real variable
     */
    public final RealVariable getRealVar(final int i) {
        return floatVars.get(i);
    }

    /**
     * Returns the number of variables modelling real numbers.
     */
    public final int getNbRealVars() {
        return floatVars.size();
    }

    /**
     * Returns a set variable.
     *
     * @param i index of the variable
     * @return the i-th real variable
     */
    public final SetVariable getSetVar(final int i) {
        return setVars.get(i);
    }

    /**
     * Returns the number of variables modelling real numbers.
     */
    public final int getNbSetVars() {
        return setVars.size();
    }


    /**
     * @see choco.kernel.model.Model#getNbTotVars()
     */
    @Override
    public int getNbTotVars() {
        return getNbIntVars() + getNbRealVars() + getNbSetVars();
    }


    /**
     * Returns a constant variable.
     *
     * @param i index of the variable
     * @return the i-th real variable
     */
    public IntegerConstantVariable getConstantVar(final int i) {
        return (IntegerConstantVariable) constantVars.get(i);
    }

    /**
     * Returns the number of variables modelling constant.
     */
    public int getNbConstantVars() {
        return constantVars.size();
    }


    @Override
    public int getNbStoredMultipleVars() {
        return storedMultipleVariables.size();
    }


    @Override
    public MultipleVariables getStoredMultipleVar(final int i) {
        return storedMultipleVariables.get(i);
    }


    /**
     * retrieving the total number of constraint
     *
     * @return the total number of constraints in the model
     */
    public final int getNbConstraints() {
        return constraints.size();
    }


    /**
     * <i>Network management:</i>
     * Retrieve a constraint by its index.
     *
     * @param i index of the constraint in the model
     */
    public final Constraint getConstraint(final int i) {
        //        return (Constraint)constraints.getValues()[i];
        return constraints.get(i);
    }

    /**
     * Return an iterator over the integer constraints of the model
     *
     * @return an iterator over the integer constraints of the model
     * @see choco.cp.model.CPModel#getConstraintIterator()
     * @deprecated
     */
    @Deprecated
    @Override
    public Iterator<Constraint> getIntConstraintIterator() {
        return getConstraintIterator();
    }

    /**
     * Return an iterator over the constraints of the model
     *
     * @return an iterator over the constraints of the model
     */
    @Override
    public Iterator<Constraint> getConstraintIterator() {
        //        return new TroveIterator(constraints.iterator());
        return constraints.iterator();
    }


    public DisposableIterator<Constraint> getConstraintByType(final ConstraintType t) {
        final TIntHashSet hs = constraintsByType.get(t);
        if (hs != null) {
            if (_iterator == null || !_iterator.reusable()) {
                _iterator = new TIHIterator<Constraint>();
            }
            _iterator.init(hs, constraints);
            return _iterator;
        }
        return EmptyIterator.get();
    }

    public int getNbConstraintByType(final ConstraintType t) {
        final TIntHashSet hs = constraintsByType.get(t);
        if (hs != null) {
            return hs.size();
        } else {
            return 0;
        }
    }


    //	@Override
    //	public <E extends IOptions> void addOption(String option, E... element) {
    //		for (E anElement : element) {
    //			anElement.addOption(option);
    //		}
    //	}

    private final Set<String> reuseOptions = new HashSet<String>(3);

    public void addOptions(final String options, final IOptions... element) {
        final DisposableIterator<String> iter = StringUtils.getOptionIterator(options);
        while (iter.hasNext()) {
            for (final IOptions anElement : element) {
                anElement.addOption(iter.next());
            }
        }
        iter.dispose();
    }

    /**
     * Add a variable to the model
     *
     * @param v a variable
     *          <p/>
     *          This method use default options.
     *          But User can define its own one.
     *          See CPModel.addVariable(String options, Variable... tabv) for more details.
     */
    public void addVariable(final Variable v) {
        this.addVariables(NO_OPTIONS, v);
    }

    /**
     * Add one variable with options to the model
     *
     * @param options define options of the variables
     *                <p/>
     *                This method use default options.
     *                But User can define its own one.
     *                See CPModel.addVariable(String options, Variable... tabv) for more details.
     * @param v       one or more variables
     */
    public void addVariable(final String options, final Variable v) {
        this.addVariables(options, v);
    }

    /**
     * Add one or more variables to the model
     *
     * @param v one or more variables
     * @see choco.kernel.model.Model#addVariables(choco.kernel.model.variables.Variable[])
     * @deprecated
     */
    @Deprecated
    public void addVariable(final Variable... v) {
        this.addVariables(NO_OPTIONS, v);
    }

    /**
     * Add one or more variables to the model with particular options
     *
     * @param options defines options of the variables
     * @param v       one or more variables
     * @see choco.kernel.model.Model#addVariables(String, choco.kernel.model.variables.Variable[])
     * @deprecated
     */
    @Deprecated
    public void addVariable(final String options, final Variable... v) {
        this.addVariables(options, v);
    }

    /**
     * Add variables to the model.
     *
     * @param tabv : variables to add
     *             <p/>
     *             This method use default options.
     *             But User can define its own one.
     *             See CPModel.addVariable(String options, Variable... tabv) for more details.
     */
    public void addVariables(final Variable... tabv) {
        this.addVariables(NO_OPTIONS, tabv);
    }


    /**
     * Add variables to CPModel.
     *
     * @param options : String that allows user to precise som parameters to the model concerning the variable tabv
     * @param tabv    : variables to add
     *                <p/>
     *                For IntegerVariable, available options are :
     *                <ul>
     *                <li> {@link choco.Options.V_ENUM} to force Solver to create enumerated domain variables (default options if options is empty)</li>
     *                <li> {@link choco.Options.V_BOUND} to force Solver to create bounded domain variables</li>
     *                <li> {@link choco.Options.V_BTREE} to force Solver to create binary tree domain variables</li>
     *                <li> {@link choco.Options.V_BLIST} to force Solver to create bipartite list domain variables</li>
     *                <li> {@link choco.Options.V_LINK} to force Solver to create linked list domain variables</li>
     *                </ul>
     *                <p/>
     *                For SetVariable, available options are :
     *                <ul>
     *                <li> {@link choco.Options.V_ENUM} to force Solver to create set variables with enumerated caridinality (default options if options is empty)</li>
     *                <li> {@link choco.Options.V_BOUND} to force Solver to create set variables with bounded cardinality</li>
     *                </ul>
     *                No options are available concerning Real variables.
     *                <p/>
     *                Options for decisionnal/undecisionnal variables
     *                <ul>
     *                <li>{@link choco.Options.V_DECISION} to force variable to be a decisional one</li>
     *                <li>{@link choco.Options.V_NO_DECISION} to force variable to be removed from the pool of decisionnal variables</li>
     *                </ul>
     *                Options for optimization
     *                <ul>
     *                <li>{@link choco.Options.V_OBJECTIVE} to define the variable to optimize</li>
     *                </ul>
     */
    public void addVariables(final String options, final Variable... tabv) {
        addOptions(options, tabv);
        for (final Variable v : tabv) {
            v.findManager(properties);
            switch (v.getVariableType()) {
                case INTEGER:
                    // It is necessary to detect boolean variable as soon as possible
                    if (((IntegerVariable) v).isBoolean()) {
                        nbBoolVar++;
                    }
                    //addVariable((IntegerVariable) v, intVars);
                    intVars.add((IntegerVariable) v);
                    break;
                case SET:
                    //                    addVariable((SetVariable) v, setVars);
                    setVars.add((SetVariable) v);
                    addVariable(options, ((SetVariable) v).getCard());
                    break;
                case REAL:
                    //                    addVariable((RealVariable) v, floatVars);
                    floatVars.add((RealVariable) v);
                    break;
                case CONSTANT_INTEGER:
                    // It is necessary to detect boolean variable as soon as possible
                    if (((IntegerConstantVariable) v).isBoolean()) {
                        nbBoolVar++;
                    }
                    //                    addVariable(v, constantVars);
                    constantVars.add(v);
                    break;
                case CONSTANT_DOUBLE:
                    //                    addVariable(v, constantVars);
                    constantVars.add(v);
                    break;
                case CONSTANT_SET:
                    //                    addVariable(v, constantVars);
                    constantVars.add(v);
                    addVariable(options, ((SetVariable) v).getCard());
                    break;
                case INTEGER_EXPRESSION: {
                    final IntegerExpressionVariable iev = (IntegerExpressionVariable) v;
                    final Iterator<Variable> it = iev.getVariableIterator();
                    while (it.hasNext()) {
                        this.addVariable(it.next());
                    }
                    break;
                }
                case SET_EXPRESSION: {
                    final SetExpressionVariable sev = (SetExpressionVariable) v;
                    final Iterator<Variable> it = sev.getVariableIterator();
                    while (it.hasNext()) {
                        this.addVariable(it.next());
                    }
                    break;
                }
                case REAL_EXPRESSION: {
                    final RealExpressionVariable rev = (RealExpressionVariable) v;
                    final Iterator<Variable> it = rev.getVariableIterator();
                    while (it.hasNext()) {
                        this.addVariable(it.next());
                    }
                    break;
                }
                case MULTIPLE_VARIABLES: {
                    final MultipleVariables mv = (MultipleVariables) v;
                    final Iterator<Variable> it = mv.getVariableIterator();
                    while (it.hasNext()) {
                        this.addVariable(it.next());
                    }
                    if (mv.isStored()
                            && !storedMultipleVariables.contains(mv)) {
                        storedMultipleVariables.add(mv);
                    }
                    break;
                }
                default:
                    throw new ModelException("unknown variable type :" + v.getVariableType());
            }
        }
    }

    /**
     * return the number of boolean variable (with binary domain) of the model
     *
     * @return int
     */
    public int getNbBoolVar() {
        return nbBoolVar;
    }

    @Deprecated
    protected <E extends Variable> void removeVariable(final E v, final DeterministicIndicedList<E> vars) {
        vars.remove(v);
        final Iterator<Constraint> it = v.getConstraintIterator(this);
        while (it.hasNext()) {
            final Constraint c = it.next();
            it.remove();
            this.removeConstraint(c);
        }
    }


    protected <E extends Variable> void remVariable(final E v) {
        final Iterator<Variable> it;
        switch (v.getVariableType()) {
            case INTEGER:
                final IntegerVariable vi = (IntegerVariable) v;
                intVars.remove(vi);
                if (vi.isBoolean()) nbBoolVar--;
                break;
            case SET:
                final SetVariable sv = (SetVariable) v;
                intVars.remove(sv.getCard());
                setVars.remove(sv);
                break;
            case REAL:
                final RealVariable rv = (RealVariable) v;
                floatVars.remove(rv);
                break;
            case CONSTANT_INTEGER:
            case CONSTANT_DOUBLE:
            case CONSTANT_SET:
                // a constant cannot be removed from a model
                return;
            case INTEGER_EXPRESSION:
                final IntegerExpressionVariable iev = (IntegerExpressionVariable) v;
                it = iev.getVariableIterator();
                while (it.hasNext()) {
                    this.removeVariable(it.next());
                }
                break;
            case SET_EXPRESSION:
                final SetExpressionVariable sev = (SetExpressionVariable) v;
                it = sev.getVariableIterator();
                while (it.hasNext()) {
                    this.removeVariable(it.next());
                }
                break;
            case REAL_EXPRESSION:
                final RealExpressionVariable rev = (RealExpressionVariable) v;
                it = rev.getVariableIterator();
                while (it.hasNext()) {
                    this.removeVariable(it.next());
                }
                break;
            case MULTIPLE_VARIABLES:
                final MultipleVariables mv = (MultipleVariables) v;
                it = mv.getVariableIterator();
                while (it.hasNext()) {
                    this.removeVariable(it.next());
                }
                storedMultipleVariables.remove(mv);
                break;
            default:
                throw new ModelException("unknown variable type :" + v.getVariableType());
        }
    }


    // Liste des contraintes à supprimer
    private final TLongHashSet conSet = new TLongHashSet();
    // Liste des variables à supprimer
    private final TLongHashSet varSet = new TLongHashSet();

    // Liste des contraintes à supprimer
    private final TLongObjectHashMap<Constraint> conQueue = new TLongObjectHashMap<Constraint>();
    // Liste des variables à supprimer
    private final TLongObjectHashMap<Variable> varQueue = new TLongObjectHashMap<Variable>();


    public void remove(final Object ob) {
        Constraint c;
        Variable v;


        if (ob instanceof Constraint) {
            c = (Constraint) ob;
            conQueue.put(c.getIndex(), c);
            final Constraint clast = constraints.getLast();
            final int id = constraints.remove(c);
            TIntHashSet hs = constraintsByType.get(c.getConstraintType());
            hs.remove(id);
            if (id != -1 && clast != null && c.getIndex() != clast.getIndex()) {
                hs = constraintsByType.get(clast.getConstraintType());
                hs.remove(constraints.size());
                hs.add(id);
            }
        } else if (ob instanceof Variable) {
            v = (Variable) ob;
            varQueue.put(v.getIndex(), v);
            remVariable(v);
        }
        DisposableIterator<Variable> itv;
        Iterator<Constraint> itc;
        while (!(varQueue.isEmpty() && conQueue.isEmpty())) {
            for (long key : conQueue.keys()) {
                c = conQueue.remove(key);
                itv = c.getVariableIterator();
                while (itv.hasNext()) {
                    v = itv.next();
                    v._removeConstraint(c);
                    if (v.getNbConstraint(this) == 0 && !varSet.contains(v.getIndex())) {
                        remVariable(v);
                        varQueue.put(v.getIndex(), v);
                    }
                }
                itv.dispose();
                conSet.add(c.getIndex());
            }
            for (long key : varQueue.keys()) {
                v = varQueue.remove(key);
                itc = v.getConstraintIterator(this);
                while (itc.hasNext()) {
                    c = itc.next();
                    if (!conSet.contains(c.getIndex())) {
                        conQueue.put(c.getIndex(), c);
                        final Constraint clast = constraints.getLast();
                        final int id = constraints.remove(c);
                        TIntHashSet hs = constraintsByType.get(c.getConstraintType());
                        hs.remove(id);
                        if (id != -1 && clast != null && c.getIndex() != clast.getIndex()) {
                            hs = constraintsByType.get(clast.getConstraintType());
                            hs.remove(constraints.size());
                            hs.add(id);
                        }
                    }
                }
                varSet.add(v.getIndex());
            }
        }
    }

    public void removeConstraint(final Constraint c) {
        this.remove(c);
    }

    /**
     * Remove one or more variables from the model
     * (also remove constraints linked to the variables)
     *
     * @param v variables to remove
     * @see choco.kernel.model.Model#removeVariables(choco.kernel.model.variables.Variable[])
     * @deprecated
     */
    @Deprecated
    public void removeVariable(final Variable... v) {
        for (final Variable aV : v) {
            this.remove(aV);
        }
    }

    /**
     * Remove one variable from the model
     * (also remove constraints linked to the variable)
     *
     * @param v the variable to remove
     */
    public void removeVariable(final Variable v) {
        this.remove(v);
    }

    /**
     * Remove one or more variables from the model
     * (also remove constraints linked to the variables)
     *
     * @param v variables to remove
     */
    public void removeVariables(final Variable... v) {
        for (final Variable aV : v) {
            this.remove(aV);
        }
    }

    /**
     * Add constraint into constraintsByType collections
     *
     * @param t type of constraint
     * @param c the constraint to add
     */
    private void updateConstraintByType(final ConstraintType t, final Constraint c) {
        TIntHashSet hs = constraintsByType.get(t);
        if (hs == null) {
            hs = new TIntHashSet();
        }
        final int id = constraints.get(c);
        if (!hs.contains(id)) {
            hs.add(id);
        }
        constraintsByType.put(t, hs);
    }

    /**
     * Add one or more constraint to the model.
     * Also add variables to the model if necessary.
     *
     * @param c one or more constraint
     * @see choco.kernel.model.Model#addConstraints(choco.kernel.model.constraints.Constraint[])
     * @deprecated
     */
    @Deprecated
    public void addConstraint(final Constraint... c) {
        this.addConstraints(NO_OPTIONS, c);
    }

    /**
     * Add one or more constraint to the model.
     * Also add variables to the model if necessary.
     *
     * @param options defines options of the constraint
     * @param c       one or more constraint
     * @see choco.kernel.model.Model#addConstraints(choco.kernel.model.constraints.Constraint[])
     * @deprecated
     */
    @Deprecated
    public void addConstraint(final String options, final Constraint... c) {
        this.addConstraints(options, c);
    }

    /**
     * Add one constraint to the model.
     * Also add variables to the model if necessary.
     *
     * @param c one constraint
     */
    public void addConstraint(final Constraint c) {
        this.addConstraints(NO_OPTIONS, c);
    }

    /**
     * Add one or more constraint to the model.
     * Also add variables to the model if necessary.
     *
     * @param c one or more constraint
     */
    public void addConstraints(final Constraint... c) {
        this.addConstraints(NO_OPTIONS, c);
    }

    /**
     * Add one constraint to the model.
     * Also add variables to the model if necessary.
     *
     * @param options defines options of the constraint
     * @param c       one constraint
     */
    public void addConstraint(final String options, final Constraint c) {
        this.addConstraints(options, c);
    }


    /**
     * Add constraints to the model
     *
     * @param options : options of constraint
     * @param tabc    : constraints to add
     *                <p/>
     *                Options of CPModel must be prefixed with cp.
     *                The following options are available:
     *                <ul>
     *                <i>cp:decomp to force decomposition on particular expression constraint</i>
     *                </ul>
     */
    public void addConstraints(final String options, final Constraint... tabc) {
        addOptions(options, tabc);
        for (final Constraint c : tabc) {
            c.addOptions(reuseOptions);
            c.findManager(properties);
            switch (c.getConstraintType()) {
                case CLAUSES:
                    storeClauses((ComponentConstraint) c);
                    break;
                default:
                    if (!constraints.contains(c)) {
                        constraints.add(c);
                    }
                    break;
            }
            updateConstraintByType(c.getConstraintType(), c);
            final DisposableIterator<Variable> it = c.getVariableIterator();
            while (it.hasNext()) {
                final Variable v = it.next();
                if (v == null) {
                    LOGGER.severe("Adding null variable in the model !");
                }
                addVariable(v);
                if (v.getVariableType() != VariableType.CONSTANT_INTEGER
                        && v.getVariableType() != VariableType.CONSTANT_SET) {
                    v._addConstraint(c);
                }
            }
            it.dispose();
        }
    }

    /**
     * Data structure to deal with clauses
     *
     * @param clause clause to add
     */
    private void storeClauses(final ComponentConstraint clause) {
        if (clausesStore == null) {
            clausesStore = new ComponentConstraintWithSubConstraints(ConstraintType.CLAUSES, clause.getVariables(), null, clause);
            clausesStore.addOptions(clause.getOptions());
            clausesStore.findManager(properties);
            constraints.add(clausesStore);

        } else {
            clausesStore.addElements(clause.getVariables(), clause);
        }
    }


    public Iterator<IntegerVariable> getIntVarIterator() {
        return intVars.iterator();
    }

    public Iterator<RealVariable> getRealVarIterator() {
        return floatVars.iterator();
    }

    public Iterator<SetVariable> getSetVarIterator() {
        return setVars.iterator();
    }

    public Iterator<Variable> getConstVarIterator() {
        return constantVars.iterator();
    }

    public Iterator<IntegerExpressionVariable> getExprVarIterator() {
        return expVars.iterator();
    }

    public Iterator<MultipleVariables> getMultipleVarIterator() {
        return storedMultipleVariables.iterator();
    }

    static class TroveIterator implements Iterator<Constraint> {
        private final TLongObjectIterator<Constraint> iterator;

        TroveIterator(final TLongObjectIterator<Constraint> tit) {
            this.iterator = tit;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Constraint next() {
            iterator.advance();
            return iterator.value();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    public boolean contains(final Constraint c) {
        return constraints.contains(c);
    }

    /**
     * Kicks off the serialization mechanism and flatten the {@code model} into the given {@code file}.
     *
     * @param model to flatten
     * @param file  scope file
     * @throws IOException if an I/O exception occurs.
     */
    public static void writeInFile(final CPModel model, final File file) throws IOException {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(model);
        out.close();
    }

    /**
     * Kicks off the serialization mechanism and flatten the {@code model} into a file
     * in the default temporary-file directory.
     *
     * @param model to flatten
     * @param file  scope file
     * @throws IOException if an I/O exception occurs.
     */
    public static File writeInFile(final CPModel model) throws IOException {
        final File file = File.createTempFile("CPMODEL_", ".ser");
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(model);
        out.close();
        return file;
    }


    /**
     * Restore flatten {@link CPModel} from the given {@code file}.
     *
     * @param file input file
     * @return a {@link CPModel}
     * @throws IOException            if an I/O exception occurs.
     * @throws ClassNotFoundException if wrong flattened object.
     */
    public static CPModel readFromFile(final File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        fis = new FileInputStream(file);
        in = new ObjectInputStream(fis);
        final CPModel model = (CPModel) in.readObject();
        in.close();
        return model;
    }

}
