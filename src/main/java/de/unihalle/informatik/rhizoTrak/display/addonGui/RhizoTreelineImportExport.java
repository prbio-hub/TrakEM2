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

package de.unihalle.informatik.rhizoTrak.display.addonGui;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.unihalle.informatik.MiToBo.apps.minirhizotron.datatypes.MTBRootTree;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.datatypes.MTBRootTreeNodeData;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBTreeNode;
import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.addon.RhizoProjectConfig;
import de.unihalle.informatik.rhizoTrak.addon.RhizoUtils;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.TreeEventListener;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

/**
 * Support methods to simplify import/export and conversion of treelines from rhizoTrak projects
 * to root tree objects in MiToBo and vice versa.
 * 
 * @author Birgit Moeller
 */
public class RhizoTreelineImportExport {	

	/**
	 * Default constructor.
	 */
	public RhizoTreelineImportExport() {
		// nothing to do here
	}

	/**
	 * Converts a rhizoTrak {@link Treeline} to a {@link MTBRootTree} object.
	 * <p>
	 * This methods works in a recursive fashion on the tree, i.e., recursively
	 * calls {@link #convertSubtreelineToMTBRootTree(AffineTransform, Node, MTBTreeNode)}.}
	 * 
	 * @param treeline	Treeline to convert.
	 * @return	Resulting root tree object.
	 */
	public MTBRootTree exportTreelineToMTBRootTree(Treeline treeline) {

		RadiusNode treelineRoot = (RadiusNode)treeline.getRoot();

		if (treeline.getAffineTransform().getType() != AffineTransform.TYPE_TRANSLATION)
			System.out.println("Affine transformation does more than a pure translation, " 
				+ "cannot deal with that.... ignoring!");

		// transform coordinates relative to absolute coordinates
		double sx, sy;
		if (!treeline.getAffineTransform().isIdentity()) {
			final float[] dps = new float[]{treelineRoot.getX(), treelineRoot.getY()};
			treeline.getAffineTransform().transform(dps, 0, dps, 0, 1);
			sx = dps[0];
			sy = dps[1];
		}
		else {
			sx = treelineRoot.getX();
			sy = treelineRoot.getY();
		}
		MTBRootTreeNodeData rootData = new MTBRootTreeNodeData(sx, sy);
		rootData.setRadius(treelineRoot.getData());
		rootData.setLayer(this.getLayerIndex(treelineRoot.getLayer()));
		rootData.setConnectorIDs(this.getConnectorIDs(treeline));
		rootData.setStatus(treelineRoot.getConfidence());
		MTBRootTree rootTree = new MTBRootTree(rootData);
		this.convertSubtreelineToMTBRootTree(treeline.getAffineTransform(), treelineRoot, rootTree.getRoot());
		return rootTree;
	}

	/**
	 * Converts a rhizoTrak {@link Treeline} subtree to a {@link MTBRootTree} object.
	 * <p>
	 * This method works in a recursive fashion on the given trees.
	 * 
	 * @param at	Affine transformation to be applied to the tree coordinates.
	 * @param treelineRootNode	Root node of the rhizoTrak treeline to be converted.
	 * @param rootTreeRootNode	Root node of the target MTBRootTree.
	 */
	protected void convertSubtreelineToMTBRootTree(AffineTransform at, 
			Node<Float> treelineRootNode, MTBTreeNode rootTreeRootNode) {
		double sx, sy;
		for (Node<Float> n: treelineRootNode.getChildrenNodes()) {
			// transform coordinates relative to absolute coordinates
			if (!at.isIdentity()) {
				final float[] dps = new float[]{n.getX(), n.getY()};
				at.transform(dps, 0, dps, 0, 1);
				sx = dps[0];
				sy = dps[1];
			}
			else {
				sx = n.getX();
				sy = n.getY();
			}
			MTBRootTreeNodeData dat = new MTBRootTreeNodeData(sx, sy);
			MTBTreeNode nn = new MTBTreeNode(dat);
			rootTreeRootNode.addChild(nn);
			this.convertSubtreelineToMTBRootTree(at, n, nn);
		}
	}

