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
as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt)

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

import ij.IJ;

import java.util.HashMap;

import de.unihalle.informatik.rhizoTrak.ControlWindow;


public abstract class Worker implements Runnable {
	private String thread_name;
	private String task_name;
	private Thread thread;
	private volatile boolean working = false;
	protected volatile boolean quit = false;
	private volatile boolean started = false;
	private volatile boolean background = false;
	private boolean interrupt_on_quit = false;
	/** Extending classes may store a resulting piece of data. */
	protected Object result = null;

	public Worker(String task_name) {
		this(task_name, !ControlWindow.isGUIEnabled() || null == IJ.getInstance());
	}
	public Worker(String task_name, boolean headless_mode) {
		super(); // the Run$_ tag is for ImageJ to properly grant it Macro.getOptions()
		this.thread_name = (headless_mode ? "Run$_": "") +  "Worker";
		this.task_name = task_name;
	}
	public Worker(String task_name, boolean headless_mode, boolean interrupt_on_quit) {
		this(task_name, headless_mode);
		this.interrupt_on_quit = interrupt_on_quit;
	}
	// private to the package
	void setThread(Thread t) {
		this.thread = t;
	}
	public void setTaskName(String name) { this.task_name = name; }
	protected void startedWorking() {
		this.working = true;
		this.started = true;
	}
	public boolean hasStarted() { return started; }
	protected void finishedWorking() { this.working = false; this.quit = true; }
	public boolean isWorking() { return working; }
	public String getTaskName() { return task_name; }
	public String getThreadName() { return thread_name; }
	public void setPriority(int priority) {
		if (null != this.thread) thread.setPriority(priority);
	}
	/** If interrupt_on_quit, then it will call thread.getThreadGroup().interrupt() to set a quit flag to each child thread. */
	public void quit() {
		this.quit = true;
		if (interrupt_on_quit) {
			if (null != thread) thread.getThreadGroup().interrupt();
		}
	}
	public void join() throws InterruptedException {
		if (null != thread) thread.join();
	}
	public boolean hasQuitted() { return this.quit; }
	protected void setAsBackground(boolean b) { this.background = b; }
	/** Whether the work is done on the background, without need to bring ImageJ toolbar to front for instance. */
	public boolean onBackground() { return this.background; }

	private boolean cleaned_up = false;
	// private to the package
	void cleanup2() {
		synchronized (this) {
			if (cleaned_up) return;
			cleaned_up = true;
		}
		cleanup();
	}
	/** When quitted or interrupted, executes this method once. */
	public void cleanup() {}

	/** Returns data generated by the worker, or null if none was set. */
	public Object getResult() { return this.result; }

	// ugly, ugly ... why, java, do you make me do this, when all I need is a closure?
	private HashMap<Object,Object> properties = null;
	public synchronized void setProperty(Object key, Object value) {
		if (null == key) return;
		if (null == properties) properties = new HashMap<Object,Object>();
		properties.put(key, value);
	}
	public synchronized Object getProperty(Object key) {
		if (null == key || null == properties) return null;
		return properties.get(key);
	}

	/** A class that calls run() wrapped properly for task monitoring;
	 *  Create it like this:
	 *
	 *  Bureaucrat b = Bureaucrat.createAndStart(new Worker.Task("Title") { public void exec() {
	 *      doSomething();
	 *      doSomethingElse();
	 *  }}, project);
	 *
	 */
	static public abstract class Task extends Worker {
		public Task(String title) {
			super(title);
		}
		public Task(String title, boolean interrupt_on_quit) {
			this(title);
			super.interrupt_on_quit = interrupt_on_quit;
		}

		abstract public void exec();

		public void run() {
			try {
				startedWorking();
				exec();
			} catch (Throwable t) {
				IJError.print(t);
			} finally {
				finishedWorking();
			}
		}
	}
}
