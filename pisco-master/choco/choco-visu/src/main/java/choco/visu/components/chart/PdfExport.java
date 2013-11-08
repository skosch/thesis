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

package choco.visu.components.chart;

import choco.kernel.common.logging.ChocoLogging;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.logging.Logger;

public final class PdfExport {

	public final static Logger LOGGER = ChocoLogging.getMainLogger();

	private PdfExport() {
		super();
	}
	
	
	public static void saveChartAsPDF(File file,
			JFreeChart chart,
			int width,
			int height) throws IOException {
		saveChartAsPDF(file, chart, width, height, new DefaultFontMapper());
	}
	
	/**
	 * Saves a chart to a PDF file.
	 *
	 * @param file the file.
	 * @param chart the chart.
	 * @param width the chart width.
	 * @param height the chart height.
	 */
	public static void saveChartAsPDF(File file,
			JFreeChart chart,
			int width,
			int height,
			FontMapper mapper) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		writeChartAsPDF(out, chart, width, height, mapper);
		out.close();
	}
	
	
	/**
	 * Writes a chart to an output stream in PDF format.
	 *
	 * @param out the output stream.
	 * @param chart the chart.
	 * @param width the chart width.
	 * @param height the chart height.
	 *
	 */
	public static void writeChartAsPDF(OutputStream out,
			JFreeChart chart,
			int width,
			int height,
			FontMapper mapper) throws IOException {
		Rectangle pagesize = new Rectangle(width, height);
		Document document = new Document(pagesize, 50, 50, 50, 50);
		try {
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.addAuthor("Choco Team");
			document.addSubject("Choco Solver Chart");
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			Graphics2D g2 = tp.createGraphics(width, height, mapper);
			Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
			chart.draw(g2, r2D);
			g2.dispose();
			cb.addTemplate(tp, 0, 0);
		}
		catch (DocumentException de) {
			LOGGER.severe(de.getMessage());
		}
		document.close();
	}

}

