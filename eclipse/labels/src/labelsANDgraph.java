// press S to pause on a snapshot
// press any other key to resume live video

//

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.io.*;
import java.net.*;

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

	// String sample_image_location = "samples/M1000004b.png"; //location of image for debugging
	String sample_image_location = "samples/M1000003.JPG"; 
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
	
	//misc.
	int lastID;

	public void setup() {
		lastID = 0;
		
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
		
		lastID =  0; // reset lastID

		present_tiles = new Tile[0]; // reset array to empty
		present_tiles = new Tile[codes.size()]; // reset array with length of the number of topcodes there are
		//println(present_tiles.length);
		
		// add all the topcodes to the present_tiles array
		for (TopCode code : codes) {
			int thisCode = code.getCode();
			
			//println (code.getCode() + " " + code.getCenterX() + ", " + code.getCenterY());
			for (int i = 0; i < tileset.length; i++) {
				if (thisCode == tileset[i].topcodeID) { // if we've found a thing that matches
					// make a new present_tile object that has all the info of the tile, plus info about the topcode
					Tile newTile = new Tile(tileset[i].topcodeID, tileset[i].topcodeName, tileset[i].type, tileset[i].connections[0], tileset[i].connections[1], tileset[i].connections[2], tileset[i].connections[3]);
					// give the tile a unique ID
					newTile.id = lastID;
					// give the tile all the properties of the topcode
					newTile.centerX = code.getCenterX();
					newTile.centerY = code.getCenterY();
					newTile.diameter = code.getDiameter();
					// add tile to the present_tiles array
					present_tiles[lastID] = newTile;
					println (present_tiles[lastID].toString());
					lastID ++;
					
					println(present_tiles[0].toString());
				} 
			}
		}
		
		for (Tile tiles : present_tiles) {
			// add all present_tiles as vertices to the graph
			tileGraph.addVertex(tiles);
			// print out all the tiles
			println(tiles.toString());
		}
		
		// get distances between tiles
		for (Tile tile1 : present_tiles) {
			for (Tile tile2 : present_tiles) {
			
				float distance = dist (tile1.centerX, tile1.centerY, 
						tile2.centerX, tile2.centerY);
				
				// make edge connections between them depending on distance
				if (distance > 0 && distance < tile1.diameter*3) { //they are close enough to be considered 'connected'			
					
					//make edge between the two things
					tileGraph.addEdge(tile1, tile2);
					//edge visualization
					line(tile1.centerX,tile1.centerY,tile2.centerX,tile2.centerY);
				}
			}
		}
		
		for (Tile present : present_tiles)	{
			// println(present.type);
			if (present.type.equals("hub")){ //check if tile is Arduino or Processing (hubs)
				println(present.toString() + " is a hub");
				//view newly filled graph from HUBS
				Iterator<Tile> iter = new DepthFirstIterator<Tile, DefaultEdge>(tileGraph, present);
				println ("from " + present.toString());
		        Tile vertex;
		        while (iter.hasNext()) {
		            vertex = iter.next();
		            println("    " + vertex.toString());
		            if (vertex != present && (vertex.type == tileset[5].type || vertex.type == tileset[9].type)) { //ends the loop if another hub is encountered
		            	break;
		            }
		        }
			}
		}
		try {
			sendToServer("hello world");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		for (TopCode code : codes) {
			String tileName = "";

			int thisCode = code.getCode();
			for (int i = 0; i < tileset.length; i++) {
				if (thisCode == tileset[i].topcodeID) {
					tileName = tileset[i].topcodeID + " " + tileset[i].topcodeName;
					println(tileName);
				} 

			// drawing all the topcode boundaries and names
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
	
	public void sendToServer(String message) throws Exception { // from http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
		
		String stringToReverse = URLEncoder.encode(message, "UTF-8");

        URL url = new URL("http://imagearts.ryerson.ca/kathryn.hartog/tilesProcessor.php");
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);

        OutputStreamWriter out = new OutputStreamWriter(
                                         connection.getOutputStream());
        out.write("message=" + stringToReverse);
        out.close();

        BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    connection.getInputStream()));
        String decodedString;
        while ((decodedString = in.readLine()) != null) {
            System.out.println(decodedString);
        }
        in.close();
	}
	
	public static void main(String args[]) {
		PApplet.main(new String[] { "labelsANDgraph" });
	}
}