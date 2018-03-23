package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

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

	/**
	 * map fixed status integer values used for internal purpose to their names
	 */
	private final HashMap<Integer,String> fixedStatusLabelMap = new HashMap<Integer, String>();

	/** definitions of user defines status labels
	 * does only hold the names, a name may be contained multiple times, order cares
	 */
	private Stack<String> statusLabelList = new Stack<String>();
	
	/**
	 * Defines the mapping from integer status values to status label names for
	 * fixed and user defined status labels.
	 */
	private HashMap<String,RhizoStatusLabel> statusLabelSet = new HashMap<String,RhizoStatusLabel>();
	
	/**
	 * This status label is return (instead of null) in case a request with invalid integer status value
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
	
	public RhizoProjectConfig() {
		// we always need the fixed status labels
		statusLabelSet.put( NAME_UNDEFINED, new RhizoStatusLabel( this, NAME_UNDEFINED, "*", DEFAULT_FIXED_STATUS_COLOR));
		statusLabelSet.put( NAME_VIRTUAL, new RhizoStatusLabel( this, NAME_VIRTUAL, "-", DEFAULT_FIXED_STATUS_COLOR));
		statusLabelSet.put( NAME_CONNECTOR, new RhizoStatusLabel( this, NAME_CONNECTOR, "@", DEFAULT_FIXED_STATUS_COLOR));
		fixedStatusLabelMap.put( STATUS_UNDEFINED, NAME_UNDEFINED);
		fixedStatusLabelMap.put( STATUS_VIRTUAL, NAME_VIRTUAL);
		fixedStatusLabelMap.put( STATUS_CONNECTOR, NAME_CONNECTOR);	

	}
	
	/** Append status label as the last label to the list.
	 * If a corresponding status label does not exist yet if will be created with the default color.
	 * If it already did exist the abbreviation will be replace which has effect also
	 * to potential further occurrences of this status label 
	 * 
	 * @param sl
	 */
	public void appendStatusLabelToList( String name, String abbrev) {
		// mind: abbreviations are not part of user settings, so do not set hasChanged
		RhizoStatusLabel sl = this.statusLabelSet.get(name);
		if ( sl == null ) {
			sl = new RhizoStatusLabel( this, name, abbrev, DEFAULT_STATUS_COLOR);
			addStatusLabelToSet(sl);
		} else {
			if ( ! abbrev.equals(sl.getAbbrev()) ) {
				sl.setAbbrev(abbrev);
			}
		}
		
		statusLabelList.push( name);
	}

	/**
	 * remove the last label from the list
	 */
	public void popStatusLabelFromList() {
		if ( statusLabelList.size() > 0) {
			statusLabelList.pop();
		}
	}
	
	/** Add the status label to the set. If one with the same name already exists
	 * the abbreviation, color, alpha, and selectable will be replaced.
	 * @param sl
	 */
	public void addStatusLabelToSet( RhizoStatusLabel sl) {	
		RhizoStatusLabel oldsl = this.statusLabelSet.get(  sl.getName());
		if ( oldsl == null) {
			statusLabelSet.put( sl.getName(), sl);
			this.userSettingsChanged = true;
		} else {
			oldsl.setAbbrev(  sl.getAbbrev());
			oldsl.setColor(  sl.getColor());
			oldsl.setAlpha( sl.getAlpha());
			oldsl.setSelectable( sl.isSelectable());
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
	public void addStatusLabelToSet( String name, String abbrev, Color color, int alpha, boolean selectable) {
		RhizoStatusLabel sl = this.statusLabelSet.get(  name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
			sl.setColor(color);
			sl.setAlpha(alpha);
			sl.setSelectable(selectable);
		} else {
			statusLabelSet.put( name, new RhizoStatusLabel( this, name, abbrev, color, alpha, selectable));
			this.userSettingsChanged = true;
		}
	}
		
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * the color, alpha, and selectable will be replaced.
	 * 
	 * @param name
	 * @param color
	 * @param alpha
	 * @param selectable
	 */
	public void addStatusLabelToSet( String name, Color color, int alpha, boolean selectable) {
		RhizoStatusLabel sl = this.statusLabelSet.get(  name);
		if ( sl != null ) {
			sl.setColor(color);
			sl.setAlpha(alpha);
			sl.setSelectable(selectable);
		} else {
			statusLabelSet.put( name, new RhizoStatusLabel( this, name, color, alpha, selectable));
			this.userSettingsChanged = true;
		}
	}
		
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * the abbreviation, color and, alpha will be replaced.
	 * 
	 * @param name
	 * @param abbrev
	 * @param color
	 * @param alpha
	 */
	public void addStatusLabelToSet( String name, String abbrev, Color color, int alpha) {
		RhizoStatusLabel sl = this.statusLabelSet.get( name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
			sl.setColor(color);
			sl.setAlpha(alpha);
		} else {
			statusLabelSet.put( name, new RhizoStatusLabel( this, name, abbrev, color, alpha));
			this.userSettingsChanged = true;
		}
	}
	
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * the abbreviation and color  will be replaced.
	 * 
	 * @param name
	 * @param abbrev
	 * @param color
	 */
	public void addStatusLabelToSet( String name, String abbrev, Color color) {
		RhizoStatusLabel sl = this.statusLabelSet.get( name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
			sl.setColor(color);
		} else {
			statusLabelSet.put( name, new RhizoStatusLabel( this, name, abbrev, color));
			this.userSettingsChanged = true;
		}
	}
	
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * the abbreviation  will be replaced.
	 * 
	 * @param name
	 * @param abbrev
	 */
	public void addStatusLabelToSet( String name, String abbrev) {
		RhizoStatusLabel sl = this.statusLabelSet.get( name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
		} else {
			statusLabelSet.put( name, new RhizoStatusLabel( this, name, abbrev));
			this.userSettingsChanged = true;
		}
	}

	/** Return names of all defined status labels, i.e. fixed and user defined ones.
	 * @return
	 */
	public Collection<RhizoStatusLabel> getAllStatusLabel() {
		LinkedList<RhizoStatusLabel> sll = new LinkedList<RhizoStatusLabel>();
		sll.addAll( statusLabelSet.values());
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
				return statusLabelSet.get( statusLabelList.get(i));
			else {
				return statusLabelSet.get(  fixedStatusLabelMap.get( STATUS_UNDEFINED));
			}
		} else {
			if ( fixedStatusLabelMap.containsKey( i) )
				return statusLabelSet.get( fixedStatusLabelMap.get(i));
			else  {
				System.out.println( "WARNING:getStatusLabel( int i) returns null for " + i);
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
				return statusLabelSet.get( statusLabelList.get(i)).getColor();
			else {
				return statusLabelSet.get(  fixedStatusLabelMap.get( STATUS_UNDEFINED)).getColor();
			}
		} else {
			if ( fixedStatusLabelMap.containsKey( i) )
				return statusLabelSet.get( fixedStatusLabelMap.get(i)).getColor();
			else  {
				System.out.println( "WARNING:getStatusLabel( int i) returns null for " + i);
				return INVALID_STATUS_LABEL.getColor();
			}
		}
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
		appendStatusLabelToList( "LIVING", "L");
		appendStatusLabelToList( "DEAD", "D");
		appendStatusLabelToList( "DECAYED", "Y");
		appendStatusLabelToList( "GAP", "G");
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
	 * @return true if the state of the instance which is part of user settings has change since instantiation or after last {@link #resetChanged}
	 */
	public boolean userSettingsChanged() {
		return userSettingsChanged;
	}

	/**
	 * Notify that  the state of the instance which is part of user settings has change since instantiatio
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
		System.out.println( "StatusLabelSet");
		for ( RhizoStatusLabel sl : this.getAllStatusLabel()) {
			System.out.println("\t" + sl.getName() + " " + sl.getAbbrev() + sl.getColor());
		}

	}

	public void printStatusLabelList() {
		System.out.println( "StatusLabelList");
		for ( String name : this.statusLabelList ) {
			System.out.println("\t" + name);
		}
	}
	
	public void printFixStatusLabels() {
		System.out.println( "FixStatusLabels");
	    for ( int i : fixedStatusLabelMap.keySet() ) {
	    	System.out.println(  i + " --> " + fixedStatusLabelMap.get(i));
	    }
	}
}
