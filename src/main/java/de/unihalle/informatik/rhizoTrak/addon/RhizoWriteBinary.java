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

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.MiToBo.apps.minirhizotron.utils.Drawing;
import de.unihalle.informatik.MiToBo.core.datatypes.images.MTBImage;
import de.unihalle.informatik.MiToBo.io.images.ImageWriterMTB;
import de.unihalle.informatik.MiToBo.io.tools.FilePathManipulator;
import de.unihalle.informatik.MiToBo.tools.image.ImageConverter;
import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.*;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;
import org.python.core.imp;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/** Write the annotaition as a binary image
 *
 */
public class RhizoWriteBinary {
	private final String ONLY_STRING = "Current layer only";
	private final String ALL_STRING = "All layers";

	final int whiteColor = 0x0000ff;

	private RhizoMain rhizoMain;

	private boolean debug = false;

	// Hash of all layers with treeline(s) to write to
	// set in computeStatistics
	private HashSet<Layer> allLayers = null;
	private MTBImage image;

	public RhizoWriteBinary(RhizoMain rhizoMain) 	{
		this.rhizoMain = rhizoMain;
	}

	/**
	 *
	 */
	public void writeBinary()
	{
		String[] choices2 = {ALL_STRING, ONLY_STRING};

		/** list of status labels (as integers) which are to be drawn
		 *
		 */
		ArrayList<Byte> statusLabelIntToWrite = new ArrayList<Byte>();

		JPanel choicesPanel = new JPanel();
		choicesPanel.setLayout(new GridLayout( 2, 1, 0, 10));

//		 only current layer at the moment
//		choicesPanel.add(new JLabel("Layers "));
//		JComboBox<String> layerCombo = new JComboBox<String>(choices2);
//		choicesPanel.add( layerCombo);

		choicesPanel.add(new JLabel("Status labels "));
		String[] statusLabelChoices = new String[rhizoMain.getProjectConfig().sizeStatusLabelMapping()];
		for ( int i = 0 ; i < rhizoMain.getProjectConfig().sizeStatusLabelMapping() ; i++ ) {
			statusLabelChoices[i] = rhizoMain.getProjectConfig().getStatusLabel(i).getName();
		}
		JList statusLabelCombo = new JList( statusLabelChoices);
		statusLabelCombo.setSelectedIndex( 0);
		choicesPanel.add( statusLabelCombo);

		int result = JOptionPane.showConfirmDialog(null, choicesPanel, "csv Output Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(result != JOptionPane.OK_OPTION) {
			return;
		}

		for ( int i : statusLabelCombo.getSelectedIndices() ) {
			statusLabelIntToWrite.add( (byte) i);
		}

//		 only current layer at the moment
//		boolean drawAnnotationsAllLayers = outputLayers.equals( ALL_STRING);
		boolean drawAnnotationsAllLayers = false;

		// Select and open and output file
		String basefilename = rhizoMain.getXmlName().replaceFirst(".xml\\z", "");

		String folder;
		if  ( rhizoMain.getStorageFolder() == null )
			folder = System.getProperty("user.home");
		else
			folder = rhizoMain.getStorageFolder();

		Path imagePath = null;
		if ( drawAnnotationsAllLayers ) {
			// will not hapen currently
			return;
		} else {
			// create a suggestion for the image filename
			imagePath = Paths.get( Display.getFront().getLayer().getPatches( false).get(0).getImageFilePath());
			String basename = FilePathManipulator.removeExtension( imagePath.toString());
			String extension = FilePathManipulator.getExtension(  imagePath.toString());
			String filenameProposal = basename+"-binary."+extension;

			// ask for the image name
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter( "File for writing annotations a binary image", "tif");
			fileChooser.setFileFilter(filter);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle("File to write binary annotation to");
			fileChooser.setSelectedFile(new File( filenameProposal));
			int returnVal = fileChooser.showOpenDialog(null);

			if (returnVal != JFileChooser.APPROVE_OPTION)
				return; // user cancelled dialog

			File saveFile = fileChooser.getSelectedFile();
			imagePath = saveFile.toPath();
		}

		ImagePlus ip = Display.getFront().getLayer().getPatches(true).get(0).getImagePlus();
		System.out.println( "IP " + ip.getCalibration());
		MTBImage image = MTBImage.createMTBImage(ip);
		System.out.println( "image " + image.getCalibration());

		// need to get calibration info and so on
//		MTBImage binaryImage = MTBImage.createMTBImage( image.getSizeX(), image.getSizeY(), image.getSizeZ(), image.getSizeT(), image.getSizeC(), MTBImage.MTBImageType.MTB_BYTE);

		MTBImage binaryImage = null;

		try {
			ImageConverter imgConv = new ImageConverter( image, MTBImage.MTBImageType.MTB_BYTE, false, false);
			imgConv.runOp( true);
			binaryImage = imgConv.getResultImg();
			System.out.println( "binaryImage " + binaryImage.getCalibration().info);

		} catch (ALDOperatorException e) {
			e.printStackTrace();
		} catch (ALDProcessingDAGException e) {
			e.printStackTrace();
		}
		ImagePlus imp = binaryImage.getImagePlus();

		if ( drawAnnotationsAllLayers ) {
			// should not happen currently
		} else {
			drawSegments( Display.getFront().getProject(), Display.getFront().getLayer(), imp, statusLabelIntToWrite);
		}

		try {
			ImageWriterMTB writer = new ImageWriterMTB(binaryImage, imagePath.toString());
			writer.runOp( true);
		} catch (final ALDOperatorException ex) {
			Utils.showMessage( "cannot write binary annotation image to  " + imagePath.toString());
			ex.printStackTrace();
		}		 catch (final ALDProcessingDAGException ex) {
			Utils.showMessage("cannot write binary annotation image to  " + imagePath.toString());
			ex.printStackTrace();
		}
	}

