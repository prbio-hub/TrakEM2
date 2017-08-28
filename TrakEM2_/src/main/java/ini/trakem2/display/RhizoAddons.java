package ini.trakem2.display;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.python.antlr.ast.Slice.Slice___init___exposer;

import de.unihalle.informatik.MiToBo_xml.MTBXMLRootAssociationType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootImageAnnotationType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootProjectDocument;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootProjectType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootReferenceType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentPointType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentStatusType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootImageAnnotationType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootType;
import ij.ImagePlus;
import ij.plugin.frame.ThresholdAdjuster;
import ini.trakem2.Project;
import ini.trakem2.conflictManagement.ConflictManager;
import ini.trakem2.conflictManagement.ConflictManager;
import ini.trakem2.display.Connector.ConnectorNode;
import ini.trakem2.display.Treeline.RadiusNode;
import ini.trakem2.persistence.Loader;
import ini.trakem2.tree.DNDTree;
import ini.trakem2.tree.ProjectThing;
import ini.trakem2.tree.ProjectTree;
import ini.trakem2.utils.Dispatcher;
import ini.trakem2.utils.Utils;

public class RhizoAddons
{
	public static boolean readyToLoad = false;
	static boolean chooseReady = false; 

	static boolean test = true;
	static boolean[] treeLineClickable = { true, true, true, true, true, true, true, true, true, true, true };
	static int[] treelineSlider ={255,255,255,255,255,255,255,255,255,255,255};
	static boolean ini = false;
	
	public static Node lastEditedOrActiveNode = null;
	
	private static JFrame colorFrame, imageLoaderFrame;
	
	static Hashtable<Byte, Color> confidencColors = new Hashtable<Byte, Color>();
	static AddonGui guiAddons;
	
	/* initialization and termination stuff */
	
	/**
	 * Initializes GUI
	 * @author Axel 
	 */
	public static void init()
	{
		if (ini == false)
		{
			for (int i = 0; i < 11; i++)
			{
				confidencColors.put((byte) i, Color.YELLOW);
			}
			// confi Color 11 is used to mark treelines of current interest
			confidencColors.put((byte) 11, Color.CYAN);
			ini = true;
		}
		
		guiAddons = new AddonGui();
	}
	
