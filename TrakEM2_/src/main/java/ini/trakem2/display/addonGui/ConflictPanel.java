package ini.trakem2.display.addonGui;

import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import ini.trakem2.conflictManagement.ConflictManager;
import ini.trakem2.conflictManagement.ConflictManager;
import ini.trakem2.conflictManagement.ConnectorConflict;
import ini.trakem2.conflictManagement.TreelineConflict;
import ini.trakem2.utils.Utils;

public class ConflictPanel extends JPanel implements ActionListener {

	private javax.swing.Box.Filler filler1;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JList<String> jList1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.DefaultListModel<String> listModel;
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
		filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0),
				new java.awt.Dimension(0, 32767));
		
		
		setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

		jList1.setModel(listModel);
		//jList1.setTransferHandler(new ListTransferHandler());
		//jList1.setDragEnabled(true);
		//jList1.setDropMode(DropMode.INSERT);
		//jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		jScrollPane1.setViewportView(jList1);

		add(jScrollPane1);

		jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

		jButton1.setText("update");
		jButton1.setActionCommand("update");
		jButton1.addActionListener(this);
		jPanel1.add(jButton1);

		jButton2.setText("cmd2");
		jButton2.setActionCommand("cmd2");
		jButton2.addActionListener(this);
		jPanel1.add(jButton2);
		
		jButton3.setText("cmd3");
		jButton3.setActionCommand("cmd3");
		jButton3.addActionListener(this);
		jPanel1.add(jButton3);
		
		jPanel1.add(filler1);
		
		add(jPanel1);
		
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

		default:
			break;
		}
		
	}
	
	public void updateList()
	{
		listModel.removeAllElements();
		for(ConnectorConflict conflict:ConflictManager.getConnectorConflicts())
		{
			listModel.addElement(conflict.toString());
		}
		for(TreelineConflict conflict:ConflictManager.getTreelineConflicts())
		{
			listModel.addElement(conflict.toString());
		}
	}
	

}
