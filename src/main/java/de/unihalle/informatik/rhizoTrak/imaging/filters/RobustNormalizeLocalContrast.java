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

public class RobustNormalizeLocalContrast implements IFilter
{
	protected int scaleLevel = 2, brx1 = 1000, bry1 = 1000, brx2 = 500, bry2 = 500;
	protected float stds1 = 2, stds2 = 3;

	public RobustNormalizeLocalContrast() {}

	public RobustNormalizeLocalContrast(
			final int scaleLevel,
			final int blockRadiusX1,
			final int blockRadiusY1,
			final float stdDevs1,
			final int blockRadiusX2,
			final int blockRadiusY2,
			final float stdDevs2) {
		set( scaleLevel, blockRadiusX1, blockRadiusY1, stdDevs1, blockRadiusX2, blockRadiusY2, stdDevs2 );
	}

	private final void set(
			final int scaleLevel,
			final int blockRadiusX1,
			final int blockRadiusY1,
			final float stdDevs1,
			final int blockRadiusX2,
			final int blockRadiusY2,
			final float stdDevs2) {
		this.scaleLevel = scaleLevel;
		this.brx1 = blockRadiusX1;
		this.bry1 = blockRadiusY1;
		this.stds1 = stdDevs1;
		this.brx2 = blockRadiusX2;
		this.bry2 = blockRadiusY2;
		this.stds2 = stdDevs2;
	}

	public RobustNormalizeLocalContrast(final Map<String,String> params) {
		try {
			set(
					Integer.parseInt(params.get("scalelevel")),
					Integer.parseInt(params.get("brx1")),
					Integer.parseInt(params.get("bry1")),
					Float.parseFloat(params.get("stds1")),
					Integer.parseInt(params.get("brx2")),
					Integer.parseInt(params.get("bry2")),
					Float.parseFloat(params.get("stds2")));
		} catch (final NumberFormatException nfe) {
			throw new IllegalArgumentException("Could not create LocalContrast filter!", nfe);
		}
	}


	@Override
	public ImageProcessor process(final ImageProcessor ip) {
		try {
			mpicbg.trakem2.util.RobustNormalizeLocalContrast.run(
					ip, scaleLevel, brx1, bry1, stds1, brx2, bry2, stds2);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return ip;
	}

	@Override
	public String toXML(final String indent) {
		return new StringBuilder(indent)
		.append("<t2_filter class=\"").append(getClass().getName())
		.append("\" scalelevel=\"").append(scaleLevel)
		.append("\" brx1=\"").append(brx1)
		.append("\" bry1=\"").append(bry1)
		.append("\" stds1=\"").append(stds1)
		.append("\" brx2=\"").append(brx2)
		.append("\" bry2=\"").append(bry2)
		.append("\" stds2=\"").append(stds2)
		.append("\" />\n").toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (null == o) return false;
		if (o.getClass() == RobustNormalizeLocalContrast.class) {
			final RobustNormalizeLocalContrast c = (RobustNormalizeLocalContrast)o;
			return
					scaleLevel == c.scaleLevel &&
					brx1 == c.brx1 &&
					bry1 == c.bry1 &&
					stds1 == c.stds1 &&
					brx2 == c.brx2 &&
					bry2 == c.bry2 &&
					stds2 == c.stds2;
		}
		return false;
	}
}
