package com.jimnelson372.aoc2023.day3;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Day3Part1 {

    public static List<Integer> findPartNumbers(List<String> schematic) {
        List<Integer> parts = new ArrayList<>();
        int currentRow = 0;
        int numRows = schematic.size();

        // Regex to get the number strings on this line.
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);

        while(currentRow < numRows) {
            var currentLine = schematic.get(currentRow);
            var matcher = pattern.matcher(currentLine);

            // loop through numbers found on currentRow.

            while (matcher.find()) {
                var ds = matcher.group();
                var st = matcher.start();
                var end = matcher.end();
                //System.out.println(ds + ": " + st + ", " + end);

                // The number must be adjacent to a part in order to be a Part Number.
                //   So we'll first check the current line.
                //      then the next line.
                //      and if still not sure its a part, we'll check the prior line.
                if ((areAnyAdjacentPartsOnLine(currentLine, st, end)) ||
                    (currentRow+1<numRows && areAnyAdjacentPartsOnLine(schematic.get(currentRow+1),st,end)) ||
                    (currentRow != 0      && areAnyAdjacentPartsOnLine(schematic.get(currentRow-1), st,end))) {
                    parts.add(Integer.valueOf(ds));
                }
            }
            currentRow++;
        }

        return parts;
    }

    // If we find any of the part symbols on a line between the start and end of the number range
    //   Then it is a truly adjacent part.
    private static boolean areAnyAdjacentPartsOnLine(String currentLine, int start, int end) {
        int from = Math.max(start-1,0);
        int to = Math.min(end+1,currentLine.length());

        var sectionToExamine = currentLine.substring(from,to);

        // There is an adjacent part if there are any characters other than '.' and digits.
        return sectionToExamine.codePoints().anyMatch(c -> !Character.isDigit(c) && c != '.');
    }

    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day3-puzzle-input.txt"))) {
            // For this problem, I loaded the full file into memory to
            // use it as like an array of arrays.
            var schematic = br.lines().toList();

            var result = findPartNumbers(schematic).stream().reduce(0,Integer::sum);

            System.out.println("result = " + result);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }

}