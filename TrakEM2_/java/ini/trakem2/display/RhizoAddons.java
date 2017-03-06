package ini.trakem2.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.unihalle.informatik.MiToBo_xml.MTBXMLRootAssociationType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootProjectDocument;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootProjectType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootReferenceType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentPointType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentStatusType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSetType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootType;
import ij.ImagePlus;
import ini.trakem2.Project;
import ini.trakem2.display.Connector.ConnectorNode;
import ini.trakem2.display.Treeline.RadiusNode;
import ini.trakem2.tree.DNDTree;
import ini.trakem2.tree.ProjectThing;
import ini.trakem2.tree.ProjectTree;
import ini.trakem2.utils.Utils;

public class RhizoAddons {
	static boolean test = true;
	static boolean[] treeLineClickable={true,true,true,true,true,true,true,true,true,true,true};
        static boolean ini =false;
        static Hashtable<Byte, Color> confidencColors = new Hashtable<Byte, Color>();
        
        public static void init(){
            if(ini==false){
                for(int i=0;i<11;i++){
                    confidencColors.put((byte)i,Color.YELLOW);
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
		//Layer nextLayer = currentLayerSet.getLayer(currentLayer.getZ() + 1);
		Layer nextLayer = currentLayerSet.next(currentLayer);
		if (nextLayer == null) {
			Utils.showMessage("no more layers as target");
			return;
		}
		//Utils.log2("Current Layer: " + currentLayer);
		//Utils.log2("Next Layer: " + nextLayer);

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
			//Utils.log2("current Tree first Layer: " + ctree.getFirstLayer());
			if (ctree.getFirstLayer() == currentLayer) {
				Treeline copy = null;
				try {

					// copy current tree
					copy = Tree.copyAs(ctree, Treeline.class, Treeline.RadiusNode.class);
					copy.setLayer(nextLayer, true);

					//copy.setLayer(nextLayer);
					for (Node<Float> cnode : copy.getRoot().getSubtreeNodes()) {
						cnode.setLayer(nextLayer);
						cnode.setColor(confidencColors.get(cnode.getConfidence()));
					}
					copy.setTitle(ctree.getTitle());
					copy.clearState();
					copy.updateCache();
					currentLayerSet.add(copy);
					ctree.getProject().getProjectTree().addSibling(ctree, copy);					
					//get the parent connector; if non exists a new will be create
					Connector parentConnector = RhizoAddons.getRightPC(ctree,copy);
					if(parentConnector==null){
						Utils.showMessage("error: couldn't add connector automatically");
					}
					else{
						//the copy of the treeline gets its own linke to the connector 
						Node<Float> conRoot = parentConnector.getRoot();
	                                        //parentConnector.addTarget(copy.getRoot().getX(), copy.getRoot().getY(), copy.getRoot().getLayer().getId(),3F);
						parentConnector.addNode(conRoot, new ConnectorNode(conRoot.getX(), conRoot.getY(), copy.getRoot().getLayer(),3F), Node.MAX_EDGE_CONFIDENCE);
						parentConnector.getRoot().setData(3F);
						parentConnector.clearState();
	                   	parentConnector.updateCache();
						
	                                        Utils.log2("Connector: " + parentConnector.getTitle()+ " target(s): " + parentConnector.getTargets());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// clonectree.setLayer(currentLayerSet.next(displayLayer));
				// clonectree.addToDatabase();
				// clonectree.setEdgeConfidence((byte) 5);
				//Utils.log2("layermap old: " + ctree.node_layer_map);
				//Utils.log2("layermap copy: " + copy.node_layer_map);
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
	public static void applyCorrespondingColor(){
		
		Display display = Display.getFront();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();

		// get treelines of current layerset
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
		
		for (Displayable cObj : trees) {
			Treeline ctree = (Treeline) cObj;
			boolean repaint = false;
			for (Node<Float> cnode : ctree.getRoot().getSubtreeNodes()) {
				byte currentConfi = cnode.getConfidence();
				Color newColor = confidencColors.get(currentConfi);
				if(cnode.getColor()!=newColor){
					cnode.setColor(newColor);
					repaint = true;
				}
			}
			if(repaint){
				cObj.repaint();
			}
		}
	}
	// open chooser for visibility and clickability [no function yet]
	public static void setVisibility(){
		boolean[] visState = new boolean[10];
		JFrame frame = new JFrame("Visibility Setter");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel temp = new AddonGui.visibilityPanel();
		frame.add(temp);
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
				//try to find if there is ProjectThing that can contain the new treeline
				ProjectThing parent;
				parent = RhizoAddons.findParentAllowing("treeline", project);
				//inform user if no ProjectThing is found
				if(parent == null){
				    Utils.showMessage("no project thing found that is capable of holding treelines");
				    return;
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
	
	private static ProjectThing findParentAllowing(String type,Project project){
	   Enumeration enum_nodes;
	    enum_nodes = project.getProjectTree().getRoot().depthFirstEnumeration();
	    while(enum_nodes.hasMoreElements()){
		DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enum_nodes.nextElement();
		ProjectThing currentProjectThing = (ProjectThing) currentNode.getUserObject();
		if(currentProjectThing.canHaveAsChild(type)){
		    return currentProjectThing;
		}
	    }
	    return null;
	}
	
    //get the right parent connector 
    private static Connector getRightPC(Treeline ptree, Treeline ctree) throws Exception {
        List<Connector>[] conLists = null;
        Connector pCon;
        Layer pLayer = ptree.getRoot().getLayer();
        Node<Float> pTreeRoot = ptree.getRoot();

	
	List<Connector>[] ConLists;
	ConLists = ptree.findConnectors();
		                        Utils.log("Incommings: "+ ConLists[0].toString());
					Utils.log("Outgoings: "+ ConLists[1].toString());
					Utils.log("Incommings: "+ ConLists[0]);
					Utils.log("Outgoings: "+ ConLists[1]);
	//if incomming list ist not empty aka there is a connector starting at the parentTreeline 
	if(ConLists[0].isEmpty() == false){
	    //get the connector and everything is fine
	    pCon=ConLists[0].get(0);
	    return pCon;
	}
	//if outgoing list ist not empty aka there is a connector ending at the parentTreeline 
	if(ConLists[1].isEmpty() == false){
	    //get the connector and everything is fine
	    pCon=ConLists[1].get(0);
	    return pCon;
	}
	//if were are here there was no connector; so make a new one
        Project project = Display.getFront().getProject();
        pCon = project.getProjectTree().tryAddNewConnector(ptree, false);
        if(pCon==null){
        	return null;
        }
        Node<Float> newRoot = pCon.newNode(pTreeRoot.getX(), pTreeRoot.getY(), pTreeRoot.getLayer(), null);
        pCon.addNode(null, newRoot, pTreeRoot.getConfidence());
        pCon.setRoot(newRoot);
        //pCon.setTitle(ptree.getTitle() + "_connector");
        pCon.setAffineTransform(ptree.getAffineTransform());
        return pCon;   
    }

    
    
    public static void test(){
	//RhizoAddons.test = !RhizoAddons.test;
        Utils.log("Aktueller Zustand: "+ RhizoAddons.test);
	Display display = Display.getFront();
		// Layer frontLayer = Display.getFrontLayer();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();
		//currentLayerSet.updateLayerTree();
		// determine next layer
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
		
		for (Displayable cObj : trees) {
			Treeline ctree = (Treeline) cObj;
			if (ctree.getFirstLayer() == currentLayer) {
				try {
				    //ctree.repaint();
					Utils.log2("current Tree first Layer: " + ctree.getFirstLayer());
					Utils.log2("current Tree Layer Property"+cObj.getLayer());
				} catch (Exception e) {
					// TODO Auto-generated catch block 
					e.printStackTrace();
				}


			}

		}
    }
	
    ///////////////////////
    // aeekz Tino
    public static void writeMTBXML()
	{
		try 
		{
			File saveFile = Utils.chooseFile(System.getProperty("user.home"), null, ".xml");
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
			
			Hashtable<Treeline, int[]> rootsTable = new Hashtable<Treeline, int[]>();
			
			// get layers
			Display display = Display.getFront();	
			Project project = display.getProject();
			LayerSet layerSet = display.getLayerSet();  
			ArrayList<Layer> layers = layerSet.getLayers();
			
			List<Patch> patches = layerSet.getAll(Patch.class);
			ImagePlus imagePlus = patches.get(0).getImagePlus();
			String[] imageNames = imagePlus.getImageStack().getSliceLabels();
			
			// setup xml file
			MTBXMLRootProjectDocument xmlRootProjectDocument = MTBXMLRootProjectDocument.Factory.newInstance();
			MTBXMLRootProjectType xmlRootProject = xmlRootProjectDocument.addNewMTBXMLRootProject();
			xmlRootProject.setXsize((int) layers.get(0).getLayerWidth());
			xmlRootProject.setYsize((int) layers.get(0).getLayerHeight());
			xmlRootProject.setXresolution((float) imagePlus.getCalibration().getX(1)); // TODO
			xmlRootProject.setYresolution((float) imagePlus.getCalibration().getY(1)); // TODO
			
			MTBXMLRootSetType[] xmlRootSets = new MTBXMLRootSetType[layers.size()];

			// Treelines in project
			List<Displayable> allTreelines = layerSet.get(Treeline.class);
			
			for(int i = 0; i < layers.size(); i++)
			{
				Layer currentLayer = layers.get(i); 
				MTBXMLRootSetType rootSet = MTBXMLRootSetType.Factory.newInstance();
				
				rootSet.setImagename(imageNames[i]);
				rootSet.setRootSetID(i);
				
				List<MTBXMLRootType> roots = new ArrayList<MTBXMLRootType>(); // arraylist for convenience
				int rootID = 0;

				// check for each treelines which layer it belongs to - inconvenient but currently the only way to get all treelines in a layer
				for(int j = 0; j < allTreelines.size(); j++)
				{
					Treeline currentTreeline = (Treeline) allTreelines.get(j);

					// if treeline belongs to the current layer (current rootset) add it
					if(currentTreeline.getFirstLayer().equals(currentLayer))
					{
						roots.add(treelineToXMLType(currentTreeline, currentLayer, rootID)); 
						rootsTable.put(currentTreeline, new int[]{i, rootID});
						Utils.log("treeline"+rootID+": layer"+i); // debugging
						rootID++;
					}
				}
				
				Utils.log("layer"+i+": "+numberOfTreelinesInLayer(currentLayer, allTreelines));
				rootSet.setRootsArray(roots.toArray(new MTBXMLRootType[numberOfTreelinesInLayer(currentLayer, allTreelines)]));
				xmlRootSets[i] = rootSet;
			}

			xmlRootProject.setRootsetsArray(xmlRootSets);
			Utils.log("CheckRS" + allTreelines.size()); // for debugging
			
			
			// Connectors in project
			List<Displayable> connectors = layerSet.get(Connector.class);
			List<MTBXMLRootAssociationType> rootAssociationList = new ArrayList<MTBXMLRootAssociationType>(); // arraylist for convenience
			
			for(int i = 0; i < connectors.size(); i++)
			{
				Connector currentConnector = (Connector) connectors.get(i);
				
				MTBXMLRootAssociationType rootAssociation = MTBXMLRootAssociationType.Factory.newInstance();
				List<MTBXMLRootReferenceType> rootReferencesList = new ArrayList<MTBXMLRootReferenceType>(); // arraylist for convenience
 				
				List<Treeline> treelinesOfConnector = treelinesOfConnector(currentConnector);
				
				for(int j = 0; j < treelinesOfConnector.size(); j++) 
				{
					int[] ids = rootsTable.get(treelinesOfConnector.get(j));
					MTBXMLRootReferenceType rootReference = MTBXMLRootReferenceType.Factory.newInstance();
					rootReference.setRootID(BigInteger.valueOf(ids[1]));
					rootReference.setRootSetID(BigInteger.valueOf(ids[0]));
					
					rootReferencesList.add(rootReference);
				}
				
				rootAssociation.setRootReferencesArray(rootReferencesList.toArray(new MTBXMLRootReferenceType[treelinesOfConnector.size()]));
				rootAssociationList.add(rootAssociation);
			}
			
			xmlRootProject.setRootAssociationsArray(rootAssociationList.toArray(new MTBXMLRootAssociationType[connectors.size()]));
			Utils.log("CheckCs"+connectors.size()); // for debugging
			
			bw.write(xmlRootProjectDocument.toString());
			bw.close();
			Utils.log("Created xml file - "+saveFile.getAbsolutePath());
				
		} 
		catch (Exception e) 
		{
			Utils.log(e.getMessage());
		}
	}	
    
    /* Returns treeline in xmlbeans format */
	private static MTBXMLRootType treelineToXMLType(Treeline treeline, Layer currentLayer, int rootId)
	{
		MTBXMLRootType xmlRoot = MTBXMLRootType.Factory.newInstance();

		xmlRoot.setRootID(BigInteger.valueOf(rootId));
		xmlRoot.setStartID(BigInteger.valueOf(0)); // TODO?
		
		List<Node<Float>> nodes = new ArrayList<Node<Float>>(treeline.getNodesAt(currentLayer)); // arraylist for convenience
		nodes.remove(treeline.getRoot());
		
		MTBXMLRootSegmentType[] rootSegmentsArray = new MTBXMLRootSegmentType[nodes.size()];
		
		for(int i = 0; i < nodes.size(); i++)
		{
			Node<Float> n = nodes.get(i);
			float startRadius = 1;
			float endRadius = 1;
			
			if(!n.equals(treeline.getRoot()))
			{
				if(n instanceof RadiusNode) startRadius = ((RadiusNode) n).getData();
				if(n.getParent() instanceof RadiusNode) endRadius = ((RadiusNode) n.getParent()).getData();
				
				MTBXMLRootSegmentType rootSegment = MTBXMLRootSegmentType.Factory.newInstance();
				rootSegment.setRootID(xmlRoot.getRootID());
				rootSegment.setSegmentID(BigInteger.valueOf(i));
				if(n.getParent().equals(treeline.getRoot())) rootSegment.setParentID(BigInteger.valueOf(-1));
				else rootSegment.setParentID(BigInteger.valueOf(i-1)); // TODO find actual parent segment id via coordinates or save the previous RootSegmentType?

				MTBXMLRootSegmentPointType xmlStart = MTBXMLRootSegmentPointType.Factory.newInstance();
				// transform local coordinates to global
				Point2D start = treeline.getAffineTransform().transform(new Point2D.Float(n.getParent().getX(), n.getParent().getY()), null);
				xmlStart.setX((float) start.getX());
				xmlStart.setY((float) start.getY());
				rootSegment.setStartPoint(xmlStart);
				rootSegment.setStartRadius(startRadius);
				
				MTBXMLRootSegmentPointType xmlEnd = MTBXMLRootSegmentPointType.Factory.newInstance();
				// transform local coordinates to global
				Point2D end = treeline.getAffineTransform().transform(new Point2D.Float(n.getX(), n.getY()), null);
				xmlEnd.setX((float) end.getX());
				xmlEnd.setY((float) end.getY());
				rootSegment.setEndPoint(xmlEnd);
				rootSegment.setEndRadius(endRadius);

				rootSegment.setType(MTBXMLRootSegmentStatusType.LIVING); // TODO
				rootSegmentsArray[i] = rootSegment;
			}
		}
		
		xmlRoot.setRootSegmentsArray(rootSegmentsArray);
		return xmlRoot;
	}

	public static void readMTBXML(String filename)
	{
		// will eventually become a fileChooser
		String[] filepath = Utils.selectFile("test");
		File file = new File(filepath[0] + filepath[1]);
		
//		try 
//		{
//			
//			
//		} 
//		catch (XmlException e) 
//		{
//			Utils.log(e.getMessage());
//		} 
//		catch (IOException e) 
//		{
//			Utils.log(e.getMessage());
//		}
		
	}
	
	private static List<Treeline> treelinesOfConnector(Connector c)
	{
		List<Treeline> treelines = new ArrayList<Treeline>();
		List<Set<Displayable>> targets = c.getTargets();
		Set<Displayable> origins = c.getOrigins();
		
		for(Displayable d: origins)
		{
			if(d instanceof Treeline) treelines.add((Treeline) d);
		}
	
		for(int i = 0; i < targets.size(); i++)
		{
			for(Displayable d: targets.get(i))
			{
				if(d instanceof Treeline) treelines.add((Treeline) d);
			}
		}
		
		return treelines;
	}

    /* Returns the number of treelines in a layer */
    private static int numberOfTreelinesInLayer(Layer l, List<Displayable> treelines)
    {
    	int res = 0;
    	
    	for(int j = 0; j < treelines.size(); j++)
		{
			Treeline currentTreeline = (Treeline) treelines.get(j);
			
			if(currentTreeline.getFirstLayer().equals(l)) res++;
		}
    	
    	return res;
    }
}

    
