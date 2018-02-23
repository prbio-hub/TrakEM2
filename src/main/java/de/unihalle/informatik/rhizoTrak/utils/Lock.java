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

package de.unihalle.informatik.rhizoTrak.utils;

/** For thread synchronization.
 * Both methods MUST ALWAYS be called from within a statement synchronizing on this object, such as:
 *  <pre>
 *  final Lock lock = new Lock();
 *  synchronized (lock) {
 *      lock.lock();
 *      try {
 *      	... (do whatever needs be synchronized on this lock) ...
 *      } catch (Exception e) {
 *      	e.printStackTrace();
 *      }
 *      lock.unlock();
 *  }
 * </pre>
 * <p>
 *  The advantage of using this class as opposed to a simple synchronized statement is that the lock may be set and unset from different synchronized blocks. For example:
 *  </p>
 *  <pre>
 *  final Lock lock = new Lock();
 *  synchronized (lock) {
 *      lock.lock();
 *      // Do something
 *  }
 *  // Exit synchronized block, wait for other events to happen
 *  
 *  // ... 
 *  // Enter again and unlock:
 *  synchronized (lock) {
 *      try {
 *      	... (do whatever needs be synchronized on this lock) ...
 *      } catch (Exception e) {
 *      	e.printStackTrace();
 *      }
 *      lock.unlock();
 *  }
 * </pre>
 */
public class Lock {
	protected boolean locked = false;
	protected boolean debug = false;
	static protected boolean debug_all = false;
	public final void lock() {
		if (debug || debug_all) Utils.printCaller(this, 7);
		while (locked) try { this.wait(); } catch (InterruptedException ie) {}
		locked = true;
	}
	public final void unlock() {
		if (debug || debug_all) Utils.printCaller(this, 7);
		locked = false;
		this.notifyAll();
	}
	public final void debug() {
		debug = true;
	}
	static public final void debugAll() {
		debug_all = true;
	}
}
