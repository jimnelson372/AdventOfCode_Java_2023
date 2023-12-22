package com.jimnelson372.aoc2023.day22;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day22Part1 {

    record Position(
            long x,
            long y,
            long z) implements Comparable<Position> {

        @Override
        public int compareTo(Position o) {
            return Comparator.comparing(Position::z)
                    .thenComparing(Position::x)
                    .thenComparing(Position::y)
                    .compare(this, o);
        }

        Position dropDistance(long distZ) {
            return new Position(x, y, z - distZ);
        }
    }

    record Range(
            long low,
            long high) {
        boolean overlaps(Range other) {
            boolean over = (other.low >= low && other.low <= high)
                    || (other.high >= low && other.high <= high)
                    || (this.low >= other.low && this.low <= other.high)
                    || (this.high >= other.low && this.high <= other.high);
            return over;
        }
    }

    record Relation(
            long supported,
            long supporter) {
    }

    static class Brick implements Comparable<Brick> {

        private static long idGen = 0;
        final public long id;
        final public Position lower;
        final public Position higher;
        final private Range lowXRange;
        final private Range highXRange;
        final private Range lowYRange;
        final private Range highYRange;

        public Brick(Position lower, Position higher) {
            this.id = ++idGen;
            this.lower = lower;
            this.higher = higher;
            if (lower.z == higher.z) {
                this.lowXRange = new Range(Math.min(lower.x, higher.x), Math.max(lower.x, higher.x));
                this.lowYRange = new Range(Math.min(lower.y, higher.y), Math.max(lower.y, higher.y));
                this.highXRange = this.lowXRange;
                this.highYRange = this.lowYRange;
            } else {
                this.lowXRange = new Range(lower.x, lower.x);
                this.lowYRange = new Range(lower.y, lower.y);
                this.highXRange = new Range(higher.x, higher.x);
                this.highYRange = new Range(higher.y, higher.y);
            }
        }

        private Brick(long id,
                      Range lowXRange,
                      Range highXRange,
                      Range lowYRange,
                      Range highYRange,
                      Position lower,
                      Position higher) {
            this.id = id;
            this.lowXRange = lowXRange;
            this.highXRange = highXRange;
            this.lowYRange = lowYRange;
            this.highYRange = highYRange;
            this.lower = lower;
            this.higher = higher;
        }

        @Override
        public int compareTo(Brick o) {
            return Comparator.comparing(Brick::getLower)
                    .thenComparing(Brick::getHigher)
                    .compare(this, o);
        }

        public Position getLower() {
            return lower;
        }

        public Position getHigher() {
            return higher;
        }

        @Override
        public String toString() {
            return "Brick{" +
                    "id=" + id +
                    ", lower=" + lower +
                    ", higher=" + higher +
                    '}';
        }

        public boolean isUnderneath(Brick other) {
            return other.lowYRange.overlaps(this.highYRange)
                    && other.lowXRange.overlaps(this.highXRange);

        }

        public Brick dropDistance(long distZ) {
            return new Brick(this.id,
                             this.lowXRange,
                             this.highXRange,
                             this.lowYRange,
                             this.highYRange,
                             this.lower.dropDistance(distZ),
                             this.higher.dropDistance(distZ));
        }
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day22-puzzle-input.txt"))) {
            var bricks = getBricks(br);

            List<Relation> relation = new ArrayList<>();

            HashSet<Brick> dropped = new HashSet<>();
            var bricksList = IntStream.range(0, bricks.size())
                    .sequential()
                    .boxed()
                    .map(i -> {
                        var brick = bricks.get(i);
                        if (brick.lower.z == 1) {
                            dropped.add(brick);
                            return brick;
                        } else {
                            AtomicReference<Brick> droppedBrick = new AtomicReference<>(brick);
                            dropped.stream()
                                    .filter(b -> b.higher.z < brick.lower.z)
                                    .filter(b2 -> b2.isUnderneath(brick))
                                    .collect(Collectors.groupingBy(b -> b.getHigher().z))
                                    .entrySet()
                                    .stream()
                                    .sorted(Map.Entry.<Long, List<Brick>>comparingByKey()
                                                    .reversed())
                                    .map(Map.Entry::getValue)
                                    .findFirst()
                                    .ifPresentOrElse(
                                            ol -> {
                                                var newZ = ol.get(0).higher.z + 1;
                                                var newBrick = brick.dropDistance(brick.lower.z - newZ);
                                                dropped.add(newBrick);
                                                droppedBrick.set(newBrick);
                                                ol.forEach(supporter -> relation.add(new Relation(brick.id,
                                                                                                  supporter.id)));
                                            }, () -> {
                                                var newBrick = brick.dropDistance(brick.lower.z - 1);
                                                dropped.add(newBrick);
                                                droppedBrick.set(newBrick);
                                            });
                            return droppedBrick.get();
                        }
                    })
                    .toList();

            var supporters = relation.stream()
                    .collect(Collectors.groupingBy(Relation::supporter));
            var supported = relation.stream()
                    .collect(Collectors.groupingBy(Relation::supported, Collectors.counting()));

            long count = bricks.size() - supporters.size();  // the count of those that support nothing.
            System.out.println("There is " + count + " that doesn't support any other bricks");

            count += supporters.values()
                    .stream()
                    .filter(v -> v.stream()
                            .allMatch(r -> supported.get(r.supported) > 1L))
                    .count();

            System.out.println("Total that can be removed safely = " + count);


        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static List<Brick> getBricks(BufferedReader br) {
        return br.lines()
                .map(l -> l.split("~"))
                .map(l2 -> {
                    var positions = Arrays.stream(l2)
                            .map(rawPos -> {
                                var pos = Arrays.stream(rawPos.split(","))
                                        .map(Long::valueOf)
                                        .toList();
                                return new Position(pos.get(0), pos.get(1), pos.get(2));
                            })
                            .sorted()
                            .toList();
                    return new Brick(positions.get(0), positions.get(1));
                })
                .sorted()
                .toList();
    }

}
