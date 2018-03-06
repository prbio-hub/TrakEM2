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
import mpicbg.models.Affine2D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.InterpolatedAffineModel2D;
import mpicbg.models.Model;
import mpicbg.models.RigidModel2D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.TranslationModel2D;

public class GenericAffineTile2D< A extends Model< A > & Affine2D< A > > extends AbstractAffineTile2D< A >
{
    private static final long serialVersionUID = 5632120826192026830L;

    public GenericAffineTile2D( final A model, final Patch patch )
	{
		super( model, patch );
	}

	@SuppressWarnings( "rawtypes" )
	@Override
	protected void initModel()
	{
		final AffineTransform a = patch.getAffineTransform();
		if ( AffineModel2D.class.isInstance( model ) )
			( ( AffineModel2D )( Object )model ).set( a );
		else if ( SimilarityModel2D.class.isInstance( model ) )
			( ( SimilarityModel2D )( Object )model ).set( a.getScaleX(), a.getShearY(), a.getTranslateX(), a.getTranslateY() );
		else if ( RigidModel2D.class.isInstance( model ) )
			( ( RigidModel2D )( Object )model ).set( a.getScaleX(), a.getShearY(), a.getTranslateX(), a.getTranslateY() );
		else if ( TranslationModel2D.class.isInstance( model ) )
			( ( TranslationModel2D )( Object )model ).set( a.getTranslateX(), a.getTranslateY() );
		else if ( InterpolatedAffineModel2D.class.isInstance( model ) )
			( ( InterpolatedAffineModel2D )( Object )model ).set( a );
	}

}
