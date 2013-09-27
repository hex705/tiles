import ketai.camera.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.http.HttpException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import android.view.inputmethod.InputMethodManager;
import android.content.Context;

// GUI 
int MODE;
int CAPTURING = 0;
int SCREENSHOT = 1;
boolean EMAILSCREEN = false;

float acceptable_distance = 3;
boolean debug_snapshot_done = false;
PImage sample;

XML xml;
XML output_template;

//array for all the data from tileset.xml
Tile[] tileset;

//array for all the tiles that are in the captured image
Tile[] present_tiles;

//JGraphT stuff
SimpleGraph<Tile, DefaultEdge> tileGraph;

// Misc.
int lastID;
Timer emailTimer = new Timer();
float scaleRatio;
float scaleOffset;

Scanner scanner; // top code scanner
int[] pixels = null;

// Set up camera globals:
PImage gBuffer;
KetaiCamera cam;

float camW = 640;
float camH = 480;

PImage freezeFrame;


//----------------------------------------------------------------------------------------
void setup() {
  size(displayWidth, displayHeight);
  orientation(LANDSCAPE);
  cam = new KetaiCamera(this, (int)camW, (int)camH, 30);  
  freezeFrame = createImage((int)camW, (int)camH, RGB);

  message = "Tap to capture the current screen";

  //hideVirtualKeyboard();

  imageMode(CENTER);
  stroke(255);
  textSize(24); // (3)

  scanner = new Scanner();
  lastID = 0;
  
  // Calculate aspect ratio and scaling since our camera operates at a different resolution
  // from the screen
  float camAspect = camW / camH;
  float screenAspect = (float)displayWidth / displayHeight;
  
  if (screenAspect > camAspect) {
    scaleRatio = (displayHeight/camH);
    scaleOffset = (displayWidth - (camW*scaleRatio)) / 2;    
  }
  else {
   scaleRatio = displayWidth / camW;
  } 
  
  println("scaleRatio: " + scaleRatio + ", offset: " + scaleOffset);
  
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

    println(id + " " + name + "  { L" + connections[3] + "; R"
      + connections[1] + "; T" + connections[0] + "; B"
      + connections[2] + " }");
    // pass all data to appropriate tile object
    tileset[i] = new Tile (id, name, type, connections[0], connections[1], connections[2], connections[3]);
  }

  for (int i = 0; i < tileset.length; i ++) {
    println( tileset[i].topcodeID + " " + tileset[i].topcodeName + "  { L" + tileset[i].connections[3] + "; R"
      + tileset[i].connections[1] + "; T" + tileset[i].connections[0] + "; B"
      + tileset[i].connections[2] + " }"); //print every available tile that is defined in tileset.xml
  }

  // JGraphT stuff
  tileGraph = new SimpleGraph<Tile, DefaultEdge>(DefaultEdge.class);
}

//----------------------------------------------------------------------------------------
void draw() {
  background(0);
  image(cam, width/2, height/2, cam.width*scaleRatio, cam.height*scaleRatio);
  if (cam.isStarted() == false) cam.start();

  if (MODE == CAPTURING) {

    List<TopCode> codes = getTopcodes(cam);
    try {
      makeGraph(codes, false);
    } 
    catch(NullPointerException e) {
      println("error: " + e);
    }
  }
  else if (MODE == SCREENSHOT) {
    image(freezeFrame, width/2, height/2, cam.width*scaleRatio, cam.height*scaleRatio); 
    List<TopCode> codes = getTopcodes(freezeFrame);
    makeGraph(codes, true);  
  }

  if (EMAILSCREEN == true) {
    // draw text box
    fill (255);
    stroke(0);
    rectMode (CENTER);
    rect(width/2, height/10 + 20, width/2, 50);
    // draw email text
    fill(0);
    text(typing + "_", width/4 + 20, height/10 + 27);
    fill(255);
  }
  emailTimer.update();
}

String message;

//----------------------------------------------------------------------------------------
void drawMessage() {
  //drawing full-width black box for user instruction
  fill(0, 0, 0, 128);
  noStroke();
  rectMode(CORNER);
  rect(0, 0, width, 40);
  fill(255);
  text(message, 20, 27);
}

