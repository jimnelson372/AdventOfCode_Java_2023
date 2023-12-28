package com.jimnelson372.aoc2023.day16;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;

public class Day16Part1 {

    static List<MirrorCoord> mirrorCoords = List.of();
    static int heightOfSpace = 0;
    static int widthOfSpace = 0;
    static Map<Long, List<MirrorCoord>> hGroupedMirrors = Map.of();
    static Map<Long, List<MirrorCoord>> vGroupedMirrors = Map.of();
    static Set<Position> seenPositions = new HashSet<>();
    static List<LightPath> lightPaths = new ArrayList<>();
    enum MirrorType {
        VERT_SPLIT,
        HORIZ_SPLIT,
        TOP_LEFT,
        TOP_RIGHT,
        NONE
    }
    enum LightPathType {
        HORIZONTAL,
        VERTICAL
    }
    enum LightDirection {
        UP,
        DOWN,
        LEFT,
        RIGHT;

        private Map<Long, List<MirrorCoord>> getPotentialMirrors() {
            return switch (this) {
                case LEFT, RIGHT -> hGroupedMirrors;
                case UP, DOWN -> vGroupedMirrors;
            };
        }
    }

    record MirrorCoord(
            MirrorType type,
            long x,
            long y) {
        public static MirrorCoord None = new MirrorCoord(MirrorType.NONE, 0L, 0L);
    }

    record LightPath(
            LightPathType type,
            long position,
            long start,
            long end) {
    }

    record Position(
            LightDirection direction,
            long x,
            long y) {
    }

    private static void clearState() {
        seenPositions = new HashSet<>();
        lightPaths = new ArrayList<>();
    }

