package ini.trakem2.display;

import ini.trakem2.tree.*;
import ini.trakem2.utils.*;
import ini.trakem2.imaging.PatchStack;
import ini.trakem2.vector.VectorString3D;

import ij.ImageStack;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import ij.gui.ShapeRoi;
import ij.gui.GenericDialog;
import ij.io.DirectoryChooser;
import ij.measure.Calibration;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.MenuBar;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.*;
import java.io.File;
import java.awt.geom.AffineTransform;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;

import javax.vecmath.Point3f;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;
import javax.media.j3d.View;
import javax.media.j3d.Transform3D;
import javax.media.j3d.PolygonAttributes;

import ij3d.ImageWindow3D;
import ij3d.Image3DUniverse;
import ij3d.Content;
import ij3d.Image3DMenubar;
import customnode.CustomMeshNode;
import customnode.CustomMesh;
import customnode.CustomTriangleMesh;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;


/** One Display3D instance for each LayerSet (maximum). */
public final class Display3D {

	/** Table of LayerSet and Display3D - since there is a one to one relationship.  */
	static private Hashtable<LayerSet,Display3D> ht_layer_sets = new Hashtable<LayerSet,Display3D>();
	/**Control calls to new Display3D. */
	static private Lock htlock = new Lock();

	/** The sky will fall on your head if you modify any of the objects contained in this table -- which is a copy of the original, but the objects are the originals. */
	static public Hashtable<LayerSet,Display3D> getMasterTable() {
		return new Hashtable<LayerSet,Display3D>(ht_layer_sets);
	}

	/** Table of ProjectThing keys versus meshes, the latter represented by List of triangles in the form of thre econsecutive Point3f in the List.*/
	private Hashtable<ProjectThing,Content> ht_pt_meshes = new Hashtable<ProjectThing,Content>();

	private Image3DUniverse universe;

	private Lock u_lock = new Lock();

	private LayerSet layer_set;
	private double width, height;
	private int resample = -1; // unset
	static private final int DEFAULT_RESAMPLE = 4;
	/** If the LayerSet dimensions are too large, then limit to max 2048 for width or height and setup a scale.*/
	private double scale = 1.0;
	static private final int MAX_DIMENSION = 1024; // TODO change to LayerSet virtualization size

	private String selected = null;

	// To fork away from the EventDispatchThread
	static private ExecutorService launchers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	// To build meshes, or edit them
	private ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	/*
	static private KeyAdapter ka = new KeyAdapter() {
		public void keyPressed(KeyEvent ke) {
			// F1 .. F12 keys to set tools
			ProjectToolbar.keyPressed(ke);
		}
	};
	*/

	/** Defaults to parallel projection. */
	private Display3D(final LayerSet ls) {
		this.layer_set = ls;
		this.universe = new Image3DUniverse(512, 512); // size of the initial canvas, not the universe itself
		this.universe.getViewer().getView().setProjectionPolicy(View.PERSPECTIVE_PROJECTION); // (View.PERSPECTIVE_PROJECTION);
		computeScale(ls);
		this.universe.show();
		this.universe.getWindow().addWindowListener(new IW3DListener(this, ls));
		this.universe.getWindow().setTitle(ls.getProject().toString() + " -- 3D Viewer");
		// it ignores the listeners:
		//preaddKeyListener(this.universe.getWindow(), ka);
		//preaddKeyListener(this.universe.getWindow().getCanvas(), ka);

		// register
		Display3D.ht_layer_sets.put(ls, this);
	}

	/*
	private void preaddKeyListener(Component c, KeyListener kl) {
		KeyListener[] all = c.getKeyListeners();
		if (null != all) {
			for (KeyListener k : all) c.removeKeyListener(k);
		}
		c.addKeyListener(kl);
		if (null != all) {
			for (KeyListener k : all) c.addKeyListener(k);
		}
	}
	*/

	public Image3DUniverse getUniverse() {
		return universe;
	}

