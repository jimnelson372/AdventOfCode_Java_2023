package com.jimnelson372.aoc2023.day18;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiFunction;

public class Day18Part2_minimal {
    record Instruct(char inst, long distance) {}
    private static List<Instruct> getPerimeterInstructionsFromInput(List<String> directionsList) {
        // Part 2 basically required changing how the coordinates were read in from the above to the following.
        var instructs = directionsList.stream()
                .map(inst -> {
                    var dir = "RDLU".charAt(inst.charAt(inst.length()-2)-'0');
                    var dist = Integer.parseInt(inst.substring(inst.length()-7, inst.length()-2),16);
                    return new Instruct(dir, dist);
                }).toList();
        return instructs;
    }

    record AreaReducer(long area, long x, long y) {
    }
    private static long calculateFullAreaFromInstructionsUsingShoelace(List<Instruct> instructs) {
        // The reducer does all the work of the Shoestring area finding algorithm
        //   and Pick's Theorem.  I pass around a record, AreaReducer to hold the
        //   area calculation so far and the previous "coordinate's" x,y.
        //   then in each step, it can calculate the new points x,y from the instruction
        //   and do the calculation as though it were scanning 2 coordinates at a time.
        //   I was able to add in the perimeter calculation, since at the end I have to
        //   divide both calculations by 2.  Then I add 1 to finish off Pick's theorem's adjustment.
        return instructs.stream()
            .reduce(new AreaReducer(0L, 0L, 0L),
                    (acc,i) -> {
                        long x = switch (i.inst) {
                            case 'L' -> acc.x - i.distance;
                            case 'R' -> acc.x + i.distance;
                            default -> acc.x;
                        };
                        long y = switch (i.inst) {
                            case 'U' -> acc.y - i.distance;
                            case 'D' -> acc.y + i.distance;
                            default -> acc.y;
                        };
                        // Now it's as though I have two coordinates to "shoelace"
                        long areaDelta = acc.x*y - x*acc.y; // shoestring algorithm for inner area

                        areaDelta += i.distance;  // adding perimeter at the same time.

                        return new AreaReducer(acc.area + areaDelta, x, y);
                    },
                    Day18Part2_minimal::combiner) // ignore this line, just needed by reduce to permit type change.
            .area / 2 + 1;  // Divide by 2 is needed for both shoestring area and Pick's theorem. +1 is Pick's.
    }

    record AreaReducerForGreens(long area, long perimeter, long y) {
    }

    static <T> T combiner(T a, T b) {
        throw new RuntimeException("This combiner should not be called.  Don't run that stream in parallel.");
    }
    private static Long calculateFullAreaFromInstructionsUsingGreens(List<Instruct> instructs) {
        // This version does Green's Area Algorithm in the reducer instead of the Shoestring.
        // Since Green's version doesn't require the area to be divided by 2, the perimeter
        // is calculated separately so that Pick's Theorem adjustments can be made at the end.
        var computed = instructs.stream()
                .reduce(new AreaReducerForGreens(0L, 0L, 0L),
                        (acc,i) -> {
                            long areaDelta = switch (i.inst) {
                                case 'R' -> acc.y * i.distance;
                                case 'L' -> -acc.y * i.distance;
                                default -> 0L;
                            };
                            long y = switch (i.inst) {
                                case 'U' -> acc.y + i.distance;
                                case 'D' -> acc.y - i.distance;
                                default -> acc.y;
                            };
                            return new AreaReducerForGreens(
                                    acc.area + areaDelta,
                                    acc.perimeter + i.distance,
                                    y);
                        },
                        Day18Part2_minimal::combiner); // ignore this line, just needed by reduce to permit type change.
                return computed.area + (computed.perimeter / 2 + 1);
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day18-puzzle-input.txt"))) {
            var directionsList = br.lines().toList();

            var instructs = getPerimeterInstructionsFromInput(directionsList);

            var shoelaceAndPickAreaCalc = calculateFullAreaFromInstructionsUsingShoelace(instructs);
            System.out.println("Shoelace Algorithm Area Calc = " + shoelaceAndPickAreaCalc);

            var greensAreaWithPicksCalc = calculateFullAreaFromInstructionsUsingGreens(instructs);
            System.out.println("Green's Algorithm Area Calc  = " + greensAreaWithPicksCalc);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

}