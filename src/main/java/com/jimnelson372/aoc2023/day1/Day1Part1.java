package com.jimnelson372.aoc2023.day1;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class Day1Part1 {

    static class Helper {
        static int codePointToInteger(int codePoint) {
            return codePoint - '0';
        }

        // Simple function to calculate the Calibration in a map.
        //   takes first entry in 10s place and last entry in 1s place.
        static Integer calcCalibration(List<Integer> ns)  {
            return ns.get(0) * 10 + ns.get(ns.size() - 1);
        }

        static List<Integer> listOfDigitsInString(String str) {
            return str.codePoints()
                    .filter(Character::isDigit)
                    .map(Helper::codePointToInteger)
                    .boxed()
                    .toList();
        }
    }

    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day1-puzzle-input.txt"))) {
            Stream<String> lines = br.lines();
            var result = lines
                .map(Helper::listOfDigitsInString)
                .map(Helper::calcCalibration)
                .reduce(0, Integer::sum);

            // Give us the results.
            System.out.println("Sum of calibration values = " + result);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }




}