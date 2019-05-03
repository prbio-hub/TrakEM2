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
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.display.addonGui.RSMLLoader;
import de.unihalle.informatik.rhizoTrak.persistence.Loader;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.StatusLabelMapping;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.PointType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.PropertyListType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType.Functions;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType.Functions.Function;
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
	//TODO  better always check if JAXB objects are null, even if they should not
	// e.g. geometry of a root - might happen with broken rsml files
	
	private boolean debug = false;
	
	private static final String RSML_VERSION = "1.0";

	private static final String SOFTWARE_NAME = "rhizoTrak";
	
	private static final String FUNCTION_NAME_DIAMETER = "diameter";
	private static final String FUNCTION_NAME_STATUSLABEL = "statusLabel";
	
	private static final String NAME_SAMPLE_ELEMENT = "sample";
	
	private static final String PROPERTY_NAME_PARENTNODE = "parent-node";
	private static final String PROPERTY_NAME_STATUSLABELMAPPING = "StatusLabelMapping";

	/**
	 * maximal distance allowed for parent nodes to deviate from precise location w
	 */
	private static final Double EPSILON = 0.01;

	private static byte default_statuslabel = 0;

	private RhizoMain rhizoMain;
	
	private File rsmlBaseDir = null;
	
	/**
	 * Set by user dialog
	 */
	private boolean deleteTreelinesBeforeImport = false;
	
	private static final String ONLY_STRING = "Current layer only";
	private static final String ROI_STRING = "ROI only";
	private static final String ALL_STRING = "All layers";
	private static final String UNIFIED_STRING = "Unified";
	private static final String NOTUNIFIED_STRING = "Not unified";

	public RhizoRSML(RhizoMain rhizoMain) {
		this.rhizoMain = rhizoMain;
	}

	private String projectName;
	
	private JDialog rsmlLoaderFrame;

	private boolean debugWrite = false;

	/**
	 *  Writes the current or all layers to a RSML file.
	 *  
	 *  @author Posch
	 */
	public void writeRSML() {
		projectName = rhizoMain.getXmlName().replaceFirst(".xml\\z", "");
		
		// query output options
		String[] choicesLayers = new String[]{ALL_STRING, ONLY_STRING, ROI_STRING};
		JComboBox<String> comboLayers = new JComboBox<String>(choicesLayers);
		
		String[] choicesUnified = {UNIFIED_STRING, NOTUNIFIED_STRING};
		JComboBox<String> comboUnified = new JComboBox<String>(choicesUnified);

		JPanel statChoicesPanel = new JPanel();
		statChoicesPanel.setLayout(new GridLayout( 4, 2, 0, 10));
		statChoicesPanel.add(new JLabel("Layers"));
		statChoicesPanel.add( comboLayers);
		statChoicesPanel.add(new JLabel("Unified "));
		statChoicesPanel.add( comboUnified);

		int result = JOptionPane.showConfirmDialog(null, statChoicesPanel, "Output Options", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(result != JOptionPane.OK_OPTION) {
			return;
		}

		boolean writeAllLAyers  = ((String) comboLayers.getSelectedItem()).equals( ALL_STRING);
		boolean writeROI  = ((String) comboLayers.getSelectedItem()).equals( ROI_STRING);

		boolean unified = ((String) comboUnified.getSelectedItem()).equals( UNIFIED_STRING);

		// query output files
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
			// TODO: potentially create ICAP conforming filenames
			fileChooser.setDialogTitle("File(s) to write RSML to");
			fileChooser.setSelectedFile(new File( folder + projectName + ".rsml"));
			int returnVal = fileChooser.showOpenDialog(null);

			if (returnVal != JFileChooser.APPROVE_OPTION)
				return; 
			
			String name = fileChooser.getSelectedFile().toString().replaceFirst(".rsml\\z", "");
			StringBuilder sb = new StringBuilder();
			ArrayList<File> files = new ArrayList<File>();
			for ( Layer layer : rhizoMain.getProject().getRootLayerSet().getLayers()) {
				File file = new File( name + "-" + String.valueOf( RhizoUtils.getTimepointForLayer( layer)) + ".rsml");
				files.add( file);
				sb.append( "Layer " + RhizoUtils.getTimepointForLayer(layer) + " to: " + file.getName());
				if ( file.exists() ) {
					sb.append( " (exists!)");
				}
				sb.append( "\n");
			}
			 
			result = JOptionPane.showConfirmDialog(null, new String( sb), 
					"write all layers to following RSML files", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if( result != JOptionPane.OK_OPTION) {
				return;
			}

			int i = 0;
			for ( Layer layer : rhizoMain.getProject().getRootLayerSet().getLayers()) {
				writeLayer( files.get( i), layer, this.rhizoMain.getLayerInfo( layer), unified);
				i++;
			}

		} else {
			Layer layer = null;
			de.unihalle.informatik.rhizoTrak.display.Polyline roi = null;
			if ( writeROI) {
				roi = rhizoMain.getRhizoRoi().getCurrentPolyline();
				layer = roi.getFirstLayer();
			} else {
				// Select and open output file
				layer = Display.getFront().getLayer();
			}

			fileChooser.setDialogTitle("File to write RSML to");
			fileChooser.setSelectedFile(new File( folder + projectName + "-" + String.valueOf( RhizoUtils.getTimepointForLayer( layer)) + ".rsml"));
			int returnVal = fileChooser.showOpenDialog(null);

			if (returnVal != JFileChooser.APPROVE_OPTION)
				return; 

			//  ask if file should be overridden, if is exists??
			File selectedFile = fileChooser.getSelectedFile();
			if ( selectedFile.exists() ) {
				result = JOptionPane.showConfirmDialog(null, "File " + selectedFile.getAbsolutePath() +
						" already exists, override?", 
						"write current layer to an RSML file", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if( result != JOptionPane.OK_OPTION) {
					return;
				}
			}
			
			// write the layer
			writeLayer( selectedFile, layer, this.rhizoMain.getLayerInfo( layer), unified, roi);
		}	
	}

	/** Write the <code>layer</code> as an RSML to <code>saveFile</code>.
	 *
	 * @param file
	 * @param layer
	 * @param layerInfo
	 * @param unified
	 */
	private void writeLayer(File file, Layer layer, RhizoLayerInfo layerInfo, boolean unified) {
		writeLayer( file, layer, layerInfo, unified, null);
	}

	/** Write the <code>layer</code> as an RSML to <code>saveFile</code>.
	 * 
	 * @param saveFile
	 * @param layer
	 * @param rhizoLayerInfo 
	 * @param unified
	 * @param  roi
	 */
	private void writeLayer(File saveFile, Layer layer, RhizoLayerInfo rhizoLayerInfo, boolean unified, de.unihalle.informatik.rhizoTrak.display.Polyline roi) {
		Rsml rsml = null;
		try {
			rsml = createRSML( layer, rhizoLayerInfo, unified, roi);
		} catch (InternalError ex) {
			Utils.showMessage( "cannot create RSML structure for layer " + String.valueOf( RhizoUtils.getTimepointForLayer( layer)));
		}
		
		if ( rsml == null ) {
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

	/** Create a RSML data structure for the current layer.
     * 
     * @param layer
	 * @param rhizoLayerInfo
	 * @param unified
	 * @param roi
     * @return the rsml data structure or null, if no rootstacks are found
     */
    private Rsml createRSML(Layer layer, RhizoLayerInfo rhizoLayerInfo, boolean unified, de.unihalle.informatik.rhizoTrak.display.Polyline roi) {
    	Project project = Display.getFront().getProject();
    	
		// collect all treelines to write 
		List<Treeline> allTreelinesInLayer;
		allTreelinesInLayer = RhizoUtils.getTreelinesBelowRootstacks( project, layer);

		if ( allTreelinesInLayer == null) {
			Utils.showMessage( "rhizoTrak", "WARNING: no rootstacks found in layer " + 
					String.valueOf( RhizoUtils.getTimepointForLayer( layer)) +
					", nothing to write");
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
    	
    	// --- meta data
    	Rsml.Metadata metadata;
    	if (rhizoLayerInfo == null || rhizoLayerInfo.getRsml() == null || rhizoLayerInfo.getRsml().getMetadata() == null) {
    		metadata = createMetatdata( layer, rhizoLayerInfo, unified, roi);
    	} else {
    		metadata = createMetatdata( layer, rhizoLayerInfo, unified, roi);
    	}
    	rsml.setMetadata( metadata);

    	// --- the scene
    	Rsml.Scene scene = new Scene();

    	// properties: status label mappings
    	PropertyListType pList = new PropertyListType();
    	for ( int i = 0 ; i < this.rhizoMain.getProjectConfig().sizeStatusLabelMapping() ; i++) {
    		pList.getAny().add( createElementForXJAXBObject(
    				createStatusLabelMapping( i, this.rhizoMain.getProjectConfig().getStatusLabel( i).getName())));
    	}
    	// add internal status labels
    	for ( int i : this.rhizoMain.getProjectConfig().getFixedStatusLabelInt()) {
    		pList.getAny().add( createElementForXJAXBObject(
    				createStatusLabelMapping( i, this.rhizoMain.getProjectConfig().getStatusLabel( i).getName())));
    	}

    	scene.setProperties( pList);

    	// now create the roots
    	for ( Treeline tl : allTreelinesInLayer ) {
			if ((roi == null) || overlaps(tl, roi)) {
				Plant plant = createPlantForTreeline(tl, rhizoLayerInfo, treelineConnectorMap.get(tl), unified, roi);
				if (plant != null && !scene.getPlant().contains(plant)) {
					scene.getPlant().add(plant);
				}
			}
		}
    	rsml.setScene( scene);

    	return rsml;
    }

	/** Check if the treeline <code>tl</code> overlaps the <code>roi</code>
	 *
	 * @param tl
	 * @param roi
	 * @return
	 */
	private boolean overlaps(Treeline tl, de.unihalle.informatik.rhizoTrak.display.Polyline roi) {
		return tl.intersects( roi);
	}


	/** Create a RSML metadata object with rhizoTrak specific information/content.
     * <p>
     * Note: The RSML JAXB Object in <code>rhizoLayerInfo</code> may be modified
     * 
     * @param layer
     * @param rhizoLayerInfo
     * @param unified
     * @param roi
	 * @return
     */
    private Rsml.Metadata createMetatdata(Layer layer, RhizoLayerInfo rhizoLayerInfo, boolean unified, de.unihalle.informatik.rhizoTrak.display.Polyline roi) {
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
    		throw new InternalError( "write RSML: can not generate time for rsml");
    	}

    	metadata.setSoftware( SOFTWARE_NAME);
    	metadata.setUser( System.getProperty("user.name"));

    	// file key 
    	if ( metadata.getFileKey() == null) {
    		metadata.setFileKey( projectName + "_" + BigInteger.valueOf( RhizoUtils.getTimepointForLayer( layer)));
    	}

    	// time sequence
    	TimeSequence timeSequence = new TimeSequence();
    	timeSequence.setLabel( projectName);
    	timeSequence.setIndex(  BigInteger.valueOf( RhizoUtils.getTimepointForLayer( layer)));
    	timeSequence.setUnified( unified);
    	metadata.setTimeSequence( timeSequence);
       	
    	// -----------------------------------------------------------------------------
    	// meta data related to the image
    	Image imageMetaData = new Image();
    	if ( oldMetadata == null || oldMetadata.getImage() == null || roi != null) {
    		if ( layer.getPatches( false).size() > 0 ) {
    			// get the first patch of the layer
    			Path imagePath = Paths.get( layer.getPatches( false).get(0).getImageFilePath());

    			Path storageFolderPath = Paths.get( this.rhizoMain.getStorageFolder());
    			Path imageDirectory = imagePath.getParent();

    			if ( imageDirectory.equals( storageFolderPath) ) {
    				imageMetaData.setName( imagePath.getFileName().toString());
    			} else if ( imagePath.toString().startsWith(storageFolderPath.toString()) ) {
    				Path relativPath = storageFolderPath.relativize( imagePath);
    				imageMetaData.setName( relativPath.toString());
    			} else {
    				imageMetaData.setName( imagePath.toString());
    			}

    			if ( roi != null ) {
    				// TODO crop the image of this layer and write to disk
					// set imagename in RSML accordingly
				}
    			// set sha256 code
    			// TODO why is rhizoLayerInfo not always defined, where to get shacode from
    			if ( rhizoLayerInfo != null )
    				imageMetaData.setSha256( rhizoLayerInfo.getImageHash());

    			// TOOD is there a chance to get hold of capture time and set it??

    			// set unit from calibration information, if available
    			if ( layer.getPatches( false).get(0) != null && layer.getPatches( false).get(0).getImagePlus() != null ) {
    				// Note: we use the calibration of x
    				metadata.setUnit(  layer.getPatches( false).get(0).getImagePlus().getCalibration().getXUnit());
    				metadata.setResolution( new BigDecimal( 1.0/layer.getPatches( false).get(0).getImagePlus().getCalibration().pixelWidth));
    			} else {
    				metadata.setUnit(  "pixel");
    				metadata.setResolution( new BigDecimal(1));
    			}

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
    					! oldPdef.getLabel().equals( "parentNode") ) {
    				pDefs.getPropertyDefinition().add( oldPdef);
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


	/** create one rsml plant with one root for one treeline.
	 * 
	 * @param tl
	 * @param rhizoLayerInfo
	 * @param connector the tl is member of, null if treeline is not member of any treeline
	 * @param unified
	 * @param roi
	 * @return the rsml plant or null, if the treeline has no root node
	 *
	 */
	private Plant createPlantForTreeline(Treeline tl, RhizoLayerInfo rhizoLayerInfo, Connector connector, boolean unified, de.unihalle.informatik.rhizoTrak.display.Polyline roi) {
		if ( tl.getRoot() != null ) {
			// create the JAXB root for the treeline
			Node<Float> rootNode = tl.getRoot();
			RootType root = createRSMLRootFromNode( tl, connector, rootNode, null, -1, -1, unified, roi);

			// check if this treeline was created from a RSML toplevel root
			if ( rhizoLayerInfo != null && rhizoLayerInfo.getRootForTreeline(tl) != null) {
				if ( debugWrite ) System.out.println( "createPlantForTreeline: found an old root");
				RootType oldRoot = rhizoLayerInfo.getRootForTreeline(tl);
				Plant plant = rhizoLayerInfo.getPlantForRoot(oldRoot);
				
				if ( rootEquals( root, oldRoot) ) {
					// geometry did not change: update diameter and status label in old JAXB root
					updateFunctions( root, oldRoot);
					if ( debugWrite ) System.out.println( "    unchanged geometry " + plant.getRoot().size() + " oldRoot " + oldRoot);
				} else {
					// geometry did change: use new JAXB root and copy everything possible from old one
					if ( debugWrite )  System.out.println( "    changed geometry " + plant.getRoot().size() + "  oldRoot contained: " + plant.getRoot().contains(oldRoot));
					root.setId( oldRoot.getId());
					root.setLabel( oldRoot.getLabel());
					root.setAccession( oldRoot.getAccession());
					root.setAnnotations( oldRoot.getAnnotations());
					plant.getRoot().remove( oldRoot);
					plant.getRoot().add( root);
				}
				
				return plant;
			} else {
				// no RSML toplevel root for this treeline
				if ( debugWrite )  System.out.println( "createPlantForTreeline: treeline with out RSML root " + tl.getId());

				Plant plant = new Plant();
				plant.getRoot().add( root);
				return plant;
			}
		} else {
			return null;
		}
	}

	/** Update the functions diameter and status labels in from <code>rosrcRootot</code> to <code>srcRoot</code>.
	 * The roots are assumed to be recursively of equal geometry, i.e. they passed
	 * {@link #rootEquals}
	 *  Specifically: the function values are copied from 
	 * @param srcRoot
	 * @param srcRoot
	 */
	private void updateFunctions(RootType srcRoot, RootType dstRoot) {
		copyFunction( getFunctionByName( srcRoot, FUNCTION_NAME_DIAMETER), getFunctionByName( dstRoot, FUNCTION_NAME_DIAMETER));
		copyFunction( getFunctionByName( srcRoot, FUNCTION_NAME_STATUSLABEL), getFunctionByName( dstRoot, FUNCTION_NAME_STATUSLABEL));
		
		List<RootType> srcChildList = srcRoot.getRoot();
		List<RootType> dstChildList = dstRoot.getRoot();
		for ( int i = 0 ; i < srcChildList.size() ; i++) {
			updateFunctions( srcChildList.get( i), dstChildList.get( i));
		}
	}

	
	/** Copy function <code>srcFunction</code> to <code>dstFunction</code>
	 * assuming equal length
	 * 
	 * @param srcFunction
	 * @param dstFunction
	 */
	private void copyFunction(Function srcFunction, Function dstFunction) {
		// original xsd schema with sample values as attributes
//		for ( int i = 0 ; i < srcFunction.getSample().size() ; i++) {
//			dstFunction.getSample().set( i, srcFunction.getSample().get( i));
//		}	
		for ( int i = 0 ; i < srcFunction.getAny().size() ; i++) {
			dstFunction.getAny().set( i, srcFunction.getAny().get( i));
	}	

	}

	/** Test if the geometry of the two roots is identical.
	 * This recursively checks the child roots for equality
	 * @param root1
	 * @param root2
	 * @return
	 */
	private boolean rootEquals(RootType root1, RootType root2) {
		if ( root1.getGeometry() == null || root2.getGeometry() == null) {
			// not conforming to RSML specification
			return false;
		}
		
		Polyline polyline1 = root1.getGeometry().getPolyline();
		Polyline polyline2 = root2.getGeometry().getPolyline();
		
		if ( polyline1 == null || polyline2 == null ) {
			// not conforming to RSML specification
			return false;
		}
		
		List<RootType> childList1 = root1.getRoot();
		List<RootType> childList2 = root2.getRoot();

		if ( childList1 == null || childList2 == null) {
			// not conforming to RSML specification
			return false;
		}

		if ( ! equalPolylines( polyline1, polyline2)) {
			return false;
		}
		
		if ( childList1.size() != childList2.size()) {
			return false;
		}
			
		for ( int i = 0 ; i < childList1.size() ; i++) {
			if ( ! rootEquals( childList1.get( i), childList2.get( i)) ) {
				return false;
			}
		}
		return true;
	}

	/** Test the polylines for equal geometry
	 * @param polyline1
	 * @param polyline2
	 * @return
	 */
	private boolean equalPolylines(Polyline polyline1, Polyline polyline2) {
		if ( polyline1.getPoint().size() != polyline2.getPoint().size()) {
			return false;
		}
			
		for ( int i = 0 ; i < polyline1.getPoint().size() ; i++) {
			PointType point1 = polyline1.getPoint().get(i);
			PointType point2 = polyline2.getPoint().get(i);
			if ( point1.getX().compareTo( point2.getX()) != 0 ||
					point1.getY().compareTo( point2.getY()) != 0 ) {
				return false;
			} else if ( point1.getZ() == null &&  point2.getZ() == null) {
				continue;
			} else if (point1.getZ() == null ||  point2.getZ() == null ||
					point1.getZ().compareTo( point2.getZ()) != 0) {
				return false;
			}
		}

		return true;
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
	 * @param unified
	 * @param roi
	 * @return
	 */
	private RootType createRSMLRootFromNode(Treeline tl, Connector connector, Node<Float> node, RootType parentRSMLRoot,
											int siblingIndex, int parentNodeIndex, boolean unified, de.unihalle.informatik.rhizoTrak.display.Polyline roi) {
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
			root.setId( getRsmlIdForTreeline( tl, connector, unified));
		} else {
			// non top level root
			root.setId( parentRSMLRoot.getId() + "-" + String.valueOf( siblingIndex));

			byte statuslabel = node.getConfidence();
			
			if ( statuslabel != RhizoProjectConfig.STATUS_VIRTUAL && statuslabel != RhizoProjectConfig.STATUS_VIRTUAL_RSML) {
				// the branch is not connected by a virtual segment, add the parent node
				addNode( parentNode, RhizoProjectConfig.STATUS_UNDEFINED, tl, polyline, diameters, statusLabels, roi);
				nodeCount++;
			}

			if ( statuslabel != RhizoProjectConfig.STATUS_VIRTUAL_RSML) {
				// add parent node property
				
				// the following using JAXB yields wrong name
				//ParentNode pn = new ParentNode();
				//pn.setValue(parentNodeIndex);
				//props.getAny().add( createElementForXJAXBObject( pn));
				
		    	Element pnProp = createW3Element( PROPERTY_NAME_PARENTNODE);
		    	pnProp.setAttribute( "value", String.valueOf(parentNodeIndex));
		    	props.getAny().add( pnProp);
			}
		}
		
		addNode( node, node.getConfidence(), tl, polyline, diameters, statusLabels, roi);
		nodeCount++;

		root.setProperties( props);
				
		// index for all (direct) branching subtrees
		int brachingSubtreeIndex = 1;
		
		ArrayList<Node<Float>> children = reorderChildNodes( node.getChildrenNodes());
		while ( children.size() > 0 ) {
			Iterator<Node<Float>> itr = children.iterator();
			// continue the polyline with the first child
			Node<Float> child = itr.next();
			
			addNode( child, child.getConfidence(), tl, polyline, diameters, statusLabels, roi);
			nodeCount++;

			while ( itr.hasNext()) {
				child = itr.next();
				// create branching root
				// beware: we already added a node after the branching node
				if ( this.rhizoMain.getProjectConfig().isParentNodeIndexStartsWithOne() )
					root.getRoot().add( createRSMLRootFromNode( tl, connector, child, root, brachingSubtreeIndex, nodeCount-1, unified, roi));
				else 
					root.getRoot().add( createRSMLRootFromNode( tl, connector, child, root, brachingSubtreeIndex, nodeCount-2, unified, roi));

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
	
	/**
	 * @param name
	 * @return
	 */
	private Element createW3Element(String name) {		
    	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    	documentBuilderFactory.setNamespaceAware(false);
   
    	try {
    		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = documentBuilder.newDocument();
	    	Element customElement = doc.createElement( name);
	    	return customElement;
		} catch (ParserConfigurationException e) {
			throw new InternalError( "rhizoRSML: can not create W3 Element for " + name);
		}
	}

	/** return a string id which represent the treeline <code>tl</code> in a RSML file.
	 * Always use the connector Id, if any
	 * @param tl
	 * @param connector
	 * @return
	 */
	private String getRsmlIdForTreeline(Treeline tl, Connector connector) {
		return getRsmlIdForTreeline(tl, connector);
	}
	
	/** return a string id which represent the treeline <code>tl</code> in a RSML file.
	 * If <code>unified</code> is true the connector Id is used, if any, otherwise always the treeline Id
	 * @param tl
	 * @param connector
	 * @param unified 
	 * @return
	 */
	private String getRsmlIdForTreeline(Treeline tl, Connector connector, boolean unified) {
		if ( connector == null || ! unified)
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
	 *  @param node
	 * @param statusLabelInteger
	 * @param tl
	 * @param polyline
	 * @param diameters
	 * @param statusLabels
	 * @param roi
	 */
	private void addNode(Node<Float> node, int statusLabelInteger, Treeline tl, Polyline polyline, Function diameters,
						 Function statusLabels, de.unihalle.informatik.rhizoTrak.display.Polyline roi) {

		Point2D p = null;
		p = RhizoUtils.getAbsoluteTreelineCoordinates( new Point2D.Float( node.getX(), node.getY()), tl);

		// translate if roi non null
		if ( roi != null) {
			p = new Point2D.Float( (float)(p.getX()-roi.getPerimeter().getBounds().x),(float)p.getY()-roi.getPerimeter().getBounds().y);
		}

		PointType pt = new PointType();
		pt.setX( new BigDecimal( p.getX()));
		pt.setY( new BigDecimal( p.getY()));
		polyline.getPoint().add( pt);
		
		// original xsd schema with sample values as attributes
//		Sample diameter = new Sample();
//		diameter.setValue( new BigDecimal( 2*((RadiusNode)node).getData()));
//		diameters.getSample().add( diameter);
//
//		Sample sl = new Sample();
//		sl.setValue( new BigDecimal( statusLabelInteger));
//		statusLabels.getSample().add(sl);
		
		Element diameterElement = createW3Element( NAME_SAMPLE_ELEMENT);
		setSampleValue( diameterElement, new BigDecimal( 2*((RadiusNode)node).getData()));
		diameters.getAny().add( diameterElement);
		
		Element slElement = createW3Element( NAME_SAMPLE_ELEMENT);
		setSampleValue( slElement, new BigDecimal( statusLabelInteger));
		statusLabels.getAny().add(slElement);
	}

	/** Set the value of a sample element in a function.
	 * <p> depending on <code>writeFunctionSamplesAsAttribute</code> it is
	 * written as the attribute <code>value</code> or the text
	 * 
	 * @param element
	 * @param value
	 */
	private void setSampleValue(Element element, BigDecimal value) {
		if ( this.rhizoMain.getProjectConfig().isWriteFunctionSamplesAsAttribute() ) {
			element.setAttribute( "value", String.valueOf( value));
		} else {
			element.setTextContent(String.valueOf( value));
		}		
	}

	/** Get the value of a sample element in a function.
	 * Use the value attribute or the text content
	 * 
	 * @param element
	 * @return null, if neither value attribute nor the text content defined
	 */ 
	private BigDecimal getSampleValue(Element element) {
		if ( element == null) {
			return null;
		} else if ( element.getAttribute( "value") != null && ! element.getAttribute( "value").isEmpty()) {
			return BigDecimal.valueOf( Double.valueOf( element.getAttribute( "value")));
		} else if ( element.getTextContent() != null ) {
			return BigDecimal.valueOf( Double.valueOf( element.getTextContent()));
		} else {
			return null;
		}
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
			throw new InternalError( "write RSML: internal error, cannot marshal object of type " + 
					jaxbObject.getClass().getCanonicalName());
		}
    	JAXB.marshal( jaxbObject, new DOMResult(document));
    	Element element = document.getDocumentElement();

    	return element;
    }
    
	/** Create a  <code>StatusLabelMapping</code> 
     * representing the status label mapping <code>idx</code> to <code>name</code>
     * 
     * @param idx
     * @param name
     * @return
     */
    private StatusLabelMapping createStatusLabelMapping ( int idx, String name) {
    	StatusLabelMapping isp = new StatusLabelMapping();
    	isp.setInt( idx);
    	isp.setValue( name);
    	
    	return isp;
    }
    
	/**
	 * Reads one or more RSML file into the rhizoTrak project. Starting with <code> firstLayer</code>.
	 * If <code> firstLayer </code> is <code> null </code> the RSML files are appended at the end of the LayerSet.
	 * 
	 * @author Posch
	 */
	public void readRSML(List<File> rsmlFiles, Layer firstLayer) {

		List<Rsml> rsmls = new LinkedList<Rsml>();
	
		// parse all RSML files
		for ( File rsmlFile : rsmlFiles ) {
			try {
				JAXBContext context = JAXBContext.newInstance( Rsml.class);
				Unmarshaller um = context.createUnmarshaller();
				rsmls.add( (Rsml) um.unmarshal( rsmlFile));
			} catch (Exception e) {
				throw new InternalError( "Cannot read RSML from " + rsmlFile.getName() + ".\nCancelling import.");
			}
		}
		
		List<Layer> availableLayers = getAvailableLayers(firstLayer);

		// check status label mappings
		StringBuilder rsmlMappings = new StringBuilder();
		RhizoProjectConfig config = rhizoMain.getProjectConfig();
		
		List<RhizoStatusLabel> statusLabelMapping = config.getStatusLabelMapping();
		Collection<Integer> fixedStatusLabelInt = config.getFixedStatusLabelInt();
		
		HashMap<Integer, RhizoStatusLabel> projectMap = new HashMap<Integer, RhizoStatusLabel>();
		List<RhizoStatusLabel> additionalLabels = new ArrayList<RhizoStatusLabel>();
		
		for(int i: fixedStatusLabelInt) projectMap.put(i, config.getStatusLabel(i));
		for(int i = 0; i < statusLabelMapping.size(); i++) projectMap.put(i, statusLabelMapping.get(i));
		
		for(int i = 0; i < rsmls.size(); i++)
		{
			HashMap<Integer, String> rsmlMap = getStatusLabelMappingFromScene(rsmls.get(i));

			if(null != rsmlMap && rsmlMap.size() > 0)
			{
				
				for(int j: rsmlMap.keySet())
				{
					if(null == projectMap.get(j))
					{
						if(j <= -fixedStatusLabelInt.size() && j > projectMap.size()) continue;
						
						RhizoStatusLabel rsl = new RhizoStatusLabel(config, rsmlMap.get(j), rsmlMap.get(j).substring(0, 1), RhizoProjectConfig.DEFAULT_STATUS_COLOR);
						projectMap.put(j, rsl);
						
						additionalLabels.add(rsl);
					}
					else
					{
						if(!projectMap.get(j).getName().equals(rsmlMap.get(j)))
						{
							rsmlMappings.append(j + ": " + projectMap.get(j).getName() + " (project) does not match "
									+ rsmlMap.get(j) + " (" + rsmlFiles.get(i).getName() + ")\n");
						}
					}
				}
				
				if(rsmlMappings.length() > 0) rsmlMappings.append("\n");
			}
			else rsmlMappings.append("No label mapping was defined in " + rsmlFiles.get(i).getName() + ".\n");
		}
		
		if(rsmlMappings.length() > 0)
		{
			rsmlMappings.insert(0, "Found inconsistencies in the RSML status label mapping."
					+ "\nIf you continue, the mapping defined in the project will be used and "
					+ "any additionally defined labels (except negative ones) will be added to the project.\n\n");
			rsmlMappings.append("\nProceed anyway?");
			
			int result = JOptionPane.showConfirmDialog(null, rsmlMappings.toString(), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			
			if(result == JOptionPane.NO_OPTION) return;
		}
		
		
		// check if treelines already exist on available layers
		StringBuilder layersWithTreelines = new StringBuilder();
		HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks(rhizoMain.getProject());
		for(Layer layer: availableLayers)
		{
			if(RhizoUtils.areTreelinesInLayer(rootstackThings, layer))
			{
				layersWithTreelines.append("Layer " + (int)(layer.getZ()+1) + "\n");
			}
		}
		
		if(layersWithTreelines.length() > 0)
		{
			layersWithTreelines.insert(0, "The following layers already contain treelines: \n\n");
			layersWithTreelines.append("\n");
			String[] options = new String[] {"Delete treelines before importing", "Import and don't delete", "Cancel"};
			
			int result = JOptionPane.showOptionDialog(null, layersWithTreelines.toString(), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
			        null, options, options[0]);
			
			if(result == 2) return; // 2 == Cancel
			
			// set the flag for the import method
			deleteTreelinesBeforeImport = result == 0;
		}
		
		// check for inconsistencies and create image list
		StringBuilder imageInconsistencies = new StringBuilder();
		List<String> imageFilePaths = new ArrayList<String>();
		for(int i = 0; i < rsmls.size(); i++)
		{
			Rsml rsml = rsmls.get(i);		
			
			if(null != rsml.getMetadata())
			{
				if(null == rsml.getMetadata().getImage())
				{
					imageFilePaths.add(null);
					imageInconsistencies.append("Image data is missing for file: " + rsmlFiles.get(i).getName() + "\n");
					continue;
				}
				
				String rsmlSHA256 = rsml.getMetadata().getImage().getSha256();
				String path = rsml.getMetadata().getImage().getName();
				
				if(null == path)
				{
					imageFilePaths.add(null);
					imageInconsistencies.append("Image path is not set in file: " + rsmlFiles.get(i).getName() + "\n");
					continue;
				}
				
				File imageFile = new File(null != rsmlBaseDir ? (rsmlBaseDir.getAbsolutePath() + File.separator + path) 
						: (rsmlFiles.get(i).getParent() + File.separator + path));
									
				if(imageFile.exists())
				{
					imageFilePaths.add(imageFile.getAbsolutePath());
					
					if(i < availableLayers.size())
					{
						Layer layer = availableLayers.get(i);
						RhizoLayerInfo layerInfo = rhizoMain.getLayerInfo(layer);
						
						if(null == layerInfo.getImageHash()) continue;
						
						if(null != rsmlSHA256 && !rsmlSHA256.equals(""))
						{
							if(!layerInfo.getImageHash().equals(rsmlSHA256))
							{
								imageInconsistencies.append("Images do not match: " + RhizoUtils.getImageName(layer) + " and " + path + "\n");
							}
						}
						else // search for image anyway and calculate the sha256
						{
							String rsmlSHA256calculated = RhizoUtils.calculateSHA256(imageFile.getAbsolutePath());
							
							if(!layerInfo.getImageHash().equals(rsmlSHA256calculated))
							{
								imageInconsistencies.append("Images do not match: " + RhizoUtils.getImageName(layer) + " and " + path + "\n");
							}
						}
					}
				}
				else
				{
					imageFilePaths.add(null);
					imageInconsistencies.append("Could not find image for file: " + rsmlFiles.get(i).getName() + "\n");
					continue;
				}
			}
			else
			{
				imageFilePaths.add(null);
				imageInconsistencies.append("Meta data is missing for file: " + rsmlFiles.get(i).getName() + "\n");
				continue;
			}
		}


		
		if(imageInconsistencies.length() > 0)
		{
			imageInconsistencies.insert(0, "Found inconsistencies:\n\n");
			imageInconsistencies.append("\nProceed anyway?");

			int result = JOptionPane.showConfirmDialog(null, imageInconsistencies.toString(), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			
			if(result == JOptionPane.NO_OPTION) return;
		}
		
		// check for unified in all files
		boolean allUnified = true;
		for(Rsml rsml: rsmls)
		{
			if(null != rsml.getMetadata() && null != rsml.getMetadata().getTimeSequence())
			{
				if(!rsml.getMetadata().getTimeSequence().isUnified())
				{
					allUnified = false;
					break;
				}
			}
			else
			{
				allUnified = false;
				break;
			}
		}
		
		if(!allUnified)
		{
			String message = "At least one unified flag has not been set or does not exist in the selected RSML files.\n"
					+ "Consequently connectors will not be created on import.\n\nProceed anyway?";
			
			int result = JOptionPane.showConfirmDialog(null, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			
			if(result == JOptionPane.NO_OPTION) return;
		}
		
		// >>>>>>>>>> checks for inconsistencies need to happen before this point !!!
		
		
		
		// create new empty layers as needed
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
		
		StringBuilder addedLabels = new StringBuilder();
		// add additionally defined status labels to the project
		for(RhizoStatusLabel rsl: additionalLabels)
		{
			config.addStatusLabelToSet(rsl);
			config.appendStatusLabelMapping(rsl);
			addedLabels.append(rsl.getName() + "\n");
		}
		
		if(addedLabels.length() > 0)
		{
			addedLabels.insert(0, "The following status labels were added: \n\n");
			Utils.showMessage(addedLabels.toString());
		}
		
		// collect for each ID of a toplevel root/polyline the treeline object created for this ID
		HashMap<String,List<Treeline>> topLevelIdTreelineListMap = new HashMap<String,List<Treeline>>();
		
		// loop over the RSML files
		for ( int i = 0 ; i < rsmls.size() ; i++ ) {
			Rsml rsml = rsmls.get( i);
			Layer layer = availableLayers.get( i);
			String imageFilePath = imageFilePaths.get(i);
			
			importRsmlToLayer( rsml, layer, imageFilePath, topLevelIdTreelineListMap);
		}
		

		if(allUnified)
		{
			// collect connectors before the loop so we dont get the newly created ones
			List<Connector> connectors = RhizoUtils.getConnectorsBelowRootstacks(rhizoMain.getProject());
			// create connector -> id map of current connectors
			Map<String, Connector> connectorIds = new HashMap<String, Connector>();
			for(Connector connector: connectors)
			{
				connectorIds.put(getRsmlIdForTreeline(null, connector), connector);
			}
			
			for(String id: topLevelIdTreelineListMap.keySet())
			{
				List<Treeline> treelineList = topLevelIdTreelineListMap.get(id);
				
				boolean connectorFound = false;
				
				// find connector
				Connector c = connectorIds.get(id);
				if(null != c)
				{
					for(Treeline treeline: treelineList)
					{
						if(!c.addConTreeline(treeline))
						{
							Utils.log("rhizoTrak", "Can not add treeline to connector " + c.getId());
						}
					}
					
					connectorFound = true;
				}

				// no connector found and more than one treeline in list
				// then create new connector
				if(!connectorFound && treelineList.size() > 1)
				{
					List<Displayable> connector = RhizoUtils.addDisplayableToProject(rhizoMain.getProject(), "connector", 1);
					Connector newConnector = (Connector) connector.get(0);
					
					for(Treeline treeline: treelineList)
					{
						if(!newConnector.addConTreeline(treeline))
						{
							Utils.log("rhizoTrak", "Can not add treeline to connector " + newConnector.getId());
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * Parses the status label mapping from a RSML file.
	 * @param rsml 
	 * @return A HashMap of the status label mapping or null if none is defined in the RSML file
	 */
	private HashMap<Integer, String> getStatusLabelMappingFromScene(Rsml rsml)
	{
		if(null == rsml.getScene() || null == rsml.getScene().getProperties() || null == rsml.getScene().getProperties().getAny()) return null;
		
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		
		for(Element e: rsml.getScene().getProperties().getAny())
		{
			if(e.getAttributes().getLength() < 2 || !e.getNodeName().equals("statusLabelMapping")) continue;
			
			int b = Integer.parseInt(e.getAttribute("int"));
			String s = e.getAttribute("value");
			
			map.put(b, s);
		}
		
		return map;
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
	 * @param rsml
	 * @param layer
	 * @param imageFilePath Is <code> null </code> if image has not been found for this rsml file
	 * @param topLevelIdTreelineListMap 
	 */
	private void importRsmlToLayer( Rsml rsml , Layer layer, String imageFilePath, HashMap<String, List<Treeline>> topLevelIdTreelineListMap) {
		// read the rsml file

		try {
			RhizoLayerInfo layerInfo = this.rhizoMain.getLayerInfo(layer);
			if ( layerInfo == null ) {
				layerInfo = new RhizoLayerInfo( layer, rsml);
				this.rhizoMain.setLayerInfo( layer, layerInfo);
			} else {
				
				// image exists in layer -> invalidate image object of incoming rsml
				if(null != layerInfo.getImageHash())
				{
					rsml.getMetadata().setImage(null);
				}
				
				// if a rsml file has been imported into this layer before
				// retain the image meta data
				if(null != layerInfo.getRsml())
				{
					rsml.getMetadata().setImage(layerInfo.getRsml().getMetadata().getImage());
				}
				
				layerInfo.setRsml( rsml);
			}
			
			// set by user
			if(deleteTreelinesBeforeImport) RhizoUtils.deleteAllTreelinesFromLayer(layer, rhizoMain.getProject());

			// load image if layer is empty and image was found for the rsml file
			if(null == layerInfo.getImageHash() && null != imageFilePath)
			{
				Project project = rhizoMain.getProject();
				Loader loader = project.getLoader();
				
				loader.importImage(layer, 0, 0, imageFilePath, true);
			}

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
				// check for start of parent node index (0 or 1)
				if ( this.rhizoMain.getProjectConfig().isParentNodeIndexStartsWithOne() )
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

	/** create a RadiusNode for the RSML node in the given layer
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
		// original xsd schema with sample values as attributes
//		if ( statuslabels != null && statuslabels.getSample().size() >= pointIndex && pointIndex >= 0) {
//			return statuslabels.getSample().get( pointIndex).getValue().byteValue();
//		} else {
//			return default_statuslabel;
//		}
		if ( statuslabels != null && statuslabels.getAny().size() > pointIndex && pointIndex >= 0) {
			return getSampleValue( statuslabels.getAny().get( pointIndex)).byteValue();
		} else {
			return default_statuslabel;
		}

	}
	
	private boolean isStatuslabelFromRsmlDefined( Function statuslabels, int pointIndex) {
		// original xsd schema with sample values as attributes
//		if ( statuslabels != null && statuslabels.getSample().size() >= pointIndex && pointIndex >= 0) {
//			return true;
//		} else {
//			return false;
//		}
		if ( statuslabels != null && statuslabels.getAny().size() > pointIndex && pointIndex >= 0) {
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
		// original xsd schema with sample values as attributes
//		if ( diameters != null && diameters.getSample().size() > pointIndex && pointIndex >= 0) {
//			return 0.5f * diameters.getSample().get( pointIndex).getValue().floatValue();
//		} else {
//			return 0.0f;
//		}
		if ( diameters != null && diameters.getAny().size() > pointIndex && pointIndex >= 0) {
			return 0.5f * getSampleValue( diameters.getAny().get( pointIndex)).floatValue();
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
		
		rsmlLoaderFrame = new JDialog((JFrame) null, null == rsmlBaseDir ? "RSML Loader" : "RSML Loader - " + rsmlBaseDir.getAbsolutePath(), true);
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
	
	public JDialog getRSMLLoaderFrame()
	{
		return rsmlLoaderFrame;
	}
	
	public void setDefaultStatusLabel(byte b)
	{
		RhizoRSML.default_statuslabel = b;
	}
}
