package com.jimnelson372.aoc2023.day13;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day13Part1and2 {

    static <T> Set<T> remove(Set<T> set1, Set<T> set2)
    {
        Set<T> mutableSet = new HashSet<T>(set1);
        mutableSet.removeAll(set2);
        return mutableSet;
    }
    static <T> Set<T> union(Set<T> set1, Set<T> set2)
    {
        Set<T> mutableSet = new HashSet<T>(set1);
        mutableSet.addAll(set2);
        return mutableSet;
    }

    static <T>  int countOfDiffs(Set<T> set1, Set<T> set2) {
        var un = union(set1,set2);
        var un1 = remove(un,set1);
        var un2 = remove(un,set2);
        return un1.size() + un2.size();
    }

    static <T> boolean setsDifferByJust1(Set<T> set1, Set<T> set2) {
        if (Math.abs(set1.size() - set2.size()) > 1) return false;
        return countOfDiffs(set1,set2) == 1;
    }
    
    record ReflectLine(int lineNumber, int errorsAllowed) {}
    record IndexedRow(int lineNumber, Set<Integer> rowHashPositions) {};
    private static List<ReflectLine> findReflectionLine(List<Set<Integer>> notesSets, int size) {
        AtomicInteger count = new AtomicInteger(1);
        var groupings = notesSets.stream()
                .map(s -> new IndexedRow(count.getAndIncrement(),s))
                .collect(Collectors.groupingBy(p -> p.rowHashPositions, Collectors.mapping(p-> p.lineNumber,Collectors.toList() )))
                .values().stream()
                .toList();

        if (groupings.isEmpty()) {
            return List.of();
        }
        return IntStream.range(1,notesSets.size())
                .boxed()
                .map(i -> List.of(i,i+1))
                .filter(li -> li.get(0)+1==li.get(1))
                .map(pointToTest -> {
                    var low = pointToTest.get(0);
                    var high = pointToTest.get(1);
                    var startingLow = low;

                    int notFoundCnt = 0;
                    int missingLow = 0;

                    while(low > 0 && high <= size) {
                        var found = false;
                        for (List<Integer> item: groupings) {
                            found = (item.contains(low) && item.contains(high));
                            if (found) break;
                        }
                        if (!found) {
                            notFoundCnt++;
                            var first = notesSets.get(low-1);
                            var second = notesSets.get(high-1);
                            if (setsDifferByJust1(first,second))
                                missingLow = low;
                        }
                        low--;
                        high++;
                    }
                    if (notFoundCnt == 1 && missingLow != 0) {
                        return new ReflectLine(startingLow,1);
                    }
                    if (notFoundCnt>0) return new ReflectLine(0,0);
                    return new ReflectLine(startingLow,0);
                })
                .filter(ip -> ip.lineNumber != 0)
                .toList();
    }
    static List<ReflectLine> findLineOfHorizontalReflection(List<String> notes) {
        var hsize = notes.size();

        var notesSets = notes.stream()
                .map(l -> {
                    Set<Integer> set = new HashSet<>();
                    for (var i = 0; i < l.length(); i++) {
                        if (l.charAt(i) == '#')
                            set.add(i);
                    }
                    ;
                    return set;
                }).toList();

        return findReflectionLine(notesSets,hsize);
    }

    static List<ReflectLine> findLineOfVerticalReflection(List<String> notes) {
        var hsize = notes.size();
        var vsize = notes.get(0).length();

        //AtomicInteger count = new AtomicInteger(0);
        var notesSets = IntStream.range(0,vsize)
                .boxed()
                .map(ndx -> {
                    Set<Integer> set = new HashSet<>();
                    for (var i = 0; i < hsize; i++) {
                        if (notes.get(i).charAt(ndx) == '#')
                            set.add(i);
                    }
                    return set;
                }). toList();

        return findReflectionLine(notesSets,vsize);
    }

    static int findLineOfReflection(List<String> notes, int smudge) {
        var reflectH = findLineOfHorizontalReflection(notes);
        var reflectV = findLineOfVerticalReflection(notes);

        var horizScore = reflectH.stream()
                .map(i -> new ReflectLine(i.lineNumber *100, i.errorsAllowed))
                .toList();

        var list = new java.util.ArrayList<>(List.copyOf(horizScore));
        list.addAll(reflectV);
        if (list.size() > 2) System.out.println("ALERT");

        return list.stream()
                .filter(ip -> ip.errorsAllowed == smudge).map(ip->ip.lineNumber)
                .findFirst().orElse(0);
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day13-puzzle-input.txt"))) {
            var data = br.lines().collect(
                    CollectorUtils.splittingByBlankLines())
                    .toList();

            var part1answer = data.stream()
                    .map(l -> findLineOfReflection(l,0))
                    .reduce(0,Integer::sum);
            var part2answer = data.stream()
                    .map(l -> findLineOfReflection(l,1))
                    .reduce(0,Integer::sum);

            System.out.println("Result for Part1: " + part1answer);
            System.out.println("Result for Part2: " + part2answer);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

}