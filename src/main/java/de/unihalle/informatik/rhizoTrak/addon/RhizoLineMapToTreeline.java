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

package de.unihalle.informatik.rhizoTrak.addon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.unihalle.informatik.MiToBo_xml.MTBXMLRootImageAnnotationType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentStatusType;
import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;

public class RhizoLineMapToTreeline
{
	private RhizoMain rhizoMain;
	
	public RhizoLineMapToTreeline(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
	}
	
//	/**
//	 * Converts a lineMap data structure into the representation as a treeline.
//	 * 
//	 * @param lineMap - Map of lines with ids. The lines are stored as points in a map.
//	 * Where the points also have an id, which is only unique for the line they belong to.
//	 */
//	public void convertLineMapToTreeLine_old(Map<Integer, Map<Integer, Point>> lineMap)
//	{
//		// get layers
//		Display display = Display.getFront();	
//		Project project = display.getProject();
//		ProjectTree projectTree = project.getProjectTree();
//		
//		LayerSet layerSet = display.getLayerSet();  
//		ArrayList<Layer> layers = layerSet.getLayers();
//		
//		if(layers.isEmpty()) 
//		{
//			return; // add warning message
//		}
//		
//		// get image names
//		List<Patch> patches = layerSet.getAll(Patch.class);
//		ImagePlus imagePlus = null;
//		if(!patches.isEmpty()) 
//		{	
//			imagePlus = patches.get(0).getImagePlus();
//		}
//			
//		String[] imageNames = new String[layers.size()];
//		if(null != imagePlus) 
//		{	
//			imageNames = imagePlus.getImageStack().getSliceLabels();
//		}
//		
//		
//		Layer currentLayer = display.getLayer(); // order of rootsets has to correspond to the layer if we don't care about image names
//			
//		int index = 0;
//		for ( Layer layer : layers )
//		{
//			if ( layer.equals(currentLayer) )
//			{
//				break;
//			}
//			index++;
//		}	
//		Utils.log("Layer index: " + index);
//		
//		for ( int id : lineMap.keySet() )
//		{
//			ProjectThing possibleParent = RhizoAddons.findParentAllowing("treeline", project);
//
//			MTBXMLRootImageAnnotationType rootSet = MTBXMLRootImageAnnotationType.Factory.newInstance();
//				
//			rootSet.setImagename(imageNames[index]);
//			rootSet.setRootSetID(index);
//				
//			// create treeline
//			ProjectThing treelineThing = possibleParent.createChild("treeline");
//				
//			// create project tree
//			DefaultMutableTreeNode parentNode = DNDTree.findNode(possibleParent, projectTree);
//			DefaultMutableTreeNode node = new DefaultMutableTreeNode(treelineThing);
//			((DefaultTreeModel) projectTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
//				
//			// get treeline (now existing)
//			Treeline treeline = (Treeline) treelineThing.getObject();
//			treeline.setLayer(currentLayer);
//				
//			// TODO: this is a workaround for the repainting issues that occur when creating new nodes out of a mtbxml file
//			currentLayer.mtbxml = true;
//				
//			Map<Integer, Point> line = lineMap.get(id);
//    		// create node -> ID map to later assign parents and children according to segment IDs and parent IDs
//    		HashMap<Integer, RadiusNode> nodeIDmap = new HashMap<Integer, RadiusNode>();
//				
//    		// Create a nodeIDmap of all points
//			for ( int pointId : line.keySet() )
//			{
//				Point p = line.get(pointId);
//
//				if(p.getPredecessor() == -1)
//    			{
//    				RadiusNode root = new RadiusNode((float) p.getX(), (float) p.getY(), currentLayer, (float) p.getRadius());
//    				nodeIDmap.put(-1, root);
//    			}
//    			RadiusNode currentNode = new RadiusNode((float) p.getX(), (float) p.getY(), currentLayer, (float) p.getRadius());
//    			nodeIDmap.put(pointId, currentNode);
//			}
//
//			// Add all points to treeline
//			for ( int pointId : line.keySet() )
//			{
//				Point p = line.get(pointId);
//					
//				// assuming that default status is defined
//    			byte s = (byte) 0;
//    			//byte s = (byte) RhizoProjectConfig.STATUS_UNDEFINED;
//    				
//    			if(p.getPredecessor() == -1)
//    			{
//    				treeline.addNode(null, nodeIDmap.get(p.getPredecessor()), s);
//    				treeline.addNode(nodeIDmap.get(p.getPredecessor()), nodeIDmap.get(pointId), s);
//    				treeline.setRoot(nodeIDmap.get(p.getPredecessor()));
//    			}
//    			else
//    			{
//    				treeline.addNode(nodeIDmap.get(p.getPredecessor()), nodeIDmap.get(pointId), s);
//    			}
//    		}
//				
//			// Display treeline
//			treeline.updateCache();
//		}
//		Utils.log(" ----- DONE!! ------ ");
//	}
	
