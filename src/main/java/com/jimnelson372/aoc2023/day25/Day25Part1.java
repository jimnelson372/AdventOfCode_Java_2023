package com.jimnelson372.aoc2023.day25;

import org.jgrapht.Graph;
import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day25Part1 {


    record Component(String name) {
    }

    record Wire(
            Component from,
            Component to) {
        Wire(Component from, Component to) {
            // ensure we have only one wire connecting the same two components
            // by ordering their positions.
            this.from = from.name.compareTo(to.name) < 0
                    ? from
                    : to;
            this.to = from.name.compareTo(to.name) < 0
                    ? to
                    : from;
        }
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day25-puzzle-input.txt"))) {
            var wireSet = getWireSet(br);
            Set<Component> componentSet = getComponentSet(wireSet);
            var graph = buildGraph(wireSet);

            var alg = new StoerWagnerMinimumCut<>(graph);
            var cut = alg.minCut();

            System.out.println(componentSet.size() + " components and " + wireSet.size() + " wires");
            System.out.println("Min cut vertices count = " + cut.size());

            var answer = cut.size() * (componentSet.size() - cut.size());
            System.out.println("Answer = " + answer);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static Set<Wire> getWireSet(BufferedReader br) {

        return br.lines()
                .map(l -> l.split(":"))
                .flatMap(cw -> {
                    var fromComponent = new Component(cw[0].trim());
                    return Arrays.stream(cw[1].trim()
                                                 .split("\\s+"))
                            .map(Component::new)
                            .flatMap(toComps -> Stream.of(new Wire(fromComponent, toComps),
                                                          new Wire(toComps, fromComponent)));
                })
                .collect(Collectors.toSet());
    }

    private static Set<Component> getComponentSet(Set<Wire> wireSet) {
        return wireSet.stream()
                .flatMap(w -> Stream.of(w.from, w.to))
                .collect(Collectors.toSet());
    }

    private static Graph<Component, DefaultEdge> buildGraph(Set<Wire> wireSet) {
        var graphBuilderType = GraphTypeBuilder.<Component, DefaultEdge>undirected()
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class)
                .weighted(false)
                .buildGraph();

        GraphBuilder<Component, DefaultEdge, Graph<Component, DefaultEdge>> baseGraphBuilder =
                new GraphBuilder<>(graphBuilderType);

        // only using the form of reduce with combiner to change type.
        // could not map to new type ahead of reduce.
        return wireSet.stream()
                .reduce(baseGraphBuilder,
                        (acc, w) -> acc.addEdge(w.from, w.to),
                        Day25Part1::combinerException) // can ignore this line.  Do not parallelize the stream.
                .build(); // the reduce gave us a builder with all the edges added.
    }

    static <T> T combinerException(T a, T b) {
        throw new RuntimeException("This combiner should not be called.  Don't run that stream in parallel.");
    }


}
