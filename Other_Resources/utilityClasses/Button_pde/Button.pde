
 import java.awt.Rectangle;
    int startMouseX;
    int startMouseY;
    Rectangle BODY;
    Rectangle SCALE;
   int btn_w,btn_h,btn_r;
    
 class Button {
   
   PVector loc;
   String label;
   PFont bFont;
   boolean LOCKED = false;
   Rectangle BODY;
   Rectangle SCALE;
  
   
   
   
   Button (int _x, int _y, int _w, int _h, String _s) {
     
     bFont = loadFont("Dialog-24.vlw");
     textFont(bFont, 32);
     label = _s;
     loc = new PVector(_x,_y);
     btn_w = _w;
     btn_h = _h;
     btn_r = 5;
     BODY  = new Rectangle((int)loc.x,(int) loc.y, btn_w, btn_h);
     SCALE = new Rectangle((int)loc.y+1,(int) loc.y+6, btn_h+1, btn_h+6);
     
   } // end constructor
   
   
   void update() {
     
        display();

   } //  end update 
   
   void display() {
     rect( loc.x,loc.y,btn_w,btn_h,btn_r,btn_r,btn_r,btn_r);
   }// end dislay
   
 }
  
  void mousePressed() {
              
           startMouseX = mouseX;
           startMouseY = mouseY;
  }
  
  void mouseDragged() {
           
       if ( BODY.contains(mouseX, mouseY)) {
         
         if (keyPressed) { 
             if (key == CODED) {
               if (keyCode == CONTROL) { 
                   // we have a CONTROL - MOUSE click
                   // what kind of edit ?
                   
                   //  change size
                   if ( SCALE.contains(mouseX,mouseY) ) {
                      btn_w = btn_w + (mouseX - startMouseX);
                      btn_h = btn_h + (mouseY - startMouseY);
                      // display();
                   }
                   
                   
                   if ( BODY.contains(mouseX, mouseY)) {
                     // change location
                   
                   }
                   
                   
                   
               }
             }
           }
           
         
         
       } // end body contains 
       
       // not on this button -- do nothing
       
       
    } // end checkmouse
         
         
      
       
       

