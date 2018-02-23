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

package de.unihalle.informatik.rhizoTrak.display.paint;

import java.awt.PaintContext;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

final class USHORTPaintContext implements PaintContext
{
	private final ComponentColorModel ccm;
	
	private WritableRaster raster;
	private final short[] value;
	
	USHORTPaintContext(final ComponentColorModel ccm, final short[] value) {
		this.value = value;
		this.ccm = ccm;
	}

	@Override
	public final Raster getRaster(final int x, final int y, final int w, final int h) {
		if (null == raster || raster.getWidth() != w || raster.getHeight() != h) {
			raster = ccm.createCompatibleWritableRaster(w, h);
		}
		final int lenY = y+h;
		final int lenX = x+w;
		for (int j=y; j<lenY; ++j) {
			for (int i=x; i<lenX; ++i) {
				raster.setDataElements(i-x, j-y, value);
			}
		}
		return raster;
	}

	@Override
	public final ColorModel getColorModel() {
		return ccm;
	}

	@Override
	public final void dispose() {
		raster = null;
	}
}