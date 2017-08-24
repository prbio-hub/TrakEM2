package ini.trakem2.conflictManagement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JFrame;

import ini.trakem2.display.Connector;
import ini.trakem2.display.Display;
import ini.trakem2.display.Displayable;
import ini.trakem2.display.Layer;
import ini.trakem2.display.LayerSet;
import ini.trakem2.display.Tree;
import ini.trakem2.display.Treeline;
import ini.trakem2.display.addonGui.ConflictPanel;

public class ConflictManager {
	
	//save conflicts and gui stuff
	
	private static HashMap<Treeline, ConnectorConflict> connectorConflictHash = new HashMap<Treeline, ConnectorConflict>();
	private static HashMap<TreelineConflictKey, TreelineConflict> treelineConflictHash = new HashMap<TreelineConflictKey, TreelineConflict>();
	
	private static ConflictPanel conflictPanel = null;
	private static JFrame	conflictFrame = null;
	

	//check for conflicts
	
	public static void processChange(Treeline tree, Connector connector){
		addConnectorConflict(tree);
		if(tree.getFirstLayer()!=null)
		{
			addTreelineConflict(connector, tree.getFirstLayer());
		}
		conflictPanel.updateList();
	}
	
	//auto resolve to minimize multiple connector conflicts if possible
	public static void autoResolveConnectorConnflicts(boolean aggressive)
	{
		if(!aggressive)
		{
			//so only solve cases if no new treeconflict will arise
			Collection<ConnectorConflict> conflicts = connectorConflictHash.values();
			
		}
		else
		{
			
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
		connectorConflictHash.remove(treeline);
	}
	
	public static void  removeTreelineConflict(TreelineConflictKey treeConKey){
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
	

}
