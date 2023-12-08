package com.jimnelson372.aoc2023.day8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;



public class Day8Part1 {

    record LeftRight(String left, String right) {}

    static HashMap<String,LeftRight> map = new HashMap<>();

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day8-puzzle-input.txt"))) {
            var turns = br.readLine();
            br.readLine();
            System.out.println("Turns: " + turns);

            br.lines().forEach(hand -> {
                var split = hand.split("[\\s,=()]+");
                map.put(split[0],new LeftRight(split[1],split[2]));
            });
            var simpTurns = turns.codePoints()
                    .mapToObj(a -> Character.toString(a).equals("L")).toList();

            int count = 0;
            var location = "AAA";
            while(!location.equals("ZZZ")) {
                for(var turn : simpTurns) {
                    System.out.println(location);
                    var lr = map.get(location);
                    location = turn ? lr.left : lr.right;
                    count++;
                    if (location.equals("ZZZ")) break;

                }
                System.out.println(location);
            }
            System.out.println(count);


        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }



}