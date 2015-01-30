package org.openstreetmap.josm.plugins.laneconnector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.swing.JComponent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

public class MyGraphicsRestrictions extends JComponent implements
MouseListener, MouseMotionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ArrayList<LaneInformation> laneInfos = new ArrayList<LaneInformation>();

	ArrayList<Relation> relations = new ArrayList<Relation>();

	private final int ARR_SIZE = 8;
	// A canvas where the user can draw lines in various colors.

	private int currentColorIndex; // Color that is currently being used for
	// drawing new lines,
	// given as an index in the ColoredLine.colorList array.

	private int currentBackgroundIndex; // Current background color, given
	// as an
	// index in the
	// ColoredLine.colorList array.

	private static ColoredLine[] lines; // An array to hold all the lines that
	// have
	// been
	// drawn on the canvas.
	private int lineCount; // The number of lines that are in the array.

	MyGraphicsRestrictions() {
		// Construct the canvas, and set it to listen for mouse events.
		// Also create an array to hold the lines that are displayed on
		// the canvas.
		setPreferredSize(new Dimension(700, 500));
		currentColorIndex = 0;
		currentBackgroundIndex = 12;
		lines = new ColoredLine[10000];
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	void setColorIndex(int c) {
		// Set the currentColorIndex, which is used for drawing, to c.
		// For safety, check first that it is in the range of legal indices
		// for the ColoredLine.colorList array.
		if (c >= 0 && c < ColoredLine.colorList.length)
			currentColorIndex = c;
	}

	void setBackgroundIndex(int c) {
		// Set the background color, and redraw the applet using the new
		// background.
		if (c >= 0 && c < ColoredLine.colorList.length) {
			currentBackgroundIndex = c;
			setBackground(ColoredLine.colorList[c]);
			repaint();
		}
	}

	void doClear() {
		// Clear all the lines from the picture.
		if (lineCount > 0) {
			lines = new ColoredLine[1000];
			lineCount = 0;
			repaint();
		}
		String filename = "";
		String path = Main.pref.getPluginsDirectory().getPath();
		filename = path + "\\" + "LaneConnectorFilesRestrictions" + "\\"
				+ "Node_"
				+ String.valueOf(LaneRelationsDialog.getSelectedNode().getId())
				+ ".txt";
		File f = new File(filename);
		if (f.exists() && !f.isDirectory()) {
			try {
				FileOutputStream writer = new FileOutputStream(filename);
				// writer.write((new String()).getbytes());
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException ioe) {
				System.err.println("IOException: " + ioe.getMessage());
			}
		}
		repaint();
	}

	void doUndo() {
		// Remove most recently added line from the picture.
		if (lineCount > 0) {
			String line = "";
			line = String.valueOf(lines[lineCount - 1].x1) + " "
					+ String.valueOf(lines[lineCount - 1].y1) + " "
					+ String.valueOf(lines[lineCount - 1].x2) + " "
					+ String.valueOf(lines[lineCount - 1].y2) + " ";
			lineCount--;
			repaint();
			String filename = "";
			String path = Main.pref.getPluginsDirectory().getPath();
			filename = path
					+ "\\"
					+ "LaneConnectorFilesRestrictions"
					+ "\\"
					+ "Node_"
					+ String.valueOf(LaneRelationsDialog.getSelectedNode()
							.getId()) + ".txt";
			removeLineFromFile(filename, line);

		}
	}

	public void removeLineFromFile(String file, String lineToRemove) {

		try {

			File inFile = new File(file);

			if (!inFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}

			// Construct the new file that will later be renamed to the original
			// filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			// Read from the original file and write to the new
			// unless content matches data to be removed.
			while ((line = br.readLine()) != null) {

				if (!line.trim().equals(lineToRemove)) {

					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();

			// Delete the original file
			if (!inFile.delete()) {
				System.out.println("Could not delete file");
				return;
			}

			// Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				System.out.println("Could not rename file");

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void makeDir() throws IOException {
		String path = Main.pref.getPluginsDirectory().getPath();
		File directory = new File(path + "\\"
				+ "LaneConnectorFilesRestrictions");
		if (directory.exists() || directory.isFile()) {
			System.out.println("The dir with name could not be"
					+ " created as it is a normal file");
		} else {
			if (!directory.exists()) {
				directory.mkdir();
			}
		}
	}

	void doSaveToFile(String fname) {
		// Save all the data for the current drawing to a file.

		try {
			makeDir();
			String filename = "";
			String path = Main.pref.getPluginsDirectory().getPath();
			filename = path + "\\" + "LaneConnectorFilesRestrictions" + "\\"
					+ "Node_" + fname + ".txt";
			// filename = System.getProperty("user.home") + "\\" + "Node_" +
			// fname
			// + ".txt";
			BufferedWriter fw = new BufferedWriter(new FileWriter(filename,
					true)); // the true
			// will
			// append
			// the new
			// data
			if (lineCount > 0) {
				for (int i = 0; i < lineCount; i++) { // Write the data for each
					// individual line.
					fw.write(String.valueOf(lines[i].x1) + "\n" + " ");
					fw.write(String.valueOf(lines[i].y1) + "\n" + " ");
					fw.write(String.valueOf(lines[i].x2) + "\n" + " ");
					fw.write(String.valueOf(lines[i].y2) + "\n" + " ");
					fw.newLine();
				}
			}
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}

		// new
		// MessageDialog(parentFrame,"Some error occured while trying to save data to the file.");

	} // end doSaveToFile()

	ColoredLine[] doLoadFromFile() {
		ColoredLine[] lin = new ColoredLine[0];
		String filename = "";
		String path = Main.pref.getPluginsDirectory().getPath();
		filename = path + "\\" + "LaneConnectorFilesRestrictions" + "\\"
				+ "Node_"
				+ String.valueOf(LaneRelationsDialog.getSelectedNode().getId())
				+ ".txt";
		// filename = System.getProperty("user.home") + "\\" + "Node_"
		// + String.valueOf(LaneRelationsDialog.getSelectedNode().getId())
		// + ".txt ";
		File f = new File(filename);
		if (f.exists() && !f.isDirectory()) {
			try {
				BufferedReader fr = new BufferedReader(new FileReader(filename)); // the
				// true
				// will
				// append
				// the new
				// data
				Scanner sc = new Scanner(new FileReader(filename));
				ColoredLine[] l = new ColoredLine[1000];
				int j = 0;
				while (sc.hasNext()) {
					l[j] = new ColoredLine();
					String x1 = sc.next();
					System.out.println("x1 " + x1);
					l[j].x1 = Integer.parseInt(x1);

					String y1 = sc.next();
					System.out.println("y1 " + y1);
					l[j].y1 = Integer.parseInt(y1);

					String x2 = sc.next();
					System.out.println("x2 " + x2);
					l[j].x2 = Integer.parseInt(x2);

					String y2 = sc.next();
					System.out.println("y2 " + y2);
					l[j].y2 = Integer.parseInt(y2);
					l[j].colorIndex = 0;
					j++;
				}
				lin = new ColoredLine[j];
				for (int i = 0; i < j; i++) {
					lin[i] = new ColoredLine();
					lin[i].x1 = l[i].x1;
					lin[i].y1 = l[i].y1;
					lin[i].x2 = l[i].x2;
					lin[i].y2 = l[i].y2;
					lin[i].colorIndex = l[i].colorIndex;
				}

				for (int i = 0; i < lin.length; i++) {
					System.out.println(lin[i].x1 + " " + lin[i].y1 + " "
							+ lin[i].x2 + " " + lin[i].y2);

				}
				System.out.println(j);
				fr.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException ioe) {
				System.err.println("IOException: " + ioe.getMessage());
			}
		}
		return lin;
	}

	public boolean checkNodes(Way way) {
		boolean isOk = true;
		int index = 0;
		for (int i = 0; i < way.getNodesCount(); i++) {
			Node node = way.getNode(i);
			if (node.isOutsideDownloadArea()) {
				index += 1;
			}
		}
		if (way.getNodesCount() - index < 4) {
			isOk = false;
		}

		return isOk;
	}

	public void drawDashedLine(Graphics g, double x1, double y1, double x2,
			double y2) {

		// creates a copy of the Graphics instance
		Graphics2D g2D = (Graphics2D) g.create();

		Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
		g2D.setStroke(dashed);
		Path2D path;
		path = new Path2D.Double();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		g2D.draw(path);

		// gets rid of the copy
		g2D.dispose();
	}

	@SuppressWarnings("unused")
	@Override
	public void paint(Graphics g) {
		boolean haskeys = true;
		// Redraw all the lines.
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect(50, 0, 600, 500);
		// g2d.setColor(Color.BLACK);
		// g2d.setStroke(new BasicStroke(5));
		// Point in the middle of the junction; reference point
		// g2d.drawLine(350, 250, 350, 250);
		int size = LaneRelationsDialog.getJunctionWays().size();
		for (int k = 0; k < size; k++) {
			haskeys = true;
			int index = k;
			Way w = LaneRelationsDialog.getJunctionWays().get(index);
			if (!w.hasKey("lanes") || !w.hasKey("lanes:forward")
					|| !w.hasKey("lanes:backward") || !checkNodes(w)) {
				haskeys = false;
				break;
			}
		}
		if (haskeys == false) {
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(5));
			g2d.drawString(
					"Please make sure to first enter all the neccessary information for all the ways or download a bigger area!",
					100, 250);
		}
		if (haskeys == true) {

			g2d.setColor(Color.RED);
			g2d.setStroke(new BasicStroke(5));
			//Point in the middle of the junction; reference point
			g2d.drawLine(350, 250, 350, 250);

			g2d.setStroke(new BasicStroke(2));
			// g2d.drawRect(320, 220, 60, 60);
			List<Integer> lanes = new ArrayList<Integer>();
			for (int k = 0; k < size; k++) {
				int index = k;
				lanes.add(Integer.parseInt(LaneRelationsDialog
						.getJunctionWays().get(index).get("lanes")));
			}
			Collections.sort(lanes);
			int upperReference = 350 - 15 * lanes.get(lanes.size() - 1);
			int lowerReference = 350 + 15 * lanes.get(lanes.size() - 1);
			int leftReference = 250 - 15 * lanes.get(lanes.size() - 1);
			int rightReference = 250 + 15 * lanes.get(lanes.size() - 1);

			size = LaneRelationsDialog.getJunctionWays().size();
			for (int k = 0; k < size; k++) {
				int index = k;
				Way w = LaneRelationsDialog.getJunctionWays().get(index);
				// check if way begins or ends at junction
				if (w.isFirstLastNode(LaneRelationsDialog.getSelectedNode())) {
					Node firstn = w.firstNode();
					// check if way begins at junction
					if (firstn.equals(LaneRelationsDialog.getSelectedNode())) {

						Path2D path;
						path = new Path2D.Double();
						double x = 350;
						double y = 250;

						double xprev = 350;
						double yprev = 250;

						double xl = 0;
						double yl = 0;

						path.moveTo(x, y);

						for (int i = 1; i < w.getNodesCount(); i++) {
							int j = i;
							Node before = w.getNode(j - 1);
							Node after = w.getNode(j);

							if (after.isOutsideDownloadArea() == false
									&& j != w.getNodesCount() - 1) {
								double latafter = after.getCoor().lat();
								double lonafter = after.getCoor().lon();

								double latbefore = before.getCoor().lat();
								double lonbefore = before.getCoor().lon();

								double latdif = latbefore - latafter;

								double londif = lonbefore - lonafter;

								path.lineTo(x - latdif, y - londif);

								xprev = x;
								yprev = y;

								x = xprev - latdif;
								y = yprev - londif;

								path.moveTo(x, y);
							} else if (!after.isOutsideDownloadArea()
									&& j == w.getNodesCount() - 1) {
								double latafter = after.getCoor().lat();
								double lonafter = after.getCoor().lon();

								double latbefore = before.getCoor().lat();
								double lonbefore = before.getCoor().lon();

								double latdif = latbefore - latafter;

								double londif = lonbefore - lonafter;

								double xdif = 0, ydif = 0;

								if (latdif > 0) {
									xdif = x - (latdif + 200);
								} else {
									xdif = x - (latdif - 200);
								}

								if (londif > 0) {
									ydif = y - (londif + 200);
								}

								else {
									ydif = y - (londif - 200);
								}

								path.lineTo(xdif, ydif);

								xprev = x;
								yprev = y;

								x = xdif;
								y = ydif;

								path.moveTo(x, y);
							} else {
								double latbefore = before.getCoor().lat();
								double lonbefore = before.getCoor().lon();

								double latdif = xprev - latbefore;
								double londif = yprev - lonbefore;

								double xdif = 0, ydif = 0;

								if (latdif > 0) {
									xdif = xprev - (latdif + 200);
								} else {
									xdif = xprev - (latdif - 200);
								}

								if (londif > 0) {
									ydif = yprev - (londif + 200);
								} else {
									ydif = yprev - (londif - 200);
								}

								path.moveTo(x, y);
								path.lineTo(x - xdif, y - ydif);
								x = x - xdif;
								y = y - ydif;

							}
						}

						xl = x;
						yl = y;

						g2d.setStroke(new BasicStroke(1));
						g2d.setColor(Color.BLACK);
						//g2d.draw(path);

						double dx = 350 * 0.70 + x * 0.30;
						double dy = 250 * 0.70 + y * 0.30;
						Path2D pa;
						pa = new Path2D.Double();
						pa.moveTo(x, y);
						pa.lineTo(dx, dy);
						g2d.setStroke(new BasicStroke(3));
						g2d.setColor(Color.BLACK);
						g2d.draw(pa);

						// draw name
						if (x < 350 && y < 250) {
							g2d.drawString(w.getName(), (float) (x - 5),
									(float) (y - 5));
						} else if (x > 350 && y < 250) {
							g2d.drawString(w.getName(), (float) (x - 5),
									(float) (y + 5));
						} else if (x < 350 && y > 250) {
							g2d.drawString(w.getName(), (float) (x + 5),
									(float) (y + 5));
						} else if (x > 350 && y > 250) {
							g2d.drawString(w.getName(), (float) (x + 5),
									(float) (y + 5));
						}

						double xcp1 = x;
						double ycp1 = y;
						double xcp2 = dx;
						double ycp2 = dy;

						// draw lanes
						if (w.hasKey("lanes:forward")) {

							double l = Math.sqrt((x - dx) * (x - dx) + (y - dy)
									* (y - dy));

							double offsetPixels = 20;

							double x1p = 0;
							double y1p = 0;
							double x2p = 0;
							double y2p = 0;

							double xinit = x;
							double yinit = y;

							double xaux = 0;
							double yaux = 0;

							Path2D plf;
							plf = new Path2D.Double();

							for (int ind = 0; ind < Integer.parseInt(w
									.get("lanes:forward")); ind++) {
								x1p = xinit + offsetPixels * (dy - yinit) / l;
								x2p = dx + offsetPixels * (dy - yinit) / l;
								y1p = yinit + offsetPixels * (xinit - dx) / l;
								y2p = dy + offsetPixels * (xinit - dx) / l;

								xprev = dx;
								yprev = dy;

								g2d.setColor(Color.WHITE);
								g2d.setStroke(new BasicStroke(2));
								drawDashedLine(g2d, x1p, y1p, x2p, y2p);

								g2d.setStroke(new BasicStroke(1));
								g2d.setColor(Color.WHITE);
								g2d.fill(new Ellipse2D.Double(
										(xprev + x2p) / 2, (yprev + y2p) / 2,
										9, 9));

								LaneInformation info = new LaneInformation(
										Integer.parseInt(w.get("lanes:forward"))
										- ind, "forward", w,
										new Ellipse2D.Double((xprev + x2p) / 2,
												(yprev + y2p) / 2, 9, 9));
								laneInfos.add(info);

								xinit = x1p;
								yinit = y1p;
								dx = x2p;
								dy = y2p;
							}
							x1p = xinit + 5 * (dy - yinit) / l;
							x2p = dx + 5 * (dy - yinit) / l;
							y1p = yinit + 5 * (xinit - dx) / l;
							y2p = dy + 5 * (xinit - dx) / l;

							g2d.setStroke(new BasicStroke(2));
							g2d.setColor(Color.BLACK);
							plf.moveTo(x1p, y1p);
							plf.lineTo(x2p, y2p);
							g2d.draw(plf);
						}

						if (w.hasKey("lanes:backward")) {
							double l = Math.sqrt((xcp1 - xcp2) * (xcp1 - xcp2)
									+ (ycp1 - ycp2) * (ycp1 - ycp2));

							double offsetPixels = 20;

							double x1p = 0;
							double y1p = 0;
							double x2p = 0;
							double y2p = 0;

							double xinit = xcp1;
							double yinit = ycp1;

							double xaux = 0;
							double yaux = 0;

							Path2D plf;
							plf = new Path2D.Double();

							for (int ind = 0; ind < Integer.parseInt(w
									.get("lanes:backward")); ind++) {

								x1p = xinit - offsetPixels * (ycp2 - yinit) / l;
								x2p = xcp2 - offsetPixels * (ycp2 - yinit) / l;
								y1p = yinit - offsetPixels * (xinit - xcp2) / l;
								y2p = ycp2 - offsetPixels * (xinit - xcp2) / l;

								xprev = xcp2;
								yprev = ycp2;

								g2d.setColor(Color.WHITE);
								g2d.setStroke(new BasicStroke(2));
								drawDashedLine(g2d, x1p, y1p, x2p, y2p);

								g2d.setStroke(new BasicStroke(1));
								g2d.setColor(Color.BLACK);
								g2d.fill(new Ellipse2D.Double(
										(xprev + x2p) / 2, (yprev + y2p) / 2,
										9, 9));

								LaneInformation info = new LaneInformation(
										Integer.parseInt(w
												.get("lanes:backward")) - ind,
												"backward", w, new Ellipse2D.Double(
														(xprev + x2p) / 2,
														(yprev + y2p) / 2, 9, 9));
								laneInfos.add(info);

								xinit = x1p;
								yinit = y1p;
								xcp2 = x2p;
								ycp2 = y2p;
							}
							x1p = xinit - 5 * (ycp2 - yinit) / l;
							x2p = xcp2 - 5 * (ycp2 - yinit) / l;
							y1p = yinit - 5 * (xinit - xcp2) / l;
							y2p = ycp2 - 5 * (xinit - xcp2) / l;

							g2d.setStroke(new BasicStroke(2));
							g2d.setColor(Color.BLACK);
							plf.moveTo(x1p, y1p);
							plf.lineTo(x2p, y2p);
							g2d.draw(plf);
						}
					}
					// way ends at junction
					else {
						Node lastn = w.lastNode();
						if (lastn.equals(LaneRelationsDialog.getSelectedNode())) {
							Path2D path;
							path = new Path2D.Double();
							double x = 350;
							double y = 250;

							double xprev = 350;
							double yprev = 250;

							double xl = 0;
							double yl = 0;

							path.moveTo(x, y);

							for (int i = w.getNodesCount() - 2; i >= 0; i--) {
								int j = i;
								Node after = w.getNode(j);

								if (after.isOutsideDownloadArea() == false
										&& j != 0) {
									Node before = w.getNode(j + 1);

									double latafter = after.getCoor().lat();
									double lonafter = after.getCoor().lon();

									double latbefore = before.getCoor().lat();
									double lonbefore = before.getCoor().lon();

									double latdif = latbefore - latafter;

									double londif = lonbefore - lonafter;

									path.lineTo(x - latdif, y - londif);

									xprev = x;
									yprev = y;

									x = xprev - latdif;
									y = yprev - londif;

									path.moveTo(x, y);
								} else if (!after.isOutsideDownloadArea()
										&& j == 0) {
									Node before = w.getNode(j + 1);

									double latafter = after.getCoor().lat();
									double lonafter = after.getCoor().lon();

									double latbefore = before.getCoor().lat();
									double lonbefore = before.getCoor().lon();

									double latdif = latbefore - latafter;

									double londif = lonbefore - lonafter;

									double xdif = 0, ydif = 0;

									if (latdif > 0) {
										xdif = x - (latdif + 200);
									} else {
										xdif = x - (latdif - 200);
									}

									if (londif > 0) {
										ydif = y - (londif + 200);
									}

									else {
										ydif = y - (londif - 200);
									}

									path.lineTo(xdif, ydif);

									xprev = x;
									yprev = y;

									x = xdif;
									y = ydif;

									path.moveTo(x, y);
								} else {
									Node before = w.getNode(j + 1);

									double latbefore = before.getCoor().lat();
									double lonbefore = before.getCoor().lon();

									double latdif = xprev - latbefore;
									double londif = yprev - lonbefore;

									double xdif = 0, ydif = 0;

									if (latdif > 0) {
										xdif = xprev - (latdif + 200);
									} else {
										xdif = xprev - (latdif - 200);
									}

									if (londif > 0) {
										ydif = yprev - (londif + 200);
									} else {
										ydif = yprev - (londif - 200);
									}

									path.moveTo(x, y);
									path.lineTo(x - xdif, y - ydif);
									x = x - xdif;
									y = y - ydif;

								}
							}

							xl = x;
							yl = y;

							g2d.setStroke(new BasicStroke(1));
							g2d.setColor(Color.BLACK);
							//g2d.draw(path);

							double dx = 350 * 0.70 + x * 0.30;
							double dy = 250 * 0.70 + y * 0.30;
							Path2D pa;
							pa = new Path2D.Double();
							pa.moveTo(x, y);
							pa.lineTo(dx, dy);
							g2d.setStroke(new BasicStroke(3));
							g2d.setColor(Color.BLACK);
							g2d.draw(pa);

							// draw name
							if (x < 350 && y < 250) {
								g2d.drawString(w.getName(), (float) (x - 5),
										(float) (y - 5));
							} else if (x > 350 && y < 250) {
								g2d.drawString(w.getName(), (float) (x - 5),
										(float) (y + 5));
							} else if (x < 350 && y > 250) {
								g2d.drawString(w.getName(), (float) (x + 5),
										(float) (y + 5));
							} else if (x > 350 && y > 250) {
								g2d.drawString(w.getName(), (float) (x + 5),
										(float) (y + 5));
							}

							double xcp1 = x;
							double ycp1 = y;
							double xcp2 = dx;
							double ycp2 = dy;

							// draw lanes
							if (w.hasKey("lanes:forward")) {

								double l = Math.sqrt((x - dx) * (x - dx)
										+ (y - dy) * (y - dy));

								double offsetPixels = 20;

								double x1p = 0;
								double y1p = 0;
								double x2p = 0;
								double y2p = 0;

								double xinit = x;
								double yinit = y;

								double xaux = 0;
								double yaux = 0;

								Path2D plf;
								plf = new Path2D.Double();

								for (int ind = 0; ind < Integer.parseInt(w
										.get("lanes:forward")); ind++) {
									x1p = xinit + offsetPixels * (dy - yinit)
											/ l;
									x2p = dx + offsetPixels * (dy - yinit) / l;
									y1p = yinit + offsetPixels * (xinit - dx)
											/ l;
									y2p = dy + offsetPixels * (xinit - dx) / l;

									xprev = dx;
									yprev = dy;

									g2d.setColor(Color.WHITE);
									g2d.setStroke(new BasicStroke(2));
									drawDashedLine(g2d, x1p, y1p, x2p, y2p);

									g2d.setStroke(new BasicStroke(1));
									g2d.setColor(Color.WHITE);
									g2d.fill(new Ellipse2D.Double(
											(xprev + x2p) / 2,
											(yprev + y2p) / 2, 9, 9));

									LaneInformation info = new LaneInformation(
											Integer.parseInt(w
													.get("lanes:forward"))
													- ind, "forward", w,
													new Ellipse2D.Double(
															(xprev + x2p) / 2,
															(yprev + y2p) / 2, 9, 9));
									laneInfos.add(info);

									xinit = x1p;
									yinit = y1p;
									dx = x2p;
									dy = y2p;
								}
								x1p = xinit + 5 * (dy - yinit) / l;
								x2p = dx + 5 * (dy - yinit) / l;
								y1p = yinit + 5 * (xinit - dx) / l;
								y2p = dy + 5 * (xinit - dx) / l;

								g2d.setStroke(new BasicStroke(2));
								g2d.setColor(Color.BLACK);
								plf.moveTo(x1p, y1p);
								plf.lineTo(x2p, y2p);
								g2d.draw(plf);
							}

							if (w.hasKey("lanes:backward")) {
								double l = Math.sqrt((xcp1 - xcp2)
										* (xcp1 - xcp2) + (ycp1 - ycp2)
										* (ycp1 - ycp2));

								double offsetPixels = 20;

								double x1p = 0;
								double y1p = 0;
								double x2p = 0;
								double y2p = 0;

								double xinit = xcp1;
								double yinit = ycp1;

								double xaux = 0;
								double yaux = 0;

								Path2D plf;
								plf = new Path2D.Double();

								for (int ind = 0; ind < Integer.parseInt(w
										.get("lanes:backward")); ind++) {

									x1p = xinit - offsetPixels * (ycp2 - yinit)
											/ l;
									x2p = xcp2 - offsetPixels * (ycp2 - yinit)
											/ l;
									y1p = yinit - offsetPixels * (xinit - xcp2)
											/ l;
									y2p = ycp2 - offsetPixels * (xinit - xcp2)
											/ l;

									xprev = xcp2;
									yprev = ycp2;

									g2d.setColor(Color.WHITE);
									g2d.setStroke(new BasicStroke(2));
									drawDashedLine(g2d, x1p, y1p, x2p, y2p);

									g2d.setStroke(new BasicStroke(1));
									g2d.setColor(Color.BLACK);
									g2d.fill(new Ellipse2D.Double(
											(xprev + x2p) / 2,
											(yprev + y2p) / 2, 9, 9));

									LaneInformation info = new LaneInformation(
											Integer.parseInt(w
													.get("lanes:backward"))
													- ind, "backward", w,
													new Ellipse2D.Double(
															(xprev + x2p) / 2,
															(yprev + y2p) / 2, 9, 9));
									laneInfos.add(info);

									xinit = x1p;
									yinit = y1p;
									xcp2 = x2p;
									ycp2 = y2p;
								}
								x1p = xinit - 5 * (ycp2 - yinit) / l;
								x2p = xcp2 - 5 * (ycp2 - yinit) / l;
								y1p = yinit - 5 * (xinit - xcp2) / l;
								y2p = ycp2 - 5 * (xinit - xcp2) / l;

								g2d.setStroke(new BasicStroke(2));
								g2d.setColor(Color.BLACK);
								plf.moveTo(x1p, y1p);
								plf.lineTo(x2p, y2p);
								g2d.draw(plf);
							}
						}
					}
				}
				// way goes through junction
				else {
					int inde = 0;
					for (int q = 0; q < w.getNodesCount(); q++) {
						inde = q;
						if (w.getNodes().get(inde)
								.equals(LaneRelationsDialog.getSelectedNode()))
							break;
					}

					// predecesor node
					Node beforen = w.getNodes().get(inde - 1);

					Path2D path;
					path = new Path2D.Double();
					double x = 350;
					double y = 250;

					double xprev = 350;
					double yprev = 250;

					double xl = 0;
					double yl = 0;

					path.moveTo(x, y);

					for (int i = inde - 1; i >= 0; i--) {
						int j = i;
						Node after = w.getNode(j);

						if (after.isOutsideDownloadArea() == false && j != 0) {
							Node before = w.getNode(j + 1);

							double latafter = after.getCoor().lat();
							double lonafter = after.getCoor().lon();

							double latbefore = before.getCoor().lat();
							double lonbefore = before.getCoor().lon();

							double latdif = latbefore - latafter;

							double londif = lonbefore - lonafter;

							path.lineTo(x - latdif, y - londif);

							xprev = x;
							yprev = y;

							x = xprev - latdif;
							y = yprev - londif;

							path.moveTo(x, y);
						} else if (!after.isOutsideDownloadArea() && j == 0) {
							Node before = w.getNode(j + 1);

							double latafter = after.getCoor().lat();
							double lonafter = after.getCoor().lon();

							double latbefore = before.getCoor().lat();
							double lonbefore = before.getCoor().lon();

							double latdif = latbefore - latafter;

							double londif = lonbefore - lonafter;

							double xdif = 0, ydif = 0;

							if (latdif > 0) {
								xdif = x - (latdif + 200);
							} else {
								xdif = x - (latdif - 200);
							}

							if (londif > 0) {
								ydif = y - (londif + 200);
							}

							else {
								ydif = y - (londif - 200);
							}

							path.lineTo(xdif, ydif);

							xprev = x;
							yprev = y;

							x = xdif;
							y = ydif;

							path.moveTo(x, y);
						} else {
							Node before = w.getNode(j + 1);

							double latbefore = before.getCoor().lat();
							double lonbefore = before.getCoor().lon();

							double latdif = xprev - latbefore;
							double londif = yprev - lonbefore;

							double xdif = 0, ydif = 0;

							if (latdif > 0) {
								xdif = xprev - (latdif + 200);
							} else {
								xdif = xprev - (latdif - 200);
							}

							if (londif > 0) {
								ydif = yprev - (londif + 200);
							} else {
								ydif = yprev - (londif - 200);
							}

							path.moveTo(x, y);
							path.lineTo(x - xdif, y - ydif);
							x = x - xdif;
							y = y - ydif;

						}
					}

					xl = x;
					yl = y;

					g2d.setStroke(new BasicStroke(1));
					g2d.setColor(Color.BLACK);
					//g2d.draw(path);

					double dx = 350 * 0.70 + x * 0.30;
					double dy = 250 * 0.70 + y * 0.30;
					Path2D pa;
					pa = new Path2D.Double();
					pa.moveTo(x, y);
					pa.lineTo(dx, dy);
					g2d.setStroke(new BasicStroke(3));
					g2d.setColor(Color.BLACK);
					g2d.draw(pa);

					// draw name
					if (x < 350 && y < 250) {
						g2d.drawString(w.getName(), (float) (x - 5),
								(float) (y - 5));
					} else if (x > 350 && y < 250) {
						g2d.drawString(w.getName(), (float) (x - 5),
								(float) (y + 5));
					} else if (x < 350 && y > 250) {
						g2d.drawString(w.getName(), (float) (x + 5),
								(float) (y + 5));
					} else if (x > 350 && y > 250) {
						g2d.drawString(w.getName(), (float) (x + 5),
								(float) (y + 5));
					}

					double xcp1 = x;
					double ycp1 = y;
					double xcp2 = dx;
					double ycp2 = dy;

					// draw lanes
					if (w.hasKey("lanes:forward")) {

						double l = Math.sqrt((x - dx) * (x - dx) + (y - dy)
								* (y - dy));

						double offsetPixels = 20;

						double x1p = 0;
						double y1p = 0;
						double x2p = 0;
						double y2p = 0;

						double xinit = x;
						double yinit = y;

						double xaux = 0;
						double yaux = 0;

						Path2D plf;
						plf = new Path2D.Double();

						for (int ind = 0; ind < Integer.parseInt(w
								.get("lanes:forward")); ind++) {
							x1p = xinit + offsetPixels * (dy - yinit) / l;
							x2p = dx + offsetPixels * (dy - yinit) / l;
							y1p = yinit + offsetPixels * (xinit - dx) / l;
							y2p = dy + offsetPixels * (xinit - dx) / l;

							xprev = dx;
							yprev = dy;

							g2d.setColor(Color.WHITE);
							g2d.setStroke(new BasicStroke(2));
							drawDashedLine(g2d, x1p, y1p, x2p, y2p);

							g2d.setStroke(new BasicStroke(1));
							g2d.setColor(Color.WHITE);
							g2d.fill(new Ellipse2D.Double((xprev + x2p) / 2,
									(yprev + y2p) / 2, 9, 9));

							LaneInformation info = new LaneInformation(
									Integer.parseInt(w.get("lanes:forward"))
									- ind, "forward", w,
									new Ellipse2D.Double((xprev + x2p) / 2,
											(yprev + y2p) / 2, 9, 9));
							laneInfos.add(info);

							xinit = x1p;
							yinit = y1p;
							dx = x2p;
							dy = y2p;
						}
						x1p = xinit + 5 * (dy - yinit) / l;
						x2p = dx + 5 * (dy - yinit) / l;
						y1p = yinit + 5 * (xinit - dx) / l;
						y2p = dy + 5 * (xinit - dx) / l;

						g2d.setStroke(new BasicStroke(2));
						g2d.setColor(Color.BLACK);
						plf.moveTo(x1p, y1p);
						plf.lineTo(x2p, y2p);
						g2d.draw(plf);
					}

					if (w.hasKey("lanes:backward")) {
						double l = Math.sqrt((xcp1 - xcp2) * (xcp1 - xcp2)
								+ (ycp1 - ycp2) * (ycp1 - ycp2));

						double offsetPixels = 20;

						double x1p = 0;
						double y1p = 0;
						double x2p = 0;
						double y2p = 0;

						double xinit = xcp1;
						double yinit = ycp1;

						double xaux = 0;
						double yaux = 0;

						Path2D plf;
						plf = new Path2D.Double();

						for (int ind = 0; ind < Integer.parseInt(w
								.get("lanes:backward")); ind++) {

							x1p = xinit - offsetPixels * (ycp2 - yinit) / l;
							x2p = xcp2 - offsetPixels * (ycp2 - yinit) / l;
							y1p = yinit - offsetPixels * (xinit - xcp2) / l;
							y2p = ycp2 - offsetPixels * (xinit - xcp2) / l;

							xprev = xcp2;
							yprev = ycp2;

							g2d.setColor(Color.WHITE);
							g2d.setStroke(new BasicStroke(2));
							drawDashedLine(g2d, x1p, y1p, x2p, y2p);

							g2d.setStroke(new BasicStroke(1));
							g2d.setColor(Color.BLACK);
							g2d.fill(new Ellipse2D.Double((xprev + x2p) / 2,
									(yprev + y2p) / 2, 9, 9));

							LaneInformation info = new LaneInformation(
									Integer.parseInt(w.get("lanes:backward"))
									- ind, "backward", w,
									new Ellipse2D.Double((xprev + x2p) / 2,
											(yprev + y2p) / 2, 9, 9));
							laneInfos.add(info);

							xinit = x1p;
							yinit = y1p;
							xcp2 = x2p;
							ycp2 = y2p;
						}
						x1p = xinit - 5 * (ycp2 - yinit) / l;
						x2p = xcp2 - 5 * (ycp2 - yinit) / l;
						y1p = yinit - 5 * (xinit - xcp2) / l;
						y2p = ycp2 - 5 * (xinit - xcp2) / l;

						g2d.setStroke(new BasicStroke(2));
						g2d.setColor(Color.BLACK);
						plf.moveTo(x1p, y1p);
						plf.lineTo(x2p, y2p);
						g2d.draw(plf);
					}

					// successor
					Node aftern = w.getNodes().get(inde + 1);

					Path2D path2;
					path2 = new Path2D.Double();
					x = 350;
					y = 250;

					xprev = 350;
					yprev = 250;

					xl = 0;
					yl = 0;

					path2.moveTo(x, y);

					for (int i = inde + 1; i < w.getNodesCount(); i++) {
						int j = i;
						Node before = w.getNode(j - 1);
						Node after = w.getNode(j);

						if (after.isOutsideDownloadArea() == false
								&& j != w.getNodesCount() - 1) {
							double latafter = after.getCoor().lat();
							double lonafter = after.getCoor().lon();

							double latbefore = before.getCoor().lat();
							double lonbefore = before.getCoor().lon();

							double latdif = latbefore - latafter;

							double londif = lonbefore - lonafter;

							path2.lineTo(x - latdif, y - londif);

							xprev = x;
							yprev = y;

							x = xprev - latdif;
							y = yprev - londif;

							path.moveTo(x, y);
						} else if (!after.isOutsideDownloadArea()
								&& j == w.getNodesCount() - 1) {
							double latafter = after.getCoor().lat();
							double lonafter = after.getCoor().lon();

							double latbefore = before.getCoor().lat();
							double lonbefore = before.getCoor().lon();

							double latdif = latbefore - latafter;

							double londif = lonbefore - lonafter;

							double xdif = 0, ydif = 0;

							if (latdif > 0) {
								xdif = x - (latdif + 200);
							} else {
								xdif = x - (latdif - 200);
							}

							if (londif > 0) {
								ydif = y - (londif + 200);
							}

							else {
								ydif = y - (londif - 200);
							}

							path2.lineTo(xdif, ydif);

							xprev = x;
							yprev = y;

							x = xdif;
							y = ydif;

							path2.moveTo(x, y);
						} else {
							double latbefore = before.getCoor().lat();
							double lonbefore = before.getCoor().lon();

							double latdif = xprev - latbefore;
							double londif = yprev - lonbefore;

							double xdif = 0, ydif = 0;

							if (latdif > 0) {
								xdif = xprev - (latdif + 200);
							} else {
								xdif = xprev - (latdif - 200);
							}

							if (londif > 0) {
								ydif = yprev - (londif + 200);
							} else {
								ydif = yprev - (londif - 200);
							}

							path2.moveTo(x, y);
							path2.lineTo(x - xdif, y - ydif);
							x = x - xdif;
							y = y - ydif;

						}
					}

					xl = x;
					yl = y;

					g2d.setStroke(new BasicStroke(1));
					g2d.setColor(Color.BLACK);
					//g2d.draw(path2);

					dx = 350 * 0.70 + x * 0.30;
					dy = 250 * 0.70 + y * 0.30;
					Path2D pa2;
					pa2 = new Path2D.Double();
					pa2.moveTo(x, y);
					pa2.lineTo(dx, dy);
					g2d.setStroke(new BasicStroke(3));
					g2d.setColor(Color.BLACK);
					g2d.draw(pa2);

					// draw name
					if (x < 350 && y < 250) {
						g2d.drawString(w.getName(), (float) (x - 5),
								(float) (y - 5));
					} else if (x > 350 && y < 250) {
						g2d.drawString(w.getName(), (float) (x - 5),
								(float) (y + 5));
					} else if (x < 350 && y > 250) {
						g2d.drawString(w.getName(), (float) (x + 5),
								(float) (y + 5));
					} else if (x > 350 && y > 250) {
						g2d.drawString(w.getName(), (float) (x + 5),
								(float) (y + 5));
					}

					xcp1 = x;
					ycp1 = y;
					xcp2 = dx;
					ycp2 = dy;

					// draw lanes
					if (w.hasKey("lanes:forward")) {

						double l = Math.sqrt((x - dx) * (x - dx) + (y - dy)
								* (y - dy));

						double offsetPixels = 20;

						double x1p = 0;
						double y1p = 0;
						double x2p = 0;
						double y2p = 0;

						double xinit = x;
						double yinit = y;

						double xaux = 0;
						double yaux = 0;

						Path2D plf;
						plf = new Path2D.Double();

						for (int ind = 0; ind < Integer.parseInt(w
								.get("lanes:forward")); ind++) {
							x1p = xinit + offsetPixels * (dy - yinit) / l;
							x2p = dx + offsetPixels * (dy - yinit) / l;
							y1p = yinit + offsetPixels * (xinit - dx) / l;
							y2p = dy + offsetPixels * (xinit - dx) / l;

							xprev = dx;
							yprev = dy;

							g2d.setColor(Color.WHITE);
							g2d.setStroke(new BasicStroke(2));
							drawDashedLine(g2d, x1p, y1p, x2p, y2p);

							g2d.setStroke(new BasicStroke(1));
							g2d.setColor(Color.WHITE);
							g2d.fill(new Ellipse2D.Double((xprev + x2p) / 2,
									(yprev + y2p) / 2, 9, 9));

							LaneInformation info = new LaneInformation(
									Integer.parseInt(w.get("lanes:forward"))
									- ind, "forward", w,
									new Ellipse2D.Double((xprev + x2p) / 2,
											(yprev + y2p) / 2, 9, 9));
							laneInfos.add(info);

							xinit = x1p;
							yinit = y1p;
							dx = x2p;
							dy = y2p;
						}
						x1p = xinit + 5 * (dy - yinit) / l;
						x2p = dx + 5 * (dy - yinit) / l;
						y1p = yinit + 5 * (xinit - dx) / l;
						y2p = dy + 5 * (xinit - dx) / l;

						g2d.setStroke(new BasicStroke(2));
						g2d.setColor(Color.BLACK);
						plf.moveTo(x1p, y1p);
						plf.lineTo(x2p, y2p);
						g2d.draw(plf);
					}

					if (w.hasKey("lanes:backward")) {
						double l = Math.sqrt((xcp1 - xcp2) * (xcp1 - xcp2)
								+ (ycp1 - ycp2) * (ycp1 - ycp2));

						double offsetPixels = 20;

						double x1p = 0;
						double y1p = 0;
						double x2p = 0;
						double y2p = 0;

						double xinit = xcp1;
						double yinit = ycp1;

						double xaux = 0;
						double yaux = 0;

						Path2D plf;
						plf = new Path2D.Double();

						for (int ind = 0; ind < Integer.parseInt(w
								.get("lanes:backward")); ind++) {

							x1p = xinit - offsetPixels * (ycp2 - yinit) / l;
							x2p = xcp2 - offsetPixels * (ycp2 - yinit) / l;
							y1p = yinit - offsetPixels * (xinit - xcp2) / l;
							y2p = ycp2 - offsetPixels * (xinit - xcp2) / l;

							xprev = xcp2;
							yprev = ycp2;

							g2d.setColor(Color.WHITE);
							g2d.setStroke(new BasicStroke(2));
							drawDashedLine(g2d, x1p, y1p, x2p, y2p);

							g2d.setStroke(new BasicStroke(1));
							g2d.setColor(Color.BLACK);
							g2d.fill(new Ellipse2D.Double((xprev + x2p) / 2,
									(yprev + y2p) / 2, 9, 9));

							LaneInformation info = new LaneInformation(
									Integer.parseInt(w.get("lanes:backward"))
									- ind, "backward", w,
									new Ellipse2D.Double((xprev + x2p) / 2,
											(yprev + y2p) / 2, 9, 9));
							laneInfos.add(info);

							xinit = x1p;
							yinit = y1p;
							xcp2 = x2p;
							ycp2 = y2p;
						}
						x1p = xinit - 5 * (ycp2 - yinit) / l;
						x2p = xcp2 - 5 * (ycp2 - yinit) / l;
						y1p = yinit - 5 * (xinit - xcp2) / l;
						y2p = ycp2 - 5 * (xinit - xcp2) / l;

						g2d.setStroke(new BasicStroke(2));
						g2d.setColor(Color.BLACK);
						plf.moveTo(x1p, y1p);
						plf.lineTo(x2p, y2p);
						g2d.draw(plf);
					}
				}

				// int size = (LaneRelationsDialog.getJunctionWays()).size();
				int x, y, z, t;
				x = 200;
				y = 10;
				z = 250;
				t = 110;
				/*
				 * for (int j = 0; j < size; j++) { g2d.setColor(Color.GREEN);
				 * g2d.drawLine(x, y, z, t); x += 10; y += 10; z += 10; t += 10;
				 * }
				 */
				//
				// g2d.setColor(Color.RED);
				// g2d.drawArc(296, 101, 85, 40, 280, 43);
				ColoredLine[] li;
				li = doLoadFromFile();
				if (li.length > 0) {
					for (int i = 0; i < li.length; i++) {
						int c = li[i].colorIndex;
						g.setColor(Color.RED);
						((Graphics2D) g).setStroke(new BasicStroke(2));
						drawArrow(g, li[i].x1, li[i].y1, li[i].x2, li[i].y2);
					}
					System.out.println("sunt numere!");
				}

				for (int i = 0; i < lineCount; i++) {
					int c = lines[i].colorIndex;
					g.setColor(Color.RED);
					drawArrow(g, lines[i].x1, lines[i].y1, lines[i].x2,
							lines[i].y2);
					// g.drawLine(lines[i].x1, lines[i].y1, lines[i].x2,
					// lines[i].y2);
				}
			}
		}
	}

	// ------------------------------------------------------------------------------------

	// The remainder of the class implements drawing of lines. While the
	// user
	// is drawing a line, the line is represented by a "rubber band" lines
	// that
	// follows the mouse. The rubber band line is drawn in XOR mode, which
	// has
	// the property that drawing the same thing twice has no effect. (That
	// is,
	// the second draw operation undoes the first.) When the user releases
	// the
	// mouse button, the rubber band line is replaced by a regular line and
	// is
	// added to the array.

	int startX, startY; // When the user presses the mouse button, the
	// location of the mouse is stored in these variables.
	int prevX, prevY; // The most recent mouse location; a rubber band line
	// has
	// been drawn from (startX, startY) to (prevX, prevY).

	boolean dragging = false; // For safety, this variable is set to true
	// while
	// a
	// drag operation is in progress.

	Graphics gc; // While dragging, gc is a graphics context that can be

	// used to

	// draw to the canvas.

	@Override
	public void mousePressed(MouseEvent evt) {
		// This is called by the system when the user presses the mouse
		// button.
		// Record the location at which the mouse was pressed. This location
		// is one endpoint of the line that will be drawn when the mouse is
		// released. This method is part of the MouseLister interface.
		startX = evt.getX();
		startY = evt.getY();

		for (LaneInformation l : laneInfos) {
			if (l.getEllipse().contains(startX, startY)) {
				prevX = startX;
				prevY = startY;
				dragging = true;
				gc = getGraphics(); // Get a graphics context for use while
				// drawing.
				gc.setColor(ColoredLine.colorList[currentColorIndex]);
				gc.setXORMode(getBackground());
				// gc.drawLine(startX, startY, prevX, prevY);
				for (LaneInformation li : laneInfos) {
					if (li.getEllipse().contains(prevX, prevY)) {
						gc.setColor(Color.RED);
						drawArrow(gc, startX, startY, prevX, prevY);
					}
				}
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent evt) {
		// This is called by the system when the user moves the mouse while
		// holding
		// down a mouse button. The previously drawn rubber band line is
		// erased
		// by
		// drawing it a second time, and a new rubber band line is drawn
		// from
		// the
		// start point to the current mouse location.
		if (!dragging) // Make sure that the drag operation has been
			// properly
			// started.
			return;
		for (LaneInformation l : laneInfos) {
			if (l.getEllipse().contains(startX, startY)) {
				for (LaneInformation li : laneInfos) {
					if (li.getEllipse().contains(prevX, prevY)) {
						gc.setColor(Color.RED);
						drawArrow(gc, startX, startY, prevX, prevY);
					}
				}
			}
		}

		// gc.drawLine(startX, startY, prevX, prevY); // Erase the previous
		// line.
		prevX = evt.getX();
		prevY = evt.getY();
		// gc.drawLine(startX, startY, prevX, prevY);

		for (LaneInformation l : laneInfos) {
			if (l.getEllipse().contains(prevX, prevY)) {
				for (LaneInformation li : laneInfos) {
					if (li.getEllipse().contains(startX, startY)) {
						gc.setColor(Color.RED);
						drawArrow(gc, startX, startY, prevX, prevY);// Draw the
						// new line.
					}
				}
			}
		}

	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		// This is called by the system when the user releases the mouse
		// button.
		// The previously drawn rubber band line is erased by drawing it a
		// second
		// time. Then a permanent line is drawn in the current drawing
		// color,
		// and is added to the array of lines.
		if (!dragging) // Make sure that the drag operation has been
			// properly
			// started.
			return;

		for (LaneInformation l : laneInfos) {
			if (l.getEllipse().contains(startX, startY)) {
				for (LaneInformation li : laneInfos) {
					if (li.getEllipse().contains(prevX, prevY)) {
						// gc.drawLine(startX, startY, prevX, prevY);
						drawArrow(gc, startX, startY, prevX, prevY);// Erase the
						// previous
						// line.
					}
				}
			}

			int endX = evt.getX(); // Where the mouse was released.
			int endY = evt.getY();
			if (l.getEllipse().contains(endX, endY)) {

				for (LaneInformation lif : laneInfos) {
					if (lif.getEllipse().contains(startX, startY)) {
						gc.setPaintMode();
						gc.setColor(Color.RED);
						drawArrow(gc, startX, startY, endX, endY); // Draw the
						// permanent
						// line
						// in
						// regular "paint" mode.
						gc.dispose(); // Free the graphics context, now that the
						// draw
						// operation
						// is over.

						Relation rel = new Relation();
						rel.addMember(new RelationMember("From", lif.getWay()));
						rel.addMember(new RelationMember("To", l.getWay()));
						rel.addMember(new RelationMember("via",
								LaneRelationsDialog.getSelectedNode()));
						relations.add(rel);
						if (lineCount < lines.length) { // Add the line to the
							// array, if
							// there
							// is room.
							lines[lineCount] = new ColoredLine();
							lines[lineCount].x1 = startX;
							lines[lineCount].y1 = startY;
							lines[lineCount].x2 = endX;
							lines[lineCount].y2 = endY;
							lines[lineCount].colorIndex = currentColorIndex;
							System.out.println(lineCount);
							lineCount++;

						}
					}
				}

			}
		}

	} // end mouseReleased

	@Override
	public void mouseClicked(MouseEvent evt) {
	} // Other methods in the MouseListener interface

	@Override
	public void mouseEntered(MouseEvent evt) {
	}

	@Override
	public void mouseExited(MouseEvent evt) {
	}

	@Override
	public void mouseMoved(MouseEvent evt) {
	} // Required by the MouseMotionListener interface.

	public static ColoredLine[] getLines() {
		return lines;
	}

	void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
		Graphics2D g = (Graphics2D) g1.create();

		double dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.transform(at);

		// Draw horizontal arrow starting in (0, 0)
		g.drawLine(0, 0, len, 0);
		g.fillPolygon(new int[] { len, len - ARR_SIZE, len - ARR_SIZE, len },
				new int[] { 0, -ARR_SIZE, ARR_SIZE, 0 }, 4);
	}

	static class ColoredLine { // an object of this class represents a colored
		// line segment

		public static final Color[] colorList = {
			// List of available colors; colors are always indicated as
			// indices into this array.
			Color.black, Color.gray, Color.red, Color.green, Color.blue,
			new Color(200, 0, 0), new Color(0, 180, 0),
			new Color(0, 0, 180), Color.cyan, Color.magenta, Color.yellow,
			new Color(120, 80, 20), Color.white };

		int x1, y1; // One endpoint of the line segment.
		int x2, y2; // The other endpoint of the line segment.
		int colorIndex; // The color of the line segment, given as an index in
		// the
		// colorList array.

	} // end class ColoredLine

} // end class MyGraphics
