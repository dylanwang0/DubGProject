public class Bullet extends Entity {
  
  private String userName;
  private int speed;
  private int numSteps = 0;
  private boolean isMoving;
  private int movingDir = 0;
  private int scale = 1;
  private int id;
  private double damage;
  private int xTileOG;
  private int yTileOG;
  
  private int colour = Colours.get(000,111,-1,-1);
  
  Bullet (Level level, String userName, int x, int y, int speed, int movingDir, int id, double damage) {
    super(level);
    this.userName = userName;
    this.x = x;
    this.y = y;
    this.speed = speed;
    this.movingDir = movingDir;
    this.id = id;
    this.damage = damage;
    this.xTileOG = 0;
    this.yTileOG = 8;
  }
  
  Bullet (Level level, String userName, int x, int y, int speed, int movingDir, int id, double damage, int xTileOG, int yTileOG) {
    super(level);
    this.userName = userName;
    this.x = x;
    this.y = y;
    this.speed = speed;
    this.movingDir = movingDir;
    this.id = id;
    this.damage = damage;
    this.xTileOG = xTileOG;
    this.yTileOG = yTileOG;
  }

  public void move(int xa, int ya) {
    numSteps++;
    if (!hasCollided(xa,ya)) {
      x += xa * speed;
      y += ya * speed;
    } else {
      level.removeEntities.add(this);
      Packet04BulletLeave packet = new Packet04BulletLeave(this.userName,this.id);
      packet.writeData(DubG.game.socketClient);
    }
  }
  
  public boolean hasCollided(int xa, int ya) {    
    int xMin = -1, xMax = 0, yMin = -5, yMax = -2;
    if (movingDir == 2 || movingDir == 3) {
      xMin = -2; xMax = 1; yMin = -5; yMax = -4;
    }
    for (int x = xMin; x < xMax; x++) {
      if (isSolidTile(xa, ya, x, yMin) || isSolidTile(xa, ya, x, yMax)) {
        return true;
      }
    }
    for (int y = yMin; y < yMax; y++) {
      if (isSolidTile(xa, ya, xMin, y) || isSolidTile(xa, ya, xMax, y)) {
        return true;
      }
    }
    
    boolean hitPlayer = false;
    
    for (Entity e: level.getEntities()) {
      if (e instanceof Player) {
        if (!((Player)e).getUsername().equalsIgnoreCase(this.userName) && !((Player)e).getUsername().equals("SERVER")) {
          int tempMovingDir = ((Player)e).getMovingDir();
          int x2 = e.x, y2 = e.y, x3 = e.x, y3 = e.y;
          if (tempMovingDir == 0) {
            x2 -= 4; y2 += 5; x3 += 11; y3 -= 6;
          } else if (tempMovingDir == 1) {
            x2 -= 4; y2 += 6; x3 += 9; y3 -= 7;
          } else if (tempMovingDir == 2) {
            x2 -= 1; y2 += 9; x3 += 10; y3 -= 7;
          } else {
            x2 -= 4; y2 += 9; x3 += 9; y3 -= 7;
          }
          
          if (this.x + xa + xMin >= x3 || x2 >= this.x + xa + xMax) {
            continue;
          }
          if (this.y + ya + yMax <= y3 || y2 <= this.y + ya + yMin) {
            continue;
          }
          hitPlayer = true;
          ((Player)e).setHealth(((Player)e).getHealth() - this.damage);
          if (((Player)e).getUsername().equalsIgnoreCase(DubG.game.player.getUsername())) {
            DubG.sound("Hurt.wav");
          }
          break;
        }
      }
    }
    return hitPlayer;
  }
  
  protected boolean isSolidTile(int xa, int ya, int x, int y) {
    if (level == null) {
      return false;
    }
    Tile lastTile = level.getTile((this.x + x) >> 3, (this.y + y) >> 3);
    Tile newTile = level.getTile((this.x + x + xa) >> 3, (this.y + y + ya) >> 3);
    if (!lastTile.equals(newTile) && newTile.isSolid()) {
      if (newTile.id != 3) {
        if (newTile.id >= 10 && ((CrateTile)newTile).count < 5) {
          ((CrateTile)newTile).count++;
          
          Packet06Crate packet = new Packet06Crate(newTile.id);
          packet.writeData(DubG.game.socketClient);
          
          if (((CrateTile)newTile).count >= 5) {
            int rand = (int)(Math.random() * 100);
            int randId = 0;
            
            if (rand <= 15) {
              randId = 0;
            } else if (rand > 15 && rand <= 30) {
              randId = 1;
            } else if (rand > 30 && rand <= 45) {
              randId = 2;
            } else {
              randId = 3;
            }

            if (level.tilesOver[((this.x + x + xa) >> 3) + (((this.y + y + ya) >> 3) * level.width)] == null) {
              Potion p = new Potion(level,this.x + x + xa + 4, this.y + y + ya + 4, randId);
              level.tilesOver[((this.x + x + xa) >> 3) + (((this.y + y + ya) >> 3) * level.width)] = p;
              
              Packet07Potion packet2 = new Packet07Potion((this.x + x + xa) >> 3, (this.y + y + ya) >> 3, this.userName, randId);
              packet2.writeData(DubG.game.socketClient);
            }
          }
          return true;
        } else if (newTile.id <= 10) {
          return true;
        }
      }
    }
    return false;
  }
  
  public void render(Screen screen) {
    int xTile = xTileOG, yTile = yTileOG;
    int flip = 0;
    
    if (movingDir == 1) {
      flip = 2;
    } else if (movingDir == 2) {
      xTile++;
      flip = 1;
    } else if (movingDir == 3) {
      xTile++;
    }
    
    int modifier = 8 * scale;
    int xOffset = x - modifier/2, yOffset = y - modifier/2 - 4;
    
    if (yTile == 9) {
      colour = Colours.get(411,111,-1,-1);
    }
    screen.render(xOffset, yOffset, xTile + yTile * 32, colour, flip, scale);
  }
  
  public void tick() {
    int xa = 0;
    int ya = 0;
    
    if (movingDir == 0) {
      ya--;
    }
    else if (movingDir == 1) {
      ya++;
    }
    else if (movingDir == 2) {
      xa--;
    }
    else if (movingDir == 3) {
      xa++;
    }
    
    if (xa != 0 || ya != 0) {
      move(xa,ya);
      isMoving = true;
      
      Packet05BulletMove packet = new Packet05BulletMove(this.userName,this.x,this.y,this.numSteps,this.isMoving,this.movingDir,this.id);
      packet.writeData(DubG.game.socketClient);
    }
    else {
      isMoving = false;
    }
    
  }
  
  public String getUsername() {
    return this.userName;
  }
  
  public int getId() {
    return this.id;
  }
  
  public int getSpeed() {
    return this.speed;
  }
  
  public int getNumSteps() {
    return this.numSteps;
  }
  
  public boolean isMoving() {
    return this.isMoving;
  }
  
  public int getMovingDir() {
    return this.movingDir;
  }
  
  public void setNumSteps(int numSteps) {
    this.numSteps = numSteps;
  }
  
  public void setMoving(boolean isMoving) {
    this.isMoving = isMoving;
  }
  
  public void setMovingDir(int movingDir) {
    this.movingDir = movingDir;
  }
  
  
}