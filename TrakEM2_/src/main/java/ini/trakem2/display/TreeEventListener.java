package ini.trakem2.display;

import java.util.ArrayList;

/** interface for the listener*/
public interface TreeEventListener {
	void eventAppeared(TreeEvent te);
	Connector getConnector();
}

/** event for the listener interaction */
class TreeEvent {
	Treeline source;
	String eventMessage;
	ArrayList<Node<Float>> interestingNodes;
	ArrayList<Treeline> interestingTrees;
	TreeEvent(Treeline source,String eventMessage, ArrayList<Node<Float>> interestingNodes,ArrayList<Treeline> interestingTrees){
		this.source = source;
		this.eventMessage= eventMessage;
		this.interestingNodes= interestingNodes;
		this.interestingTrees= interestingTrees;
	}
	
	public Tree getSource() {
		return source;
	}
	
	public String getEventMessage() {
		return eventMessage;
	}
	
	public ArrayList<Node<Float>> getInterestingNodes() {
		return interestingNodes;
	}
	
	public ArrayList<Treeline> getInterestingTrees() {
		return interestingTrees;
	}
}