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

import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Polyline;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.tree.TemplateThing;
import de.unihalle.informatik.rhizoTrak.tree.TemplateTree;
import de.unihalle.informatik.rhizoTrak.utils.ProjectToolbar;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.gui.Roi;

/**
 * Manager class to handle all actions related to ROIs.
 * 
 * @author Birgit Moeller
 */
public class RhizoROIManager {

	/**
	 * Reference to the corresponding rhizoTrak project.
	 */
	protected RhizoMain rMain;
	
  /**
   * Default constructor.
   * @param rm	Reference to project handle.
   */
  protected RhizoROIManager(RhizoMain rm) {
  	this.rMain = rm;
  }
  
  /**
   * Set ROI for active layer from given (closed) polyline selection.
   */
  public void setROI() {
  	
  	// gets the currently drawn ROI from the GUI
    Display display = Display.getFront();
    Roi roi = display.getRoi();
    if (roi == null) {
        Utils.showMessage("SetROI: no selection given!");
        return;
    }

    // delete current ROI for active layer if there is any
    this.clearROI();
    
    Project project = display.getProject();
    ProjectTree projectTree = project.getProjectTree();
    ProjectThing roiParentThing = RhizoUtils.getOneRoiParentForROIs(project);
    if (roiParentThing == null) {

    	// search for a rootstack object
    	ProjectThing rootstack = RhizoUtils.getRootstacks(project).iterator().next();
    	if (rootstack == null) {
    		Utils.showMessage("rhizoTrak.setROI(): WARNING - cannot find a rootstack in project tree!");
    		return;
    	}
    	// add ROI things
    	TemplateTree templateTree = project.getTemplateTree();
    	TemplateThing template_root = project.getTemplateThing("rootstack");
    	TemplateThing template_roi = templateTree.addNewChildType(template_root, "roi");
    	templateTree.addNewChildType(template_roi, "polyline");

    	// add ROI instance
    	roiParentThing = rootstack.createChild("roi");
    	//add it to the tree
			if (roiParentThing != null) {
				DefaultMutableTreeNode parentNode = DNDTree.findNode(rootstack, projectTree);
				DefaultMutableTreeNode new_node = new DefaultMutableTreeNode(roiParentThing);
				((DefaultTreeModel)projectTree.getModel()).insertNodeInto(
						new_node, parentNode, parentNode.getChildCount());
				TreePath treePath = new TreePath(new_node.getPath());
				projectTree.scrollPathToVisible(treePath);
				projectTree.setSelectionPath(treePath);
			}
			// bring the display to front
			if (roiParentThing.getObject() instanceof Displayable) {
				Display.getFront().getFrame().toFront();
			}
    }
    
    // make new closed polyline as ROI and add to current layer
    try {
    	this.addROI(project, roiParentThing, Display.getFrontLayer(), roi);
    } catch (Exception e) {
    		e.printStackTrace();
        Utils.showMessage("Cannot create polyline");
        return;
    }
		ProjectToolbar.setTool(ProjectToolbar.SELECT);
		// select nothing
		Display.clearSelection();
		Display.repaint();
  }
  
  /**
   * Delete ROI for currently active layer if there is any.
   */
  public void clearROI() {
    Display display = Display.getFront();
    Project project = display.getProject();
    Layer activeLayer = Display.getFrontLayer(); 

    // find roi child of rootstack
    ProjectThing roiProjectThing = RhizoUtils.getOneRoiParentForROIs(project);
    if (roiProjectThing == null) {
    	return;
    }

    // delete current ROI for active layer if there is any
    this.clearROI(roiProjectThing, activeLayer);
  }
  
  /**
   * Delete ROIs of all layers in project.
   */
  public void clearROIsAll() {
    Display display = Display.getFront();
    Project project = display.getProject();

    // find roi child of rootstack
    ProjectThing roiProjectThing = RhizoUtils.getOneRoiParentForROIs(project);
    if (roiProjectThing == null) {
    	return;
    }

    // delete current ROI for all layers if there is any
    this.clearROI(roiProjectThing, null);
  }

