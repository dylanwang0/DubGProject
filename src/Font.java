class Font {
  
  private static String chars =  "ABCDEFGHIJKLMNOPQRSTUVWXYZ      " + "0123456789.,:;'\"!?$%()-=+/      ";
    
  static void render(String msg, Screen screen, int x, int y, int colour, int scale) {
    msg = msg.toUpperCase();
    
    for (int i = 0; i < msg.length(); i++) {
      int charIndex = chars.indexOf(msg.charAt(i));
      if (charIndex >= 0) {
        screen.render(x + (i*scale*8), y, charIndex + 30 * 32,colour,0,scale);
      }
    }
  }
}