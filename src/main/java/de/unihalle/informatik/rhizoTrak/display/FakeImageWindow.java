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

/**

TrakEM2 plugin for ImageJ(C).
Copyright (C) 2005-2009 Albert Cardona and Rodney Douglas.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 

You may contact Albert Cardona at acardona at ini.phys.ethz.ch
Institute of Neuroinformatics, University of Zurich / ETH, Switzerland.
**/

package de.unihalle.informatik.rhizoTrak.display;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;

import java.awt.Graphics;

import de.unihalle.informatik.rhizoTrak.utils.Utils;

/** A class to prevent ROIs from failing. */
public class FakeImageWindow extends ImageWindow {

	private static final long serialVersionUID = 1L;
	private Display display;

	public FakeImageWindow(ImagePlus imp, ImageCanvas ic, Display display) {
		super(imp.getTitle());
		this.display = display;
		ij = IJ.getInstance();
		this.imp = imp;
		this.ic = ic;
		imp.setWindow(this);
		WindowManager.addWindow(this);
	}

	/** Returns the display's FakeImagePlus. */
	public ImagePlus getImagePlus() {
		return super.getImagePlus();
	}

	// just in case .. although it never should be shown
	public void drawInfo(Graphics g) {
		Utils.log("FakeImageWindow: can't drawInfo");
	}

	public void paint(Graphics g) {
		Utils.log("FakeImageWindow: can't paint");
	}

	/* // problematic, ImageJ doesn't quit
	public boolean close() {
		WindowManager.removeWindow(this);
		if (ij.quitting()) return true; // just let go
		if (display.remove(true)) { // check
			display = null;
			return true;
		} else return false;
	}
	*/

	public void updateImage(ImagePlus imp) {
		Utils.log("FakeImageWindow: Can't updateImage");
	}

	public boolean isClosed() {
		return null == display;
	}

	public void paste() {
		Utils.log("FakeImageWindow: can't paste"); // TODO test how
	}
}
