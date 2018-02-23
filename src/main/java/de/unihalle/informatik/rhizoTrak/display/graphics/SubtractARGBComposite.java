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
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.unihalle.informatik.rhizoTrak.display.graphics;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import mpicbg.util.Util;

/**
 * 
 * @author Stephan Saalfeld saalfeld@mpi-cbg.de
 * @version 0.1b
 */
public class SubtractARGBComposite implements Composite
{
	static private interface Composer
	{
		public void compose( final int[] src, final int[] dst, final float alpha );
	}
	final static private class ARGB2ARGB implements Composer
	{
		final public void compose( final int[] src, final int[] dst, final float alpha )
		{
			final float srcAlpha = src[ 3 ] / 255.0f * alpha;
			
			dst[ 0 ] = Math.max( 0, Util.round( dst[ 0 ] - src[ 0 ] * srcAlpha ) );
			dst[ 1 ] = Math.max( 0, Util.round( dst[ 1 ] - src[ 1 ] * srcAlpha ) );
			dst[ 2 ] = Math.max( 0, Util.round( dst[ 2 ] - src[ 2 ] * srcAlpha ) );
			dst[ 3 ] = 255;
		}
	}
	final static private class RGB2ARGB implements Composer
	{
		final public void compose( final int[] src, final int[] dst, final float alpha )
		{
			dst[ 0 ] = Math.max( 0, Util.round( dst[ 0 ] - src[ 0 ] * alpha ) );
			dst[ 1 ] = Math.max( 0, Util.round( dst[ 1 ] - src[ 1 ] * alpha ) );
			dst[ 2 ] = Math.max( 0, Util.round( dst[ 2 ] - src[ 2 ] * alpha ) );
			dst[ 3 ] = 255;
		}
	}
	final static private class Gray2ARGB implements Composer
	{
		final public void compose( final int[] src, final int[] dst, final float alpha )
		{
			dst[ 0 ] = Math.max( 0, Util.round( dst[ 0 ] - src[ 0 ] * alpha ) );
			dst[ 1 ] = Math.max( 0, Util.round( dst[ 1 ] - src[ 0 ] * alpha ) );
			dst[ 2 ] = Math.max( 0, Util.round( dst[ 2 ] - src[ 0 ] * alpha ) );
			dst[ 3 ] = 255;
		}
	}
	
	static private SubtractARGBComposite instance = new SubtractARGBComposite();

	final private float alpha;

	public static SubtractARGBComposite getInstance( final float alpha )
	{
		if ( alpha == 1.0f ) { return instance; }
		return new SubtractARGBComposite( alpha );
	}

	private SubtractARGBComposite()
	{
		this.alpha = 1.0f;
	}

	private SubtractARGBComposite( final float alpha )
	{
		this.alpha = alpha;
	}

	public CompositeContext createContext( ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints )
	{
		final Composer c;
		if ( srcColorModel.getNumColorComponents() > 1 )
		{
			if ( srcColorModel.hasAlpha() )
				c = new ARGB2ARGB();
			else
				c = new RGB2ARGB();
		}
		else
			c = new Gray2ARGB();
		
		return new CompositeContext()
		{
			private Composer composer = c;
			public void compose( Raster src, Raster dstIn, WritableRaster dstOut )
			{
				final int[] srcPixel = new int[ 4 ];
				final int[] dstInPixel = new int[ 4 ];
				
				for ( int x = 0; x < dstOut.getWidth(); x++ )
				{
					for ( int y = 0; y < dstOut.getHeight(); y++ )
					{
						src.getPixel( x, y, srcPixel );
						dstIn.getPixel( x, y, dstInPixel );
						
						composer.compose( srcPixel, dstInPixel, alpha );
						
						dstOut.setPixel( x, y, dstInPixel );
					}
				}
			}

			public void dispose()
			{}
		};
	}
}
