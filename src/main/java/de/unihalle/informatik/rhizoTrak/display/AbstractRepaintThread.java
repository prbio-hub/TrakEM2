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
Copyright (C) 2007-2009 Albert Cardona and Rodney Douglas.

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

import java.awt.Component;
import java.awt.Rectangle;
import java.util.LinkedList;

import de.unihalle.informatik.rhizoTrak.utils.CachingThread;

public abstract class AbstractRepaintThread extends CachingThread {

	final protected AbstractOffscreenThread off;
	private final java.util.List<PaintEvent> events = new LinkedList<PaintEvent>();
	private final Component target;

	public AbstractRepaintThread(final Component target, final String name, final AbstractOffscreenThread off) {
		super(name);
		this.target = target;
		this.off = off;
		setPriority(Thread.NORM_PRIORITY + 1);
		try { setDaemon(true); } catch (Exception e) { e.printStackTrace(); }
		start();
	}

	private class PaintEvent {
		final Rectangle clipRect;
		final boolean update_graphics;
		PaintEvent(final Rectangle clipRect, final boolean update_graphics) {
			this.clipRect = clipRect;
			this.update_graphics = update_graphics; // java is sooo verbose... this class is just a tuple!
		}
	}

	/** Queue a new request for painting, updating offscreen graphics. */
	public final void paint(final Rectangle clipRect) {
		paint(clipRect, true);
	}

	/** Queue a new request for painting. */
	public void paint(final Rectangle clipRect, final boolean update_graphics) {
		//Utils.log2("update_graphics: " + update_graphics);
		//Utils.printCaller(this, 5);
		// queue the event and signal a repaint request
		synchronized (events) {
			events.add(new PaintEvent(clipRect, update_graphics));
			events.notifyAll();
		}
	}

	/** Will gracefully kill this thread by breaking its infinite wait-for-event loop, and also call cancel on all registered offscreen threads. */
	public void quit() {
		interrupt();
		// notify and finish
		synchronized (events) {
			events.notifyAll();
		}
		//
		off.quit();
	}

	public void run() {
		while (!isInterrupted()) {
			try {
				// wait until anyone issues a repaint event
				synchronized (events) {
					while (0 == events.size()) {
						if (isInterrupted()) return;
						try { events.wait(); } catch (InterruptedException ie) {}
					}
				}

				if (isInterrupted()) {
					return;
				}

				// wait a bit to catch fast subsequent events
				// 	10 miliseconds
				try { Thread.sleep(10); } catch (InterruptedException ie) {}

				// obtain all events up to now and clear the event queue
				final PaintEvent[] pe;
				synchronized (events) {
					pe = new PaintEvent[events.size()];
					events.toArray(pe);
					events.clear();
				}
				if (0 == pe.length) {
					continue;
				}

				// obtain repaint parameters from merged events
				Rectangle clipRect = pe[0].clipRect;
				boolean update_graphics = pe[0].update_graphics;
				for (int i=1; i<pe.length; i++) {
					if (null != clipRect) {
						if (null == pe[i].clipRect) clipRect = null; // all
						else clipRect.add(pe[i].clipRect);
					} // else 'null' clipRect means repaint the entire canvas
					if (!update_graphics) update_graphics = pe[i].update_graphics;
					else if (null == clipRect) break;
				}

				// issue an offscreen thread if necessary
				if (update_graphics) {
					handleUpdateGraphics(target, clipRect);
				}

				// repaint
				/*
				if (null == clipRect) target.repaint(0, 0, 0, target.getWidth(), target.getHeight()); // using super.repaint() causes infinite thread loops in the IBM-1.4.2-ppc
				else target.repaint(0, clipRect.x, clipRect.y, clipRect.width, clipRect.height);
				*/

				// Crazy idea: paint NOW
				final java.awt.Graphics g = target.getGraphics();
				if (null != g) {
					// Ensure full clip rect
					g.setClip(0, 0, target.getWidth(), target.getHeight());
					target.paint(g);
					g.dispose();
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/** Child classes need to extend this method for handling the need of recreating offscreen images. */
	abstract protected void handleUpdateGraphics(Component target, Rectangle clipRect);

	/** Waits until the offscreen thread is finished with the current cycle. */
	public void waitForOffs() {
		off.waitOnRepaintCycle();
	}
}
