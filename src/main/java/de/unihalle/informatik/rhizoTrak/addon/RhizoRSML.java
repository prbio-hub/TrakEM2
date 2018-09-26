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
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.IntegerStringPairType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.ParentNodeIndex;
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

		projectName = rhizoMain.getXmlName().replaceFirst(".xml\\z", "");
		
		// query output options
		String[] choicesLayers = {ALL_STRING, ONLY_STRING};
		JComboBox<String> comboLayers = new JComboBox<String>(choicesLayers);

		JPanel statChoicesPanel = new JPanel();
		statChoicesPanel.setLayout(new GridLayout( 4, 2, 0, 10));
		statChoicesPanel.add(new JLabel("Layers"));
		statChoicesPanel.add( comboLayers);

		//    	TODO: commented just as long as we only write the current layer
		//    	int result = JOptionPane.showConfirmDialog(null, statChoicesPanel, "Output Options", 
		//    			JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		//
		//    	if(result != JOptionPane.OK_OPTION) {
		//    		return;
		//    	}
		//
		//    	boolean writeAllLAyers  = ((String) comboLayers.getSelectedItem()).equals( ALL_STRING);
		boolean writeAllLAyers  = false;

		if ( writeAllLAyers ) {

		} else {
			// Select and open output file
			String folder;
			if  ( this.rhizoMain.getStorageFolder() == null )
				folder = System.getProperty("user.home");
			else 
				folder = this.rhizoMain.getStorageFolder();

			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter( "RSML file", "rsml"); 
			fileChooser.setFileFilter(filter);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle("File to write RSML to");
			fileChooser.setSelectedFile(new File( folder + projectName + ".rsml"));
			int returnVal = fileChooser.showOpenDialog(null);

			if (returnVal != JFileChooser.APPROVE_OPTION)
				return; // user cancelled dialog

			// TODO: ask if file should be overridden, if is exists??
			File saveFile = fileChooser.getSelectedFile();

			Rsml rsml = createRSML( Display.getFront().getLayer());
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

			Utils.log("Saved to RSML file  - " + saveFile.getAbsolutePath());
		}
	}	
    
    /** Create a rsml xml data structure for the current layer from scratch, i.e. no previous rsml file loaded
     * 
     * @param layer 
     *
     * @return the rsml data structure or null, if no rootstacks are found
     */
    private Rsml createRSML(Layer layer) {
    	Project project = Display.getFront().getProject();
    	
		// compile all treelines to write 
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
					System.out.println( "add " + tl.getId() + " -> " + conn.getId());
					treelineConnectorMap.put( tl,  conn);
				}
			}
		}
		
    	// create rsml
    	Rsml rsml = new Rsml();
    	
    	try {
    		// --- meta data
    		Rsml.Metadata metadata = new Metadata();
    		metadata.setVersion(  RSML_VERSION);
    		// TODO: set unit from calibration information, if available
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

    		metadata.setFileKey( projectName + "_" + BigInteger.valueOf( (long)layer.getZ() + 1));
    		
    		// TOOD image
    		metadata.setImage( new Image());
    		
    		TimeSequence timeSequence = new TimeSequence();
    		timeSequence.setLabel( projectName);
    		timeSequence.setIndex(  BigInteger.valueOf( (long)layer.getZ() + 1));
    		timeSequence.setUnified( true);
    		metadata.setTimeSequence( timeSequence);

    		// property definitions
    		PropertyDefinitions pDefs = new PropertyDefinitions();

    		PropertyDefinition pDef = new PropertyDefinition();
    		pDef.setLabel( "StatusLabelMapping");
    		pDef.setType( "Integer-String-Pair");
    		pDefs.getPropertyDefinition().add( pDef); 
    		
    		PropertyDefinition pnDef = new PropertyDefinition();
    		pnDef.setLabel( "parent-node");
    		pnDef.setType( "integer");
    		pDefs.getPropertyDefinition().add( pnDef); 
    		
    		metadata.setPropertyDefinitions( pDefs);

    		rsml.setMetadata( metadata);

    		// --- the scene
    		Rsml.Scene scene = new Scene();

    		// properties: status label mappings; something to indicate branching without VIRTUAL segments
    		PropertyListType pList = new PropertyListType();
    		for ( int i = 0 ; i < this.rhizoMain.getProjectConfig().sizeStatusLabelMapping() ; i++) {
    	    	pList.getAny().add( createElementForStatusLabelMapping(i, this.rhizoMain.getProjectConfig().getStatusLabel( i).getName()));
    		}
    		// add UNDEFINED
    		this.rhizoMain.getProjectConfig();
			this.rhizoMain.getProjectConfig();
			pList.getAny().add( createElementForStatusLabelMapping( RhizoProjectConfig.STATUS_UNDEFINED, RhizoProjectConfig.NAME_UNDEFINED));
 		
			scene.setProperties( pList);

			// now create the roots
    		for ( Treeline tl : allTreelinesInLayer ) {
    			scene.getPlant().add(createPlantForTreeline( tl, treelineConnectorMap.get( tl)));
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
   
	/** create one rsml plant with one root for one treeline
	 * 
	 * @param tl
	 * @param connector the tl is member of, null if treeline is not member of any treeline
	 * @return
	 */
	private Plant createPlantForTreeline(Treeline tl, Connector connector) {
		Plant plant = new Plant();
		
		Node<Float> rootNode = tl.getRoot();
		RootType root = createRSMLRootFromNode( tl, connector, rootNode, null, -1, -1);

		plant.getRoot().add( root);
		return plant;
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
		diameters.setName( "diameter");
		diameters.setDomain( "polyline");
		
		Function statusLabels = new Function();
		statusLabels.setName( "statusLabel");
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
			props.getAny().add( createElementForParentNode( parentNodeIndex));
			
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
		
		// TODO we have to choose the node we use to continue this polyline from those
		// child nodes with a status label not equal to VIRTUAL and VIRTUAL_RSML
		ArrayList<Node<Float>> children = node.getChildrenNodes();
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
		
		AffineTransform at = tl.getAffineTransform();
		Point2D p = at.transform(new Point2D.Float( node.getX(), node.getY()), null);
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

	/** Create a <code>org.w3c.dom.Element</code> holing a <code>IntegerStringPairType</code> 
     * representing the status label mapping <code>idx</code> to <code>name</code>
     * 
     * @param idx
     * @param name
     * @return
     */
    private Element createElementForStatusLabelMapping ( int idx, String name) {
    	IntegerStringPairType sp = new IntegerStringPairType();
    	sp.setInt( idx);
    	sp.setValue( name);
    	System.out.println( "add " + idx + " " + name);

    	Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			Utils.showMessage( "write RSML: internal error, cannot marshal status label mapping " + idx + " " + name);
			e.printStackTrace();
		}
    	JAXB.marshal(sp, new DOMResult(document));
    	Element element = document.getDocumentElement();

    	return element;
    }
	 /** Create a <code>org.w3c.dom.Element</code> holing a <code>xs:int</code> 
     * representing the index of the parent node
     * 
     * @param idx
     * @param name
     * @return
     */
    private Element createElementForParentNode( int idx) {
    	ParentNodeIndex  rsmlIndex = new ParentNodeIndex();
    	rsmlIndex.setIndex( idx);
    	Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			Utils.showMessage( "write RSML: internal error, cannot marshal parent node index " + idx);
			e.printStackTrace();
		}
    	JAXB.marshal( rsmlIndex, new DOMResult(document));
    	Element element = document.getDocumentElement();

    	return element;
    }

	/**
	 * Reads one or more RSML file into the rhizoTrak project.
	 * @author Posch
	 */
	public void readRSML() {
	}
}
