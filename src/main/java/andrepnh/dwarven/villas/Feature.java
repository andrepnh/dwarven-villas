package andrepnh.dwarven.villas;

import java.util.Objects;

public record Feature(Tile tile, int i, int j) {
  public Feature {
    Objects.requireNonNull(tile);
  }

  public static Feature floor(int i, int j) {
    return new Feature(Tile.FLOOR, i, j);
  }
}
