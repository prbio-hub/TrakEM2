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

package legacy.mpicbg.trakem2.transform;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import de.unihalle.informatik.rhizoTrak.display.MipMapImage;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.persistence.Loader;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import mpicbg.models.CoordinateTransformMesh;
import mpicbg.trakem2.transform.AffineModel2D;
import mpicbg.trakem2.transform.TransformMeshMappingWithMasks;
import mpicbg.trakem2.transform.TransformMeshMappingWithMasks.ImageProcessorWithMasks;
import mpicbg.trakem2.util.Pair;

public class ExportUnsignedByte
{
	/** Works only when mipmaps are available, returning nonsense otherwise. */
	static public final Pair< ByteProcessor, ByteProcessor > makeFlatImage(final List<Patch> patches, final Rectangle roi, final double backgroundValue, final double scale)
	{
		final Pair< FloatProcessor, FloatProcessor > p = makeFlatImageFloat( patches, roi, backgroundValue, scale );
		return new Pair< ByteProcessor, ByteProcessor >( p.a.convertToByteProcessor(true), p.b.convertToByteProcessor() );
	}

	/** Works only when mipmaps are available, returning nonsense otherwise. */
	static public final Pair< FloatProcessor, FloatProcessor > makeFlatImageFloat(final List<Patch> patches, final Rectangle roi, final double backgroundValue, final double scale)
	{
		final FloatProcessor target = new FloatProcessor((int)(roi.width * scale), (int)(roi.height * scale));
		target.setInterpolationMethod( ImageProcessor.BILINEAR );
		final FloatProcessor targetMask = new FloatProcessor( target.getWidth(), target.getHeight() );
		targetMask.setInterpolationMethod( ImageProcessor.BILINEAR );
		final ImageProcessorWithMasks targets = new ImageProcessorWithMasks( target, targetMask, null );

		final Loader loader = patches.get(0).getProject().getLoader();

		for (final Patch patch : patches) {
			// MipMap image, already including any coordinate transforms and the alpha mask (if any), by definition.
			final MipMapImage mipMap = loader.fetchImage(patch, scale);

			// Place the mipMap data into FloatProcessors
			final FloatProcessor fp = new FloatProcessor(mipMap.image.getWidth( null ), mipMap.image.getHeight( null ));
			final FloatProcessor alpha = new FloatProcessor( fp.getWidth(), fp.getHeight() );
			
			// Transfer pixels to a grey image (avoids incorrect readings for ARGB images that end up cropping down to 7-bit)
			final BufferedImage bi = new BufferedImage(fp.getWidth(), fp.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			final Graphics2D g = bi.createGraphics();
			g.drawImage(mipMap.image, 0, 0, null);
			g.dispose();
			
			// Extract pixels from grey image and copy into fp
			final byte[] bpix = ( byte[] )new ByteProcessor( bi ).getPixels();
			for (int i=0; i<bpix.length; ++i) {
				fp.setf( i, bpix[i] & 0xff );
			}

			// Extract the alpha channel from the mipmap, if any
			if ( patch.hasAlphaChannel() )
			{
				final byte[] apix = new ColorProcessor( mipMap.image ).getChannel( 4 );
				for (int i=0; i<apix.length; ++i) {
					alpha.setf( i, (apix[i] & 0xff) / 255.0f ); // between [0..1]
				}
			} else {
				// The default: full opacity
				Arrays.fill( ( float[] )alpha.getPixels(), 1.0f );
			}

			// The affine to apply to the MipMap.image
			final AffineTransform atc = new AffineTransform();
			atc.scale( scale, scale );
			atc.translate( -roi.x, -roi.y );
			
			final AffineTransform at = new AffineTransform();
			at.preConcatenate( atc );
			at.concatenate( patch.getAffineTransform() );
			at.scale( mipMap.scaleX, mipMap.scaleY );
			
			final AffineModel2D aff = new AffineModel2D();
			aff.set( at );
			
			final CoordinateTransformMesh mesh = new CoordinateTransformMesh( aff, patch.getMeshResolution(), fp.getWidth(), fp.getHeight() );
			final TransformMeshMappingWithMasks< CoordinateTransformMesh > mapping = new TransformMeshMappingWithMasks< CoordinateTransformMesh >( mesh );
			
			fp.setInterpolationMethod( ImageProcessor.BILINEAR );
			alpha.setInterpolationMethod( ImageProcessor.NEAREST_NEIGHBOR ); // no interpolation
			
			mapping.mapInterpolated( new ImageProcessorWithMasks( fp, alpha, null), targets );
		}
		
		return new Pair< FloatProcessor, FloatProcessor >( target, targetMask );
	}
}
