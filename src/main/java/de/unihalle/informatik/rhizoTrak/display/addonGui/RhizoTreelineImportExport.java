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

import java.awt.GridLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.unihalle.informatik.MiToBo.apps.minirhizotron.datatypes.MTBRootTree;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.datatypes.MTBRootTreeNodeData;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBTreeNode;
import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.addon.RhizoMain;
import de.unihalle.informatik.rhizoTrak.addon.RhizoProjectConfig;
import de.unihalle.informatik.rhizoTrak.addon.RhizoUtils;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.TreeEventListener;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class RhizoTreelineImportExport {	

	public RhizoTreelineImportExport() {
	}

	public MTBRootTree exportTreelineToMTBRootTree(Treeline treeline) {

		RadiusNode treelineRoot = (RadiusNode)treeline.getRoot();

		if (treeline.getAffineTransform().getType() != AffineTransform.TYPE_TRANSLATION)
			System.out.println("Affine transformation does more than a pure translation, cannot deal with that.");

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

	protected void convertSubtreelineToMTBRootTree(AffineTransform at, Node<Float> treelineRootNode, MTBTreeNode rootTreeRootNode) {
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

	public void importMTBRootTreesReplace(Vector<MTBRootTree> rootTrees, List<Displayable> treelineList) {

		Display display = Display.getFront();	
		Layer currentLayer = display.getLayer();
		this.convertRootTreesToTreelines(rootTrees, treelineList, currentLayer);
	}

	public void importMTBRootTreesAddToCurrentLayer(Vector<MTBRootTree> treelines) {
		Display display = Display.getFront();	
		Layer currentLayer = display.getLayer();
		this.importMTBRootTreesAddToLayer(treelines, this.getLayerIndex(currentLayer));
	}

	public void importMTBRootTreesAddToLayer(Vector<MTBRootTree> rootTrees, int layerIndex) {

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
		Layer targetLayer = layers.get(layerIndex); // order of rootsets has to correspond to the layer if we don't care about image names
			
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

		this.convertRootTreesToTreelines(rootTrees, treelineList, targetLayer);

		RhizoUtils.repaintTreelineList(treelineList);
	}

	protected void convertRootTreesToTreelines(Vector<MTBRootTree> rootTrees, List<Displayable> treelineList, 
			Layer targetLayer) {

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
			// clear treeline by removing all nodes
			treeline.removeNode(treeline.getRoot());
			treeline.setAffineTransform(at);
			treeline.setLayer(targetLayer);
					
			// TODO: this is a workaround for the repainting issues that occur when creating new nodes out of a mtbxml file
			targetLayer.mtbxml = true;
						
			// assuming that default status is defined
			byte s = (byte) RhizoProjectConfig.STATUS_UNDEFINED;
			//if ( status != null && !(status.equals("STATUS_UNDEFINED") || status.equals("UNDEFINED")) )
					// 	{
					// 		if ( status.equals("LIVING") ) 			s = (byte) 0;
					// 		else if ( status.equals("DEAD") ) 		s = (byte) 1;
					// 		else if ( status.equals("DECAYED") )	s = (byte) 2;
					// 		else if ( status.equals("GAP") )		s = (byte) 3;
					// 	}

			nn = new RadiusNode((float)((MTBRootTreeNodeData)root.getData()).getXPos() - xmin, 
				(float)((MTBRootTreeNodeData)root.getData()).getYPos() - ymin, targetLayer, (float)0.0);
			treeline.addNode(null, nn, (byte)0, true);
			treeline.setRoot(nn);

			this.rootTreeToTreeline(root, treeline, nn, targetLayer, xmin, ymin);

			// display treeline
			treeline.updateCache();
			treelineIndex++;
		}
	}

	protected void rootTreeToTreeline(MTBTreeNode rootTreeNode, Treeline targetTreeLine, Node<Float> n, Layer layer, float xmin, float ymin) {
		for (MTBTreeNode tnn: rootTreeNode.getChilds()) {
			RadiusNode nn = new RadiusNode(
				(float)((MTBRootTreeNodeData)tnn.getData()).getXPos() - xmin, 
				(float)((MTBRootTreeNodeData)tnn.getData()).getYPos() - ymin, layer, (float) 0.0);
			targetTreeLine.addNode(n, nn, (byte)0, true);
			this.rootTreeToTreeline(tnn, targetTreeLine, nn, layer, xmin, ymin);
		}
	}

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
