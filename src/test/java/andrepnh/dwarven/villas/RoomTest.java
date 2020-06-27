package andrepnh.dwarven.villas;

import static andrepnh.dwarven.villas.Feature.door;
import static andrepnh.dwarven.villas.Feature.floor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class RoomTest {
  @Test
  void roomsSplitByDoorsAreNotValid() {
    var roomDrawing = "---D---";
    var features = Arrays.asList(
        floor(0, 0), floor(0, 1), floor(0, 2),
        door(0, 3),
        floor(0, 4), floor(0, 5), floor(0, 6));
    assertThatThrownBy(() -> new Room(features))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(roomDrawing);
  }

  @Test
  void bifurcatedRoomsEndingAtADoorAreValid() {
    var roomDrawing = """
    D--
    - -
    ---""";
    var features = Arrays.asList(
        door(0, 0), floor(0, 1), floor(0, 2),
        floor(1, 0), floor(1, 2),
        floor(2, 0), floor(2, 1), floor(2, 2));
    assertThat(new Room(features).toString())
        .isEqualTo(roomDrawing);
  }

  @Test
  void roomsWithDoorsNotAtTheEdgeAreNotValid() {
    var roomDrawing = """
    ---
    -D-
    - -""";
    var features = Arrays.asList(
        floor(0, 0), floor(0, 1), floor(0, 2),
        floor(1, 0), door(1, 1), floor(1, 2),
        floor(2, 0), floor(2, 2));
    assertThatThrownBy(() -> new Room(features))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(roomDrawing);
  }

  // @Test // Might make more sense when rooms are mixed to grids
  void doorsNeedOneOrthogonallyAdjacentWall() {
    var roomDrawing = String.format("---%n-D-%n---");
    var features = Arrays.asList(
        floor(0, 0), floor(0, 1), floor(0, 2),
        floor(1, 0), door(1, 1), floor(1, 2),
        floor(2, 0), floor(2, 1), floor(2, 2));
    assertThatThrownBy(() -> new Room(features))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(roomDrawing);
  }

  @Test
  void roomDoorsAreOptional() {
    new Room(floor(0, 0), floor(0, 1), floor(0, 2));
  }

  @Test
  void roomsWithDoorsNeedToHaveThemAdjacentToFloor() {
    // Each D marks an invalid door position (blanks are walls)
    // DDDDDDD
    // D     D
    // D --- D
    // D     D
    // DDDDDDD
    IntStream.range(0, 6)
        .forEach(i -> {
          var topOrBottomLine = i == 0 || i == 5;
          var jValues = topOrBottomLine ? IntStream.range(0, 7) : IntStream.of(0, 6);
          jValues.forEach(j -> {
            var roomFeatures = Arrays.asList(door(i, j), floor(2, 2), floor(2, 3), floor(2, 4));
            assertThatThrownBy(() -> new Room(roomFeatures))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("---")
                .hasMessageContaining("D");
          });
        });
  }

  @Test
  void roomsMustHaveAtLeast3FloorTiles() {
    var invalidRoomDrawing = "D--D";
    ThrowingCallable invalidRoomSupplier = () -> new Room(
        ImmutableList.of(door(0, 0), floor(0, 1), floor(0, 2), door(0, 3)));
    assertThatThrownBy(invalidRoomSupplier)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(invalidRoomDrawing);
  }

  @Test
  void roomsWithADoorAdjacentInAnyDirectionAreValid() {
    // D marks door locations to test:
    // DDD
    // D---
    // DDD
    var entranceFloor = floor(1, 1);
    for (int iOffset = -1; iOffset < 2; iOffset++) {
      for (int jOffset = -1; jOffset < 2; jOffset++) {
        var door = door(entranceFloor.i() - iOffset, entranceFloor.j() - jOffset);
        var roomFeatures = Arrays.asList(door, entranceFloor, floor(1, 2), floor(1, 3));
        var occupiesAnotherFloor = door.i() == 1 && door.j() >= 1;
        if (!occupiesAnotherFloor) {
          new Room(roomFeatures);
        }
      }
    }
  }

  @Test
  void roomsWithOneWidthAreValid() {
    new Room(
        floor(0, 0),
        floor(1, 0),
        floor(2, 0));
  }

  @Test
  void roomsWithOneHeightAreValid() {
    new Room(
        floor(0, 0),
        floor(0, 1),
        floor(0, 2));
  }

  @Test
  void bigSquareRoomsAreValid() {
    new Room(
        floor(0, 0), floor(0, 1), floor(0, 2), floor(0, 3), floor(0, 4),
        floor(1, 0), floor(1, 1), floor(1, 2), floor(1, 3), floor(1, 4),
        floor(2, 0), floor(2, 1), floor(2, 2), floor(2, 3), floor(2, 4),
        floor(3, 0), floor(3, 1), floor(3, 2), floor(3, 3), floor(3, 4),
        floor(4, 0), floor(4, 1), floor(4, 2), floor(4, 3), floor(4, 4));
  }

  @Test
  void uShapedRoomsAreValid() {
    // - -
    // - -
    // ---
    // Arguments arrange to follow the U
    new Room(
        floor(0, 0), floor(1, 0), floor(2, 0),
        floor(2, 1),
        floor(2, 2), floor(1, 2), floor(0, 2));
  }

  @Test
  void continuousXShapedRoomsAreValid() {
    // ---     ---
    //   --- ---
    //     ---
    //   --- ---
    // ---     ---
    // Arguments arranged to draw line by line
    new Room(
        floor(0, 0), floor(0, 1), floor(0, 2), /*        */ floor(0, 8), floor(0, 9), floor(0, 10),
        floor(1, 2), floor(1, 3), floor(1, 4), /*        */ floor(1, 6), floor(1, 7), floor(1, 8),
                                  floor(2, 4), floor(2, 5), floor(2, 6),
        floor(3, 2), floor(3, 3), floor(3, 4), /*        */ floor(3, 6), floor(3, 7), floor(3, 8),
        floor(4, 0), floor(4, 1), floor(4, 2), /*        */ floor(4, 8), floor(4, 9), floor(4, 10));
  }

  @Test
  void squareRoomsWithAContinuousDanglingFloorOnTheTopAreValid() {
    //   -
    // ---
    new Room(
        floor(0, 2),
        floor(1, 0), floor(1, 1), floor(1, 2));
  }

  @Test
  void squareRoomsWithAContinuousDanglingFloorOnTheBottomAreValid() {
    // ---
    //   -
    new Room(
        floor(0, 0), floor(0, 1), floor(0, 2),
        floor(1, 2));
  }

  @Test
  void squareRoomsWithAContinuousDanglingFloorToTheLeftAreValid() {
    //  ---
    // ----
    new Room(
        floor(0, 1), floor(0, 2), floor(0, 3),
        floor(1, 0), floor(1, 1), floor(1, 2), floor(1, 3));
  }

  @Test
  void squareRoomsWithAContinuousDanglingFloorToTheRightAreValid() {
    // ---
    // ----
    new Room(
        floor(0, 0), floor(0, 1), floor(0, 2),
        floor(1, 0), floor(1, 1), floor(1, 2), floor(1, 3));
  }

  @Test
  void nonContinuousRoomsAreNotValid() {
    String roomDrawing = "- --";
    assertThatThrownBy(() -> new Room(floor(0, 0), floor(0, 2), floor(0, 3)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(roomDrawing);
  }

  @Test
  void roomsWithDiagonallyAdjacentRoomsAreNotValid() {
    var roomDrawing = "--   \n"
        + "  ---";
    ThrowingCallable roomSupplier = () -> new Room(
        floor(0, 0), floor(0, 1),
        floor(1, 2), floor(1, 3), floor(1, 4));
    assertThatThrownBy(roomSupplier)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(roomDrawing);
  }
}