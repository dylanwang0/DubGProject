import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class MouseHandler implements MouseListener, MouseMotionListener {
  
  Player player;
  DubG game;
  
  MouseHandler(DubG game) {
    this.game = game;
    game.addMouseListener(this);
    game.addMouseMotionListener(this);
  }
  
  public void setPlayer(Player player) {
    this.player = player;
  }
  
  public void mouseMoved(MouseEvent e) {
    int x = e.getX(), y = e.getY();
    if (player != null && ((player.expLvl == 3 && player.classType == 0) || (player.expLvl == 5 && player.classType <= 2))) {
      if (x >= 157 && x <= 247 && y >= 6 && y <= 94) {
        player.classColour = Colours.get(210,555,-1,-1);
      } else if (x >= 256 && x <= 343 && y >= 6 && y <= 94) {
        player.classColour2 = Colours.get(210,555,-1,-1);
      } else {
        player.classColour = Colours.get(210,430,-1,-1);
        player.classColour2 = Colours.get(210,430,-1,-1);
      }
    } else if (player != null && player.dead) {
      if (x >= 175 && x <= 317 && y >= 250 && y <= 318) {
        player.quitColour = Colours.get(400,-1,-1,555);
      } else {
        player.quitColour = Colours.get(400,-1,-1,544);
      }
    }
  }
  
  public void mouseDragged(MouseEvent e) {
    
  }
  
  public void mousePressed(MouseEvent e) {
    int x = e.getX(), y = e.getY();
    if (player != null && player.expLvl == 3 && player.classType == 0) {
      if (x >= 157 && x <= 247 && y >= 6 && y <= 94) {
        player.classType = 1;
        player.xTileOG = 8;
        player.yTileOG = 28;
        player.colour = Colours.get(-1,111,151,541);
        player.setDamage(1.5);
        player.setBulletSpeed(3);
        player.setReloadTime(800);
        
        Packet20ChangeClass packet = new Packet20ChangeClass(player.getUsername(),1,player.colour,8,28);
        packet.writeData(DubG.game.socketClient);
        
      } else if (x >= 256 && x <= 343 && y >= 6 && y <= 94) {
        player.classType = 2;
        player.xTileOG = 16;
        player.yTileOG = 28;
        player.colour = Colours.get(-1,111,151,224);
        
        Packet20ChangeClass packet = new Packet20ChangeClass(player.getUsername(),2,player.colour,16,28);
        packet.writeData(DubG.game.socketClient);
      } 
      
    } else if (player != null && player.expLvl == 5 && player.classType <= 2) {
      if (x >= 157 && x <= 247 && y >= 6 && y <= 94) {
        if (player.classType == 1) {
          player.classType = 3;
          player.xTileOG = 24;
          player.yTileOG = 28;
          player.colour = Colours.get(-1,111,151,055);
          player.setDamage(2);
          player.setBulletSpeed(4);
          player.setReloadTime(1200);
          
          Packet20ChangeClass packet = new Packet20ChangeClass(player.getUsername(),3,player.colour,24,28);
          packet.writeData(DubG.game.socketClient);
          
        } else {
          player.classType = 5;
          player.xTileOG = 8;
          player.yTileOG = 24;
          player.colour = Colours.get(-1,111,151,040);
          player.setDamage(1);
          player.setReloadTime(250);
          
          Packet20ChangeClass packet = new Packet20ChangeClass(player.getUsername(),5,player.colour,8,24);
          packet.writeData(DubG.game.socketClient);
          
        }
      } else if (x >= 256 && x <= 343 && y >= 6 && y <= 94) {
        if (player.classType == 1) {
          player.classType = 4;
          player.xTileOG = 0;
          player.yTileOG = 24;
          player.colour = Colours.get(-1,111,151,214);
          player.setDamage(3.5);
          player.setReloadTime(1500);
          player.setBulletSpeed(1);
          
          Packet20ChangeClass packet = new Packet20ChangeClass(player.getUsername(),4,player.colour,0,24);
          packet.writeData(DubG.game.socketClient);
          
        } else {
          player.classType = 6;
          player.xTileOG = 16;
          player.yTileOG = 24;
          player.colour = Colours.get(-1,111,151,210);
          player.setDamage(1);
          player.setReloadTime(700);
          
          Packet20ChangeClass packet = new Packet20ChangeClass(player.getUsername(),6,player.colour,16,24);
          packet.writeData(DubG.game.socketClient);
          
        }
      }
    } else if (player.dead) {
      if (x >= 175 && x <= 317 && y >= 250 && y <= 318) {
        System.exit(0);
      }
    }
  }
  
  public void mouseReleased(MouseEvent e) {

  }
  
  public void mouseEntered(MouseEvent e) {

  }
  
  public void mouseExited(MouseEvent e) {

  }
  
  public void mouseClicked(MouseEvent e) {

  }
}