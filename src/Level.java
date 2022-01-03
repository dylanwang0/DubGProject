import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;

class Level {
  
  private byte[] tiles;
  Entity[] tilesOver;
  private String imagePath;
  private BufferedImage image;
  int width;
  int height;
  List<Entity> entities = new ArrayList<Entity>();
  List<Entity> addedEntities = new ArrayList<Entity>();
  List<Entity> removeEntities = new ArrayList<Entity>();
  
  Level(String imagePath) {
    if (imagePath != null) {
      this.imagePath = imagePath;
      this.loadLevelFromFile();
    } else {
      this.width = 64;
      this.height = 64;
      this.generateLevel();
    }
  }
  
  private void loadLevelFromFile() {
    try {
      this.image = ImageIO.read(Level.class.getResourceAsStream(this.imagePath));
      this.width = image.getWidth();
      this.height = image.getHeight();
      tiles = new byte[(width + 1) * (height + 1)];
      tilesOver = new Entity[(width + 1) * (height + 1)];
      this.loadTiles();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void loadTiles() {
    int[] tileColours = this.image.getRGB(0,0,width,height,null,0,width);
    byte index = 10;
    Tile[] temp = Tile.tiles;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        for (Tile t: temp) {
          if (t == null) {
            break;
          }
          
          if (tileColours[x + y * width] == 0xFFC72ABB) {
            Turret turret = new Turret(this,x*8 + 4,y*8 + 8,3);
            tilesOver[x + y * width] = turret;
            break;
          } else if (tileColours[x + y * width] == 0xFF00965A) {
            Turret turret = new Turret(this,x*8 + 4, y * 8 + 8, 0);
            tilesOver[x + y * width] = turret;
            break;
          } else if (tileColours[x + y * width] == 0xFF1D0098) {
            Turret turret = new Turret(this,x*8 + 4, y * 8 + 8, 2);
            tilesOver[x + y * width] = turret;
            break;
          }
          
          if (t.getColour() == tileColours[x + y * width]) {
            if (t.getColour() == 0xFFF0F000) {
              this.tiles[x + y * width] = index;
              Tile CRATE = new CrateTile(index,new int[][] {{6,0},{0,6},{1,6},{1,0}},Colours.get(320,210,-1,432),0xFFA1B3C2);
              index++;
            } else {
              this.tiles[x + y * width] = t.getId();
            }
          }
        }
      }
    }
  }
  
  private void saveLevelToFile() {
    try {
      ImageIO.write(image,"png",new File(Level.class.getResource(this.imagePath).getFile()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void alterTile(int x, int y, Tile newTile) {
    this.tiles[x + y * width] = newTile.getId();
    image.setRGB(x, y, newTile.getColour());
  }
  
  public void generateLevel() {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (x * y % 10 < 7) {
          tiles[x + y * width] = Tile.GROUND.getId();
        } else {
          tiles[x + y * width] = Tile.WALL.getId();
        }
      }
    }
  }
  
  public synchronized List<Entity> getEntities() {
    return this.entities;
  }
  
  public synchronized List<Entity> getAddedEntities() {
    return this.addedEntities;
  }
  
  public synchronized List<Entity> getRemovedEntities() {
    return this.removeEntities;
  }
  
  public synchronized void tick() {
    for (Entity e: this.getEntities()) {
      e.tick();
    }
    
    List<Entity> temp = this.getAddedEntities();
    while (temp.size() != 0) {
      this.addEntity(temp.get(0));
      temp.remove(0);
    }
    
    temp = this.getRemovedEntities();
    while (temp.size() != 0) {
      this.removeEntity(temp.get(0));
      temp.remove(0);
    }
    
    for (Tile t: Tile.tiles) {
      if (t == null) {
        break;
      }
      t.tick();
    }
    
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (tilesOver[x + y * width] != null) {
          tilesOver[x + y * width].tick();
        }
      }
    }
  }
  
  public void renderTiles(Screen screen, int xOffset, int yOffset) {
    if (xOffset < 0) {
      xOffset = 0;
    }
    if (xOffset > ((width << 3) - screen.width)) {
      xOffset = ((width << 3) - screen.width);
    }
    if (yOffset < 0) {
      yOffset = 0;
    }
    if (yOffset > ((height << 3) - screen.height)) {
      yOffset = ((height << 3) - screen.height);
    }
    screen.setOffset(xOffset,yOffset);
    
    for (int y = (yOffset >> 3); y < (yOffset  + screen.height >> 3) + 1; y++) {
      for (int x = (xOffset >> 3); x < (xOffset + screen.width >> 3) + 1; x++) {
        getTile(x,y).render(screen,this, x << 3, y << 3);
      }
    }
    
    for (int y = (yOffset >> 3); y < (yOffset  + screen.height >> 3) + 1; y++) {
      for (int x = (xOffset >> 3); x < (xOffset + screen.width >> 3) + 1; x++) {
        if (DubG.game.player.dead) {
          screen.render(x << 3, y << 3, 0, Colours.get(000,000,000,000), 0, 1);
        } else {
          if (tilesOver[x + y * width] != null) {
            tilesOver[x + y * width].render(screen);
          }
        }
      }
    }
  }
  
