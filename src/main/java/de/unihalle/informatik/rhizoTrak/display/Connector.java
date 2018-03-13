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
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scijava.vecmath.Point3f;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.conflictManagement.ConflictManager;
import de.unihalle.informatik.rhizoTrak.display.addonGui.SplitDialog;
import de.unihalle.informatik.rhizoTrak.utils.M;
import de.unihalle.informatik.rhizoTrak.utils.ProjectToolbar;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.measure.Calibration;
import ij.measure.ResultsTable;

import java.util.Iterator;

/** A one-to-many connection, represented by one source point and one or more target points. The connector is drawn by click+drag+release, defining the origin at click and the target at release. By clicking anywhere else, the connector can be given another target. Points can be dragged and removed.
 * Connectors are meant to represent synapses, in particular polyadic synapses. */
/** actyc: over all added a new TreeEventListener Interface/Implementation to keep track of changes in the linked trees */
public class Connector extends Treeline  implements TreeEventListener{
	
	//actyc: variable to store connected Treelines explicitly 
	protected HashSet<Treeline>  conTreelines = new HashSet<Treeline>();

	public Connector(final Project project, final String title) {
		super(project, title);
	}

	public Connector(final Project project, final long id, final String title, final float width, final float height, final float alpha, final boolean visible, final Color color, final boolean locked, final AffineTransform at) {
		super(project, project.getLoader().getNextId(), title, width, height, alpha, visible, color, locked, at);
	}

	/** Reconstruct from XML. */
	public Connector(final Project project, final long id, final HashMap<String,String> ht_attr, final HashMap<Displayable,String> ht_links) {
		super(project, id, ht_attr, ht_links);
	}

	@Override
	public Tree<Float> newInstance() {
		return new Connector(project, project.getLoader().getNextId(), title, width, height, alpha, visible, color, locked, at);
	}

	@Override
	public Node<Float> newNode(final float lx, final float ly, final Layer la, final Node<?> modelNode) {
		return new ConnectorNode(lx, ly, la, null == modelNode ? 0 : ((ConnectorNode)modelNode).r);
	}

	@Override
	public Node<Float> newNode(final HashMap<String,String> ht_attr) {
		return new ConnectorNode(ht_attr);
	}

	static public class ConnectorNode extends Treeline.RadiusNode {

		public ConnectorNode(final float lx, final float ly, final Layer la) {
			super(lx, ly, la);
		}
		public ConnectorNode(final float lx, final float ly, final Layer la, final float radius) {
			super(lx, ly, la, radius);
		}
		/** To reconstruct from XML, without a layer. */
		public ConnectorNode(final HashMap<String,String> attr) {
			super(attr);
		}

