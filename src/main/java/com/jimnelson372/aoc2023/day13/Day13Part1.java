package com.jimnelson372.aoc2023.day13;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day13Part1 {

    private static Integer confirmReflection(List<List<Integer>> list, int max, Integer low, Integer high) {
        var startingLow = low;
        if (low +1 != high)
            return 0;

        while(low > 1 && high <= max -1) {
            low--;
            high++;
            var found = false;
            for (List<Integer> item: list) {
                found = (item.contains(low) && item.contains(high));
                if (found) break;
            }
            if (!found) return 0;
        }
        return startingLow;
    }

    private static int findReflectionLine(List<List<Integer>> potentialLines, int size) {
        if (potentialLines.isEmpty()) {
            return 0;
        }

        return potentialLines.stream()
                .<List<Integer>>mapMulti((li, consumer) -> {
                    if (li.size() == 2)
                        consumer.accept(li);
                    else {
                        for (int i: li) {
                            if (li.contains(i + 1))
                                consumer.accept(List.of(i,i+1));
                        }
                    }
                })
                .filter(li -> li.get(0)+1==li.get(1))
                .map(pointToTest -> confirmReflection(potentialLines, size, pointToTest.get(0), pointToTest.get(1)))
                .reduce(0, Integer::max);
    }
    static int findLineOfHorizontalReflection(List<String> notes) {
        var hsize = notes.size();
        var halfway = hsize / 2;

        AtomicInteger count = new AtomicInteger(1);
        var res = notes.stream()
                .map(l -> new ImmutablePair<Integer,String>(count.getAndIncrement(),l))
                .collect(Collectors.groupingBy(p -> p.right, Collectors.mapping(p-> p.left,Collectors.toList() )))
                .values().stream()
                .filter(a-> a.size()>1)
                .toList();

        return findReflectionLine(res,hsize);
    }

    static int findLineOfVerticalReflection(List<String> notes) {
        var hsize = notes.size();
        var vsize = notes.get(0).length();
        var halfway = vsize / 2;

        //AtomicInteger count = new AtomicInteger(0);
        var res = IntStream.range(0,vsize)
                .boxed()
                .map(ndx -> {
                    Set<Integer> set = new HashSet<>();
                    for(var i = 0; i < hsize; i++) {
                        if (notes.get(i).charAt(ndx) == '#')
                            set.add(i);
                    }
                    return new ImmutablePair<Integer,Set<Integer>>(ndx+1,set);
                })
                .collect(Collectors.groupingBy(p -> p.right, Collectors.mapping(p-> p.left,Collectors.toList() )))
                .values().stream()
                .filter(a-> a.size()>1)
                .toList();

        return findReflectionLine(res,vsize);
    }

    static int findLineOfReflection(List<String> notes) {
        var reflectH = findLineOfHorizontalReflection(notes);
        var reflectV = findLineOfVerticalReflection(notes);

        if (reflectV > 0) return reflectV;

        if (reflectH > 0) return 100 * reflectH;

        return 0;
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day13-puzzle-input.txt"))) {
            var answer = br.lines().collect(
                    CollectorUtils.splittingByBlankLines())
                    .toList().stream()
                    .map(Day13Part1::findLineOfReflection)
                    .reduce(0,Integer::sum);

            System.out.println("Summary = " + answer);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

}