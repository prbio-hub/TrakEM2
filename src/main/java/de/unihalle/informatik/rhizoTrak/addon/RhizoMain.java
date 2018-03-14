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
    * Used for disposing JFrames when closing the control window
    * @return The image loader JFrame
    */
   public void disposeGUIs()
   {
   		rCV.disposeColorVisibilityFrame();
   		rI.disposeImageLoaderFrame();
   		rA.getConflictManager().disposeConflictFrame();
   }

}
