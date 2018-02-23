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

package de.unihalle.informatik.rhizoTrak.display;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Collection;

import de.unihalle.informatik.rhizoTrak.display.graphics.DefaultGraphicsSource;
import de.unihalle.informatik.rhizoTrak.display.graphics.GraphicsSource;

public class DefaultMode implements Mode {

	final private DefaultGraphicsSource gs = new DefaultGraphicsSource();
	final private Display display;

	public DefaultMode(final Display display) {
		this.display = display;
	}

	public GraphicsSource getGraphicsSource() {
		return gs;
	}

	public boolean canChangeLayer() { return true; }
	public boolean canZoom() { return true; }
	public boolean canPan() { return true; }

	private boolean dragging = false;

	public boolean isDragging() {
		return dragging;
	}

	public void mousePressed(MouseEvent me, int x_p, int y_p, double magnification) {
		final Collection<Displayable> sel = display.getSelection().getSelected();
		dragging = false; //reset
		for (final Displayable d : sel) {
			if (d.contains(x_p, y_p)) {
				dragging = true;
				break;
			}
		}
		final Collection<Displayable> affected = display.getSelection().getAffected();
		if (display.getLayerSet().prepareStep(affected)) {
			display.getLayerSet().addTransformStep(affected);
		}
	}
	public void mouseDragged(MouseEvent me, int x_p, int y_p, int x_d, int y_d, int x_d_old, int y_d_old) {
		int dx = x_d - x_d_old;
		int dy = y_d - y_d_old;

		if (dragging) execDrag(me, dx, dy);
	}
	private void execDrag(MouseEvent me, int dx, int dy) {
		if (0 == dx && 0 == dy) return;
		// drag all selected and linked
		display.getSelection().translate(dx, dy);
	}
	public void mouseReleased(MouseEvent me, int x_p, int y_p, int x_d, int y_d, int x_r, int y_r) {
		// Record current state for selected Displayable set, if there was any change:
		final int dx = x_r - x_p;
		final int dy = y_r - y_p;
		if (0 != dx || 0 != dy) {
			display.getLayerSet().addTransformStep(display.getSelection().getAffected()); // all selected and their links: i.e. all that will change
		}

		dragging = false;
	}

	public void undoOneStep() {
		display.getLayerSet().undoOneStep();
		Display.repaint(display.getLayerSet());
	}

	public void redoOneStep() {
		display.getLayerSet().redoOneStep();
		Display.repaint(display.getLayerSet());
	}

	public boolean apply() { return true; } // already done
	public boolean cancel() { return true; } // nothing to cancel

	public Rectangle getRepaintBounds() {
		return display.getSelection().getLinkedBox();
	}

	public void srcRectUpdated(Rectangle srcRect, double magnification) {}
	public void magnificationUpdated(Rectangle srcRect, double magnification) {}
}
