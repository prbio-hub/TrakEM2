package de.unihalle.informatik.rhizoTrak.display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import de.unihalle.informatik.rhizoTrak.Project;

public interface Bucketable {
	public ArrayList<? extends Displayable> getDisplayableList();
	public HashMap<Displayable, HashSet<Bucket>> getBucketMap(Layer layer);
	public void updateBucket(Displayable d, Layer layer);
	public Project getProject();
	public float getLayerWidth();
	public float getLayerHeight();
}
