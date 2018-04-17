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

package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

/** Hold all project specific settings, including some general and fixed ones.
 * Most of these will be imported/exported to project specific files.
 * <p>
 * Includes
 * <ul>
 * <li> mapping from status integer values to status labels (user defined and fixed)
 * including the definition, color etc
 * <li>highlight colors
 * </ul>
 * <p>
 * Status integer values for user defined status is a contiguous range of non negative  values starting with 0.
 * Multiple integer status value may be mapped to the same status label.
 * Fixed status labels are mapped via negative integers which need not be contiguous
 * 
 * @author posch
 *
 */
public class RhizoProjectConfig {
	
	/**
	 * to represent a non-negative integer status value with has (currently) no label name
	 * associated in the <code>statusLabelList</code>
	 */
	public static final int STATUS_UNDEFINED = -1;
	
	/**
	 * The name for <code><STATUS_UNDEFINE/code>
	 */
	
	public static final String NAME_UNDEFINED = "UNDEFINED";
	/**
	 * to represent /virtual) segments created on import from RSML to connect all
	 * branches (polylines) of a root to form one (connected) treeline
	 * 
	 */
	public static final int STATUS_VIRTUAL = -2;
	
	/**
	 * The name for <code>STATUS_VIRTUAL</code>
	 */
	public static final String NAME_VIRTUAL= "VIRTUAL";
	
	/**
	 * segments of a connector treeline
	 */
	public static final int STATUS_CONNECTOR = -3;
	
	/**
	 * The name for <code>STATUS_CONNECTOR</code>
	 */
	public static final String NAME_CONNECTOR= "CONNECTOR";
	
	
	public static final Color DEFAULT_STATUS_COLOR = new Color( 255, 255, 0);
	public static final Color DEFAULT_FIXED_STATUS_COLOR = new Color( 0, 255, 255);

	public static final int DEFAULT_ALPHA = 255;
	public static final boolean DEFAULT_SELECTABLE = true;
	/**
	 * map fixed status integer values used for internal purpose to their names
	 */
	private final HashMap<Integer,String> fixedStatusLabelMap = new HashMap<Integer, String>();

	/** definitions of user defines status labels
	 * does only hold the names, a status label may be contained multiple times, order cares
	 */
	private ArrayList<RhizoStatusLabel> statusLabelList = new ArrayList<RhizoStatusLabel>();
	
	/**
	 * Holds all fixed and user defined status labels.
	 */
	private HashMap<String,RhizoStatusLabel> statusLabelSet = new HashMap<String,RhizoStatusLabel>();
	
	/**
	 * This status label is returned (instead of null) in case a request with invalid integer status value
	 * or name is issued
	 */
	public final  RhizoStatusLabel INVALID_STATUS_LABEL = new RhizoStatusLabel( this, "INVALID_STATUS_LABEL", "?", Color.BLACK);
	
	// highlight colors
	private Color highlightColor1 = Color.MAGENTA;
	private Color highlightColor2 = Color.PINK;
	
	/**
	 * true if anything in the user setting part of instance has change, e.g. after last save operation
	 */
	private boolean userSettingsChanged = false;

	/**
	 * Director where to search new images in
	 */
	private File imageSearchDir = null;
	
	/**
	 * The color of the receiver node, i.e. the active node
	 */
	private Color receiverNodeColor = null;
	
	/**
	 * If true the user will be asked before merging treelines
	 */
	private boolean askMergeTreelines = true;
	
	/**
	 * If true the user will be asked before splitting a treeline
	 */
	private boolean askSplitTreeline = true;
	
	
	/**
	 * If true the image calibration information is shown to the user when writing statistics with non-pixel units
	 */
	private boolean showCalibrationInfo = true;
	
