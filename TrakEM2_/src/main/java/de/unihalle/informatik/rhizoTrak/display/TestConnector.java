package de.unihalle.informatik.rhizoTrak.display;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.HashMap;

import de.unihalle.informatik.rhizoTrak.Project;

public class TestConnector extends ZDisplayable {

	public TestConnector(Project project, String title) {
		super(project, title,0,0);
		addToDatabase();
		// TODO Auto-generated constructor stub
	}

	public TestConnector(Project project, long id, String title, boolean locked, AffineTransform at, float width,
			float height) {
		super(project, id, title, locked, at, width, height);
		// TODO Auto-generated constructor stub
	}

	public TestConnector(Project project, long id, HashMap<String, String> ht, HashMap<Displayable, String> ht_links) {
		super(project, id, ht, ht_links);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean linkPatches() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Layer getFirstLayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean intersects(Area area, double z_first, double z_last) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean calculateBoundingBox(Layer la) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDeletable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Displayable clone(Project pr, boolean copy_id) {
		// TODO Auto-generated method stub
		return null;
	}

}
