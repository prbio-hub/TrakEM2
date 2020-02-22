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
 *  https://prbio-hub.github.io/rhizoTrak
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
 *    https://prbio-hub.github.io/rhizoTrak
 *
 */

/* === original file header below (if any) === */

package de.unihalle.informatik.rhizoTrak.display.addonGui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import de.unihalle.informatik.rhizoTrak.addon.RhizoImages;
import de.unihalle.informatik.rhizoTrak.addon.RhizoMain;
import de.unihalle.informatik.rhizoTrak.addon.RhizoUtils;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

@SuppressWarnings("serial")
public class ImageImport extends JPanel {
	@SuppressWarnings("unused")
	private javax.swing.Box.Filler filler1;
	private javax.swing.JButton jButtonSelectImage;
	private javax.swing.JButton jButtonClearSelection;
	private javax.swing.JButton jButtonImportImages;
	private javax.swing.JButton jButtonSortByTime;
	private javax.swing.JButton jButtonSelectImageDir;
	private javax.swing.JButton jButtonSearchNewImages;
	private javax.swing.JList<String> jImageNameList;
	private javax.swing.JButton jButtonDeleteImage;
	private javax.swing.JPanel jPanelFunctions;
	private javax.swing.JPanel jPanelFilenames;
	private javax.swing.JPanel jPanelRemoveClear;
	private javax.swing.JScrollPane jScrollPane1;
	
	/**
	 * List of filename in the panel of selected images
	 * Needs to be kept in sync with <code>allFiles</code>
	 */
	private javax.swing.DefaultListModel<String> listModel;
	
	/**
	 * List of File handle of all filenames in <code>listModel</code>
	 * Needs to be kept in sync
	 */
	private ArrayList<File> allFiles = new ArrayList<>();
	
	private RhizoMain rhizoMain = null;

	public ImageImport(RhizoMain rhizoMain) 
	{
        this.rhizoMain = rhizoMain;
		iniComponents();
	}

