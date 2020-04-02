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

import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.utils.ProjectToolbar;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.gui.Roi;

/**
 * Manager class to handle all actions related to layer metadata in a rhizoTrak project.
 * 
 * @author Birgit Moeller
 */
public class RhizoLayerMetadataManager {

	/**
	 * Reference to the corresponding rhizoTrak project.
	 */
	protected RhizoMain rMain;
	
  /**
   * Default constructor.
   * @param rm	Reference to project handle.
   */
  protected RhizoLayerMetadataManager(RhizoMain rm) {
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

    // make new closed polyline as ROI and add to current layer
    this.rMain.getLayerInfo(Display.getFrontLayer()).setROI(roi);

    // clean-up GUI
    ProjectToolbar.setTool(ProjectToolbar.SELECT);
		// select nothing
		Display.clearSelection();
		Display.repaint();
  }
  
  /**
   * Delete ROI for currently active layer if there is any.
   */
  public void clearROI() {
    // delete current ROI for active layer if there is any
    this.rMain.getLayerInfo(Display.getFrontLayer()).clearROI();
  }
  
  /**
   * Delete ROIs of all layers in project.
   */
  public void clearROIsAll() {
  	Display display = Display.getFront();
  	LayerSet currentLayerSet = display.getLayerSet();
  	for (int z=0; z<currentLayerSet.size(); ++z) {
      this.rMain.getLayerInfo(currentLayerSet.getLayer(z)).clearROI();
  	}
  }

  /**
   * Propagates the ROI from the current layer to all others.
   */
  public void propagateROI() {
  	
  	// get ROI of active layer
  	Display display = Display.getFront();
    Layer activeLayer = Display.getFrontLayer(); 
    RhizoROI activeROI = this.rMain.getLayerInfo(activeLayer).getROI();
    
    if (activeROI == null) {
  		Utils.showMessage("rhizoTrak - propagate ROI: active layer has no ROI!");
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
      // make new closed polyline as ROI and add to current layer
      this.rMain.getLayerInfo(layer).setROI((Roi)activeROI.getRoi().clone());
  	}
		ProjectToolbar.setTool(ProjectToolbar.SELECT);
		// select nothing
		Display.clearSelection();
		Display.repaint();
  }
}	