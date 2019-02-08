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
 *  https://prbio-hub.github.io/rhizoTrak
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
 *    https://prbio-hub.github.io/rhizoTrak
 *
 */

/* === original file header below (if any) === */

package de.unihalle.informatik.rhizoTrak.io;

import ij.ImagePlus;
import loci.formats.ChannelSeparator;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;

public class ImageFileHeader {

	public final int width, height, type;
	public final IFormatReader fr;
	
	/** Reads the header of the image file at {@code filepath}
	 * using an {@link IFormatReader} from the bio-formats library,
	 * and then extracts the {link #width}, {@link #height} and {@link #type}
	 * of the image. If the type is not one supported by TrakEM2, the {@link #type}
	 * is set to the value returned by {@link IFormatReader#getPixelType()}.
	 * 
	 * @param filepath
	 * @throws Exception
	 */
	public ImageFileHeader(final String filepath) throws Exception {
		fr = new ChannelSeparator();
		fr.setGroupFiles(false);
		try {
			fr.setId(filepath);
			width = fr.getSizeX();
			height = fr.getSizeY();

			if (fr.isRGB()) {
				type = ImagePlus.COLOR_RGB;
			} else {
				switch (fr.getPixelType()) {
				case FormatTools.INT8:
				case FormatTools.UINT8:
					if (fr.isIndexed() || fr.isFalseColor()) {
						type = ImagePlus.COLOR_256;
					} else {
						type = ImagePlus.GRAY8;
					}
					break;
				case FormatTools.INT16:
				case FormatTools.UINT16:
					type = ImagePlus.GRAY16;
					break;
				case FormatTools.FLOAT:
					type = ImagePlus.GRAY32;
					break;
				default:
					type = fr.getPixelType();
					break;
				}
			}
		} finally {
			fr.close();
		}
	}
	
	/**
	 * @return Whether the {link #type} is supported by ImageJ and TrakEM2.
	 */
	public boolean isSupportedType() {
		switch (type) {
			case ImagePlus.GRAY8:
			case ImagePlus.GRAY16:
			case ImagePlus.GRAY32:
			case ImagePlus.COLOR_256:
			case ImagePlus.COLOR_RGB:
				return true;
			default:
				return false;
		}
	}
}
