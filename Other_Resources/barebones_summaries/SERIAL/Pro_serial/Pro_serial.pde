

// Barebones Serial  for Processing

// import

  import processing.serial.*;

// declare
  Serial usbSerial;

void setup() {
  println(Serial.list());
  
  usbSerial = new Serial (this, Serial.list()[6], 9600); // attach arduino with USB
  
}

void draw() {
  
  usbSerial.write("Hi Mom");
   
}
