package com.jimnelson372.aoc2023.day2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;


public class Day2Part2 {

    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day2-puzzle-input.txt"))) {
            var result = br.lines().map(game -> {
               var gameSets = game.split(":")[1];
               //var gameNum = Integer.valueOf(split1[0].replace("Game ",""));

               var gameRequiredCounts = Arrays.stream(gameSets.split(";"))
                       // gather all counts,colors pairs for all sets
                       // of this game into a single stream
                       .flatMap(set -> Arrays.stream(set.split(","))
                                        .map(cubes -> List.of(cubes.trim().split(" ")))
                       )
                       // Group them by color and identify the max count for that color.
                       .collect(groupingBy(
                                    cc -> cc.get(1),
                                    maxBy(comparingInt(cc2 -> Integer.parseInt(cc2.get(0))))
                               )
                       );

               // clean up the gameRequiredCounts to be List<Integer> for each game.
               return gameRequiredCounts.values().stream().flatMap(Optional::stream)
                       .map(d -> Integer.valueOf(d.get(0)))
                       // calculate the power for each game by multiplying the required counts for each color
                       .reduce(1,(acc,cnt) -> acc * cnt);

            }).reduce(0,Integer::sum); // sum the power of these games.

            // Give us the results.
            System.out.println("result = " + result);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }


}