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

package de.unihalle.informatik.rhizoTrak.conflictManagement;

import java.util.ArrayList;

import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class TreelineConflict extends Conflict {
	TreelineConflictKey treeConKey;
	ArrayList<Treeline> treelineList;
	
	/**
	 * @param connector
	 * @param treelineList
	 * @param layer
	 */
	public TreelineConflict(TreelineConflictKey treeConKey, ArrayList<Treeline> treelineList) {
		this.treeConKey = treeConKey;
		this.treelineList = treelineList;
	}
	
	/**
	 * @param connector
	 * @param layer
	 */
	public TreelineConflict(TreelineConflictKey treeConKey) {
		this.treeConKey = treeConKey;
		update();
	}


	/**
	 * @return the treeConKey
	 */
	public TreelineConflictKey getTreeConKey() {
		return treeConKey;
	}

	/**
	 * @return the connected Treelines
	 */
	public ArrayList<Treeline> getTreelineOne() {
		return treelineList;
	}

	private String getTreelinesAsString(){
		String result = "Treelines: ";
		for (Treeline treeline : treelineList) {
			result = result + treeline.getId() + "; ";
		}
		return result;
	}
	
	public void update()
	{
		//fetch the connector list
		ArrayList<Treeline> treelineList = new ArrayList<Treeline>();
		Connector connector = this.treeConKey.getConnector();
		Layer layer = this.treeConKey.getLayer();
		for(Treeline tree: connector.getConTreelines())
		{
			if(tree.getFirstLayer().equals(layer))
			{
				treelineList.add(tree);	
			}
		}
		this.treelineList=treelineList;
		
		if(this.treelineList.size()<2)
		{
			//no real issue here
			ConflictManager.removeTreelineConflict(this.treeConKey);
		}
	}
	
	@Override 
	public String toString(){
		return "Multiple Treeline Conflict on Connector: "+ treeConKey.getConnector().getId() + " " + getTreelinesAsString();
	}
	
}
