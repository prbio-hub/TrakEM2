package de.unihalle.informatik.rhizoTrak.addon;

import java.util.HashMap;

import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Scene;

/** Hold all information needed when loading a RSML file into a layer
 * 
 * @author posch
 *
 */
public class RhizoRSMLLayerInfo {
	/**
	 * the layer the rsml has been imported into
	 */
	Layer layer;
	
	/**
	 * The JAXM rsml object for this layer
	 */
	Rsml rsml;
	
	/**
	 * map the treelines generated to the source RSML (top level) roots
	 */
	HashMap<Treeline,RootType> treelineRootMap = new HashMap<Treeline,RootType>();
	
	/**
	 * map RSML (top level) roots to their plant as read from RSML file
	 */
	HashMap<RootType,Scene.Plant> rootPlantMap = new HashMap<RootType,Scene.Plant>();	
	
	public RhizoRSMLLayerInfo( Layer layer, Rsml rsml) {
		this.layer = layer;
		this.rsml = rsml;
	}
	
	public void mapTreeline( RootType root, Treeline tl) {
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
}
