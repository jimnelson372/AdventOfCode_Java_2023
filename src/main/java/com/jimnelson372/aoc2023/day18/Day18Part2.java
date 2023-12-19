package com.jimnelson372.aoc2023.day18;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Day18Part2 {

    record Coordinate(long x, long y){}
    record Instruct(char inst, long distance) {}

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day18-puzzle-input.txt"))) {
            var directionsList = br.lines().toList();

            var instructs = getPerimeterInstructionsFromInput(directionsList);

            // This version of the solution uses the Shoelace Algorithm modified by Pick's Theorem.
            //  It was a complete rewrite of how to solve this kind of problem from how I did it in Part 1.
            //  In my defense, I thought to stick with the grid array in Part 1 since I thought the colors
            //  would come into play to create an image.  However, it turned out the colors were just
            //  considered incorrect encoding of the instructions.   Had Part 1 not mentioned color, I might
            //  have gone with this coordinate approach.

            var perimeter = calculateAreaOfThePerimeter(instructs);
            List<Coordinate> coordinates = convertInstructionsToListOfCornerCoordinates(instructs);
            var shoelaceAreaCalc = calculateInnerAreaUsingShoelaceAlgorithm(coordinates);

            System.out.println("Number of Coordinates = " + coordinates.size());
            System.out.println("The Perimeter Area = " + perimeter);
            System.out.println("Shoelace Algorithm Area Calc = " + shoelaceAreaCalc);
            long picksTheoremAdjustment = perimeter / 2 + 1;
            System.out.println("Pick's Theorem Ajustment = " + picksTheoremAdjustment);
            System.out.println("Shoelace Area + Pick's Theorem = " + (shoelaceAreaCalc + picksTheoremAdjustment));

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static List<Instruct> getPerimeterInstructionsFromInput(List<String> directionsList) {
//        var instructsPart1 = directionsList.stream()
//                .map(inst -> {
//                    var dir = inst.charAt(0);
//                    var dist = Integer.valueOf(inst.substring(2,inst.indexOf(" ",2)));
//                    return new Instruct(dir, dist);
//                }).toList();

        // Part 2 basically required changing how the coordinates were read in from the above to the following.
        var instructs = directionsList.stream()
                .map(inst -> {
                    var dir = "RDLU".charAt(inst.charAt(inst.length()-2)-'0');
                    var dist = Integer.parseInt(inst.substring(inst.length()-7, inst.length()-2),16);
                    return new Instruct(dir, dist);
                }).toList();
        return instructs;
    }

    private static long calculateInnerAreaUsingShoelaceAlgorithm(List<Coordinate> coordinates) {
        return IntStream.range(0, coordinates.size()-1)
                .boxed()
                .map(i -> {
                    var c1 = coordinates.get(i);
                    var c2 = coordinates.get(i+1);
                    long area = Math.subtractExact(Math.multiplyExact(c1.x,c2.y), Math.multiplyExact(c2.x,c1.y));
                    return area;
                })
                .reduce(0L,Long::sum) / 2;
    }

    private static List<Coordinate> convertInstructionsToListOfCornerCoordinates(List<Instruct> instructs) {
        AtomicLong x = new AtomicLong(0);
        AtomicLong y = new AtomicLong(0);
        return instructs.stream()
                .map(i -> {
                    var coord = switch (i.inst) {
                        case 'U' -> new Coordinate(x.get(), y.addAndGet(-i.distance));
                        case 'D' -> new Coordinate(x.get(), y.addAndGet(i.distance));
                        case 'L' -> new Coordinate(x.addAndGet(-i.distance), y.get());
                        case 'R' -> new Coordinate(x.addAndGet(i.distance), y.get());
                        default -> new Coordinate(x.get(), y.get());
                    };
                    return coord;
                })
                .toList();
    }

    private static Long calculateAreaOfThePerimeter(List<Instruct> instructs) {
        return instructs.stream()
                .map(i -> i.distance)
                .reduce(0L, Long::sum);
    }


}