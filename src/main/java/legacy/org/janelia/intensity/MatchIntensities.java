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

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.persistence.FSLoader;
import de.unihalle.informatik.rhizoTrak.plugin.TPlugIn;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import mpicbg.models.Affine1D;
import mpicbg.models.AffineModel1D;
import mpicbg.models.IdentityModel;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.InterpolatedAffineModel1D;
import mpicbg.models.Model;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;
import mpicbg.models.Tile;
import mpicbg.models.TileConfiguration;
import mpicbg.models.TranslationModel1D;
import net.imglib2.img.list.ListImg;
import net.imglib2.img.list.ListRandomAccess;
import net.imglib2.util.ValuePair;

/**
 *
 * @author Stephan Saalfeld saalfelds@janelia.hhmi.org
 * @author Philipp Hanslovsky
 */
public class MatchIntensities implements TPlugIn
{
	final private class Matcher implements Runnable
	{
		final private Rectangle roi;
		final private ValuePair< Patch, Patch > patchPair;
		final private HashMap< Patch, ArrayList< Tile< ? > > > coefficientsTiles;
		final private PointMatchFilter filter;
		final private double scale;
		final private int numCoefficients;

		public Matcher(
				final Rectangle roi,
				final ValuePair< Patch, Patch > patchPair,
				final HashMap< Patch, ArrayList< Tile< ? > > > coefficientsTiles,
				final PointMatchFilter filter,
				final double scale,
				final int numCoefficients )
		{
			this.roi = roi;
			this.patchPair = patchPair;
			this.coefficientsTiles = coefficientsTiles;
			this.filter = filter;
			this.scale = scale;
			this.numCoefficients = numCoefficients;
		}

		@Override
		public void run()
		{
			final Patch p1 = patchPair.getA();
			final Patch p2 = patchPair.getB();

			final Rectangle box1 = p1.getBoundingBox().intersection( roi );

			/* get the coefficient tiles */
			final ArrayList< Tile< ? > > p1CoefficientsTiles = coefficientsTiles.get( p1 );

			/* render intersection */
			final Rectangle box2 = p2.getBoundingBox();
			final Rectangle box = box1.intersection( box2 );

			final int w = ( int ) ( box.width * scale + 0.5 );
			final int h = ( int ) ( box.height * scale + 0.5 );
			final int n = w * h;

			final FloatProcessor pixels1 = new FloatProcessor( w, h );
			final FloatProcessor weights1 = new FloatProcessor( w, h );
			final ColorProcessor coefficients1 = new ColorProcessor( w, h );
			final FloatProcessor pixels2 = new FloatProcessor( w, h );
			final FloatProcessor weights2 = new FloatProcessor( w, h );
			final ColorProcessor coefficients2 = new ColorProcessor( w, h );

			Render.render( p1, numCoefficients, numCoefficients, pixels1, weights1, coefficients1, box.x, box.y, scale );
			Render.render( p2, numCoefficients, numCoefficients, pixels2, weights2, coefficients2, box.x, box.y, scale );

			/*
			 * generate a matrix of all coefficients in p1 to all
			 * coefficients in p2 to store matches
			 */
			final ArrayList< ArrayList< PointMatch > > list = new ArrayList< ArrayList< PointMatch > >();
			for ( int i = 0; i < numCoefficients * numCoefficients * numCoefficients * numCoefficients; ++i )
				list.add( new ArrayList< PointMatch >() );
			final ListImg< ArrayList< PointMatch > > matrix = new ListImg< ArrayList< PointMatch > >( list, numCoefficients * numCoefficients, numCoefficients * numCoefficients );
			final ListRandomAccess< ArrayList< PointMatch > > ra = matrix.randomAccess();

			/*
			 * iterate over all pixels and feed matches into the match
			 * matrix
			 */
			for ( int i = 0; i < n; ++i )
			{
				final int c1 = coefficients1.get( i );
				if ( c1 > 0 )
				{
					final int c2 = coefficients2.get( i );
					if ( c2 > 0 )
					{
						final double w1 = weights1.getf( i );
						if ( w1 > 0 )
						{
							final double w2 = weights2.getf( i );
							if ( w2 > 0 )
							{
								final double p = pixels1.getf( i );
								final double q = pixels2.getf( i );
								final PointMatch pq = new PointMatch( new Point( new double[] { p } ), new Point( new double[] { q } ), w1 * w2 );

								/* first label is 1 */
								ra.setPosition( c1 - 1, 0 );
								ra.setPosition( c2 - 1, 1 );
								ra.get().add( pq );
							}
						}
					}
				}
			}

			/* filter matches */
			final ArrayList< PointMatch > inliers = new ArrayList< PointMatch >();
			for ( final ArrayList< PointMatch > candidates : matrix )
			{
				inliers.clear();
				filter.filter( candidates, inliers );
				candidates.clear();
				candidates.addAll( inliers );
			}

			/* get the coefficient tiles of p2 */
			final ArrayList< Tile< ? > > p2CoefficientsTiles = coefficientsTiles.get( p2 );

			/* connect tiles across patches */
			for ( int i = 0; i < numCoefficients * numCoefficients; ++i )
			{
				final Tile< ? > t1 = p1CoefficientsTiles.get( i );
				ra.setPosition( i, 0 );
				for ( int j = 0; j < numCoefficients * numCoefficients; ++j )
				{
					ra.setPosition( j, 1 );
					final ArrayList< PointMatch > matches = ra.get();
					if ( matches.size() > 0 )
					{
						final Tile< ? > t2 = p2CoefficientsTiles.get( j );
						synchronized ( MatchIntensities.this )
						{
							t1.connect( t2, ra.get() );
							IJ.log( "Connected patch " + p1.getId() + ", coefficient " + i + "  +  patch " + p2.getId() + ", coefficient " + j + " by " + matches.size() + " samples." );
						}
					}
				}
			}
		}
	}

