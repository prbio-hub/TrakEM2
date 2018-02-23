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

package de.unihalle.informatik.rhizoTrak.parallel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.unihalle.informatik.rhizoTrak.utils.Utils;

/** For all methods, if the number of processors given as argument is zero or larger than the maximum available plus 2,
 *  the number of processors will be adjusted to fall within the range [1, max+2]. */
public class Process {

	static private final int MIN_AHEAD = 4;
	static public final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();

	static final int sensible(final int nproc) {
		return Math.max(1, Math.min(nproc, NUM_PROCESSORS + 2));
	}

	/** Takes a Collection of inputs, applies a function to each created by the generator,
	 *  and places their output in outputs in the same order as each input was retrieved from inputs. */
	static public final <I,O> void progressive(final Iterable<I> inputs, final TaskFactory<I,O> generator, final Collection<O> outputs) throws Exception {
		progressive(inputs, generator, outputs, NUM_PROCESSORS);
	}
	/** Takes a Collection of inputs, applies a function to each created by the generator,
	 *  and places their output in outputs in the same order as each input was retrieved from inputs. */
	static public final <I,O> void progressive(final Iterable<I> inputs, final TaskFactory<I,O> generator, final Collection<O> outputs, final int n_proc) throws Exception {
		process(inputs, generator, outputs, n_proc, true);
	}

	/** Takes a Collection of inputs, applies a function to each created by the generator,
	 *  and places their output in outputs in the same order as each input was retrieved from inputs;
	 *  will not wait for executing tasks to finish before creating and submitting new tasks. */
	static public final <I,O> void unbound(final Iterable<I> inputs, final TaskFactory<I,O> generator, final Collection<O> outputs, final int n_proc) throws Exception {
		process(inputs, generator, outputs, n_proc, false);
	}

	static private final <I,O> void process(final Iterable<I> inputs, final TaskFactory<I,O> generator, final Collection<O> outputs, final int n_proc, final boolean bound) throws Exception {
		final int nproc = sensible(n_proc);
		final ExecutorService exec = Utils.newFixedThreadPool(nproc, "Process." + (bound ? "progressive" : "unbound"));
		try {
			final LinkedList<Future<O>> fus = new LinkedList<Future<O>>();
			final int ahead = Math.max(nproc + nproc, MIN_AHEAD);
			for (final I input : inputs) {
				if (Thread.currentThread().isInterrupted()) {
					return;
				}
				fus.add(exec.submit(generator.create(input)));
				if (bound) while (fus.size() > ahead) {
					// wait
					outputs.add(fus.removeFirst().get());
				}
			}
			// wait for remaining, if any
			for (final Future<O> fu : fus) {
				if (null != fu) outputs.add(fu.get());
				else outputs.add(null);
			}
		} finally {
			exec.shutdown();
		}
	}

	/** Takes a Collection of inputs, applies a function to each created by the generator. */
	static public final <I,O> void progressive(final Iterable<I> inputs, final TaskFactory<I,O> generator) throws Exception {
		progressive(inputs, generator, NUM_PROCESSORS);
	}
	static public final <I,O> void progressive(final Iterable<I> inputs, final TaskFactory<I,O> generator, final int n_proc) throws Exception {
		process(inputs, generator, n_proc, true);
	}
	static public final <I,O> void unbound(final Iterable<I> inputs, final TaskFactory<I,O> generator) throws Exception {
		unbound(inputs, generator, NUM_PROCESSORS);
	}
	static public final <I,O> void unbound(final Iterable<I> inputs, final TaskFactory<I,O> generator, final int n_proc) throws Exception {
		process(inputs, generator, n_proc, false);
	}
	static private final <I,O> void process(final Iterable<I> inputs, final TaskFactory<I,O> generator, final int n_proc, final boolean bound) throws Exception {
		final int nproc = sensible(n_proc);
		final ExecutorService exec = Utils.newFixedThreadPool(nproc, "Process." + (bound ? "progressive" : "unbound"));
		try {
			final LinkedList<Future<O>> fus = new LinkedList<Future<O>>();
			final int ahead = Math.max(nproc + nproc, MIN_AHEAD);
			for (final I input : inputs) {
				if (Thread.currentThread().isInterrupted()) {
					return;
				}
				fus.add(exec.submit(generator.create(input)));
				if (bound) while (fus.size() > ahead) {
					fus.removeFirst().get();
				}
			}
			for (final Future<O> fu : fus) if (null != fu) fu.get();
		} finally {
			exec.shutdown();
		}
	}
}
