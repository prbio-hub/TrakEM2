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

package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.Polyline;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.tree.TemplateThing;
import de.unihalle.informatik.rhizoTrak.tree.TemplateTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Scene;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/** Hold all information associated with a layer, especially including information related to RSML import/export
 * 
 * @author posch
 *
 */
public class RhizoLayerInfo {
	
	/**
	 * Reference to the project to which the layer associated with this info belongs to.
	 */
	private Project project;
	
	/**
	 * the layer the rsml has been imported into
	 */
	Layer layer;
	
	/**
	 * The JAXM rsml object for this layer
	 */
	Rsml rsml;
	
	/**
	 * the sha-hash of the layer patch image
	 */
	String imageHash;
	
	/**
	 * ROI associated with the layer, maybe null.
	 */
	private RhizoROI roi = null;
	
	/**
	 * Gravitational direction in image of layer, maybe null.
	 */
	private RhizoGravitationalDirection gravDir = null;
	
	/**
	 * map the treelines generated to the source RSML (top level) roots
	 */
	HashMap<Treeline,RootType> treelineRootMap = new HashMap<Treeline,RootType>();
	
	/**
	 * map RSML (top level) roots to their plant as read from RSML file
	 */
	HashMap<RootType,Scene.Plant> rootPlantMap = new HashMap<RootType,Scene.Plant>();	
	
	public RhizoLayerInfo( Project p, Layer layer, Rsml rsml) {
		this.project = p;
		this.layer = layer;
		this.rsml = rsml;
		updateImageHash();
	}
	
	public void mapTreeline( Treeline tl, RootType root) {
		treelineRootMap.put( tl,  root);
	}
	
	public RootType getRootForTreeline( Treeline tl) {
		return treelineRootMap.get( tl);
	}

	public void mapRoot( Scene.Plant plant, RootType root) {
		rootPlantMap.put( root, plant);
	}
	
	public Scene.Plant getPlantForRoot( RootType root) {
		return rootPlantMap.get( root);
	}

	/**
	 * @return the rsml
	 */
	public Rsml getRsml() {
		return rsml;
	}

	/** Set a new RSML data structure. This invalidates maps.
	 * @param rsml the rsml to set
	 */
	public void setRsml(Rsml rsml) {
		this.rsml = rsml;
		this.treelineRootMap = new HashMap<Treeline,RootType>();
		this.rootPlantMap = new HashMap<RootType,Scene.Plant>();	

	}
	
	/**
	 * return the imageHash
	 * @return
	 */
	public String getImageHash() {
		//make sure to deliver the current hash
		updateImageHash();
		return imageHash;
	}
	
	
	/** update the SHA256 hash for the layer, if a image is found for this layer
	 * @return true if SHA256 hash has been updated
	 */
	public boolean updateImageHash(){
		if(layer.getPatches(false)==null || layer.getPatches(false).size() < 1) return false;
		Patch patch = layer.getPatches(false).get(0);
		if(patch.getImagePlus()==null) return false;
		this.imageHash = RhizoUtils.calculateSHA256(patch.getFilePath());
    	return true;
	}
	
	/*
	 * ROI related stuff.
	 */
	
	/**
	 * Set the ROI for this layer from ImageJ Roi.
	 * @param roi	 ImageJ ROI from which to initialize the internal ROI object.
	 */
	public void setROI(Roi roi) {
		
    // make new closed polyline as ROI and add to current layer
    try {
    	this.addROI(roi);
    } catch (Exception e) {
    		e.printStackTrace();
        Utils.showMessage("Cannot create ROI from ImageJ Roi, adding ROI failed!");
        return;
    }

	}
			
	/**
	 * Set the ROI for this layer.
	 * <p>
	 * Attention: we assume here that the given polyline is already properly integrated
	 * in rhizoTrak's project structure, no GUI or project update is performed!
	 *
	 * @param polyline	Polyine representing the ROI.
	 */
	public void setROI(Polyline polyline) {
		Polygon polygon = polyline.getPerimeter();
		if (polygon.npoints < 3) {
			System.err.println("Found a ROI with less than 3 points, skipping...");
			return;
		}
		// create ImageJ Roi
    float[] xPoints = new float[polygon.npoints];
		float[] yPoints = new float[polygon.npoints];
		for (int i=0; i<polygon.npoints; ++i) {
			xPoints[i] = polygon.xpoints[i];
			yPoints[i] = polygon.ypoints[i];
		}
		PolygonRoi pRoi = new PolygonRoi(xPoints, yPoints, Roi.POLYGON);
		this.roi = new RhizoROI(pRoi, polyline);
	}
	
