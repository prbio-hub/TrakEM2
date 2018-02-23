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

package de.unihalle.informatik.rhizoTrak.display.inspect;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ShapeProxy implements Shape
{
	private Shape s;
	
	/** Create a new proxy for {@code s}. */
	public ShapeProxy(final Shape s) {
		this.s = s;
	}
	
	/** Replace the wrapped {@link Shape} with {@code s}.
	 * 
	 * @param s
	 */
	public final void set(final Shape s) {
		this.s = s;
	}
	
	@Override
	public final Rectangle getBounds() {
		return s.getBounds();
	}

	@Override
	public final Rectangle2D getBounds2D() {
		return s.getBounds2D();
	}

	@Override
	public final boolean contains(double x, double y) {
		return s.contains(x, y);
	}

	@Override
	public final boolean contains(Point2D p) {
		return s.contains(p);
	}

	@Override
	public final boolean intersects(double x, double y, double w, double h) {
		return s.intersects(x, y, w, h);
	}

	@Override
	public final boolean intersects(Rectangle2D r) {
		return s.intersects(r);
	}

	@Override
	public final boolean contains(double x, double y, double w, double h) {
		return s.contains(x, y, w, h);
	}

	@Override
	public final boolean contains(Rectangle2D r) {
		return s.contains(r);
	}

	@Override
	public final PathIterator getPathIterator(AffineTransform at) {
		return s.getPathIterator(at);
	}

	@Override
	public final PathIterator getPathIterator(AffineTransform at, double flatness) {
		return s.getPathIterator(at, flatness);
	}
}
