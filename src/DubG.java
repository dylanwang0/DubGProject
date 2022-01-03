import java.awt.Canvas;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.BufferStrategy;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.Scanner;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent;

public class DubG extends Canvas implements Runnable {
  
  private static final long serialVersionUID = 1L;
  
  static final int WIDTH = 160;
  static final int HEIGHT = WIDTH / 12 * 9;
  static final int SCALE = 3;
  static final String NAME = "DubG";
  static final Dimension DIMENSIONS = new Dimension(WIDTH*SCALE, HEIGHT*SCALE);
  static boolean runServer = false;
  public static DubG game;
  
  JFrame frame;
  boolean running = false;
  int tickCount = 0;
  
  private BufferedImage image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
  //the number of pixels inside of the image
  private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
  private int[] colours = new int[6*6*6];
  
  private Screen screen;
  InputHandler input;
  MouseHandler mouseHandler;
  WindowHandler windowHandler;
  
  Level level;
  Player player;
  
  GameClient socketClient;
  GameServer socketServer;
  
  static JFrame menu;
  
  public int[][] spawnCoords = {{256,47}, {32,252}, {256,468}, {480,252}};
  
  static Color BLUE = new Color(0,0,255);
  
  
  public DubG() {
    setMinimumSize(new Dimension(WIDTH*SCALE,HEIGHT*SCALE));
    setMaximumSize(new Dimension(WIDTH*SCALE,HEIGHT*SCALE));
    setPreferredSize(new Dimension(WIDTH*SCALE,HEIGHT*SCALE));
    
    frame = new JFrame(NAME);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    
    //Center the canvas inside the jframe
    frame.add(this,BorderLayout.CENTER);
    frame.pack();
    
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
  
  public void init() {
    game = this;
    int index = 0;
    for (int r = 0; r < 6; r++) {
      for (int g = 0; g < 6; g++) {
        for (int b = 0; b < 6; b++) {
          int rr = (r * 255/5);
          int gg = (g * 255/5);
          int bb = (b * 255/5);
          
          colours[index++] = rr << 16 | gg << 8 | bb;
        }
      }
    }
    screen = new Screen(WIDTH,HEIGHT,new SpriteSheet("res/sprite_sheet.png"));
    input = new InputHandler(this);
    mouseHandler = new MouseHandler(this);
    windowHandler = new WindowHandler(this);
    level = new Level("res/Main_Map.png");
    if (socketServer == null) {
      int rand = (int)(Math.random() * 100);
      int randId = 0;
      
      if (rand <= 25) {
        randId = 0;
      } else if (rand > 25 && rand <= 50) {
        randId = 1;
      } else if (rand > 50 && rand <= 75) {
        randId = 2;
      } else {
        randId = 3;
      }
      
      player = new PlayerMP(level,spawnCoords[randId][0],spawnCoords[randId][1],input,JOptionPane.showInputDialog(this,"Please enter a username:"),400,null,-1);
    } else {
      player = new PlayerMP(level,257,253,input,"SERVER",400,null,-1);
    }
    
    input.setPlayer(player);
    mouseHandler.setPlayer(player);
    level.addEntity(player);
    Packet00Login loginPacket = new Packet00Login(player.getUsername(), player.x, player.y);
    if (socketServer != null) {
      socketServer.addConnection((PlayerMP)player,loginPacket);
    }
    loginPacket.writeData(socketClient);
  }
  
  public synchronized void start() {
    running = true;
    new Thread(this).start();
    
    if (runServer) {
      socketServer = new GameServer(this);
      socketServer.start();
    }
    socketClient = new GameClient(this, "localhost");
    socketClient.start();
  }
  
  public synchronized void stop() {
    running = false;
  }
  
  public void run() {
    long lastTime = System.nanoTime();
    double nsPerTick = 1000000000D/60D;
    
    int ticks = 0;
    int frames = 0;
    
    long lastTimer = System.currentTimeMillis();
    double delta = 0;
    
    if (socketServer == null) {
      init();
    }
    
    while (running) {
      long now = System.nanoTime();
      delta += (now - lastTime) / nsPerTick;
      lastTime = now;
      boolean shouldRender = true;
      
      while (delta >= 1) {
        ticks++;
        tick();
        delta--;
        shouldRender = true;
      }
      
      try {
        Thread.sleep(2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      if (shouldRender) {
        frames++;
        render();
      }
      
      if (System.currentTimeMillis() - lastTimer >= 1000) {
        lastTimer += 1000;
        frames = 0;
        ticks = 0;
      }
    }
  }
  
  public void tick() {
    tickCount++;
    level.tick();
  }
  
  public void render() {
    BufferStrategy bs = getBufferStrategy();
    if (bs == null) {
      //triple buffering (the higher the buffer, the more tearing that is reduced in the image)
      createBufferStrategy(3);
      return;
    }
    
    int xOffset = player.x - (screen.width/2), yOffset = player.y - (screen.height/2);
    level.renderTiles(screen,xOffset,yOffset);
    
    level.renderEntities(screen);
    
    for (int y = 0; y < screen.height; y++) {
      for (int x = 0; x < screen.width; x++) {
        int colourCode = screen.pixels[x+y*screen.width];
        if (colourCode < 255) {
          pixels[x+y*WIDTH] = colours[colourCode];
        }
      }
    }
    
    Graphics g = bs.getDrawGraphics();    
    g.drawImage(image,0,0,getWidth(),getHeight(),null);
    
    g.dispose();
    bs.show();
  }
  
  /**
   * sound
   * This method plays the background music for the game
   * @return void
   */
  
  public static void sound(String fileName) {
    try {
      File audioFile = new File(fileName);
      AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
      DataLine.Info info = new DataLine.Info(Clip.class, audioStream.getFormat());
      Clip clip = (Clip) AudioSystem.getLine(info);
      if (fileName.equals("IntroMusic.wav")) {
        clip.addLineListener(new RepeatListener());
      } else {
        clip.addLineListener(new SoundListener());
      }
      clip.open(audioStream);
      clip.start();
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * SoundListener
   * This class indicates when the sound effect is finished
   * @return void
   */
    
  static class SoundListener implements LineListener {
    public void update(LineEvent event) {
      if (event.getType() == LineEvent.Type.STOP) {
        event.getLine().close();
      }
    }
  }
  
  static class RepeatListener implements LineListener {
    public void update(LineEvent event) {
      if (event.getType() == LineEvent.Type.STOP) {
        event.getLine().close();
        sound("IntroMusic.wav");
      }
    }
  }
  
  static void displayMenu() {
    menu = new JFrame(NAME);
    menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    menu.setLayout(new BorderLayout());
    menu.setPreferredSize(new Dimension(WIDTH*SCALE,HEIGHT*SCALE));
    
    //Center the canvas inside the jframe
    MenuPanel menuPanel = new MenuPanel();
    menu.add(menuPanel,BorderLayout.CENTER);
    
    menu.pack();
    
    menu.setResizable(false);
    menu.setLocationRelativeTo(null);
    menu.setVisible(true);
  }
  
  static class MenuPanel extends JPanel implements MouseListener, MouseMotionListener {
    
    Color BLACK = new Color(0,0,0);
    Color WHITE = new Color(255,255,255);
    Color LIGHT_BLUE = new Color(190,226,240);
    Color DARK_RED = new Color(88,0,0);
    
    boolean inMenu = true;
    boolean playHover = false;
    boolean controlHover = false;
    boolean quitHover = false;
    boolean playClick = false;
    boolean hostHover = false;
    boolean playerHover = false;
    boolean leftHover = false;
    boolean rightHover = false;
    int controlPic = 0;
    
    Image title = Toolkit.getDefaultToolkit().getImage("Title.png");
    Image subtitle = Toolkit.getDefaultToolkit().getImage("Subtitle.png");
    Image pixelGuy = Toolkit.getDefaultToolkit().getImage("Pixel Guy.gif");
    Image rightArrow = Toolkit.getDefaultToolkit().getImage("ArrowRight.png");
    Image leftArrow = Toolkit.getDefaultToolkit().getImage("ArrowLeft.png");
    Image leftArrowWhite = Toolkit.getDefaultToolkit().getImage("ArrowLeftWhite.png");
    Image rightArrowWhite = Toolkit.getDefaultToolkit().getImage("ArrowRightWhite.png");
    Image[] controls = {Toolkit.getDefaultToolkit().getImage("ControlPage1.png"), Toolkit.getDefaultToolkit().getImage("ControlPage2.png"),
      Toolkit.getDefaultToolkit().getImage("ControlPage3.png")};
    
    MenuPanel() {
      addMouseListener(this);
      addMouseMotionListener(this);
    }
    
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (inMenu) {
        g.setColor(BLACK);
        g.fillRect(0,0,1000,1000);
        
        g.drawImage(title,20,20,260,80,this);
        g.drawImage(subtitle,30,96,260,30,this);
        
        g.drawImage(pixelGuy,300,50,170,240,this);
        
        g.setColor(WHITE);
        if (!playClick) {
          g.drawRect(29,149,251,36);
        } else {
          g.drawRect(29,149,120,36);
          g.drawRect(159,149,120,36);
        }
        g.drawRect(29,199,251,36);
        g.drawRect(29,249,251,36);
        
        if (!playClick) {
          if (playHover) {
            g.setColor(WHITE);
          } else {
            g.setColor(BLACK);
          }
          g.fillRect(30,150,250,35);
        } else {
          if (hostHover) {
            g.setColor(WHITE);
          } else {
            g.setColor(BLACK);
          }
          g.fillRect(30,150,119,35);
          
          if (playerHover) {
            g.setColor(WHITE);
          } else {
            g.setColor(BLACK);
          }
          g.fillRect(160,150,119,35);
        }
        
        if (controlHover) {
          g.setColor(WHITE);
        } else {
          g.setColor(BLACK);
        }
        g.fillRect(30,200,250,35);
        
        if (quitHover) {
          g.setColor(WHITE);
        } else {
          g.setColor(BLACK);
        }
        g.fillRect(30,250,250,35);
        
      
        g.setFont(new Font("Serif", Font.PLAIN, 20));
        
        if (!playClick) {
          if (playHover) {
            g.setColor(BLACK);
          } else {
            g.setColor(WHITE);
          }
          g.drawString("PLAY",40,175);
        } else {
          if (hostHover) {
            g.setColor(BLACK);
          } else {
            g.setColor(WHITE);
          }
          g.drawString("HOST",40,175);
          
          if (playerHover) {
            g.setColor(BLACK);
          } else {
            g.setColor(WHITE);
          }
          g.drawString("PLAYER",170,175);
        }
        
        if (controlHover) {
          g.setColor(BLACK);
        } else {
          g.setColor(WHITE);
        }
        g.drawString("CONTROLS",40,225);
        
        if (quitHover) {
          g.setColor(BLACK);
        } else {
          g.setColor(WHITE);
        }
        g.drawString("QUIT",40,275);
      } else {
        g.drawImage(controls[controlPic-1],0,0,480,329,this);
        if (leftHover) {
          g.drawImage(leftArrowWhite,20,0,80,60,this);
        } else {
          g.drawImage(leftArrow,20,0,80,60,this);
        }
        
        if (rightHover) {
          g.drawImage(rightArrowWhite,375,0,80,60,this);
        } else {
          g.drawImage(rightArrow,375,0,80,60,this);
        }
      }
    }
    
    public void mouseMoved(MouseEvent e) {
      int x = e.getX(), y = e.getY();
      if (inMenu) {
        if (x >= 31 && x <= 279 && y >= 153 && y <= 187 && !playClick) {
          playHover = true;
        } else if (x >= 31 && x <= 279 && y >= 203 && y <= 238) {
          controlHover = true;
        } else if (x >= 31 && x <= 279 && y >= 252 && y <= 287) {
          quitHover = true;
        } else if (x >= 31 && x <= 149 && y >= 153 && y <= 187 && playClick) {
          hostHover = true;
        } else if (x >= 161 && x <= 279 && y >= 153 && y <= 187 && playClick) {
          playerHover = true;
        } else {
          playHover = false;
          controlHover = false;
          quitHover = false;
          hostHover = false;
          playerHover = false;
        }
      } else {
        if (x >= 45 && x <= 93 && y >= 22 && y <= 42) {
          leftHover = true;
        } else if (x >= 383 && x <= 446 && y >= 22 && y <= 42) {
          rightHover = true;
        } else {
          leftHover = false;
          rightHover = false;
        }
      }
    }
    
    public void mouseDragged(MouseEvent e) {
    
    }
  
    public void mousePressed(MouseEvent e) {
      int x = e.getX(), y = e.getY();
      if (inMenu) {
        if (x >= 31 && x <= 279 && y >= 153 && y <= 187 && !playClick) {
          playClick = true;
        } else if (x >= 31 && x <= 279 && y >= 203 && y <= 238) {
          inMenu = false;
          controlPic = 1;
        } else if (x >= 31 && x <= 279 && y >= 252 && y <= 287) {
          System.exit(0);
        } else if (x >= 31 && x <= 149 && y >= 153 && y <= 187 && playClick) {
          runServer = true;
          menu.dispose();
          new DubG().start();
        } else if (x >= 161 && x <= 279 && y >= 153 && y <= 187 && playClick) {
          menu.dispose();
          new DubG().start();
        }
      } else {
        if (x >= 45 && x <= 93 && y >= 22 && y <= 42) {
          controlPic--;
          if (controlPic == 0) {
            inMenu = true;
          }
        } else if (x >= 383 && x <= 446 && y >= 22 && y <= 42) {
          controlPic++;
          if (controlPic == 4) {
            inMenu = true;
            controlPic = 0;
          }
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
  
  public static void main(String[] args) throws Exception {
    sound("IntroMusic.wav");
    displayMenu();
    //new DubG().start();
  }
}