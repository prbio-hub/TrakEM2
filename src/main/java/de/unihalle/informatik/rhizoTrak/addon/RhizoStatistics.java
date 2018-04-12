package de.unihalle.informatik.rhizoTrak.addon;

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

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
	
	
	private final String ONLY_STRING = "Current layer only";
	private final String ALL_STRING = "All layers";
	private RhizoMain rhizoMain;
	
	private boolean debug = true;
	
	private String outputUnit; 
	
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
		
		JComboBox<String> combo1 = new JComboBox<String>(choices1);
		JComboBox<String> combo2 = new JComboBox<String>(choices2);
		JComboBox<String> combo3 = new JComboBox<String>(choices3);
		
		JPanel statChoicesPanel = new JPanel();
		statChoicesPanel.setLayout(new GridLayout(3, 2, 0, 10));
		statChoicesPanel.add(new JLabel("Separator "));
		statChoicesPanel.add(combo1);
		statChoicesPanel.add(new JLabel("Output type "));
		statChoicesPanel.add(combo2);
		statChoicesPanel.add(new JLabel("Unit "));
		statChoicesPanel.add(combo3);

		int result = JOptionPane.showConfirmDialog(null, statChoicesPanel, "Statistics Output Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(result != JOptionPane.OK_OPTION) {
			return;
		}
		
		String sep = "";
		String outputType = "";
		this.outputUnit = "";

		sep = choices1_[Arrays.asList(choices1).indexOf(combo1.getSelectedItem())];
		outputType = (String) combo2.getSelectedItem();
		this.outputUnit = (String) combo3.getSelectedItem();

		if(sep.equals("") || outputType.equals("") || this.outputUnit.equals("")) {
			Utils.showMessage( "rhizoTrak", "illegal choice of options for Write Statistics");
			return;
		}
		
		// compile all segments to write
		boolean allLayers = outputType.equals( ALL_STRING);
		List<Segment> allSegments;
		if ( allLayers ) {
			allSegments = computeStatistics( Display.getFront().getProject(), null);
		} else {
			allSegments = computeStatistics( Display.getFront().getProject(), Display.getFront().getLayer());
		}
		
		if ( allSegments == null) {
			return;
		}
		
		// write
		File saveFile = null;
		try {
			String basefilename = rhizoMain.getXmlName().replaceFirst(".xml\\z", "");
			
			String folder;
			if  ( rhizoMain.getStorageFolder() == null )
				folder = System.getProperty("user.home");
			else 
				folder = rhizoMain.getStorageFolder();
			
			saveFile = Utils.chooseFile( folder, basefilename, ".csv");		
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));

			// write header
			bw.write("experiment" + sep + "tube" + sep + "timepoint" + sep + "rootID" + sep + "layerID" +sep + "segmentID" +  
					sep + "length_" + this.outputUnit + sep + "startDiameter_" + this.outputUnit + sep + "endDiameter_" + this.outputUnit +
					sep + "surfaceArea_" + this.outputUnit + "^2" + sep + "volume_" + this.outputUnit + "^3" + sep + "children" + sep + "status" + sep + "statusName" + "\n");
			for (Segment segment : allSegments) {
				bw.write(segment.getStatistics(sep));
				bw.newLine();
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
		HashSet<Layer> allLayers = new HashSet<Layer>(); // all layers we hat a treeline in to write
		
		for ( ProjectThing rootstackThing :rootstackThings ) {
			if ( debug)	System.out.println("rootstack " + rootstackThing.getId());
			for ( ProjectThing pt : rootstackThing.findChildrenOfTypeR( Treeline.class)) {
				// we also find connectors!
				Treeline tl = (Treeline)pt.getObject();
				if ( debug)	System.out.println( "    treeline " + tl.getId());

				if ( tl.getClass().equals( Treeline.class)) {
					if ( currentLayer == null || currentLayer.equals( tl.getFirstLayer())) {
						if ( debug)	System.out.println( "           as treeline");
						allTreelines.add(tl);
						allLayers.add( tl.getFirstLayer());
					}
				} else if ( tl.getClass().equals( Connector.class)) {
					if ( debug)	System.out.println( "           as conector");

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
							Segment currentSegment = new Segment(rhizoMain, tl, treelineID, 
									segmentID, (RadiusNode) node, (RadiusNode) node.getParent(), this.outputUnit, (int) node.getConfidence());
							segmentID++;

							if(currentSegment.cohenSutherlandLineClipping()) 
								allSegments.add(currentSegment);
						}
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
			msg.insert(0, "WARNING Write Statistics\n \n");
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

		for ( Layer layer : allLayers) {
			LayerSet layerSet = layer.getParent();
			List<Patch> patches = layerSet.getAll(Patch.class);

			boolean found = false;
			for(Patch patch: patches) 	{
				if(patch.getLayer().getZ() == layer.getZ()) {
					ImagePlus ip = patch.getImagePlus();
					if (  ip != null  ) {
						myAllCalibInfos.put( (int)layer.getZ()+1, new ImagePlusCalibrationInfo(ip));
						if ( debug ) System.out.println( "found layer " + (int)layer.getZ()+1);
						found = true;
						break;
					} 
				}
			}
			if ( ! found) {
				myAllCalibInfos.put( (int)layer.getZ()+1, new ImagePlusCalibrationInfo());
				if ( debug ) System.out.println( "found layer " + (int)layer.getZ()+1 + " without patch");

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

						Utils.showMessage( new String( msg));
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
		 * @param ip
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
				 return "NA";
		 }
		 
		 private String fmtd( int value) {
			 if ( value > 0 ) 
				 return String.format("%d", value);
			 else 
				 return "NA";
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
	}

	/**
	 * Segment class for writing statistics.
	 * @author Axel, Tino
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
		private String imageName, experiment, tube, timepoint;
		private double length, surfaceArea, volume;
		private int segmentID, numberOfChildren;

		private int status;

		private long treeID;

		private float scale = 1;
		private final float minRadius = 0.0f;
		private float radiusParent = minRadius;
		private float radiusChild = minRadius;

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
			this.layerIndex = (int) layer.getZ() + 1;

			ImagePlusCalibrationInfo calibInfo;
			if ( allCalibInfos == null ||
					(calibInfo = allCalibInfos.get( this.layerIndex)) == null ) {
				// ERROR: this should never happen
				System.err.println( "output Unit " + outputUnit + " but allCalibInfo == null");
				throw new ExceptionInInitializerError( "output Unit " + outputUnit + " but allCalibInfo == null");
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

			this.treeID = treeID;
			this.segmentID = segmentID;
			this.status = status;
			this.rhizoMain = rhizoMain;

			calculate();
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
			String result = experiment + sep + tube + sep + timepoint + sep + Long.toString(treeID) +
					sep + this.layerIndex  +
					sep + Integer.toString(segmentID) +
					sep + Double.toString(length) + sep + Double.toString(2*radiusParent) + sep + Double.toString(2*radiusChild) +
					sep + Double.toString(surfaceArea) + sep + Double.toString(volume) + 
					sep + Integer.toString(numberOfChildren) + sep + status + sep + rhizoMain.getProjectConfig().getStatusLabel(status).getName();

			return result;
		}

		/**
		 * Cohen-Sutherland line clipping algorithm. if imageplus is null (i.e. no image associated) no clipping is done.
		 * 
		 * @return true - if both nodes are inside the image or if at least one node is outside and the line is clipped with the image
		 * 		  false - if both nodes are outside and the line does not intersect with the image
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
			double xMax = calibInfo.ip.getWidth();
			double yMax = calibInfo.ip.getWidth();;

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
}
