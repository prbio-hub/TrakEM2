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

package de.unihalle.informatik.rhizoTrak.display.addonGui;

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

@SuppressWarnings("serial")
public class ImageImport extends JPanel {
	@SuppressWarnings("unused")
	private javax.swing.Box.Filler filler1;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JButton jButton5;
	private javax.swing.JList<String> jList1;
	private javax.swing.JLabel imgDir;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.DefaultListModel<String> listModel;
	
	private ArrayList<File> allFiles = new ArrayList<>();
	
	//image filter ini: constant_part > constant across all images; sort_part > part that is used to reconstruct the timeline
	private String filterReg_constant_part = "_(T|t)\\d*";
	private String filterReg_sort_part = "_\\d{3}";
        
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
		jPanel1 = new javax.swing.JPanel();
		jList1 = new javax.swing.JList<String>();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jButton4 = new javax.swing.JButton();
		jButton5 = new javax.swing.JButton();
		imgDir = new javax.swing.JLabel();
		filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0),
				new java.awt.Dimension(0, 32767));
		
		
//		//get current loaded images
//		LayerSet layerSet	=	Display.getFront().getLayerSet();
//		List<Patch> patches = layerSet.getAll(Patch.class);
//		ImagePlus imagePlus = patches.get(0).getImagePlus();
//		String[] imageNames = imagePlus.getImageStack().getSliceLabels();
		
		setLayout(new BorderLayout());
		//make listModel and load all images that are already in trackem project
//		for (String string : imageNames) {
//			if(string!=null){
//				listModel.addElement(string);	
//			}
//		}
		jList1.setModel(listModel);
		jList1.setTransferHandler(new ListTransferHandler());
		jList1.setDragEnabled(true);
		jList1.setDropMode(DropMode.INSERT);
		jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		jScrollPane1.setViewportView(jList1);
		
		add(jScrollPane1);

		
		jPanel1.setLayout(new GridLayout(3, 1));
		

		jButton1.setText("Select Images");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		jPanel1.add(jButton1);

		jButton2.setText("Import Images");
		jButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButton2ActionPerformed(evt);
				
				if(SwingUtilities.getRoot(jPanel1) instanceof JFrame)
				{
					JFrame jf = (JFrame) SwingUtilities.getRoot(jPanel1);
					jf.dispose();
				}
			}
		});
		jPanel1.add(jButton2);
		
		jButton3.setText("Sort by Timepoint");
		jButton3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				sortList(evt);
			}
		});
		jPanel1.add(jButton3);
		
		jButton4.setText("Select Image Directory");
		jButton4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				chooseImageDir();
			}
		});
		jPanel1.add(jButton4);
		
		jButton5.setText("Search New Images");
		jButton5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				autoImport();
			}
		});
		jPanel1.add(jButton5);
		
