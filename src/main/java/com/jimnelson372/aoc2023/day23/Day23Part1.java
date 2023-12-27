package com.jimnelson372.aoc2023.day23;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Day23Part1 {

    static int heightOfSpace = 0;
    static int widthOfSpace = 0;
    static char[][] hikingTrailPosition = new char[0][0];
    static Long[][] distances = new Long[0][0];
    static Position[][] prev = new Position[0][0];
    static Set<Position> unsettled = new HashSet<>();
    static Set<Position> seen = new HashSet<>();
    static Map<Position, Set<Position>> posFrom = new HashMap<>();


    record Position(
            int x,
            int y) {

        int edgeWeight() {
            return 1;
        }

        void addAPrev(Position p) {
            var set = posFrom.getOrDefault(this, new HashSet<>());
            set.add(p);
            posFrom.put(this, set);
        }

        Stream<Position> adjacentPositionsStream() {
            return Stream.of(this.right()
                            , this.up()
                            , this.left()
                            , this.down())
                    .flatMap(Optional::stream);
        }

        Optional<Position> right() {
            if (x + 1 == widthOfSpace) return Optional.empty();
            if ("#<".indexOf(hikingTrailPosition[y][x + 1]) >= 0) return Optional.empty();
            Position newPosition = new Position(x + 1, y);
            if (this.hasPrevOf(newPosition)) {
                return Optional.empty();
            }
//            if (seen.contains(newPosition)) {
//                return Optional.empty();
//            }
            return Optional.of(newPosition);
        }

        Optional<Position> up() {
            if (y - 1 < 0) return Optional.empty();
            if ("#v".indexOf(hikingTrailPosition[y - 1][x]) >= 0) return Optional.empty();
            Position newPosition = new Position(x, y - 1);
            if (this.hasPrevOf(newPosition)) {
                return Optional.empty();
            }

//            if (seen.contains(newPosition)) {
//                return Optional.empty();
//            }
            return Optional.of(newPosition);
        }

        Optional<Position> left() {
            if (x - 1 < 0) return Optional.empty();
            if ("#>".indexOf(hikingTrailPosition[y][x - 1]) >= 0) return Optional.empty();
            Position newPosition = new Position(x - 1, y);
            if (this.hasPrevOf(newPosition)) {
                return Optional.empty();
            }
//            if (seen.contains(newPosition)) {
//                return Optional.empty();
//            }
            return Optional.of(newPosition);
        }

        Optional<Position> down() {
            if (y + 1 == heightOfSpace) return Optional.empty();
            if ("#^".indexOf(hikingTrailPosition[y + 1][x]) >= 0) return Optional.empty();
            Position newPosition = new Position(x, y + 1);
            if (this.hasPrevOf(newPosition)) {
                return Optional.empty();
            }
//            if (seen.contains(newPosition)) {
//                return Optional.empty();
//            }
            return Optional.of(newPosition);
        }

        boolean hasPrevOf(Position p) {
            var set = posFrom.getOrDefault(this, new HashSet<>());
            return set.contains(p);

        }

        long currentKnownDistance() {

            return distances[this.y][this.x];
        }

        void setDistance(long dist) {

            distances[this.y][this.x] = dist;
        }

        public Position getPrev() {
            return prev[this.y][this.x];
        }

        public void setPrev(Position node) {
            prev[this.y][this.x] = node;
        }
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day23-puzzle-input.txt"))) {

            initializeMap(br);
            initializeState();

//            System.out.println("width;height " + List.of(widthOfSpace, heightOfSpace));

            var solution = findLongestPath();

            System.out.println("least energy lost: " + solution);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static void initializeMap(BufferedReader br) {
        var initialCityHeatLossMap = br.lines()
                .toList();
        heightOfSpace = initialCityHeatLossMap.size();
        widthOfSpace = initialCityHeatLossMap.get(0)
                .length();

        hikingTrailPosition = new char[heightOfSpace][widthOfSpace];
        distances = new Long[heightOfSpace][widthOfSpace];
        prev = new Position[heightOfSpace][widthOfSpace];
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                hikingTrailPosition[y][x] = initialCityHeatLossMap.get(y)
                        .charAt(x);
            }
        }
    }

    private static void initializeState() {
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                distances[y][x] = Long.MAX_VALUE;
                prev[y][x] = null;
            }
        }
        seen = new HashSet<>();
    }

    private static long findLongestPath() {
        // Needed to provide 2 starting directions on positoin 0,0
        // so it could properly move forward RIGHT and DOWN.
        var source1 = new Position(1, 0);
        source1.setDistance(0);

        unsettled.add(source1);

        while (hasUnsettled()) {
            var node = getHighestUnsettled();
            unsettled.remove(node);
            long prevNodeDist = node.currentKnownDistance();
            seen.add(node);
            var stream = node.adjacentPositionsStream()
                    .toList();
            stream.forEach(p -> {
                p.addAPrev(node);
                int edgeWeight = -p.edgeWeight();
                long potentialNewDistance = prevNodeDist + edgeWeight;
                if (potentialNewDistance < p.currentKnownDistance()) {
                    p.setDistance(potentialNewDistance);
                    p.setPrev(node);
                }
                unsettled.add(p);
            });
        }

        var dest1 = new Position(widthOfSpace - 2, heightOfSpace - 1);
        int cnt = 0;
        while (dest1.getPrev() != null) {
            hikingTrailPosition[dest1.y][dest1.x] = 'O';
            cnt++;
            dest1 = dest1.getPrev();
        }
//        System.out.println("------- " + cnt);
//        printMap();

        return -distances[heightOfSpace - 1][widthOfSpace - 2];
    }

    static boolean hasUnsettled() {
        return !unsettled.isEmpty();
    }

    static Position getHighestUnsettled() {
        return unsettled.stream()
                .min((a, b) -> Long.compare(a.currentKnownDistance(), b.currentKnownDistance()))
                //.get();
                .orElseGet(() -> {
                    System.out.println("{{{{{{{{{}}}}}}}}}}");
                    return new Position(-1, -1);
                });
    }

    static void printMap() {
        for (char[] rows : hikingTrailPosition) {
            for (char elem : rows) {
                System.out.print(elem);
            }
            System.out.println();
        }
    }
}