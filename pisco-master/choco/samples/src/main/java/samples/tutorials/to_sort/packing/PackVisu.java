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

package samples.tutorials.to_sort.packing;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.pack.PackModel;
import choco.visu.components.chart.ChocoChartFactory;
import samples.tutorials.PatternExample;

import java.util.logging.Level;

public class PackVisu extends PatternExample {

	private PackModel pm1, pm2;

	@Override
	public void buildModel() {
		model =new CPModel();
		pm1 = new PackModel(BinPackingInstances.N1C1W1_N,BinPackingInstances.OPT_N+2,BinPackingInstances.CAPACITY_N);
		pm2 = new PackModel(BinPackingInstances.N2C2W1_H,BinPackingInstances.OPT_H+4,BinPackingInstances.CAPACITY);
		model.addConstraints( Choco.pack(pm1), Choco.pack(pm2));
	}

	@Override
	public void buildSolver() {
		solver =new CPSolver();
		solver.read(model);
		solver.setTimeLimit(2000);
	}

	@Override
	public void prettyOut() {
		if(LOGGER.isLoggable(Level.INFO)) {
			final String title = "Bin Packing Constraint Visualization";
			LOGGER.info(title);
			if(solver.existsSolution()) ChocoChartFactory.createAndShowGUI(title,ChocoChartFactory.createPackChart(title, solver,pm1, pm2));
		}
	}

	@Override
	public void solve() {
		solver.solve();
	}

	public static void main(String[] args) {
		new PackVisu().execute();
	}

}
