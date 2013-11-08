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

import static org.jfree.chart.plot.DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE;
import static org.jfree.chart.plot.DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE;
import static org.jfree.chart.plot.DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE;
import static org.jfree.chart.plot.DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE;
import static org.jfree.chart.plot.DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE;

import java.awt.Color;
import java.awt.Paint;
import java.io.IOException;
import java.net.URISyntaxException;

import org.jfree.chart.ChartColor;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;

public final class ChocoColor {

	//http://www.colorcombos.com/color-scheme-124.html
	public final static Color COLOR_124_1 = new Color(0xE8, 0xD0, 0xA9);
	public final static Color COLOR_124_2 = new Color(0xB7, 0xAF, 0xA3);
	public final static Color COLOR_124_3 = new Color(0xC1, 0xDA, 0xD6);
	public final static Color COLOR_124_4 = new Color(0xF5, 0xFA, 0xFA);
	public final static Color COLOR_124_5 = new Color(0xAC, 0xD1, 0xE9);
	public final static Color COLOR_124_6 = new Color(0x6D, 0x92, 0x9B);
	//http://www.colorcombos.com/color-scheme-211.html
	public final static Color COLOR_211_1 = new Color(0xFF, 0xEC, 0x94);
	public final static Color COLOR_211_2 = new Color(0xFF, 0xAE, 0xAE);
	public final static Color COLOR_211_3 = new Color(0xFF, 0xF0, 0xAA);
	public final static Color COLOR_211_4 = new Color(0xB0, 0xE5, 0x7C);
	public final static Color COLOR_211_5 = new Color(0xB4, 0xD8, 0xE7);
	public final static Color COLOR_211_6 = new Color(0x56, 0xBA, 0xEC);
	//http://www.colorcombos.com/color-scheme-142.html
	public final static Color COLOR_142_1 = new Color(0x6B, 0xCA, 0xE2);
	public final static Color COLOR_142_2 = new Color(0x51, 0xA5, 0xBA);
	public final static Color COLOR_142_3 = new Color(0x41, 0x92, 0x4B);
	public final static Color COLOR_142_4 = new Color(0xAF, 0xEA, 0xAA);
	public final static Color COLOR_142_5 = new Color(0x87, 0xE2, 0x93);
	public final static Color COLOR_142_6 = new Color(0xFE, 0x84, 0x02);
	//http://www.colorcombos.com/color-scheme-274.html
	public final static Color COLOR_274_1 = new Color(0xEF, 0xD2, 0x79);
	public final static Color COLOR_274_2 = new Color(0x95, 0xCB, 0xE9);
	public final static Color COLOR_274_3 = new Color(0x02, 0x47, 0x69);
	public final static Color COLOR_274_4 = new Color(0xAF, 0xD7, 0x75);
	public final static Color COLOR_274_5 = new Color(0x2C, 0x57, 0x00);
	public final static Color COLOR_274_6 = new Color(0xDE, 0x9D, 0x7F);
	//http://www.colorcombos.com/color-scheme-148.html
	public final static Color COLOR_148_1 = new Color(0x4D, 0x89, 0x63);
	public final static Color COLOR_148_2 = new Color(0x69, 0xA5, 0x83);
	public final static Color COLOR_148_3 = new Color(0xE1, 0xB3, 0x78);
	public final static Color COLOR_148_4 = new Color(0xE0, 0xCC, 0x97);
	public final static Color COLOR_148_5 = new Color(0xEC, 0x79, 0x9A);
	public final static Color COLOR_148_6 = new Color(0x9F, 0x02, 0x51);


	
	/**
	 * Convenience method to return an array of <code>Paint</code> objects that
	 * represent the pre-defined colors in the <code>Color<code> and
	 * <code>ChartColor</code> objects.
	 *
	 * @return An array of objects with the <code>Paint</code> interface.
	 */
	public static Paint[] createDefaultPaintArray() {
		return new Paint[] {
				COLOR_124_2,
				COLOR_124_3, 
				COLOR_211_4,
				//COLOR_124_4, //almost white
				COLOR_211_1,
				COLOR_211_2,
				COLOR_124_6,
				COLOR_148_1,
				COLOR_211_3,
				COLOR_142_2,
				COLOR_148_4,
				COLOR_142_4,
				COLOR_211_5,
				COLOR_142_5,
				COLOR_142_1,
				COLOR_142_6,
				COLOR_274_1,
				COLOR_274_2,
				COLOR_274_3,
				COLOR_274_4,
				COLOR_274_5,
				COLOR_274_6,
				COLOR_124_5,
				COLOR_124_1,
				COLOR_148_2,
				COLOR_211_6,
				COLOR_148_3,
				COLOR_142_3,
				COLOR_148_5,
				COLOR_124_5,
				COLOR_148_6,
		};
	}

