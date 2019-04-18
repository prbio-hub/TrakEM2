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
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.Polyline;
import de.unihalle.informatik.rhizoTrak.tree.DNDTree;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import static de.unihalle.informatik.rhizoTrak.display.Display.*;

public class RhizoROI {

	private RhizoMain rhizoMain;
	private Roi currentRoi = null;

	protected RhizoROI(RhizoMain rhizoMain) 	{
		this.rhizoMain = rhizoMain;
	}

    /** first version to create a ROI
     *
     * Get the current selection, if it is a rectangle, store it as a closed polyline
     */
	public void getROI() {
        Roi roi = Display.getFront().getRoi();

        if (roi.getType() != Roi.RECTANGLE) {
            System.out.println("RhizoRIOI.getROI: no rectangle selected");
            return;
        }
        currentRoi = roi;

        // get the relevant stuff
        Display display = Display.getFront();
        // TODO do not know it we want to do this
        Display.clearSelection();
        Project project = display.getProject();
        ProjectTree currentTree = project.getProjectTree();

        // find one rootstack
        // TODO: need to check it it is able to hold a polyline
        ProjectThing rootstackProjectThing = RhizoUtils.getOneRootstack(project);
        if ( rootstackProjectThing == null ) {
            Utils.showMessage( "RhizoRIOI.getROI: Create treeline: WARNING  can not find a rootstack in project tree");
            return;
        }

        // make new closed polyline
        try {
            ProjectThing pt = rootstackProjectThing.createChild("polyline");
            pt.setTitle(pt.getUniqueIdentifier());

            Polyline pl = (Polyline) pt.getObject();

            int n;
            for ( n = 0 ; n < roi.getPolygon().npoints ; n++ ) {
                pl.insertPoint( n, roi.getPolygon().xpoints[n], roi.getPolygon().ypoints[n], Display.getFront().getLayer().getId());
            }
            pl.insertPoint( n, roi.getPolygon().xpoints[0], roi.getPolygon().ypoints[0], Display.getFront().getLayer().getId());

            // add new polyline to the project tree
            DefaultMutableTreeNode parentNode = DNDTree.findNode(rootstackProjectThing, currentTree);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(pt);
            ((DefaultTreeModel) currentTree.getModel()).insertNodeInto(node, parentNode, parentNode.getChildCount());
        } catch ( Exception e ) {
            System.out.println( "Cannot create polyline");
        }

	}
}