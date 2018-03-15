package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.xsd.config.Config;
import de.unihalle.informatik.rhizoTrak.xsd.config.GlobalSettings;
import de.unihalle.informatik.rhizoTrak.xsd.config.Config.StatusList.Status;
import de.unihalle.informatik.rhizoTrak.xsd.config.GlobalSettings.GlobalStatusList;
import de.unihalle.informatik.rhizoTrak.xsd.config.GlobalSettings.GlobalStatusList.GlobalStatus;
import de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class RhizoIO
{

	public static final int FIXEDSTATUSSIZE = 3;
	
	private RhizoMain rhizoMain;
	
	public static File userSettingsFile = new File(System.getProperty("user.home") + File.separator + ".rhizoTrakSettings" + File.separator + "settings.xml");
	
	public List<GlobalStatus> globalStatusList = new ArrayList<GlobalStatus>();

	// used for drawing, GUI and save/load operations
	private LinkedHashMap<Integer, Status> statusMap = new LinkedHashMap<Integer, Status>();
	
	public RhizoIO(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
	}


	/**
	 * Calls load methods when opening a project
	 * @param file - saved project file
	 * @author Axel
	 */
	public Thread addonLoader(File file, Project project)
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
				project.getRhizoMain().getRhizoImages().setImageDir(file.getParentFile());
				// load connector data
				Utils.log2("loading user settings...");
				loadUserSettings();
				Utils.log2("done");

				Utils.log2("loading connector data...");
				loadConnector(file);
				Utils.log2("done");
				
				Utils.log2("restoring conflicts...");
                
				//TODO: have to be restored for every Project
				rhizoMain.getRhizoAddons().getConflictManager().restorConflicts(project);
				Utils.log2("done");
				
				Utils.log2("restoring status conventions...");
				loadConfigFile(file.getAbsolutePath());
				Utils.log2("done");
                                
				//lock all images
				RhizoAddons.lockAllImagesInAllProjects();
				
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
			Utils.showMessage( "config file " + configFile.getPath() + " not found: using default settings");
			setDefaultStatus();
			return;
		}

		
		try {
			JAXBContext context = JAXBContext.newInstance(RhizoTrakProjectConfig.class);
			Unmarshaller um = context.createUnmarshaller();
			RhizoTrakProjectConfig config = (RhizoTrakProjectConfig) um.unmarshal(configFile);
			List<de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status> sl = config.getStatusList().getStatus();

			for(int i = 0; i < sl.size(); i++)
			{
				Status oldStatus = new Status();
				de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status newStatus = sl.get(i);
				oldStatus.setFullName(  newStatus.getFullName());
				oldStatus.setAbbreviation( newStatus.getAbbreviation());
				oldStatus.setRed(BigInteger.valueOf(255));
				oldStatus.setGreen(BigInteger.valueOf(255));
				oldStatus.setBlue(BigInteger.valueOf(0));
				oldStatus.setAlpha(BigInteger.valueOf(255));
				oldStatus.setSelectable(true);
				
				
				statusMap.put(i, oldStatus);
			}

			setFixedStatus();
			updateStatusMap();
		} catch (JAXBException e) {    
			try {
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
			} catch (JAXBException e1) 		{
				Utils.showMessage( "cannot parse config file " + configFile.getPath() + ": using default settings");
				setDefaultStatus();

			}
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

                if (rhizoMain.getRhizoAddons().getProject() != null) {
                    LayerSet layerSet = rhizoMain.getRhizoAddons().getProject().getRootLayerSet();
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
			JAXBContext context = JAXBContext.newInstance(RhizoTrakProjectConfig.class);
                        Marshaller m = context.createMarshaller();
                        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        
            de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList sl = 
                        		new de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList();
	        for(int i: statusMap.keySet()) 	        {
	        	// ignore undefined, virtual and connector
	        	Status oldStatus = statusMap.get(i);
	        	de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status newStatus =
	        			new de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status();
	        	newStatus.setFullName( oldStatus.getFullName());
	        	newStatus.setAbbreviation( oldStatus.getAbbreviation());
	        	if(i >= 0) sl.getStatus().add( newStatus);
	        }

	        RhizoTrakProjectConfig config = new RhizoTrakProjectConfig();
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
        LayerSet layerSet = rhizoMain.getRhizoAddons().getProject().getRootLayerSet();

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


	public byte getStatusMapSize() 
	{
		return (byte) (statusMap.size() - FIXEDSTATUSSIZE - 1);
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
	
	public LinkedHashMap<Integer, Status> getStatusMap()
	{
		return statusMap;
	}
	
	public void putStatus(int i, Status s)
	{
		statusMap.put(i, s);
	}
	

    public void clearColorVisibilityLists()
    {
    	statusMap.clear();
    }
    
}
