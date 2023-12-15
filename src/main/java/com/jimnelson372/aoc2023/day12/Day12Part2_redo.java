package com.jimnelson372.aoc2023.day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


public class Day12Part2_redo {

    record RowState(String pattern, List<Integer> groups, long arrangements, boolean done){}

    private static RowState getInitialRowStateFromLine(String line) {
        var splitAtSpace = line.split("\\s");
        String springs = (splitAtSpace[0] + "?").repeat(4) + splitAtSpace[0];
        String countBadSprings = (splitAtSpace[1] +",").repeat(4) + splitAtSpace[1];
//        String springs = splitAtSpace[0];
//        String countBadSprings = splitAtSpace[1];

        var numbers = Stream.of(countBadSprings.split(","))
                .map(Integer::valueOf)
                .toList();
        return new RowState(springs,numbers, 0, false);
    }
    private static RowState countArrangements(RowState rs) {
        var numQMs = recursiveScanForArrangements(rs, new ScanState(0,0,0L));
        dynamicProcessingMap.clear();
        return new RowState(rs.pattern, rs.groups, numQMs, true);
    }

    // Greatly modeled on solution algorithm shared by Jonathan Paulson on his youtube page
    // https://www.youtube.com/watch?v=xTGkP2GNmbQ
    // This really helped me to see that I was approaching the problem incorrectly,
    //  Trying to figure out ways to match the Group Numbers to the pattern,
    //    rather than matching the mattern to the group numbers
    //  Plus, it was nice to see how dynamic programming was easy to add to his recursive
    //    algorithm to drastically improve performance.  I have not been using recursion
    //    much, so this was a nice example to urge me in that direction.
    //
    record ScanState(int patternNdx, int groupNdx, long hashMarkCount) {}
    static Map<ScanState,Long> dynamicProcessingMap = new HashMap<>();
    enum NextStep { Nothing, ContinueOnBetweenHashes, MoveToNextGroup, KeepCountingHashes }
    private static long recursiveScanForArrangements(RowState rs, ScanState state) {
        var dm = dynamicProcessingMap.getOrDefault(state,-1L);
        if (dm >= 0) return dm;

        int patternNdx = state.patternNdx;
        long currentHashCount = state.hashMarkCount;
        int groupNdx = state.groupNdx;

        if (patternNdx == rs.pattern.length()) {  // base condition
            // if we've reached the end of the pattern, then we've only gotten a valid arrangement if
            //   1) We are not in the middle of counting a group and we've matched all the specified groups
            //   2) We are still counting, but the count matches the final specified group count.
            // Otherwise propagate up that we don't have an arrangement.
            return (currentHashCount == 0 && groupNdx == rs.groups.size())
                    || (groupNdx == rs.groups.size() - 1
                            && currentHashCount == rs.groups.get(groupNdx) )
                    ? 1
                    : 0;
        }

        // Each recursive pass through this method needs to sum up the counts from
        //    two different possible recursive branches.
        //    since for each '?' in the pattern, it will perform
        //      bptj the '#' operation and the '.' operation.
        long sumBranchedArrangementHashMarkCounts = 0;

        // This approach to looping over the values that ? can be was a nice lesson from
        //  seeing Jonathan Paulson's solution.
        var currChar = rs.pattern.charAt(patternNdx);

        for(char hashOrDot: List.of('.', '#')) {

            // Determine what our next step is given currChar and state.
            NextStep nextStep = NextStep.Nothing;
            if (currChar == hashOrDot || currChar=='?')
                nextStep = (hashOrDot == '.')
                        ? (currentHashCount == 0)
                            ? NextStep.ContinueOnBetweenHashes
                            : (groupNdx < rs.groups.size() && currentHashCount == rs.groups.get(groupNdx))
                                ? NextStep.MoveToNextGroup
                                : NextStep.Nothing
                        : NextStep.KeepCountingHashes;

            // Perform next step, if any, adding valid arrangements to our sum.
            // I move these calls out of the logic of the previous step to try out enums amd switche expressions,
            //  and to make the nature of these calls more clear in the code.
            sumBranchedArrangementHashMarkCounts += switch (nextStep) {
                case ContinueOnBetweenHashes -> recursiveScanForArrangements(rs,
                        new ScanState(patternNdx + 1, groupNdx, 0));
                case MoveToNextGroup -> recursiveScanForArrangements(rs,
                        new ScanState(patternNdx + 1, groupNdx + 1, 0));
                case KeepCountingHashes -> recursiveScanForArrangements(rs,
                        new ScanState(patternNdx + 1, groupNdx, currentHashCount + 1));
                case Nothing -> 0;
            };

        }
        // Record this state's outcome, in case it happens again.
        dynamicProcessingMap.put(state, sumBranchedArrangementHashMarkCounts);
        return sumBranchedArrangementHashMarkCounts;
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day12-puzzle-input.txt"))) {
            var res =
                    br.lines().map(Day12Part2_redo::getInitialRowStateFromLine)
                    .map(Day12Part2_redo::countArrangements)
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



