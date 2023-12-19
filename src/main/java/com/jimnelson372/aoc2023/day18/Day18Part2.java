package com.jimnelson372.aoc2023.day18;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Day18Part2 {


    record Cooridnate(long x, long y){}
    record Instruct(char inst, long distance) {}

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day18-puzzle-input.txt"))) {
            var directionsList = br.lines().toList();

            var instructsPart1 = directionsList.stream()
                    .map(inst -> {
                        var dir = inst.charAt(0);
                        var dist = Integer.valueOf(inst.substring(2,inst.indexOf(" ",2)));
                        return new Instruct(dir, dist);
                    }).toList();

            var instructs = directionsList.stream()
                    .map(inst -> {
                        var dir = "RDLU".charAt(inst.charAt(inst.length()-2)-'0');
                        var dist = Integer.parseInt(inst.substring(inst.length()-7, inst.length()-2),16);
                        return new Instruct(dir, dist);
                    }).toList();

            var perimeter = instructs.stream()
                    .map(i -> i.distance)
                    .reduce(0L, Long::sum);

            System.out.println("Perimeter is = " + perimeter);

            AtomicLong x = new AtomicLong(0);
            AtomicLong y = new AtomicLong(0);
            List<Cooridnate> coordinates = new java.util.ArrayList<>(instructs.stream()
                    .map(i -> {
                        var coord = switch (i.inst) {
                            case 'U' -> new Cooridnate(x.get(), y.addAndGet(-i.distance));
                            case 'D' -> new Cooridnate(x.get(), y.addAndGet(i.distance));
                            case 'L' -> new Cooridnate(x.addAndGet(-i.distance), y.get());
                            case 'R' -> new Cooridnate(x.addAndGet(i.distance), y.get());
                            default -> new Cooridnate(x.get(), y.get());
                        };
                        return coord;
                    })
                    .toList());

            System.out.println(coordinates.size());

            var shoelaceAreaCalc = IntStream.range(0,coordinates.size()-1)
                    .boxed()
                    .map(i -> {
                        var c1 = coordinates.get(i);
                        var c2 = coordinates.get(i+1);
                        long area = Math.subtractExact(Math.multiplyExact(c1.x,c2.y), Math.multiplyExact(c2.x,c1.y));
                        return area;
                    })
                    .reduce(0L,Long::sum) / 2;
            System.out.println("shoelaceAreaCalc = " + shoelaceAreaCalc);
            System.out.println("Pick's Theorem Ajustment = " + (perimeter/2+1));
            System.out.println("Shoelace+picks = " + (shoelaceAreaCalc+perimeter/2+1));

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }



}