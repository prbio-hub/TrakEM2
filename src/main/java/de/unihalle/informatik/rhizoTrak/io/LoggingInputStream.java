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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;


/** A class to monitor an input stream for speed and total byte download. */
public class LoggingInputStream extends BufferedInputStream {

	private long last;
	private long n = 0;
	private long accum_time = 0;
	private long accum_bytes = 0;

	public LoggingInputStream(InputStream in) {
		super(in);
		last = System.currentTimeMillis();
	}

	public int read() throws IOException {
		int m = super.read();
		n += m;
		return m;
	}

	public int read(byte[] b) throws IOException {
		int m = super.read(b);
		n += m;
		return m;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int m = super.read(b, off, len);
		n += m;
		return m;
	}

	/** Put the counter to zero. */
	public void resetInfo() { // to work perfect, this would need a synchronized clause, but no such perfection is needed, and there are perfomance issues.
		accum_bytes = n = 0;
		last = System.currentTimeMillis();
		accum_time = 0;
	}

	/** Returns info as
	* [0] = current time in ms
	* [1] = elapsed time in ms since last call to getInfo(long[])
	* [2] = n_bytes_read since last call to getInfo(long[])
	* [3] = accumulated time in ms since last call to resetInfo()
	* [4] = accumulated bytes since last call to resetInfo()
	* 
	* So current speed = info[2]/info[1] Kb/s
	*/
	public void getInfo(long[] info) {
		long now = System.currentTimeMillis();
		accum_time += now - last;
		accum_bytes += n;
		info[0] = now;
		info[1] = now - last; // elapsed time
		info[2] = n;
		info[3] = accum_time; // total time since last call to resetInfo()
		info[4] = accum_bytes; // total bytes since last call to resetInfo()
		// reset cycle vars:
		n = 0;
		last = now;
	}
}
