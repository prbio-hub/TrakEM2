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

package de.unihalle.informatik.rhizoTrak.display;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.persistence.XMLOptions;
import de.unihalle.informatik.rhizoTrak.utils.Search;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

/** For Pipes and other objects that must be shown in all Layers of a LayerSet. */

public abstract class ZDisplayable extends Displayable {

	protected LayerSet layer_set;

	public ZDisplayable(Project project, String title, double x, double y) {
		super(project, title, x, y);
	}

	/** For reconstruction from the database. */
	public ZDisplayable(Project project, long id, String title, boolean locked, AffineTransform at, float width, float height) {
		super(project, id, title, locked, at, width, height);
	}

	/** For reconstruction from an XML file. */
	public ZDisplayable(Project project, long id, HashMap<String,String> ht, HashMap<Displayable,String> ht_links) {
		super(project, id, ht, ht_links);
		Object data = ht.get("layer_set_id");
		if (null != data) {
			// construct a dummy layerset
			this.layer_set = new LayerSet(project, Long.parseLong((String)data));
			// TODO fix this hack
			//  -- one year later: seems like it will stay.
		}
	}

	public void setLayerSet(LayerSet layer_set) {
		setLayerSet(layer_set, true); // should check that the new LayerSet belongs to the same project
	}

	public void setLayerSet(LayerSet layer_set, boolean update_db) {
		if (layer_set == this.layer_set) return;
		this.layer_set = layer_set;
		if (update_db) updateInDatabase("layer_set_id");
	}

	public LayerSet getLayerSet() { return layer_set; }

	// Overriding, for repainting the proper part, without updating the database
	public void setLayer(Layer layer) { this.layer = layer; }

	/** Link the Patch objects that lay underneath the part of the bounding box of this profile that shows in the current layer, so that they cannot be dragged independently. */
	abstract public boolean linkPatches();

	/** Returns the layer of lowest Z coordinate where this ZDisplayable has a point in. */
	abstract public Layer getFirstLayer();

	public void exportXML(StringBuilder sb_body, String indent, XMLOptions options) {
		super.exportXML(sb_body, indent, options);
		sb_body.append(indent).append("layer_set_id=\"").append(layer_set.getId()).append("\"\n");
	}
	static public void exportDTD(final String type, final StringBuilder sb_header, final HashSet<String> hs, final String indent) {
		if (hs.contains(type)) return;
		Displayable.exportDTD(type, sb_header, hs, indent);
		sb_header.append(indent).append(TAG_ATTR1).append(type).append(" layer_set_id").append(TAG_ATTR2)
		;
	}

	/** Transform points falling within the given layer; translate by dx,dy and rotate by rot relative to origin xo,yo*/
	@Deprecated
	public void transformPoints(Layer layer, double dx, double dy, double rot, double xo, double yo) {}

	@Override
	protected boolean remove2(boolean check) {
		return project.getProjectTree().remove(check, project.findProjectThing(this), null); // will call remove(check) here
	}

	@Override
	public boolean remove(boolean check) {
		if (check && !Utils.check("Really remove " + this.toString() + " ?")) return false;
		if (layer_set.remove(this) && removeFromDatabase()) {
			unlink();
			removeLinkedPropertiesFromOrigins();
			Search.remove(this); // duplication of code from Displayable.remove, because there isn't a proper hierarchy of classes
			Display.flush(this);
			project.decache(this);
			return true;
		}
		return false;
	}
	
	/** Does not remove from the LayerSet. */
	@Override
	public boolean softRemove() {
		if (removeFromDatabase()) {
			unlink();
			removeLinkedPropertiesFromOrigins();
			Search.remove(this); // duplication of code from Displayable.remove, because there isn't a proper hierarchy of classes
			Display.flush(this);
			return true;
		}
		return false;
	}

	/** Check if this instance will paint anything at the level of the given Layer. */
	public boolean paintsAt(final Layer layer) {
		if (null == layer || layer_set != layer.getParent()) return false;
		return true;
	}

	/** Get the list of Layer ids on which this ZDisplayable has data on.*/
	@Override
	public Collection<Long> getLayerIds() {
		// Unless overriden, return all
		final ArrayList<Long> l = new ArrayList<Long>();
		for (final Layer la : layer_set.getLayers()) l.add(la.getId());
		return l;
	}

	public void setColor(Color color) {
		if (null == color || color.equals(this.color)) return;
		this.color = color;
		Displayable.last_color = color;
		updateInDatabase("color");
		Display.repaint(layer_set, this, 5);
		Display3D.setColor(this, color);
	}

	abstract public boolean intersects(Area area, double z_first, double z_last);

	public void setVisible(final boolean visible, final boolean repaint) {
		if (visible == this.visible) {
			// patching synch error
			//Display.updateVisibilityCheckbox(layer, this, null);
			return;
		}
		this.visible = visible;
		if (repaint) {
			Display.repaint(layer_set, null, getBoundingBox(), 5, true);
		}
		updateInDatabase("visible");
	}

	public Bucketable getBucketable() {
		return this.layer_set;
	}

	/** Retain the data within the layer range, and through out all the rest. */
	public boolean crop(List<Layer> range) {
		return true;
	}

	/** Update internal datastructures to reflect the fact that @param layer has been removed from the containing LayerSet.*/
	protected boolean layerRemoved(Layer la) {
		if (null != hs_linked) {
			for (final Displayable d : hs_linked) if (d.layer == la) unlink(d);
		}
		return true;
	}

	public void updateBucket(final Layer la) {
		if (null == la) updateBucket(); // for all layers
		else if (null != getBucketable()) getBucketable().updateBucket(this, la);
	}

	/** Update buckets for all Layers. */
	@Override
	public void updateBucket() {
		if (null == getBucketable()) return;
		for (final Layer layer : getLayersWithData()) {
			getBucketable().updateBucket(this, layer);
		}
	}
	
	abstract protected boolean calculateBoundingBox(final Layer la);
}
