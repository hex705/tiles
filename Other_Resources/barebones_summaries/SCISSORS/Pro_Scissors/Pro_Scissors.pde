// Barebones Scissors for Processing

// NOTE THIS FILE DOES NOT HAVE A STREAM -- IT WON'T WORK WITHOUT ONE
// look at full examples to see how to connect a stream 


// import

  // NOTHING TO IMPORT -- but you must add SCISSORS to sketch. 
  
  // DO ONE of following:
      // drop SCISSORS.pde onto SKETCH
      // or 
      // SKETCH --> ADD FILE --> SCISSORS.pde


// declare
Scissors scissors;  // new scissors object

void setup() {
  	
 scissors = new Scissors( STREAM );   // instantiate the object
                         
        // where:
        
        // STREAM is the variable name of: 
        //      the SERIAL port
        //      the CLIENT 
        //      OR
        //      the SERVER 
        // on which you are receiving messages.
}

void draw() {

// poll STREAM to see if anything has arrived
 
   if (scissors.update() > 0) {  // returns number of elements in MESSAGE 
     
       // if new ELEMENTS have arrived extract them
       // You need to know the TYPE and the INDEX to get 
       // an ELEMENT from a MESSAGE.
       
       // note you can retrieve out of order
       
       float    f = scissors.getFloat(2);  // get a  float  element from INDEX = 2 and stores it in f
       String   s = scissors.getString(0); // get a  String element from INDEX = 0 and stores it in s
       int      i = scissors.getInt(1);    // get an int    element from INDEX = 1 and stores it in i
 
       
       println( " float f:: " + f + " int i:: " + i + " String s:: " + s ); // would display message in debug window

   } 
   
} // end draw

