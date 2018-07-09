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

package mpicbg.trakem2.transform;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Arrays;
import java.util.List;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import de.unihalle.informatik.rhizoTrak.display.MipMapImage;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.persistence.Loader;
import mpicbg.models.CoordinateTransformMesh;
import mpicbg.trakem2.transform.TransformMeshMappingWithMasks.ImageProcessorWithMasks;
import mpicbg.trakem2.util.Pair;

public class ExportARGB {
	
	static public final Pair< ColorProcessor, ByteProcessor > makeFlatImageARGB(
			final List<Patch> patches, final Rectangle roi, final double backgroundValue, final double scale, final boolean use_mipmaps)
	{
		return use_mipmaps ? makeFlatImageARGBFromMipMaps( patches, roi, backgroundValue, scale )
				           : makeFlatImageARGBFromOriginals( patches, roi, backgroundValue, scale );
	}
	
	static public final int[] extractARGBIntArray( final Image img )
	{
		final int[] pix = new int[img.getWidth(null) * img.getHeight(null)];
		PixelGrabber pg = new PixelGrabber( img, 0, 0, img.getWidth(null), img.getHeight(null), pix, 0, img.getWidth(null) );
		try {
			pg.grabPixels();
		} catch (InterruptedException ie) {}
		return pix;
	}

