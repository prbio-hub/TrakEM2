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

package de.unihalle.informatik.rhizoTrak.display.d3d;

import java.awt.event.MouseEvent;

import org.scijava.vecmath.Point3d;

import de.unihalle.informatik.rhizoTrak.display.Coordinate;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.measure.Calibration;
import ij3d.Content;
import ij3d.Image3DUniverse;
import ij3d.behaviors.InteractiveBehavior;
import ij3d.behaviors.Picker;

/** A class to provide the behavior on control-clicking on
content in the 3D viewer.  This will attempt to center
the front TrakEM2 Display on the clicked point */
public class ControlClickBehavior extends InteractiveBehavior {

	protected Image3DUniverse universe;
	protected LayerSet ls;

	public ControlClickBehavior(final Image3DUniverse univ, final LayerSet ls) {
		super(univ);
		this.universe = univ;
		this.ls = ls;
	}

	@Override
	public void doProcess(final MouseEvent e) {
		if(!e.isControlDown() ||
				e.getID() != MouseEvent.MOUSE_PRESSED) {
			super.doProcess(e);
			return;
		}
		final Picker picker = universe.getPicker();
		final Content content = picker.getPickedContent(e.getX(),e.getY());
		if(content==null)
			return;
		final Point3d p = picker.getPickPointGeometry(content,e);
		if(p==null) {
			Utils.log("No point was found on content "+content);
			return;
		}
		final Display display = Display.getFront(ls.getProject());
		if(display==null) {
			// If there's no Display, just return...
			return;
		}
		if (display.getLayerSet() != ls) {
			Utils.log("The LayerSet instances do not match");
			return;
		}
		if(ls==null) {
			Utils.log("No LayerSet was found for the Display");
			return;
		}
		final Calibration cal = ls.getCalibration();
		if(cal==null) {
			Utils.log("No calibration information was found for the LayerSet");
			return;
		}
		final double scaledZ = p.z/cal.pixelWidth;
		final Layer l = ls.getNearestLayer(scaledZ);
		if(l==null) {
			Utils.log("No layer was found nearest to "+scaledZ);
			return;
		}
		final Coordinate<?> coordinate = new Coordinate<Object>(p.x/cal.pixelWidth,p.y/cal.pixelHeight,l,null);
		display.center(coordinate);
	}
}
