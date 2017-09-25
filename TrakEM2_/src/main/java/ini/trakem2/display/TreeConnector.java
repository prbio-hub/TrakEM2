package ini.trakem2.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.HashMap;

import ini.trakem2.Project;
import ini.trakem2.utils.M;

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