	/* Take a snapshot know-it-all mode. Each Transform3D given as argument gets assigned to the (nearly) homonimous TransformGroup, which have the following relationships:
	 *
	 *  scaleTG contains rotationsTG
	 *  rotationsTG contains translateTG
	 *  translateTG contains centerTG
	 *  centerTG contains the whole scene, with all meshes, etc.
	 *
	 *  Any null arguments imply the current transform in the open Display3D.
	 *
	 *  By default, a newly created Display3D has the scale and center transforms modified to make the scene fit nicely centered (and a bit scaled down) in the given Display3D window. The translate and rotate transforms are set to identity.
	 *
	 *  The TransformGroup instances may be reached like this:
	 *
	 *  LayerSet layer_set = Display.getFrontLayer().getParent();
	 *  Display3D d3d = Display3D.getDisplay(layer_set);
	 *  TransformGroup scaleTG = d3d.getUniverse().getGlobalScale();
	 *  TransformGroup rotationsTG = d3d.getUniverse().getGlobalRotate();
	 *  TransformGroup translateTG = d3d.getUniverse().getGlobalTranslate();
	 *  TransformGroup centerTG = d3d.getUniverse().getCenterTG();
	 *
	 *  ... and the Transform3D from each may be read out indirectly like this:
	 *
	 *  Transform3D t_scale = new Transform3D();
	 *  scaleTG.getTransform(t_scale);
	 *  ...
	 *
	 * WARNING: if your java3d setup does not support offscreen rendering, the Display3D window will be brought to the front and a screen snapshot cropped to it to perform the snapshot capture. Don't cover the Display3D window with any other windows (not even an screen saver).
	 *
	 */
	/*public ImagePlus makeSnapshot(final Transform3D scale, final Transform3D rotate, final Transform3D translate, final Transform3D center) {
		return universe.makeSnapshot(scale, rotate, translate, center);
	}*/

	/** Uses current scaling, translation and centering transforms! */
	/*public ImagePlus makeSnapshotXY() { // aka posterior
		// default view
		return universe.makeSnapshot(null, new Transform3D(), null, null);
	}*/
	/** Uses current scaling, translation and centering transforms! */
	/*public ImagePlus makeSnapshotXZ() { // aka dorsal
		Transform3D rot1 = new Transform3D();
		rot1.rotZ(-Math.PI/2);
		Transform3D rot2 = new Transform3D();
		rot2.rotX(Math.PI/2);
		rot1.mul(rot2);
		return universe.makeSnapshot(null, rot1, null, null);
	}
	*/
	/** Uses current scaling, translation and centering transforms! */
	/*
	public ImagePlus makeSnapshotYZ() { // aka lateral
		Transform3D rot = new Transform3D();
		rot.rotY(Math.PI/2);
		return universe.makeSnapshot(null, rot, null, null);
	}*/

	/*
	public ImagePlus makeSnapshotZX() { // aka frontal
		Transform3D rot = new Transform3D();
		rot.rotX(-Math.PI/2);
		return universe.makeSnapshot(null, rot, null, null);
	}
	*/

	/** Uses current scaling, translation and centering transforms! Opposite side of XZ. */
	/*
	public ImagePlus makeSnapshotXZOpp() {
		Transform3D rot1 = new Transform3D();
		rot1.rotX(-Math.PI/2); // 90 degrees clockwise
		Transform3D rot2 = new Transform3D();
		rot2.rotY(Math.PI); // 180 degrees around Y, to the other side.
		rot1.mul(rot2);
		return universe.makeSnapshot(null, rot1, null, null);
	}*/

	private class IW3DListener extends WindowAdapter {
		private Display3D d3d;
		private LayerSet ls;
		IW3DListener(Display3D d3d, LayerSet ls) {
			this.d3d = d3d;
			this.ls = ls;
		}
		public void windowClosing(WindowEvent we) {
			//Utils.log2("Display3D.windowClosing");
			d3d.executors.shutdownNow();
			/*Object ob =*/ ht_layer_sets.remove(ls);
			/*if (null != ob) {
				Utils.log2("Removed Display3D from table for LayerSet " + ls);
			}*/
		}
		public void windowClosed(WindowEvent we) {
			//Utils.log2("Display3D.windowClosed");
			ht_layer_sets.remove(ls);
		}
	}

	/** Reads the #ID in the name, which is immutable. */
	private ProjectThing find(String name) {
		long id = Long.parseLong(name.substring(name.lastIndexOf('#')+1));
		for (final ProjectThing pt : ht_pt_meshes.keySet()) {
			Displayable d = (Displayable) pt.getObject();
			if (d.getId() == id) return pt;
		}
		return null;
	}

