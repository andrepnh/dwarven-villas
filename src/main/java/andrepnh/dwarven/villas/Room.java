package andrepnh.dwarven.villas;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.ArrayList;
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

public record Room(ImmutableList<Feature> features) {

  public Room {
    Objects.requireNonNull(features);
    ImmutableList<Feature> roomFloor = features.stream()
        .filter(feature -> feature.tile() == Tile.FLOOR)
        .collect(toImmutableList());
    checkArgument(roomFloor.size() >= 3,
        "A room cannot have less than 3 floor tiles; got: %s",
        roomFloor);
    checkContinuousFloor(roomFloor);
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
        .collect(Collectors.joining(System.lineSeparator()));
  }

  private void checkContinuousFloor(ImmutableList<Feature> roomFeatures) {
    Feature firstFloor = roomFeatures.stream()
        .filter(feature -> feature.tile() == Tile.FLOOR)
        .findFirst()
        .orElseThrow();
    Set<Feature> visited = walkOrthogonally(firstFloor, roomFeatures);
    checkArgument(visited.size() == roomFeatures.size(),
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
      List<Feature> unvisited = getOrthogonallyAdjacentAndWalkable(curr, tiles)
          .stream()
          .filter(Predicate.not(visited::contains))
          .collect(Collectors.toList());
      pending.addAll(unvisited);
    }
    return visited;
  }

  private List<Feature> getOrthogonallyAdjacentAndWalkable(Feature curr, Feature[][] features) {
    var adjacent = new ArrayList<Feature>(4);

    Range<Integer> iBounds = Range.closedOpen(0, features.length);
    if (iBounds.contains(curr.i() - 1) && features[curr.i() - 1][curr.j()].tile().isWalkable()) {
      adjacent.add(features[curr.i() - 1][curr.j()]);
    }
    if (iBounds.contains(curr.i() + 1) && features[curr.i() + 1][curr.j()].tile().isWalkable()) {
      adjacent.add(features[curr.i() + 1][curr.j()]);
    }

    Range<Integer> jBounds = Range.closedOpen(0, features[curr.i()].length);
    if (jBounds.contains(curr.j() - 1) && features[curr.i()][curr.j() - 1].tile().isWalkable()) {
      adjacent.add(features[curr.i()][curr.j() - 1]);
    }
    if (jBounds.contains(curr.j() + 1) && features[curr.i()][curr.j() + 1].tile().isWalkable()) {
      adjacent.add(features[curr.i()][curr.j() + 1]);
    }

    return adjacent;
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
