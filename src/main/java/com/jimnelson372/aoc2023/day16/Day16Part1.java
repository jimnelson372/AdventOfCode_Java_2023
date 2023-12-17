package com.jimnelson372.aoc2023.day16;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.groupingBy;

public class Day16Part1 {

    record MirrorCoord(MirrorType type, long x, long y) {

        public static MirrorCoord none = new MirrorCoord(MirrorType.NONE,0L,0L);
    }
    record LightPath(LightPathType type, long position, long start, long end) {}
    record Position(LightDirection direction, long x, long y) {}

    enum MirrorType {VERT_SPLIT, HORIZ_SPLIT, TOP_LEFT, TOP_RIGHT, NONE}

    enum LightPathType {HORIZONTAL, VERTICAL}

    enum LightDirection {UP, DOWN, LEFT, RIGHT;
        private Map<Long, List<MirrorCoord>> getPotentialMirrors() {
            return switch (this) {
                case LEFT, RIGHT -> hGroupedMirrors;
                case UP, DOWN -> vGroupedMirrors;
            };
        }


    }

    static List<MirrorCoord> mirrorCoords = List.of();
    static int heightOfSpace = 0;
    static int widthOfSpace = 0;
    static Map<Long, List<MirrorCoord>> hGroupedMirrors = Map.of();
    static Map<Long, List<MirrorCoord>> vGroupedMirrors = Map.of();
    static Set<Position> seenPositions = new HashSet<>();
    static List<LightPath> lightPaths = new ArrayList<>();

    private static void clearState() {
        seenPositions = new HashSet<>();
        lightPaths = new ArrayList<>();
    }

    private static List<Position> beamLightFromStep(Position position) {
        if (seenPositions.contains(position))
            return List.of();

        seenPositions.add(position);

        var potentialMirrors = position.direction.getPotentialMirrors();

        if (position.direction == LightDirection.RIGHT) {
            if (position.x == widthOfSpace-1) return List.of();
            var mirror = potentialMirrors.get(position.y).stream()
                    .filter(mc -> mc.x > position.x && mc.type != MirrorType.HORIZ_SPLIT)
                    .findFirst()
                    .orElse(MirrorCoord.none);
            if (mirror.type == MirrorType.NONE) {
                lightPaths.add(new LightPath(LightPathType.HORIZONTAL,position.y,position.x+1,widthOfSpace-1));
                return List.of();
            } else {
                lightPaths.add(new LightPath(LightPathType.HORIZONTAL,position.y,position.x+1,mirror.x));

                return switch(mirror.type) {
                    case VERT_SPLIT -> List.of(new Position(LightDirection.UP, mirror.x, mirror.y),
                                                new Position(LightDirection.DOWN, mirror.x, mirror.y));
                    case TOP_LEFT -> List.of(new Position(LightDirection.DOWN, mirror.x, mirror.y));
                    case TOP_RIGHT ->  List.of(new Position(LightDirection.UP, mirror.x, mirror.y));
                    default -> List.of();
                };
            }
        }

        if (position.direction == LightDirection.LEFT) {
            if (position.x == 0) return List.of();
            var mirror = potentialMirrors.get(position.y).stream()
                    .filter(mc -> mc.x < position.x && mc.type != MirrorType.HORIZ_SPLIT)
                    .reduce((acc, mc) -> mc) // get us the last one.
                    .orElse(MirrorCoord.none);
            if (mirror.type == MirrorType.NONE) {
                lightPaths.add(new LightPath(LightPathType.HORIZONTAL,position.y,position.x-1,0));
                return List.of();
            } else {
                lightPaths.add(new LightPath(LightPathType.HORIZONTAL,position.y,position.x-1,mirror.x));

                return switch(mirror.type) {
                    case VERT_SPLIT -> List.of(new Position(LightDirection.UP, mirror.x, mirror.y),
                            new Position(LightDirection.DOWN, mirror.x, mirror.y));
                    case TOP_LEFT -> List.of(new Position(LightDirection.UP, mirror.x, mirror.y));
                    case TOP_RIGHT ->  List.of(new Position(LightDirection.DOWN, mirror.x, mirror.y));
                    default -> List.of();
                };
            }
        }

        if (position.direction == LightDirection.UP) {
            if(position.y == 0) return List.of();
            var mirror = potentialMirrors.get(position.x).stream()
                    .filter(mc -> mc.y < position.y && mc.type != MirrorType.VERT_SPLIT)
                    .reduce((acc, mc) -> mc) // get us the last one.
                    .orElse(MirrorCoord.none);
            if (mirror.type == MirrorType.NONE) {
                lightPaths.add(new LightPath(LightPathType.VERTICAL,position.x,position.y-1,0));
                return List.of();
            } else {
                lightPaths.add(new LightPath(LightPathType.VERTICAL,position.x,position.y-1,mirror.y));

                return switch(mirror.type) {
                    case HORIZ_SPLIT -> List.of(new Position(LightDirection.LEFT, mirror.x, mirror.y),
                            new Position(LightDirection.RIGHT, mirror.x, mirror.y));
                    case TOP_LEFT -> List.of(new Position(LightDirection.LEFT, mirror.x, mirror.y));
                    case TOP_RIGHT ->  List.of(new Position(LightDirection.RIGHT, mirror.x, mirror.y));
                    default -> List.of();
                };
            }
        }

        if (position.direction == LightDirection.DOWN) {
            if (position.y == heightOfSpace -1) return List.of();
            var mirror = potentialMirrors.get(position.x).stream()
                    .filter(mc -> mc.y > position.y && mc.type != MirrorType.VERT_SPLIT)
                    .findFirst()
                    .orElse(MirrorCoord.none);
            if (mirror.type == MirrorType.NONE) {
                lightPaths.add(new LightPath(LightPathType.VERTICAL,position.x,position.y+1,heightOfSpace-1));
                return List.of();
            } else {
                lightPaths.add(new LightPath(LightPathType.VERTICAL,position.x,position.y+1,mirror.y));

                return switch(mirror.type) {
                    case HORIZ_SPLIT -> List.of(new Position(LightDirection.LEFT, mirror.x, mirror.y),
                            new Position(LightDirection.RIGHT, mirror.x, mirror.y));
                    case TOP_LEFT -> List.of(new Position(LightDirection.RIGHT, mirror.x, mirror.y));
                    case TOP_RIGHT ->  List.of(new Position(LightDirection.LEFT, mirror.x, mirror.y));
                    default -> List.of();
                };
            }
        }


        return  List.of();
    }

