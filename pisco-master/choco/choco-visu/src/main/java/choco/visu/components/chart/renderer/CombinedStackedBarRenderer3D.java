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

package choco.visu.components.chart.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.BooleanUtilities;

class CombinedStackedBarRenderer3D extends StackedBarRenderer3D {

	private static final long serialVersionUID = 3553006088256129486L;
	
	public final int columns;
	
	public CombinedStackedBarRenderer3D(int columns) {
		super();
		this.columns = columns;
	}

	@Override
	protected void calculateBarWidth(CategoryPlot plot, Rectangle2D dataArea,
			int rendererIndex, CategoryItemRendererState state) {
		 // calculate the bar width
        CategoryAxis domainAxis = getDomainAxis(plot, rendererIndex);
        CategoryDataset data = plot.getDataset(rendererIndex);
        if (data != null) {
            PlotOrientation orientation = plot.getOrientation();
            double space = 0.0;
            if (orientation == PlotOrientation.HORIZONTAL) {
                space = dataArea.getHeight();
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                space = dataArea.getWidth();
            }
            double maxWidth = space * getMaximumBarWidth();
           
            // replace by an attributes int columns = data.getColumnCount();
            double categoryMargin = 0.0;
            if (columns > 1) {
                categoryMargin = domainAxis.getCategoryMargin();
            }

            double used = space * (1 - domainAxis.getLowerMargin()
                                     - domainAxis.getUpperMargin()
                                     - categoryMargin);
            if (columns > 0) {
                state.setBarWidth(Math.min(used / columns, maxWidth));
            }
            else {
                state.setBarWidth(Math.min(used, maxWidth));
            }
        }
	}
	
