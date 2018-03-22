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
import de.unihalle.informatik.rhizoTrak.xsd.config.GlobalSettings.HighlightcolorList;
import de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class RhizoIO
{
	private RhizoMain rhizoMain;
	
	public static File userSettingsFile = new File(System.getProperty("user.home") + File.separator + ".rhizoTrakSettings" + File.separator + "settings.xml");
	
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
			Utils.log("unable to load user settings: file not found");
			return;
		}

		try {
			JAXBContext context = JAXBContext.newInstance(GlobalSettings.class);
			Unmarshaller um = context.createUnmarshaller();

			GlobalSettings gs = (GlobalSettings) um.unmarshal(userSettingsFile);
			for ( GlobalStatus status : gs.getGlobalStatusList().getGlobalStatus() ) {
				// we have no abbreviation in user settings, however only status labels in project.cfg will be used
				rhizoMain.getProjectConfig().addStatusLabelToSet( status.getFullName(),
						new Color( status.getRed().intValue(), status.getGreen().intValue(), status.getBlue().intValue()),
								status.getAlpha().intValue(), status.isSelectable());
			}
			
			if(null != gs.getHighlightcolorList() || null != gs.getHighlightcolorList().getColor() ) {
				if ( gs.getHighlightcolorList().getColor().size() > 0)
					rhizoMain.getProjectConfig().setHighlightColor1(settingsToColor( gs.getHighlightcolorList().getColor().get( 0) ));
				if ( gs.getHighlightcolorList().getColor().size() > 1)
					rhizoMain.getProjectConfig().setHighlightColor2(settingsToColor( gs.getHighlightcolorList().getColor().get( 1) ));
			}
		} 
		catch (JAXBException e) 
		{
			Utils.showMessage( "cannot load user settings from config file " + userSettingsFile.getPath());
			e.printStackTrace();
		}
	}
	
	
	private Color settingsToColor(
			de.unihalle.informatik.rhizoTrak.xsd.config.GlobalSettings.HighlightcolorList.Color colorSettings) {
		Color color = new Color( colorSettings.getRed().intValue(), colorSettings.getGreen().intValue(), colorSettings.getBlue().intValue());
		return color;
	}

	/**
	 * 
	 * Loads the user settings on project file level.
	 * @param path - The project file
	 * @author Tino
	 */
	public void loadConfigFile(String path)
	{
		// New project..
		if(null == path) // user cancelled the open file dialog
		{
			rhizoMain.getProjectConfig().setDefaultUserStatusLabel();
			return;
		}
		
		// TODO: check file ending if coming from file chooser
		// Open project..
		File configFile = new File(path.replace(".xml", ".cfg")); // looking for cfg file in directory
		
		if(!configFile.exists()) {
			Utils.showMessage( "config file " + configFile.getPath() + " not found: using default settings");
			rhizoMain.getProjectConfig().setDefaultUserStatusLabel();

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
				
				this.rhizoMain.getProjectConfig().appendStatusLabelToList( newStatus.getFullName(), newStatus.getAbbreviation());
			}

		} catch (JAXBException e) {    
			try {
				JAXBContext context = JAXBContext.newInstance(Config.class);
				Unmarshaller um = context.createUnmarshaller();
				Config config = (Config) um.unmarshal(configFile);
				List<Status> sl = config.getStatusList().getStatus();

				for(int i = 0; i < sl.size(); i++) {
					this.rhizoMain.getProjectConfig().appendStatusLabelToList( sl.get(i).getFullName(), sl.get(i).getAbbreviation());
				}

			} catch (JAXBException e1) 		{
				Utils.showMessage( "cannot parse config file " + configFile.getPath() + ": using default settings");
				
				rhizoMain.getProjectConfig().setDefaultUserStatusLabel();
			}
		}
		}
	
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
	 * Main method project configuration and connector data
	 * @param file - The project save file
	 * @author Axel
	 */
	public void addonSaver(File file)
	{
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
		
		// ######
//		try 
//		{
//			JAXBContext context = JAXBContext.newInstance(RhizoTrakProjectConfig.class);
//                        Marshaller m = context.createMarshaller();
//                        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//	        
//            de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList sl = 
//                        		new de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList();
//	        for(int i: statusMap.keySet()) 	        {
//	        	// ignore undefined, virtual and connector
//	        	Status oldStatus = statusMap.get(i);
//	        	de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status newStatus =
//	        			new de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status();
//	        	newStatus.setFullName( oldStatus.getFullName());
//	        	newStatus.setAbbreviation( oldStatus.getAbbreviation());
//	        	if(i >= 0) sl.getStatus().add( newStatus);
//	        }
//
//	        RhizoTrakProjectConfig config = new RhizoTrakProjectConfig();
//	        config.setStatusList(sl);
//	        
//	        m.marshal(config, configFile);
//		}
//		catch (Exception e) 
//		{
//			e.printStackTrace();
//		}
		
		// new data structure
		try 
		{
			JAXBContext context = JAXBContext.newInstance(RhizoTrakProjectConfig.class);
                        Marshaller m = context.createMarshaller();
                        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        
            de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList jaxbStatusList = 
                        		new de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList();
	        for( int i = 0 ; i < rhizoMain.getProjectConfig().sizeStatusLabelList() ; i++ ) {
//            for ( RhizoStatusLabel statusLabel : rhizoMain.getProjectConfig().getAllStatusLabel()) {
	        	de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status
	        	newStatus =
	        			new de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status();
	        	RhizoStatusLabel statusLabel = rhizoMain.getProjectConfig().getStatusLabel( i);
	        				
	        	newStatus.setFullName( statusLabel.getName());
	        	newStatus.setAbbreviation( statusLabel.getAbbrev());
	        	jaxbStatusList.getStatus().add( newStatus);
	        }

	        RhizoTrakProjectConfig config = new RhizoTrakProjectConfig();
	        config.setStatusList(jaxbStatusList);
	        
//	        m.marshal(config, new File(file.getAbsolutePath().replace(".xml", ".cfg")+ ".new"));
	        m.marshal(config, configFile);
		}
		catch (Exception e) 
		{
			Utils.showMessage( "cannot write project configuration to " + configFile.getPath());
			e.printStackTrace();
		}

	}

	/**
	 * Saves the global user settings in the users home folder.
	 * @author Axel, Tino
	 */
	public void saveUserSettings()
	{
		try {
			if(!userSettingsFile.getParentFile().exists()) userSettingsFile.getParentFile().mkdirs();

			JAXBContext context = JAXBContext.newInstance(GlobalSettings.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
   
	        GlobalStatusList gsl = new GlobalStatusList();
	        for ( RhizoStatusLabel sl : rhizoMain.getProjectConfig().getAllStatusLabel() ) {
	        	GlobalStatus gStatus = new GlobalStatus();
				gStatus.setFullName( sl.getName());

				gStatus.setRed( BigInteger.valueOf(sl.getColor().getRed()));
				gStatus.setGreen( BigInteger.valueOf( sl.getColor().getGreen()));
				gStatus.setBlue( BigInteger.valueOf(sl.getColor().getBlue()));
				gStatus.setAlpha(BigInteger.valueOf( sl.getAlpha()));
				gStatus.setSelectable( sl.isSelectable());
				
				gsl.getGlobalStatus().add( gStatus);
			}

	        // highlight colors
	        HighlightcolorList hlc = new HighlightcolorList();
	        hlc.getColor().add( colorToSettings( rhizoMain.getProjectConfig().getHighlightColor1()));
	        hlc.getColor().add( colorToSettings( rhizoMain.getProjectConfig().getHighlightColor2()));
	        
	        GlobalSettings gs = new GlobalSettings();
	        gs.setGlobalStatusList(gsl);
	        gs.setHighlightcolorList(hlc);
			
			m.marshal(gs, userSettingsFile);
		} catch(Exception e) {
			Utils.showMessage( "cannot write user settings to " + userSettingsFile.getPath());
			e.printStackTrace();
		}
	
	}
	
	/** convert a awt Color to the xsd representation in user settings
	 * @param color
	 * @return
	 */
	private de.unihalle.informatik.rhizoTrak.xsd.config.GlobalSettings.HighlightcolorList.Color colorToSettings( Color color) {
		de.unihalle.informatik.rhizoTrak.xsd.config.GlobalSettings.HighlightcolorList.Color colorSettings = 
				new de.unihalle.informatik.rhizoTrak.xsd.config.GlobalSettings.HighlightcolorList.Color();
		colorSettings.setRed( BigInteger.valueOf( color.getRed()));
		colorSettings.setGreen( BigInteger.valueOf( color.getGreen()));
		colorSettings.setBlue( BigInteger.valueOf( color.getBlue()));
		
		return colorSettings;
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
	 * 
	 * @param file - File to be read
	 * @return The contents of the file as string
	 * @author Axel
	 */
	public static String readFileToString(File file)
	{
		String result = "";
		StringBuilder sb = new StringBuilder();
		
		try (FileReader fr = new FileReader(file)) {
			int c = fr.read();
			while (c != -1)
			{
				sb.append((char) c);
				c = fr.read();
			}
		} catch (IOException e) {
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
		try (FileWriter fr = new FileWriter(file)) {
			file.createNewFile();
			fr.write(string);
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
}
