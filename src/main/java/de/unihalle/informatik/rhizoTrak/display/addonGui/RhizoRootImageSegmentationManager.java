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
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.unihalle.informatik.Alida.operator.ALDOperatorCollectionElement;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorCollectionEvent;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorCollectionEventListener;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorCollectionEvent.ALDOperatorCollectionEventType;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.datatypes.MTBRootTree;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.datatypes.MTBRootTreeNodeData;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.RootImageSegmentationOperator;
import de.unihalle.informatik.MiToBo.core.datatypes.MTBTreeNode;
import de.unihalle.informatik.MiToBo.core.operator.MTBOperatorCollection;
import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.addon.RhizoMain;
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
import de.unihalle.informatik.rhizoTrak.utils.IJError;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;

public class RhizoRootImageSegmentationManager {	

	private JButton enhancerRunButton;

	private MTBOperatorCollection<RootImageSegmentationOperator> annotationEnhancerCollection;

	private JList<String> annotationEnhancerList;

	private ALDOperatorCollectionElement annotationEnhancer;

	private List<Displayable> treelinesUnderProcessing;

	private Layer activeLayer;

	public RhizoRootImageSegmentationManager(JButton runButton) {
		this.enhancerRunButton = runButton;
	}

	public int getNumberOfAvailableOperators() {
		if (this.annotationEnhancerList != null) 
			return this.annotationEnhancerList.getModel().getSize();
		return 0;
	}

