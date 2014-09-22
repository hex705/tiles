#include <SPI.h>
#include <Ethernet.h>
#include <EthernetUdp.h>
#include <OscUDP.h>

int baudRate = 9600;
// arduino needs us to start ethernet, then start UDP
// http://arduino.cc/en/Reference/EthernetBegin

// Enter a MAC address and IP address for your SHIELD.
// BLUE shield -- sticker on bottom has mac
// WHITE shield -- sticker on top -- change LAST 2 pairs
byte mac[] = {
    0x00, 0xA1, 0xB1, 0xEF, 0xXX, 0xXX
};

// The local port to listen on
unsigned int listeningPort = 12001;

// The IP address of a computer you are trying to reach
IPAddress destinationIP(192, 168, 0, 101);
int destinationPort = 12000;

// setup a UDP object
EthernetUDP UDP;
OscUDP oscUdp;
NetAddress destination;
long netTimerEnd;
long netTimerInterval = 100;

void setup() {
    Serial.begin(baudRate);
    // start ethernet on the shield
    Ethernet.begin(mac);

    // start UDP object, listening on port listeningPort
    UDP.begin(listeningPort);

    // display IP in serial debug (uncomment to use)
    // Serial.begin(9600);
    // Serial.println(Ethernet.localIP());
    oscUdp.begin(UDP);
    destination.set(destinationIP, destinationPort);
}

void loop() {
    //  limits sends to every 100 mS
    if (millis()>= netTimerEnd) {
        sendOscNet();
        netTimerEnd = millis() + netTImerInterval;
    }

    // important! this is needed in order to receive messages  
    oscUdp.listen();

    delay(50);
}

// listen for All OSC messages here
void oscEvent(OscMessage & msg) {
    // handle data here 
    // OR
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