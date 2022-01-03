class BasicSolidTile extends BasicTile {
  
  BasicSolidTile(int id, int x, int y, int tileColour, int colour) {
    super(id,x,y,tileColour,colour);
    this.solid = true;
  }
}