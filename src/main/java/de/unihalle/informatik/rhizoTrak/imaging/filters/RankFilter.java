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

import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

import java.util.Map;

public class RankFilter implements IFilter
{
	protected double radius = 2;
	/** See {@link RankFilters}. */
	protected int type = RankFilters.MEDIAN;
	
	public RankFilter() {}

	/**
	 * @param radius The radius around every pixel to get values from for the specific algorithm {@code type}.
	 * @param type Any of the types in {@link RankFilters} such as {@link RankFilters#MEDIAN}, {@link RankFilters#DESPECKLE}, etc.
	 */
	public RankFilter(double radius, int type) {
		this.radius = radius;
	}
	
	public RankFilter(Map<String,String> params) {
		try {
			this.radius = Double.parseDouble(params.get("radius"));
			this.type = Integer.parseInt(params.get("type"));
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Cannot create RankFilter!", nfe);
		}
	}

	@Override
	public ImageProcessor process(ImageProcessor ip) {
		RankFilters rf = new RankFilters();
		rf.rank(ip, radius, RankFilters.MEDIAN);
		return ip;
	}

	@Override
	public String toXML(String indent) {
		return new StringBuilder(indent)
			.append("<t2_filter class=\"").append(getClass().getName())
			.append("\" radius=\"").append(radius)
			.append("\" type=\"").append(type)
			.append("\" />\n").toString();
	}
	@Override
	public boolean equals(final Object o) {
		if (null == o) return false;
		if (o.getClass() == getClass()) {
			final RankFilter r = (RankFilter)o;
			return type == r.type && radius == r.radius;
		}
		return false;
	}
}
