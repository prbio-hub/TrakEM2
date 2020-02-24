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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.ALDOperatorCollectionElement;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorCollectionEvent;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorCollectionEventListener;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorCollectionEvent.ALDOperatorCollectionEventType;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.datatypes.MTBRootTree;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.datatypes.MTBRootTreeNodeData;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.RootImageSegmentationOperator;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.RootImageSegmentationOperator.LayerSubset;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.RootImageSegmentationOperator.OpWorkingMode;
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
import de.unihalle.informatik.rhizoTrak.display.Selection;
import de.unihalle.informatik.rhizoTrak.display.TreeEventListener;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.IJError;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;

public class RhizoRootImageSegmentationManager implements ActionListener, ALDOperatorCollectionEventListener {

	/**
	 * Reference to display in rhizoTrak;
	 */
	private Display rhizoDisplay;

	/**
	 * Panel to be shown in GUIs to allow for selection, configuration and running
	 * of operators.
	 */
	private JPanel segOpPanel = null;

	private JButton operatorRunButton;

	private JButton operatorConfigButton;

	/**
	 * Set of available operators managed by this class.
	 */
	private MTBOperatorCollection<RootImageSegmentationOperator> segOperatorCollection;

	/**
	 * List shown in GUI to select operator(s).
	 */
	private JList<String> operatorDisplayList;

	/**
	 * Currently selected operator.
	 */
	private ALDOperatorCollectionElement selectedSegOp;

	/**
	 * Stores treelines handed-over to operator for later update steps.
	 */
	private HashMap<Integer, ArrayList<Displayable>> treelinesUnderProcessing;

	/**
	 * Stores the layer active upon calling an operator.
	 */
	private Layer activeLayer;

	/**
	 * Reference to the layers of the project.
	 */
	private LinkedList<Layer> projectLayers;

