package com.jimnelson372.aoc2023.day1;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Day1Part2 {

    static class Helper {
        static int codePointToInteger(int codePoint) {
            return codePoint - '0';
        }

        // Simple function to calculate the Calibration in a map.
        //   takes first entry in 10s place and last entry in 1s place.
        static Integer calcCalibration(List<Integer> ns)  {
            return calcCalibration(ns.get(0), ns.get(ns.size() - 1));
        }

        static int calcCalibration(int lowDigit, int highDigit) {
            return lowDigit * 10 + highDigit;
        }
    }

    private static List<Integer> containedDigitsAsListInteger(String str) {
        return str.codePoints()
                .filter(Character::isDigit)
                .map(Helper::codePointToInteger)
                .boxed()
                .toList();
    }

    private static final List<String> digitNames= List.of("one","two","three","four","five","six","seven","eight","nine");
    private static int getDigitValueFromName(String word) {
        return digitNames.indexOf(word) + 1;
    }

    //Convert String to List of the Integers
    private static Integer getCalibrationValueBySearch(String s) {
        if (s.isEmpty()) return 0;

        int lowVal = 0;
        int highVal = 0;
        int low = s.length();
        int high= -1;

        var digits =  containedDigitsAsListInteger(s);

        // get the low and high values and positions of these digits.
        if (!digits.isEmpty()) {
            lowVal = digits.get(0);
            highVal = digits.get(digits.size() - 1);
            low = s.indexOf('0' + lowVal);
            high = s.lastIndexOf('0' + highVal);
        }

        // now check each digitName to see if it is lower or higher
        //  than the current detected low and high.
        //  and update the low or high if so.
        for (String word: digitNames) {
            int where = s.indexOf(word);
            if (where >= 0 && where < low) {
                    lowVal = getDigitValueFromName(word);
                    low = where;
                }

            where = s.lastIndexOf(word);
            if (where >=0 && where > high) {
                    highVal = getDigitValueFromName(word);
                    high = where;
                }
        }

        return Helper.calcCalibration(lowVal, highVal);
    }


    //  This is the 2nd version, using regular expressions to find the numeric words and numbers
    //   even if the words overlap, making the solution almost as easy as in Part 1, in that
    //   the digits all end up in a List of Integers, and we simply take the first and last
    //   to calculate the Calibration.
    private static Integer getCalibrationValueByExp(String line) {
        // Define and compile the regex pattern
        String regex = "(?=(one|two|three|four|five|six|seven|eight|nine|0|1|2|3|4|5|6|7|8|9))";
        Pattern pattern = Pattern.compile(regex);

        // Start with empty list that we'll fill in.
        var digitList = new ArrayList<Integer>();
        var matcher = pattern.matcher(line);
        while (matcher.find()){
            var ds = matcher.group(1);
            // if single digit, it must be numeric so just parse it ot Int.
            // if longer, it must be a digit name, so find its value by the index in our list.
            var num = ds.length()==1 ? Integer.parseInt(ds) : getDigitValueFromName(ds);

            digitList.add(num);
        }
        // Now that we have the list of digits on the line, we can combine the first and last
        // digit into a "Calibration" value.
        //return digitList.get(0)*10+ digitList.get(digitList.size()-1);
        return Helper.calcCalibration(digitList);
    }

//removed attempts.
//        // Use splitAsStream to split the input by the pattern and print each match
//        var digitList = pattern.matcher(line).results()
//                .map(MatchResult::group)
//                .map((ds) -> ds.length()==1 ? Integer.valueOf(ds) : digitNames.indexOf(ds)+1)
//                .toList();

    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day1-puzzle-input.txt"))) {
            Stream<String> lines = br.lines();
            var result = lines
                    .map(Day1Part2::getCalibrationValueBySearch)
                    //.map(Day1Part2::getCalibrationValueByExp)
                    .reduce(0, Integer::sum);
            // Give us the results.
            System.out.println("Sum of calibration values = " + result);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }
}