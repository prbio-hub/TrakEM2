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

import java.io.File;
import java.util.HashMap;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.persistence.FSLoader;
import de.unihalle.informatik.rhizoTrak.persistence.Loader;
import de.unihalle.informatik.rhizoTrak.xsd.rsml.Rsml;

public class RhizoMain
{
	private RhizoAddons rA;
	private RhizoIO rIO;
	private RhizoColVis rCV;
	private RhizoImages rI;
	private RhizoStatistics rS;
	private RhizoMTBXML rMTBXML;
	private RhizoRSML rRSML;
	private RhizoLineMapToTreeline rLineMapToTreeline;
	
	private HashMap<Layer,RhizoRSMLLayerInfo> layerInfoMap = new HashMap<Layer,RhizoRSMLLayerInfo>();
	
	private Project p;

	/**
	 * if true the layer window displays the z coordinate in the title (as trakem does)
	 */
	private boolean titleWithZcoord = false;
	
	/**
	 * The (mainly) project specific configuration
	 */
	private RhizoProjectConfig projectConfig = new RhizoProjectConfig();
	
	public RhizoMain(Project p)
	{
		this.p = p;

		rA = new RhizoAddons(this, p);
		
		rCV = new RhizoColVis(this);
		rIO = new RhizoIO(this);
		rI = new RhizoImages(this);
		rS = new RhizoStatistics(this);
		rMTBXML = new RhizoMTBXML(this);
		rRSML = new RhizoRSML( this);
		rLineMapToTreeline = new RhizoLineMapToTreeline(this);
	}
	
	public RhizoAddons getRhizoAddons()
	{
		return rA;
	}
	
	public RhizoIO getRhizoIO()
	{
		return rIO;
	}
	
	public RhizoColVis getRhizoColVis()
	{
		return rCV;
	}
	
	public RhizoImages getRhizoImages()
	{
		return rI;
	}
	
	public RhizoStatistics getRhizoStatistics()
	{
		return rS;
	}
	
	public RhizoMTBXML getRhizoMTBXML()
	{
		return rMTBXML;
	}
	
	/**
	 * @return the rRSML
	 */
	public RhizoRSML getRhizoRSML() {
		return rRSML;
	}

	public RhizoLineMapToTreeline getRhizoLineMapToTreeline()
	{
		return rLineMapToTreeline;
	}
	
	public Project getProject()
	{
		return p;
	}
	
	/** 
	 * @param layer
	 * @return the LayerInfo associated with this layer or null if unset
	 */
	public RhizoRSMLLayerInfo getLayerInfo( Layer layer) {
		return layerInfoMap.get( layer);
	}
	
	public void setLayerInfo( Layer layer, RhizoRSMLLayerInfo layerInfo) {
		layerInfoMap.put( layer, layerInfo);
	}
    
    /**
	 * @return the projectConfig
	 */
	public RhizoProjectConfig getProjectConfig() {
		return projectConfig;
	}

	/**
    * Used for disposing JFrames when closing the control window
    * @return The image loader JFrame
    */
   public void disposeGUIs()
   {
   		rCV.disposeColorVisibilityFrame();
   		rI.disposeImageLoaderFrame();
   		rA.getConflictManager().disposeConflictFrame();
   }

	/** Code a string to conform to html convention
	 * @param rel_path
	 * @return
	 */
   public static String htmlCode( String s) {
	   StringBuilder out = new StringBuilder(Math.max(16, s.length()));
	   for (int i = 0; i < s.length(); i++) {
		   char c = s.charAt(i);
		   if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
			   out.append("&#");
			   out.append((int) c);
			   out.append(';');
		   } else {
			   out.append(c);
		   }
	   }
	   return out.toString();
   }
   
   /** Get the <code>xmlPath</code> of the project, that is where the project .xml file
    * is to be saved
    * 
    * @return xmlpath or null
    */
   public String getXmlPath() {
	   try   {
		   Loader loader = this.getProject().getLoader();

		   if ( loader instanceof FSLoader) {
			   File f = new File(((FSLoader)loader).getProjectXMLPath());
			   return f.getAbsolutePath();
		   } else {
			   return null;
		   }
	   } catch (Exception ex) {
		   return null;
	   }
   }
   
   /** Get the filename part of <code>xmlPath</code> of the project, that is where the project .xml file
    * is to be saved
    * 
    * @return filename part of xmlpath or null
    */
   public String getXmlName() {
	   try   {
		   Loader loader = this.getProject().getLoader();

		   if ( loader instanceof FSLoader) {
			   File f = new File(((FSLoader)loader).getProjectXMLPath());
			   return f.getName();
		   } else {
			   return null;
		   }
	   } catch (Exception ex) {
		   return null;
	   }
   }
   
   /** get the storage folder of this projects, i.e. the directory where this project is located in the file system
    * @return storage folder or null
    */
   public String getStorageFolder() {
	   try {
		   return this.getProject().getLoader().getStorageFolder();
	   } catch (Exception ex) {
		   return null;
	   }
   }

	/** true if title of layer window should contain the z coordinate
	 * @return
	 */
	public boolean getTitleWithZcoord() {
		return titleWithZcoord;
	}

	/**
	 * set to true 
	 * @param titleWithZcoord the titleWithZcoord to set if title of layer window should contain the z coordinate
	 */
	public void setTitleWithZcoord(boolean titleWithZcoord) {
		this.titleWithZcoord = titleWithZcoord;
	}
}
