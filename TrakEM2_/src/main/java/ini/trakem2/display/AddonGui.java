package ini.trakem2.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import ch.qos.logback.classic.pattern.Util;
import ij.ImagePlus;
import ini.trakem2.Project;
import ini.trakem2.conflictManagement.ConflictManager;
import ini.trakem2.conflictManagement.ConnectorConflict;
import ini.trakem2.persistence.Loader;
import ini.trakem2.tree.DNDTree;
import ini.trakem2.tree.ProjectThing;
import ini.trakem2.tree.ProjectTree;
import ini.trakem2.utils.Utils;

public class AddonGui
{
	public VisibilityPanel visibilityPanel()
	{
		VisibilityPanel visPanel = new VisibilityPanel();
		return visPanel;
	}

	public ImageImport imageImport()
	{
		ImageImport imageImportPanel = new ImageImport();
		return imageImportPanel;
	}
	
//	public ConflictPanel conflictManagerPanel()
//	{
//		ConflictPanel conflictPanel = new ConflictPanel();
//		return conflictPanel;
//	}
}

class VisibilityPanel extends javax.swing.JPanel 
{

	public VisibilityPanel() 
	{
		initComponents();
	}

	private void initComponents() 
	{

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

		jPanelNames.setLayout(new javax.swing.BoxLayout(jPanelNames, javax.swing.BoxLayout.LINE_AXIS));

		jLabel31.setText("state");
		jPanelNames.add(jLabel31);
		jPanelNames.add(filler3);

		jLabel32.setText("alpha");
		jPanelNames.add(jLabel32);
		jPanelNames.add(filler5);

		jLabel33.setText("clickable");
		jPanelNames.add(jLabel33);
		jPanelNames.add(filler4);

		jLabel34.setText("Color");
		jPanelNames.add(jLabel34);

		add(jPanelNames);

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
		

		
		//highlight color stuff
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
	Action colorChangeButton = new AbstractAction("colorChangeButton")
	{
		public void actionPerformed(ActionEvent e)
		{
			int state = Integer.parseInt(e.getActionCommand());
			javax.swing.JButton source = (JButton) e.getSource();
			javax.swing.JSlider cSlider = (JSlider) componentCollection_hash.get(state)[0];

			Color currentColor = JColorChooser.showDialog(source, "Choose color", Color.WHITE);
			if (currentColor != null)
			{
				int alpha=255;
				if(cSlider!=null)
				{
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
	ChangeListener sliderAction = new ChangeListener()
	{
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
	Action clickablityAction = new AbstractAction("clickablityAction") 
	{
		public void actionPerformed(ActionEvent e) 
		{
			int state = Integer.parseInt(e.getActionCommand());
			javax.swing.JCheckBox source = (JCheckBox) e.getSource();

			RhizoAddons.treeLineClickable[state] = source.isSelected();
		}
	};
}

class ImageImport extends javax.swing.JPanel 
{
	private javax.swing.Box.Filler filler1;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JList<String> jList1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.DefaultListModel<String> listModel;
	File[] files;

	public ImageImport() 
	{
		iniComponents();
	}

	private void iniComponents() 
	{
		//TODO check if there are any annotations on currentImage > if yes lock stack/layers
		//TODO check if names are duplicated > if yes ask user to rename
		
		listModel	=	new javax.swing.DefaultListModel<String>();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		jList1 = new javax.swing.JList<String>();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0),
				new java.awt.Dimension(0, 32767));
		
//		//get current loaded images
//		LayerSet layerSet	=	Display.getFront().getLayerSet();
//		List<Patch> patches = layerSet.getAll(Patch.class);
//		ImagePlus imagePlus = patches.get(0).getImagePlus();
//		String[] imageNames = imagePlus.getImageStack().getSliceLabels();
		
		setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
		//make listModel and load all images that are already in trackem project
//		for (String string : imageNames) {
//			if(string!=null){
//				listModel.addElement(string);	
//			}
//		}
		jList1.setModel(listModel);
		jList1.setTransferHandler(new ListTransferHandler());
		jList1.setDragEnabled(true);
		jList1.setDropMode(DropMode.INSERT);
		jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		jScrollPane1.setViewportView(jList1);

		add(jScrollPane1);

		jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

		jButton1.setText("Open images");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		jPanel1.add(jButton1);

		jButton2.setText("Import images");
		jButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});
		jPanel1.add(jButton2);
		
		jButton3.setText("Sort by date");
		jButton3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				sortList(evt);
			}
		});
		jPanel1.add(jButton3);
		
		jPanel1.add(filler1);
		
