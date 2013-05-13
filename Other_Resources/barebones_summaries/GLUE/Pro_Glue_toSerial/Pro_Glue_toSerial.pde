// Barebones for Processing

// import

   // NOTHING TO IMPORT -- but you must add GLUE.pde to the sketch. 
   // Drop a copy of GLUE.pde onto SKETCH
   // or 
   // SKETCH --> ADD FILE --> /yourpath_to_course_files/MPM503_code/utilityClasses/GLUE.pde


// declare
  Glue glue = new Glue();

void setup() {	
  // nothing here
}

void draw() {
  
  // build a message with glue 
  glue.clear();                     // start fresh
       
  glue.add( "teleClient" );         //  add a String
  glue.add(  23 );                  //  add an Int
  glue.add(  5.67 );                //  add a float

  // get the whole package (it’s a String) 
  String myPackage = glue.getPackage();  

  // now SEND the package: 
  // .write(package) with Serial or Network depending on STREAM you are using to send
  
  
  //  (un)comment the following line to see the output of GLUE
   println( myPackage); 
  
}