    private static void beamLightFrom(Position position) {
        var workingList = new LinkedList<Position>();
        workingList.add(position);
        while (!workingList.isEmpty()) {
            var nextPosition = workingList.removeFirst();
            //System.out.println("Processing position:" + nextPosition);
            List<Position> resultPositionsList = beamLightFromStep(nextPosition);
            //printMap();
            workingList.addAll(resultPositionsList);
        }
    }


    public static void printMap() {
        char[][] arr = new char[heightOfSpace][widthOfSpace];
        for(int y=0; y<heightOfSpace;y++) {
            for (int x=0; x<widthOfSpace; x++) {
                arr[y][x] = '.';
            }
        }
        lightPaths.stream()
                .filter(p -> p.type == LightPathType.HORIZONTAL)
                .forEach(hp -> {
                    long r =  hp.position;
                    var minPos = Math.min(hp.start,hp.end);
                    var maxPos = Math.max(hp.start,hp.end);
                    for(long c = minPos; c <= maxPos; c++) {
                        arr[(int)r][(int)c]='#';
                    }
                });
        lightPaths.stream()
                .filter(p -> p.type == LightPathType.VERTICAL)
                .forEach(hp -> {
                    var c = hp.position;
                    var minPos = Math.min(hp.start,hp.end);
                    var maxPos = Math.max(hp.start,hp.end);
                    for(var r = minPos; r <= maxPos; r++) {
                        arr[(int)r][(int)c]='#';
                    }
                });
        System.out.println("--Map---");
        for(int y=0; y<heightOfSpace;y++) {
            for (int x=0; x<widthOfSpace; x++) {
                System.out.print(arr[y][x]);
            }
            System.out.println();
        }
    }

    public static long countEnergized() {
        char[][] arr = new char[heightOfSpace][widthOfSpace];
        for(int y=0; y<heightOfSpace;y++) {
            for (int x=0; x<widthOfSpace; x++) {
                arr[y][x] = '.';
            }
        }
        AtomicLong cnt = new AtomicLong();
        lightPaths.stream()
                .filter(p -> p.type == LightPathType.HORIZONTAL)
                .forEach(hp -> {
                    long r =  hp.position;
                    var minPos = Math.min(hp.start,hp.end);
                    var maxPos = Math.max(hp.start,hp.end);
                    for(long c = minPos; c <= maxPos; c++) {
                        if (arr[(int)r][(int)c] == '.') cnt.getAndIncrement();
                        arr[(int)r][(int)c]='#';
                    }
                });
        lightPaths.stream()
                .filter(p -> p.type == LightPathType.VERTICAL)
                .forEach(hp -> {
                    var c = hp.position;
                    var minPos = Math.min(hp.start,hp.end);
                    var maxPos = Math.max(hp.start,hp.end);
                    for(var r = minPos; r <= maxPos; r++) {
                        if (arr[(int)r][(int)c] == '.') cnt.getAndIncrement();
                        arr[(int)r][(int)c]='#';
                    }
                });
        return cnt.longValue();
//        System.out.println("--Map---");
//        for(int y=0; y<heightOfSpace;y++) {
//            for (int x=0; x<widthOfSpace; x++) {
//                System.out.print(arr[y][x]);
//            }
//            System.out.println();
//        }
    }



    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day16-puzzle-input.txt"))) {
            initializeMap(br);

