/*
 * This file is part of the rhizoTrak project.
 *
 * Note that rhizoTrak extends TrakEM2, hence, its code base substantially
 * relies on the source code of the TrakEM2 project and the corresponding Fiji
 * plugin, initiated by A. Cardona in 2005. Large portions of rhizoTrak's code
 * are directly derived/copied from the source code of TrakEM2.
 *
 * For more information on TrakEM2 please visit its websites:
 *
 *  https://imagej.net/TrakEM2
 *
 *  https://github.com/trakem2/TrakEM2/wiki
 *
 * Fore more information on rhizoTrak, visit
 *
 *  https://prbio-hub.github.io/rhizoTrak
 *
 * Both projects, TrakEM2 and rhizoTrak, are released under GPL.
 * Please find below first the copyright notice of rhizoTrak, and further on
 * (in case that this file was part of the original TrakEM2 source code base
 * and contained a TrakEM2 file header) the original file header with the
 * TrakEM2 license note.
 */

/*
 * Copyright (C) 2018 - @YEAR@ by the rhizoTrak development team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Fore more information on rhizoTrak, visit
 *
 *    https://prbio-hub.github.io/rhizoTrak
 *
 */

package de.unihalle.informatik.rhizoTrak.addon;

import de.unihalle.informatik.MiToBo.core.datatypes.MTBLineSegment2D;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.process.ImageProcessor;

import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * Utility methods to draw filled convex polygons (and isosceles as special cases) and circles
 * into an ImagePlus
 *
 * @author posch
 */

public class Drawing {
	private static final boolean debug = false;

	// visualization works in a sensible way only for one single isosceles, shows the image (i.e. pops up a window)
	private static final boolean visualize = false;

	/**
	 * Draws a filled isosceles (a trapezoid where the base angles have the same measure).
	 * If both startWidth and endWidth are less equal <code>1.0</code> one single straight line is drawn.
	 * It is specified by the coordinates of the symmetry axis and width at both ends.
	 *
	 * @param imp         The image on which is drawn
	 * @param x1          x-coordinate of the start point of the center line
	 * @param y1          y-coordinate of the start point of the center line
	 * @param x2          x-coordinate of the end point of the center line
	 * @param y2          y-coordinate of the end point of the center line
	 * @param startWidth Root Width of start point (x1,y1)
	 * @param endWidth   Root Width of end point (x2,y2)
	 * @param color       Color to draw pixels
	 */
	public static void drawIsosceles(ImagePlus imp, float x1, float y1, float x2, float y2, float startWidth, float endWidth, int color) {
//		drawIsosceles( imp,  x1,  y1,  x2,  y2,  startWidth,  endWidth, color, 0.8f);
		drawIsosceles( imp,  x1,  y1,  x2,  y2,  startWidth,  endWidth, color, 1.0001f);
	}

	/**
	 * Draws a filled isosceles (a trapezoid where the base angles have the same measure).
	 * If both startWidth and endWidth are less equal minWidth, one single straight line is drawn.
	 * It is specified by the coordinates of the symmetry axis and width at both ends.
	 * Both width are decremented my 2 x 0.5 pixels in oder to account for the thickness of the discrete line
	 * of 1 pixel.
	 *
	 * @param imp         The image on which is drawn
	 * @param x1          x-coordinate of the start point of the center line
	 * @param y1          y-coordinate of the start point of the center line
	 * @param x2          x-coordinate of the end point of the center line
	 * @param y2          y-coordinate of the end point of the center line
	 * @param startWidth Root Width of start point (x1,y1)
	 * @param endWidth   Root Width of end point (x2,y2)
	 * @param color       Color to draw pixels
	 * @param minWidth       minWidth to draw isoscele, not one single straight line
	 */
	public static void drawIsosceles(ImagePlus imp, float x1, float y1, float x2, float y2, float startWidth, float endWidth, int color, float minWidth) {
		float startWidthCorrected = Math.max( 0, startWidth-1);
		float endWidthCorrected = Math.max( 0, endWidth-1);

		if (debug) {
			System.out.println("drawIsosceles (" + x1 + "," + y1 + ")" + "(" + x2 + "," + y2 + ")");
		}
		if ( startWidth > minWidth || endWidth > minWidth ) {
			// construct the (convex) polygon to draw
			float[] a = getContourPoints(x1, y1, x2, y2, startWidthCorrected);
			float[] b = getContourPoints(x2, y2, x1, y1, endWidthCorrected);
			if (debug) {
				System.out.println("    (" + a[0] + "," + a[1] + ")" + "(" + a[2] + "," + a[3] + ")" + "(" + b[0] + "," + b[1] + ")" + "(" + b[2] + "," + b[3] + ")");
			}

			Point2D.Double[] polygon = new Point2D.Double[4];
			polygon[0] = new Point2D.Double(a[0], a[1]);
			polygon[1] = new Point2D.Double(a[2], a[3]);
			polygon[2] = new Point2D.Double(b[0], b[1]);
			polygon[3] = new Point2D.Double(b[2], b[3]);

			drawConvexPolygon(imp, polygon, color);
		} else {
			ImageProcessor ip = imp.getProcessor();
			ip.setValue(color);

			// as getPixelsAlongSegment adds 0.5 conforming with IJ conventions
			double offset = 0.5;

			MTBLineSegment2D line = new MTBLineSegment2D( x1-offset, y1-offset, x2-offset, y2-offset);
			LinkedList<Point2D.Double> pixelList = line.getPixelsAlongSegment();
			for( Point2D.Double p : pixelList ) {
				double x = p.getX();
				double y = p.getY();
				if ( x >= 0 && x < ip.getWidth() && y >= 0 && y < ip.getHeight()) {
					ip.drawPixel( (int)x, (int)y);
				}
			}
		}

		if (visualize) {
			Overlay overlay = new Overlay();

			// grid
			int xmin = (int) Math.floor(Math.min(x1 - startWidth, x2 - endWidth));
			int xmax = (int) Math.ceil(Math.max(x1 + startWidth, x2 + endWidth));
			int ymin = (int) Math.floor(Math.min(y1 - startWidth, y2 - endWidth));
			int ymax = (int) Math.ceil(Math.max(y1 + startWidth, y2 + endWidth));

			for (int y = ymin; y <= ymax; y++) {
				overlay.add(new Line(xmin, y, xmax, y));
			}
			for (int x = xmin; x <= xmax; x++) {
				overlay.add(new Line(x, ymin, x, ymax));
			}

			// symmetry axis of isoscele
			overlay.add(new Line(x1, y1, x2, y2));

			// the isoscele
			float[] a = getContourPoints(x1, y1, x2, y2, startWidth);
			float[] b = getContourPoints(x2, y2, x1, y1, endWidth);

			overlay.add(new Line(a[0], a[1], a[2], a[3]));
			overlay.add(new Line(b[0], b[1], b[2], b[3]));
			overlay.add(new Line(a[0], a[1], b[2], b[3]));
			overlay.add(new Line(a[2], a[3], b[0], b[1]));

			imp.setOverlay(overlay);
			imp.show();
		}
	}

