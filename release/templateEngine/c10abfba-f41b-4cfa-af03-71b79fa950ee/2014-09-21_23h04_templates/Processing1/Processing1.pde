import processing.serial.*;
import oscP5.*;

int serialTimerEnd;
int serialTimerInterval = 250;
Serial proSerial;
String serialName = "/dev/tty.usbXXXXXXX"; // put the name of your serial port
int baudRate = 9600; // set your baud rate
OscSerial oscSerial;

void setup() {
    // You'll find your serial device in this list 
    println(Serial.list());
    //start serial 
    proSerial = new Serial(this, serialName, baudRate);
    oscSerial = new OscSerial(this, proSerial);
}

void draw() {
    if (millis()>= serialTimerEnd) {
        sendOscSerial();
        serialTimerEnd = millis() + serialTimerInterval;
    }
}

// listen for ALL incoming OSC messages
void oscEvent(OscMessage incoming) {
    print(incoming);
    int val0 = incoming.get(0).intValue();
}
// send OSC serial messages 
void sendOscSerial() {
    OscMessage msg = new OscMessage("/outgoingSerial");
    msg.add(mouseX);
    oscSerial.send(msg);
}