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

/*
 * The Cohen-Sutherland line clipping algorithm implemented in the internal
 * Segment class has been inspired by
 *
 * //
 *  * CohenSutherland.java
 *  * --------------------
 *  * (c) 2007 by Intevation GmbH
 *  *
 *  * @author Sascha L. Teichmann (teichmann@intevation.de)
 *  * @author Ludwig Reiter       (ludwig@intevation.de)
 *  *
 *  * This program is free software under the LGPL (>=v2.1)
 *  * Read the file LICENSE.txt coming with the sources for details.
 *  //
 *
 *  originally released under LGPL (>=v2.1). The original source file can,
 *  e.g., be found on Github:
 *
 *  https://github.com/tabulapdf/tabula-java/blob/master/src/main/java/technology/tabula/CohenSutherlandClipping.java
 *
 */

package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.TreeEventListener;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;

public class RhizoStatistics {
	final static double inchToMM = 25.4;

	/**
	 * All units supported in Imagej ImagePlus for calibration
	 */
	private final static HashSet<String>  IMAGEJ_UNITS  = new HashSet<String>();
	{ IMAGEJ_UNITS.add( "inch");
		IMAGEJ_UNITS.add( "mm");
	}


	private final String AGGREGATED = "Aggregated";
	private final String SEGMENTS = "Segments";
	private final String ONLY_STRING = "Current layer only";
	private final String ALL_STRING = "All layers";
	private RhizoMain rhizoMain;

	private boolean debug = false;

	private String outputUnit;

	// Hash of all layers with treeline(s) to write to
	// set in computeStatistics
	private HashSet<Layer> allLayers = null;

	/**
	 * Calibration info from imageplus for each layer
	 */
	HashMap<Integer,ImagePlusCalibrationInfo> allCalibInfos;


	public RhizoStatistics(RhizoMain rhizoMain) 	{
		this.rhizoMain = rhizoMain;
	}

