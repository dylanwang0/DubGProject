import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class InputHandler implements KeyListener {
  
  Player player;
  
  InputHandler(DubG game) {
    game.addKeyListener(this);
  }
  
  public void setPlayer(Player player) {
    this.player = player;
  }
  
  class Key {
    private int numTimesPressed = 0;
    private boolean pressed = false;

    public int getNumTimesPressed() {
      return numTimesPressed;
    }
    
    public boolean isPressed() {
      return pressed;
    }
    
    public void toggle(boolean isPressed) {
      pressed = isPressed;
      if (isPressed) {
        numTimesPressed++;
      }
    }
    
  }
  
  Key up = new Key(), down = new Key(), left = new Key(), right = new Key(), shoot = new Key(), use = new Key();
  
  public void keyPressed(KeyEvent e) {
    toggleKey(e.getKeyCode(),true);
  }
  
  public void keyReleased(KeyEvent e) {
    toggleKey(e.getKeyCode(),false);
  }
  
  public void keyTyped(KeyEvent e) {
    
  }
  
  public void toggleKey(int keyCode, boolean isPressed) {
    if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
      up.toggle(isPressed);
    }
    if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
      down.toggle(isPressed);
    }
    if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
      left.toggle(isPressed);
    }
    if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
      right.toggle(isPressed);
    }
    if (keyCode == KeyEvent.VK_SPACE) {
      if (isPressed) {
        if (System.currentTimeMillis() - player.getLastBulletTime() >= player.getReloadTime()) {
          player.setLastBulletTime(System.currentTimeMillis());
          shoot.toggle(isPressed);
        }
      } else {
        shoot.toggle(isPressed);
      }
    }
    if (keyCode == KeyEvent.VK_P) {
      if (isPressed) {
        if (System.currentTimeMillis() - player.getLastPotionTime() >= player.getPotionCD()) {
          player.setLastPotionTime(System.currentTimeMillis());
          use.toggle(isPressed);
        }
      } else {
        use.toggle(isPressed);
      }
    }
  }
}