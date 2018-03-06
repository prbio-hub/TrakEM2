/**
 * 
 */
package legacy.mpicbg.trakem2.align;

import java.awt.geom.AffineTransform;

import de.unihalle.informatik.rhizoTrak.display.Patch;
import mpicbg.models.RigidModel2D;

public class RigidTile2D extends AbstractAffineTile2D< mpicbg.models.RigidModel2D >
{
	public RigidTile2D( final mpicbg.models.RigidModel2D model, final Patch patch )
	{
		super( model, patch );
	}
	
	public RigidTile2D( final Patch patch )
	{
		this( new RigidModel2D(), patch );
	}
	
	/**
	 * Initialize the model with the parameters of the {@link AffineTransform}
	 * of the {@link Patch}.  The {@link AffineTransform} should be a
	 * Rigid Transformation, otherwise the results will not be what you might
	 * expect.  This means, that:
	 * <ul>
	 * <li>{@link AffineTransform#getScaleX()} == {@link AffineTransform#getScaleY()}</li>
	 * <li>{@link AffineTransform#getShearX()} == -{@link AffineTransform#getShearY()}</li>
	 * <li>{@link AffineTransform#getScaleX()}<sup>2</sup> + {@link AffineTransform#getShearX()}<sup>2</sup> == 1</li>
	 * </ul>
	 */
	@Override
	protected void initModel()
	{
		final AffineTransform a = patch.getAffineTransform();
		model.set( ( float )a.getScaleX(), ( float )a.getShearY(), ( float )a.getTranslateX(), ( float )a.getTranslateY() );
	}

}
