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

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.process.ImageProcessor;

import java.awt.geom.Point2D;

/**
 *
 * @author posch
 *
 */

class Drawing {
	private static final boolean debug = false;

	// visualization works in a sensible way only for one single isosceles, shows the image
	private static final boolean visualize = false;

	/**
	 * Draws a filled isosceles (a trapezoid where the base angles have the same measure).
	 * It is specified by the coordinates of the symmetry axis
	 *
	 * @param imp         The image on which is drawn
	 * @param x1          x-coordinate of the start point of the center line
	 * @param y1          y-coordinate of the start point of the center line
	 * @param x2          x-coordinate of the end point of the center line
	 * @param y2          y-coordinate of the end point of the center line
	 * @param startWidth Root Width of start point
	 * @param endWidth   Root Width of end point
	 * @param color       Color to draw pixels
	 */
	 public static void drawIsosceles(ImagePlus imp, float x1, float y1, float x2, float y2, float startWidth, float endWidth, int color) {
		 // construct the (convex) polygon to draw
		 float[] a = getContourPoints(x1, y1, x2, y2, startWidth);
		 float[] b = getContourPoints(x2, y2, x1, y1, endWidth);
		 if (debug) {
			 System.out.println("(" + x1 + "," + y1 + ")" + "(" + x2 + "," + y2 + ")");
			 System.out.println("    (" + a[0] + "," + a[1] + ")" + "(" + a[2] + "," + a[3] + ")" + "(" + b[0] + "," + b[1] + ")" + "(" + b[2] + "," + b[3] + ")");
		 }

		 Overlay overlay = new Overlay();
		 if (visualize) {
			 // control lines, width plus 1 pixel
			 float[] aPlus = getContourPoints(x1, y1, x2, y2, startWidth + 2);
			 float[] bPlus = getContourPoints(x2, y2, x1, y1, endWidth + 2);

			 overlay.add(new Line(aPlus[0], aPlus[1], bPlus[2], bPlus[3]));
			 overlay.add(new Line(aPlus[2], aPlus[3], bPlus[0], bPlus[1]));

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

			 overlay.add(new Line(x1, y1, x2, y2));

			 overlay.add(new Line(a[0], a[1], a[2], a[3]));
			 overlay.add(new Line(b[0], b[1], b[2], b[3]));
			 overlay.add(new Line(a[0], a[1], b[2], b[3]));
			 overlay.add(new Line(a[2], a[3], b[0], b[1]));

			 imp.setOverlay(overlay);
		 }

		 Point2D.Double[] polygon = new Point2D.Double[4];
		 polygon[0] = new Point2D.Double(a[0], a[1]);
		 polygon[1] = new Point2D.Double(a[2], a[3]);
		 polygon[2] = new Point2D.Double(b[0], b[1]);
		 polygon[3] = new Point2D.Double(b[2], b[3]);

		 drawConvexPolygon( imp, polygon, color);

		 if ( visualize ) {
		 	imp.show();
		 }
	 }

	 public static void drawConvexPolygon( ImagePlus imp, Point2D.Double[] polygon, int color) {
		Integer[] minX = new Integer[imp.getHeight()];
		Integer[] maxX = new Integer[imp.getHeight()];
		for (int i = 0; i < imp.getHeight(); i++) {
			minX[i] = Integer.MAX_VALUE;
			maxX[i] = Integer.MIN_VALUE;
		}

		for (int i = 0; i < 4; i++) {
			imp.getProcessor().setValue( color);
			drawLinesegment( imp, polygon[i], polygon[(i+1) % 4], minX, maxX);
		}

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
        OvalRoi oval = new OvalRoi(Math.round(x - radius), Math.round(y - radius), width, width);
        ip.setValue(color);
        ip.setMask(oval.getMask());
        ip.setRoi(oval.getBounds());
        ip.fill(ip.getMask());
    }

	/** draw a straight line segment into imp and update minimal and maximal x coordinates
	 *
	 * @param imp
	 * @param p1
	 * @param p2
	 * @param minX
	 * @param maxX
	 */
	private static void drawLinesegment(ImagePlus imp, Point2D.Double p1, Point2D.Double p2, Integer[] minX, Integer[] maxX) {
	 	if ( Math.abs( p1.getX() - p2.getX()) > Math.abs( p1.getY() - p2.getY()) ) {
	 		if ( p1.getX() > p2.getX()) {
				// swap
				Point2D.Double tmp;
				tmp = p1; p1 = p2; p2 = tmp;
			}
			if ( debug ) System.out.println( "drawLinesegment (iterate x) " + p1 + "   " + p2);

			for (int x = (int)Math.floor( p1.getX()); x <= Math.floor( p2.getX()); x++) {
				int y = (int) Math.floor( getY( x+0.5, p1, p2));
				setPixel(  imp.getProcessor(),  x,  y, minX, maxX);
			}
		} else {
			if ( p1.getY() > p2.getY()) {
				// swap
				Point2D.Double tmp;
				tmp = p1; p1 = p2; p2 = tmp;
			}
			if ( debug ) System.out.println( "drawLinesegment (iterate y) " + p1 + "   " + p2);

			for (int y = (int)Math.floor( p1.getY()); y <= Math.floor( p2.getY()); y++) {
				int x = (int) Math.floor( getX( y+0.5, p1, p2));
				setPixel(  imp.getProcessor(),  x,  y, minX, maxX);
			}
		}
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
		if ( x < minX[y] ) minX[y] = x;
		if ( x > maxX[y]) maxX[y] = x;
	 	ip.drawPixel( x, y);
	}

	/**Compute x coordinate at y for line segment specified by p1 and p2
	 *
	 * @param y
	 * @param p1
	 * @param p2
	 * @return
	 */
	private static double getX(double y, Point2D.Double p1, Point2D.Double p2) {
	 	double x = p1.getX() + (y - p1.getY())  /(p2.getY() - p1.getY()) * (p2.getX() - p1.getX());
		//System.out.println( "getX  x = " + x + " y = " + y);
		return x;
	}

	/** Compute y coordinate at x for line segment specified by p1 and p2
	 *
	 * @param x
	 * @param p1
	 * @param p2
	 * @return
	 */
	private static double getY(double x, Point2D.Double p1, Point2D.Double p2) {
	 	double y = p1.getY() + (x - p1.getX())  /(p2.getX() - p1.getX()) * (p2.getY() - p1.getY());
		//System.out.println( "getY  y = " + y + " x = " + x);
		return y;
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