	public RhizoProjectConfig() {
		// we always need the fixed status labels
		statusLabelSet.put( NAME_UNDEFINED, new RhizoStatusLabel( this, NAME_UNDEFINED, "*", DEFAULT_FIXED_STATUS_COLOR));
		statusLabelSet.put( NAME_VIRTUAL, new RhizoStatusLabel( this, NAME_VIRTUAL, "-", DEFAULT_FIXED_STATUS_COLOR));
		statusLabelSet.put( NAME_CONNECTOR, new RhizoStatusLabel( this, NAME_CONNECTOR, "@", DEFAULT_FIXED_STATUS_COLOR));
		fixedStatusLabelMap.put( STATUS_UNDEFINED, NAME_UNDEFINED);
		fixedStatusLabelMap.put( STATUS_VIRTUAL, NAME_VIRTUAL);
		fixedStatusLabelMap.put( STATUS_CONNECTOR, NAME_CONNECTOR);	
		setReceiverNodeColor( Node.getReceiverColor());	
	}
	
	/** Append status label as the last label to the list.
	 * If a corresponding status label does not exist yet if will be created with the default color.
	 * If it already did exist the abbreviation will be replace which has effect also
	 * to potential further occurrences of this status label 
	 * 
	 * @param sl
	 */
	public void appendStatusLabelToList( RhizoStatusLabel statusLabel) {
		// mind: abbreviations are not part of user settings, so do not set hasChanged
		
		statusLabelList.add( statusLabel);
	}

	/**
	 * remove the last label from the list
	 */
	public void popStatusLabelFromList() {
		if ( statusLabelList.size() > 0) {
			statusLabelList.remove( statusLabelList.size()-1);
		}
	}
	
	/** Replaces the i-th entry with the given status label
	 * @param i
	 * @param statusLabel
	 * @return true on success, false if index out of range
	 */
	public boolean replaceStatusLabelList( int i, RhizoStatusLabel statusLabel) {
		if ( i >= 0 && i < statusLabelList.size() ) {
			statusLabelList.set( i, statusLabel);
			return true;
		} else {
			Utils.log( "rhizotrak", "PRhizoProjectConfig.replaceStatusLabelList index " + i + " out of bounds");
			return false;
		}
	}
	
