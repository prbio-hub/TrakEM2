package de.unihalle.informatik.rhizoTrak.conflictManagement;

import de.unihalle.informatik.rhizoTrak.display.Connector;
import de.unihalle.informatik.rhizoTrak.display.Layer;

public class TreelineConflictKey {
	
	Connector connector;
	Layer layer;
	
	public TreelineConflictKey(Connector connector,Layer layer)
	{
		this.connector = connector;
		this.layer = layer;
	}

	/**
	 * @return the connector
	 */
	public Connector getConnector() {
		return connector;
	}

	/**
	 * @param connector the connector to set
	 */
	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	/**
	 * @return the layer
	 */
	public Layer getLayer() {
		return layer;
	}

	/**
	 * @param layer the layer to set
	 */
	public void setLayer(Layer layer) {
		this.layer = layer;
	}
}
