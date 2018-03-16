package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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
import de.unihalle.informatik.rhizoTrak.xsd.config.Config.StatusList.Status;
import ij.ImagePlus;

public class RhizoMTBXML
{
	private RhizoMain rhizoMain;

	public RhizoMTBXML(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
	}

	
    /**
     *  Writes the current project to a xmlbeans file. In contrast to the standard TrakEM xml format, this file may be opened with MiToBo
     *  @author Tino
     */
    public void writeMTBXML()
	{
		try 
		{
			File saveFile = Utils.chooseFile(System.getProperty("user.home"), null, ".xml");
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
			
			Hashtable<Treeline, int[]> rootsTable = new Hashtable<Treeline, int[]>();
			
			// get layers
			Display display = Display.getFront();	
			LayerSet layerSet = display.getLayerSet();  
			ArrayList<Layer> layers = layerSet.getLayers();
			
			// get image names
			List<Patch> patches = layerSet.getAll(Patch.class);
			ImagePlus imagePlus = patches.get(0).getImagePlus();
			String[] imageNames = imagePlus.getImageStack().getSliceLabels();
			
			// setup xml file --- stack of images = rootProject
			MTBXMLRootProjectDocument xmlRootProjectDocument = MTBXMLRootProjectDocument.Factory.newInstance();
			MTBXMLRootProjectType xmlRootProject = xmlRootProjectDocument.addNewMTBXMLRootProject();
			xmlRootProject.setXsize((int) layers.get(0).getLayerWidth());
			xmlRootProject.setYsize((int) layers.get(0).getLayerHeight());
			xmlRootProject.setXresolution((float) imagePlus.getCalibration().getX(1));
			xmlRootProject.setYresolution((float) imagePlus.getCalibration().getY(1));
			
			MTBXMLRootImageAnnotationType[] xmlRootSets = new MTBXMLRootImageAnnotationType[layers.size()];

			// all treelines in the project
			List<Displayable> allTreelines = layerSet.get(Treeline.class);
			
			// layer = rootSet
			for(int i = 0; i < layers.size(); i++)
			{
				Layer currentLayer = layers.get(i); 
				MTBXMLRootImageAnnotationType rootSet = MTBXMLRootImageAnnotationType.Factory.newInstance();
				
				rootSet.setImagename(imageNames[i]); // TODO: why does this not work?
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
			List<MTBXMLRootAssociationType> rootAssociationList = new ArrayList<MTBXMLRootAssociationType>(); // arraylist for convenience
			
			for(int i = 0; i < connectors.size(); i++)
			{
				Connector currentConnector = (Connector) connectors.get(i);
				
				MTBXMLRootAssociationType rootAssociation = MTBXMLRootAssociationType.Factory.newInstance();
				
				// treeline = rootReference TODO: first rootreference is redundant in xml output? might be caused by changes to the connector class
				List<MTBXMLRootReferenceType> rootReferencesList = new ArrayList<MTBXMLRootReferenceType>(); // arraylist for convenience
				List<Treeline> treelinesOfConnector = treelinesOfConnector(currentConnector);
 				
				
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
		catch (Exception e) 
		{
			Utils.log(e.getMessage());
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
		xmlRoot.setStartSegmentID(0); // TODO?
		
		List<Node<Float>> treelineNodes = new ArrayList<Node<Float>>(treeline.getNodesAt(currentLayer)); // arraylist for convenience
		treelineNodes.remove(treeline.getRoot()); // skip the root node

		// TODO: some nodes in annotated projects (roman) appear to be parentless? how?
		List<Node<Float>> nodes = sortTreelineNodes(treelineNodes, treeline);
		
		MTBXMLRootSegmentType[] rootSegmentsArray = new MTBXMLRootSegmentType[nodes.size()];
		
		for(int i = 0; i < nodes.size(); i++)
		{
			Node<Float> n = nodes.get(i);
			float startRadius = 1; // default if node is not a RadiusNode
			float endRadius = 1; // default if node is not a RadiusNode 
			
			if(!n.equals(treeline.getRoot()))
			{
				if(n.getParent() instanceof RadiusNode) startRadius = ((RadiusNode) n.getParent()).getData();
				if(n instanceof RadiusNode) endRadius = ((RadiusNode) n).getData();
				
				MTBXMLRootSegmentType rootSegment = MTBXMLRootSegmentType.Factory.newInstance();
				rootSegment.setRootID(xmlRoot.getRootID());
				rootSegment.setSegmentID(i); 
				if(n.getParent().equals(treeline.getRoot())) rootSegment.setParentID(-1);
				else rootSegment.setParentID(i-1); // this works because nodes are sorted

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
				
				LinkedHashMap<Integer, Status> statusMap = rhizoMain.getRhizoIO().getStatusMap();
				if(null != statusMap.get((int) n.getConfidence()))
				{
					String statusName = statusMap.get((int) n.getConfidence()).getFullName();
					if(statusName.equals("DEAD")) rootSegment.setType(MTBXMLRootSegmentStatusType.DEAD);
					else if(statusName.equals("DECAYED")) rootSegment.setType(MTBXMLRootSegmentStatusType.DECAYED);
					else if(statusName.equals("GAP")) rootSegment.setType(MTBXMLRootSegmentStatusType.GAP);
					else rootSegment.setType(MTBXMLRootSegmentStatusType.LIVING);
				}
				else rootSegment.setType(MTBXMLRootSegmentStatusType.LIVING);
				
				rootSegmentsArray[i] = rootSegment;
			}
		}
		
		xmlRoot.setRootSegmentsArray(rootSegmentsArray);
		return xmlRoot;
	}
	
	/**
	 * Sorts the nodes of a treeline according their start and end coordinates
	 * @param List of nodes in a treeline
	 * @return Sorted list of nodes
	 * @author Tino
	 */
	private static List<Node<Float>> sortTreelineNodes(List<Node<Float>> treelineNodes, Treeline treeline)
	{
		List<Node<Float>> result = new ArrayList<Node<Float>>();
		// first node
		Node<Float> current = null;
		for(Node<Float> n: treelineNodes)
		{
			if(n.getParent().equals(treeline.getRoot()))
			{
				result.add(n);
				current = n;
			}
		}

		while(result.size() < treelineNodes.size())
		{
			boolean found = false;
			for(Node<Float> n: treelineNodes)
			{
				if(n.getParent().equals(current))
				{
					result.add(n);
					current = n;
					found = true;
					break;
				}
			}
			
			if(!found)
			{
				Utils.log("treeline "+treeline+" has parentless nodes.");
				break;
			}
		}

		return result;
	}

	/**
	 * Converts MTBXML to TrakEM project.
	 * INCOMPLETE - TODO: add connectors, image names, find better workaround
	 * @author Tino
	 */
	public void readMTBXML()
	{
		String[] filepath = Utils.selectFile("test");
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
			LinkedHashMap<Integer, Status> statusMap = rhizoMain.getRhizoIO().getStatusMap();

			// error if default status is not defined
			List<String> fullNames = new ArrayList<String>();
			for(int i: statusMap.keySet())
			{
				fullNames.add(statusMap.get(i).getFullName());
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
			
			while(rootSets.length > layers.size())
			{
				Utils.showMessage("Number of rootSets in the xml file is greater than the number of layers.");
				filepath = Utils.selectFile("test");
				file = new File(filepath[0] + filepath[1]);
				
				rootProjectDocument = MTBXMLRootProjectDocument.Factory.parse(file);
				rootProject = rootProjectDocument.getMTBXMLRootProject();
				rootSets = rootProject.getCollectionOfImageAnnotationsArray();
			}
			
			
			/* catch exceptions:
			 * - check resolution 
			 */
			for(int i = 0; i < rootSets.length; i++)
			{
				Layer currentLayer = layers.get(i); // order of rootsets has to correspond to the layer if we don't care about image names
				MTBXMLRootImageAnnotationType currentRootSet = rootSets[i];
				
				ProjectThing possibleParent = RhizoAddons.findParentAllowing("treeline", project);
				if(possibleParent == null)
				{
				    Utils.showMessage("Project does not contain object that can hold treelines.");
				    return;
				}
				
				MTBXMLRootType[] roots = currentRootSet.getRootsArray();
				Utils.log("@readMTBXML: number of roots in rootset "+ i + ": " + roots.length);
		
				for(int j = 0; j < roots.length; j++)
				{
					MTBXMLRootType currentRoot = roots[j];
					
					ProjectThing treelineThing = possibleParent.createChild("treeline");
					
					DefaultMutableTreeNode parentNode = DNDTree.findNode(possibleParent, projectTree);
	    			DefaultMutableTreeNode node = new DefaultMutableTreeNode(treelineThing);
	    			((DefaultTreeModel) projectTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
	    			
	    			Treeline treeline = (Treeline) treelineThing.getObject();
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
	    				byte s = -1;

	    				for(int index: statusMap.keySet())
	    				{
	    					String statusName = statusMap.get(index).getFullName();
	    					if(statusName.equals("LIVING") && currentRootSegment.getType() == MTBXMLRootSegmentStatusType.LIVING) s = (byte) index;
	    					else if(statusName.equals("DEAD") && currentRootSegment.getType() == MTBXMLRootSegmentStatusType.DEAD) s = (byte) index;
	    					else if(statusName.equals("GAP") && currentRootSegment.getType() == MTBXMLRootSegmentStatusType.GAP) s = (byte) index;
	    					else if(statusName.equals("DECAYED") && currentRootSegment.getType() == MTBXMLRootSegmentStatusType.DECAYED) s = (byte) index;
	    				}

	    				if(currentRootSegment.getParentID() == -1)
	    				{
	    					treeline.addNode(null, nodeIDmap.get(currentRootSegment.getParentID()), s);
	    					treeline.addNode(nodeIDmap.get(currentRootSegment.getParentID()), nodeIDmap.get(currentRootSegment.getSegmentID()), s);
	    					treeline.setRoot(nodeIDmap.get(currentRootSegment.getParentID()));
	    			
	    				}
	    				else
	    				{
	    					treeline.addNode(nodeIDmap.get(currentRootSegment.getParentID()), nodeIDmap.get(currentRootSegment.getSegmentID()), s);
	    				}
	    			}
	    			
	    			treeline.updateCache();
	    			Utils.log("treeline"+j+"; set layer at "+treeline.getLayer() + " " + treeline.getFirstLayer());
	    			Utils.log(treeline.getNodesAt(currentLayer).size()); 
				}
			}
		}
		catch (Exception e) 
		{
			Utils.log(e.getMessage());
			Utils.log(e.getLocalizedMessage());
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
    

	/**
	 * Returns the list of all treelines under the origin and under the targets. Used for reading MTBXML formats
	 * TODO deprecated due to changes to the connector class
	 * @param c - Connector
	 * @return List of treelines
	 * @author Tino
	 */
	private List<Treeline> treelinesOfConnector(Connector c)
	{
		List<Treeline> treelines = new ArrayList<Treeline>();
		List<Set<Displayable>> targets = c.getTargets();
		Set<Displayable> origins = c.getOrigins();
		
		for(Displayable d: origins)
		{
			if(d instanceof Treeline) treelines.add((Treeline) d);
		}
	
		for(int i = 0; i < targets.size(); i++)
		{
			for(Displayable d: targets.get(i))
			{
				if(d instanceof Treeline) treelines.add((Treeline) d);
			}
		}
		
		return treelines;
	}

	
}