	/**
	 * Imports an root trees into the given layer replacing the old treelines
	 * associated with these trees.
	 * 
	 * @param layerZ				Target layer ID where to insert the treeline.
	 * @param layer					Target layer itself.
	 * @param rootTrees			Set of {@link MTBRootTree} to import.
	 * @param treelineList	List of treelines to be replaced with given root trees.
	 */
	public void importMTBRootTreesReplace(int layerZ, Layer layer, Vector<MTBRootTree> rootTrees, 
			ArrayList<Displayable> treelineList) {
		this.convertRootTreesToTreelines(rootTrees, treelineList, layer, null);
	}

	/**
	 * Imports root trees into the currently active layer.
	 * 
	 * @param rootTrees			Set of {@link MTBRootTree} to import.
	 * @param targetStatus	Target status the new segments should get.
	 */
	public void importMTBRootTreesAddToCurrentLayer(Vector<MTBRootTree> rootTrees, 
			String targetStatus) {
		Display display = Display.getFront();	
		Layer currentLayer = display.getLayer();
		this.importMTBRootTreesAddToLayer(this.getLayerIndex(currentLayer), rootTrees, targetStatus);
	}

	/**
	 * Imports root trees into the specified layer.
	 * 
	 * @param layerZ				Layer ID of target layer.
	 * @param rootTrees			Set of {@link MTBRootTree} to import.
	 * @param targetStatus	Target status the new segments should get.
	 */
	public void importMTBRootTreesAddToLayer(int layerZ, Vector<MTBRootTree> rootTrees,
			String targetStatus) {

		Display display = Display.getFront();	
		Project project = display.getProject();
		ProjectTree projectTree = project.getProjectTree();
		LayerSet layerSet = display.getLayerSet();  
		ArrayList<Layer> layers = layerSet.getLayers();
				
		if(layers.isEmpty()) {
			return; 
		}
				
		// get image names
		// List<Patch> patches = layerSet.getAll(Patch.class);
		// ImagePlus imagePlus = null;
		// if(!patches.isEmpty()) 
		// {	
		// 	imagePlus = patches.get(0).getImagePlus();
		// }
					
		// String[] imageNames = new String[layers.size()];
		// if(null != imagePlus) 
		// {	
		// 	imageNames = imagePlus.getImageStack().getSliceLabels();
		// }
				
		// get target layer				
		Layer targetLayer = layers.get(layerZ); // order of rootsets has to correspond to the layer if we don't care about image names
			
		// check if rootstack exists, otherwise create it
		ProjectThing possibleParent = RhizoAddons.findParentAllowing("treeline", project);
		if(possibleParent == null) 	{
			try {
				ProjectThing rootNode = null;
				rootNode = (ProjectThing) projectTree.getRoot().getUserObject();
				if ( rootNode != null ) {
					ProjectThing rootstackThing = rootNode.createChild("rootstack");
					DefaultMutableTreeNode node = new DefaultMutableTreeNode(rootstackThing);
					DefaultMutableTreeNode parentNode = DNDTree.findNode(rootNode, projectTree);
					((DefaultTreeModel) projectTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
				} 
				else {	
					Utils.showMessage("Project does not contain object that can hold treelines.");
					return;
				} 
			} 
			catch (Exception ex) {
				Utils.showMessage("Project does not contain object that can hold treelines.");
				return; 
			}
		}
		
		//create new list of empty treelines
		List<Displayable> treelineList = RhizoUtils.addDisplayableToProject(project, "treeline", rootTrees.size());

		this.convertRootTreesToTreelines(rootTrees, treelineList, targetLayer, targetStatus);

		RhizoUtils.repaintTreelineList(treelineList);
	}

	/**
	 * Converts the set of root trees into corresponding treelines.
	 * <p>
	 * Old data potentially existing in treeline objects will be deleted.
	 * 
	 * @param rootTrees			List of root trees to be converted.
	 * @param treelineList	Target list of treelines to be replaced.
	 * @param targetLayer		Target layer to which the treelines should be added.
	 * @param targetStatus	Target status for the treelines.
	 */
	protected void convertRootTreesToTreelines(Vector<MTBRootTree> rootTrees, 
			List<Displayable> treelineList, Layer targetLayer, String targetStatus) {

		AffineTransform at;
		MTBTreeNode root;
		MTBRootTree rootTree;
		RadiusNode nn;
		Treeline treeline;
		int treelineIndex = 0;

		for (int j=0; j<rootTrees.size(); ++j) {

			rootTree = rootTrees.elementAt(j);
			root = rootTree.getRoot();

			float xmin = Float.MAX_VALUE, ymin = Float.MAX_VALUE;
			Vector<MTBTreeNode> nodes = rootTree.getAllNodesDepthFirst();
			for (MTBTreeNode tn: nodes) {
				if (((MTBRootTreeNodeData)tn.getData()).getXPos() < xmin)
					xmin = (float)((MTBRootTreeNodeData)tn.getData()).getXPos();
				if (((MTBRootTreeNodeData)tn.getData()).getYPos() < ymin)
					ymin = (float)((MTBRootTreeNodeData)tn.getData()).getYPos();
			}
			at = new AffineTransform();
			at.setToTranslation(xmin, ymin);

			// get treeline (now existing)
			treeline = (Treeline) treelineList.get(treelineIndex);
			// clear treeline by removing all nodes, if there are any
			if (treeline.getRoot() != null)
				treeline.removeNode(treeline.getRoot());
			treeline.setAffineTransform(at);
			treeline.setLayer(targetLayer);
					
			// TODO: this is a workaround for the repainting issues that occur when creating new nodes out of a mtbxml file
			targetLayer.mtbxml = true;
						
			// assuming that default status is defined
			byte s = (byte) RhizoProjectConfig.STATUS_UNDEFINED;
			if (     targetStatus != null 
					&& !(targetStatus.equals("STATUS_UNDEFINED") || targetStatus.equals("UNDEFINED"))) {
				if ( targetStatus.equals("LIVING") ) 			
					s = (byte) 0;
				else if ( targetStatus.equals("DEAD") ) 		
					s = (byte) 1;
				else if ( targetStatus.equals("DECAYED") )	
					s = (byte) 2;
				else if ( targetStatus.equals("GAP") )		
					s = (byte) 3;
			}

			nn = new RadiusNode((float)((MTBRootTreeNodeData)root.getData()).getXPos() - xmin, 
				(float)((MTBRootTreeNodeData)root.getData()).getYPos() - ymin, targetLayer, (float)0.0);
			treeline.setRoot(nn);
			treeline.addNode(null, nn, (byte)s, true);

			this.convertRootTreeToTreeline(root, treeline, nn, targetLayer, xmin, ymin, s);

			// display treeline
			treeline.updateCache();
			treelineIndex++;
		}
	}

	/**
	 * Recursively converts a single root tree into a treeline.
	 * 
	 * @param rootTreeNode		Root node of the root tree to convert.
	 * @param targetTreeLine	Target treeline.
	 * @param n								Target node in result treeline.
	 * @param layer						Target layer of the treeline.
	 * @param xmin						Left boundary of bounding box for affine transformation.
	 * @param ymin						Upper boundary of bounding box for affine transformation.
	 * @param status					Target status of the treeline nodes.
	 */
	protected void convertRootTreeToTreeline(MTBTreeNode rootTreeNode, Treeline targetTreeLine, 
			Node<Float> n, Layer layer, float xmin, float ymin, byte status) {
		for (MTBTreeNode tnn: rootTreeNode.getChilds()) {
			RadiusNode nn = new RadiusNode(
				(float)((MTBRootTreeNodeData)tnn.getData()).getXPos() - xmin, 
				(float)((MTBRootTreeNodeData)tnn.getData()).getYPos() - ymin, layer, (float) 0.0);
			targetTreeLine.addNode(n, nn, (byte)status, true);
			this.convertRootTreeToTreeline(tnn, targetTreeLine, nn, layer, xmin, ymin, status);
		}
	}

	/**
	 * Get the index of a given layer.
	 * @param queryLayer	Layer for which index is to be retrieved.
	 * @return	The index of the layer.
	 */
	private int getLayerIndex(Layer queryLayer) {
		Display display = Display.getFront();	
		LayerSet layerSet = display.getLayerSet();  
		ArrayList<Layer> layers = layerSet.getLayers();

		int layerIndex = 0;
		for ( Layer layer : layers ) {
			if ( layer.equals(queryLayer) ) {
				return layerIndex;
			}
			layerIndex++;
		}
		return -1;
	}

	/**
	 * Get the IDs of all connectors a treeline is associated with.
	 * 
	 * @param treeline	Treeline for which connector IDs should be retrieved.
	 * @return	List of connector IDs, usually just containing one element.
	 */
	private long[] getConnectorIDs(Treeline treeline) {
		// find connector(s) of selected treeline
		long[] connectorIDs = null;

		List<TreeEventListener> treelineListener = treeline.getTreeEventListener();
		if (treelineListener != null && treelineListener.size() > 0) { 
			
			connectorIDs = new long[treelineListener.size()];
			int index = 0;
			for(TreeEventListener tel: treeline.getTreeEventListener()) { 
				connectorIDs[index] = tel.getConnector().getId();
				++index;
			}  
		}			
		return connectorIDs;
	}

	// public static void propagateRadiiToNextLayer(Treeline sourceLine) {


	// 	// search for treeline in next layer
	// 	Treeline targetLine = null;
	// 	Set<Node<Float>> targetNodes = null;
	// 	for (Treeline t : ctl) {
	// 		if (t.getFirstLayer().equals(nextLayer)) {
	// 			targetLine = t;
	// 			targetNodes = t.node_layer_map.get(nextLayer);
	// 			break;
	// 		}
	// 	}
	// 	if (targetNodes == null) return;
		
	// 	float dist, minDist;
	// 	Node<Float> cNode = null;
	// 	Set<Node<Float>> sourceNodes = 
	// 			sourceLine.getNodesAt(sourceLine.getFirstLayer());
					
	// 	float sx=0, sy=0, tx=0, ty=0;
	// 	for (Node<Float> sn: sourceNodes) {
			
	// 		cNode = null;
	// 		sx = sn.x; sy = sn.y;
			
	// 		// transform coordinates
	// 		if (!sourceLine.at.isIdentity()) {
	// 			final float[] dps = new float[]{sn.x, sn.y};
	// 			sourceLine.at.transform(dps, 0, dps, 0, 1);
	// 			sx = dps[0];
	// 			sy = dps[1];
	// 		}

	// 		// find for each node the closest one of treeline in next layer
	// 		minDist = Float.MAX_VALUE;
	// 		for (Node<Float> tn: targetNodes) {		
				
	// 			tx = tn.x; ty = tn.y;
				
	// 			// transform coordinates
	// 			if (!targetLine.at.isIdentity()) {
	// 				final float[] dps = new float[]{tn.x, tn.y};
	// 				targetLine.at.transform(dps, 0, dps, 0, 1);
	// 				tx = dps[0];
	// 				ty = dps[1];
	// 			}

	// 			// Euclidean distance
	// 			dist = (sx-tx)*(sx-tx) + (sy-ty)*(sy-ty);
	// 			if (dist < minDist) {
	// 				minDist = dist;
	// 				cNode = tn;
	// 			}
	// 		}				
	// 		// copy radius of closest node 
	// 		if (cNode != null)
	// 			cNode.setData(sn.getData());
	// 	}
	// 	// repaint all the nodes
	// 	Display.repaint(getLayerSet());
	// }

}
