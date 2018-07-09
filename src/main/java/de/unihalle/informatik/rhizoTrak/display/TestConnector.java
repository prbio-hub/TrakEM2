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

/* === original file header below (if any) === */

package de.unihalle.informatik.rhizoTrak.display;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.HashMap;

import de.unihalle.informatik.rhizoTrak.Project;

public class TestConnector extends ZDisplayable {

	public TestConnector(Project project, String title) {
		super(project, title,0,0);
		addToDatabase();
		// TODO Auto-generated constructor stub
	}

	public TestConnector(Project project, long id, String title, boolean locked, AffineTransform at, float width,
			float height) {
		super(project, id, title, locked, at, width, height);
		// TODO Auto-generated constructor stub
	}

	public TestConnector(Project project, long id, HashMap<String, String> ht, HashMap<Displayable, String> ht_links) {
		super(project, id, ht, ht_links);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean linkPatches() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Layer getFirstLayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean intersects(Area area, double z_first, double z_last) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean calculateBoundingBox(Layer la) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDeletable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Displayable clone(Project pr, boolean copy_id) {
		// TODO Auto-generated method stub
		return null;
	}

}
