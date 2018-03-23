package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.Color;
import java.math.BigInteger;

import org.python.antlr.base.boolop;

/**
 * A status label string with its abbreviation and so forth.
 * 
 * <b>NOTE</> setting of values should only be done via {@link RhizoProjectConfig} to let is control if
 * user settings were changed
 * @author posch
 *
 */
public class RhizoStatusLabel{
	private final  RhizoProjectConfig projectConfig;
	private String name;
	private String abbrev = "";
	private Color color;
	private int alpha = 255;
	boolean selectable = true;
	
	RhizoStatusLabel( RhizoProjectConfig projectConfig, String name, String abbrev, Color color) {
		this.projectConfig = projectConfig;
		this.name = name;
		this.abbrev = abbrev;
		this.color = color;
	}

	RhizoStatusLabel( RhizoProjectConfig projectConfig, String name, String abbrev, Color color, int alpha, boolean selectable) {
		this.projectConfig = projectConfig;
		this.name = name;
		this.abbrev = abbrev;
		this.color = color;
		this.alpha = alpha;
		this.selectable = selectable;
	}
	public RhizoStatusLabel(RhizoProjectConfig projectConfig, String name, String abbrev, Color color, int alpha) {
		this.projectConfig = projectConfig;
		this.name = name;
		this.abbrev = abbrev;
		this.color = color;
		this.alpha = alpha;
	}

	public RhizoStatusLabel( RhizoProjectConfig projectConfig, String name, Color color, int alpha, boolean selectable) {
		this.projectConfig = projectConfig;
		this.name = name;
		this.color = color;
		this.alpha = alpha;
		this.selectable = selectable;
	}

	public RhizoStatusLabel( RhizoProjectConfig projectConfig, String name, String abbrev) {
		this.projectConfig = projectConfig;
		this.name = name;
		this.abbrev = abbrev;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	} 
	
	/**
	 * @return the abbrev
	 */
	public String getAbbrev() {
		return abbrev;
	}

	/**
	 * @param abbrev the abbrev to set
	 */
	void setAbbrev(String abbrev) {
		this.abbrev = abbrev;
	}

/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
		this.projectConfig.setUserSettingsChanged();
	}

	/**
	 * @return the alpha
	 */
	public int getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha the alpha to set
	 */
	 public void setAlpha(int alpha) {
		this.alpha = alpha;
		this.projectConfig.setUserSettingsChanged();
	}

	/**
	 * @return the selectable
	 */
	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * @param selectable the selectable to set
	 */
	 public void setSelectable(boolean selectable) {
		this.selectable = selectable;
		this.projectConfig.setUserSettingsChanged();
	}

}
