import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class GameServer extends Thread {
  
  private DatagramSocket socket;
  private DubG game;
  private List<PlayerMP> connectedPlayers = new ArrayList<PlayerMP>();
  private Map<String,PlayerMP> connPlayers = new HashMap<String,PlayerMP>();
  
  public GameServer(DubG game) {
    this.game = game;
    try {
      this.socket = new DatagramSocket(1331);
    } catch (SocketException e) {
      e.printStackTrace();
    } 
  }
  
  public void run() {
    while (true) {
      byte[] data = new byte [1024];
      DatagramPacket packet = new DatagramPacket(data,data.length);
      try {
        socket.receive(packet);
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      this.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
      
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
        System.out.println("["+address.getHostAddress() + ":" + port + "]" + ((Packet00Login)packet).getUsername() + " has connected . . .");
        PlayerMP player = new PlayerMP(game.level,((Packet00Login)packet).getX(), ((Packet00Login)packet).getY(), ((Packet00Login)packet).getUsername(), 400, address, port);
        this.addConnection(player,(Packet00Login)packet);
        
        break;
      case DISCONNECT:
        packet = new Packet01Disconnect(data);
        System.out.println("["+address.getHostAddress() + ":" + port + "]" + ((Packet01Disconnect)packet).getUsername() + " has left . . .");
        this.removeConnection((Packet01Disconnect)packet);
        break;
      case MOVE:
        packet = new Packet02Move(data);
        this.handleMove(((Packet02Move)packet));
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
  
  public void addConnection(PlayerMP player, Packet00Login packet) {
    boolean alreadyConnected = false;
    for (PlayerMP p: this.connectedPlayers) {
      if (player.getUsername().equalsIgnoreCase(p.getUsername())) {
        if (p.ipAddress == null) {
          p.ipAddress = player.ipAddress;
        }
        if (p.port == -1) {
          p.port = player.port;
        }
        alreadyConnected = true;
      } else {
        //send to already connected players
        sendData(packet.getData(),p.ipAddress,p.port);
        
        //send to new player
        Packet00Login packet2 = new Packet00Login(p.getUsername(), p.x, p.y);
        sendData(packet2.getData(),player.ipAddress,player.port);
      }
    }
    if (!alreadyConnected) {
      this.connectedPlayers.add(player);
    }
    
    for (String name: connPlayers.keySet()) {
      PlayerMP p = connPlayers.get(name);
      if (!name.equalsIgnoreCase(packet.getUsername())) {
        //sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
    
    if (connPlayers.containsKey(packet.getUsername())) {
      this.connPlayers.put(packet.getUsername(), player);
    }
    
  }
  
  public void removeConnection(Packet01Disconnect packet) {
    int ind = getPlayerMPIndex(packet.getUsername());
    if (ind < this.connectedPlayers.size()) {
      this.connectedPlayers.remove(getPlayerMPIndex(packet.getUsername()));
    }
    if (connPlayers.containsKey(packet.getUsername())) { 
      this.connPlayers.remove(packet.getUsername());
    }
    packet.writeData(this);
  }
  
  public PlayerMP getPlayerMP(String username) {
    for (PlayerMP player: this.connectedPlayers) {
      if (player.getUsername().equalsIgnoreCase(username)) {
        return player;
      }
    }
    return null;
  }
  
  public int getPlayerMPIndex(String username) {
    int index = 0;
    for (PlayerMP player: this.connectedPlayers) {
      if (player.getUsername().equalsIgnoreCase(username)) {
        break;
      }
      index++;
    }
    return index;
  }
  
  public void sendData(byte[] data, InetAddress ipAddress, int port) {
    DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
    try{
      this.socket.send(packet); 
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void sendDataToAllClients(byte[] data) {
    for (PlayerMP p : connectedPlayers) {
      sendData(data, p.ipAddress, p.port);
    }
  }
  
  public void sendDataToSpecificClient(byte[] data, String username) {
    PlayerMP p = connPlayers.get(username);
    sendData(data, p.ipAddress, p.port);
  }
  
  private void handleMove(Packet02Move packet) {
    if (getPlayerMP(packet.getUsername()) != null) {
      int index = getPlayerMPIndex(packet.getUsername());
      PlayerMP player = this.connectedPlayers.get(index);
      player.x = packet.getX();
      player.y = packet.getY();
      player.setMoving(packet.isMoving());
      player.setMovingDir(packet.getMovingDir());
      player.setNumSteps(packet.getNumSteps());
      packet.writeData(this);
    }
  }
  
  private void handleEnter(Packet03BulletEnter packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleLeave(Packet04BulletLeave packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleBulletMove(Packet05BulletMove packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleCrate(Packet06Crate packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
        break;
      }
    }
  }
  
  private void handlePotion(Packet07Potion packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleHealth(Packet10HealthPotion packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleRageStart(Packet11RageStart packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleRageEnd(Packet12RageEnd packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleSpeedStart(Packet13SpeedStart packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleSpeedEnd(Packet14SpeedEnd packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleHealStop(Packet15HealStop packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleExpStart(Packet16ExpStart packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleExpStop(Packet17ExpStop packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
  
  private void handleChangeClass(Packet20ChangeClass packet) {
    for (PlayerMP p: this.connectedPlayers) {
      if (!p.getUsername().equalsIgnoreCase(packet.getUsername())) {
        sendData(packet.getData(),p.ipAddress,p.port);
      }
    }
  }
}