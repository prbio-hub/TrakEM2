package de.unihalle.informatik.rhizoTrak.display.graphics;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Paintable;

public interface GraphicsSource {

	/** Takes as list of Paintable and returns the same or another list with perhaps a different number of Paintable objects, or some replaced by new Paintable instances. */
	public List<? extends Paintable> asPaintable(final List<? extends Paintable> ds);

	/** The canvas will call this method after painting everything else. */
	public void paintOnTop(final Graphics2D g, final Display display, final Rectangle srcRect, final double magnification);
}
