package com.jimnelson372.aoc2023.day2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

public class Day2Part1 {

    static HashMap<String, Integer> availableDice = new HashMap<>() {{
        put("red", 12);
        put("green",13);
        put("blue", 14);
    }};

    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day2-puzzle-input.txt"))) {
            var result = br.lines().map(game -> {
               var split1 = game.split("(:)");
               int gameNum = Integer.parseInt(split1[0].replace("Game ", ""));

               // I chose to count the number of impossible games, rather than just return a boolean
                // if any are impossible.
               var numImpossibleGames = Arrays.stream(split1[1].split("(;)")).map(set -> {
                   return Arrays.stream(set.split("(,)"))
                       .map(dice -> {
                           var countColor = dice.trim().split(" ");
                           int shownCount = Integer.parseInt(countColor[0]);
                           int avail = availableDice.get(countColor[1]);
                           return (shownCount > avail) ? 1 : 0;
                       }).reduce(0,Integer::sum);
               }).reduce(0, Integer::sum);

               return numImpossibleGames>0 ? 0 : gameNum;
            }).reduce(0,Integer::sum);

            // Give us the results.
            System.out.println("result = " + result);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }


}