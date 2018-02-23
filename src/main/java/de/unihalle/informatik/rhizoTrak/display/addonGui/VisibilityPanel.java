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
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class VisibilityPanel extends JPanel {

	public VisibilityPanel() {
		initComponents();
	}

	private void initComponents() {

		jPanelNames = new javax.swing.JPanel();
		jLabel31 = new javax.swing.JLabel();
		filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10),
				new java.awt.Dimension(32767, 10));
		jLabel32 = new javax.swing.JLabel();
		filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 25), new java.awt.Dimension(0, 25),
				new java.awt.Dimension(32767, 25));
		jLabel33 = new javax.swing.JLabel();
		filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0),
				new java.awt.Dimension(5, 0));
		jLabel34 = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jSlider1 = new javax.swing.JSlider();
		jCheckBox1 = new javax.swing.JCheckBox();
		jButton1 = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		jSlider2 = new javax.swing.JSlider();
		jCheckBox2 = new javax.swing.JCheckBox();
		jButton2 = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jSlider3 = new javax.swing.JSlider();
		jCheckBox3 = new javax.swing.JCheckBox();
		jButton3 = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		jLabel4 = new javax.swing.JLabel();
		jSlider4 = new javax.swing.JSlider();
		jCheckBox4 = new javax.swing.JCheckBox();
		jButton4 = new javax.swing.JButton();
		jPanel5 = new javax.swing.JPanel();
		jLabel5 = new javax.swing.JLabel();
		jSlider5 = new javax.swing.JSlider();
		jCheckBox5 = new javax.swing.JCheckBox();
		jButton5 = new javax.swing.JButton();
		jPanel6 = new javax.swing.JPanel();
		jLabel6 = new javax.swing.JLabel();
		jSlider6 = new javax.swing.JSlider();
		jCheckBox6 = new javax.swing.JCheckBox();
		jButton6 = new javax.swing.JButton();
		jPanel7 = new javax.swing.JPanel();
		jLabel7 = new javax.swing.JLabel();
		jSlider7 = new javax.swing.JSlider();
		jCheckBox7 = new javax.swing.JCheckBox();
		jButton7 = new javax.swing.JButton();
		jPanel8 = new javax.swing.JPanel();
		jLabel8 = new javax.swing.JLabel();
		jSlider8 = new javax.swing.JSlider();
		jCheckBox8 = new javax.swing.JCheckBox();
		jButton8 = new javax.swing.JButton();
		jPanel9 = new javax.swing.JPanel();
		jLabel9 = new javax.swing.JLabel();
		jSlider9 = new javax.swing.JSlider();
		jCheckBox9 = new javax.swing.JCheckBox();
		jButton9 = new javax.swing.JButton();
		jPanel10 = new javax.swing.JPanel();
		jLabel10 = new javax.swing.JLabel();
		jSlider10 = new javax.swing.JSlider();
		jCheckBox10 = new javax.swing.JCheckBox();
		jButton10 = new javax.swing.JButton();
		jPanel11 = new javax.swing.JPanel();
		jLabel11 = new javax.swing.JLabel();
		jSlider11 = new javax.swing.JSlider();
		jCheckBox11 = new javax.swing.JCheckBox();
		jButton11 = new javax.swing.JButton();
		jPanel12 = new javax.swing.JPanel();
		jLabel12 = new javax.swing.JLabel();
		jButton12 = new javax.swing.JButton();
		

		setMinimumSize(new java.awt.Dimension(300, 320));
		setName("filter panel"); // NOI18N
		setPreferredSize(new java.awt.Dimension(300, 320));
		setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		jPanelNames.setLayout(new javax.swing.BoxLayout(jPanelNames, javax.swing.BoxLayout.LINE_AXIS));

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

		if(RhizoAddons.statusFileExists)
		{
			List<String> status = RhizoAddons.statusList;
			List<String> statusAbbr = RhizoAddons.statusListAbbr;
			
			for(int i = 0; i < status.size(); i++)
			{
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				
				
				String temp = statusAbbr.get(i)+" | "+status.get(i);
				JLabel lab = new JLabel(temp);
				panel.add(lab);
				
				// workaround TODO: find max dimensions
				panel.add(Box.createRigidArea(new Dimension(80 - Utils.getDimensions(temp, UIManager.getFont("Label.font")).width, 0)));
				
				JSlider slider = new JSlider();
				slider.setMinimum(0);
				slider.setName(Integer.toString(11-i));
				slider.setMaximum(255);
				slider.setValue(255);
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
				button.setBackground(RhizoAddons.confidencColors.get((byte) i));
				panel.add(button);
				
				Component[] c = { slider, button };
				componentCollection_hash.put(i, c);
				
				add(panel);
			}

		}
		else // temporary solution
		{
			jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

			jLabel1.setText("10");
			jPanel1.add(jLabel1);

			jSlider1.setName("1");
			jSlider1.setMinimum(0);
			jSlider1.setMaximum(255);
			jSlider1.setValue(255);
			jSlider1.addChangeListener(sliderAction);
			jPanel1.add(jSlider1);

			jCheckBox1.setSelected(checkBoxArray[10]);
			jCheckBox1.setActionCommand("10");
			jCheckBox1.addActionListener(clickablityAction);
			jPanel1.add(jCheckBox1);

			jButton1.setActionCommand("10");
			jButton1.addActionListener(colorChangeButton);
			jButton1.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton1.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton1.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton1.setContentAreaFilled(false);
			jButton1.setOpaque(true);
			jButton1.setBackground(RhizoAddons.confidencColors.get((byte) 10));
			jPanel1.add(jButton1);

			Component[] c1 = { jSlider1, jButton1 };
			componentCollection_hash.put(10, c1);
			add(jPanel1);

			jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

			jLabel2.setText("9");
			jLabel2.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel2.setMinimumSize(new java.awt.Dimension(12, 14));
			jLabel2.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel2.add(jLabel2);
			
			jSlider2.setName("2");
			jSlider2.setMinimum(0);
			jSlider2.setMaximum(255);
			jSlider2.setValue(255);
			jSlider2.addChangeListener(sliderAction);
			jPanel2.add(jSlider2);

			jCheckBox2.setSelected(checkBoxArray[9]);
			jCheckBox2.setActionCommand("9");
			jCheckBox2.addActionListener(clickablityAction);
			jPanel2.add(jCheckBox2);

			jButton2.setActionCommand("9");
			jButton2.addActionListener(colorChangeButton);
			jButton2.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton2.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton2.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton2.setContentAreaFilled(false);
			jButton2.setOpaque(true);
			jButton2.setBackground(RhizoAddons.confidencColors.get((byte) 9));
			jPanel2.add(jButton2);

			Component[] c2 = { jSlider2, jButton2 };
			componentCollection_hash.put(9, c2);
			add(jPanel2);

			jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

			jLabel3.setText("8");
			jLabel3.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel3.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel3.add(jLabel3);

			jSlider3.setName("3");
			jSlider3.setMinimum(0);
			jSlider3.setMaximum(255);
			jSlider3.setValue(255);
			jSlider3.addChangeListener(sliderAction);
			jPanel3.add(jSlider3);

			jCheckBox3.setSelected(checkBoxArray[8]);
			jCheckBox3.setActionCommand("8");
			jCheckBox3.addActionListener(clickablityAction);
			jPanel3.add(jCheckBox3);

			jButton3.setActionCommand("8");
			jButton3.addActionListener(colorChangeButton);
			jButton3.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton3.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton3.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton3.setContentAreaFilled(false);
			jButton3.setOpaque(true);
			jButton3.setBackground(RhizoAddons.confidencColors.get((byte) 8));
			jPanel3.add(jButton3);

			Component[] c3 = { jSlider3, jButton3 };
			componentCollection_hash.put(8, c3);
			add(jPanel3);

			jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

			jLabel4.setText("7");
			jLabel4.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel4.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel4.add(jLabel4);

			jSlider4.setName("4");
			jSlider4.setMinimum(0);
			jSlider4.setMaximum(255);
			jSlider4.setValue(255);
			jSlider4.addChangeListener(sliderAction);
			jPanel4.add(jSlider4);

			jCheckBox4.setSelected(checkBoxArray[7]);
			jCheckBox4.setActionCommand("7");
			jCheckBox4.addActionListener(clickablityAction);
			jPanel4.add(jCheckBox4);

			jButton4.setActionCommand("7");
			jButton4.addActionListener(colorChangeButton);
			jButton4.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton4.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton4.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton4.setContentAreaFilled(false);
			jButton4.setOpaque(true);
			jButton4.setBackground(RhizoAddons.confidencColors.get((byte) 7));
			jPanel4.add(jButton4);

			Component[] c4 = { jSlider4, jButton4 };
			componentCollection_hash.put(7, c4);
			add(jPanel4);

			jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.LINE_AXIS));

			jLabel5.setText("6");
			jLabel5.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel5.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel5.add(jLabel5);

			jSlider5.setName("5");
			jSlider5.setMinimum(0);
			jSlider5.setMaximum(255);
			jSlider5.setValue(255);
			jSlider5.addChangeListener(sliderAction);
			jPanel5.add(jSlider5);

			jCheckBox5.setSelected(checkBoxArray[6]);
			jCheckBox5.setActionCommand("6");
			jCheckBox5.addActionListener(clickablityAction);
			jPanel5.add(jCheckBox5);

			jButton5.setActionCommand("6");
			jButton5.addActionListener(colorChangeButton);
			jButton5.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton5.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton5.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton5.setContentAreaFilled(false);
			jButton5.setOpaque(true);
			jButton5.setBackground(RhizoAddons.confidencColors.get((byte) 6));
			jPanel5.add(jButton5);

			Component[] c5 = { jSlider5, jButton5 };
			componentCollection_hash.put(6, c5);
			add(jPanel5);

			jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.LINE_AXIS));

			jLabel6.setText("5");
			jLabel6.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel6.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel6.add(jLabel6);

			jSlider6.setName("6");
			jSlider6.setMinimum(0);
			jSlider6.setMaximum(255);
			jSlider6.setValue(255);
			jSlider6.addChangeListener(sliderAction);
			jPanel6.add(jSlider6);

			jCheckBox6.setSelected(checkBoxArray[5]);
			jCheckBox6.setActionCommand("5");
			jCheckBox6.addActionListener(clickablityAction);
			jPanel6.add(jCheckBox6);

			jButton6.setActionCommand("5");
			jButton6.addActionListener(colorChangeButton);
			jButton6.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton6.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton6.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton6.setContentAreaFilled(false);
			jButton6.setOpaque(true);
			jButton6.setBackground(RhizoAddons.confidencColors.get((byte) 5));
			jPanel6.add(jButton6);

			Component[] c6 = { jSlider6, jButton6 };
			componentCollection_hash.put(5, c6);
			add(jPanel6);

			jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.LINE_AXIS));

			jLabel7.setText("4");
			jLabel7.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel7.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel7.add(jLabel7);

			jSlider7.setName("7");
			jSlider7.setMinimum(0);
			jSlider7.setMaximum(255);
			jSlider7.setValue(255);
			jSlider7.addChangeListener(sliderAction);
			jPanel7.add(jSlider7);

			jCheckBox7.setSelected(checkBoxArray[4]);
			jCheckBox7.setActionCommand("4");
			jCheckBox7.addActionListener(clickablityAction);
			jPanel7.add(jCheckBox7);

			jButton7.setActionCommand("4");
			jButton7.addActionListener(colorChangeButton);
			jButton7.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton7.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton7.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton7.setContentAreaFilled(false);
			jButton7.setOpaque(true);
			jButton7.setBackground(RhizoAddons.confidencColors.get((byte) 4));
			jPanel7.add(jButton7);

			Component[] c7 = { jSlider7, jButton7 };
			componentCollection_hash.put(4, c7);
			add(jPanel7);

			jPanel8.setLayout(new javax.swing.BoxLayout(jPanel8, javax.swing.BoxLayout.LINE_AXIS));

			jLabel8.setText("3");
			jLabel8.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel8.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel8.add(jLabel8);

			jSlider8.setName("8");
			jSlider8.setMinimum(0);
			jSlider8.setMaximum(255);
			jSlider8.setValue(255);
			jSlider8.addChangeListener(sliderAction);
			jPanel8.add(jSlider8);

			jCheckBox8.setSelected(checkBoxArray[3]);
			jCheckBox8.setActionCommand("3");
			jCheckBox8.addActionListener(clickablityAction);
			jPanel8.add(jCheckBox8);

			jButton8.setActionCommand("3");
			jButton8.addActionListener(colorChangeButton);
			jButton8.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton8.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton8.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton8.setContentAreaFilled(false);
			jButton8.setOpaque(true);
			jButton8.setBackground(RhizoAddons.confidencColors.get((byte) 3));
			jPanel8.add(jButton8);

			Component[] c8 = { jSlider8, jButton8 };
			componentCollection_hash.put(3, c8);
			add(jPanel8);

			jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));

			jLabel9.setText("2");
			jLabel9.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel9.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel9.add(jLabel9);

			jSlider9.setName("9");
			jSlider9.setMinimum(0);
			jSlider9.setMaximum(255);
			jSlider9.setValue(255);
			jSlider9.addChangeListener(sliderAction);
			jPanel9.add(jSlider9);

			jCheckBox9.setSelected(checkBoxArray[2]);
			jCheckBox9.setActionCommand("2");
			jCheckBox9.addActionListener(clickablityAction);
			jPanel9.add(jCheckBox9);

			jButton9.setActionCommand("2");
			jButton9.addActionListener(colorChangeButton);
			jButton9.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton9.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton9.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton9.setContentAreaFilled(false);
			jButton9.setOpaque(true);
			jButton9.setBackground(RhizoAddons.confidencColors.get((byte) 2));
			jPanel9.add(jButton9);

			Component[] c9 = { jSlider9, jButton9 };
			componentCollection_hash.put(2, c9);
			add(jPanel9);

			jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.LINE_AXIS));

			jLabel10.setText("1");
			jLabel10.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel10.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel10.add(jLabel10);

			jSlider10.setName("10");
			jSlider10.setMinimum(0);
			jSlider10.setMaximum(255);
			jSlider10.setValue(255);
			jSlider10.addChangeListener(sliderAction);
			jPanel10.add(jSlider10);

			jCheckBox10.setSelected(checkBoxArray[1]);
			jCheckBox10.setActionCommand("1");
			jCheckBox10.addActionListener(clickablityAction);
			jPanel10.add(jCheckBox10);

			jButton10.setActionCommand("1");
			jButton10.addActionListener(colorChangeButton);
			jButton10.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton10.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton10.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton10.setContentAreaFilled(false);
			jButton10.setOpaque(true);
			jButton10.setBackground(RhizoAddons.confidencColors.get((byte) 1));
			jPanel10.add(jButton10);

			Component[] c10 = { jSlider10, jButton10 };
			componentCollection_hash.put(1, c10);
			add(jPanel10);

			jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.LINE_AXIS));

			jLabel11.setText("0");
			jLabel11.setMaximumSize(new java.awt.Dimension(12, 14));
			jLabel11.setPreferredSize(new java.awt.Dimension(12, 14));
			jPanel11.add(jLabel11);

			jSlider11.setName("11");
			jSlider11.setMinimum(0);
			jSlider11.setMaximum(255);
			jSlider11.setValue(255);
			jSlider11.addChangeListener(sliderAction);
			jPanel11.add(jSlider11);

			jCheckBox11.setSelected(checkBoxArray[0]);
			jCheckBox11.setActionCommand("0");
			jCheckBox11.addActionListener(clickablityAction);
			jPanel11.add(jCheckBox11);

			jButton11.setActionCommand("0");
			jButton11.addActionListener(colorChangeButton);
			jButton11.setMaximumSize(new java.awt.Dimension(33, 15));
			jButton11.setMinimumSize(new java.awt.Dimension(33, 15));
			jButton11.setPreferredSize(new java.awt.Dimension(33, 12));
			jButton11.setContentAreaFilled(false);
			jButton11.setOpaque(true);
			jButton11.setBackground(RhizoAddons.confidencColors.get((byte) 0));
			jPanel11.add(jButton11);

			Component[] c11 = { jSlider11, jButton11 };
			componentCollection_hash.put(0, c11);
			add(jPanel11);
		}
		


		// highlight color stuff
		jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.LINE_AXIS));

		jLabel12.setText("Highlighting color");
		jLabel12.setMaximumSize(new java.awt.Dimension(500, 100));
		jLabel12.setMinimumSize(new java.awt.Dimension(12, 14));
		jLabel12.setPreferredSize(new java.awt.Dimension(120, 14));
		jPanel12.add(jLabel12);

		jButton12.setActionCommand("11");
		jButton12.addActionListener(colorChangeButton);
		jButton12.setMaximumSize(new java.awt.Dimension(33, 15));
		jButton12.setMinimumSize(new java.awt.Dimension(33, 15));
		jButton12.setPreferredSize(new java.awt.Dimension(33, 12));
		jButton12.setContentAreaFilled(false);
		jButton12.setOpaque(true);
		jButton12.setBackground(RhizoAddons.confidencColors.get((byte) 11));
		jPanel12.add(jButton12);

		Component[] c12 = { null, jButton12 };
		componentCollection_hash.put(11, c12);
		add(jPanel12);
	}

	private javax.swing.Box.Filler filler3;
	private javax.swing.Box.Filler filler4;
	private javax.swing.Box.Filler filler5;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton10;
	private javax.swing.JButton jButton11;
	private javax.swing.JButton jButton12;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JButton jButton5;
	private javax.swing.JButton jButton6;
	private javax.swing.JButton jButton7;
	private javax.swing.JButton jButton8;
	private javax.swing.JButton jButton9;
	private javax.swing.JCheckBox jCheckBox1;
	private javax.swing.JCheckBox jCheckBox10;
	private javax.swing.JCheckBox jCheckBox11;
	private javax.swing.JCheckBox jCheckBox2;
	private javax.swing.JCheckBox jCheckBox3;
	private javax.swing.JCheckBox jCheckBox4;
	private javax.swing.JCheckBox jCheckBox5;
	private javax.swing.JCheckBox jCheckBox6;
	private javax.swing.JCheckBox jCheckBox7;
	private javax.swing.JCheckBox jCheckBox8;
	private javax.swing.JCheckBox jCheckBox9;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel11;
	private javax.swing.JLabel jLabel12;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel31;
	private javax.swing.JLabel jLabel32;
	private javax.swing.JLabel jLabel33;
	private javax.swing.JLabel jLabel34;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel11;
	private javax.swing.JPanel jPanel12;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JPanel jPanelNames;
	private javax.swing.JSlider jSlider1;
	private javax.swing.JSlider jSlider10;
	private javax.swing.JSlider jSlider11;
	private javax.swing.JSlider jSlider2;
	private javax.swing.JSlider jSlider3;
	private javax.swing.JSlider jSlider4;
	private javax.swing.JSlider jSlider5;
	private javax.swing.JSlider jSlider6;
	private javax.swing.JSlider jSlider7;
	private javax.swing.JSlider jSlider8;
	private javax.swing.JSlider jSlider9;
	// component hash
	private Hashtable<Integer, Component[]> componentCollection_hash = new Hashtable<Integer, Component[]>();
	private boolean[] checkBoxArray = RhizoAddons.treeLineClickable;

	// Color change button action
	Action colorChangeButton = new AbstractAction("colorChangeButton") {
		public void actionPerformed(ActionEvent e) {
			int state = Integer.parseInt(e.getActionCommand());
			javax.swing.JButton source = (JButton) e.getSource();
			javax.swing.JSlider cSlider = (JSlider) componentCollection_hash.get(state)[0];

			Color currentColor = JColorChooser.showDialog(source, "Choose color", Color.WHITE);
			if (currentColor != null) {
				int alpha = 255;
				if (cSlider != null) {
					alpha = cSlider.getValue();
				}
				int red = currentColor.getRed();
				int green = currentColor.getGreen();
				int blue = currentColor.getBlue();
				Color newColor = new Color(red, green, blue, alpha);

				RhizoAddons.confidencColors.put((byte) state, newColor);
				RhizoAddons.applyCorrespondingColor();
				source.setBackground(newColor);
			}
		}
	};

	// alpha change slider action
	ChangeListener sliderAction = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			int state = 10;
			javax.swing.JSlider currentSlider = (JSlider) e.getSource();

			switch (currentSlider.getName()) {
			case "1":
				state = 10;
				break;
			case "2":
				state = 9;
				break;
			case "3":
				state = 8;
				break;
			case "4":
				state = 7;
				break;
			case "5":
				state = 6;
				break;
			case "6":
				state = 5;
				break;
			case "7":
				state = 4;
				break;
			case "8":
				state = 3;
				break;
			case "9":
				state = 2;
				break;
			case "10":
				state = 1;
				break;
			case "11":
				state = 0;
				break;
			}

			// javax.swing.JButton cButton = (JButton)
			// componentCollection_hash.get(state)[1];
			Color currentColor = RhizoAddons.confidencColors.get((byte) state);
			int alpha = currentSlider.getValue();
			int red = currentColor.getRed();
			int green = currentColor.getGreen();
			int blue = currentColor.getBlue();
			Color newColor = new Color(red, green, blue, alpha);

			RhizoAddons.confidencColors.put((byte) state, newColor);
			RhizoAddons.applyCorrespondingColor();
			// cButton.setBackground(newColor);

		}
	};

	// clickablity change action
	Action clickablityAction = new AbstractAction("clickablityAction") {
		public void actionPerformed(ActionEvent e) {
			int state = Integer.parseInt(e.getActionCommand());
			javax.swing.JCheckBox source = (JCheckBox) e.getSource();

			RhizoAddons.treeLineClickable[state] = source.isSelected();
		}
	};
}
