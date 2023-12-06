package com.jimnelson372.aoc2023.day5;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;


public class Day5Part2 {

    record NumberRange(long fromStart, long fromEnd, long mapDiff) {}
    record SeedRange(long from, long limit) {}

    static List<ArrayList<NumberRange>> mapList = new ArrayList<>();

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day5-puzzle-input.txt"))) {
            //Parse Seeds Line to get our starting seed #s.
            String nextLine = br.readLine();
            List<Long> seedData = parseToListOfNumbers(nextLine.split(":\\s+")[1]);

            //System.out.println("Starting seed #s: " + seedData);
            System.out.println("Number of seeds we're exploring: " + seedData.size());

            // Convert the stream of numbers into a stream of pairs
            var pairs = IntStream.range(0, seedData.size()/2)
                    .map(i -> i * 2)
                    .mapToObj(i -> new SeedRange(seedData.get(i), seedData.get(i + 1))).toList();

            long total = 0L;
            for(int i=0; i<seedData.size()-1; i++) {
                total = total + seedData.get(i+1);
            }
            System.out.println("The number of seeds to test will explode to: " + total);

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
                // Be sure to sort the list for future binarySearches on it.
                toMap.sort(Comparator.comparingLong(p -> p.fromStart)); // sorting it to make finding one faster.
                mapList.add(toMap);

                if (nextLine == null) break;
            }

            long min = Long.MAX_VALUE;
            for(SeedRange pair : pairs) {
                System.out.println("beginning range: " + pair);

                for(long i=0; i<pair.limit; i++) {
                    var value = pair.from+i;
                    for(var m : mapList) {
                        value = doMapping(value, m); // if not found in a range, return the same value.
                    }
                    min = Math.min(value, min);
                }
            }

            //System.out.println("Resulting location count is: " + valuesToMap.size());

            // find the smallest location number of this final mapping and that's our solution!
            var result =min;
            System.out.println("Result (minimum location) = " + result);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("Time=" +(System.nanoTime() - startTime)/ 1_000_000_000 + "secs");
    }

    private static Long doMapping(long value, ArrayList<NumberRange> m) {
        var rg = Collections.binarySearch(m,value, (a,b) -> {
            if (a instanceof NumberRange n && b instanceof Long bl) {
                if (valueInNumberRange(bl, n)) return 0;  // if anywhere in the range, we'll consider it equal/found.
                if (bl < n.fromStart) return 1;
                if (bl >= n.fromEnd) return -1;
            }
            return 0;
        });
        return rg >=0 ? value+m.get(rg).mapDiff : value;
    }

    private static boolean valueInNumberRange(long value, NumberRange r) {
        return value >= r.fromStart && value < r.fromEnd;
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