	/**
	 *
	 */
	public void writeStatistics()
	{
		String[] choices1 = {"{Tab}" , "{;}", "{,}", "Space"};
		String[] choices1_ = {"\t", ";", ",", " "};
		String[] choices2 = {ALL_STRING, ONLY_STRING};
		String[] choices3 = {"pixel", "inch", "mm"};
		String[] choicesType = { SEGMENTS, AGGREGATED};

		JComboBox<String> combo1 = new JComboBox<String>(choices1);
		JComboBox<String> combo2 = new JComboBox<String>(choices2);
		JComboBox<String> combo3 = new JComboBox<String>(choices3);
		JComboBox<String> comboType = new JComboBox<String>( choicesType);

		JPanel statChoicesPanel = new JPanel();
		statChoicesPanel.setLayout(new GridLayout( 4, 2, 0, 10));
		statChoicesPanel.add(new JLabel("Output type "));
		statChoicesPanel.add( comboType);
		statChoicesPanel.add(new JLabel("Layers "));
		statChoicesPanel.add( combo2);
		statChoicesPanel.add(new JLabel("Unit "));
		statChoicesPanel.add(combo3);
		statChoicesPanel.add(new JLabel("Separator "));
		statChoicesPanel.add(combo1);

		int result = JOptionPane.showConfirmDialog(null, statChoicesPanel, "csv Output Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(result != JOptionPane.OK_OPTION) {
			return;
		}

		String sep = "";
		String outputLayers = "";
		this.outputUnit = "";
		boolean aggregatedStatistics;

		sep = choices1_[Arrays.asList(choices1).indexOf(combo1.getSelectedItem())];
		outputLayers = (String) combo2.getSelectedItem();
		this.outputUnit = (String) combo3.getSelectedItem();
		if ( ((String) comboType.getSelectedItem()).equals( AGGREGATED)) {
			aggregatedStatistics = true;
		} else {
			aggregatedStatistics = false;
		}

		if(sep.equals("") || outputLayers.equals("") || this.outputUnit.equals("")) {
			Utils.showMessage( "rhizoTrak", "illegal choice of options for Write csv file");
			return;
		}

		// Select and open and output file
		String basefilename = rhizoMain.getXmlName().replaceFirst(".xml\\z", "");

		String folder;
		if  ( rhizoMain.getStorageFolder() == null )
			folder = System.getProperty("user.home");
		else
			folder = rhizoMain.getStorageFolder();

		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter( "File for wrting experimental data", "csv");
		fileChooser.setFileFilter(filter);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("File to write experimental to");
		fileChooser.setSelectedFile(new File( folder + basefilename + ".csv"));
		int returnVal = fileChooser.showOpenDialog(null);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return; // user cancelled dialog

		File saveFile = fileChooser.getSelectedFile();

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(saveFile));
		} catch (IOException e) {
			Utils.showMessage( "WriteStatistics can not open " + saveFile.getAbsolutePath());
			return;
		}

		// compile all segments to write
		boolean statForAllLayers = outputLayers.equals( ALL_STRING);
		List<Segment> allSegments;
		if ( statForAllLayers ) {
			allSegments = computeStatistics( Display.getFront().getProject(), null);
		} else {
			allSegments = computeStatistics( Display.getFront().getProject(), Display.getFront().getLayer());
		}

		if ( allSegments == null) {
			try {
				bw.close();
			} catch (IOException e) {
			}
			return;
		}

		// write
		try {
			if ( aggregatedStatistics ) {

				RhizoProjectConfig config = rhizoMain.getProjectConfig();

				// create arrays with lines for layers and columns for status labels
				HashMap<Integer,Double[]> aggregatedLength = new HashMap<Integer, Double[]>();
				HashMap<Integer,Double[]> aggregatedSurface = new HashMap<Integer, Double[]>();
				HashMap<Integer,Double[]> aggregatedVolume = new HashMap<Integer, Double[]>();

				for ( Layer layer : this.allLayers) {
					Double[] stats = new Double[config.sizeStatusLabelMapping()];
					for ( int i = 0 ; i < stats.length ; i++ )
						stats[i] = 0.0;
					aggregatedLength.put( layer.getParent().indexOf(layer) + 1, stats);

					stats = new Double[config.sizeStatusLabelMapping()];
					for ( int i = 0 ; i < stats.length ; i++ )
						stats[i] = 0.0;
					aggregatedSurface.put( layer.getParent().indexOf(layer) + 1, stats);

					stats = new Double[config.sizeStatusLabelMapping()];
					for ( int i = 0 ; i < stats.length ; i++ )
						stats[i] = 0.0;
					aggregatedVolume.put( layer.getParent().indexOf(layer) + 1, stats);

				}

				// iterate over all segments and aggregate the segment length
				for (Segment segment : allSegments) {
					int layerID = segment.layerIndex;
					int status = segment.status;
					aggregatedLength.get( layerID)[ status] += segment.length;
					aggregatedSurface.get( layerID)[ status] += segment.surfaceArea;
					aggregatedVolume.get( layerID)[ status] += segment.volume;
				}

				// write header and one line per layer
				bw.write( "experiment" + sep + "tube" + sep + "timepoint" + sep + "timepoint" + sep + "layerID" +
						sep + "length_" + this.outputUnit + sep + "surfaceArea_" + this.outputUnit + "^2" + sep + "volume_" + this.outputUnit + "^3" +
						sep + "status" + sep + "statusName" + sep + "ImageWidth" + sep + "ImageHeight");

				bw.newLine();

				List<Integer> sortedLayerIDs = new LinkedList<Integer>(aggregatedLength.keySet());
				Collections.sort( sortedLayerIDs);
				for ( int layerID : sortedLayerIDs) {
					ImagePlusCalibrationInfo calibInfo = allCalibInfos.get( layerID);
					String imageName = calibInfo.imagename;
					String tube = RhizoUtils.getICAPTube( imageName);
					String experiment = RhizoUtils.getICAPExperiment(imageName);
					String timepoint = RhizoUtils.getICAPTimepoint(imageName);
					String date = RhizoUtils.getICAPDate(imageName);
					String width = RhizoUtils.NA_String;
					String height =  RhizoUtils.NA_String;

					if ( calibInfo != null && calibInfo.ip != null ) {
						width = String.valueOf( calibInfo.ip.getWidth());
						height = String.valueOf( calibInfo.ip.getHeight());
					}

					for ( int s = 0 ; s < config.sizeStatusLabelMapping(); s++ ) {
						bw.write( experiment + sep + tube + sep + timepoint + sep + date + sep + Integer.toString( layerID) +
								sep + aggregatedLength.get(layerID)[s] +
								sep + aggregatedSurface.get(layerID)[s] +
								sep + aggregatedVolume.get(layerID)[s] +
								sep + s + sep + config.getStatusLabel( s).getName() +
								sep + width + sep + height);
						bw.newLine();

					}
				}

			} else {
				// write header
				bw.write("experiment" + sep + "tube" + sep + "timepoint" + sep + "date" + sep + "rootID" + sep + "layerID" +sep + "segmentID" + sep + "parentID" +
						sep + "x_start_pixel" + sep + "y_start_pixel" + sep + "x_end_pixel" + sep + "y_end_pixel" +
						sep + "length_" + this.outputUnit + sep + "startDiameter_" + this.outputUnit + sep + "endDiameter_" + this.outputUnit +
						sep + "surfaceArea_" + this.outputUnit + "^2" + sep + "volume_" + this.outputUnit + "^3" + sep + "children" + sep + "status" + sep + "statusName" + "\n");
				for (Segment segment : allSegments) {
					bw.write(segment.getStatistics(sep));
					bw.newLine();
				}
			}

			bw.close();
		} catch (IOException e) {
			Utils.showMessage( "WriteStatistics cannot write to " + saveFile.getAbsolutePath());
		}
	}

	/**
	 * @param project
	 * @param currentLayer if null all layers are considered
	 *
	 * @return list of all <code>Segment</code>s to write, null on failure
	 */
	private List<Segment> computeStatistics( Project project, Layer currentLayer) {
		List<Segment> allSegments = new ArrayList<Segment>();

		// all treelines below a rootstack
		LinkedList<Treeline> allTreelines = new LinkedList<Treeline>();
		// all connectors below a rootstack
		LinkedList<Connector> allConnectors = new LinkedList<Connector>();

		// find all rootstacks
		HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks( project);
		if ( rootstackThings == null) {
			Utils.showMessage( "WriteStatistics warning: no rootstack found");
			return allSegments;
		}

		// and collect treelines and connectors below all rootstacks
		this.allLayers = new HashSet<Layer>(); // all layers we have a treeline in to write

		for ( ProjectThing rootstackThing :rootstackThings ) {
			if ( debug)	System.out.println("rootstack " + rootstackThing.getId());
			for ( ProjectThing pt : rootstackThing.findChildrenOfTypeR( Treeline.class)) {
				// we also find connectors!
				Treeline tl = (Treeline)pt.getObject();
				if ( debug)	System.out.println( "    treeline " + tl.getId());

				if ( tl.getClass().equals( Treeline.class)) {
					if ( tl.getFirstLayer() != null &&
							( currentLayer == null || currentLayer.equals( tl.getFirstLayer())) ) {
						if ( debug)	System.out.println( "           as treeline");
						allTreelines.add(tl);
						allLayers.add( tl.getFirstLayer());
					}
				} else if ( tl.getClass().equals( Connector.class)) {
					if ( debug)	System.out.println( "           as connector");

					Connector conn = (Connector)tl;
					allConnectors.add(conn);
				}
			}
		}

		// collect calibration information for all layers
		allCalibInfos = getCalibrationInfo( allLayers);
		if ( allCalibInfos == null)
			return null;

		// create all segments for these treelines to write to the csv file
		// consider image bounds if patch has an associated image
		StringBuilder msg = new StringBuilder();
		try {
			for ( Treeline tl : allTreelines)  {

				List<Segment> currentSegments = new ArrayList<Segment>();
				Map<RadiusNode, Integer> parents = new HashMap<RadiusNode, Integer>();

				if ( debug)	System.out.println( "segment to write " + tl.getId());
				HashSet<Connector> connectorSet = new HashSet<>();
				if ( tl.getTreeEventListener() != null ) {
					for(TreeEventListener tel: tl.getTreeEventListener()) {
						connectorSet.add(tel.getConnector());
					}
				}

				if ( debug)	System.out.println( "got connset" + connectorSet);
				long treelineID;
				if ( connectorSet.size() == 0 ) {
					if ( debug)	System.out.println( "no connector, use treeline ID");
					treelineID = tl.getId();
				} else {
					if ( debug)	System.out.println( "no connector, use connector ID");
					Iterator<Connector> itr = connectorSet.iterator();
					treelineID = itr.next().getId();
					if ( connectorSet.size() > 1 ) {
						msg.append( "WriteStatitics warning: treeline " + tl.getId() + " has more than one connector\n");
					}
				}
				if ( debug)	System.out.println( "Id " + treelineID);

				if ( tl.getRoot() != null) {
					int segmentID = 1;
					Collection<Node<Float>> allNodes = tl.getRoot().getSubtreeNodes();

					for(Node<Float> node : allNodes) {
						if(!node.equals(tl.getRoot())) {
							if ( debug)	{
								System.out.println( "    create segment for node " + node.getConfidence() +
										" patch " + RhizoAddons.getPatch(tl));
							}
							RadiusNode radiusNode = (RadiusNode) node;
							RadiusNode radiusParentNode = (RadiusNode) node.getParent();

							Segment currentSegment = new Segment(rhizoMain, tl, treelineID,
									segmentID, radiusNode, radiusParentNode, this.outputUnit, (int) node.getConfidence());

							currentSegments.add(currentSegment);
							parents.put(radiusNode, segmentID);

							segmentID++;

							if(currentSegment.cohenSutherlandLineClipping())
								allSegments.add(currentSegment);
						}
					}

					for(Segment s: currentSegments){

						s.setParentID(null == parents.get(s.getParentNode()) ? -1 : parents.get(s.getParentNode()));
					}


				}

				if ( debug)	System.out.println( "created segments");
			}
		} catch (Exception ex) {
			Utils.showMessage( "rhizoTrak", "internal error: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
		if ( msg.length() > 1 ) {
			msg.insert(0, "WARNING Write experimental data\n \n");
			Utils.showMessage( "rhizoTrak", new String( msg));
		}

		return allSegments;
	}

	/** get the calibration info for each layer from the ImageJ ImapePlus
	 * @param allLayers
	 * @return
	 */
	private HashMap<Integer,ImagePlusCalibrationInfo> getCalibrationInfo( HashSet<Layer> allLayers) {
		HashMap<Integer,ImagePlusCalibrationInfo> myAllCalibInfos  = new HashMap<Integer,ImagePlusCalibrationInfo>();
		boolean haveAllPatches = true;

		if ( debug ) {
			System.out.println( "getCalibrationInfo, allLayers = " + allLayers);

			for ( Layer layer : allLayers) {
				System.out.println( "layer = " + layer);
//				System.out.println( "layerIndex " + (layer.getParent().indexOf(layer) + 1));
			}
		}

		for ( Layer layer : allLayers) {
			if ( layer == null ) continue;

			int layerIndex = layer.getParent().indexOf(layer) + 1;
			LayerSet layerSet = layer.getParent();
			List<Patch> patches = layerSet.getAll(Patch.class);

			boolean found = false;
			for(Patch patch: patches) 	{
				if(patch.getLayer() == layer) {
					ImagePlus ip = patch.getImagePlus();
					if (  ip != null  ) {
						if ( debug ) System.out.println( "found layer " + layerIndex);
						myAllCalibInfos.put( layerIndex, new ImagePlusCalibrationInfo(ip));
						found = true;
						break;
					}
				}
			}
			if ( ! found) {
				myAllCalibInfos.put( layerIndex, new ImagePlusCalibrationInfo());
				if ( debug ) System.out.println( "found layer " + layerIndex + " without patch");

				haveAllPatches = false;
			}
		}

		// if outputUnit != pixels check validity of calibration information
		// known units and square pixels
		if ( ! this.outputUnit.equals( "pixel")) {
			LinkedList<Integer> zValues = new LinkedList<Integer>();
			zValues.addAll( myAllCalibInfos.keySet());
			Collections.sort( zValues);

			if ( ! haveAllPatches ) {
				StringBuilder msg = new StringBuilder("Warning: not for all layers an image is loaded\n");
				for ( int i : zValues ) {
					msg.append(  "layer " + i + ": " + myAllCalibInfos.get(i).toString() + "\n");
				}
				msg.append( " \nUse pixel units and proceed?\n");

				if ( Utils.checkYN( new String( msg)) ) {
					this.outputUnit = "pixel";
				} else {
					return null;
				}
			} else {

				boolean allValid = true;
				for ( int i : zValues ) {
					if ( ! myAllCalibInfos.get(i).isValid()) {
						allValid = false;
						break;
					}
				}
				if ( ! allValid) {
					StringBuilder msg = new StringBuilder("Warning: Could not find valid calibration for all layers\n");

					for ( int i : zValues ) {
						msg.append(  "layer " + i +
								(myAllCalibInfos.get(i).isValid() ? "(valid): " : "(invalid): ") +
								myAllCalibInfos.get(i).toString() + "\n");
					}
					msg.append( " \nUse pixel units and proceed?\n");

					if ( Utils.checkYN( new String( msg)) ) {
						this.outputUnit = "pixel";
					} else {
						return null;
					}
				} else {
					if ( rhizoMain.getProjectConfig().isShowCalibrationInfo() ) {
						StringBuilder msg = new StringBuilder( "Calibrations for all layers\n");
						for ( int i : zValues ) {
							msg.append(  "layer " + i +
									(myAllCalibInfos.get(i).isValid() ? " (valid): " : " (invalid): ") +
									myAllCalibInfos.get(i).toString() + "\n");
						}

						Object[] options = {"Ok", "Edit calibration"};
						int n = JOptionPane.showOptionDialog(null, new String( msg),
								"",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null,
								options,
								null);

						if(n == 1)
						{
							CalibrationPanel editCalibrationPanel = new CalibrationPanel(zValues, myAllCalibInfos);
							int result = JOptionPane.showConfirmDialog(null, editCalibrationPanel, "Edit calibration information", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

							if(result == JOptionPane.OK_OPTION)
							{
								Map<Integer, JTextField> textFields = editCalibrationPanel.getTextFields();
								Map<Integer, JComboBox> comboBoxes = editCalibrationPanel.getComboBoxes();
								
 								for(int i: zValues)
								{
									ImagePlusCalibrationInfo calibInfo = myAllCalibInfos.get(i);
									JTextField tf = textFields.get(i);
									JComboBox comboBox = comboBoxes.get(i);
									
									String pixelSize = tf.getText();
									String unit = (String) comboBox.getSelectedItem();

									if(null != pixelSize && !pixelSize.equals(""))
									{
										calibInfo.setPixelWidth(Double.parseDouble(pixelSize));
										calibInfo.setPixelHeight(Double.parseDouble(pixelSize));
									}
									
									if(null != unit && !unit.equals(""))
									{
										if(unit.equals("DPI") && Double.parseDouble(pixelSize) != 0)
										{
											calibInfo.setPixelWidth(1.0 / Double.parseDouble(pixelSize));
											calibInfo.setPixelHeight(1.0 / Double.parseDouble(pixelSize));
											calibInfo.setXUnit("inch");
											calibInfo.setYUnit("inch");
										}
										else
										{
											calibInfo.setXUnit(unit);
											calibInfo.setYUnit(unit);
										}
									}
								}
							}
							else return null;
						}
					}
				}
			}
		}
		return myAllCalibInfos;
	}

	class ImagePlusCalibrationInfo {
		ImagePlus ip;
		String imagename;
		String xUnit;
		String yUnit;
		double pixelWidth;
		double pixelHeight;

		ImagePlusCalibrationInfo( ImagePlus ip) {
			this.ip = ip;
			this.imagename = ip.getTitle();
			this.xUnit = ip.getCalibration().getXUnit();
			this.yUnit = ip.getCalibration().getYUnit();
			this.pixelWidth = ip.getCalibration().pixelWidth;
			this.pixelHeight = ip.getCalibration().pixelHeight;
		}

		public ImagePlusCalibrationInfo() {
			this.ip = null;
			this.imagename = "";
			this.xUnit = "";
			this.yUnit = "";
			this.pixelWidth = -1.0;
			this.pixelHeight = -1.0;
		}

		/** return the pixel width in mm
		 * @return return the pixel width in mm,
		 * a negative value, if the image plus has no calibration information
		 */
		double getPixelWidhtMM() {
			if ( this.xUnit.equals("mm")) {
				return this.pixelWidth;
			} else if ( this.xUnit.equals( "inch") ) {
				return this.pixelWidth * inchToMM;
			}

			return -1.0;
		}

		/** return the pixel height  in mm
		 *
		 * @return return the pixel width in mm,
		 * a negative value, if the image plus has no calibration information
		 */
		double getPixelHeightMM() {
			if ( this.yUnit.equals("mm")) {
				return this.pixelHeight;
			} else if ( this.yUnit.equals( "inch") ) {
				return this.pixelHeight * inchToMM;
			}

			return -1.0;
		}

		/** return the DPI for square pixels
		 * @returnDPI for square pixels, return a negative value for non square pixels or in
		 * case of not sufficient calibration information
		 */
		int getDPI() {
			if ( this.isValid()) {
				return (int) Math.round( inchToMM / this.getPixelWidhtMM());
			} else {
				return -1;
			}
		}

		public String toString() {
			return new String( this.imagename + ": DPI =" + fmtd( this.getDPI()) +
					" (pixelwidth [mm] = " + fmt( this.getPixelWidhtMM()) + ", pixelheight[mm] = " + fmt( this.getPixelHeightMM()) +
					", xunit = " + this.xUnit + ", yunit = " + this.yUnit +
					", pixelwidth [xunit] = " + fmt( this.pixelWidth) + ", pixelheight[yunit] = " + fmt( this.pixelHeight) + ")");
		}

		private String fmt( double value) {
			if ( value > 0 )
				return String.format("%.4f", value);
			else
				return RhizoUtils.NA_String;
		}

		private String fmtd( int value) {
			if ( value > 0 )
				return String.format("%d", value);
			else
				return RhizoUtils.NA_String;
		}

		/** Valid if xunit and yunit are supported to convert and if the pixels are square
		 * @return
		 */
		boolean isValid() {
			if ( this.getPixelWidhtMM() > 0.0 && this.getPixelHeightMM() > 0.0 &&
					Math.abs( this.getPixelWidhtMM()-this.getPixelHeightMM()) < 0.00001) {
				return true;
			} else {
				return false;
			}
		}

		public String getImageName() {
			return imagename;
		}

		public double getPixelWidth() {
			return pixelWidth;
		}

		public double getPixelHeight() {
			return pixelHeight;
		}

		public void setPixelWidth(double pixelWidth) {
			this.pixelWidth = pixelWidth;
		}

		public void setPixelHeight(double pixelHeight) {
			this.pixelHeight = pixelHeight;
		}
		
		public String getXUnit() {
			return xUnit;
		}
		
		public String getYUnit() {
			return yUnit;
		}
		
		public void setXUnit(String xUnit) {
			this.xUnit = xUnit;
		}
		
		public void setYUnit(String yUnit) {
			this.yUnit = yUnit;
		}
	}

	/**
	 * Segment class for writing statistics.
	 * @author Axel, Tino
	 *
	 * Note that the Cohen-Sutherland line clipping algorithm implemented in this
	 * class has been inspired by
	 *
	 * //
	 *  * CohenSutherland.java
	 *  * --------------------
	 *  * (c) 2007 by Intevation GmbH
	 *  *
	 *  * @author Sascha L. Teichmann (teichmann@intevation.de)
	 *  * @author Ludwig Reiter       (ludwig@intevation.de)
	 *  *
	 *  * This program is free software under the LGPL (>=v2.1)
	 *  * Read the file LICENSE.txt coming with the sources for details.
	 *  //
	 *
	 *  The original source file can, e.g., be found on Github:
	 *  https://github.com/tabulapdf/tabula-java/blob/master/src/main/java/technology/tabula/CohenSutherlandClipping.java
	 *
	 */
	class Segment {
		// used for clipping
		private final int INSIDE = 0;
		private final int LEFT   = 1;
		private final int RIGHT  = 2;
		private final int BOTTOM = 4;
		private final int TOP    = 8;

		private RadiusNode child;
		private RadiusNode parent;

		private Layer layer;
		private int layerIndex;
		private Treeline t;

		// infos
		private String imageName, experiment, tube, timepoint, date;
		private double length, surfaceArea, volume;
		private int segmentID, numberOfChildren;
		private int parentID;

		private int status;

		private long treeID;

		private float scale = 1;
		private final float minRadius = 0.0f;
		private float radiusParent = minRadius;
		private float radiusChild = minRadius;
		private double xStart;
		private double yStart;
		private double xEnd;
		private double yEnd;

//		private Patch p;

		private RhizoMain rhizoMain;

		/** if <code>p != null</code> assume that a valid calibration info is available for the layer
		 *
		 * @param rhizoMain
		 * @param t
		 * @param treeID
		 * @param segmentID
		 * @param child
		 * @param parent
		 * @param outputUnit
		 * @param status
		 */
		public Segment(RhizoMain rhizoMain, Treeline t, long treeID, int segmentID,
					   RadiusNode child, RadiusNode parent, String outputUnit, int status) throws ExceptionInInitializerError{

			this.t = t;
			this.child = child;
			this.parent = parent;
			this.layer = child.getLayer();
//			this.layerIndex = (int) layer.getZ() + 1;
			this.layerIndex = layer.getParent().indexOf(layer) + 1;

			AffineTransform at = t.getAffineTransform();
			Point2D p1 = at.transform(new Point2D.Float(parent.getX(), parent.getY()), null);
			Point2D p2 = at.transform(new Point2D.Float(child.getX(), child.getY()), null);

			this.xStart = p1.getX();
			this.yStart = p1.getY();
			this.xEnd = p2.getX();
			this.yEnd = p2.getY();

			ImagePlusCalibrationInfo calibInfo = null;
			if ( allCalibInfos == null ||
					(calibInfo = allCalibInfos.get( this.layerIndex)) == null ) {
				// ERROR: this should never happen
				System.err.println( "instantiate Segment, but allCalibInfos == " + allCalibInfos + " and calibInfo = " + calibInfo);
				throw new ExceptionInInitializerError( "instantiate Segment, but allCalibInfos == " + allCalibInfos + " and calibInfo = " + calibInfo);
			}

			if(outputUnit.equals("pixel"))  {
				this.scale = 1;
			} else {
				if ( calibInfo.isValid() ) {
					if(outputUnit.equals("inch")) {
						this.scale = (float) (calibInfo.getPixelHeightMM() / RhizoStatistics.inchToMM);
					} else if(outputUnit.equals("mm"))  {
						this.scale = (float) (calibInfo.getPixelHeightMM() );
					} else {
						// ERROR: this should never happen
						System.err.println( "output Unit " + outputUnit + " unknown");
						throw new ExceptionInInitializerError("output Unit " + outputUnit + " unknown");
					}
				} else {
					// ERROR: this should never happen
					System.err.println( "output Unit " + outputUnit + " but invalid calibration inImagePlusfo");
					throw new ExceptionInInitializerError("output Unit " + outputUnit + " but invalid calibration inImagePlusfo");
				}
			}

			if(parent.getData() > minRadius)
				this.radiusParent = parent.getData() * scale;
			if(child.getData() > minRadius)
				this.radiusChild = child.getData() * scale;

			this.imageName = calibInfo.imagename;
			this.tube = RhizoUtils.getICAPTube( imageName);
			this.experiment = RhizoUtils.getICAPExperiment(imageName);
			this.timepoint = RhizoUtils.getICAPTimepoint(imageName);
			this.date = RhizoUtils.getICAPDate( imageName);

			this.treeID = treeID;
			this.segmentID = segmentID;
			this.status = status;
			this.rhizoMain = rhizoMain;

			calculate();
		}

		public Object getParentNode() {
			return parent;
		}

		public void setParentID(int parentID) {
			this.parentID = parentID;
		}

		/**
		 * calculate length and so forth, also no of children
		 */
		private void calculate() {
			this.length = Math.sqrt(Math.pow(parent.getX() - child.getX(), 2) + Math.pow(parent.getY() - child.getY(), 2)) * scale;
			double s = Math.sqrt(Math.pow((radiusParent - radiusChild), 2) + Math.pow(this.length, 2));
			this.surfaceArea = Math.PI * s * (radiusParent + radiusChild);
			this.volume = (Math.PI * length * (Math.pow(radiusParent, 2) + Math.pow(radiusChild, 2) + radiusParent * radiusChild)) / 3;
			this.numberOfChildren = child.getChildrenCount();
		}

		/**
		 * return the line for this segment for the csv file
		 * @param sep
		 * @return
		 */
		public String getStatistics(String sep) {
			String result = experiment + sep + tube + sep + timepoint + sep + date + sep + Long.toString(treeID) +
					sep + this.layerIndex  +
					sep + Integer.toString(segmentID) +
					sep + Integer.toString(parentID) +
					sep + xStart + sep + yStart + sep + xEnd + sep + yEnd +
					sep + Double.toString(length) + sep + Double.toString(2*radiusParent) + sep + Double.toString(2*radiusChild) +
					sep + Double.toString(surfaceArea) + sep + Double.toString(volume) +
					sep + Integer.toString(numberOfChildren) + sep + status + sep + rhizoMain.getProjectConfig().getStatusLabel(status).getName();

			return result;
		}

		/**
		 * Cohen-Sutherland line clipping algorithm.
		 * <p>
		 * The code of this algorithm has been inspired by
		 *
		 * //
		 *  * CohenSutherland.java
		 *  * --------------------
		 *  * (c) 2007 by Intevation GmbH
		 *  *
		 *  * @author Sascha L. Teichmann (teichmann@intevation.de)
		 *  * @author Ludwig Reiter       (ludwig@intevation.de)
		 *  *
		 *  * This program is free software under the LGPL (>=v2.1)
		 *  * Read the file LICENSE.txt coming with the sources for details.
		 *  //
		 *
		 *  The original source file can, e.g., be found on Github:
		 *  https://github.com/tabulapdf/tabula-java/blob/master/src/main/java/technology/tabula/CohenSutherlandClipping.java
		 *
		 * If imageplus is null (i.e. no image associated) no clipping is done.
		 *
		 * @return true - if both nodes are inside the image or if at least one
		 * 								node is outside and the line is clipped with the image
		 * 		  		false - if both nodes are outside and the line does not
		 * 									intersect with the image
		 */
		public boolean cohenSutherlandLineClipping() {

			ImagePlusCalibrationInfo calibInfo = allCalibInfos.get( this.layerIndex);
			if ( calibInfo == null )
				throw new ExceptionInInitializerError( "can not find calibration information");

			// we have no image extent and cannot clip therefore
			if ( calibInfo.ip == null)
				return true;

			double xMin = 0;
			double yMin = 0;
			double xMax = calibInfo.ip.getWidth() - 1;
			double yMax = calibInfo.ip.getHeight() - 1;

			AffineTransform at = t.getAffineTransform();
			Point2D p1 = at.transform(new Point2D.Float(parent.getX(), parent.getY()), null);
			Point2D p2 = at.transform(new Point2D.Float(child.getX(), child.getY()), null);

			// save original points
			Point2D oldp1 = (Point2D) p1.clone();
			Point2D oldp2 = (Point2D) p2.clone();

			double qx = 0d;
			double qy = 0d;

			boolean vertical = (p1.getX() == p2.getX());

			double slope = vertical ? 0d : (p2.getY()-p1.getY())/(p2.getX() - p1.getX());

			int c1 = regionCode(p1.getX(), p1.getY(), xMin, yMin, xMax, yMax);
			int c2 = regionCode(p2.getX(), p2.getY(), xMin, yMin, xMax, yMax);

			if(c1 == INSIDE && c2 == INSIDE) return true; // both nodes inside - no need to calculate new radii

			while(c1 != INSIDE || c2 != INSIDE)
			{
				if ((c1 & c2) != INSIDE) return false; // both nodes outside and on the same side

				int c = c1 == INSIDE ? c2 : c1;

				if ((c & LEFT) != INSIDE)
				{
					qx = xMin;
					qy = (qx-p1.getX())*slope + p1.getY();
				}
				else if ((c & RIGHT) != INSIDE)
				{
					qx = xMax;
					qy = (qx-p1.getX())*slope + p1.getY();
				}
				else if ((c & BOTTOM) != INSIDE)
				{
					qy = yMin;
					qx = vertical ? p1.getX() : (qy-p1.getY())/slope + p1.getX();
				}
				else if ((c & TOP) != INSIDE)
				{
					qy = yMax;
					qx = vertical ? p1.getX() : (qy-p1.getY())/slope + p1.getX();
				}

				if (c == c1)
				{
					p1.setLocation(qx, qy);
					c1  = regionCode(p1.getX(), p1.getY(), xMin, yMin, xMax, yMax);
				}
				else
				{
					p2.setLocation(qx, qy);
					c2 = regionCode(p2.getX(), p2.getY(), xMin, yMin, xMax, yMax);
				}
			}

			try
			{

				Utils.log("newParent: " + p1.getX() + " " + p1.getY());
				Utils.log("newChild: " + p2.getX()  + " " + p2.getY());

				Point2D newParent = at.inverseTransform(p1, null);
				Point2D newChild = at.inverseTransform(p2, null);

				Utils.log("old radii: " + radiusParent + " " + radiusChild);
				// adjust radii
				//        	float parentRadius = parent.getData();
				//        	float childRadius = child.getData();
				if( this.radiusParent != this.radiusChild)
				{
					boolean parentGreater = radiusParent > radiusChild;

					double fullConeHeight = parentGreater ? length + (length*radiusChild) / (radiusParent - radiusChild) :  length + (length*radiusParent) / (radiusChild - radiusParent);

					if(!p1.equals(oldp1))
					{
						double h = (parentGreater ? p1.distance(oldp1) : p1.distance(oldp2)) * scale;
						radiusParent = (float) (parentGreater ? (radiusParent - radiusParent*h/fullConeHeight) : (radiusChild - radiusChild*h/fullConeHeight));
						Utils.log("cone heights p1: " + fullConeHeight + " " + h);
					}

					if(!p2.equals(oldp2))
					{
						double h = (parentGreater ? p2.distance(oldp1) : p2.distance(oldp2)) * scale;
						radiusChild = (float) (parentGreater ? (radiusParent - radiusParent*h/fullConeHeight) : (radiusChild - radiusChild*h/fullConeHeight));
						Utils.log("cone heights p2: " + fullConeHeight + " " + h);
					}

				}

				Utils.log("new radii: " + radiusParent + " " + radiusChild);

				this.parent = new RadiusNode((float) newParent.getX(), (float) newParent.getY(), layer, radiusParent / scale);
				this.child = new RadiusNode((float) newChild.getX(), (float) newChild.getY(), layer, radiusChild / scale);

				calculate();
			}
			catch(NoninvertibleTransformException e)
			{
				e.printStackTrace();
			}

			return true;
		}

		private final int regionCode(double x, double y, double xMin, double yMin, double xMax, double yMax)
		{
			int code = x < xMin ? LEFT : x > xMax ? RIGHT : INSIDE;
			if (y < yMin) code |= BOTTOM;
			else if (y > yMax) code |= TOP;
			return code;
		}
	}
	
	class CalibrationPanel extends JPanel
	{
		public Map<Integer, JTextField> textFields;
		public Map<Integer, JComboBox> comboBoxes;
 	
		public CalibrationPanel(List<Integer> zValues, Map<Integer, ImagePlusCalibrationInfo> myAllCalibInfos)
		{
			this.textFields = new HashMap<Integer, JTextField>();
			this.comboBoxes = new HashMap<Integer, JComboBox>();
			
//			this.setLayout(new GridLayout(0, 4, 5, 0));
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			String[] unitChoices = {"inch", "mm", "DPI"};
			
			for(int i: zValues )
			{
				RhizoStatistics.ImagePlusCalibrationInfo calibInfo = myAllCalibInfos.get(i);
				JLabel label = new JLabel("layer " + i + (calibInfo.isValid() ? " (valid) - " : " (invalid) - ") + calibInfo.getImageName() + ": ");
				JTextField pixelSizeTf = new JTextField(Double.toString(calibInfo.getPixelWidth()));
				JComboBox<String> comboBox = new JComboBox<String>(unitChoices);

				JPanel flowPanel = new JPanel(new FlowLayout());
				flowPanel.add(label);
				flowPanel.add(new JLabel("pixelsize"));
				flowPanel.add(pixelSizeTf);
				flowPanel.add(comboBox);
				this.add(flowPanel);
				
				textFields.put(i, pixelSizeTf);
				comboBoxes.put(i, comboBox);
			}
		}
		
		public Map<Integer, JTextField> getTextFields()
		{
			return this.textFields;
		}
		
		public Map<Integer, JComboBox> getComboBoxes()
		{
			return this.comboBoxes;
		}
	}
}
