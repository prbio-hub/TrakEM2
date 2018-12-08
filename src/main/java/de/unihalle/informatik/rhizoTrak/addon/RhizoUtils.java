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

package de.unihalle.informatik.rhizoTrak.addon;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.display.LayerSet;
import de.unihalle.informatik.rhizoTrak.display.Node;
import de.unihalle.informatik.rhizoTrak.display.Patch;
import de.unihalle.informatik.rhizoTrak.display.Treeline;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.ImagePlus;

/**
 * some helper methods
 * 
 * @author posch
 *
 */
public class RhizoUtils {
	
	/**
	 * String to eoncode not available values
	 */
	public final static String NA_String = "NA";
	
	/** 
	 * The default character separating part of an ICAP image filename
	 */
	public static final char DEFAULT_IMAGENAME_SEPARATOR = '_';

	/** 
	 * @param project
	 * @return all rootstacks in the project tree of <code>project</code> which can hold treelines and connectors or
	 * null if non are found
	 */
	public static HashSet<ProjectThing> getRootstacks( Project project) {
		try {
			ProjectTree projectTree = project.getProjectTree();
		
			ProjectThing projectTreeRoot = (ProjectThing)projectTree.getRoot().getUserObject();
			HashSet<ProjectThing> rootstackProjectThings = projectTreeRoot.findChildrenOfTypeR( "rootstack");
			
			Iterator<ProjectThing> itr = rootstackProjectThings.iterator();
			while ( itr.hasNext()) {
				ProjectThing rootstackProjectThing = itr.next();
				if ( ! ( rootstackProjectThing.canHaveAsChild( "treeline") && rootstackProjectThing.canHaveAsChild( "connector") ) ) {
					rootstackProjectThings.remove(rootstackProjectThing);
				}
			}
			
			if ( rootstackProjectThings.size() == 0) {
				Utils.log( "RhizoUtils:getRootstacks Error can not find a rootstack in project tree");
				return null;
			}
				
			return rootstackProjectThings;
		} catch ( Exception ex ) {
			Utils.log( "RhizoUtils:getRootstacks Error can get root of project things");
			return null;
		}

	}
	
	/** get all Connectors below any of the  rootstacks hashed by its Id
	 * 
	 * @param project
	 * @return hashmap of Id,connector
	 */
	public static HashMap<Long,Connector>  getConnectorsBelowRootstacks( HashSet<ProjectThing> rootstackThings) {
		
		// all connectors below a rootstack
		HashMap<Long,Connector> allConnectors = new HashMap<Long,Connector>();

		for ( ProjectThing rootstackThing :rootstackThings ) {
			for ( ProjectThing pt : rootstackThing.findChildrenOfTypeR( Treeline.class)) {
				Object obj = pt.getObject();

				if ( obj.getClass().equals(Connector.class) ) {
					Connector con = (Connector) obj;
					allConnectors.put( con.getId(), con);
				}
			}
		}
		
		return allConnectors;
	}
	
	/** get all Treelines below any of the  rootstacks hashed by its Id
	 * 
	 * @param project
	 * @return hashmap of Id,connector
	 */
	public static HashMap<Long,Treeline>  getTreelinesBelowRootstacks( HashSet<ProjectThing> rootstackThings) {
		
		// all connectors below a rootstack
		HashMap<Long,Treeline> allTreelines = new HashMap<Long,Treeline>();

		for ( ProjectThing rootstackThing :rootstackThings ) {
			for ( ProjectThing pt : rootstackThing.findChildrenOfTypeR( Treeline.class)) {
				Object obj = pt.getObject();

				if ( obj .getClass().equals(Treeline.class) ) {
					Treeline tl = (Treeline) obj;
					allTreelines.put( tl.getId(), tl);
				}
			}
		}
		
		return allTreelines;
	}
	
	/** get all Connectors below any of the  rootstack hashed by its Id
	 * 
	 * @param project
	 * @return an arbitrary of all  rootstacks in the project tree of <code>project</code> which can hold treelines and connectors or
	 * null if non are found
	 */
	public static ProjectThing getOneRootstack(Project project) {
		HashSet<ProjectThing> rootstackProjectThings = RhizoUtils.getRootstacks( project);
		if ( rootstackProjectThings != null) {
			Iterator<ProjectThing> itr = rootstackProjectThings.iterator();
			return itr.next();
		} else {
			return null;
		}
	}

