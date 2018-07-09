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
 * FROM : package org.freehep.graphics3d;
 *
 * Keeps a 3 dimensional vector.
 * <p>
 * Original ArcBall C code from Ken Shoemake, Graphics Gems IV, 1993.
 *
 * @author Mark Donszelmann
 * @version $Id: Vector3.java,v 1.2 2002/06/12 23:15:15 duns Exp $
 */



package de.unihalle.informatik.rhizoTrak.utils;

public class Vector3 {

	public double x, y, z;

	/**
	 * creates vector from x,y,z
	 */
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * creates null vector
	 */
	public Vector3() {
		this(0,0,0);
	}

	/**
	 * creates vector from v
	 */
	public Vector3(Vector3 v) {
		this(v.x, v.y, v.z);
	}

	/**
	 * @return string representation of vector
	 */

	/*public String toString() {
		return "["+x+", "+y+", "+z+"]";
}*/

	/**
	 * @return length
	 */
	public double length() {
		return Math.sqrt(x*x + y*y + z*z);
	}

	/**
	 * @return normal vector , or null if length is 0
	 */


	public Vector3 normalize(Vector3 r) {
		if (r == null) r = new Vector3();
		double vlen = length();
		if (vlen != 0.0) {
			return r.set(x/vlen, y/vlen, z/vlen);
		}
		return null;
	}


	/**
	 * @return vector scaled by s
	 */
	public Vector3 scale(double s, Vector3 r) {
		if (r == null) r = new Vector3();
		return r.set(s*x, s*y, s*z);
	}

	/**
	 * @return difference between vector and s
	 */
	/*public Vector3 sub(Vector3 s, Vector3 r) {
		if (r == null) r = new Vector3();
		return r.set(x - s.x, y - s.y, z - s.z);
}*/

	/**
	 * @return sum of vector and v
	 */
	public Vector3 add(Vector3 v, Vector3 r) {
		if (r == null) r = new Vector3();
		return r.set(x + v.x, y + v.y, z + v.z);
	}

	/**
	 * @return the negation of vector
	 */
	/*public Vector3 negate(Vector3 r) {
		if (r == null) r = new Vector3();
		return r.set(-x, -y, -z);
}*/

	/**
	 * @return dot product of vector and v
	 */
	/*public double dot(Vector3 v) {
		return x*v.x + y*v.y + z*v.z;
	*/


	/**
	 * @return cross produc of vector x v
	 */
	/*public Vector3 cross(Vector3 v, Vector3 r) {
		if (r == null) r = new Vector3();
		return r.set(y*v.z-z*v.y, z*v.x-x*v.z, x*v.y-y*v.x);
}*/

	/**
	 * @return half arc between vector and v
	 */
	/*public Vector3 bisect(Vector3 v, Vector3 r) {
		if (r == null) r = new Vector3();
		add(v, r);
		double length = r.length();
		return (length < 1.0e-7) ? r.set(0, 0, 1) : r.scale(1/length, r);
}*/

	/**
	 * @return the vector set to x,y,z
	 */
	public Vector3 set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/** From my former program, A_3D_Editing.java and Pipe.java */
	static public final Vector3 rotate_v_around_axis(final Vector3 v, final Vector3 axis, final double sin, final double cos) {

		final Vector3 result = new Vector3();
		final Vector3 r = axis.normalize(axis);

		result.set((cos + (1-cos) * r.x * r.x) * v.x + ((1-cos) * r.x * r.y - r.z * sin) * v.y + ((1-cos) * r.x * r.z + r.y * sin) * v.z,
		           ((1-cos) * r.x * r.y + r.z * sin) * v.x + (cos + (1-cos) * r.y * r.y) * v.y + ((1-cos) * r.y * r.z - r.x * sin) * v.z,
		           ((1-cos) * r.y * r.z - r.y * sin) * v.x + ((1-cos) * r.y * r.z + r.x * sin) * v.y + (cos + (1-cos) * r.z * r.z) * v.z);

		/*
		result.x += (cos + (1-cos) * r.x * r.x) * v.x;
		result.x += ((1-cos) * r.x * r.y - r.z * sin) * v.y;
		result.x += ((1-cos) * r.x * r.z + r.y * sin) * v.z;

		result.y += ((1-cos) * r.x * r.y + r.z * sin) * v.x;
		result.y += (cos + (1-cos) * r.y * r.y) * v.y;
		result.y += ((1-cos) * r.y * r.z - r.x * sin) * v.z;

		result.z += ((1-cos) * r.y * r.z - r.y * sin) * v.x;
		result.z += ((1-cos) * r.y * r.z + r.x * sin) * v.y;
		result.z += (cos + (1-cos) * r.z * r.z) * v.z;
		*/
		return result;
	}


}  // End of class Vector3
