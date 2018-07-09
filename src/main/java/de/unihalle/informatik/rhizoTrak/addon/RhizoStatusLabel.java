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
import java.math.BigInteger;

import org.python.antlr.base.boolop;

/**
 * A status label string with its abbreviation and so forth.
 * 
 * <b>NOTE</> if the value of a member change notify the  {@link #projectConfig} that its
 * user settings were changed
 * </b>
 * 
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
	
	public RhizoStatusLabel( RhizoProjectConfig projectConfig, String name, String abbrev, Color color) {
		this.projectConfig = projectConfig;
		this.name = name;
		this.abbrev = abbrev;
		this.color = color;
	}

	public RhizoStatusLabel( RhizoProjectConfig projectConfig, String name, String abbrev, Color color, int alpha, boolean selectable) {
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
		if ( ! this.abbrev.equals(abbrev)) {
			this.abbrev = abbrev;
			this.projectConfig.setUserSettingsChanged();
		}
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
		if ( this.color.getRed() != color.getRed() ||
				this.color.getGreen() != color.getGreen() ||
				this.color.getBlue() != color.getBlue()) {
			this.color = color;
			this.projectConfig.setUserSettingsChanged();
		}
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
		 if ( this.alpha != alpha) {
			 this.alpha = alpha;
			 this.projectConfig.setUserSettingsChanged();
		 }
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
		if ( this.selectable != selectable) {
			this.selectable = selectable;
			this.projectConfig.setUserSettingsChanged();
		}
	}

}
