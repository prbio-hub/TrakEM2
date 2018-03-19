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
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;

public class RhizoStatistics
{

	private RhizoMain rhizoMain;

	public RhizoStatistics(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
	}
	
	/**o
	 * TODO return/error messages
	 */
	public void writeStatistics()
	{
		String[] choices1 = {"{Tab}" , "{;}", "{,}", "Space"};
		String[] choices1_ = {"\t", ";", ",", " "};
		String[] choices2 = {"Current layer only", "All layers"};
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

		String sep = "";
		String outputType = "";
		String unit = "";
		if(result == JOptionPane.OK_OPTION)
		{
			sep = choices1_[Arrays.asList(choices1).indexOf(combo1.getSelectedItem())];
			outputType = (String) combo2.getSelectedItem();
			unit = (String) combo3.getSelectedItem();
			
			if(sep.equals("") || outputType.equals("") || unit.equals("")) return;
		}
		else return;

		Display display = Display.getFront();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();

		List<Displayable> processedTreelines = new ArrayList<Displayable>();
		List<Displayable> trees = null;
		List<Segment> allSegments = new ArrayList<Segment>();		

		if(outputType.equals("All layers")) trees = currentLayerSet.get(Treeline.class);
		else trees = RhizoAddons.filterTreelinesByLayer(currentLayer, currentLayerSet.get(Treeline.class));

		List<Displayable> connectors = currentLayerSet.get(Connector.class);

		for(Displayable cObj: connectors)
		{
			Connector c = (Connector) cObj;

			List<Treeline> treelines = c.getConTreelines();

			for(Treeline ctree: treelines)
			{
				if(null == ctree || null == ctree.getRoot()) continue; // empty treelines
				if(processedTreelines.contains(ctree)) continue; // already processed treelines
				if(!trees.contains(ctree)) continue; // when current layer only is selected
				
				trees.remove(ctree);

				int segmentID = 1;
				Collection<Node<Float>> allNodes = ctree.getRoot().getSubtreeNodes();

				for(Node<Float> node : allNodes)
				{
					if(!node.equals(ctree.getRoot()))
					{
						Segment currentSegment = new Segment(rhizoMain, RhizoAddons.getPatch(ctree), ctree, cObj.getId(), segmentID, (RadiusNode) node, (RadiusNode) node.getParent(), unit, (int) node.getConfidence());
						segmentID++;

						if(currentSegment.cohenSutherlandLineClipping()) allSegments.add(currentSegment);
					}
				}
				
				processedTreelines.add(ctree);
			}
		}

		Utils.log(trees.size());

		for(Displayable t: trees)
		{
			Treeline tl = (Treeline) t;
			if(processedTreelines.contains(tl)) continue;
			
			int segmentID = 1;
			Collection<Node<Float>> allNodes = tl.getRoot().getSubtreeNodes();

			for(Node<Float> node : allNodes)
			{
				if(!node.equals(tl.getRoot()))
				{
					Segment currentSegment = new Segment(rhizoMain, RhizoAddons.getPatch(tl), tl, tl.getId(), segmentID, (RadiusNode) node, (RadiusNode) node.getParent(), unit, (int) node.getConfidence());
					segmentID++;

					if(currentSegment.cohenSutherlandLineClipping()) allSegments.add(currentSegment);
				}
			}
			
			processedTreelines.add(tl);
		}

		// write
		try
		{
			File saveFile = Utils.chooseFile(System.getProperty("user.home"), null, ".csv");
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));

			bw.write("experiment"+sep+"tube"+sep+"timepoint"+sep+"rootID"+sep+"segmentID"+sep+"layer"+sep+"length"+sep+"avgRadius"+sep+"surfaceArea"+sep+"volume"+sep+"children"+sep
					+"status"+sep+"statusName"+"\n");
			for (Segment segment : allSegments)
			{
				bw.write(segment.getStatistics(sep));
				bw.newLine();
			}
			bw.close();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}




/**
 * Segment class for writing statistics.
 * @author Axel, Tino
 *
 */
class Segment
{
	// used for clipping
	private final int INSIDE = 0;
	private final int LEFT   = 1;
	private final int RIGHT  = 2;
	private final int BOTTOM = 4;
	private final int TOP    = 8;


	private RadiusNode child;
	private RadiusNode parent;
	
	private Layer layer;
	private Treeline t;
	
	// infos
	private String imageName, experiment, tube, timepoint;
	private double length, avgRadius, surfaceArea, volume;
	private int segmentID, numberOfChildren;
	
	private int status;

	private long treeID;
	
	private float scale;
	private final double inchToMM = 25.4;
	
	private final float minRadius = 0.5f;
	private float radiusParent = minRadius;
	private float radiusChild = minRadius;
	
	private Patch p;
	
	private RhizoMain rhizoMain;