	/**
	 * Converts a lineMap data structure into the representation as a treeline.
	 * 
	 * @param lineMap - Map of lines with ids. The lines are stored as points in a map.
	 * Where the points also have an id, which is only unique for the line they belong to.
	 */
	public void convertLineMapToTreeLine(Map<Integer, Map<Integer, de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.Node>> lineMap,
			String status)
	{
		// get layers
		Display display = Display.getFront();	
		Project project = display.getProject();
		LayerSet layerSet = display.getLayerSet();  
		ArrayList<Layer> layers = layerSet.getLayers();
		
		if(layers.isEmpty()) 
		{
			return; // add warning message
		}
		
		// get image names
		List<Patch> patches = layerSet.getAll(Patch.class);
		ImagePlus imagePlus = null;
		if(!patches.isEmpty()) 
		{	
			imagePlus = patches.get(0).getImagePlus();
		}
			
		String[] imageNames = new String[layers.size()];
		if(null != imagePlus) 
		{	
			imageNames = imagePlus.getImageStack().getSliceLabels();
		}
		
		
		Layer currentLayer = display.getLayer(); // order of rootsets has to correspond to the layer if we don't care about image names
			
		int index = 0;
		for ( Layer layer : layers )
		{
			if ( layer.equals(currentLayer) )
			{
				break;
			}
			index++;
		}
		
		//create the needed treelines
		List<Displayable> treelineList = RhizoUtils.addDisplayableToProject(project, "treeline", lineMap.keySet().size());
		
		Utils.log("Layer index: " + index);
		
		int treelineIndex =0;
		for ( int id : lineMap.keySet() )
		{
			MTBXMLRootImageAnnotationType rootSet = MTBXMLRootImageAnnotationType.Factory.newInstance();
				
			rootSet.setImagename(imageNames[index]);
			rootSet.setRootSetID(index);

			// get treeline (now existing)
			Treeline treeline = (Treeline) treelineList.get(treelineIndex);
			treeline.setLayer(currentLayer);
				
			// TODO: this is a workaround for the repainting issues that occur when creating new nodes out of a mtbxml file
			currentLayer.mtbxml = true;
				
			Map<Integer, de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.Node> line = lineMap.get(id);
    		// create node -> ID map to later assign parents and children according to segment IDs and parent IDs
    		HashMap<Integer, RadiusNode> nodeIDmap = new HashMap<Integer, RadiusNode>();
				
    		// Create a nodeIDmap of all points
			for ( int pointId : line.keySet() )
			{
				de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.Node p = line.get(pointId);

				if(p.getPredecessor() == -1)
    			{
    				RadiusNode root = new RadiusNode((float) p.getX(), (float) p.getY(), currentLayer, (float) p.getRadius());
    				nodeIDmap.put(-1, root);
    			}
    			RadiusNode currentNode = new RadiusNode((float) p.getX(), (float) p.getY(), currentLayer, (float) p.getRadius());
    			nodeIDmap.put(pointId, currentNode);
			}

			// Add all points to treeline
			for ( int pointId : line.keySet() )
			{
				de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.Node p = line.get(pointId);
				
				// assuming that default status is defined
				byte s = (byte) RhizoProjectConfig.STATUS_UNDEFINED;
				if ( status != null && !status.equals("STATUS_UNDEFINED") )
				{
					if ( status.equals("LIVING") ) 			s = (byte) 0;
					else if ( status.equals("DEAD") ) 		s = (byte) 1;
					else if ( status.equals("GAP") )		s = (byte) 2;
					else if ( status.equals("DECAYED") )	s = (byte) 3;
				}
    				
    			if(p.getPredecessor() == -1)
    			{
    				treeline.addNode(null, nodeIDmap.get(p.getPredecessor()), s,true);
    				treeline.addNode(nodeIDmap.get(p.getPredecessor()), nodeIDmap.get(pointId), s,true);
    				treeline.setRoot(nodeIDmap.get(p.getPredecessor()));
    			}
    			else
    			{
    				treeline.addNode(nodeIDmap.get(p.getPredecessor()), nodeIDmap.get(pointId), s,true);
    			}
    		}
				
			// Display treeline
			treeline.updateCache();
			treelineIndex++;
		}
		RhizoUtils.repaintTreelineList(treelineList);
		Utils.log(" ----- DONE!! ------ ");
	}
}
