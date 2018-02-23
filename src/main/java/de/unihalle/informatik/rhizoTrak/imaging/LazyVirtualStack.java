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

import ij.VirtualStack;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import de.unihalle.informatik.rhizoTrak.utils.IJError;
import de.unihalle.informatik.rhizoTrak.utils.Utils;


public class LazyVirtualStack extends VirtualStack {
	private final List<Callable<ImageProcessor>> tasks = new ArrayList<Callable<ImageProcessor>>();
	private int initial_size;
	public LazyVirtualStack(final int width, final int height, final int initial_size) {
		super();
		Utils.setField(this, ij.ImageStack.class, "width", width);
		Utils.setField(this, ij.ImageStack.class, "height", width);
	}

	public void addSlice(String name) {
		throw new UnsupportedOperationException("LazyVirtualStack accepts Callable<ImageProcessor> slices only.");
	}
	public void deleteSlice(int i) {
		throw new UnsupportedOperationException("LazyVirtualStack: can't remove slices.");
	}
	public void addSlice(final Callable<ImageProcessor> task) {
		tasks.add(task);
	}
	public ImageProcessor getProcessor(final int n) {
		try {
			return tasks.get(n-1).call();
		} catch (Exception e) {
			IJError.print(e);
		}
		return null;
	}
	public int getSize() {
		return Math.max(initial_size, tasks.size());
	}
}
