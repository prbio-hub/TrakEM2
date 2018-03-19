package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.addonGui.VisibilityPanel;

public class RhizoColVis
{

	private JFrame colorFrame;
	
	private RhizoMain rhizoMain;
	
	// ######
	private Color highlightColor1 = Color.MAGENTA;
	private Color highlightColor2 = Color.PINK;

	public RhizoColVis(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
	}

	/**
	 * Opens the color and visibility panel
	 * @author Axel
	 */
	public void createColVisFrame()
	{
		colorFrame = new JFrame("Color & Visibility");
		
		colorFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JPanel temp = new VisibilityPanel(rhizoMain);
		//JPanel temp = guiAddons.visibilityPanel();
		colorFrame.add(temp);
		colorFrame.setSize(320, 450);
//		colorFrame.pack();
		colorFrame.setVisible(true);
	}
	

	

	/**
	 * Updates the color for all treelines and repaints them.
	 * @author Axel
	 */
	public void applyCorrespondingColor() 
	{
		Display display = Display.getFront();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();

		// get treelines of current layerset
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
                ArrayList<Displayable> connectors = currentLayerSet.get(Connector.class);
                trees.addAll(connectors);
		boolean repaint = false;
		for (Displayable cObj : trees) 
		{			
			Treeline ctree = (Treeline) cObj;
			if(ctree.getRoot() == null || ctree.getRoot().getSubtreeNodes() == null) continue;
			
			for (Node<Float> cnode : ctree.getRoot().getSubtreeNodes())
			{
				byte currentConfi = cnode.getConfidence();
				// ################
//				Color newColor = rhizoMain.getRhizoIO().getColorFromStatusMap(currentConfi);
				Color newColor = rhizoMain.getProjectConfig().getColorForStatus(currentConfi);


				if (cnode.getColor()==null || !cnode.getColor().equals(newColor))
				{
					cnode.setColor(newColor);
					repaint = true;
				}
			}			
		}
		if(repaint)
		{
			Display.repaint(currentLayer);		
		}
	}
	
	/**
	 * 
	 * @param toBeHigh - Treeline to be highlighted
	 * @author Axel
	 */
	public static void highlight(Displayable toBeHigh, boolean choose)
	{
		if(toBeHigh instanceof Treeline)
		{
			Treeline tree = (Treeline) toBeHigh;
			if(tree.getRoot()==null)
			{
				return;
			}
			for (Node<Float> cnode : tree.getRoot().getSubtreeNodes())
			{
				if(choose){
					cnode.chooseHighlight();
				} else {
					cnode.highlight();
				}	
			}
			Display.repaint(((Treeline) toBeHigh).getFirstLayer());
		}
	}

	/**
	 * 
	 * @param toBeHigh - Treeline to be highlighted
	 * @author Axel
	 */
	public static void highlight(List<Displayable> toBeHigh, boolean choose)
	{		
		for (Displayable disp : toBeHigh)
		{
			highlight(disp,choose);
		}
	}

	/**
	 * Removes highlighting from treeline
	 * @param notToBeHigh - Treeline to be dehighlighted
	 * @author Axel
	 */
    public static void removeHighlight(Displayable notToBeHigh, boolean choose)
    {
        Treeline tree=null;
        if (notToBeHigh instanceof Treeline) {
            tree = (Treeline) notToBeHigh;
        }
        if (notToBeHigh instanceof Connector) {
            tree = (Treeline) notToBeHigh;
        }
        if (tree==null) return;
        if (tree.getRoot() == null) {
            return;
        }
        for (Node<Float> cnode : tree.getRoot().getSubtreeNodes()) {
            if (choose) {
                cnode.removeChooseHighlight();
            } else {
                cnode.removeHighlight();
            }
        }
        Display.repaint(((Treeline) notToBeHigh).getFirstLayer());

    }

	/**
	 * Removes highlighting from treelines
	 * @param notToBeHigh - Treelines to be dehighlighted
	 * @author Axel
	 */
	public static void removeHighlight(List<Displayable> toBeHigh,boolean choose)
	{
		for (Displayable disp : toBeHigh)
		{
			if (disp.getClass().equals(Treeline.class) || disp.getClass().equals(Connector.class))
			{
				removeHighlight((Treeline) disp,choose);
			}
		}
	}

	
    
    /**
     * Used for disposing JFrames when closing the control window
     * @return The color and visbility JFrame
     */
    public JFrame getColorVisibilityFrame()
    {
    	return colorFrame;
    }
    
     /**
     * call to dispose the ColorVisibilityFrame
     */
    public void disposeColorVisibilityFrame()
    {
        if(colorFrame==null) return;
        
        // ######################
//        rhizoMain.getRhizoIO().clearColorVisibilityLists();
    	colorFrame.dispose();
    }

	/**
	 * @return the highlightColor1
	 */
	public Color getHighlightColor1() {
		return highlightColor1;
	}

	/**
	 * @param highlightColor1 the highlightColor1 to set
	 */
	public void setHighlightColor1(Color highlightColor1) {
		this.highlightColor1 = highlightColor1;
	}

	/**
	 * @return the highlightColor2
	 */
	public Color getHighlightColor2() {
		return highlightColor2;
	}

	/**
	 * @param highlightColor2 the highlightColor2 to set
	 */
	public void setHighlightColor2(Color highlightColor2) {
		this.highlightColor2 = highlightColor2;
	}
}
