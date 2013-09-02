// Basic UDP communication example using netP5 

import netP5.*;

UdpServer me;
int myPort = xxxx; // this is the port others can send you messages on 

UdpClient other;
String otherIp = "xxx.xxx.xxx.xxx"; // this is the IP address of a program you are trying to reach
int otherPort = xxxx; // this is the port the other computer is listening on

void setup() {
  me = new UdpServer(this, myPort);  
  other = new UdpClient(otherIp, otherPort); 
}

void draw() {    
  // nada
}

void keyPressed() {
  println("Sending data: " + key); 
  // The data has to be sent as an array of bytes -- UDP is pretty low level 
  byte[] data = new byte[1];
  data[0] = (byte)key; // sending the ASCII code of the last key pressed as an example 
  other.send(data);
}

void netEvent( NetMessage msg ) {
  println("Received data");  
  byte[] data = msg.getData();
  // print both the raw byte data and the data converted back to a character
  println(data[0] + " -> " + (char)data[0]);  
}



