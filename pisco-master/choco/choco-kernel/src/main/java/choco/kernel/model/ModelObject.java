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

package choco.kernel.model;

import choco.Options;
import choco.kernel.common.HashCoding;
import choco.kernel.common.IIndex;
import choco.kernel.common.IndexFactory;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.tools.CollectionUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.variables.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModelObject extends VariableArray implements IOptions, IIndex {

	private static final long serialVersionUID = 1700344707758777465L;
	private final long indice;
	private final List<String> options;
	
	public ModelObject(Variable[] variables, boolean enableOptions) {
		super(variables);
		options = makeOptions(enableOptions);
		indice = IndexFactory.getId();
	}
	
	public ModelObject(boolean enableOptions) {
		super();
		options = makeOptions(enableOptions);
        indice = IndexFactory.getId();
	}

	private static List<String> makeOptions(boolean enableOptions) {
		return enableOptions ? new ArrayList<String>(8) : CollectionUtils.<String>emptyList();
	}
	
	@Override
	public final int hashCode() {
		return HashCoding.hashCodeMe(indice);
	}
	
	@Override
	public void addOption(String option) {
        int h = Options.getCategorie(option);
        int  i = this.options.size();
        while(i - h <= 0){
            this.options.add(i++, Options.NO_OPTION);
        }
		this.options.set(h, option);
	}
		
	@Override
	public final void addOptions(String options) {
		DisposableIterator<String> iter = StringUtils.getOptionIterator(options);
		while(iter.hasNext()){
            addOption(iter.next());
        }
        iter.dispose();
	}

	@Override
	public final void addOptions(String[] options) {
		for (String option : options) {
			addOption(option);
		}
	}

    @Override
	public final void addOptions(List<String> options) {
		for (String option : options) {
			addOption(option);
		}
	}

	@Override
	public final void addOptions(Set<String> options) {
		for (String option : options) {
			addOption(option);
		}
	}

	@Override
	public final List<String> getOptions() {
		return options;
	}
    	
	@Override
	public final boolean containsOption(String option) {
        return options.lastIndexOf(option) >= 0;
	}

	@Override
	public final long getIndex() {
		return indice;
	}

	@Override
	public String toString() {
		return pretty();
	}

	
}
