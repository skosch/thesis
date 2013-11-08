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

package choco.kernel.model.variables;

import choco.kernel.model.IConstraintList;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.ModelObject;
import choco.kernel.model.constraints.Constraint;

import java.util.Iterator;


/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 17 mars 2008
 * Time: 18:28:35
 * Abstract class for variable with basic methods
 */
public abstract class AbstractVariable extends ModelObject implements Variable, Comparable{

	private final static String NO_NAME= "";

	protected final VariableType type;
	protected String name = NO_NAME;
	private final IConstraintList constraints;
	private int hook = NO_HOOK; //utility field

	public AbstractVariable(VariableType type, boolean enableOption, IConstraintList constraints) {
		super(enableOption);
		this.type = type;
		this.constraints = constraints;
	}

	public AbstractVariable(VariableType type, boolean enableOptions) {
		super(enableOptions);
		this.type = type;
		this.constraints = new VConstraintsDataStructure();
	}

	public AbstractVariable(VariableType type, Variable[] variables, boolean enableOptions) {
		super(variables, enableOptions);
		this.type = type;
		this.constraints = new VConstraintsDataStructure();
	}

	protected static void throwConstantException() {
		throw new ModelException("Constant are immutable.");
	}
	public final String getName() {
		return name;
	}


	public final void setName(String name) {
		this.name = name;
	}


	public final VariableType getVariableType() {
		return type;
	}

	@Override
	public String pretty() {
		return type.name() + super.pretty();
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return pretty();
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
	public int compareTo(Object o) {
		if(this.equals(o)){
			return 0;
		}else{
			return 1;
		}
	}

	public final void _addConstraint(Constraint c) {
		constraints._addConstraint(c);
	}

	public final void _removeConstraint(Constraint c) {
		constraints._removeConstraint(c);
	}

    @Override
    public boolean _contains(final Constraint c) {
        return constraints._contains(c);
    }

    @Deprecated
	public Iterator<Constraint> getConstraintIterator() {
		throw new UnsupportedOperationException("deprecated");
	}

	public final Iterator<Constraint> getConstraintIterator(final Model m) {
		return constraints.getConstraintIterator(m);
	}



	public Constraint[] getConstraints() {
		return constraints.getConstraints();
	}

	public final Constraint getConstraint(final int idx) {
		return constraints.getConstraint(idx);
	}

	@Deprecated
	public int getNbConstraint() {
		throw new UnsupportedOperationException("deprecated");
	}

	public int getNbConstraint(Model m) {
		return constraints.getNbConstraint(m);
	}

	@Override
	public void removeConstraints() {
		constraints.removeConstraints();		
	}

	@Override
	public final int getHook() {
		return hook;
	}

	@Override
	public final void resetHook() {
		this.hook = NO_HOOK;		
	}

	@Override
	public final void setHook(int hook) {
		if( this.hook == NO_HOOK) {
			this.hook = hook;
		}else {
			throw new ModelException("The hook of the variable "+this.pretty()+" is already set to "+this.hook);
		}

	}
	
}
