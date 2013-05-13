class Glue {
  
  // default package variables
  char START_BYTE  =  '*' ;   //  42 = * 
  char DELIMITER   =  ',' ;   //  44 = ,  
  char END_BYTE    =  '#' ;   //  35 = # 
  char WHITE_SPACE =  ' ' ;   //  32 = ' ' 
  
  String serverID;
  
  String gluePackage;
  

  Glue () {
    // probbly need to do something here 
    clear();
    
  } 
 
  

  // clear the current message
  void clear( ) {
    gluePackage = "";
    gluePackage += START_BYTE ;
  }
  
   // function to send messages over the server
  void add( int i ) {
    gluePackage += i;
    gluePackage += DELIMITER;
  }
  
  void add( float f ) {
     gluePackage += f ;
     gluePackage += DELIMITER ;
    
  }
  
  void add ( String stringToAdd ) {
   gluePackage += stringToAdd ;
   gluePackage += DELIMITER ;
   
  } 
    
  String getPackage() {
    gluePackage += END_BYTE ;
    return gluePackage;
  } 
  
  String debug() {
    println( "debug: " + gluePackage);
    return gluePackage;
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
  
  String getID () {
    return serverID;
  }
 
  
  
  
} // end class 


