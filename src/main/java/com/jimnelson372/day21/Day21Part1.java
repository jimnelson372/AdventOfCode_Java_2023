package com.jimnelson372.day21;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public class Day21Part1 {


    record Position (int x, int y, int steps) {}

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day21-puzzle-input.txt"))) {
            var initialMap = new ArrayList<>(br.lines()
                    .map(l -> "#" + l + "#")
                    .toList());
            int width = initialMap.get(0).length();
            String st = "#".repeat(width);
            initialMap.add(0,st);
            initialMap.add(st);
            int height = initialMap.size();

            var garden = new char[height][width];

            Position start = new Position(0,0,0);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    char content = initialMap.get(y).charAt(x);
                    if (content == 'S') {
                        start = new Position(x,y,64);
                        garden[y][x] = '.';
                    } else
                        garden[y][x] = content;
                }
            }

            long count = recurse(garden, start);

            System.out.println("Count = " + count);

            //printMap(garden);
            System.out.println(start);
            //initialMap.forEach(System.out::println);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

    static Set<Position> seen = new HashSet<>();
    private static long recurse(char[][] garden, Position from) {
        if (seen.contains(from)) return 0;
        if (from.steps == 0) {

            var content = garden[from.y][from.x];
            if (content == 'O') return 0;
            //System.out.println(from);
            garden[from.y][from.x] = 'O';
            return 1;
        }
        seen.add(from);

        long count =  Stream.of(List.of(0,1), List.of(0,-1), List.of(1,0), List.of(-1,0))
                .map(dir -> {
                    int nextY = from.y+dir.get(0);
                    int nextX = from.x+dir.get(1);
                    char content = garden[nextY][nextX];
                    return switch(content) {
                        case '.','O' -> recurse(garden,new Position(nextX, nextY,from.steps-1));
                        case '#' -> 0L;
                        default -> 0L;
                    };
                })
                .reduce(0L,Long::sum) ;
        return count;
    }

    static void printMap(char[][] map) {
        var width = map[0].length;
        var height = map.length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(map[y][x]);
            }
            System.out.println();
        }
        System.out.println();
    }

}