	/**
	 * Calls load methods when opening a project
	 * @param file - saved project file
	 * @author Axel
	 */
	public static void addonLoader(File file)
	{
		Thread loader = new Thread()
		{
			{
				setPriority(Thread.NORM_PRIORITY);
			}

			@Override
			public void run()
			{
				while (RhizoAddons.readyToLoad == false)
				{
					try
					{
						sleep(500);
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}

				// load connector data
				Utils.log("loading user settings ...");
				loadUserSettings();
				Utils.log("done");

				Utils.log("loading connector data ...");
				loadConnector(file);
				Utils.log("done");
				
				Utils.log("restore conflicts ...");
				ConflictManager.restorConflicts();
				Utils.log("done");

				RhizoAddons.readyToLoad = false;
				return;
			}

		};
		
		// start the thread
		loader.start();
		return;
	}
	
	/**
	 * Loads the user settings (color, visibility etc.)
	 * @author Axel
	 */
	public static void loadUserSettings()
	{
		File userSettingFile = new File(System.getProperty("user.home") + File.separator + ".rhizoTrakSettings" + File.separator + "settings");

		if (!userSettingFile.exists())
		{
			Utils.log("unable to load user settings: file not found");
			return;
		}

		String savetxt = readFileToString(userSettingFile);
		ArrayList<String> content = stringToLineArray(savetxt);

		Utils.log(savetxt);

		if (content.size() != 12)
		{
			Utils.log("unable to load user settings: incorrect content size");
			return;
		}

		for (int i = 0; i < 11; i++)
		{
			String[] currentLine = content.get(i).split(";");
			if (currentLine.length != 5)
			{
				Utils.log("unable to load user settings: incorrect line length (1)");
				return;
			}
			
			Color currentColor = stringToColor(currentLine[0] + ";" + currentLine[1] + ";" + currentLine[2] + ";" + currentLine[3]);

			boolean currentBool = false;
			if (currentLine[4].equals("true"))
			{
				currentBool = true;
			}

			confidencColors.put((byte) i, currentColor);
			treeLineClickable[i] = currentBool;
		}
		
		String[] currentLine = content.get(11).split(";");
		
		if (currentLine.length != 4)
		{
			Utils.log("Unable to load user settings: incorrect line length (2)");
			return;
		}
		Color currentColor = stringToColor(currentLine[0] + ";" + currentLine[1] + ";" + currentLine[2] + ";" + currentLine[3]);
		
		confidencColors.put((byte) 11, currentColor);
		ini = true;
	}
	
	/**
	 * Loads the connector file
	 * @param file - The project save file
	 * @author Axel
	 */
	public static void loadConnector(File file)
	{
		// read the save file
		File conFile = new File(file.getParentFile().getAbsolutePath() + File.separator + file.getName().split("\\.")[0] + ".con");
																													
		if (!conFile.exists())
		{
			// no con file create a new
			try
			{
				conFile.createNewFile();
				return;
			} 
			catch (IOException e)
			{
				Utils.log("error: no *.con file found creating new one");
				e.printStackTrace();
				return;
			}
		}

		// get all the projects
		ArrayList<Project> allProjects = Project.getProjects();
		int currentProjectID = 0;

		FileReader fr;
		try
		{
			fr = new FileReader(conFile);
			BufferedReader br = new BufferedReader(fr);

			String line = br.readLine();
			while (line != null)
			{
				if (line.equals("###"))
				{
					currentProjectID++;
					line = br.readLine();
					break;
				}
				
				if (allProjects.get(currentProjectID) != null)
				{
					LayerSet layerSet = allProjects.get(currentProjectID).getRootLayerSet();
					List<Displayable> trees = layerSet.get(Treeline.class);
					List<Displayable> connector = layerSet.get(Connector.class);

					// load the line
					String[] content = line.split(";");
					if (content.length > 1)
					{
						long currentConID = Long.parseLong(content[0]);
						ArrayList<Treeline> conTrees = new ArrayList<Treeline>();

						Connector rightConn=null;
						
						for (Displayable conn : connector)
						{
							if (conn.getId() == currentConID)
							{
								rightConn = (Connector) conn;
							}
						}
						
						for (int i = 1; i < content.length; i++)
						{
							long currentID = Long.parseLong(content[i]);
							
							for (Displayable tree : trees)
							{
								if (tree.getId() == currentID && tree.getClass().equals(Treeline.class) && rightConn!=null)
								{
									rightConn.addConTreeline( (Treeline) tree);
								}
							}
						}


						
//						for (int i = 1; i < content.length; i++)
//						{
//							long currentID = Long.parseLong(content[i]);
//							
//							for (Displayable tree : trees)
//							{
//								if (tree.getId() == currentID)
//								{
//									conTrees.add((Treeline) tree);
//								}
//							}
//						}
//						
//						for (Displayable conn : connector)
//						{
//							if (conn.getId() == currentConID)
//							{
//								Connector rightConn = (Connector) conn;
//								rightConn.setConTreelines(conTrees);
//							}
//						}

					}
				}

				// read the next line
				line = br.readLine();
			}
			br.close();
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Main method for saving user settings and connector data
	 * @param file - The project save file
	 * @author Axel
	 */
	public static void addonSaver(File file)
	{
		//save user settings
		saveUserSettings();
		//save connector data
		saveConnectorData(file);
		return;		
	}
	
	/**
	 * Saves the user settings
	 * @author Axel
	 */
	public static void saveUserSettings()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < 11; i++)
		{
			// color
			sb.append(colorToString(confidencColors.get((byte) i)));
			sb.append(";" + treeLineClickable[i]);
			sb.append("\n");
		}
		sb.append(colorToString(confidencColors.get((byte) 11)));

		File userSettingfolder = new File(System.getProperty("user.home") + File.separator + ".rhizoTrakSettings");
		userSettingfolder.mkdirs();
		
		if (!userSettingfolder.exists())
		{
			Utils.log("unable to save user settings");
			return;
		}

		File userSettingFile = new File(System.getProperty("user.home") + File.separator + ".rhizoTrakSettings" + File.separator + "settings");

		writeStringToFile(userSettingFile, sb.toString());
	}
	
	/**
	 * Saves the connector data
	 * @param file - The project save file
	 * @author Axel
	 */
	public static void saveConnectorData(File file)
	{
		StringBuilder saveBuilder = new StringBuilder();
		for (Project pro : Project.getProjects())
		{
			LayerSet layerSet = pro.getRootLayerSet();

			StringBuilder sb = new StringBuilder(); // content of the save file
			// for each Connector
			List<Displayable> connectors = layerSet.get(Connector.class);

			for (int i = 0; i < connectors.size(); i++)
			{
				Connector currentConnector = (Connector) connectors.get(i);
				long id = currentConnector.getId();
				sb.append(id + ";");
				ArrayList<Treeline> conTrees = currentConnector.getConTreelines();
				for (Treeline treeline : conTrees)
				{
					sb.append(treeline.getId() + ";");
				}
				sb.append("\n");
			}
			String saveText = sb.toString();
			saveBuilder.append(saveText);
			saveBuilder.append("###" + "\n");

		}
		String saveText = saveBuilder.toString();

		File conFile = new File(file.getParentFile().getAbsolutePath() + File.separator + file.getName().split("\\.")[0] + ".con");																														// file
		File tempconFile = new File(file.getParentFile().getAbsolutePath() + File.separator + "temp_" + file.getName().split("\\.")[0] + ".con");

		if (conFile.exists())
		{
			String old = readFileToString(conFile); // read current file
			writeStringToFile(tempconFile, old); // and save to temp
		}

		if (!writeStringToFile(conFile, saveText) && tempconFile.exists())
		{
			tempconFile.renameTo(conFile);
		}
	}

	/* helpers below */
	
	/**
	 * 
	 * @param file - File to be read
	 * @return The contents of the file as string
	 * @author Axel
	 */
	public static String readFileToString(File file)
	{
		String result = "";
		StringBuilder sb = new StringBuilder();
		
		try (FileReader fr = new FileReader(file))
		{
			int c = fr.read();
			while (c != -1)
			{
				sb.append((char) c);
				c = fr.read();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		result = sb.toString();
		return result;
	}
	
	
	/**
	 * 
	 * @param file - File to be saved
	 * @param string - String to be written in the file
	 * @return Success
	 * @author Axel
	 */
	public static boolean writeStringToFile(File file, String string)
	{
		try (FileWriter fr = new FileWriter(file))
		{
			file.createNewFile();
			fr.write(string);
			fr.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * Converts color to string for the con file format
	 * @param col
	 * @return Color string
	 * @author Axel
	 */
	public static String colorToString(Color col)
	{
		String result = col.getRed() + ";" + col.getGreen() + ";" + col.getBlue() + ";" + col.getAlpha();
		return result;
	}

	/**
	 * Converts string to color for the con file format
	 * @param string 
	 * @return Color
	 * @author Axel
	 */
	public static Color stringToColor(String string)
	{
		String[] info = string.split(";");
		return new Color(Integer.parseInt(info[0]), Integer.parseInt(info[1]), Integer.parseInt(info[2]), Integer.parseInt(info[3]));
	}

	/**
	 * Splits a string by newlines
	 * @param string - String to be split
	 * @return List of lines
	 * @author Axel
	 */
	public static ArrayList<String> stringToLineArray(String string)
	{
		String[] lines = string.split("\\n");
		return new ArrayList<String>(Arrays.asList(lines));
	}
	
	/* visual stuff */

	/**
	 * Creates a color chooser dialogue and applies colors to treelines with the corresponding confidence value
	 * @param i - Confidence value
	 * @param list - Parent component for the chooser
	 * @author Axel
	 */
	public static void colorChooser(int i, JList list)
	{
		Color newColor = JColorChooser.showDialog(list, "Choose color", Color.WHITE);
		confidencColors.put((byte) i, newColor);
		
		Display display = Display.getFront();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();
		
		// get treelines of current layerset
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
		for (Displayable cObj : trees)
		{
			Treeline ctree = (Treeline) cObj;
			for (Node<Float> cnode : ctree.getRoot().getSubtreeNodes())
			{
				if ((int) cnode.getConfidence() == i)
				{
					cnode.setColor(newColor);
				}
			}
			cObj.repaint();
		}
	}
	
	/**
	 * Updates the color for all treelines and repaints them
	 * @author Axel
	 */
	public static void applyCorrespondingColor() {
		Display display = Display.getFront();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();

		// get treelines of current layerset
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
		for (Displayable cObj : trees) {
			Utils.log("current Treeline: " + cObj.getUniqueIdentifier());
			Treeline ctree = (Treeline) cObj;
			boolean repaint = false;
			if(ctree.getRoot() == null || ctree.getRoot().getSubtreeNodes() == null) continue; 
			
			for (Node<Float> cnode : ctree.getRoot().getSubtreeNodes())
			{
				byte currentConfi = (byte) 1;
				if(cnode.high())
				{
					currentConfi = (byte) 11;
				}
				else
				{
					currentConfi = cnode.getConfidence();
				}

				Color newColor = confidencColors.get(currentConfi);

				if (cnode.getColor() != newColor) {
					cnode.setColor(newColor);
					repaint = true;
					Utils.log("new color on a node");
				}
			}
			if (repaint) {
				cObj.repaint();
			}
		}
	}

	/**
	 * Update the color for the given treeline and repaint
	 * @author Axel
	 */
	public static void applyCorrespondingColorToDisplayable(Displayable disp) {
		if(disp instanceof Treeline)
		{
			Treeline ctree = (Treeline) disp;
			boolean repaint = false;
			if(ctree.getRoot() == null || ctree.getRoot().getSubtreeNodes() == null) return; 

			for (Node<Float> cnode : ctree.getRoot().getSubtreeNodes())
			{
				byte currentConfi = (byte) 1;
				if(cnode.high())
				{
					currentConfi = (byte) 11;
				}
				else
				{
					currentConfi = cnode.getConfidence();
				}

				Color newColor = confidencColors.get(currentConfi);

				if (cnode.getColor() != newColor) {
					cnode.setColor(newColor);
					repaint = true;
					Utils.log("new color on a node");
				}
			}
			if (repaint) {
				ctree.repaint();
			}	
		}
	}
	
	/**
	 * 
	 * @param toBeHigh - Treeline to be highlighted
	 * @author Axel
	 */
	public static void highlight(Displayable toBeHigh)
	{
		if(toBeHigh instanceof Treeline){
			Treeline tree = (Treeline) toBeHigh;
			for (Node<Float> cnode : tree.getRoot().getSubtreeNodes())
			{
				cnode.high(true);
			}
			RhizoAddons.applyCorrespondingColorToDisplayable(tree);	
		}
	}

	/**
	 * 
	 * @param toBeHigh - Treeline to be highlighted
	 * @author Axel
	 */
	public static void highlight(List<Displayable> toBeHigh)
	{		
		for (Displayable disp : toBeHigh)
		{
			highlight(disp);
		}
	}

	/**
	 * Removes highlighting from treeline
	 * @param notToBeHigh - Treeline to be dehighlighted
	 * @author Axel
	 */
	public static void removeHighlight(Displayable notToBeHigh)
	{
		if(notToBeHigh instanceof Treeline){
			Treeline tree = (Treeline) notToBeHigh;
			for (Node<Float> cnode : tree.getRoot().getSubtreeNodes())
			{
				cnode.high(false);
			}	
			RhizoAddons.applyCorrespondingColorToDisplayable(tree);
		}

	}

	/**
	 * Removes highlighting from treelines
	 * @param notToBeHigh - Treelines to be dehighlighted
	 * @author Axel
	 */
	public static void removeHighlight(List<Displayable> toBeHigh)
	{
		for (Displayable disp : toBeHigh)
		{
			if (disp.getClass().equals(Treeline.class))
			{
				removeHighlight((Treeline) disp);
			}
		}
	}

	/**
	 * Opens the color and visibility panel
	 * @author Axel
	 */
	public static void setVisibility()
	{
		colorFrame = new JFrame("Color and visibility panel");
		colorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel temp = guiAddons.visibilityPanel();
		colorFrame.add(temp);
		colorFrame.pack();
		colorFrame.setVisible(true);
	}
	
	
	/* displayable stuff */

	/**
	 * Copies treelines from the current layer to the next one
	 * @author Axel
	 */
	public static void copyTreeLine()
	{
		Display display = Display.getFront();
		// Layer frontLayer = Display.getFrontLayer();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();
		
		Utils.log(currentLayer);
		Utils.log(currentLayerSet.next(currentLayer));
		
		// determine next layer
		Layer nextLayer = currentLayerSet.next(currentLayer);
		//copytreelineconnector
		if (nextLayer == null || nextLayer.getZ()==currentLayer.getZ()) {
			Utils.showMessage("no more layers as target");
			return;
		}
		// get treelines of current layerset
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
		for (Displayable cObj : trees) {
			Treeline ctree = (Treeline) cObj;
			// Utils.log2("current Tree first Layer: " + ctree.getFirstLayer());
			if (ctree.getFirstLayer() == currentLayer) {
				Treeline copy = null;
				try {
					// copy current tree
					copy = Tree.copyAs(ctree, Treeline.class, Treeline.RadiusNode.class);
					copy.setLayer(nextLayer, true);
					for (Node<Float> cnode : copy.getRoot().getSubtreeNodes()) {
						cnode.setLayer(nextLayer);
						cnode.setColor(confidencColors.get(cnode.getConfidence()));
					}
					copy.setTitle("treeline");
					copy.clearState();
					copy.updateCache();
					currentLayerSet.add(copy);
					ctree.getProject().getProjectTree().addSibling(ctree, copy);
					// get the parent connector; if non exists a new will be create
					//copytreelineconnector
					if(!RhizoAddons.getRightPC(ctree, copy)){
						Utils.showMessage("error: couldn't add connector automatically");
					}
					Display.update(currentLayerSet);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * Creates a shortcut for drawing treelines. Currently: Strg+T
	 * @param obj - Component where the shortcut works
	 * @author Axel
	 */
	public static void shortyForTreeLine(JComponent obj)
	{
		@SuppressWarnings("serial")
		Action blaAction = new AbstractAction("bla")
		{
			public void actionPerformed(ActionEvent e)
			{
				// get the relevant stuff
				Display display = Display.getFront();
				Display.clearSelection();
				Project project = display.getProject();
				ProjectTree currentTree = project.getProjectTree();
				
				// try to find if there is ProjectThing that can contain the new treeline
				ProjectThing parent;
				parent = RhizoAddons.findParentAllowing("treeline", project);
				// inform user if no ProjectThing is found
				if (parent == null)
				{
					Utils.showMessage("Project does not contain object that can hold treelines.");
					return;
				}
				// make new treeline
				ProjectThing pt = parent.createChild("treeline");
				pt.setTitle(pt.getUniqueIdentifier());
				// add new treeline to the project tree
				DefaultMutableTreeNode parentNode = DNDTree.findNode(parent, currentTree);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(pt);
				((DefaultTreeModel) currentTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
			}
		};
		obj.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, true), "bla");
		obj.getActionMap().put("bla", blaAction);
	}

	/**
	 * Finds the parent connector
	 * @param ptree - Parent treeline
	 * @param ctree - Current treeline
	 * @return Success
	 * @author Axel
	 */
	private static boolean getRightPC(Treeline ptree, Treeline ctree)
	{
		//copytreelineconnector - two possibilities: a) ptree has no connector, so a new one needs to be created b) ptree has a connector so we can call copyEvent
		if (ptree.getTreeEventListener().size() < 1)
		{
			Node<Float> pTreeRoot = ptree.getRoot();
			Project project = Display.getFront().getProject();
			Connector pCon = project.getProjectTree().tryAddNewConnector(ptree, false);
			if (pCon == null)
			{
				return false;
			}
			Node<Float> newRoot = pCon.newNode(pTreeRoot.getX(), pTreeRoot.getY(), pTreeRoot.getLayer(), null);
			pCon.addNode(null, newRoot, pTreeRoot.getConfidence());
			pCon.setRoot(newRoot);
			pCon.setAffineTransform(ptree.getAffineTransform());
			boolean suc = pCon.addConTreeline(ptree);
			ptree.copyEvent(ctree);
			return suc;
		}
		else
		{
			ptree.copyEvent(ctree);
		}
		return true;
	}
	
	/**
	 * Tool for merging treelines in a more convenient way
	 * @param la - Current layer
	 * @param x_p - Mouse x position
	 * @param y_p - Mouse y position
	 * @param mag - Current magnification
	 * @param anode - Selected active node
	 * @param parentTl - Selected active treeline
	 * @param me - MouseEvent
	 * @author Axel
	 */
	public static void mergeTool(final Layer la, final int x_p, final int y_p, double mag, RadiusNode anode, Treeline parentTl, MouseEvent me)
	{
		Thread mergeRun = new Thread()
		{
			{
				setPriority(Thread.NORM_PRIORITY);
			}

			@Override
			public void run()
			{
				Display display = Display.getFront();
				DisplayCanvas dc = display.getCanvas();
				final Point po = dc.getCursorLoc();
				// Utils.log(display.getActive());
				Displayable oldActive = display.getActive();
				Thread t = RhizoAddons.choose(me.getX(), me.getY(), x_p, y_p, Treeline.class, display);
				t.start();
				try
				{
					t.join();
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Utils.log(display.getActive());
				if (oldActive.equals(display.getActive()))
				{
					Utils.log("found no target");
					return;
				}
				Treeline target = (Treeline) display.getActive();
				if (target == null)
				{
					Utils.log("no active Treeline found");
					return;
				}
				RadiusNode nd = (RadiusNode) target.findClosestNodeW(target.getNodesToPaint(la), po.x, po.y, dc.getMagnification());
				if (nd == null)
				{
					Utils.log("found no target node");
					return;
				}
				if (parentTl.getClass().equals(Treeline.class) == false)
				{
					Utils.log("to-be-parent is no treeline");
					return;
				}
				ArrayList<Tree<Float>> joinList = new ArrayList<>();

				joinList.add(parentTl);

				target.setLastMarked(nd);
				joinList.add(target);
				
				parentTl.join(joinList);
				parentTl.unmark();

				target.deselect();
				
				//get the Connector of the target, remove the target and add the parent treeline
				//furthermore update ConflictManager 
				ArrayList<Connector> connectorList = new ArrayList<Connector>();
				List<TreeEventListener> listenerList = target.getTreeEventListener();
				for(TreeEventListener currentListener: listenerList)
				{
					Connector currentCon = currentListener.getConnector();
					if(currentCon!=null){
						connectorList.add(currentCon);
					}
				}
				for(Connector currentCon: connectorList)
				{
					currentCon.removeConTreeline(target);
					ConflictManager.processChange(target, currentCon);
				}
				
				//targetConnector.removeConTreeline(target);
				//targetConnector.addConTreeline(parentTl);
				
				for(Connector currentCon: connectorList)
				{
					currentCon.addConTreeline(parentTl);
					ConflictManager.processChange(parentTl, currentCon);
				}
				
				display.getProject().remove(target);

			};
		};
		mergeRun.start();
	}
	
	/**
	 * Tool for binding connectors to treelines
	 * @param la - Current layer
	 * @param x_p - Mouse x position
	 * @param y_p - Mouse y position
	 * @param mag - Current magnification
	 * @param parentConnector - Connector to be bound
	 * @param me - MouseEvent
	 * @author Axel
	 */
	public static void bindConnectorToTreeline(final Layer la, final int x_p, final int y_p, double mag, Connector parentConnector, MouseEvent me)
	{
		Thread bindRun = new Thread()
		{
			{
				setPriority(Thread.NORM_PRIORITY);
			}

			@Override
			public void run()
			{
				Display display = Display.getFront();
				DisplayCanvas dc = display.getCanvas();
				final Point po = dc.getCursorLoc();
				// Utils.log(display.getActive());
				Displayable oldActive = display.getActive();
				Thread t = RhizoAddons.choose(me.getX(), me.getY(), x_p, y_p, Treeline.class, display);
				t.start();
				try
				{
					t.join();
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Utils.log(display.getActive());
				if (oldActive.equals(display.getActive()))
				{
					Utils.log("found no target");
					return;
				}
				Treeline target = (Treeline) display.getActive();
				if (target == null)
				{
					Utils.log("no active Treeline found");
					return;
				}
				// check if the treeline is already connected
				for (Treeline tree : parentConnector.getConTreelines())
				{
					if (tree.equals(target))
					{
						// that should happen if the target is already connected to the Connector
						if (Utils.check("Really remove connection between " + parentConnector.getId() + " and " + target.getId() + " ?"))
						{
							parentConnector.removeConTreeline(target);
							ConflictManager.processChange(target, parentConnector);
						}
						display.setActive(parentConnector);
						return;
					}
				}
				parentConnector.addConTreeline(target);
				display.setActive(parentConnector);
				ConflictManager.processChange(target, parentConnector);
			};
		};
		bindRun.start();
	}
	
	
	/* other stuff */
	
	/**
	 * Open image loading dialogue
	 * @author Axel
	 */
	public static void imageLoader()
	{
		imageLoaderFrame = new JFrame("Image Loader");
		imageLoaderFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel temp = guiAddons.imageImport();
		imageLoaderFrame.add(temp);
		imageLoaderFrame.pack();
		imageLoaderFrame.setVisible(true);
	}

	/**
	 * Finds a projecThing that can hold objects of the given type
	 * @param type
	 * @param project - Current project
	 * @return ProjectThing
	 */
	private static ProjectThing findParentAllowing(String type, Project project)
	{
		Enumeration enum_nodes;
		enum_nodes = project.getProjectTree().getRoot().depthFirstEnumeration();
		while (enum_nodes.hasMoreElements())
		{
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enum_nodes.nextElement();
			ProjectThing currentProjectThing = (ProjectThing) currentNode.getUserObject();
			if (currentProjectThing.canHaveAsChild(type))
			{
				return currentProjectThing;
			}
		}
		return null;
	}

	/**
	 * TODO
	 */
	public static void writeStatistic()
	{
		Display display = Display.getFront();
		// Layer frontLayer = Display.getFrontLayer();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();
		// currentLayerSet.updateLayerTree();
		// determine next layer
		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
		ArrayList<Segment> all_segments = new ArrayList<Segment>();
		// for all treelines > calculate things
		for (Displayable cObj : trees)
		{
			Treeline ctree = (Treeline) cObj;
			// traveres tree
			Collection<Node<Float>> all_nodes = ctree.getRoot().getSubtreeNodes();
			for (Node<Float> node : all_nodes)
			{
				if (!node.equals(ctree.getRoot()))
				{
					Segment currentSegment = new Segment((RadiusNode) node, (RadiusNode) node.getParent());
					all_segments.add(currentSegment);
				}
			}
		}
		// write things to a csv
		try
		{
			File saveFile = Utils.chooseFile(System.getProperty("user.home"), null, ".csv");
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
			for (Segment segment : all_segments)
			{
				bw.write(segment.getStatistics());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds images from the load images dialogue and creates new layers
	 * @param files - Array of image files
	 * @author Axel
	 */
	public static void addLayerAndImage(File[] files)
	{
		if (files.length > 0)
		{
//			LayerSet parent = Display.getFrontLayer().getParent();
//			int numberOfLayers = parent.getLayers().size();
//			Utils.log("number of layers: " + numberOfLayers);
//			Layer currentLastLayer = parent.getLayers().get(numberOfLayers - 1);
//			Utils.log("current last: " + currentLastLayer);
//			int lastZ = (int) currentLastLayer.getZ();
//
//			Project project = parent.getProject();
//			Loader loader = project.getLoader();
//
//			for (int e = 1; e < files.length + 1; e++)
//			{
//				Layer layer = new Layer(project, lastZ + e, 1, parent);
//				parent.add(layer);
//				project.getLayerTree().addLayer(parent, layer);
//				parent.recreateBuckets(layer, true);
//				loader.importImage(layer, 0, 0, files[e - 1].getPath(), true);
//				// layer.getDisplayables(Patch.class).get(0).setLocked(true);
//			}
//
//			// TODO: remove all empty layers
//			
			//next try
			LayerSet parent = Display.getFrontLayer().getParent();
			ArrayList<Layer> layerlist = parent.getLayers();
			
			double firstEmptyAtBack=-1;
			double realLast=-1;
			int emptys = 0;
			boolean lastBack=false;
			for (Layer layer : layerlist)
			{
				//check if layer have no patch
				if(layer.getDisplayables(Patch.class).size()<1)
				{
					if(!lastBack)
					{
						firstEmptyAtBack=layer.getZ();
						lastBack=true;
						emptys++;
					}
					else
					{
						emptys++;
					}
					
				}
				else
				{
					lastBack=false;
					firstEmptyAtBack=-1;
					emptys=0;
				}
				realLast = layer.getZ();
			}
			//so firstEmptyAtBack is z of first empty at the end of the stack and emptys is the number of emptyLayers
			
			int numberToAdd = files.length-emptys;
			if(numberToAdd<0)
			{
				numberToAdd =0;
			}
			//so numberToAdd additionally layers are needed
			
			Project project = parent.getProject();
			
			if(firstEmptyAtBack==-1)
			{
				Layer layer = new Layer(project, realLast+1, 1, parent);
				parent.add(layer);
				project.getLayerTree().addLayer(parent, layer);
				parent.recreateBuckets(layer, true);
				firstEmptyAtBack=realLast+1;
				numberToAdd--;
			}
			for(int i=0;i<numberToAdd;i++){
				Layer layer = new Layer(project, firstEmptyAtBack+1+i, 1, parent);
				parent.add(layer);
				project.getLayerTree().addLayer(parent, layer);
				parent.recreateBuckets(layer, true);				
			}
			//now we have enough empty layers starting from z=firstEmptyAtBack
			
			Loader loader = project.getLoader();
			for (File file : files)
			{
				Layer currentLayer =parent.getLayer(firstEmptyAtBack); 
				loader.importImage(currentLayer, 0, 0, file.getPath(), true);
				firstEmptyAtBack++;
			}
			

		}
	}

	/**
	 * Converts connector coordinates to treeline coordinates and vice versa
	 * @param x
	 * @param y
	 * @param start - Source 
	 * @param end - Target
	 * @author Axel
	 * @return Point2D of the converted coordinates
	 */
	public static Point2D changeSpace(float x, float y, AffineTransform start, AffineTransform end)
	{
		Point2D result = new Point2D.Float(x, y);
		result = start.transform(result, null);
		try
		{
			result = end.inverseTransform(result, null);
		} catch (NoninvertibleTransformException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = null;
		}
		return result;
	}
	
	/**
	 * For testing purposes
	 */
	public static void test() 
	{
		// RhizoAddons.test = !RhizoAddons.test;
		Utils.log("Aktueller Zustand: " + RhizoAddons.test);
		Display display = Display.getFront();
		// Layer frontLayer = Display.getFrontLayer();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();
		Project project = display.getProject();
		project.getProjectTree();
//		Utils.log("Status: "+RhizoAddons.mergeActive);
//		// currentLayerSet.updateLayerTree();
//		// determine next layer
//		ArrayList<Displayable> trees = currentLayerSet.get(Treeline.class);
//
//		for (Displayable cObj : trees) {
//			Treeline ctree = (Treeline) cObj;
//			if (ctree.getFirstLayer() == currentLayer) {
//				try {
//					// ctree.repaint();
//					Utils.log2("current Tree first Layer: " + ctree.getFirstLayer());
//					Utils.log2("current Tree Layer Property" + cObj.getLayer());
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			}
//
//		}
	}
	
	
	protected static Thread choose(final int screen_x_p, final int screen_y_p, final int x_p, final int y_p, final Class<?> c, final Display currentDisplay)
	{
		return choose(screen_x_p, screen_y_p, x_p, y_p, false, c, currentDisplay);
	}

	protected static Thread choose(final int screen_x_p, final int screen_y_p, final int x_p, final int y_p, final Display currentDisplay)
	{
		return choose(screen_x_p, screen_y_p, x_p, y_p, false, null, currentDisplay);
	}

	/**
	 * Find a Displayable to add to the selection under the given point (which is in offscreen coords); will use a popup menu to give the user a set of Displayable objects to select from.
	 * @param screen_x_p - Clicked global x coordinate
	 * @param screen_y_p - Clicked global y coordinate
	 * @param x_p - Clicked local x coordinate
	 * @param y_p - Clicked local y coordinate
	 * @param shift_down - Is shift pressed?
	 * @param c - Class of objects to be choosing from
	 * @param currentDisplay
	 * @return Thread created by clicking overlapping nodes
	 * @author Axel
	 */
	protected static Thread choose(final int screen_x_p, final int screen_y_p, final int x_p, final int y_p, final boolean shift_down, final Class<?> c, Display currentDisplay)
	{
		// Utils.log("Display.choose: x,y " + x_p + "," + y_p);
		Thread t = new Thread()
		{
			{
				setPriority(Thread.NORM_PRIORITY);
			}

			@Override
			public void run()
			{
			};
		};
		Layer layer = currentDisplay.getFrontLayer();
		final ArrayList<Displayable> al = new ArrayList<Displayable>(layer.find(x_p, y_p, true));
		al.addAll(layer.getParent().findZDisplayables(layer, x_p, y_p, true)); // only visible ones

		// actyc: remove those trees that contain a non clickable node at xp und yp
		ArrayList<Displayable> alternatedList = new ArrayList<Displayable>();
		for (Displayable displayable : al)
		{
			if (displayable.getClass() == Treeline.class)
			{
				Treeline currentTreeline = (Treeline) displayable;
				double transX = x_p - currentTreeline.getAffineTransform().getTranslateX();
				double transY = y_p - currentTreeline.getAffineTransform().getTranslateY();
				Node<Float> nearestNode = currentTreeline.findNearestNode((float) transX, (float) transY, layer);
				// Utils.log(nearestNode);
				if (RhizoAddons.treeLineClickable[(int) nearestNode.getConfidence()] == false)
				{
					alternatedList.add(displayable);
				}
			}
		}
		al.removeAll(alternatedList);

		if (al.isEmpty())
		{
			final Displayable act = currentDisplay.getActive();
			currentDisplay.clearSelection();
			currentDisplay.getCanvas().setUpdateGraphics(true);
			// Utils.log("choose: set active to null");
			// fixing lack of repainting for unknown reasons, of the active one
			// TODO this is a temporary solution
			if (null != act) Display.repaint(layer, act, 5);
		}
		else if (1 == al.size())
		{
			final Displayable d = (Displayable) al.get(0);
			if (null != c && d.getClass() != c)
			{
				currentDisplay.clearSelection();
				return t;
			}
			currentDisplay.select(d, shift_down);
			// Utils.log("choose 1: set active to " + active);
		} 
		else
		{
			if (al.contains(currentDisplay.getActive()) && !shift_down)
			{
				// do nothing
			}
			else
			{
				if (null != c)
				{
					// check if at least one of them is of class c
					// if only one is of class c, set as selected
					// else show menu
					for (final Iterator<?> it = al.iterator(); it.hasNext();)
					{
						final Object ob = it.next();
						if (ob.getClass() != c)
							it.remove();
					}
					if (0 == al.size())
					{
						// deselect
						currentDisplay.clearSelection();
						return t;
					}
					if (1 == al.size())
					{
						currentDisplay.select((Displayable) al.get(0), shift_down);
						return t;
					}
					// else, choose among the many
				}
				return choose(screen_x_p, screen_y_p, al, shift_down, x_p, y_p, currentDisplay);
			}
			// Utils.log("choose many: set active to " + active);
		}
		
		return t;
	}
	
	private static Thread choose(final int screen_x_p, final int screen_y_p, final Collection<Displayable> al, final boolean shift_down, final int x_p, final int y_p, Display currentDisplay)
	{
		// show a popup on the canvas to choose
		Thread t = new Thread()
		{
			{
				setPriority(Thread.NORM_PRIORITY);
			}

			@Override
			public void run()
			{
				final Object lock = new Object();
				final DisplayableChooser d_chooser = new DisplayableChooser(al, lock);
				final JPopupMenu pop = new JPopupMenu("Select:");
				for (final Displayable d : al)
				{
					final JMenuItem menu_item = new JMenuItem(d.toString());
					menu_item.addActionListener(d_chooser);
					pop.add(menu_item);
					// actyc: try to do something on mouse hoover
					menu_item.addMouseListener(new MouseListener()
					{

						@Override
						public void mouseReleased(MouseEvent e)
						{
							// TODO Auto-generated method stub

						}

						@Override
						public void mousePressed(MouseEvent e)
						{
							// TODO Auto-generated method stub

						}

						@Override
						public void mouseExited(MouseEvent e)
						{
							if (d.getClass().equals(Treeline.class))
							{
								RhizoAddons.removeHighlight((Treeline) d);
							}
						}

						@Override
						public void mouseEntered(MouseEvent e)
						{
							RhizoAddons.highlight( d);

						}

						@Override
						public void mouseClicked(MouseEvent e)
						{
							// TODO Auto-generated method stub

						}

					});
				}

				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						pop.show(currentDisplay.getCanvas(), screen_x_p, screen_y_p);
					}
				});

				// now wait until selecting something
				synchronized (lock)
				{
					do
					{
						try
						{
							lock.wait();
						} catch (final InterruptedException ie)
						{
						}
					} while (d_chooser.isWaiting() && pop.isShowing());
				}

				// grab the chosen Displayable object
				final Displayable d = d_chooser.getChosen();
				// Utils.log("Chosen: " + d.toString());
				if (null == d)
				{
					Utils.log2("Display.choose: returning a null!");
				}
				currentDisplay.select(d, shift_down);
				pop.setVisible(false);

				// fix selection bug: never receives mouseReleased event when
				// the popup shows
				currentDisplay.getMode().mouseReleased(null, x_p, y_p, x_p, y_p, x_p, y_p);

				// actyc: return to the original color
				RhizoAddons.removeHighlight(new ArrayList<Displayable>(al));

				return;
			}
		};
		return t;
	}
	
    /**
     *  Writes the current project to a xmlbeans file. In contrast to the standard TrakEM xml format,  this file may be opened with MiToBo
     *  @author Tino
     */
    public static void writeMTBXML()
	{
		try 
		{
			File saveFile = Utils.chooseFile(System.getProperty("user.home"), null, ".xml");
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
			
			Hashtable<Treeline, int[]> rootsTable = new Hashtable<Treeline, int[]>();
			
			// get layers
			Display display = Display.getFront();	
			LayerSet layerSet = display.getLayerSet();  
			ArrayList<Layer> layers = layerSet.getLayers();
			
			// get image names
			List<Patch> patches = layerSet.getAll(Patch.class);
			ImagePlus imagePlus = patches.get(0).getImagePlus();
			String[] imageNames = imagePlus.getImageStack().getSliceLabels();
			
			// setup xml file --- stack of images = rootProject
			MTBXMLRootProjectDocument xmlRootProjectDocument = MTBXMLRootProjectDocument.Factory.newInstance();
			MTBXMLRootProjectType xmlRootProject = xmlRootProjectDocument.addNewMTBXMLRootProject();
			xmlRootProject.setXsize((int) layers.get(0).getLayerWidth());
			xmlRootProject.setYsize((int) layers.get(0).getLayerHeight());
			xmlRootProject.setXresolution((float) imagePlus.getCalibration().getX(1)); // TODO
			xmlRootProject.setYresolution((float) imagePlus.getCalibration().getY(1)); // TODO
			
			MTBXMLRootImageAnnotationType[] xmlRootSets = new MTBXMLRootImageAnnotationType[layers.size()];

			// all treelines in the project
			List<Displayable> allTreelines = layerSet.get(Treeline.class);
			
			// layer = rootSet
			for(int i = 0; i < layers.size(); i++)
			{
				Layer currentLayer = layers.get(i); 
				MTBXMLRootImageAnnotationType rootSet = MTBXMLRootImageAnnotationType.Factory.newInstance();
				
				rootSet.setImagename(imageNames[i]); // also adds "Software: IrfanView" to the string for some reason
				rootSet.setRootSetID(i);
				
				List<MTBXMLRootType> roots = new ArrayList<MTBXMLRootType>(); // arraylist for convenience
				int rootID = 0;

				// check for each treelines which layer it belongs to - inconvenient but currently the only way to get all treelines in a layer
				for(int j = 0; j < allTreelines.size(); j++)
				{
					Treeline currentTreeline = (Treeline) allTreelines.get(j);

					if(null == currentTreeline.getFirstLayer()) continue;
					
					// if treeline belongs to the current layer, then add it
					if(currentTreeline.getFirstLayer().equals(currentLayer))
					{
						roots.add(treelineToXMLType(currentTreeline, currentLayer, rootID)); 
						rootsTable.put(currentTreeline, new int[]{i, rootID});
						rootID++;
					}
				}
				numberOfTreelinesInLayer(currentLayer, allTreelines);
				rootSet.setRootsArray(roots.toArray(new MTBXMLRootType[numberOfTreelinesInLayer(currentLayer, allTreelines)]));
				xmlRootSets[i] = rootSet;
			}
			xmlRootProject.setCollectionOfImageAnnotationsArray(xmlRootSets);
			
			
			// Connectors in project
			List<Displayable> connectors = layerSet.get(Connector.class);
			
			// connector = rootAssociation
			List<MTBXMLRootAssociationType> rootAssociationList = new ArrayList<MTBXMLRootAssociationType>(); // arraylist for convenience
			
			for(int i = 0; i < connectors.size(); i++)
			{
				Connector currentConnector = (Connector) connectors.get(i);
				
				MTBXMLRootAssociationType rootAssociation = MTBXMLRootAssociationType.Factory.newInstance();
				
				// treeline = rootReference
				List<MTBXMLRootReferenceType> rootReferencesList = new ArrayList<MTBXMLRootReferenceType>(); // arraylist for convenience
				List<Treeline> treelinesOfConnector = treelinesOfConnector(currentConnector);
 				
				
				for(int j = 0; j < treelinesOfConnector.size(); j++) 
				{
					int[] ids = rootsTable.get(treelinesOfConnector.get(j));
					MTBXMLRootReferenceType rootReference = MTBXMLRootReferenceType.Factory.newInstance();
					rootReference.setRootID(ids[1]);
					rootReference.setRootSetID(ids[0]);
					
					rootReferencesList.add(rootReference);
				}
				
				rootAssociation.setRootReferencesArray(rootReferencesList.toArray(new MTBXMLRootReferenceType[treelinesOfConnector.size()]));
				rootAssociationList.add(rootAssociation);
			}
			
			xmlRootProject.setRootAssociationsArray(rootAssociationList.toArray(new MTBXMLRootAssociationType[connectors.size()]));
			
			bw.write(xmlRootProjectDocument.toString());
			bw.close();
			Utils.log("Created xml file - "+saveFile.getAbsolutePath());
				
		} 
		catch (Exception e) 
		{
			Utils.log(e.getMessage());
		}
	}	
    
    /**
     * Converts a given treeline to MTBXML format
     * @param treeline - Treeline to be converted
     * @param currentLayer
     * @param rootId - Current rootID
     * @return MTBXMLRootType
     * @author Tino
     */
	private static MTBXMLRootType treelineToXMLType(Treeline treeline, Layer currentLayer, int rootId)
	{
		MTBXMLRootType xmlRoot = MTBXMLRootType.Factory.newInstance();

		xmlRoot.setRootID(rootId);
		xmlRoot.setStartSegmentID(0); // TODO?
		
		List<Node<Float>> nodes = new ArrayList<Node<Float>>(treeline.getNodesAt(currentLayer)); // arraylist for convenience
		nodes.remove(treeline.getRoot());
		
		MTBXMLRootSegmentType[] rootSegmentsArray = new MTBXMLRootSegmentType[nodes.size()];
		
		for(int i = 0; i < nodes.size(); i++)
		{
			Node<Float> n = nodes.get(i);
			float startRadius = 1; // default if node is not a RadiusNode
			float endRadius = 1; // default if node is not a RadiusNode 
			
			if(!n.equals(treeline.getRoot()))
			{
				if(n instanceof RadiusNode) startRadius = ((RadiusNode) n).getData();
				if(n.getParent() instanceof RadiusNode) endRadius = ((RadiusNode) n.getParent()).getData();
				
				MTBXMLRootSegmentType rootSegment = MTBXMLRootSegmentType.Factory.newInstance();
				rootSegment.setRootID(xmlRoot.getRootID());
				rootSegment.setSegmentID(i); 
				if(n.getParent().equals(treeline.getRoot())) rootSegment.setParentID(-1);
				else rootSegment.setParentID(i-1); // TODO find actual parent segment id via coordinates or save the previous RootSegmentType?

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

	/**
	 * Converts MTBXML to TrakEM project.
	 * Does not work yet!
	 * @author Tino
	 */
	public static void readMTBXML()
	{
		String[] filepath = Utils.selectFile("test");
		File file = new File(filepath[0] + filepath[1]);
		
		while(!filepath[1].contains(".xml"))
		{
			Utils.showMessage("Selected file is not a valid xml file.");
			filepath = Utils.selectFile("test");
			file = new File(filepath[0] + filepath[1]);
		}
		
		try 
		{
			MTBXMLRootProjectDocument rootProjectDocument = MTBXMLRootProjectDocument.Factory.parse(file);
			MTBXMLRootProjectType rootProject = rootProjectDocument.getMTBXMLRootProject();
			MTBXMLRootImageAnnotationType[] rootSets = rootProject.getCollectionOfImageAnnotationsArray();
			
			Display display = Display.getFront();	
			
			Project project = display.getProject();
			ProjectTree projectTree = project.getProjectTree();
			
			LayerSet layerSet = display.getLayerSet();  
			ArrayList<Layer> layers = layerSet.getLayers();
			
			while(rootSets.length != layers.size())
			{
				Utils.showMessage("Number of rootSets in the xml file does not match the number of layers.");
				filepath = Utils.selectFile("test");
				file = new File(filepath[0] + filepath[1]);
				
				rootProjectDocument = MTBXMLRootProjectDocument.Factory.parse(file);
				rootProject = rootProjectDocument.getMTBXMLRootProject();
				rootSets = rootProject.getCollectionOfImageAnnotationsArray();
			}
			
			/* catch exceptions:
			 * - check resolution 
			 * - check image names
			 */
			
			for(int i = 0; i < rootSets.length; i++)
			{
				Layer currentLayer = layers.get(i);
				MTBXMLRootImageAnnotationType currentRootSet = rootSets[i];
				
				ProjectThing possibleParent = findParentAllowing("treeline", project);
				if(possibleParent == null)
				{
				    Utils.showMessage("Project does not contain object that can hold treelines.");
				    return;
				}
				
				MTBXMLRootType[] roots = currentRootSet.getRootsArray();
				
				for(int j = 0; j < roots.length; j++)
				{
					MTBXMLRootType currentRoot = roots[j];
					
					ProjectThing treelineThing = possibleParent.createChild("treeline");
					
					DefaultMutableTreeNode parentNode = DNDTree.findNode(possibleParent, projectTree);
	    			DefaultMutableTreeNode node = new DefaultMutableTreeNode(treelineThing);
	    			((DefaultTreeModel) projectTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
	    			
	    			Treeline treeline = (Treeline) treelineThing.getObject();
	    			treeline.setLayer(currentLayer);
	    			
	    			MTBXMLRootSegmentType[] rootSegments = currentRoot.getRootSegmentsArray();
	    			
	    			for(int k = 0; k < rootSegments.length; k++)
	    			{
	    				MTBXMLRootSegmentType currentRootSegment = rootSegments[k];
	    				MTBXMLRootSegmentPointType xmlStart = currentRootSegment.getStartPoint();
	    				MTBXMLRootSegmentPointType xmlEnd = currentRootSegment.getEndPoint();
	    				
	    				RadiusNode root = null;
	    				RadiusNode currentParent = null;
	    				
	    				// TODO global coordinates to local
	    				// ist erstes Segment wirklich root?
	    				if(currentRootSegment.getParentID() == -1)
	    				{
	    					root = new RadiusNode(xmlStart.getX(), xmlStart.getY(), currentLayer, currentRootSegment.getStartRadius());
	    					currentParent = new RadiusNode(xmlEnd.getX(), xmlEnd.getY(), currentLayer, currentRootSegment.getEndRadius());
	    					
	    					Utils.log("if "+k);
	    					Utils.log("added root: " + treeline.addNode(null, root, (byte) 1)); // TODO confidence
	    					Utils.log("added child: " + treeline.addNode(root, currentParent, (byte) 1)); // TODO confidence
	    					treeline.setRoot(root);
	    					
	    				}
	    				else
	    				{
	    					RadiusNode child = new RadiusNode(xmlEnd.getX(), xmlEnd.getY(), currentLayer, currentRootSegment.getEndRadius());
	    					Utils.log("else "+k);
	    					Utils.log("added child: " + treeline.addNode(currentParent, child, (byte) 1)); 
	    					
	    					currentParent = child;
	    				}
	    				treeline.repaint();
//	    				treeline.clearState();
	    				treeline.updateCache();
	    			}
	    			
	    			Utils.log("treeline"+j+"; set layer at "+treeline.getLayer() + " " + treeline.getFirstLayer());
	    			Utils.log(treeline.getNodesAt(currentLayer).size()); 
				}
			}
		}
		catch (Exception e) 
		{
			Utils.log(e.getMessage());
			Utils.log(e.getLocalizedMessage());
		} 

		
	}
	
	/**
	 * Returns the list of all treelines under the origin and under the targets. Used for reading MTBXML formats
	 * @param c - Connector
	 * @return List of treelines
	 * @author Tino
	 */
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

    /**
     * Returns the number of treelines in a layer
     * @param l - Current layer
     * @param treelines - List of all treelines 
     * @return Number of treelines in the given layer
     * @author Tino
     */
    private static int numberOfTreelinesInLayer(Layer l, List<Displayable> treelines)
    {
    	int res = 0;
    	
    	for(int j = 0; j < treelines.size(); j++)
		{
			Treeline currentTreeline = (Treeline) treelines.get(j);
			
			if(null == currentTreeline.getFirstLayer()) continue;
			if(currentTreeline.getFirstLayer().equals(l)) res++;
		}
    	
    	return res;
    }
    
    public static JFrame getColorVisbilityFrame()
    {
    	return colorFrame;
    }
    
    public static JFrame getImageLoaderFrame()
    {
    	return imageLoaderFrame;
    }
}




/**
 * Segment class for writing statistics. Not used yet.
 * @author Axel
 *
 */
class Segment
{
	RadiusNode child;
	RadiusNode parent;
	// infos
	float length;
	float avgRadius;
	float surfaceArea;
	float volume;
	int numberOfChildren;
	int state;
	float minRadius = 1;
	float r1 = minRadius;
	float r2 = minRadius;

	public Segment(RadiusNode self, RadiusNode parent)
	{
		this.state = child.getConfidence();
		this.child = self;
		this.parent = parent;
		this.length = (float) Math.sqrt(Math.pow(child.getX() - parent.getX(), 2) + Math.pow(child.getX() - parent.getX(), 2));
		if (parent.getData() > 0)
		{
			this.r1 = parent.getData();
		}
		if (child.getData() > 0)
		{
			this.r2 = child.getData();
		}
		this.avgRadius = (r1 + r2) / 2;
		float s = (float) Math.sqrt(Math.pow((r1 - r2), 2) + Math.pow(this.length, 2));
		this.surfaceArea = (float) (Math.PI * Math.pow(r1, 2) + Math.PI * Math.pow(r2, 2) + Math.PI * s * (r1 + r2));
		this.volume = (float) ((Math.PI * length * (Math.pow(r1, 2) + Math.pow(r1, 2) + r1 * r2)) / 3);
	}

	public RadiusNode getChild()
	{
		return child;
	}

	public RadiusNode getParent()
	{
		return parent;
	}

	public float getLength()
	{
		return length;
	}

	public float getAvgRadius()
	{
		return avgRadius;
	}

	public float getSurfaceArea()
	{
		return surfaceArea;
	}

	public float getVolume()
	{
		return volume;
	}

	public int getNumberOfChildren()
	{
		return numberOfChildren;
	}

	public int getState()
	{
		return state;
	}

	public String getStatistics()
	{
		String result = Integer.toString(state) + ";" + Float.toString(length) + ";" + Float.toString(avgRadius) + ";" + Float.toString(surfaceArea);
		result = result + ";" + Float.toString(volume) + ";" + Integer.toString(numberOfChildren);
		return result;
	}
}