	/**
	 * Set ROI for this layer from point list, e.g., read from RSML files.
	 * @param points	List of points of ROI polygon.
	 */
	public void setROI(List<Point2D.Double> points) {

		// create ImageJ Roi
    float[] xPoints = new float[points.size()];
		float[] yPoints = new float[points.size()];
		for (int i=0; i<points.size(); ++i) {
			xPoints[i] = (float)points.get(i).x;
			yPoints[i] = (float)points.get(i).y;
		}
		PolygonRoi pRoi = new PolygonRoi(xPoints, yPoints, Roi.POLYGON);
		
    // make new closed polyline as ROI and add to current layer
    try {
    	this.addROI(pRoi);
    } catch (Exception e) {
    		e.printStackTrace();
        Utils.showMessage("Cannot create ROI from points, adding ROI failed!");
        return;
    }
	}
	 
  /**
   * Add ROI to project.
   * @param roi	Roi object to set as ROI for this layer.
   */
  private void addROI(Roi roi) {
  	
		// delete old ROI if there is one
		this.clearROI();
		
		// get the parent thing in project tree where to add the ROI as a child
		ProjectThing roiParentThing = this.getParentThing("roi");

    ProjectThing pt = roiParentThing.createChild("polyline");
    Polyline newPolyline = (Polyline) pt.getObject();

    int n;
    for (n = 0; n < roi.getPolygon().npoints; n++) {
        newPolyline.insertPoint(n, roi.getPolygon().xpoints[n], roi.getPolygon().ypoints[n], 
        		layer.getId());
    }
    newPolyline.insertPoint(n, roi.getPolygon().xpoints[0], roi.getPolygon().ypoints[0], 
    		layer.getId());

    // add new polyline to the project tree
    ProjectTree currentTree = this.project.getProjectTree();
    DefaultMutableTreeNode parentNode = DNDTree.findNode(roiParentThing, currentTree);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(pt);
    ((DefaultTreeModel) currentTree.getModel()).insertNodeInto(node, parentNode, 
    		parentNode.getChildCount());
    
    newPolyline.setVisible(true, true);
    pt.setVisible(true);
    // BM: not sure why, but this seems to be essential here to ensure that the ROI is visible
    newPolyline.repaint(true, layer);

    // store new ROI object
    this.roi = new RhizoROI(roi, newPolyline);
  }
  
	/**
	 * Get ROI for layer.
	 * @return	ROI object.
	 */
	public RhizoROI getROI() {
    System.out.println("Getting roi from layer " + this.layer.getZ());
    if (this.roi != null)
    	System.out.println(this.roi.toString());
    else 
    	System.out.println("null");
		return this.roi;
	}
	
	/**
	 * Get points of ROI.
	 * @return	List of ROI points.
	 */
	public List<Point2D.Double> getROIPoints() {
		Vector<Point2D.Double> points = new Vector<Point2D.Double>();
		Polygon poly = this.roi.getRoi().getPolygon();
		for (int i = 0; i < poly.npoints; i++) {
			System.out.println(poly.xpoints[i] + " " + poly.ypoints[i]);
			points.add(new Point2D.Double(poly.xpoints[i], poly.ypoints[i]));
		}
		return points;
	}

	/**
	 * Delete the ROI from info object and also from the project tree.
	 */
	public void clearROI() {
		
    // find roi child of rootstack
    ProjectThing roiProjectThing = this.getParentThing("roi");
    if (roiProjectThing == null) {
    	return;
    }

		Set<Displayable> deleteSet = new HashSet<Displayable>();

  	for (ProjectThing ptc : roiProjectThing.findChildrenOfTypeR(Polyline.class)) {
  		Polyline pl = (Polyline) ptc.getObject();
  		if (layer == null || pl.getFirstLayer().getId() == layer.getId()) {
  			deleteSet.add(pl);
  		}
  	}

  	if (!deleteSet.isEmpty()) {
  		this.project.removeAll(deleteSet);
  	}
  	
  	// delete ROI object itself
		this.roi = null;
	}
	
	/*
	 * Gravitational direction related stuff.
	 */

	/**
	 * Set gravitational direction and add a proper segment to project.
	 * <p>
	 * The direction is visualized at location (10,10) with a polyline of length 100.
	 * 
	 * @param direction	Direction of gravitation in layer.
	 */
	public void setGravitationalDirection(double direction) {
		this.setGravitationalDirection(direction, new Point2D.Double(10, 10), 100.0);
	}

