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

package de.unihalle.informatik.rhizoTrak.display;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.esotericsoftware.kryo.util.Util;

import de.unihalle.informatik.MiToBo_xml.MTBXMLRootAssociationType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootImageAnnotationType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootProjectDocument;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootProjectType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootReferenceType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentPointType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootSegmentType;
import de.unihalle.informatik.MiToBo_xml.MTBXMLRootType;
import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.config.Config;
import de.unihalle.informatik.rhizoTrak.config.Config.StatusList;
import de.unihalle.informatik.rhizoTrak.config.Config.StatusList.Status;
import de.unihalle.informatik.rhizoTrak.config.GlobalSettings;
import de.unihalle.informatik.rhizoTrak.config.GlobalSettings.GlobalStatusList.GlobalStatus;
import de.unihalle.informatik.rhizoTrak.config.GlobalSettings.GlobalStatusList;
import de.unihalle.informatik.rhizoTrak.conflictManagement.ConflictManager;
import de.unihalle.informatik.rhizoTrak.display.Treeline.RadiusNode;
import de.unihalle.informatik.rhizoTrak.display.addonGui.ImageImport;
import de.unihalle.informatik.rhizoTrak.display.addonGui.VisibilityPanel;
import de.unihalle.informatik.rhizoTrak.persistence.Loader;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;

public class RhizoAddons
{
	 boolean chooseReady = false;
	
	public  boolean splitDialog = false;
	
	public  File imageDir = null;

	static boolean test = true;

	public String relativPatchDir="/_images";

	public static File userSettingsFile = new File(System.getProperty("user.home") + File.separator + ".rhizoTrakSettings" + File.separator + "settings.xml");
	
	public List<GlobalStatus> globalStatusList = new ArrayList<GlobalStatus>();
	
	// used for drawing, GUI and save/load operations
	public LinkedHashMap<Integer, Status> statusMap = new LinkedHashMap<Integer, Status>();
	
	public static final int FIXEDSTATUSSIZE = 3;
	
	public  Node lastEditedOrActiveNode = null;
	
	private  JFrame colorFrame, imageLoaderFrame;
	
	static AddonGui guiAddons;

	private ConflictManager conflictManager = null;

	public Project project = null;

	public RhizoAddons(Project project) 
	{
		this.project = project;
		this.conflictManager = new ConflictManager(this);
	}

	public void setConflictManager(ConflictManager conflictManager) 
	{
		this.conflictManager = conflictManager;
	}

	public ConflictManager getConflictManager()
	{
		return conflictManager;
	}

	/**
	 * Calls load methods when opening a project
	 * @param file - saved project file
	 * @author Axel
	 */
	public Thread addonLoader(File file,Project project)
	{
		Thread loader = new Thread()
		{
			{
				setPriority(Thread.NORM_PRIORITY);
			}
			@Override
			public void run()
			{
				//set imgDir
				imageDir = file.getParentFile();
				// load connector data
				Utils.log2("loading user settings...");
				loadUserSettings();
				Utils.log2("done");

				Utils.log2("loading connector data...");
				loadConnector(file);
				Utils.log2("done");
				
				Utils.log2("restoring conflicts...");
                                //TODO: have to be restored for every Project
				conflictManager.restorConflicts(project);
				Utils.log2("done");
				
				Utils.log2("restoring status conventions...");
				loadConfigFile(file.getAbsolutePath());
				Utils.log2("done");
                                
				//lock all images
				lockAllImagesInAllProjects();
				
				//
				
				return;
			}

		};
		
		// start the thread
		loader.start();
		Utils.log2("return loader");
		return loader;
	}
	
