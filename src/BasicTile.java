class BasicTile extends Tile {
  
  protected int tileId;
  protected int tileColour;
  
  BasicTile(int id, int x, int y, int tileColour, int colour) {
    super(id,false,false,colour);
    this.tileId = x + y * 32;
    this.tileColour = tileColour;
  }
  
  public void tick() {
  }
  
  public void render(Screen screen, Level level, int x, int y) {
    screen.render(x,y,tileId,tileColour,0,1);
  }
}