	/** If the layer set is too large in width and height, then set a scale that makes it maximum MAX_DIMENSION in any of the two dimensions. */
	private void computeScale(LayerSet ls) {
		this.width = ls.getLayerWidth();
		this.height = ls.getLayerHeight();
		if (width > MAX_DIMENSION) {
			scale = MAX_DIMENSION / width;
			height *= scale;
			width = MAX_DIMENSION;
		}
		if (height > MAX_DIMENSION) {
			scale = MAX_DIMENSION / height;
			width *= scale;
			height = MAX_DIMENSION;
		}
		//Utils.log2("scale, width, height: " + scale + ", " + width + ", " + height);
	}

	static private boolean check_j3d = true;
	static private boolean has_j3d_3dviewer = false;

	static private boolean hasLibs() {
		if (check_j3d) {
			check_j3d = false;
			try {
				Class p3f = Class.forName("javax.vecmath.Point3f");
				has_j3d_3dviewer = true;
			} catch (ClassNotFoundException cnfe) {
				Utils.log("Java 3D not installed.");
				has_j3d_3dviewer = false;
				return false;
			}
			try {
				Class ij3d = Class.forName("ij3d.ImageWindow3D");
				has_j3d_3dviewer = true;
			} catch (ClassNotFoundException cnfe) {
				Utils.log("3D Viewer not installed.");
				has_j3d_3dviewer = false;
				return false;
			}
		}
		return has_j3d_3dviewer;
	}

	/** Get an existing Display3D for the given LayerSet, or create a new one for it (and cache it). */
	static private Display3D get(final LayerSet ls) {
		synchronized (htlock) {
			htlock.lock();
			try {
				// test:
				if (!hasLibs()) return null;
				//
				Display3D d3d = ht_layer_sets.get(ls);
				if (null != d3d) return d3d;
				// Else, new:
				final boolean[] done = new boolean[]{false};
				javax.swing.SwingUtilities.invokeAndWait(new Runnable() { public void run() {
					ht_layer_sets.put(ls, new Display3D(ls));
					done[0] = true;
				}});
				// wait to avoid crashes in amd64
				// try { Thread.sleep(500); } catch (Exception e) {}
				while (!done[0]) {
					try { Thread.sleep(10); } catch (Exception e) {}
				}
				return ht_layer_sets.get(ls);
			} catch (Exception e) {
				IJError.print(e);
			} finally {
				// executed even when returning from within the try-catch block
				htlock.unlock();
			}
		}
		return null;
	}

	/** Get the Display3D instance that exists for the given LayerSet, if any. */
	static public Display3D getDisplay(final LayerSet ls) {
		return ht_layer_sets.get(ls);
	}

