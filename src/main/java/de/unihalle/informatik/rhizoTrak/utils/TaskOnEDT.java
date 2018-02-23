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

package de.unihalle.informatik.rhizoTrak.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskOnEDT<V> implements Future<V> {

	private V result;
	private Callable<V> fn;
	private AtomicBoolean started = new AtomicBoolean(false);
	
	/** The task @param fn should not be threaded; this class is intended to run
	 *  small snippets of code under the event dispatch thread, while still being
	 *  able to retrieve the result of the computation. */
	public TaskOnEDT(final Callable<V> fn) {
		this.fn = fn;
	}

	/** Will only prevent execution, not interrupt it if its happening.
	 *  @return true if the task didn't start yet. */
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (this) {
			fn = null;
			return !started.get();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get() throws InterruptedException, ExecutionException {
		if (null != result) return result;
		final V[] v = (V[])new Object[1];
		final ExecutionException[] ee = new ExecutionException[1];
		final AtomicBoolean launched = new AtomicBoolean(false);
		Utils.invokeLater(new Runnable() {
			public void run() {
				launched.set(true);
				synchronized (v) {
					final Callable<V> c;
					synchronized (TaskOnEDT.this) {
						c = fn;
						if (null == c) return;
						started.set(true);
					}
					try {
						v[0] = c.call();
					} catch (Throwable t) {
						ee[0] = new ExecutionException(t);
					}
				}
			}
		});
		// Wait until the event dispatch thread runs the Runnable.
		// (Or it gets run immediately if get() is called from within the event dispatch thread.)
		while (!launched.get()) try { Thread.sleep(5); } catch (InterruptedException ie) {}
		// Block until the computation is done
		synchronized (v) {
			if (null != ee[0]) throw ee[0];
			result = v[0];
			return result;
		}
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		new Thread() {
			{ setPriority(Thread.NORM_PRIORITY); }
			public void run() {
				try {
					result = get();
				} catch (Throwable t) {
					IJError.print(t);
				}
			}
		}.start();
		final long end = System.currentTimeMillis() + unit.toMillis(timeout);
		final long period = timeout < 200 ? timeout : 200;
		while (null == result && System.currentTimeMillis() < end) {
			Thread.sleep(period);
		}
		return result;
	}

	@Override
	public boolean isCancelled() {
		synchronized (this) {
			return null == fn;
		}
	}

	@Override
	public boolean isDone() {
		return null != result;
	}
}