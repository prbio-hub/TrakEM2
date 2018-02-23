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

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.Map;

import mpicbg.ij.clahe.Flat;

public class CLAHE implements IFilter
{
	protected int blockRadius = 63,
	              bins = 255;
	protected float slope = 3;
	protected boolean fast = true;

	public CLAHE() {}
	
	public CLAHE(boolean fast, int blockRadius, int bins, float slope) {
		this.fast = fast;
		this.blockRadius = blockRadius;
		this.bins = bins;
		this.slope = slope;
	}
	
	public CLAHE(Map<String,String> params) {
		try {
			this.fast = Boolean.parseBoolean(params.get("fast"));
			this.blockRadius = Integer.parseInt(params.get("blockradius"));
			this.bins = Integer.parseInt(params.get("bins"));
			this.slope = Float.parseFloat(params.get("slope"));
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Could not create CLAHE filter!", nfe);
		}
	}
	
	@Override
	public ImageProcessor process(ImageProcessor ip) {
		if (fast) {
			Flat.getFastInstance().run(new ImagePlus("", ip), blockRadius, bins, slope, null, false);
		} else {
			Flat.getInstance().run(new ImagePlus("", ip), blockRadius, bins, slope, null, false);
		}
		return ip;
	}

	@Override
	public String toXML(String indent) {
		return new StringBuilder(indent)
			.append("<t2_filter class=\"").append(getClass().getName())
			.append("\" fast=\"").append(fast)
			.append("\" blockradius=\"").append(blockRadius)
			.append("\" bins=\"").append(bins)
			.append("\" slope=\"").append(slope)
			.append("\" />\n").toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (null == o) return false;
		if (o.getClass() == CLAHE.class) {
			final CLAHE c = (CLAHE)o;
			return bins == c.bins && blockRadius == c.blockRadius && slope == c.slope && fast == c.fast;
		}
		return false;
	}
}
