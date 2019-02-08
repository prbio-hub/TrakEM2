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

package de.unihalle.informatik.rhizoTrak.display.addonGui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.unihalle.informatik.rhizoTrak.addon.RhizoMain;
import de.unihalle.informatik.rhizoTrak.addon.RhizoStatusLabel;
import de.unihalle.informatik.rhizoTrak.addon.RhizoUtils;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

@SuppressWarnings("serial")
public class RSMLLoader extends JPanel
{
	private RhizoMain rhizoMain;
		
	// left panel and components
	private JPanel jPanelLeft;
	private JPanel jPanelRemoveClear;
	private JScrollPane jScrollPane;
	private JList<String> jListFiles;
	private DefaultListModel<String> listModel;
	private JButton jButtonRemove, jButtonClear;
	
	// right panel and components
	private JPanel jPanelRight;
	private JPanel jPanelComboBox, jPanelRightButtons;
	private JLabel jLabelComboBoxLayers, jLabelComboBoxLabels;
	private JComboBox<String> jComboBoxLayers, jComboBoxLabels;
	private List<String> layerNames; // can't use layers directly without editing Layer.toString() method
	private List<String> labelNames;
	private JButton jButtonSelect, jButtonImport, jButtonBaseDir;

	private List<File> allFiles;
	
	private final Dimension comboBoxDimension = new Dimension(100, 26);
	
	private final String APPEND = "Append...";
	
	public RSMLLoader(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
		initAndAddComponents();
	}
	
	private void initAndAddComponents()
	{
		allFiles = new ArrayList<File>();
		
		// start left panel
		listModel = new DefaultListModel<String>();
		
		jListFiles = new JList<String>(listModel);
		jListFiles.setTransferHandler(new ListTransferHandler());
		jListFiles.setDragEnabled(true);
		jListFiles.setDropMode(DropMode.INSERT);
		jListFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		jScrollPane = new JScrollPane(jListFiles);
		
		jButtonRemove = new JButton("Remove selection");
		jButtonRemove.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int index = jListFiles.getSelectedIndex();
				if(index == -1) return;
				
				allFiles.remove(index);
				listModel.remove(index);
			}
		});
		
		jButtonClear = new JButton("Clear");
		jButtonClear.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				listModel.clear();
				allFiles.clear();
				jListFiles.setTransferHandler(new ListTransferHandler());
			}
		});
		
		jPanelRemoveClear = new JPanel();
		jPanelRemoveClear.add(jButtonRemove);
		jPanelRemoveClear.add(jButtonClear);
		
		jPanelLeft = new JPanel(new BorderLayout());
		jPanelLeft.add(jScrollPane, BorderLayout.NORTH);
		jPanelLeft.add(jPanelRemoveClear, BorderLayout.SOUTH);
		
		// start right panel
		jButtonSelect = new JButton("Select RSML");
		jButtonSelect.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				openRSMLFileChooser();
			}
		});
		
		jButtonImport = new JButton("Import RSML");
		jButtonImport.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if(allFiles.size() > 0)
				{
					if(jComboBoxLayers.getSelectedItem().equals(APPEND))
					{
						rhizoMain.getRhizoRSML().readRSML(allFiles, null);
					}
					else
					{
						LayerSet layerSet = rhizoMain.getProject().getRootLayerSet();
						List<Layer> layerList = layerSet.getLayers();
						
						int index = layerNames.indexOf(jComboBoxLayers.getSelectedItem());
						
						byte labelIndex = (byte) labelNames.indexOf(jComboBoxLabels.getSelectedItem());
						
						rhizoMain.getRhizoRSML().setDefaultStatusLabel(labelIndex);
						rhizoMain.getRhizoRSML().readRSML(allFiles, layerList.get(index));
					}
					
					if(SwingUtilities.getRoot(jPanelLeft) instanceof JDialog)
					{
						JDialog jd = (JDialog) SwingUtilities.getRoot(jPanelLeft);
						jd.dispose();
					}
				}
				else Utils.showMessage("Nothing to import.\nNo RSML files were selected.");
			}
		});
		
		jButtonBaseDir = new JButton("Select base directory");
		jButtonBaseDir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				openRSMLBaseDirChooser();
			}
		});
		
		jLabelComboBoxLayers = new JLabel("Choose first layer: ");
		jLabelComboBoxLayers.setHorizontalAlignment(JLabel.RIGHT);
		
		jComboBoxLayers = createLayerComboBox();
		
		jLabelComboBoxLabels = new JLabel("Choose default status: ");
		jLabelComboBoxLabels.setHorizontalAlignment(JLabel.RIGHT);
		
		jComboBoxLabels = createLabelComboBox();
		// TODO show full name on popup but not in panel
