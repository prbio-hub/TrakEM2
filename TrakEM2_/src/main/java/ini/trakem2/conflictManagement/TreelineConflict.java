package ini.trakem2.conflictManagement;

import java.util.ArrayList;

import ini.trakem2.display.Connector;
import ini.trakem2.display.Layer;
import ini.trakem2.display.Treeline;
import ini.trakem2.utils.Utils;

public class TreelineConflict extends Conflict {
	TreelineConflictKey treeConKey;
	ArrayList<Treeline> treelineList;
	
	/**
	 * @param connector
	 * @param treelineList
	 * @param layer
	 */
	public TreelineConflict(TreelineConflictKey treeConKey, ArrayList<Treeline> treelineList) {
		this.treeConKey = treeConKey;
		this.treelineList = treelineList;
	}
	
	/**
	 * @param connector
	 * @param layer
	 */
	public TreelineConflict(TreelineConflictKey treeConKey) {
		this.treeConKey = treeConKey;
		update();
	}


	/**
	 * @return the treeConKey
	 */
	public TreelineConflictKey getTreeConKey() {
		return treeConKey;
	}

	/**
	 * @return the connected Treelines
	 */
	public ArrayList<Treeline> getTreelineOne() {
		return treelineList;
	}

	private String getTreelinesAsString(){
		String result = "Treelines: ";
		for (Treeline treeline : treelineList) {
			result = result + treeline.getId() + "; ";
		}
		return result;
	}
	
	public void update()
	{
		//fetch the connector list
		ArrayList<Treeline> treelineList = new ArrayList<Treeline>();
		Connector connector = this.treeConKey.getConnector();
		Layer layer = this.treeConKey.getLayer();
		for(Treeline tree: connector.getConTreelines())
		{
			if(tree.getFirstLayer().equals(layer))
			{
				treelineList.add(tree);	
			}
		}
		this.treelineList=treelineList;
		
		if(this.treelineList.size()<2)
		{
			//no real issue here
			ConflictManager.removeTreelineConflict(this.treeConKey);
		}
	}
	
	@Override 
	public String toString(){
		return "Connector:"+ treeConKey.getConnector().getId() + getTreelinesAsString();
	}
	
}
