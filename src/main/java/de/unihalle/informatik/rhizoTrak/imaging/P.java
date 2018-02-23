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

package de.unihalle.informatik.rhizoTrak.imaging;

import de.unihalle.informatik.rhizoTrak.utils.CachingThread;

public final class P {
	public static final int[] blend(final byte[] pi, final byte[] pm) {
		final int[] p = CachingThread.getOrCreateIntArray(pi.length); // new int[pi.length];
		for (int i=0; i<p.length; ++i) {
			final int c = (pi[i]&0xff);
			p[i] = ((pm[i]&0xff) << 24) | (c << 16) | (c << 8) | c;
		}
		return p;
	}
	
	/** Puts the {@code pi} (the greyscale channel) into the R, G and B components of the returned {@code int[]}
	 * after having multiplied them by the {@code pm} (the alpha channel); the alpha channel gets inserted into
	 * the int[] as well. */
	public static final int[] blendPre(final byte[] pi, final byte[] pm) {
		final int[] p = CachingThread.getOrCreateIntArray(pi.length); // new int[pi.length];
		for (int i=0; i<p.length; ++i) {
			final int a = (pm[i]&0xff);
			final double K = a / 255.0;
			final int c = (int)((pi[i]&0xff) * K + 0.5);
			p[i] = (a << 24) | (c << 16) | (c << 8) | c;
		}
		return p;
	}
	
	public static final int[] blend(final byte[] r, final byte[] g, final byte[] b, final byte[] a) {
		final int[] p = CachingThread.getOrCreateIntArray(r.length); // new int[r.length];
		for (int i=0; i<p.length; ++i) {
			p[i] = ((a[i]&0xff) << 24) | ((r[i]&0xff) << 16) | ((g[i]&0xff) << 8) | (b[i]&0xff);
		}
		return p;
	}

	/** Pre-multiplies alpha. */
	public static final int[] blendPre(final byte[] r, final byte[] g, final byte[] b, final byte[] alpha) {
		final int[] p = CachingThread.getOrCreateIntArray(r.length); // new int[r.length];
		for (int i=0; i<p.length; ++i) {
			final int a = (alpha[i]&0xff);
			final double K = a / 255.0;
			p[i] = (a << 24)
			       | (((int)((r[i]&0xff) * K + 0.5)) << 16)
			       | (((int)((g[i]&0xff) * K + 0.5)) <<  8)
			       |  ((int)((b[i]&0xff) * K + 0.5));
		}
		return p;
	}
	
	public static final int[] blend(final byte[] r, final byte[] g, final byte[] b) {
		final int[] p = CachingThread.getOrCreateIntArray(r.length); // new int[r.length];
		for (int i=0; i<p.length; ++i) {
			p[i] = ((r[i]&0xff) << 16) | ((g[i]&0xff) << 8) | (b[i]&0xff);
		}
		return p;
	}
	

	/** Merges into alpha if outside is null, otherwise returns alpha as is. */
	static public final byte[] merge(final byte[] alpha, byte[] outside) {
		if (null == outside) return alpha;
		for (int i=0; i<alpha.length; ++i) {
			alpha[i] = -1 == outside[i] ? alpha[i] : 0;
		}
		return alpha;
	}

	static public final byte[][] asRGBABytes(final int[] pixels, final byte[] alpha, byte[] outside) {
		merge(alpha, outside); // into alpha
		final byte[] r = CachingThread.getOrCreateByteArray(pixels.length), // new byte[pixels.length],
		             g = CachingThread.getOrCreateByteArray(pixels.length), // new byte[pixels.length],
		             b = CachingThread.getOrCreateByteArray(pixels.length); // new byte[pixels.length];
		for (int i=0; i<pixels.length; ++i) {
			final int x = pixels[i];
			r[i] = (byte)((x >> 16)&0xff);
			g[i] = (byte)((x >>  8)&0xff);
			b[i] = (byte) (x       &0xff);
		}
		return new byte[][]{r, g, b, alpha};
	}

	static public byte[][] asRGBBytes(final int[] pix) {
		final byte[] r = CachingThread.getOrCreateByteArray(pix.length), // new byte[pix.length],
	                 g = CachingThread.getOrCreateByteArray(pix.length), // new byte[pix.length],
	                 b = CachingThread.getOrCreateByteArray(pix
	                		 .length); // new byte[pix.length];
		for (int i=0; i<pix.length; ++i) {
			final int x = pix[i];
			r[i] = (byte)((x >> 16)&0xff);
			g[i] = (byte)((x >>  8)&0xff);
			b[i] = (byte) (x       &0xff);
		}
		return new byte[][]{r, g, b};
	}
}