//		jComboBox.setPreferredSize(comboBoxDimension);
//		jComboBox.setMaximumSize(comboBoxDimension);
		
		jPanelComboBox = new JPanel(new GridLayout(2, 2));
		jPanelComboBox.add(jLabelComboBoxLayers);
		jPanelComboBox.add(jComboBoxLayers);
		jPanelComboBox.add(jLabelComboBoxLabels);
		jPanelComboBox.add(jComboBoxLabels);
		
		jPanelRightButtons = new JPanel(new GridLayout(0, 1));
		jPanelRightButtons.add(jButtonSelect);
		jPanelRightButtons.add(jButtonImport);
		jPanelRightButtons.add(jButtonBaseDir);

		jPanelRight = new JPanel(new BorderLayout());
		jPanelRight.add(jPanelRightButtons, BorderLayout.NORTH);
		jPanelRight.add(jPanelComboBox, BorderLayout.SOUTH);

		
		setLayout(new BorderLayout());
		add(jPanelLeft);
		add(jPanelRight, BorderLayout.EAST);
	}
	
	private JComboBox<String> createLabelComboBox() 
	{
		Collection<RhizoStatusLabel> labels = rhizoMain.getProjectConfig().getAllUserDefinedStatusLabel();
		
		labelNames = new ArrayList<String>();
		
		for(RhizoStatusLabel rsl: labels)
		{
			String labelName = rsl.getName();
			labelNames.add(labelName);
		}
		
		return new JComboBox<String>(labelNames.toArray(new String[labelNames.size()]));
	}

	/**
	 * Creates a combobox with layers and their corresponding image names
	 * 
	 * @return
	 */
	private JComboBox<String> createLayerComboBox()
	{
		// TODO handle adding and deleting layers when RSMLLoader is open
		LayerSet layerSet = rhizoMain.getProject().getRootLayerSet();
		List<Layer> layerList = layerSet.getLayers();
		
		layerNames = new ArrayList<String>();
		
		for(int i = 0; i < layerList.size(); i++)
		{
			Layer layer = layerList.get(i);
			String imageName = RhizoUtils.getImageName(layer);
			layerNames.add("Layer " + (int) (layer.getZ()+1) + (null == imageName ? "" : " - " + imageName));
		}
		
		layerNames.add(APPEND);
		
		return new JComboBox<String>(layerNames.toArray(new String[layerNames.size()]));
	}
	
	/**
	 * Open file chooser for RSML files
	 */
	private void openRSMLFileChooser()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setCurrentDirectory(rhizoMain.getRhizoRSML().getRSMLBaseDir());
		chooser.setFileFilter(new FileNameExtensionFilter("RSML File", "rsml"));

		int result = chooser.showOpenDialog(null);
		
		if(result == JFileChooser.APPROVE_OPTION)
		{
			File[] files = chooser.getSelectedFiles();
			for (File file : files) 
			{
				listModel.addElement(file.getName());	
				allFiles.add(file);
			}
			
			jListFiles.setTransferHandler(new ListTransferHandler());
		}
	}
	
	/**
	 * Open directory chooser for the RSML base directory
	 */
	private void openRSMLBaseDirChooser()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(rhizoMain.getRhizoRSML().getRSMLBaseDir());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = chooser.showOpenDialog(null);

		if(result == JFileChooser.APPROVE_OPTION)
		{
			rhizoMain.getRhizoRSML().setRSMLBaseDir(chooser.getSelectedFile());
			rhizoMain.getRhizoRSML().getRSMLLoaderFrame().setTitle("RSML Loader - " + chooser.getSelectedFile().getAbsolutePath());
		}
	}
}
