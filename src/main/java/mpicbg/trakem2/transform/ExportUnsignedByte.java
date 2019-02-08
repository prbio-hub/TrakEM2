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
 *  https://prbio-hub.github.io/rhizoTrak
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
 *    https://prbio-hub.github.io/rhizoTrak
 *
 */

/* === original file header below (if any) === */

package mpicbg.trakem2.transform;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import ij.plugin.filter.GaussianBlur;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import de.unihalle.informatik.rhizoTrak.display.MipMapImage;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import mpicbg.models.CoordinateTransformMesh;
import mpicbg.trakem2.util.Pair;

public class ExportUnsignedByte
{
	/** Works only when mipmaps are available, returning nonsense otherwise. */
	static public final Pair< ByteProcessor, ByteProcessor > makeFlatImageFromMipMaps(final List<Patch> patches, final Rectangle roi, final double backgroundValue, final double scale)
	{
		return makeFlatImage( patches, roi, backgroundValue, scale, new MipMapSource() );
	}
	
	static public final Pair< ByteProcessor, ByteProcessor > makeFlatImageFromOriginals(final List<Patch> patches, final Rectangle roi, final double backgroundValue, final double scale)
	{
		return makeFlatImage( patches, roi, backgroundValue, scale, new OriginalSource() );
	}
	
	static public class ImageData {
		private ByteProcessor bp;
		private ByteProcessor alpha;
		private double scaleX;
		private double scaleY;

		public ImageData( final ByteProcessor bp, final ByteProcessor alpha, final double scaleX, final double scaleY )
		{
			this.bp = bp;
			this.alpha = alpha;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
		}
	}

	static public interface ImageSource
	{
		ImageData fetch(Patch p, double scale);
	}
	
	static public class MipMapSource implements ImageSource
	{
		@Override
		public final ImageData fetch(final Patch patch, final double scale)
		{
			// The scale must be adjusted for the scaling introduced by the Patch affine transform.
			final double aK = Math.max( patch.getWidth() / patch.getOWidth(),
								        patch.getHeight() / patch.getOHeight() );
			
			final double K = aK > 1.0 ? 1.0 : Math.min(1.0, scale * aK);

			// MipMap image, already including any coordinate transforms and the alpha mask (if any), by definition.
			final MipMapImage mipMap = patch.getProject().getLoader().fetchImage(patch, K);

			// Place the mipMap data into FloatProcessors
			final ByteProcessor bp; // new ByteProcessor(mipMap.image.getWidth( null ), mipMap.image.getHeight( null ));
			final ByteProcessor alpha;

			// Transfer pixels to a grey image (avoids incorrect readings for ARGB images that end up cropping down to 7-bit)
			final BufferedImage bi = new BufferedImage(mipMap.image.getWidth(null), mipMap.image.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
			final Graphics2D g = bi.createGraphics();
			g.drawImage(mipMap.image, 0, 0, null);
			g.dispose();

			// Extract pixels from grey image and copy into fp
			bp = new ByteProcessor( bi );

			// Extract the alpha channel from the mipmap, if any
			if ( patch.hasAlphaChannel() )
			{
				alpha = new ColorProcessor( mipMap.image ).getChannel( 4, null );
			} else {
				// The default: full opacity
				alpha = new ByteProcessor( bp.getWidth(), bp.getHeight() );
				Arrays.fill( ( byte[] )alpha.getPixels(), (byte)255 );
			}
			
			return new ImageData( bp, alpha, mipMap.scaleX, mipMap.scaleY );
		}
	}
	
	static public class OriginalSource implements ImageSource
	{
		@Override
		public final ImageData fetch(final Patch patch, final double scale)
		{
			Patch.PatchImage pai = patch.createTransformedImage();
			
			// The scale must be adjusted for the scaling introduced by the Patch affine transform.
			final double aK = Math.max( patch.getWidth() / patch.getOWidth(),
					                    patch.getHeight() / patch.getOHeight() );
			
			if ( aK > 1.0 || aK * scale > 1.0 ) {
				return new ImageData( pai.target.convertToByteProcessor(true), pai.getMask(), 1.0, 1.0 );
			}
			
			final double K = aK * scale;
			
			// Gaussian downsample the image to the target dimensions
			final int width = pai.target.getWidth(),
					  height = pai.target.getHeight(),
					  s_width = (int) ( width * K ),
					  s_height = (int) ( height * K );
			final double max_dimension_source = Math.max( width, height );
	    	final double max_dimension_target = Math.max( s_width, s_height );
	    	final double s = 0.5; // same sigma for source and target
	    	final double sigma = s * max_dimension_source / max_dimension_target - s * s ;
	    	
	    	new GaussianBlur().blurGaussian( pai.target, sigma, sigma, 0.0002 );
	    	pai.target.setInterpolationMethod( ImageProcessor.NEAREST_NEIGHBOR );

	    	final ByteProcessor alpha,
	    	                    mask = pai.getMask();
	    	
	    	if ( null != mask ) {
	    		new GaussianBlur().blurGaussian( mask, sigma, sigma, 0.002 ); // 0.002 only, suggested for 8-bit
	    		mask.setInterpolationMethod( ImageProcessor.NEAREST_NEIGHBOR );
	    		alpha = (ByteProcessor) mask.resize( s_width, s_height );
	    	} else {
	    		alpha = new ByteProcessor( s_width, s_height );
				Arrays.fill( ( byte[] )alpha.getPixels(), (byte)255 );
	    	}
	    	
	    	return new ImageData(
	    			pai.target.resize( s_width, s_height ).convertToByteProcessor(true),
	    			alpha,
	    			1/K,
	    			1/K);
		}
	}

	static public final Pair< ByteProcessor, ByteProcessor > makeFlatImage(
			final List<Patch> patches,
			final Rectangle roi,
			final double backgroundValue,
			final double scale,
			final ImageSource fetcher)
	{
		final ByteProcessor target = new ByteProcessor((int)(roi.width * scale), (int)(roi.height * scale));
		target.setInterpolationMethod( ImageProcessor.BILINEAR );
		final ByteProcessor targetMask = new ByteProcessor( target.getWidth(), target.getHeight() );
		targetMask.setInterpolationMethod( ImageProcessor.NEAREST_NEIGHBOR );

		for (final Patch patch : patches) {
			final ImageData imgd = fetcher.fetch( patch, scale );

			// The affine to apply to the MipMap.image
			final AffineTransform atc = new AffineTransform();
			atc.scale( scale, scale );
			atc.translate( -roi.x, -roi.y );
			
			final AffineTransform at = new AffineTransform();
			at.preConcatenate( atc );
			at.concatenate( patch.getAffineTransform() );
			at.scale( imgd.scaleX, imgd.scaleY );
			
			final AffineModel2D aff = new AffineModel2D();
			aff.set( at );
			
			final CoordinateTransformMesh mesh = new CoordinateTransformMesh( aff, patch.getMeshResolution(), imgd.bp.getWidth(), imgd.bp.getHeight() );
			final TransformMeshMappingWithMasks< CoordinateTransformMesh > mapping = new TransformMeshMappingWithMasks< >( mesh );
			
			imgd.bp.setInterpolationMethod( ImageProcessor.BILINEAR );
			imgd.alpha.setInterpolationMethod( ImageProcessor.NEAREST_NEIGHBOR );
			
			mapping.map( imgd.bp, imgd.alpha, target, targetMask );
		}
		
		return new Pair< >( target, targetMask );
	}
}
