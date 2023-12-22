package com.jimnelson372.aoc2023.day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

// Based on solution algorithm shared by Jonathan Paulson on his youtube page
// https://www.youtube.com/watch?v=xTGkP2GNmbQ

public class Day12Part1_redo {

    record RowState(
            String pattern,
            List<Integer> groups,
            long arrangements,
            boolean done) {

        private static RowState getInitialRowStateFromLine(String line) {
            var splitAtSpace = line.split("\\s");
            var numbers = Stream.of(splitAtSpace[1].split(","))
                                .map(Integer::valueOf)
                                .toList();
            return new RowState(splitAtSpace[0], numbers, 0, false);
        }

        private long recursiveScanForArrangements(int patternNdx, int groupNdx,
                                                  long curGroupCount) {
            if (patternNdx == pattern.length()) {  // base condition
                // if we've reached the end of the pattern, then we've only gotten a valid arrangement if
                //   1) We are not in the middle of counting a group and we've matched all the specified groups
                //   2) We are still counting, but the count matches the final specified group count.
                // Otherwise propagate up that we don't have an arrangement.
                return (curGroupCount == 0 && groupNdx == groups.size())
                        || (groupNdx == groups.size() - 1 && curGroupCount == groups.get(groupNdx))
                        ? 1
                        : 0;
            }
            // we are counting arrangements from index patternNdx to the end of the pattern.
            long sumCounts = 0;

            // see the value of the current pattern position and determine next move from that.
            var currChar = pattern.charAt(patternNdx);
            for (char hashOrDot : List.of('.', '#')) {
                if (currChar == hashOrDot || currChar == '?') {
                    if (hashOrDot == '.') {
                        if (curGroupCount == 0) {
                            sumCounts += recursiveScanForArrangements(patternNdx + 1, groupNdx, 0);
                        } else {
                            if (curGroupCount >= 0 && groupNdx < groups.size()
                                    && curGroupCount == groups.get(groupNdx)) {
                                sumCounts += recursiveScanForArrangements(patternNdx + 1, groupNdx + 1, 0);
                            }
                        }
                    } else {
                        sumCounts += recursiveScanForArrangements(patternNdx + 1, groupNdx, curGroupCount + 1);
                    }
                }
            }
            return sumCounts;
        }

        private long countArrangements() {
            return recursiveScanForArrangements(0, 0, 0L);
        }
    }


    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources")
                                    .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day12-puzzle-input.txt"))) {

            br.lines()
              .map(RowState::getInitialRowStateFromLine)
              .map(RowState::countArrangements)
              .reduce(Long::sum)
              .ifPresentOrElse(sum -> System.out.println("Sum of arrangements = " + sum),
                               () -> System.out.println("No result."));

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

}



