package com.jimnelson372.aoc2023.day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Day12Part1 {

    record RowState1(String origPattern, List<String> patterns, List<Integer> numeric, long arrangements, boolean done){}

    private static RowState1 getInitialRowStateFromLine(String line) {
        var splitAtSpace = line.split("\\s");
        var patterns = Stream.of(splitAtSpace[0].split("\\.+")).filter(s -> !s.isEmpty()).toList();
        var numbers = Stream.of(splitAtSpace[1].split(",")).map(Integer::valueOf).toList();
        return new RowState1(splitAtSpace[0],patterns, numbers, 0, false);
    }

    private static RowState1 testSimplestCase(RowState1 rs) {
        var minSizePatterns = rs.patterns.stream().reduce(-1, (acc,p) -> acc + 1 + p.length(),(a,b)->0);
        var minSizeNumbers = rs.numeric.stream().reduce(-1, (acc,p) -> acc+p+1);
        if (minSizePatterns.equals(minSizeNumbers))
            return new RowState1(rs.origPattern,rs.patterns,rs.numeric,1,true);

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
            return new RowState1(rs.origPattern,rs.patterns, rs.numeric,1,true);
        }
        return rs;
    }




    private static RowState1 testFullStringPositioning(RowState1 rs) {
        if (rs.done) return rs;
        //String pattern = rs.origPattern.replaceAll("[.]","O") + "O";
        String pattern = rs.patterns.stream().reduce("", (acc, s) -> acc + s + "O");
        var arrangements = getArrangements(rs, pattern);

        return new RowState1(rs.origPattern,rs.patterns,rs.numeric,arrangements,true);
    }

    private static Long getArrangements(RowState1 rs, String pattern) {
        String regexPattern = pattern.replaceAll("\\?", "[#O]");
        var groupings = rs.numeric.stream()
                .map("#"::repeat)
                .map(s -> s + "O").toList();

        var minLengthOfGrouped = rs.numeric.stream().reduce(0, (acc, n) -> acc + n + 1);
        var extraSpace = pattern.length() - minLengthOfGrouped;
        var baseNumber = extraSpace+1;
        var gapNum = NumberGenerator.generateNumbers(groupings.size()+1,extraSpace);

        //System.out.println(pattern + " " + extraSpace + " : " + gapNum);
        //System.out.println(regexPattern);

        var arrangements = gapNum.stream()
                .map(gn -> {
                    //System.out.print("gap: " + gn + " -- ");
                    return IntStream.range(0, groupings.size()+1)
                            .boxed()
                            .reduce("",
                                    (acc,i) -> {
                                        long whichDigit = (long) Math.pow(baseNumber,i);
                                        long repCnt = (gn / whichDigit) % baseNumber;
                                        String repeat = "O".repeat((int) repCnt);
                                        return acc + repeat + ((i<groupings.size()) ? groupings.get(i) : "");
                                    }
                                    ,(a,b)->a);
                })
                 .map(s -> {

                     boolean matches = s.matches(regexPattern);
//                     if (matches)
//                        System.out.println(s + " : matches = " + matches);
                     return matches;
                 })
                 .reduce(0L,(acc,n) -> acc + (n ? 1L : 0L),(a,b) -> a);
        return arrangements;
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day12-puzzle-input.txt"))) {
            var res =
                    br.lines().map(Day12Part1::getInitialRowStateFromLine)
                    .map(Day12Part1::testSimplestCase)
                    .map(Day12Part1::testNextSimplestCase)
                    .map(Day12Part1::testFullStringPositioning)
                    //.filter(rs -> rs.arrangements == 1)
                      //      .map(rs -> rs.arrangements)
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

