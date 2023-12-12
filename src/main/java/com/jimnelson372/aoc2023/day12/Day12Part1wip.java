package com.jimnelson372.aoc2023.day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Day12Part1wip {

    record RowState1(List<String> patterns, List<Integer> numeric, long arrangements, boolean done){}

    private static RowState1 getInitialRowStateFromLine(String line) {
        var splitAtSpace = line.split("\\s");
        var patterns = Stream.of(splitAtSpace[0].split("\\.+")).filter(s -> !s.isEmpty()).toList();
        var numbers = Stream.of(splitAtSpace[1].split(",")).map(Integer::valueOf).toList();
        return new RowState1(patterns, numbers, 0, false);
    }

    private static RowState1 testSimplestCase(RowState1 rs) {
        var minSizePatterns = rs.patterns.stream().reduce(-1, (acc,p) -> acc + 1 + p.length(),(a,b)->0);
        var minSizeNumbers = rs.numeric.stream().reduce(-1, (acc,p) -> acc+p+1);
        if (minSizePatterns.equals(minSizeNumbers))
            return new RowState1(rs.patterns,rs.numeric,1,true);

        return rs;
    }
    private static RowState1 testNextSimplestCase(RowState1 rs) {
        boolean condition2 = rs.numeric.size() == rs.patterns.size() &&
                IntStream.range(0, rs.numeric.size())
                        .boxed()
                        .reduce(true,
                                (acc, i) -> acc && rs.patterns.get(i).length() == rs.numeric.get(i),
                                (a, b) -> false);

        if (condition2) {
            return new RowState1(rs.patterns, rs.numeric,1,true);
        }
        return rs;
    }
//
//    record strRange(Integer num, String testing, int startPos, int maxPos) {}
//    private static RowState1 testFullStringPositioning(RowState1 rs) {
//        String pattern = rs.patterns.stream().reduce("", (acc,s) -> acc + s + ".");
//
//        int pos = 0;
//        for(var num : rs.numeric) {
//            var hasDot = pattern.substring(0,num).indexOf(".") >= 0;
//
//        return rs;
//    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day12-puzzle-input.txt"))) {
            br.lines().map(Day12Part1wip::getInitialRowStateFromLine)
                    .map(Day12Part1wip::testSimplestCase)
                    .map(Day12Part1wip::testNextSimplestCase)
                    .filter(rs -> !rs.done)
                    .forEach(System.out::println);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

}

