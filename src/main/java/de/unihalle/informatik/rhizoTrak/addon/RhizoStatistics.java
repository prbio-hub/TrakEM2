package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.GridLayout;
import java.awt.geom.AffineTransform;
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
	
	/**
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
						Segment currentSegment = new Segment(rhizoMain.getRhizoIO(), RhizoAddons.getPatch(ctree), ctree, cObj.getId(), segmentID, (RadiusNode) node, (RadiusNode) node.getParent(), unit, (int) node.getConfidence());
						segmentID++;

						if(currentSegment.checkNodesInImage()) allSegments.add(currentSegment);
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
					Segment currentSegment = new Segment(rhizoMain.getRhizoIO(), RhizoAddons.getPatch(tl), tl, tl.getId(), segmentID, (RadiusNode) node, (RadiusNode) node.getParent(), unit, (int) node.getConfidence());
					segmentID++;

					if(currentSegment.checkNodesInImage()) allSegments.add(currentSegment);
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
	
	private double scale;
	private final double inchToMM = 25.4;
	
	private final double minRadius = 1;
	private double r1 = minRadius;
	private double r2 = minRadius;
	
	private Patch p;
	
	private RhizoIO r;

	// TODO: add warning that if no images are present the unit will be pixel
	public Segment(RhizoIO r, Patch p, Treeline t, long treeID, int segmentID, RadiusNode child, RadiusNode parent, String unit, int status)
	{
		this.p = p;
		this.t = t;
		
		if(null == p) this.scale = 1;
		else if(unit.equals("inch")) this.scale = p.getImagePlus().getCalibration().pixelWidth;
		else if(unit.equals("mm")) this.scale = p.getImagePlus().getCalibration().pixelWidth * inchToMM;
		else this.scale = 1;
		
		this.child = child;
		this.parent = parent;
		this.layer = child.getLayer();
		
		if(parent.getData() > 0) this.r1 = parent.getData() * scale;
		if(child.getData() > 0) this.r2 = child.getData() * scale;
		
		if(null != p) this.imageName = p.getImagePlus().getTitle();
		else this.imageName = "";
		
		parseImageName(imageName);
		
		this.treeID = treeID;
		this.segmentID = segmentID;
		this.status = status;
		this.r = r;
		
		calculate();
	}
	
	private void calculate()
	{
		this.length = Math.sqrt(Math.pow(child.getX() - parent.getX(), 2) + Math.pow(child.getY() - parent.getY(), 2)) * scale;
		this.avgRadius = (r1 + r2) / 2;
		double s = Math.sqrt(Math.pow((r1 - r2), 2) + Math.pow(this.length, 2));
		this.surfaceArea = (Math.PI * Math.pow(r1, 2) + Math.PI * Math.pow(r2, 2) + Math.PI * s * (r1 + r2));
		this.volume = ((Math.PI * length * (Math.pow(r1, 2) + Math.pow(r1, 2) + r1 * r2)) / 3);
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
				sep + Double.toString(volume) + sep + Integer.toString(numberOfChildren) + sep + status + sep + r.getStatusMap().get(status).getFullName();

		return result;
	}
	
	public boolean checkNodesInImage()
	{
		// no image
		if(null == p) return true;

		ImagePlus image = p.getImagePlus();
		
		AffineTransform at = t.getAffineTransform();
		Point2D p1 = at.transform(new Point2D.Float(parent.getX(), parent.getY()), null);
		Point2D p2 = at.transform(new Point2D.Float(child.getX(), child.getY()), null);
		
		Utils.log(p1.getX() + " " + p1.getY() + " " + p2.getX() + " " + p2.getY() + "\t" + image.getWidth() + " " + image.getHeight());
		
		// both nodes are inside
		if(p1.getX() < image.getWidth() && p1.getY() < image.getHeight() && p2.getX() < image.getWidth() && p2.getY() < image.getHeight()) return true;
		// both nodes are outside
		else if((p1.getX() > image.getWidth() || p1.getY() > image.getHeight()) && (p2.getX() > image.getWidth() || p2.getY() > image.getHeight())) return false; 
		// parent node is outside, child is inside
		else if((p1.getX() > image.getWidth() || p1.getY() > image.getHeight()) && (p2.getX() < image.getWidth() || p2.getY() < image.getHeight()))
		{
			double m = (p1.getY() - p2.getY())/(p1.getX() - p2.getX());
			double b = p1.getY() + m*p1.getX();
			
			if(p1.getX() > image.getWidth())
			{
				double newY = m*image.getWidth() + b;
				parent = new RadiusNode(image.getWidth(), (float) newY, layer, 1.0f);
				calculate();
				return true;
			}			
			if(p1.getY() > image.getHeight())
			{
				double newX = (image.getHeight() - b) / m;
				parent = new RadiusNode((float) newX, image.getHeight(), layer, 1.0f);
				calculate();
				return true;
			}
		}
		// child node is outside, parent is inside
		else if((p1.getX() < image.getWidth() || p1.getY() < image.getHeight()) && (p2.getX() > image.getWidth() || p2.getY() > image.getHeight()))
		{
			double m = (p1.getY() - p2.getY())/(p1.getX() - p2.getX());
			double b = p1.getY() + m*p1.getX();
			
			if(p2.getX() > image.getWidth())
			{
				double newY = m*image.getWidth() + b;
				parent = new RadiusNode(image.getWidth(), (float) newY, layer, 1.0f);
				calculate();
				return true;
			}			
			if(p2.getY() > image.getHeight())
			{
				double newX = (image.getHeight() - b) / m;
				parent = new RadiusNode((float) newX, image.getHeight(), layer, 1.0f);
				calculate();
				return true;
			}
		}

	
		return false;
	}
}