  public void renderEntities(Screen screen) {
    List<Entity> temp = this.getEntities();
    for (int i = temp.size() - 1; i >= 0; i--) {
      Entity e = temp.get(i);
      e.render(screen);
    }
  }
  
  public Tile getTile(int x, int y) {
    if (0 > x || x >= width || 0 > y || y >= height) {
      return Tile.VOID;
    }
    return Tile.tiles[tiles[x + y * width]];
  }
  
  public Potion getPotion(int x, int y) {
    if (tilesOver[x + y * width] instanceof Potion) {
      return (Potion)tilesOver[x + y * width];
    }
    return null;
  }
  
  public void setPotion(int x, int y, int id) {
    if (id == -1) {
      tilesOver[x + y * width] = null;
    } else {
      tilesOver[x + y * width] = new Potion(this, x * 8 + 4, y * 8 + 4, id);
    }
  }
  
  public void setPlayerHealth(String username) {
    int index = getPlayerMPIndex(username);
    PlayerMP player = (PlayerMP)this.getEntities().get(index);
    player.setHealth(Math.min(player.getHealth() + 2.0,5.0));
  }
  
  public void setPlayerHealed(String username, boolean isHealed) {
    int index = getPlayerMPIndex(username);
    PlayerMP player = (PlayerMP)this.getEntities().get(index);
    player.setHealed(isHealed);
  }
  
  public void setPlayerRage(String username, boolean isRaged) {
    int index = getPlayerMPIndex(username);
    PlayerMP player = (PlayerMP)this.getEntities().get(index);
    player.setRage(isRaged);
  }
  
  public void setPlayerFast(String username, boolean isFast) {
    int index = getPlayerMPIndex(username);
    PlayerMP player = (PlayerMP)this.getEntities().get(index);
    player.setFast(isFast);
  }
  
  public void setPlayerExped(String username, boolean isExped) {
    int index = getPlayerMPIndex(username);
    PlayerMP player = (PlayerMP)this.getEntities().get(index);
    player.setExped(isExped);
  }
  
  public void changePlayerClass(String username, int classType, int colour, int xTileOG, int yTileOG) {
    int index = getPlayerMPIndex(username);
    PlayerMP player = (PlayerMP)this.getEntities().get(index);
    player.classType = classType;
    player.colour = colour;
    player.xTileOG = xTileOG;
    player.yTileOG = yTileOG;
  }
  
  public void addEntity(Entity entity) {
    this.getEntities().add(entity);
  }
  
  public void removeEntity(Entity entity) {
    this.getEntities().remove(entity);
  }
  
  public void removePlayerMP(String userName) {
    int index = 0;
    List<Entity> temp = this.getEntities();
    for (Entity e: temp) {
      if (e instanceof PlayerMP && ((PlayerMP)e).getUsername().equalsIgnoreCase(userName)) {
        break;
      }
      index++;
    }
    if (index < temp.size()) {
      temp.remove(index);
    }
  }
  
  public synchronized void removeBullet(String userName, int id) {
    int index = 0;
    List<Entity> temp = this.getEntities();
    for (Entity e: temp) {
      if (e instanceof Bullet && ((Bullet)e).getUsername().equals(userName) && ((Bullet)e).getId() == id) {
        break;
      }
      index++;
    }
    if (index < temp.size()) {
      this.getRemovedEntities().add(temp.get(index));
    }
  }
  
  private int getPlayerMPIndex(String username) {
    int index = 0;
    List<Entity> temp = this.getEntities();
    for (Entity e: temp) {
      if (e instanceof PlayerMP && ((PlayerMP)e).getUsername().equals(username)) {
        break;
      }
      index++;
    }
    return index;
  }
  
  public void movePlayer(String username, int x, int y, int numSteps, boolean isMoving, int movingDir) {
    int index = getPlayerMPIndex(username);
    List<Entity> temp = this.getEntities();
    if (index < temp.size()) {
      PlayerMP player = (PlayerMP)temp.get(index);
      player.x = x;
      player.y = y;
      player.setMoving(isMoving);
      player.setNumSteps(numSteps);
      player.setMovingDir(movingDir);
    }
  }
  
  public void moveBullet(String userName, int x, int y, int numSteps, boolean isMoving, int movingDir, int id) {
    int index = 0;
    List<Entity> temp = this.getEntities();
    for (Entity e: temp) {
      if (e instanceof Bullet && ((Bullet)e).getUsername().equals(userName) && ((Bullet)e).getId() == id) {
        break;
      }
      index++;
    }
    if (index < temp.size()) {
      Bullet bullet = (Bullet)temp.get(index);
      bullet.x = x;
      bullet.y = y;
      bullet.setMoving(isMoving);
      bullet.setNumSteps(numSteps);
      bullet.setMovingDir(movingDir);
    }
  }
  
  public void breakCrate(int id) {
    ((CrateTile)Tile.tiles[id]).count = ((CrateTile)Tile.tiles[id]).count;
  }
}