	/**
	 * Collect all operators available for annotation enhancement.
	 * @return Scroll pane with available operators.
	 */
	public JScrollPane getScrollableAnnotationEnhancerList() {	
		
		Vector<String> enhancerList = new Vector<String>();
					
		try {
		 	this.annotationEnhancerCollection = 
		 		new MTBOperatorCollection<RootImageSegmentationOperator>(RootImageSegmentationOperator.class);		
		 	// allows to restart the operator with the same parameter values
		 	this.annotationEnhancerCollection.setRerunFlags(true);
		 	this.annotationEnhancerCollection.addALDOperatorCollectionEventListener(
		 		new ALDOperatorCollectionEventListener() {	
		 			// Event Listener for operator
		 			@Override
		 			public void handleALDOperatorCollectionEvent(ALDOperatorCollectionEvent event) {
		 				if ( event.getEventType() == ALDOperatorCollectionEventType.RESULTS_AVAILABLE ) {
							RhizoRootImageSegmentationManager.this.enhancerRunButton.setEnabled(true);
								
		 					// get results from operator
							HashMap<Integer, Vector<MTBRootTree>> enhancedTreelines = 
								((RootImageSegmentationOperator)annotationEnhancer).getAllResultTreelines();
								
		// 					// Map<Integer, Map<Integer, de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.Node>> resultLineMap = null;
								
		// 					// 	int sizeStatusLabel = getProject().getRhizoMain().getProjectConfig().sizeStatusLabelMapping();
		// 					// 	String[] fullNames = new String[sizeStatusLabel+1];
		// 					// 	for ( int i = 0; i < sizeStatusLabel; i++) 
		// 					// 	{
		// 					// 		fullNames[i] = getProject().getRhizoMain().getProjectConfig().getStatusLabel(i).getName();
		// 					// 	}
		// 					// 	fullNames[sizeStatusLabel] = "STATUS_UNDEFINED";
		
		// 					// 	final String status = (String) JOptionPane.showInputDialog(null,
		// 					// 			"Which status should the treeline nodes have?\n"
		// 					// 			+ "If you then press \'OK\', "
		// 					// 			+ "the treelines will be imported in the image.",
		// 					// 			"Choose status", JOptionPane.PLAIN_MESSAGE,
		// 					// 			null, fullNames, fullNames[0]);
		// 					// 	if( status != null )
		// 					// 	{
		// 					// 		if(operator instanceof RootSegmentationOperator)
		// 					// 		{
		// 					// 			resultLineMap = ((RootSegmentationOperator) operator).getMap();
		// 					// 		}
									
 							/*
		 					 *  starts two threads: first one to transfer the polylines to treelines, second on to freeze the GUI through
		 					 *  	a modal window. The window is then closed by the first thread if the transfer has been finished.
		 					 */
		 					// 		final Map<Integer, Map<Integer, de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.Node>> rLM = resultLineMap;
							JDialog d = new JDialog();
									
							// transfer the result polylines to treelines
					 		Thread transferTreeline = new Thread() {
							 	public void run()	{
							// 		// make a copy of the old treelines
							// 		List<Treeline> formerTreelines = new LinkedList<>();
							// 		for (Displayable t: RhizoRootImageSegmentationManager.this.treelinesUnderProcessing)
							// 			formerTreelines.add((Treeline)t.clone());

							// 		RhizoTreelineImportExport converter = new RhizoTreelineImportExport();
							// 		converter.importMTBRootTreesReplace(enhancedTreelines, 
							// 			RhizoRootImageSegmentationManager.this.treelinesUnderProcessing);

							// 		// transfer status, radius and connector information from old to new treeline
							// 		RhizoRootImageSegmentationManager.this.transferTreelineProperties(
							// 			formerTreelines, RhizoRootImageSegmentationManager.this.treelinesUnderProcessing);	

							// 		RhizoUtils.repaintTreelineList(RhizoRootImageSegmentationManager.this.treelinesUnderProcessing);
							// 		d.setVisible(false);
							 	}
							};
		 							
							// modal window to freeze the GUI
					 		Thread showDialog = new Thread() {
					 			public void run() {
		 							JLabel l = new JLabel("Transfer of polylines to treelines in progress ... please wait!", JLabel.CENTER); 
									l.setVerticalAlignment(JLabel.CENTER);
		 							d.add(l);
		 							d.setTitle("Transfer to treelines");
		 							d.setSize(600,200);
									d.setModal(true);
		 							d.setVisible(true);
		 						}
							};
									
							// start both threads
							showDialog.start();
							transferTreeline.start();

						}	else if ( event.getEventType() == ALDOperatorCollectionEventType.OP_NOT_CONFIGURED ) {
							JOptionPane.showMessageDialog(null, "Operator not completely configured.", 
								"Configure operator", JOptionPane.ERROR_MESSAGE);
		 				} else if ( event.getEventType() == ALDOperatorCollectionEventType.RUN_FAILURE ) {
		 					JOptionPane.showMessageDialog(null, 
								"Something went wrong during execution of the operator.", 
									"Run failure", JOptionPane.ERROR_MESSAGE);
						} else if ( event.getEventType() == ALDOperatorCollectionEventType.INIT_FAILURE )	{
		 					JOptionPane.showMessageDialog(null, 
		 						"Operator is not well initialized.", 
		 							"Initialization failure", JOptionPane.ERROR_MESSAGE);
		 				} else { // ALDOperatorCollectionEventType.UNKNOWN
							 // do nothing
						}
					}
		 		}
		 	);
		 	Collection<String> uniqueClassIDs = annotationEnhancerCollection.getUniqueClassIDs();
		 	enhancerList.addAll(uniqueClassIDs);
		} catch (Exception e) {
		 	IJError.print(e);
		}
				
		Collections.sort(enhancerList);
		this.annotationEnhancerList = new JList<String>(enhancerList);
		this.annotationEnhancerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// first operator selected as default
		this.annotationEnhancerList.setSelectedIndex(0);
		// only as high as needed for all found operators
		this.annotationEnhancerList.setVisibleRowCount(enhancerList.size());
				
		JScrollPane scroll = new JScrollPane(this.annotationEnhancerList);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			
		return scroll;
	}

