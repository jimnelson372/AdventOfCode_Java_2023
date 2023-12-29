package com.jimnelson372.aoc2023.day24;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


public class Day24Part2_JavaUsingSymja {

    record Position(long x, long y, long z) {
        /*
         * string format as expected by Symja language.
         */
        @Override
        public String toString() {
            return "{" + x +
                    "," + y +
                    "," + z +
                    '}';
        }
    }
    record Hail(
            Position position,
            Position velocity) {
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day24-puzzle-input.txt"))) {
            var hailList = getHailWithLimit(br, 3);  //Only need 3 points.

            var solution = solvePart2(hailList);

            System.out.println("Part2 solution: " + solution.orElse(-1L));

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }


    private static List<Hail> getHailWithLimit(BufferedReader br, int limit) {
        return br.lines()
                .map(l -> l.split("@"))
                .map(l2 -> {
                    var positions = Arrays.stream(l2)
                            .map(rawPos -> {
                                var pos = Arrays.stream(rawPos.split(","))
                                        .map(String::trim)
                                        .map(Long::valueOf)
                                        .toList();
                                return new Position(pos.get(0), pos.get(1), pos.get(2));
                            })
                            .toList();
                    return new Hail(positions.get(0), positions.get(1));
                })
                .limit(limit)
                .toList();
    }

    private static Optional<Long> solvePart2(List<Hail> hailHitList) {
        if (hailHitList.size()<3) return Optional.empty();
        // Create an expression evaluator
        ExprEvaluator eval = new ExprEvaluator();

        // generate the time base equations for each Hail stone
        //  there will be a different t for each, t1, t2, t3.
        // with the equations being I1, I2, I3.
        IntStream.range(1,4).boxed().forEach(i -> {
            var hp = eval.eval("p" + i + " = " + hailHitList.get(i-1).position);
            var hv = eval.eval("v" + i + " = " + hailHitList.get(i-1).velocity);
            var ii = eval.eval("I" + i + " = p" + i + " + v" + i + " * t" + i);
        });

        IExpr RP = eval.eval("RP = {xR1, xR2, xR3}");
        IExpr RV = eval.eval("RV = {vR1, vR2, vR3}");
        IExpr RatI1 = eval.eval("RatI1 = RP + RV * t1");
        IExpr RatI2 = eval.eval("RatI2 = RP + RV * t2");
        IExpr RatI3 = eval.eval("RatI3 = RP + RV * t3");

        // Solve the system of equations
        IExpr result = eval.eval(
                "r = Solve(" +
                        "{RatI1 == I1, RatI2 == I2, RatI3 == I3}, " +
                        "{xR1, xR2, xR3, vR1, vR2, vR3, t1, t2, t3})");

        // Extract the values of xR1, xR2, xR3 from the result
        IExpr x1 = eval.eval("{x1} = Lookup(r, xR1)");
        IExpr x2 = eval.eval("{x2} = Lookup(r, xR2)");
        IExpr x3 = eval.eval("{x3} = Lookup(r, xR3)");

        // Calculate the final result
        IExpr finalResult = eval.eval("x1 + x2 + x3");

        return Optional.of(finalResult.toLongDefault(0L));
    }
}