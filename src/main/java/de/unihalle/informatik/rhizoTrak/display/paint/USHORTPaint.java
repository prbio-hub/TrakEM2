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

/* === original file header below (if any) === */

package de.unihalle.informatik.rhizoTrak.display.paint;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;

public final class USHORTPaint implements Paint
{
	private final short[] value;
	private final ComponentColorModel ccm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[]{16}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);

	public USHORTPaint(final short value) {
		this.value = new short[]{value};
	}

	/** Will alter the value for this instance and for all {@link USHORTPaintContext} instances
	 * returned from {@link USHORTPaint#createContext(ColorModel, Rectangle, Rectangle2D, AffineTransform, RenderingHints)}. */
	public void setValue(final short value) {
		this.value[0] = value;
	}

	@Override
	public int getTransparency() {
		return Transparency.OPAQUE;
	}

	/** Return a new {@link USHORTPaintContext} that shares the value and ccm fields with this instance. */
	@Override
	public PaintContext createContext(
			ColorModel cm, Rectangle deviceBounds,
			Rectangle2D userBounds,
			AffineTransform xform,
			RenderingHints hints) {
		return new USHORTPaintContext(this.ccm, this.value);
	}

	public ComponentColorModel getComponentColorModel() {
		return this.ccm;
	}
}
