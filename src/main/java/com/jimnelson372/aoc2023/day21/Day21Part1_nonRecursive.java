package com.jimnelson372.aoc2023.day21;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Day21Part1_nonRecursive {

    record Position(
            int x,
            int y) {

        Position up() {
            return new Position(x, y - 1);
        }

        Position down() {
            return new Position(x, y + 1);
        }

        Position left() {
            return new Position(x - 1, y);
        }

        Position right() {
            return new Position(x + 1, y);
        }
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day21-puzzle-input.txt"))) {
            var initialMap = new ArrayList<>(br.lines()
                                                     .map(l -> "#" + l + "#")
                                                     .toList());
            int width = initialMap.get(0)
                    .length();
            String st = "#".repeat(width);
            initialMap.add(0, st);
            initialMap.add(st);
            int height = initialMap.size();

            var garden = new char[height][width];

            Position start = new Position(0, 0);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    char content = initialMap.get(y)
                            .charAt(x);
                    if (content == 'S') {
                        start = new Position(x, y);
                        garden[y][x] = '.';
                    } else {
                        garden[y][x] = content;
                    }
                }
            }

            // The following two streams with flatmap are based on a solution I saw (after my own solution
            //  was complete) on github: https://github.com/SPixs/AOC2023/blob/master/src/Day21.java
            //  This first is very much like that, using a toSet() to filter out the duplicates,
            //  and having the set at the end of each iteration.  Plus I liked his use of a Point
            // class to handle the right, left, up, down, rather than my original approach of List.of(dx,dy).

            Set<Position> posSet = new HashSet<>();
            posSet.add(start);
            for (var i = 0; i < 64; i++) {
                posSet = posSet.stream()
                        .flatMap(p -> Stream.of(p.left(),
                                                p.right(),
                                                p.up(),
                                                p.down())
                                .filter(pos -> garden[pos.y][pos.x] == '.'))
                        .collect(Collectors.toSet());
            }
            var count = posSet.size();

//            var count = Stream.of(start)
//                    .flatMap(p -> {
//                        var strm = Stream.of(p);
//                        for (var i = 0; i < 64; i++) {
//                            strm = strm.flatMap(from ->
//                                                Stream.of(from.up(),
//                                                          from.down(),
//                                                          from.left(),
//                                                          from.right())
//                                                        .filter(dir -> garden[dir.y][dir.x] == '.'))
//                                    .distinct();
//                        }
//                        ;
//                        return strm;
//                    })
//                    .distinct()
//                    .count();

            System.out.println("Count = " + count);

            //printMap(garden);
            System.out.println(start);
            //initialMap.forEach(System.out::println);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    static void printMap(char[][] map) {
        for (char[] rows : map) {
            for (char elem : rows) {
                System.out.print(elem);
            }
            System.out.println();
        }
    }
}