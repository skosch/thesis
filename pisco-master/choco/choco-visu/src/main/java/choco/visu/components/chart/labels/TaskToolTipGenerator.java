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

package choco.visu.components.chart.labels;

import java.text.DateFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

public class TaskToolTipGenerator extends AbstractXYTaskGenerator implements XYToolTipGenerator {



	private static final long serialVersionUID = -3185597210687549691L;

	public TaskToolTipGenerator() {
		this(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT);
	}
	
	public TaskToolTipGenerator(String formatString) {
		this(formatString,NumberFormat.getInstance(),NumberFormat.getInstance());
	}
	public TaskToolTipGenerator(String formatString, NumberFormat format,
			NumberFormat format2) {
		super(formatString, format, format2);
	}

	public TaskToolTipGenerator(String formatString, DateFormat format,
			NumberFormat format2) {
		super(formatString, format, format2);
	}

	public TaskToolTipGenerator(String formatString, NumberFormat format,
			DateFormat format2) {
		super(formatString, format, format2);
	}

	public TaskToolTipGenerator(String formatString, DateFormat format,
			DateFormat format2) {
		super(formatString, format, format2);
	}

	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {
		return generateLabelString(dataset, series, item);
	}



	//	//Format = new Integer
	//	@Override
	//	public String generateLabelString(XYDataset dataset, int series, int item) {
	//		if (dataset instanceof XYTaskDataset) {
	//			Task t = ( (XYTaskDataset) dataset).getTasks().getSeries(series).get(item);
	//			StringBuilder b =new StringBuilder();
	//			b.append(t.getDescription()).append(": ");
	//			b.append(this.getXDateFormat().format(t.getDuration().getStart()));
	//			b.append("->");
	//			b.append(this.getXDateFormat().format(t.getDuration().getEnd()));
	//			return new String(b);
	//		}else {
	//			return super.generateLabelString(dataset, series, item);
	//		}
	//		
	//	}


}
