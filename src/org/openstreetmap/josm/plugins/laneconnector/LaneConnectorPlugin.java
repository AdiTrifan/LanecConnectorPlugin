package org.openstreetmap.josm.plugins.laneconnector;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class LaneConnectorPlugin extends Plugin {
	LaneRelationsAction action = null;

	/**
	 * constructor
	 */
	public LaneConnectorPlugin(PluginInformation info) {
		super(info);
		Main.main.menu.dataMenu.addSeparator();
		action = new LaneRelationsAction();
		MainMenu.add(Main.main.menu.dataMenu, action, false, 0);

	}

}
