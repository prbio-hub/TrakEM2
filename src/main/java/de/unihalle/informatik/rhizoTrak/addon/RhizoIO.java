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
import de.unihalle.informatik.rhizoTrak.xsd.config.GlobalSettings.ReceiverNodeColor;
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
	private RhizoMain rhizoMain;
	
	public static final File userSettingsFile = new File(System.getProperty("user.home") + File.separator + ".rhizoTrakSettings" + File.separator + "settings.xml");
	
	private boolean debug = false;
	
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
			public void run() 	{	
				
			    // the project filename without extension .xml or .xml.gz
				String filenameWoExtension = removeProjectfileExtension( file.getAbsolutePath());

				// load user settings 
				Utils.log2("loading user settings...");
				loadUserSettings();
				// reset changed in project config
				project.getRhizoMain().getProjectConfig().resetChanged();
				Utils.log2("done");

				Utils.log2("loading connector data...");
				loadConnector( filenameWoExtension);
				Utils.log2("done");
				
				Utils.log2("restoring conflicts...");
                
				//TODO: have to be restored for every Project
				rhizoMain.getRhizoAddons().getConflictManager().restorConflicts(project);
				Utils.log2("done");
				
				Utils.log2("restoring status conventions...");
				loadConfigFile( filenameWoExtension + ".cfg");
				Utils.log2("done");
                                
				//lock all images
				RhizoAddons.lockAllImagesInAllProjects();
					
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
			
			if ( null != gs.getReceiverNodeColor() ) {
				rhizoMain.getProjectConfig().setReceiverNodeColor(
						new Color( gs.getReceiverNodeColor().getRed().intValue(), 
								gs.getReceiverNodeColor().getGreen().intValue(), 
								gs.getReceiverNodeColor().getBlue().intValue()));
			} else {
				// set the default color from Node
				rhizoMain.getProjectConfig().setReceiverNodeColor( Node.getReceiverColor());
			}
		
			if ( gs.isAskMergeTreelines() != null)
				rhizoMain.getProjectConfig().setAskMergeTreelines( gs.isAskMergeTreelines());
			if ( gs.isAskSplitTreeline() != null)
				rhizoMain.getProjectConfig().setAskSplitTreeline(  gs.isAskSplitTreeline());
			
			rhizoMain.getProjectConfig().resetChanged();
			
			if ( debug ) {
				rhizoMain.getProjectConfig().printStatusLabelSet();
			}
		} catch (JAXBException e) {
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
	 * Loads the project config file. If <code>path</code> is null or the file cannot be parse the default configuration is
	 * used.
	 * <p>
	 * Also the first xml-version is supported.
	 * 
	 * @param path Filename for the config file. If it does not end with  <code>.cfg</code> this extension is appended
	 * 
	 * if <code>null</code> the default settings will be set
	 * 
	 * @author Tino, Posch
	 */
	public void loadConfigFile(String path) {
		// New project..
		if(null == path){
			rhizoMain.getProjectConfig().setDefaultUserStatusLabel();
			return;
		}
		
		File configFile;
		if ( ! path.endsWith( ".cfg"))
			configFile = new File( path + ".cfg");
		else 
			configFile = new File( path);
		
		if(!configFile.exists()) {
			Utils.showMessage( "config file " + configFile.getPath() + " not found: using default settings");
			rhizoMain.getProjectConfig().setDefaultUserStatusLabel();

			return;
		}

		try {
			
			// try RhizoTrakProjectConfig.xsd, i.e. current version
			JAXBContext context = JAXBContext.newInstance(RhizoTrakProjectConfig.class);
			Unmarshaller um = context.createUnmarshaller();
			RhizoTrakProjectConfig config = (RhizoTrakProjectConfig) um.unmarshal(configFile);
			List<de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status> sl = config.getStatusList().getStatus();

			for(int i = 0; i < sl.size(); i++) {
				de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status newStatus = sl.get(i);
				
				this.rhizoMain.getProjectConfig().appendStatusLabelToList(
						this.rhizoMain.getProjectConfig().addStatusLabelToSet( newStatus.getFullName(), newStatus.getAbbreviation()));
			}
			
			if ( config.getImageSeachDir() != null ) {
				this.rhizoMain.getProjectConfig().setImageSearchDir( new File( config.getImageSeachDir()));
			} else if ( rhizoMain.getStorageFolder() != null ) {
				this.rhizoMain.getProjectConfig().setImageSearchDir( new File( rhizoMain.getStorageFolder()));
			} else {
				this.rhizoMain.getProjectConfig().setImageSearchDir(  new File( System.getProperty("user.home")));
			}
			

		} catch (JAXBException e) {    
			try {
				// try old version: config.xsd
				JAXBContext context = JAXBContext.newInstance(Config.class);
				Unmarshaller um = context.createUnmarshaller();
				Config config = (Config) um.unmarshal(configFile);
				List<Status> sl = config.getStatusList().getStatus();

				for(int i = 0; i < sl.size(); i++) {
					this.rhizoMain.getProjectConfig().appendStatusLabelToList( 
							this.rhizoMain.getProjectConfig().addStatusLabelToSet( sl.get(i).getFullName(), sl.get(i).getAbbreviation()));
				}

				if ( rhizoMain.getStorageFolder() != null ) {
					this.rhizoMain.getProjectConfig().setImageSearchDir( new File( rhizoMain.getStorageFolder()));
				} else {
					this.rhizoMain.getProjectConfig().setImageSearchDir(  new File( System.getProperty("user.home")));
				}
			} catch (JAXBException e1) 		{
				Utils.showMessage( "cannot parse config file " + configFile.getPath() + ": using default settings");
				
				rhizoMain.getProjectConfig().setDefaultUserStatusLabel();
			}
		}
		
		if ( debug) {
			rhizoMain.getProjectConfig().printStatusLabelList();
			rhizoMain.getProjectConfig().printStatusLabelSet();
			rhizoMain.getProjectConfig().printFixStatusLabels();
		}
	}
	
    /**
     * Loads the connector file
     *
     * @param path Filename for the connector filename. if it does not end wtih <code>.con</code> this extension is appended
     * @return success
     * 
     * @author Axel, posch
     */
    public boolean loadConnector(String path) {
        // read the save file
    	
        File conFile;
        if ( path.endsWith( ".con"))
        	conFile = new File( path);
        else
        	conFile = new File(path + ".con");

        FileReader fr;

        try {
        	fr = new FileReader(conFile);
        } catch (FileNotFoundException e) {
        	e.printStackTrace();

        	Utils.showMessage( "rhizoTrak", "Warning: connector file " + conFile.getAbsolutePath() + " not found");
        	return false;
        }

        try {
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
        } catch (IOException e) {
        	Utils.showMessage( "rhizoTrak", "Warning: can not parse connector file " + conFile.getAbsolutePath());

            e.printStackTrace();
            return false;
        }
        
        return true;
    }
	
	/**
	 * Main method project configuration and connector data
	 * 
	 * @param file - The project save file. It is assume that the filename ends with <code>.xml</code> or
	 * <code>.xml.gz</code>
	 * 
	 * @author Axel
	 */
	public void addonSaver(File file) {
	
	    // the project filename without extension .xml or .xml.gz
		String filenameWoExtension = removeProjectfileExtension( file.getAbsolutePath());
		
		//save connector data
		saveConnectorData(filenameWoExtension + ".con");
		saveConfigFile(filenameWoExtension + ".cfg");
		
		return;		
	}
	
	/**
	 * Crates a new config file (.cfg) or overwrites an existing one in the same directory and with the same name as the project file. 
	 * 
	 * @param filename  the filename for the config file  
	 * @author Tino, Posch
	 */
	public void saveConfigFile(String filename) {	
		File configFile = new File(filename);

		try {
			JAXBContext context = JAXBContext.newInstance(RhizoTrakProjectConfig.class);
                        Marshaller m = context.createMarshaller();
                        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        
            de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList jaxbStatusList = 
                        		new de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList();
	        for( int i = 0 ; i < rhizoMain.getProjectConfig().sizeStatusLabelList() ; i++ ) {
	        	de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status
	        	newStatus = new de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig.StatusList.Status();
	        	RhizoStatusLabel statusLabel = rhizoMain.getProjectConfig().getStatusLabel( i);
	        				
	        	newStatus.setFullName( statusLabel.getName());
	        	newStatus.setAbbreviation( statusLabel.getAbbrev());
	        	jaxbStatusList.getStatus().add( newStatus);
	        }

	        RhizoTrakProjectConfig config = new RhizoTrakProjectConfig();
	        config.setStatusList(jaxbStatusList);
	        if ( rhizoMain.getProjectConfig().getImageSearchDir() != null ) {
	        	config.setImageSeachDir(  rhizoMain.getProjectConfig().getImageSearchDir().getAbsolutePath());
	        } else if ( rhizoMain.getStorageFolder() != null) {
	        	config.setImageSeachDir( rhizoMain.getStorageFolder());
	        } else {
	        	config.setImageSeachDir( System.getProperty("user.home"));
	        }
	        
	        m.marshal(config, configFile);
		}
		catch (Exception e) {
			Utils.showMessage( "rhizoTrak", "cannot write project configuration to " + configFile.getPath());
			e.printStackTrace();
		}

	}

	/**
	 * Saves the global user settings in the users home folder.
	 * @author Axel, Tino
	
	 * @return return true if saving was sucessful
	 */
	public boolean saveUserSettings() 	{
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
	        
	        // node receiver color
	        ReceiverNodeColor receiverColor = new ReceiverNodeColor();
	        receiverColor.setRed(BigInteger.valueOf( rhizoMain.getProjectConfig().getReceiverNodeColor().getRed()));
	        receiverColor.setGreen(BigInteger.valueOf( rhizoMain.getProjectConfig().getReceiverNodeColor().getGreen()));
	        receiverColor.setBlue(BigInteger.valueOf( rhizoMain.getProjectConfig().getReceiverNodeColor().getBlue()));

	        
	        GlobalSettings gs = new GlobalSettings();
	        gs.setGlobalStatusList(gsl);
	        gs.setHighlightcolorList(hlc);
	        gs.setReceiverNodeColor(receiverColor);
	        gs.setAskMergeTreelines( rhizoMain.getProjectConfig().isAskMergeTreelines());
	        gs.setAskSplitTreeline( rhizoMain.getProjectConfig().isAskSplitTreeline());
			
			m.marshal(gs, userSettingsFile);
		} catch(Exception e) {
			Utils.showMessage( "cannot write user settings to " + userSettingsFile.getPath());
			e.printStackTrace();
			return false;
		}
		
		return true;
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
     * @param filename - The project save file without extension 
     * @author Axel
     */
    /**
     * @param filename
     * @return sucess
     */
    public boolean saveConnectorData(String filename) {
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

        File conFile = new File( filename);																														// file
        File tempconFile = new File( filename + ".con.bak");
        
        boolean savedOldVersion = false;
        if (conFile.exists()) {
        	conFile.renameTo( tempconFile);
        	savedOldVersion = true;
//            String old = readFileToString(conFile); // read current file
//            if ( old != null ) {
//            	savedOldVersion = writeStringToFile(tempconFile, old); // and save to temp
//            }
        }

		if (!writeStringToFile(conFile, saveText) ) {
			if ( savedOldVersion) {
				tempconFile.renameTo(conFile);
				Utils.showMessage( "rhizoTrak", "Warning: cannot save connector data to " + conFile.getAbsolutePath() +
						" reusing previous version");
			} else {
				Utils.showMessage( "rhizoTrak", "Warning: cannot save connector data to " + conFile.getAbsolutePath());
			}
			return false;
		} else {
			return true;
		}
	}
	
//	/**
//	 * 
//	 * @param file - File to be read
//	 * @return The contents of the file as string or null if an error occurred
//	 * @author Axel
//	 */
//	public static String readFileToString(File file)
//	{
//		String result = "";
//		StringBuilder sb = new StringBuilder();
//		
//		try (FileReader fr = new FileReader(file)) {
//			int c = fr.read();
//			while (c != -1)
//			{
//				sb.append((char) c);
//				c = fr.read();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//		
//		result = sb.toString();
//		return result;
//	}
	
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
			return false;
		}
		
		return true;
	}
	
	private String removeProjectfileExtension( String filename) {
		String filenameWoExtension;
		
		if ( filename.endsWith( ".xml")) {
			filenameWoExtension = filename.substring(0, filename.length()-4);
		} else if ( filename.endsWith( ".xml.gz") ) {
			filenameWoExtension = filename.substring(0, filename.length()-7);
		} else {
			filenameWoExtension = filename;
			Utils.showMessage( "rhizoTrak", "Warning: can not construct correct filenames for .con and .cfg files. Using " +
					filename + "{.con|.cfg} instead");
		}
		return filenameWoExtension;
	}
}
