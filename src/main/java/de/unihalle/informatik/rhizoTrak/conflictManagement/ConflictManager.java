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
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.addon.RhizoColVis;
import de.unihalle.informatik.rhizoTrak.addon.RhizoMain;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.TreeEventListener;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.addonGui.ConflictPanel;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

import java.util.HashSet;
import java.util.Iterator;

public class ConflictManager 
{

	//save conflicts and gui stuff
	
	private  HashMap<Treeline, ConnectorConflict> connectorConflictHash = new HashMap<Treeline, ConnectorConflict>();
	private  HashMap<TreelineConflictKey, TreelineConflict> treelineConflictHash = new HashMap<TreelineConflictKey, TreelineConflict>();
        
	private  ConflictPanel conflictPanel = null;
    private  RhizoMain rhizoMain;
	private  JFrame	conflictFrame = null;
	private boolean autoResolveNA=false;
    
	private boolean isSolving=false;
	
    public ConflictManager(RhizoMain rhizoMain) 
    {
        this.rhizoMain = rhizoMain;    
    }
    
	/**
	 * @return the isSolving
	 */
	public boolean isSolving() {
		return isSolving;
	}

	/**
	 * @param isSolving the isSolving to set
	 */
	public void setSolving(boolean isSolving) {
		this.isSolving = isSolving;
	}

	/**
	 * @return the currentSolvingConflict
	 */
	public Conflict getCurrentSolvingConflict() {
		return currentSolvingConflict;
	}

	/**
	 * @param currentSolvingConflict the currentSolvingConflict to set
	 */
	public void setCurrentSolvingConflict(Conflict currentSolvingConflict) {
		this.currentSolvingConflict = currentSolvingConflict;
                setSolving(true);
	}

	private Conflict currentSolvingConflict=null;
	

	//check for conflicts
	
