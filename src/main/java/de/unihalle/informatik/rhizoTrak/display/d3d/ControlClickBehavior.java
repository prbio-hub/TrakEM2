package de.unihalle.informatik.rhizoTrak.display.d3d;

import java.awt.event.MouseEvent;

import org.scijava.vecmath.Point3d;

import de.unihalle.informatik.rhizoTrak.display.Coordinate;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.measure.Calibration;
import ij3d.Content;
import ij3d.Image3DUniverse;
import ij3d.behaviors.InteractiveBehavior;
import ij3d.behaviors.Picker;

/** A class to provide the behavior on control-clicking on
content in the 3D viewer.  This will attempt to center
the front TrakEM2 Display on the clicked point */
public class ControlClickBehavior extends InteractiveBehavior {

	protected Image3DUniverse universe;
	protected LayerSet ls;

	public ControlClickBehavior(final Image3DUniverse univ, final LayerSet ls) {
		super(univ);
		this.universe = univ;
		this.ls = ls;
	}

	@Override
	public void doProcess(final MouseEvent e) {
		if(!e.isControlDown() ||
				e.getID() != MouseEvent.MOUSE_PRESSED) {
			super.doProcess(e);
			return;
		}
		final Picker picker = universe.getPicker();
		final Content content = picker.getPickedContent(e.getX(),e.getY());
		if(content==null)
			return;
		final Point3d p = picker.getPickPointGeometry(content,e);
		if(p==null) {
			Utils.log("No point was found on content "+content);
			return;
		}
		final Display display = Display.getFront(ls.getProject());
		if(display==null) {
			// If there's no Display, just return...
			return;
		}
		if (display.getLayerSet() != ls) {
			Utils.log("The LayerSet instances do not match");
			return;
		}
		if(ls==null) {
			Utils.log("No LayerSet was found for the Display");
			return;
		}
		final Calibration cal = ls.getCalibration();
		if(cal==null) {
			Utils.log("No calibration information was found for the LayerSet");
			return;
		}
		final double scaledZ = p.z/cal.pixelWidth;
		final Layer l = ls.getNearestLayer(scaledZ);
		if(l==null) {
			Utils.log("No layer was found nearest to "+scaledZ);
			return;
		}
		final Coordinate<?> coordinate = new Coordinate<Object>(p.x/cal.pixelWidth,p.y/cal.pixelHeight,l,null);
		display.center(coordinate);
	}
}