	/**
	 * 
	 * Returns nonsense or throws an Exception if mipmaps are not available.
	 * Limited to 2GB arrays for the final image.
	 * 
	 * @param patches
	 * @param roi
	 * @param backgroundValue
	 * @param scale
	 * @return
	 */
	static public final Pair< ColorProcessor, ByteProcessor > makeFlatImageARGBFromMipMaps(
			final List<Patch> patches, final Rectangle roi, final double backgroundValue, final double scale)
	{
		final int width = (int)(roi.width * scale);
		final int height = (int)(roi.height * scale);
		// Process the three channels separately in order to use proper alpha composition
		final ColorProcessor target = new ColorProcessor( width, height );
		target.setInterpolationMethod( ImageProcessor.BILINEAR );
		final ByteProcessor targetMask = new ByteProcessor( width, height );
		targetMask.setInterpolationMethod( ImageProcessor.BILINEAR );

		final Loader loader = patches.get(0).getProject().getLoader();

		for (final Patch patch : patches) {
			
			// MipMap image, already including any coordinate transforms and the alpha mask (if any), by definition.
			final MipMapImage mipMap = loader.fetchImage(patch, scale);
			
			/// DEBUG: is there an alpha channel at all?
			//new ij.ImagePlus("alpha of " + patch.getTitle(), new ByteProcessor( mipMap.image.getWidth(null), mipMap.image.getHeight(null), new ColorProcessor( mipMap.image ).getChannel( 4 ))).show();
			// Yes, there is, even though the mipmap images have the alpha pre-multiplied
			
			// Work-around strange bug that makes mipmap-loaded images paint with 7-bit depth instead of 8-bit depth
			final BufferedImage bi = new BufferedImage(mipMap.image.getWidth(null), mipMap.image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = bi.createGraphics();
			g2d.drawImage(mipMap.image, 0, 0, null);
			g2d.dispose();
			
			final int[] pix = extractARGBIntArray(bi);
			bi.flush();
			
			// DEBUG: does the BufferedImage have the alpha channel?
			//{
			//	final byte[] aa = new byte[pix.length];
			//	for (int i=0; i<aa.length; ++i) aa[i] = (byte)((pix[i] & 0xff000000) >> 24);
			//	new ij.ImagePlus("alpha of BI of " + patch.getTitle(), new ByteProcessor(bi.getWidth(), bi.getHeight(), aa)).show();
			//}
			// YES: the alpha, containing the outside too. All fine.
			
			final ByteProcessor alpha;
			final ColorProcessor rgb = new ColorProcessor( bi.getWidth(), bi.getHeight(), pix );
			
			if ( patch.hasAlphaChannel() ) {
				// The mipMap has the alpha channel in it, even if the alpha is pre-multiplied as well onto the images.
				final byte[]  a = new byte[pix.length];
				for (int i=0; i<a.length; ++i) {
					a[i] = (byte )((pix[i] & 0xff000000) >> 24);
				}
				alpha = new ByteProcessor(bi.getWidth(), bi.getHeight(), a);
			} else {
				alpha = new ByteProcessor( bi.getWidth(), bi.getHeight() );
				Arrays.fill( (byte[]) alpha.getPixels(), (byte)255 );
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
			
			final CoordinateTransformMesh mesh = new CoordinateTransformMesh( aff, patch.getMeshResolution(), bi.getWidth(), bi.getHeight() );
			final TransformMeshMappingWithMasks< CoordinateTransformMesh > mapping = new TransformMeshMappingWithMasks< CoordinateTransformMesh >( mesh );
			
			alpha.setInterpolationMethod( ImageProcessor.NEAREST_NEIGHBOR ); // no interpolation
			rgb.setInterpolationMethod( ImageProcessor.BILINEAR );
			mapping.map(rgb, alpha, target, targetMask);
		}
		
		return new Pair< ColorProcessor, ByteProcessor >( target, targetMask );
	}
	
	/**
	 * Limited to 2GB arrays for the requested image.
	 * 
	 * @param patches
	 * @param roi
	 * @param backgroundValue
	 * @param scale
	 * @return
	 */
	static public final Pair< ColorProcessor, ByteProcessor > makeFlatImageARGBFromOriginals(
			final List<Patch> patches, final Rectangle roi, final double backgroundValue, final double scale)
	{
		final ColorProcessor target = new ColorProcessor((int)(roi.width * scale), (int)(roi.height * scale));
		target.setInterpolationMethod( ImageProcessor.BILINEAR );
		final ByteProcessor targetMask = new ByteProcessor( target.getWidth(), target.getHeight() );
		targetMask.setInterpolationMethod( ImageProcessor.BILINEAR );

		for (final Patch patch : patches) {
			final Patch.PatchImage pai = patch.createTransformedImage();
			final ColorProcessor fp = (ColorProcessor) pai.target.convertToRGB();
			final ByteProcessor alpha;
			
			System.out.println("IMAGE:" + patch.getTitle());
			System.out.println("mask: " + pai.mask);
			System.out.println("outside: " + pai.outside);
			
			if ( null == pai.mask ) {
				if ( null == pai.outside ) {
					alpha = new ByteProcessor( fp.getWidth(), fp.getHeight() );
					Arrays.fill( ( byte[] )alpha.getPixels(), (byte)255 ); // fully opaque
				} else {
					alpha = pai.outside;
				}
			} else {
				alpha = pai.mask;
			}

			// The affine to apply
			final AffineTransform atc = new AffineTransform();
			atc.scale( scale, scale );
			atc.translate( -roi.x, -roi.y );
			
			final AffineTransform at = new AffineTransform();
			at.preConcatenate( atc );
			at.concatenate( patch.getAffineTransform() );
			
			final AffineModel2D aff = new AffineModel2D();
			aff.set( at );
			
			final CoordinateTransformMesh mesh = new CoordinateTransformMesh( aff, patch.getMeshResolution(), fp.getWidth(), fp.getHeight() );
			final TransformMeshMappingWithMasks< CoordinateTransformMesh > mapping = new TransformMeshMappingWithMasks< CoordinateTransformMesh >( mesh );
			
			fp.setInterpolationMethod( ImageProcessor.BILINEAR );
			alpha.setInterpolationMethod( ImageProcessor.BILINEAR );
			
			mapping.map( fp, alpha, target, targetMask );
		}
		
		return new Pair< ColorProcessor, ByteProcessor >( target, targetMask );
	}
}
