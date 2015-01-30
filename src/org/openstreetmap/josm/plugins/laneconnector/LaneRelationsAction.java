package org.openstreetmap.josm.plugins.laneconnector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class LaneRelationsAction extends JosmAction implements
SelectionChangedListener {

	List<String> values = Arrays.asList("pedestrian", "footway", "cycleway", "bridleway", "steps", "path", "track", "bus_stop","crossing", "services", "service");

	public LaneRelationsAction() {
		super(tr("Edit lanes"), "mapmode/arrow", tr("Edit lanes"), Shortcut
				.registerShortcut("edit:arrow",
						tr("Edit: {0}", tr("Edit lanes")), KeyEvent.VK_E,
						Shortcut.ALT_CTRL), false);
		setEnabled(false);
		DataSet.addSelectionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		new LaneRelationsDialog();
		// LaneRelationsDialog dlg = new LaneRelationsDialog();
		// if (dlg.getValue() == 1) {
		// dlg.saveSettings();
		// }
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		for (OsmPrimitive osm : newSelection) {
			if (osm instanceof Node && newSelection.size() == 1) {
				Node node = (Node) osm;
				DataSet currentDataSet = Main.main.getCurrentDataSet();
				List<Way> drum = new ArrayList<Way>();
				List<Way> drum_bun = new ArrayList<Way>();
				drum.addAll(currentDataSet.getWays());
				for (Way way : drum) {
					if (way instanceof Way && way.isArea() == false
							&& way.containsNode(node)
							&& way.hasTag("highway", values) == false
							&& way.hasKey("building") == false) {
						drum_bun.add(way);
					}
				}
				if (drum_bun.size() >= 2) {
					setEnabled(true);
					return;
				}
			}
		}
		setEnabled(false);
	}

}
