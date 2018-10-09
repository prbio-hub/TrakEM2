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

import java.awt.GridLayout;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.display.addonGui.RSMLLoader;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.IntegerStringPairType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.ParentNode;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.PointType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.PropertyListType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType.Functions;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType.Functions.Function;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType.Functions.Function.Sample;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType.Geometry;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType.Geometry.Polyline;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Metadata;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Metadata.Image;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Metadata.PropertyDefinitions;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Metadata.PropertyDefinitions.PropertyDefinition;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Metadata.TimeSequence;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Scene;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Scene.Plant;

/**
 * I/O between rhizoTrak and RSML
 * 
 * @author posch
 *
 */

public class RhizoRSML
{
	//TODO general TODO: better always check if JAXB objects are null, even if they should not
	// e.g. geometry of a root - might happen with broken rsml files
	
	private boolean debug = false;
	
	private static final String RSML_VERSION = "1.0";

	private static final String SOFTWARE_NAME = "rhizoTrak";
	
	private static final String FUNCTION_NAME_DIAMETER = "diameter";
	private static final String FUNCTION_NAME_STATUSLABEL = "statusLabel";
	
	private static final String PROPERTY_NAME_PARENTNODE = "parentNode";
	private static final String PROPERTY_NAME_STATUSLABELMAPPING = "StatusLabelMapping";

	private static final Double EPSILON = 0.01;

	// TODO make this configurable
	private static byte default_statuslabel = 0;


	private RhizoMain rhizoMain;
	
	private File rsmlBaseDir = null;
	
	private final String ONLY_STRING = "Current layer only";
	private final String ALL_STRING = "All layers";

	public RhizoRSML(RhizoMain rhizoMain) {
		this.rhizoMain = rhizoMain;
	}

	private String projectName;
	
	/**
	 * true f the parent indices start with 1, otherwise it is assume the they start with 0
	 * 
	 */
	// TODO move to ProjectConfig and store in user settings
	private boolean parentNodeIndexStartsWithOne = true;

	
	private JFrame rsmlLoaderFrame;

	/**
	 *  Writes the current project to a RSML file.
	 *  @author Posch
	 */
	public void writeRSML() {
		projectName = rhizoMain.getXmlName().replaceFirst(".xml\\z", "");
		
		// query output options
		String[] choicesLayers = {ALL_STRING, ONLY_STRING};
		JComboBox<String> comboLayers = new JComboBox<String>(choicesLayers);

		JPanel statChoicesPanel = new JPanel();
		statChoicesPanel.setLayout(new GridLayout( 4, 2, 0, 10));
		statChoicesPanel.add(new JLabel("Layers"));
		statChoicesPanel.add( comboLayers);

		int result = JOptionPane.showConfirmDialog(null, statChoicesPanel, "Output Options", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(result != JOptionPane.OK_OPTION) {
			return;
		}

		boolean writeAllLAyers  = ((String) comboLayers.getSelectedItem()).equals( ALL_STRING);

		String folder;
		if  ( this.rhizoMain.getStorageFolder() == null )
			folder = System.getProperty("user.home");
		else 
			folder = this.rhizoMain.getStorageFolder();

		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter( "RSML file", "rsml"); 
		fileChooser.setFileFilter(filter);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if ( writeAllLAyers ) {
            // TODO: ask user if generated filenames are ok? at least if existing files are overridden?
			// TODO: potentially create ICAP conforming filenames
			fileChooser.setDialogTitle("File(s) to write RSML to");
			fileChooser.setSelectedFile(new File( folder + projectName + ".rsml"));
			int returnVal = fileChooser.showOpenDialog(null);

			if (returnVal != JFileChooser.APPROVE_OPTION)
				return; // user cancelled dialog
			String name = fileChooser.getSelectedFile().toString().replaceFirst(".rsml\\z", "");;
			for ( Layer layer : rhizoMain.getProject().getRootLayerSet().getLayers()) {
				writeLayer( new File( name + "-" + String.valueOf( RhizoUtils.getTimepointForLayer( layer)) + ".rsml"), layer, this.rhizoMain.getLayerInfo( layer));
			}

		} else {
			// Select and open output file
			Layer layer = Display.getFront().getLayer();

			fileChooser.setDialogTitle("File to write RSML to");
			fileChooser.setSelectedFile(new File( folder + projectName + "-" + String.valueOf( RhizoUtils.getTimepointForLayer( layer)) + ".rsml"));
			int returnVal = fileChooser.showOpenDialog(null);

			if (returnVal != JFileChooser.APPROVE_OPTION)
				return; // user cancelled dialog

			// TODO: ask if file should be overridden, if is exists??
			writeLayer( fileChooser.getSelectedFile(), layer, this.rhizoMain.getLayerInfo( layer));
		}	
	}
    
	/** Write the <code>layer</code> as an RSML to <code>saveFile</code>.
	 * 
	 * @param saveFile
	 * @param layer
	 * @param rhizoLayerInfo 
	 */
	private void writeLayer(File saveFile, Layer layer, RhizoLayerInfo rhizoLayerInfo) {
		Rsml rsml = createRSML( layer, rhizoLayerInfo);
		if ( rsml == null ) {
			Utils.showMessage( "cannot create RSML for layer " + RhizoUtils.getTimepointForLayer( layer));
			return;
		}

		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Rsml.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal( rsml, saveFile);

		} catch (JAXBException e) {
			Utils.showMessage( "cannot write RSML to  " + saveFile.getPath());
			e.printStackTrace();
		}

		Utils.log("Saved layer " + String.valueOf( RhizoUtils.getTimepointForLayer( layer)) + " to RSML file  - " + saveFile.getAbsolutePath());
	}

