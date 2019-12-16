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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.rowset.spi.SyncResolver;
import javax.swing.KeyStroke;

public class RhizoShortcutManager {

  private static RhizoShortcutManager instance = null;

  protected HashMap<RhizoCommand, RhizoCommandConfig> commandMap;

  public static enum RhizoCommand {
    GENERIC_DESELECT_ALL,
    GENERIC_DESELECT,
    GENERIC_LAYER_SCROLL_DOWN,
    GENERIC_LAYER_SCROLL_UP,
    GENERIC_REDO,
    GENERIC_SELECT_ALL_VISIBLE,
    GENERIC_SELECT_NODE,
    GENERIC_TOGGLE_NODE_TAGS_HIDE,
    GENERIC_TOGGLE_TREELINES_ARROWHEAD,
    GENERIC_TOGGLE_TREELINES_CURRENT_LAYER_HIDE,
    GENERIC_TOOL_CONNECTOR,
    GENERIC_TOOL_HAND,
    GENERIC_TOOL_LINE,
    GENERIC_TOOL_PEN,
    GENERIC_TOOL_PENCIL,
    GENERIC_TOOL_SELECT,
    GENERIC_TOOL_TEXT,
    GENERIC_TOOL_ZOOM,
    GENERIC_TREELINE_CREATE,
    GENERIC_UNDO,
    GENERIC_ZOOM_IN,
    GENERIC_ZOOM_OUT,
    DISPLAYABLE_ALL_UNHIDE,
    DISPLAYABLE_DESELECTED_HIDE_EXCEPT_IMAGES,
    DISPLAYABLE_DESELECTED_HIDE,
    DISPLAYABLE_GOTO_ZSPACE_BOTTOM,
    DISPLAYABLE_GOTO_ZSPACE_DOWN,
    DISPLAYABLE_GOTO_ZSPACE_TOP,
    DISPLAYABLE_GOTO_ZSPACE_UP,
    DISPLAYABLE_OPEN_ADJUST_CONTRAST,
    DISPLAYABLE_OPEN_MEASUREMENT_TABLE,
    DISPLAYABLE_SELECTED_DELETE,
    DISPLAYABLE_SELECTED_HIDE,
    DISPLAYABLE_TOGGLE_STATUS_LABEL_HIDE,
    IMAGES_WINDOW_DUPLICATE,
    NODE_ADD_NODE_ON_EDGE,
    NODE_ADJUST_DIAMETER_OF_SUBTREE,
    NODE_ADJUST_DIAMETER,
    NODE_CHANGE_STATUS_LABEL_OF_SUBTREE,
    NODE_CHANGE_STATUS_LABEL,
    NODE_DELETE_SUBTREE,
    NODE_DELETE,
    NODE_GOTO_BASE_NODE,
    NODE_GOTO_LAST_ADDED_NODE,
    NODE_GOTO_LAST_EDITED_NODE,
    NODE_GOTO_NEXT_NODE,
    NODE_GOTO_PREVIOUS_NODE,
    NODE_OPEN_DIAMETER_ADJUSTMENT,
    NODE_PROPAGATE_RADII_TO_NEXT_LAYER,
    NODE_TAG_ADD,
    NODE_TAG_REMOVE,
    NODE_TREELINE_REROOT,
    FULLGUI_IMAGE_IMPORT_NEXT,
    FULLGUI_IMAGE_IMPORT,
    FULLGUI_OPEN_COLOR_ADJUSTMENT,
    FULLGUI_SEARCH,
    FULLGUI_TRANSFORM_APPLY,
    FULLGUI_TRANSFORM_CANCEL,
    FULLGUI_TRANSFORM_ENTER_NODE_AFFINE,
    FULLGUI_TRANSFORM_ENTER_NODE_NONLINEAR
  }

  private static synchronized RhizoShortcutManager getInstance() {
    if (instance == null) {
      instance = new RhizoShortcutManager();
    }
    return instance;
  }

