package com.jimnelson372.aoc2023.day25;

import org.jgrapht.Graph;
import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Day25Part1 {

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day25-puzzle-input.txt"))) {

            solveDay25(br);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static void solveDay25(BufferedReader br) {
        var graph = buildGraphFromInput(br);

        var alg = new StoerWagnerMinimumCut<>(graph);
        var cut = alg.minCut();

        System.out.println(graph.vertexSet().size() + " components and " + graph.edgeSet().size() + " wires");
        System.out.println("Min cut vertices count = " + cut.size());

        int numberOfOtherVertices = graph.vertexSet().size() - cut.size();

        var answer = cut.size() * numberOfOtherVertices;
        System.out.println("Answer = " + answer);
    }

    private static Graph<String, DefaultEdge> buildGraphFromInput(BufferedReader br) {
        var graph = new DefaultUndirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        br.lines()
                .map(l -> l.split(":"))
                .forEach(cw -> {
                    var fromComponent = cw[0].trim();
                    graph.addVertex(fromComponent);
                    Arrays.stream(cw[1].trim()
                                          .split("\\s+"))
                            .forEach(toComp -> {
                                graph.addVertex(toComp);
                                graph.addEdge(fromComponent, toComp);
                            });
                });
        return graph;
    }
}
