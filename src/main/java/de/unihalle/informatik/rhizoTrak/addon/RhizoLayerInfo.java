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
