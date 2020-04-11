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

import java.awt.Polygon;

import de.unihalle.informatik.rhizoTrak.display.Polyline;

/**
 * Class defining the gravitational direction for individual layers.
 *
 * @author Birgit Moeller
 */
public class RhizoGravitationalDirection {

	/**
	 * Gravitational direction in degrees.
	 */
	private double direction = Double.NaN;
	
	/**
	 * The TrakEM polyline representing the direction.
	 */
	private Polyline polyline = null;

	/**
	 * Default constructor.
	 * @param p		Polyline.
	 */
	public RhizoGravitationalDirection(Polyline p) 	{
		this.polyline = p;
		this.updateDirectionValue();
	}

	/**
	 * Constructor with direction argument.
	 * <p>
	 * Note that in this case no checks are performed and we assumen that the polyline 
	 * and the given direction are consistent.
	 * 
	 * @param p		Polyline.
	 * @param d		Direction in degrees between 0 and 360.
	 */
	public RhizoGravitationalDirection(Polyline p, double d) 	{
		this.polyline = p;
		this.direction = d;
	}

	/**
	 * Get direction.
	 * @return	Direction in degrees.
	 */
	public double getDirection() {
		this.updateDirectionValue();
		return this.direction;
	}

	/**
	 * Get directional polyline.
	 * @return	Polyline.
	 */
	public Polyline getPolyline() {
		return this.polyline;
	}
	
	/**
	 * Check if direction and given polyline are consistent and if not update direction.
	 * <p>
	 * The direction cannot be changed without any notice, however, the polyline can
	 * manually be edited by the user. Thus, consistency checks are needed.
	 */
	private void updateDirectionValue() {
		Polygon polygon = polyline.getPerimeter();
		double dx = polygon.xpoints[1] - polygon.xpoints[0];
		double dy = polygon.ypoints[1] - polygon.ypoints[0];
		this.direction = Math.toDegrees(Math.atan2(dy, dx));
		if (this.direction < 0)
			this.direction += 360;
	}
}