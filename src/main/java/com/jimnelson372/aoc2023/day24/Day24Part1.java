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
import java.util.stream.IntStream;


public class Day24Part1 {

    static Vector3d Origin = new Vector3d();
    static double minPosition = 200000000000000.0;
    static double maxPosition = 400000000000000.0;

    record HailPairing(
            int a,
            int b) {
        HailPairing(int a, int b) {
            this.a = Math.min(a, b);
            this.b = Math.max(a, b);
        }
    }

    record LineIntersect(
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

            var answer = countLineIntersectionsOfEachPairOfHail(hailList);
            System.out.println("Answer is " + answer);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static int countLineIntersectionsOfEachPairOfHail(List<Hail> hailList) {
        var seen = new HashSet<>();
        return IntStream.range(0, hailList.size())
                .boxed()
                .map(i -> {
                    var hail1 = hailList.get(i); // get the hail we'll compare with the rest.
                    // Now loop through all the others to see if their 2d paths cross.
                    return (int) IntStream.range(0, hailList.size())
                            .boxed()
                            .filter(j -> {
                                if (i.equals(j)) return false;

                                var pairing = new HailPairing(i, j);
                                if (seen.contains(pairing)) return false;
                                seen.add(pairing);

                                // This is the hail to test against hail1.
                                var hail2 = hailList.get(j);

                                var intersect =
                                        get2DLineIntersectionPoint(hail1.position,
                                                                   hail1.velocity,
                                                                   hail2.position,
                                                                   hail2.velocity);
                                if (intersect.isPresent()) {
                                    var o = intersect.get();
                                    // we know we're not parallel at this point.  Now to see
                                    // if the intersection is within the range and in the future.
                                    return o.at.x >= minPosition && o.at.y >= minPosition
                                            && o.at.x <= maxPosition && o.at.y <= maxPosition
                                            && o.future;
                                }
                                return false;
                            })
                            .count(); // we count the number of matches per inner stream
                })
                .reduce(0, Integer::sum); // we then sum them to get our count.
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

    private static Optional<LineIntersect> get2DLineIntersectionPoint(Vector3d p1,
                                                                      Vector3d v1, Vector3d p2,
                                                                      Vector3d v2) {
        if (isParallel(v1, v2)) {
            return Optional.empty(); // No Line Intersect will occur on parallel vectors.
        }

        // We now test to see if we have an intersection by calculating t2, the time of intersection:
        //      t2 == (v1 X (p1 - p2)) / (v1 X v2)
        // We can do the division, since for this 2d problem, the cross products are numbers, not vectors.
        // I found this formula in a 2012 presentation by Prof. N. Harnew of the University of Oxford, MT.
        // It is derived from:
        //      p1 + t1*v1 == p2 + t2*v2
        // Prof Harnew did a cross product on both sides to get the equation:
        //      v1 X (p1 - p2) == t2 * (v1 X v2)
        // And I then solved for t2

        // This is: p1 - p2
        var positionDiff = new Vector3d();
        positionDiff.sub(p1, p2);

        // This is: v1 X v2
        var velocityCrossProduct = new Vector3d();
        velocityCrossProduct.cross(v1, v2);

        // This is: v1 X (p1 - p2)
        var v1CrossPositionDiffs = new Vector3d();
        v1CrossPositionDiffs.cross(v1, positionDiff);
        // As I said, the cross products of the 2d art of the vectors are numbers, found in the z component
        //  of this 3d library cross product.
        var t2 = v1CrossPositionDiffs.z / velocityCrossProduct.z; // z's hold cross products on 2 dim problem.

        // Using t2, we can now find the point where the two vector line cross in 2d space:
        //   which is at p2 + t2*v2
        var a2CrossesA1At = new Vector3d(v2);
        a2CrossesA1At.scaleAdd(t2, p2);

        // Though we have our point of crossing, we have to also calculate t1
        // in order to test if the point of crossing happens in the past or future.
        var v2CrossPositionDiffs = new Vector3d();
        v2CrossPositionDiffs.cross(v2, positionDiff);
        var t1 = v2CrossPositionDiffs.z / velocityCrossProduct.z;

        // It's only in the future if both t1 and t2 are positive.
        boolean isIntersectionInTheFuture = t2 > 0 && t1 > 0;

        // We return it as Optional since the Parallel condition tested above
        //  will not find a crossing point.
        return Optional.of(new LineIntersect(a2CrossesA1At, isIntersectionInTheFuture));
    }

    private static boolean isParallel(Vector3d vec1, Vector3d vec2) {
        var testParallel = new Vector3d();
        testParallel.cross(vec1, vec2);
        return testParallel.equals(Origin); // a 3d cross product on parallel vectors will result in 0,0,0.
    }

    // Was only needed during development.
//    private static void printVerificationOutputForTestData(Optional<LineIntersect> intersect) {
//        intersect.ifPresentOrElse(o -> {
//            System.out.print("Paths will cross at " + o.at);
//            if (o.at.x >= minPosition && o.at.y >= minPosition && o.at.x <= maxPosition && o.at.y <=
//                    maxPosition) {
//                System.out.print(" inside text area");
//            } else {
//                System.out.print(" outside the test area");
//            }
//            System.out.println(" in the " + ((o.future)
//                    ? "future."
//                    : "past."));
//        }, () -> System.out.println("paths are parallel"));
//    }
}