	/**
	 * @param project
	 * @param currentLayer if null all layers are considered*
	 * @param imp image processor of the binary image
	 * @return list of all <code>Segment</code>s to write, null on failure
	 */
	private void drawSegments(Project project, Layer currentLayer, ImagePlus imp, ArrayList<Byte> statusLabelIntToWrite) {
		// all treelines below a rootstack
		LinkedList<Treeline> allTreelines = new LinkedList<Treeline>();

		// find all rootstacks
		HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks( project);
		if ( rootstackThings == null) {
			Utils.showMessage( "WriteStatistics warning: no rootstack found");
			return;
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
				}
			}
		}

		// traverse alls treelines and draw segments
		for ( Treeline tl : allTreelines)  {
			if ( debug)	System.out.println( "segment to write " + tl.getId());

			if ( tl.getRoot() != null) {
				int segmentID = 1;
				Collection<Node<Float>> allNodes = tl.getRoot().getSubtreeNodes();

				for(Node<Float> node : allNodes) {
					if( !node.equals(tl.getRoot()) && statusLabelIntToWrite.contains( node.getConfidence())) {
						if ( debug)	{
							System.out.println( "    create segment for node " + node.getConfidence() +
									" patch " + RhizoAddons.getPatch(tl));
						}

						AffineTransform at = tl.getAffineTransform();
						Point2D p1 = at.transform(new Point2D.Float(node.getParent().getX(), node.getParent().getY()), null);
						Point2D p2 = at.transform(new Point2D.Float(node.getX(), node.getY()), null);


						int cx1 = (int) p1.getX();
						int cy1 = (int) p1.getY();

						int cx2 = (int) p2.getX();
						int cy2 = (int) p2.getY();

						float startRadius = node.getParent().getData();
						float endRadius = node.getData();

						Drawing.drawSegment(imp, cx1, cy1, cx2, cy2, (int)startRadius, (int)endRadius, whiteColor);
						Drawing.drawFilledCircle(imp, cx1, cy1, startRadius, whiteColor);
						Drawing.drawFilledCircle(imp, cx2, cy2, endRadius, whiteColor);

						imp.updateAndDraw();
					}
				}
			}

			if ( debug)	System.out.println( "created segments");
		}

		return ;
	}
}
