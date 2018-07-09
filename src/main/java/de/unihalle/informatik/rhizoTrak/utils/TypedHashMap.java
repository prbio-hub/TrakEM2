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

import java.util.HashMap;
import java.util.Map;

/**
 * An extended {@link HashMap} that throws {@link UnsupportedOperationException}
 * for calls to {@link HashMap#get(Object)}, {@link HashMap#remove(Object)},
 * {@link HashMap#containsKey(Object)} and {@link HashMap#containsValue(Object)}.
 * This class offers versions of these four methods but requiring typed arguments.
 * 
 * @author Albert Cardona
 *
 * @param <K>
 * @param <V>
 */
public class TypedHashMap<K,V> extends HashMap<K,V>
{
	private static final long serialVersionUID = -7817318751687157665L;
	
	public TypedHashMap() {
		super();
	}
	
	public TypedHashMap(final int initialCapacity) {
		super(initialCapacity);
	}
	
	public TypedHashMap(final Map<? extends K, ? extends V> map) {
		super(map);
	}
	
	public TypedHashMap(final int initialCapacity, final float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/** Typed version of {@link HashMap#get(Object)}. */
	public V getValue(final K key) {
		return super.get(key);
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public V get(final Object key) {
		throw new UnsupportedOperationException();
	}

	/** Typed version of {@link HashMap#remove(Object)}. */
	public V removeEntry(final K key) {
		return super.remove(key);
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public V remove(final Object key) {
		throw new UnsupportedOperationException();
	}
	
	/** Typed version of {@link HashMap#containsKey(Object)}. */
	public boolean hasKey(final K key) {
		return super.containsKey(key);
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean containsKey(final Object key) {
		throw new UnsupportedOperationException();
	}
	
	/** Typed version of {@link HashMap#containsValue(Object)}. */
	public boolean hasValue(final V value) {
		return super.containsValue(value);
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean containsValue(final Object value) {
		throw new UnsupportedOperationException();
	}
}
