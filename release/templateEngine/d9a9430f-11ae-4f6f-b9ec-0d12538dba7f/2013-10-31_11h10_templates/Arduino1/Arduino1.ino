// IMPORTANT!!! I AM ASSUMING THIS IS THE SAME SETUP AS THE ETHERNET SHIELD FOR NOW
// THE CODE PROBABLY NEEDS TO BE UPDATED TO WORK WITH THE WIFI SHIELD

#include <SPI.h>
#include <Ethernet.h>
#include <EthernetUdp.h>
#include <OscUDP.h>

int baudRate = 9600;
// arduino needs us to start ethernet, then start UDP
// http://arduino.cc/en/Reference/EthernetBegin

// Enter a MAC address and IP address for your SHIELD.
// The IP address will be dependent on your local network	
byte mac[] = {
    0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED
};

// listening IP ==  SHIELD initialization IP
// you need to set this
IPAddress ip(192, 168, 0, 180);

// The local port to listen on
unsigned int listeningPort = 12001;

// The IP address of a computer you are trying to reach
IPAddress destinationIP(192, 168, 0, 101);
int destinationPort = 12000;

// setup a UDP object
EthernetUDP UDP;
OscUDP oscUdp;
NetAddress destination;
long netTimer;

void setup() {
    Serial.begin(baudRate);
    // start ethernet on the shield
    Ethernet.begin(mac, ip);
    // start UDP object, listening on port listeningPort
    UDP.begin(listeningPort)
    oscUdp.begin(UDP);
    destination.set(destinationIP, destinationPort);
}

void loop() {
    //  limits sends to every 100 mS
    if (millis() - netTimer> 100) {
        sendOscNet();
        netTimer = millis();
    }

    // important! this is needed in order to receive messages  
    oscUdp.listen();

    delay(50);
}

// listen for All OSC messages here
void oscEvent(OscMessage & msg) {
    // route messages using the "plug" functionality

    // this example redirects messages called "/incoming" to myFunction (below) 
    msg.plug("/incoming", myFunction);
}

void myFunction(OscMessage & msg) {
    // get the data out of the message 
    int data = msg.getInt(0);
}
// send OSC Internet messages
void sendOscNet() {
    OscMessage msg("/outgoingInternet"); // this could be any pattern
    msg.add(0); // this could be any data
    oscUdp.send(msg, destination);
}