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

package de.unihalle.informatik.rhizoTrak.io;

import amira.AmiraMeshDecoder;
import amira.AmiraParameters;
import de.unihalle.informatik.rhizoTrak.display.AreaList;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.YesNoDialog;
import de.unihalle.informatik.rhizoTrak.utils.AreaUtils;
import de.unihalle.informatik.rhizoTrak.utils.IJError;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import mpi.fruitfly.general.MultiThreading;

/** Parses an amira labelfield and imports the labels as AreaList instances into the project tree.*/
public class AmiraImporter {

	/** Returns the array of AreaList or null if the file dialog is canceled. The xo,yo is the pivot of reference. */
	static public Collection<AreaList> importAmiraLabels(Layer first_layer, double xo, double yo, final String default_dir) {
		// open file
		OpenDialog od = new OpenDialog("Choose Amira Labels File", default_dir, "");
		String filename = od.getFileName();
		if (null == filename || 0 == filename.length()) return null;
		String dir = od.getDirectory();
		if (IJ.isWindows()) dir = dir.replace('\\', '/');
		if (!dir.endsWith("/")) dir += "/";
		String path =  dir + filename;
		AmiraMeshDecoder dec = new AmiraMeshDecoder();
		if (!dec.open(path)) {
			YesNoDialog yn = new YesNoDialog("Error", "File was not an Amira labels file.\nChoose another one?");
			if (yn.yesPressed()) return importAmiraLabels(first_layer, xo, yo, default_dir);
			return null;
		}
		ImagePlus imp = null;
		if (dec.isTable()) {
			Utils.showMessage("Select the other file (the labels)!");
			return null;
		} else {
			FileInfo fi = new FileInfo();
			fi.fileName = filename;
			fi.directory = dir;
			imp = new ImagePlus("Amira", dec.getStack());
			dec.parameters.setParameters(imp);
		}
		return extractAmiraLabels(imp, dec.parameters, first_layer, xo, yo);
	}

	/** Returns an ArrayList containing all AreaList objects. The xo,yo is the pivot of reference. */
	static public Collection<AreaList> extractAmiraLabels(final ImagePlus labels, final AmiraParameters ap, final Layer first_layer, final double xo, final double yo) {
		final String[] materials = ap.getMaterialList();
		// extract labels as ArrayList of Area

		final Map<Float,AreaList> alis = extractAreaLists(labels, first_layer, xo, yo, 0.4f, false);

		for (int i=0; i<materials.length; i++) {
			final int id = ap.getMaterialID(materials[i]);
			final double[] color = ap.getMaterialColor(id);
			final String name = ap.getMaterialName(id);
			if (name.equals("Exterior")) {
				Utils.log("Ignoring Amira's \"Exterior\" label");
				continue;
			}
			final AreaList ali = alis.get(new Float(id));
			if (null == ali) {
				Utils.log("ERROR: no AreaList for label id " + id);
				continue;
			}
			ali.setColor(new Color((float)color[0], (float)color[1], (float)color[2]));
			ali.setTitle(name);
		}
		return alis.values();
	}

	/** Returns a map of label vs AreaList. */
	static public Map<Float,AreaList> extractAreaLists(final ImagePlus imp, final Layer first_layer, final double base_x, final double base_y, final float alpha, final boolean add_background) {

		try {
			final HashMap<Integer,HashMap<Float,Area>> map = new HashMap<Integer,HashMap<Float,Area>>();
			final ImageStack stack = imp.getStack(); // works even for images that are not stacks: it creates one

			final AtomicInteger ai = new AtomicInteger(1);
			final AtomicInteger completed_slices = new AtomicInteger(0);
			final int n_slices = imp.getNSlices();

			final Thread parent = Thread.currentThread();

			final Thread[] threads = MultiThreading.newThreads();
			for (int ithread = 0; ithread < threads.length; ithread++) {
				threads[ithread] = new Thread() {
					public void run() {

						final Rectangle box = new Rectangle(0, 0, 1, 1);
						for (int i = ai.getAndIncrement(); i <= n_slices; i = ai.getAndIncrement()) {
							final ImageProcessor ip;
							synchronized (map) {
								ip = stack.getProcessor(i);
							}
							if (parent.isInterrupted()) return;
							final HashMap<Float,Area> layer_map = new HashMap<Float,Area>();
							synchronized (map) {
								map.put(i, layer_map);
							}

							AreaUtils.extractAreas(ip, layer_map, add_background, box, parent, true);

							Utils.showProgress(completed_slices.incrementAndGet() / (float)n_slices);
						}
					}
				};
			}

			MultiThreading.startAndJoin(threads);
			Utils.showProgress(1);

			if (parent.isInterrupted()) return null;

			final HashMap<Float,AreaList> alis = new HashMap<Float,AreaList>();


			Utils.log2("Recreating arealists...");

			Utils.log2("map.size() = " + map.size());

			final double thickness = first_layer.getThickness();
			final double first_z = first_layer.getZ();

			// Recreate AreaLists
			for (final Map.Entry<Integer,HashMap<Float,Area>> e : map.entrySet()) {
				final int slice_index = e.getKey();
				final HashMap<Float,Area> layer_map = e.getValue();

				for (final Map.Entry<Float,Area> fa : layer_map.entrySet()) {
					Float label = fa.getKey();
					AreaList ali = alis.get(label);
					if (null == ali) {
						ali = new AreaList(first_layer.getProject(), "Label " + label.intValue(), base_x, base_y);
						alis.put(label, ali);
					}
					double z = first_z + (slice_index-1) * thickness;
					Layer layer = first_layer.getParent().getLayer(z, thickness, true);
					ali.setArea(layer.getId(), fa.getValue());
				}
			}

			Utils.log2("Done recreating.");

			first_layer.getParent().addAll(alis.values());

			Utils.log2("Done adding all to LayerSet");

			float hue = 0;

			for (final Map.Entry<Float,AreaList> e : alis.entrySet()) {
				final AreaList ali = e.getValue();
				ali.setProperty("label", Integer.toString(e.getKey().intValue()));
				ali.calculateBoundingBox(null);
				ali.setColor(Color.getHSBColor(hue, 1, 1));
				ali.setAlpha(alpha);
				hue += 0.38197f; // golden angle
				if (hue > 1) hue = hue - 1;
			}

			Utils.log2("Done setting properties");

			return alis;

		} catch (Exception e) {
			IJError.print(e);
		}

		return null;
	}
}
