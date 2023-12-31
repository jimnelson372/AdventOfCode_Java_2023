package com.jimnelson372.aoc2023.day23;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Day23Part2c_jgrapht {

    static List<String> hikingMap = List.of();
    static int heightOfSpace = 0;
    static int widthOfSpace = 0;
    static Position startNode;
    static Position endNode;
    static DefaultDirectedWeightedGraph<Position, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    record Position(
            int x,
            int y) implements Comparable<Position> {

        Position up() {
            return new Position(x, y - 1);
        }

        Position down() {
            return new Position(x, y + 1);
        }

        Position left() {
            return new Position(x - 1, y);

        }

        Position right() {
            return new Position(x + 1, y);
        }

        @Override
        public int compareTo(@NotNull Position o) {
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
        public int compareTo(@NotNull EdgeSpan o) {
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
        public int compareTo(@NotNull Edge o) {
            return Comparator.comparing(Edge::nodeId1)
                    .thenComparing(Edge::nodeId2)
                    .compare(this, o);
        }
    }

    record ExploreInfo(
            Position position,
            Position blockage) {
    }

    record WalkState(
            int steps,
            ExploreInfo exploreInfo,
            boolean twoWay) {
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day23-puzzle-input.txt"))) {

            initializeMap(br);
            createTwoWayGraphFromMap();

            // None of the JGraphT shortest path algorithms would do the trick.  Asking for the
            // list of all simple paths and then finding the longest did work.
            var allDirectedPathsAlg = new AllDirectedPaths<Position, DefaultWeightedEdge>(graph);
            var pathList = allDirectedPathsAlg.getAllPaths(startNode,
                                                           endNode,
                                                           true,
                                                           graph.vertexSet()
                                                                   .size());
            var longest = pathList.stream()
                    .map(p -> (int) p.getWeight())
                    .max(Integer::compare)
                    .orElse(0);

            System.out.println("Longest Path " + longest);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static void initializeMap(BufferedReader br) {
        hikingMap = br.lines()
                .toList();
        heightOfSpace = hikingMap.size();
        widthOfSpace = hikingMap.get(0)
                .length();
    }

    private static void createTwoWayGraphFromMap() {
        startNode = new Position(1, 0);
        endNode = new Position(widthOfSpace - 2, heightOfSpace - 1);
        graph.addVertex(startNode);

        var unsettled = new ArrayDeque<ExploreInfo>();
        unsettled.add(new ExploreInfo(startNode, startNode));
        while (!unsettled.isEmpty()) {
            var pair = unsettled.poll();
            var fromPosition = pair.position;
            outDirections(fromPosition, pair.blockage) // all directions except backwards (the blockage)
                    .forEach(exploreDirection -> {
                        // Move forward along path, counting steps, so long as we only
                        // have one direction we can move.
                        int steps = 1;
                        boolean twoWay = true; // Assume two-way unless we hit a slippery slope.
                        Position toPosition = exploreDirection;
                        var blockedPosition = fromPosition;  // blockedPosition just says we can't go backwards.
                        var localDir = outDirections(toPosition, blockedPosition);
                        while (localDir.size() == 1) {
                            steps++;
                            //twoWay = twoWay && charOnMapAt(toPosition) == '.';
                            blockedPosition = toPosition;
                            toPosition = localDir.get(0);
                            localDir = outDirections(toPosition, blockedPosition);
                        }

                        // If the position we have reached is not already a known Node,
                        //   add it to the nodeSet and also add it to our unsettled queue
                        //   for further exploration.
                        if (!graph.containsVertex(toPosition)) {
                            //nodeSet.add(toPosition);
                            graph.addVertex(toPosition);
                            unsettled.add(new ExploreInfo(toPosition,
                                                          blockedPosition)); //again, backwards is blocked.
                        }
                        // Also add the weighted edge from the 'fromPosition' to this new 'toPosition'
                        var edge = graph.addEdge(fromPosition, toPosition);
                        if (edge != null) {
                            graph.setEdgeWeight(edge, (double) steps);
                        }
                        //if twoWay, add the edge in the opposite direction as well.
                        if (twoWay) {
                            edge = graph.addEdge(toPosition, fromPosition);
                            if (edge != null) {
                                graph.setEdgeWeight(edge, (double) steps);
                            }
                        }
                    });
        }
    }

    private static List<Position> outDirections(Position nextPosition, Position blockPosition) {
        return Stream.of(filterPosition(nextPosition.up(), blockPosition, "#"),
                         filterPosition(nextPosition.right(), blockPosition, "#"),
                         filterPosition(nextPosition.down(), blockPosition, "#"),
                         filterPosition(nextPosition.left(), blockPosition, "#"))
                .flatMap(Optional::stream)
                .toList();
    }

    static Optional<Position> filterPosition(Position newPosition,
                                             Position blockPosition,
                                             String barriers) {
        var newY = newPosition.y;
        var newX = newPosition.x;
        return ((newY < 0) || (newY == heightOfSpace)
                || (newX < 0) || (newX == widthOfSpace)
                || (newPosition.equals(blockPosition))
                || (barriers.indexOf(charOnMapAt(newPosition)) >= 0))
                ? Optional.empty()
                : Optional.of(newPosition);
    }

    static char charOnMapAt(Position position) {
        return hikingMap.get(position.y)
                .charAt(position.x);
    }

}