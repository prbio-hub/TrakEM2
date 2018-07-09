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

package de.unihalle.informatik.rhizoTrak.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import de.unihalle.informatik.rhizoTrak.imaging.P;
import de.unihalle.informatik.rhizoTrak.persistence.ImageBytes;
import de.unihalle.informatik.rhizoTrak.utils.CachingThread;
import de.unihalle.informatik.rhizoTrak.utils.IJError;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

/** Like {@link RawMipMaps}, but the alpha channel is compressed with GZIP.
 * Uses level 4 compression which is the best balance between speed and
 * compression for the typical alpha mask:
 * 
 * -rw-r--r-- 1 albert albert  5032222 2012-02-21 10:35 mask.tif
 * -rw-r--r-- 1 albert albert    34688 2012-02-24 06:08 mask.tif.1.gz
 * -rw-r--r-- 1 albert albert    34157 2012-02-24 06:07 mask.tif.2.gz
 * -rw-r--r-- 1 albert albert    34234 2012-02-24 06:07 mask.tif.3.gz
 * -rw-r--r-- 1 albert albert    15440 2012-02-24 06:07 mask.tif.4.gz
 * -rw-r--r-- 1 albert albert    15872 2012-02-24 06:07 mask.tif.5.gz
 * -rw-r--r-- 1 albert albert    16034 2012-02-24 06:07 mask.tif.6.gz
 * -rw-r--r-- 1 albert albert    16014 2012-02-24 06:07 mask.tif.7.gz
 * -rw-r--r-- 1 albert albert    11320 2012-02-24 06:07 mask.tif.8.gz
 * -rw-r--r-- 1 albert albert     9795 2012-02-24 06:06 mask.tif.9.gz
 * 
 * The test above was done with a mask (mask.tif) which encodes for the outside of
 * a coordinate transformed image. The program used was e.g. {@code gzip -4 -c mask.tif > mask.tif.4.gz}.
 * 
 * */
public final class RagMipMaps
{
	/** These are both the 4 supported types and the number of channels that each type has. */
	static public final byte GREY = 1,
	                         GREY_ALPHA = 2,
	                         RGB = 3,
	                         RGBA = 4;
	/** Two 4-byte ints, for width and height, and one byte for the type. */
	static public final int HEADER_SIZE = 9;
	
	static public final boolean save(final String path, final byte[][] b, final int width, final int height) {
		if (!ImageSaver.checkPath(path)) return false;
		RandomAccessFile ra = null;
		try {
			ra = new RandomAccessFile(new File(path), "rw");
			// Header: must write as an array or integers get saved with less than 4 bytes
			final byte[] h = new byte[9];
			h[0] = (byte)((width  >> 24) & 0xff);
			h[1] = (byte)((width  >> 16) & 0xff);
			h[2] = (byte)((width  >>  8) & 0xff);
			h[3] = (byte) (width         & 0xff);
			h[4] = (byte)((height >> 24) & 0xff);
			h[5] = (byte)((height >> 16) & 0xff);
			h[6] = (byte)((height >>  8) & 0xff);
			h[7] = (byte) (height        & 0xff);
			h[8] = (byte)  b.length             ; // only possible values: 1,2,3,4; it's the type
			ra.write(h);
			// Write channels
			if (1 == b.length || 3 == b.length) {
				// Without alpha
				for (int i=0; i<b.length; ++i) {
					ra.write(b[i]);
				}
			} else {
				// With alpha, compressed
				// The image channels first:
				for (int i=0; i<b.length-1; ++i) {
					ra.write(b[i]);
				}
				// Now write a compressed alpha channel:
				final ByteArrayOutputStream ba = new ByteArrayOutputStream(b.length);
				final DeflaterOutputStream def = new DeflaterOutputStream(ba, new Deflater(4, false), 1024);
				def.write(b[b.length-1]);
				def.finish();
				def.flush(); // likely not needed
				ra.write((byte[])ImageSaver.Bbuf.get(ba), 0, ba.size());
			}
			return true;

		} catch (Exception e) {
			IJError.print(e);
		} finally {
			if (null != ra) try { ra.close(); } catch (Exception e) { IJError.print(e); }
		}
		return false;
	}
	
