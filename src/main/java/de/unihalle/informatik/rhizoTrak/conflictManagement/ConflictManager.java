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

/* === original file header below (if any) === */

package de.unihalle.informatik.rhizoTrak.conflictManagement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Tree;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.addonGui.ConflictPanel;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class ConflictManager {
	
	//save conflicts and gui stuff
	
	private static HashMap<Treeline, ConnectorConflict> connectorConflictHash = new HashMap<Treeline, ConnectorConflict>();
	private static HashMap<TreelineConflictKey, TreelineConflict> treelineConflictHash = new HashMap<TreelineConflictKey, TreelineConflict>();
	
	private static ConflictPanel conflictPanel = null;
	private static JFrame	conflictFrame = null;
	
	private static boolean isSolving=false;
	/**
	 * @return the isSolving
	 */
	public static boolean isSolving() {
		return isSolving;
	}

	/**
	 * @param isSolving the isSolving to set
	 */
	public static void setSolving(boolean isSolving) {
		ConflictManager.isSolving = isSolving;
	}

	/**
	 * @return the currentSolvingConflict
	 */
	public static Conflict getCurrentSolvingConflict() {
		return currentSolvingConflict;
	}

	/**
	 * @param currentSolvingConflict the currentSolvingConflict to set
	 */
	public static void setCurrentSolvingConflict(Conflict currentSolvingConflict) {
		ConflictManager.currentSolvingConflict = currentSolvingConflict;
	}

	private static Conflict currentSolvingConflict=null;
	

	//check for conflicts
	
	public static void processChange(Treeline tree, Connector connector){
		addConnectorConflict(tree);
		if(tree.getFirstLayer()!=null)
		{
			addTreelineConflict(connector, tree.getFirstLayer());
		}
		if(conflictPanel!=null)
		{
			conflictPanel.updateList();	
		}
	}
	
	//auto resolve to minimize multiple connector conflicts if possible
	public static void autoResolveConnectorConnflicts(boolean aggressive)
	{
		if(!aggressive)
		{
			//so only solve cases if no new treeconflict will arise aka max one connected tree per layer
			
			ArrayList<ConnectorConflict> conflictsList = new ArrayList<ConnectorConflict>(connectorConflictHash.values());
			while(conflictsList.size()>0)
			{
				//as long as there are conflicts to solve
				ConnectorConflict currentConflict = conflictsList.get(0);
				ArrayList<Connector> currentConnectorList = currentConflict.getConnectorList();
				HashMap<Layer,Connector> conflictAtlas = new HashMap<Layer,Connector>();
				boolean isSolvable=true;
				for(Connector currentConnector: currentConnectorList)
				{
					//for every connector in the current conflict > try to find if there are more than max one connected tree
					ArrayList<Treeline> currentTreelineList = currentConnector.getConTreelines();
					for(Treeline currentTreeline: currentTreelineList)
					{
						Layer currentLayer = currentTreeline.getFirstLayer();
						//check if its really resolvable so the non max one is not the current conflict point and and its already taken and the connectors aren't in a subset situation
						if(conflictAtlas.containsKey(currentLayer) && currentLayer!=currentConflict.getConflictTree().getFirstLayer() && !conflictAtlas.get(currentLayer).getConTreelines().contains(currentTreeline))
						{
							isSolvable=false;
						}
						conflictAtlas.put(currentLayer, currentConnector);
					}
				}
				if(isSolvable)
				{
					//its solvable so we need to find the most upper connector as the future parent to be.
					Set<Layer> keySet =conflictAtlas.keySet();
					Layer first = null;
					for(Layer currentLayer: keySet)
					{
						if(first==null)
						{
							first = currentLayer;
						}
						if(currentLayer.getZ() < first.getZ())
						{
							first = currentLayer;
						}
					}
					
					Connector parent = conflictAtlas.get(first);
					for(Connector currentConnector: currentConnectorList)
					{
						//for every connector in the current conflict > port treelines to parent
						mergeConnector(parent, currentConnector);
						
					}
					//conflictsList = new ArrayList<ConnectorConflict>(connectorConflictHash.values());
				}
				if(conflictsList.size()>0)
				{
					conflictsList.remove(0);
				}		
			}		
		}
		else
		{
			//so the goal is to convert multiple connectorconflicts to treeconflicts
			
			ArrayList<ConnectorConflict> conflictsList = new ArrayList<ConnectorConflict>(connectorConflictHash.values());
			while(conflictsList.size()>0)
			{
				//as long as there are conflicts to solve
				ConnectorConflict currentConflict = conflictsList.get(0);
				ArrayList<Connector> currentConnectorList = currentConflict.getConnectorList();
				HashMap<Layer,Connector> conflictAtlas = new HashMap<Layer,Connector>();
				boolean isSolvable=true;
				for(Connector currentConnector: currentConnectorList)
				{
					//for every connector in the current conflict > try to find if there are more than max one connected tree
					ArrayList<Treeline> currentTreelineList = currentConnector.getConTreelines();
					for(Treeline currentTreeline: currentTreelineList)
					{
						Layer currentLayer = currentTreeline.getFirstLayer();
						conflictAtlas.put(currentLayer, currentConnector);
					}
				}
				if(isSolvable)
				{
					//its solvable so we need to find the most upper connector as the future parent to be.
					Set<Layer> keySet =conflictAtlas.keySet();
					Layer first = null;
					for(Layer currentLayer: keySet)
					{
						if(first==null)
						{
							first = currentLayer;
						}
						if(currentLayer.getZ() < first.getZ())
						{
							first = currentLayer;
						}
					}
					
					Connector parent = conflictAtlas.get(first);
					for(Connector currentConnector: currentConnectorList)
					{
						//for every connector in the current conflict > port treelines to parent
						mergeConnector(parent, currentConnector);
						
					}
					//conflictsList = new ArrayList<ConnectorConflict>(connectorConflictHash.values());
				}
				if(conflictsList.size()>0)
				{
					conflictsList.remove(0);
				}		
			}	
		}
	}

	
	//add conflicts
	
	public static void addConnectorConflict(Treeline tree)
	{
		//update the potential currentConflict
		ConnectorConflict currentConflict = connectorConflictHash.get(tree);
		if(currentConflict!=null)
		{
			currentConflict.update();
		}
		else
		{
			//open a potential Conflict
			currentConflict = new ConnectorConflict(tree);
			connectorConflictHash.put(tree,currentConflict);
			currentConflict.update(); //the update will insure if there is a real issue
		}
	}
	
	public static void addTreelineConflict(Connector connector,Layer layer)
	{
		TreelineConflictKey treeConKey = new TreelineConflictKey(connector, layer);
		Set<TreelineConflictKey> keys  = treelineConflictHash.keySet();
		for (TreelineConflictKey treelineConflictKey : keys) {
			if(treelineConflictKey.getConnector().equals(connector) && treelineConflictKey.getLayer().equals(layer))
			{
				treeConKey=treelineConflictKey;
			}
		}
		
		//update the potential currentConflict
		TreelineConflict currentConflict = treelineConflictHash.get(treeConKey);
		if(currentConflict!=null)
		{
			currentConflict.update();
		}
		else
		{
			//open a potential Conflict
			currentConflict = new TreelineConflict(treeConKey);
			treelineConflictHash.put(currentConflict.getTreeConKey(),currentConflict);
			currentConflict.update(); //the update will insure if there is a real issue
		}
	}
	
	//remove conflicts
		
	public static void  removeConnectorConflict(Treeline treeline){
		ConnectorConflict currentConflict = connectorConflictHash.get(treeline);
		
		if(currentConflict!=null && currentConflict.equals(ConflictManager.getCurrentSolvingConflict()))
		{
			ConflictManager.abortCurrentSolving();
		}
		
		connectorConflictHash.remove(treeline);
	}
	
	public static void  removeTreelineConflict(TreelineConflictKey treeConKey){
		TreelineConflict currentConflict = treelineConflictHash.get(treeConKey);
		
		if(currentConflict!=null && currentConflict.equals(ConflictManager.getCurrentSolvingConflict()))
		{
			ConflictManager.abortCurrentSolving();
		}
		
		treelineConflictHash.remove(treeConKey);
	}
	
	//restore conflicts
	public static void restorConflicts()
	{
		clear();
		for(Project project: Project.getProjects()){
			
			LayerSet currentLayerSet = project.getRootLayerSet();
			
			ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);

			for (Displayable cObj : trees)
			{
				Treeline ctree = (Treeline) cObj;
				addConnectorConflict(ctree);
			}
			
			ArrayList<Displayable> connectors = currentLayerSet.get(Connector.class);

			for (Displayable cObj : connectors)
			{
				Connector cconnector = (Connector) cObj;
				for(Layer layer: currentLayerSet.getLayers())
				{
					addTreelineConflict(cconnector, layer);	
				}
			}
		}
	}
	
	public static void clearAndUpdate()
	{
		connectorConflictHash.clear();
		connectorConflictHash.clear();
		restorConflicts();
	}
	
	//gui stuff
	
	//TODO make a panel for showing and resolving conflicts
	public static void showConflicts()
	{
		if(conflictPanel==null || conflictFrame==null || !conflictFrame.requestFocusInWindow())
		{
			conflictPanel = new ConflictPanel();
			conflictFrame = new JFrame("Conflict Manager");
			conflictFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			conflictFrame.add(conflictPanel);
			conflictFrame.setVisible(true);
			conflictFrame.setSize(500, 300);
		}		
	}
	
	//addons
	
	public static Collection<ConnectorConflict> getConnectorConflicts()
	{
		return connectorConflictHash.values();
	}
	
	public static Collection<TreelineConflict> getTreelineConflicts()
	{
		return treelineConflictHash.values();
	}
	
	private static void mergeConnector(Connector parentConnector, Connector childConnector){
		if(parentConnector.equals(childConnector)){return;}
		Utils.log("connector: "+childConnector.getUniqueIdentifier() + " has " + childConnector.getConTreelines().size() + " trees");
		ArrayList<Treeline> treelineList = new ArrayList<Treeline>(childConnector.getConTreelines());
		for(Treeline curentTreeline: treelineList)
		{
			Utils.log("current Treeline tobe remove and added"+curentTreeline.getUniqueIdentifier());
			childConnector.removeConTreeline(curentTreeline);
			parentConnector.addConTreeline(curentTreeline);
		}
		Display.getFront().getProject().removeProjectThing(childConnector, false);
		childConnector.remove(false);
	}
	
	public static boolean abortCurrentSolving()
	{
		Conflict currentConflict = currentSolvingConflict;
		if(currentConflict!=null){
			if(currentConflict.getClass().equals(TreelineConflict.class)){		
				TreelineConflict conflict = (TreelineConflict)currentConflict;		
				if(isSolving)
				{
					//abort
					Utils.log("solve abort");
					List<Displayable> treelineList = new ArrayList<Displayable>(conflict.getTreelineOne());
					RhizoAddons.removeHighlight(treelineList,false);
					
					if(conflictPanel!=null){
						conflictPanel.setSolved();
					}
					
					isSolving=false;
					return true;
				}
			}
			if(currentConflict.getClass().equals(ConnectorConflict.class)){		
				TreelineConflict conflict = (TreelineConflict)currentConflict;		
				if(isSolving)
				{
					isSolving=false;
					return true;
				}
			}	
		}
		isSolving=true;
		return false;
	}
	
	public static boolean currentConflictIsTreelineConflict()
	{
		if(currentSolvingConflict==null){
			return false;
		}
		if(currentSolvingConflict.getClass().equals(TreelineConflict.class))
		{
			return true;
		}
		return false;
	}
	
	private static void clear()
	{
		treelineConflictHash.clear();
		connectorConflictHash.clear();
	}
	
	public static boolean isPartOfSolution(Displayable d)
	{
		if(currentConflictIsTreelineConflict())
		{
			TreelineConflict currentConflict = (TreelineConflict) currentSolvingConflict;
			List<Treeline> currentTrees = currentConflict.getTreelineOne();
			if(d instanceof Treeline){
				if(currentTrees.contains(d))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean userAbort()
	{
		if(Utils.check("currently solving ... abort?"))
		{
			return true;
		}
		return false;
	}
	
//	public static boolean abortSolvingUserInteraction(Displayable d)
//	{
//		if(isSolving)
//		{
//			if(currentConflictIsTreelineConflict()){
//				TreelineConflict currentConflict = (TreelineConflict) currentSolvingConflict;
//				List<Treeline> currentTrees = currentConflict.getTreelineOne();
//				if(d instanceof Treeline){
//					if(currentTrees.contains(d))
//					{
//						return false;
//					}
//					else
//					{
//						if(Utils.check("currently solving ... abort?"))
//						{
//							return true;
//						}
//						else
//						{
//							return false;
//						}
//					}
//				}
//				else 
//				{
//					return false;
//				}
//			}
//			else
//			{
//				return false;
//			}
//		}
//		else
//		{
//			return false;
//		}
//	}
	

}
