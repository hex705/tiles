import netP5.*;
import oscP5.*;

OscP5 oscNet;
int listeningPort = 12000;

// Someone to talk to
NetAddress destination;
String destinationIP = "127.0.0.1";
int destinationPort = 1200;

void setup() {
    oscNet = new OscP5(this, listeningPort);

    destination = new NetAddress(destinationIP, destinationPort);
}

void draw() {
    sendOscNet(); // send to network destination
}

// Listen for ALL OSC messages
void oscEvent(OscMessage incoming) {
    // all the received messages come here
    println(incoming);
}
// send network messages to destination
void sendOscNet() {
    OscMessage msg = new OscMessage("/outgoingInternet");
    msg.add(mouseX); // this could be any data 
    oscNet.send(msg, destination);
}