//----------------------------------------------------------------------------------------
class Timer {
  int timeLeft;
  int startTime;
  boolean active;

  void start(int duration) {
    startTime = millis();
    timeLeft = duration;
    active = true;
  }
  void update() {
    if (millis() - startTime > timeLeft) {
      timerEnd(this);
      active = false;
    }
  }
}

//----------------------------------------------------------------------------------------
void timerEnd(Timer t) {
  if (emailTimer.active == false && MODE == CAPTURING) {
    message = "Tap to capture the current screen";
  }
}

//----------------------------------------------------------------------------------------
List<TopCode> getTopcodes(PImage cam) {
  if (pixels == null) pixels = new int[cam.pixels.length];
  System.arraycopy(cam.pixels, 0, pixels, 0, cam.pixels.length);
  List<TopCode> codes = scanner.scan(pixels, cam.width, cam.height);
  return codes;
}

//----------------------------------------------------------------------------------------
void snapShot(PImage img, List<TopCode> codes) {
  //normal behaviour: live video with topcodes being identified
  //image(cam, 0, 0);
  rectMode(CORNER);

  //codes = getTopcodes(gBuffer);

  //pushMatrix();
  //scale(scaleRatio/1);
  //imageMode(CORNER);
  //image(gBuffer, 0, 0); // draw cam image
  //popMatrix();

  //println("present tiles:");
  for (TopCode code : codes) {
    String tileName = "";
    int thisCode = code.getCode();
    for (int i = 0; i < tileset.length; i++) {
      if (thisCode == tileset[i].topcodeID) {
        //tileName = tileset[i].topcodeID + " " + tileset[i].topcodeName;
        tileName = tileset[i].topcodeName;
        //println("    " + tileName);
        // drawing all the topcode boundaries and names
        pushMatrix();
        translate(scaleOffset, 0);
        scale(scaleRatio/1);
        translate(code.getCenterX(), code.getCenterY());
        //rotate(code.getOrientation());
        noStroke();
        fill(0, 255, 0, 128);
        ellipse(0, 0, code.getDiameter(), code.getDiameter());
        stroke(255);
        fill(255);
        //scale(scaleRatio);
        textAlign(CENTER);
        textSize(code.getDiameter()/2*0.8);
        text(tileName, 0, -code.getDiameter());
        textAlign(LEFT);
        popMatrix();
      }
    }
  }
}

//----------------------------------------------------------------------------------------
void makeGraph(List<TopCode> codes, boolean graphFunctions) {
  snapShot(gBuffer, codes); 
  drawMessage();

  // remove all vertices from graph (reverting it to an empty state)
  for (int i = 0; i < present_tiles.length; i ++) {
    tileGraph.removeVertex(present_tiles[i]);
  }

  // get all the topcodes from the snapshot
  //List<TopCode> codes = getTopcodes(img);

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
  if (graphFunctions == true) {
    for (Tile tiles : present_tiles) {
      // add all present_tiles as vertices to the graph
      tileGraph.addVertex(tiles);
      // print out all the tiles
      //println(tiles.toString());
    }
  }

  if (present_tiles.length > 1) { // only do edge things if more than one tile is present
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
          if (graphFunctions == true) {
            tileGraph.addEdge(tile1, short_tile);
          }
          //edge visualization
          pushStyle();
          pushMatrix();
          translate(scaleOffset, 0);
          scale(scaleRatio/1);          
          stroke(0, 255, 0);
          strokeWeight(4);
          line(tile1.centerX, tile1.centerY, short_tile.centerX, short_tile.centerY);
          popMatrix();
          popStyle();
        }
      }
    }
  }

  if (graphFunctions == true) {
    if (EMAILSCREEN == false) emailScreen(); //get the user's email via a prompt
  }
  /*
    try {
   //sendToServer(getOutput());
   //println(getOutput());
   } catch (Exception e) {
   //do nothing
   e.printStackTrace();
   }*/
}

