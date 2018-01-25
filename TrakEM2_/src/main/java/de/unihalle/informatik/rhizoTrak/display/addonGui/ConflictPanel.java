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
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import de.unihalle.informatik.rhizoTrak.conflictManagement.Conflict;
import de.unihalle.informatik.rhizoTrak.conflictManagement.ConflictManager;
import de.unihalle.informatik.rhizoTrak.conflictManagement.ConnectorConflict;
import de.unihalle.informatik.rhizoTrak.conflictManagement.TreelineConflict;
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
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JList<String> jList1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.DefaultListModel<String> listModel;
	private HashMap<String,Conflict> dataTable= new HashMap<String,Conflict>();
	File[] files;

	public ConflictPanel() 
	{
		iniComponents();
	}

	private void iniComponents() 
	{

		listModel	=	new javax.swing.DefaultListModel<String>();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		jList1 = new javax.swing.JList<String>();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
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

		jPanel1.setLayout(new GridLayout(4, 1));

		jButton1.setText("Update");
		jButton1.setActionCommand("update");
		jButton1.addActionListener(this);
		jPanel1.add(jButton1);
		
		jButton4.setText("Solve");
		jButton4.setActionCommand("solve");
		jButton4.addActionListener(this);
		jPanel1.add(jButton4);

		jButton2.setText("Auto Resolve NA");
		jButton2.setActionCommand("autoResolveNonAggressiv");
		jButton2.addActionListener(this);
		jPanel1.add(jButton2);
		
		jButton3.setText("Auto Resolve A");
		jButton3.setActionCommand("autoResolveAggressiv");
		jButton3.addActionListener(this);
		jPanel1.add(jButton3);
		
		
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
			ConflictManager.autoResolveConnectorConnflicts(false);
			break;
			
		case "autoResolveAggressiv":
			ConflictManager.autoResolveConnectorConnflicts(true);
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
		ConflictManager.clearAndUpdate();
		
		listModel.removeAllElements();	
		ArrayList<ConnectorConflict> currentConnectorConflicts = new ArrayList<ConnectorConflict>(ConflictManager.getConnectorConflicts());
		for(ConnectorConflict conflict:currentConnectorConflicts)
		{
			conflict.update();
			listModel.addElement(conflict.toString());
			dataTable.put(conflict.toString(), conflict);
		}
		ArrayList<TreelineConflict> currentTreelineConflicts = new ArrayList<TreelineConflict>(ConflictManager.getTreelineConflicts());
		for(TreelineConflict conflict:currentTreelineConflicts)
		{
			conflict.update();
			listModel.addElement(conflict.toString());
			dataTable.put(conflict.toString(), conflict);
		}
	}
	
	private void solveButton()
	{	
		if(ConflictManager.abortCurrentSolving()) {return;}
		
		int selection = jList1.getSelectedIndex();
		
		if(selection>-1)
		{
			Utils.log("start solving");
			String selectedConflictString = listModel.getElementAt(selection);
			Conflict currentConflict = dataTable.get(selectedConflictString);
			//case one: Treeline conflict
			if(currentConflict.getClass().equals(TreelineConflict.class)){
	
				TreelineConflict conflict = (TreelineConflict)currentConflict;
				ConflictManager.setCurrentSolvingConflict(conflict);
				
				jButton4.setText("Solving ... abort?");
				jButton4.setBackground(new Color(255, 0, 0));

				//find the correct layer
				Layer layer = conflict.getTreeConKey().getLayer();
				Display.getFront().setLayer(layer);
				//find the interesting treelines and highlight
				List<Displayable> treelineList = new ArrayList<Displayable>(conflict.getTreelineOne());
				RhizoAddons.highlight(treelineList,false);
				

				Display.getFront().getFrame().toFront();
				
			}
			//case two: Connector conflict
		}
	}
	
	public void setSolved(){
		jButton4.setText("Solve");
		jButton4.setBackground(jButton3.getBackground());
	}
}
