import java.util.List;

import processing.core.*;
import processing.video.Capture;
import topcodes.*;

@SuppressWarnings("serial")
public class Template extends PApplet {

	Capture cam;
	Scanner scanner;

	public void setup() {
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
		    println("Codes found: " + codes.size());
		    image(cam, 0, 0);		    
		    rectMode(CENTER);
		    for (TopCode code : codes) {
		       pushMatrix();
		       stroke(0, 255, 0);
		       noFill(); 
		       translate(code.getCenterX(), code.getCenterY());
		       text(code.getCode(), 0, 0);
		       rotate(code.getOrientation());
		       rect(0, 0, code.getDiameter(), code.getDiameter());		       
		       popMatrix();
		    } 
		} 
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "Template" });
	}
}