	/** Create a rsml xml data structure for the current layer.
     * 
     * @param layer 
	 * @param rhizoLayerInfo 
     * @return the rsml data structure or null, if no rootstacks are found
     */
    private Rsml createRSML(Layer layer, RhizoLayerInfo rhizoLayerInfo) {
    	Project project = Display.getFront().getProject();
    	
		// collect all treelines to write 
		List<Treeline> allTreelinesInLayer;
		allTreelinesInLayer = RhizoUtils.getTreelinesBelowRootstacks( project, layer);

		if ( allTreelinesInLayer == null) {
			Utils.showMessage( "rhizoTrak", "WARNING: no rootstacks found, nothing to write");
			return null;
		}

		// create hash map form treeline to connector
		// if a treeline is contained in more than one connector an arbitray one is used
		HashMap<Treeline,Connector> treelineConnectorMap = new HashMap<Treeline,Connector>();
		
		List<Connector> allConnectors = RhizoUtils.getConnectorsBelowRootstacks(project);
		if ( allConnectors == null)
			allConnectors = new LinkedList<Connector> ();
		
		for ( Treeline tl : allTreelinesInLayer) {
			for ( Connector conn : allConnectors) {
				if ( conn.getConTreelines().contains( tl) ) {
					treelineConnectorMap.put( tl,  conn);
				}
			}
		}
		
    	// create rsml
    	Rsml rsml = new Rsml();
    	
    	try {
    		// --- meta data
    		Rsml.Metadata metadata;
    		if (rhizoLayerInfo == null || rhizoLayerInfo.getRsml() == null || rhizoLayerInfo.getRsml().getMetadata() == null) {
    			metadata = createMetatdata( layer, rhizoLayerInfo);
    		} else {
    			metadata = createMetatdata( layer, rhizoLayerInfo);
    		}
    		rsml.setMetadata( metadata);

    		// --- the scene
    		Rsml.Scene scene = new Scene();

    		// properties: status label mappings
    		PropertyListType pList = new PropertyListType();
    		for ( int i = 0 ; i < this.rhizoMain.getProjectConfig().sizeStatusLabelMapping() ; i++) {
    	    	pList.getAny().add( createElementForXJAXBObject(
    	    			createIntegerStringPair( i, this.rhizoMain.getProjectConfig().getStatusLabel( i).getName())));
    		}
    		// add internal status labels
    		for ( int i : this.rhizoMain.getProjectConfig().getFixedStatusLabelInt()) {
    			pList.getAny().add( createElementForXJAXBObject(
    					createIntegerStringPair( i, this.rhizoMain.getProjectConfig().getStatusLabel( i).getName())));
    		}
 		
			scene.setProperties( pList);

			// now create the roots
    		for ( Treeline tl : allTreelinesInLayer ) {
    			Plant plant = createPlantForTreeline( tl, treelineConnectorMap.get( tl));
    			if ( plant != null ) {
    				scene.getPlant().add( plant);
    			}
    		}

    		rsml.setScene( scene);

    	} catch (Exception e) {
			Utils.showMessage( "rhizoTrak", "Internal error when creating the rsml representation for layer " + 
					String.valueOf( RhizoUtils.getTimepointForLayer( layer)));
    		//TODO should we keep the stack trace on production??
    		e.printStackTrace();
    	}
    	return rsml;
    }
   
    
    /** Create a RSML metadata object with rhizoTrak specific information/content
     * 
     * @param layer
     * @param rhizoLayerInfo 
     * @return
     */
    private Rsml.Metadata createMetatdata( Layer layer, RhizoLayerInfo rhizoLayerInfo) {
    	Rsml.Metadata oldMetadata = null;
    	if ( rhizoLayerInfo != null && rhizoLayerInfo.getRsml() != null)
    		oldMetadata = rhizoLayerInfo.getRsml().getMetadata();
    		
    	Rsml.Metadata metadata = new Metadata();
    	metadata.setVersion( RSML_VERSION);
    	
    	// -----------------------------------------------------------------------------
    	// first everything which is always set independent of a previous meta data object
    	final GregorianCalendar now = new GregorianCalendar();
    	try {
    		metadata.setLastModified(  DatatypeFactory.newInstance().newXMLGregorianCalendar(now));
    	} catch (DatatypeConfigurationException e1) {
    		Utils.showMessage( "write RSML: can not generate time for rsml");
    	}

    	metadata.setSoftware( SOFTWARE_NAME);
    	metadata.setUser( System.getProperty("user.name"));

    	// file key 
    	if ( metadata.getFileKey() == null) {
    		metadata.setFileKey( projectName + "_" + BigInteger.valueOf( (long)layer.getZ() + 1));
    	}

    	// time sequence
    	TimeSequence timeSequence = new TimeSequence();
    	timeSequence.setLabel( projectName);
    	timeSequence.setIndex(  BigInteger.valueOf( (long)layer.getZ() + 1));
    	timeSequence.setUnified( true);
    	metadata.setTimeSequence( timeSequence);
       	
    	// -----------------------------------------------------------------------------
    	// meta data related to the image
    	Image imageMetaData = new Image();
    	if ( oldMetadata == null || oldMetadata.getImage() == null ) { 
    		if ( layer.getPatches( false).size() > 0 ) {
    			// get the first patch of the layer
    			Path imagePath = Paths.get( layer.getPatches( false).get(0).getImageFilePath());

    			Path storageFolderPath = Paths.get( this.rhizoMain.getStorageFolder());
    			Path imageDirectory = imagePath.getParent();

    			System.out.println( "sfp:" + storageFolderPath + " id: " + imageDirectory + " ip:" + imagePath);
    			if ( imageDirectory.equals( storageFolderPath) ) {
    				imageMetaData.setName( imagePath.getFileName().toString());
    			} else if ( imagePath.toString().startsWith(storageFolderPath.toString()) ) {
    				Path relativPath = storageFolderPath.relativize( imagePath);
    				imageMetaData.setName( relativPath.toString());
    			} else {
    				imageMetaData.setName( imagePath.toString());
    			}

    			// set sha256 code
    			// TODO why is rhizoLayerInfo not always define, where to get shacode from
    			if ( rhizoLayerInfo != null )
    				imageMetaData.setSha256( rhizoLayerInfo.getImageHash());

    			// TOOD is there a chance to get hold of capture time and set it??

    			// TODO: set unit from calibration information, if available
            	metadata.setUnit(  "pixel");
            	metadata.setResolution( new BigDecimal(1));

    		} else {
    			// no image in this layer
    			imageMetaData.setName( "");
    			imageMetaData.setSha256( "");
            	metadata.setUnit(  "pixel");
            	metadata.setResolution( new BigDecimal(1));
    		}
    		
    	} else {
    		imageMetaData.setName( oldMetadata.getImage().getName());
    		imageMetaData.setSha256( oldMetadata.getImage().getSha256());
    		imageMetaData.setCaptured( oldMetadata.getImage().getCaptured());
    		
    		metadata.setUnit( oldMetadata.getUnit());
    		metadata.setResolution( oldMetadata.getResolution());
    	}
    	metadata.setImage( imageMetaData);

    	// -----------------------------------------------------------------------------
    	// property definitions
    	PropertyDefinitions pDefs = new PropertyDefinitions();
    	PropertyDefinition pDef;
    	
    	// copy old definitions if any, but skip status label mapping and parent node
    	if ( oldMetadata != null && oldMetadata.getPropertyDefinitions() != null &&
    			oldMetadata.getPropertyDefinitions().getPropertyDefinition() != null) {
    		for( PropertyDefinition oldPdef : oldMetadata.getPropertyDefinitions().getPropertyDefinition()) {
    			if ( ! oldPdef.getLabel().equals( PROPERTY_NAME_PARENTNODE) &&
    					! oldPdef.getLabel().equals( PROPERTY_NAME_STATUSLABELMAPPING) ) {
    				pDef = new PropertyDefinition();
    				pDef.setLabel( oldPdef.getLabel());
    				pDef.setType( oldPdef.getType());
    				pDef.setUnit( oldPdef.getUnit());
    				pDef.setDefault( oldPdef.getDefault());
    			}
    		}
    	}

    	// StatusLabelMapping
    	pDef = new PropertyDefinition();
    	pDef.setLabel( PROPERTY_NAME_STATUSLABELMAPPING);
    	pDef.setType( "Integer-String-Pair");
    	pDefs.getPropertyDefinition().add( pDef); 

    	// ParentNode
    	pDef = new PropertyDefinition();
    	pDef.setLabel( PROPERTY_NAME_PARENTNODE);
    	pDef.setType( "integer");
    	pDefs.getPropertyDefinition().add( pDef); 

    	metadata.setPropertyDefinitions( pDefs);
    	return metadata;
    }


