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
import java.util.Date;

import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.xy.XYDataset;

public abstract class AbstractXYTaskGenerator extends
		AbstractXYItemLabelGenerator {

	private static final long serialVersionUID = -6976436429618157861L;

	public AbstractXYTaskGenerator(String formatString, NumberFormat format,
			NumberFormat format2) {
		super(formatString, format, format2);
	}

	public AbstractXYTaskGenerator(String formatString, DateFormat format,
			NumberFormat format2) {
		super(formatString, format, format2);
	}

	public AbstractXYTaskGenerator(String formatString, NumberFormat format,
			DateFormat format2) {
		super(formatString, format, format2);
	}

	public AbstractXYTaskGenerator(String formatString, DateFormat format,
			DateFormat format2) {
		super(formatString, format, format2);
	}

	protected static String format(double val, DateFormat dformat,
			NumberFormat format) {
		return dformat == null ? format.format(val) : dformat.format(new Date(
				(long) val));
	}

	protected String xformat(double val) {
		return format(val, getXDateFormat(), getXFormat());
	}

	protected String yformat(double val) {
		return format(val, getYDateFormat(), getYFormat());
	}

	@Override
	protected Object[] createItemArray(XYDataset dataset, int series, int item) {
		if (dataset instanceof XYTaskDataset) {
			Task t = ((XYTaskDataset) dataset).getTasks().getSeries(series)
					.get(item);
			Object[] result = new Object[4];
			result[0] = t.getDescription();
			result[1] = xformat(t.getDuration().getStart().getTime());
			double y = dataset.getYValue(series, item);
			if (Double.isNaN(y) && dataset.getY(series, item) == null) {
				result[2] = this.getNullYString();
			} else {
				result[2] = yformat(y);
			}
			result[3] = xformat(t.getDuration().getEnd().getTime());
			return result;
		} else {
			return super.createItemArray(dataset, series, item);
		}
	}

}