	/** Add the status label to the set. If one with the same name already exists
	 * the abbreviation, color, alpha, and selectable will be replaced.
	 * @param sl
	 */
	public RhizoStatusLabel addStatusLabelToSet( RhizoStatusLabel sl) {	
		RhizoStatusLabel oldsl = this.statusLabelSet.get(  sl.getName());
		if ( oldsl == null) {
			statusLabelSet.put( sl.getName(), sl);
			this.userSettingsChanged = true;
			return sl;
		} else {
			oldsl.setAbbrev(  sl.getAbbrev());
			oldsl.setColor(  sl.getColor());
			oldsl.setAlpha( sl.getAlpha());
			oldsl.setSelectable( sl.isSelectable());
			return oldsl;
		}
	}
	
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * the abbreviation, color, alpha, and selectable will be replaced.
	 * 
	 * @param name
	 * @param abbrev
	 * @param color
	 * @param alpha
	 * @param selectable
	 */
	public RhizoStatusLabel addStatusLabelToSet( String name, String abbrev, Color color, int alpha, boolean selectable) {
		RhizoStatusLabel sl = this.statusLabelSet.get(  name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
			sl.setColor(color);
			sl.setAlpha(alpha);
			sl.setSelectable(selectable);
		} else {
			sl = new RhizoStatusLabel( this, name, abbrev, color, alpha, selectable);
			statusLabelSet.put( name, sl);
			this.userSettingsChanged = true;
		}
		return sl;
	}
		
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * the color, alpha, and selectable will be replaced.
	 * 
	 * @param name
	 * @param color
	 * @param alpha
	 * @param selectable
	 */
	public RhizoStatusLabel addStatusLabelToSet( String name, Color color, int alpha, boolean selectable) {
		RhizoStatusLabel sl = this.statusLabelSet.get(  name);
		if ( sl != null ) {
			sl.setColor(color);
			sl.setAlpha(alpha);
			sl.setSelectable(selectable);
		} else {
			sl = new RhizoStatusLabel( this, name, name.substring(1, 1), color, alpha, selectable);
			statusLabelSet.put( name, sl);
			this.userSettingsChanged = true;
		}
		return sl;
	}
		
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * the abbreviation, color and, alpha will be replaced.
	 * 
	 * @param name
	 * @param abbrev
	 * @param color
	 * @param alpha
	 */
	public RhizoStatusLabel addStatusLabelToSet( String name, String abbrev, Color color, int alpha) {
		RhizoStatusLabel sl = this.statusLabelSet.get( name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
			sl.setColor(color);
			sl.setAlpha(alpha);
		} else {
			sl = new RhizoStatusLabel( this, name, abbrev, color, alpha, DEFAULT_SELECTABLE);
			statusLabelSet.put( name, sl);
			this.userSettingsChanged = true;
		}
		return sl;
	}
	
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * the abbreviation and color  will be replaced.
	 * 
	 * @param name
	 * @param abbrev
	 * @param color
	 */
	public RhizoStatusLabel addStatusLabelToSet( String name, String abbrev, Color color) {
		RhizoStatusLabel sl = this.statusLabelSet.get( name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
			sl.setColor(color);
		} else {
			sl = new RhizoStatusLabel( this, name, abbrev, color, DEFAULT_ALPHA, DEFAULT_SELECTABLE);
			statusLabelSet.put( name, sl);
			this.userSettingsChanged = true;
		}
		return sl;
	}
	
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * the abbreviation  will be replaced.
	 * 
	 * @param name
	 * @param abbrev
	 */
	public RhizoStatusLabel addStatusLabelToSet( String name, String abbrev) {
		RhizoStatusLabel sl = this.statusLabelSet.get( name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
		} else {
			sl = new RhizoStatusLabel( this, name, abbrev, DEFAULT_STATUS_COLOR, DEFAULT_ALPHA, DEFAULT_SELECTABLE);
			statusLabelSet.put( name, sl);
			this.userSettingsChanged = true;
		}
		return sl;
	}
	
	/**
	 * Removes the given key from statusLabelSet
	 * @param name
	 */
	public boolean removeStatusLabelFromSet(String name)
	{
		if(null != statusLabelSet.remove(name)) {
			this.userSettingsChanged = true;
			return true;
		}
		
		return false;
	}

	/** Return names of all defined status labels, i.e. fixed and user defined ones.
	 * @return
	 */
	public Collection<RhizoStatusLabel> getAllStatusLabel() {
		LinkedList<RhizoStatusLabel> sll = new LinkedList<RhizoStatusLabel>();
		sll.addAll( statusLabelSet.values());
		return sll;
	}

	public Collection<RhizoStatusLabel> getAllUserDefinedStatusLabel() {
		LinkedList<RhizoStatusLabel> sll = new LinkedList<RhizoStatusLabel>();
		for ( RhizoStatusLabel sl : statusLabelSet.values()) {
			Utils.log(sl.getName());
			if ( ! this.fixedStatusLabelMap.containsValue(sl.getName()))
				sll.add( sl);
		}
		return sll;
	}
	/**
	 * return the number of defined status labels in the set including the fixed and user defined status label names
	 * @return
	 */
	public int sizeStatusLabelSet() {
		return statusLabelSet.size();
	}
	
	/**
	 * @return The number of user defined status labels in the list, i.e. the number of currently valid
	 * mappings from integer to label names. Otherwise stated: the number returned miuns one is
	 * the largest mapped integer.
	 */
	public int sizeStatusLabelList() {
		return statusLabelList.size();
	}
	
	/** This is a convenience function for compaittility with former rhizoTrak/trakEM version.
	 * @return largest user status integer value currently existing
	 */
	public int getMaxEdgeConfidence () {
		return sizeStatusLabelList()-1;
	}
	
	/**
	 * @return All (negative) status integer values of known fixed status labels
	 */
	public Collection<Integer> getFixedStatusLabelInt() {
		return fixedStatusLabelMap.keySet();
	}
	
