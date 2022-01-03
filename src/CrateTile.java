class CrateTile extends BasicSolidTile {
  
  private int[][] animationTileCoords;
  private int currentAnimationIndex;
  public int count;
  
  CrateTile(int id, int[][] animationTileCoords, int tileColour, int colour) {
    super(id,animationTileCoords[0][0],animationTileCoords[0][1],tileColour,colour);
    this.animationTileCoords = animationTileCoords;
    this.currentAnimationIndex = 0;
    this.count = 0;
  }
  
  public void tick() {
    if (count >= 1 && count <= 2 && currentAnimationIndex == 0) {
      currentAnimationIndex++;
    } else if (count >= 3 && count <= 4 && currentAnimationIndex == 1) {
      currentAnimationIndex++;
    } else if (count >= 5 && currentAnimationIndex == 2) {
      currentAnimationIndex++;
      this.solid = false;
    }
    this.tileId = (animationTileCoords[currentAnimationIndex][0] + (animationTileCoords[currentAnimationIndex][1] * 32));
  }
}