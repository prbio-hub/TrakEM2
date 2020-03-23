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

import java.util.HashMap;

import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Scene;

/** Hold all information associated with a layer, especially including information related to RSML import/export
 * 
 * @author posch
 *
 */
public class RhizoLayerInfo {
	
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
	RhizoROI roi = null;
	
	/**
	 * map the treelines generated to the source RSML (top level) roots
	 */
	HashMap<Treeline,RootType> treelineRootMap = new HashMap<Treeline,RootType>();
	
	/**
	 * map RSML (top level) roots to their plant as read from RSML file
	 */
	HashMap<RootType,Scene.Plant> rootPlantMap = new HashMap<RootType,Scene.Plant>();	
	
	public RhizoLayerInfo( Layer layer, Rsml rsml) {
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
	 * Set ROI for this layer.
	 * @param r	ROI object.
	 */
	public void setROI(RhizoROI r) {
		this.roi = r;
	}
	 
	/**
	 * Get ROI for layer.
	 * @return	ROI object.
	 */
	public RhizoROI getROI() {
		return this.roi;
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
}
