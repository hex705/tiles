// Barebones Scissors for Arduino 

// this file receives and parses delimited messages.

  #include <Scissors.h>
  
  
  Scissors incomingMessage;   // SCISSORS parse messages
  

  void setup() {
    Serial.begin(19200);
    incomingMessage.begin(Serial);   // NOTE HOW THIS STARTS!!!  -- we need to tell SCISSORS where to find messages! (ie STREAM)
     pinMode(2,OUTPUT);                          // this call starts the serial port at BAUD 19200 and attaches SCISSORS to the incoming serial data
                                                                     
  }
  
  
  void loop(){
    
   
        if (incomingMessage.update() > 0) {         // poll the SCISSOR object -- any new MESSAGES (returns element count)
           
            float  f = incomingMessage.getFloat(2);       // get ELEMENT 2 from MESSAGE -- assuming ELEMENT(2) is a  float
            String s = incomingMessage.getString(0);     // get ELEMENT 0 from MESSAGE -- assuming ELEMENT(0) is a  String
            int    i = incomingMessage.getInt(1);       // get ELEMENT 1 from MESSAGE -- assuming ELEMENT(1) is an int  
         
         
               if (i > 0 ) {
                   digitalWrite(2,HIGH);
                } else {
                   digitalWrite(2,LOW);
                }       
         
        }
         
         
          
  }

