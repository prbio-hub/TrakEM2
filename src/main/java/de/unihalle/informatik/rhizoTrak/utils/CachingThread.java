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

/* === original file header below (if any) === */

package de.unihalle.informatik.rhizoTrak.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;

public class CachingThread extends Thread
{
	static private final int SIZE = 10;
	
	static private final class ArrayCache<A> extends HashMap<Integer, LinkedList<SoftReference<A>>>
	{
		private static final long serialVersionUID = 1L;
		private final Class<A> clazz;
		private int count = 0;
		
		private ArrayCache(final Class<A> clazz) {
			this.clazz = clazz;
		}

		private final A getOrCreateArray(final int length) {
			final LinkedList<SoftReference<A>> l = this.get(length);
			if (null == l) return newArray(length);
			if (l.isEmpty()) {
				this.remove(length);
				return newArray(length);
			}
			// Else:
			A a;
			do {
				a = l.removeFirst().get();
				--count;
			} while (null == a && !l.isEmpty());
			return null == a ? newArray(length) : a;
		}

		@SuppressWarnings("unchecked")
		private final A newArray(final int length) {
			return (A) Array.newInstance(clazz.getComponentType(), length);
		}

		private final void storeForReuse(final A a, final int length) {
			LinkedList<SoftReference<A>> l = this.get(length);
			if (null == l) {
				l = new LinkedList<SoftReference<A>>();
				this.put(length, l);
			}
			l.add(new SoftReference<A>(a));
			++count;
			// Clean up
			if (count > SIZE) {
				restructure();
			}
		}

		@SuppressWarnings("unchecked")
		synchronized private final void restructure() {
			count = 0;
			final LinkedList<SoftReference<A>>[] ls = this.values().toArray(new LinkedList[this.size()]);
			for (final LinkedList<SoftReference<A>> l : ls) {
				final SoftReference<A>[] s = l.toArray(new SoftReference[l.size()]);
				// Remove stale references and crop to maximum SIZE
				l.clear();
				for (int i=0, c=count; i < s.length && c < SIZE; ++i) {
					if (null == s[i].get()) continue; // stale reference
					// Re-add good reference
					l.add(s[i]);
					++c;
				}
				// Update
				count += l.size();
			}
		}
	}


	private final ArrayCache<byte[]> cacheBytes = new ArrayCache<byte[]>(byte[].class);
	private final ArrayCache<int[]> cacheInts = new ArrayCache<int[]>(int[].class);	

	public void clear() {
		synchronized (cacheBytes) { cacheBytes.clear(); }
		synchronized (cacheInts) { cacheInts.clear(); }
	}
	
	public CachingThread() { super(); }
	public CachingThread(final Runnable r) { super(r); }
	public CachingThread(final String name) { super(name); }
	public CachingThread(final Runnable r, final String name) { super(r, name); }
	public CachingThread(final ThreadGroup tg, final Runnable r) { super(tg, r); }
	public CachingThread(final ThreadGroup tg, final String name) { super(tg, name); }
	public CachingThread(final ThreadGroup tg, final Runnable r, final String name) { super(tg, r, name); }
	public CachingThread(final ThreadGroup tg, final Runnable r, final String name, final long priority) { super(tg, r, name, priority); }

	public static final byte[][] getOrCreateByteArray(final int num, final int length) {
		final Thread t = Thread.currentThread();
		if (CachingThread.class.isAssignableFrom(t.getClass())) {
			final CachingThread c = (CachingThread) t;
			final byte[][] b = new byte[num][];
			for (int i=0; i<num; ++i) b[i] = c.cacheBytes.getOrCreateArray(length);
			return b;
		}
		return new byte[num][length];
	}
	
	public static final byte[] getOrCreateByteArray(final int length) {
		final Thread t = Thread.currentThread();
		if (CachingThread.class.isAssignableFrom(t.getClass())) {
			final CachingThread c = (CachingThread) t;
			Object o = c.cacheBytes.getOrCreateArray(length);
			System.out.println("instance of: " + o.getClass());
			return (byte[]) o;
		}
		return new byte[length];
	}

	public static final int[] getOrCreateIntArray(final int length) {
		final Thread t = Thread.currentThread();
		if (CachingThread.class.isAssignableFrom(t.getClass())) {
			final CachingThread c = (CachingThread) t;
			return c.cacheInts.getOrCreateArray(length);
		}
		return new int[length];
	}

	public static final void storeForReuse(final byte[][] b) {
		final Thread t = Thread.currentThread();
		if (CachingThread.class.isAssignableFrom(t.getClass())) {
			final CachingThread c = (CachingThread) t;
			for (int i=0; i<b.length; ++i) c.cacheBytes.storeForReuse(b[i], b[i].length);
		}
	}
	
	public static final void storeForReuse(final byte[] b) {
		final Thread t = Thread.currentThread();
		if (CachingThread.class.isAssignableFrom(t.getClass())) {
			final CachingThread c = (CachingThread) t;
			c.cacheBytes.storeForReuse(b, b.length);
		}
	}
	
	public static final void storeForReuse(final int[] b) {
		final Thread t = Thread.currentThread();
		if (CachingThread.class.isAssignableFrom(t.getClass())) {
			final CachingThread c = (CachingThread) t;
			c.cacheInts.storeForReuse(b, b.length);
		}
	}

	public static final void storeArrayForReuse(final Image img) {
		if (img.getClass() == BufferedImage.class) {
			final DataBuffer db = ((BufferedImage)img).getData().getDataBuffer();
			if (db.getClass() == DataBufferInt.class) {
				CachingThread.storeForReuse(((DataBufferInt)db).getData());
			} else if (db.getClass() == DataBufferByte.class) {
				CachingThread.storeForReuse(((DataBufferByte)db).getData());
			}
		}
	}
	
	/** Tell all instances to clear their caches. */
	public static final void releaseAll() {
		// Find the top-most parent Thread
		ThreadGroup parent = Thread.currentThread().getThreadGroup();
		while (true) {
		    ThreadGroup p = parent.getParent();
		    if (null == p) break;
		    parent = p;
		}
		// Collect all live Thread instances
		Thread[] ts = new Thread[parent.activeCount()];
		while (parent.enumerate(ts, true) == ts.length) {
		    ts = new Thread[ ts.length * 2 ];
		}
		// For each Thread, if it's a CachingThread, clear its contents
		for (Thread t : ts) {
			if (null == t) continue;
			if (CachingThread.class.isAssignableFrom(t.getClass())) {
				((CachingThread)t).clear();
			}
		}
	}
}
