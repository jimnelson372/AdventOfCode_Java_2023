package com.jimnelson372.aoc2023.day6;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;


public class Day6Part1 {

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day6-puzzle-input.txt"))) {
            //Parse Seeds Line to get our starting seed #s.
            var times = readListNumbers(br);
            var distances = readListNumbers(br);
            System.out.println("Time allowed per race = " + times);
            System.out.println("Records in each race = " +distances);



            var result = IntStream.range(0,times.size())
                    .boxed()
                    .map(i -> {
                        var time = times.get(i);
                        var record = distances.get(i);
                        var cntBetterThanRecord = 0;
                        for(var hold=time/2+1;hold<time;hold++) {
                            var potential = hold * (time-hold);
                            if (potential > record) cntBetterThanRecord++;
                            else break;
                        }
                        return (cntBetterThanRecord)*2 + ((time % 2 == 0) ? 1 : 0);
                    }).reduce(1, (acc, l) -> acc*l);

            System.out.println("The result of the puzzle is = " + result);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

    private static List<Long> readListNumbers(BufferedReader br) throws IOException {
        return Arrays.stream(br.readLine().split("(:)")[1].trim().split("\\s+"))
                .map(Long::valueOf).toList();
    }


}