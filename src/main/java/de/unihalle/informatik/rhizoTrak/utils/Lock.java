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
