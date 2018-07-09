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
import java.util.HashMap;
import java.util.HashSet;
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
import de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProject;
import de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProject.*;
import de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProject.ConnectorLinksList.ConnectorLink;
import de.unihalle.informatik.rhizoTrak.xsd.config.RhizoTrakProjectConfig;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class RhizoIO {
	public static final String RHIZOTRAK_PROJECTFILE_EXTENSION = "rtk";
	
	private RhizoMain rhizoMain;
	
	public static final File userSettingsFile = new File(System.getProperty("user.home") + File.separator + ".rhizoTrakSettings" + File.separator + "settings.xml");
	
	private boolean debug = false;
	
	public RhizoIO(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
	}


	/** Load the rhizotrak specific data. requires the trakEM project already setup.
	 * <p>
	 * If the file wiht extenions <code>RHIZOTRAK_PROJECTFILE_EXTENSION</code> it is used.
	 * Otherwise the previous version with separate files for the configuration and the connector data is tried.
	 * <p>
	 * In both cases the conflict manager is restored.
	 * 
	 * @param file File with the trakEM project .xml file
	 * @param project the project
	 */
	
	public void addonLoader( File file, Project project) {
			String filenameWoExtension = removeProjectfileExtension( file.getAbsolutePath());

			// load user settings 
			Utils.log2("loading user settings...");
			loadUserSettings();
			// reset changed in project config
			project.getRhizoMain().getProjectConfig().resetChanged();
			Utils.log2("done");

			File f = new File(filenameWoExtension + "." + RHIZOTRAK_PROJECTFILE_EXTENSION);
			if(f.exists() && !f.isDirectory()) { 
				Utils.log2("loading rhizotrak project data...");
				loadProject(filenameWoExtension + "." + RHIZOTRAK_PROJECTFILE_EXTENSION , false);
				Utils.log2("done");

				Utils.log2("restoring conflicts...");
				rhizoMain.getRhizoAddons().getConflictManager().restorConflicts(project);
				Utils.log2("done");

			} else {
				// old version with .cfg and .con files

				Utils.log2("loading connector data...");
				loadConnectorHeadless( filenameWoExtension + ".con");
				Utils.log2("done");


				Utils.log2("restoring conflicts...");
				//TODO: have to be restored for every Project
				rhizoMain.getRhizoAddons().getConflictManager().restorConflicts(project);
				Utils.log2("done");

				Utils.log2("restoring status conventions...");
				loadConfigFile( filenameWoExtension + ".cfg");
				Utils.log2("done");
			}
                         
			//lock all images
			RhizoAddons.lockAllImagesInAllProjects();
	    	
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
				String abbrev;
				if ( status.getAbbreviation() != null) {
					abbrev = status.getAbbreviation();
				} else {
					if ( status.getFullName().length() > 0) {
						abbrev = status.getFullName().substring( 0, 1); 
					} else {
						abbrev = "";
					}
				}
				
				rhizoMain.getProjectConfig().addStatusLabelToSet( status.getFullName(), abbrev,
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
		
			if ( gs.getAskMergeTreelines() != null)
				rhizoMain.getProjectConfig().setAskMergeTreelines( gs.getAskMergeTreelines());
			if ( gs.getAskSplitTreeline() != null)
				rhizoMain.getProjectConfig().setAskSplitTreeline(  gs.getAskSplitTreeline());
			if ( gs.getShowCalibrationInfo() != null)
				rhizoMain.getProjectConfig().setShowCalibrationInfo(  gs.getShowCalibrationInfo());
			
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
	
	/** Initialize the status label mapping from a rhizotrak project file or an old config file.
	 * If no path  is given or can not be parsed  then use default settings
	 * @param path
	 */
	public void initStatusLabelMapping( String path) {
		if(null == path){
			rhizoMain.getProjectConfig().setDefaultUserStatusLabel();
		} else if ( path.endsWith(RHIZOTRAK_PROJECTFILE_EXTENSION)) {
			loadProject( path, true);
		} else {
			loadConfigFile(path);
		}
	}
	
	/**
	 * Loads the project config file. If <code>path</code> is null or the file cannot be parse the default configuration is
	 * used.
	 * <p>
	 * Also the first xml-version is supported.
	 * 
	 * @param path Filename for the config file. 
	 * 
	 * if <code>null</code> the default settings will be set
	 * 
	 * @author Tino, Posch
	 */
	public void loadConfigFile(String path) {
		if(null == path){
			rhizoMain.getProjectConfig().setDefaultUserStatusLabel();
			return;
		}
		
		File configFile = new File( path);
		
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
				
				this.rhizoMain.getProjectConfig().appendStatusLabelMapping(
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
					this.rhizoMain.getProjectConfig().appendStatusLabelMapping( 
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
			rhizoMain.getProjectConfig().printStatusLabelMapping();
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
	@Deprecated
    public boolean loadConnector(String path) {

        // read the save file
    	if (rhizoMain.getRhizoAddons().getProject() == null) {
    		Utils.showMessage( "rhizoTrak", "Warning: can not load connector file, project not found");
    		return false;
    	}

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
     * Loads the connector file without display
     *
     * @param path Filename for the connector filename. if it does not end with <code>.con</code> this extension is appended
     * @return success
     * 
     * @author posch
     */
    public boolean loadConnectorHeadless(String path) {
        // read the save file
    	
    	if (rhizoMain.getRhizoAddons().getProject() == null) {
    		Utils.showMessage( "rhizoTrak", "Warning: can not load connector file, project not found");
    		return false;
    	}
    	
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

		HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks( rhizoMain.getRhizoAddons().getProject());
		if ( rootstackThings == null) {
			Utils.showMessage( "rhizoTrak", "RhizoIO.loadConnectorHeadless warning: no rootstack found");
			try {
				fr.close();
			} catch (IOException e) {
				// ignore
			}
			return false;
		}

		// and collect treelines and connectors below all rootstacks
		HashMap<Long,Treeline> allTreelines = RhizoUtils.getTreelinesBelowRootstacks(rootstackThings);
		HashMap<Long,Connector> allConnectors = RhizoUtils.getConnectorsBelowRootstacks(rootstackThings);

        try {
            StringBuilder msg = new StringBuilder();

            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while (line != null) {
                if (line.equals("###")) {
                    line = br.readLine();
                    break;
                }
                
                // load the line
                String[] content = line.split(";");
                if (content.length > 1) {
                	long currentConID = Long.parseLong(content[0]);

                	Connector conn = allConnectors.get( currentConID);
                	if ( conn == null ) {
                		msg.append( "connector " + currentConID + " in .con file not found in project");
                		continue;
                	}
                   	for (int i = 1; i < content.length; i++) {
                		long currentTreelineID = Long.parseLong(content[i]);
                		Treeline tl = allTreelines.get(currentTreelineID);
                		
                		if ( tl == null ) {
                			msg.append( "treeline " + currentTreelineID + " not found in project for connector " + currentConID);
                		} else  if ( ! conn.addConTreelineHeadless( tl) ) {
                			msg.append( "Error adding treeline " + currentTreelineID + " to connector " + currentConID);
                		}
                	}
                }

                // read the next line
                line = br.readLine();
            }
            br.close();
            
            if ( msg.length() > 1 ) {
            	msg.insert(0, "RhizoIO.loadConnectorHeadless: Errors");
            	Utils.showMessage( "rhizoTrak", new String( msg));
            }
        } catch (IOException e) {
        	Utils.showMessage( "rhizoTrak", "Warning: can not parse connector file " + conFile.getAbsolutePath());

            e.printStackTrace();
            return false;
        }
           
        return true;
    }
	
    /**
	 * Loads the rhizotrak project data file. If <code>filenameWoExtension</code> is null or the file 
	 * with appended extension cannot be parse the default configuration is used.
	 * 
	 * @param path Filename for the rhizotrak project data file
	 * @param onlyMapping if true, read and restore only the mapping of status labels
     * 
     * @author Tino, Posch
	 */

	private void loadProject( String path, boolean onlyMapping) {
		// New project..
		if(null == path){
			rhizoMain.getProjectConfig().setDefaultUserStatusLabel();
			return;
		}

		File projectFile = new File( path);
		if(!projectFile.exists()) {
			Utils.showMessage( "config file " + projectFile.getPath() + " not found: using default settings");
			rhizoMain.getProjectConfig().setDefaultUserStatusLabel();
			return;
		}

		// parse the xml-file
		RhizoTrakProject xmlProject = null;
		try {	
			// try RhizoTrakProject.xsd, i.e. current version
			JAXBContext context = JAXBContext.newInstance(RhizoTrakProject.class);
			Unmarshaller um = context.createUnmarshaller();
			xmlProject = (RhizoTrakProject) um.unmarshal(projectFile);
		} catch (JAXBException e) {    
			Utils.showMessage( "cannot parse config file " + projectFile.getPath() + ": using default settings");

			rhizoMain.getProjectConfig().setDefaultUserStatusLabel();	
			return;
		}
		
		// restore mapping of status labels
		List<StatusList.Status> sl = xmlProject.getStatusList().getStatus();

		for(int i = 0; i < sl.size(); i++) {
			StatusList.Status newStatus = sl.get(i);

			this.rhizoMain.getProjectConfig().appendStatusLabelMapping(
					this.rhizoMain.getProjectConfig().addStatusLabelToSet( newStatus.getFullName(), newStatus.getAbbreviation()));
		}

		if ( onlyMapping )
			return;
		
		// ... and image search directory
		if ( xmlProject.getImageSeachDir() != null ) {
			this.rhizoMain.getProjectConfig().setImageSearchDir( new File( xmlProject.getImageSeachDir()));
		} else if ( rhizoMain.getStorageFolder() != null ) {
			this.rhizoMain.getProjectConfig().setImageSearchDir( new File( rhizoMain.getStorageFolder()));
		} else {
			this.rhizoMain.getProjectConfig().setImageSearchDir(  new File( System.getProperty("user.home")));
		}			

		// restore connector data
		HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks( rhizoMain.getRhizoAddons().getProject());
		if ( rootstackThings == null) {
			Utils.showMessage( "rhizoTrak", "RhizoIO.loadConnectorHeadless warning: no rootstack found");

			return;
		}

		// and collect treelines and connectors below all rootstacks
		HashMap<Long,Treeline> allTreelines = RhizoUtils.getTreelinesBelowRootstacks(rootstackThings);
		HashMap<Long,Connector> allConnectors = RhizoUtils.getConnectorsBelowRootstacks(rootstackThings);

		if ( xmlProject.getConnectorLinksList() != null && xmlProject.getConnectorLinksList().getConnectorLink() != null ) {
			StringBuilder msg = new StringBuilder();
			for ( ConnectorLink cl : xmlProject.getConnectorLinksList().getConnectorLink()) {
				if ( cl.getTreelineIds() == null)
					continue;
				
				long currentConID = cl.getConnectorId();

				Connector conn = allConnectors.get( currentConID);
				if ( conn == null ) {
					msg.append( "connector " + currentConID + " in ." + RHIZOTRAK_PROJECTFILE_EXTENSION + " file not found in project");
					continue;
				}
				for ( Long currentTreelineID : cl.getTreelineIds()) {
					Treeline tl = allTreelines.get(currentTreelineID);

					if ( tl == null ) {
						msg.append( "treeline " + currentTreelineID + " not found in project for connector " + currentConID);
					} else 	if ( ! conn.addConTreelineHeadless( tl) ) {
						msg.append( "Error adding treeline " + currentTreelineID + " to connector " + currentConID);
					}
				}
			}

			if ( msg.length() > 1 ) {
				msg.insert(0, "RhizoIO.loadConnectorHeadless: Errors\n");
				Utils.showMessage( "rhizoTrak", new String( msg));
			}
		}
		
		if ( debug) {
			rhizoMain.getProjectConfig().printStatusLabelMapping();
			rhizoMain.getProjectConfig().printStatusLabelSet();
			rhizoMain.getProjectConfig().printFixStatusLabels();
		}
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
		saveProject(filenameWoExtension + "." + RHIZOTRAK_PROJECTFILE_EXTENSION);
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
	        for( int i = 0 ; i < rhizoMain.getProjectConfig().sizeStatusLabelMapping() ; i++ ) {
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
				gStatus.setAbbreviation( sl.getAbbrev());
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

	        // compile the parts of global settings
	        GlobalSettings gs = new GlobalSettings();
	        gs.setGlobalStatusList(gsl);
	        gs.setHighlightcolorList(hlc);
	        gs.setReceiverNodeColor(receiverColor);
	        gs.setAskMergeTreelines( rhizoMain.getProjectConfig().isAskMergeTreelines());
	        gs.setAskSplitTreeline( rhizoMain.getProjectConfig().isAskSplitTreeline());
	        gs.setShowCalibrationInfo( rhizoMain.getProjectConfig().isShowCalibrationInfo());
			
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
     * @param filename
     * @return sucess
     * 
     * @author Axel, Posch

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
	
    /** Save rhizoTrak specific data of a project:
     * <ul>
     * <li> mapping of status labels
     * <li> image search directory
     * <li> connector data
     * </ul>
     * 
     * @param filename
     * @return
     */
    public boolean saveProject( String filename) {
    	boolean success = true;
    	
    	RhizoTrakProject xmlProject = new RhizoTrakProject();

    	// collect mapping of status labels        
    	StatusList jaxbStatusList =  new StatusList();
    	for( int i = 0 ; i < rhizoMain.getProjectConfig().sizeStatusLabelMapping() ; i++ ) {
    		StatusList.Status newStatus = new StatusList.Status();
    		RhizoStatusLabel statusLabel = rhizoMain.getProjectConfig().getStatusLabel( i);

    		newStatus.setFullName( statusLabel.getName());
    		newStatus.setAbbreviation( statusLabel.getAbbrev());
    		jaxbStatusList.getStatus().add( newStatus);
    	}
    	xmlProject.setStatusList(jaxbStatusList);

    	// image search dir and so forth
    	if ( rhizoMain.getProjectConfig().getImageSearchDir() != null ) {
    		xmlProject.setImageSeachDir(  rhizoMain.getProjectConfig().getImageSearchDir().getAbsolutePath());
    	} else if ( rhizoMain.getStorageFolder() != null) {
    		xmlProject.setImageSeachDir( rhizoMain.getStorageFolder());
    	} else {
    		xmlProject.setImageSeachDir( System.getProperty("user.home"));
    	}

    	// connector data
    	
    	// first find rootstacks
    	HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks( rhizoMain.getRhizoAddons().getProject());
    	if ( rootstackThings == null) {
    		Utils.showMessage( "rhizoTrak", "RhizoIO.loadConnectorHeadless warning: no rootstack found");
    		success = false;
    	} else {
    		ConnectorLinksList xmlConnectors = new ConnectorLinksList();
    		// all connectors below a rootstack
    		HashMap<Long,Connector> allConnectors = RhizoUtils.getConnectorsBelowRootstacks(rootstackThings);

    		for ( Connector con : allConnectors.values()) {
    			ConnectorLinksList.ConnectorLink connectorLink = new ConnectorLinksList.ConnectorLink();
    			connectorLink.setConnectorId( con.getId());
    			for (Treeline treeline : con.getConTreelines() ) {
    				connectorLink.getTreelineIds().add( treeline.getId());
    			}

    			xmlConnectors.getConnectorLink().add( connectorLink);
    		}
    		xmlProject.setConnectorLinksList(xmlConnectors);
    	}
    	
    	File configFile = new File(filename);
    	try {
    		JAXBContext context = JAXBContext.newInstance(RhizoTrakProject.class);
    		Marshaller m = context.createMarshaller();
    		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    		
    		m.marshal(xmlProject, configFile);
    	} catch (Exception e) {
    		Utils.showMessage( "rhizoTrak", "cannot write rhizotrak specific project data to " + configFile.getPath());
    		e.printStackTrace();
    		return false;
    	}

    	return success;
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
