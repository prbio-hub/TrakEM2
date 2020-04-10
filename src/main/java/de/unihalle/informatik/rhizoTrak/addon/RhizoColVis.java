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

package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.addonGui.PreferencesTabbedPane;

public class RhizoColVis
{

	private JFrame preferencesFrame;
	
	private PreferencesTabbedPane tabbedPane;
	
	private RhizoMain rhizoMain;
	
	// ######
//	private Color highlightColor1 = Color.MAGENTA;
//	private Color highlightColor2 = Color.PINK;

	public RhizoColVis(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
	}

	/**
	 * Opens the color and visibility panel
	 * @author Axel, Tino
	 */
	public void createPreferencesFrame()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if(null != preferencesFrame)
				{
					preferencesFrame.setVisible(true);
					preferencesFrame.toFront();
				}
				else
				{
					preferencesFrame = new JFrame("Preferences");
					
					preferencesFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
					tabbedPane = new PreferencesTabbedPane(rhizoMain);

					preferencesFrame.add(tabbedPane);
					preferencesFrame.setSize(420, 480);
					preferencesFrame.setVisible(true);
				}
			}
			
		});

	}
	
	public void setScalebarVisibility(boolean flag) {
		if (this.tabbedPane != null)
			this.tabbedPane.setScalebarVisibility(flag);
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
    	return preferencesFrame;
    }
    
     /**
     * call to dispose the ColorVisibilityFrame
     */
    public void disposeColorVisibilityFrame()
    {
        if(preferencesFrame==null) return;
    	preferencesFrame.dispose();
    }

}