            // beam our initial light.
            Position startingDirectionAndPosition = new Position(LightDirection.RIGHT, -1L, 0L);
            beamLightFrom(startingDirectionAndPosition);
            //hGroupedMirrors.forEach((k,v) -> System.out.println(k + ": " + v));
            //System.out.println("-----");
            //vGroupedMirrors.forEach((k,v) -> System.out.println(k + ": " + v));

//            System.out.println("Seen Positions: ");
//            System.out.println(seenPositions.size());
//            System.out.println("Light Paths");
//            System.out.println(lightPaths.size());
//
//            System.out.println("Max left " + seenPositions.stream().max((a,b) -> Long.compare(a.x,b.x)));
//            System.out.println("Max row " + seenPositions.stream().max((a,b) -> Long.compare(a.y,b.y)));
//
//            System.out.println("Max left path " + lightPaths.stream()
//                            .filter(a -> a.type == LightPathType.HORIZONTAL)
//                    .max((a,b) -> Long.compare(a.end,b.end)));
//            System.out.println("Max row path" + lightPaths.stream()
//                    .filter(a -> a.type == LightPathType.VERTICAL)
//                    .max((a,b) -> Long.compare(a.end,b.end)));
//
//            var cellsHorizontalPaths = lightPaths.stream()
//                    .filter(p -> p.type == LightPathType.HORIZONTAL)
//                    .map(p -> Math.abs(p.end - p.start+1))
//                    .reduce(0L, Long::sum);
//            var cellsVerticalPaths = lightPaths.stream()
//                    .filter(p -> p.type == LightPathType.VERTICAL)
//                    .map(p -> Math.abs(p.end - p.start+1))
//                    .reduce(0L, Long::sum);
//            System.out.println("Cells Hit by Horizontal Paths:" + cellsHorizontalPaths);
//            System.out.println("Cells Hit by Vertical Paths:" + cellsVerticalPaths);
//
//            System.out.println("Horizontal Paths:");
//            lightPaths.stream()
//                    .filter(p -> p.type == LightPathType.HORIZONTAL)
//                    .forEach(p -> System.out.println(p + ": " + Math.abs(p.end - p.start+1)));
//            System.out.println("Vertical Paths:");
//            lightPaths.stream()
//                    .filter(p -> p.type == LightPathType.VERTICAL)
//                    .forEach(p -> System.out.println(p + ": " + Math.abs((p.end - p.start+1))));;

            //printMap();

            var solution = countEnergized();

            System.out.println("Number of energized tiles: " + solution);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static void initializeMap(BufferedReader br) {
        var initialMirrorMap = br.lines().toList();
        heightOfSpace = initialMirrorMap.size();
        widthOfSpace = initialMirrorMap.get(0).length();
        mirrorCoords = getListOfMirrorCoords(initialMirrorMap, heightOfSpace);

        //mirrorCoords.forEach(System.out::println);
        System.out.println("Number of mirrors: " + mirrorCoords.size());

        hGroupedMirrors = mirrorCoords.stream()
                .collect(groupingBy(c -> c.y));
        vGroupedMirrors = mirrorCoords.stream()
                .collect(groupingBy(c -> c.x));
    }


    private static long calculateTotalSumOfShortestPaths(List<MirrorCoord> galaxies) {
        int listSize = galaxies.size();
        return LongStream.range(0, listSize - 1)
                .reduce(0L, (acc, ndx) -> acc +
                        sumOfShortestPathsFrom(galaxies.get((int) ndx), galaxies.subList((int) (ndx + 1), listSize)));
    }

    private static long getShortestPath(MirrorCoord c1, MirrorCoord c2) {
        return Math.abs(c2.x - c1.x) + Math.abs(c2.y - c1.y);
    }

    private static long sumOfShortestPathsFrom(MirrorCoord base, List<MirrorCoord> remList) {
        // Math.addExact to be alerted of possible long overflow exceptions.
        return remList.stream()
                .reduce(0L,
                        (acc, c) -> Math.addExact(acc, getShortestPath(base, c))
                , (a, b) -> 0L);
    }

    private static List<Long> findEmptySlicesOfSpace(List<MirrorCoord> positions, Function<MirrorCoord, Long> xySelector, int totalSize) {
        var nonEmptyColumns = positions.stream()
                .collect(groupingBy(xySelector,
                                Collectors.counting()));
        //System.out.println(nonEmptyColumns);
        return LongStream.range(0, totalSize)
                .filter(i -> !nonEmptyColumns.containsKey(i))
                .boxed().toList();
    }

    private static List<MirrorCoord> getListOfMirrorCoords(List<String> initialMirrorMap, int heightOfSpace) {
        return IntStream.range(0, heightOfSpace).boxed()
                .<MirrorCoord>mapMulti((y, consumer) -> {
                    var line = initialMirrorMap.get(y);
                    AtomicInteger xOffset = new AtomicInteger();
                    line.chars().forEachOrdered(c -> {
                        var x = xOffset.getAndIncrement();
                        var mirrorType = switch((char) c) {
                            case '|' -> MirrorType.VERT_SPLIT;
                            case '-' -> MirrorType.HORIZ_SPLIT;
                            case '\\' -> MirrorType.TOP_LEFT;
                            case '/' -> MirrorType.TOP_RIGHT;
                            default -> MirrorType.NONE;
                        };
                        if (mirrorType != MirrorType.NONE) {
                            consumer.accept(new MirrorCoord(mirrorType,x,y));
                        }
                    });
                })
                .toList();
    }
}