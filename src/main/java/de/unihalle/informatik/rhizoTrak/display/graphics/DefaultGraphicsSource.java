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

package de.unihalle.informatik.rhizoTrak.display.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Paintable;
import de.unihalle.informatik.rhizoTrak.utils.ProjectToolbar;

/** Handles default mode, i.e. just plain images without any transformation handles of any kind. */
public class DefaultGraphicsSource implements GraphicsSource {

	/** Returns the list given as argument without any modification. */
	public List<? extends Paintable> asPaintable(final List<? extends Paintable> ds) {
		return ds;
	}

	/** Paints bounding boxes of selected objects as pink and active object as white. */
	public void paintOnTop(final Graphics2D g, final Display display, final Rectangle srcRect, final double magnification) {
		if (ProjectToolbar.getToolId() >= ProjectToolbar.PENCIL) { // PENCIL == SPARE2
			return;
		}
		g.setColor(Color.pink);
		Displayable active = display.getActive();
		final Rectangle bbox = new Rectangle();
		for (final Displayable d : display.getSelection().getSelected()) {
			d.getBoundingBox(bbox);
			if (d == active) {
				g.setColor(Color.white);
				//g.drawPolygon(d.getPerimeter());
				g.drawRect(bbox.x, bbox.y, bbox.width, bbox.height);
				g.setColor(Color.pink);
			} else {
				//g.drawPolygon(d.getPerimeter());
				g.drawRect(bbox.x, bbox.y, bbox.width, bbox.height);
			}
		}
	}
}
