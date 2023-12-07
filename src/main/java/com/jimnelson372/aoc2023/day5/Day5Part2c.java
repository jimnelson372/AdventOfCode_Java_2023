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

    record MappingRangeInfo(Range<Long> range, long mapDiff) {}
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
            var listOfMappings = parseMappingStagesSeesToLocations(br);

            // This is the mainStream for part 2.
            // Convert the stream of numbers into a stream of pairs
            //   then flatMap over those pairs to produce each sead # we're going to examine.
            var mainStream = IntStream.range(0, seedData.size()/2)
                    .map(i -> i * 2)
                    .mapToObj(i -> Range.between(seedData.get(i), seedData.get(i) + seedData.get(i + 1)));

            // add the mapping of the location finding steps to the main steam.
            //  so they will be a pipeline processing ranges to ranges.
            for(var mappingInfo : listOfMappings) {
                // flatMap is used since the number of ranges going into doMappingOfRanges
                // may differ from the number coming out.  A List is generated, needing to be flattened.
                mainStream = mainStream.flatMap(fromRange -> doMappingOfRanges(fromRange,mappingInfo).stream());
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

    private static List<ArrayList<MappingRangeInfo>> parseMappingStagesSeesToLocations(BufferedReader br) throws IOException {
        List<ArrayList<MappingRangeInfo>> mapList = new ArrayList<>();
        String nextLine;
        // For Each of the Maps
        //    parse and organize the ranges for lookup
        nextLine = readLineAfterSkippingBlanks(br);

        while (nextLine != null) {
            //System.out.println("Mapping: " + nextLine);
            nextLine = readLineAfterSkippingBlanks(br);

            // Build Mapping To list, sorted by ranges.
            var toMap = new ArrayList<MappingRangeInfo>();
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

    private static List<Range<Long>> doMappingOfRanges(Range<Long> fromRange, ArrayList<MappingRangeInfo> mappingList) {
        // Use binary search to locate which MappingRangeInfo to start from for our fromRange intersect tests.
        //  it will use the fromRange minimum value for the testing if below, within, or above the mapping range.
        var searchNdx = Collections.binarySearch(mappingList,fromRange.getMinimum(), (low,high) -> {
            if (low instanceof MappingRangeInfo validLower && high instanceof Long validHigher) {
                // note that we reverse the compare result here,
                // as the elementCompareTo compares validHigher to itself, rather than itself to validHigher
                return -validLower.range.elementCompareTo(validHigher);
            }
            return 0;
        });

        var posNdx = searchNdx>=0 ? searchNdx : -(searchNdx+1);

        // Condition where our fromRange doesn't overlap any of the mapping ranges.
        // So pass it back as is.
        if (posNdx >= mappingList.size() || !fromRange.isOverlappedBy(mappingList.get(posNdx).range)) {
            return List.of(fromRange);
        }

        // From here forward we may have multiple ranges, so
        // we'll add any we need to this list.
        var toRangeList = new ArrayList<Range<Long>>();

        if (searchNdx<0) {
            //we've already handled the non-overlap case, so we know this range is a partial overlap.
            // first put the non-overlapped section into the toRangeList.
            toRangeList.add(Range.between(fromRange.getMinimum(),mappingList.get(posNdx).range.getMinimum()-1));
            // we can leave the workingRange as is, since it will be intersected as needed.
        }

        // we'll keep intersecting with further map number ranges, until we don't have an overlap.
        for(int i = posNdx; i<mappingList.size() && mappingList.get(i).range.isOverlappedBy(fromRange); i++) {
            var currentMappingInfo = mappingList.get(i);
            var intersection = currentMappingInfo.range.intersectionWith(fromRange);

            // even though the full range will move on, it must be adjusted by the mapDiff amount.
            var mapDiff = currentMappingInfo.mapDiff;
            var newMin = intersection.getMinimum() + mapDiff;
            var newMax = intersection.getMaximum() + mapDiff;

            toRangeList.add(Range.between(newMin,newMax));
        }

        // finally, if our fromRange extends beyond the last mapping range, we need that range
        // to be added as well.
        Long maximumOfTheMappingRanges = mappingList.get(mappingList.size() - 1).range.getMaximum();
        if (maximumOfTheMappingRanges < fromRange.getMaximum())
            toRangeList.add(Range.between(maximumOfTheMappingRanges+1,fromRange.getMaximum()));


        return toRangeList;
    }

    private static List<Long> parseToListOfNumbers(String line) {
        return Arrays.stream(line.split("\\s+"))
                                .map(Long::valueOf)
                                .toList();
    }

    private static MappingRangeInfo parseToNumRange(String line) {
        var input = parseToListOfNumbers(line);
        return new MappingRangeInfo(
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