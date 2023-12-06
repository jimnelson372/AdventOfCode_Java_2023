package com.jimnelson372.aoc2023.day6;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


// Note: The formula I used below: distance = holding * (time - holding)
//   is derived simply from the basic equations:
//      speed = holding  (measured in milliseconds)
// and:
//      distance = speed * (time - holding)
//
// Combining these we get:
//
//      distance = holding * (time - holding)
//
// Rearranging it into a quadratic equation:
//
//      time*holding - holding^2 - distance = 0
//
// Then taking the derivative:
//
//      time - 2*holding = 0;
//
// Then rearranging:
//
//      holding = time/2
//
// This reveals that the optimal holding period is half the given time.
// After solving it with just a simple loop of holding from 1 to the amount of time,
//   I optimized it by starting at the center and moving up until it no longer beat the record.
//   For odd time races, doubling this count is the answer.
//   For even time races, we have to add back 1 -- the middle.
public class Day6Part2 {

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day6-puzzle-input.txt"))) {
            long time = readListNumbers(br);
            long record = readListNumbers(br);
            System.out.println("Time allowed in race = " + time);
            System.out.println("Record in race = " + record);

            // Part 2 much easier than part 1.   And fast even if not optimized.
            var cntBetterThanRecord = 0;
            for(var holding = time/2 + 1; holding<time; holding++) {
                var potential = holding * (time - holding); // simple formula to get result for a given time of holding, h.
                if (potential > record)
                    cntBetterThanRecord++;
                else break;
            }
            System.out.println("Result is: " + (cntBetterThanRecord*2 + ((time % 2 == 0) ? 1 : 0)));

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

    private static Long readListNumbers(BufferedReader br) throws IOException {
        return Long.valueOf(br.readLine().split("(:)")[1].replaceAll(" ",""));

    }


}