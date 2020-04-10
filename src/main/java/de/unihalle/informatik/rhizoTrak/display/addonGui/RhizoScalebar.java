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

package de.unihalle.informatik.rhizoTrak.display.addonGui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.DisplayCanvas;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.measure.Calibration;

/**
 * Scalebar visualizing the image calibration.
 * 
 * @author moeller
 */
public class RhizoScalebar {
	
	/**
	 * Position where the scalebar is to be shown.
	 */
	public static enum DisplayPosition {
		/**
		 * Top left corner of the display.
		 */
		TOP_LEFT,
		/**
		 * Top in the middle of the display.
		 */
		TOP_CENTER,
		/**
		 * Top right corner of the display.
		 */
		TOP_RIGHT,
		/**
		 * Bottom left corner of the display.
		 */
		BOTTOM_LEFT,
		/**
		 * Bottom in the middle of the display.
		 */
		BOTTOM_CENTER,
		/**
		 * Bottom right corner of the display.
		 */
		BOTTOM_RIGHT		
	}
	
	/**
	 * Flag if scalebar is visible or not.
	 */
	private boolean isVisible = false;

	/**
	 * Position where the scalebar is to be drawn.
	 */
	private DisplayPosition position = DisplayPosition.BOTTOM_LEFT;
	
	/**
	 * Width of the scalebar line.
	 */
	private int linewidth=3;

	/**
	 * Width of the scalebar in pixels.
	 */
	private int pixelwidth = 200;

	/**
	 * Color of scalebar.
	 */
	private Color color = new Color(255,255,0,255); // yellow with full alpha
	
	/**
	 * Font size to be used.
	 */
	private int fontSize = 24;

	/**
	 * Hide or unhide the scalebar.
	 * @param flag	If true, the scalebar becomes visible.
	 */
	public void setVisible(boolean flag) {
		this.isVisible = flag;
	}

	/**
	 * Repaint the scalebar.
	 * @param g				Graphics object.
	 * @param canvas	Target canvas.
	 */
	public void repaint(final Graphics2D g, DisplayCanvas canvas) {

		if (!this.isVisible)
			return;

		Calibration calib = null;
		//			calib = Display.this.getLayerSet().getCalibration();
		//			if (calib == null) {
		//				// get calibration information from active image if there is none in the layerset
		//				Layer activeLayer = Display.getFrontLayer();
		//				ImagePlus activeImg = activeLayer.getPatches(true).get(0).getImagePlus();
		//				calib = activeImg.getCalibration();
		//			}
		//			

		// get calibration information
		Layer activeLayer = Display.getFrontLayer();
		calib = activeLayer.getPatches(true).get(0).getImagePlus().getCalibration();

		// no calibration information available
		if (calib == null) {
			Utils.showMessage("Scalebar - no calibration data found!", 
					"Could not find calibration data, please check your image metadata.");
			return;				
		}

		String unitString = calib.getUnit();
		double physicalPixelWidth = calib.pixelWidth;

		// always place the scalebar in the lower left corner
		Rectangle viewPane = canvas.getSrcRect();
		double y1 = viewPane.getMaxY() - 50/canvas.getMagnification();
		double y2 = y1;
		double x1 = viewPane.getMinX() + 50/canvas.getMagnification();
		double physicalScalebarWidth = pixelwidth*physicalPixelWidth; 
		double x2 = x1 + pixelwidth;
		String resolutionString = String.format("%.3f", physicalScalebarWidth);

		// if there is a comma in the string, convert to digital dot
		resolutionString = resolutionString.replace(",", ".");

		Line2D.Double line = new Line2D.Double(x1, y1, x2, y2);
		g.setStroke(new BasicStroke((float)(linewidth/canvas.getMagnification())));
		g.setColor(this.color);
		g.draw(line);
		Font font = new Font("TimesRoman", Font.PLAIN, (int)(this.fontSize/canvas.getMagnification())); 
		this.drawCenteredString(g, canvas, line, resolutionString + " " + unitString, font);
	}

	/**
	 * Set position where to draw the scalebar.
	 * @param dp	Scalebar position.
	 */
	public void setPosition(DisplayPosition dp) {
		this.position = dp;
	}

	/**
	 * Set color of the scalebar.
	 * @param c	Color of scalebar.
	 */
	public void setColor(Color c) {
		this.color = c;
	}

	/**
	 * Set length of scalebar in pixels.
	 * @param pixellength	Length in pixels.
	 */
	public void setPixelWidth(int pw) {
		this.pixelwidth = pw;
	}

	/**
	 * Set linewidth of scalebar.
	 * @param linewidth	Line width of scalebar.
	 */
	public void setLinewidth(int lw) {
		this.linewidth = lw;
	}

	/**
	 * Set size of font of scalebar label.
	 * @param size	Font size.
	 */
	public void setLabelFontsize(int size) {
		this.fontSize = size;
	}

	/**
	 * Draw a string centered on the given line.
	 * @param g 			Graphics instance.
	 * @param canvas	Target canvas.
	 * @param text 		String to draw.
	 * @param line		Line on which to center the text.
	 */
	private void drawCenteredString(Graphics g, DisplayCanvas canvas, 
			Line2D.Double line, String text, Font font) {
		FontMetrics metrics = g.getFontMetrics(font);
		double length = line.getX2() - line.getX1();
		int x = (int)(line.getX1() + (length - metrics.stringWidth(text)) / 2);
		int y = (int)(line.getY1() - 10/canvas.getMagnification());
		g.setFont(font);
		g.drawString(text, x, y);
	}		
}	