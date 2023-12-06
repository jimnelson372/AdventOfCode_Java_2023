package com.jimnelson372.aoc2023.day4;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day4Part1 {

    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day4-puzzle-input.txt"))) {
            Stream<String> lines = br.lines();
              var result = getScorePerCard(lines);

              // Give us the results.
            System.out.println("result = " + result);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }

    private static Integer getScorePerCard(Stream<String> lines) {
        return lines.map(card -> {
           var sections = List.of(card.split("[:|]\\s+"));

           var winning = getWinningNumberSet(sections.get(1));
           var ourNumbers = getWinningNumberSet(sections.get(2));

            Set<Integer> intersect = intersectionSet(winning, ourNumbers);

            return intersect.stream().reduce(0, (acc, val) -> acc == 0 ? 1 : acc+acc);
        }).reduce(0, Integer::sum);
    }

    // This didn't need to be generic, but it didn't seem to depend on the type, so I made it generic.
    // I'm surprised it's not part of the collection library already.
    private static <T> Set<T> intersectionSet(Set<T> set1, Set<T> set2) {
        Set<T> intersect = new HashSet<>(set1);
        intersect.retainAll(set2);
        return intersect;
    }

    private static Set<Integer> getWinningNumberSet(String winningPart) {
        return Arrays.stream(winningPart.split("\\s+")).map(Integer::valueOf).collect(Collectors.toSet());
    }


}