//		jPanel1.add(imgDir);
//		if(RhizoAddons.imageDir!=null){
//			imgDir.setText(RhizoAddons.imageDir.getAbsolutePath());
//		}
		JFrame parentFrame = (JFrame) SwingUtilities.getRoot(this);
		if(null != parentFrame && null != rhizoMain.getRhizoImages().getImageDir()) parentFrame.setTitle("Image Loader - "+ rhizoMain.getRhizoImages().getImageDir().getAbsolutePath());
		
		
		add(jPanel1, BorderLayout.EAST);
	}
	
	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt){
		
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);

		Component frame = null;

		chooser.showOpenDialog(frame);
		File[] files = chooser.getSelectedFiles();
		for (File file : files) {
			// file.getName();
			listModel.addElement(file.getName());	
			allFiles.add(file);
		}
		jList1.setTransferHandler(new ListTransferHandler());
	}
	
	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt){
		//TODO add code to load images to stack
        //make files array consistent
        List<File> reord = new ArrayList<File>();
        int i=0;
        for (Object string : listModel.toArray()) {
        	for (File file : allFiles) {
				if(file.getName().equals((String)string)){
					reord.add(file);
					Utils.log(string +":"+file.getName());
				}
			}
        	i++;
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
		JFileChooser dialog = new JFileChooser();
		dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int indicator = dialog.showSaveDialog(this);
		if(indicator == JFileChooser.APPROVE_OPTION)
		{
			File dir = dialog.getSelectedFile();
			rhizoMain.getRhizoImages().setImageDir(dir);
//			imgDir.setText(RhizoAddons.imageDir.getAbsolutePath());
			JFrame parentFrame = (JFrame) SwingUtilities.getRoot(this);
			if(null != parentFrame) parentFrame.setTitle("Image Loader - "+ rhizoMain.getRhizoImages().getImageDir().getAbsolutePath());
		}
		
	}
	
	// aeekz - maybe add them to the window first before importing
	private void autoImport()
	{
		Utils.log2("Start auto import:");
		File imageDir = rhizoMain.getRhizoImages().getImageDir();
		if(imageDir == null)
		{
			return;
		}
		
		List<Patch> patches = Display.getFrontLayer().getParent().getAll(Patch.class);

		ArrayList<File> toImport = new ArrayList<>();
		
				
		java.io.FileFilter ioFilter = new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				  String filename = pathname.getName();
				  if (pathname.isDirectory()) {
				    return false;

				  } else if ((filename.endsWith("jpg") || filename.endsWith("jpeg") || filename.endsWith("png") || filename.endsWith("gif") || filename.endsWith("tif") || filename.endsWith("tiff"))) {
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
		
		//if there is no patch already simply import
		if(patches.isEmpty()){
			RhizoImages.addLayerAndImage(files);
			return;
		}
		
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
		
		//filter files
		//get the tube number
		String template=patches.get(0).getImagePlus().getTitle();
		Matcher matcher = Pattern.compile(filterReg_constant_part).matcher(template);
		matcher.find();
		String currentTubeNumber = matcher.group();
		Utils.log2("found tube number was:"+ currentTubeNumber);
		//filter for the found tube
		Iterator<File> importIt = toImport.iterator();
		while(importIt.hasNext()){
			File currentFile = importIt.next();
			if(!currentFile.getName().contains(currentTubeNumber)){
				importIt.remove();
			}
		}
		
//        File[] importArray = new File[toImport.size()];
//        toImport.toArray(importArray);
//        this.files=importArray;
		allFiles.addAll(toImport);
		
		for(File file : toImport){
			listModel.addElement(file.getName());
		}
		//RhizoAddons.addLayerAndImage(toImport);
	}
}














class ListTransferHandler extends TransferHandler
{
	private int selectedindex = -1;

	public boolean canImport(TransferHandler.TransferSupport support)
	{
		return support.isDataFlavorSupported(DataFlavor.stringFlavor);
	}

	protected Transferable createTransferable(JComponent comp)
	{
		JList<String> list = (JList<String>) comp;
		selectedindex = list.getSelectedIndex();
		String value = list.getSelectedValue();
		return new StringSelection(value);
	}

	public int getSourceActions(JComponent c)
	{
		return TransferHandler.MOVE;
	}

	public boolean importData(TransferHandler.TransferSupport support)
	{
		if (!support.isDrop())
		{
			return false;
		}
		JList<String> list = (JList<String>) support.getComponent();
		DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
		JList.DropLocation dl = list.getDropLocation();
		
		int index = dl.getIndex();
		
		// string that dropped
		Transferable t = support.getTransferable();
		String data;
		try
		{
			data = (String) t.getTransferData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		listModel.insertElementAt(data, index);
		if (index < selectedindex)
		{
			selectedindex++;
		}

		return true;
	}

	public void exportDone(JComponent comp, Transferable trans, int action)
	{
		JList list = (JList) comp;
		DefaultListModel listModel = (DefaultListModel) list.getModel();
		if (action == MOVE)
		{
			listModel.remove((selectedindex));
		}
	}
}
