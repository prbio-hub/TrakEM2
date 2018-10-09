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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.unihalle.informatik.MiToBo_xml.MTBXMLRootAssociationType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootImageAnnotationType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootProjectDocument;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootProjectType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootReferenceType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentPointType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentStatusType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootType;
import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;

public class RhizoMTBXML
{
	private RhizoMain rhizoMain;
	
	private final int INSIDE = 0;
	private final int LEFT   = 1;
	private final int RIGHT  = 2;
	private final int BOTTOM = 4;
	private final int TOP    = 8;

	private final float DEFAULT_RADIUS = 0f;

	public RhizoMTBXML(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
	}

	
    /**
     *  Writes the current project to a xmlbeans file.
     *  @author Tino
     */
    public void writeMTBXML()
	{
		try 
		{
			// get layers
			Display display = Display.getFront();	
			LayerSet layerSet = display.getLayerSet();  
			ArrayList<Layer> layers = layerSet.getLayers();
			
			if(layers.isEmpty()) return; // add warning message

			File saveFile = Utils.chooseFile(System.getProperty("user.home"), null, ".xml");
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
			
			Hashtable<Treeline, int[]> rootsTable = new Hashtable<Treeline, int[]>();
			
			// get image names
			List<Patch> patches = layerSet.getAll(Patch.class);
			ImagePlus imagePlus = null;
			if(!patches.isEmpty()) imagePlus = patches.get(0).getImagePlus();
			
			String[] imageNames = new String[layers.size()];
			if(null != imagePlus) imageNames = imagePlus.getImageStack().getSliceLabels();
			
			
			// setup xml file --- stack of images = rootProject
			MTBXMLRootProjectDocument xmlRootProjectDocument = MTBXMLRootProjectDocument.Factory.newInstance();
			MTBXMLRootProjectType xmlRootProject = xmlRootProjectDocument.addNewMTBXMLRootProject();
			xmlRootProject.setXsize((int) layers.get(0).getLayerWidth());
			xmlRootProject.setYsize((int) layers.get(0).getLayerHeight());
			
			float xCal = imagePlus == null ? -1 : (float) imagePlus.getCalibration().getX(1);
			float yCal = imagePlus == null ? -1 : (float) imagePlus.getCalibration().getY(1);
			xmlRootProject.setXresolution(xCal);
			xmlRootProject.setYresolution(yCal);
			
			MTBXMLRootImageAnnotationType[] xmlRootSets = new MTBXMLRootImageAnnotationType[layers.size()];

			// all treelines in the project
			List<Displayable> allTreelines = layerSet.get(Treeline.class);
			
			Utils.log("@writeMTBXML: "+allTreelines.size() + " " + layers.size());
			
			// layer = rootSet
			for(int i = 0; i < layers.size(); i++)
			{
				Layer currentLayer = layers.get(i); 
				MTBXMLRootImageAnnotationType rootSet = MTBXMLRootImageAnnotationType.Factory.newInstance();
				
				rootSet.setImagename(imageNames[i]);
				rootSet.setRootSetID(i);
				
				List<MTBXMLRootType> roots = new ArrayList<MTBXMLRootType>(); // arraylist for convenience
				int rootID = 0;

				// check for each treelines which layer it belongs to - inconvenient but currently the only way to get all treelines in a layer
				for(int j = 0; j < allTreelines.size(); j++)
				{
					Treeline currentTreeline = (Treeline) allTreelines.get(j);

					if(null == currentTreeline.getFirstLayer()) continue;
					
					// if treeline belongs to the current layer, then add it
					if(currentTreeline.getFirstLayer().equals(currentLayer))
					{
						roots.add(treelineToXMLType(currentTreeline, currentLayer, rootID)); 
						rootsTable.put(currentTreeline, new int[]{i, rootID});
						rootID++;
					}
				}
				numberOfTreelinesInLayer(currentLayer, allTreelines);
				rootSet.setRootsArray(roots.toArray(new MTBXMLRootType[numberOfTreelinesInLayer(currentLayer, allTreelines)]));
				xmlRootSets[i] = rootSet;
			}
			xmlRootProject.setCollectionOfImageAnnotationsArray(xmlRootSets);
			
			
			// Connectors in project
			List<Displayable> connectors = layerSet.get(Connector.class);
			
			// connector = rootAssociation
			List<MTBXMLRootAssociationType> rootAssociationList = new ArrayList<MTBXMLRootAssociationType>();
			
			for(int i = 0; i < connectors.size(); i++)
			{
				Connector currentConnector = (Connector) connectors.get(i);
				
				MTBXMLRootAssociationType rootAssociation = MTBXMLRootAssociationType.Factory.newInstance();
				
				// treeline = rootReference
				List<MTBXMLRootReferenceType> rootReferencesList = new ArrayList<MTBXMLRootReferenceType>();
				List<Treeline> treelinesOfConnector = currentConnector.getConTreelines();
				
				for(int j = 0; j < treelinesOfConnector.size(); j++) 
				{
					int[] ids = rootsTable.get(treelinesOfConnector.get(j));
					MTBXMLRootReferenceType rootReference = MTBXMLRootReferenceType.Factory.newInstance();
					rootReference.setRootID(ids[1]);
					rootReference.setRootSetID(ids[0]);
					
					rootReferencesList.add(rootReference);
				}
				
				rootAssociation.setRootReferencesArray(rootReferencesList.toArray(new MTBXMLRootReferenceType[treelinesOfConnector.size()]));
				rootAssociationList.add(rootAssociation);
			}
			
			xmlRootProject.setRootAssociationsArray(rootAssociationList.toArray(new MTBXMLRootAssociationType[connectors.size()]));
			
			bw.write(xmlRootProjectDocument.toString());
			bw.close();
			Utils.log("Created xml file - "+saveFile.getAbsolutePath());
				
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}	
    
    /**
     * Converts a given treeline to MTBXML format
     * @param treeline - Treeline to be converted
     * @param currentLayer
     * @param rootId - Current rootID
     * @return MTBXMLRootType
     * @author Tino
     */
	private MTBXMLRootType treelineToXMLType(Treeline treeline, Layer currentLayer, int rootId)
	{
		MTBXMLRootType xmlRoot = MTBXMLRootType.Factory.newInstance();

		xmlRoot.setRootID(rootId);
		
		Set<Node<Float>> treelineNodes = treeline.getNodesAt(currentLayer);
		List<Node<Float>> nodes = new ArrayList<Node<Float>>(treelineNodes);

		xmlRoot.setStartSegmentID(0);
		
		List<MTBXMLRootSegmentType> rootSegmentList = new ArrayList<MTBXMLRootSegmentType>();

		Utils.log("@treelineToXMLType: " + nodes.size());
		
		for(int i = 0; i < nodes.size(); i++)
		{
			Node<Float> n = nodes.get(i);
			float startRadius = DEFAULT_RADIUS; // default if node is not a RadiusNode
			float endRadius = DEFAULT_RADIUS; // default if node is not a RadiusNode 
			
			if(null != n.getParent())
			{
				if(n.getParent() instanceof RadiusNode) startRadius = ((RadiusNode) n.getParent()).getData();
				if(n instanceof RadiusNode) endRadius = ((RadiusNode) n).getData();
				
				MTBXMLRootSegmentType rootSegment = MTBXMLRootSegmentType.Factory.newInstance();
				rootSegment.setRootID(xmlRoot.getRootID());
				rootSegment.setSegmentID(i); 
				if(n.getParent().equals(treeline.getRoot()))
				{
					 rootSegment.setParentID(-1);
					 xmlRoot.setStartSegmentID(i);
				}
				else rootSegment.setParentID(nodes.indexOf(n.getParent()));

				MTBXMLRootSegmentPointType xmlStart = MTBXMLRootSegmentPointType.Factory.newInstance();
				// transform local coordinates to global
				Point2D start = treeline.getAffineTransform().transform(new Point2D.Float(n.getParent().getX(), n.getParent().getY()), null);
				xmlStart.setX((float) start.getX());
				xmlStart.setY((float) start.getY());
		
				rootSegment.setStartPoint(xmlStart);
				rootSegment.setStartRadius(startRadius);
				
				MTBXMLRootSegmentPointType xmlEnd = MTBXMLRootSegmentPointType.Factory.newInstance();
				// transform local coordinates to global
				Point2D end = treeline.getAffineTransform().transform(new Point2D.Float(n.getX(), n.getY()), null);
				xmlEnd.setX((float) end.getX());
				xmlEnd.setY((float) end.getY());
				
				rootSegment.setEndPoint(xmlEnd);
				rootSegment.setEndRadius(endRadius);

				RhizoStatusLabel sl = rhizoMain.getProjectConfig().getStatusLabel( (int) n.getConfidence());
				if(sl != null) 
				{
					String statusName = sl.getName();

					if(statusName.equals("DEAD")) rootSegment.setType(MTBXMLRootSegmentStatusType.DEAD);
					else if(statusName.equals("DECAYED")) rootSegment.setType(MTBXMLRootSegmentStatusType.DECAYED);
					else if(statusName.equals("GAP")) rootSegment.setType(MTBXMLRootSegmentStatusType.GAP);
					else rootSegment.setType(MTBXMLRootSegmentStatusType.LIVING);
				}
				else rootSegment.setType(MTBXMLRootSegmentStatusType.LIVING);

				rootSegmentList.add(rootSegment);
			}
		}
		
		Utils.log(rootSegmentList.size());
		xmlRoot.setRootSegmentsArray(rootSegmentList.toArray(new MTBXMLRootSegmentType[rootSegmentList.size()]));
		return xmlRoot;
	}
	

	/**
	 * Converts MTBXML to TrakEM project.
	 * @author Tino
	 */
	public void readMTBXML()
	{		
		String[] filepath = Utils.selectFile("test");
		if(null == filepath) return;
		
		File file = new File(filepath[0] + filepath[1]);
		
		// TODO: check other violations
		while(!filepath[1].contains(".xml"))
		{
			Utils.showMessage("Selected file is not a valid xml file.");
			filepath = Utils.selectFile("test");
			file = new File(filepath[0] + filepath[1]);
		}
		
		try 
		{
			// error if default status is not defined
			List<String> fullNames = new ArrayList<String>();
			for ( RhizoStatusLabel sl : rhizoMain.getProjectConfig().getAllStatusLabel()) {
				fullNames.add( sl.getName());
			}
			
			if(!fullNames.contains("LIVING") || !fullNames.contains("DEAD") || !fullNames.contains("DECAYED") || !fullNames.contains("GAP"))
			{
				Utils.showMessage("ERROR: not all default status are defined. Cancelling MTBXML import.");
				return;
			}
			
			MTBXMLRootProjectDocument rootProjectDocument = MTBXMLRootProjectDocument.Factory.parse(file);
			MTBXMLRootProjectType rootProject = rootProjectDocument.getMTBXMLRootProject();
			MTBXMLRootImageAnnotationType[] rootSets = rootProject.getCollectionOfImageAnnotationsArray();
			
			Utils.log("@readMTBXML: number of rootsets " + rootSets.length);
			
			Display display = Display.getFront();	
			
			Project project = display.getProject();
			ProjectTree projectTree = project.getProjectTree();
			
			LayerSet layerSet = display.getLayerSet();  
			ArrayList<Layer> layers = layerSet.getLayers();
			
			if(rootSets.length > layers.size())
			{
				Utils.showMessage("Number of rootSets in the xml file is greater than the number of layers.\nCancelling MTBXML import.");
				return;
			}
			
			// used for creating connectors
			Map<List<Integer>, Treeline> rootsTable = new HashMap<List<Integer>, Treeline>();
			
			
			ArrayList<Treeline> allTheNewTreelines = new ArrayList<>();
			
			for(int i = 0; i < rootSets.length; i++)
			{		
				Layer currentLayer = layers.get(i); // order of rootsets has to correspond to the layer if we don't care about image names
				MTBXMLRootImageAnnotationType currentRootSet = rootSets[i];
				
				ProjectThing possibleParent = RhizoAddons.findParentAllowing("treeline", project);
				if(possibleParent == null) 	{
					try {
						//ProjectTree projectTree = project.getProjectTree();
						ProjectThing rootNode = null;
						rootNode = (ProjectThing) projectTree.getRoot().getUserObject();
						if ( rootNode != null ) {
							ProjectThing rootstackThing = rootNode.createChild("rootstack");
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(rootstackThing);
							DefaultMutableTreeNode parentNode = DNDTree.findNode(rootNode, projectTree);
							((DefaultTreeModel) projectTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
						} else {	
							Utils.showMessage("Project does not contain object that can hold treelines.");
							return;
						} 
					} catch (Exception ex) {
						Utils.showMessage("Project does not contain object that can hold treelines.");
						return;
					}
				}

				MTBXMLRootType[] roots = currentRootSet.getRootsArray();
				
				//add the appropriate number of treelines
				final List<Displayable> addedRootsList = RhizoUtils.addDisplayableToProject(project, "treeline", roots.length);

				//add the nodes and so on in the treelines stored in addedRootsList
				for(int j = 0; j < roots.length; j++)
				{
					MTBXMLRootType currentRoot = roots[j];
					
	    			Treeline treeline = (Treeline) addedRootsList.get(j);
	    			allTheNewTreelines.add(treeline);
	    			treeline.setLayer(currentLayer);
	    			
	    			// TODO: this is a workaround for the repainting issues that occur when creating new nodes out of a mtbxml file
	    			currentLayer.mtbxml = true;
	    			
	    			MTBXMLRootSegmentType[] rootSegments = currentRoot.getRootSegmentsArray();
//	    			Utils.log("@readMTBXML: number of segments in root "+ j + " in rootset "+ i + ": " + rootSegments.length);
	    			
	    			// create node -> ID map to later assign parents and children according to segment IDs and parent IDs
	    			HashMap<Integer, RadiusNode> nodeIDmap = new HashMap<Integer, RadiusNode>();
	    			for(int k = 0; k < rootSegments.length; k++)
	    			{
	    				MTBXMLRootSegmentType currentRootSegment = rootSegments[k];
	    				MTBXMLRootSegmentPointType xmlStart = currentRootSegment.getStartPoint();
	    				MTBXMLRootSegmentPointType xmlEnd = currentRootSegment.getEndPoint();

	    				if(currentRootSegment.getParentID() == -1)
	    				{
	    					RadiusNode root = new RadiusNode(xmlStart.getX(), xmlStart.getY(), currentLayer, currentRootSegment.getStartRadius());
	    					RadiusNode currentNode = new RadiusNode(xmlEnd.getX(), xmlEnd.getY(), currentLayer, currentRootSegment.getEndRadius());
	    					nodeIDmap.put(-1, root);
	    					nodeIDmap.put(currentRootSegment.getSegmentID(), currentNode);
	    				}
	    				else
	    				{
	    					RadiusNode currentNode = new RadiusNode(xmlEnd.getX(), xmlEnd.getY(), currentLayer, currentRootSegment.getEndRadius());
	    					nodeIDmap.put(currentRootSegment.getSegmentID(), currentNode);
	    				}
	    			}
	    			
	    			for(int k = 0; k < rootSegments.length; k++)
	    			{
	    				MTBXMLRootSegmentType currentRootSegment = rootSegments[k];
	    				
    					// assuming that default status are defined
	    				byte s = (byte) RhizoProjectConfig.STATUS_UNDEFINED;
	    				
	    				for(int index = 0 ; index < rhizoMain.getProjectConfig().sizeStatusLabelMapping() ; index++) 
	    				{
	    					String statusName = rhizoMain.getProjectConfig().getStatusLabel(index).getName();
	    					
	    					if(statusName.equals("LIVING") && currentRootSegment.getType() == MTBXMLRootSegmentStatusType.LIVING) s = (byte) index;
	    					else if(statusName.equals("DEAD") && currentRootSegment.getType() == MTBXMLRootSegmentStatusType.DEAD) s = (byte) index;
	    					else if(statusName.equals("GAP") && currentRootSegment.getType() == MTBXMLRootSegmentStatusType.GAP) s = (byte) index;
	    					else if(statusName.equals("DECAYED") && currentRootSegment.getType() == MTBXMLRootSegmentStatusType.DECAYED) s = (byte) index;
	    				}

	    				if(currentRootSegment.getParentID() == -1)
	    				{
	    					treeline.addNode(null, nodeIDmap.get(currentRootSegment.getParentID()), s,true);
	    					treeline.addNode(nodeIDmap.get(currentRootSegment.getParentID()), nodeIDmap.get(currentRootSegment.getSegmentID()), s,true);
	    					treeline.setRoot(nodeIDmap.get(currentRootSegment.getParentID()));
	    			
	    				}
	    				else
	    				{
	    					treeline.addNode(nodeIDmap.get(currentRootSegment.getParentID()), nodeIDmap.get(currentRootSegment.getSegmentID()), s,true);
	    				}
	    			}
	    			treeline.updateCache();
	    			rootsTable.put(Arrays.asList(currentRootSet.getRootSetID(), currentRoot.getRootID()), treeline);
	    			
				}
			}

			for (Treeline treeline2 : allTheNewTreelines) {
				treeline2.repaint(true,treeline2.getFirstLayer());
			}
			
			MTBXMLRootAssociationType[] rootAssociations = rootProject.getRootAssociationsArray();
			
			//add the appropriate number of connectors
			final List<Displayable> addedConnectorList = RhizoUtils.addDisplayableToProject(project, "connector", rootAssociations.length);

			
			// set connectors
			for(int i = 0;  i < rootAssociations.length; i++)
			{
				Connector currentConnector = (Connector) addedConnectorList.get(i);
				
				MTBXMLRootAssociationType currentRootAssociation = rootAssociations[i];
				MTBXMLRootReferenceType[] rootReferences = currentRootAssociation.getRootReferencesArray();
				
				for(int j = 0; j < rootReferences.length; j++)
				{
					MTBXMLRootReferenceType currentRootReference = rootReferences[j];
					Treeline ctree = rootsTable.get(Arrays.asList(currentRootReference.getRootSetID(), currentRootReference.getRootID()));
					
					if(!currentConnector.addConTreeline(ctree));
					{
						Utils.log("rhizoTrak", "Can not create or find connector between #" + ctree.getId() + " and #" + ctree.getId());
					}
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		
		if(!endNodesInside())
		{
			int answer = JOptionPane.showConfirmDialog(null, "Some segments appear to be outside of the image.\nDo you want to trim them to the image bounds?", "", JOptionPane.YES_NO_OPTION);
			if(answer == JOptionPane.YES_OPTION) cutTreelines();
		}
	}
	
	

    /**
     * Returns the number of treelines in a layer
     * @param l - Layer
     * @param treelines - List of treelines 
     * @return Number of treelines in the given layer
     * @author Tino
     */
    private int numberOfTreelinesInLayer(Layer l, List<Displayable> treelines)
    {
    	int res = 0;
    	
    	for(int j = 0; j < treelines.size(); j++)
		{
			Treeline currentTreeline = (Treeline) treelines.get(j);
			
			if(null == currentTreeline.getFirstLayer()) continue;
			if(currentTreeline.getFirstLayer().equals(l)) res++;
		}
    	
    	return res;
    }

	
	private boolean endNodesInside()
	{
		// get layers
		Display display = Display.getFront();	
		LayerSet layerSet = display.getLayerSet();  
		
		// all treelines in the project
		List<Displayable> allTreelines = layerSet.get(Treeline.class);
		
		for(int j = 0; j < allTreelines.size(); j++)
		{
			Treeline currentTreeline = (Treeline) allTreelines.get(j);

			if(null == currentTreeline.getFirstLayer()) continue;
			
			Set<Node<Float>> endNodes = currentTreeline.getEndNodes();
			
			Patch p = RhizoAddons.getPatch(currentTreeline);
			if(null == p) continue;
			
			ImagePlus image = p.getImagePlus();

			for(Node<Float> n: endNodes)
			{
				AffineTransform at = currentTreeline.getAffineTransform();
				Point2D point = at.transform(new Point2D.Float(n.getX(), n.getY()), null);
				if(regionCode(point.getX(), point.getY(), 0, 0, image.getWidth(), image.getHeight()) != INSIDE) return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Clips imported treelines with the image bounds.
	 */
	private void cutTreelines()
	{
		// get layers
		Display display = Display.getFront();	
		LayerSet layerSet = display.getLayerSet();  
		
		// all treelines in the project
		List<Displayable> allTreelines = layerSet.get(Treeline.class);
		
		for(int j = 0; j < allTreelines.size(); j++)
		{
			Treeline currentTreeline = (Treeline) allTreelines.get(j);
			AffineTransform at = currentTreeline.getAffineTransform();			
			
			if(null == currentTreeline.getFirstLayer()) continue;
			Patch p = RhizoAddons.getPatch(currentTreeline);
			if(null == p) continue;
			
			ImagePlus image = p.getImagePlus();
			Utils.log(image.getWidth() + " " + image.getHeight());

			// delete treeline if it is completely outside of the image
			if(isTreelineCompletelyOutside(currentTreeline, image))
			{
				rhizoMain.getProject().remove(currentTreeline);
				continue;
			}
			
			Set<Node<Float>> endNodes = currentTreeline.getEndNodes();
			
			Utils.log("endnodes of " + currentTreeline.getId() + ": "  + endNodes.size());
					
			for(Node<Float> n: endNodes)
			{					
				Node<Float> temp = n;
				Node<Float> temp2 = temp.getParent(); // second temp node because removeNode sets parent pointer to null

				while(null != temp2)
				{			
 					Point2D parent = at.transform(new Point2D.Float(temp2.getX(), temp2.getY()), null);
					Point2D point = at.transform(new Point2D.Float(temp.getX(), temp.getY()), null);
					
					Utils.log(parent + " " + point);
					
					if(regionCode(point.getX(), point.getY(), 0, 0, image.getWidth(), image.getHeight()) != INSIDE
							&& regionCode(parent.getX(), parent.getY(), 0, 0, image.getWidth(), image.getHeight()) != INSIDE)
					{
						temp2 = temp.getParent();
						currentTreeline.removeNode(temp);
					}
					else break; // at least one node inside

					temp = temp2;
					temp2 = temp.getParent();
				}
			}

			currentTreeline.updateCache();
		}
	}
	
	
	
	private boolean isTreelineCompletelyOutside(Treeline t, ImagePlus image)
	{
		List<Node<Float>> treelineNodes = new ArrayList<Node<Float>>(t.getNodesAt(t.getFirstLayer()));
				
		for(Node<Float> n: treelineNodes)
		{
			Point2D point = t.getAffineTransform().transform(new Point2D.Float(n.getX(), n.getY()), null);
			if(regionCode(point.getX(), point.getY(), 0, 0, image.getWidth(), image.getHeight()) == INSIDE) return false;
		}
		
		return true;
	}

	
	private final int regionCode(double x, double y, double xMin, double yMin, double xMax, double yMax) 
	{
        int code = x < xMin ? LEFT : x > xMax ? RIGHT : INSIDE;
        if (y < yMin) code |= BOTTOM;
        else if (y > yMax) code |= TOP;
        Utils.log("region code: "+code);
        return code;
    }
}
