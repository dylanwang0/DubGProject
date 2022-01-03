abstract class Entity {
  
  int x, y;
  protected Level level;
  
  Entity(Level level) {
    init(level);
  }
  
  public final void init(Level level) {
    this.level = level;
  }
  
  public abstract void tick();
  
  public abstract void render(Screen screen);
}