	/** Collect all <code>treelines</code>s below a <code>rootstack</code> in the current layer or all layers
	 * @param project
	 * @param currentLayer if null all layers are considered 
	 * 
	 * @return list of collected  <code>treelines</code>s or <code>null</code> if no rootstack found
	 */
	public static List<Treeline> getTreelinesBelowRootstacks( Project project, Layer currentLayer) {
		// find all rootstacks
		HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks( project);
		if ( rootstackThings == null) {
			return null;
		}

		// all treelines below a rootstack
		LinkedList<Treeline> allTreelines = new LinkedList<Treeline>();

		for ( ProjectThing rootstackThing :rootstackThings ) {
			//			if ( debug)	System.out.println("rootstack " + rootstackThing.getId());
			for ( ProjectThing pt : rootstackThing.findChildrenOfTypeR( Treeline.class)) {
				// we also find connectors!
				Treeline tl = (Treeline)pt.getObject();
				//				if ( debug)	System.out.println( "    treeline " + tl.getId());

				if ( tl.getClass().equals( Treeline.class)) {
					if ( tl.getFirstLayer() != null && 
							( currentLayer == null || currentLayer.equals( tl.getFirstLayer())) ) {
						//						if ( debug)	System.out.println( "           as treeline");
						allTreelines.add(tl);
					}
				}
			}
		}
		return allTreelines;
	}

	/** Collect all <code>connector</code>s below a <code>rootstack</code> 
	 * 
	 * @param project
	 * 
	 * @return list of collected  <code>connector</code>s or <code>null</code> if no rootstack found
	 */
	public static List<Connector> getConnectorsBelowRootstacks( Project project) {
		// find all rootstacks
		HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks( project);
		if ( rootstackThings == null) {
			return null;
		}

		// all treelines below a rootstack
		LinkedList<Connector> allConnectors = new LinkedList<Connector>();

		for ( ProjectThing rootstackThing :rootstackThings ) {
			//			if ( debug)	System.out.println("rootstack " + rootstackThing.getId());
			for ( ProjectThing pt : rootstackThing.findChildrenOfTypeR( Connector.class)) {
				Connector conn = (Connector)pt.getObject();
				//				if ( debug)	System.out.println( "    treeline " + tl.getId());
				allConnectors.add(conn);
			}
		}
		return allConnectors;
	}


