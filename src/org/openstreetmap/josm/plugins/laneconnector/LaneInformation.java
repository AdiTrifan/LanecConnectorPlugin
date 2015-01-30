package org.openstreetmap.josm.plugins.laneconnector;

import java.awt.geom.Ellipse2D;

import org.openstreetmap.josm.data.osm.Way;

public class LaneInformation {

	private int laneNumber;
	private String laneDirection;
	private Way way;
	private Ellipse2D.Double ellipse;

	public LaneInformation(int laneNumber, String laneDirection, Way way,
			Ellipse2D.Double ellipse) {
		this.laneNumber = laneNumber;
		this.laneDirection = laneDirection;
		this.way = way;
		this.ellipse = ellipse;
	}

	public int getLaneNumber() {
		return laneNumber;
	}

	public void setLaneNumber(int laneNumber) {
		this.laneNumber = laneNumber;
	}

	public String getLaneDirection() {
		return laneDirection;
	}

	public void setLaneDirection(String laneDirection) {
		this.laneDirection = laneDirection;
	}

	public Way getWay() {
		return way;
	}

	public void setWay(Way way) {
		this.way = way;
	}

	public Ellipse2D.Double getEllipse() {
		return ellipse;
	}

	public void setEllipse(Ellipse2D.Double ellipse) {
		this.ellipse = ellipse;
	}

}