		/**
	 * Configure the annotation enhancer operators.
	 */
	public void configureImageSegmentationOperator() {
		String selectedOperatorName = this.annotationEnhancerList.getSelectedValue();
		if ( selectedOperatorName != null ) {
			// Checks if the window is already open
			boolean isConfigFrameOpen = false;
			for ( Display display : Display.getDisplays() ) {
				JFrame frame = display.getFrame();
				if ( frame.getTitle().contains("ALDOperatorConfigurationFrame:") && frame.isShowing() ) {
					isConfigFrameOpen = true;
				}
			}
			if ( !isConfigFrameOpen ) {
				this.annotationEnhancerCollection.openOperatorConfigWindow(selectedOperatorName);
			} 
		}
		else {
			JOptionPane.showMessageDialog(null, "Please choose an operator to configure.", 
				"Choose operator", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Execute the selected annotation enhancer.
	 * @param tls	Vector of treelines to improve.
	 */ 
	public void runImageSegmentationOperator(List<Displayable> tls) {

		this.treelinesUnderProcessing = tls;

		RhizoTreelineImportExport converter = new RhizoTreelineImportExport();

		// Gets the current image
		Layer l = Display.getFrontLayer();
		ImagePlus img = l.getPatches(true).get(0).getImagePlus();
		this.activeLayer = l;
		
		this.annotationEnhancer = this.annotationEnhancerCollection.getOperator(this.annotationEnhancerList.getSelectedValue());	
		if( this.annotationEnhancer != null ) {
			// this.enhancerRunButton.setEnabled(false);
			// // Add method of RootSegmentationOperator to set image 
			// if ( this.annotationEnhancer instanceof RootImageSegmentationOperator) {
			// 	((RootImageSegmentationOperator) this.annotationEnhancer).setImage(img);

			// 	Vector<MTBRootTree> tset = new Vector<>();
			// 	for (Displayable tl: tls) {
			// 		tset.add(converter.exportTreelineToMTBRootTree((Treeline)tl));
			// 	}
			// 	((RootImageSegmentationOperator) this.annotationEnhancer).setInputTreelines(tset);
			// }
		
			// LinkedList<String> operatorList = new LinkedList<String>();
			// // only one operator can be selected
			// System.out.println(this.annotationEnhancerList.getSelectedValue());
			// operatorList.add(this.annotationEnhancerList.getSelectedValue());
			// annotationEnhancerCollection.runOperators(operatorList);
		}
		else {
			JOptionPane.showMessageDialog(null, "Please choose an annotation enhancer to run.", 
				"Choose operator", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * 
	 * Note: connectors should hopefully have survived...
	 * 
	 * @param sourceTreelines
	 * @param targetTreelines
	 */
	private void transferTreelineProperties(List<Treeline> sourceTreelines, List<Displayable> targetTreelines) {

		Node<Float> minNode;
	 	float dist, minDist, sx=0, sy=0, tx=0, ty=0;
		for (int counter= 0; counter<sourceTreelines.size(); ++counter) {

			Treeline sourceTreeline = (Treeline)sourceTreelines.get(counter);
			Treeline targetTreeline = (Treeline)targetTreelines.get(counter);

			// iterate over nodes of new treeline and for each node search
			// for closest one in old treeline to copy properties
			Set<Node<Float>> sourceNodes = sourceTreeline.getNodesAt(this.activeLayer);
			Set<Node<Float>> targetNodes = RhizoAddons.getTreeLineNodeInstancesInLayer(targetTreeline, this.activeLayer);
			for (Node<Float> tn : targetNodes) {
				tx = tn.getX(); 
				ty = tn.getY();
			
				// transform coordinates
				if (!targetTreeline.getAffineTransform().isIdentity()) {
					final float[] dps = new float[]{tn.getX(), tn.getY()};
					targetTreeline.getAffineTransform().transform(dps, 0, dps, 0, 1);
					tx = dps[0];
					ty = dps[1];
				}

				minDist = Float.MAX_VALUE;
				minNode = null;
		 		for (Node<Float> sn: sourceNodes) {		
					sx = sn.getX();
					sy = sn.getY();
				
					// transform coordinates
					if (!sourceTreeline.getAffineTransform().isIdentity()) {
						final float[] dps = new float[]{sn.getX(), sn.getY()};
						sourceTreeline.getAffineTransform().transform(dps, 0, dps, 0, 1);
						sx = dps[0];
						sy = dps[1];
					}
					// Euclidean distance
					dist = (sx-tx)*(sx-tx) + (sy-ty)*(sy-ty);
					if (dist < minDist) {
						minDist = dist;
						minNode = sn;
					}
				}
				// copy data
				if (minNode != null) {
					tn.setData(minNode.getData());
					tn.setConfidence(minNode.getConfidence());
				}
			}
		}
	}
}