	/** Code a string to conform to html convention
		 * @param rel_path
		 * @return
		 */
	   public static String htmlCode( String s) {
		   StringBuilder out = new StringBuilder(Math.max(16, s.length()));
		   for (int i = 0; i < s.length(); i++) {
			   char c = s.charAt(i);
			   if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
				   out.append("&#");
				   out.append((int) c);
				   out.append(';');
			   } else {
				   out.append(c);
			   }
		   }
		   return out.toString();
	   }
 
		/** Get the tube part of an ICAP image filename
		 * 
		 * @param filename
		 * @return
		 */
		public static String getICAPTube(String filename) {
			String[] s = splitICAP( filename);
			if ( s == null ) 
				return NA_String;
			else
				return s[1];

		}

		/** Get the experiment part of an ICAP image filename
		 * 
		 * @param filename
		 * @return
		 */
		public static String getICAPExperiment(String filename) {
			String[] s = splitICAP( filename);
			if ( s == null ) 
				return NA_String;
			else
				return s[0];
		}

		/** Get the time point part of an ICAP image filename
		 * 
		 * @param filename
		 * @return
		 */
		public static String getICAPTimepoint(String filename) {
			String[] s = splitICAP( filename);
			if ( s == null ) 
				return NA_String;
			else
				return s[5];
		}

		/** Get the experiment part of an ICAP image filename
		 * date
		 * @param filename
		 * @return
		 */
		public static String getICAPDate(String filename) {
			String[] s = splitICAP( filename);
			if ( s == null ) 
				return NA_String;
			else
				return s[3];
		}

		/** splits into ICAP part
		 * 
		 * @param filename
		 * @return ICAP parts or <code>null</code>
		 */
		private static String[] splitICAP( String filename) {
			if ( filename == null)
				return null;
			else {
				String[] s = filename.split( String.valueOf( DEFAULT_IMAGENAME_SEPARATOR));
				if ( s.length == 7) {
					return s;
				} else {
					return null;
				}
			}
		}
		
		public static boolean segmentsExist(Project project, int i)
		{
			HashSet<ProjectThing> rootStackThings = RhizoUtils.getRootstacks(project);
			if(null != rootStackThings)
			{
				for(ProjectThing rootStackThing: rootStackThings) 
				{
					for(ProjectThing pt: rootStackThing.findChildrenOfTypeR(Treeline.class)) 
					{
						Treeline tl = (Treeline) pt.getObject();

						for(Node<Float> n: tl.getRoot().getSubtreeNodes())
						{
							if(n.getConfidence() ==(byte) i) return true;
						}
					}
				}
			}

			return false;
		}
		
		public static void setSegmentsStatus(Project project, int i, byte confidence)
		{
			HashSet<ProjectThing> rootStackThings = RhizoUtils.getRootstacks(project);
			if(null != rootStackThings)
			{
				for(ProjectThing rootStackThing: rootStackThings) 
				{
					for(ProjectThing pt: rootStackThing.findChildrenOfTypeR(Treeline.class)) 
					{
						Treeline tl = (Treeline) pt.getObject();

						for(Node<Float> n: tl.getRoot().getSubtreeNodes())
						{
							if(n.getConfidence() ==(byte) i) n.setConfidence(confidence);
						}
					}
				}
			}
		}

		/** Print the project tree to stdout
		 * @param project
		 */
		public static void printProjecttree( Project project) {
			printDefaultMutableTreeNode( project.getProjectTree().getRoot(), "");
		}

		/** Recursively prints the <code>DefaultMutableTreeNode node</code> and its content to stdout.
		 *  The indentation <code>indent</code> is incremented as we recurse into the tree
		 * @param node
		 * @param indent
		 */
		private static void printDefaultMutableTreeNode( DefaultMutableTreeNode node, String indent) {
			System.out.println( indent + node + " class "  + node.getClass() + " hash " + node.hashCode());
			printProjectThing( (ProjectThing)node.getUserObject(), indent+" +");
			Enumeration<?> enum_nodes = node.children();
			while (enum_nodes.hasMoreElements()) {
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enum_nodes.nextElement();
				printDefaultMutableTreeNode( currentNode, indent+"    ");
			}
		}
		
		/** Print the content pf the PorjectThing to stdout
		 * @param pt
		 * @param indent
		 */
		private static void printProjectThing( ProjectThing pt, String indent) {
			ProjectThing parent =  (ProjectThing) pt.getParent();
			System.out.println( indent + pt.getId() + " hash " + pt.hashCode() + " object " + pt.getObject() + 
					" hash " + pt.getObject().hashCode() + 
					" (" + pt.getObject().getClass() + ")" +
					", parent " + (parent != null ? parent.getId() : "null" ));
		
		}

		/** Transform coordinates of <code>srcPoint</code> which are relative to the transformation of treeline <code>tl</code> to
		 * absolute coordinates
		 * 
		 * @param srcPoint
		 * @param tl
		 * @return
		 */
		public static Point2D.Float getAbsoluteTreelineCoordinates( Point2D.Float srcPoint, Treeline tl ) {
			AffineTransform at = tl.getAffineTransform();
			return (Point2D.Float) at.transform( srcPoint, null);
		}

		/** Transform coordinates <code>(x,y)</code> which are relative to the transformation of treeline <code>tl</code> to
		 * absolute coordinates
		 * 
		 * @param x
		 * @param y
		 * @param tl
		 * @return
		 */
		public static Point2D.Float getAbsoluteTreelineCoordinates( float x, float y, Treeline tl ) {
			return getAbsoluteTreelineCoordinates( new Point2D.Float( x, y), tl);
		}
		
		/** Transform absolute coordinates of <code>srcPoint</code> in relative coordinates of treeline <code>tl</code>
		 * 
		 * @param srcPoint
		 * @param tl
		 * @return
		 * @throws NoninvertibleTransformException if the transformation of <code>tl</code> is not invertible
		 */
		public static Point2D.Float getRelativeTreelineCoordinates( Point2D.Float srcPoint, Treeline tl ) throws NoninvertibleTransformException  {
			AffineTransform at = tl.getAffineTransform();
			return (Point2D.Float) at.inverseTransform( srcPoint, null);
		}
		
		/** Transform absolute coordinates <code>(x,y)</code> in relative coordinates of treeline <code>tl</code>
		
		 * @param x
		 * @param y
		 * @param tl
		 * @return
		 * @throws NoninvertibleTransformException if the transformation of <code>tl</code> is not invertible
		 */
		public static Point2D.Float getRelativeTreelineCoordinates( float x, float y, Treeline tl ) throws NoninvertibleTransformException  {
			AffineTransform at = tl.getAffineTransform();
			return (Point2D.Float) at.inverseTransform( new Point2D.Float( x, y), null);
		}
		
		/**
		 * Returns the image name associated with the first patch in a layer
		 * @param layer
		 * @return
		 */
		public static String getImageName(Layer layer)
		{
			LayerSet layerSet = layer.getParent();
			List<Patch> patches = layerSet.getAll(Patch.class);
			
			for(Patch patch: patches) 	
			{
				if(patch.getLayer().equals(layer)) 
				{
					ImagePlus ip = patch.getImagePlus();
					if(ip != null) return ip.getTitle();
				}
			}
			
			return null;
		}
		
		/** Return the timepoint for the <code>layer</code>
		 * @param layer
		 * @return
		 */
		public static int getTimepointForLayer( Layer layer) {
			return (int)layer.getZ()+1;
		}
		
		/**		 
		 * calculate the sha-hash of the image on the layer
		 * @param ip
		 * @return
		 */
		public static String calculateSHA256(String pathString) {
			String result="";
			try {
				Path path = Paths.get(pathString);
				byte[] fileByteArray = Files.readAllBytes(path);
				MessageDigest dige = MessageDigest.getInstance("SHA-256");
				byte[] shaHash = dige.digest(fileByteArray);	
				result=Base64.getEncoder().encodeToString(shaHash);
			} catch (NoSuchAlgorithmException | IOException e) {
				Utils.log2("unable to create sha-256 code for path"+pathString);
			}
			System.out.println(result);
			return result;
		}

		/** Add the defined number of Displayables to the project, e.g. type='treeline'
		 * @param project
		 * @param type
		 * @param count
		 */	
		public static List<Displayable> addDisplayableToProject(Project project,String type,int count){
			ArrayList<Displayable> result = new ArrayList<>();
			// find one rootstack
			ProjectThing rootstackProjectThing = RhizoUtils.getOneRootstack(project);
			if ( rootstackProjectThing == null ) {
				Utils.showMessage( "Create treeline: WARNING  can not find a rootstack in project tree");
				return null;	
			}
			
			project.getRootLayerSet().addChangeTreesStep();
			final ArrayList<ProjectThing> addedList = rootstackProjectThing.createChildren(type, count, true);		
			project.getProjectTree().addLeafs(addedList, new Runnable() {
				public void run() {
					project.getRootLayerSet().addChangeTreesStep();
				}});
			
			for (ProjectThing projectThing : addedList) {
				result.add((Displayable) projectThing.getObject());
			}
			return result;
		}
		
		/**
		 * Checks if there is at least on treeline in the specified layer.
		 * @param rootstackThings Set of rootstacks
		 * @param layer Layer to be checked
		 * @author Tino
		 */
		public static boolean areTreelinesInLayer(HashSet<ProjectThing> rootstacks, Layer layer)
		{
			for(ProjectThing rootstackThing: rootstacks) 
			{
				for(ProjectThing pt: rootstackThing.findChildrenOfTypeR(Treeline.class)) 
				{
					Treeline ctree = (Treeline) pt.getObject();
					if(ctree.getFirstLayer() != null && layer.equals(ctree.getFirstLayer())) return true;
				}
			}
					// we also find connectors!
			return false;
		}

		/**
		 * Deletes all treelines from the specified layer
		 * @param layer Layer from which all treelines should be deleted
		 * @author Tino 
		 */
		public static void deleteAllTreelinesFromLayer(Layer layer, Project project)
		{
			if(layer == null) 
			{
				Utils.showMessage("rhizoTrak", "Deleting treelines failed: internal error, can not find layer.");
				return;
			}

			HashSet<ProjectThing> rootstackThings = RhizoUtils.getRootstacks( project);
			if(rootstackThings == null) 
			{
				Utils.showMessage( "rhizoTrak", "Deleting treelines failed: no rootstack found.");
				return;
			}
			
			Set<Displayable> set = new HashSet<Displayable>();
			
			for(ProjectThing rootstackThing :rootstackThings) 
			{
				for(ProjectThing pt: rootstackThing.findChildrenOfTypeR(Treeline.class)) 
				{
					Treeline ctree = (Treeline) pt.getObject();

					if(ctree.getClass().equals(Treeline.class)) 
					{
						if(ctree.getFirstLayer() != null && layer.equals(ctree.getFirstLayer())) set.add(ctree);
					}
				}
			}
			
			if(project.removeAll(set)) Utils.log("deleted "+set.size()+" treelines");
		}

		/*
		 * helper to repaint a List of Treelines in their first Layer
		 * @param displayableList
		 */
		public static void repaintTreelineList(List<Displayable> displayableList){
			for (Displayable displayable : displayableList) {
				if(displayable instanceof Treeline) {
					Treeline treeline = (Treeline) displayable;
					if(treeline.getFirstLayer()==null) return;
					treeline.repaint(true, treeline.getFirstLayer());
				}
			}
		}
		
}
