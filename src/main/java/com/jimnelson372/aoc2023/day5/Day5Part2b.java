package com.jimnelson372.aoc2023.day5;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;


// This is the version I first attempted to write, a more functional streaming version.
//  But it seemed to take forever to run, so I slowly converted it back to standard
//  Java loops.
//  After I figured out the bottleneck was actually in quickly finding the range in the maps
//    using filter and findFirst, I found it was optimal to use a binarySearch on that
//    operation.   With that fixed, I covered everything back to stream maps, and I was
//    even able to parallelize it...which cut the run time almost in half.

public class Day5Part2b {

    record NumberRange(long fromStart, long fromEnd, long mapDiff) {}
    record SeedRange(long from, long limit) {}

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day5-puzzle-input.txt"))) {
            //Parse Seeds Line to get our starting seed #s.
            String nextLine = br.readLine();

            // Get the seed #s we're going to work on.
            List<Long> seedData = parseToListOfNumbers(nextLine.split(":\\s+")[1]);
            System.out.println("Number of seed ranges we're exploring: " + seedData.size());
            System.out.println("Be patient. This will take about 3 minutes....");

            // Get the mapping information.
            var mapList = parseMappingStagesSeesToLocations(br);

//            // This is the mainStream base for part 1.
//            var mainStream = seedData.stream().parallel();

            // This is the mainStream for part 2.
            // Convert the stream of numbers into a stream of pairs
            //   then flatMap over those pairs to produce each sead # we're going to examine.
            var mainStream = IntStream.range(0, seedData.size()/2)
                    .parallel()   // does actually nearly halve this heavy processor operation.
                    .map(i -> i * 2)
                    .mapToObj(i -> new SeedRange(seedData.get(i), seedData.get(i + 1)))
                    .flatMap(pair ->
                            Stream.iterate(pair.from, n -> n + 1).limit(pair.limit));



            // add the mapping of the location finding steps to the main steam.
            for(var m : mapList) {
                mainStream = mainStream.map(val -> doMapping(val,m)); // This is still adding map() calls to the stream above.
            }
            // Finally,
            // find the smallest location number of this final mapping and that's our solution!
            // This min operation causes the stream to be processed to get the final result.
            var result = mainStream.min(Long::compareTo).orElse(0L);
            System.out.println("Result (minimum location) = " + result);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("Time=" +(System.nanoTime() - startTime)/ 1_000_000_000 + "secs");
    }

    private static List<ArrayList<NumberRange>> parseMappingStagesSeesToLocations(BufferedReader br) throws IOException {
        List<ArrayList<NumberRange>> mapList = new ArrayList<>();
        String nextLine;
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
        return mapList;
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