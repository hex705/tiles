class Tile {
  int id;
  int[] connections;
  int topcodeID;
  String topcodeName;
  String type;
  
  float centerX, centerY, diameter;
  
  Tile(int _topcodeID, String _topcodeName, String _type, int c0, int c1, int c2, int c3) {
    
    connections = new int[4];
    topcodeID = _topcodeID;
    topcodeName = _topcodeName;
    type = _type;

    // connections are in order: TRBL
    connections[0] = c0;
    connections[1] = c1;
    connections[2] = c2;
    connections[3] = c3;
  }
  public String toString() {
    return str(this.topcodeID) + " " + this.topcodeName + " (" + this.type + ") " + this.id + " (" + this.centerX + ", " + this.centerY + ", " + this.diameter + ")";
  }
}
