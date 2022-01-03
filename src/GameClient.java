import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;

public class GameClient extends Thread {
  
  private InetAddress ipAddress;
  private DatagramSocket socket;
  private DubG game;
  
  public GameClient(DubG game, String ipAddress) {
    this.game = game;
    try {
      this.socket = new DatagramSocket();
      this.ipAddress = InetAddress.getByName(ipAddress);
    } catch (SocketException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }
  
  public void run() {
    while (true) {
      //the array of bytes of data that we will send to and from the server
      byte[] data = new byte [1024];
      DatagramPacket packet = new DatagramPacket(data,data.length);
      try {
        socket.receive(packet);
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      this.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
      //System.out.println("SERVER > " + new String(packet.getData()));
    }
  }
  
  private void parsePacket(byte[] data, InetAddress address, int port) {
    String message = new String(data).trim();
    Packet.PacketTypes type = Packet.lookupPacket(message.substring(0,2));
    Packet packet = null;
    switch(type) {
      default:
      case INVALID:
        break;
      case LOGIN:
        packet = new Packet00Login(data);
        this.handleLogin((Packet00Login)packet, address, port);
        break;
      case DISCONNECT:
        packet = new Packet01Disconnect(data);
        System.out.println("["+address.getHostAddress() + ":" + port + "]" + ((Packet01Disconnect)packet).getUsername() + " has left the world. . .");
        game.level.removePlayerMP(((Packet01Disconnect)packet).getUsername());
        break;
      case MOVE:
        packet = new Packet02Move(data);
        this.handleMove((Packet02Move)packet);
        break;
      case ENTER:
        packet = new Packet03BulletEnter(data);
        this.handleEnter((Packet03BulletEnter)packet);
        break;
      case LEAVE:
        packet = new Packet04BulletLeave(data);
        this.handleLeave((Packet04BulletLeave)packet);
        break;
      case BULLET:
        packet = new Packet05BulletMove(data);
        this.handleBulletMove((Packet05BulletMove)packet);
        break;
      case CRATE:
        packet = new Packet06Crate(data);
        this.handleCrate((Packet06Crate)packet);
        break;
      case POTION:
        packet = new Packet07Potion(data);
        this.handlePotion((Packet07Potion)packet);
        break;
      case HEALTH:
        packet = new Packet10HealthPotion(data);
        this.handleHealth((Packet10HealthPotion)packet);
        break;
      case RAGESTART:
        packet = new Packet11RageStart(data);
        this.handleRageStart((Packet11RageStart)packet);
        break;
      case RAGEEND:
        packet = new Packet12RageEnd(data);
        this.handleRageEnd((Packet12RageEnd)packet);
        break;
      case SPEEDSTART:
        packet = new Packet13SpeedStart(data);
        this.handleSpeedStart((Packet13SpeedStart)packet);
        break;
      case SPEEDEND:
        packet = new Packet14SpeedEnd(data);
        this.handleSpeedEnd((Packet14SpeedEnd)packet);
        break;
      case HEALSTOP:
        packet = new Packet15HealStop(data);
        this.handleHealStop((Packet15HealStop)packet);
        break;
      case EXPSTART:
        packet = new Packet16ExpStart(data);
        this.handleExpStart((Packet16ExpStart)packet);
        break;
      case EXPEND:
        packet = new Packet17ExpStop(data);
        this.handleExpStop((Packet17ExpStop)packet);
        break;
      case CLASS:
        packet = new Packet20ChangeClass(data);
        this.handleChangeClass((Packet20ChangeClass)packet);
        break;
    }
  }
  
  public void sendData(byte[] data) {
    DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1331);
    try {
      socket.send(packet); 
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void handleLogin(Packet00Login packet, InetAddress address, int port) {
    System.out.println("["+address.getHostAddress() + ":" + port + "]" + packet.getUsername() + " has joined the game . . .");
    PlayerMP player = new PlayerMP(game.level, packet.getX(), packet.getY(), packet.getUsername(), 400, address, port);
    game.level.addedEntities.add(player);
  }
  
  private void handleMove(Packet02Move packet) {
    this.game.level.movePlayer(packet.getUsername(), packet.getX(), packet.getY(), packet.getNumSteps(), packet.isMoving(), packet.getMovingDir());
  }
  
  private void handleEnter(Packet03BulletEnter packet) {
    Bullet bullet = new Bullet(game.level,packet.getUsername(),packet.getX(),packet.getY(),packet.getSpeed(),packet.getMovingDir(),packet.getId(),packet.getDamage(),packet.getXTile(),packet.getYTile());
    game.level.addedEntities.add(bullet);
    if (packet.getUsername().equalsIgnoreCase("Turret3")) {
      ((Turret)game.level.tilesOver[10 + 9 * game.level.width]).lastBulletTime = System.currentTimeMillis();
    } else if (packet.getUsername().equalsIgnoreCase("Turret0")) {
      ((Turret)game.level.tilesOver[50 + 53 * game.level.width]).lastBulletTime = System.currentTimeMillis();
    } else if (packet.getUsername().equalsIgnoreCase("Turret2")) {
      ((Turret)game.level.tilesOver[42 + 13 * game.level.width]).lastBulletTime = System.currentTimeMillis();
    }
  }
  
  private void handleLeave(Packet04BulletLeave packet) {
    game.level.removeBullet(packet.getUsername(),packet.getId());
  }
  
  private void handleBulletMove(Packet05BulletMove packet) {
    game.level.moveBullet(packet.getUsername(),packet.getX(),packet.getY(),packet.getNumSteps(),packet.isMoving(),packet.getMovingDir(),packet.getId());
  }
  
  private void handleCrate(Packet06Crate packet) {
    game.level.breakCrate(packet.getId());
  }
  
  private void handlePotion(Packet07Potion packet) {
    game.level.setPotion(packet.getX(),packet.getY(),packet.getId());
  }
  
  private void handleHealth(Packet10HealthPotion packet) {
    game.level.setPlayerHealth(packet.getUsername());
    game.level.setPlayerHealed(packet.getUsername(), true);
  }
  
  private void handleRageStart(Packet11RageStart packet) {
    game.level.setPlayerRage(packet.getUsername(), true);
  }
  
  private void handleRageEnd(Packet12RageEnd packet) {
    game.level.setPlayerRage(packet.getUsername(), false);
  }
  
  private void handleSpeedStart(Packet13SpeedStart packet) {
    game.level.setPlayerFast(packet.getUsername(), true);
  }
  
  private void handleSpeedEnd(Packet14SpeedEnd packet) {
    game.level.setPlayerFast(packet.getUsername(), false);
  }
  
  private void handleHealStop(Packet15HealStop packet) {
    game.level.setPlayerHealed(packet.getUsername(), false);
  }
  
  private void handleExpStart(Packet16ExpStart packet) {
    game.level.setPlayerExped(packet.getUsername(), true);
  }
  
  private void handleExpStop(Packet17ExpStop packet) {
    game.level.setPlayerExped(packet.getUsername(), false);
  }
  
  private void handleChangeClass(Packet20ChangeClass packet) {
    game.level.changePlayerClass(packet.getUsername(),packet.getClassType(),packet.getColour(),packet.getXTile(),packet.getYTile());
  }
}