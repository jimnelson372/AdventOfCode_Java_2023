package com.jimnelson372.aoc2023.day8;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

// Code that did help me find the correct answer to Part 2's puzzle,
// but based on very simplified assumptions about the cycles (see comments below).
// It's not a general solution.

class BorrowedUtils {
    // this Least Common Multiple method is from https://www.baeldung.com/java-least-common-multiple
    // It is fast enough for my purposes here.
    public static BigInteger lcm(BigInteger number1, BigInteger number2) {
        BigInteger gcd = number1.gcd(number2);
        BigInteger absProduct = number1.multiply(number2).abs();
        return absProduct.divide(gcd);
    }
}

public class Day8Part2_NonGeneralSolution_Exploration {

    record LeftRight(String left, String right) {
    }

    static BigInteger lcmOf(List<BigInteger> list) {
        return list.stream().reduce((acc, b) -> acc = BorrowedUtils.lcm(acc,b)).orElse(BigInteger.ZERO);
    }

    static HashMap<String, LeftRight> map = new HashMap<>();

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day8-puzzle-input.txt"))) {
            var turns = br.readLine();
            br.readLine();
            System.out.println("Turns cnt: " + turns.length());

            br.lines().forEach(hand -> {
                var split = hand.split("[\\s,=()]+");
                map.put(split[0], new LeftRight(split[1], split[2]));
            });

            var simpTurns = turns.codePoints()
                    .mapToObj(a -> Character.toString(a).equals("L")).toList();

            var multiStartLocations = map.keySet().stream().filter(k -> k.endsWith("A")).toList();

            // Like I said, this is an exploration program.
            //   This solution did work to complete the Day 8 Part 2 on Advent of Code, but it is very FRAGILE to this
            //   Data set, resting on the assumptions:
            //      The # of steps distance from the *A to its *Z is identical to the steps from *Z back to itself.
            //          And divergence from this would need to be accounted for.
            //      The cycles must occur at steps where the Turns repeat themselves, otherwise there may
            //          be multiple cycles.
            //   Otherwise, the use of lcm (Least Common Multiple) is not going to give a correct answer.
            //   I added some tests for these conditions, throwing an exception if they are not met.
            System.out.println("Starting locations: " + multiStartLocations);
            var steps = multiStartLocations.stream()
                    .map(location -> {
                        int count = 0;
                        int reachingAZCnt = 0;
                        int priorCountReachingZ = 0;
                        var currentLocation = location;
                        var sourceLocation = location;
                        boolean done = false;
                        while (!done) {
                            for (var turn : simpTurns) {
                                //System.out.println(location);
                                var nextLocations = map.get(currentLocation);
                                currentLocation = turn ? nextLocations.left : nextLocations.right;
                                count++;
                                if (currentLocation.endsWith("Z")) {
                                    int modTurn = count % simpTurns.size();

                                    var nextStep = simpTurns.get(modTurn);
                                    System.out.println("Location " + sourceLocation + " reached " + currentLocation + " in "
                                            + count + " steps, next going " + (nextStep ? "left" : "right")
                                            + " next position in turns list: " + modTurn);
                                    sourceLocation = currentLocation;

                                    if (++reachingAZCnt > 1) {
                                        //if (modTurn != 0) throw new RuntimeException("The steps between this
                                        // *A and *Z and *Z are not multiples of the provided steps.");
                                        // I only needed to see 1 cycle after reaching a *Z since the counts are
                                        // even multiples of the # of turns given.
                                        if (priorCountReachingZ != count)
                                            throw new RuntimeException("The Cycles from *A to *Z and from *Z back to itself are not the same.");

                                        System.out.println();
                                        done = true;
                                        break;
                                    } else {
                                        priorCountReachingZ = count;
                                        count = 0; // reset count if we're cycling again.
                                    }
                                };
                            }
                        }
                        return count;
                    })
                    .map(BigInteger::valueOf)
                    .toList();

                    System.out.println("---------------");
                    System.out.println("Cycles size of each path: " + steps);

                    System.out.println("The paths all simultaneously reach a *Z location at step: " + lcmOf(steps));
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("This program's algorithm will not work for the provided input.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }


}