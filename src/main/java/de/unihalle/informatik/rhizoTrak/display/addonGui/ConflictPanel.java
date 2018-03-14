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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import de.unihalle.informatik.rhizoTrak.addon.RhizoColVis;
import de.unihalle.informatik.rhizoTrak.conflictManagement.Conflict;
import de.unihalle.informatik.rhizoTrak.conflictManagement.ConflictManager;
import de.unihalle.informatik.rhizoTrak.conflictManagement.ConnectorConflict;
import de.unihalle.informatik.rhizoTrak.conflictManagement.TreelineConflict;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.IJ;

public class ConflictPanel extends JPanel implements ActionListener {

	private javax.swing.Box.Filler filler1;
	private javax.swing.JButton jButton1;
//	private javax.swing.JButton jButton2;
//	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JList<String> jList1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.DefaultListModel<String> listModel;
	private HashMap<String,Conflict> dataTable= new HashMap<String,Conflict>();
	public File[] files;
        public ConflictManager conflictManager = null;

	public ConflictPanel(ConflictManager conflictManager) 
	{
                this.conflictManager = conflictManager;
		iniComponents();
	}

	private void iniComponents() 
	{

		listModel	=	new javax.swing.DefaultListModel<String>();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		jList1 = new javax.swing.JList<String>();
		jButton1 = new javax.swing.JButton();
//		jButton2 = new javax.swing.JButton();
//		jButton3 = new javax.swing.JButton();
		jButton4 = new javax.swing.JButton();
		filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0),
				new java.awt.Dimension(0, 32767));
		
		
		setLayout(new BorderLayout());

		jList1.setModel(listModel);
		//jList1.setTransferHandler(new ListTransferHandler());
		//jList1.setDragEnabled(true);
		//jList1.setDropMode(DropMode.INSERT);
		jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		jScrollPane1.setViewportView(jList1);

		add(jScrollPane1);

		jPanel1.setLayout(new GridLayout(2, 1));

		jButton1.setText("Update");
		jButton1.setActionCommand("update");
		jButton1.addActionListener(this);
		jPanel1.add(jButton1);
		
		jButton4.setText("Solve");
		jButton4.setActionCommand("solve");
		jButton4.addActionListener(this);
		jPanel1.add(jButton4);

//		jButton2.setText("Auto Resolve NA");
//		jButton2.setActionCommand("autoResolveNonAggressiv");
//		jButton2.addActionListener(this);
//		jPanel1.add(jButton2);
//		
//		jButton3.setText("Auto Resolve A");
//		jButton3.setActionCommand("autoResolveAggressiv");
//		jButton3.addActionListener(this);
//		jPanel1.add(jButton3);
		
		
//		jPanel1.add(filler1);
		
		add(jPanel1, BorderLayout.EAST);
		
		updateList();
		
		//this.setBounds(new Rectangle(30, 30, 30, 100));
		//this.setVisible(true);
		

	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		switch (evt.getActionCommand()) {
		case "update":
			Utils.log("Update button pressed");
			updateList();
			break;
		
		case "autoResolveNonAggressiv":
			conflictManager.autoResolveConnectorConnflicts(false);
			break;
			
		case "autoResolveAggressiv":
			conflictManager.autoResolveConnectorConnflicts(true);
			break;
			
		case "solve":
			solveButton();
			break;

		default:
			break;
		}
		
	}
	
	public void updateList()
	{
		conflictManager.clearAndUpdate();
		
		listModel.removeAllElements();	
		ArrayList<ConnectorConflict> currentConnectorConflicts = new ArrayList<ConnectorConflict>(conflictManager.getConnectorConflicts());
		for(ConnectorConflict conflict:currentConnectorConflicts)
		{
			conflict.update();
			listModel.addElement(conflict.toString());
			dataTable.put(conflict.toString(), conflict);
		}
		ArrayList<TreelineConflict> currentTreelineConflicts = new ArrayList<TreelineConflict>(conflictManager.getTreelineConflicts());
		for(TreelineConflict conflict:currentTreelineConflicts)
		{
			conflict.update();
			listModel.addElement(conflict.toString());
			dataTable.put(conflict.toString(), conflict);
		}
	}
	
	private void solveButton()
	{	
		if(conflictManager.abortCurrentSolving()) {return;}
		
		int selection = jList1.getSelectedIndex();
		
		if(selection>-1)
		{
			Utils.log("start solving");
			String selectedConflictString = listModel.getElementAt(selection);
			Conflict currentConflict = dataTable.get(selectedConflictString);
			//case one: Treeline conflict
			if(currentConflict.getClass().equals(TreelineConflict.class)){
                Utils.log("currently solving a treelineConflict");
				TreelineConflict conflict = (TreelineConflict)currentConflict;
				conflictManager.setCurrentSolvingConflict(conflict);
				
				jButton4.setText("Solving ... abort?");
				jButton4.setBackground(new Color(255, 0, 0));

				//find the correct layer
				Layer layer = conflict.getTreeConKey().getLayer();
				Display.getFront().setLayer(layer);
				//find the interesting treelines and highlight
				List<Displayable> treelineList = new ArrayList<Displayable>(conflict.getTreelineOne());
				RhizoColVis.highlight(treelineList,false);
				
				Display.getFront().getFrame().toFront();
				
			}
			//case two: Connector conflict
			if(currentConflict.getClass().equals(ConnectorConflict.class)){
                Utils.log("currently solving a connectorConflict");
				ConnectorConflict conflict = (ConnectorConflict)currentConflict;
				conflictManager.setCurrentSolvingConflict(conflict);
				
				jButton4.setText("Solving ... abort?");
				jButton4.setBackground(new Color(255, 0, 0));

				ArrayList<Connector> connectorList = conflict.getConnectorList();
				int goAhead = conflictManager.userInteraction(new HashSet<Connector>(connectorList),true);
				if(goAhead==1) 
				{
					conflictManager.resolve(new HashSet<Connector>(connectorList));
				}
				if(goAhead==0)
				{
					conflictManager.abortCurrentSolving();
				}
				
			}
		}
	}
	
	public void setSolved(){
		jButton4.setText("Solve");
		jButton4.setBackground(jButton1.getBackground());
	}
        
        public void clearList(){
            listModel.clear();
        }
}
