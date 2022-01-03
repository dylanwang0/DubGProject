abstract class Packet {
  
  public static enum PacketTypes {
    INVALID(-1), LOGIN(00), DISCONNECT(01), MOVE(02), ENTER(03), LEAVE(04), 
      BULLET(05), CRATE(06), POTION(07), HEALTH(10), RAGESTART(11), RAGEEND(12), 
      SPEEDSTART(13), SPEEDEND(14), HEALSTOP(15), EXPSTART(16), EXPEND(17), CLASS(20);
    
    private int packetId;
    
    private PacketTypes(int packetId) {
      this.packetId = packetId;
    }
    
    public int getId() {
      return this.packetId;
    }
  }
  
  public byte packetId;
  
  public Packet(int packetId) {
    this.packetId = (byte)packetId;
  }
  
  public abstract void writeData(GameClient client);
  
  public abstract void writeData(GameServer server);
  
  public String readData(byte[] data) {
    String message = new String(data).trim();
    return message.substring(2);
  }
  
  public abstract byte[] getData();
  
  public static PacketTypes lookupPacket(String packetId) {
    try {
      return lookupPacket(Integer.parseInt(packetId));
    } catch (NumberFormatException e) {
      return PacketTypes.INVALID;
    }
  }
  
  public static PacketTypes lookupPacket(int id) {
    for (PacketTypes p : PacketTypes.values()) {
      if (p.getId() == id) {
        return p;
      }
    }
    return PacketTypes.INVALID;
  }
}