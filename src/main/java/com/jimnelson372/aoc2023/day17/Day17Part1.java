package com.jimnelson372.aoc2023.day17;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class Day17Part1 {

    enum Direction {
        UP, DOWN, LEFT, RIGHT;

        Direction directionRight() {
            return switch(this) {
                case UP -> RIGHT;
                case RIGHT -> DOWN;
                case DOWN -> LEFT;
                case LEFT -> UP;
            };
        }
        Direction directionLeft() {
            return switch(this) {
                case UP -> LEFT;
                case RIGHT -> UP;
                case DOWN -> RIGHT;
                case LEFT -> DOWN;
            };
        }

        int numericDirection() {
            return switch(this) {
                case UP -> 0;
                case RIGHT -> 1;
                case DOWN -> 2;
                case LEFT -> 3;
            };
        }
    }
    record Position(Direction direction, int x, int y, int step) {
        
        static Map<Position,Position> cache = new HashMap<>();
        
        static Position makePosition(Direction direction, int x, int y, int step) {
            var tmp =  new Position(direction, x, y, step);
            return cache.computeIfAbsent(tmp,K -> K);
        }

        Optional<Position> forward() {
            if (isEndPosition()) return Optional.empty();
            if (!forwardValid()) return Optional.empty();
            final int incStep = this.step + 1;
            return Optional.of(switch(this.direction) {
                case UP -> Position.makePosition(this.direction,this.x, this.y-1, incStep);
                case RIGHT -> Position.makePosition(this.direction, this.x+1, this.y, incStep);
                case DOWN -> Position.makePosition(this.direction, this.x, this.y+1, incStep);
                case LEFT -> Position.makePosition(this.direction,this.x-1, this.y,incStep);
            });
        }

        private boolean isEndPosition() {
            return this.x == widthOfSpace - 1 && this.y == heightOfSpace - 1;
        }

        private boolean forwardValid() {
            if (this.step == MAX_STEPS_IN_SAME_DIRECTION) return false;
            return switch(this.direction) {
                case UP -> this.y-1 >= 0;
                case RIGHT -> this.x+1 < widthOfSpace;
                case DOWN -> this.y+1 < heightOfSpace;
                case LEFT -> this.x-1 >=0;
            };
        }

        Optional<Position> right() {
            if (isEndPosition()) return Optional.empty();
            var rightPosition = Position.makePosition(this.direction.directionRight(), this.x, this.y, 0);
            return rightPosition.forward();
        }
        Optional<Position> left() {
            if (isEndPosition()) return Optional.empty();
            var leftPosition = Position.makePosition(this.direction.directionLeft(), this.x, this.y, 0);
            return leftPosition.forward();
        }

        int edgeWeight() {
            return cityBlocks[this.y][this.x] - '0';
        }

        Stream<Position> adjacentPositionsStream() {
            return Stream.of(this.right()
                            ,this.forward()
                            ,this.left())
                    .flatMap(Optional::stream);
        }

        long currentKnownDistance() {
            return distances[this.y][this.x][this.direction.numericDirection()][step-1];
        }

        void setDistance(long dist) {
            distances[this.y][this.x][this.direction.numericDirection()][step-1] = dist;
        }
    }


    static final int MAX_STEPS_IN_SAME_DIRECTION = 3;
    static int heightOfSpace = 0;
    static int widthOfSpace = 0;
    static char[][] cityBlocks = new char[0][0];
    static char[][] trackingMap = new char[0][0];
    static Long[][][][] distances = new Long[0][0][0][0];
//    static PriorityQueue<Position> unvisited = new PriorityQueue<>(10,
//            (a,b) -> Long.compare(a.currentKnownDistance(),b.currentKnownDistance()));
    static Set<Position> unsettled = new HashSet<>();


    static Set<Position> seen = new HashSet<>();
    public static void printMaps() {

        System.out.println("--Blocks Map---");
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                System.out.print(cityBlocks[y][x]);
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("--Tracking Map---");
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                System.out.print(trackingMap[y][x]);
            }
            System.out.println();
        }
    }

    private static void initializeMap(BufferedReader br) {
        var initialCityHeatLossMap = br.lines().toList();
        heightOfSpace = initialCityHeatLossMap.size();
        widthOfSpace = initialCityHeatLossMap.get(0).length();

        cityBlocks = new char[heightOfSpace][widthOfSpace];
        trackingMap = new char[heightOfSpace][widthOfSpace];
        distances = new Long[heightOfSpace][widthOfSpace][4][3];
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                cityBlocks[y][x] = initialCityHeatLossMap.get(y).charAt(x);
            }
        }
    }

    private static void initializeState() {
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                trackingMap[y][x] = '=';
            }
        }
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                for (int d=0; d < 4; d++) {
                  for(int st=0; st < 3; st++){
                    distances[y][x][d][st] = Long.MAX_VALUE;
                  }
                }
            }
        }
        seen = new HashSet<>();
    }

    static boolean hasUnsettled() {
        return !unsettled.isEmpty();
    }

    static Position getLowestUnsettled() {
        return unsettled.stream()
                .min((a,b) -> Long.compare(a.currentKnownDistance(),b.currentKnownDistance()))
                .get();
    }
    private static long findLeastEnergyLossPath() {
//        if (source.isEndPosition()) {
//            return 0L; // We're DONE.
//        }

        // Needed to provide 2 starting directions on positoin 0,0
        // so it could properly move forward RIGHT and DOWN.
        var source1 = Position.makePosition(Direction.UP, 0,0, 1);
        var source2 = Position.makePosition(Direction.LEFT, 0,0, 1);
        source1.setDistance(0);
        source2.setDistance(0);
        unsettled.add(source1);
        unsettled.add(source2);

        while (hasUnsettled()) {
            var node = getLowestUnsettled();
            unsettled.remove(node);
            long prevNodeDist = node.currentKnownDistance();
//            if (node.isEndPosition())
//                System.out.println("An EndPosition Result = " + prevNodeDist);
            seen.add(node);
            node.adjacentPositionsStream()
                    .forEach(p -> {
                         if (!seen.contains(p)) {
                             // for now we'll not track the shorteest path.
                             int edgeWeight = p.edgeWeight();
                             long potentialNewDistance = prevNodeDist + edgeWeight;
                             if (potentialNewDistance < p.currentKnownDistance())
                                 p.setDistance(potentialNewDistance);
                             unsettled.add(p);
                         }
                    });
        }

        return 0L;
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day17-puzzle-input.txt"))) {

            initializeMap(br);
            initializeState();
            var solution = findLeastEnergyLossPath();
            //printMaps();

            //System.out.println((seen.size()));
            long min = Long.MAX_VALUE;
            for(int d=0;d<4;d++) {
                for (int st = 0; st < 3; st++) {
                    min = Math.min(min,distances[heightOfSpace-1][widthOfSpace-1][d][st]);
                }
            }

            System.out.println("least energy lost: " + min);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }
}