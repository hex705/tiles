#include <OscSerial.h>

// tile support
#include <EthernetUdp.h>
#include <SPI.h>

int baudRate = 9600;
OscSerial oscSerial;
long serialTimerEnd;
long serialTimerInterval = 100;

void setup() {
    Serial.begin(baudRate);
    oscSerial.begin(Serial);
}

void loop() {
    //  limits sends to every 100 mS
    if (millis()>= serialTimerEnd) {
        sendOscSerial();
        serialTimerEnd = millis() + serialTimerInterval;
    }

    // important! this is needed in order to receive messages  
    oscSerial.listen();
}

// incoming messages will arrive here
void oscEvent(OscMessage & msg) {
    // route messages using the "plug" functionality

    // this example redirects messages called "/incoming" to myFunction (below) 
    msg.plug("/incoming", myFunction);
}

// plugged function
void myFunction(OscMessage & msg) {
    // get the data out of the message 
    int data = msg.getInt(0);
}
// send OSC Serial messages
void sendOscSerial() {
    OscMessage msg("/outgoingSerial"); // this could be any pattern
    msg.add(0); // this could be any data
    oscSerial.send(msg);
}