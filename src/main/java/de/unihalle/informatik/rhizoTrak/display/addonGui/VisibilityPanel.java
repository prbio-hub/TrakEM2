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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.unihalle.informatik.rhizoTrak.config.Config.StatusList.Status;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class VisibilityPanel extends JPanel 
{

	
	private Box.Filler filler3;
	private Box.Filler filler4;
	private Box.Filler filler5;
	private JLabel jLabel31;
	private JLabel jLabel32;
	private JLabel jLabel33;
	private JLabel jLabel34;
	private JPanel jPanelNames;
        public RhizoAddons rhizoAddons=null;

	public VisibilityPanel(RhizoAddons rhizoAddons)
	{
		this.rhizoAddons = rhizoAddons;
		initComponents();
	}

	// TODO: more cleaning
	private void initComponents()
	{

		jPanelNames = new JPanel();
		jLabel31 = new JLabel();
		filler3 = new Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10),
				new java.awt.Dimension(32767, 10));
		jLabel32 = new JLabel();
		filler5 = new Box.Filler(new java.awt.Dimension(0, 25), new java.awt.Dimension(0, 25),
				new java.awt.Dimension(32767, 25));
		jLabel33 = new JLabel();
		filler4 = new Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0),
				new java.awt.Dimension(5, 0));
		jLabel34 = new JLabel();

		setMinimumSize(new java.awt.Dimension(300, 320));
		setName("filter panel"); // NOI18N
		setPreferredSize(new java.awt.Dimension(300, 320));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		jPanelNames.setLayout(new BoxLayout(jPanelNames, BoxLayout.LINE_AXIS));

		jLabel31.setText("state");
		jPanelNames.add(jLabel31);
		jPanelNames.add(filler3);

		jLabel32.setText("alpha");
		jPanelNames.add(jLabel32);
		jPanelNames.add(filler5);

		jLabel33.setText("selectable");
		jPanelNames.add(jLabel33);
		jPanelNames.add(filler4);

		jLabel34.setText("color");
		jPanelNames.add(jLabel34);

		add(jPanelNames);
		
		// TODO: add 3 default status first

		HashMap<Integer, Status> map = rhizoAddons.statusMap;
		for(int i: rhizoAddons.statusMap.keySet())
		{
			Status s = map.get(i);
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			
			String temp = s.getAbbreviation()+" | "+s.getFullName();
			JLabel lab = new JLabel(temp);
			panel.add(lab);
			
			// workaround TODO: find max dimensions
			panel.add(Box.createRigidArea(new Dimension(80 - Utils.getDimensions(temp, UIManager.getFont("Label.font")).width, 0)));
			
			JSlider slider = new JSlider();
			slider.setMinimum(0);
			slider.setName(Integer.toString(i));
			slider.setMaximum(255);
			slider.setValue(s.getAlpha().intValue());
			slider.addChangeListener(sliderAction);
			panel.add(slider);
			
			JCheckBox checkBox = new JCheckBox("", true);
			checkBox.setActionCommand(Integer.toString(i));
			checkBox.addActionListener(clickablityAction);
			panel.add(checkBox);
			
			JButton button = new JButton();
			button.setActionCommand(Integer.toString(i));
			button.addActionListener(colorChangeButton);
			button.setMaximumSize(new java.awt.Dimension(33, 15));
			button.setMinimumSize(new java.awt.Dimension(33, 15));
			button.setPreferredSize(new java.awt.Dimension(33, 12));
			button.setContentAreaFilled(false);
			button.setOpaque(true);
			button.setBackground(rhizoAddons.getColorFromStatusMap(i));
			panel.add(button);

			add(panel);
			if(i == rhizoAddons.getStatusMapSize()) add(new JSeparator());
		}
		
	}
        
	// Color change button action
	Action colorChangeButton = new AbstractAction("colorChangeButton") {
		public void actionPerformed(ActionEvent e) {
			int index = Integer.parseInt(e.getActionCommand());
			JButton source = (JButton) e.getSource();

			Color selectedColor = JColorChooser.showDialog(source, "Choose color", Color.WHITE);
			if (selectedColor != null) 
			{
				Status s = rhizoAddons.statusMap.get(index);
				s.setRed(BigInteger.valueOf(selectedColor.getRed()));
				s.setGreen(BigInteger.valueOf(selectedColor.getGreen()));
				s.setBlue(BigInteger.valueOf(selectedColor.getBlue()));
				rhizoAddons.statusMap.put(index, s);

				rhizoAddons.applyCorrespondingColor();
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
			int index = Integer.parseInt(currentSlider.getName()); // name will always be an integer
			
			Status s = rhizoAddons.statusMap.get(index);
			s.setAlpha(BigInteger.valueOf(currentSlider.getValue()));
			rhizoAddons.statusMap.put(index, s);
			
			rhizoAddons.applyCorrespondingColor();
			// cButton.setBackground(newColor);

		}
	};

	// clickablity change action
	Action clickablityAction = new AbstractAction("clickablityAction") {
		public void actionPerformed(ActionEvent e) {
			int index = Integer.parseInt(e.getActionCommand());
			JCheckBox source = (JCheckBox) e.getSource();

			Status s = rhizoAddons.statusMap.get(index);
			s.setSelectable(source.isSelected());
			rhizoAddons.statusMap.put(index, s);
		}
	};
}
