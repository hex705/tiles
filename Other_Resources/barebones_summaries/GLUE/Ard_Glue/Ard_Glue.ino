// barebones code
// Arduino Glue


#include <Glue.h>

Glue outgoingMessage;          // GLUE builds MESSAGES


void setup() {
  Serial.begin(9600);          // GLUE needs you to start Serial (or other STREAM)
  
  outgoingMessage.create();    //  create an empty GLUE message -- this sets aside some memory
}


void loop(){
  
      
      // assemble a message with GLUE
      outgoingMessage.clear();                   // start fresh, clear the old message
      
      // order here matters 
      outgoingMessage.add("Ar");          // String added -- given INDEX 0
      outgoingMessage.add( 23 );                // int added    -- given INDEX 1
     // outgoingMessage.add( 4.54f );             // float added  -- given INDEX 2  -- NOTE: the 'f' at end of digits is a cast
     
      // send the message with SERIAL
      Serial.println( outgoingMessage.getPackage()) ;  // open serial debug window to see output
          
}

