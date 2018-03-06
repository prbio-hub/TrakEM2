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
package legacy.mpicbg.trakem2.align;


import ij.IJ;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.parallel.ExecutorProvider;
import de.unihalle.informatik.rhizoTrak.utils.IJError;
import de.unihalle.informatik.rhizoTrak.utils.Filter;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import mpicbg.ij.SIFT;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;

/**
 * @author Stephan Saalfeld saalfeld@mpi-cbg.de
 */
final public class AlignmentUtils
{
	final static public class ParamPointMatch implements Serializable
	{
		private static final long serialVersionUID = 7526084042028501775L;

		final public FloatArray2DSIFT.Param sift = new FloatArray2DSIFT.Param();

		/**
		 * Closest/next closest neighbor distance ratio
		 */
		public float rod = 0.92f;

		@Override
		public boolean equals( final Object o )
		{
			if ( getClass().isInstance( o ) )
			{
				final ParamPointMatch oppm = ( ParamPointMatch )o;
				return
					oppm.sift.equals( sift ) &
					oppm.rod == rod;
			}
			else
				return false;
		}

		public boolean clearCache = true;

		public int maxNumThreadsSift = Runtime.getRuntime().availableProcessors();
	}

	private AlignmentUtils() {}

	final static public String layerName( final Layer layer )
	{
		return new StringBuffer( "layer z=" )
			.append( String.format( "%.3f", layer.getZ() ) )
			.append( " `" )
			.append( layer.getTitle() )
			.append( "'" )
			.toString();

	}

	final static public List< Patch > filterPatches( final Layer layer, final Filter< Patch > filter )
	{
		final List< Patch > patches = layer.getAll( Patch.class );
		if ( filter != null )
		{
			for ( final Iterator< Patch > it = patches.iterator(); it.hasNext(); )
			{
				if ( !filter.accept( it.next() ) )
					it.remove();
			}
		}
		return patches;
	}

	/**
	 * Extract SIFT features and save them into the project folder.
	 *
	 * @param layerRange the list of layers to be aligned
	 * @param box a rectangular region of interest that will be used for alignment
	 * @param scale scale factor &lt;= 1.0
	 * @param filter a name based filter for Patches (can be null)
	 * @param siftParam SIFT extraction parameters
	 * @param clearCache
	 * @param numThreads
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	final static protected void extractAndSaveLayerFeatures(
			final List< Layer > layerRange,
			final Rectangle box,
			final double scale,
			final Filter< Patch > filter,
			final FloatArray2DSIFT.Param siftParam,
			final boolean clearCache,
			final int numThreads ) throws ExecutionException, InterruptedException
	{
        final long sTime = System.currentTimeMillis();
		final ExecutorService exec = ExecutorProvider.getExecutorService(1.0f / (float)numThreads);

		/* extract features for all slices and store them to disk */
		final AtomicInteger counter = new AtomicInteger( 0 );
		final ArrayList< Future< ArrayList< Feature > > > siftTasks = new ArrayList< Future< ArrayList< Feature > > >();

        for (final Layer layer : layerRange)
        {
            siftTasks.add(exec.submit(
                    new LayerFeatureCallable(layer, box, scale, filter,
                            siftParam, clearCache)));
        }

		/* join */
		try
		{
			for ( final Future< ArrayList< Feature > > fu : siftTasks )
            {
                IJ.showProgress( counter.getAndIncrement(), layerRange.size() - 1 );
				fu.get();
            }
		}
		catch ( final InterruptedException e )
		{
			Utils.log( "Feature extraction interrupted." );
			IJError.print( e );
			siftTasks.clear();
			//exec.shutdownNow();
			throw e;
		}
		catch ( final ExecutionException e )
		{
			Utils.log( "Execution exception during feature extraction." );
			IJError.print( e );
			siftTasks.clear();
			//exec.shutdownNow();
			throw e;
		}

		siftTasks.clear();
        IJ.log("Extracted features in " + (System.currentTimeMillis() - sTime) + "ms");
		//exec.shutdown();
	}

    private static class LayerFeatureCallable implements Callable<ArrayList<Feature>>, Serializable
    {

        private final Layer layer;
        private final Filter<Patch> filter;
        private final boolean clearCache;
        private final Rectangle finalBox;
        final FloatArray2DSIFT.Param siftParam;
        final double scale;

        public LayerFeatureCallable(final Layer layer,
                                    final Rectangle finalBox,
                                    final double scale,
                                    final Filter<Patch> filter,
                                    final FloatArray2DSIFT.Param siftParam,
                                    final boolean clearCache)
        {
            this.layer = layer;
            this.filter = filter;
            this.clearCache = clearCache;
            this.finalBox = finalBox;
            this.siftParam = siftParam;
            this.scale = scale;
        }

        @Override
        public ArrayList<Feature> call() throws Exception
        {
            final String layerName = layerName( layer );

            //IJ.showProgress( counter.getAndIncrement(), layerRange.size() - 1 );

            final List< Patch > patches = filterPatches( layer, filter );

            ArrayList< Feature > fs = null;
            if ( !clearCache )
                fs = legacy.mpicbg.trakem2.align.Util.deserializeFeatures( layer.getProject(), siftParam, "layer", layer.getId() );

            if ( null == fs )
            {
                /* free memory */
                layer.getProject().getLoader().releaseAll();

                final FloatArray2DSIFT sift = new FloatArray2DSIFT( siftParam );
                final SIFT ijSIFT = new SIFT( sift );
                fs = new ArrayList< Feature >();
                ijSIFT.extractFeatures( Patch.makeFlatGrayImage( patches, finalBox, 0, scale ), fs );
                Utils.log( fs.size() + " features extracted for " + layerName );

                if ( !legacy.mpicbg.trakem2.align.Util.serializeFeatures( layer.getProject(), siftParam, "layer", layer.getId(), fs ) )
                    Utils.log( "FAILED to store serialized features for " + layerName );
            }
            else
                Utils.log( fs.size() + " features loaded for " + layerName );

            return fs;
        }
    }
}
