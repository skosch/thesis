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

import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;

public class TaskLabelGenerator extends AbstractXYTaskGenerator implements XYItemLabelGenerator {

	private static final long serialVersionUID = 7955578441394246380L;

	public TaskLabelGenerator() {
		this(StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT);
	}
	
	public TaskLabelGenerator(String stringformat) {
		this(stringformat,NumberFormat.getNumberInstance(),
                NumberFormat.getNumberInstance());
	}

	public TaskLabelGenerator(String formatString, DateFormat format,
			DateFormat format2) {
		super(formatString, format, format2);
	}

	public TaskLabelGenerator(String formatString, DateFormat format,
			NumberFormat format2) {
		super(formatString, format, format2);
	}

	public TaskLabelGenerator(String formatString, NumberFormat format,
			DateFormat format2) {
		super(formatString, format, format2);
	}

	public TaskLabelGenerator(String formatString, NumberFormat format,
			NumberFormat format2) {
		super(formatString, format, format2);
	}

	@Override
	public String generateLabel(XYDataset dataset, int series, int item) {
		return generateLabelString(dataset, series, item);
	}
}