	public RhizoRootImageSegmentationManager(Display d) {
		this.rhizoDisplay = d;
		try {
			this.segOperatorCollection = new MTBOperatorCollection<RootImageSegmentationOperator>(
				RootImageSegmentationOperator.class);
			// allows to restart the operator with the same parameter values
			this.segOperatorCollection.setRerunFlags(true);
			this.segOperatorCollection.addALDOperatorCollectionEventListener(this);
		} catch (InstantiationException | ALDOperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getNumberOfAvailableOperators() {
		if (this.segOperatorCollection != null) 
			return this.segOperatorCollection.getAvailableClasses().size();
		return 0;
	}

	public JPanel getSegmentationOpsPanel() {

		if (this.segOpPanel != null)
			return this.segOpPanel;

		// GUI panels for annotation enhancement
		JPanel groupImageSegmentOpsLabel = new JPanel(new GridLayout(0, 1, 5, 1));
		JPanel groupImageSegmentOpsSelection = new JPanel(new GridLayout(0, 1, 5, 1));
		JPanel groupImageSegmentOpsRunConfig = new JPanel(new GridLayout(0, 2, 5, 1));

		this.segOpPanel = new JPanel();
		this.segOpPanel.setLayout(new BoxLayout(this.segOpPanel, BoxLayout.Y_AXIS));
		JScrollPane	scrollPane = this.getScrollableSegmentationOperatorList();
		JLabel labelOperators = new JLabel("Available Segmentation Operators:", JLabel.LEFT);
		groupImageSegmentOpsLabel.add(labelOperators);
		groupImageSegmentOpsSelection.add(scrollPane);
					
		this.operatorRunButton = new JButton("Run");
		this.operatorRunButton.setToolTipText("Runs the selected image segmentation operator.");
		this.operatorRunButton.setActionCommand("runOperator");
		this.operatorRunButton.addActionListener(this);
		groupImageSegmentOpsRunConfig.add(this.operatorRunButton);
				
		this.operatorConfigButton = new JButton("Configure");
		this.operatorConfigButton.setToolTipText("Opens a new window to configure the selected operator.");
		this.operatorConfigButton.setActionCommand("configureOperator");
		this.operatorConfigButton.addActionListener(this);
		groupImageSegmentOpsRunConfig.add(this.operatorConfigButton);

		final int VGAP = 8;
		this.segOpPanel.add(Box.createRigidArea(new Dimension(0, VGAP)));
		this.segOpPanel.add(groupImageSegmentOpsLabel);
		this.segOpPanel.add(groupImageSegmentOpsSelection);
		this.segOpPanel.add(Box.createRigidArea(new Dimension(0, VGAP)));
		this.segOpPanel.add(groupImageSegmentOpsRunConfig);
		this.segOpPanel.add(Box.createRigidArea(new Dimension(0, VGAP)));
		this.segOpPanel.add(new JSeparator(JSeparator.HORIZONTAL));

		return this.segOpPanel;
	}

	/**
	 * Collect all operators available for image segmentation.
	 * @return Scroll pane with available operators.
	 */
	private JScrollPane getScrollableSegmentationOperatorList() {	
		
		Vector<String> operatorList = new Vector<String>();

		Collection<String> uniqueOpIDs = this.segOperatorCollection.getUniqueClassIDs();
		operatorList.addAll(uniqueOpIDs);
		Collections.sort(operatorList);

		this.operatorDisplayList = new JList<String>(operatorList);
		this.operatorDisplayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// first operator selected as default
		this.operatorDisplayList.setSelectedIndex(0);
		// only as high as needed for all found operators
		this.operatorDisplayList.setVisibleRowCount(operatorList.size());
				
		JScrollPane scroll = new JScrollPane(this.operatorDisplayList);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		return scroll;
	}

	@Override
	public void actionPerformed(final ActionEvent ae) {

		final String command = ae.getActionCommand();

		if(command.equals("configureOperator")){
			this.configureImageSegmentationOperator();
		}
		else if(command.equals("runOperator")) {
			this.runImageSegmentationOperator();
		}
	}


		/**
	 * Configure the annotation enhancer operators.
	 */
	public void configureImageSegmentationOperator() {
		String selectedOperatorName = this.operatorDisplayList.getSelectedValue();
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
				this.segOperatorCollection.openOperatorConfigWindow(selectedOperatorName);
			} 
		}
		else {
			JOptionPane.showMessageDialog(null, "Please choose an operator to configure.", 
				"Choose operator", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Execute the selected segmentation operator.
	 */ 
	public void runImageSegmentationOperator() {

		// get the currently selected operator
		String selectedSegOpName = this.operatorDisplayList.getSelectedValue();
		this.selectedSegOp = this.segOperatorCollection.getOperator(selectedSegOpName);

		if( this.selectedSegOp != null ) {
			this.operatorRunButton.setEnabled(false);

			RootImageSegmentationOperator imSegOp = (RootImageSegmentationOperator)this.selectedSegOp;
					
			// ask the operator which data to process
			EnumSet<LayerSubset> requestedImages = imSegOp.getLayerSubsetForInputImages();
			EnumSet<LayerSubset> requestedTreelines = imSegOp.getLayerSubsetForInputTreelines();

			// get requested images
			this.activeLayer = Display.getFrontLayer();
			LayerSet currentLayerSet = this.rhizoDisplay.getLayerSet();

			this.projectLayers = new LinkedList<>();

			// add layers up to previous
			int z=0;
			int prevLayers = 0;
			for (z=0; z<activeLayer.getZ(); ++z) {
				projectLayers.add(currentLayerSet.getLayer(z));
				++prevLayers;
			}

			// add active one
			this.projectLayers.add(activeLayer);

			// add layers up to the end
			for (z = z+1; z<currentLayerSet.size(); ++z) {
				this.projectLayers.add(currentLayerSet.getLayer(z));
			}

			int prevLayerID = prevLayers - 1;
			int activeLayerID = prevLayers;
			int nextLayerID = activeLayerID + 1;

			// System.out.println("Prev = " + prevLayerID);
			// System.out.println("Act = " + activeLayerID);
			// System.out.println("Next = " + nextLayerID);

			// fill image map
			HashMap<Integer, ImagePlus> inputImages = new HashMap<>();
			if (		requestedImages.contains(LayerSubset.FIRST_TO_PREVIOUS)
					|| 	requestedImages.contains(LayerSubset.ALL)) {
				for (int i=0; i<prevLayerID; ++i)
					inputImages.put(i, 
						this.projectLayers.get(i).getPatches(true).get(0).getImagePlus());
			}
			if (		requestedImages.contains(LayerSubset.PREVIOUS)
					|| 	requestedImages.contains(LayerSubset.ALL)) {
				inputImages.put(prevLayerID, 
					this.projectLayers.get(prevLayerID).getPatches(true).get(0).getImagePlus());	
			}
			if (		requestedImages.contains(LayerSubset.ACTIVE)
					|| 	requestedImages.contains(LayerSubset.ALL)) {
				inputImages.put(activeLayerID, 
					this.projectLayers.get(activeLayerID).getPatches(true).get(0).getImagePlus());	
			}
			if (		requestedImages.contains(LayerSubset.NEXT)
					|| 	requestedImages.contains(LayerSubset.ALL)) {
				inputImages.put(nextLayerID, 
					this.projectLayers.get(nextLayerID).getPatches(true).get(0).getImagePlus());	
			}
			if (		requestedImages.contains(LayerSubset.NEXT_TO_LAST)
					|| 	requestedImages.contains(LayerSubset.ALL)) {
				for (int i=nextLayerID+1; i<this.projectLayers.size(); ++i)
					inputImages.put(i, 
						this.projectLayers.get(i).getPatches(true).get(0).getImagePlus());	
			}
		
			// fill treelines map(s)
			this.treelinesUnderProcessing = new HashMap<>();
			HashMap<Integer, Vector<MTBRootTree>> inputTreelines = new HashMap<>();
			boolean onlySelected = imSegOp.getOnlySelectedTreelines();
			if (		requestedTreelines.contains(LayerSubset.FIRST_TO_PREVIOUS)
					|| 	requestedTreelines.contains(LayerSubset.ALL)) {
				for (int i=0; i<prevLayerID; ++i) {
					this.getTreelinesFromLayer(i, this.projectLayers.get(i), onlySelected,
						this.treelinesUnderProcessing, inputTreelines);
				}
			}
			if (		requestedTreelines.contains(LayerSubset.PREVIOUS)
					|| 	requestedTreelines.contains(LayerSubset.ALL)) {
				this.getTreelinesFromLayer(prevLayerID, this.projectLayers.get(prevLayerID), onlySelected,
					this.treelinesUnderProcessing, inputTreelines);
			}
			if (		requestedTreelines.contains(LayerSubset.ACTIVE)
					|| 	requestedTreelines.contains(LayerSubset.ALL)) {
				this.getTreelinesFromLayer(activeLayerID, this.projectLayers.get(activeLayerID), onlySelected,
					this.treelinesUnderProcessing, inputTreelines);
			}
			if (		requestedTreelines.contains(LayerSubset.NEXT)
					|| 	requestedTreelines.contains(LayerSubset.ALL)) {
				this.getTreelinesFromLayer(nextLayerID, this.projectLayers.get(nextLayerID), onlySelected,
					this.treelinesUnderProcessing, inputTreelines);
			}
			if (		requestedTreelines.contains(LayerSubset.NEXT_TO_LAST)
					|| 	requestedTreelines.contains(LayerSubset.ALL)) {
				for (int i=nextLayerID+1; i<this.projectLayers.size(); ++i) {
					this.getTreelinesFromLayer(i, this.projectLayers.get(i), onlySelected,
						this.treelinesUnderProcessing, inputTreelines);
				}
			}
		
			// configure operator
			((RootImageSegmentationOperator)this.selectedSegOp).setInputImages(inputImages);
			((RootImageSegmentationOperator)this.selectedSegOp).setInputTreelines(inputTreelines);

			// run the operator
			LinkedList<String> operatorList = new LinkedList<String>();
			operatorList.add(selectedSegOpName);
			this.segOperatorCollection.runOperators(operatorList);
		}
		else {
			JOptionPane.showMessageDialog(null, "Please choose a segmentation operator to run!", 
				"Choose operator", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void getTreelinesFromLayer(int layerID, Layer layer, boolean onlySelected,
			HashMap<Integer, ArrayList<Displayable>> treelines, 
				HashMap<Integer, Vector<MTBRootTree>> rootTrees) {

		RhizoTreelineImportExport converter = new RhizoTreelineImportExport();

		Selection selection = this.rhizoDisplay.getSelection();
		
		ArrayList<Displayable> tlines = new ArrayList<>();
		ArrayList<Displayable> selObjs  = selection.getSelected(Treeline.class);
		if (onlySelected && !selObjs.isEmpty()) {
			for (Displayable d : selObjs) {
				Treeline t = (Treeline)d;
				// check if treeline starts in target layer, otherwise skip
				if ((int)(t.getFirstLayer().getCalibratedZ()) == layerID) 
					tlines.add(t);
			}
		}
		else {
			tlines = layer.getDisplayables(Treeline.class);
		}

		Vector<MTBRootTree> tset = new Vector<>();
		for (Displayable tl: tlines)
			tset.add(converter.exportTreelineToMTBRootTree((Treeline)tl));

		treelines.put(layerID, tlines);
		rootTrees.put(layerID, tset);
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

	@Override
	public void handleALDOperatorCollectionEvent(ALDOperatorCollectionEvent event) {

 		if ( event.getEventType() == ALDOperatorCollectionEventType.RESULTS_AVAILABLE ) {
 		 	this.operatorRunButton.setEnabled(true);
			 
			RootImageSegmentationOperator segOp = (RootImageSegmentationOperator)this.selectedSegOp;

 			// get results from operator
			HashMap<Integer, Vector<MTBRootTree>> resultTreelines = segOp.getAllResultTreelines();
			 
			switch(segOp.getOperatorWorkingMode())
			{
				case SEGMENTATION_CREATE:
				case SEGMENTATION_UPDATE:
				{
					JDialog d = new JDialog();

					System.out.println("Here we are...");

		 			/*
 					 *  starts two threads: first one to transfer the polylines to treelines, second on to freeze the GUI through
 			 		 *  	a modal window. The window is then closed by the first thread if the transfer has been finished.
			 		 */
					Thread showDialog = new Thread() {
						public void run() {
							JLabel l = new JLabel("Transfer of result treelines in progress ... please wait!", 
								JLabel.CENTER); 
							l.setVerticalAlignment(JLabel.CENTER);
							d.add(l);
							d.setTitle("Transfer to treelines");
							d.setSize(600,200);
							d.setModal(true);
							d.setVisible(true);
						}
					};
					showDialog.start();

					// transfer the modified treelines back into the corresponding layers
		 			Thread transferTreelines = new Thread() {
		 				public void run()	{

							Set<Integer> layerIDs = resultTreelines.keySet();
							for (Integer id: layerIDs) {

								System.out.println(RhizoRootImageSegmentationManager.this.projectLayers.get(id));

								// make a copy of the old treelines
								List<Treeline> formerTreelines = new LinkedList<>();
								ArrayList<Displayable> treelines = 
									RhizoRootImageSegmentationManager.this.treelinesUnderProcessing.get(id);
 					  		for (Displayable t: treelines)
 		  						formerTreelines.add((Treeline)t.clone());

 		 						RhizoTreelineImportExport converter = new RhizoTreelineImportExport();
								converter.importMTBRootTreesReplace(id.intValue(), 
									RhizoRootImageSegmentationManager.this.projectLayers.get(id), 
										resultTreelines.get(id),
											RhizoRootImageSegmentationManager.this.treelinesUnderProcessing.get(id));

			 					// transfer status, radius and connector information from old to new treeline
//						 		this.transferTreelineProperties(formerTreelines, this.treelinesUnderProcessing);	

 		  					RhizoUtils.repaintTreelineList(
									RhizoRootImageSegmentationManager.this.treelinesUnderProcessing.get(id));
							}
							d.setVisible(false);
							d.dispose(); 
						}
 		 			};
					transferTreelines.start();
						
					break;
				}
				case SEGMENTATION_CREATE_AND_UPDATE:
			}
// // 					// Map<Integer, Map<Integer, de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.Node>> resultLineMap = null;
			 
// // 					// 	int sizeStatusLabel = getProject().getRhizoMain().getProjectConfig().sizeStatusLabelMapping();
// // 					// 	String[] fullNames = new String[sizeStatusLabel+1];
// // 					// 	for ( int i = 0; i < sizeStatusLabel; i++) 
// // 					// 	{
// // 					// 		fullNames[i] = getProject().getRhizoMain().getProjectConfig().getStatusLabel(i).getName();
// // 					// 	}
// // 					// 	fullNames[sizeStatusLabel] = "STATUS_UNDEFINED";

// // 					// 	final String status = (String) JOptionPane.showInputDialog(null,
// // 					// 			"Which status should the treeline nodes have?\n"
// // 					// 			+ "If you then press \'OK\', "
// // 					// 			+ "the treelines will be imported in the image.",
// // 					// 			"Choose status", JOptionPane.PLAIN_MESSAGE,
// // 					// 			null, fullNames, fullNames[0]);
// // 					// 	if( status != null )
// // 					// 	{
// // 					// 		if(operator instanceof RootSegmentationOperator)
// // 					// 		{
// // 					// 			resultLineMap = ((RootSegmentationOperator) operator).getMap();
// // 					// 		}
				 
				 
					
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
// } catch (InstantiationException | ALDOperatorException e) {
// 	// TODO Auto-generated catch block
// 	e.printStackTrace();
}
