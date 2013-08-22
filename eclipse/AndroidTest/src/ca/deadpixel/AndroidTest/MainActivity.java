package ca.deadpixel.AndroidTest;

import java.util.ArrayList;
import java.util.List;
import ketai.camera.KetaiCamera;
import processing.core.*;

public class MainActivity extends PApplet {

	Scanner scanner; // top code scanner
	KetaiCamera cam;
	List<TopCode> codes = new ArrayList();
	int[] pixels = null;

	public void setup() {
		// 720p is a very slow on my Note 2, but it works 
		cam = new KetaiCamera(this, 1280, 720, 30);

		// This breaks 3D mode on Note 2
		//orientation(LANDSCAPE);

		println(cam.list());

		// 0: back camera; 1: front camera
		imageMode(CENTER);
		stroke(255);
		textSize(24); // (3)

		scanner = new Scanner();
	}

	public void draw() {

		if (cam.isStarted() == false)
			cam.start();

		image(cam, width / 2, height / 2, width, height);
		
		// we need a copy of the pixels array
		// because the scanner modifies the image it is given
		if (pixels == null) pixels = new int[cam.pixels.length];
		System.arraycopy(cam.pixels, 0, pixels, 0, cam.pixels.length);

		codes = scanner.scan(pixels, cam.width, cam.height);
		
		// draw the codes (if any)
		rectMode(CENTER);
		stroke(0, 255, 0);
		noFill();
		for (TopCode code : codes) {
			pushMatrix();
			translate(code.getCenterX(), code.getCenterY());
			text(code.getCode(), 0, 0);
			rotate(code.getOrientation());
			rect(0, 0, code.getDiameter(), code.getDiameter());
			popMatrix();
			println(code.getCode());
		}

		ellipse(mouseX, mouseY, 50, 50);

	}

	public void onCameraPreviewEvent() {
		cam.read();
	}
 
	public String sketchRenderer() {
		return P3D;
	}
}
