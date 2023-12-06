package com.jimnelson372.aoc2023.day5;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Day5Part1 {

    record NumberRange(Long fromStart, Long fromEnd, Long mapDiff) {}

    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day5-puzzle-input.txt"))) {
            //Parse Seeds Line to get our starting seed #s.
            String nextLine = br.readLine();
            var valuesToMap = parseToListOfNumbers(nextLine.split(":\\s+")[1]);
            //System.out.println("Starting seed #s: " + valuesToMap);
            System.out.println("Number of seeds we're exploring: " + valuesToMap.size());

            // For Each of the Maps
            //    parse and organize the ranges for lookup
            nextLine = readLineAfterSkippingBlanks(br);
            while (nextLine != null) {
                //System.out.println("Mapping: " + nextLine);
                nextLine = readLineAfterSkippingBlanks(br);

                // Build Mapping To list, sorted by ranges.
                var toMap = new ArrayList<NumberRange>();
                while (nextLine != null && Character.isDigit(nextLine.codePointAt(0))) {
                    toMap.add(parseToNumRange(nextLine));
                    nextLine = readLineAfterSkippingBlanks(br);
                }
                toMap.sort(Comparator.comparing(p -> p.fromStart)); // sorting it to make finding one faster.

                // Now we can perform our first mapping of the seeds
                valuesToMap = valuesToMap.stream().map(value -> {
                    return toMap.stream()
                            .filter(range -> valueInNumberRange(value, range))
                            .findFirst()  // an Optional if found.
                            .map(val-> value + val.mapDiff)  //if found in a range, map from the range.
                            .orElse(value); // if not found in a range, return the same value.
                }).toList();
                //System.out.println("   mapped to: " + valuesToMap);

                if (nextLine == null) break;
            }

            //System.out.println("Resulting location #s: " + valuesToMap);

            // find the smallest location number of this final mapping and that's our solution!
            var result = valuesToMap.stream().min(Long::compareTo).orElse(0L);
            System.out.println("Result (minimum location) = " + result);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }

    private static boolean valueInNumberRange(Long value, NumberRange r) {
        return value.compareTo(r.fromStart) >= 0 && value.compareTo(r.fromEnd) < 0;
    }

    private static List<Long> parseToListOfNumbers(String line) {
        return Arrays.stream(line.split("\\s+"))
                .map(Long::valueOf)
                .toList();
    }

    private static NumberRange parseToNumRange(String line) {
        var input = parseToListOfNumbers(line);
        return new NumberRange(
                input.get(1),
                input.get(1) + input.get(2),
                input.get(0) - input.get(1));
    }

    private static String readLineAfterSkippingBlanks(BufferedReader br) throws IOException {
        var nextLine = br.readLine();
        while (nextLine != null && nextLine.trim().isEmpty()) {
            nextLine = br.readLine();
        }
        return nextLine;
    }


}