	static public void setWaitingCursor() {
		for (Display3D d3d : ht_layer_sets.values()) {
			d3d.universe.getWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}

	static public void doneWaiting() {
		for (Display3D d3d : ht_layer_sets.values()) {
			d3d.universe.getWindow().setCursor(Cursor.getDefaultCursor());
		}
	}

	static public Future<List<Content>> show(ProjectThing pt) {
		return show(pt, false, -1);
	}

	static public void showAndResetView(final ProjectThing pt) {
		new Thread() { public void run() {
			setPriority(Thread.NORM_PRIORITY);
			// wait until done
			Future<List<Content>> fu = show(pt, true, -1);
			try {
				fu.get(); // wait until done
			} catch (Exception e) { IJError.print(e); }
			Display3D d3d = ht_layer_sets.get(pt.getProject().getRootLayerSet()); // TODO should change for nested layer sets
			if (null != d3d) {
				d3d.universe.resetView(); // reset the absolute center
				d3d.universe.adjustView(); // zoom out to bring all elements in universe within view
			}
		}}.start();
	}

	/** Scan the ProjectThing children and assign the renderable ones to an existing Display3D for their LayerSet, or open a new one. If true == wait && -1 != resample, then the method returns only when the mesh/es have been added. */
	static public Future<List<Content>> show(final ProjectThing pt, final boolean wait, final int resample) {
		if (null == pt) return null;
		final Callable<List<Content>> c = new Callable<List<Content>>() {
			public List<Content> call() {
		try {
			// scan the given ProjectThing for 3D-viewable items not present in the ht_meshes
			// So: find arealist, pipe, ball, and profile_list types
			final HashSet hs = pt.findBasicTypeChildren();
			if (null == hs || 0 == hs.size()) {
				Utils.logAll("Node " + pt + " does not contain any 3D-displayable children");
				return null;
			}

			final List<Content> list = new ArrayList<Content>(hs.size());

			final List<Callable<Content>> to_add = new ArrayList<Callable<Content>>();
			long last_added = System.currentTimeMillis();

			for (final Iterator it = hs.iterator(); it.hasNext(); ) {
				// obtain the Displayable object under the node
				final ProjectThing child = (ProjectThing)it.next();
				Object obc = child.getObject();
				Displayable displ = obc.getClass().equals(String.class) ? null : (Displayable)obc;
				if (null != displ) {
					if (displ.getClass().equals(Profile.class)) {
						//Utils.log("Display3D can't handle Bezier profiles at the moment.");
						// handled by profile_list Thing
						continue;
					}
					if (!displ.isVisible()) {
						Utils.log("Skipping non-visible node " + displ);
						continue;
					}
				}
				//StopWatch sw = new StopWatch();
				// obtain the containing LayerSet
				Display3D d3d = null;
				if (null != displ) d3d = Display3D.get(displ.getLayerSet());
				else if (child.getType().equals("profile_list")) {
					ArrayList al_children = child.getChildren();
					if (null == al_children || 0 == al_children.size()) continue;
					// else, get the first Profile and get its LayerSet
					d3d = Display3D.get(((Displayable)((ProjectThing)al_children.get(0)).getObject()).getLayerSet());
				} else {
					Utils.log("Don't know what to do with node " + child);
				}
				if (null == d3d) {
					Utils.log("Could not get a proper 3D display for node " + displ);
					return null; // java3D not installed most likely
				}
				if (d3d.ht_pt_meshes.contains(child)) {
					Utils.log2("Already here: " + child);
					continue; // already here
				}
				setWaitingCursor(); // the above may be creating a display
				//sw.elapsed("after creating and/or retrieving Display3D");


				// TODO: rewrite to add whole sublists of meshes in one shot with addContentLater.
				//       For that, rewrite addMesh to accept a list of Future<Content> or so.

				Future<Content> fu = d3d.addMesh(child, displ, resample);
				if (wait) {
					list.add(fu.get());
				}

				if (wait) {
					list.add(fu.get());
				}

				// Add meshes every 4 seconds
				long now = System.currentTimeMillis();
				if (now - last_added > 4000) {
					last_added = now;

				}

				//sw.elapsed("after creating mesh");
			}

			// Since it's sometimes not obvious when done, say so:
			if (wait && hs.size() > 1) {
				Utils.logAll("Done showing " + hs.size());
			}

			return list;

		} catch (Exception e) {
			IJError.print(e);
			return null;
		} finally {
			doneWaiting();
		}
		}};

		return launchers.submit(c);
	}

	static public void resetView(final LayerSet ls) {
		Display3D d3d = ht_layer_sets.get(ls);
		if (null != d3d) d3d.universe.resetView();
	}

	static public void showOrthoslices(Patch p) {
		Display3D d3d = get(p.getLayerSet());
		d3d.adjustResampling();
		//d3d.universe.resetView();
		String title = makeTitle(p) + " orthoslices";
		// remove if present
		d3d.universe.removeContent(title);
		PatchStack ps = p.makePatchStack();
		ImagePlus imp = get8BitStack(ps);
		d3d.universe.addOrthoslice(imp, null, title, 0, new boolean[]{true, true, true}, d3d.resample);
		Content ct = d3d.universe.getContent(title);
		setTransform(ct, ps.getPatch(0));
		ct.setLocked(true); // locks the added content
	}

	static public void showVolume(Patch p) {
		Display3D d3d = get(p.getLayerSet());
		d3d.adjustResampling();
		//d3d.universe.resetView();
		String title = makeTitle(p) + " volume";
		// remove if present
		d3d.universe.removeContent(title);
		PatchStack ps = p.makePatchStack();
		ImagePlus imp = get8BitStack(ps);
		d3d.universe.addVoltex(imp, null, title, 0, new boolean[]{true, true, true}, d3d.resample);
		Content ct = d3d.universe.getContent(title);
		setTransform(ct, ps.getPatch(0));
		ct.setLocked(true); // locks the added content
	}

	static private void setTransform(Content ct, Patch p) {
		final double[] a = new double[6];
		p.getAffineTransform().getMatrix(a);
		Calibration cal = p.getLayerSet().getCalibration();
		// a is: m00 m10 m01 m11 m02 m12
		// d expects: m01 m02 m03 m04, m11 m12 ...
		ct.applyTransform(new Transform3D(new double[]{a[0], a[2], 0, a[4] * cal.pixelWidth,
			                                       a[1], a[3], 0, a[5] * cal.pixelWidth,
					                          0,    0, 1, p.getLayer().getZ() * cal.pixelWidth,
					                          0,    0, 0, 1}));
	}

	/** Returns a stack suitable for the ImageJ 3D Viewer, either 8-bit gray or 8-bit color.
	 *  If the PatchStack is already of the right type, it is returned,
	 *  otherwise a copy is made in the proper type.
	 */
	static private ImagePlus get8BitStack(final PatchStack ps) {
		switch (ps.getType()) {
			case ImagePlus.COLOR_RGB:
				// convert stack to 8-bit color
				return ps.createColor256Copy();
			case ImagePlus.GRAY16:
			case ImagePlus.GRAY32:
				// convert stack to 8-bit
				return ps.createGray8Copy();
			case ImagePlus.GRAY8:
			case ImagePlus.COLOR_256:
				return ps;
			default:
				Utils.logAll("Cannot handle stacks of type: " + ps.getType());
				return null;
		}
	}

	/** Considers there is only one Display3D for each LayerSet. */
	static public void remove(ProjectThing pt) {
		if (null == pt) return;
		if (null == pt.getObject()) return;
		Object ob = pt.getObject();
		if (!(ob instanceof Displayable)) return;
		Displayable displ = (Displayable)ob;
		Display3D d3d = ht_layer_sets.get(displ.getLayerSet()); // TODO profile_list is going to fail here
		if (null == d3d) {
			// there is no Display3D showing the pt to remove
			Utils.log2("No Display3D contains ProjectThing: " + pt);
			return;
		}
		if (null == d3d.ht_pt_meshes.remove(pt)) {
			Utils.log2("No mesh contained within " + d3d + " for ProjectThing " + pt);
			return; // not contained here
		}
		/*
		String title = makeTitle(displ);
		//Utils.log(d3d.universe.contains(title) + ": Universe contains " + displ);
		d3d.universe.removeContent(title); // WARNING if the title changes, problems: will need a table of pt vs title as it was when added to the universe. At the moment titles are not editable for basic types, but this may change in the future. TODO the future is here: titles are editable for basic types.
		*/
		Utils.log2(Utils.toString(d3d.ht_pt_meshes));
		Content ct = d3d.ht_pt_meshes.get(pt);
		if (null != ct) d3d.universe.removeContent(ct.getName());
	}

	/** Creates a mesh for the given Displayable in a separate Thread, and adds it to the universe.
	 *  The outer future creates the mesh; the inner adds it to the universe. */
	private Future<Future<Content>> addMesh(final ProjectThing pt, final Displayable displ, final int resample) {
		return executors.submit(new Callable<Future<Content>>() {
			public Future<Content> call() {
				try {
					// 1 - Create content
					Callable<Content> c1 = createMesh(pt, displ, resample);
					if (null == c1) return null;
					Content content = c1.call();
					if (null == content) return null;
					String title = content.getName();
					// 2 - Remove from universe any content of the same title
					if (universe.contains(title)) {
						universe.removeContent(title);
					}
					// 3 - Add to universe
					return universe.addContentLater(content);
				} catch (Exception e) {
					IJError.print(e);
					return null;
				}
			}
		});
	}



	/** Returns a function that returns a Content object.
	 *  Does NOT add the Content to the universe; it merely creates it. */
	private Callable<Content> createMesh(final ProjectThing pt, final Displayable displ, final int resample) {
		final double scale = this.scale;
		return new Callable<Content>() {
			public Content call() {
				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				try {

		// the list 'triangles' is really a list of Point3f, which define a triangle every 3 consecutive points. (TODO most likely Bene Schmid got it wrong: I don't think there's any need to have the points duplicated if they overlap in space but belong to separate triangles.)
		final List triangles;
		boolean no_culling_ = false; // don't show back faces when false
		if (displ instanceof AreaList) {
			int rs = resample;
			if (-1 == resample) rs = Display3D.this.resample = adjustResampling(); // will adjust this.resample, and return it (even if it's a default value)
			else rs = Display3D.this.resample;
			triangles = ((AreaList)displ).generateTriangles(scale, rs);
			//triangles = removeNonManifold(triangles);
		} else if (displ instanceof Ball) {
			double[][][] globe = Ball.generateGlobe(12, 12);
			triangles = ((Ball)displ).generateTriangles(scale, globe);
		} else if (displ instanceof Line3D) {
			// Pipe and Polyline
			// adjustResampling();  // fails horribly, needs first to correct mesh-generation code
			triangles = ((Line3D)displ).generateTriangles(scale, 12, 1 /*Display3D.this.resample*/);
		} else if (null == displ && pt.getType().equals("profile_list")) {
			triangles = Profile.generateTriangles(pt, scale);
			no_culling_ = true;
		} else {
			Utils.log("Unrecognized type for 3D mesh generation: " + (null != displ ? displ.getClass() : null) + " : " + displ);
			triangles = null;
		}
		// safety checks
		if (null == triangles) {
			Utils.log("Some error ocurred: can't create triangles for " + displ);
			return null;
		}
		if (0 == triangles.size()) {
			Utils.log2("Skipping empty mesh for " + displ.getTitle());
			return null;
		}
		if (0 != triangles.size() % 3) {
			Utils.log2("Skipping non-multiple-of-3 vertices list generated for " + displ.getTitle());
			return null;
		}


		/* // debug: extra check: find NaN
		for (Point3f p3 : (List<Point3f>)triangles) {
			if (null == p3) {
				Utils.log2("Found a null Point3f! Aborting.");
				return null;
			}
			if (Float.isNaN(p3.x)
			 || Float.isNaN(p3.y)
			 || Float.isNaN(p3.z))
			{
				Utils.log("A Point3f has a NaN coordinate! Aborting.");
				return null;
			}
		}
		*/


		final Color color;
		final float alpha;
		final String title;
		if (null != displ) {
			color = displ.getColor();
			alpha = displ.getAlpha();
			title = makeTitle(displ);
		} else if (pt.getType().equals("profile_list")) {
			// for profile_list: get from the first (what a kludge; there should be a ZDisplayable ProfileList object)
			Object obp = ((ProjectThing)pt.getChildren().get(0)).getObject();
			if (null == obp) return null;
			Displayable di = (Displayable)obp;
			color = di.getColor();
			alpha = di.getAlpha();
			Object ob = pt.getParent().getTitle();
			if (null == ob || ob.equals(pt.getParent().getType())) title = pt.toString() + " #" + pt.getId(); // Project.getMeaningfulTitle can't handle profile_list properly
			else title = ob.toString() + " /[" + pt.getParent().getType() + "]/[profile_list] #" + pt.getId();
		} else {
			title = pt.toString() + " #" + pt.getId();
			color = null;
			alpha = 1.0f;
		}

		// TODO why for all? Above no_culling_ is set to true or false, depending upon type.
		final boolean no_culling = true; // for ALL

		Content ct = null;

		// add to 3D view (synchronized)
		synchronized (u_lock) {
			u_lock.lock();
			try {
				Color3f c3 = new Color3f(color);

				if (no_culling) {
					// create a mesh with the same color and zero transparency (that is, full opacity)
					CustomTriangleMesh mesh = new CustomTriangleMesh(triangles, c3, 0);
					// Set mesh properties for double-sided triangles
					PolygonAttributes pa = mesh.getAppearance().getPolygonAttributes();
					pa.setCullFace(PolygonAttributes.CULL_NONE);
					pa.setBackFaceNormalFlip(true);
					mesh.setColor(c3);
					// After setting properties, add to the viewer
					//ct = universe.addCustomMesh(mesh, title);
					ct = universe.createContent(mesh, title);
				} else {
					//ct = universe.addTriangleMesh(triangles, c3, title);
					ct = universe.createContent(new CustomTriangleMesh(triangles, c3, 0), title);
				}

				if (null == ct) return null;

				// Set general content properties
				ct.setTransparency(1f - alpha);
				// Default is unlocked (editable) transformation; set it to locked:
				ct.setLocked(true);

				// register mesh
				ht_pt_meshes.put(pt, ct);
				Utils.log2("Put: ht_pt_meshes.put(" + pt + ", " + ct + ")");

			} catch (Throwable e) {
				Utils.logAll("Mesh generation failed for \"" + title + "\"  from " + pt);
				IJError.print(e);
				e.printStackTrace();
			} finally {
				u_lock.unlock();
			}
		}

		Utils.log2(pt.toString() + " n points: " + triangles.size());

		return ct;

				} catch (Exception e) {
					IJError.print(e);
					return null;
				}

		}};
	}

	/** Creates a mesh from the given VectorString3D, which is unbound to any existing Pipe. */
	static public Future<Content> addMesh(final LayerSet ref_ls, final VectorString3D vs, final String title, final Color color) {
		return addMesh(ref_ls, vs, title, color, null, 1.0f);
	}

	/** Creates a mesh from the given VectorString3D, which is unbound to any existing Pipe. */
	static public Future<Content> addMesh(final LayerSet ref_ls, final VectorString3D vs, final String title, final Color color, final double[] widths, final float alpha) {
		final FutureTask<Content> fu = new FutureTask<Content>(new Callable<Content>() {
			public Content call() {
				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				try {
		/////
		final Display3D d3d = Display3D.get(ref_ls);
		final double scale = d3d.scale;
		final double width = d3d.width;
		float transp = 1 - alpha;
		if (transp < 0) transp = 0;
		if (transp > 1) transp = 1;
		if (1 == transp) {
			Utils.log("WARNING: adding a 3D object fully transparent.");
		}

		double[] wi = widths;
		if (null == widths) {
			wi = new double[vs.getPoints(0).length];
			//Utils.log2("len: " + wi.length + vs.getPoints(0).length + vs.getPoints(1).length);
			Arrays.fill(wi, 2.0);
		} else if (widths.length != vs.length()) {
			Utils.log("ERROR: widths.length != VectorString3D.length()");
			return null;
		}

		List triangles = Pipe.generateTriangles(Pipe.makeTube(vs.getPoints(0), vs.getPoints(1), vs.getPoints(2), wi, 1, 12, null), scale);

		Content ct = null;

		// add to 3D view (synchronized)
		synchronized (d3d.u_lock) {
			d3d.u_lock.lock();
			try {
				// ensure proper default transform
				//d3d.universe.resetView();
				//
				//Utils.log2(title + " : vertex count % 3 = " + triangles.size() % 3 + " for " + triangles.size() + " vertices");
				//d3d.universe.ensureScale((float)(width*scale));
				ct = d3d.universe.addMesh(triangles, new Color3f(color), title, /*(float)(width*scale),*/ 1);
				ct.setTransparency(transp);
				ct.setLocked(true);
			} catch (Exception e) {
				IJError.print(e);
			} finally {
				d3d.u_lock.unlock();
			}
		}

		return ct;

		/////
				} catch (Exception e) {
					IJError.print(e);
					return null;
				}

		}});


		launchers.submit(new Runnable() { public void run() {
			final Display3D d3d = Display3D.get(ref_ls);
			d3d.executors.submit(fu);
		}});

		return fu;
	}

	// This method has the exclusivity in adjusting the resampling value.
	synchronized private final int adjustResampling() {
		if (resample > 0) return resample;
		final GenericDialog gd = new GenericDialog("Resample");
		gd.addSlider("Resample: ", 1, 20, -1 != resample ? resample : DEFAULT_RESAMPLE);
		gd.showDialog();
		if (gd.wasCanceled()) {
			resample = -1 != resample ? resample : DEFAULT_RESAMPLE; // current or default value
			return resample;
		}
		resample = ((java.awt.Scrollbar)gd.getSliders().get(0)).getValue();
		return resample;
	}

	/** Checks if there is any Display3D instance currently showing the given Displayable. */
	static public boolean isDisplayed(final Displayable d) {
		if (null == d) return false;
		final String title = makeTitle(d);
		for (Display3D d3d : ht_layer_sets.values()) {
			if (null != d3d.universe.getContent(title)) return true;
		}
		if (d.getClass() == Profile.class) {
			Content content = getProfileContent(d);
		}
		return false;
	}

	/** Checks if the given Displayable is a Profile, and tries to find a possible Content object in the Image3DUniverse of its LayerSet according to the title as created from its profile_list ProjectThing. */
	static public Content getProfileContent(final Displayable d) {
		if (null == d) return null;
		if (d.getClass() != Profile.class) return null;
		Display3D d3d = get(d.getLayer().getParent());
		if (null == d3d) return null;
		ProjectThing pt = d.getProject().findProjectThing(d);
		if (null == pt) return null;
		pt = (ProjectThing) pt.getParent();
		return d3d.universe.getContent(new StringBuffer(pt.toString()).append(" #").append(pt.getId()).toString());
	}

	static public Future<Boolean> setColor(final Displayable d, final Color color) {
		final Display3D d3d = getDisplay(d.getLayer().getParent());
		if (null == d3d) return null; // no 3D displays open
		return d3d.executors.submit(new Callable() { public Boolean call() {
			Content content = d3d.universe.getContent(makeTitle(d));
			if (null == content) content = getProfileContent(d);
			if (null != content) {
				content.setColor(new Color3f(color));
				return true;
			}
			return false;
		}});
	}

	static public Future<Boolean> setTransparency(final Displayable d, final float alpha) {
		if (null == d) return null;
		Layer layer = d.getLayer();
		if (null == layer) return null; // some objects have no layer, such as the parent LayerSet.
		final Display3D d3d = ht_layer_sets.get(layer.getParent());
		if (null == d3d) return null;
		return d3d.executors.submit(new Callable<Boolean>() { public Boolean call() {
			String title = makeTitle(d);
			Content content = d3d.universe.getContent(title);
			if (null == content) content = getProfileContent(d);
			if (null != content) content.setTransparency(1 - alpha);
			else if (null == content && d.getClass().equals(Patch.class)) {
				Patch pa = (Patch)d;
				if (pa.isStack()) {
					title = pa.getProject().getLoader().getFileName(pa);
					for (Display3D dd : ht_layer_sets.values()) {
						for (Iterator cit = dd.universe.getContents().iterator(); cit.hasNext(); ) {
							Content c = (Content)cit.next();
							if (c.getName().startsWith(title)) {
								c.setTransparency(1 - alpha);
								// no break, since there could be a volume and an orthoslice
							}
						}
					}
				}
			}
			return true;
		}});
	}

	static public String makeTitle(final Displayable d) {
		return d.getProject().getMeaningfulTitle(d) + " #" + d.getId();
	}
	static public String makeTitle(final Patch p) {
		return new File(p.getProject().getLoader().getAbsolutePath(p)).getName()
		       + " #" + p.getProject().getLoader().getNextId();
	}

	/** Remake the mesh for the Displayable in a separate Thread, if it's included in a Display3D
	 *  (otherwise returns null). */
	static public Future<Content> update(final Displayable d) {
		Layer layer = d.getLayer();
		if (null == layer) return null; // some objects have no layer, such as the parent LayerSet.
		Display3D d3d = ht_layer_sets.get(layer.getParent());
		if (null == d3d) return null;
		return d3d.addMesh(d.getProject().findProjectThing(d), d, d3d.resample);
	}

	/*
	static public final double computeTriangleArea() {
		return 0.5 *  Math.sqrt(Math.pow(xA*yB + xB*yC + xC*yA, 2) +
					Math.pow(yA*zB + yB*zC + yC*zA, 2) +
					Math.pow(zA*xB + zB*xC + zC*xA, 2));
	}
	*/

	static public final boolean contains(final LayerSet ls, final String title) {
		final Display3D d3d = getDisplay(ls);
		if (null == d3d) return false;
		return null != d3d.universe.getContent(title);
	}

	static public void destroy() {
		launchers.shutdownNow();
	}

	static public void init() {
		if (launchers.isShutdown()) {
			launchers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		}
	}
}
