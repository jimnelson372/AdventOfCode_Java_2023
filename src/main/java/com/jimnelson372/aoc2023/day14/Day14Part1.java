package com.jimnelson372.aoc2023.day14;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Day14Part1 {

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

    private static List<RockCoord> tiltNorth(List<RockCoord> rockCoords, int width) {
        for (int row = 0; row < width; row++) {
            int curRow = row;
            var hashrocks = rockCoords.stream().filter(rock -> rock.x == curRow && rock.type == RockType.HASH).toList();
            //System.out.println(hashrocks);
            long lastLowest = hashrocks.get(0).y - 1;
            for (RockCoord hashrock : hashrocks) {
                AtomicLong dropTo = new AtomicLong(lastLowest);
                long finalLastLowest = lastLowest;
                rockCoords = rockCoords.stream()
                        .map(rock -> {
                            if (rock.type == RockType.BIG_O && rock.y > finalLastLowest && rock.y < hashrock.y && rock.x == curRow) {
                                return new RockCoord(RockType.BIG_O, rock.x, dropTo.incrementAndGet());
                            } else
                                return rock;
                        }).toList();

                lastLowest = hashrock.y;
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
            rockCoords = tiltNorth(rockCoords, width);

            var load = rockCoords.stream().reduce(0L, (acc,r) -> acc + ((r.type == RockType.BIG_O) ? (height-2)-r.y+1 : 0), (a,b)->a);

            System.out.println("Load on North Support: " + load);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }




}