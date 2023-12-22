package com.jimnelson372.day21;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public class Day21Part2 {


    static Set<Position> seen = new HashSet<>();

    record Position(
            long x,
            long y,
            int steps) {
    }

    private static long recursivelyStep(char[][] garden, Position from) {
        if (seen.contains(from)) return 0;
        if (from.steps == 0) {
            seen.add(from);
            return 1;
        }
        seen.add(from);

        long width = garden[0].length;
        long height = garden.length;

        return Stream.of(List.of(0L, 1L), List.of(0L, -1L),
                         List.of(1L, 0L), List.of(-1L, 0L))
                     .map(dir -> {
                         long posY = Math.addExact(from.y, dir.get(0));
                         long posX = Math.addExact(from.x, dir.get(1));

                         long lookX = ((posX % width) + width) % width;
                         long lookY = ((posY % height) + height) % height;
                         char content = garden[(int) lookY][(int) lookX];

                         return switch (content) {
                             case '.', 'O' -> recursivelyStep(garden, new Position(posX, posY, from.steps - 1));
                             case '#' -> 0L;
                             default -> 0L;
                         };
                     })
                     .reduce(0L, Long::sum);
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources")
                                    .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day21-puzzle-input.txt"))) {
            var initialMap = br.lines()
                               .toList();

            int width = initialMap.get(0).length();
            int height = initialMap.size();

            var garden = new char[height][width];

            int countHash = 0;
            Position start = new Position(0, 0, 0);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    char content = initialMap.get(y).charAt(x);
                    if (content == 'S') {
                        start = new Position(x, y, 0);
                        garden[y][x] = '.';
                    } else {
                        if (content == '#') {countHash++;}
                        garden[y][x] = content;
                    }
                }
            }

            Position finalStart1 = start;
            List.of(64, 26501365)
                .forEach(testSteps -> {
                    // Only really work with the specific puzzle data for Part 2.
                    //   I had seen the pattern for this particular puzzle, but wasn't able to
                    //   figure out how to use the lower calculated values to reach the solution.
                    //   Prior problems had just needed a Least Common Multiple calculation.  This one needed
                    //   A polynomial interpolation.
                    if (testSteps > 500) {
                        System.out.println("Now working on Part 2 solution (in stages)....");
                        var stepsList = Stream.of(0L, 1L, 2L)
                                              .map(i -> (width - 1) / 2 + (width * i))
                                              .toList();
                        var posList = stepsList.stream()
                                               .map(steps -> {
                                                   seen = new HashSet<>();
                                                   var count = recursivelyStep(garden,
                                                                               new Position(finalStart1.x,
                                                                                            finalStart1.y,
                                                                                            Math.toIntExact(steps)));
                                                   System.out.println("Found output for " + steps + " steps: " + count);
                                                   return count;
                                               })
                                               .toList();
                        var count = NewtonInterpolation.interpolate(stepsList, posList, testSteps);
                        System.out.println("Part 2 solution: Interpolated Result for " + testSteps + " steps: " + count);
                    } else {
                        // Even "smaller" steps can be larger in this version of the program than in my part 1.
                        var countForSmallerSets =
                                recursivelyStep(garden, new Position(finalStart1.x, finalStart1.y, testSteps));
                        System.out.println("Part 1 solution: Smaller set solution for " + testSteps + " steps: " + countForSmallerSets);
                    }
                });

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

}