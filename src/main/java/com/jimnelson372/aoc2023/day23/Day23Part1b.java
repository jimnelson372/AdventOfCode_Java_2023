package com.jimnelson372.aoc2023.day23;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Day23Part1b {

    static int heightOfSpace = 0;
    static int widthOfSpace = 0;
    static Position startNode;
    static Position endNode;
    static Map<Position, Position> nodeSet = new HashMap<>();
    static Set<Edge> edgeSet = new HashSet<>();

    record Position(
            int x,
            int y) implements Comparable<Position> {

        Optional<Position> up(List<String> map, Position blockPosition, String barriers) {
            var newY = y - 1;
            if (newY < 0) return Optional.empty();
            Position newPosition = new Position(x, newY);
            if (newPosition.equals(blockPosition)) return Optional.empty();
            if (barriers.indexOf(charAt(map, newPosition)) >= 0) return Optional.empty();
            return Optional.of(newPosition);
        }

        Optional<Position> down(List<String> map, Position blockPosition, String barriers) {
            var newY = y + 1;
            if (newY == map.size()) return Optional.empty();
            Position newPosition = new Position(x, newY);
            if (newPosition.equals(blockPosition)) return Optional.empty();
            if (barriers.indexOf(charAt(map, newPosition)) >= 0) return Optional.empty();
            return Optional.of(newPosition);
        }

        Optional<Position> left(List<String> map, Position blockPosition, String barriers) {
            var newX = x - 1;
            if (newX < 0) return Optional.empty();
            Position newPosition = new Position(newX, y);
            if (newPosition.equals(blockPosition)) return Optional.empty();
            if (barriers.indexOf(charAt(map, newPosition)) >= 0) return Optional.empty();
            return Optional.of(newPosition);
        }

        Optional<Position> right(List<String> map, Position blockPosition, String barriers) {
            var newX = x + 1;
            if (newX == map.get(0)
                    .length()) {
                return Optional.empty();
            }
            Position newPosition = new Position(newX, y);
            if (newPosition.equals(blockPosition)) return Optional.empty();
            if (barriers.indexOf(charAt(map, newPosition)) >= 0) return Optional.empty();
            return Optional.of(newPosition);
        }

        @Override
        public int compareTo(Position o) {
            return Comparator.comparing(Position::x)
                    .thenComparing(Position::y)
                    .compare(this, o);
        }
    }

    record EdgeSpan(
            Position to,
            Long dist,
            boolean hasCycle) implements Comparable<EdgeSpan> {
        @Override
        public int compareTo(EdgeSpan o) {
            return Comparator.comparing(EdgeSpan::to)
                    .thenComparing(EdgeSpan::dist)
                    .compare(this, o);
        }
    }

    record Edge(
            Position nodeId1,
            Position nodeId2,
            long length) implements Comparable<Edge> {
        @Override
        public int compareTo(Edge o) {
            return Comparator.comparing(Edge::nodeId1)
                    .thenComparing(Edge::nodeId2)
                    .compare(this, o);
        }
    }

    record PosAndBlock(
            Position pos,
            Position block) {
    }

    record WalkState(
            int steps,
            PosAndBlock posBlock,
            boolean twoWay) {
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day23-puzzle-input.txt"))) {

            initializeMap(br);
            var longest = findLongestPath();
            System.out.println("Longest Path " + longest);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static void initializeMap(BufferedReader br) {
        var hikingMap = br.lines()
                .toList();
        heightOfSpace = hikingMap.size();
        widthOfSpace = hikingMap.get(0)
                .length();

        startNode = new Position(1, 0);
        endNode = new Position(widthOfSpace - 2, heightOfSpace - 1);
        nodeSet.put(startNode, startNode);
        var unsettled = new ArrayDeque<PosAndBlock>();
        unsettled.add(new PosAndBlock(startNode, startNode));
        while (!unsettled.isEmpty()) {
            var pair = unsettled.poll();
            var fromPosition = pair.pos;
            var blockingPosition = pair.block;
            var directions = outDirections(fromPosition, blockingPosition, hikingMap);
            var endNodes = directions.stream()
                    .map(d -> {
                        int cnt = 1;
                        boolean twoWay = true;
                        Position localToPosition = d;
                        var localBlocking = fromPosition;
                        var localDir = outDirections(localToPosition, localBlocking, hikingMap);
                        while (localDir.size() == 1) {
                            cnt++;
                            twoWay = twoWay && charAt(hikingMap, localToPosition) == '.';
                            localBlocking = localToPosition;
                            localToPosition = localDir.get(0);
                            localDir = outDirections(localToPosition, localBlocking, hikingMap);
                        }
                        return new WalkState(cnt,
                                             new PosAndBlock(
                                                     localToPosition,
                                                     localBlocking), twoWay);
                    })
                    .toList();
            endNodes.stream()
                    .filter(ip -> nodeSet.containsKey(ip.posBlock.pos))
                    .forEach(ip -> {
                        if (ip.posBlock.pos.equals(endNode)) {
                            addToEdgeMap(fromPosition, endNode, ip.steps, ip.twoWay);
                        } else {
                            addToEdgeMap(fromPosition, ip.posBlock.pos, ip.steps, ip.twoWay);
                        }
                    });
            endNodes.stream()
                    .filter(ip -> !nodeSet.containsKey(ip.posBlock.pos))
                    .forEach(ip -> {
                        nodeSet.put(ip.posBlock.pos, ip.posBlock.pos);

                        addToEdgeMap(fromPosition, ip.posBlock.pos, ip.steps, ip.twoWay);
                        unsettled.add(new PosAndBlock(ip.posBlock.pos, ip.posBlock.block));
                    });

        }

//        nodeSet.values()
//                .stream()
//                .sorted()
//                .forEach(System.out::println);
//        System.out.println("-------");
//        edgeSet.stream()
//                .sorted()
//                .forEach(System.out::println);
//        edgeSet.stream()
//                .filter(ip -> ip.length < 0)
//                .sorted()
//                .forEach(ip -> System.out.println("cycle on " + ip));
    }

    static Stream<EdgeSpan> getAdjacent(Position p) {
        return edgeSet.stream()
                .filter(e -> e.nodeId1.equals(p))
                .map(e -> new EdgeSpan(e.nodeId2,
                                       (e.length < 0)
                                               ? -e.length
                                               : e.length,
                                       (e.length < 0)));
    }

    private static List<Position> outDirections(Position nextPosition, Position blockPosition, List<String> hikingMap) {
        return Stream.of(nextPosition.up(hikingMap, blockPosition, "v#"),
                         nextPosition.right(hikingMap, blockPosition, "<#"),
                         nextPosition.down(hikingMap, blockPosition, "^#"),
                         nextPosition.left(hikingMap, blockPosition, ">#"))
                .flatMap(Optional::stream)
                .toList();
    }

    static void addToEdgeMap(Position p1, Position p2, int distance, boolean twoWay) {
        edgeSet.add(new Edge(p1, p2, distance));
        if (twoWay) {
            edgeSet.add(new Edge(p2, p1, distance));
        }
    }

    static char charAt(List<String> map, Position position) {
        return map.get(position.y)
                .charAt(position.x);
    }

    private static long findLongestPathRecur(Position from, Set<Position> seen, long distance) {
        if (from.equals(endNode)) {
            return distance;
        }
        var res = getAdjacent(from)
                .filter(pto -> !seen.contains(pto.to) || pto.to.equals(endNode))
                .map(es -> {
                    seen.add(from);
                    long latestDist = findLongestPathRecur(es.to, seen, es.dist);
                    seen.remove(from);
                    return latestDist;
                })
                .max(Long::compare)
                .orElse(Long.MIN_VALUE);
        return res + distance;
    }

    private static long findLongestPath() {
        return findLongestPathRecur(startNode, new HashSet<>(), 0);
    }

}