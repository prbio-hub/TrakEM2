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
 

}