	// TODO: add warning that if no images are present the unit will be pixel
	public Segment(RhizoMain rhizoMain, Patch p, Treeline t, long treeID, int segmentID, RadiusNode child, RadiusNode parent, String unit, int status)
	{
		this.p = p;
		this.t = t;
		
		if(null == p) this.scale = 1;
		else if(unit.equals("inch")) this.scale = (float) p.getImagePlus().getCalibration().pixelWidth;
		else if(unit.equals("mm")) this.scale = (float) (p.getImagePlus().getCalibration().pixelWidth * inchToMM);
		else this.scale = 1;
		
		this.child = child;
		this.parent = parent;
		this.layer = child.getLayer();
		
		if(parent.getData() > minRadius) this.radiusParent = parent.getData() * scale;
		if(child.getData() > minRadius) this.radiusChild = child.getData() * scale;
		
		if(null != p) this.imageName = p.getImagePlus().getTitle();
		else this.imageName = "";
		
		parseImageName(imageName);
		
		this.treeID = treeID;
		this.segmentID = segmentID;
		this.status = status;
		this.rhizoMain = rhizoMain;
		
		calculate();
	}
	
	private void calculate()
	{
		this.length = Math.sqrt(Math.pow(parent.getX() - child.getX(), 2) + Math.pow(parent.getY() - child.getY(), 2)) * scale;
		this.avgRadius = (radiusParent + radiusChild) / 2;
		double s = Math.sqrt(Math.pow((radiusParent - radiusChild), 2) + Math.pow(this.length, 2));
		this.surfaceArea = (Math.PI * Math.pow(radiusParent, 2) + Math.PI * Math.pow(radiusChild, 2) + Math.PI * s * (radiusParent + radiusChild));
		this.volume = ((Math.PI * length * (Math.pow(radiusParent, 2) + Math.pow(radiusParent, 2) + radiusParent * radiusChild)) / 3);
		this.numberOfChildren = child.getChildrenCount();
	}
	
	private void parseImageName(String name)
	{
		if(name.equals(""))
		{
			experiment = "NA";
			tube = "NA";
			timepoint ="NA";
			return;
		}
		
		String[] split = name.split("_");
		if(split.length < 6)
		{
			experiment = "NA";
			tube = "NA";
			timepoint ="NA";
			return;
		}
		
		experiment = split[0];
		tube = split[1];
		timepoint = split[5];
	}

	public String getStatistics(String sep)
	{
		String result = experiment + sep + tube + sep + timepoint + sep + Long.toString(treeID) + sep + Integer.toString(segmentID) + sep + Integer.toString((int) layer.getZ() + 1)  +
				sep + Double.toString(length) + sep + Double.toString(avgRadius) + sep + Double.toString(surfaceArea) +
// #########
				//				sep + Double.toString(volume) + sep + Integer.toString(numberOfChildren) + sep + status + sep + r.getStatusMap().get(status).getFullName();
		        sep + Double.toString(volume) + sep + Integer.toString(numberOfChildren) + sep + status + sep + 
		        rhizoMain.getProjectConfig().getStatusLabel(status).getName();

		return result;
	}

	
	
	/**
	 * Cohen-Sutherland line clipping algorithm.
	 * @return true - if both nodes are inside the image or if at least one node is outside and the line is clipped with the image
	 * 		  false - if both nodes are outside and the line does not intersect with the image
	 */
	public boolean cohenSutherlandLineClipping()
	{
		ImagePlus image = p.getImagePlus();
		double xMin = 0;
		double yMin = 0;
		double xMax = image.getWidth();
		double yMax = image.getHeight();

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
        	
        	// adjust radii
//        	float parentRadius = parent.getData();
//        	float childRadius = child.getData();
        	if( this.radiusParent != this.radiusChild)
        	{
        		boolean parentGreater = radiusParent > radiusChild;
        		
        		double fullConeHeight = parentGreater ? length + (length*radiusChild) / (radiusParent - radiusChild) :  length + (length*radiusParent) / (radiusChild - radiusParent);
        		
        		if(!p1.equals(oldp1))
        		{
        			double h = parentGreater ? p1.distance(oldp1) : p1.distance(oldp2);
        			radiusParent = (float) (parentGreater ? (radiusParent - radiusParent*h/fullConeHeight) : (radiusChild - radiusChild*h/fullConeHeight));
        			Utils.log("cone heights p1: " + fullConeHeight + " " + h);
        		}
        		
        		if(!p2.equals(oldp2))
        		{
        			double h = parentGreater ? p2.distance(oldp1) : p2.distance(oldp2);
        			radiusChild = (float) (parentGreater ? (radiusParent - radiusParent*h/fullConeHeight) : (radiusChild - radiusChild*h/fullConeHeight));
        			Utils.log("cone heights p2: " + fullConeHeight + " " + h);
        		}
        		
        	}
        	
        	Utils.log("old radii: " + radiusParent + " " + radiusChild);
        	Utils.log("new radii: " + radiusParent + " " + radiusChild);
        	
        	this.radiusParent = radiusParent;
        	this.radiusChild = radiusChild;
        	
        	this.parent = new RadiusNode((float) newParent.getX(), (float) newParent.getY(), layer, radiusParent);
        	this.child = new RadiusNode((float) newChild.getX(), (float) newChild.getY(), layer, radiusChild);
        	
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