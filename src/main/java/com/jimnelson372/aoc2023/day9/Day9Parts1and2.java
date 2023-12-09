package com.jimnelson372.aoc2023.day9;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Day9Parts1and2 {


    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day9-puzzle-input.txt"))) {
            var lines = br.lines().toList();

            var part1Result = solveThePuzzle(lines, Day9Parts1and2::getNextInSequence);
            System.out.println("Result part 1 = " + part1Result);

            var part2Result = solveThePuzzle(lines, Day9Parts1and2::getPriorInSequence);
            System.out.println("Result part 2 = " + part2Result);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

    private static Integer solveThePuzzle(List<String> lines, Function<List<Integer>, Integer> solveTheSequence) {
        return lines.stream().map(sequence -> {
            var seqList = readListNumbers(sequence);

            return solveTheSequence.apply(seqList);
        }).reduce(0,Integer::sum);
    }

    private static int getNextInSequence(List<Integer> seqList) {
        //System.out.println(seqList);

        // recursive base condition
        if (allDiffsAreZero(seqList))
            return 0;

        return seqList.getLast() + getNextInSequence(calcDiffSequence(seqList));
    }
    private static int getPriorInSequence(List<Integer> seqList) {
        //System.out.println(seqList);

        // recursive base condition
        if (allDiffsAreZero(seqList))
            return 0;

        return seqList.getFirst() - getPriorInSequence(calcDiffSequence(seqList));
    }

    private static boolean allDiffsAreZero(List<Integer> seqList) {
        // It was important to check that all values in the row were 0, but I ONLY sum if the first and last are
        //  easily seen to be 0.  No need to sum all lines to see if they are 0.
        //  BTW, 1 line did end in a 0, but was in reverse order.  So I initially got the wrong answer until
        //   I checked both sides for 0 first.
        return (seqList.getLast() == 0
                && seqList.getFirst() == 0
                && seqList.stream().reduce(0, Integer::sum) == 0);
    }

    private static List<Integer> calcDiffSequence(List<Integer> diffs) {
        return IntStream.range(0, diffs.size() - 1)
                 .mapToObj(ndx -> diffs.get(ndx+1) - diffs.get(ndx))
                 .toList();
    }

    private static List<Integer> readListNumbers(String line)  {
        return Arrays.stream(line.split("\\s+"))
                .map(Integer::valueOf)
                .toList();
    }

}