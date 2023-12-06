package com.jimnelson372.aoc2023.day5;



import org.apache.commons.lang3.Range;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;


// Even with my stream() based solution in Day5Part2b, and even with parallel() activated,
//   it still took between 3 and 4 minutes to process the actual puzzle data.
//   That was fine, given I was just trying to solve the problem while learning new Java features,
//   but reading others discuss split second speeds, I wanted to understand what else I could do.
//
//   Without looking at code, I did understand that others were not processing every seed in a range,
//    but were rather processing the ranges, splitting them and adjusting them as they transitioned
//    on the way from seed to location.
//
//    I was able to implement this too.   I did use org.apache.commons.lang3.Range class, just to have
//    pre-made intersect, contains, overlaps operations, but I didn't use other libraries beyond the
//    Java packages.
//
//    With these changes, it now processes the puzzle data in less than 50ms.  And that's with un optimized
//    Java code running on a 2015 iMac.   Algorithms and process structures make all the difference.
//    (Especially considering many who were sharing blazing fast speeds were using Javascript.)
//    In this version, I turned of parallel() as it seemed to make the processing a tiny bit slower.

public class Day5Part2c {


    record NumberRange(Range<Long> range, long mapDiff) {}
//    record SeedRange(long from, long limit) {}

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day5-puzzle-input.txt"))) {
            //Parse Seeds Line to get our starting seed #s.
            String nextLine = br.readLine();

            // Get the seed #s we're going to work on.
            List<Long> seedData = parseToListOfNumbers(nextLine.split(":\\s+")[1]);
            System.out.println("Number of seed ranges we're exploring: " + seedData.size());

            // Get the mapping information.
            var mapList = parseMappingStagesSeesToLocations(br);

            // This is the mainStream for part 2.
            // Convert the stream of numbers into a stream of pairs
            //   then flatMap over those pairs to produce each sead # we're going to examine.
            var mainStream = IntStream.range(0, seedData.size()/2)
                    //.parallel()   // does actually nearly halve this heavy processor operation.
                    .map(i -> i * 2)
                    .mapToObj(i -> Range.between(seedData.get(i), seedData.get(i) + seedData.get(i + 1)));


            // add the mapping of the location finding steps to the main steam.
            for(var m : mapList) {
                mainStream = mainStream.flatMap(val -> doMapping(val,m).stream());
            }
            // Finally,
            // find the smallest location number of this final mapping and that's our solution!
            // This min operation causes the stream to be processed to get the final result.
            var result = mainStream
                    .map(Range::getMinimum)
                    .min(Long::compareTo).orElse(0L);
            System.out.println("Result (minimum location) = " + result);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("Time=" +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
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
            toMap.sort(Comparator.comparingLong(p -> p.range.getMinimum())); // sorting it to make finding one faster.

            mapList.add(toMap);

            if (nextLine == null) break;
        }
        return mapList;
    }

    private static List<Range<Long>> doMapping(Range<Long> fromRange, ArrayList<NumberRange> m) {
        var searchNdx = Collections.binarySearch(m,fromRange.getMinimum(), (a,b) -> {
            if (a instanceof NumberRange n && b instanceof Long bl) {
                return -n.range.elementCompareTo(bl);
            }
            return 0;
        });

//        if (searchNdx < 0)
//            System.out.println("-insert at: " + searchNdx + " or " + (-(searchNdx+1)) + " with size() at " + m.size());
//        else {
//            System.out.println("+insert at: " + searchNdx + " with size() at " + m.size());
//        }

        var posNdx = searchNdx>=0 ? searchNdx : -(searchNdx-1);

        var newPairsList = new ArrayList<Range<Long>>();

        if (posNdx >= m.size() || !fromRange.isOverlappedBy(m.get(posNdx).range)) {
            newPairsList.add(fromRange); // pass it on, as it didn't intersect any.
            return newPairsList;
        }

        var workingRange = fromRange;
        var match = m.get(posNdx);

        if (searchNdx<0) {
                //we've already handled the non-overlap case, so we must overlap one.
                // first put the non-overlapped section into the newPairsList.
                newPairsList.add(Range.between(workingRange.getMinimum(),m.get(posNdx).range.getMinimum()-1));
                // Then pass on the intersected section for further processing.
                workingRange = m.get(posNdx).range.intersectionWith(workingRange);
        }

        if (match.range.equals(workingRange) || match.range.containsRange(workingRange)) {
            var mapDiff = match.mapDiff;  // even though the full range will move on, it must be adjusted
            var newMin = workingRange.getMinimum() + mapDiff;
            var newMax = workingRange.getMaximum() + mapDiff;
            newPairsList.add(Range.between(newMin,newMax));
        } else {
            for(int i = posNdx; i<m.size() && m.get(i).range.isOverlappedBy(workingRange); i++) {
                match = m.get(i);
                var intersection = match.range.intersectionWith(workingRange);
                var mapDiff = match.mapDiff;  // even though the full range will move on, it must be adjusted
                var newMin = intersection.getMinimum() + mapDiff;
                var newMax = intersection.getMaximum() + mapDiff;
                newPairsList.add(Range.between(newMin,newMax));
            }
        }

        return newPairsList;
        //return rg >=0 && rg < m.size() ? value+m.get(rg).mapDiff : value;
    }

//    private static boolean valueInNumberRange(long value, NumberRange r) {
//        return r.range.contains(value);
//    }

    private static List<Long> parseToListOfNumbers(String line) {
        return Arrays.stream(line.split("\\s+"))
                                .map(Long::valueOf)
                                .toList();
    }

    private static NumberRange parseToNumRange(String line) {
        var input = parseToListOfNumbers(line);
        return new NumberRange(
                Range.between(input.get(1),
                input.get(1) + input.get(2) -1),
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