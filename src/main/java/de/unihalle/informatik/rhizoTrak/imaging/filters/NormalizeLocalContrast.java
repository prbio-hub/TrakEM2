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

import ij.process.ImageProcessor;

import java.util.Map;

public class NormalizeLocalContrast implements IFilter
{
	protected int brx = 500, bry = 500;
	protected float stds = 3;
	protected boolean cent = true, stret = true;

	public NormalizeLocalContrast() {}

	public NormalizeLocalContrast(
			final int blockRadiusX,
			final int blockRadiusY,
			final float stdDevs,
			final boolean center,
			final boolean stretch) {
		set(blockRadiusX, blockRadiusY, stdDevs, center, stretch);
	}

	private final void set(final int blockRadiusX,
			final int blockRadiusY,
			final float stdDevs,
			final boolean center,
			final boolean stretch) {
		this.brx = blockRadiusX;
		this.bry = blockRadiusY;
		this.stds = stdDevs;
		this.cent = center;
		this.stret = stretch;
	}

	public NormalizeLocalContrast(final Map<String,String> params) {
		try {
			set(Integer.parseInt(params.get("brx")),
			    Integer.parseInt(params.get("bry")),
			    Float.parseFloat(params.get("stds")),
			    Boolean.parseBoolean(params.get("stret")),
			    Boolean.parseBoolean(params.get("cent")));
		} catch (final NumberFormatException nfe) {
			throw new IllegalArgumentException("Could not create LocalContrast filter!", nfe);
		}
	}


	@Override
	public ImageProcessor process(final ImageProcessor ip) {
		try {
			mpicbg.ij.plugin.NormalizeLocalContrast.run(ip, brx, bry, stds, cent, stret);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return ip;
	}

	@Override
	public String toXML(final String indent) {
		return new StringBuilder(indent)
		.append("<t2_filter class=\"").append(getClass().getName())
		.append("\" brx=\"").append(brx)
		.append("\" bry=\"").append(bry)
		.append("\" stds=\"").append(stds)
		.append("\" cent=\"").append(cent)
		.append("\" stret=\"").append(stret)
		.append("\" />\n").toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (null == o) return false;
		if (o.getClass() == NormalizeLocalContrast.class) {
			final NormalizeLocalContrast c = (NormalizeLocalContrast)o;
			return brx == c.brx && bry == c.bry && stds == c.stds && cent == c.cent && stret == c.stret;
		}
		return false;
	}
}
