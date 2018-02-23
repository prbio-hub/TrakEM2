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

package de.unihalle.informatik.rhizoTrak.persistence;


import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.imaging.P;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import mpicbg.trakem2.util.Downsampler;
import mpicbg.trakem2.util.Downsampler.Pair;

public final class DownsamplerMipMaps
{
	static private final ImageBytes asBytes(final ByteProcessor bp) {
		 return new ImageBytes(new byte[][]{(byte[])bp.getPixels()}, bp.getWidth(), bp.getHeight());
	}
	static private final ImageBytes asBytes(final ShortProcessor sp) {
		return asBytes((ByteProcessor)sp.convertToByte(true));
	}
	static private final ImageBytes asBytes(final FloatProcessor fp) {
		return asBytes((ByteProcessor)fp.convertToByte(true));
	}
	static private final ImageBytes asBytes(final ColorProcessor cp) {
		return new ImageBytes(P.asRGBBytes((int[])cp.getPixels()), cp.getWidth(), cp.getHeight());
	}
	
	static private final ImageBytes asBytes(final ByteProcessor bp, final ByteProcessor mask) {
		 return new ImageBytes(new byte[][]{(byte[])bp.getPixels(), (byte[])mask.getPixels()}, bp.getWidth(), bp.getHeight());
	}
	static private final ImageBytes asBytes(final ShortProcessor sp, final ByteProcessor mask) {
		return asBytes((ByteProcessor)sp.convertToByte(true), mask);
	}
	static private final ImageBytes asBytes(final FloatProcessor fp, final ByteProcessor mask) {
		return asBytes((ByteProcessor)fp.convertToByte(true), mask);
	}
	static private final ImageBytes asBytes(final ColorProcessor cp, final ByteProcessor mask) {
		return new ImageBytes(P.asRGBABytes((int[])cp.getPixels(), (byte[])mask.getPixels(), null), cp.getWidth(), cp.getHeight());
	}

	// TODO the int[] should be preserved for color images
	static public final ImageBytes[] create(
			final Patch patch,
			final int type,
			final ImageProcessor ip,
			final ByteProcessor alpha,
			final ByteProcessor outside) {
		// Create pyramid
		final ImageBytes[] p = new ImageBytes[Loader.getHighestMipMapLevel(patch) + 1];

		if (null == alpha && null == outside) {
			int i = 1;
			switch (type) {
				case ImagePlus.GRAY8:
					ByteProcessor bp = (ByteProcessor)ip;
					p[0] = asBytes(bp);
					while (i < p.length) {
						bp = Downsampler.downsampleByteProcessor(bp);
						p[i++] = asBytes(bp);
					}
					break;
				case ImagePlus.GRAY16:
					ShortProcessor sp = (ShortProcessor)ip;
					p[0] = asBytes(sp);
					Pair<ShortProcessor, byte[]> rs;
					while (i < p.length) {
						rs = Downsampler.downsampleShort(sp);
						sp = rs.a;
						p[i++] = new ImageBytes(new byte[][]{rs.b}, sp.getWidth(), sp.getHeight());
					}
					break;
				case ImagePlus.GRAY32:
					FloatProcessor fp = (FloatProcessor)ip;
					p[0] = asBytes(fp);
					Pair<FloatProcessor, byte[]> rf;
					while (i < p.length) {
						rf = Downsampler.downsampleFloat(fp);
						fp = rf.a;
						p[i++] = new ImageBytes(new byte[][]{rf.b}, fp.getWidth(), fp.getHeight());
					}
					break;
				case ImagePlus.COLOR_RGB:
					ColorProcessor cp = (ColorProcessor)ip;
					p[0] = asBytes(cp); // TODO the int[] could be reused
					Pair<ColorProcessor, byte[][]> rc;
					while (i < p.length) {
						rc = Downsampler.downsampleColor(cp);
						cp = rc.a;
						p[i++] = new ImageBytes(rc.b, cp.getWidth(), cp.getHeight());
					}
					break;
			}
		} else {
			// Alpha channel
			final ByteProcessor[] masks = new ByteProcessor[p.length];
			if (null != alpha && null != outside) {
				// Use both alpha and outside:
				final byte[] b1 = (byte[])alpha.getPixels(),
				             b2 = (byte[])outside.getPixels();
				for (int i=0; i<b1.length; ++i) {
					b1[i] = b2[i] != -1 ? 0 : b1[i]; // 'outside' is a binary mask, qualitative. -1 means 255
				}
				masks[0] = alpha;
				//
				int i = 1;
				Pair<ByteProcessor,ByteProcessor> pair;
				ByteProcessor a = alpha,
				              o = outside;
				while (i < p.length) {
					pair = Downsampler.downsampleAlphaAndOutside(a, o);
					a = pair.a;
					o = pair.b;
					masks[i] = a; // o is already combined into it
					++i;
				}
			} else {
				// Only one of the two is not null:
				if (null == alpha) {
					masks[0] = outside;
					int i = 1;
					while (i < p.length) {
						masks[i] = Downsampler.downsampleOutside(masks[i-1]);
						++i;
					}
				} else {
					masks[0] = alpha;
					int i = 1;
					while (i < p.length) {
						masks[i] = Downsampler.downsampleByteProcessor(masks[i-1]);
						++i;
					}
				}
			}
			// Image channels
			int i = 1;
			switch (type) {
				case ImagePlus.GRAY8:
					ByteProcessor bp = (ByteProcessor)ip;
					p[0] = asBytes(bp, masks[0]);
					while (i < p.length) {
						bp = Downsampler.downsampleByteProcessor(bp);
						p[i] = asBytes(bp, masks[i]);
						++i;
					}
					break;
				case ImagePlus.GRAY16:
					ShortProcessor sp = (ShortProcessor)ip;
					p[0] = asBytes(sp, masks[0]);
					while (i < p.length) {
						final Pair< ShortProcessor, byte[] > rs = Downsampler.downsampleShort(sp);
						sp = rs.a;
						p[i] = new ImageBytes(new byte[][]{rs.b, (byte[])masks[i].getPixels()}, sp.getWidth(), sp.getHeight());
						++i;
					}
					break;
				case ImagePlus.GRAY32:
					FloatProcessor fp = (FloatProcessor)ip;
					p[0] = asBytes(fp, masks[0]);
					while (i < p.length) {
						final Pair< FloatProcessor, byte[] > rs = Downsampler.downsampleFloat( fp );
						fp = rs.a;
						p[i] = new ImageBytes(new byte[][]{rs.b, (byte[])masks[i].getPixels()}, fp.getWidth(), fp.getHeight());
						++i;
					}
					break;
				case ImagePlus.COLOR_RGB:
					ColorProcessor cp = (ColorProcessor)ip;
					p[0] = asBytes(cp, masks[0]); // TODO the int[] could be reused
					while (i < p.length) {
						final Pair< ColorProcessor, byte[][] > rs = Downsampler.downsampleColor( cp );
						cp = rs.a;
						final byte[][] rgb = rs.b;
						p[i] = new ImageBytes(new byte[][]{rgb[0], rgb[1], rgb[2], (byte[])masks[i].getPixels()}, cp.getWidth(), cp.getHeight());
						++i;
					}
					break;
			}
		}

		return p;
	}
}
