import processing.net.* ;
import processing.serial.*;

class Scissors {
  
 // package variables
  char START_BYTE  =  '*' ;   //  42 = * 
  char DELIMITER   =  ',' ;   //  44 = ,  
  char END_BYTE    =  '#';   //  35 = # 
  char WHITE_SPACE =  ' ';   //  32 = ' ' 
  
  String TOKENS = new String( "" + START_BYTE + DELIMITER + END_BYTE);  // MAKE A STRING OF TOKENS
  
  String incomingData;
  String[] parsedData;
  
  int TYPE = -1;

  Server server;
  Client c;
  Serial s;

  
  boolean DEBUG = false;

  Scissors (Client _c ) {
       TYPE = 1; 
       c= _c;
    
  }
  
  Scissors (Serial _s) {
      TYPE = 2;
      s = _s;
  }

	Scissors (Server _server) {
      TYPE = 3;
      server = _server;
  }
  
  int update(){
	
		if (TYPE == 3 ) {
			// see if any clients have spoken to the server.
		      Client serverClient = server.available();

		      if (serverClient != null ) {
			
				incomingData = serverClient.readStringUntil( END_BYTE );
				
				if (DEBUG) {
					println("SERVER INCOMING data stream (raw)  " +incomingData);
				}
				
			    serverClient.clear();
		         
		      }  // if serverClient
		
		      
		
	    } // end type = 3
          
       
          if (TYPE == 1 ) {
              incomingData = c.readStringUntil( END_BYTE );
              if (DEBUG) {
              println("CLIENT INCOMING data stream (raw)  " +incomingData);
              }
          }  /// end type =2 
          
          
          if (TYPE == 2 ) {
              incomingData = s.readStringUntil( END_BYTE );
              if (DEBUG) {
              println("SERIAL data stream (raw)  " +incomingData);
              }
              
          }
        
        
          if( incomingData != null ){   // make sure you have something
          
            int startPos = incomingData.indexOf( START_BYTE );
            int endPos   = incomingData.indexOf( END_BYTE )  ;
            if (DEBUG) {
            println( "start " + startPos + " end " + endPos);
            }
            
                if ( ( startPos >= 0 ) && ( endPos > startPos ) ){ // make sure the something has a start and end
                 
                    incomingData = incomingData.substring(startPos,endPos);
                    parsedData = splitTokens( incomingData, TOKENS  ); 
                } 
                else {
                    if (DEBUG) {
                      println("incomplete message");
                    }
                     return -1;
                }
              
          } // end IF
          else {
            if(DEBUG) {
            println("Stream Error");
            }
            return -1;
          }
      
      
     if (TYPE == 1)  c.clear();
     if (TYPE == 2)  s.clear();
    
     
     return parsedData.length;
      
   }  // end read net packet
 
 
  String getString(int position) {
    return parsedData[position];
  } 
  
  float getFloat(int position) {
    return Float.parseFloat( parsedData[position] );
  } 
  
  int getInt(int position) {
    
    return Integer.parseInt( parsedData[position] );
  } 
 
  // getters and setters
  void setStartByte(char s) {
    START_BYTE = s;
  }
   void setEndByte(char e) {
     END_BYTE = e;
  }
   void setDelimiter(char d) {
     DELIMITER = d;
  }
 
 
  char getStartByte() {
      return START_BYTE;
  }
  
  char getEndByte() {
     return END_BYTE;
  }
  
  char getDelimiter() {
     return DELIMITER;
  }

  String getRaw() {
	      //println ("debug: " + incomingData);
	      return incomingData;
  }
  
  
} // end class