	/**
	 * Loads the user settings (color, visibility etc.)
	 * @author Axel, Tino
	 */
	public void loadUserSettings()
	{
		if (!userSettingsFile.exists())
		{
			setDefaultGlobalStatus();
			Utils.log("unable to load user settings: file not found");
			return;
		}

		try 
		{
			JAXBContext context = JAXBContext.newInstance(GlobalSettings.class);
                        Unmarshaller um = context.createUnmarshaller();
                        GlobalSettings gs = (GlobalSettings) um.unmarshal(userSettingsFile);
                        globalStatusList.addAll(gs.getGlobalStatusList().getGlobalStatus());
                        Utils.log(globalStatusList.size());
	        
	        updateStatusMap();
		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Updates the local status map with the global user settings
	 * @author Tino
	 */
	public void updateStatusMap() 
	{
		for(int i: statusMap.keySet())
		{
			Status s = statusMap.get(i);
			
			for(GlobalStatus gs: globalStatusList)
			{
				if(s.getFullName().equals(gs.getFullName()))
				{
					Status sTemp = s;
					sTemp.setRed(gs.getRed());
					sTemp.setGreen(gs.getGreen());
					sTemp.setBlue(gs.getBlue());
					sTemp.setAlpha(gs.getAlpha());
					sTemp.setSelectable(gs.isSelectable());
					statusMap.put(i, sTemp);
				}
			}
		}
		
	}


	/**
	 * 
	 * Loads the user settings on project file level.
	 * @param path - The project file
	 * @author Tino
	 */
	public void loadConfigFile(String path)
	{
		// TODO: add popup warnings
		// New project..
		if(null == path) // user cancelled the open file dialog
		{
			setDefaultStatus();
			return;
		}
		
		// TODO: check file ending if coming from file chooser
		// Open project..
		File configFile = new File(path.replace(".xml", ".cfg")); // looking for cfg file in directory
		
		if(!configFile.exists())
		{
			setDefaultStatus();
			return;
		}
		
		
		try 
		{
			JAXBContext context = JAXBContext.newInstance(Config.class);
	        Unmarshaller um = context.createUnmarshaller();
	        Config config = (Config) um.unmarshal(configFile);
	        List<Status> sl = config.getStatusList().getStatus();
	        
	        for(int i = 0; i < sl.size(); i++)
	        {
	        	statusMap.put(i, sl.get(i));
	        }
	        
	        setFixedStatus();
	        updateStatusMap();
		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
		}

		Node.MAX_EDGE_CONFIDENCE = getStatusMapSize();
	}
	
    //TODO: needs to be recode for non-static behavior
    /**
     * Loads the connector file
     *
     * @param file - The project save file
     * @author Axel
     */
    public void loadConnector(File file) {
        // read the save file
        File conFile = new File(file.getParentFile().getAbsolutePath() + File.separator + file.getName().replace(".xml", ".con"));

        if (!conFile.exists()) {
            // no con file create a new
            try {
                conFile.createNewFile();
                return;
            } catch (IOException e) {
                Utils.log("error: no *.con file found creating new one");
                e.printStackTrace();
                return;
            }
        }

        FileReader fr;
        try {
            fr = new FileReader(conFile);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while (line != null) {
                if (line.equals("###")) {
                    line = br.readLine();
                    break;
                }

                if (project != null) {
                    LayerSet layerSet = project.getRootLayerSet();
                    List<Displayable> trees = layerSet.get(Treeline.class);
                    List<Displayable> connector = layerSet.get(Connector.class);

                    // load the line
                    String[] content = line.split(";");
                    if (content.length > 1) {
                        long currentConID = Long.parseLong(content[0]);
                        ArrayList<Treeline> conTrees = new ArrayList<Treeline>();

                        Connector rightConn = null;

                        for (Displayable conn : connector) {
                            if (conn.getId() == currentConID) {
                                rightConn = (Connector) conn;
                            }
                        }

                        for (int i = 1; i < content.length; i++) {
                            long currentID = Long.parseLong(content[i]);

                            for (Displayable tree : trees) {
                                if (tree.getId() == currentID && tree.getClass().equals(Treeline.class) && rightConn != null) {
                                    rightConn.addConTreeline((Treeline) tree);
                                }
                            }
                        }
                    }
                }

                // read the next line
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
        /**
         * Methods to lock all images/patches on project opening
         * @author Axel
         */
        private static void lockAllImagesInAllProjects()
        {
            List<Project> projects = Project.getProjects();
            projects.stream().forEach((project) -> {
                lockAllImageInLayerSet(project.getRootLayerSet());
            });
        }
        
        private static void lockAllImageInLayerSet(LayerSet layerSet)
        {
            List<Displayable> patches = layerSet.getDisplayables(Patch.class);
            
            if(patches.size() == 0){return;}
            
            patches.stream().forEach((patch) -> {
                patch.setLocked(true);
            });
        }
        
	/**
	 * Main method for saving user settings and connector data
	 * @param file - The project save file
	 * @author Axel
	 */
	public void addonSaver(File file)
	{
		//save user settings
		saveUserSettings();
		//save connector data
		saveConnectorData(file);
		saveConfigFile(file);
		
		return;		
	}
	
	/**
	 * Crates a new config file (.cfg) or overwrites an existing one in the same directory and with the same name as the project file. 
	 * 
	 * @param file - The project xml file
	 * @author Tino
	 */
	public void saveConfigFile(File file) 
	{
		Utils.log(file.getAbsolutePath());
		// TODO: add warnings
		if(null == file) return;
		
		File configFile = new File(file.getAbsolutePath().replace(".xml", ".cfg"));
		
		try 
		{
			JAXBContext context = JAXBContext.newInstance(Config.class);
                        Marshaller m = context.createMarshaller();
                        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        
	        StatusList sl = new StatusList();
	        for(int i: statusMap.keySet())
	        {
	        	// ignore undefined, virtual and connector
	        	if(i >= 0) sl.getStatus().add(statusMap.get(i));
	        }

	        Config config = new Config();
	        config.setStatusList(sl);
	        
	        m.marshal(config, configFile);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Saves the global user settings in the users home folder.
	 * @author Axel, Tino
	 */
	public void saveUserSettings()
	{
		// string list for easy comparisons
		List<String> temp = new ArrayList<String>();
		
		// global status list
		for(GlobalStatus s: globalStatusList)
		{
			temp.add(s.getFullName());
		}
		
		// local status list
		for(Status s: statusMap.values())
		{
			if(!temp.contains(s.getFullName())) // add new global status
			{
				GlobalStatus gStatus = new GlobalStatus();
				gStatus.setAbbreviation(s.getAbbreviation());
				gStatus.setFullName(s.getFullName());
				gStatus.setRed(s.getRed());
				gStatus.setGreen(s.getGreen());
				gStatus.setBlue(s.getBlue());
				gStatus.setAlpha(s.getAlpha());
				gStatus.setSelectable(s.isSelectable());
			
				globalStatusList.add(gStatus);
			}
			else // update existing global status
			{
				for(GlobalStatus g: globalStatusList)
				{
					if(g.getFullName().equals(s.getFullName()))
					{
						g.setAbbreviation(s.getAbbreviation());
						g.setRed(s.getRed());
						g.setGreen(s.getGreen());
						g.setBlue(s.getBlue());
						g.setAlpha(s.getAlpha());
						g.setSelectable(s.isSelectable());
					}
				}
			}
		}
		
		try
		{
			if(!userSettingsFile.getParentFile().exists()) userSettingsFile.getParentFile().mkdirs();

			JAXBContext context = JAXBContext.newInstance(GlobalSettings.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			
	        GlobalStatusList gsl = new GlobalStatusList();
	        gsl.getGlobalStatus().addAll(globalStatusList);
	        
	        GlobalSettings gs = new GlobalSettings();
	        gs.setGlobalStatusList(gsl);
			
			m.marshal(gs, userSettingsFile);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
    /**
     * Saves the connector data
     *
     * @param file - The project save file
     * @author Axel
     */
    public void saveConnectorData(File file) {
        LayerSet layerSet = project.getRootLayerSet();

        StringBuilder sb = new StringBuilder(); // content of the save file
        List<Displayable> connectors = layerSet.get(Connector.class);

        for (int i = 0; i < connectors.size(); i++) {
            Connector currentConnector = (Connector) connectors.get(i);
            long id = currentConnector.getId();
            sb.append(id + ";");
            ArrayList<Treeline> conTrees = currentConnector.getConTreelines();
            for (Treeline treeline : conTrees) {
                sb.append(treeline.getId() + ";");
            }
            sb.append("\n");
        }
        sb.append("###" + "\n");
        String saveText = sb.toString();

        File conFile = new File(file.getParentFile().getAbsolutePath() + File.separator + file.getName().split("\\.")[0] + ".con");																														// file
        File tempconFile = new File(file.getParentFile().getAbsolutePath() + File.separator + "temp_" + file.getName().split("\\.")[0] + ".con");

        if (conFile.exists()) {
            String old = readFileToString(conFile); // read current file
            writeStringToFile(tempconFile, old); // and save to temp
        }

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

	/**
	 * Sets up the default global status list. Only gets called if no settings file exists in the users home folder.
	 * @author Tino
	 */
	public void setDefaultGlobalStatus() 
	{
		GlobalStatus undefined = new GlobalStatus();
		undefined.setFullName("UNDEFINED");
		undefined.setAbbreviation("U");
		undefined.setRed(BigInteger.valueOf(0));
		undefined.setGreen(BigInteger.valueOf(255));
		undefined.setBlue(BigInteger.valueOf(255));
		undefined.setAlpha(BigInteger.valueOf(255));
		undefined.setSelectable(true);
		globalStatusList.add(undefined);
		
		GlobalStatus connector = new GlobalStatus();
		connector.setFullName("CONNECTOR");
		connector.setAbbreviation("C");
		connector.setRed(BigInteger.valueOf(0));
		connector.setGreen(BigInteger.valueOf(255));
		connector.setBlue(BigInteger.valueOf(255));
		connector.setAlpha(BigInteger.valueOf(255));
		connector.setSelectable(true);
		globalStatusList.add(connector);
		
		GlobalStatus virtual = new GlobalStatus();
		virtual.setFullName("VIRTUAL");
		virtual.setAbbreviation("V");
		virtual.setRed(BigInteger.valueOf(0));
		virtual.setGreen(BigInteger.valueOf(255));
		virtual.setBlue(BigInteger.valueOf(255));
		virtual.setAlpha(BigInteger.valueOf(255));
		virtual.setSelectable(true);
		globalStatusList.add(virtual);
		
		GlobalStatus living = new GlobalStatus();
		living.setFullName("LIVING");
		living.setAbbreviation("L");
		living.setRed(BigInteger.valueOf(255));
		living.setGreen(BigInteger.valueOf(255));
		living.setBlue(BigInteger.valueOf(0));
		living.setAlpha(BigInteger.valueOf(255));
		living.setSelectable(true);
		globalStatusList.add(living);
		
		GlobalStatus dead = new GlobalStatus();
		dead.setFullName("DEAD");
		dead.setAbbreviation("D");
		dead.setRed(BigInteger.valueOf(255));
		dead.setGreen(BigInteger.valueOf(255));
		dead.setBlue(BigInteger.valueOf(0));
		dead.setAlpha(BigInteger.valueOf(255));
		dead.setSelectable(true);
		globalStatusList.add(dead);
		
		GlobalStatus decayed = new GlobalStatus();
		decayed.setFullName("DECAYED");
		decayed.setAbbreviation("Y");
		decayed.setRed(BigInteger.valueOf(255));
		decayed.setGreen(BigInteger.valueOf(255));
		decayed.setBlue(BigInteger.valueOf(0));
		decayed.setAlpha(BigInteger.valueOf(255));
		decayed.setSelectable(true);
		globalStatusList.add(decayed);
		
		GlobalStatus gap = new GlobalStatus();
		gap.setFullName("GAP");
		gap.setAbbreviation("G");
		gap.setRed(BigInteger.valueOf(255));
		gap.setGreen(BigInteger.valueOf(255));
		gap.setBlue(BigInteger.valueOf(0));
		gap.setAlpha(BigInteger.valueOf(255));
		gap.setSelectable(true);
		globalStatusList.add(gap);
	}
	
	/**
	 * Sets up the default status map. Only gets called if the user cancels the file dialog to select a .cfg file or
	 * no .cfg file is found when opening a project.
	 * @author Tino
	 */
	public void setDefaultStatus() 
	{
		
		List<Status> statusList = new ArrayList<Status>();
		Status living = new Status();
		living.setFullName("LIVING");
		living.setAbbreviation("L");
		living.setRed(BigInteger.valueOf(255));
		living.setGreen(BigInteger.valueOf(255));
		living.setBlue(BigInteger.valueOf(0));
		living.setAlpha(BigInteger.valueOf(255));
		living.setSelectable(true);
		statusList.add(living);
		
		Status dead = new Status();
		dead.setFullName("DEAD");
		dead.setAbbreviation("D");
		dead.setRed(BigInteger.valueOf(255));
		dead.setGreen(BigInteger.valueOf(255));
		dead.setBlue(BigInteger.valueOf(0));
		dead.setAlpha(BigInteger.valueOf(255));
		dead.setSelectable(true);
		statusList.add(dead);
		
		Status decayed = new Status();
		decayed.setFullName("DECAYED");
		decayed.setAbbreviation("Y");
		decayed.setRed(BigInteger.valueOf(255));
		decayed.setGreen(BigInteger.valueOf(255));
		decayed.setBlue(BigInteger.valueOf(0));
		decayed.setAlpha(BigInteger.valueOf(255));
		decayed.setSelectable(true);
		statusList.add(decayed);
		
		Status gap = new Status();
		gap.setFullName("GAP");
		gap.setAbbreviation("G");
		gap.setRed(BigInteger.valueOf(255));
		gap.setGreen(BigInteger.valueOf(255));
		gap.setBlue(BigInteger.valueOf(0));
		gap.setAlpha(BigInteger.valueOf(255));
		gap.setSelectable(true);
		statusList.add(gap);
		
		for(int i = 0; i < statusList.size(); i++)
		{
			statusMap.put(i, statusList.get(i));
		}
		
		setFixedStatus();
	}
	
	/**
	 * Puts the fixed status UNDEFINED, VIRTUAL and CONNECTOR to the status map. Always included.
	 * @author Tino
	 */
	private void setFixedStatus()
	{
		// Standard status - always included
		Status undefined = new Status();
		undefined.setFullName("UNDEFINED");
		undefined.setAbbreviation("U");
		undefined.setRed(BigInteger.valueOf(255));
		undefined.setBlue(BigInteger.valueOf(255));
		undefined.setGreen(BigInteger.valueOf(0));
		undefined.setAlpha(BigInteger.valueOf(255));
		statusMap.put(-1, undefined);

		Status virtual = new Status();
		virtual.setFullName("VIRTUAL");
		virtual.setAbbreviation("V");
		virtual.setRed(BigInteger.valueOf(255));
		virtual.setBlue(BigInteger.valueOf(255));
		virtual.setGreen(BigInteger.valueOf(0));
		virtual.setAlpha(BigInteger.valueOf(255));
		statusMap.put(-2, virtual);

		Status connector = new Status();
		connector.setFullName("CONNECTOR");
		connector.setAbbreviation("C");
		connector.setRed(BigInteger.valueOf(255));
		connector.setBlue(BigInteger.valueOf(255));
		connector.setGreen(BigInteger.valueOf(0));
		connector.setAlpha(BigInteger.valueOf(255));
		statusMap.put(-3, connector);
	}

	/* helpers below */
	
	/**
	 * Converts RGBA from a status object to a color object.
	 * @param i - Node confidence value
	 * @return A color object with the retrieved RGBA values.
	 * @author Tino
	 */
	public Color getColorFromStatusMap(int i)
	{
		Status s = statusMap.get(i);
		if(null == s) return Color.BLACK;
		return new Color(s.getRed().intValue(), s.getGreen().intValue(), s.getBlue().intValue(), s.getAlpha().intValue());
	}
	
	
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
				Color newColor = getColorFromStatusMap(currentConfi);

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
			Display.repaint(Display.getFrontLayer());
			//tree.repaint();	
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
        Display.repaint(Display.getFrontLayer());

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
	 * Opens the color and visibility panel
	 * @author Axel
	 */
	public void setVisibility()
	{
		colorFrame = new JFrame("Color & Visibility: " + project.getTitle());
		
		colorFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JPanel temp = new VisibilityPanel(this);
		//JPanel temp = guiAddons.visibilityPanel();
		colorFrame.add(temp);
		colorFrame.setSize(320, 450);
//		colorFrame.pack();
		colorFrame.setVisible(true);
	}
	
	
	/* displayable stuff */

	/**
	 * Copies treelines from the current layer to the next one
	 * @author Axel
	 */
	public void copyTreeLine()
	{
		Display display = Display.getFront();
		// Layer frontLayer = Display.getFrontLayer();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();
		
		
		
//		Utils.log(currentLayer);
//		Utils.log(currentLayerSet.next(currentLayer));
		
		// determine next layer
		Layer nextLayer = currentLayerSet.next(currentLayer);
		//copytreelineconnector
		if (nextLayer == null || nextLayer.getZ()==currentLayer.getZ()) {
			Utils.showMessage("Can't copy. This is the last layer.");
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
						Color col = getColorFromStatusMap(cnode.getConfidence());
						cnode.setColor(col);
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
			pCon.addNode(null, newRoot, (byte) -3); // aeekz - TODO: -3 does not work why? 
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
	
	public static Connector giveNewConnector(Treeline target,Treeline model)
	{
		if(model == null)
		{
			model = target;
		}
		if(!target.getTreeEventListener().isEmpty())
		{
			//already own a connector
			return null;
		}
			
		Node<Float> pTreeRoot = target.getRoot();
		Project project = Display.getFront().getProject();
		Connector con = project.getProjectTree().tryAddNewConnector(model, false);
		
		if (con == null)
		{
			//something went wrong
			return null;
		}
		
		Node<Float> newRoot = con.newNode(pTreeRoot.getX(), pTreeRoot.getY(), pTreeRoot.getLayer(), null);
		con.addNode(null, newRoot, pTreeRoot.getConfidence());
		con.setRoot(newRoot);
		con.setAffineTransform(target.getAffineTransform());
		
		boolean suc = con.addConTreeline(target);
		
		if(!suc)
		{
			//something went wrong
			return null;
		}
		
		return con;
	}
	
	public static void transferConnector(Treeline donor, Treeline acceptor)
	{
		List<TreeEventListener> listenerList = new ArrayList<TreeEventListener>(donor.getTreeEventListener());
		for(TreeEventListener currentListener: listenerList)
		{
			Connector currentConnector = currentListener.getConnector();
			currentConnector.removeConTreeline(donor);
			currentConnector.addConTreeline(acceptor);
		}
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
	public void mergeTool(final Layer la, final int x_p, final int y_p, double mag, RadiusNode anode, Treeline parentTl, MouseEvent me)
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
				Thread t = choose(me.getX(), me.getY(), x_p, y_p, Treeline.class, display);
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
					parentTl.unmark();
					return;
				}
				Treeline target = (Treeline) display.getActive();
				if (target == null)
				{
					Utils.log("no active Treeline found");
					parentTl.unmark();
					return;
				}
				RadiusNode nd = (RadiusNode) target.findClosestNodeW(target.getNodesToPaint(la), po.x, po.y, dc.getMagnification());
				if (nd == null)
				{
					Utils.log("found no target node");
					parentTl.unmark();
					return;
				}
				if (parentTl.getClass().equals(Treeline.class) == false)
				{
					Utils.log("to-be-parent is no treeline");
					parentTl.unmark();
					return;
				}

				display.setActive(parentTl);
				
				ArrayList<Tree<Float>> joinList = new ArrayList<>();

				joinList.add(parentTl);

				target.setLastMarked(nd);
				joinList.add(target);
				
				parentTl.join(joinList);
				parentTl.unmark();

				//target.deselect();
				
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
                                        //RhizoAddons rhizoAddons = currentCon.getProject().getRhizoAddons();
                                        //ConflictManager conflictManager = this.getConflictManager();
					conflictManager.processChange(target, currentCon);
				}
				
				//targetConnector.removeConTreeline(target);
				//targetConnector.addConTreeline(parentTl);
				
				for(Connector currentCon: connectorList)
				{
					currentCon.addConTreeline(parentTl);
                                        //RhizoAddons rhizoAddons = currentCon.getProject().getRhizoAddons();
                                        //ConflictManager conflictManager = this.getConflictManager();
					conflictManager.processChange(parentTl, currentCon);
				}
				

				target.remove2(false);
                                Display.updateVisibleTabs();
				
				Display.repaint(display.getLayerSet());
				
				//display.getProject().remove(target);

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
	public void bindConnectorToTreeline(final Layer la, final int x_p, final int y_p, double mag, Connector parentConnector, MouseEvent me)
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
                                RhizoAddons rhizoAddons = display.getProject().getRhizoAddons();
                                ConflictManager conflictManager = rhizoAddons.getConflictManager();
				DisplayCanvas dc = display.getCanvas();
				final Point po = dc.getCursorLoc();
				// Utils.log(display.getActive());
				Displayable oldActive = display.getActive();
				Thread t = choose(me.getX(), me.getY(), x_p, y_p, Treeline.class, display);
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
							conflictManager.processChange(target, parentConnector);
						}
						display.setActive(parentConnector);
						return;
					}
				}
				parentConnector.addConTreeline(target);
				display.setActive(parentConnector);
				conflictManager.processChange(target, parentConnector);
			};
		};
		bindRun.start();
	}
	
	
	/* other stuff */
	
	/**
	 * Open image loading dialogue
	 * @author Axel
	 */
	public void imageLoader()
	{
		String title = "Image Loader";
		if(null != imageDir) title = "Image Loader - " + imageDir.getAbsolutePath();
		
		imageLoaderFrame = new JFrame(title);
		imageLoaderFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel temp = new ImageImport(this);
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
	 * @author Tino
	 * @param t - a treeline
	 * @return - The patch t is displayed on
	 */
	private static Patch getPatch(Treeline t)
	{
		Layer layer = t.getFirstLayer();
		LayerSet layerSet = layer.getParent();
		List<Patch> patches = layerSet.getAll(Patch.class);
		
		for(Patch patch: patches)
		{
			if(patch.getLayer().getZ() == layer.getZ()) return patch;
		}
		
		return null;
	}

	/**
	 * TODO return/error messages
	 */
	public void writeStatistics()
	{
		String[] choices1 = {"{Tab}" , "{;}", "{,}", "Space"};
		String[] choices1_ = {"\t", ";", ",", " "};
		String[] choices2 = {"Current layer only", "All layers"};
		String[] choices3 = {"pixel", "inch", "mm"};
		
		JComboBox<String> combo1 = new JComboBox<String>(choices1);
		JComboBox<String> combo2 = new JComboBox<String>(choices2);
		JComboBox<String> combo3 = new JComboBox<String>(choices3);
		
		JPanel statChoicesPanel = new JPanel();
		statChoicesPanel.setLayout(new GridLayout(3, 2, 0, 10));
		statChoicesPanel.add(new JLabel("Separator "));
		statChoicesPanel.add(combo1);
		statChoicesPanel.add(new JLabel("Output type "));
		statChoicesPanel.add(combo2);
		statChoicesPanel.add(new JLabel("Unit "));
		statChoicesPanel.add(combo3);

		int result = JOptionPane.showConfirmDialog(null, statChoicesPanel, "Statistics Output Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		String sep = "";
		String outputType = "";
		String unit = "";
		if(result == JOptionPane.OK_OPTION)
		{
			sep = choices1_[Arrays.asList(choices1).indexOf(combo1.getSelectedItem())];
			outputType = (String) combo2.getSelectedItem();
			unit = (String) combo3.getSelectedItem();
			
			if(sep.equals("") || outputType.equals("") || unit.equals("")) return;
		}
		else return;

		Display display = Display.getFront();
		Layer currentLayer = display.getLayer();
		LayerSet currentLayerSet = currentLayer.getParent();

//		Utils.log(currentLayerSet.getAll(Patch.class).size());
//		Utils.log(currentLayerSet.get(Treeline.class).size());
//		Utils.log(currentLayerSet.get(Connector.class).size());
		
		List<Displayable> processedTreelines = new ArrayList<Displayable>();
		List<Displayable> trees = null;
		List<Segment> allSegments = new ArrayList<Segment>();

		if(outputType.equals("All layers")) trees = currentLayerSet.get(Treeline.class);
		else trees = filterTreelinesByLayer(currentLayer, currentLayerSet.get(Treeline.class));

		List<Displayable> connectors = currentLayerSet.get(Connector.class);

		for(Displayable cObj: connectors)
		{
			Connector c = (Connector) cObj;

			List<Treeline> treelines = c.getConTreelines();

			for(Treeline ctree: treelines)
			{
				if(null == ctree || null == ctree.getRoot()) continue;
				if(processedTreelines.contains(ctree)) continue;
				
				trees.remove(ctree);

				int segmentID = 1;
				Collection<Node<Float>> allNodes = ctree.getRoot().getSubtreeNodes();

				for(Node<Float> node : allNodes)
				{
					if(!node.equals(ctree.getRoot()))
					{
						Segment currentSegment = new Segment(this, getPatch(ctree), ctree, cObj.getId(), segmentID, (RadiusNode) node, (RadiusNode) node.getParent(), unit, (int) node.getConfidence());
						segmentID++;

						if(currentSegment.checkNodesInImage()) allSegments.add(currentSegment);
					}
				}
				
				processedTreelines.add(ctree);
			}
		}

		Utils.log(trees.size());

		for(Displayable t: trees)
		{
			Treeline tl = (Treeline) t;
			if(processedTreelines.contains(tl)) continue;
			
			int segmentID = 1;
			Collection<Node<Float>> allNodes = tl.getRoot().getSubtreeNodes();

			for(Node<Float> node : allNodes)
			{
				if(!node.equals(tl.getRoot()))
				{
					Segment currentSegment = new Segment(this, getPatch(tl), tl, tl.getId(), segmentID, (RadiusNode) node, (RadiusNode) node.getParent(), unit, (int) node.getConfidence());
					segmentID++;

					if(currentSegment.checkNodesInImage()) allSegments.add(currentSegment);
				}
			}
			
			processedTreelines.add(tl);
		}

		// write
		try
		{
			File saveFile = Utils.chooseFile(System.getProperty("user.home"), null, ".csv");
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));

			bw.write("experiment"+sep+"tube"+sep+"timepoint"+sep+"rootID"+sep+"segmentID"+sep+"layer"+sep+"length"+sep+"avgRadius"+sep+"surfaceArea"+sep+"volume"+sep+"children"+sep
					+"status"+sep+"statusName"+"\n");
			for (Segment segment : allSegments)
			{
				bw.write(segment.getStatistics(sep));
				bw.newLine();
			}
			bw.close();
		} 
		catch (IOException e)
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
				final Layer layer = new Layer(project, realLast+1, 1, parent);
				parent.add(layer);
				layer.recreateBuckets();
				layer.updateLayerTree();
				firstEmptyAtBack=realLast+1;
				numberToAdd--;
			}
			for(int i=0;i<numberToAdd;i++){
				final Layer layer = new Layer(project, firstEmptyAtBack+1+i, 1, parent);
				parent.add(layer);
				layer.recreateBuckets();
				layer.updateLayerTree();
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
	 * Adds images from the load images dialogue and creates new layers
	 * @param files - ArrayList of image files
	 * @author Axel
	 */
	public static void addLayerAndImage(ArrayList<File> files)
	{
		int size = files.size();
		File[] fileArray = new File[size];
		for(int i=0;i<size;i++){
			fileArray[i]=files.get(i);
		}
		addLayerAndImage(fileArray);
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
	 * gives relative patchdir
	 * @author actyc
	 * @return String representing the relative patches directory
	 */
	public String convertToRelativPath(String currentPathString){
		if(currentPathString.contains(File.separator) && !currentPathString.contains(".")){
			Path currentPath = Paths.get(currentPathString);
			Path base = Paths.get(imageDir.getAbsolutePath());
			Path relativPath = base.relativize(currentPath);
			//Utils.log("convert path from: "+currentPathString+" to: "+relativPath.toString());
			return relativPath.toString();
		}
		return currentPathString;

	}
	
	//check if path is equal on level up if yes return path else return empty String
	public static String imageParentPath(){
		String result="";
		boolean equal=true;
		//get all patches
		LayerSet layerSet = Display.getFront().getLayerSet();
		List<Patch> patches = layerSet.getAll(Patch.class);
		//equal path
		Path currentEqual=null;
		
		for (Patch patch : patches) {
			Path currentPath = Paths.get(patch.getImageFilePath());
			currentPath = currentPath.getParent();
			
			if(currentEqual==null){
				currentEqual=currentPath;
			}
			else {
				if(!currentEqual.equals(currentPath)){
					equal=false;
				}
			}
		}
		if(equal && currentEqual!=null){
			result=currentEqual.toString();
		}
		return result;
	}
	
	/**
	 * For testing purposes
	 */
	public static void test() 
	{
		// aeekz:
	
		// get layers
		Display display = Display.getFront();	
		LayerSet layerSet = display.getLayerSet();  
		ArrayList<Layer> layers = layerSet.getLayers();
		
		// get image names
		List<Patch> patches = layerSet.getAll(Patch.class);
		
		for(Patch p: patches)
		{
			ImagePlus image = p.getImagePlus();
			Utils.log(image.getTitle());
			Utils.log("1 pixel = " + image.getCalibration().pixelWidth + image.getCalibration().getUnits());
		}
		
		// actyc:
		
		//SplitDialog splitDialog = new SplitDialog();
		// RhizoAddons.test = !RhizoAddons.test;
		//Utils.log("Aktueller Zustand: " + RhizoAddons.test);
		//Display display = Display.getFront();
		// Layer frontLayer = Display.getFrontLayer();
		//Layer currentLayer = display.getLayer();
		//LayerSet currentLayerSet = currentLayer.getParent();
		//Project project = display.getProject();
		//project.getProjectTree();
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
	
	
	protected Thread choose(final int screen_x_p, final int screen_y_p, final int x_p, final int y_p, final Class<?> c, final Display currentDisplay)
	{
		return choose(screen_x_p, screen_y_p, x_p, y_p, false, c, currentDisplay);
	}

	protected Thread choose(final int screen_x_p, final int screen_y_p, final int x_p, final int y_p, final Display currentDisplay)
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
	protected Thread choose(final int screen_x_p, final int screen_y_p, final int x_p, final int y_p, final boolean shift_down, final Class<?> c, Display currentDisplay)
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
                
                RhizoAddons rhizoAddons = layer.getProject().getRhizoAddons();
                ConflictManager conflictManager = rhizoAddons.getConflictManager();                
                
		final ArrayList<Displayable> al = new ArrayList<Displayable>(layer.find(x_p, y_p, true));
		al.addAll(layer.getParent().findZDisplayables(layer, x_p, y_p, true)); // only visible ones

		// actyc: remove those trees that contain a non clickable node at xp und yp
		ArrayList<Displayable> alternatedList = new ArrayList<Displayable>();
		for (Displayable displayable : al)
		{
			if (displayable.getClass() == Treeline.class || displayable.getClass() == Connector.class)
			{
				Treeline currentTreeline = (Treeline) displayable;
				double transX = x_p - currentTreeline.getAffineTransform().getTranslateX();
				double transY = y_p - currentTreeline.getAffineTransform().getTranslateY();
				Node<Float> nearestNode = currentTreeline.findNearestNode((float) transX, (float) transY, layer);
				if(nearestNode == null)
				{
					alternatedList.add(displayable);
					continue;
				}
				if(nearestNode.getConfidence() > 0 && !statusMap.get((int) nearestNode.getConfidence()).isSelectable())
				{
					alternatedList.add(displayable);
				}
			}
			if(displayable.getClass()== Patch.class)
			{
				if(displayable.isLocked2()==true){
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
			
			if(conflictManager.isSolving()){
				if(conflictManager.isPartOfSolution(d))
				{
					currentDisplay.select(d, shift_down);
				}
				else
				{
					if(conflictManager.userAbort())
					{
						conflictManager.abortCurrentSolving();
						currentDisplay.select(d, shift_down);
					}
					else
					{
						return t;
					}
				}
			}
			else
			{
				currentDisplay.select(d, shift_down);
			}
			
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
							RhizoAddons.removeHighlight( d,true);
						}

						@Override
						public void mouseEntered(MouseEvent e)
						{
							RhizoAddons.highlight( d,true);

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
				
				//check if there is a solving situation is running
                                
                                RhizoAddons rhizoAddons = currentDisplay.getProject().getRhizoAddons();
                                ConflictManager conflictManager = rhizoAddons.getConflictManager();
				
				if(conflictManager.isSolving()){
					if(conflictManager.isPartOfSolution(d))
					{
						currentDisplay.select(d, shift_down);
					}
					else
					{
						if(conflictManager.userAbort())
						{
							conflictManager.abortCurrentSolving();
							currentDisplay.select(d, shift_down);
						}
					}
				}
				else
				{
					currentDisplay.select(d, shift_down);
				}
				
				
				pop.setVisible(false);

				// fix selection bug: never receives mouseReleased event when
				// the popup shows
				currentDisplay.getMode().mouseReleased(null, x_p, y_p, x_p, y_p, x_p, y_p);

				// actyc: return to the original color
				RhizoAddons.removeHighlight(new ArrayList<Displayable>(al),true);

				return;
			}
		};
		return t;
	}
	
    /**
     *  Writes the current project to a xmlbeans file. In contrast to the standard TrakEM xml format, this file may be opened with MiToBo
     *  @author Tino
     */
    public void writeMTBXML()
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
			xmlRootProject.setXresolution((float) imagePlus.getCalibration().getX(1));
			xmlRootProject.setYresolution((float) imagePlus.getCalibration().getY(1));
			
			MTBXMLRootImageAnnotationType[] xmlRootSets = new MTBXMLRootImageAnnotationType[layers.size()];

			// all treelines in the project
			List<Displayable> allTreelines = layerSet.get(Treeline.class);
			
			// layer = rootSet
			for(int i = 0; i < layers.size(); i++)
			{
				Layer currentLayer = layers.get(i); 
				MTBXMLRootImageAnnotationType rootSet = MTBXMLRootImageAnnotationType.Factory.newInstance();
				
				rootSet.setImagename(imageNames[i]); // TODO: why does this not work?
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
				
				// treeline = rootReference TODO: first rootreference is redundant in xml output? might be caused by changes to the connector class
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
	private MTBXMLRootType treelineToXMLType(Treeline treeline, Layer currentLayer, int rootId)
	{
		MTBXMLRootType xmlRoot = MTBXMLRootType.Factory.newInstance();

		xmlRoot.setRootID(rootId);
		xmlRoot.setStartSegmentID(0); // TODO?
		
		List<Node<Float>> treelineNodes = new ArrayList<Node<Float>>(treeline.getNodesAt(currentLayer)); // arraylist for convenience
		treelineNodes.remove(treeline.getRoot()); // skip the root node

		// TODO: some nodes in annotated projects (roman) appear to be parentless? how?
		List<Node<Float>> nodes = sortTreelineNodes(treelineNodes, treeline);
		
		MTBXMLRootSegmentType[] rootSegmentsArray = new MTBXMLRootSegmentType[nodes.size()];
		
		for(int i = 0; i < nodes.size(); i++)
		{
			Node<Float> n = nodes.get(i);
			float startRadius = 1; // default if node is not a RadiusNode
			float endRadius = 1; // default if node is not a RadiusNode 
			
			if(!n.equals(treeline.getRoot()))
			{
				if(n.getParent() instanceof RadiusNode) startRadius = ((RadiusNode) n.getParent()).getData();
				if(n instanceof RadiusNode) endRadius = ((RadiusNode) n).getData();
				
				MTBXMLRootSegmentType rootSegment = MTBXMLRootSegmentType.Factory.newInstance();
				rootSegment.setRootID(xmlRoot.getRootID());
				rootSegment.setSegmentID(i); 
				if(n.getParent().equals(treeline.getRoot())) rootSegment.setParentID(-1);
				else rootSegment.setParentID(i-1); // this works because nodes are sorted

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
				
				// TODO: this is temporary
//				if(statusFileExists && n.getConfidence() < statusList.size())
//				{
//					String status = statusList.get(n.getConfidence());
//					if(status.equals("DEAD")) rootSegment.setType(MTBXMLRootSegmentStatusType.DEAD);
//					else if(status.equals("DECAYED")) rootSegment.setType(MTBXMLRootSegmentStatusType.DECAYED);
//					else if(status.equals("GAP")) rootSegment.setType(MTBXMLRootSegmentStatusType.GAP);
//					else rootSegment.setType(MTBXMLRootSegmentStatusType.LIVING);
//				}
//				else rootSegment.setType(MTBXMLRootSegmentStatusType.LIVING); // TODO: custom status vs enums?
				
				rootSegmentsArray[i] = rootSegment;
			}
		}
		
		xmlRoot.setRootSegmentsArray(rootSegmentsArray);
		return xmlRoot;
	}
	
	/**
	 * Sorts the nodes of a treeline according their start and end coordinates
	 * @param List of nodes in a treeline
	 * @return Sorted list of nodes
	 * @author Tino
	 */
	private static List<Node<Float>> sortTreelineNodes(List<Node<Float>> treelineNodes, Treeline treeline)
	{
		List<Node<Float>> result = new ArrayList<Node<Float>>();
		// first node
		Node<Float> current = null;
		for(Node<Float> n: treelineNodes)
		{
			if(n.getParent().equals(treeline.getRoot()))
			{
				result.add(n);
				current = n;
			}
		}

		while(result.size() < treelineNodes.size())
		{
			boolean found = false;
			for(Node<Float> n: treelineNodes)
			{
				if(n.getParent().equals(current))
				{
					result.add(n);
					current = n;
					found = true;
					break;
				}
			}
			
			if(!found)
			{
				Utils.log("treeline "+treeline+" has parentless nodes.");
				break;
			}
		}

		return result;
	}

	/**
	 * Converts MTBXML to TrakEM project.
	 * INCOMPLETE - TODO: add connectors, image names, find better workaround
	 * @author Tino
	 */
	public void readMTBXML()
	{
		String[] filepath = Utils.selectFile("test");
		File file = new File(filepath[0] + filepath[1]);
		
		// TODO: check other violations
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
			
			Utils.log("@readMTBXML: number of rootsets " + rootSets.length);
			
			Display display = Display.getFront();	
			
			Project project = display.getProject();
			ProjectTree projectTree = project.getProjectTree();
			
			LayerSet layerSet = display.getLayerSet();  
			ArrayList<Layer> layers = layerSet.getLayers();
			
			while(rootSets.length > layers.size())
			{
				Utils.showMessage("Number of rootSets in the xml file is greater than the number of layers.");
				filepath = Utils.selectFile("test");
				file = new File(filepath[0] + filepath[1]);
				
				rootProjectDocument = MTBXMLRootProjectDocument.Factory.parse(file);
				rootProject = rootProjectDocument.getMTBXMLRootProject();
				rootSets = rootProject.getCollectionOfImageAnnotationsArray();
			}
			
			/* catch exceptions:
			 * - check resolution 
			 */
			for(int i = 0; i < rootSets.length; i++)
			{
				Layer currentLayer = layers.get(i); // order of rootsets has to correspond to the layer if we don't care about image names
				MTBXMLRootImageAnnotationType currentRootSet = rootSets[i];
				
				ProjectThing possibleParent = findParentAllowing("treeline", project);
				if(possibleParent == null)
				{
				    Utils.showMessage("Project does not contain object that can hold treelines.");
				    return;
				}
				
				MTBXMLRootType[] roots = currentRootSet.getRootsArray();
				Utils.log("@readMTBXML: number of roots in rootset "+ i + ": " + roots.length);
		
				for(int j = 0; j < roots.length; j++)
				{
					MTBXMLRootType currentRoot = roots[j];
					
					ProjectThing treelineThing = possibleParent.createChild("treeline");
					
					DefaultMutableTreeNode parentNode = DNDTree.findNode(possibleParent, projectTree);
	    			DefaultMutableTreeNode node = new DefaultMutableTreeNode(treelineThing);
	    			((DefaultTreeModel) projectTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
	    			
	    			Treeline treeline = (Treeline) treelineThing.getObject();
	    			treeline.setLayer(currentLayer);
	    			
	    			// TODO: this is a workaround for the repainting issues that occur when creating a new nodes out of a mtbxml file
	    			currentLayer.mtbxml = true;
	    			
	    			MTBXMLRootSegmentType[] rootSegments = currentRoot.getRootSegmentsArray();
//	    			Utils.log("@readMTBXML: number of segments in root "+ j + " in rootset "+ i + ": " + rootSegments.length);
	    			
	    			// create node -> ID map to later assign parents and children according to segment IDs and parent IDs
	    			HashMap<Integer, RadiusNode> nodeIDmap = new HashMap<Integer, RadiusNode>();
	    			for(int k = 0; k < rootSegments.length; k++)
	    			{
	    				MTBXMLRootSegmentType currentRootSegment = rootSegments[k];
	    				MTBXMLRootSegmentPointType xmlStart = currentRootSegment.getStartPoint();
	    				MTBXMLRootSegmentPointType xmlEnd = currentRootSegment.getEndPoint();

	    				if(currentRootSegment.getParentID() == -1)
	    				{
	    					RadiusNode root = new RadiusNode(xmlStart.getX(), xmlStart.getY(), currentLayer, currentRootSegment.getStartRadius());
	    					RadiusNode currentNode = new RadiusNode(xmlEnd.getX(), xmlEnd.getY(), currentLayer, currentRootSegment.getEndRadius());
	    					nodeIDmap.put(-1, root);
	    					nodeIDmap.put(currentRootSegment.getSegmentID(), currentNode);
	    				}
	    				else
	    				{
	    					RadiusNode currentNode = new RadiusNode(xmlEnd.getX(), xmlEnd.getY(), currentLayer, currentRootSegment.getEndRadius());
	    					nodeIDmap.put(currentRootSegment.getSegmentID(), currentNode);
	    				}
	    			}
	    			
	    			for(int k = 0; k < rootSegments.length; k++)
	    			{
	    				MTBXMLRootSegmentType currentRootSegment = rootSegments[k];
	    				
    					// TODO: this is temporary
	    				byte s = 0;
	    				
//	    				if(statusFileExists)
//	    				{
//		    				if(currentRootSegment.getType() == MTBXMLRootSegmentStatusType.LIVING) s = (byte) statusList.indexOf("LIVING");
//		    				else if(currentRootSegment.getType() == MTBXMLRootSegmentStatusType.DEAD) s = (byte) statusList.indexOf("DEAD");
//		    				else if(currentRootSegment.getType() == MTBXMLRootSegmentStatusType.GAP) s = (byte) statusList.indexOf("GAP");
//		    				else if(currentRootSegment.getType() == MTBXMLRootSegmentStatusType.DECAYED) s = (byte) statusList.indexOf("DECAYED");
//	    				}

    					if(s == -1) s = 0;
	    				

	    				if(currentRootSegment.getParentID() == -1)
	    				{
	    					treeline.addNode(null, nodeIDmap.get(currentRootSegment.getParentID()), s);
	    					treeline.addNode(nodeIDmap.get(currentRootSegment.getParentID()), nodeIDmap.get(currentRootSegment.getSegmentID()), s);
	    					treeline.setRoot(nodeIDmap.get(currentRootSegment.getParentID()));
	    			
	    				}
	    				else
	    				{
	    					treeline.addNode(nodeIDmap.get(currentRootSegment.getParentID()), nodeIDmap.get(currentRootSegment.getSegmentID()), s);
	    				}
	    			}
	    			
	    			treeline.updateCache();
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
	 * TODO deprecated due to changes to the connector class
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
     * @param l - Layer
     * @param treelines - List of treelines 
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
    
    /**
     * Filters a list of treelines by the given layer
     * @param l - Layer
     * @param treelines - List of treelines
     * @return Treelines in the given layer
     * @author Tino
     */
    private static List<Displayable> filterTreelinesByLayer(Layer l, List<Displayable> treelines)
    {
    	 List<Displayable> res = new ArrayList<Displayable>();
    	
    	for(int j = 0; j < treelines.size(); j++)
		{
			Treeline currentTreeline = (Treeline) treelines.get(j);
			
			if(null == currentTreeline.getFirstLayer()) continue;
			if(currentTreeline.getFirstLayer().equals(l)) res.add(currentTreeline);
		}
    	
    	return res;
    }
    
    /**
     * 
     */
    public void clearColorVisibilityLists()
    {
    	statusMap.clear();
    }
    
    /**
     * Used for disposing JFrames when closing the control window
     * @return The color and visbility JFrame
     */
    public JFrame getColorVisibilityFrame()
    {
    	return colorFrame;
    }
    
     /**
     * call to dispose the ColorVisibilityFrame
     */
    public void disposeColorVisibilityFrame()
    {
        if(colorFrame==null){
            return;
        }
        clearColorVisibilityLists();
    	colorFrame.dispose();
    }
    
     /**
     * Used for disposing JFrames when closing the control window
     * @return The image loader JFrame
     */
    public void disposeGui()
    {
    	disposeColorVisibilityFrame();
        disposeImageLoaderFrame();
        conflictManager.disposeConflictFrame();
    }
    
    /**
     * Used for disposing JFrames when closing the control window
     * @return The image loader JFrame
     */
    public JFrame getImageLoaderFrame()
    {
    	return imageLoaderFrame;
    }
    
     /**
     * call to dispose the ColorVisibilityFrame
     */
    public void disposeImageLoaderFrame()
    {
        if(imageLoaderFrame==null){
            return;
        }
    	imageLoaderFrame.dispose();
    }

	/**
	 * @return the relativPatchDir
	 */
	public String getRelativPatchDir() {
		return relativPatchDir;
	}

	/**
	 * @param relativPatchDir the relativPatchDir to set
	 */
	public void setRelativPatchDir(String relativPatchDir) {
		this.relativPatchDir = relativPatchDir;
	}

	public byte getStatusMapSize() 
	{
		return (byte) (statusMap.size() - FIXEDSTATUSSIZE - 1);
	}
}




/**
 * Segment class for writing statistics.
 * @author Axel, Tino
 *
 */
class Segment
{
	private RadiusNode child;
	private RadiusNode parent;
	
	private Layer layer;
	private Treeline t;
	
	// infos
	private String imageName, experiment, tube, timepoint;
	private double length, avgRadius, surfaceArea, volume;
	private int segmentID, numberOfChildren;
	
	private int status;

	private long treeID;
	
	private double scale;
	private final double inchToMM = 25.4;
	
	private final double minRadius = 1;
	private double r1 = minRadius;
	private double r2 = minRadius;
	
	private Patch p;
	
	private RhizoAddons r;

	// TODO: add warning that if no images are present the unit will be pixel
	public Segment(RhizoAddons r, Patch p, Treeline t, long treeID, int segmentID, RadiusNode child, RadiusNode parent, String unit, int status)
	{
		this.p = p;
		this.t = t;
		
		if(null == p) this.scale = 1;
		else if(unit.equals("inch")) this.scale = p.getImagePlus().getCalibration().pixelWidth;
		else if(unit.equals("mm")) this.scale = p.getImagePlus().getCalibration().pixelWidth * inchToMM;
		else this.scale = 1;
		
		this.child = child;
		this.parent = parent;
		this.layer = child.getLayer();
		
		if(parent.getData() > 0) this.r1 = parent.getData() * scale;
		if(child.getData() > 0) this.r2 = child.getData() * scale;
		
		if(null != p) this.imageName = p.getImagePlus().getTitle();
		else this.imageName = "";
		
		parseImageName(imageName);
		
		this.treeID = treeID;
		this.segmentID = segmentID;
		this.status = status;
		this.r = r;
		
		calculate();
	}
	
	private void calculate()
	{
		this.length = Math.sqrt(Math.pow(child.getX() - parent.getX(), 2) + Math.pow(child.getY() - parent.getY(), 2)) * scale;
		this.avgRadius = (r1 + r2) / 2;
		double s = Math.sqrt(Math.pow((r1 - r2), 2) + Math.pow(this.length, 2));
		this.surfaceArea = (Math.PI * Math.pow(r1, 2) + Math.PI * Math.pow(r2, 2) + Math.PI * s * (r1 + r2));
		this.volume = ((Math.PI * length * (Math.pow(r1, 2) + Math.pow(r1, 2) + r1 * r2)) / 3);
		this.numberOfChildren = child.getChildrenCount();
	}
	
	private void parseImageName(String name)
	{
		if(name.equals(""))
		{
			experiment = "NA";
			tube = "NA";
			timepoint ="NA";
			return;
		}
		
		String[] split = name.split("_");
		if(split.length < 6)
		{
			experiment = "NA";
			tube = "NA";
			timepoint ="NA";
			return;
		}
		
		experiment = split[0];
		tube = split[1];
		timepoint = split[5];
	}

	public String getStatistics(String sep)
	{
		String result = experiment + sep + tube + sep + timepoint + sep + Long.toString(treeID) + sep + Integer.toString(segmentID) + sep + Integer.toString((int) layer.getZ() + 1)  +
				sep + Double.toString(length) + sep + Double.toString(avgRadius) + sep + Double.toString(surfaceArea) +
				sep + Double.toString(volume) + sep + Integer.toString(numberOfChildren) + sep + status + sep + r.statusMap.get(status).getFullName();

		return result;
	}
	
	public boolean checkNodesInImage()
	{
		// no image
		if(null == p) return true;

		ImagePlus image = p.getImagePlus();
		
		AffineTransform at = t.getAffineTransform();
		Point2D p1 = at.transform(new Point2D.Float(parent.getX(), parent.getY()), null);
		Point2D p2 = at.transform(new Point2D.Float(child.getX(), child.getY()), null);
		
		Utils.log(p1.getX() + " " + p1.getY() + " " + p2.getX() + " " + p2.getY() + "\t" + image.getWidth() + " " + image.getHeight());
		
		// both nodes are inside
		if(p1.getX() < image.getWidth() && p1.getY() < image.getHeight() && p2.getX() < image.getWidth() && p2.getY() < image.getHeight()) return true;
		// both nodes are outside
		else if((p1.getX() > image.getWidth() || p1.getY() > image.getHeight()) && (p2.getX() > image.getWidth() || p2.getY() > image.getHeight())) return false; 
		// parent node is outside, child is inside
		else if((p1.getX() > image.getWidth() || p1.getY() > image.getHeight()) && (p2.getX() < image.getWidth() || p2.getY() < image.getHeight()))
		{
			double m = (p1.getY() - p2.getY())/(p1.getX() - p2.getX());
			double b = p1.getY() + m*p1.getX();
			
			if(p1.getX() > image.getWidth())
			{
				double newY = m*image.getWidth() + b;
				parent = new RadiusNode(image.getWidth(), (float) newY, layer, 1.0f);
				calculate();
				return true;
			}			
			if(p1.getY() > image.getHeight())
			{
				double newX = (image.getHeight() - b) / m;
				parent = new RadiusNode((float) newX, image.getHeight(), layer, 1.0f);
				calculate();
				return true;
			}
		}
		// child node is outside, parent is inside
		else if((p1.getX() < image.getWidth() || p1.getY() < image.getHeight()) && (p2.getX() > image.getWidth() || p2.getY() > image.getHeight()))
		{
			double m = (p1.getY() - p2.getY())/(p1.getX() - p2.getX());
			double b = p1.getY() + m*p1.getX();
			
			if(p2.getX() > image.getWidth())
			{
				double newY = m*image.getWidth() + b;
				parent = new RadiusNode(image.getWidth(), (float) newY, layer, 1.0f);
				calculate();
				return true;
			}			
			if(p2.getY() > image.getHeight())
			{
				double newX = (image.getHeight() - b) / m;
				parent = new RadiusNode((float) newX, image.getHeight(), layer, 1.0f);
				calculate();
				return true;
			}
		}

	
		return false;
	}
}
