package com.jimnelson372.aoc2023.day24;

import javax.vecmath.Vector3d;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;


public class Day24Part1 {

    static Vector3d Origin = new Vector3d();
    static double minPosition = 200000000000000.0;
    static double maxPosition = 400000000000000.0;

    record HailMatchup(
            int a,
            int b) {
        HailMatchup(int a, int b) {
            this.a = Math.min(a, b);
            this.b = Math.max(a, b);
        }
    }

    record Intersect(
            Vector3d at,
            boolean future) {
    }

    record Hail(
            Vector3d position,
            Vector3d velocity) {
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day24-puzzle-input.txt"))) {
            var hailList = getHail(br);
            System.out.println("Hail count = " + hailList.size());

            //testVectorIntercept();

            var seen = new HashSet<>();
            var hailListSize = hailList.size();
            AtomicInteger cnt = new AtomicInteger();
            for (int i = 0; i < hailListSize; i++) {
                var hail1 = hailList.get(i);
                for (int j = 0; j < hailListSize; j++) {
                    var matchup = new HailMatchup(i, j);
                    if (i == j || seen.contains(matchup)) continue;
                    var hail2 = hailList.get(j);

//                    System.out.println("Hailstone A" + i + ": " + hail1);
//                    System.out.println("Hailstone B" + j + ": " + hail2);
                    var intersect = getIntersectXY(hail1.position, hail2.position, hail1.velocity, hail2.velocity);
                    intersect.ifPresent(o -> {

                        if (o.at.x >= minPosition && o.at.y >= minPosition && o.at.x <= maxPosition && o.at.y <= maxPosition && o.future) {
                            cnt.getAndIncrement();
                        }
                    });
//                    intersect.ifPresentOrElse(o -> {
//
//                        if (o.at.x >= minPosition && o.at.y >= minPosition && o.at.x <= maxPosition && o.at.y <=
//                        maxPosition && o.future) {
//                            cnt.getAndIncrement();
//                        }
//                        System.out.print("Paths will cross at " + o.at);
//                        if (o.at.x >= minPosition && o.at.y >= minPosition && o.at.x <= maxPosition && o.at.y <=
//                        maxPosition) {
//                             System.out.print(" inside text area");
//                        } else {
//                            System.out.print(" outside the test area");
//                        }
//                         System.out.println(" in the " + ((o.future)
//                                ? "future."
//                                : "past."));
//                    }, () -> System.out.println("paths are parallel"));
//                                        System.out.println();
                    seen.add(matchup);
                }

            }
            System.out.println("Answer is " + cnt);


            //hailList.forEach(System.out::println);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static List<Hail> getHail(BufferedReader br) {
        return br.lines()
                .map(l -> l.split("@"))
                .map(l2 -> {
                    var positions = Arrays.stream(l2)
                            .map(rawPos -> {
                                var pos = Arrays.stream(rawPos.split(","))
                                        .map(String::trim)
                                        .map(Long::valueOf)
                                        .toList();
                                return new Vector3d(pos.get(0), pos.get(1), pos.get(2));
                            })
                            .toList();
                    return new Hail(positions.get(0), positions.get(1));
                })
                .toList();
    }

    private static Optional<Intersect> getIntersectXY(Vector3d a1, Vector3d a2, Vector3d b1, Vector3d b2) {
        var testParallel = new Vector3d();
        testParallel.cross(b1, b2);
        if (testParallel.equals(Origin)) {
            return Optional.empty();
        }

        var positionDiff = new Vector3d();
        positionDiff.sub(a1, a2);
        var intersectLeftOnB1 = new Vector3d();
        intersectLeftOnB1.cross(b1, positionDiff);
        var intersectLeftOnB2 = new Vector3d();
        intersectLeftOnB2.cross(b2, positionDiff);
        //System.out.println(intersectLeftOnB1);

        var intersectRight = new Vector3d();
        intersectRight.cross(b1, b2);
        //System.out.println(intersectRight);
        var lambdaB1Vec = new Vector3d(intersectLeftOnB1.x / intersectRight.x,
                                       intersectLeftOnB1.y / intersectRight.y,
                                       intersectLeftOnB1.z / intersectRight.z);
        var lambdaB1 = lambdaB1Vec.z;

        var lambdaB2Vec = new Vector3d(intersectLeftOnB2.x / intersectRight.x,
                                       intersectLeftOnB2.y / intersectRight.y,
                                       intersectLeftOnB2.z / intersectRight.z);
        var lambdaB2 = lambdaB2Vec.z;
        //System.out.println(lambdaB);
        var result = new Vector3d(b2);
        result.scaleAdd(lambdaB1, a2);
        var result2 = new Vector3d(b1);
        result2.scaleAdd(lambdaB2, a1);

        //System.out.println(result);
        return Optional.of(new Intersect(result, lambdaB1 > 0 && lambdaB2 > 0));
    }

    private static void testVectorIntercept() {
        var b1 = new Vector3d(4, 2, 2);
        var b2 = new Vector3d(3, 2, -1);
//            b1.scale(-1);
//            b2.scale(-1);
        var cross = new Vector3d();
        cross.cross(b1, b2);
        //System.out.println(cross);

        var a2 = new Vector3d(-5, 0, 5);
        var a1 = new Vector3d(-5 + 2, 2, 5 - 4);

        var intersectAt = getIntersectXY(a1, a2, b1, b2);
        //System.out.println(intersectAt);

        var A = new Vector3d(19, 13, 30);
        var B = new Vector3d(18, 19, 22);
        var d1 = new Vector3d(-2, 1, -2);
        var d2 = new Vector3d(-1, -1, -2);

        var testIntersect = getIntersectXY(A, B, d1, d2);
        //System.out.println(testIntersect);
    }


}