	/** return the status label associated with <code>i</code>.
	 * 
	 * @param i
	 * @return <ul>
	 *         <li> the status label associated with <code>i</code>, if it exists
	 *         <li> the status label associated with <code>STATUS_UNDEFINED</code>, if not existing and <code>i</code> is non negative
	 *         <li> <code>INVALID_STATUS_LABEL</code>,  if not existing and <code>i</code> is negative
	 *         </ul>
	 */
	public RhizoStatusLabel getStatusLabel( int i) {
		if ( i >= 0 ) {
			if ( i < sizeStatusLabelList() )
				return statusLabelList.get(i);
			else {
				return statusLabelSet.get(  fixedStatusLabelMap.get( STATUS_UNDEFINED));
			}
		} else {
			if ( fixedStatusLabelMap.containsKey( i) )
				return statusLabelSet.get( fixedStatusLabelMap.get(i));
			else  {
				Utils.log( "WARNING:getStatusLabel( int i) returns null for " + i);
				return INVALID_STATUS_LABEL;
			}
		}
	}
	
	/** return the status label associated with <code>name</code>.
	 * 
	 * @param name
	 * @return the status label or <code>INVALID_STATUS_LABEL</code>, if non existing
	 */
	public RhizoStatusLabel getStatusLabel( String name) {
	
		if ( statusLabelSet.get(name) != null ) {
				return statusLabelSet.get(name);
		} else {
			Utils.log( "RhizoProjectConfig WARNING: getStatusLabel( String) non existing for " + name);
			return INVALID_STATUS_LABEL;
		}			
	}
 
	/**
	 * @param i
	 * @return <ul>
	 *         <li> the color of status label associated with <code>i</code>, if it exists
	 *         <li> the color of status label associated with <code>STATUS_UNDEFINED</code>, if not existing and <code>i</code> is non negative
	 *         <li> color of  <code>INVALID_STATUS_LABEL</code>,  if not existing and <code>i</code> is negative
	 *         </ul>
	 */
	public Color getColorForStatus( int i) {
		if ( i >= 0 ) {
			if ( i < sizeStatusLabelList() )
				return makeColor( statusLabelList.get(i));
			else {
				return makeColor( statusLabelSet.get(  fixedStatusLabelMap.get( STATUS_UNDEFINED)));
			}
		} else {
			if ( fixedStatusLabelMap.containsKey( i) )
				return makeColor( statusLabelSet.get( fixedStatusLabelMap.get(i)));
			else  {
				Utils.log( "WARNING:getStatusLabel( int i) returns null for " + i);
				return makeColor( INVALID_STATUS_LABEL);
			}
		}
	}
	
	/** make color including alpha from status label
	 * @param sl
	 * @return
	 */
	private Color makeColor( RhizoStatusLabel sl) {
		return new Color( sl.getColor().getRed(), sl.getColor().getGreen(),sl.getColor().getBlue(), sl.getAlpha() );
	}

	/**
	 * 
	 * Set the default user defined status label
	 * <ul>
	 * <li> LIVING
	 * <li>DEAD
	 * <li>DECAYED
	 * <li>GAP
	 * </ul>.
	 * 
	 * The List defining the mapping will be clear in advance.
	 * If corresponding status label names are not define yet they will me created.
	 */
	public void setDefaultUserStatusLabel() {
		statusLabelList.clear();
	
		appendStatusLabelToList( addStatusLabelToSet( "LIVING", "L"));
		appendStatusLabelToList( addStatusLabelToSet( "DEAD", "D"));
		appendStatusLabelToList( addStatusLabelToSet( "DECAYED", "Y"));
		appendStatusLabelToList( addStatusLabelToSet( "GAP", "G"));
		
	}
	
	/**
	 * clear the list of user define status labels
	 */
	public void clearStatusLabels() {
		statusLabelList.clear();
	}

