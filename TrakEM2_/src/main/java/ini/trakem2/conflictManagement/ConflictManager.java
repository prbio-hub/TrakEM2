package ini.trakem2.conflictManagement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import ini.trakem2.display.Connector;
import ini.trakem2.display.Display;
import ini.trakem2.display.Displayable;
import ini.trakem2.display.Layer;
import ini.trakem2.display.LayerSet;
import ini.trakem2.display.RhizoAddons;
import ini.trakem2.display.Tree;
import ini.trakem2.display.Treeline;
import ini.trakem2.display.addonGui.ConflictPanel;
import ini.trakem2.utils.Utils;

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
		Display display = Display.getFront();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();

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
	
	//gui stuff
	
	//TODO make a panel for showing and resolving conflicts
	public static void showConflicts()
	{
		if(conflictPanel==null || conflictFrame==null)
		{
			conflictPanel = new ConflictPanel();
			conflictFrame = new JFrame("ConflictManager");
			conflictFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			conflictFrame.add(conflictPanel);
		}

		conflictFrame.setVisible(true);
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
					RhizoAddons.removeHighlight(treelineList);
					
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
		if(currentSolvingConflict.getClass().equals(TreelineConflict.class))
		{
			return true;
		}
		return false;
	}
	

}
