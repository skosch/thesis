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

package choco.visu.components.chart.ui;

import static choco.visu.components.chart.ChocoChartFactory.*;
import javax.swing.JComponent;

import org.jfree.chart.ChartPanel;

import choco.cp.solver.constraints.global.pack.IPackSConstraint;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.scheduling.ICumulativeResource;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class DefaultResourceView implements IResourceNode {

		private final Object obj;

		private JComponent viewPanel;

		public DefaultResourceView(Object obj) {
			super();
			this.obj = obj;
		}

		@Override
		public final JComponent getResourceView() {
			if(viewPanel == null) { 
				viewPanel = createViewPanel();
			}
			return viewPanel;
		}


		protected JComponent createViewPanel() {
			if (obj instanceof Solver) {
				return new ChartPanel( createUnaryHChart(null, (Solver) obj));
			} else if (obj instanceof IPackSConstraint) {
				return new ChartPanel(createPackChart(null, (IPackSConstraint) obj));
			} else if (obj instanceof ICumulativeResource<?>) {
				return new ChartPanel(createCumulativeChart(null, (ICumulativeResource<TaskVar>) obj, true));
			} else if (obj instanceof IResource<?>) {
				return new ChartPanel(createUnaryHChart(null, (IResource<TaskVar>) obj));
			}		
			return ChocoChartPanel.NO_DISPLAY;
		}

		@Override
		public String toString() {
			return obj instanceof Solver ? "Disjunctive" : obj.toString();
		}
	}
