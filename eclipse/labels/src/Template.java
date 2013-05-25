import java.util.List;

import processing.core.*;
import processing.data.*;
import processing.video.Capture;
import topcodes.*;

@SuppressWarnings({ "unused", "serial" })
public class Template extends PApplet {

	XML xml;
	Capture cam;
	Scanner scanner;

	//array for all the data from tileset.xml
	Tile[] tileset;

	public void setup() {
		// xml stuff
		xml = loadXML("tileset.xml");
		XML[] children = xml.getChildren("tile");
		
		tileset = new Tile[children.length];
				
		for (int i = 0; i < children.length; i++) {
			String topcodeID;
			String topcodeName;
			int[] connections = new int[4];

			int id = children[i].getInt("topcodeID");
			String name = children[i].getChild("name").getContent();

			// connections are in order: TRBL
			connections[0] = children[i].getChild("connections").getInt("T");
			connections[1] = children[i].getChild("connections").getInt("R");
			connections[2] = children[i].getChild("connections").getInt("B");
			connections[3] = children[i].getChild("connections").getInt("L");

			//println(id + " " + name + "  { L" + connections[3] + "; R"
			//		+ connections[1] + "; T" + connections[0] + "; B"
			//		+ connections[2] + " }");
			// pass all data to appropriate tile object
			tileset[i] = new Tile (id, name, connections[0], connections[1], connections[2], connections[3]);
		}
		for (int i = 0; i < tileset.length; i ++) {
			println (tileset[i].topcodeID + " " + tileset[i].topcodeName + "  { L" + tileset[i].connections[3] + "; R"
					+ tileset[i].connections[1] + "; T" + tileset[i].connections[0] + "; B"
					+ tileset[i].connections[2] + " }");
		}

		// camera stuff
		size(640, 480);
		String[] cameras = Capture.list();
		cam = new Capture(this, cameras[0]);
		cam.start();

		scanner = new Scanner();
	}

	public void draw() {
		if (cam.available()) {
			// Reads the new frame
			cam.read();

			// We need a copy -- the scanner modifies the image
			int[] pixels = new int[cam.pixels.length];
			System.arraycopy(cam.pixels, 0, pixels, 0, cam.pixels.length);

			// Do the work
			List<TopCode> codes = scanner.scan(pixels, cam.width, cam.height);

			// show the results
			// println("Codes found: " + codes.size());
			image(cam, 0, 0);
			rectMode(CENTER);

			for (TopCode code : codes) {
				String tileName = "";

				int thisCode = code.getCode();
				for (int i = 0; i < tileset.length; i++) {
					if (thisCode == tileset[i].topcodeID) {
						tileName = tileset[i].topcodeID + " " + tileset[i].topcodeName;
						println(tileName);
					} else {
						tileName = str(code.getCode());
						println(tileName + " no name found");
					}

				pushMatrix();
				stroke(0, 255, 0);
				noFill();
				translate(code.getCenterX(), code.getCenterY());
				text(tileName, 0, 0);
				rotate(code.getOrientation());
				rect(0, 0, code.getDiameter(), code.getDiameter());
				popMatrix();
				}
			}
		}
	}
	
	public class Tile {
		int[] connections;
		int topcodeID;
		String topcodeName;
		
		Tile(int _topcodeID, String _topcodeName, int c0, int c1, int c2, int c3) {
			connections = new int[4];
			topcodeID = _topcodeID;
			topcodeName = _topcodeName;
	
			// connections are in order: TRBL
			connections[0] = c0;
			connections[1] = c1;
			connections[2] = c2;
			connections[3] = c3;
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "Template" });
	}
}
