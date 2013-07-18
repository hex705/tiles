// press S to pause on a snapshot
// press any other key to resume live video

//

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import processing.core.*;
import processing.data.*;
import processing.video.Capture;
import topcodes.*;

//JGraphT imports
import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

//sample tile configuration is in samples/640_topcodes_47-55.png

@SuppressWarnings({ "unused", "serial" })
public class labelsANDgraph extends PApplet {

	String sample_image_location = "samples/M1000004.JPG"; //location of image for debugging
	boolean debug_mode = true;
	boolean debug_snapshot_done = false;
	PImage sample;
	
	XML xml;
	Capture cam;
	Scanner scanner;

	//array for all the data from tileset.xml
	Tile[] tileset;
	
	//array for all the tiles that are in the captured image
	Tile[] present_tiles;
	

	//JGraphT stuff
	SimpleGraph<Tile, DefaultEdge> tileGraph;
    //end JGraphT stuff

	public void setup() {
		// xml stuff
		xml = loadXML("tileset.xml");
		XML[] children = xml.getChildren("tile");
		
		tileset = new Tile[children.length];
		present_tiles = new Tile[0];
				
		for (int i = 0; i < children.length; i++) {
			String topcodeID;
			String topcodeName;
			int[] connections = new int[4];

			int id = children[i].getInt("topcodeID");
			String name = children[i].getChild("name").getContent();
			String type = children[i].getString("type");

			// connections are in order: TRBL
			connections[0] = children[i].getChild("connections").getInt("T");
			connections[1] = children[i].getChild("connections").getInt("R");
			connections[2] = children[i].getChild("connections").getInt("B");
			connections[3] = children[i].getChild("connections").getInt("L");

			//println(id + " " + name + "  { L" + connections[3] + "; R"
			//		+ connections[1] + "; T" + connections[0] + "; B"
			//		+ connections[2] + " }");
			// pass all data to appropriate tile object
			tileset[i] = new Tile (id, name, type, connections[0], connections[1], connections[2], connections[3]);
		}
		for (int i = 0; i < tileset.length; i ++) {
			println (tileset[i].topcodeID + " " + tileset[i].topcodeName + "  { L" + tileset[i].connections[3] + "; R"
					+ tileset[i].connections[1] + "; T" + tileset[i].connections[0] + "; B"
					+ tileset[i].connections[2] + " }"); //print every available tile that is defined in tileset.xml
		}

		// camera stuff
		size(640, 480);
		String[] cameras = Capture.list();
		cam = new Capture(this, cameras[0]);
		if (debug_mode == false) {
			cam.start();
		}

		scanner = new Scanner();
		
		// JGraphT stuff
		tileGraph = new SimpleGraph<Tile, DefaultEdge>(DefaultEdge.class);
				
		Iterator<Tile> iter = new DepthFirstIterator<Tile, DefaultEdge>(tileGraph);
        Tile vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            println(
                "Vertex " + vertex.toString() + " is connected to edges: " + tileGraph.edgesOf(vertex).toString());
        }		
        
