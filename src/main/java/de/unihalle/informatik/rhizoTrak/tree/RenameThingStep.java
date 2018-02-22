package de.unihalle.informatik.rhizoTrak.tree;

import de.unihalle.informatik.rhizoTrak.display.Displayable;
import de.unihalle.informatik.rhizoTrak.display.DoStep;

public class RenameThingStep implements DoStep {
	TitledThing thing;
	String title;
	RenameThingStep(TitledThing thing) {
		this.thing = thing;
		this.title = thing.getTitle();
	}
	public boolean apply(int action) {
		thing.setTitle(title);
		return true;
	}
	public boolean isEmpty() { return false; }
	public Displayable getD() { return null; }
	public boolean isIdenticalTo(Object ob) {
		if (!(ob instanceof RenameThingStep)) return false;
		RenameThingStep rn = (RenameThingStep) ob;
		return rn.thing == this.thing && rn.title == this.title;
	}
}