	/** create one rsml plant with one root for one treeline
	 * 
	 * @param tl
	 * @param connector the tl is member of, null if treeline is not member of any treeline
	 * @return the rsml plant or null, if the treeline has no root node
	 */
	private Plant createPlantForTreeline(Treeline tl, Connector connector) {
		if ( tl.getRoot() != null ) {
		Plant plant = new Plant();
		
		Node<Float> rootNode = tl.getRoot();
		RootType root = createRSMLRootFromNode( tl, connector, rootNode, null, -1, -1);

		plant.getRoot().add( root);
		return plant;
		} else {
			return null;
		}
	}

	/** Create a rsml representation for the subtree of a treeline <code>tl</code> .
	 * If the subtree contains branches this representation comprises mulitple rsml roots, i.e. polylines.
	 * <br>
	 * If the parent node of  <code>node</code> is null, a representation for the  complete treeline <code>tl</code> is created.
	 * Otherwise  the subtree starting with the segment defined by the parent node 
	 * and <code>node</code> is considered as the subtree. I.e. the parent node may included if this
	 * segment has not the status VIRTUAL or VIRTUAL_RSML.
	 * 
	 * @param tl
	 * @param connector connector the tl is member of, null if treeline is not member of any treeline
	 * @param node
	 * @param parentRSMLRoot the parent root of the rsml root to be created, null if the top level root 
	 * @param siblingIndex gives the position of the rsml root to be created amongst its sibling polyline, starting with 1,
	 *                    ignored for the top level rsml root for a treeline
	 * @param parentNodeIndex index of the parent node in the rsml root above this subtree to be created, 
	 *                    ignored for the top level rsml root for a treeline
	 * @return
	 */
	private RootType createRSMLRootFromNode( Treeline tl, Connector connector, Node<Float> node, RootType parentRSMLRoot, 
			int siblingIndex, int parentNodeIndex) {
		Node<Float> parentNode = node.getParent();
		
		RootType root = new RootType();
		
		Polyline polyline = new Polyline();
				
		Function diameters = new Function();
		diameters.setName( FUNCTION_NAME_DIAMETER);
		diameters.setDomain( "polyline");
		
		Function statusLabels = new Function();
		statusLabels.setName( FUNCTION_NAME_STATUSLABEL);
		statusLabels.setDomain( "polyline");		
		
		// counting the nodes in this rsml root, i.e. polyline, starting with 1
		int nodeCount = 0; // no node yet

		// RSML R reader requires a property list, even if empty
		PropertyListType props = new PropertyListType();

		if ( parentNode == null ) {
			// top level root
			root.setId( getRsmlIdForTreeline( tl, connector));
		} else {
			// non top level root
			root.setId( parentRSMLRoot.getId() + "-" + String.valueOf( siblingIndex));

			byte statuslabel = node.getConfidence();
			
			if ( statuslabel != RhizoProjectConfig.STATUS_VIRTUAL && statuslabel != RhizoProjectConfig.STATUS_VIRTUAL_RSML) {
				// the branch is not connected by a virtual segment, add the parent node
				addNode( parentNode, RhizoProjectConfig.STATUS_UNDEFINED, tl, polyline, diameters, statusLabels);
				nodeCount++;
			}

			if ( statuslabel != RhizoProjectConfig.STATUS_VIRTUAL_RSML) {
				// add parent node property
				// TODO check for start of parent node index (0 or 1)
				ParentNode pn = new ParentNode();
				pn.setValue(parentNodeIndex);
				props.getAny().add( createElementForXJAXBObject( pn));
			}
		}
		
		addNode( node, node.getConfidence(), tl, polyline, diameters, statusLabels);
		nodeCount++;

		root.setProperties( props);
				
		// index for all (direct) branching subtrees
		int brachingSubtreeIndex = 1;
		
		ArrayList<Node<Float>> children = reorderChildNodes( node.getChildrenNodes());
		while ( children.size() > 0 ) {
			Iterator<Node<Float>> itr = children.iterator();
			// continue the polyline with the first child
			Node<Float> child = itr.next();
			
			addNode( child, child.getConfidence(), tl, polyline, diameters, statusLabels);
			nodeCount++;

			while ( itr.hasNext()) {
				child = itr.next();
				// create branching root
				// beware: we already added a node after the branching node
				if ( parentNodeIndexStartsWithOne )
					root.getRoot().add( createRSMLRootFromNode( tl, connector, child, root, brachingSubtreeIndex, nodeCount-1));
				else 
					root.getRoot().add( createRSMLRootFromNode( tl, connector, child, root, brachingSubtreeIndex, nodeCount-2));

				brachingSubtreeIndex++;
			}
			
			node = children.get(0);
			children = node.getChildrenNodes();
		}
		
		Functions functions = new Functions();
		functions.getFunction().add(  diameters);
		functions.getFunction().add( statusLabels);
		root.setFunctions( functions);
	
		Geometry geometry = new Geometry();
		geometry.setPolyline( polyline);
		root.setGeometry( geometry);
		
		return root;
	}		
	
