// Click to pause on a snapshot
// Click again  to resume live video

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

	boolean debug_mode = false;
	
	// String sample_image_location = "samples/M1000004b.png"; //location of image for debugging
	String sample_image_location = "samples/M1020008.png"; 
	float acceptable_distance = 3;
	boolean debug_snapshot_done = false;
	PImage sample;
	
	int MODE;
	int CAPTURING = 0;
	int SCREENSHOT = 1;
	boolean EMAILSCREEN = false;
	
	XML xml;
	XML output_template;
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
	
				if (MODE == SCREENSHOT) {
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
		
		if (EMAILSCREEN == true)  {
			// draw text box
			fill (255);
			stroke(0);
			rectMode (CENTER);
			rect(width/2, height/10, width/2, 50);
			// draw email text
			fill(0);
			if (typing == "") {
				text("enter email", width/4 + 20, height/10 + 7);
			} else {
				text(typing, width/4 + 20, height/10 + 7);
			}
			fill(255);
		}
	}
		
	String typing = "";
	String saved = "";

	public void emailScreen() {
		typing = "";
		EMAILSCREEN = true;
	}
	
	public void keyPressed() { //TODO
		if (EMAILSCREEN == false) {
			if (key == 's' || key == 'S') {	
				MODE = SCREENSHOT;
				snapShot(cam); //here it only happens once, when 'S' is pressed. Effectively pauses video capture.
				makeGraph(cam); //only once, when the right frame is captured
			}
			
			// For testing
			if (key == ' ') {
				try {
					//sendToServer(getOutput());
				} catch (Exception e) { 
					e.printStackTrace();
				}
			}
		} else {
			if (keyCode == ENTER || keyCode == RETURN){ // on enter, print full string to console. this is where
				saved = typing;
				// call some output function
				println("email: " + saved);
				EMAILSCREEN = false;
	
				try {
					sendToServer(getOutput(saved));
				} catch (Exception e) {
					//do nothing
					e.printStackTrace();
				}

				MODE = CAPTURING;
			} else if (keyCode == BACKSPACE) {
				if (typing.length() > 0) {
					typing = typing.substring(0, typing.length() - 1);
				}
			} else if (key != CODED) {
				typing = typing + key; // add key to the end of the in=progress string
				//println(typing);
			} 
			//Log.v("Msg", "key: " + key);
			//Log.v("Msg",  typing);
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
					//println (present_tiles[lastID].toString());
					lastID ++;
					
					//println(present_tiles[0].toString());
				} 
			}
		}
		
		for (Tile tiles : present_tiles) {
			// add all present_tiles as vertices to the graph
			tileGraph.addVertex(tiles);
			// print out all the tiles
			//println(tiles.toString());
		}
		
		// get distances between tiles
		for (Tile tile1 : present_tiles) {
			for (int tile1c : tile1.connections) { //cycle through every connection on tile1
				float shortest_distance = acceptable_distance*tile1.diameter; // for determining the closest tile
				Tile short_tile = null;
				for (Tile tile2 : present_tiles) {
					float distance = dist (tile1.centerX, tile1.centerY, 
					tile2.centerX, tile2.centerY);
				
					if (distance > 0 && distance < tile1.diameter*acceptable_distance) { //they are close enough
						for (int tile2c : tile2.connections) { //cycle through tile2 connections
							if (tile1c == tile2c && tile1c != 0) {
								//println(tile1c);
								//println(distance);
								if (distance < shortest_distance) {
									shortest_distance = distance;
									short_tile = tile2;
								}
							}
						}
					}
				}
				if (short_tile != null) {
					//println(shortest_distance);
					//println(tile1.toString() + " " + short_tile.toString());
							
					//make edge between the two things (that are the closest)
					tileGraph.addEdge(tile1, short_tile);
					//edge visualization
					line(tile1.centerX,tile1.centerY,short_tile.centerX,short_tile.centerY);
				}
			}
		}

		emailScreen(); //get the user's email via a prompt
		/*
		try {
			//sendToServer(getOutput());
			//println(getOutput());
		} catch (Exception e) {
			//do nothing
			e.printStackTrace();
		}*/
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

			println("present tiles:");
		for (TopCode code : codes) {
			String tileName = "";

			int thisCode = code.getCode();
			for (int i = 0; i < tileset.length; i++) {
				if (thisCode == tileset[i].topcodeID) {
					tileName = tileset[i].topcodeID + " " + tileset[i].topcodeName;
					println("    " + tileName);
				}

				// drawing all the topcode boundaries and names
				pushMatrix();
				stroke(255);
				fill(255);
				translate(code.getCenterX(), code.getCenterY());
				text(tileName, 0, 0);
				//rotate(code.getOrientation());
				noStroke();
				fill(0,255,0,20);
				ellipse(0, 0, code.getDiameter(), code.getDiameter());
				popMatrix();
			}
		}
	}

	
	public String getOutput(String email) {
		println("output:");		
		// reload the output template every time		
		output_template = loadXML("outputTemplate.xml");
		// get email node
		XML emailNode = output_template.getChild("email");
		emailNode.setContent(email);
		// adding all NODES
			XML nodes = output_template.getChild("nodes");
		for (Tile tiles : present_tiles) {
			// add all present_tiles as children to output_template in <nodes>
			XML newNode = nodes.addChild("node"); //new node
			//populating the node
			XML newID = newNode.addChild("id");
			newID.setContent(str(tiles.id));
			XML newTileName = newNode.addChild("tileName");
			newTileName.setContent(tiles.topcodeName);
			XML newTopcodeID = newNode.addChild("topcodeID");
			newTopcodeID.setContent(str(tiles.topcodeID));
			XML newType = newNode.addChild("type");
			newType.setContent(tiles.type);
		}
		
		//making PATHS
			XML paths = output_template.getChild("paths");
		for (Tile present : present_tiles)	{
			// println(present.type);
			
			if (present.type.equals("hub")){ //check if tile is Arduino or Processing (hubs)
				println(present.toString() + " is a hub");

				Set<DefaultEdge> allEdges = tileGraph.edgesOf(present); 				
				int numEdges = allEdges.size(); 
				println(numEdges + " edges connected to this tile"); //number of edges connected to hub
				if (numEdges == 1) { // only hubs at the very end
					
					XML newPath = paths.addChild("path");
					//iterate through graph from the hub
					Iterator<Tile> iter = new DepthFirstIterator<Tile, DefaultEdge>(tileGraph, present);
			        Tile vertex;
			        while (iter.hasNext()) {
			            vertex = iter.next();
				            println("    " + vertex.toString()); // print node
							XML newNode = newPath.addChild("node"); // add node to output_template
							populateNode(vertex, newNode);
						
			            if (vertex != present && vertex.type.equals("hub")) { //ends the path if another hub is encountered
			            	allEdges = tileGraph.edgesOf(vertex); 				
							numEdges = allEdges.size(); 
							println(numEdges + " edges connected to this tile"); //number of edges connected to hub
							if (numEdges > 1) {
			            	
				            	//new path with the last hub to start with
								newPath = paths.addChild("path");
								//add middle hub again, in this new path
					            println("    " + vertex.toString()); // print node
								newNode = newPath.addChild("node"); // add node to output_template
								populateNode(vertex, newNode);
							}
			            }
			        }
				}
			}
		}		
		String output = output_template.format(4);
		//println(output_template); // prints xml file
		
		return output;
	}
	
	public void populateNode(Tile tiles, XML newNode) {
		//populating the node
		XML newID = newNode.addChild("id");
		newID.setContent(str(tiles.id));
		XML newTileName = newNode.addChild("tileName");
		newTileName.setContent(tiles.topcodeName);
		XML newTopcodeID = newNode.addChild("topcodeID");
		newTopcodeID.setContent(str(tiles.topcodeID));
		XML newType = newNode.addChild("type");
		newType.setContent(tiles.type);
	}
	
	public void sendToServer(String message) throws Exception { // from http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
		
		String stringToReverse = URLEncoder.encode(message, "UTF-8");

        URL url = new URL("http://www.deadpixel.ca/tiles/template.cgi");
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