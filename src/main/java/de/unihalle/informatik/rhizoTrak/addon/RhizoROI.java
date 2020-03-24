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

/*
 * The Cohen-Sutherland line clipping algorithm implemented in the internal
 * Segment class has been inspired by
 * 
 * //
 *  * CohenSutherland.java 
 *  * -------------------- 
 *  * (c) 2007 by Intevation GmbH 
 *  * 
 *  * @author Sascha L. Teichmann (teichmann@intevation.de)
 *  * @author Ludwig Reiter       (ludwig@intevation.de)
 *  * 
 *  * This program is free software under the LGPL (>=v2.1) 
 *  * Read the file LICENSE.txt coming with the sources for details. 
 *  //
 *  
 *  originally released under LGPL (>=v2.1). The original source file can, 
 *  e.g., be found on Github:
 *  
 *  https://github.com/tabulapdf/tabula-java/blob/master/src/main/java/technology/tabula/CohenSutherlandClipping.java
 *  
 */

package de.unihalle.informatik.rhizoTrak.addon;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Polyline;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.gui.Roi;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Class defining polyline ROIs for individual layers.
 * <p>
 * ROIs can be used to restrict processing of a layer to a specific region or 
 * to restrict export of RSML. Each layer may have one or none ROI.
 * <p>
 * The ROI can be specified using the rectangle section of ImageJ and stored using the "set ROI" tab.
 * It is stored as a <code>polyline</code> into one rootstack object in the project tree.
 *
 * </p>
 */
public class RhizoROI {

	/**
	 * Reference to the rhizoTrak project.
	 */
	private RhizoMain rhizoMain;

	/**
	 * Original ImageJ Roi.
	 */
	private Roi roi;
	
	/**
	 * The trakEM polyline representing the ROI (or null)
	 */
	private Polyline polyline = null;

	/**
	 * Flag indicating if we have a rectangle or not.
	 */
	private boolean isRectangle = false;
	
	/**
	 * do we still have to check the project tree for polylines (after opening a project)?
	 */
	private boolean firstGetCurrentPolyline = true;

	/**
	 *
	 * @param rhizoMain
	 */
	protected RhizoROI(RhizoMain rhizoMain, Roi ijRoi, Polyline line, boolean rectangle) 	{
		this.rhizoMain = rhizoMain;
		this.roi = ijRoi;
		this.polyline = line;
		this.isRectangle = rectangle;
	}

	public Roi getRoi() {
		return this.roi;
	}
	
    /** get the current ROI polyline
     *
     * @return
     */
//    public Polyline getCurrentPolyline() {
//        if ( firstGetCurrentPolyline ) {
//            // we cannot get the polyline during opening the project and the  instantiation of the RhizoROI object,
//            // as the project tree is not yet constructed
//            // so need to be done this way
//            try {
//                HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks(rhizoMain.getProject());
//
//                for (ProjectThing rootstackThing : rootstackThings) {
//                    if ( ! firstGetCurrentPolyline ) break;
//
//                    for (ProjectThing pt : rootstackThing.findChildrenOfTypeR(Polyline.class)) {
//                        Polyline pl = (Polyline) pt.getObject();
//                        currentPolyline = pl;
//                        firstGetCurrentPolyline = false;
//                        break;
//                    }
//                }
//            } catch (Exception ex) {
//                System.out.println("error in getCurrentPolyline first of rhizoROI");
//            }
//            firstGetCurrentPolyline = false;
//        }
//
//        return currentPolyline;
//    }
}