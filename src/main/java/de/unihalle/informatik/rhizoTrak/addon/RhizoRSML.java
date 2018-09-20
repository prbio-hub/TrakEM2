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
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;


import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.PropertyListType;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.RootType;
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

//    	int result = JOptionPane.showConfirmDialog(null, statChoicesPanel, "Output Options", 
//    			JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
//
//    	if(result != JOptionPane.OK_OPTION) {
//    		return;
//    	}
//
//    	boolean writeAllLAyers  = ((String) comboLayers.getSelectedItem()).equals( ALL_STRING);
    	boolean writeAllLAyers  = false;

		// compile all segments to write 
		List<Treeline> allTreelines;
		if ( writeAllLAyers ) {
			allTreelines = RhizoUtils.getTreelinesBelowRootstacks( Display.getFront().getProject(), null);
		} else {
			allTreelines = RhizoUtils.getTreelinesBelowRootstacks( Display.getFront().getProject(), Display.getFront().getLayer());
		}
		
		if ( allTreelines == null) {
			Utils.showMessage( "rhizoTrak", "WARNING: no rootstacks found");
			return;
		}
			
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

    	// create rsml
    	// TODO currently only the current layer
    	Rsml rsml = new Rsml();
    	
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
    	pDef.setType( "String-Integer-Pair");
		pDefs.getPropertyDefinition().add( pDef); 
		
		pDef = new PropertyDefinition();
    	pDef.setLabel( "VirtualBraching");
    	pDef.setType( "boolean");
		pDefs.getPropertyDefinition().add( pDef); 
	
		metadata.setPropertyDefinitions( pDefs);
    	
    	rsml.setMetadata( metadata);
    	
    	// --- the scene
    	Rsml.Scene scene = new Scene();
    	
    	PropertyListType pList = new PropertyListType();
    	// TODO properties: status label mappings; something to indicate branching without VIRUTAL segments
    	scene.setProperties( pList);
 
    	for ( Treeline tl :allTreelines ) {
    		scene.getPlant().add(createPlantForTreeline( tl));
    	}
    	
    	rsml.setScene( scene);
    	
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

	/** create one rsml plant for one treeline
	 * 
	 * @param tl
	 * @return
	 */
	private Plant createPlantForTreeline(Treeline tl) {
		Plant plant = new Plant();
		RootType root = new RootType();
	
		plant.getRoot().add( root);
		return plant;
	}

	/**
	 * Reads one or more RSML file into the rhizoTrak project.
	 * @author Posch
	 */
	public void readRSML() {
	}
}
