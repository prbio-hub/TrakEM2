package de.unihalle.informatik.rhizoTrak.addon;

import java.util.HashSet;
import java.util.Iterator;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.tree.ProjectThing;
import de.unihalle.informatik.rhizoTrak.tree.ProjectTree;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

/**
 * some helper methods
 * 
 * @author posch
 *
 */
public class RhizoUtils {
	
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
			if ( rootstackProjectThings.size() == 0) {
//				Utils.log( "RhizoUtils:getRootstacks Error can not find a rootstack in project tree");
				return null;
			}
			
			// just take the first node capable to hold treelines
			Iterator<ProjectThing> itr = rootstackProjectThings.iterator();
			ProjectThing rootstackProjectThing = itr.next();
			if ( ! ( rootstackProjectThing.canHaveAsChild( "treeline") && rootstackProjectThing.canHaveAsChild( "connector") ) ) {
//				Utils.log( "RhizoUtils:getRootstacks Error rootstack cannot hold treelies and connectors");
				return null;
			}
				
			return rootstackProjectThings;
		} catch ( Exception ex ) {
//			Utils.log( "RhizoUtils:getRootstacks Error can get root of project things");
			return null;
		}

	}
	
	/**
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
				return "NA";
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
				return "NA";
			else
				return s[0];
		}

		/** Get the experiment part of an ICAP image filename
		 * 
		 * @param filename
		 * @return
		 */
		public static String getICAPTimepoint(String filename) {
			String[] s = splitICAP( filename);
			if ( s == null ) 
				return "NA";
			else
				return s[5];
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
		

}