	/** return a string id which represent the treeline <code>tl</code> in a RSML file
	 * @param tl
	 * @param connector
	 * @return
	 */
	private String getRsmlIdForTreeline(Treeline tl, Connector connector) {
		if ( connector == null)
			return String.valueOf( "T-" + tl.getId());
		else
			return String.valueOf( "C-" + connector.getId());	
	}

	/** Reorder the list of child nodes. The first node in the list return will continue the rsml root/poly line.
	 * The remaining nodes will be branching, i.e. the second or first node of a rsml root/polyline on the
	 * next level
	 * 
	 * @param childNodes
	 * @return
	 */
	private ArrayList<Node<Float>> reorderChildNodes(ArrayList<Node<Float>> childNodes) {
		// to first child connected by segment with a user define status label will be the continuing node,
		// (if any)
		// the rest are branching nodes in untouched order
		
		return childNodes;
	}

	/** Add a node to the RSML representation of the treeline, i.e., the corresponding polyline.
	 * NOTE: the status label is given as argument and not taken from the node to facilitate special coding of the
	 * base node of a rsml root
	 * 
	 * @param node
	 * @param statusLabelInteger
	 * @param tl
	 * @param polyline
	 * @param diameters
	 * @param statusLabels
	 */
	private void addNode(Node<Float> node, int statusLabelInteger, Treeline tl, Polyline polyline, Function diameters,
			Function statusLabels) {
		
		Point2D p = RhizoUtils.getAbsoluteTreelineCoordinates( new Point2D.Float( node.getX(), node.getY()), tl);

		PointType pt = new PointType();
		pt.setX( new BigDecimal( p.getX()));
		pt.setY( new BigDecimal( p.getY()));
		polyline.getPoint().add( pt);
		
		Sample diameter = new Sample();
		diameter.setValue( new BigDecimal( 2*((RadiusNode)node).getData()));
		diameters.getSample().add( diameter);

		Sample sl = new Sample();
		sl.setValue( new BigDecimal( statusLabelInteger));
		statusLabels.getSample().add(sl);
	}

