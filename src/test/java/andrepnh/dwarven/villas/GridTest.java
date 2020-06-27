package andrepnh.dwarven.villas;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import andrepnh.dwarven.villas.Grid.Bounds;
import com.google.common.collect.Lists;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.control.Try;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class GridTest {
  private Grid walls5x5;

  @BeforeEach
  void init() {
    walls5x5 = new Grid(5, 5);
  }

  @ParameterizedTest
  @EnumSource(Tile.class)
  void placeShouldAllowReplacingWallWithAnyOtherTile(Tile tile) {
    walls5x5.place(tile , 0, 0);
    assertEquals(tile, walls5x5.get(0, 0));
  }

  @Test
  void placeShouldAllowReplacingADoorWithAnother() {
    walls5x5.place(Tile.DOOR, 0, 0);
    walls5x5.place(Tile.DOOR, 0, 0);
    assertEquals(Tile.DOOR, walls5x5.get(0, 0));
  }

  @ParameterizedTest
  @EnumSource(Tile.class)
  void placeShouldAllowReplacingAFloorWithAnythingButAnotherFloorOrADoor(Tile tile) {
    walls5x5.place(Tile.FLOOR, 0, 0);
    switch (tile) {
      case FLOOR, DOOR -> {
        walls5x5.place(tile, 0 ,0);
        assertEquals(tile, walls5x5.get(0, 0));
      }
      default -> assertThrows(IllegalArgumentException.class, () -> walls5x5.place(tile, 0, 0));
    }
  }

  @ParameterizedTest
  @EnumSource(Tile.class)
  void placeShouldNotAllowReplacingStairsWithAnythingButStairs(Tile tile) {
    walls5x5.place(Tile.STAIR, 0, 0);
    if (tile == Tile.STAIR) {
      walls5x5.place(tile, 0, 0);
      assertEquals(Tile.STAIR, walls5x5.get(0, 0));
    } else {
      assertThrows(IllegalArgumentException.class, () -> walls5x5.place(tile, 0, 0));
    }
  }

  @Test
  void placeShouldNotAllowNullTile() {
    assertThrows(NullPointerException.class, () -> walls5x5.place(null, 0, 0));
  }

  @ParameterizedTest
  @CsvSource({ "-1, -1", "6, 6", "0, 5", "5, 0"})
  void placeShouldNotAllowOutOfBoundsIndexes(int i, int j) {
    assertThatThrownBy(() -> walls5x5.place(Tile.WALL, i, j))
        .isInstanceOf(IndexOutOfBoundsException.class)
        .hasMessageContaining(walls5x5.bounds().toString())
        .hasMessageContaining(String.valueOf(i))
        .hasMessageContaining(String.valueOf(j));
  }

  @Test
  void placing2TilesAtOnceShouldBeTheSameAsPlacingThemOneAtATime() {
    var anotherWalls5x5 = new Grid(5, 5);
    Stream<Tuple3<Tile, Integer, Integer>> randomTiles = randomTiles(walls5x5.bounds()).limit(200);
    Stream<List<Tuple3<Tile, Integer, Integer>>> tiles = splitIntoGroupsOf(2, randomTiles);

    tiles.forEach(group -> {
      Tuple3<Tile, Integer, Integer> triple1 = group.get(0);
      Tuple3<Tile, Integer, Integer> triple2 = group.get(1);

      assertSameResults(
          () -> {
            walls5x5.place(triple1._1, triple1._2, triple1._3);
            walls5x5.place(triple2._1, triple2._2, triple2._3);
            return walls5x5;
          },
          () -> {
            anotherWalls5x5.place(
                triple1._1, triple1._2, triple1._3,
                triple2._1, triple2._2, triple2._3);
            return anotherWalls5x5;
          });
    });
  }

  @Test
  void placing3TilesAtOnceShouldBeTheSameAsPlacingThemOneAtATime() {
    var anotherWalls5x5 = new Grid(5, 5);
    Stream<Tuple3<Tile, Integer, Integer>> randomTiles = randomTiles(walls5x5.bounds()).limit(300);
    Stream<List<Tuple3<Tile, Integer, Integer>>> tiles = splitIntoGroupsOf(3, randomTiles);

    tiles.forEach(group -> {
      Tuple3<Tile, Integer, Integer> triple1 = group.get(0);
      Tuple3<Tile, Integer, Integer> triple2 = group.get(1);
      Tuple3<Tile, Integer, Integer> triple3 = group.get(2);

      assertSameResults(
          () -> {
            walls5x5.place(triple1._1, triple1._2, triple1._3);
            walls5x5.place(triple2._1, triple2._2, triple2._3);
            walls5x5.place(triple3._1, triple3._2, triple3._3);
            return walls5x5;
          },
          () -> {
            anotherWalls5x5.place(
                triple1._1, triple1._2, triple1._3,
                triple2._1, triple2._2, triple2._3,
                triple3._1, triple3._2, triple3._3);
            return anotherWalls5x5;
          });
    });
  }

  private <T> void assertSameResults(Supplier<T> firstAction, Supplier<T> secondAction) {
    Function<Throwable, Tuple2<Class<? extends  Throwable>, String>> getClassAndMessage
        = ex -> Tuple.of(ex.getClass(), ex.getMessage());
    var firstResult = Try.ofSupplier(firstAction)
        .toEither()
        .mapLeft(getClassAndMessage);
    var secondResult = Try.ofSupplier(secondAction)
        .toEither()
        .mapLeft(getClassAndMessage);
    assertEquals(firstResult, secondResult);
  }

  private <T> Stream<List<T>> splitIntoGroupsOf(int size, Stream<T> stream) {
    return Lists.partition(stream.collect(Collectors.toList()), size).stream();
  }

  private Stream<Tuple3<Tile, Integer, Integer>> randomTiles(Bounds bounds) {
    Random rng = new Random();
    return Stream.generate(() -> {
      var tile = Tile.values()[rng.nextInt(Tile.values().length)];
      int i = rng.nextInt(bounds.rows());
      int j = rng.nextInt(bounds.columns());
      return Tuple.of(tile, i, j);
    });
  }
}