import java.awt.Rectangle;

final color GREYSCALE = color(255, 255, 255);
final color RED = color(255, 0, 0);

class Slider {

  color activeColor     = color(255, 0, 0);
  color backgroundColor = color(240, 240, 240);
  color borderColor     = color(30, 30, 30);
  color sliderColor;
  color fieldColor;

  color gradientEnd = color (30, 30, 30);
  color gradientStart;

  int rounding = 5;
  int sliderHeight = 2;
  int sliderAlpha = 100;
  int border = 10;  // invisible
  int textBorder = 20;

  int loc[] = new int[4];
  int range = 255;
  int sliderMax = 255;
  int sliderMin = 0;

  int currentValue = 10;  // the value from MIN to MAX
  int activeValue = 10;  // currentValue mapped for drawing

  boolean GRADIENT = true;
  boolean FIELDSET = false;

  PFont f;

  Rectangle body;

  Slider(int _x, int _y, int _w, int _h, int _min, int _max, color _c, boolean _gradient) {
    loc[0] = _x;
    loc[1] = _y;
    loc[2] = _w;
    loc[3] = _h;
    sliderMax = _max;
    sliderMin = _min;

    body= new Rectangle(loc[0], loc[1], loc[2], loc[3]);
    activeColor = _c; 
    setColor(activeColor);
    activeValue = loc[3]+loc[1]-activeValue-rounding; 

    GRADIENT = _gradient;   
    if (GRADIENT) {
      gradientStart = sliderColor;
    }

    f = createFont( "Arial", 18, true );
    textFont (f, 18);
  } 

  int update() {

    checkMouse();

    // make a background mask == so numbers SHOWUP
    if (FIELDSET) {
      fill(fieldColor);
    }
    else {
    fill(g.backgroundColor);  // this is sketch background
    }
    noStroke();
    rect(loc[0]-border, loc[1]-border, loc[2]+border*2, loc[3]+border*2+textBorder);

    // fill whole slider
    fill(backgroundColor); // slider background  - likely white
    noStroke();
    rect(loc[0], loc[1], loc[2], loc[3], rounding, rounding , rounding, rounding);


    if (activeValue > loc[3]+loc[1] -sliderHeight/2 -rounding) {
      activeValue = loc[3]+loc[1]-sliderHeight/2-rounding;
    }
    if (activeValue < loc[1]+sliderHeight/2 + rounding ) {
      activeValue = loc[1]+sliderHeight+rounding;
    }

    if (GRADIENT == false ) {
      // fill whole slider
      fill(backgroundColor); // slider background  - likely white
      noStroke();
      rect(loc[0], loc[1], loc[2], loc[3], rounding, rounding , rounding, rounding);

      fill(activeColor);
      //rect(loc[0],loc[3]+loc[1]-rounding,loc[2],rounding,rounding,rounding);
      rect(loc[0], activeValue, loc[2], loc[3]-activeValue+loc[1]);

      stroke(sliderColor);
      //fill(sliderColor);
      rect(loc[0]+3, activeValue-sliderHeight/2, loc[2]-6, sliderHeight);
    }
    else {
      // fill the slider with gradient

      fill(gradientStart); // slider background  - likely white
      noStroke();
      rect(loc[0], loc[1], loc[2], loc[3], rounding, rounding, rounding, rounding);

      // fill bottom
      fill(gradientEnd);
      rect(loc[0], loc[3]+loc[1]-rounding*2, loc[2], rounding*2, rounding, rounding , rounding, rounding);


      // gradient fill
      for (int i = loc[1]+rounding ; i < loc[3]+loc[1]-rounding; i ++ ) {
        float j = map (i, loc[1]+rounding, loc[3]+loc[1], 0, 1 );
        color intermediate = lerpColor(gradientStart, gradientEnd, j);
        stroke(intermediate);
        line (loc[0], i, loc[0]+loc[2], i);
      }



      if (sliderColor == GREYSCALE) {
        stroke(255, 0, 0);
      }
      else {
        stroke(sliderColor ^ 0x00FFFFFF);
    
      }

      rect(loc[0]+3, activeValue-sliderHeight/2, loc[2]-6, sliderHeight);
    }
    //Border
    noFill();
    strokeWeight(2);
    stroke(borderColor);
    rect(loc[0], loc[1], loc[2], loc[3], rounding, rounding, rounding,rounding);

    fill(sliderColor);
    if (sliderColor == GREYSCALE) {
      fill(255, 0, 0);
    }

    textAlign(RIGHT);
      textFont (f, 18);
    text(currentValue, loc[0]+loc[2], loc[3]+loc[1]+textBorder);

    return currentValue;
  }

  void checkMouse() {
    if ( body.contains(mouseX, mouseY)) {
      // we are in the slider body
      if (mousePressed ==true) {

        //if (GRADIENT == false ) {
        // we have pressed the mouse as well
        currentValue = (int) map (mouseY, loc[3] + loc[1] , loc[1], sliderMin, sliderMax );
        currentValue = min (currentValue, sliderMax);
        currentValue = max (currentValue, sliderMin);

       
        activeValue = mouseY ;
       // println("actValue " + activeValue + "\tcurrentVal " + currentValue);
      }  // if pressed
    }// if body
  } // end update

    void setColor (color _c) {
    activeColor = color ( red(_c), green(_c), blue(_c), sliderAlpha);
    sliderColor = _c;
  } // end set color
  
  void setField(color _f){
    FIELDSET = true;
    fieldColor = _f;
    
    
  } // end set
  
} // end class

