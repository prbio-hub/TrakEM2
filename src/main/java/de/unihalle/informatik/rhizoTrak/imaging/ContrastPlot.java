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
 *  https://github.com/prbio-hub/rhizoTrak/wiki
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
 *    https://github.com/prbio-hub/rhizoTrak/wiki
 *
 */

/* === original file header below (if any) === */

package de.unihalle.informatik.rhizoTrak.imaging;

import ij.process.ImageStatistics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

/** Copied and modified from Wayne Rasband's ImageJ ContrastPlot inner class in
 *  ij.plugin.frame.ContrastAdjuster class, in ImageJ 1.43h. */
public class ContrastPlot extends Canvas
{	
	private static final long serialVersionUID = 1L;

	static final int WIDTH = 128, HEIGHT=64;
	double defaultMin = 0;
	double defaultMax = 255;
	double min = 0;
	double max = 255;
	int[] histogram;
	int hmax;
	Image os;
	Color color = Color.gray;
	
	public ContrastPlot(final double defaultMin, final double defaultMax, final double firstMin, final double firstMax) {
		setSize(WIDTH+1, HEIGHT+1);
		this.defaultMin = defaultMin;
		this.defaultMax = defaultMax;
		this.min = firstMin;
		this.max = firstMax;
	}

	/** Overrides Component getPreferredSize(). Added to work 
		around a bug in Java 1.4.1 on Mac OS X.*/
	public Dimension getPreferredSize() {
		return new Dimension(WIDTH+1, HEIGHT+1);
	}

	public void setHistogram(ImageStatistics stats, Color color) {
		this.color = color;
		histogram = stats.histogram;
		if (histogram.length!=256)
			{histogram=null; return;}
		for (int i=0; i<128; i++)
			histogram[i] = (histogram[2*i]+histogram[2*i+1])/2;
		int maxCount = 0;
		int mode = 0;
		for (int i=0; i<128; i++) {
			if (histogram[i]>maxCount) {
				maxCount = histogram[i];
				mode = i;
			}
		}
		int maxCount2 = 0;
		for (int i=0; i<128; i++) {
			if ((histogram[i]>maxCount2) && (i!=mode))
				maxCount2 = histogram[i];
		}
		hmax = stats.maxCount;
		if ((hmax>(maxCount2*2)) && (maxCount2!=0)) {
			hmax = (int)(maxCount2*1.5);
			histogram[mode] = hmax;
		}
		os = null;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		int x1, y1, x2, y2;
		double scale = (double)WIDTH/(defaultMax-defaultMin);
		double slope = 0.0;
		if (max!=min)
			slope = HEIGHT/(max-min);
		if (min>=defaultMin) {
			x1 = (int)(scale*(min-defaultMin));
			y1 = HEIGHT;
		} else {
			x1 = 0;
			if (max>min)
				y1 = HEIGHT-(int)((defaultMin-min)*slope);
			else
				y1 = HEIGHT;
		}
		if (max<=defaultMax) {
			x2 = (int)(scale*(max-defaultMin));
			y2 = 0;
		} else {
			x2 = WIDTH;
			if (max>min)
				y2 = HEIGHT-(int)((defaultMax-min)*slope);
			else
				y2 = 0;
		}
		if (histogram!=null) {
			if (os==null && hmax!=0) {
				os = createImage(WIDTH,HEIGHT);
				Graphics osg = os.getGraphics();
				osg.setColor(Color.white);
				osg.fillRect(0, 0, WIDTH, HEIGHT);
				osg.setColor(color);
				for (int i = 0; i < WIDTH; i++)
					osg.drawLine(i, HEIGHT, i, HEIGHT - ((int)(HEIGHT * histogram[i])/hmax));
				osg.dispose();
			}
			if (os!=null) g.drawImage(os, 0, 0, this);
		} else {
			g.setColor(Color.white);
			g.fillRect(0, 0, WIDTH, HEIGHT);
		}
		g.setColor(Color.black);
 		g.drawLine(x1, y1, x2, y2);
 		g.drawLine(x2, HEIGHT-5, x2, HEIGHT);
 		g.drawRect(0, 0, WIDTH, HEIGHT);

		//System.out.println(" hmax " + hmax + "\n x1,y1 " + x1 +", "+ y1 + "\n min,max " + min +", " + max + "\n defaultMin,Max: " + defaultMin +"," + defaultMax + "\n WIDTH,HEIGHT " + WIDTH +"," + HEIGHT);
	}

	/** Set new min and max (of the image, not of the plot) and repaint. */
	public void update(double min, double max) {
		this.min = min;
		this.max = max;
		repaint();
	}

	/** Set default min and max (of the image, not of the plot). */
	public void setDefaultMinAndMax(double min, double max) {
		defaultMin = min;
		defaultMax = max;
		System.out.println("default min/max are " + min + ", " + max);
	}
}