		@Override
		public final Node<Float> newInstance(final float lx, final float ly, final Layer layer) {
			return new ConnectorNode(lx, ly, layer, 0);
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

	public void readLegacyXML(final LayerSet ls, final HashMap<String,String> ht_attr, final HashMap<Displayable,String> ht_links) {
		final String origin = ht_attr.get("origin");
		final String targets = ht_attr.get("targets");
		if (null != origin) {
			final String[] o = origin.split(",");
			String[] t = null;
			int len = 1;
			final boolean new_format = 0 == o.length % 4;
			if (null != targets) {
				t = targets.split(",");
				if (new_format) {
					// new format, with radii
					len += t.length / 4;
				} else {
					// old format, without radii
					len += t.length / 3;
				}
			}
			final float[] p = new float[len + len];
			final long[] lids = new long[len];
			final float[] radius = new float[len];

			// Origin:
			/* X  */ p[0] = Float.parseFloat(o[0]);
			/* Y  */ p[1] = Float.parseFloat(o[1]);
			/* LZ */ lids[0] = Long.parseLong(o[2]);
			if (new_format) {
				radius[0] = Float.parseFloat(o[3]);
			}

			// Targets:
			if (null != targets && targets.length() > 0) {
				final int inc = new_format ? 4 : 3;
				for (int i=0, k=1; i<t.length; i+=inc, k++) {
					/* X  */ p[k+k] = Float.parseFloat(t[i]);
					/* Y  */ p[k+k+1] = Float.parseFloat(t[i+1]);
					/* LZ */ lids[k] = Long.parseLong(t[i+2]);
					if (new_format) radius[k] = Float.parseFloat(t[i+3]);
				}
			}
			//if (!new_format) calculateBoundingBox(null);

			// Now, into nodes:
			final Node<Float> root = new ConnectorNode(p[0], p[1], ls.getLayer(lids[0]), radius[0]);
			for (int i=1; i<lids.length; i++) {
				final Node<Float> nd = new ConnectorNode(p[i+i], p[i+i+1], ls.getLayer(lids[i]), radius[i]);
				root.add(nd, Node.MAX_EDGE_CONFIDENCE);
			}
			setRoot(root);

			// Above, cannot be done with addNode: would call repaint and thus calculateBoundingBox, which would screw up relative coords.

			// Fix bounding box to new tree methods:
			calculateBoundingBox(null);
		}
	}

	public int addTarget(final float x, final float y, final long layer_id, final float r) {
		if (null == root) return -1;
		root.add(new ConnectorNode(x, y, layer_set.getLayer(layer_id), r), Node.MAX_EDGE_CONFIDENCE);
		return root.getChildrenCount() - 1;
	}

	public int addTarget(final double x, final double y, final long layer_id, final double r) {
		return addTarget((float)x, (float)y, layer_id, (float)r);
	}

	protected void mergeTargets(final Connector c) throws NoninvertibleTransformException {
		if (null == c.root) return;
		if (null == this.root) this.root = newNode(c.root.x, c.root.y, c.root.la, c.root);
		final AffineTransform aff = new AffineTransform(c.at);
		aff.preConcatenate(this.at.createInverse());
		final float[] f = new float[4];
		for (final Map.Entry<Node<Float>,Byte> e : c.root.getChildren().entrySet()) {
			final ConnectorNode nd = (ConnectorNode)e.getKey();
			f[0] = nd.x;
			f[1] = nd.y;
			f[2] = nd.x + nd.r; // make the radius be a point to the right of x,y
			f[3] = nd.y;
			aff.transform(f, 0, f, 0, 2);
			this.root.add(new ConnectorNode(f[0],f[1], nd.la, Math.abs(f[2] - f[0])), e.getValue().byteValue());
		}
	}

	public boolean intersectsOrigin(final Area area, final Layer la) {
		if (null == root || root.la != la) return false;
		final Area a = root.getArea();
		a.transform(this.at);
		return M.intersects(area, a);
	}

	/** Whether the area of the root node intersects the world coordinates {@code wx}, {@code wy} at {@link Layer} {@code la}. */
	public boolean intersectsOrigin(final double wx, final double wy, final Layer la) {
		if (null == root || root.la != la) return false;
		final Area a = root.getArea();
		a.transform(this.at);
		return a.contains(wx, wy);
	}

	/** Returns the set of Displayable objects under the origin point, or an empty set if none. */
	public Set<Displayable> getOrigins(final Class<?> c) {
		final int m = c.getModifiers();
		return getOrigins(c, Modifier.isAbstract(m) || Modifier.isInterface(m));
	}
	public Set<Displayable> getOrigins(final Class<?> c, final boolean instance_of) {
		if (null == root) return new HashSet<Displayable>();
		return getUnder(root, c, instance_of);
	}

	private final Set<Displayable> getUnder(final Node<Float> node, final Class<?> c, final boolean instance_of) {
		final Area a = node.getArea();
		a.transform(this.at);
		final HashSet<Displayable> targets = new HashSet<Displayable>(layer_set.find(c, node.la, a, false, instance_of));
		targets.remove(this);
		return targets;
	}

	/** Returns the set of Displayable objects under the origin point, or an empty set if none. */
	public Set<Displayable> getOrigins() {
		if (null == root) return new HashSet<Displayable>();
		return getUnder(root, Displayable.class, true);
	}

	public List<Set<Displayable>> getTargets(final Class<?> c, final boolean instance_of) {
		final List<Set<Displayable>> al = new ArrayList<Set<Displayable>>();
		if (null == root || !root.hasChildren()) return al;
		for (final Node<Float> nd : root.getChildrenNodes()) {
			al.add(getUnder(nd, c, instance_of));
		}
		return al;
	}

	/** Returns the list of sets of visible Displayable objects under each target, or an empty list if none. */
	public List<Set<Displayable>> getTargets(final Class<?> c) {
		final int m = c.getModifiers();
		return getTargets(c, Modifier.isAbstract(m) || Modifier.isInterface(m));
	}

	/** Returns the list of sets of visible Displayable objects under each target, or an empty list if none. */
	public List<Set<Displayable>> getTargets() {
		return getTargets(Displayable.class, true);
	}

	public int getTargetCount() {
		if (null == root) return 0;
		return root.getChildrenCount();
	}

	static public void exportDTD(final StringBuilder sb_header, final HashSet<String> hs, final String indent) {
		Tree.exportDTD(sb_header, hs, indent);
		final String type = "t2_connector";
		if (hs.contains(type)) return;
		hs.add(type);
		sb_header.append(indent).append("<!ELEMENT t2_connector (t2_node*,").append(Displayable.commonDTDChildren()).append(")>\n");
		Displayable.exportDTD(type, sb_header, hs, indent);
	}

	@Override
	public Connector clone(final Project pr, final boolean copy_id) {
		final long nid = copy_id ? this.id : pr.getLoader().getNextId();
		final Connector copy = new Connector(pr, nid, title, width, height, this.alpha, true, this.color, this.locked, this.at);
		copy.root = null == this.root ? null : this.root.clone(pr);
		copy.addToDatabase();
		if (null != copy.root) copy.cacheSubtree(copy.root.getSubtreeNodes());
		return copy;
	}

	private final void insert(final Node<Float> nd, final ResultsTable rt, final int i, final Calibration cal, final float[] f) {
		f[0] = nd.x;
		f[1] = nd.y;
		this.at.transform(f, 0, f, 0, 1);
		//
		rt.incrementCounter();
		rt.addLabel("units", cal.getUnits());
		rt.addValue(0, this.id);
		rt.addValue(1, i);
		rt.addValue(2, f[0] * cal.pixelWidth);
		rt.addValue(3, f[1] * cal.pixelHeight);
		rt.addValue(4, nd.la.getZ() * cal.pixelWidth); // NOT pixelDepth!
		rt.addValue(5, ((ConnectorNode)nd).r);
		rt.addValue(6, nd.confidence);
	}

	@Override
	public ResultsTable measure(ResultsTable rt) {
		if (null == root) return rt;
		if (null == rt) rt = Utils.createResultsTable("Connector results", new String[]{"id", "index", "x", "y", "z", "radius", "confidence"});
		final Calibration cal = layer_set.getCalibration();
		final float[] f = new float[2];
		insert(root, rt, 0, cal, f);
		if (null == root.children) return rt;
		for (int i=0; i<root.children.length; i++) {
			insert(root.children[i], rt, i+1, cal, f);
		}
		return rt;
	}

	public List<Point3f> getTargetPoints(final boolean calibrated) {
		if (null == root) return null;
		final List<Point3f> targets = new ArrayList<Point3f>();
		if (null == root.children) return targets;
		final float[] f = new float[2];
		for (final Node<Float> nd : root.children) {
			targets.add(fix(nd.asPoint(), calibrated, f));
		}
		return targets;
	}

	public Coordinate<Node<Float>> getCoordinateAtOrigin() {
		if (null == root) return null;
		return createCoordinate(root);
	}

	/** Get a coordinate for target i. */
	public Coordinate<Node<Float>> getCoordinate(final int i) {
		if (null == root || !root.hasChildren()) return null;
		return createCoordinate(root.children[i]);
	}

	@Override
	public String getInfo() {
		if (null == root) return "Empty";
		return new StringBuilder("Targets: ").append(root.getChildrenCount()).append('\n').toString();
	}

	/** If the root node is in Layer @param la, then all nodes are removed. */
	@Override
	protected boolean layerRemoved(final Layer la) {
		if (null == root) return true;
		if (root.la == la) {
			super.removeNode(root); // and all its children
			return true;
		}
		// Else, remove any targets
		return super.layerRemoved(la);
	}

	/** Takes the List of Connector instances and adds the targets of all to the first one.
	 *  Removes the others from the LayerSet and from the Project.
	 *  If any of the Connector instances cannot be removed, returns null. */
	static public Connector merge(final List<Connector> col) throws NoninvertibleTransformException {
		if (null == col || 0 == col.size()) return null;
		final Connector base = col.get(0);
		for (final Connector con : col.subList(1, col.size())) {
			base.mergeTargets(con);
			if (!con.remove2(false)) {
				Utils.log("FAILED to merge Connector " + con + " into " + base);
				return null;
			}
		}
		return base;
	}
	//actyc: same as in tree; change the !=Pen>return situation to ==Pen{} to get the shiny CON Tool working
	/** Add a root or child nodes to root. */
	@Override
	public void mousePressed(final MouseEvent me, final Layer layer, final int x_p, final int y_p, final double mag) {
		if (ProjectToolbar.PEN == ProjectToolbar.getToolId()) {

			if (-1 == last_radius) {
				last_radius = 10 / (float)mag;
			}

			if (null != root) {
				// transform the x_p, y_p to the local coordinates
				int x_pl = x_p;
				int y_pl = y_p;
				if (!this.at.isIdentity()) {
					final Point2D.Double po = inverseTransformPoint(x_p, y_p);
					x_pl = (int)po.x;
					y_pl = (int)po.y;
				}

				Node<Float> found = findNode(x_pl, y_pl, layer, mag);
				setActive(found);

				if (null != found) {
					if (2 == me.getClickCount()) {
						setLastMarked(found);
						setActive(null);
						return;
					}
					if (me.isShiftDown() && Utils.isControlDown(me)) {
						if (found == root) {
							// Remove the whole Connector
							layer_set.addChangeTreesStep();
							if (remove2(true)) {
								setActive(null);
								layer_set.addChangeTreesStep();
							} else {
								layer_set.removeLastUndoStep(); // no need
							}
							return;
						} else {
							// Remove point
							removeNode(found);
						}
					}
				} else {
					if (2 == me.getClickCount()) {
						setLastMarked(null);
						return;
					}
					// Add new target point to root:
					found = newNode(x_pl, y_pl, layer, root);
					((ConnectorNode)found).setData(last_radius);
					addNode(root, found, (byte) -3); // aeekz
					setActive(found);
					repaint(true, layer);
				}
				return;
			} else {
				// First point
				root = newNode(x_p, y_p, layer, null); // world coords, so calculateBoundingBox will do the right thing
				addNode(null, root, (byte)0);
				((ConnectorNode)root).setData(last_radius);
				setActive(root);
			}
		}
		if(ProjectToolbar.CON == ProjectToolbar.getToolId()){
			if(root != null){
				
				// transform the x_p, y_p to the local coordinates
				int x_pl = x_p;
				int y_pl = y_p;
				if (!this.at.isIdentity()) {
					final Point2D.Double po = inverseTransformPoint(x_p, y_p);
					x_pl = (int)po.x;
					y_pl = (int)po.y;
				}
				int oldSize = conTreelines.size();
				
				if(!me.isShiftDown() && !Utils.isControlDown(me)){
					this.getProject().getRhizoAddons().bindConnectorToTreeline(layer, x_p, y_p, mag, this, me);
					return;
				}

				if (me.isShiftDown() && Utils.isControlDown(me)) {
					Utils.log("list of connected trees:");
					for(Treeline tree: conTreelines){
						Utils.log("Treeline: "+tree.getId());
					}
					return;
				}
				return;
			}
			return;
		}
		return;
	}

	@Override
	protected boolean requireAltDownToEditRadius() { return false; }

	@Override
	protected Rectangle getBounds(final Collection<? extends Node<Float>> nodes) {
		final Rectangle nb = new Rectangle();
		Rectangle box = null;
		for (final RadiusNode nd : (Collection<RadiusNode>)(Collection)nodes) {
			final int r = 0 == nd.r ? 1 : (int)nd.r;
			if (null == box) box = new Rectangle((int)nd.x - r, (int)nd.y - r, r+r, r+r);
			else {
				nb.setBounds((int)nd.x - r, (int)nd.y - r, r+r, r+r);
				box.add(nb);
			}
		}
		return box;
	}

	/** If the root node (the origin) does not remain within the range, this Connector is left empty. */
	@Override
	public boolean crop(final List<Layer> range) {
		if (null == root) return true; // it's empty already
		if (!range.contains(root.la)) {
			this.root = null;
			synchronized (node_layer_map) {
				clearCache();
			}
			return true;
		}
		return super.crop(range);
	}
	
	/*actyc: sanity check for the connector*/
	
	public boolean sanityCheck(){
		if(!conTreelines.isEmpty()){
			if(root!=null && root.hasChildren()){
				ArrayList<Node<Float>> targets = new ArrayList<Node<Float>>();
				ArrayList<Node<Float>> deleteList = targets;
				
				for (final Node<Float> nd : root.children) {
					targets.add(nd);
				}
				
				for(Treeline tree: conTreelines){
					Node<Float> treeRoot = tree.getRoot();
					if(treeRoot==null) return false;
					Layer treeRootLayer = tree.getRoot().getLayer();
					if(treeRootLayer==null) return false;
					Point2D result = RhizoAddons.changeSpace(treeRoot.getX(),treeRoot.getY(),tree.getAffineTransform(),this.getAffineTransform());
					if(result==null) return false;

					//make sure all the connected treelines still exists
					if(!tree.getClass().equals(Treeline.class)) {
						if(!conTreelines.remove(tree)) return false;
					}
					
					
					
					//make sure all the connected treelines have a target node at there root
					boolean found = false;
					//Utils.log("active treeline: "+ tree.getId());
					for (final Node<Float> nd : targets) {
						//Utils.log("xdist: "+ Math.abs((float)result.getX()-nd.getX()) +" ydist: "+Math.abs((float)result.getY()-nd.getY()));
						
						if( Math.abs((float)result.getX()-nd.getX())<3 && Math.abs((float)result.getY()-nd.getY())<3 && nd.getLayer().equals(treeRootLayer)) {
							found = true;
							deleteList.remove(nd);
							//Utils.log("inside the if");
							break;
						}
					}
					if(!found){
						Node<Float> newTarget = newNode((float) result.getX(),(float) result.getY(), treeRoot.getLayer(), root);
						found = ((ConnectorNode)newTarget).setData(last_radius);
						found = addNode(root, newTarget,(byte)-3);					
					}
				}
				//delete all unused nodes
				for(Node<Float> nd: deleteList){
					this.removeNode(nd);
				}
				//set the root of the connector to the appropriate position
				float[] posi = RhizoAddons.findConnectorRootPosition(this);
				if(posi!=null)
				{
					this.getRoot().setPosition(posi);
				}
			} else {
				//conTreelines is not empty but the connector have no children
				for(Treeline tree: conTreelines){
					//take tree root
					Node<Float> treeRoot = tree.getRoot();
					//transfer the position with the connector affine-transform
					Point2D result = RhizoAddons.changeSpace(treeRoot.getX(),treeRoot.getY(),tree.getAffineTransform(),this.getAffineTransform());
					if(result==null) return false;
					//make a connector leaf and put it on the calculate position
					Node<Float> newTarget = newNode((float) result.getX(),(float) result.getY(), treeRoot.getLayer(), root);
					((ConnectorNode)newTarget).setData(last_radius);
					if(!addNode(root, newTarget, (byte)-3)) return false;
				}
				//set the root of the connector to the appropriate position
				float[] posi = RhizoAddons.findConnectorRootPosition(this);
				if(posi!=null)
				{
					this.getRoot().setPosition(posi);
				}
			}
		} else {
			if(root!=null && root.hasChildren()){
				for(Node<Float> nd: root.children){
					this.removeNode(nd);
				}
			}
		}
		//Utils.log(root.children.length);
//		if(layer!=null)
//		{
//			repaint();
//		}
		return true;
	}
	
	//get layers with connected treelines
	public List<Layer> getConnectedLayerList()
	{
		List<Layer> result = new ArrayList<Layer>();
		for(Treeline treeline:conTreelines)
		{
			result.add(treeline.getFirstLayer());
		}
		return result;
	}
	
	/* actyc: methodes to interact with the connected treelines */
	
	public ArrayList<Treeline> getConTreelines() {
		ArrayList<Treeline> result = new ArrayList<Treeline>();
		result.addAll(this.conTreelines);
		return result;
	}
	
	public void setConTreelines(HashSet<Treeline> newList){
		conTreelines=newList;
	}
	
	public boolean addConTreeline(Treeline newTreeline){
		if(conTreelines.contains(newTreeline)) return false;
		boolean added = conTreelines.add(newTreeline);
		newTreeline.addTreeEventListener(this);
		sanityCheck();
        RhizoAddons rhizoAddons = this.getProject().getRhizoAddons();
        ConflictManager conflictManager = rhizoAddons.getConflictManager();
		conflictManager.processChange(newTreeline, this);
		return added;
	}
	
	public boolean removeConTreeline(Treeline tobeRemoved){
		boolean removed = conTreelines.remove(tobeRemoved);
		tobeRemoved.removeTreeEventListener(this);
		sanityCheck();
        RhizoAddons rhizoAddons = this.getProject().getRhizoAddons();
        ConflictManager conflictManager = rhizoAddons.getConflictManager();
		conflictManager.processChange(tobeRemoved, this);
		return removed;
	}
	
	/* actyc: start of the listener implementation */
	public void eventAppeared(TreeEvent te){
		//oh captain my captain the event appeared
		if(te.getEventMessage().equals("drag")){
			sanityCheck();
		}

		//copytreelineconnector
		if(te.getEventMessage().equals("copy")){
			addConTreeline(te.getInterestingTrees().get(0));
			sanityCheck();
		}
		
		if(te.getEventMessage().equals("split"))
		{
			ArrayList<Treeline> trees = te.getInterestingTrees();
			if(!this.getProject().getRhizoAddons().splitDialog)
			{
				this.getProject().getRhizoAddons().splitDialog = true;
				new SplitDialog(trees,project.getRhizoAddons());
			}
			
			
		}
		if(te.getEventMessage().equals("remove")){
			removeConTreeline((Treeline) te.getSource());
                        if(conTreelines.size()==0)
                        {
                            this.remove2(false);
                        }
		}
	}
	
	public Connector getConnector(){
		return this;
	}
	
	public void removeAllTreelines(){
            for (Iterator<Treeline> it = conTreelines.iterator(); it.hasNext();) {
                Treeline currentTree = it.next();
                if(currentTree != null)
                {
                    currentTree.removeTreeEventListener(this);
                    this.getProject().getRhizoAddons().getConflictManager().processChange(currentTree, this);
                    it.remove();
                    //this.removeConTreeline(currentTree);
                }
            }
	}
	
	
}

