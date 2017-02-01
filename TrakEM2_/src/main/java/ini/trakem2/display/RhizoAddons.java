package ini.trakem2.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


import ini.trakem2.Project;
import ini.trakem2.display.Connector.ConnectorNode;
import ini.trakem2.tree.DNDTree;
import ini.trakem2.tree.ProjectThing;
import ini.trakem2.tree.ProjectTree;
import ini.trakem2.utils.*;
import java.awt.Rectangle;
import java.util.Collection;

public class RhizoAddons {
	static boolean[] treeLineAlphas={true,true,true,true,true,true,true,true,true,true};
        static boolean ini =false;
        static Hashtable<Byte, Color> confidencColors = new Hashtable<Byte, Color>();
        
        public static void init(){
            if(ini==false){
                for(int i=0;i<11;i++){
                    confidencColors.put((byte)10,Color.YELLOW);
                }
                ini = true;
            }
        }


        //copy tree lines from current layer
	public static void copyTreeLine() {
		Display display = Display.getFront();
		// Layer frontLayer = Display.getFrontLayer();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();
		// determine next layer
		Layer nextLayer = currentLayerSet.getLayer(currentLayer.getZ() + 1);
		if (nextLayer == null) {
			Utils.showMessage("Es existiert kein weiterer Layer auf den kopiert werden kann");
			return;
		}
		Utils.log2("Current Layer: " + currentLayer);
		Utils.log2("Next Layer: " + nextLayer);

		// debug Info
		// Utils.log2("blub");
		// Utils.log2("layer: " + displayLayer);
		// Utils.log2("layerset: " + currentLayerSet);

		// Tree tree= (Tree) display.getActive();
		// Node root = tree.getRoot();
		// Utils.log2("root: "+root.getX());

		// get treelines of current layerset
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
		// ArrayList<Treeline> trees = currentLayer.getAll(Treeline.class);

		for (Displayable cObj : trees) {
			Treeline ctree = (Treeline) cObj;
			Utils.log2("current Tree first Layer: " + ctree.getFirstLayer());
			if (ctree.getFirstLayer() == currentLayer) {
				Treeline copy = null;
				try {

					// copy current tree
					copy = Tree.copyAs(ctree, Treeline.class, Treeline.RadiusNode.class);
					copy.setLayer(nextLayer, true);
					copy.setLayer(nextLayer);
					for (Node<Float> cnode : copy.getRoot().getSubtreeNodes()) {
						cnode.setLayer(nextLayer);
                                                cnode.setColor(confidencColors.get(cnode.getConfidence()));
					}
                                        copy.setTitle(ctree.getTitle());
					copy.clearState();
					copy.updateCache();
					currentLayerSet.add(copy);
					ctree.getProject().getProjectTree().addSibling(ctree, copy);
					Connector parentConnector = RhizoAddons.getRightPC(ctree,copy);
                                        parentConnector.addTarget(copy.getRoot().getX(), copy.getRoot().getY(), copy.getRoot().getLayer().getId(),3F);
                                        parentConnector.getRoot().setData(3F);
                                        parentConnector.updateCache();
                                        Utils.log2("Connector: " + parentConnector.getTitle()+ " target(s): " + parentConnector.getTargets());

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// clonectree.setLayer(currentLayerSet.next(displayLayer));
				// clonectree.addToDatabase();
				// clonectree.setEdgeConfidence((byte) 5);
				Utils.log2("layermap old: " + ctree.node_layer_map);
				Utils.log2("layermap copy: " + copy.node_layer_map);
				// if(croot.getLayer()==displayLayer){
				// Utils.log2("root x: "+croot.getX()+" y: "+croot.getY() );
				// }
			}

		}

	}
	//open chooser to select confi that should be recolored
	public static void changeColor(){
		Integer[] data = {10,9,8,7,6,5,4,3,2,1,0};
		//Frame
		JFrame frame = new JFrame("ColorChoser");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//List of typs
		JList list = new JList(data); //data has type Object[]
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		//ColorChooserButton
		//JPanel
		JPanel btnPanle = new JPanel();
		//Buttons
		JButton btnColor = new JButton("Chose Color");
		//btnColor.addActionListener(new ColorChoser(btnColor,list));
		
		JButton btnApply = new JButton("Apply Color");
		//btnApply.addActionListener(new ColorApply());
	    
		
		//Apply Button
		list.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					JList list = (JList) e.getSource();
					int index = (int) list.getSelectedValue();
					
					RhizoAddons.ColorChooser(index, list);
				}
			}
		});
		
		btnPanle.add(listScroller);
		//btnPanle.add(btnColor);
		//btnPanle.add(btnApply);
	    frame.add(btnPanle, BorderLayout.NORTH);
	    frame.pack();
	    frame.setVisible(true);
	}
	
	//chose new color and apply to the coresponding nodes (with confidence i)
	public static void ColorChooser(int i,JList list){
		Color newColor = JColorChooser.showDialog(list, "chose desired color", Color.WHITE);
                confidencColors.put((byte)i,newColor);
		Display display = Display.getFront();
		// Layer frontLayer = Display.getFrontLayer();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();

		// get treelines of current layerset
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
		// ArrayList<Treeline> trees = currentLayer.getAll(Treeline.class);

		for (Displayable cObj : trees) {
			Treeline ctree = (Treeline) cObj;
			for (Node<Float> cnode : ctree.getRoot().getSubtreeNodes()) {
				if((int) cnode.getConfidence() == i){
					//Utils.log2("bin mal beim setzen von Farbe: "+ newColor + " der Index lautete: "+i);
					cnode.setColor(newColor);
				}
			}
			cObj.repaint();
		}
		
	}
	

	//update colors
	public static void applyCorrespondigColor(){

		
		Display display = Display.getFront();
		// Layer frontLayer = Display.getFrontLayer();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();

		// get treelines of current layerset
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
		// ArrayList<Treeline> trees = currentLayer.getAll(Treeline.class);

		for (Displayable cObj : trees) {
			Treeline ctree = (Treeline) cObj;
			for (Node<Float> cnode : ctree.getRoot().getSubtreeNodes()) {
				Utils.log2("old color: " + cnode.getColor()+ " with conf: " +(int) cnode.getConfidence());
				cnode.setColor(confidencColors.get(cnode.getConfidence()));
				Utils.log2("new color: " + cnode.getColor());
			}
			cObj.repaint();
		}
	}
	// open chooser for visibility and clickability [no function yet]
	public static void setVisibility(){
		boolean[] visState = new boolean[10];
		JFrame frame = new JFrame("Visibility Setter");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    for(int i=10;i>-1;i--){
	    	JPanel pn = new JPanel();
	    	JLabel label = new JLabel(Integer.toString(i));
	    	pn.add(label);
	    	JCheckBox ccheckBox = new JCheckBox("");
	    	pn.add(ccheckBox);
	    	frame.add(pn, BorderLayout.NORTH);
	    }
	    frame.pack();
	    frame.setVisible(true);		
	}
	
	//shortCut for new treeline currently cmd+t
	public static void shortyForTreeLine(JComponent obj){
	    @SuppressWarnings("serial")
		Action blaAction = new AbstractAction("bla") {
	    		public void actionPerformed(ActionEvent e) {
	    			//get the relevant stuff
	    			Display display = Display.getFront();
	    			Display.clearSelection();
	    			Project project =display.getProject();	    			
	    			ProjectTree currentTree = project.getProjectTree();
	    			ProjectThing parent = (ProjectThing) project.getRootProjectThing().findChildrenOfTypeR("roots").toArray()[0];
	    			String childType = "treeline";
	    			//catch things
	    			if (!parent.canHaveAsChild(childType)) {
	    				Utils.log("The type '" + parent.getType() + "' cannot have as child the type '" + childType + "'");
	    			}
	    			//make new treeline
	    			ProjectThing pt= parent.createChild("treeline");
                                pt.setTitle(pt.getUniqueIdentifier());
	    			//add new treeline to the project tree
	    			DefaultMutableTreeNode parentNode = DNDTree.findNode(parent, currentTree);
	    			DefaultMutableTreeNode node = new DefaultMutableTreeNode(pt);
	    			((DefaultTreeModel)currentTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
	    		}
	    };
	    obj.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.CTRL_DOWN_MASK,true),"bla");
	    obj.getActionMap().put("bla",blaAction);
	}
	
    //get the right parent connector 
    private static Connector getRightPC(Treeline ptree, Treeline ctree) {
        List<Connector>[] conLists = null;
        Connector pCon;
        Layer pLayer = ptree.getRoot().getLayer();
        Node<Float> pTreeRoot = ptree.getRoot();
        Rectangle rec = new Rectangle((int) pTreeRoot.getX() - 5, (int) pTreeRoot.getY() - 5, 10, 10);
        Collection<Displayable> currentFound = pLayer.find(rec);
        Utils.log("foundObj: " + currentFound);
        Utils.log2("foundObj: " + currentFound);
//        try {
//            Displayable[] foundObje = (Displayable[]) currentFound.toArray();
//            Utils.log("foundObj: " + foundObje);
//        Utils.log2("foundObj: " + foundObje);
//
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        //check if a connector exists
        Project project = Display.getFront().getProject();
        pCon = project.getProjectTree().tryAddNewConnector(ptree, false);
        Node<Float> newRoot = pCon.newNode(pTreeRoot.getX(), pTreeRoot.getY(), pTreeRoot.getLayer(), null);
        pCon.addNode(null, newRoot, pTreeRoot.getConfidence());
        pCon.setRoot(newRoot);
        pCon.setTitle(ptree.getTitle() + "_connector");
        pCon.setAffineTransform(ptree.getAffineTransform());
        return pCon;
    }
	
	
	
}
