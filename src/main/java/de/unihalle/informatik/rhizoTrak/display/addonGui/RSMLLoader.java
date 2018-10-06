package de.unihalle.informatik.rhizoTrak.display.addonGui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.unihalle.informatik.rhizoTrak.addon.RhizoMain;
import de.unihalle.informatik.rhizoTrak.addon.RhizoUtils;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;

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
	private JLabel jLabelComboBox;
	private JComboBox<String> jComboBox;
	private List<String> layerNames; // can't use layers directly without editing Layer.toString() method
	private JButton jButtonSelect, jButtonSort, jButtonImport;

	private List<File> allFiles;
	
	private final Dimension comboBoxDimension = new Dimension(100, 26);
	
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
				openSelectDialog();
			}
		});
		
		jButtonSort = new JButton("Sort");
		jButtonSort.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				//sortList();
			}
		});
		
		jButtonImport = new JButton("Import RSML");
		jButtonImport.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				LayerSet layerSet = rhizoMain.getProject().getRootLayerSet();
				List<Layer> layerList = layerSet.getLayers();
				List<Layer> layerListFromIndex = new ArrayList<Layer>();
				
				int index = layerNames.indexOf(jComboBox.getSelectedItem());
				for(int i = index; i < layerList.size(); i++)
				{
					layerListFromIndex.add(layerList.get(i));
				}
				
				rhizoMain.getRhizoRSML().readRSML(allFiles, layerListFromIndex);
				
				if(SwingUtilities.getRoot(jPanelLeft) instanceof JFrame)
				{
					JFrame jf = (JFrame) SwingUtilities.getRoot(jPanelLeft);
					jf.dispose();
				}
			}
		});
		
		jLabelComboBox = new JLabel("Choose first layer:");
		jLabelComboBox.setHorizontalAlignment(JLabel.RIGHT);
		
		jComboBox = createLayerComboBox();
		// TODO show full name on popup but not in panel
//		jComboBox.setPreferredSize(comboBoxDimension);
//		jComboBox.setMaximumSize(comboBoxDimension);
		
		jPanelComboBox = new JPanel();
		jPanelComboBox.add(jLabelComboBox);
		jPanelComboBox.add(jComboBox);
		
		jPanelRightButtons = new JPanel(new GridLayout(0, 2));
		jPanelRightButtons.add(jButtonSelect);
//		jPanelRightButtons.add(jButtonSort); // TODO
		jPanelRightButtons.add(jButtonImport);

		jPanelRight = new JPanel(new BorderLayout());
		jPanelRight.add(jPanelRightButtons, BorderLayout.NORTH);
		jPanelRight.add(jPanelComboBox, BorderLayout.SOUTH);

		
		setLayout(new BorderLayout());
		add(jPanelLeft);
		add(jPanelRight, BorderLayout.EAST);
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
		
		return new JComboBox<String>(layerNames.toArray(new String[layerNames.size()]));
	}
	
	/**
	 * Open file chooser for RSML files
	 */
	private void openSelectDialog()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setCurrentDirectory(rhizoMain.getProjectConfig().getImageSearchDir()); // TODO image search dir is temporary
		chooser.setFileFilter(new FileNameExtensionFilter("RSML File", "rsml"));

		chooser.showOpenDialog(null);
		
		File[] files = chooser.getSelectedFiles();
		for (File file : files) 
		{
			listModel.addElement(file.getName());	
			allFiles.add(file);
		}
		
		jListFiles.setTransferHandler(new ListTransferHandler());
	}
}
