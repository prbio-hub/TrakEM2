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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
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
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.IntegerStringPairType;
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
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Metadata.PropertyDefinitions;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml.Metadata.PropertyDefinitions.PropertyDefinition;
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


	/**
	 *  Writes the current project to a RSML file.
	 *  @author Posch
	 */
	public void writeRSML() {

		// query output options
		String[] choicesLayers = {ALL_STRING, ONLY_STRING};
		JComboBox<String> comboLayers = new JComboBox<String>(choicesLayers);

		JPanel statChoicesPanel = new JPanel();
		statChoicesPanel.setLayout(new GridLayout( 4, 2, 0, 10));
		statChoicesPanel.add(new JLabel("Layers"));
		statChoicesPanel.add( comboLayers);

		//    	TODO: comment just as long as we only write current layer
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
			String basefilename = rhizoMain.getXmlName().replaceFirst(".xml\\z", "");

			String folder;
			if  ( rhizoMain.getStorageFolder() == null )
				folder = System.getProperty("user.home");
			else 
				folder = rhizoMain.getStorageFolder();

			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter( "RSML file", "rsml"); 
			fileChooser.setFileFilter(filter);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle("File to write RSML to");
			fileChooser.setSelectedFile(new File( folder + basefilename + ".rsml"));
			int returnVal = fileChooser.showOpenDialog(null);

			if (returnVal != JFileChooser.APPROVE_OPTION)
				return; // user cancelled dialog

			File saveFile = fileChooser.getSelectedFile();

			Rsml rsml = createRSML( Display.getFront().getLayer());
			if ( rsml == null ) return;
			
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
     * @return the rsml data strucutre or null, if no rootstacks are found
     */
    private Rsml createRSML(Layer layer) {
    	Project project = Display.getFront().getProject();
    	
		// compile all segments to write 
		List<Treeline> allTreelines;
		allTreelines = RhizoUtils.getTreelinesBelowRootstacks( project, layer);

		if ( allTreelines == null) {
			Utils.showMessage( "rhizoTrak", "WARNING: no rootstacks found");
			return null;
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

    		// TODO metadata.setFileKey);
    		// TOOD image
    		// TODO time series

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

    		for ( Treeline tl :allTreelines ) {
    			scene.getPlant().add(createPlantForTreeline( tl));
    		}

    		rsml.setScene( scene);

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return rsml;
    }
    
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
   

	/** create one rsml plant with one root for one treeline
	 * 
	 * @param tl
	 * @return
	 */
	private Plant createPlantForTreeline(Treeline tl) {
		Plant plant = new Plant();
		
		Node<Float> rootNode = tl.getRoot();
		RootType root = createRSMLRootFromNode( tl, rootNode, null);

		plant.getRoot().add( root);
		return plant;
	}

	/** Create a rsml root for a part of a treeline. 
	 * if <code>parentNode</code> is null, <code>node</code> is the root node of the treeline.
	 * Otherwise  the subtree starting with the segment defined by <code>parentNode</code> 
	 * and <code>node</code> is used
	 * 
	 * @param node
	 * @param parentNode
	 * @return
	 */
	private RootType createRSMLRootFromNode( Treeline tl, Node<Float> node, Node<Float> parentNode) {
		RootType root = new RootType();
		
		Polyline polyline = new Polyline();
		
		Function diameters = new Function();
		diameters.setName( "diameter");
		diameters.setDomain( "polyline");
		
		Function statusLabels = new Function();
		statusLabels.setName( "statusLabel");
		statusLabels.setDomain( "polyline");			
	
		if ( parentNode != null ) {
			addNode( parentNode, RhizoProjectConfig.STATUS_UNDEFINED, tl, polyline, diameters, statusLabels);
		}
		
		addNode( node, node.getConfidence(), tl, polyline, diameters, statusLabels);

		ArrayList<Node<Float>> children = node.getChildrenNodes();
		while ( children.size() > 0 ) {
			Iterator<Node<Float>> itr = children.iterator();
			// continue the polyline with the first child
			Node<Float> child = itr.next();
			addNode( child, child.getConfidence(), tl, polyline, diameters, statusLabels);

			while ( itr.hasNext()) {
				child = itr.next();
				// create branching root
				root.getRoot().add( createRSMLRootFromNode( tl, child, node));
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
	 * NOTE: the status label is not taken from the node to faciliated special coding of the
	 * base node of a polyline
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
		setRsmlPoint( tl, node, pt);
		polyline.getPoint().add( pt);
		
		Sample diameter = new Sample();
		diameter.setValue( new BigDecimal( 2*((RadiusNode)node).getData()));
		diameters.getSample().add( diameter);

		Sample sl = new Sample();
		sl.setValue( new BigDecimal( statusLabelInteger));
		statusLabels.getSample().add(sl);
	}


	/** sets the coordinates in the rsml point <code>pt</code> fromt the node
	 * reflecting the transformation of the treeline <code>tl</code>
	 * 
	 * @param tl
	 * @param node
	 * @param pt
	 */
	private void setRsmlPoint(Treeline tl, Node<Float> node, PointType pt) {
	}
	

	/**
	 * Reads one or more RSML file into the rhizoTrak project.
	 * @author Posch
	 */
	public void readRSML() {
	}
}