	/**
	 * @return the highlightColor1
	 */
	public Color getHighlightColor1() {
		return this.highlightColor1;
	}

	/**
	 * @param highlightColor1 the highlightColor1 to set
	 */
	public void setHighlightColor1(Color highlightColor1) {
		this.highlightColor1 = highlightColor1;
	}

	/**
	 * @return the highlightColor2
	 */
	public Color getHighlightColor2() {
		return this.highlightColor2;
	}

	/**
	 * @param highlightColor2 the highlightColor2 to set
	 */
	public void setHighlightColor2(Color highlightColor2) {
		this.highlightColor2 = highlightColor2;
	}
	
	/**
	 * @return the imageSearchDir
	 */
	public File getImageSearchDir() {
		return imageSearchDir;
	}

	/**
	 * @param imageSearchDir the imageSearchDir to set
	 */
	public void setImageSearchDir(File imageSearchDir) {
		this.imageSearchDir = imageSearchDir;
	}

	/**
	 * @return the receiverNodeColor
	 */
	public Color getReceiverNodeColor() {
		return receiverNodeColor;
	}

	/**
	 * @param receiverNodeColor the receiverNodeColor to set
	 */
	// we use the static member in Node as thus no reference to RhizoMain is needed in Node
	public void setReceiverNodeColor(Color color) {
		Node.setReceiverColor( color);
		setUserSettingsChanged();
		this.receiverNodeColor = color;
	}

	/**
	 * @param askMergeTreelines the askMergeTreelines to set
	 */
	public void setAskMergeTreelines(boolean askMergeTreelines) {
		setUserSettingsChanged();
		this.askMergeTreelines = askMergeTreelines;
	}

	/**
	 * @return the askMergeTreelines
	 */
	public boolean isAskMergeTreelines() {
		return askMergeTreelines;
	}

	/**
	 * @return the askSplitTreeline
	 */
	public boolean isAskSplitTreeline() {
		return askSplitTreeline;
	}

	/**
	 * @param askSplitTreeline the askSplitTreeline to set
	 */
	public void setAskSplitTreeline(boolean askSplitTreeline) {
		setUserSettingsChanged();
		this.askSplitTreeline = askSplitTreeline;
	}

	/**
	 * @return the showCalibrationInfo
	 */
	public boolean isShowCalibrationInfo() {
		return showCalibrationInfo;
	}

	/**
	 * @param showCalibrationInfo the showCalibrationInfo to set
	 */
	public void setShowCalibrationInfo(boolean showCalibrationInfo) {
		setUserSettingsChanged();
		this.showCalibrationInfo = showCalibrationInfo;
	}

	/**
	 * @return true if the state of the instance which is part of user settings has change since instantiation or after last {@link #resetChanged}
	 */
	public boolean userSettingsChanged() {
		return userSettingsChanged;
	}

	/**
	 * Notify that  the state of the instance which is part of user settings has change since instantiation
	 * or last reset
	 */
	void setUserSettingsChanged() {
		userSettingsChanged = true;
	}

	/**
	 * Reset the changed state of the instance which is part of user settings to false, e.g. subsequent to saving the project configuration
	 */
	public void resetChanged() {
		this.userSettingsChanged = false;
	}

	public void printStatusLabelSet() {
		for ( RhizoStatusLabel sl : this.getAllStatusLabel()) {
			System.out.println("\t" + sl.getName() + " " + sl.getAbbrev() + " " + sl.getColor() + " " +
			sl.getAlpha() + " " + sl.isSelectable());
		}

	}

	public void printStatusLabelList() {
		System.out.println( "StatusLabelList");
		for ( RhizoStatusLabel sl : this.statusLabelList ) {
			System.out.println("\t" + sl.getName());
		}
	}
	
	public void printFixStatusLabels() {
		System.out.println( "FixStatusLabels");
	    for ( int i : fixedStatusLabelMap.keySet() ) {
	    	System.out.println(  i + " --> " + fixedStatusLabelMap.get(i));
	    }
	}
}
