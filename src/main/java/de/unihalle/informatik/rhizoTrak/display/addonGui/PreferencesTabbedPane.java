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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.unihalle.informatik.rhizoTrak.addon.RhizoMain;
import de.unihalle.informatik.rhizoTrak.addon.RhizoProjectConfig;
import de.unihalle.informatik.rhizoTrak.addon.RhizoStatusLabel;
import de.unihalle.informatik.rhizoTrak.addon.RhizoUtils;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

@SuppressWarnings("serial")
public class PreferencesTabbedPane extends JTabbedPane 
{
	// first tab
	private JPanel statusPanel;
	private JPanel dynamicPanel;
	private JPanel fixedPanel;
	
	// list of JPanels contained in dynamicPanel
	private List<JPanel> panelList = new ArrayList<JPanel>();
	private List<JPanel> selectedPanels = new ArrayList<JPanel>();
	
	private final Color selectColor = Color.LIGHT_GRAY;
	
	// list of JTextFields and JLabels that corresponds to the list of JPanels
	private List<JTextField> textFieldList = new ArrayList<JTextField>();
	private List<JLabel> labelList = new ArrayList<JLabel>();
	
	private Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
	private Border selectBorder = BorderFactory.createLineBorder(selectColor);
	
	// second tab
	private JPanel mappingPanel;
	// alternative to using a ComboBoxModel because we can't use the same model for every ComboBox
	private List<String> choices = new ArrayList<String>(); 
	private Stack<JComboBox<String>> comboStack = new Stack<JComboBox<String>>();
	private Stack<Component> comboGlueStack = new Stack<Component>();
       
	// third tab
	private JPanel configurePanel;
	private JPanel configChoicesPanel;
	private String CONFIRM_MERGETREELINES = "Confirm merge treelines";
	private String CONFIRM_SPLITREELINE = "Confirm split treeline";
	private String FULLGUI = "Full GUI (includes all TrakEM2 features)";
	private String SHOW_CALIBRATION_INFO = "Show calibration info";

	
	// general members
	private RhizoMain rhizoMain = null;
	private RhizoProjectConfig config = null;
	
	static private final String HIGHLIGHTCOLOR1ACTIONSTRING1 ="Highlightcolor 1";
	static private final String HIGHLIGHTCOLOR1ACTIONSTRING2 ="Highlightcolor 2";

	static private final String RECEIVERNODECOLORSTRING = "Color active node of treeline";
	
	
	public PreferencesTabbedPane(RhizoMain rhizoMain)
	{
		this.rhizoMain = rhizoMain;
		this.config = rhizoMain.getProjectConfig();
		addStatusTab();
		addComboBoxTab();
		addConfigureTab();
	}
	
       
	private void addComboBoxTab()
	{
		// set up tab
		mappingPanel = new JPanel();
		mappingPanel.setLayout(new BoxLayout(mappingPanel, BoxLayout.Y_AXIS));
		mappingPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel comboBoxPanel = new JPanel();
		comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.Y_AXIS));	
		
		// getAllUserDefinedStatusLabel() order does not correspond to statusLabelList order
		// but these are the ones we need! (sp)
