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

package de.unihalle.informatik.rhizoTrak.imaging.filters;

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.image.IndexColorModel;
import java.util.Map;

import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class LUTCustom implements IFilter
{
	protected float r = 1, g = 1, b = 1;
	
	public LUTCustom() {}
	
	/**
	 * @param r Between [0, 1]
	 * @param g Between [0, 1]
	 * @param b Between [0, 1]
	 */
	public LUTCustom(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
		fix();
	}
	
	private void fix() {
		if (r < 0) r = 0;
		if (r > 1) r = 1;
		if (g < 0) g = 0;
		if (g > 1) g = 1;
		if (b < 0) b = 0;
		if (b > 1) b = 1;
	}

	public LUTCustom(Map<String,String> params) {
		try {
			this.r = Float.parseFloat(params.get("r"));
			this.g = Float.parseFloat(params.get("g"));
			this.b = Float.parseFloat(params.get("b"));
			fix();
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Could not create LUTCustom filter!", nfe);
		}
	}

	@Override
	public ImageProcessor process(ImageProcessor ip) {
		if (ip instanceof ColorProcessor) {
			Utils.log("Ignoring " + getClass().getSimpleName() + " filter for RGB image");
			return ip;
		}
		byte[] s1 = new byte[256];
		byte[] s2 = new byte[256];
		byte[] s3 = new byte[256];
		for (int i=0; i<256; ++i) {
			s1[i] = (byte)(int)(i * r);
			s2[i] = (byte)(int)(i * g);
			s3[i] = (byte)(int)(i * b);
			Utils.log2(i + ": r, g, b " + s1[i] + ", " + s2[i] + ", " + s3[i]);
		}
		ip.setColorModel(new IndexColorModel(8, 256, s1, s2, s3));
		return ip;
	}

	@Override
	public String toXML(String indent) {
		return new StringBuilder(indent)
			.append("<t2_filter class=\"").append(getClass().getName())
			.append("\" r=\"").append(r)
			.append("\" g=\"").append(g)
			.append("\" b=\"").append(b)
			.append("\" />\n").toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (null == o) return false;
		if (o instanceof LUTCustom) {
			final LUTCustom l = (LUTCustom)o;
			return r == l.r && g == l.g && b == l.b;
		}
		return false;
	}
}

