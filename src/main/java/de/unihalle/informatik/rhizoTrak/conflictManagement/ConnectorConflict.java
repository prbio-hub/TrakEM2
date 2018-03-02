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
import de.unihalle.informatik.rhizoTrak.display.TreeEventListener;
import de.unihalle.informatik.rhizoTrak.display.Treeline;

public class ConnectorConflict extends Conflict{
	Treeline conflictTree;
	ArrayList<Connector> connectorList;
	
	ConnectorConflict(Treeline tree, ArrayList<Connector> connectorList){
		conflictTree = tree;
		this.connectorList = connectorList;
	}

	ConnectorConflict(Treeline tree){
		conflictTree = tree;
		update();
	}
	
	/**
	 * @return the conflictTree
	 */
	public Treeline getConflictTree() {
		update();
		return conflictTree;
	}
	
	/**
	 * @return the connectorList
	 */
	public ArrayList<Connector> getConnectorList() {
		return connectorList;
	}


	private String getConnectorAsString()
	{
		String result = " Connectors: ";
		for (Connector connector : connectorList) {
			result = result + connector.getId() + "; ";
		}
		return result;
	}
	
	public void update()
	{
		//fetch the connector list
		ArrayList<Connector> connectorList = new ArrayList<Connector>();
		for(TreeEventListener listener: conflictTree.getTreeEventListener())
		{
			connectorList.add(listener.getConnector());
		}
		this.connectorList=connectorList;
		
		if(this.connectorList.size()<2)
		{
			//no real issue here so remove its self
			ConflictManager.removeConnectorConflict(conflictTree);
		}
	}


	@Override 
	public String toString(){
		return "Multiple Connector Conflict on Treeline: "+ conflictTree.getId() + getConnectorAsString();
	}
	
	private boolean stillExists()
	{
		boolean result = true;
		return result;
	}
	
}
