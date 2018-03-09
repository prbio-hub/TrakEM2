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

import de.unihalle.informatik.rhizoTrak.conflictManagement.ConflictManager;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;

public class SplitDialog extends JDialog implements ActionListener {

	private javax.swing.JPanel jPanel0;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	
	private javax.swing.JLabel jLabel1;
	
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	
	
	private ArrayList<Treeline> trees;
        
        private RhizoAddons rhizoAddons;
	
	public SplitDialog(ArrayList<Treeline> trees,RhizoAddons rhizoAddons)
	{
                this.rhizoAddons = rhizoAddons;
		this.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
		this.trees = trees;
		
		this.setLocationByPlatform(true);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		jPanel0 = new JPanel();
		jPanel0.setLayout(new javax.swing.BoxLayout(jPanel0, javax.swing.BoxLayout.PAGE_AXIS));
				
		jPanel1 = new JPanel();
		jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));
		
		jPanel1.add(new javax.swing.Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(30000, 30000)));
		
		jLabel1 = new JLabel("Which root should get a new connector?");
		jPanel1.add(jLabel1);
		
		jPanel1.add(new javax.swing.Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(30000, 30000)));
		
		jPanel1.setBorder(new EmptyBorder(20, 10, 10, 20));
		
		
		jPanel2 = new JPanel();
		jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
		
		jPanel2.add(new javax.swing.Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(30000, 30000)));
		
		jButton1 = new JButton("upstream root");
		jButton1.setActionCommand("1");
		jButton1.addActionListener(this);
		jPanel2.add(jButton1);
		
		jPanel2.add(new javax.swing.Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(30000, 30000)));
		
		jButton2 = new JButton("downstream root");
		jButton2.setActionCommand("2");
		jButton2.addActionListener(this);
		jPanel2.add(jButton2);
		
		jPanel2.add(new javax.swing.Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(30000, 30000)));
		
		jPanel2.setBorder(new EmptyBorder(20, 10, 10, 20));

		
		
		
		jPanel0.add(jPanel1);
		jPanel0.add(jPanel2);
		
		
		this.add(jPanel0);
		//this.add(jPanel1);
		//this.add(jPanel2);
		
		this.setSize(new Dimension(350, 180));
		
		this.toFront();
		this.setVisible(true);
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		if(evt.getActionCommand().equals("1"))
		{
			//connect current connector to the downstream root
			RhizoAddons.transferConnector(trees.get(0), trees.get(1));
			//new connector for upstream root
			RhizoAddons.giveNewConnector(trees.get(0),null);
			rhizoAddons.splitDialog = false;
		}
		
		
		if(evt.getActionCommand().equals("2"))
		{
			//new connector for downstream root
			RhizoAddons.giveNewConnector(trees.get(1),trees.get(0));
			rhizoAddons.splitDialog = false;
		}
                this.dispose();
	}
	

}
