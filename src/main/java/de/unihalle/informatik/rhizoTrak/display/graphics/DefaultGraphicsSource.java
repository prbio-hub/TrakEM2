package de.unihalle.informatik.rhizoTrak.display.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Paintable;
import de.unihalle.informatik.rhizoTrak.utils.ProjectToolbar;

/** Handles default mode, i.e. just plain images without any transformation handles of any kind. */
public class DefaultGraphicsSource implements GraphicsSource {

	/** Returns the list given as argument without any modification. */
	public List<? extends Paintable> asPaintable(final List<? extends Paintable> ds) {
		return ds;
	}

	/** Paints bounding boxes of selected objects as pink and active object as white. */
	public void paintOnTop(final Graphics2D g, final Display display, final Rectangle srcRect, final double magnification) {
		if (ProjectToolbar.getToolId() >= ProjectToolbar.PENCIL) { // PENCIL == SPARE2
			return;
		}
		g.setColor(Color.pink);
		Displayable active = display.getActive();
		final Rectangle bbox = new Rectangle();
		for (final Displayable d : display.getSelection().getSelected()) {
			d.getBoundingBox(bbox);
			if (d == active) {
				g.setColor(Color.white);
				//g.drawPolygon(d.getPerimeter());
				g.drawRect(bbox.x, bbox.y, bbox.width, bbox.height);
				g.setColor(Color.pink);
			} else {
				//g.drawPolygon(d.getPerimeter());
				g.drawRect(bbox.x, bbox.y, bbox.width, bbox.height);
			}
		}
	}
}
