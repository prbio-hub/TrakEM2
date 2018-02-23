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

package de.unihalle.informatik.rhizoTrak.analysis;

import ij.gui.GenericDialog;
import ij.text.TextWindow;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import de.unihalle.informatik.rhizoTrak.display.AreaList;
import de.unihalle.informatik.rhizoTrak.display.AreaTree;
import de.unihalle.informatik.rhizoTrak.display.Ball;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.DLabel;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.Pipe;
import de.unihalle.informatik.rhizoTrak.display.Polyline;
import de.unihalle.informatik.rhizoTrak.display.Profile;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.display.ZDisplayable;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class Graph {


	static public final <T extends Displayable> Map<String,StringBuilder> extractGraph(final LayerSet ls, final Set<Class<T>> only) {
		
		final StringBuilder sif = new StringBuilder(4096),
							xml = new StringBuilder(4096).append("<graph>\n"),
							names = new StringBuilder(4096);
		
		final Set<Displayable> seen = new HashSet<Displayable>();

		for (final Connector con : ls.getAll(Connector.class)) {
			Set<Displayable> origins = con.getOrigins();
			if (origins.isEmpty()) {
				Utils.log("Graph: ignoring connector without origins: #" + con.getId());
				continue;
			}
			List<Set<Displayable>> target_lists = con.getTargets();
			if (target_lists.isEmpty()) {
				Utils.log("Graph: ignoring connector without targets: #" + con.getId());
				continue;
			}
			for (final Displayable origin : origins) {
				if (Thread.currentThread().isInterrupted()) return null;
				if (null != only && !only.contains(origin.getClass())) continue;
				seen.add(origin);
				for (final Set<Displayable> targets : target_lists) {
					for (final Displayable target : targets) {
						if (null != only && !only.contains(target.getClass())) continue;
						sif.append(origin.getId()).append(" pd ").append(target.getId()).append('\n');
						xml.append('\t').append("<edge cid=\"").append(con.getId()).append("\" origin=\"").append(origin.getId()).append("\" target=\"").append(target.getId()).append("\" />\n");
						seen.add(target);
					}
				}
			}
		}

		xml.append("</graph>\n");

		for (final Displayable d : seen) {
			names.append(d.getId()).append('\t').append(d.getProject().getMeaningfulTitle(d)).append('\n');
		}

		final Map<String,StringBuilder> m = new HashMap<String,StringBuilder>();
		m.put("sif", sif);
		m.put("xml", xml);
		m.put("names", names);
		return m;
	}

	/** Extract the graph based on connectors; leave @param only null to include all types. */
	static public final <T extends Displayable> void extractAndShowGraph(final LayerSet ls, final Set<Class<T>> only) {
		final Map<String,StringBuilder> m = Graph.extractGraph(ls, only);
		if (null == m) return;
		SwingUtilities.invokeLater(new Runnable() { public void run() {
			new TextWindow("Graph", m.get("xml").toString(), 500, 500);
			TextWindow tw = new TextWindow("SIF", m.get("sif").toString(), 500, 500);
			Point p = tw.getLocation();
			tw.setLocation(p.x + 50, p.y + 50);
			tw = new TextWindow("Names", m.get("names").toString(), 500, 500);
			tw.setLocation(p.x + 100, p.y + 100);
		}});
	}

	/** Shows a dialog to pick which classes is one interested in. */
	static public final void extractAndShowGraph(final LayerSet ls) {
		GenericDialog gd = new GenericDialog("Graph elements");
		Class<Displayable>[] c = new Class[]{AreaList.class, AreaTree.class, Ball.class, Connector.class, Patch.class, Pipe.class, Polyline.class, Profile.class, DLabel.class, Treeline.class};
		String[] types = new String[]{"AreaList", "AreaTree", "Ball", "Connector", "Image", "Pipe", "Polyline", "Profile", "Text", "Treeline"};
		boolean[] states = new boolean[]{true, true, false, false, false, false, true, true, false, true};
		assert(c.length == types.length && types.length == states.length);
		for (int i=0; i<c.length; i++) {
			if (ZDisplayable.class.isAssignableFrom(c[i])) {
				if (!ls.contains(c[i])) states[i] = false;
			} else if (!ls.containsDisplayable(c[i])) states[i] = false;
		}
		gd.addCheckboxGroup(types.length, 1, types, states, new String[]{"Include only:"});
		gd.showDialog();
		if (gd.wasCanceled()) return;
		HashSet<Class<Displayable>> only = new HashSet<Class<Displayable>>();
		for (int i=0; i<types.length; i++) {
			if (gd.getNextBoolean()) only.add(c[i]);
		}
		Graph.extractAndShowGraph(ls, only);
	}
}