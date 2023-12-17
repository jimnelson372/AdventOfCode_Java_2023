package com.jimnelson372.aoc2023.day15;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.LongStream;


public class Day15Part1 {
    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day15-puzzle-input.txt"))) {
            var sequence = br.readLine();


            String finalSequence = sequence;
            var result = LongStream.range(0, 1)
                    .mapMulti((c,consumer) -> {
                        var curNdx = 0;
                        var comma = finalSequence.indexOf(",",curNdx);
                        while(comma > 0 && curNdx < finalSequence.length()) {
                            var curCode = 0;
                            while (curNdx < comma) {
                                int ascii = finalSequence.charAt(curNdx);
                                curCode += ascii;
                                curCode = curCode * 17;
                                curCode = curCode % 256;
                                curNdx++;
                            }

                            consumer.accept(curCode);
                            if (curNdx >= finalSequence.length()) return;
                            comma = finalSequence.indexOf(",",++curNdx);
                            if (comma < 0) comma = finalSequence.length();
                        }
                    })
                    .reduce(0,Long::sum);

            System.out.println(result);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }



}