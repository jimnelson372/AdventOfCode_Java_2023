package com.jimnelson372.aoc2023.day11;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day11Part1 {

    record Coord(
            int x,
            int y) {

    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day11-puzzle-input.txt"))) {
            var initialSpaceMap = br.lines()
                    .toList();
            int heightOfSpace = initialSpaceMap.size();
            int widthOfSpace = initialSpaceMap.get(0)
                    .length();

            var initialGalaxyCoords = getListOfGalaxyCoordinates(heightOfSpace, initialSpaceMap);

            var emptyColumns = findEmptySlicesOfSpace(initialGalaxyCoords, Coord::x, widthOfSpace);
            var emptyRows = findEmptySlicesOfSpace(initialGalaxyCoords, Coord::y, heightOfSpace);

            var coordsAfterExpansion = expandSpace(initialGalaxyCoords, emptyColumns, emptyRows);
            System.out.println("Total space coordinates before expansion: " + heightOfSpace * widthOfSpace);
            System.out.println("Total space coordinates after expansion: "
                                       + (heightOfSpace + emptyRows.size() * 2) * (widthOfSpace + emptyColumns.size() * 2));
            System.out.println("Number of galaxies: " + initialGalaxyCoords.size());

//            // debug info
//            initialSpaceMap.forEach(System.out::println);
//            System.out.println(initialGalaxyCoords);
//            System.out.println(emptyColumns);
//            System.out.println(emptyRows);
//            System.out.println(coordsAfterExpansion);

            var sumOfShortestPaths = calculateTotalSumOfShortestPaths(coordsAfterExpansion);

            System.out.println("Sum Of Shortest Paths between all galaxies: " + sumOfShortestPaths);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static List<Coord> getListOfGalaxyCoordinates(int heightOfSpace, List<String> initialSpaceMap) {
        return IntStream.range(0, heightOfSpace)
                .boxed()
                .<Coord>mapMulti((y, consumer) -> {
                    var line = initialSpaceMap.get(y);
                    var x = line.indexOf("#");
                    while (x >= 0) {
                        consumer.accept(new Coord(x, y));
                        x = line.indexOf("#", x + 1);
                    }
                })
                .toList();
    }

    private static List<Integer> findEmptySlicesOfSpace(List<Coord> positions, Function<Coord, Integer> xySelector,
                                                        int totalSize) {
        var nonEmptyColumns = positions.stream()
                .collect(Collectors.groupingBy(xySelector, Collectors.counting()));
        //System.out.println(nonEmptyColumns);
        return IntStream.range(0, totalSize)
                .filter(i -> !nonEmptyColumns.containsKey(i))
                .boxed()
                .toList();
    }

    private static List<Coord> expandSpace(List<Coord> positions, List<Integer> emptyColumns, List<Integer> emptyRows) {
        return positions.stream()
                .map(c -> {
                    var x = c.x + emptyColumns.stream()
                            .reduce(0,
                                    (acc, p) -> acc + (c.x > p
                                            ? 1
                                            : 0));
                    var y = c.y + emptyRows.stream()
                            .reduce(0,
                                    (acc, p) -> acc + (c.y > p
                                            ? 1
                                            : 0));
                    return new Coord(x, y);
                })
                .toList();
    }

    private static int calculateTotalSumOfShortestPaths(List<Coord> galaxies) {
        int listSize = galaxies.size();
        return IntStream.range(0, listSize - 1)
                .reduce(0, (acc, ndx) -> acc +
                        sumOfShortestPathsFrom(galaxies.get(ndx), galaxies.subList(ndx + 1, listSize)));
    }

    private static int sumOfShortestPathsFrom(Coord base, List<Coord> remList) {
        // Math.addExact to be alerted of possible int overflow exceptions.
        return remList.stream()
                .map(c -> getShortestPath(base, c))
                .reduce(0, Math::addExact);
    }

    private static int getShortestPath(Coord c1, Coord c2) {
        return Math.abs(c2.x - c1.x) + Math.abs(c2.y - c1.y);
    }


}