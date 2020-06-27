package andrepnh.dwarven.villas;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Grid {
  private final Bounds bounds;
  private final Tile[][] tiles;

  public Grid(int width, int height) {
    this.bounds = new Bounds(width, height);
    this.tiles = new Tile[bounds.rows()][bounds.columns()];
    Stream.of(tiles).forEach(row -> Arrays.fill(row, Tile.WALL));
  }

  public void place(Tile tile, int i, int j) {
    checkTileReplacement(tile, i, j);
    tiles[i][j] = requireNonNull(tile);
  }

  public void place(Tile tile1, int i1, int j1,
      Tile tile2, int i2, int j2) {
    place(tile1, i1, j1);
    place(tile2, i2, j2);
  }

  public void place(Tile tile1, int i1, int j1,
      Tile tile2, int i2, int j2,
      Tile tile3, int i3, int j3) {
    place(tile1, i1, j1);
    place(tile2, i2, j2);
    place(tile3, i3, j3);
  }

  public Tile get(int i, int j) {
    return tiles[i][j];
  }

  public Bounds bounds() {
    return bounds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Grid grid = (Grid) o;
    return Arrays.deepEquals(tiles, grid.tiles);
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(tiles);
  }

  @Override
  public String toString() {
    return Stream.of(tiles)
        .map(Arrays::toString)
        .collect(Collectors.joining("," + System.lineSeparator(), "[", "]"));
  }

  private void checkTileReplacement(Tile tile, int i, int j) {
    bounds.check(i, j);
    var current = tiles[i][j];
    switch (current) {
      case FLOOR -> checkArgument(tile == Tile.DOOR || tile == Tile.FLOOR,
          "%s at [%s][%s] cannot be replaced with %s",
          current, i, j, tile);
      case DOOR -> checkArgument(tile == Tile.DOOR,
          "%s at [%s][%s] cannot be replaced with %s",
          current, i, j, tile);
      case STAIR -> checkArgument(tile == Tile.STAIR,
          "%s at [%s][%s] cannot be replaced. Got tile %s",
          current, i, j, tile);
      case WALL -> {}
      default -> checkState(false,
          "Unknown tile %s when trying to place %s at [%s][%s]",
          current, tile, i, j);
    }
  }

  public record Bounds(int width, int height) {
    public Bounds {
      checkArgument(width > 0, "Width <= 0: %s", width);
      checkArgument(height > 0, "Height <= 0: %s", height);
    }

    public int rows() {
      return height;
    }

    public int columns() {
      return width;
    }

    public void check(int i, int j) {
      if (i < 0 || i >= rows() || j < 0 || j >= columns()) {
        throw new IndexOutOfBoundsException(String.format(
            "[%d][%d] is invalid for an object with %s", i, j, this));
      }
    }
  }
}