	protected LayerSet layerset = null;

	static protected int numCoefficients = 8;
	static protected double lambda1 = 0.01;
	static protected double lambda2 = 0.01;
	static protected double neighborWeight = 0.1;
	static protected int radius = 5;
	static protected int iterations = 2000;
	static protected double scale = -1;

	private Layer currentLayer( final Object... params )
	{
		final Layer layer;
		if ( params != null && params[ 0 ] != null )
		{
			final Object param = params[ 0 ];
			if ( Layer.class.isInstance( param ) )
				layer = ( Layer ) param;
			else if ( LayerSet.class.isInstance( param ) )
				layer = ( ( LayerSet ) param ).getLayer( 0 );
			else if ( Displayable.class.isInstance( param ) )
				layer = ( ( Displayable ) param ).getLayer();
			else
				layer = null;
		}
		else
		{
			final Display front = Display.getFront();
			if ( front == null )
				layer = Project.getProjects().get( 0 ).getRootLayerSet().getLayer( 0 );
			else
				layer = front.getLayer();
		}
		return layer;
	}

	private static Rectangle getRoi( final LayerSet layerset )
	{
		final Roi roi;
		final Display front = Display.getFront();
		if ( front == null )
			roi = null;
		else
			roi = front.getRoi();
		if ( roi == null )
			return new Rectangle( 0, 0, ( int ) layerset.getLayerWidth(), ( int ) layerset.getLayerHeight() );
		else
			return roi.getBounds();
	}

	private double suggestScale( final List< Layer > layers )
	{
		if ( layers.size() < 2 )
			return 0.0;

		final Layer layer1 = layers.get( 0 );
		final Layer layer2 = layers.get( 1 );
		final Calibration calib = layer1.getParent().getCalibration();
		final double width = ( calib.pixelWidth + calib.pixelHeight ) * 0.5;
		final double depth = calib.pixelDepth;

		return ( layer2.getZ() - layer1.getZ() ) * width / depth;
	}

