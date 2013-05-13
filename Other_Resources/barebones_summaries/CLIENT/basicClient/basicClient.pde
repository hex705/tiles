// basic client 
// from Processing help file 
//
//  http://processing.org/reference/libraries/net/Client.html

import processing.net.*;

Client myClient; 
String serverIP = "127.0.0.1";
int    serverPORT = 12345;


int dataReceived; 
 
 
void setup() { 
  size(200, 200); 
  // Connect to the local machine (IP 127.0.0.1) at port 12345.
  // This example will not run if you haven't
  // previously started a server on this port
  myClient = new Client(this, serverIP, serverPORT); 
} 
 
void draw() { 
  
  // clients can read
  
  if (myClient.available() > 0) {   // poll for data
    dataReceived = myClient.read(); 
  } 
  background(dataReceived); 
  
  // clients can write
  myClient.write("test 123");
  
}