  private RhizoShortcutManager() {
    this.commandMap = new HashMap<>();
    this.commandMap.put(RhizoCommand.GENERIC_DESELECT_ALL,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
        "Deselect all"));
//    this.commandMap.put(RhizoCommand.GENERIC_DESELECT, Shift-Click);
    this.commandMap.put(RhizoCommand.GENERIC_LAYER_SCROLL_DOWN,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0, true),
        "Scroll down to next layer"));
    this.commandMap.put(RhizoCommand.GENERIC_LAYER_SCROLL_UP,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_COLON, 0, true),
        "Scroll up to next layer"));
    this.commandMap.put(RhizoCommand.GENERIC_REDO,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_DOWN_MASK, true),
        "Redo"));
    this.commandMap.put(RhizoCommand.GENERIC_SELECT_ALL_VISIBLE, 
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK, true),
        "Select all visible"));
    this.commandMap.put(RhizoCommand.GENERIC_SELECT_NODE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0, true),
        "Select node"));
    this.commandMap.put(RhizoCommand.GENERIC_TOGGLE_NODE_TAGS_HIDE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, true),
        "Hide/unhide node tags"));
    this.commandMap.put(RhizoCommand.GENERIC_TOGGLE_TREELINES_ARROWHEAD,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK, true),
        "Hide/unhide treeline arrowheads"));
    this.commandMap.put(RhizoCommand.GENERIC_TOGGLE_TREELINES_CURRENT_LAYER_HIDE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0, true),
        "Hide/unhide all treelines in the current layer"));
    this.commandMap.put(RhizoCommand.GENERIC_TOOL_CONNECTOR,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0, true),
        "Connector tool"));
    this.commandMap.put(RhizoCommand.GENERIC_TOOL_HAND,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0, true),
        "Hand tool"));
    this.commandMap.put(RhizoCommand.GENERIC_TOOL_LINE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, true),
        "Line tool"));
    this.commandMap.put(RhizoCommand.GENERIC_TOOL_PEN,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0, true),
        "Pen tool"));
    this.commandMap.put(RhizoCommand.GENERIC_TOOL_PENCIL,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0, true),
        "Pencil tool"));
    this.commandMap.put(RhizoCommand.GENERIC_TOOL_SELECT,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, true),
        "Select tool"));
    this.commandMap.put(RhizoCommand.GENERIC_TOOL_TEXT,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, true),
        "Text tool"));
    this.commandMap.put(RhizoCommand.GENERIC_TOOL_ZOOM,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, true),
        "Zoom tool"));
    this.commandMap.put(RhizoCommand.GENERIC_TREELINE_CREATE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true),
        "Create new treeline"));
    this.commandMap.put(RhizoCommand.GENERIC_UNDO,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK, true),
        "Undo"));
    this.commandMap.put(RhizoCommand.GENERIC_ZOOM_IN,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0, true),
        "Zoom in"));
    this.commandMap.put(RhizoCommand.GENERIC_ZOOM_OUT,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0, true),
        "Zoom out"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_ALL_UNHIDE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK, true),
        "Unhide all"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_DESELECTED_HIDE_EXCEPT_IMAGES,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, true),
        "Hide deselected except images"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_DESELECTED_HIDE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.SHIFT_DOWN_MASK, true),
        "Hide deselected"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_GOTO_ZSPACE_BOTTOM,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, true),
        "Move to bottom in Z space or patches panel"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_GOTO_ZSPACE_DOWN,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0, true),
        "Move down in Z space or patches panel"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_GOTO_ZSPACE_TOP,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, true),
        "Move to top in Z space or patches panel"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_GOTO_ZSPACE_UP,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK, true),
        "Move up in Z space or patches panel"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_OPEN_ADJUST_CONTRAST,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0, true),
        "Open adjust contrast"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_OPEN_MEASUREMENT_TABLE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0, true),
        "Open measurement table"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_SELECTED_DELETE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true),
        "Delete selected"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_SELECTED_HIDE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0, true),
        "Hide selected"));
    this.commandMap.put(RhizoCommand.DISPLAYABLE_TOGGLE_STATUS_LABEL_HIDE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0, true),
        "Hide/unhide status labels"));
    this.commandMap.put(RhizoCommand.IMAGES_WINDOW_DUPLICATE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK, true),
        "Open duplicate in Fiji window"));