	/** Create a <code>org.w3c.dom.Element</code> holding a Object created by JAXB 
     * 
     * @param jaxbObject
     * @return
     */
    private Element createElementForXJAXBObject( Object jaxbObject) {
    	
    	Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			Utils.showMessage( "write RSML: internal error, cannot marshal object of type " + jaxbObject.getClass().getCanonicalName());
			e.printStackTrace();
		}
    	JAXB.marshal( jaxbObject, new DOMResult(document));
    	Element element = document.getDocumentElement();

    	return element;
    }
    
	/** Create a  <code>IntegerStringPairType</code> 
     * representing the status label mapping <code>idx</code> to <code>name</code>
     * 
     * @param idx
     * @param name
     * @return
     */
    private IntegerStringPairType createIntegerStringPair ( int idx, String name) {
    	IntegerStringPairType isp = new IntegerStringPairType();
    	isp.setInt( idx);
    	isp.setValue( name);
    	
    	return isp;
    }
    
	/**
	 * Reads one or more RSML file into the rhizoTrak project.
	 * 
	 * @author Posch
	 */
	public void readRSML(List<File> rsmlFiles, Layer firstLayer) {
		//TODO base directory to which filename in the RSML files are relative
		//TODO firstLayer == null means that "append..." was selected
		
		List<Rsml> rsmls = new LinkedList<Rsml>();
	
		// parse all RSML files
		for ( File rsmlFile : rsmlFiles ) {
			try {
				JAXBContext context = JAXBContext.newInstance( Rsml.class);
				Unmarshaller um = context.createUnmarshaller();
				rsmls.add( (Rsml) um.unmarshal( rsmlFile));
			} catch (Exception e) {
				Utils.showMessage( "cannot read RSML from  " + rsmlFile.getName());
			}
		}
		
		List<Layer> availableLayers = getAvailableLayers(firstLayer);

		// check if it is possible to import (potentially with user feedback)
		// e.g. status label mappings, inconsistent sha256 codes when importing into non empty layer
		// if in a layer there are already annotations (i.e. treelines) ask the user if these should be deleted
		
		// create new empty layers as needed
		// TODO check if there are images in the base directory that correspond to the image names/sha codes in the rsml
		// if found we can borrow methods from RhizoImages to import a layer and an image
		// for now only add empty layers
		while(rsmlFiles.size() > availableLayers.size())
		{
			LayerSet layerSet = rhizoMain.getProject().getRootLayerSet();
			
			Layer lastLayer = availableLayers.size() > 0 ? availableLayers.get(availableLayers.size() - 1) : layerSet.getLayers().get(layerSet.size() - 1);
			double z = lastLayer.getZ();
			
			Layer newLayer = new Layer(lastLayer.getParent().getProject(), z + 1, 1, layerSet);
			layerSet.add(newLayer);
			newLayer.recreateBuckets();
			newLayer.updateLayerTree();
			
			availableLayers.add(newLayer);
		}

		// TODO list with imageplus objects for empty layers
		
		// collect for each ID of a toplevel root/polyline the treeline object created for this ID
		HashMap<String,List<Treeline>> topLevelIdTreelineListMap = new HashMap<String,List<Treeline>>();
		
		// loop over the RSML files
		for ( int i = 0 ; i < rsmls.size() ; i++ ) {
			Rsml rsml = rsmls.get( i);
			Layer layer = availableLayers.get( i);
			importRsmlToLayer( rsml, layer, topLevelIdTreelineListMap);
		}
	}
	
	/**
	 * Returns all layers in the project starting from <code>firstLayer</code>. LayerSet class ensures that the list of
	 * layers is always ordered by Z value.
	 * @param firstLayer - The first layer to import into. If <code>null==firstLayer</code> an empty list is returned.
	 * @return
	 */
	private List<Layer> getAvailableLayers(Layer firstLayer)
	{
		List<Layer> layerList = new ArrayList<Layer>();
		
		if(null == firstLayer) return layerList;

		LayerSet layerSet = rhizoMain.getProject().getRootLayerSet();
		List<Layer> allLayers = layerSet.getLayers();
		
		int index = allLayers.indexOf(firstLayer);
		for(int i = index; i < allLayers.size(); i++)
		{
			layerList.add(allLayers.get(i));
		}
		
		return layerList;
	}
		
	/** import the RSML file  <code>file</code> into rhizoTrak for <code>layer</code>.
	 * @param file
	 * @param layer
	 * @param topLevelIdTreelineListMap 
	 */
	private void importRsmlToLayer( Rsml rsml , Layer layer, HashMap<String, List<Treeline>> topLevelIdTreelineListMap) {
		// read the rsml file

		try {
			RhizoLayerInfo layerInfo = this.rhizoMain.getLayerInfo(layer);
			if ( layerInfo == null ) {
				layerInfo = new RhizoLayerInfo( layer, rsml);
				this.rhizoMain.setLayerInfo( layer, layerInfo);
			} else {
				layerInfo.setRsml( rsml);
			}

			// TODO read image if layer is empty 
			// TODO we have to figure out what to do about the meta data of the RSML at this point
			
			// import the root
			for ( Scene.Plant plant : rsml.getScene().getPlant() ) {
				for ( RootType root : plant.getRoot() ) {
					layerInfo.mapRoot( plant, root);

					Treeline tl = createTreelineForRoot( root, layer);
					layerInfo.mapTreeline( tl , root);
					List<Treeline> tlList = topLevelIdTreelineListMap.get( root.getId());
					if ( tlList == null) {
						tlList = new LinkedList<Treeline>();
						topLevelIdTreelineListMap.put( root.getId(), tlList);
					}
					
					tlList.add(tl);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Create a treeline for the toplevel <code>root</code>
	 * 
	 * @param root
	 * @param layer project layer in which to insert the treeline
	 * @return
	 */
	private Treeline createTreelineForRoot(RootType root, Layer layer) {
		Project project = this.rhizoMain.getProject();
		ProjectTree projectTree = project.getProjectTree();

		ProjectThing possibleParent = RhizoAddons.findParentAllowing("treeline", project);
		ProjectThing treelineThing = possibleParent.createChild("treeline");

		DefaultMutableTreeNode parentNode = DNDTree.findNode(possibleParent, projectTree);
		DefaultMutableTreeNode projecttreeNode = new DefaultMutableTreeNode(treelineThing);
		((DefaultTreeModel) projectTree.getModel()).insertNodeInto(projecttreeNode, parentNode, parentNode.getChildCount());
		
		Treeline treeline = (Treeline) treelineThing.getObject();
		treeline.setLayer( layer);
		fillTreelineFromRoot( root, treeline, null, layer);
		return treeline;
	}
	
	/** This adds one root/polyline to the treeline
	 * 
	 * @param root to be added
	 * @param treeline to add the root to
	 * @param parentTreelineNodes hashmap of indices in the root as represented in RSML to the corresponding RadiusNode
	 * @param layer
	 */
	private void fillTreelineFromRoot( RootType root, Treeline treeline, HashMap<Integer, RadiusNode> parentTreelineNodes, Layer layer) {
		Geometry geometry = root.getGeometry();
		Function diameters = getFunctionByName( root, FUNCTION_NAME_DIAMETER);
		Function statuslabels = getFunctionByName( root, FUNCTION_NAME_STATUSLABEL);
				
		Iterator<PointType> pointItr = geometry.getPolyline().getPoint().iterator();
		if ( ! pointItr.hasNext()) {
			// no nodes at all
			return;
		}
		
		// create the rhizoTrak nodes and segments for the polyline/root
		// index of the next node to insert into the treeline
		int pointIndex = 0;
		
		// hash map to map the index of a point in the RSML polyline to  treeline nodes created for this RSML point
		// coordinates are as in the RSML polyline
		// indices start at 0
		HashMap<Integer,RadiusNode> treelineNodes = new HashMap<Integer,RadiusNode>( geometry.getPolyline().getPoint().size());
		
		// this radius node is the last one when we will iterate the nodes of this treeline
		RadiusNode previousnode;
		
		PointType firstPoint = pointItr.next();

		// create first node and connect to parent node within the treeline
		
		if ( parentTreelineNodes == null) {
			// top level root/polyline
			previousnode = createRhizoTrakNode( firstPoint, getRadiusFromRsml( diameters, pointIndex), treeline, layer);
			if ( debug )
				System.out.println("First rt node " + RhizoUtils.getAbsoluteTreelineCoordinates( previousnode.getX(), previousnode.getY(), treeline));
			treeline.addNode( null, previousnode, (byte) 0);
			treelineNodes.put( pointIndex, previousnode);
			treeline.setRoot( previousnode);
			
			pointIndex++;
		} else {
			// non toplevel root/polyline
			
			// find the parent node in the treeline from which the current root/polyline gets connected into the treeline
			int parentNodeIndex = getParentNodeIndex( root);
			
			RadiusNode parentNode = null;
			if ( parentNodeIndex != (-1) ) {
				// TODO the RSML homepage (thesaurus) seems to indicate that the node indices in RSML files
				// start with 1
				// if this is correct, we need to lookup  parentNodeIndex-1, as pointIndex starts with 0
				// TODO check for start of parent node index (0 or 1)
				if ( parentNodeIndexStartsWithOne )
					parentNode = parentTreelineNodes.get( parentNodeIndex-1);
				else 
					parentNode = parentTreelineNodes.get( parentNodeIndex);
				

				if ( parentNode == null ) {
					for ( int i : parentTreelineNodes.keySet()) {
						if ( debug ) System.out.println( "          " + i + "  -> " + parentTreelineNodes.get(i));
					}
				} else {
				if ( debug ) 
					System.out.println("  parent rt node " +  
						RhizoUtils.getAbsoluteTreelineCoordinates( parentNode.getX(), parentNode.getY(), treeline));
				}
			}
			
			boolean foundParentNode;
			Double minDist;
			// if no parent-node is specified or parent-node as read from RSML is broken, i.e. invalid index
			if ( parentNode == null ) {
				foundParentNode = false;
				// nearest node of the parent root/polyline
				minDist = Double.MAX_VALUE;
				for ( RadiusNode node : parentTreelineNodes.values() ) {
					Double dist = distance( (float)firstPoint.getX().doubleValue(), (float)firstPoint.getY().doubleValue(), node, treeline);
					if ( dist < minDist) {
						parentNode = node;
						minDist = dist;
					}
				}
			} else {
				foundParentNode = true;
				minDist = distance( (float)firstPoint.getX().doubleValue(), (float)firstPoint.getY().doubleValue(), parentNode, treeline);
			}
			
			// create the first treeline node linking it into the treeline
			byte statuslabel = getStatuslabelFromRsml( statuslabels, pointIndex);
 
			if ( isStatuslabelFromRsmlDefined(statuslabels, pointIndex) && statuslabel == RhizoProjectConfig.STATUS_UNDEFINED &&
					minDist < EPSILON) {
				// skip the first node of this root/polyline if there are mode nodes

				if ( pointItr.hasNext()) {
					pointIndex++;
					firstPoint = pointItr.next();
					statuslabel = getStatuslabelFromRsml( statuslabels, pointIndex);	
				}

			} else {
				if ( ! foundParentNode ) {
					statuslabel = RhizoProjectConfig.STATUS_VIRTUAL_RSML;
				} else {
					statuslabel = RhizoProjectConfig.STATUS_VIRTUAL;
				}
			}
			
			previousnode = createRhizoTrakNode( firstPoint, getRadiusFromRsml( diameters, pointIndex), treeline, layer);
			if ( debug )
				System.out.println("First rt node " + RhizoUtils.getAbsoluteTreelineCoordinates( previousnode.getX(), previousnode.getY(), treeline));
			treeline.addNode( parentNode, previousnode, statuslabel);
			treelineNodes.put( pointIndex, previousnode);
			
			pointIndex++;
		}
		
		// loop over the remaining points
		while ( pointItr.hasNext()) {
			PointType point = pointItr.next();
			RadiusNode node = createRhizoTrakNode( point, getRadiusFromRsml( diameters, pointIndex), treeline, layer);
			if ( debug ) System.out.println("     rt node " +  RhizoUtils.getAbsoluteTreelineCoordinates( node.getX(), node.getY(), treeline));

			byte statuslabel = getStatuslabelFromRsml( statuslabels, pointIndex);
			
			// TODO do we need to check for VIRTUAL and VIRTUAL_RSML status labels ?????
			treeline.addNode(previousnode, node, statuslabel);
			treelineNodes.put( pointIndex, node);
			previousnode = node;
			pointIndex++;
		}
		
		// recursively add the child roots
		for ( RootType childRoot : root.getRoot() ) {
			fillTreelineFromRoot( childRoot, treeline, treelineNodes, layer);
		}
	}

	/** Distance of <code>(x,y)</code> in absolute coordinates to <code>node</code> in <code>treeline</code>
	 * @param x
	 * @param y
	 * @param node
	 * @return
	 */
	private Double distance(float x, float y, RadiusNode node, Treeline treeline) {
		Point2D.Float pt = RhizoUtils.getAbsoluteTreelineCoordinates( node.getX(), node.getY(), treeline);
		return pt.distance( x, y);
	}

	/** Parse the parent-node or parentNode attribute from the root
	 * @param root
	 * @return parent node index (starting with 1 by RSML convetions) or <code>-1</code> if not found
	 */
	private int getParentNodeIndex(RootType root) {
		int parentNodeIndex = -1;
		
//		// parse from XML element
//		if ( root.getProperties() != null && root.getProperties().getAny() != null ) {
//			for ( Element e : root.getProperties().getAny()) {
//				if ( e.getNodeName().equals( PROPERTY_NAME_PARENTNODE)) {
//					for ( int index = 0 ; index <e.getChildNodes().getLength(); index++ ) {
//						if ( e.getChildNodes().item(index).getNodeName().equals("value")) {
//							parentNodeIndex = Integer.valueOf(e.getChildNodes().item(index).getTextContent());
//						}
//					}
//				}
//			}
//		}
		
		if ( root.getProperties() != null && root.getProperties().getAny() != null ) {
			for ( Element e : root.getProperties().getAny()) {
				if ( e.getNodeName().equals( PROPERTY_NAME_PARENTNODE) || e.getNodeName().equals( "parent-node") ) {
					if ( debug ) System.out.println( "  parent-node index " + e.getAttribute( "value"));
					parentNodeIndex =  Integer.valueOf( e.getAttribute( "value"));
				} 
			}
		}
		return parentNodeIndex;
	}

	/** create a RadiusNode for the rsml node in the given layer
	 * 
	 * @param rsmlNode
	 * @param radius
	 * @param layer
	 * @return
	 */
	private RadiusNode createRhizoTrakNode(PointType rsmlNode, float radius, Treeline tl, Layer layer) {
		Point2D pt = null;
		try {
			pt = RhizoUtils.getRelativeTreelineCoordinates( new Point2D.Float( rsmlNode.getX().floatValue(), rsmlNode.getY().floatValue()), tl);
		} catch (NoninvertibleTransformException e) {
			// this should not happen
			// TODO sensible error message ??
			pt = new Point2D.Float( 0.0f, 0.0f);
			e.printStackTrace();
		}
		return new RadiusNode( (float)pt.getX(), (float)pt.getY(), layer, radius);
	}

	/**Get the status label for index <code>pointIndex</code> from the function <code>statuslabels</code>
	 * or <code>default_statuslabel</code> if function <code>statuslabels</code> is null or index out of range
	 * @param statuslabels
	 * @param pointIndex
	 * @return
	 */
	private byte getStatuslabelFromRsml(Function statuslabels, int pointIndex) {
		if ( statuslabels != null && statuslabels.getSample().size() >= pointIndex && pointIndex >= 0) {
			return statuslabels.getSample().get( pointIndex).getValue().byteValue();
		} else {
			return default_statuslabel;
		}
	}
	
	private boolean isStatuslabelFromRsmlDefined( Function statuslabels, int pointIndex) {
		if ( statuslabels != null && statuslabels.getSample().size() >= pointIndex && pointIndex >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/** Get the radius for index <code>pointIndex</code> from the function <code>diameters</code>
	 * or null if  <code>diameters</code> is null or index out of range
	 * @param diameters
	 * @param pointIndex
	 * @return
	 */
	private float getRadiusFromRsml(Function diameters, int pointIndex) {
		if ( diameters != null && diameters.getSample().size() >= pointIndex && pointIndex >= 0) {
			return 0.5f * diameters.getSample().get( pointIndex).getValue().floatValue();
		} else {
			return 0.0f;
		}
	}

	/**
	 * @param root
	 * @param name
	 * 
	 * @return the first function with name <code>name</code>, null if none exists
	 */
	private Function getFunctionByName(RootType root, String name) {
		if ( root.getFunctions() == null || root.getFunctions().getFunction() == null ) {
			return null;
		}
		
		ListIterator<Function> itr = root.getFunctions().getFunction().listIterator();
		
		while ( itr.hasNext() ) {
			Function fct = itr.next();
			if ( fct.getName().equals(name) ) {
				return fct;
			}
		}
		return null;
	}

	/**
	 * Opens the RSML loader window
	 * @author Tino
	 */
	public void openRSMLLoader() 
	{
		// create a new window on every button press so that the layers in the combobox get updated
		if(null != rsmlLoaderFrame) rsmlLoaderFrame.dispose();
		
		rsmlLoaderFrame = new JFrame(null == rsmlBaseDir ? "RSML Loader" : "RSML Loader - " + rsmlBaseDir.getAbsolutePath());
		rsmlLoaderFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel temp = new RSMLLoader(rhizoMain);
		rsmlLoaderFrame.add(temp);
		rsmlLoaderFrame.pack();
		rsmlLoaderFrame.setVisible(true);
	}
	
	public void setRSMLBaseDir(File dir)
	{
		if(!dir.isDirectory()) Utils.log("Can not set RSML base directory. File has been selected."); 
		else this.rsmlBaseDir = dir;
	}
	
	public File getRSMLBaseDir()
	{
		return rsmlBaseDir;
	}
	
	public JFrame getRSMLLoaderFrame()
	{
		return rsmlLoaderFrame;
	}
}
