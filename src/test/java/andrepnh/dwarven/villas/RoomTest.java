package andrepnh.dwarven.villas;

import static andrepnh.dwarven.villas.Feature.floor;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class RoomTest {
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
    String roomDrawing = "--   " + System.lineSeparator() + "  ---";
    ThrowingCallable roomSupplier = () -> new Room(
        floor(0, 0), floor(0, 1),
        floor(1, 2), floor(1, 3), floor(1, 4));
    assertThatThrownBy(roomSupplier)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(roomDrawing);
  }
}