	  /**
     * Draws a stack of bars for one category, with a horizontal orientation.
     *
     * @param values  the value list.
     * @param category  the category.
     * @param g2  the graphics device.
     * @param state  the state.
     * @param dataArea  the data area (adjusted for the 3D effect).
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     *
     * @since 1.0.4
     */
    @Override
	protected void drawStackHorizontal(List values, Comparable category,
            Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot,
            CategoryAxis domainAxis, ValueAxis rangeAxis,
            CategoryDataset dataset) {

        int column = dataset.getColumnIndex(category);
/////////////////////////////////////////        
//        double barX0 = domainAxis.getCategoryMiddle(column,
//                dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge())
//                - state.getBarWidth() / 2.0;
//        double barW = state.getBarWidth();
        
        double barX0 = domainAxis.getCategoryMiddle(column,
                columns, dataArea, plot.getDomainAxisEdge())
                - state.getBarWidth() / 2.0;
        double barW = state.getBarWidth();
        
        //////////////////////////////

        // a list to store the series index and bar region, so we can draw
        // all the labels at the end...
        List itemLabelList = new ArrayList();

        // draw the blocks
        boolean inverted = rangeAxis.isInverted();
        int blockCount = values.size() - 1;
        for (int k = 0; k < blockCount; k++) {
            int index = (inverted ? blockCount - k - 1 : k);
            Object[] prev = (Object[]) values.get(index);
            Object[] curr = (Object[]) values.get(index + 1);
            int series = 0;
            if (curr[0] == null) {
                series = -((Integer) prev[0]).intValue() - 1;
            }
            else {
                series = ((Integer) curr[0]).intValue();
                if (series < 0) {
                    series = -((Integer) prev[0]).intValue() - 1;
                }
            }
            double v0 = ((Double) prev[1]).doubleValue();
            double vv0 = rangeAxis.valueToJava2D(v0, dataArea,
                    plot.getRangeAxisEdge());

            double v1 = ((Double) curr[1]).doubleValue();
            double vv1 = rangeAxis.valueToJava2D(v1, dataArea,
                    plot.getRangeAxisEdge());

            Shape[] faces = createHorizontalBlock(barX0, barW, vv0, vv1,
                    inverted);
            Paint fillPaint = getItemPaint(series, column);
            Paint fillPaintDark = fillPaint;
            if (fillPaintDark instanceof Color) {
                fillPaintDark = ((Color) fillPaint).darker();
            }
            boolean drawOutlines = isDrawBarOutline();
            Paint outlinePaint = fillPaint;
            if (drawOutlines) {
                outlinePaint = getItemOutlinePaint(series, column);
                g2.setStroke(getItemOutlineStroke(series, column));
            }
            for (int f = 0; f < 6; f++) {
                if (f == 5) {
                    g2.setPaint(fillPaint);
                }
                else {
                    g2.setPaint(fillPaintDark);
                }
                g2.fill(faces[f]);
                if (drawOutlines) {
                    g2.setPaint(outlinePaint);
                    g2.draw(faces[f]);
                }
            }

            itemLabelList.add(new Object[] {new Integer(series),
                    faces[5].getBounds2D(),
                    BooleanUtilities.valueOf(v0 < getBase())});

            // add an item entity, if this information is being collected
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, series, column, faces[5]);
            }

        }

        for (int i = 0; i < itemLabelList.size(); i++) {
            Object[] record = (Object[]) itemLabelList.get(i);
            int series = ((Integer) record[0]).intValue();
            Rectangle2D bar = (Rectangle2D) record[1];
            boolean neg = ((Boolean) record[2]).booleanValue();
            CategoryItemLabelGenerator generator
                    = getItemLabelGenerator(series, column);
            if (generator != null && isItemLabelVisible(series, column)) {
                drawItemLabel(g2, dataset, series, column, plot, generator,
                        bar, neg);
            }

        }
    }
    
    /**
     * Creates an array of shapes representing the six sides of a block in a
     * horizontal stack.
     *
     * @param x0  left edge of bar (in Java2D space).
     * @param width  the width of the bar (in Java2D units).
     * @param y0  the base of the block (in Java2D space).
     * @param y1  the top of the block (in Java2D space).
     * @param inverted  a flag indicating whether or not the block is inverted
     *     (this changes the order of the faces of the block).
     *
     * @return The sides of the block.
     */
    private Shape[] createHorizontalBlock(double x0, double width, double y0,
            double y1, boolean inverted) {
        Shape[] result = new Shape[6];
        Point2D p00 = new Point2D.Double(y0, x0);
        Point2D p01 = new Point2D.Double(y0, x0 + width);
        Point2D p02 = new Point2D.Double(p01.getX() + getXOffset(),
                p01.getY() - getYOffset());
        Point2D p03 = new Point2D.Double(p00.getX() + getXOffset(),
                p00.getY() - getYOffset());

        Point2D p0 = new Point2D.Double(y1, x0);
        Point2D p1 = new Point2D.Double(y1, x0 + width);
        Point2D p2 = new Point2D.Double(p1.getX() + getXOffset(),
                p1.getY() - getYOffset());
        Point2D p3 = new Point2D.Double(p0.getX() + getXOffset(),
                p0.getY() - getYOffset());

        GeneralPath bottom = new GeneralPath();
        bottom.moveTo((float) p1.getX(), (float) p1.getY());
        bottom.lineTo((float) p01.getX(), (float) p01.getY());
        bottom.lineTo((float) p02.getX(), (float) p02.getY());
        bottom.lineTo((float) p2.getX(), (float) p2.getY());
        bottom.closePath();

        GeneralPath top = new GeneralPath();
        top.moveTo((float) p0.getX(), (float) p0.getY());
        top.lineTo((float) p00.getX(), (float) p00.getY());
        top.lineTo((float) p03.getX(), (float) p03.getY());
        top.lineTo((float) p3.getX(), (float) p3.getY());
        top.closePath();

        GeneralPath back = new GeneralPath();
        back.moveTo((float) p2.getX(), (float) p2.getY());
        back.lineTo((float) p02.getX(), (float) p02.getY());
        back.lineTo((float) p03.getX(), (float) p03.getY());
        back.lineTo((float) p3.getX(), (float) p3.getY());
        back.closePath();

        GeneralPath front = new GeneralPath();
        front.moveTo((float) p0.getX(), (float) p0.getY());
        front.lineTo((float) p1.getX(), (float) p1.getY());
        front.lineTo((float) p01.getX(), (float) p01.getY());
        front.lineTo((float) p00.getX(), (float) p00.getY());
        front.closePath();

        GeneralPath left = new GeneralPath();
        left.moveTo((float) p0.getX(), (float) p0.getY());
        left.lineTo((float) p1.getX(), (float) p1.getY());
        left.lineTo((float) p2.getX(), (float) p2.getY());
        left.lineTo((float) p3.getX(), (float) p3.getY());
        left.closePath();

        GeneralPath right = new GeneralPath();
        right.moveTo((float) p00.getX(), (float) p00.getY());
        right.lineTo((float) p01.getX(), (float) p01.getY());
        right.lineTo((float) p02.getX(), (float) p02.getY());
        right.lineTo((float) p03.getX(), (float) p03.getY());
        right.closePath();
        result[0] = bottom;
        result[1] = back;
        if (inverted) {
            result[2] = right;
            result[3] = left;
        }
        else {
            result[2] = left;
            result[3] = right;
        }
        result[4] = top;
        result[5] = front;
        return result;
    }
}