	/**
	 * Set gravitational direction and add a proper segment to project.
	 * @param direction	Direction of gravitation in layer.
	 * @param sPoint		Start point where to draw the direction in canvas.
	 * @param length		Length of direction polyline.
	 */
	public void setGravitationalDirection(double direction, Point2D.Double sPoint, double length) {
		
		// delete old ROI if there is one
		this.clearGravitationalDirection();
		
		// get the parent thing in project tree where to add the ROI as a child
		ProjectThing dirParentThing = this.getParentThing("gravitationaldirection");
		
		// add new polyline to project
		Polyline newPolyline = null;

		// make new polyline for gravitational direction in current layer
		ProjectThing pt = dirParentThing.createChild("polyline");
		newPolyline = (Polyline) pt.getObject();
		newPolyline.insertPoint(0, (int)sPoint.x, (int)sPoint.y, this.layer.getId());
		newPolyline.insertPoint(1, (int)(sPoint.x + Math.cos(Math.toRadians(direction))*length),
				(int)(sPoint.y + Math.sin(Math.toRadians(direction))*length), this.layer.getId());

		// add new polyline to the project tree
		ProjectTree currentTree = this.project.getProjectTree();
		DefaultMutableTreeNode parentNode = DNDTree.findNode(dirParentThing, currentTree);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(pt);
		((DefaultTreeModel) currentTree.getModel()).insertNodeInto(node, parentNode, 
				parentNode.getChildCount());

		newPolyline.setVisible(true, true);
		pt.setVisible(true);
		// BM: not sure why, but this seems to be essential here to ensure that the ROI is visible
		newPolyline.repaint(true, this.layer);
		Display.repaint();
		
		// store directional data
		this.gravDir = new RhizoGravitationalDirection(newPolyline, direction);		
	}

	/**
	 * Set the gravitational direction for this layer.
	 * <p>
	 * Attention: we assume here that the given polyline is already properly integrated
	 * in rhizoTrak's project structure, no GUI or project update is performed!
	 *
	 * @param polyline	Polyine representing the gravitational direction.
	 */
	public void setGravitationalDirection(Polyline polyline) {
		Polygon polygon = polyline.getPerimeter();
		if (polygon.npoints != 2) {
			System.err.println("Found a gravitational direction with more than 2 points, skipping...");
			return;
		}
		double dx = polygon.xpoints[1] - polygon.xpoints[0];
		double dy = polygon.ypoints[1] - polygon.ypoints[0];
		double dir = Math.toDegrees(Math.atan2(dy, dx));
		if (dir < 0)
			dir += 360;
		this.gravDir = new RhizoGravitationalDirection(polyline, dir);
	}
	
	/**
	 * Get gravitational direction as angle in degrees.
	 * @return	Angle of gravitational direction, 0 degrees point to the right.
	 */
	public double getGravitationalDirection() {
		if (this.gravDir != null)
			return this.gravDir.getDirection();
		return Double.NaN;
	}

	/**
	 * Clear gravitational direction and remove polyline from layer.
	 */
	public void clearGravitationalDirection() {
		
    // find roi child of rootstack
    ProjectThing dirProjectThing = this.getParentThing("gravitationaldirection");
    if (dirProjectThing == null) {
    	return;
    }

		// delete old direction if present
		Set<Displayable> deleteSet = new HashSet<Displayable>();

		for (ProjectThing ptc : dirProjectThing.findChildrenOfTypeR(Polyline.class)) {
			Polyline pl = (Polyline) ptc.getObject();
			if (layer == null || pl.getFirstLayer().getId() == layer.getId()) {
				deleteSet.add(pl);
			}
		}

		if (!deleteSet.isEmpty()) {
			project.removeAll(deleteSet);
		}
	}
	
  /**
   * Helper method to find parent thing in rhizoTrak project tree to keep given child.
   * @param 	child		Identifier for parent thing.
   * @return	Parent project thing.
   */
  private ProjectThing getParentThing(String child) {
  	
    ProjectTree projectTree = this.project.getProjectTree();
    ProjectThing parentThing = RhizoUtils.getParentThingForChild(this.project, child);
    if (parentThing == null) {

    	// search for a rootstack object
    	ProjectThing rootstack = RhizoUtils.getRootstacks(this.project).iterator().next();
    	if (rootstack == null) {
    		Utils.showMessage("rhizoTrak.setROI(): WARNING - cannot find a rootstack in project tree!");
    		return null;
    	}
    	// add ROI things
    	TemplateTree templateTree = this.project.getTemplateTree();
    	TemplateThing template_root = this.project.getTemplateThing("rootstack");
    	TemplateThing template_roi = templateTree.addNewChildType(template_root, child);
    	templateTree.addNewChildType(template_roi, "polyline");

    	// add ROI instance
    	parentThing = rootstack.createChild(child);
    	//add it to the tree
			if (parentThing != null) {
				DefaultMutableTreeNode parentNode = DNDTree.findNode(rootstack, projectTree);
				DefaultMutableTreeNode new_node = new DefaultMutableTreeNode(parentThing);
				((DefaultTreeModel)projectTree.getModel()).insertNodeInto(
						new_node, parentNode, parentNode.getChildCount());
				TreePath treePath = new TreePath(new_node.getPath());
				projectTree.scrollPathToVisible(treePath);
				projectTree.setSelectionPath(treePath);
			}
			// bring the display to front
			if (parentThing.getObject() instanceof Displayable) {
				Display.getFront().getFrame().toFront();
			}
    }
    return parentThing;
  }
}
