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
package legacy.mpicbg.trakem2.align;

import java.awt.geom.AffineTransform;

import de.unihalle.informatik.rhizoTrak.display.Patch;
import mpicbg.models.TranslationModel2D;

public class TranslationTile2D extends AbstractAffineTile2D< mpicbg.models.TranslationModel2D >
{
	public TranslationTile2D( final mpicbg.models.TranslationModel2D model, final Patch patch )
	{
		super( model, patch );
	}

	public TranslationTile2D( final Patch patch )
	{
		this( new TranslationModel2D(), patch );
	}

	/**
	 * Initialize the model with the parameters of the {@link AffineTransform}
	 * of the {@link Patch}.  The {@link AffineTransform} should be a
	 * Translation, otherwise the results will not be what you might expect.
	 * This means, that:
	 * <pre>
	 *   {@link AffineTransform#getScaleX()} == {@link AffineTransform#getScaleY()} == 1
	 *   {@link AffineTransform#getShearX()} == {@link AffineTransform#getShearY()} == 0
	 * </pre>
	 */
	@Override
	protected void initModel()
	{
		final AffineTransform a = patch.getAffineTransform();
		model.set( a.getTranslateX(), a.getTranslateY() );
	}

}