		add(jPanel1);
	}
	
	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt){
		//TODO add code to open images
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);

		Component frame = null;

		chooser.showOpenDialog(frame);
		files = chooser.getSelectedFiles();
		for (File file : files) {
			// file.getName();
			listModel.addElement(file.getName());		
		}
		jList1.setTransferHandler(new ListTransferHandler());
	}
	
	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt){
		//TODO add code to load images to stack
        //make files array consistent
        List<File> reord = new ArrayList<File>();
        int i=0;
        for (Object string : listModel.toArray()) {
        	for (File file : files) {
				if(file.getName().equals((String)string)){
					reord.add(file);
					Utils.log(string +":"+file.getName());
				}
			}
        	i++;
		}
        File[] reordArray = new File[reord.size()];
        reord.toArray(reordArray);
        files=reordArray;
        //adding appropriate number of layer and image
        RhizoAddons.addLayerAndImage(files);
        
	}
	
	private void sortList(ActionEvent e)
	{
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> currentList = new ArrayList<String>();
		
		for(Object string:listModel.toArray())
		{
			currentList.add((String) string);
		}
		
		while(!currentList.isEmpty()){
			
			int currentMinPosi = 0;
			String currentMinString = currentList.get(0);
			
			if(currentList.size()>1)
			{
				for(int i=1;i<currentList.size();i++)
				{
					if(getSortingNumber(currentMinString)>getSortingNumber(currentList.get(i)))
					{
						currentMinPosi=i;
						currentMinString=currentList.get(i);
					}
				}
			}
			
			result.add(currentMinString);
			
			currentList.remove(currentMinPosi);
			
		}
		
		listModel.clear();
		for (String string : result)
		{
			listModel.addElement(string);
		}
	}
	
	private int getSortingNumber(String string){
		int result =-1;
		if(string.split("_").length>5)
		{
			result = Integer.parseInt(string.split("_")[5]);
			Utils.log("currentSortNum: " + result);
		}
		return result;
	}
}

class ListTransferHandler extends TransferHandler
{
	private int selectedindex = -1;

	public boolean canImport(TransferHandler.TransferSupport support)
	{
		return support.isDataFlavorSupported(DataFlavor.stringFlavor);
	}

	protected Transferable createTransferable(JComponent comp)
	{
		JList<String> list = (JList<String>) comp;
		selectedindex = list.getSelectedIndex();
		String value = list.getSelectedValue();
		return new StringSelection(value);
	}

	public int getSourceActions(JComponent c)
	{
		return TransferHandler.MOVE;
	}

	public boolean importData(TransferHandler.TransferSupport support)
	{
		if (!support.isDrop())
		{
			return false;
		}
		JList<String> list = (JList<String>) support.getComponent();
		DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
		JList.DropLocation dl = list.getDropLocation();
		
		int index = dl.getIndex();
		
		// string that dropped
		Transferable t = support.getTransferable();
		String data;
		try
		{
			data = (String) t.getTransferData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		listModel.insertElementAt(data, index);
		if (index < selectedindex)
		{
			selectedindex++;
		}

		return true;
	}

	public void exportDone(JComponent comp, Transferable trans, int action)
	{
		JList list = (JList) comp;
		DefaultListModel listModel = (DefaultListModel) list.getModel();
		if (action == MOVE)
		{
			listModel.remove((selectedindex));
		}
	}
}

//class ConflictPanel extends javax.swing.JPanel implements ActionListener
//{
//	private javax.swing.Box.Filler filler1;
//	private javax.swing.JButton jButton1;
//	private javax.swing.JButton jButton2;
//	private javax.swing.JButton jButton3;
//	private javax.swing.JList<String> jList1;
//	private javax.swing.JPanel jPanel1;
//	private javax.swing.JScrollPane jScrollPane1;
//	private javax.swing.DefaultListModel<String> listModel;
//	File[] files;
//
//	public ConflictPanel() 
//	{
//		iniComponents();
//	}
//
//	private void iniComponents() 
//	{
//
//		listModel	=	new javax.swing.DefaultListModel<String>();
//		jScrollPane1 = new javax.swing.JScrollPane();
//		jPanel1 = new javax.swing.JPanel();
//		jList1 = new javax.swing.JList<String>();
//		jButton1 = new javax.swing.JButton();
//		jButton2 = new javax.swing.JButton();
//		jButton3 = new javax.swing.JButton();
//		filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0),
//				new java.awt.Dimension(0, 32767));
//		
//		
//		setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
//
//		jList1.setModel(listModel);
//		jList1.setTransferHandler(new ListTransferHandler());
//		jList1.setDragEnabled(true);
//		jList1.setDropMode(DropMode.INSERT);
//		jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//		jScrollPane1.setViewportView(jList1);
//
//		add(jScrollPane1);
//
//		jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));
//
//		jButton1.setText("update");
//		jButton1.setActionCommand("update");
//		jButton1.addActionListener(this);
//		jPanel1.add(jButton1);
//
//		jButton2.setText("cmd2");
//		jButton2.setActionCommand("cmd2");
//		jButton2.addActionListener(this);
//		jPanel1.add(jButton2);
//		
//		jButton3.setText("cmd3");
//		jButton3.setActionCommand("cmd3");
//		jButton3.addActionListener(this);
//		jPanel1.add(jButton3);
//		
//		jPanel1.add(filler1);
//		
//		add(jPanel1);
//		
//		updateListModel();
//		
//
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent evt) {
//		switch (evt.getActionCommand()) {
//		case "update":
//			Utils.log("Update button pressed");
//			ConflictManager.validateConflicts();
//			updateListModel();
//			break;
//
//		default:
//			break;
//		}
//		
//	}
//	
//	public void updateListModel()
//	{
//		listModel.removeAllElements();
//		for(ConnectorConflict conflict:ConflictManager.getOpenTwoConnectorConflicts())
//		{
//			listModel.addElement(conflict.toString());
//		}		
//	}
//	
//
//	
//}