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

package de.unihalle.informatik.rhizoTrak.display.inspect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;

import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Mode;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.graphics.DefaultGraphicsSource;
import de.unihalle.informatik.rhizoTrak.display.graphics.GraphicsSource;
import mpicbg.models.AffineModel2D;
import mpicbg.models.PointMatch;
import mpicbg.trakem2.transform.TransformMesh;


/**
 * A view-only mode that shows, using {@link LayerSet#getOverlay()}, the
 * triangle of the transform mesh in the {@link Patch} under the cursor.
 *
 * @author Stephan Saalfeld and Albert Cardona
 */
public class InspectPatchTrianglesMode implements Mode {

	protected final Display display;
	final private DefaultGraphicsSource gs = new DefaultGraphicsSource();

	public InspectPatchTrianglesMode(final Display display) {
		this.display = display;
	}

	@Override
	public GraphicsSource getGraphicsSource() {
		return gs;
	}

	@Override
	public boolean canChangeLayer() { return false; }

	@Override
	public boolean canZoom() { return true; }

	@Override
	public boolean canPan() { return true; }

	@Override
	public boolean isDragging() { return false; }

	@Override
	public void undoOneStep() {}

	@Override
	public void redoOneStep() {}


	// Initialize with a Shape that can never be painted
	protected final ShapeProxy proxy = new ShapeProxy(new Rectangle(-1, -1, 0, 0));
	protected final Inspector inspector = new Inspector();

	protected class Inspector extends Thread {
		private boolean quit = false;
		private double wx, wy;
		private Layer layer;

		Inspector() {
			setPriority(Thread.NORM_PRIORITY);
			start();
		}

		private void quit() {
			this.quit = true;
		}

		private void viewAt(final double wX, final double wY, final Layer l) {
			synchronized (this) {
				this.wx = wX;
				this.wy = wY;
				this.layer = l;
				notify();
			}
		}

		@Override
		public void run() {
			double wX = 0, wY = 0;
			Layer l = null;

			while (!isInterrupted()) {
				try {
					synchronized (this) {
						if (this.wx == wX && this.wy == wY && this.layer == l) {
							// Nothing changed
							wait();
						}
					}
				} catch (final InterruptedException ie) {
					ie.printStackTrace();
					return;
				}

				if (quit) return;

				// Acquire local copy
				synchronized (this) {
					wX = this.wx;
					wY = this.wy;
					l = this.layer;
				}

				// Find a Patch under wx, wy
				final Collection<Displayable> ps = l.find(Patch.class, wX, wY, true, false);
				if (ps.isEmpty()) {
					continue;
				}

				final Patch patch = (Patch) ps.iterator().next();

				// Find the triangle, if any
				if (null != patch.getCoordinateTransform()) { // TODO update this to hasCoordinateTransform
					final double[] f = new double[]{wx, wy};
					final AffineTransform ai = patch.getAffineTransformCopy();
					final AffineTransform aiInverse = new AffineTransform( ai );
					try {
						aiInverse.invert();
					} catch ( final NoninvertibleTransformException x ) {}
					aiInverse.transform( f, 0, f, 0, 1 );
					final TransformMesh mesh = new TransformMesh( patch.getCoordinateTransform(), patch.getMeshResolution(), patch.getOWidth(), patch.getOHeight() );
					final AffineModel2D triangle = mesh.closestTargetAffine( f );
					final ArrayList< PointMatch > pm = mesh.getAV().get( triangle );
					final GeneralPath path = new GeneralPath();
					final double[] p1 = pm.get( 0 ).getP2().getW();
					final double[] q = new double[ 2 ];
					ai.transform( p1, 0, q, 0, 1 );
					path.moveTo( q[ 0 ], q[ 1 ] );
					for ( int i = 1; i < pm.size(); ++i )
					{
						final double[] p = pm.get( i ).getP2().getW();
						ai.transform( p, 0, q, 0, 1 );
						path.lineTo( q[ 0 ], q[ 1 ] );
					}
					path.closePath();
					proxy.set( path );
					display.getCanvas().repaint(proxy.getBounds(), 0, false);
				}
			}
		}
	}

	@Override
	public void mousePressed(final MouseEvent me, final int x_p, final int y_p, final double magnification) {
		display.getLayerSet().getOverlay().add( proxy, Color.YELLOW, new BasicStroke( 1 ) );
	}

	@Override
	public void mouseDragged(final MouseEvent me, final int x_p, final int y_p, final int x_d, final int y_d, final int x_d_old, final int y_d_old) {
		synchronized (inspector) {
			inspector.viewAt(x_d, y_d, display.getLayer());
			inspector.notifyAll();
		}
	}

	@Override
	public void mouseReleased(final MouseEvent me, final int x_p, final int y_p, final int x_d, final int y_d, final int x_r, final int y_r) {
		display.getLayerSet().getOverlay().remove(proxy);
	}

	@Override
	public void srcRectUpdated(final Rectangle srcRect, final double magnification) {}

	@Override
	public void magnificationUpdated(final Rectangle srcRect, final double magnification) {}

	@Override
	public boolean apply() {
		return cancel();
	}

	@Override
	public boolean cancel() {
		display.getLayerSet().getOverlay().remove(proxy);
		synchronized (inspector) {
			inspector.quit();
			inspector.notifyAll();
		}
		return true;
	}

	@Override
	public Rectangle getRepaintBounds() {
		return display.getCanvas().getSrcRect();
	}

}
