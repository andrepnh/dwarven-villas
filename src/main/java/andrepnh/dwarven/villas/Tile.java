package andrepnh.dwarven.villas;

public enum Tile {
  WALL(' ', false), FLOOR('-', true), DOOR('D', true), STAIR('x', true);

  private final char repr;

  private final boolean walkable;

  Tile(char repr, boolean walkable) {
    this.repr = repr;
    this.walkable = walkable;
  }

  public char repr() {
    return repr;
  }

  public boolean isWalkable() {
    return walkable;
  }

  @Override
  public String toString() {
    return String.valueOf(repr);
  }
}