	static public final ImageBytes load(final String path) {
		return load(path, 0);
	}

	static private final ImageBytes load(final String path, final int retry) {
		RandomAccessFile ra = null;
		try {
			final File f = new File(path);
			ra = new RandomAccessFile(f, "r");
			final byte[] h = new byte[9];
			read(ra, h);
			final int width =  ((h[0]&0xff) << 24) | ((h[1]&0xff) << 16) | ((h[2]&0xff) << 8) | (h[3]&0xff);
			final int height = ((h[4]&0xff) << 24) | ((h[5]&0xff) << 16) | ((h[6]&0xff) << 8) | (h[7]&0xff);
			final int nCh = h[8];
			final int chLength = width * height;
			final byte[][] ch = CachingThread.getOrCreateByteArray(nCh, chLength); // new byte[nCh][chLength];
			// Types 2 and 4 have a compressed alpha channel
			final int end = 0 == nCh % 2 ? nCh -1 : nCh;
			for (int i=0; i<end; ++i) {
				read(ra, ch[i]);
			}
			if (end < nCh) {
				// Read the alpha channel
				final int len = (int)(f.length() - HEADER_SIZE - chLength * (nCh -1));
				final byte[] a = new byte[len];
				read(ra, a);
				// Decompress the alpha channel
				final ByteArrayInputStream ba = new ByteArrayInputStream(a);
				final InflaterInputStream inf = new InflaterInputStream(ba, new Inflater(false), 1024);
				int sum = 0;
				while (sum < ch[nCh-1].length) {
					int r = inf.read(ch[nCh-1], sum, ch[nCh-1].length - sum);
					if (-1 == r) break;
					sum += r;
				}
			}
			return new ImageBytes(ch, width, height);
		} catch (FileNotFoundException fnfe) {
			Utils.log2("File not found: " + path);
		} catch (Exception e) {
			// Possible: EOFException, NegativeArraySizeException
			// ... all meaning that the file exists but hasn't yet been fully written
			// Rather than going fancy with file locks, just wait 100 ms and retry
			// Retry
			if (retry < 2) {
				// Wait for image to be fully written
				try { Thread.sleep(100); } catch (InterruptedException ie) {}
				return load(path, retry + 1);
			}
			// Else the error is for real
			else IJError.print(e);
		} finally {
			if (null != ra) try { ra.close(); } catch (Exception e) { IJError.print(e); }
		}
		return null;
	}

	static public final BufferedImage read(final String path) {
		try {
			final ImageBytes ib = load(path);
			if (null == ib) return null;
			final byte[][] ch = ib.c;
			// Channel length also specifies the type
			switch (ch.length) {
				case GREY:
					return ImageSaver.createGrayImage(ch[0], ib.width, ib.height);
			}
			try {
				// Given that the BufferedImage is created with an int[], store the byte[] arrays for reuse
				switch (ch.length) {
					case GREY_ALPHA:
						// TODO: price of PRE should be paid when saving, not when reading
						return ImageSaver.createARGBImagePre(P.blendPre(ch[0], ch[1]), ib.width, ib.height);
					case RGB:
						return ImageSaver.createRGBImage(P.blend(ch[0], ch[1], ch[2]), ib.width, ib.height);
					case RGBA:
						// TODO: price of PRE should be paid when saving, not when reading
						return ImageSaver.createARGBImagePre(P.blendPre(ch[0], ch[1], ch[2], ch[3]), ib.width, ib.height);
				}
			} finally {
				CachingThread.storeForReuse(ch);
			}
		} catch (Exception e) {
			IJError.print(e);
		}
		return null;
	}
	
	static private final void read(final RandomAccessFile ra, final byte[] b) throws IOException {
		int s = 0;
		while (s < b.length) {
			int r = ra.read(b, s, b.length - s);
			if (-1 == r) return; // EOF
			s += r;
		}
	}
}
