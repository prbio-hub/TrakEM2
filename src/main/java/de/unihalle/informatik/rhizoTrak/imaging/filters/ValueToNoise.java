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
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.Map;
import java.util.Random;

public class ValueToNoise implements IFilter
{
	final static private void processFloatNaN(final FloatProcessor ip, final double min, final double max) {
		final double scale = max - min;
		final Random rnd = new Random();
		final int n = ip.getWidth() * ip.getHeight();
		for (int i =0; i < n; ++i) {
			final float v = ip.getf(i);
			if (Float.isNaN(v))
				ip.setf(i, (float)(rnd.nextDouble() * scale + min));
		}
	}

	final static private void processFloat(final FloatProcessor ip, final float value, final double min, final double max) {
		final double scale = max - min;
		final Random rnd = new Random();
		final int n = ip.getWidth() * ip.getHeight();
		for (int i =0; i < n; ++i) {
			final float v = ip.getf(i);
			if (v == value)
				ip.setf(i, (float)(rnd.nextDouble() * scale + min));
		}
	}

	final static private void processGray(final ImageProcessor ip, final int value, final int min, final int max) {
		final int scale = max - min + 1;
		final Random rnd = new Random();
		final int n = ip.getWidth() * ip.getHeight();
		for (int i =0; i < n; ++i) {
			final int v = ip.get(i);
			if (v == value)
				ip.set(i, rnd.nextInt(scale) + min);
		}
	}

	final static private void processColor(final ColorProcessor ip, final int value, final int min, final int max) {
		final int scale = max - min + 1;
		final Random rnd = new Random();
		final int n = ip.getWidth() * ip.getHeight();
		for (int i =0; i < n; ++i) {
			final int v = ip.get(i);
			if (v == value)
			{
				final int r = rnd.nextInt(scale) + min;
				final int g = rnd.nextInt(scale) + min;
				final int b = rnd.nextInt(scale) + min;

				ip.set(i, (((((0xff << 8) | r) << 8) | g) << 8) | b);
			}
		}
	}


	protected double value = Double.NaN, min = 0, max = 255;

	public ValueToNoise() {}

	public ValueToNoise(
			final double value,
			final double min,
			final double max) {
		set(value, min, max);
	}

	private final void set(
			final double value,
			final double min,
			final double max) {
		this.value = value;
		this.min = min;
		this.max = max;
	}

	public ValueToNoise(final Map<String,String> params) {
		try {
			set(
					Double.parseDouble(params.get("value")),
					Double.parseDouble(params.get("min")),
					Double.parseDouble(params.get("max")));
		} catch (final NumberFormatException nfe) {
			throw new IllegalArgumentException("Could not create ValueToNoise filter!", nfe);
		}
	}


	@Override
	public ImageProcessor process(final ImageProcessor ip) {
		try {
			if (FloatProcessor.class.isInstance(ip)) {
				if (Double.isNaN(value))
					processFloatNaN((FloatProcessor)ip, min, max);
				else
					processFloat((FloatProcessor)ip, (float)value, min, max);
			} else {
				if (ColorProcessor.class.isInstance(ip))
					processColor((ColorProcessor)ip, (int)Math.round(value), (int)Math.round(min), (int)Math.round(max));
				else
					processGray(ip, (int)Math.round(value), (int)Math.round(min), (int)Math.round(max));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return ip;
	}

	@Override
	public String toXML(final String indent) {
		return new StringBuilder(indent)
		.append("<t2_filter class=\"").append(getClass().getName())
		.append("\" value=\"").append(value)
		.append("\" min=\"").append(min)
		.append("\" max=\"").append(max)
		.append("\" />\n").toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (null == o) return false;
		if (o.getClass() == ValueToNoise.class) {
			final ValueToNoise c = (ValueToNoise)o;
			return
					(Double.isNaN(value) && Double.isNaN(c.value) || value == c.value) &&
					min == c.min &&
					max == c.max;
		}
		return false;
	}
}
