package com.jimnelson372.aoc2023.day3;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

class Solver {

    private final Map<String,List<Integer>> partsMap = new HashMap<>();
    private final List<String> schematic;
    private boolean havePartInformation = false;

    public Solver(List<String> schematic) {
        this.schematic = schematic;
    }

    // This was the first part of the Day 3 challenge.
    //  But refactored in this file to do the calculation based on the partsMap information.
    public void sumOfPartNumbers() {
        if (!havePartInformation) findPartsAndPartNumbers();
        // With this partsMap collection, we can now also find the same values as in part 1,
        // Using flatmap to get all the part #s, then summing them together for the result.
        var sumPartNumbers = partsMap.values()  //All values in Map are the Part Numbers
                .stream().parallel()
                .flatMap(Collection::stream)
                .reduce(0,Integer::sum);

        System.out.println("Part1: Sum of Part Numbers = " + sumPartNumbers);
    }

    // This was the 2nd part of the Day 3 challenge.
    //   It needed the map of Parts and numbers, so I reorganized the first part effort
    //   to use the same model.
    public void sumOfGearRatios() {
        if (!havePartInformation) findPartsAndPartNumbers();
        // With the parts map, we can now filter for the "*" parts
        // and those which have more than one part #
        // so we can calculate the gear ratio (the product of those part #s)
        // and then sum those gear ratios together.
        var sumOfGearRatios = partsMap.entrySet()
                .stream().parallel()
                .filter(e -> e.getKey().startsWith("*"))
                .filter(e -> e.getValue().size()>1)
                .map(e -> e.getValue().stream().reduce(1,(acc,val) -> acc*val))
                .reduce(0, Integer::sum);

        System.out.println("Part2: Sum of Gear Ratios = " + sumOfGearRatios);
    }

    // function to identify the part numbers and assign them to the part to which they belong.
    // these will end up in the partsMap, keyed by the part id, which is the part type and location.
    private  void findPartsAndPartNumbers() {
        int currentRow = 0;
        int numRows = schematic.size();

        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);

        while(currentRow < numRows) {
            var currentLine = schematic.get(currentRow);
            var matcher = pattern.matcher(currentLine);

            // loop through numbers found on currentRow.
            while (matcher.find()) {
                var partNum = Integer.parseInt(matcher.group());
                var start = matcher.start();
                var end = matcher.end();

                // Associated Parts will be between start and end on the
                //   prior row, the current row, or the next row.
                assignNumberToAssociatedParts(currentRow-1, partNum, start,end);
                assignNumberToAssociatedParts(currentRow, partNum, start, end);
                assignNumberToAssociatedParts(currentRow+1,partNum, start,end);
            }
            currentRow++;
        }
        havePartInformation = true;
    }

    private void assignNumberToAssociatedParts(int row, int num, int start, int end) {
        if (row < 0 || row >= schematic.size()) return; // if the row doesn't exist, return.
        var currentLine = schematic.get(row);
        // Unfortunately, since this problem relies on positions and relative positions,
        // I had to use an approach where I had row numbers and column numbers.
        //  so I resorted to for loops rather than streams.

        // Ensure range is valid.
        int from = Math.max(start-1,0);
        int to = Math.min(end+1,currentLine.length());

        for(int col=from; col<to; col++) {
            var cp = currentLine.codePointAt(col);
            boolean isItAPart = !Character.isDigit(cp) && cp != '.';
            if (isItAPart) {
                var partKey = Character.toString(cp) + "_" + row + "_" + col;
                partsMap.computeIfAbsent(partKey, k -> new ArrayList<>())
                        .add(num);
            }
        }
    }
}

public class Day3Part2 {
    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day3-puzzle-input.txt"))) {
            // This version loads the full input into memory.
            // A more satisfying solution would work with 3 lines in memory at a time only.  Maybe another time.
            var schematic = br.lines().toList();
            Solver solver = new Solver(schematic);

            // Here is our call to solve part 1 with the loaded data structures.
            solver.sumOfPartNumbers();
            // Here is our call to solve part 2 with the loaded data structures.
            solver.sumOfGearRatios();

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }
}