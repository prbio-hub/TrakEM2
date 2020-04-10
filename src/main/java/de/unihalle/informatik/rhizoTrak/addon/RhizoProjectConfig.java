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

package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.addonGui.RhizoScalebar.DisplayPosition;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

/** Hold all project specific settings, including some general and fixed ones.
 * Most of these will be imported/exported to project specific files.
 * <p>
 * Includes
 * <ul>
 * <li> mapping from status integer values to status labels (user defined and fixed)
 * {@linkplain #getStatusLabel(int)}
 * <li> a set of all user and fixed labels {@linkplain #getAllStatusLabel() }defined currently
 * including the definition, color etc
 * <li>highlight colors
 * <li>user settings (flags)
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
	 * associated in the <code>statusLabelMapping</code>
	 */
	public static final int STATUS_UNDEFINED = -1;
	
	/**
	 * The name for <code><STATUS_UNDEFINE/code>
	 */
	
	public static final String NAME_UNDEFINED = "UNDEFINED";
	
	/**
	 * to represent (virtual) segments to connect 
	 * branches (polylines) of a root to form one (connected) treeline
	 * <br>
	 * used to represent genuine rizoTrak virtual segments
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
	
	/**
	 * to represent (virtual) segments created on import from RSML to connect all
	 * branches (polylines) of a root to form one (connected) treeline
	 * 	 * <br>
	 * used to represent virtual segments create for/from RSML
	 * 
	 */
	public static final int STATUS_VIRTUAL_RSML = -4;
	
	/**
	 * The name for <code>STATUS_VIRTUAL_RSML</code>
	 */
	public static final String NAME_VIRTUAL_RSML = "VIRTUAL_RSML";
	
	
	public static final Color DEFAULT_STATUS_COLOR = new Color( 255, 255, 0);
	public static final Color DEFAULT_FIXED_STATUS_COLOR = new Color( 0, 255, 255);

	public static final int DEFAULT_ALPHA = 255;
	public static final boolean DEFAULT_SELECTABLE = true;
	
	/**
	 * Mapping of fixed status integer values used for internal purpose to their names
	 */
	private final HashMap<Integer,String> fixedStatusLabelMap = new HashMap<Integer, String>();

	/** Mapping of status integer to status labels names.
	  * Different status integers may be mapped to the same status label name.
	 */
	private ArrayList<RhizoStatusLabel> statusLabelMapping = new ArrayList<RhizoStatusLabel>();
	
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
	 * If true the nodes of a treeline will drawn as a circle
	 */
	private boolean nodesAsCircle = true;
	
	/**
	 * If true the treeline-segment radius will be represented as a line at each endpoints
	 */
	private boolean nodesDiameterLines = true;
	
	/**
	 * If true the treeline-segment will be drawn as the outer polygon 
	 */
	private boolean segmentsAsPolygon = true;
	
	/**
	 * If true the treeline-segment will be a filled polygon
	 */
	private boolean segmentsFill = true;
	
	/**
	 * If true the rhizoTrak will start with the full GUI (as opposed to a lean GUI)
	 */
	private boolean fullGUI = false;
	
	/**
	 * If true the image calibration information is shown to the user when writing statistics with non-pixel units
	 */
	private boolean showCalibrationInfo = true;

	/**
	 * If true a scalebar is overlaid to the images.
	 */
	private boolean showScalebar = false;

	/**
	 * Position where the scalebar is to be drawn.
	 */
	private DisplayPosition scalebarPosition = DisplayPosition.BOTTOM_LEFT;
	
	/**
	 * Width of the scalebar line.
	 */
	private int scalebarLinewidth=3;

	/**
	 * Width of the scalebar in pixels.
	 */
	private int scalebarPixelwidth = 200;

	/**
	 * Color of scalebar.
	 */
	private Color scalebarColor = new Color(255,255,0,255); // yellow with full alpha
	
	/**
	 * Font size to be used.
	 */
	private int scalebarFontSize = 24;

	/**
	 * true if the parent indices start with 1, otherwise it is assume the they start with 0
	 * 
	 */
	private boolean parentNodeIndexStartsWithOne = true;

	/**
	 * if true sample element in functions of RSML files are written as attribute value,
	 * otherwise written as text
	 */
	private boolean writeFunctionSamplesAsAttribute = true;
	
	public RhizoProjectConfig() {
		// we always need the fixed status labels
		statusLabelSet.put( NAME_UNDEFINED, new RhizoStatusLabel( this, NAME_UNDEFINED, "*", DEFAULT_FIXED_STATUS_COLOR));
		statusLabelSet.put( NAME_VIRTUAL, new RhizoStatusLabel( this, NAME_VIRTUAL, "-", DEFAULT_FIXED_STATUS_COLOR));
		statusLabelSet.put( NAME_CONNECTOR, new RhizoStatusLabel( this, NAME_CONNECTOR, "@", DEFAULT_FIXED_STATUS_COLOR));
		statusLabelSet.put( NAME_VIRTUAL_RSML, new RhizoStatusLabel( this, NAME_VIRTUAL_RSML, "-", DEFAULT_FIXED_STATUS_COLOR));
		fixedStatusLabelMap.put( STATUS_UNDEFINED, NAME_UNDEFINED);
		fixedStatusLabelMap.put( STATUS_VIRTUAL, NAME_VIRTUAL);
		fixedStatusLabelMap.put( STATUS_CONNECTOR, NAME_CONNECTOR);	
		fixedStatusLabelMap.put( STATUS_VIRTUAL_RSML, NAME_VIRTUAL_RSML);
		setReceiverNodeColor( Node.getReceiverColor());	
	}
	
	/** Append status label as the last label to the mapping of status labels.
	 * If a corresponding status label does not exist yet if will be created with the default color.
	 * If it already did exist the abbreviation will be replace which has effect also
	 * to potential further occurrences of this status label 
	 * 
	 * @param sl
	 */
	public void appendStatusLabelMapping( RhizoStatusLabel statusLabel) {
		statusLabelMapping.add( statusLabel);
		if(null != Display.getFront()) Display.getFront().getProject().getLoader().setChanged(true);
	}

	/**
	 * remove the last label from the mapping of status labels
	 */
	public void popStatusLabelMapping() {
		if ( statusLabelMapping.size() > 0) {
			statusLabelMapping.remove( statusLabelMapping.size()-1);
			if(null != Display.getFront()) Display.getFront().getProject().getLoader().setChanged(true);
		}
	}
	
	/** Replaces the i-th entry with the given status label
	 * @param i
	 * @param statusLabel
	 * @return true on success, false if index out of range
	 */
	public boolean replaceStatusLabelMapping( int i, RhizoStatusLabel statusLabel) {
		if ( i >= 0 && i < statusLabelMapping.size() ) {
			statusLabelMapping.set( i, statusLabel);
			if(null != Display.getFront()) Display.getFront().getProject().getLoader().setChanged(true);
			return true;
		} else {
			Utils.log( "rhizotrak", "PRhizoProjectConfig.replaceStatusLabelMapping index " + i + " out of bounds");
			return false;
		}
	}
	
	/** Add the status label to the set of all currently defined status labels. If one with the same name already exists
	 * the abbreviation, color, alpha, and selectable will be replaced.
	 * <br>
	 * Note: add a status label does not define a mapping from a status label integer to a name 
	 *
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
	
	/**Add the status label to the set of all currently defined status labels. If one with the same name already exists
	 * the abbreviation, color, alpha, and selectable will be replaced.
	 * <br>
	 * Note: add a status label does not define a mapping from a status label integer to a name
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
		
	/**Add the status label to the set of all currently defined status label. If one with the same name already exists
	 * the color, alpha, and selectable will be replaced.
	 * <br>
	 * Note: add a status label does not define a mapping from a status label integer to a name
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
		
	/**Add the status label to the set of all currently defined status label. If one with the same name already exists
	 * the abbreviation, color and, alpha will be replaced.
	 * <br>
	 * Note: add a status label does not define a mapping from a status label integer to a name 
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
	
	/**Add the status label to the set of all currently defined status label. If one with the same name already exists
	 * the abbreviation and color  will be replaced.
	 * <br>
	 * Note: add a status label does not define a mapping from a status label integer to a name
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
	
	/**Add the status label to the set of all currently defined status labels. If one with the same name already exists
	 * the abbreviation  will be replaced.
	 * <br>
	 * Note: add a status label does not define a mapping from a status label integer to a name
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
	 * Removes the given status label with name <code>name</code>the set of all currently defined status labels.
	 * 
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

	/** Return all defined status labels, i.e. fixed and user defined ones.
	 * @return
	 */
	public Collection<RhizoStatusLabel> getAllStatusLabel() {
		LinkedList<RhizoStatusLabel> sll = new LinkedList<RhizoStatusLabel>();
		sll.addAll( statusLabelSet.values());
		return sll;
	}

	/** Return all user defined status labels, i.e. excluding fixed ones.
	 * @return
	 */
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
	 * return the number of currently defined status labels, i.e. both the fixed and user defined status labels
	 * @return
	 */
	public int sizeStatusLabelSet() {
		return statusLabelSet.size();
	}
	
	/**
	 * The number of currently mapped status labels, i.e. the number of currently valid
	 * mappings from integer to label names. Otherwise stated: the number returned minus one is
	 * the largest mapped integer.
	 * 
	 * 	 @return 
	 */
	public int sizeStatusLabelMapping() {
		return statusLabelMapping.size();
	}
	
	/** This is a convenience function for compatibility with former rhizoTrak/trakEM version.
	 * @return largest user status integer value currently mapped
	 */
	public int getMaxEdgeConfidence () {
		return sizeStatusLabelMapping()-1;
	}
	
	public List<RhizoStatusLabel> getStatusLabelMapping() {
		return statusLabelMapping;
	}
	
	/**
	 * @return All (negative) status integer values of known fixed status labels
	 */
	public Collection<Integer> getFixedStatusLabelInt() {
		return fixedStatusLabelMap.keySet();
	}
	
	/** The status label currently mapped to <code>i</code>.
	 * 
	 * @param i
	 * @return <ul>
	 *         <li> the status label mapped to <code>i</code>, if it exists
	 *         <li> the status label mapped to  <code>STATUS_UNDEFINED</code>, if <code>i</code> is currently not mapped and is non negative
	 *         <li> <code>INVALID_STATUS_LABEL</code>,  if not existing and <code>i</code> is negative
	 *         </ul>
	 */
	public RhizoStatusLabel getStatusLabel( int i) {
		if ( i >= 0 ) {
			if ( i < sizeStatusLabelMapping() )
				return statusLabelMapping.get(i);
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
	
	/** The status label associated with <code>name</code>.
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
	
	
 
	/** The color of the status label currently mapped to <code>i</code>.
	 * 
	 * @param i
	 * @return <ul>
	 *         <li> the color of the status label mapped to <code>i</code>, if it exists
	 *         <li> the color of the status label mapped to  <code>STATUS_UNDEFINED</code>, if <code>i</code> is currently not mapped and is non negative
	 *         <li> the color of  <code>INVALID_STATUS_LABEL</code>,  if not existing and <code>i</code> is negative
	 *         </ul>

	 */
	public Color getColorForStatus( int i) {
		if ( i >= 0 ) {
			if ( i < sizeStatusLabelMapping() )
				return makeColor( statusLabelMapping.get(i));
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
	
	/** Make a color object of class {@link java.awt.Color} including alpha from a status label
	 * 
	 * @param sl
	 * @return
	 */
	private Color makeColor( RhizoStatusLabel sl) {
		return new Color( sl.getColor().getRed(), sl.getColor().getGreen(),sl.getColor().getBlue(), sl.getAlpha() );
	}

	/**
	 * 
	 * Set the default mapping of status label
	 * <ul>
	 * <li> LIVING
	 * <li>DEAD
	 * <li>DECAYED
	 * <li>GAP
	 * </ul>.
	 * 
	 * The mapping of status labels will be cleared in advance.
	 * If corresponding status label names are not define yet they will me created.
	 */
	public void setDefaultUserStatusLabel() {
		statusLabelMapping.clear();
	
		appendStatusLabelMapping( addStatusLabelToSet( "LIVING", "L"));
		appendStatusLabelMapping( addStatusLabelToSet( "DEAD", "D"));
		appendStatusLabelMapping( addStatusLabelToSet( "DECAYED", "Y"));
		appendStatusLabelMapping( addStatusLabelToSet( "GAP", "G"));
		
	}
	
	/**
	 * Clear the mapping of status labels. Subsequently no status label integer is mapped.
	 */
	public void clearStatusLabelMapping() {
		statusLabelMapping.clear();
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
	 * @return the nodesAsCircle
	 */
	public boolean isNodesAsCircle() {
		return nodesAsCircle;
	}

	/**
	 * @param nodesAsCircle the nodesAsCircle to set
	 */
	public void setNodesAsCircle(boolean nodesAsCircle) {
		setUserSettingsChanged();
		this.nodesAsCircle = nodesAsCircle;
	}
	
	/**
	 * @return the nodesDiameterLines
	 */
	public boolean isNodesDiameterLines() {
		return nodesDiameterLines;
	}

	/**
	 * @param nodesDiameterLines the nodesDiameterLines to set
	 */
	public void setNodesDiameterLines(boolean nodesDiameterLines) {
		setUserSettingsChanged();
		this.nodesDiameterLines = nodesDiameterLines;
	}
	
	/**
	 * @return the segmentsAsPolygon
	 */
	public boolean isSegmentsAsPolygon() {
		return segmentsAsPolygon;
	}

	/**
	 * @param segmentsAsPolygon the segmentsAsPolygon to set
	 */
	public void setSegmentsAsPolygon(boolean segmentsAsPolygon) {
		setUserSettingsChanged();
		this.segmentsAsPolygon = segmentsAsPolygon;
	}
	
	/**
	 * @return the segmentsFill
	 */
	public boolean isSegmentsFill() {
		return segmentsFill;
	}

	/**
	 * @param segmentsFill the segmentsFill to set
	 */
	public void setSegmentsFill(boolean segmentsFill) {
		setUserSettingsChanged();
		this.segmentsFill = segmentsFill;
	}

	/**
	 * @return the fullGUI
	 */
	public boolean isFullGUI() {
		return fullGUI;
	}

	/**
	 * @param fullGUI the fullGUI to set
	 */
	public void setFullGUI(boolean fullGUI) {
		setUserSettingsChanged();
		this.fullGUI = fullGUI;
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
	 * Request if scalebar is to be shown.
	 * @return True if scalebar should be displayer.
	 */
	public boolean isShowScalebar() {
		return this.showScalebar;
	}

	/**
	 * Configure if scalebar is to be shown.
	 * @param showBar	If true, the scalebar is visible.
	 */
	public void setShowScalebar(boolean showBar) {
		this.showScalebar = showBar;
		setUserSettingsChanged();
	}

	/**
	 * Set scalebar position.
	 * @param pos	Position of scalebar in display.
	 */
	public void setScalebarPosition(DisplayPosition pos) {
		this.scalebarPosition = pos;
		setUserSettingsChanged();
	}
	
	/**
	 * Set scalebar line width.
	 * @param pos	Line width of scalebar.
	 */
	public void setScalebarLinewidth(int width) {
		this.scalebarLinewidth = width;
		setUserSettingsChanged();
	}

	/**
	 * Set scalebar length in pixels.
	 * @param pos	Length of scalebar in pixels.
	 */
	public void setScalebarPixelwidth(int width) {
		this.scalebarPixelwidth = width;
		setUserSettingsChanged();
	}

	/**
	 * Set scalebar color.
	 * @param pos	Color of scalebar.
	 */
	public void setScalebarColor(Color color) {
		this.scalebarColor = color;
		setUserSettingsChanged();
	}

	/**
	 * Set font size of scalebar label.
	 * @param pos	Label font size.
	 */
	public void setScalebarFontsize(int size) {
		this.scalebarFontSize = size;
		setUserSettingsChanged();
	}

	/**
	 * Get scalebar position.
	 * @return	Position of scalebar.
	 */
	public DisplayPosition getScalebarPosition() {
		return this.scalebarPosition;
	}
	
	/**
	 * Get scalebar line width.
	 * @return	Line width of scalebar.
	 */
	public int getScalebarLinewidth() {
		return this.scalebarLinewidth;
	}

	/**
	 * Get scalebar pixel length.
	 * @return	Length of scalebar in pixels.
	 */
	public int getScalebarPixelwidth() {
		return this.scalebarPixelwidth;
	}

	/**
	 * Get scalebar color.
	 * @return	Color of scalebar.
	 */
	public Color getScalebarColor() {
		return this.scalebarColor;
	}

	/**
	 * Get font size of scalebar label.
	 * @return	Font size of label.
	 */
	public int getScalebarFontsize() {
		return this.scalebarFontSize;
	}

	/**
	 * @return the parentNodeIndexStartsWithOne
	 */
	public boolean isParentNodeIndexStartsWithOne() {
		return parentNodeIndexStartsWithOne;
	}

	/**
	 * @param parentNodeIndexStartsWithOne the parentNodeIndexStartsWithOne to set
	 */
	public void setParentNodeIndexStartsWithOne(boolean parentNodeIndexStartsWithOne) {
		setUserSettingsChanged();
		this.parentNodeIndexStartsWithOne = parentNodeIndexStartsWithOne;
	}

	/**
	 * @return the writeFunctionSamplesAsAttribute
	 */
	public boolean isWriteFunctionSamplesAsAttribute() {
		return writeFunctionSamplesAsAttribute;
	}

	/**
	 * @param writeFunctionSamplesAsAttribute the writeFunctionSamplesAsAttribute to set
	 */
	public void setWriteFunctionSamplesAsAttribute(boolean writeFunctionSamplesAsAttribute) {
		setUserSettingsChanged();
		this.writeFunctionSamplesAsAttribute = writeFunctionSamplesAsAttribute;
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
	public void setUserSettingsChanged() {
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

	public void printStatusLabelMapping() {
		System.out.println( "StatusLabelMapping");
		for ( RhizoStatusLabel sl : this.statusLabelMapping ) {
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