	@Override
	public boolean setup( final Object... params )
	{
	    if ( params != null && params[ 0 ] != null )
		{
			final Object param = params[ 0 ];
			if ( LayerSet.class.isInstance( param ) )
				layerset = ( LayerSet ) param;
			else if ( Displayable.class.isInstance( param ) )
				layerset = ( ( Displayable ) param ).getLayerSet();
			else
				return false;
		}
		else
		{
			final Display front = Display.getFront();
			if ( front == null )
				layerset = Project.getProjects().get( 0 ).getRootLayerSet();
			else
				layerset = front.getLayerSet();
		}
		return true;
	}

	final static protected < T extends Model< T > & Affine1D< T > > HashMap< Patch, ArrayList< Tile< T > > > generateCoefficientsTiles(
			final Collection< Patch > patches,
			final T template,
			final int nCoefficients )
	{
		final HashMap< Patch, ArrayList< Tile< T > > > map = new HashMap< Patch, ArrayList< Tile< T > > >();
		for ( final Patch p : patches )
		{
			final ArrayList< Tile< T > > coefficientModels = new ArrayList< Tile< T > >();
			for ( int i = 0; i < nCoefficients; ++i )
				coefficientModels.add( new Tile< T >( template.copy() ) );

			map.put( p, coefficientModels );
		}
		return map;
	}

	final static protected void identityConnect( final Tile< ? > t1, final Tile< ? > t2, final double weight )
	{
		final ArrayList< PointMatch > matches = new ArrayList< PointMatch >();
		matches.add( new PointMatch( new Point( new double[] { 0 } ), new Point( new double[] { 0 } ) ) );
		matches.add( new PointMatch( new Point( new double[] { 1 } ), new Point( new double[] { 1 } ) ) );
		t1.connect( t2, matches );
	}

	@Override
	public Object invoke( final Object... params )
	{
		if ( !setup( params ) )
			return null;

		final Layer layer = currentLayer( params );
        final GenericDialog gd = new GenericDialog( "Match intensities" );
        Utils.addLayerRangeChoices( layer, gd );
        gd.addMessage( "Layer range :" );
        gd.addNumericField( "scale : ", scale > 0 ? scale : suggestScale( layerset.getLayers() ), 3, 6, "" );
        gd.addNumericField( "coefficient resolution : ", numCoefficients, 0, 6, "" );
        gd.addNumericField( "test_maximally :", radius, 0, 6, "layers" );
        gd.addMessage( "Optimizer :" );
        gd.addNumericField( "iterations :", iterations, 0, 6, "" );
        gd.addNumericField( "scale_regularization :", lambda1, 2, 6, "" );
        gd.addNumericField( "translation_regularization :", lambda2, 2, 6, "" );
        gd.addNumericField( "smoothness_regularization :", neighborWeight, 2, 6, "" );
        gd.showDialog();

        if ( gd.wasCanceled() )
            return null;

        final List< Layer > layers = layerset.getLayers().subList( gd.getNextChoiceIndex(), gd.getNextChoiceIndex() + 1 );
        System.out.println( layers.size() );
        scale = gd.getNextNumber();
        numCoefficients = ( int )gd.getNextNumber();
        radius = ( int ) gd.getNextNumber();
        iterations = ( int )gd.getNextNumber();
        lambda1 = gd.getNextNumber();
        lambda2 = gd.getNextNumber();
        neighborWeight = gd.getNextNumber();

		try
		{
			run( layers, radius, scale, numCoefficients, lambda1, lambda2, neighborWeight, getRoi( layerset ) );
		}
		catch ( final InterruptedException e )
		{
			Utils.log( "Match intensities interrupted." );
			e.printStackTrace( System.out );
		}
		catch ( final ExecutionException e )
		{
			Utils.log( "Match intenities ExecutiuonException occurred:" );
			e.printStackTrace( System.out );
		}

		return null;
	}

    @Override
    public boolean applies( final Object ob )
    {
        return true;
    }


