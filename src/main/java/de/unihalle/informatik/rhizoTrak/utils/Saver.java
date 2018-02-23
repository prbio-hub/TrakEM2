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

package de.unihalle.informatik.rhizoTrak.utils;

import de.unihalle.informatik.rhizoTrak.io.ImageSaver;
import ij.ImagePlus;
import ij.io.FileSaver;

public class Saver
{
	/** Returns the supported formats. */
	static public final String[] formats() {
		return new String[]{".tif", ".tif.zip", ".png", ".jpg"};
	}
	
	private abstract class ASaver {
		float q = 1;
		abstract boolean save(ImagePlus imp, String path);
		void setQuality(float q) {
			this.q = q;
		}
	}
	private class JPEGSaver extends ASaver {
		JPEGSaver() {
			super();
			super.q = FileSaver.getJpegQuality() / 100.0f;
		}
		@Override
		boolean save(ImagePlus imp, String path) {
			return ImageSaver.saveAsJpeg(imp.getProcessor(), path, q, ImagePlus.COLOR_RGB != imp.getType());
		}
	}
	private class PNGSaver extends ASaver {
		@Override
		boolean save(ImagePlus imp, String path) {
			return new  FileSaver(imp).saveAsPng(path);
		}
	}
	private class TIFFSaver extends ASaver {
		@Override
		boolean save(ImagePlus imp, String path) {
			return new FileSaver(imp).saveAsTiff(path);
		}
	}
	private class ZIPSaver extends ASaver {
		@Override
		boolean save(ImagePlus imp, String path) {
			return new FileSaver(imp).saveAsZip(path);
		}
	}

	private final ASaver saver;
	private final String extension;
	
	public Saver(String extension) {
		String ext = extension.toLowerCase();
		if ('.' != ext.charAt(0)) ext = "." + ext;
		this.extension = ext;
		if (".jpeg".equals(ext) || ".jpg".equals(ext)) {
			this.saver = new JPEGSaver();
		} else if (".png".equals(ext)) {
			this.saver = new PNGSaver();
		} else if (".tiff".equals(ext) || ".tif".equals(ext)) {
			this.saver = new TIFFSaver();
		} else if (".tiff.zip".equals(ext) || ".tif.zip".equals(ext) || ".zip".equals(ext)) {
			this.saver = new ZIPSaver();
		} else {
			throw new RuntimeException("Unknown format '" + extension + "'");
		}
	}

	/**
	 * @param q Between 0 and 1.
	 */
	public void setQuality(final float q) {
		this.saver.setQuality(q);
	}
	
	/**
	 * @param imp The {@link ImagePlus} to save.
	 * @param path The path to save the image at, to which the {@link #extension} will be appended if not there.
	 * @return
	 */
	public boolean save(final ImagePlus imp, String path) {
		if (!path.toLowerCase().endsWith(this.extension)) {
			path += this.extension;
		}
		return this.saver.save(imp, path);
	}
	
	public String getExtension() {
		return this.extension;
	}
}