//----------------------------------------------------------------------------------------
// for keyboard email entry
String typing = "";
String saved = "";
void emailScreen() {
  EMAILSCREEN = true;
  showVirtualKeyboard();
}

//----------------------------------------------------------------------------------------
void keyPressed() {
  if (key == '\n') { // on enter, print full string to console. this is where
    saved = typing;
    typing = "";
    // call some output function
    println("email: " + saved);
    emailTimer.start(5000);
    message = "check your email for code templates";
    hideVirtualKeyboard();
    EMAILSCREEN = false;

    MODE = CAPTURING;

    try {
      if (saved.equals("")) {
        println("no email was given");
      } 
      else {
        sendToServer(getOutput(saved));
      }
    } 
    catch (Exception e) {
      //do nothing
      e.printStackTrace();
      println("Error sending to server: " + e);
    }
  } // Don't know where these codes come from, or why they're not the same as in Processing desktop
  else if (key == 65535) {
    if (keyCode == 67) {  // 67 --> backspace        
      if (typing.length() > 0) {
        typing = typing.substring(0, typing.length() - 1);
      }
    }
  } 
  else if (key != CODED) {
    typing = typing + key; // add key to the end of the in=progress string
  }


  //println("key: " + key);
  //println(" typing);
}

//----------------------------------------------------------------------------------------
// keyboard functions from http://forum.processing.org/topic/show-hide-the-keyboard
void showVirtualKeyboard() {
  InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
  imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
}

//----------------------------------------------------------------------------------------
void hideVirtualKeyboard() {
  InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
  imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
}

//----------------------------------------------------------------------------------------
void sendToServer(String message) throws Exception { // from http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html

  String stringToSend = URLEncoder.encode(message, "UTF-8");

  //URL url = new URL("http://imagearts.ryerson.ca/kathryn.hartog/tilesProcessor.php");
  URL url = new URL("http://www.deadpixel.ca/tiles/template.cgi");
  URLConnection connection = url.openConnection();
  connection.setDoOutput(true);

  OutputStreamWriter out = new OutputStreamWriter(
  connection.getOutputStream());
  out.write("message=" + stringToSend);
  out.close();

  BufferedReader in = new BufferedReader(
  new InputStreamReader(
  connection.getInputStream()));
  String decodedString;
  while ( (decodedString = in.readLine ()) != null) {
    //System.out.println(decodedString);
    println(decodedString);
  }
  in.close();
}

//----------------------------------------------------------------------------------------
String getOutput(String email) {
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
  for (Tile present : present_tiles) {
    // println(present.type);

    if (present.type.equals("hub")) { //check if tile is Arduino or Processing (hubs)
      println(present.toString() + " is a hub");

      Set<DefaultEdge> allEdges = tileGraph.edgesOf(present);         
      int numEdges = allEdges.size(); 
      println(numEdges + " edges connected to this tile"); //number of edges connected to hub
      if (numEdges == 1) { // only hubs at the very end

        XML newPath = paths.addChild("path");
        //iterate through graph from the hub
        Iterator<Tile> iter = new DepthFirstIterator<Tile, DefaultEdge>(tileGraph, present);
        Tile vertex;
        while (iter.hasNext ()) {
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

//----------------------------------------------------------------------------------------
void populateNode(Tile tiles, XML newNode) {
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

//----------------------------------------------------------------------------------------
void mousePressed() {
  println("Touch");
  if (EMAILSCREEN == false) {
    if (MODE == CAPTURING) {
      MODE = SCREENSHOT;
      message = "enter your email address, then press enter";
      // make a copy of the frame
      
      freezeFrame.copy(cam, 0, 0, (int)camW, (int)camH, 0, 0, (int)camW, (int)camH); 
    } 
    else if (MODE == SCREENSHOT) {
      MODE = CAPTURING; //continuous video
    }
  } 
  else {
    if (mouseY < height/2) {
      typing = "";
      showVirtualKeyboard();
    }
  }
}

//----------------------------------------------------------------------------------------s
void stop() {
  // Give the cam back to the phone:
  cam.stop();
  super.stop();
}



void onCameraPreviewEvent()
{
  cam.read();
}

