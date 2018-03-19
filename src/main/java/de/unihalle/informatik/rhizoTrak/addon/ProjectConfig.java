package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

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
 * Status integer values for user defined status is a contiguous range of positive values starting with 0.
 * Multiple integer status value may be mapped to the same status label.
 * 
 * @author posch
 *
 */
public class ProjectConfig {
	
	/**
	 * to represent a non-negative integer status value with has (currently) no label name
	 * associated in the <code>statusLabelList</code>
	 */
	public static final int STATUS_UNDEFINED = -1;
	/**
	 * to represent /virtual) segments created on import from RSML to connect all
	 * branches (polylines) of a root to form one (connected) treeline
	 * 
	 */
	public static final int STATUS_VIRTUAL = -2;
	
	/**
	 * segments of a connector treeline
	 */
	public static final int STATUS_CONNECTOR = -3;
	
	public static final Color DEFAULT_STATUS_COLOR = new Color( 255, 255, 0);
	public static final Color DEFAULT_FIXED_STATUS_COLOR = new Color( 0, 255, 255);

	/**
	 * map to hold all fixed status labels used for internal purpose
	 */
	private HashMap< Integer,RhizoStatusLabel> fixedStatusLabelMap = new HashMap<Integer, RhizoStatusLabel>();
	

	/** definitions of user defines status labels
	 * does only hold the names, a name may be contained multiple times, order cares
	 */
	private Stack<String> statusLabelList = new Stack<String>();
	
	/**
	 * defines the mapping from integer status values to status label names
	 * fixed and user defined status labels
	 */
	private HashMap<String,RhizoStatusLabel> statusLabelSet = new HashMap<String,RhizoStatusLabel>();
	
	// highlight colors
	private Color highlightColor1 = Color.MAGENTA;
	private Color highlightColor2 = Color.PINK;
	
	public ProjectConfig() {
		// we always need the fixed status labels
		fixedStatusLabelMap.put( STATUS_UNDEFINED, new RhizoStatusLabel( "UNDEFINED", "*", DEFAULT_FIXED_STATUS_COLOR));
		fixedStatusLabelMap.put( STATUS_VIRTUAL, new RhizoStatusLabel( "VIRTUAL", "-", DEFAULT_FIXED_STATUS_COLOR));
		fixedStatusLabelMap.put( STATUS_CONNECTOR, new RhizoStatusLabel( "CONNECTOR", "@", DEFAULT_FIXED_STATUS_COLOR));
		statusLabelSet.put( "UNDEFINED", fixedStatusLabelMap.get(STATUS_UNDEFINED));
		statusLabelSet.put( "VIRTUAL", fixedStatusLabelMap.get(STATUS_VIRTUAL));
		statusLabelSet.put( "CONNECTOR", fixedStatusLabelMap.get(STATUS_CONNECTOR));		
	}
	
	/** Append status label as the last label to the list.
	 * If a corresponding status label does not exist yet if will be created with the default color.
	 * If it already did exist the abbreviation will be replace which has effect also
	 * to potential further occurrences of this status label 
	 * 
	 * @param sl
	 */
	public void appendStatusLabelToList( String name, String abbrev) {
		RhizoStatusLabel sl = statusLabelSet.get(name);
		if ( sl == null ) {
			sl = new RhizoStatusLabel(name, abbrev, DEFAULT_STATUS_COLOR);
			statusLabelSet.put(name, sl);
		} else {
			sl.setAbbrev(abbrev);
		}
		
		statusLabelList.push( name);
	}
			

	/**
	 * remove the last label from the list
	 */
	public void popStatusLabelFromList() {
		if ( statusLabelList.size() > 0)
			statusLabelList.pop();
	}
	
