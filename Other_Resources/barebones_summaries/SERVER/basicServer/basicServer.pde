// basic server 
// from Processing help file 
//
//  http://processing.org/reference/libraries/net/Server.html

import processing.net.*;

Server myServer;

int serverPORT = 12345;


int dataToSend = 0;


void setup() {
  size(200, 200);
  // Start your TCP/IP Server on port 'serverPORT' (ie 12345)
  
  // you must start the server before clients try to connect!
  myServer = new Server(this, serverPORT); 
}

void draw() {
  
  dataToSend = (dataToSend + 1)%255;
 
  // Servers can write... 
  myServer.write(dataToSend);
  
  background(dataToSend);
  
  //  servers can read 
  //  you can have your server read directly -- but your life will be easier with SCISSORS
  //  if you really wnat to read on your own (at some point you should -- 
  //  http://processing.org/reference/libraries/net/Server_available_.html
  
}


