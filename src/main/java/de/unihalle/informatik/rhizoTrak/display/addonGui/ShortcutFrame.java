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
 *  https://prbio-hub.github.io/rhizoTrak
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
 *    https://prbio-hub.github.io/rhizoTrak
 *
 */

package de.unihalle.informatik.rhizoTrak.display.addonGui;

import java.awt.Dialog.ModalityType;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;

import de.unihalle.informatik.rhizoTrak.addon.RhizoMain;
import de.unihalle.informatik.rhizoTrak.display.addonGui.RhizoShortcutManager.RhizoCommand;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

@SuppressWarnings("serial")
public class ShortcutFrame extends JFrame {

    private static ShortcutFrame instance = null;

    private int row=0;
    private int col=0;
    private RhizoMain rhizoMain = null;
    JTable table=null;
    DefaultTableModel tableModel=null;
    KeyEvent last_key_event = null;
    ArrayList<RhizoCommand> command_list;

    public static synchronized ShortcutFrame getInstance() {
        if (instance == null) {
            instance = new ShortcutFrame();
        }
        return instance;
    }

    private ShortcutFrame() {
        this.setSize(420, 480);
        makeTable();
        this.setVisible(true);
    }

    public static void setActivePair(int row, int col){
        getInstance().row = row;
        getInstance().col = col;
    }

    public static void changeShortcut(){
        KeyEvent e = getInstance().last_key_event;
        KeyStroke current_key_stroke = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers(),true);
        RhizoCommand current_command = getInstance().command_list.get(getInstance().row);
        Utils.log("new short cut "+ current_key_stroke.toString());
        if(ShortcutFrame.is_duplicated(current_key_stroke,current_command)){
            //duplicates are forbidden
            Utils.log("is duplicate");
            Utils.showMessage("shortcut already assigned");
        } else {
            Utils.log("is not duplicate");
            String mod = KeyEvent.getKeyModifiersText(current_key_stroke.getModifiers());
            String key = KeyEvent.getKeyText(e.getKeyCode());
            getInstance().tableModel.setValueAt(mod + " " + key, getInstance().row, getInstance().col);
            if(getInstance().rhizoMain!=null){
                getInstance().rhizoMain.getProjectConfig().setUserSettingsChanged();
            }
            RhizoShortcutManager.setShortcut(current_command, current_key_stroke);
        }
        
    }
    
    private static boolean keyPressedIsModifier(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_SHIFT ||
            e.getKeyCode() == KeyEvent.VK_ALT ||
            e.getKeyCode() == KeyEvent.VK_CONTROL || 
            e.getKeyCode() == KeyEvent.VK_ALT_GRAPH) return true;
        
        return false;
    }

    private static boolean is_duplicated(KeyStroke input,RhizoCommand currentCommand){
        RhizoCommand input_command = RhizoShortcutManager.getRhizoCommandFromShortcut(input);
        if(input_command!=null && input_command!=currentCommand){
            return true;
        }
        return false;
    }

    public static  void addKeys(KeyEvent e){
        //this.pressed_Keys.add(e);
        getInstance().last_key_event = e;
    }

    private void makeTable() {

        this.table = new JTable(){
            public boolean isCellEditable(int r, int c){
                return false;
            }
        };
        this.tableModel = new DefaultTableModel();

        
        this.command_list = RhizoShortcutManager.getCommands();
        Collections.sort(command_list);
        String[] commands = new String[command_list.size()];
        String[] key_strokes = new String[command_list.size()];
        for(int i=0;i<command_list.size();i++){
            commands[i] = RhizoShortcutManager.getDescriptorString(command_list.get(i));
            KeyStroke kStroke = RhizoShortcutManager.getShortcut(command_list.get(i));
            String mod_string = KeyEvent.getKeyModifiersText(kStroke.getModifiers());
            String character_string = KeyEvent.getKeyText(kStroke.getKeyCode());
            key_strokes[i] = mod_string +" "+ character_string;
        }

        this.tableModel.addColumn("Command", commands);
        this.tableModel.addColumn("shortcut", key_strokes);

        

        KeyListener key_listener = new KeyListener() {

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                // nothing todo

            }

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                    if(!keyPressedIsModifier(e)) ShortcutFrame.addKeys(e);
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if(!keyPressedIsModifier(e)){
                    ShortcutFrame.changeShortcut();
                    JDialog dialog = (JDialog) e.getSource();
                    dialog.setVisible(false); 
                }
            }

        };


        table.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable table = (JTable) e.getSource();
                Point point = e.getPoint();
                int row = table.rowAtPoint(point);
                int col = table.columnAtPoint(point);
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1 && table.getSelectedColumn() >0 && table.getSelectedColumn() != -1) {
                    ShortcutFrame.setActivePair(row, col);
                    JDialog key_press_dialog = new JDialog(ShortcutFrame.getInstance(), "Please press any key ...");
                    key_press_dialog.setSize(200, 100);
                    key_press_dialog.setModalityType(ModalityType.APPLICATION_MODAL);
                    key_press_dialog.addKeyListener(key_listener);
                    key_press_dialog.setLocationRelativeTo(ShortcutFrame.getInstance());
                    key_press_dialog.setVisible(true);
                    
                }
            }
        });

        table.setModel(this.tableModel);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        this.add(scrollPane);
    }

    class KeyPressDialog extends JDialog {
        
        int row=0;
        int col=0;

        public KeyPressDialog(int row, int col){
            this.row=row;
            this.col=col;
        }

        
    }

    public RhizoMain getRhizoMain() {
        return rhizoMain;
    }

    public void setRhizoMain(RhizoMain rhizoMain) {
        this.rhizoMain = rhizoMain;
    }
  
    
    
}