	/** Add the status label to the set. If one with the same name already exists
	 * it will be replaced.
	 * @param sl
	 */
	public void addStatusLabelToSet( RhizoStatusLabel sl) {	
		RhizoStatusLabel oldsl = this.getStatusLabel(  sl.getName());
		if ( oldsl == null) {
			statusLabelSet.put( sl.getName(), sl);
		} else {
			oldsl.setAbbrev(  sl.getAbbrev());
			oldsl.setColor(  sl.getColor());
			oldsl.setAlpha( sl.getAlpha());
			oldsl.setSelectable( sl.isSelectable());
		}
	}
	
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * it will be replaced.
	 * 
	 * @param name
	 * @param abbrev
	 * @param color
	 * @param alpha
	 * @param selectable
	 */
	public void addStatusLabelToSet( String name, String abbrev, Color color, int alpha, boolean selectable) {
		RhizoStatusLabel sl = this.getStatusLabel(  name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
			sl.setColor(color);
			sl.setAlpha(alpha);
			sl.setSelectable(selectable);
		} else {
			statusLabelSet.put( name, new RhizoStatusLabel(name, abbrev, color, alpha, selectable));
		}
	}
	
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * it will be replaced.
	 * 
	 * @param name
	 * @param abbrev
	 * @param color
	 * @param alpha
	 */
	public void addStatusLabelToSet( String name, String abbrev, Color color, int alpha) {
		System.out.println(" XX <" + name + "> - <" + this.getStatusLabel(-1).getName() + "> " + name.equals(this.getStatusLabel(-1).getName()));
		RhizoStatusLabel sl = this.statusLabelSet.get( name);
		if ( sl != null ) {
			sl.setAbbrev(abbrev);
			sl.setColor(color);
			sl.setAlpha(alpha);
			System.out.println("THEN");
		} else {
			statusLabelSet.put( name, new RhizoStatusLabel(name, abbrev, color, alpha));
			System.out.println("ELSE");
			printStatusLabelSet();
		}
	}
	
	/**Add a status label with the given information to the set. If one with the same name already exists
	 * it will be replaced.
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
			statusLabelSet.put( name, new RhizoStatusLabel(name, abbrev, color));
		}
	}
	
	/** Return all defined status labels, i.e. fixed and user defined ones.
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
	 * @return The number of status labels in the list, i.e. the number of currently valid
	 * mappings from integer to label names. Otherwise stated: the number returned miuns one is
	 * the largest mapped integer.
	 */
	public int sizeStatusLabelList() {
		return statusLabelList.size();
	}
	
	/** 
	 * @return number of fixed status labels
	 */
	public int sizeFixedStatusLabelList() {
		return fixedStatusLabelMap.size();
	}
	
	/** return the status label associated with <code>i</code>.
	 * 
	 * @param i
	 * @return the status label or null, if not defined
	 */
	public RhizoStatusLabel getStatusLabel( int i) {
		if ( i >= 0 ) {
			if ( i < sizeStatusLabelList() )
				return statusLabelSet.get( statusLabelList.get(i));
			else
				return null;
		} else {
			if ( i >= - sizeFixedStatusLabelList() )
				return fixedStatusLabelMap.get(i);
			else 
				return null;
		}
	}
	
	/** return the status label associated with <code>name</code>.
	 * 
	 * @param name
	 * @return the status label or null, if not defined
	 */
	public RhizoStatusLabel getStatusLabel( String name) {
	
		if ( statusLabelSet.get(name) != null ) {
				return statusLabelSet.get(name);
		} else {
			for ( RhizoStatusLabel sl : fixedStatusLabelMap.values()) {
				if ( sl.getName().equals(name)  )
					return sl;
			}
			return null;
		}			
	}
 
	public Color getColorForStatus( int i) {
		RhizoStatusLabel sl = getStatusLabel(i);
		if ( sl != null ) {
			return sl.getColor();
		} else {
			return getStatusLabel(STATUS_UNDEFINED).getColor();
		}
	}
	/**
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
		return highlightColor1;
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
		return highlightColor2;
	}

	/**
	 * @param highlightColor2 the highlightColor2 to set
	 */
	public void setHighlightColor2(Color highlightColor2) {
		this.highlightColor2 = highlightColor2;
	}
	
	public void printStatusLabelSet() {
		System.out.println( "StatusLabelSet");
		for ( RhizoStatusLabel sl : this.getAllStatusLabel()) {
			System.out.println("\t" + sl.getName() + " " + sl.getAbbrev() );
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
	    	System.out.println(  i + " --> " + fixedStatusLabelMap.get(i).getName());
	    }
	}
}
