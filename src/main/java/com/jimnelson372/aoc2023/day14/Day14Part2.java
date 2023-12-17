package com.jimnelson372.aoc2023.day14;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Day14Part2 {

    record RockCoord(RockType type, long x, long y) {}
    enum RockType { HASH, BIG_O }

    private static List<RockCoord> getListOfRockCoordinates(int heightOfSpace, List<String> initialMap) {
        return IntStream.range(0, heightOfSpace).boxed()
                .<RockCoord>mapMulti((y, consumer) -> {
                    var line = initialMap.get(y);
                    var x = line.indexOf("#");
                    while (x >= 0) {
                        consumer.accept(new RockCoord(RockType.HASH, x, y));
                        x = line.indexOf("#", x + 1);
                    }
                    x = line.indexOf("O");
                    while (x >= 0) {
                        consumer.accept(new RockCoord(RockType.BIG_O, x, y));
                        x = line.indexOf("O", x + 1);
                    }
                }).toList();
    }

    private static List<RockCoord> tiltNorth(List<RockCoord> rockCoords, int width, int height) {
        for (int col = 0; col < width; col++) {
            int curCol = col;
            var hashrocks = rockCoords.stream().filter(rock -> rock.x == curCol && rock.type == RockType.HASH).toList();
            //System.out.println(hashrocks);
            long lastLowest = hashrocks.get(0).y - 1;
            for (RockCoord hashrock : hashrocks) {
                AtomicLong dropTo = new AtomicLong(lastLowest);
                long finalLastLowest = lastLowest;
                rockCoords = rockCoords.stream()
                        .map(rock -> {
                            if (rock.type == RockType.BIG_O && rock.y > finalLastLowest && rock.y < hashrock.y && rock.x == curCol) {
                                return new RockCoord(RockType.BIG_O, rock.x, dropTo.incrementAndGet());
                            } else
                                return rock;
                        }).toList();

                lastLowest = hashrock.y;
            }
        }
        return rockCoords;
    }

    private static List<RockCoord> tiltSouth(List<RockCoord> rockCoords, int width, int height) {
        for (int col = 0; col < width; col++) {
            int curCol = col;
            var hashRocks = rockCoords.stream()
                    .filter(rock -> rock.x == curCol && rock.type == RockType.HASH)
                    .sorted((a,b) -> Long.compare(b.y,a.y))
                    .toList();
            long lastHighest = hashRocks.get(hashRocks.size()-1).y + 1;
            for (RockCoord hashRock : hashRocks) {
                AtomicLong dropTo = new AtomicLong(lastHighest);
                long finalLastHighest = lastHighest;
                rockCoords = rockCoords.stream()
                        .map(rock -> {
                            if (rock.type == RockType.BIG_O && rock.y < finalLastHighest && rock.y > hashRock.y && rock.x == curCol) {
                                return new RockCoord(RockType.BIG_O, rock.x, dropTo.decrementAndGet());
                            } else
                                return rock;
                        }).toList();

                lastHighest = hashRock.y;
            }
//            var os = rockCoords.stream().filter(rocks -> rocks.type == RockType.BIG_O && rocks.x == curCol).toList();
//            System.out.println(os);
        }
        return rockCoords;
    }

    private static List<RockCoord> tiltWest(List<RockCoord> rockCoords, int width, int height) {
        for (int row = 0; row < height; row++) {
            int curRow = row;
            var hashrocks = rockCoords.stream().filter(rock -> rock.y == curRow && rock.type == RockType.HASH).toList();
            //System.out.println(hashrocks);
            long mostWestardly = hashrocks.get(0).x - 1;
            for (RockCoord hashrock : hashrocks) {
                AtomicLong dropTo = new AtomicLong(mostWestardly);
                long finalMostWestardly = mostWestardly;
                rockCoords = rockCoords.stream()
                        .map(rock -> {
                            if (rock.type == RockType.BIG_O && rock.x > finalMostWestardly && rock.x < hashrock.x && rock.y == curRow) {
                                return new RockCoord(RockType.BIG_O, dropTo.incrementAndGet(), rock.y);
                            } else
                                return rock;
                        }).toList();

                mostWestardly = hashrock.x;
            }
//            var os = rockCoords.stream().filter(rocks -> rocks.type == RockType.BIG_O && rocks.y == curRow).toList();
//            System.out.println(os);
        }
        return rockCoords;
    }

    private static List<RockCoord> tiltEast(List<RockCoord> rockCoords, int width, int height) {
        for (int row = 0; row < height; row++) {
            int curRow = row;
            var hashRocks = rockCoords.stream()
                    .filter(rock -> rock.y == curRow && rock.type == RockType.HASH)
                    .sorted((a,b) -> Long.compare(b.x,a.x))
                    .toList();
            long mostEasterly = hashRocks.get(hashRocks.size()-1).x + 1;
            for (RockCoord hashRock : hashRocks) {
                AtomicLong dropTo = new AtomicLong(mostEasterly);
                long finalMostEasterly = mostEasterly;
                rockCoords = rockCoords.stream()
                        .map(rock -> {
                            if (rock.type == RockType.BIG_O && rock.x < finalMostEasterly && rock.x > hashRock.x && rock.y == curRow) {
                                return new RockCoord(RockType.BIG_O, dropTo.decrementAndGet(), rock.y);
                            } else
                                return rock;
                        }).toList();

                mostEasterly = hashRock.x;
            }

        }
        return rockCoords;
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day14-puzzle-input.txt"))) {
            var initialMap = new ArrayList<>(br.lines()
                    .map(l -> "#" + l + "#")
                    .toList());
            int width = initialMap.get(0).length();
            String st = "#".repeat(width);
            initialMap.add(0,st);
            initialMap.add(st);
            int height = initialMap.size();

            var rockCoords = getListOfRockCoordinates(height, initialMap);
//
//            for(int )

            // it turns out that after 300 cycles, it begins a 700 cycle repetition.
            //  The value a 1000 thus turns out to be the same ast 1000000000.  I have
            //  not worked to improve the performance, but a cache that recognizes repetitions
            //  would be able to figure out the final solution programatically.  I solved it by
            //  observing the output, detecting the cycle, then determining the above.
            for(int cycle=0; cycle<1000; cycle++) {
                rockCoords = tiltNorth(rockCoords, width, height);
                rockCoords = tiltWest(rockCoords, width, height);
                rockCoords = tiltSouth(rockCoords, width, height);
                rockCoords = tiltEast(rockCoords, width, height);
                if (cycle % 100 == 0) {
                    var load = rockCoords.stream().reduce(0L, (acc, r) -> acc + ((r.type == RockType.BIG_O) ? (height - 2) - r.y + 1 : 0), (a, b) -> a);
                    System.out.println("Load on North Support: " + load + " at cycle: " + cycle);
                }
            }
            var load = rockCoords.stream().reduce(0L, (acc, r) -> acc + ((r.type == RockType.BIG_O) ? (height - 2) - r.y + 1 : 0), (a, b) -> a);
            System.out.println("Load on North Support: " + load + " final");

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }




}