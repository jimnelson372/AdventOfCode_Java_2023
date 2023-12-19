package com.jimnelson372.aoc2023.day18;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Day18Part1 {


    record Dimensions(int startX, int startY, int width, int height) {}
    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day18-test.txt"))) {
            AtomicInteger line = new AtomicInteger();
            var directionsList = br.lines().toList();

            var dims = getMapDimenssions(directionsList);
            System.out.println(dims);

            char[][] map = new char[dims.height][dims.width];
            initializeMap(dims, map);
            //printMap(dims, map);

            int x = dims.startX;;
            int y = dims.startY;
            for(var inst : directionsList) {
                var dir = inst.charAt(0);
                var amount = Integer.valueOf(inst.substring(2,inst.indexOf(" ",2)));
                for(var i=0; i<amount; i++) {
                    map[y][x] = '#';
                    switch (dir) {
                        case 'U':
                            y--;
                            break;
                        case 'D':
                            y++;
                            break;
                        case 'L':
                            x--;
                            break;
                        case 'R':
                            x++;
                            break;
                        default:
                            break;
                    }
                }
            }
            printMap(dims, map);

            var solution = countArea(dims, map);

            System.out.println("Area = " + solution);


        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static int hashAboveBelow(Dimensions dims, char[][] map, int y, int x) {
        var above = (y > 0 && map[y-1][x] == '#') ? 0b10 : 0b00;
        int below = (y < dims.height-1 && map[y+1][x] == '#') ? 0b01 : 0b00;
        int andBits = above | below;
        return andBits;
    }
    private static void printMap(Dimensions dims, char[][] map) {

        for(int i = 0; i< dims.height; i++) {
            char priorChar = map[i][0];
            boolean in = false; //priorChar == '#';
            int hashThroughTest = hashAboveBelow(dims, map, i,0);
            for(int j = 0; j< dims.width; j++) {
                var c= map[i][j];
                int newHashAboveBelow = 0;
                if (priorChar != c) {
                    if (priorChar == '.' && c == '#')
                        newHashAboveBelow = hashAboveBelow(dims, map, i, j);
                    if (priorChar == '#' && c == '.') {
                        newHashAboveBelow = hashAboveBelow(dims, map, i, j - 1);
                        if ((hashThroughTest | newHashAboveBelow) == 0b11)
                            in = !in;
                    }
                    hashThroughTest = newHashAboveBelow;
                }
                priorChar = c;
                char pc = (c == '.') ? (in ? 'X' : '.') : '#';


                System.out.print(pc);
            }
            System.out.println();
        }
    }

    private static int countArea(Dimensions dims, char[][] map) {
        int cnt = 0;
        for(int i = 0; i< dims.height; i++) {
            char priorChar = map[i][0];
            boolean in = false; //priorChar == '#';
            int hashThroughTest = hashAboveBelow(dims, map, i,0);
            for(int j = 0; j< dims.width; j++) {
                var c= map[i][j];
                int newHashAboveBelow = 0;
                if (priorChar != c) {
                    if (priorChar == '.' && c == '#')
                        newHashAboveBelow = hashAboveBelow(dims, map, i, j);
                    if (priorChar == '#' && c == '.') {
                        newHashAboveBelow = hashAboveBelow(dims, map, i, j - 1);
                        if ((hashThroughTest | newHashAboveBelow) == 0b11)
                            in = !in;
                    }
                    hashThroughTest = newHashAboveBelow;
                }
                priorChar = c;
                char pc = (c == '.') ? (in ? 'X' : '.') : '#';

                if (pc != '.') cnt++;
            }
        }
        return cnt;
    }



    private static void initializeMap(Dimensions dims, char[][] map) {
        for(int i = 0; i< dims.height; i++) {
            for(int j = 0; j< dims.width; j++) {
                map[i][j] = '.';
            }
        }
    }

    private static Dimensions getMapDimenssions(List<String> directionsList) {
        int left = 0;
        int right = 0;
        int top = 0;
        int bottom = 0;
        int x = 0;
        int y = 0;
        for(var inst : directionsList) {
            var dir = inst.charAt(0);
            var amount = Integer.valueOf(inst.substring(2,inst.indexOf(" ",2)));
            switch(dir) {
                case 'U':  y -= amount; break;
                case 'D':  y += amount; break;
                case 'L': x -= amount; break;
                case 'R': x += amount; break;
                default:
                    break;
            }
            left = Math.min(left, x);
            right = Math.max(right, x);
            top = Math.min(top, y);
            bottom = Math.max(bottom,y);
        }
        var dims = new Dimensions(-left, -top,right-left+1, bottom-top+1);
        return dims;
    }
}