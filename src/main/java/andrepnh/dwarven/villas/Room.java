package andrepnh.dwarven.villas;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Room(ImmutableList<Feature> features) {

  public Room {
    Objects.requireNonNull(features);
    ImmutableList<Feature> roomFloor = features.stream()
        .filter(feature -> feature.tile() == Tile.FLOOR)
        .collect(toImmutableList());
    checkArgument(roomFloor.size() >= 3,
        "A room cannot have less than 3 floor tiles; got:%n%s",
        draw(features));
    checkContinuousFloor(features);
    checkDoors(features);
  }

  public Room(Collection<Feature> features) {
    this(ImmutableList.copyOf(features));
  }

  public Room(Feature first, Feature second, Feature third, Feature... others) {
    this(toList(first, second, third, others));
  }

  private static ImmutableList<Feature> toList(
      Feature first, Feature second, Feature third, Feature[] others) {
    var builder = ImmutableList.<Feature>builderWithExpectedSize(others.length + 3);
    builder.add(first);
    builder.add(second);
    builder.add(third);
    builder.addAll(Arrays.asList(others));
    return builder.build();
  }

  @Override
  public String toString() {
    return draw(features);
  }

  private String draw(Collection<Feature> features) {
    Feature[][] featuresArray = fillBlanksWithWalls(features);
    var stats = features.stream().mapToInt(Feature::i).summaryStatistics();
    int minI = stats.getMin(), maxI = stats.getMax();
    stats = features.stream().mapToInt(Feature::j).summaryStatistics();
    int minJ = stats.getMin(), maxJ = stats.getMax();
    return IntStream.range(0, featuresArray.length)
        .filter(i -> minI <= i && i <= maxI)
        .mapToObj(i -> IntStream.range(0, featuresArray[i].length)
            .filter(j -> minJ <= j && j <= maxJ)
            .mapToObj(j -> featuresArray[i][j])
            .map(feature -> String.valueOf(feature.tile().repr()))
            .collect(Collectors.joining()))
        .collect(Collectors.joining("\n"));
  }

  private void checkDoors(ImmutableList<Feature> features) {
    Feature[][] featuresArray = fillBlanksWithWalls(features);
    List<Feature> invalidDoors = features.stream()
        .filter(f -> f.tile() == Tile.DOOR)
        .filter(door -> !isAdjacentTo(Tile.FLOOR, door, featuresArray)
            || !isAtTheEdge(door, featuresArray))
        .collect(Collectors.toList());
    checkArgument(invalidDoors.isEmpty(),
        "These doors are not adjacent to floors or orthogonally adjacent to walls: %s. Room:%s%s",
        invalidDoors, System.lineSeparator(), draw(features));
  }

  private boolean isAtTheEdge(Feature door, Feature[][] featuresArray) {
    return getOrthogonallyAdjacent(door, featuresArray).size() < 4;
  }

  private boolean isOrthogonallyAdjacentTo(Tile tile, Feature feature, Feature[][] featuresArray) {
    return getOrthogonallyAdjacent(feature, featuresArray)
        .stream()
        .anyMatch(adjacent -> adjacent.tile() == tile);
  }

  private boolean isAdjacentTo(Tile tile, Feature feature, Feature[][] featuresArray) {
    return getAdjacent(feature, featuresArray)
        .stream()
        .anyMatch(adjacent -> adjacent.tile() == tile);
  }

  private void checkContinuousFloor(ImmutableList<Feature> roomFeatures) {
    var floors = roomFeatures.stream()
        .filter(feature -> feature.tile() == Tile.FLOOR)
        .collect(Collectors.toList());
    var firstFloor = floors.get(0);
    Set<Feature> visited = walkOrthogonally(firstFloor, roomFeatures);
    checkArgument(visited.size() == floors.size(),
        "Rooms with non-orthogonally adjacent floors are not allowed:\n%s",
        draw(roomFeatures));
  }

  private Set<Feature> walkOrthogonally(Feature origin, ImmutableList<Feature> features) {
    Feature[][] tiles = fillBlanksWithWalls(features);
    Queue<Feature> pending = new ArrayDeque<>(Collections.singleton(origin));
    Set<Feature> visited = Sets.newHashSetWithExpectedSize(features.size());
    while (!pending.isEmpty()) {
      Feature curr = pending.poll();
      visited.add(curr);
      List<Feature> unvisited = getOrthogonallyAdjacent(curr, tiles)
          .stream()
          .filter(feature -> feature.tile() == Tile.FLOOR)
          .filter(Predicate.not(visited::contains))
          .collect(Collectors.toList());
      pending.addAll(unvisited);
    }
    return visited;
  }

  private Set<Feature> getAdjacent(Feature curr, Feature[][] features) {
    return Sets.union(
        getDiagonallyAdjacent(curr, features),
        getOrthogonallyAdjacent(curr, features));
  }

  private Set<Feature> getDiagonallyAdjacent(Feature curr, Feature[][] features) {
    return getFrom(curr,
        Stream.of(Tuple.of(-1, -1), Tuple.of(-1, 1), Tuple.of(1, -1), Tuple.of(1, 1)),
        features);
  }

  private Set<Feature> getOrthogonallyAdjacent(Feature curr, Feature[][] features) {
    return getFrom(curr,
        Stream.of(Tuple.of(-1, 0), Tuple.of(1, 0), Tuple.of(0, -1), Tuple.of(0, 1)),
        features);
  }

  private Set<Feature> getFrom(Feature curr,
      Stream<Tuple2<Integer, Integer>> offsets, Feature[][] features) {
    Range<Integer> iBounds = Range.closedOpen(0, features.length);
    Range<Integer> jBounds = Range.closedOpen(0, features[curr.i()].length);
    return offsets
        .map(offset -> offset.map((i, j) -> Tuple.of(curr.i() + i, curr.j() + j)))
        .filter(coords -> iBounds.contains(coords._1) && jBounds.contains(coords._2))
        .map(coords -> features[coords._1][coords._2])
        .collect(Collectors.toSet());
  }

  private Feature[][] fillBlanksWithWalls(Collection<Feature> features) {
    int maxI = features.stream().mapToInt(Feature::i).max().orElseThrow(),
        maxJ = features.stream().mapToInt(Feature::j).max().orElseThrow();
    var featuresArray = new Feature[maxI + 1][maxJ + 1];
    fill(featuresArray, (i, j) -> new Feature(Tile.WALL, i, j));
    features.forEach(feature -> featuresArray[feature.i()][feature.j()] = feature);
    return featuresArray;
  }

  private <T> void fill(T[][] array, BiFunction<Integer, Integer, T> supplier) {
    for (int i = 0; i < array.length; i++) {
      T[] line = array[i];
      for (int j = 0; j < line.length; j++) {
        line[j] = supplier.apply(i, j);
      }
    }
  }
}
