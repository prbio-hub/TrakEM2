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

/**

TrakEM2 plugin for ImageJ(C).
Copyright (C) 2005-2009 Albert Cardona and Rodney Douglas.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 

You may contact Albert Cardona at acardona at ini.phys.ethz.ch
Institute of Neuroinformatics, University of Zurich / ETH, Switzerland.
**/

package de.unihalle.informatik.rhizoTrak.persistence;

import java.util.Set;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

/** Base class of all objects that can be saved in a database or XML file.
 * <p>
 *  Methods to add to, update in and remove from a database are called anyway for XML projects,
 *  and can thus be used to perform tasks on updating a specific object.
 * </p>
 */
public class DBObject {

	protected long id;
	protected Project project;

	/** Create new and later add it to the database. */
	public DBObject(Project project) {
		this.project = project;
		this.id = project.getLoader().getNextId();
	}

	/** Reconstruct from database. */
	public DBObject(Project project, long id) {
		this.project = project;
		this.id = id;
	}

	/** For the Project */
	public DBObject(Loader loader) {
		this.id = loader.getNextId();
	}

	public final long getId() { return id; }
	
	/**
	 *  Create a unique String identifier for this object instance.
	 *  
	 *  TODO
	 *    The default implementation returns the project-specific id.
	 *    This behaviour has to be overridden in order to get an identifier
	 *    that is unique beyond the project scope, e.g. for use in cache file
	 *    names.
	 *    
	 * @return Unique name
	 */
	public String getUniqueIdentifier()
	{
		return Long.toString(this.id);
	}

	public final Project getProject() { return project; }

	public boolean addToDatabase() {
		return project.getLoader().addToDatabase(this);
	}

	public boolean updateInDatabase(String key) {
		return project.getLoader().updateInDatabase(this, key);
	}
	public boolean updateInDatabase(Set<String> keys) {
		return project.getLoader().updateInDatabase(this, keys);
	}

	public boolean removeFromDatabase() {
		return project.getLoader().removeFromDatabase(this);
	}

	/** Subclasses can override this method to perform other tasks before removing itself from the database. */
	public boolean remove(boolean check) {
		if (check && !Utils.check("Really remove " + this.toString() + " ?")) return false;
		return removeFromDatabase();
	}

	/** Subclasses can override this method to store the instance as XML. */
	public void exportXML(StringBuilder sb_body, String indent, XMLOptions options) {
		Utils.log("ERROR: exportXML not implemented for " + getClass().getName());
	}

	/** Sublcasses can override this method to provide a proper String, otherwise calls toString()*/
	public String getTitle() {
		return this.toString();
	}

	/** Sublcasses can override this method to provide a proper String, otherwise calls getTitle() */
	public String getShortTitle() {
		return getTitle();
	}

	/** Returns id and project name; this method is meant to be overriden by any of the subclasses. */
	public String getInfo() {
		return "Class: " + this.getClass().getName() + "\nID: " + this.id + "\nFrom:\n" + this.project.getInfo();
	}
}