    /**
     * @param layers
     * @param radius
     * @param scale
     * @param numCoefficients
     * @param lambda1
     * @param lambda2
     * @param neighborWeight
     * @param roi
     */
	public < M extends Model< M > & Affine1D< M > > void run(
			final List< Layer > layers,
			final int radius,
			final double scale,
			final int numCoefficients,
			final double lambda1,
			final double lambda2,
			final double neighborWeight,
			final Rectangle roi ) throws InterruptedException, ExecutionException
	{
		final int firstLayerIndex = layerset.getLayerIndex( layers.get( 0 ).getId() );
		final int lastLayerIndex = layerset.getLayerIndex( layers.get( layers.size() - 1 ).getId() );

		// final PointMatchFilter filter = new RansacRegressionFilter();
		final PointMatchFilter filter = new RansacRegressionReduceFilter();

		/* collect patches */
		Utils.log( "Collecting patches ... " );
		final ArrayList< Patch > patches = new ArrayList< Patch >();
		for ( final Layer layer : layers )
			patches.addAll( ( Collection )layer.getDisplayables( Patch.class, roi ) );

		/* delete existing intensity coefficients */
		Utils.log( "Clearing existing intensity maps ... " );
		for ( final Patch p : patches )
			p.clearIntensityMap();

		/* generate coefficient tiles for all patches
		 * TODO consider offering alternative models */
		final HashMap< Patch, ArrayList< Tile< ? extends M > > > coefficientsTiles =
				( HashMap ) generateCoefficientsTiles(
						patches,
						new InterpolatedAffineModel1D< InterpolatedAffineModel1D< AffineModel1D, TranslationModel1D >, IdentityModel >(
								new InterpolatedAffineModel1D< AffineModel1D, TranslationModel1D >(
										new AffineModel1D(), new TranslationModel1D(), lambda1 ),
								new IdentityModel(), lambda2 ),
						numCoefficients * numCoefficients );

		/* completed patches */
		final HashSet< Patch > completedPatches = new HashSet< Patch >();

		/* collect patch pairs */
		Utils.log( "Collecting patch pairs ... " );
		final ArrayList< ValuePair< Patch, Patch > > patchPairs = new ArrayList< ValuePair< Patch, Patch > >();

		for ( final Patch p1 : patches )
		{
			completedPatches.add( p1 );

			final Rectangle box1 = p1.getBoundingBox().intersection( roi );

			final ArrayList< Patch > p2s = new ArrayList< Patch >();

			/* across adjacent layers */
			final int layerIndex = layerset.getLayerIndex( p1.getLayer().getId() );
			for ( int i = Math.max( firstLayerIndex, layerIndex - radius ); i <= Math.min( lastLayerIndex, layerIndex + radius ); ++i )
			{
				final Layer layer = layerset.getLayer( i );
				if ( layer != null )
					p2s.addAll( ( Collection ) layer.getDisplayables( Patch.class, box1 ) );
			}

			for ( final Patch p2 : p2s )
			{
				/*
				 * if this patch had been processed earlier, all matches are
				 * already in
				 */
				if ( completedPatches.contains( p2 ) )
					continue;

				patchPairs.add( new ValuePair< Patch, Patch >( p1, p2 ) );
			}
		}

		final int numThreads = Integer.parseInt(
				layerset.getProperty(
						"n_mipmap_threads",
						Integer.toString( Runtime.getRuntime().availableProcessors() ) ) );

		Utils.log( "Matching intensities using " + numThreads + " threads ... " );

		final ExecutorService exec = Executors.newFixedThreadPool( numThreads );
		final ArrayList< Future< ? > > futures = new ArrayList< Future< ? > >();
		for ( final ValuePair< Patch, Patch > patchPair : patchPairs )
		{
			futures.add(
					exec.submit(
							new Matcher(
									roi,
									patchPair,
									( HashMap )coefficientsTiles,
									filter,
									scale,
									numCoefficients ) ) );
		}

		for ( final Future< ? > future : futures )
			future.get();

		/* connect tiles within patches */
		Utils.log( "Connecting coefficient tiles in the same patch  ... " );
		for ( final Patch p1 : completedPatches )
		{
			/* get the coefficient tiles */
			final ArrayList< Tile< ? extends M > > p1CoefficientsTiles = coefficientsTiles.get( p1 );

			for ( int y = 1; y < numCoefficients; ++y )
			{
				final int yr = numCoefficients * y;
				final int yr1 = yr - numCoefficients;
				for ( int x = 0; x < numCoefficients; ++x )
				{
					identityConnect( p1CoefficientsTiles.get( yr1 + x ), p1CoefficientsTiles.get( yr + x ), neighborWeight );
				}
			}
			for ( int y = 0; y < numCoefficients; ++y )
			{
				final int yr = numCoefficients * y;
				for ( int x = 1; x < numCoefficients; ++x )
				{
					final int yrx = yr + x;
					identityConnect( p1CoefficientsTiles.get( yrx ), p1CoefficientsTiles.get( yrx - 1 ), neighborWeight );
				}
			}
		}

		/* optimize */
		Utils.log( "Optimizing ... " );
		final TileConfiguration tc = new TileConfiguration();
		for ( final ArrayList< Tile< ? extends M > > coefficients : coefficientsTiles.values() )
		{
			// for ( final Tile< ? > t : coefficients )
			// if ( t.getMatches().size() == 0 )
			// IJ.log( "bang" );
			tc.addTiles( coefficients );
		}

		try
		{
			tc.optimize( 0.01f, iterations, iterations, 0.75f );
		}
		catch ( final NotEnoughDataPointsException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( final IllDefinedDataPointsException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* save coefficients */
		final double[] ab = new double[ 2 ];
		final FSLoader loader = ( FSLoader ) layerset.getProject().getLoader();
		final String itsDir = loader.getUNUIdFolder() + "trakem2.its/";
		for ( final Entry< Patch, ArrayList< Tile< ? extends M > > > entry : coefficientsTiles.entrySet() )
		{
			final FloatProcessor as = new FloatProcessor( numCoefficients, numCoefficients );
			final FloatProcessor bs = new FloatProcessor( numCoefficients, numCoefficients );

			final Patch p = entry.getKey();

			final double min = p.getMin();
			final double max = p.getMax();

			final ArrayList< Tile< ? extends M > > tiles = entry.getValue();
			for ( int i = 0; i < numCoefficients * numCoefficients; ++i )
			{
				final Tile< ? extends M > t = tiles.get( i );
				final Affine1D< ? > affine = t.getModel();
				affine.toArray( ab );

				/* coefficients mapping into existing [min, max] */
				as.setf( i, ( float ) ab[ 0 ] );
				bs.setf( i, ( float ) ( ( max - min ) * ab[ 1 ] + min - ab[ 0 ] * min ) );
			}
			final ImageStack coefficientsStack = new ImageStack( numCoefficients, numCoefficients );
			coefficientsStack.addSlice( as );
			coefficientsStack.addSlice( bs );

			final String itsPath = itsDir + FSLoader.createIdPath( Long.toString( p.getId() ), "it", ".tif" );
			new File( itsPath ).getParentFile().mkdirs();
			IJ.saveAs( new ImagePlus( "", coefficientsStack ), "tif", itsPath );
		}

		/* update mipmaps */
		for ( final Patch p : patches )
			p.getProject().getLoader().decacheImagePlus(p.getId());
		final ArrayList< Future< Boolean > > mipmapFutures = new ArrayList< Future< Boolean > >();
		for ( final Patch p : patches )
			mipmapFutures.add( p.updateMipMaps() );

		for ( final Future< Boolean > f : mipmapFutures )
			f.get();

		Utils.log( "Matching intensities done." );
	}

	final static public void main( final String... args )
	{
		new ImageJ();

		final Project project = Project.openFSProject( "/home/saalfeld/tmp/intensity-corrected/elastic.xml", true );

		final MatchIntensities matcher = new MatchIntensities();
		matcher.invoke( project.getRootLayerSet() );
	}
}
