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
 *
 */
package legacy.org.janelia.intensity;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import mpicbg.models.AffineModel1D;
import mpicbg.models.Model;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;

/**
 * @author Stephan Saalfeld saalfelds@janelia.hhmi.org
 *
 */
public class RansacRegressionReduceFilter implements PointMatchFilter
{
	final protected Model< ? > model = new AffineModel1D();
	final protected int iterations = 1000;
	final protected double  maxEpsilon = 0.1;
	final protected double minInlierRatio = 0.1;
	final protected int minNumInliers = 10;
	final protected double maxTrust = 3.0;

	final static protected double[] minMax( final Iterable< PointMatch > matches )
	{
		final Iterator< PointMatch > iter = matches.iterator();
		PointMatch m = iter.next();
		double min = m.getP1().getL()[ 0 ], max = min;
		while ( iter.hasNext() )
		{
			m = iter.next();
			final double x = m.getP1().getL()[ 0 ];
			if ( x < min )
				min = x;
			else if ( x > max )
				max = x;
		}
		return new double[]{ min, max };
	}

	@Override
	public void filter( final List< PointMatch > candidates, final Collection< PointMatch > inliers )
	{
		try
		{
			if (
					model.filterRansac(
							candidates,
							inliers,
							iterations,
							maxEpsilon,
							minInlierRatio,
							minNumInliers,
							maxTrust ) )
			{
				model.fit( inliers );


				final double[] minMax = minMax( inliers );

				inliers.clear();

				final Point p1 = new Point( new double[]{ minMax[ 0 ] } );
				final Point p2 = new Point( new double[]{ minMax[ 1 ] } );
				p1.apply( model );
				p2.apply( model );
				inliers.add( new PointMatch( p1, new Point( p1.getW().clone() ) ) );
				inliers.add( new PointMatch( p2, new Point( p2.getW().clone() ) ) );
			}
			else
					inliers.clear();
		}
		catch ( final Exception e )
		{
			inliers.clear();
		}
	}

}
