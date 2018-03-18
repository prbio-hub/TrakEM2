package de.unihalle.informatik.rhizoTrak.addon;

import de.unihalle.informatik.rhizoTrak.Project;
import de.unihalle.informatik.rhizoTrak.display.RhizoAddons;

public class RhizoMain
{
	private RhizoAddons rA;
	
	private RhizoIO rIO;
	private RhizoColVis rCV;
	private RhizoImages rI;
	private RhizoStatistics rS;
	private RhizoMTBXML rMTBXML;
	
	private Project p;
	
	/**
	 * The (mainly) project specific configuration
	 */
	private ProjectConfig projectConfig = new ProjectConfig();
	
	public RhizoMain(Project p)
	{
		this.p = p;
		
		rA = new RhizoAddons(this, p);
		
		rCV = new RhizoColVis(this);
		rIO = new RhizoIO(this);
		rI = new RhizoImages(this);
		rS = new RhizoStatistics(this);
		rMTBXML = new RhizoMTBXML(this);
	}
	
	public RhizoAddons getRhizoAddons()
	{
		return rA;
	}
	
	public RhizoIO getRhizoIO()
	{
		return rIO;
	}
	
	public RhizoColVis getRhizoColVis()
	{
		return rCV;
	}
	
	public RhizoImages getRhizoImages()
	{
		return rI;
	}
	
	public RhizoStatistics getRhizoStatistics()
	{
		return rS;
	}
	
	public RhizoMTBXML getRhizoMTBXML()
	{
		return rMTBXML;
	}
	
	public Project getProject()
	{
		return p;
	}
	
    
    /**
	 * @return the projectConfig
	 */
	public ProjectConfig getProjectConfig() {
		return projectConfig;
	}

	/**
    * Used for disposing JFrames when closing the control window
    * @return The image loader JFrame
    */
   public void disposeGUIs()
   {
   		rCV.disposeColorVisibilityFrame();
   		rI.disposeImageLoaderFrame();
   		rA.getConflictManager().disposeConflictFrame();
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
}