	private static Color gray(int val) {
		return new Color(val,val,val);
	}


	public static Paint[] createMonochromePaintArray(int start, int end,int step, int gap) {
		int idx=0;
		final Paint[] palette = new Paint[(end-start)/step +1];
		int instep = 0;
		while(instep<gap) {
			int val = start + instep;
			do{
				palette[idx++]=gray(val);
				val+=gap;
			}while(val<=end);
			instep+=step;
		}
		return palette;
	}

	public static Paint[] createLinearGradientPaintArray(Color color1, Color color2, int n) {
		final Paint[] palette = new Paint[n];
		float[] rgb1 = color1.getRGBComponents(null);
		float[] rgb2 = color2.getRGBComponents(null);
		for (int i = 0; i < n; i++) {
			float t = (float) i / (float) n;
			float ct = 1-t;
			palette[i] = new Color( 
					ct*rgb1[0] + t*rgb2[0],
					ct*rgb1[1] + t*rgb2[1],
					ct*rgb1[2] + t*rgb2[2]
					);
		}
		return palette;
	}

	protected static DrawingSupplier createDrawingSupplier(Paint[] palette) {
		return new DefaultDrawingSupplier(
				palette, 
				DEFAULT_FILL_PAINT_SEQUENCE,
				DEFAULT_OUTLINE_PAINT_SEQUENCE,
				DEFAULT_STROKE_SEQUENCE,
				DEFAULT_OUTLINE_STROKE_SEQUENCE,
				DEFAULT_SHAPE_SEQUENCE);

	}

	public static DrawingSupplier createDefaultDrawingSupplier() {
		return createDrawingSupplier(createDefaultPaintArray());
	}

	public static DrawingSupplier createMonochromeDrawingSupplier() {
		return createDrawingSupplier(createMonochromePaintArray(120,220,10,30));
	}

	public static DrawingSupplier createJFreeDrawingSupplier() {
		return createDrawingSupplier(ChartColor.createDefaultPaintArray());
	}

	private static String toHex(Paint paint) {
		return Integer.toHexString(( (Color) paint).getRGB()).substring(2).toUpperCase();
	}

	public static void browsePalette(Paint[] paints) throws IOException, URISyntaxException {
		if(paints != null && paints.length > 0) {
			StringBuilder b =new StringBuilder();
			b.append("http://www.colorcombos.com/combomaker.html?design=circles&output_width=100&size_option=element&colors=");
			b.append(toHex(paints[0]));
			for (int i = 1; i < Math.min(30, paints.length); i++) {
				b.append(',').append(toHex(paints[i]));
			}
			b.append("&background_color=FFFFFF&show_hex_flag=Y");
			System.out.println(b.toString());
			browse(b.toString());
		}
	}

	public static void browse(String URI) throws IOException, URISyntaxException {
		if( !java.awt.Desktop.isDesktopSupported() ) {

			System.err.println( "Desktop is not supported (fatal)" );
			System.exit( 1 );
		}

		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

		if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {

			System.err.println( "Desktop doesn't support the browse action (fatal)" );
			System.exit( 1 );
		}
		java.net.URI uri = new java.net.URI(URI);
		desktop.browse( uri );
	}

	public static void main(String [] args) throws IOException, URISyntaxException {
		browsePalette(createLinearGradientPaintArray(Color.YELLOW, Color.RED, 40));
//		System.out.println(toHex(COLOR_124_1));
//		System.out.println(toHex(COLOR_124_2));
//		System.out.println(toHex(COLOR_124_3));
//		System.out.println(toHex(COLOR_211_4));
	}


}