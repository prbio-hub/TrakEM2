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
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
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
	private static final String RSML_VERSION = "1.0";

	private static final String SOFTWARE_NAME = "rhizoTrak";
	
	private static final String FUNCTION_NAME_DIAMETER = "diameter";
	private static final String FUNCTION_NAME_STATUSLABEL = "statusLabel";


	private RhizoMain rhizoMain;
	
	private final String ONLY_STRING = "Current layer only";
	private final String ALL_STRING = "All layers";

	public RhizoRSML(RhizoMain rhizoMain) {
		this.rhizoMain = rhizoMain;
	}

	private String projectName;

	/**
	 *  Writes the current project to a RSML file.
	 *  @author Posch
	 */
	public void writeRSML() {
		//TODO this writes currently always from scratch

		projectName = rhizoMain.getXmlName().replaceFirst(".xml\\z", "");
		
		// query output options
		String[] choicesLayers = {ALL_STRING, ONLY_STRING};
		JComboBox<String> comboLayers = new JComboBox<String>(choicesLayers);

		JPanel statChoicesPanel = new JPanel();
		statChoicesPanel.setLayout(new GridLayout( 4, 2, 0, 10));
		statChoicesPanel.add(new JLabel("Layers"));
		statChoicesPanel.add( comboLayers);

		// TODO: commented just as long as we only write the current layer
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
				writeLayerFromScratch( new File( name + "-" + String.valueOf( (int)layer.getZ()+1) + ".rsml"), layer);
			}

		} else {
			// Select and open output file
			Layer layer = Display.getFront().getLayer();

			fileChooser.setDialogTitle("File to write RSML to");
			fileChooser.setSelectedFile(new File( folder + projectName + "-" + String.valueOf( (int)layer.getZ()+1) + ".rsml"));
			int returnVal = fileChooser.showOpenDialog(null);

			if (returnVal != JFileChooser.APPROVE_OPTION)
				return; // user cancelled dialog

			// TODO: ask if file should be overridden, if is exists??
			
			// TODO check if a RSML file was read into this layer previously
			writeLayerFromScratch( fileChooser.getSelectedFile(), layer);
		}	
	}
    
	/** Write the <code>layer</code> as an RSML to <code>saveFaile</code> from scratch.
	 * This means we did not read a RSML file previously for this layer (or ignore it)
	 * 
	 * @param saveFile
	 * @param layer
	 */
	private void writeLayerFromScratch(File saveFile, Layer layer) {
		Rsml rsml = createRSMLFromScratch( layer);
		if ( rsml == null ) {
			Utils.showMessage( "cannot write RSML");
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

		Utils.log("Saved layer " + String.valueOf( layer.getZ()+1) + " to RSML file  - " + saveFile.getAbsolutePath());
	}

	/** Create a rsml xml data structure for the current layer from scratch, from scratch.
	 * This means we did not read a RSML file previously for this layer (or ignore it)
     * 
     * @param layer 
     *RhizoTrakProjectConfig
     * @return the rsml data structure or null, if no rootstacks are found
     */
    private Rsml createRSMLFromScratch(Layer layer) {
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
    		Rsml.Metadata metadata = createRhizotrakMetatdata( layer);
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
					String.valueOf( (long)layer.getZ() + 1));
    		//TODO should we keep the stack trace on production??
    		e.printStackTrace();
    	}
    	return rsml;
    }
   
    /** Create the metadata for a rhizotrak project for the given <code>layer</code>.
     * <br>
     * Property definitions for status label mapping and parent node
     * 
     * @param layer
     * @return
     */
    private Metadata createRhizotrakMetatdata( Layer layer) {
    	Rsml.Metadata metadata= new Metadata();
    	metadata.setVersion(  RSML_VERSION);
    	
    	return extendMetatdata( metadata, layer);
    }
    
    /** Extend a RSM metadata object with rhizoTrak specific information/content
     * 
     * @param metadata
     * @param layer
     * @return
     */
    private Rsml.Metadata extendMetatdata( Rsml.Metadata metadata, Layer layer) {
    	//TOOO:; maybe can generalize this method to also extend an existing annotation object
    	// read previously with rhizoTrak specific information if not already contained
    	
    	// TODO: set unit from calibration information, if available
    	// TODO cope with extending, i.e. set only if not already set ???
    	metadata.setUnit(  "pixel");
    	metadata.setResolution( new BigDecimal(1));

    	final GregorianCalendar now = new GregorianCalendar();
    	try {
    		metadata.setLastModified(  DatatypeFactory.newInstance().newXMLGregorianCalendar(now));
    	} catch (DatatypeConfigurationException e1) {
    		Utils.showMessage( "write RSML: can not generate time for rsml");
    	}

    	metadata.setSoftware( SOFTWARE_NAME);
    	metadata.setUser( System.getProperty("user.name"));

    	// TODO cope with extending, i.e. set only if not already set ???
    	metadata.setFileKey( projectName + "_" + BigInteger.valueOf( (long)layer.getZ() + 1));

    	// TOOD image
    	// TODO cope with extending, i.e. set only if not already set ???
    	metadata.setImage( new Image());

    	// TODO cope with extending, i.e. set only if not already set ???
    	TimeSequence timeSequence = new TimeSequence();
    	timeSequence.setLabel( projectName);
    	timeSequence.setIndex(  BigInteger.valueOf( (long)layer.getZ() + 1));
    	timeSequence.setUnified( true);
    	metadata.setTimeSequence( timeSequence);

    	// property definitions
    	PropertyDefinitions pDefs = metadata.getPropertyDefinitions();
    	if ( pDefs == null ) pDefs = new PropertyDefinitions();

    	// TODO cope with extending, i.e. set only if not already set ???
    	PropertyDefinition pDef = new PropertyDefinition();
    	pDef.setLabel( "StatusLabelMapping");
    	pDef.setType( "Integer-String-Pair");
    	pDefs.getPropertyDefinition().add( pDef); 

    	// TODO cope with extending, i.e. set only if not already set ???
    	PropertyDefinition pnDef = new PropertyDefinition();
    	pnDef.setLabel( "parent-node");
    	pnDef.setType( "integer");
    	pDefs.getPropertyDefinition().add( pnDef); 

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
	 * segment has not the status VIRTUAL.
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

		// RSML R reader requires a, even if empty, property list
		PropertyListType props = new PropertyListType();

		if ( parentNode != null ) {
			// non top level root
			root.setId( parentRSMLRoot.getId() + "-" + String.valueOf( siblingIndex));

			//TODO we have to cope with VIRTUAL and VIRTUAL_RSML status labels
			addNode( parentNode, RhizoProjectConfig.STATUS_UNDEFINED, tl, polyline, diameters, statusLabels);
			nodeCount++;
			
			// add parent node
			ParentNode pn = new ParentNode();
			pn.setValue(parentNodeIndex);
			props.getAny().add( createElementForXJAXBObject( pn));
			
			
		} else {
			// top level root
			long id;
			if ( connector == null)
				id = tl.getId();
			else
				id = connector.getId();
			
			root.setId( String.valueOf( id));
		}
		root.setProperties( props);

				
		addNode( node, node.getConfidence(), tl, polyline, diameters, statusLabels);
		nodeCount++;

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
				root.getRoot().add( createRSMLRootFromNode( tl, connector, child, root, brachingSubtreeIndex, nodeCount-1));
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
	 * @author Posch
	 */
	public void readRSML() {
		// TODO; just a first (zeroth) version; read into current layer
		//TODO 
		String[] filepath = Utils.selectFile("test");
		if(null == filepath) return;
		File file = new File(filepath[0] + filepath[1]);
		
		Rsml rsml = null;
		try {
			JAXBContext context = JAXBContext.newInstance( Rsml.class);
			Unmarshaller um = context.createUnmarshaller();
			rsml  = (Rsml) um.unmarshal( file);
		} catch (Exception e) {
			Utils.showMessage( "cannot read RSML from  " + file.getPath());
		}
		
		Layer layer = Display.getFront().getLayer();
		parseRsmlToLayer( rsml, layer);
	}
		
	/** parse the information in <code>rsml</code> into rhizoTrak for <code>layer</code>.
	 * @param rsml
	 * @param layer
	 */
	private void parseRsmlToLayer( Rsml rsml, Layer layer) {
		RhizoRSMLLayerInfo layerInfo = new RhizoRSMLLayerInfo( layer, rsml);
		this.rhizoMain.setLayerInfo( layer, layerInfo);

		// TODO check meta data

		// TODO check status label mapping

		// import the root
		for ( Scene.Plant plant : rsml.getScene().getPlant() ) {
			for ( RootType root : plant.getRoot() ) {
				layerInfo.mapRoot( plant, root);

				Treeline tl = createTreelineForRoot( root, layer);
				layerInfo.mapTreeline( root, tl);
			}
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
		
		// TODO fill in the data, i.e. geometry, potentially diameter/radius and status labels
		Geometry geometry = root.getGeometry();
		Function diameters = getFunctionByName( root, FUNCTION_NAME_DIAMETER);
		Function statuslabels = getFunctionByName( root, FUNCTION_NAME_STATUSLABEL);
		
//		System.out.println( "new treeline");
		
		Iterator<PointType> pointItr = geometry.getPolyline().getPoint().iterator();
		if ( ! pointItr.hasNext()) {
			// no nodes at all
			return treeline;
		}
		
		// index of the next node to insert into the treeline
		int pointIndex = 0;
		
		// add base node
		RadiusNode previousnode = createRhizoTrakNode( pointItr.next(), getRadius( diameters, pointIndex), treeline, layer);
//		System.out.println("rt node " + previousnode.getX() + "   " + previousnode.getY());
		treeline.addNode( null, previousnode, (byte) 0);
		treeline.setRoot( previousnode);
		pointIndex++;
		
		
		while ( pointItr.hasNext()) {
			PointType point = pointItr.next();
			RadiusNode node = createRhizoTrakNode( point, getRadius( diameters, pointIndex), treeline, layer);
			System.out.println("rt node " + node.getX() + "   " + node.getY());

			byte statuslabel = getStatuslabel( statuslabels, pointIndex);
			treeline.addNode(previousnode, node, statuslabel);
			previousnode = node;
			pointIndex++;
		}
		
		return treeline;
	}

	/** create a RadiusNode for the rsmlnode in the given layer
	 * @param rsmlNode
	 * @param radius
	 * @param layer
	 * @return
	 */
	private RadiusNode createRhizoTrakNode(PointType rsmlNode, float radius, Treeline tl, Layer layer) {
		AffineTransform at = tl.getAffineTransform();
		Point2D pt = null;
		try {
			pt = RhizoUtils.getRelativeTreelineCoordinates( new Point2D.Float( rsmlNode.getX().floatValue(), rsmlNode.getY().floatValue()), tl);
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			pt = new Point2D.Float( 0.0f, 0.0f);
			e.printStackTrace();
		}
		return new RadiusNode( (float)pt.getX(), (float)pt.getY(), layer, radius);
	}

	/**Get the statuslabel for index <code>pointIndex</code> from the function <code>statuslabels</code>
	 * or null if  <code>statuslabels</code> is null or index out of range
	 * @param statuslabels
	 * @param pointIndex
	 * @return
	 */
	private byte getStatuslabel(Function statuslabels, int pointIndex) {
		if ( statuslabels != null && statuslabels.getSample().size() >= pointIndex && pointIndex >= 0) {
			return statuslabels.getSample().get( pointIndex).getValue().byteValue();
		} else {
			return RhizoProjectConfig.STATUS_UNDEFINED;
		}
	}

	/** Get the radius for index <code>pointIndex</code> from the function <code>diameters</code>
	 * or null if  <code>diameters</code> is null or index out of range
	 * @param diameters
	 * @param pointIndex
	 * @return
	 */
	private float getRadius(Function diameters, int pointIndex) {
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
		ListIterator<Function> itr = root.getFunctions().getFunction().listIterator();
		
		while ( itr.hasNext() ) {
			Function fct = itr.next();
			if ( fct.getName().equals(name) ) {
				return fct;
			}
		}
		return null;
	}
}