  /**
   * Propagates the ROI from the current layer to all others.
   */
  public void propagateROI() {
  	
  	// get ROI of active layer
  	Display display = Display.getFront();
    Project project = display.getProject();
    Layer activeLayer = Display.getFrontLayer(); 
    RhizoROI activeROI = this.rMain.getLayerInfo(activeLayer).getROI();
    
    if (activeROI == null) {
  		Utils.showMessage("rhizoTrak - propagate ROI: active layer has no ROI!");
    	return;
    }

    // find roi child of rootstack
    ProjectThing roiProjectThing = RhizoUtils.getOneRoiParentForROIs(project);
    if (roiProjectThing == null) {
  		Utils.showMessage("rhizoTrak - propagate ROI: no ROI found!");
    	return;
    }

  	// process each layer
    Layer layer;
  	LayerSet currentLayerSet = display.getLayerSet();
  	for (int z=0; z<currentLayerSet.size(); ++z) {
  		// skip active layer
  		if (z == activeLayer.getZ())
  			continue;
  		layer = currentLayerSet.getLayer(z);
  		this.clearROI(roiProjectThing, layer);
  		this.addROI(project, roiProjectThing, layer, (Roi)activeROI.getRoi().clone());
  	}
		ProjectToolbar.setTool(ProjectToolbar.SELECT);
		// select nothing
		Display.clearSelection();
		Display.repaint();
  }

  /**
   * Adds a ROI to the given project and layer.
   * @param project				Target project.
   * @param parentThing		Parent node under which to add the ROI in the project tree.
   * @param layer					Target layer.
   * @param roi						The ROI to add.
   */
  private void addROI(Project project, ProjectThing parentThing, Layer layer, Roi roi) {
    ProjectThing pt = parentThing.createChild("polyline");
    Polyline newPolyline = (Polyline) pt.getObject();

    int n;
    for (n = 0; n < roi.getPolygon().npoints; n++) {
        newPolyline.insertPoint(n, roi.getPolygon().xpoints[n], roi.getPolygon().ypoints[n], 
        		layer.getId());
    }
    newPolyline.insertPoint(n, roi.getPolygon().xpoints[0], roi.getPolygon().ypoints[0], 
    		layer.getId());

    // add new polyline to the project tree
    ProjectTree currentTree = project.getProjectTree();
    DefaultMutableTreeNode parentNode = DNDTree.findNode(parentThing, currentTree);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(pt);
    ((DefaultTreeModel) currentTree.getModel()).insertNodeInto(node, parentNode, 
    		parentNode.getChildCount());
    
    newPolyline.setVisible(true, true);
    pt.setVisible(true);
    // BM: not sure why, but this seems to be essential here to ensure that the ROI is visible
    newPolyline.repaint(true, layer);

    // store ROI
    this.rMain.getLayerInfo(layer).setROI(new RhizoROI(this.rMain, roi, newPolyline, 
    		roi.getType() == Roi.RECTANGLE));
  }
  
  /**
   * Deletes the ROI of the given layer.
   * @param pt			Project thing under which to delete the ROI(s).
   * @param layer		Target layer where to delete the ROIs.
   */
  private void clearROI(ProjectThing pt, Layer layer) {
  	Set<Displayable> deleteSet = new HashSet<Displayable>();

  	for (ProjectThing ptc : pt.findChildrenOfTypeR(Polyline.class)) {
  		Polyline pl = (Polyline) ptc.getObject();
  		if (layer == null || pl.getFirstLayer().getId() == layer.getId()) {
  			deleteSet.add(pl);
  		}
  	}

  	if (!deleteSet.isEmpty()) {
  		Project project = rMain.getProject();
  		project.removeAll(deleteSet);
  	}
  	
  	// delete from layer info object as well
  	this.rMain.getLayerInfo(layer).setROI(null);
  }

}	