//		for(int i = 0; i < config.sizeStatusLabelMapping(); i++)	
//		{
		for ( RhizoStatusLabel sl : config.getAllUserDefinedStatusLabel() ) {
			choices.add( sl.getName());
		}
		choices.sort( String.CASE_INSENSITIVE_ORDER);

		for(int i = 0; i < config.sizeStatusLabelMapping(); i++)
		{
			addComboBox(i, comboBoxPanel);
		}
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		
		JButton addButton = new JButton("Add mapping");
		addButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				// default selected item = status that was added last
				addComboBox(choices.size() - 1, comboBoxPanel);
				config.appendStatusLabelMapping(config.getStatusLabel(choices.get(choices.size() - 1)));
				mappingPanel.revalidate();
				mappingPanel.repaint();
			}
			
		});
		JButton removeButton = new JButton("Remove mapping");
		removeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if(!comboStack.isEmpty())
				{
					int option = JOptionPane.showConfirmDialog(null, "This will delete the last mapping in the list. Are you sure?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
					if(option == JOptionPane.OK_OPTION)
					{
						if(!RhizoUtils.segmentsExist(rhizoMain.getProject(), comboStack.size() - 1))
						{
							comboBoxPanel.remove(comboStack.pop());
							comboBoxPanel.remove(comboGlueStack.pop());
							config.popStatusLabelMapping();
							mappingPanel.revalidate();
							mappingPanel.repaint();
						}
						else 
						{
							int option2 = JOptionPane.showConfirmDialog(null, "You are about to remove a status label that is currently mapped to at least one segment.\n"
									+ "If you continue, the corresponding segments will be set to "+RhizoProjectConfig.NAME_UNDEFINED, "Confirm", JOptionPane.OK_CANCEL_OPTION);
							if(option2 == JOptionPane.OK_OPTION)
							{
								RhizoUtils.setSegmentsStatus(rhizoMain.getProject(), comboStack.size()-1, (byte) RhizoProjectConfig.STATUS_UNDEFINED);
								comboBoxPanel.remove(comboGlueStack.pop());
								comboBoxPanel.remove(comboStack.pop());
								config.popStatusLabelMapping();
								mappingPanel.revalidate();
								mappingPanel.repaint();
								
								Display.getFront(rhizoMain.getProject()).repaintAll();
							}
						}	
					}
				}
			}
			
		});
		
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		
		mappingPanel.add(comboBoxPanel);
		mappingPanel.add(buttonPanel);
		
		this.addTab("Label Mappings", mappingPanel);
	}

	private void addStatusTab()
	{
		// set up tab
		statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
		statusPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		dynamicPanel = new JPanel();
		dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));
		dynamicPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
		
		fixedPanel = new JPanel();
		fixedPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		fixedPanel.setLayout(new BoxLayout(fixedPanel, BoxLayout.Y_AXIS));

		
		// panel header
		JPanel jPanelNames = new JPanel();
		JLabel jLabelStatus = new JLabel("state");
		jLabelStatus.setFont(new Font(jLabelStatus.getFont().getName(), Font.BOLD, jLabelStatus.getFont().getSize()));
		Filler filler1 = new Filler(new Dimension(0, 10), new Dimension(0, 10), new Dimension(32767, 10));
		JLabel jLabelAlpha = new JLabel("alpha");
		jLabelAlpha.setFont(new Font(jLabelAlpha.getFont().getName(), Font.BOLD, jLabelAlpha.getFont().getSize()));
		Filler filler2 = new Filler(new Dimension(0, 25), new Dimension(0, 25), new Dimension(32767, 25));
		JLabel jLabelSelectable = new JLabel("selectable");
		jLabelSelectable.setFont(new Font(jLabelSelectable.getFont().getName(), Font.BOLD, jLabelSelectable.getFont().getSize()));
		Filler filler3 = new Filler(new Dimension(5, 0), new Dimension(5, 0), new Dimension(5, 0));
		JLabel jLabelColor = new JLabel("color");
		jLabelColor.setFont(new Font(jLabelColor.getFont().getName(), Font.BOLD, jLabelColor.getFont().getSize()));

		jPanelNames.setLayout(new BoxLayout(jPanelNames, BoxLayout.LINE_AXIS));
		jPanelNames.add(jLabelStatus);
		jPanelNames.add(filler1);
		jPanelNames.add(jLabelAlpha);
		jPanelNames.add(filler2);
		jPanelNames.add(jLabelSelectable);
		jPanelNames.add(filler3);
		jPanelNames.add(jLabelColor);

		statusPanel.add(jPanelNames);

		// add status labels
		for ( RhizoStatusLabel sl : config.getAllUserDefinedStatusLabel() ) {
			Utils.log( "VisibilityPanel: add " + sl.getName());
			addStatus( sl, dynamicPanel);
		}
		
		statusPanel.add(dynamicPanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		
		String editingButtonString = "Edit Abbreviations";
		JButton editingButton = new JButton(editingButtonString);
		editingButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if(editingButton.getText().equals(editingButtonString))
				{
					 for(JTextField tf: textFieldList) tf.setEnabled(true);
					 editingButton.setText("Disable Editing");
				}
				else 
				{
					 for(JTextField tf: textFieldList) tf.setEnabled(false);
					 editingButton.setText(editingButtonString);
				}
			}
			
		});
		
		JButton addButton = new JButton("Add Status");
		addButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				JTextField fullNameTF = new JTextField();
				JTextField abbrevTF = new JTextField();
				Object[] message = {"Full Name:", fullNameTF, "Abbreviation:", abbrevTF};
				
				int option = JOptionPane.showConfirmDialog(null, message, "Add Status", JOptionPane.OK_CANCEL_OPTION);
				if(option == JOptionPane.OK_OPTION)
				{
					if(fullNameTF.getText().equals("") || abbrevTF.getText().equals("")) Utils.showMessage("New status was not added. At least one field was empty.");
					else
					{
						if(!choices.contains(fullNameTF.getText()))
						{
							// add to config
							RhizoStatusLabel sl = new RhizoStatusLabel(config, fullNameTF.getText(), abbrevTF.getText(),
									RhizoProjectConfig.DEFAULT_STATUS_COLOR, RhizoProjectConfig.DEFAULT_ALPHA, RhizoProjectConfig.DEFAULT_SELECTABLE);
							
							config.addStatusLabelToSet(sl);
							
							// update choices
							choices.add(fullNameTF.getText());
							choices.sort( String.CASE_INSENSITIVE_ORDER);
//							for(JComboBox<String> c: comboStack) c.addItem(fullNameTF.getText());
							for(JComboBox<String> c: comboStack) {
								c.removeAllItems();
								for ( String n : choices) {
									c.addItem(n);
								}
							}
							
							// add to status tab
							addStatus(sl, dynamicPanel);
							
							dynamicPanel.revalidate();
							dynamicPanel.repaint();
						}
						else Utils.showMessage("New status label was not added. A status with the same name already exists.");
					}
				}
			}
			
		});
		JButton removeButton = new JButton("Remove Selected Status");
		removeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!panelList.isEmpty() && !selectedPanels.isEmpty())
				{
					int option = JOptionPane.showConfirmDialog(null, "This will delete the selected status. Are you sure?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
					if(option == JOptionPane.OK_OPTION)
					{
						// temp lists for safe removal after iteration
						List<JPanel> panelsToBeRemoved = new ArrayList<JPanel>();
						List<JTextField> textFieldsToBeRemoved = new ArrayList<JTextField>();
						List<JLabel> labelsToBeRemoved = new ArrayList<JLabel>();
						List<String> choicesToBeRemoved = new ArrayList<String>();
						
						String notRemoved = "";
											
						// currently used status in the label mapping tab
						List<String> mappedStatus = new ArrayList<String>();
						for(JComboBox<String> c: comboStack)
						{
							String selectedString = (String) c.getSelectedItem();
							mappedStatus.add(selectedString);
						}
						
						for(JPanel selectedPanel: selectedPanels)
						{
							int i = panelList.indexOf(selectedPanel);
							
							if(i != -1)
							{
								String stringToBeRemoved = labelList.get(i).getText();
								
								if(!mappedStatus.contains(stringToBeRemoved))
								{
									panelsToBeRemoved.add(panelList.get(i));
									textFieldsToBeRemoved.add(textFieldList.get(i));
									labelsToBeRemoved.add(labelList.get(i));
									choicesToBeRemoved.add(stringToBeRemoved);
									
									config.removeStatusLabelFromSet(stringToBeRemoved);
								}
								else notRemoved += stringToBeRemoved + "\n";
							}
						}
		
						// update lists
						panelList.removeAll(panelsToBeRemoved);
						textFieldList.removeAll(textFieldsToBeRemoved);
						labelList.removeAll(labelsToBeRemoved);
						choices.removeAll(choicesToBeRemoved);
						
						// update comboStack choices
						for(JComboBox<String> c: comboStack)
						{
							for(String s: choicesToBeRemoved)
							{
								c.removeItem(s);
							}
						}
						
						// update dynamicPanel
						for(JPanel panel: panelsToBeRemoved)
						{
							dynamicPanel.remove(panel);
						}

						dynamicPanel.revalidate();
						dynamicPanel.repaint();
						
						Display.getFront(rhizoMain.getProject()).repaintAll();
						
						if(!notRemoved.equals("")) Utils.showMessage("The following status were not removed because they are currently mapped to an integer:\n" + notRemoved);
					}
				}
			}
		});
		
		
		buttonPanel.add(editingButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		
		statusPanel.add(buttonPanel);
		
		statusPanel.add(new JSeparator());

		// then add the fixed labels
		for ( int i : config.getFixedStatusLabelInt() ) {
			Utils.log( "VisibilityPanel: add " + i);
			addStatus(i, fixedPanel);
		}
		
		statusPanel.add(fixedPanel);
		fixedPanel.add(new JSeparator());

		// add highlighting color
		addColor(HIGHLIGHTCOLOR1ACTIONSTRING1, config.getHighlightColor1(), fixedPanel);
		addColor(HIGHLIGHTCOLOR1ACTIONSTRING2, config.getHighlightColor2(), fixedPanel);

		addColor(RECEIVERNODECOLORSTRING, config.getReceiverNodeColor(), fixedPanel);

		statusPanel.add(new JSeparator());

		this.addTab("Color & Visibility", statusPanel);
	}
	
	/**
	 * Panel for project configurations
	 */
	private void addConfigureTab() {
		configurePanel = new JPanel();
		configurePanel.setLayout(new BoxLayout(configurePanel, BoxLayout.Y_AXIS));
		configurePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		configChoicesPanel = new JPanel();
		configChoicesPanel.setLayout(new BoxLayout(configChoicesPanel, BoxLayout.Y_AXIS));
		configChoicesPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
		
		configChoicesPanel.add( addChoice( this.rhizoMain.getProjectConfig().isAskMergeTreelines(), CONFIRM_MERGETREELINES));
		configChoicesPanel.add( addChoice( this.rhizoMain.getProjectConfig().isAskSplitTreeline(), CONFIRM_SPLITREELINE));
		configChoicesPanel.add( addChoice( this.rhizoMain.getProjectConfig().isFullGUI(), FULLGUI));
		configChoicesPanel.add( addChoice( this.rhizoMain.getProjectConfig().isShowCalibrationInfo(), SHOW_CALIBRATION_INFO));

		configurePanel.add( configChoicesPanel);
		this.addTab("Configuration", configurePanel);
	}
	
	private JCheckBox addChoice( boolean choice, String name) {
		JCheckBox checkBox = new JCheckBox( name, choice);
		checkBox.setActionCommand( name);
		checkBox.addActionListener( configCheckAction);
		return checkBox;

	}

	private void addComboBox(int selectedIndex, JPanel parentPanel)
	{

		JComboBox<String> combo = new JComboBox<String>(choices.toArray(new String[choices.size()]));
		combo.setSelectedIndex(selectedIndex);
		combo.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String selected = (String) combo.getSelectedItem();
				config.replaceStatusLabelMapping(comboStack.indexOf(e.getSource()), config.getStatusLabel(selected));
				
				Display.getFront(rhizoMain.getProject()).repaintAll();
			}
		});
		
		parentPanel.add(combo);
		
		Component glue = Box.createVerticalGlue();
		parentPanel.add(glue);
		
		comboStack.push(combo);
		comboGlueStack.push(glue);
	}
	
	private void addStatus( int i, JPanel parentPanel) {
		RhizoStatusLabel sl = config.getStatusLabel(i);
		addStatus(sl, parentPanel);
	}

	
	/** we assume that the status label for <code>i</code> exists
	 * 
	 * @param i
	 */
	private void addStatus(RhizoStatusLabel sl, JPanel parentPanel) {
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JLabel lab = new JLabel(sl.getName());
		panel.add(lab);
		
		panel.add(Box.createRigidArea(new Dimension(80 - Utils.getDimensions(sl.getName(), UIManager.getFont("Label.font")).width, 0)));
		
		JTextField tf = new JTextField(sl.getAbbrev());
		tf.setEnabled(false);
		tf.getDocument().addDocumentListener(new DocumentListener() // fires on text change
		{
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateTF();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateTF();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateTF();
			}
			
			public void updateTF()
			{
				String name = labelList.get(textFieldList.indexOf(tf)).getText();
				config.addStatusLabelToSet(name, tf.getText());
				
				Display.getFront(rhizoMain.getProject()).repaintAll();
			}
		});

		panel.add(tf);

		JSlider slider = new JSlider();
		slider.setMinimum(0);
		slider.setName(sl.getName());
		slider.setMaximum(255);
		slider.setValue(sl.getAlpha());

		slider.addChangeListener(sliderAction);
		panel.add(slider);

		JCheckBox checkBox = new JCheckBox("", sl.isSelectable());
		checkBox.setActionCommand(sl.getName());
		checkBox.addActionListener(clickablityAction);
		panel.add(checkBox);

		JButton button = new JButton();
		button.setActionCommand(sl.getName());
		button.addActionListener(colorChangeButton);
		button.setMaximumSize(new Dimension(33, 15));
		button.setMinimumSize(new Dimension(33, 15));
		button.setPreferredSize(new Dimension(33, 12));
		button.setContentAreaFilled(false);
		button.setOpaque(true);
		button.setBackground(sl.getColor());
		panel.add(button);
		
		panel.setBorder(emptyBorder);

		if(parentPanel.equals(dynamicPanel))
		{
			panel.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					if(!selectedPanels.contains(panel))
					{
						panel.setBackground(selectColor);
						selectedPanels.add(panel);
					}
					else
					{
						panel.setBackground(UIManager.getColor("Panel.background"));
						selectedPanels.remove(panel);
					}
				}

				public void mouseEntered(MouseEvent e)
				{
					panel.setBorder(selectBorder);
				}

				public void mouseExited(MouseEvent e)
				{
					panel.setBorder(emptyBorder);
				}
			});

			panelList.add(panel);
			labelList.add(lab);
			textFieldList.add(tf);
		}
		
		parentPanel.add(panel);
	}
	
	private void addColor( String label, Color color, JPanel parentPanel) {
		// add highlighting color
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JLabel jLabel = new JLabel();
		jLabel.setText( label);
		jLabel.setMaximumSize(new Dimension(500, 100));
		jLabel.setMinimumSize(new Dimension(12, 14));
		jLabel.setPreferredSize(new Dimension(120, 14));
		panel.add(jLabel);

		JButton jButton = new JButton();
		jButton.setActionCommand( label);
		jButton.addActionListener(colorChangeButton);
		jButton.setMaximumSize(new Dimension(33, 15));
		jButton.setMinimumSize(new Dimension(33, 15));
		jButton.setPreferredSize(new Dimension(33, 12));
		jButton.setContentAreaFilled(false);
		jButton.setOpaque(true);
		jButton.setBackground( color);
		panel.add(jButton);

		parentPanel.add(panel);
	}
	
	// Color change button action
	Action colorChangeButton = new AbstractAction("colorChangeButton") {
		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton) e.getSource();
			Color selectedColor = JColorChooser.showDialog(source, "Choose color", Color.WHITE);

			if (selectedColor != null)  {
				if ( e.getActionCommand().equals( HIGHLIGHTCOLOR1ACTIONSTRING1) ) {
					config.setHighlightColor1( new Color( 
							selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue()));

				} else if ( e.getActionCommand().equals( HIGHLIGHTCOLOR1ACTIONSTRING2) ) {
					config.setHighlightColor2( new Color( 
							selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue()));
				} else if ( e.getActionCommand().equals( RECEIVERNODECOLORSTRING) ) {
					config.setReceiverNodeColor( new Color( 
							selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue()));
				} else {
					RhizoStatusLabel sl = config.getStatusLabel(e.getActionCommand());
					sl.setColor( selectedColor);
				}
				
				rhizoMain.getRhizoColVis().applyCorrespondingColor();
				source.setBackground(selectedColor);
			}
		}
	};

	// alpha change slider action
	ChangeListener sliderAction = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e)
		{
			JSlider currentSlider = (JSlider) e.getSource();
			
			RhizoStatusLabel sl = config.getStatusLabel(currentSlider.getName());
			sl.setAlpha( currentSlider.getValue());
			

			rhizoMain.getRhizoColVis().applyCorrespondingColor();
		}
	};

	// clickablity change action
	/**
	 * This action takes care of selectable state of status labels
	 */
	Action clickablityAction = new AbstractAction("clickablityAction") {
		public void actionPerformed(ActionEvent e) {
			JCheckBox source = (JCheckBox) e.getSource();

			RhizoStatusLabel sl = config.getStatusLabel(e.getActionCommand());
			sl.setSelectable( source.isSelected());
		}
	};

	/**
	 * This action takes care of all events to change boolean states of the project configuration
	 */
	Action configCheckAction = new AbstractAction( "configCheckAction") {

		public void actionPerformed(ActionEvent e) {
			JCheckBox source = (JCheckBox) e.getSource();
			String actionCommand = e.getActionCommand();

			if ( actionCommand.equals(  CONFIRM_MERGETREELINES)) {
				rhizoMain.getProjectConfig().setAskMergeTreelines( source.isSelected());
			} else if ( actionCommand.equals( CONFIRM_SPLITREELINE)) {
				rhizoMain.getProjectConfig().setAskSplitTreeline( source.isSelected() );
			} else if ( actionCommand.equals( FULLGUI)) {
				rhizoMain.getProjectConfig().setFullGUI( source.isSelected() );
				Utils.showMessage("Full GUI will be" + (source.isSelected() ? " enabled " : " disabled ") + "on rhizoTrak restart.");
			} else if ( actionCommand.equals( SHOW_CALIBRATION_INFO)) {
				rhizoMain.getProjectConfig().setShowCalibrationInfo( source.isSelected() );
			} else {
				Utils.showMessage( "rhizoTrak", "PrefrencesTabbedPane.configCheckAction: internal error, unkonwn actionCommand " + actionCommand);
			}
		}
	};


}