//    this.commandMap.put(NODE_ADD_NODE_ON_EDGE, Shift+Click);
//    this.commandMap.put(NODE_ADJUST_DIAMETER_OF_SUBTREE, Shift + Alt + r + SW)
//    this.commandMap.put(NODE_ADJUST_DIAMETER, Shift + Alt + SW);
//    this.commandMap.put(NODE_CHANGE_STATUS_LABEL_OF_SUBTREE, Shift + Ctrl + Alt + SW)
//    this.commandMap.put(NODE_CHANGE_STATUS_LABEL, Shift + SW)
//    this.commandMap.put(NODE_DELETE_SUBTREE, Shift + Ctrl + Alt + Click)
//    this.commandMap.put(NODE_DELETE, Shift + Ctrl + Click)
    this.commandMap.put(RhizoCommand.NODE_GOTO_BASE_NODE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, true),
        "Go to base node"));
    this.commandMap.put(RhizoCommand.NODE_GOTO_LAST_ADDED_NODE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0, true),
        "Go to last added node"));
    this.commandMap.put(RhizoCommand.NODE_GOTO_LAST_EDITED_NODE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, true),
        "Go to last edited node"));
    this.commandMap.put(RhizoCommand.NODE_GOTO_NEXT_NODE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0, true),
        "Go to next branch node or end"));
    this.commandMap.put(RhizoCommand.NODE_GOTO_PREVIOUS_NODE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0, true),
        "Go to previous branch node or start"));
    this.commandMap.put(RhizoCommand.NODE_OPEN_DIAMETER_ADJUSTMENT,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0, true),
        "Open diameter adjustment"));
    this.commandMap.put(RhizoCommand.NODE_PROPAGATE_RADII_TO_NEXT_LAYER,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, true),
        "Propagate node radii of treeline to connected treeline in next layer"));
    this.commandMap.put(RhizoCommand.NODE_TAG_ADD,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0, true),
        "Add tag to node"));
    this.commandMap.put(RhizoCommand.NODE_TAG_REMOVE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.SHIFT_DOWN_MASK, true),
        "Remove tag from node"));
    this.commandMap.put(RhizoCommand.NODE_TREELINE_REROOT,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true),
        "Reroot the treeline"));
    this.commandMap.put(RhizoCommand.FULLGUI_IMAGE_IMPORT_NEXT,
      new RhizoCommandConfig(  KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK, true),
        "Import next image in image directory into current layer"));
    this.commandMap.put(RhizoCommand.FULLGUI_IMAGE_IMPORT,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, true),
        "Import image into current layer"));
    this.commandMap.put(RhizoCommand.FULLGUI_OPEN_COLOR_ADJUSTMENT,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK, true),
        "Open color adjustment"));
    this.commandMap.put(RhizoCommand.FULLGUI_SEARCH,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, true),
        "Search"));
    this.commandMap.put(RhizoCommand.FULLGUI_TRANSFORM_APPLY,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
        "Apply transformation (in transform mode)"));
    this.commandMap.put(RhizoCommand.FULLGUI_TRANSFORM_CANCEL,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
        "Cancel transformation (in transform mode)"));
    this.commandMap.put(RhizoCommand.FULLGUI_TRANSFORM_ENTER_NODE_AFFINE,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0, true),
        "Enter affine transform mode"));
    this.commandMap.put(RhizoCommand.FULLGUI_TRANSFORM_ENTER_NODE_NONLINEAR,
      new RhizoCommandConfig(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.SHIFT_DOWN_MASK, true),
        "Enter non-linear transform mode"));
  }

  public static synchronized KeyStroke getShortcut(RhizoCommand rc) {
    return getInstance().commandMap.get(rc).getStroke();    
  }

  public static synchronized void setShortcut(RhizoCommand rc, KeyStroke ks){
    RhizoShortcutManager rsm = getInstance();
    RhizoCommandConfig rcc =  rsm.commandMap.get(rc);
    rcc.setStroke(ks);
  }

  public static synchronized String getDescriptorString(RhizoCommand rc) {
    return getInstance().commandMap.get(rc).getDescription();    
  }

  public static boolean doesCommandMatchEvent(RhizoCommand rc, KeyEvent ke) {
    KeyStroke ks = KeyStroke.getKeyStroke(ke.getKeyCode(), ke.getModifiers(), true);
    return doesCommandMatchStroke(rc, ks);
  }

  public static boolean doesCommandMatchKeys(RhizoCommand rc, int key, int modifiers, boolean onKeyRelease) {
    KeyStroke ks = KeyStroke.getKeyStroke(key, modifiers, onKeyRelease);
    return doesCommandMatchStroke(rc, ks);
  }

  public static synchronized boolean doesCommandMatchStroke(RhizoCommand rc, KeyStroke ks) {
    if (getInstance().commandMap.get(rc) == null)
      return false;
    return getInstance().commandMap.get(rc).getStroke().equals(ks);
  }

  public static synchronized ArrayList<RhizoCommand> getCommands(){
    return new ArrayList<RhizoCommand>(getInstance().commandMap.keySet());
  }

  private class RhizoCommandConfig {

    private KeyStroke ks;

    private String description;

    public RhizoCommandConfig(KeyStroke k, String d) {
      this.ks = k;
      this.description = d;
    }

    public KeyStroke getStroke() {
      return this.ks;
    }

    public void setStroke(KeyStroke ks){
      this.ks = ks;
    }

    public String getDescription() {
      return this.description;
    }

  }

}	