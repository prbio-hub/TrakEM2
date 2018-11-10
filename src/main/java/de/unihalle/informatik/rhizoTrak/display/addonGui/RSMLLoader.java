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
