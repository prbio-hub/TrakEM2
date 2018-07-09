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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.HashMap;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.utils.M;

public class TreeConnector extends Connector {
	
	public TreeConnector(Project project, long id, String title, float width, float height, float alpha,
			boolean visible, Color color, boolean locked, AffineTransform at) {
		super(project, id, title, width, height, alpha, visible, color, locked, at);
		// TODO Auto-generated constructor stub
	}
	
	public TreeConnector(Project project, long id, HashMap<String, String> ht_attr,
			HashMap<Displayable, String> ht_links) {
		super(project, id, ht_attr, ht_links);
		// TODO Auto-generated constructor stub
	}

	public TreeConnector(Project project, String title) {
		super(project, title);
		// TODO Auto-generated constructor stub
	}

	static public class TreeConnectorNode extends Treeline.RadiusNode {
		Treeline connectedTreeline;

		public TreeConnectorNode(final float lx, final float ly, final Layer la) {
			super(lx, ly, la);
			this.connectedTreeline=null;
		}
		public TreeConnectorNode(final float lx, final float ly, final Layer la, final float radius) {
			super(lx, ly, la, radius);
			this.connectedTreeline=null;
		}
		public TreeConnectorNode(final float lx, final float ly, final Layer la, final float radius,Treeline treeline) {
			super(lx, ly, la, radius);
			this.connectedTreeline=treeline;
		}
		/** To reconstruct from XML, without a layer. */
		public TreeConnectorNode(final HashMap<String,String> attr) {
			super(attr);
		}

		@Override
		public final Node<Float> newInstance(final float lx, final float ly, final Layer layer) {
			return new TreeConnectorNode(lx, ly, layer, 0,null);
		}
		@Override
		public void paintData(final Graphics2D g, final Rectangle srcRect,
				final Tree<Float> tree, final AffineTransform to_screen, final Color cc,
				final Layer active_layer) {
			g.setColor(cc);
			g.draw(to_screen.createTransformedShape(new Ellipse2D.Float(x -r, y -r, r+r, r+r)));
		}

		@Override
		public boolean intersects(final Area a) {
			if (0 == r) return a.contains(x, y);
			return M.intersects(a, getArea());
		}

		@Override
		public boolean isRoughlyInside(final Rectangle localbox) {
			final float r = this.r <= 0 ? 1 : this.r;
			return localbox.intersects(x - r, y - r, r + r, r + r);
		}

		@Override
		public Area getArea() {
			if (0 == r) return super.getArea(); // a little square
			return new Area(new Ellipse2D.Float(x-r, y-r, r+r, r+r));
		}

		@Override
		public void paintHandle(final Graphics2D g, final Rectangle srcRect, final double magnification, final Tree<Float> t) {
			final Point2D.Double po = t.transformPoint(this.x, this.y);
			final float x = (float)((po.x - srcRect.x) * magnification);
			final float y = (float)((po.y - srcRect.y) * magnification);

			if (null == parent) {
				g.setColor(brightGreen);
				g.fillOval((int)x - 6, (int)y - 6, 11, 11);
				g.setColor(Color.black);
				g.drawString("o", (int)x -4, (int)y + 3); // TODO ensure Font is proper
			} else {
				g.setColor(Color.white);
				g.fillOval((int)x - 6, (int)y - 6, 11, 11);
				g.setColor(Color.black);
				g.drawString("x", (int)x -4, (int)y + 3); // TODO ensure Font is proper
			}
		}
	}

	static private final Color brightGreen = new Color(33, 255, 0);

}