	/** draw a filled convex polygon into the ImagePlus imp
	 *
	 * @param imp
	 * @param polygon
	 * @param color
	 */
	 public static void drawConvexPolygon( ImagePlus imp, Point2D.Double[] polygon, int color) {
		Integer[] minX = new Integer[imp.getHeight()];
		Integer[] maxX = new Integer[imp.getHeight()];
		for (int i = 0; i < imp.getHeight(); i++) {
			minX[i] = Integer.MAX_VALUE;
			maxX[i] = Integer.MIN_VALUE;
		}

		// draw the segments of the polygon and remember minimum and maximum x coordinates
		 imp.getProcessor().setValue( color);

		 for (int i = 0; i < polygon.length; i++) {
			// as getPixelsAlongSegment adds 0.5 conforming with IJ conventions
			double offset = 0.5;
			MTBLineSegment2D line = new MTBLineSegment2D( polygon[i].getX()-offset, polygon[i].getY()-offset,
					polygon[(i+1) % polygon.length].getX()-offset, polygon[(i+1) % polygon.length].getY()-offset);
			LinkedList<Point2D.Double> pixelList = line.getPixelsAlongSegment();
			for( Point2D.Double p : pixelList ) {
				setPixel(  imp.getProcessor(),  (int)p.getX(),  (int)p.getY(), minX, maxX);
			}
		}

		// fill the polygon
		for (int y = 0; y < imp.getHeight(); y++) {
			if ( minX[y] != Integer.MAX_VALUE) {
				for (int x = minX[y]; x <= maxX[y]; x++) {
					imp.getProcessor().drawPixel(x, y);
				}
			}
		}
	}

    /**
     * Draws a filled circle at position ('x', 'y') with radius 'radius'.
	 * Iterate over x or y coordinates whichever has maximum range and draw all
	 * pixels intersected
     *
     * @param imp    The image on which to draw.
     * @param x      x-coordinate of the circle center.
     * @param y      y-coordinate of the circle center.
     * @param radius The radius of the circle.
     * @param color  The fill color of the circle.
     */
    public static void drawFilledCircle(ImagePlus imp, float x, float y,
                float radius, int color) {
        ImageProcessor ip = imp.getProcessor();
		float width = 2.0f * radius;
		if ( debug ) System.out.println( "Cricle " + (x - radius) + "," + (y - radius) + " "  + width);
		OvalRoi oval = new OvalRoi(x - radius, y - radius, width, width);
		ip.setValue(color);
        ip.setMask(oval.getMask());
        ip.setRoi(oval.getBounds());
        ip.fill(ip.getMask());
    }


	/** sets the pixel at (x,y) into ip and update minimal and maximal x coordinates
	 *
	 * @param ip
	 * @param x
	 * @param y
	 * @param minX
	 * @param maxX
	 */
	private static void setPixel( ImageProcessor ip, int x, int y, Integer[] minX, Integer[] maxX) {
		//System.out.println( "   setPixel " + x + "," + y);
		if ( x >= 0 && x < ip.getWidth() && y >= 0 && y < ip.getHeight()) {
			if ( x < minX[y] ) minX[y] = x;
			if ( x > maxX[y]) maxX[y] = x;
			ip.drawPixel(x, y);
		}
	}

	/**
	 * Computes both points perpendicular to the vector defined by (x1,y1) and (x2,y2) at the first point (x1,y1) with distance 'width'
	 * between the constructed points.
	 *
	 * @param x1       First point, x coordinate
	 * @param y1       First point, y coordinate
	 * @param x2       Second point, x coordinate
	 * @param y2       Second point, y coordinate
	 * @param width The distance between the constructed points
	 * @return An array containing the coordinates of both points: {p1_x, p1_y, p2_x, p2_y}
	 */
	private static float[] getContourPoints(float x1, float y1, float x2, float y2, float width)
	{
		float lx = x2 - x1;
		float ly = y2 - y1;
		float len = (float)Math.sqrt(lx * lx + ly * ly);
		lx /= len;
		ly /= len;

		float halfWidth = 0.5f * width;

		float a1x = x1 + (ly * halfWidth);
		float a1y = y1 + (-lx * halfWidth);
		float a2x = x1 + (-ly * halfWidth);
		float a2y = y1 + (lx * halfWidth);

		return new float[]{a1x, a1y, a2x, a2y};
	}
}