	private void iniComponents() 
	{
		//TODO check if there are any annotations on currentImage > if yes lock stack/layers
		//TODO check if names are duplicated > if yes ask user to rename
		
		listModel	=	new javax.swing.DefaultListModel<String>();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanelFunctions = new javax.swing.JPanel();
		jImageNameList = new javax.swing.JList<String>();
		jButtonDeleteImage = new javax.swing.JButton();
		jButtonSelectImage = new javax.swing.JButton();
		jButtonClearSelection = new javax.swing.JButton();
		jButtonImportImages = new javax.swing.JButton();
		jButtonSortByTime = new javax.swing.JButton();
		jButtonSelectImageDir = new javax.swing.JButton();
		jButtonSearchNewImages = new javax.swing.JButton();
		filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0),
				new java.awt.Dimension(0, 32767));
				
		
		jImageNameList.setModel(listModel);
		jImageNameList.setTransferHandler(new ListTransferHandler());
		jImageNameList.setDragEnabled(true);
		jImageNameList.setDropMode(DropMode.INSERT);
		jImageNameList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
				
		jScrollPane1.setViewportView(jImageNameList);

		jButtonDeleteImage.setText( "Delete selection");
		jButtonDeleteImage.addActionListener(
				new ActionListener() {
					@Override public void actionPerformed( ActionEvent e ) {
						int[] indices = jImageNameList.getSelectedIndices();
						
						if (indices.length == 0) return;
						
						
						for(int i = indices[indices.length - 1]; i >= indices[0]; i--) listModel.remove(i);
						jImageNameList.clearSelection();
					}
				} );
		jButtonClearSelection.setText("Clear");
		jButtonClearSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButtonClearSelectionActionPerformed(evt);
			}
		});

		jPanelRemoveClear = new JPanel();
		jPanelRemoveClear.add(jButtonDeleteImage);
		jPanelRemoveClear.add(jButtonClearSelection);

		jPanelFilenames = new JPanel();
		jPanelFilenames.setLayout( new BorderLayout());
		jPanelFilenames.add(jScrollPane1, BorderLayout.CENTER);
		jPanelFilenames.add( jPanelRemoveClear, BorderLayout.SOUTH);
		
		jButtonSelectImage.setText("Select Images");
		jButtonSelectImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButtonSelectImageActionPerformed(evt);
			}
		});

		jButtonImportImages.setText("Import Images");
		jButtonImportImages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButtonImportImagesActionPerformed(evt);
				
				if(SwingUtilities.getRoot(jPanelFunctions) instanceof JFrame)
				{
					JFrame jf = (JFrame) SwingUtilities.getRoot(jPanelFunctions);
					jf.dispose();
				}
			}
		});
		
		jButtonSortByTime.setText("Sort by Timepoint");
		jButtonSortByTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				sortList(evt);
			}
		});
		
		jButtonSelectImageDir.setText("Select Image Directory");
		jButtonSelectImageDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				chooseImageDir();
			}
		});
		
		jButtonSearchNewImages.setText("Search New Images");
		jButtonSearchNewImages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				searchNewImages();
			}
		});
		

		jPanelFunctions.add(jButtonSelectImage);
		jPanelFunctions.add(jButtonSearchNewImages);
		jPanelFunctions.add(jButtonSortByTime);
		jPanelFunctions.add(jButtonImportImages);
		jPanelFunctions.add(jButtonSelectImageDir);
		jPanelFunctions.setLayout(new GridLayout(0,2));
		
		JFrame parentFrame = (JFrame) SwingUtilities.getRoot(this);
		if(null != parentFrame && null != rhizoMain.getProjectConfig().getImageSearchDir()) 
			parentFrame.setTitle("Image Loader - "+ rhizoMain.getProjectConfig().getImageSearchDir().getAbsolutePath());
		
		
		setLayout(new BorderLayout());
		add( jPanelFilenames);
		add(jPanelFunctions, BorderLayout.EAST);
	}
	
	private void jButtonSelectImageActionPerformed(java.awt.event.ActionEvent evt){
		
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setCurrentDirectory(rhizoMain.getProjectConfig().getImageSearchDir());

		int ans = chooser.showOpenDialog(null);
		if(ans == JFileChooser.CANCEL_OPTION) return;
		
		File[] files = chooser.getSelectedFiles();
		for (File file : files) {
			// file.getName();
			listModel.addElement(file.getName());	
			allFiles.add(file);
		}
		jImageNameList.setTransferHandler(new ListTransferHandler());
	}
	
	private void jButtonClearSelectionActionPerformed(java.awt.event.ActionEvent evt){
		listModel.clear();
		allFiles.clear();
		jImageNameList.setTransferHandler(new ListTransferHandler());
	}
	
	private void jButtonImportImagesActionPerformed(java.awt.event.ActionEvent evt){
		//TODO add code to load images to stack
        //make files array consistent
        List<File> reord = new ArrayList<File>();
        for (Object string : listModel.toArray()) {
        	for (File file : allFiles) {
				if(file.getName().equals((String)string)){
					reord.add(file);
					Utils.log(string +":"+file.getName());
				}
			}
		}
        File[] reordArray = new File[reord.size()];
        reord.toArray(reordArray);
//        files=reordArray;
        //adding appropriate number of layer and image
        
        RhizoImages.addLayerAndImage( reordArray);
        listModel.clear();
        allFiles.clear();
        
	}
	
	private void sortList(ActionEvent e)
	{
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> currentList = new ArrayList<String>();
		
		for(Object string:listModel.toArray())
		{
			currentList.add((String) string);
		}
		
		while(!currentList.isEmpty())
		{
			
			int currentMinPosi = 0;
			String currentMinString = currentList.get(0);
			
			if(currentList.size()>1)
			{
				for(int i=1;i<currentList.size();i++)
				{
					if(getSortingNumber(currentMinString) > getSortingNumber(currentList.get(i)))
					{
						currentMinPosi=i;
						currentMinString=currentList.get(i);
					}
				}
			}
			
			result.add(currentMinString);
			
			currentList.remove(currentMinPosi);
		}
		
		listModel.clear();
		for (String string : result)
		{
			listModel.addElement(string);
		}
	}
	
	private int getSortingNumber(String string)
	{
		int result = -1;
		if(string.split("_").length>5)
		{
			result = Integer.parseInt(string.split("_")[5]);
		}
		return result;
	}
	
	private void chooseImageDir()
	{
		JFileChooser dialog = new JFileChooser(rhizoMain.getProjectConfig().getImageSearchDir() );
		dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int indicator = dialog.showSaveDialog(this);
		if(indicator == JFileChooser.APPROVE_OPTION)
		{
			File dir = dialog.getSelectedFile();
			rhizoMain.getProjectConfig().setImageSearchDir(dir);
			JFrame parentFrame = (JFrame) SwingUtilities.getRoot(this);
			if(null != parentFrame) 
				parentFrame.setTitle("Image Loader - "+ rhizoMain.getProjectConfig().getImageSearchDir().getAbsolutePath());
		}
		
	}
	
	/**
	 * Searches for new images in the search directory
	 * If no image was imported until now return all images found
	 */
	private void searchNewImages()
	{
		Utils.log2("Start searching for new images:");
		File imageDir = rhizoMain.getProjectConfig().getImageSearchDir();
		if(imageDir == null) {
			return;
		}
		
		// get all images (according to extension) im image searh directory
		java.io.FileFilter ioFilter = new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				  String filename = pathname.getName();
				  if (pathname.isDirectory()) {
				    return false;

				  } else if ((filename.endsWith("jpg") || filename.endsWith("jpeg") || filename.endsWith("png") || 
						  filename.endsWith("gif") || filename.endsWith("tif") || filename.endsWith("tiff"))) {
				    return true;
				  } else {
				    return false;
				  }
			}	
		};
		
		File[] files = imageDir.listFiles(ioFilter);
		if(files.length==0){
			// open dialog to inform user
			return;
		}
				
		// eliminate images already imported
		ArrayList<File> toImport = new ArrayList<>();
		List<Patch> patches = Display.getFrontLayer().getParent().getAll(Patch.class);
		
		for (File file : files) {
			boolean imp = true;
			String currentFileName = file.getName();
			for(Patch currentPatch : patches){
				if(currentPatch.getImagePlus().getTitle().equals(currentFileName)){
					imp = false;
				}
			}
			if(imp){
				toImport.add(file);
			}
		}
		
		//filter files according to tube number, 
		// if not images yet loaded keep all
		//get the tube number
		if ( patches.size() > 0 ) {
			String filename=patches.get(0).getImagePlus().getTitle();
			String currentTube = RhizoUtils.getICAPTube( filename);
			Utils.log2("found tube :"+ currentTube);
			//filter for the found tube
			Iterator<File> importIt = toImport.iterator();
			while(importIt.hasNext()){
				File nextFilen = importIt.next();
				if ( ! currentTube.equals( RhizoUtils.getICAPTube(nextFilen.getName())) ) {
					importIt.remove();
				}
			}
		}

		// now stage for import
		for(File file : toImport){
			allFiles.add(file);
			listModel.addElement(file.getName());
		}
	}
}

