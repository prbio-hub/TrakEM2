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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.addonGui.ImageImport;
import de.unihalle.informatik.rhizoTrak.persistence.Loader;

public class RhizoImages
{
	
	private JFrame imageLoaderFrame;
	
	private RhizoMain rhizoMain;
	
	public RhizoImages(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
	}

	/**
	 * Open image loading dialogue
	 * @author Axel
	 */
	public void createImageLoaderFrame()
	{
		String title = "Image Loader";
		if(null != rhizoMain.getProjectConfig().getImageSearchDir());
			title = "Image Loader - " + rhizoMain.getProjectConfig().getImageSearchDir().getAbsolutePath();
		if(imageLoaderFrame!=null)
		{
			imageLoaderFrame.setVisible(true);
			imageLoaderFrame.toFront();
		}
		else
		{
			imageLoaderFrame = new JFrame(title);
			imageLoaderFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			JPanel temp = new ImageImport(rhizoMain);
			imageLoaderFrame.add(temp);
			imageLoaderFrame.pack();
			imageLoaderFrame.setVisible(true);
		}

	}
	
	/**
	 * gives relative patchdir
	 * @author actyc
	 * @return String representing the relative patches directory
	 */
	public String convertToRelativPath(String currentPathString){
		if(currentPathString.contains(File.separator) && !currentPathString.contains(".")){
			Path currentPath = Paths.get(currentPathString);
			Path base = Paths.get(rhizoMain.getProjectConfig().getImageSearchDir().getAbsolutePath());
			Path relativPath = base.relativize(currentPath);
			//Utils.log("convert path from: "+currentPathString+" to: "+relativPath.toString());
			return relativPath.toString();
		}
		return currentPathString;

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
        if(imageLoaderFrame == null) return;
        
    	imageLoaderFrame.dispose();
    }
}