		//Image stuff
		sample = loadImage(sample_image_location);
        
	}

	public void draw() {		
		if (debug_mode == false) {
			if (cam.available()) {
				// Reads the new frame
				cam.read();
	
				if (key == 's' || key == 'S') {
					//nothing happens; the keyPressed() event below will have been activated
				} else { 
					snapShot(cam); //this happens continuously as long as 'S' has not been pressed
				}
				
			}
		} 
		else { //debug_mode is true; we are using an image instead of live video
			if (debug_snapshot_done == false) {
				image(sample,0,0);
				snapShot(sample);
				makeGraph(sample);
				debug_snapshot_done = true;
			}
		}
	}
	public void keyPressed() {
		if (key == 's' || key == 'S') {
		
			snapShot(cam); //here it only happens once, when 'S' is pressed. Effectively pauses video capture.
			makeGraph(cam); //only once, when the right frame is captured
		}
	}
	
	public void makeGraph(PImage img) {
		// remove all vertices from graph (reverting it to an empty state)
		for (int i = 0; i < present_tiles.length; i ++) {
			tileGraph.removeVertex(present_tiles[i]);
		}
		
		// get all the topcodes from the snapshot
		List<TopCode> codes = getTopcodes(img);
		/*int[] pixels = new int[cam.pixels.length];
		System.arraycopy(cam.pixels, 0, pixels, 0, cam.pixels.length);
		
		List<TopCode> codes = scanner.scan(pixels, cam.width, cam.height);*/
		
		// fill graph with Tiles as vertices
		for (TopCode code : codes) {
			int thisCode = code.getCode();
			for (int i = 0; i < tileset.length; i++) {
				if (thisCode == tileset[i].topcodeID) { //if we've found a thing that matches
					// make a new present_tile object that has all the info of the tile, plus info about the topcode
					present_tiles = (Tile[]) append (present_tiles, tileset[i]);
					//then add the vertex
					int thisTile = present_tiles.length-1;
					present_tiles[thisTile].centerX = code.getCenterX();
					present_tiles[thisTile].centerY = code.getCenterY();
					present_tiles[thisTile].diameter = code.getDiameter();
					
					tileGraph.addVertex(present_tiles[thisTile]);
				} 
			}
		}
		
		// get distances between topcodes
		for (TopCode code1 : codes) {
			for (TopCode code2 : codes) {
			
				float distance = dist (code1.getCenterX(), code1.getCenterY(), 
						code2.getCenterX(), code2.getCenterY());
				
				if (distance > 0) println (distance);
				println(code1.getDiameter());

				// make edge connections between them depending on distance
				if (distance > 0 && distance < code1.getDiameter()*3) { //they are close enough to be considered 'connected'
					//get both tiles
					int code1i = 2;
					int code2i = 3;
					
					int thisCode = code1.getCode();
					for (int i = 0; i < present_tiles.length; i++) {
						if (thisCode == present_tiles[i].topcodeID) {
							code1i = i;
						} 
					}
					thisCode = code2.getCode();
					for (int i = 0; i < present_tiles.length; i++) {
						if (thisCode == present_tiles[i].topcodeID) {
							code2i = i;
						} 
					}
					
					
					//make edge between the two things
					tileGraph.addEdge(present_tiles[code1i], present_tiles[code2i]);
				}
				
			}
		}
		
		//view newly filled graph
		Iterator<Tile> iter = new DepthFirstIterator<Tile, DefaultEdge>(tileGraph);
        Tile vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            println(
                "Vertex " + vertex.toString() + " is connected to edges: " + tileGraph.edgesOf(vertex).toString());
        }
        
        //draw lines between the edges
        drawEdges();
		
	}
	
	public List<TopCode> getTopcodes(PImage img) {
		// get all the topcodes from the snapshot
		int[] pixels = new int[img.pixels.length];
		System.arraycopy(img.pixels, 0, pixels, 0, img.pixels.length);
		
		return scanner.scan(pixels, img.width, img.height);
	}

	public void snapShot(PImage img) {
		//normal behaviour: live video with topcodes being identified
		image(cam, 0, 0);
		rectMode(CENTER);
		
		
		List<TopCode> codes = getTopcodes(img);
		// We need a copy -- the scanner modifies the image
		/*int[] pixels = new int[cam.pixels.length];
		System.arraycopy(cam.pixels, 0, pixels, 0, cam.pixels.length);

		// Do the work
		List<TopCode> codes = scanner.scan(pixels, cam.width, cam.height);*/
		
		// show the results
		// println("Codes found: " + codes.size());
		// image(cam, 0, 0);
		// rectMode(CENTER);

		for (TopCode code : codes) {
			String tileName = "";

			int thisCode = code.getCode();
			for (int i = 0; i < tileset.length; i++) {
				if (thisCode == tileset[i].topcodeID) {
					tileName = tileset[i].topcodeID + " " + tileset[i].topcodeName;
					println(tileName);
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
		
	public void drawEdges() {
		// do stuff
	}
	
	public class Tile {
		int[] connections;
		int topcodeID;
		String topcodeName;
		String type;
		
		float centerX, centerY, diameter;
		
		Tile(int _topcodeID, String _topcodeName, String _type, int c0, int c1, int c2, int c3) {
			connections = new int[4];
			topcodeID = _topcodeID;
			topcodeName = _topcodeName;
			type = _type;
	
			// connections are in order: TRBL
			connections[0] = c0;
			connections[1] = c1;
			connections[2] = c2;
			connections[3] = c3;
		}
		@Override
		public String toString() {
			return str(this.topcodeID) + " " + this.topcodeName + " (" + this.type + ") ( " + this.centerX + ", " + this.centerY + ", " + this.diameter + ")";
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "labelsANDgraph" });
	}
}