    public static void printMap() {
        char[][] arr = new char[heightOfSpace][widthOfSpace];
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                arr[y][x] = '.';
            }
        }
        lightPaths.stream()
                .filter(p -> p.type == LightPathType.HORIZONTAL)
                .forEach(hp -> {
                    long r = hp.position;
                    for (long c = hp.start; c <= hp.end; c++) {
                        arr[(int) r][(int) c] = '#';
                    }
                });
        lightPaths.stream()
                .filter(p -> p.type == LightPathType.VERTICAL)
                .forEach(hp -> {
                    var c = hp.position;
                    for (var r = hp.start; r <= hp.end; r++) {
                        arr[(int) r][(int) c] = '#';
                    }
                });
        System.out.println("--Map---");
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                System.out.print(arr[y][x]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day16-puzzle-input.txt"))) {
            initializeMap(br);

            // beam our initial light.
            Position startingDirectionAndPosition = new Position(LightDirection.RIGHT, -1L, 0L);
            beamLightFrom(startingDirectionAndPosition);

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
        var initialMirrorMap = br.lines()
                .toList();
        heightOfSpace = initialMirrorMap.size();
        widthOfSpace = initialMirrorMap.get(0)
                .length();
        mirrorCoords = getListOfMirrorCoords(initialMirrorMap, heightOfSpace);

        System.out.println("Number of mirrors: " + mirrorCoords.size());

        hGroupedMirrors = mirrorCoords.stream()
                .collect(groupingBy(c -> c.y));
        vGroupedMirrors = mirrorCoords.stream()
                .collect(groupingBy(c -> c.x));
    }

    private static void beamLightFrom(Position position) {
        var workingList = new LinkedList<Position>();
        workingList.add(position);
        while (!workingList.isEmpty()) {
            var nextPosition = workingList.removeFirst();

            List<Position> resultPositionsList = beamLightFromStep(nextPosition);

            workingList.addAll(resultPositionsList);
        }
    }

    public static long countEnergized() {
        char[][] arr = new char[heightOfSpace][widthOfSpace];
        for (int y = 0; y < heightOfSpace; y++) {
            for (int x = 0; x < widthOfSpace; x++) {
                arr[y][x] = '.';
            }
        }
        AtomicLong cnt = new AtomicLong();
        lightPaths.stream()
                .filter(p -> p.type == LightPathType.HORIZONTAL)
                .forEach(hp -> {
                    var r = hp.position;
                    for (long c = hp.start; c <= hp.end; c++) {
                        if (arr[(int) r][(int) c] == '.') cnt.getAndIncrement();
                        arr[(int) r][(int) c] = '#';
                    }
                });
        lightPaths.stream()
                .filter(p -> p.type == LightPathType.VERTICAL)
                .forEach(hp -> {
                    var c = hp.position;
                    for (var r = hp.start; r <= hp.end; r++) {
                        if (arr[(int) r][(int) c] == '.') cnt.getAndIncrement();
                        arr[(int) r][(int) c] = '#';
                    }
                });
        return cnt.longValue();
    }

    private static List<MirrorCoord> getListOfMirrorCoords(List<String> initialMirrorMap, int heightOfSpace) {
        return IntStream.range(0, heightOfSpace)
                .boxed()
                .<MirrorCoord>mapMulti((y, consumer) -> {
                    var line = initialMirrorMap.get(y);
                    AtomicInteger xOffset = new AtomicInteger();
                    line.chars()
                            .forEachOrdered(c -> {
                                var x = xOffset.getAndIncrement();
                                var mirrorType = switch ((char) c) {
                                    case '|' -> MirrorType.VERT_SPLIT;
                                    case '-' -> MirrorType.HORIZ_SPLIT;
                                    case '\\' -> MirrorType.TOP_LEFT;
                                    case '/' -> MirrorType.TOP_RIGHT;
                                    default -> MirrorType.NONE;
                                };
                                if (mirrorType != MirrorType.NONE) {
                                    consumer.accept(new MirrorCoord(mirrorType, x, y));
                                }
                            });
                })
                .toList();
    }

    private static List<Position> beamLightFromStep(Position position) {
        if (seenPositions.contains(position)) {
            return List.of();
        }

        seenPositions.add(position);

        var potentialMirrors = position.direction.getPotentialMirrors();

        if (position.direction == LightDirection.RIGHT) {
            if (position.x == widthOfSpace - 1) return List.of();
            var mirror = potentialMirrors.getOrDefault(position.y, List.of())
                    .stream()
                    .filter(mc -> mc.x > position.x && mc.type != MirrorType.HORIZ_SPLIT)
                    .findFirst()
                    .orElse(MirrorCoord.None);
            if (mirror.type == MirrorType.NONE) {
                lightPaths.add(new LightPath(LightPathType.HORIZONTAL, position.y, position.x + 1, widthOfSpace - 1));
                return List.of();
            } else {
                lightPaths.add(new LightPath(LightPathType.HORIZONTAL, position.y, position.x + 1, mirror.x));

                return switch (mirror.type) {
                    case VERT_SPLIT -> List.of(new Position(LightDirection.UP, mirror.x, mirror.y),
                                               new Position(LightDirection.DOWN, mirror.x, mirror.y));
                    case TOP_LEFT -> List.of(new Position(LightDirection.DOWN, mirror.x, mirror.y));
                    case TOP_RIGHT -> List.of(new Position(LightDirection.UP, mirror.x, mirror.y));
                    default -> List.of();
                };
            }
        }

        if (position.direction == LightDirection.LEFT) {
            if (position.x == 0) return List.of();
            var mirror = potentialMirrors.getOrDefault(position.y, List.of())
                    .stream()
                    .filter(mc -> mc.x < position.x && mc.type != MirrorType.HORIZ_SPLIT)
                    .reduce((acc, mc) -> mc) // get us the last one.
                    .orElse(MirrorCoord.None);
            if (mirror.type == MirrorType.NONE) {
                lightPaths.add(new LightPath(LightPathType.HORIZONTAL, position.y, 0, position.x - 1));
                return List.of();
            } else {
                lightPaths.add(new LightPath(LightPathType.HORIZONTAL, position.y, mirror.x, position.x - 1));

                return switch (mirror.type) {
                    case VERT_SPLIT -> List.of(new Position(LightDirection.UP, mirror.x, mirror.y),
                                               new Position(LightDirection.DOWN, mirror.x, mirror.y));
                    case TOP_LEFT -> List.of(new Position(LightDirection.UP, mirror.x, mirror.y));
                    case TOP_RIGHT -> List.of(new Position(LightDirection.DOWN, mirror.x, mirror.y));
                    default -> List.of();
                };
            }
        }

        if (position.direction == LightDirection.UP) {
            if (position.y == 0) return List.of();
            var mirror = potentialMirrors.getOrDefault(position.x, List.of())
                    .stream()
                    .filter(mc -> mc.y < position.y && mc.type != MirrorType.VERT_SPLIT)
                    .reduce((acc, mc) -> mc) // get us the last one.
                    .orElse(MirrorCoord.None);
            if (mirror.type == MirrorType.NONE) {
                lightPaths.add(new LightPath(LightPathType.VERTICAL, position.x, 0, position.y - 1));
                return List.of();
            } else {
                lightPaths.add(new LightPath(LightPathType.VERTICAL, position.x, mirror.y, position.y - 1));

                return switch (mirror.type) {
                    case HORIZ_SPLIT -> List.of(new Position(LightDirection.LEFT, mirror.x, mirror.y),
                                                new Position(LightDirection.RIGHT, mirror.x, mirror.y));
                    case TOP_LEFT -> List.of(new Position(LightDirection.LEFT, mirror.x, mirror.y));
                    case TOP_RIGHT -> List.of(new Position(LightDirection.RIGHT, mirror.x, mirror.y));
                    default -> List.of();
                };
            }
        }

        if (position.direction == LightDirection.DOWN) {
            if (position.y == heightOfSpace - 1) return List.of();
            var mirror = potentialMirrors.getOrDefault(position.x, List.of())
                    .stream()
                    .filter(mc -> mc.y > position.y && mc.type != MirrorType.VERT_SPLIT)
                    .findFirst()
                    .orElse(MirrorCoord.None);
            if (mirror.type == MirrorType.NONE) {
                lightPaths.add(new LightPath(LightPathType.VERTICAL, position.x, position.y + 1, heightOfSpace - 1));
                return List.of();
            } else {
                lightPaths.add(new LightPath(LightPathType.VERTICAL, position.x, position.y + 1, mirror.y));

                return switch (mirror.type) {
                    case HORIZ_SPLIT -> List.of(new Position(LightDirection.LEFT, mirror.x, mirror.y),
                                                new Position(LightDirection.RIGHT, mirror.x, mirror.y));
                    case TOP_LEFT -> List.of(new Position(LightDirection.RIGHT, mirror.x, mirror.y));
                    case TOP_RIGHT -> List.of(new Position(LightDirection.LEFT, mirror.x, mirror.y));
                    default -> List.of();
                };
            }
        }


        return List.of();
    }

}