class ListTransferHandler extends StringTransferHandler
{
	private int[] indices = null;
	private int addIndex = -1;
	private int addCount = 0;
	
	@Override
	protected String exportString(JComponent c)
	{
		Utils.log("@exportString");
		JList list = (JList) c;
		indices = list.getSelectedIndices();
		List<String> values = list.getSelectedValuesList();
		
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < values.size(); i++)
		{
			buffer.append(null == values.get(i) ? "" : values.get(i));
			if(i != values.size() - 1) buffer.append("\n");
		}
		
		return buffer.toString();
	}
	
	@Override
	protected void importString(JComponent c, String s)
	{
		Utils.log("@importString");
		JList list = (JList) c;
		DefaultListModel listModel = (DefaultListModel) list.getModel();
		int index = list.getDropLocation().getIndex();
		Utils.log("huhu1");
		Utils.log(Arrays.toString(indices));
		Utils.log("index " + index);
		Utils.log("are you nuts" + (indices[0] - 1));
		if(null != indices && index > indices[0] - 1 && index <= indices[indices.length - 1])
		{
			indices = null;
			return;
		}
		Utils.log("huhu2");
		
		int max = listModel.getSize();
		if(index < 0 || index > max) index = max;

		Utils.log("huhu3" + " " + index);
		Utils.log("\n" + s);
		
		addIndex = index;
		String[] values = s.split("\n");
		addCount = values.length;
		for(int i = values.length - 1; i >= 0 ; i--)
		{
			listModel.add(index, values[i]);
		}
	}
	
	@Override
	protected void cleanup(JComponent c, boolean remove)
	{		
		Utils.log("@cleanup");
		
		if(remove && null != indices)
		{
			JList list = (JList) c;
			DefaultListModel listModel = (DefaultListModel) list.getModel();
			
			if(addCount > 0)
			{
				for(int i = 0; i < indices.length; i++)
				{
					if(indices[i] > addIndex) indices[i] += addCount;
				}
			}
			
			for(int i = indices.length - 1; i >= 0; i--)
			{
				listModel.remove(indices[i]);
			}
		}
	}
}

abstract class StringTransferHandler extends TransferHandler
{
	protected abstract String exportString(JComponent c);
	protected abstract void importString(JComponent c, String str);
	protected abstract void cleanup(JComponent c, boolean remove);
	
	protected Transferable createTransferable(JComponent c)
	{
		return new StringSelection((exportString(c)));
	}
	
	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}
	
	public boolean importData(JComponent c, Transferable t)
	{
		if(canImport(c, t.getTransferDataFlavors()))
		{
			try
			{
				String s = (String) t.getTransferData(DataFlavor.stringFlavor);
				importString(c, s);
				return true;
			}
			catch(UnsupportedFlavorException | IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	protected void exportDone(JComponent c, Transferable data, int action)
	{
		cleanup(c, action == MOVE);
	}
	
	public boolean canImport(JComponent c, DataFlavor[] flavors)
	{
		for(int i = 0; i < flavors.length; i++)
		{
			if(DataFlavor.stringFlavor.equals(flavors[i])) return true;
		}
		
		return false;
	}
}