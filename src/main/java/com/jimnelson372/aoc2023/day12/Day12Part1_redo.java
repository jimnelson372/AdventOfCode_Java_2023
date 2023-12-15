package com.jimnelson372.aoc2023.day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;


public class Day12Part1_redo {

    record RowState1(String pattern, List<Integer> groups, long arrangements, boolean done){}

    private static RowState1 getInitialRowStateFromLine(String line) {
        var splitAtSpace = line.split("\\s");
        var numbers = Stream.of(splitAtSpace[1].split(","))
                .map(Integer::valueOf)
                .toList();
        return new RowState1(splitAtSpace[0],numbers, 0, false);
    }
    private static RowState1 countArrangements(RowState1 rs) {
        var numQMs = recursiveScanForArrangements(rs,0,0,0L);
        return new RowState1(rs.pattern, rs.groups, numQMs, true);
    }

    // Based on solution algorithm shared by Jonathan Paulson on his youtube page
    // https://www.youtube.com/watch?v=xTGkP2GNmbQ
    record ScanState(int patternNdx, int groupNdx, long curGroupCount) {}
    private static long recursiveScanForArrangements(RowState1 rs, int patternNdx, int groupNdx, long curGroupCount) {
        if (patternNdx == rs.pattern.length()) {  // base condition
            // if we've reached the end of the pattern, then we've only gotten a valid arrangement if
            //   1) We are not in the middle of counting a group and we've matched all the specified groups
            //   2) We are still counting, but the count matches the final specified group count.
            // Otherwise propagate up that we don't have an arrangement.
            return (curGroupCount == 0 && groupNdx == rs.groups.size())
                    || (groupNdx == rs.groups.size() - 1 && curGroupCount == rs.groups.get(groupNdx) )
                    ? 1
                    : 0;
        }
        // we are counting arrangements from index patternNdx to the end of the pattern.
        long sumCounts = 0;
        // see the value of the current pattern position and determine next move from that.
        var currChar = rs.pattern.charAt(patternNdx);
        for(char hashOrDot: List.of('.', '#')) {
            if (currChar == hashOrDot || currChar=='?') {
                if (hashOrDot == '.') {
                    if (curGroupCount == 0)
                        sumCounts += recursiveScanForArrangements(rs, patternNdx + 1, groupNdx, 0);
                    else if (curGroupCount >= 0
                            && groupNdx < rs.groups.size()
                            && curGroupCount == rs.groups.get(groupNdx))
                        sumCounts += recursiveScanForArrangements(rs, patternNdx + 1, groupNdx + 1, 0);
                } else {
                    sumCounts += recursiveScanForArrangements(rs, patternNdx + 1, groupNdx, curGroupCount + 1);
                }
            }
        }
        return sumCounts;
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day12-puzzle-input.txt"))) {
            var res =
                    br.lines().map(Day12Part1_redo::getInitialRowStateFromLine)
                    .map(Day12Part1_redo::countArrangements)
                    //.filter(rs -> rs.arrangements == 1)
                            //.map(rs -> rs.arrangements)
                    //.forEach(System.out::println);
                            //.toList();
                    .reduce(0L, (acc,n) -> (acc + n.arrangements), (a, b)->a);

            System.out.println("Sum of arrangements = " + res);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

}



