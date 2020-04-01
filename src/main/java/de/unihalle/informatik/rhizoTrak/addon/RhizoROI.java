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
 * The Cohen-Sutherland line clipping algorithm implemented in the internal
 * Segment class has been inspired by
 * 
 * //
 *  * CohenSutherland.java 
 *  * -------------------- 
 *  * (c) 2007 by Intevation GmbH 
 *  * 
 *  * @author Sascha L. Teichmann (teichmann@intevation.de)
 *  * @author Ludwig Reiter       (ludwig@intevation.de)
 *  * 
 *  * This program is free software under the LGPL (>=v2.1) 
 *  * Read the file LICENSE.txt coming with the sources for details. 
 *  //
 *  
 *  originally released under LGPL (>=v2.1). The original source file can, 
 *  e.g., be found on Github:
 *  
 *  https://github.com/tabulapdf/tabula-java/blob/master/src/main/java/technology/tabula/CohenSutherlandClipping.java
 *  
 */

package de.unihalle.informatik.rhizoTrak.addon;

import de.unihalle.informatik.rhizoTrak.display.Polyline;
import ij.gui.Roi;

/**
 * Class defining polyline ROIs for individual layers.
 * <p>
 * ROIs can be used to restrict processing of a layer to a specific region or 
 * to restrict export of RSML. Each layer may have one or none ROI.
 * <p>
 * The ROI can be specified using the ImageJ drawing tools and stored using the "SetROI" button.
 * It is stored as a <code>polyline</code> into one rootstack object in the project tree.
 * </p>
 * 
 * @author posch
 */
public class RhizoROI {

	/**
	 * Original ImageJ Roi.
	 */
	private Roi roi;
	
	/**
	 * The TrakEM polyline representing the ROI (or null).
	 */
	private Polyline polyline = null;

	/**
	 * Default constructor.
	 * @param rhizoMain	Reference to RhizoMain object.
	 */
	public RhizoROI(Roi ijRoi, Polyline line) 	{
		this.roi = ijRoi;
		this.polyline = line;
	}

	/**
	 * Get a copy of the ImageJ Roi.
	 * @return	Copy of ImageJ Roi.
	 */
	public Roi getRoi() {
		return (Roi)this.roi.clone();
	}

	/**
	 * Get a reference to the TrakEM2/rhizoTrak polyline.
	 * @return	Polyline.
	 */
	public Polyline getPolyline() {
		return this.polyline;
	}
}