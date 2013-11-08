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

package samples;

import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import static choco.visu.components.chart.ChocoChartFactory.*;

public class DeviationDemo {

	public static void run() {
		YIntervalSeriesCollection dataset = createDeviationDataset(10000, 10, new double[]{0.1,0.15,0.05});
		createAndShowGUI("Deviation Example", createDeviationLineChart(null, "X", "Y", dataset));
	}
	
	private static YIntervalSeriesCollection createDeviationDataset(int length, int stdDiv, double coeffs[]) {
    	  YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
    	  for (int i = 0; i < coeffs.length; i++) {
    		  YIntervalSeries series = new YIntervalSeries("Series "+i);
    		  for (int c = 1; c < length; c++) {
    			  double m = 1.5 + coeffs[i]*Math.exp(1-0.01*c);
    			  double std = (m-1)/stdDiv;
				series.add(c, m, m-std, m+std);
			}
    		  dataset.addSeries(series);
		}
    	  return dataset;
    }
    
	public static void main(String[] args) {
		run();
	}
}
