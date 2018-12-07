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
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.addonGui.ImageImport;
import de.unihalle.informatik.rhizoTrak.persistence.Loader;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

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
	
	public static void addLayerAndImage(File[] files) {
		List<Double> final_targets = new ArrayList<Double>();
		int number_of_images_to_import = files.length;
		List<Double> existing_but_empty = findTargetLayers(true,number_of_images_to_import);
		
		//check if we have empty layers and if so ask the user if we should use them
		if(existing_but_empty.size()>0) {
			if(Utils.checkYN("Found empty layers. Should these be filled first?")) {
				final_targets.addAll(existing_but_empty);
			}
		}
		
		//check if we have a sufficient number of empty layers
		if(!(existing_but_empty.size()>=number_of_images_to_import)) {
			//no, so we need more targets
			int missing = number_of_images_to_import-final_targets.size();
			List<Double> new_empty_layer_targets = findTargetLayers(false,missing);
			//as the target z's are non existing layer position we have to create the layers
			addLayerIfNeeded(new_empty_layer_targets);
			//now we can add them to our final target list
			final_targets.addAll(new_empty_layer_targets);
		}
		//if nothing went terribly wrong we should have enough targets and layers by now
		//lets fill them
		LayerSet parent = Display.getFrontLayer().getParent();
		Project project = parent.getProject();
		Loader loader = project.getLoader();
		for (File file : files)
		{
			Layer currentLayer =parent.getLayer(final_targets.get(0)); 
			loader.importImage(currentLayer, 0, 0, file.getPath(), true);
			final_targets.remove(0);
		}
		
		for ( Display display : Display.getDisplays()) {
			display.updateFrameTitle();
		}
	}
	
	/**
	 * create a layer at the target-z-level if its not already exists
	 * @param targets - List of layer-z position as targets
	 * @author Axel
	 */
	private static void addLayerIfNeeded(List<Double> targets) {
		LayerSet parent = Display.getFrontLayer().getParent();
		Project project = parent.getProject();
		for (Double target : targets) {
			if(parent.getLayer(target)==null) {
				final Layer layer = new Layer(project, target, 1, parent);
				parent.add(layer);
				layer.recreateBuckets();
				layer.updateLayerTree();
			}
		}	
	}
	
	/**
	 * give List of layer-z position as targets to image import
	 * @param give_existing_layers - indicates if already existing but empty layers should be returned
	 * @param number_of_images_to_import - indicates the number of targets that should be created
	 * @author Axel
	 */
	private static List<Double> findTargetLayers(boolean give_existing_layers,int number_of_images_to_import) {
		List<Double> target = new ArrayList<Double>();
		LayerSet parent = Display.getFrontLayer().getParent();
		ArrayList<Layer> layerlist = parent.getLayers();
		if(give_existing_layers) {

			for (Layer layer : layerlist) {
				if(layer.getDisplayables(Patch.class).size()<1) {
					target.add(layer.getZ());
				}
			}
		} else {
			double lastZ = layerlist.get(layerlist.size()-1).getZ();
			for(double i=lastZ+1;i<lastZ+1+number_of_images_to_import;i=i+1) {
				target.add(i);
			}
		}		
		return target;
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