	public void processChange(Treeline tree, Connector connector){
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
	public void autoResolveConnectorConnflicts(boolean aggressive)
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
        
        //returns false on user abort as a feedback for other methods like merge
        //precondition: two or more connectors: so a list of Connectors
        public int userInteraction(HashSet<Connector> initialConnectorSet,boolean fromConflictPanel)
        {
        	if(initialConnectorSet.size()<2) 
        	{
        		//abort because of no conflict
        		return 3;
        	}
            //first thing: update the connector list, so be prepared for the non conflict free case
    		HashSet<Connector> connectorSet = searchAllConnectors(initialConnectorSet);
    		//TODO: complain to the user if conectorSet > initialConnectorSet because of unsolved issues
    		if(connectorSet.size()>initialConnectorSet.size() || connectorSet.size()>2) {
        		if(Utils.checkYN("It looks like your action interfere with current conflicts.\n It is highly advisable to solve current conflicts before continue. Abort?"))
        		{
        			//abort
        			return 0;
        		}
    		}
            //now check if the connectors can be combined without new conflicts
    		boolean na = canBeCombinedNA(connectorSet);
    		if(na)
    		{
    			if(autoResolveNA || fromConflictPanel)
    			{        				
    				//continue and resolve
    				return 1;
    			}
    			else
    			{
    				return userDialogNA(connectorSet);
    			}
    		}
    		else
    		{
    			return userDialogA(connectorSet);
    		}
        }
        
        private int userDialogNA(HashSet<Connector> connectorSet)
        {
			if(Utils.checkYN("This action will cause new conflicts that can be auto-resolved. Continue?"))
			{
				String message = "To prevent conflicts the following connectors will be merged: \n";
	            for(Iterator<Connector> conIt = connectorSet.iterator(); conIt.hasNext();)
	            {
	                    Connector cConnector = conIt.next();
	                    message = message + cConnector.getId()+ "; "; 
	            }
	            message = message + "\n"+"Merge?";
				if(Utils.checkYN(message))
				{
					//continue and resolve
					return 1;
				}            
	            //continue but not resolve
				return 2;
			}
			//abort
			return 0;
        }
        
        private int userDialogA(HashSet<Connector> connectorSet)
        {
    		//check how bad it will be       		
    		ArrayList<String> report = reportHowBad(connectorSet);
    		int conflictCount = Integer.parseInt(report.get(report.size()-1));
    		report.remove(report.size()-1);
    		String message = "This action will cause " +conflictCount+ " new conflicts that can not be auto-resolved. Continue?"+ "\n" + "conflicts:" + "\n";
    		for (String string : report) {
				message = message + string;
			}
			if(Utils.checkYN(message))
			{
				//continue and resolve
				return 1;
			}
			//abort
            return 0;
        }
        
        public int userInteractionTree(HashSet<Treeline> treelineSet)
        {
        	HashSet<Connector> connectorSet = new HashSet<Connector>();
        	for(Iterator<Treeline> treelineIt = treelineSet.iterator(); treelineIt.hasNext();)
        	{
        		Treeline cTreeline = treelineIt.next();
        		List<TreeEventListener> telList = cTreeline.getTreeEventListener();
        		for (TreeEventListener tel : telList) {
					connectorSet.add(tel.getConnector());
				}
        		
        	}
        	return userInteraction(connectorSet,false);
        }
        
        public void resolveTree(HashSet<Treeline> treelineSet)
        {
        	HashSet<Connector> connectorSet = new HashSet<Connector>();
        	for(Iterator<Treeline> treelineIt = treelineSet.iterator(); treelineIt.hasNext();)
        	{
        		Treeline cTreeline = treelineIt.next();
        		List<TreeEventListener> telList = cTreeline.getTreeEventListener();
        		for (TreeEventListener tel : telList) {
					connectorSet.add(tel.getConnector());
				}
        		
        	}
        	resolve(connectorSet);
        }
        
        public void resolve(HashSet<Connector> connectorSet)
        {
        	Connector parent = findParentToBe(connectorSet);
            for(Iterator<Connector> conIt = connectorSet.iterator(); conIt.hasNext();)
            {
                    Connector cConnector = conIt.next();
                    mergeConnector(parent,cConnector);
            }
            Display.updateVisibleTabs();				
        }
        
        private Connector findParentToBe(HashSet<Connector> connectorSet)
        {
        	Connector currentWinner=null;
        	double currentHighest=-1;
            for(Iterator<Connector> conIt = connectorSet.iterator(); conIt.hasNext();)
            {
                Connector cConnector = conIt.next();
                if(currentHighest==-1 && cConnector.getConTreelines().size()>0) 
                {
                	currentHighest = findHighestZofConnector(cConnector);
                	currentWinner = cConnector;
                }
                else
                {
                	double currentZ = findHighestZofConnector(cConnector);
                	if(currentZ<currentHighest)
                	{
                		currentHighest = currentZ;
                		currentWinner = cConnector;
                	}
                }
            }
            return currentWinner;
        }
        
        private double findHighestZofConnector(Connector connector) 
        {
        	double result;
        	ArrayList<Treeline> trees = connector.getConTreelines();
        	//dirty fix
        	result = 9999999;
        	for (Treeline treeline : trees) {
				double currentZ = treeline.getFirstLayer().getZ();
        		if(result>currentZ) 
        		{
        			result=currentZ;
        		}
			}
        	return result;
        }
        
        private ArrayList<String> reportHowBad(HashSet<Connector> connectorSet) 
        {
        	ArrayList<String> finalResult = new ArrayList<String>();
        	Map<Layer,ArrayList<Treeline>> conflictAtlas =  new HashMap<Layer,ArrayList<Treeline>>();
        	
            for(Iterator<Connector> conIt = connectorSet.iterator(); conIt.hasNext();)
            {
                    Connector cConnector = conIt.next();
                    ArrayList<Treeline> currentTrees = cConnector.getConTreelines();
                    for(Iterator<Treeline> treelineIt = currentTrees.iterator();treelineIt.hasNext();)
                    {
                            Treeline cTreeline = treelineIt.next();
                            Layer cLayer = cTreeline.getFirstLayer();
                            //check if layer is occupied
                            if(conflictAtlas.get(cLayer)==null) {
                            	ArrayList<Treeline> newList = new ArrayList<Treeline>();
                            	newList.add(cTreeline);
                            	conflictAtlas.put(cLayer,newList);
                            }  
                            else 
                            {
                            	ArrayList<Treeline> oldList = conflictAtlas.get(cLayer);
                            	oldList.add(cTreeline);
                            	conflictAtlas.put(cLayer,oldList);
                            }
                    }
            }
            int conflictCount=0;
            Set<Layer> keys = conflictAtlas.keySet();
            for(Iterator<Layer> layerIt = keys.iterator();layerIt.hasNext();)
            {
            	String result="";
            	Layer cLayer = layerIt.next();
            	ArrayList<Treeline> cTreelines = conflictAtlas.get(cLayer);
            	if(cTreelines.size()>1)
            	{
            		conflictCount=conflictCount+(cTreelines.size()-1);
            		result = result + "layer: " + cLayer.getId() +" treelines: ";
            		for (Treeline treeline : cTreelines) {
						result = result + treeline.getId()+"; ";
					}
            		result = result + "\n";
            		finalResult.add(result);
            	}
            }
            finalResult.add(Integer.toString(conflictCount));
        	return finalResult;
        }
        
        private boolean canBeCombinedNA(HashSet<Connector> connectorSet) 
        {
        	HashMap<Layer,Treeline> layers = new HashMap<Layer,Treeline>();
            for(Iterator<Connector> conIt = connectorSet.iterator(); conIt.hasNext();)
            {
                    Connector cConnector = conIt.next();
                    ArrayList<Treeline> currentTrees = cConnector.getConTreelines();
                    if(currentTrees.size()==1) continue;
                    for(Iterator<Treeline> treelineIt = currentTrees.iterator();treelineIt.hasNext();)
                    {
                            Treeline cTreeline = treelineIt.next();
                            Layer cLayer = cTreeline.getFirstLayer();
                            if(layers.get(cLayer)!=null) 
                            {
                            	if(!layers.get(cLayer).equals(cTreeline)) 
                            	{
                                	//the layer was already taken by another treeline so no easy solution here
                                	return false;
                            	}

                            }
                            else
                            {
                            	layers.put(cLayer, cTreeline);
                            }
                    }
            }
        	return true;
        }
        
        private HashSet<Connector> searchAllConnectors(HashSet<Connector> connectorSet)
        {
        	int formerSize=0;
        	while(formerSize != connectorSet.size())
        	{
        		formerSize = connectorSet.size();
        		HashSet<Connector> currentSet = new HashSet<Connector>();
                for(Iterator<Connector> conIt = connectorSet.iterator(); conIt.hasNext();)
                {
                        Connector cConnector = conIt.next();
                        ArrayList<Treeline> currentTrees = cConnector.getConTreelines();
                        currentSet.addAll(searchConnectorsOfTreelineList(currentTrees));
                }
                connectorSet.addAll(currentSet);
        	}
        	return connectorSet;
        }
        
        private HashSet<Connector> searchConnectorsOfTreelineList(ArrayList<Treeline> treelineList)
        {
                HashSet<Connector> connectorSet = new HashSet<>();
                for(Iterator<Treeline> treelineIt = treelineList.iterator();treelineIt.hasNext();)
                {
                        Treeline cTreeline = treelineIt.next();
                        for(TreeEventListener tel: cTreeline.getTreeEventListener())
                        {
                                connectorSet.add(tel.getConnector());
                        }
                }               
                return connectorSet;
        }
	
	//add conflicts
	
	public void addConnectorConflict(Treeline tree)
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
			currentConflict = new ConnectorConflict(this,tree);
			connectorConflictHash.put(tree,currentConflict);
			currentConflict.update(); //the update will insure if there is a real issue
		}
	}
	
	public void addTreelineConflict(Connector connector,Layer layer)
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
			currentConflict = new TreelineConflict(this,treeConKey);
			treelineConflictHash.put(currentConflict.getTreeConKey(),currentConflict);
			currentConflict.update(); //the update will insure if there is a real issue
		}
	}
	
	//remove conflicts
		
	public void  removeConnectorConflict(Treeline treeline){
		ConnectorConflict currentConflict = connectorConflictHash.get(treeline);
		
		if(currentConflict!=null && currentConflict.equals(getCurrentSolvingConflict()))
		{
			abortCurrentSolving();
		}
		
		connectorConflictHash.remove(treeline);
	}
	
    public void removeTreelineConflict(TreelineConflictKey treeConKey) {
        TreelineConflict currentConflict = treelineConflictHash.get(treeConKey);
        if (currentConflictIsTreelineConflict()) {
            TreelineConflict currentSolving = (TreelineConflict) getCurrentSolvingConflict();
            if (currentConflict == null) {
                return;
            }
            if (currentConflict.equalsConflict(currentSolving)) {
                abortCurrentSolving();
            }
        }

        treelineConflictHash.remove(treeConKey);
    }
	
    //restore conflicts
    public void restorConflicts(Project project) {
        clear();
        LayerSet currentLayerSet = project.getRootLayerSet();

        ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);

        for (Displayable cObj : trees) {
            Treeline ctree = (Treeline) cObj;
            addConnectorConflict(ctree);
        }

        ArrayList<Displayable> connectors = currentLayerSet.get(Connector.class);

        for (Displayable cObj : connectors) {
            Connector cconnector = (Connector) cObj;
            for (Layer layer : currentLayerSet.getLayers()) {
                addTreelineConflict(cconnector, layer);
            }
        }
    }
	
	public void clearAndUpdate()
	{
		connectorConflictHash.clear();
		connectorConflictHash.clear();
		restorConflicts(Display.getFront().getProject());
	}
	
	//gui stuff

	public void showConflicts()
	{
		if(conflictPanel==null || conflictFrame==null)
		{
			conflictPanel = new ConflictPanel(this);
			conflictFrame = new JFrame("Conflict Manager: "+ rhizoMain.getProject().getTitle());
			conflictFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			conflictFrame.add(conflictPanel);
			conflictFrame.setVisible(true);
			conflictFrame.setSize(500, 300);
                        return;
		}
                conflictFrame.setVisible(true);
                conflictFrame.toFront();
	}
	
	//addons
	
	public Collection<ConnectorConflict> getConnectorConflicts()
	{
		return connectorConflictHash.values();
	}
	
	public Collection<TreelineConflict> getTreelineConflicts()
	{
		return treelineConflictHash.values();
	}
	
	private static void mergeConnector(Connector parentConnector, Connector childConnector){
		if(parentConnector.equals(childConnector)){return;}
		ArrayList<Treeline> treelineList = new ArrayList<Treeline>(childConnector.getConTreelines());
		for(Treeline curentTreeline: treelineList)
		{
			childConnector.removeConTreeline(curentTreeline);
			parentConnector.addConTreeline(curentTreeline);
		}
		childConnector.remove2(false);
		Display.repaint(parentConnector.getLayerSet());
	}
	
	public boolean abortCurrentSolving()
	{
		Conflict currentConflict = currentSolvingConflict;
		if(currentConflict!=null){
			if(currentConflict.getClass().equals(TreelineConflict.class)){		
				TreelineConflict conflict = (TreelineConflict)currentConflict;		
				if(isSolving)
				{
					List<Displayable> treelineList = new ArrayList<Displayable>(conflict.getTreelineOne());
					RhizoColVis.removeHighlight(treelineList,false);
					
					if(conflictPanel!=null){
						conflictPanel.setSolved();
					}
					isSolving=false;
					return true;
				}
			}
			if(currentConflict.getClass().equals(ConnectorConflict.class)){		
				ConnectorConflict conflict = (ConnectorConflict)currentConflict;		
				if(isSolving)
				{
					if(conflictPanel!=null){
						conflictPanel.setSolved();
					}
					isSolving=false;
					return true;
				}
			}	
		}
//                Utils.log("setting isSolving to false in abortCurrentSolving");
//		isSolving=false;
//                if(conflictPanel!=null){
//                        conflictPanel.setSolved();
//		}
		return false;
	}
	
	public boolean currentConflictIsTreelineConflict()
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
	
	private void clear()
	{
		treelineConflictHash.clear();
		connectorConflictHash.clear();
	}
	
	public boolean isPartOfSolution(Displayable d)
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
	
	public boolean userAbort()
	{
		if(Utils.check("currently solving ... abort?"))
		{
			return true;
		}
		return false;
	}
        
        public void disposeConflictFrame(){
            if(conflictFrame==null){
                return;
            }
            conflictPanel.clearList();
            conflictFrame.dispose();
        }
}
