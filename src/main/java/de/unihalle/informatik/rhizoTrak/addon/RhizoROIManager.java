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

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.Polyline;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.gui.Roi;

public class RhizoROIManager {

	protected RhizoMain rMain;
	
  protected RhizoROIManager(RhizoMain rm) {
  	this.rMain = rm;
  }
  
  public void setROI() {
  	
  	// gets the currently drawn ROI from the GUI
    Display display = Display.getFront();
    Roi roi = display.getRoi();
    if (roi == null) {
        Utils.showMessage("SetROI: no selection given!");
        return;
    }

    Display.clearSelection();

    // delete current ROI for active layer if there is any
    this.clearROI();
    
    Project project = display.getProject();
    ProjectThing roiParentThing = RhizoUtils.getOneRoiParentForROIs(project);
    if (roiParentThing == null) {
        Utils.showMessage("RhizoROI.getROI: Create treeline: WARNING  can not find a rootstack in project tree able to hold a polyline");
        return;
    }
    
    // make new closed polyline
    try {
        ProjectThing pt = roiParentThing.createChild("polyline");
        pt.setTitle(pt.getUniqueIdentifier());

        Polyline newPolyline = (Polyline) pt.getObject();

        int n;
        for (n = 0; n < roi.getPolygon().npoints; n++) {
            newPolyline.insertPoint(n, roi.getPolygon().xpoints[n], roi.getPolygon().ypoints[n], 
            		Display.getFrontLayer().getId());
        }
        newPolyline.insertPoint(n, roi.getPolygon().xpoints[0], roi.getPolygon().ypoints[0], 
        		Display.getFrontLayer().getId());

        // add new polyline to the project tree
        ProjectTree currentTree = project.getProjectTree();
        DefaultMutableTreeNode parentNode = DNDTree.findNode(roiParentThing, currentTree);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(pt);
        ((DefaultTreeModel) currentTree.getModel()).insertNodeInto(node, parentNode, 
        		parentNode.getChildCount());
        Display.clearSelection();
        
        // remember ROI
        Layer activeLayer = Display.getFrontLayer();
        this.rMain.getLayerInfo(activeLayer).setROI(new RhizoROI(this.rMain, newPolyline, 
        		roi.getType() == Roi.RECTANGLE));
    } catch (Exception e) {
    		e.printStackTrace();
        Utils.showMessage("Cannot create polyline");
        return;
    }
  }
  
  public void clearROI() {
    Display display = Display.getFront();
    Project project = display.getProject();
    Layer activeLayer = Display.getFrontLayer(); 

    // find roi child of rootstack
    ProjectThing roiProjectThing = RhizoUtils.getOneRoiParentForROIs(project);
    if (roiProjectThing == null) {
        Utils.showMessage("RhizoROI.getROI: Create treeline: WARNING  can not find a rootstack in project tree able to hold a polyline");
        return;
    }

    // delete current ROI for active layer if there is any
    this.clearROI(roiProjectThing, activeLayer);
  }
  
  public void clearROIsAll() {
    Display display = Display.getFront();
    Project project = display.getProject();

    // find roi child of rootstack
    ProjectThing roiProjectThing = RhizoUtils.getOneRoiParentForROIs(project);
    if (roiProjectThing == null) {
        Utils.showMessage("RhizoROI.getROI: Create treeline: WARNING  can not find a rootstack in project tree able to hold a polyline");
        return;
    }

    // delete current ROI for all layers if there is any
    this.clearROI(roiProjectThing, null);
  }

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
  }

  public void propagateROI() {
  	
  }
}	