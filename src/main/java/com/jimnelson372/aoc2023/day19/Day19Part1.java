package com.jimnelson372.aoc2023.day19;

import com.jimnelson372.aoc2023.day13.CollectorUtils;

import javax.swing.text.html.Option;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day19Part1 {

    record Part (Map<Character,Long> ratings) {
        public Long ratingsScore() {
            return ratings.values().stream().reduce(0L, (acc, r) -> acc + r);
        }
    }
    record Rule(char rating, char oper, long value, String next) {

        Optional<String> eval(Part part) {
            var valueToTest = part.ratings.get(rating);
            var passes = switch(oper) {
                case '<' -> valueToTest < value;
                case '>' -> valueToTest > value;
                default -> false;
            };
            return passes ? Optional.of(next) : Optional.empty();
        }
    }

    record RuleSet(List<Rule> rules, String defaultNext) {

        String eval(Part part) {
            var result = rules.stream()
                    .map(r -> r.eval(part))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

            return result.orElse(defaultNext);
        }
    }

    record ParsingWorkFlowInfo(String name, RuleSet set) {}


    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day19-puzzle-input.txt"))) {
            var data = br.lines()
                    .collect(CollectorUtils.splittingByBlankLines())
                    .toList();


            var workflows = getWorkflows(data.get(0));
            var parts = getParts(data.get(1));

            var results = parts.stream()
                    .map(part -> {
                                var currentWF = "in";
                                while (!List.of("A", "R").contains(currentWF)) {
                                    var inWF = workflows.get(currentWF);
                                    currentWF = inWF.eval(part);
                                };
                                return (Objects.equals(currentWF, "A"))
                                        ? part.ratingsScore()
                                        : 0L;
                            })
                            .reduce(0L,Long::sum);

              System.out.println("Results = " + results);

//            workflows.entrySet().forEach(System.out::println);
//            System.out.println(parts);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

    private static Map<String, RuleSet> getWorkflows(List<String> data) {
        return data.stream()
                .map(l -> {
                   var split = l.split("[{},]");
                   var workflowName = split[0];
                   var defaultNext = split[split.length-1];
                   var rules = IntStream.range(1,split.length-1)
                            .boxed()
                            .map(i -> split[i].split(":"))
                            .map(rs -> new Rule(rs[0].charAt(0),
                                                rs[0].charAt(1),
                                                Long.parseLong(rs[0].substring(2)),
                                                rs[1])
                                )
                            .toList();
                   return new ParsingWorkFlowInfo(workflowName,new RuleSet(rules,defaultNext));
                })
                .collect(Collectors.toMap(k -> k.name, v-> v.set));
    }

    private static List<Part> getParts(List<String> data) {
        return data.stream()
                 .map(l -> l.replaceAll("[{}]",""))
                 .map(l -> Arrays.stream(l.split(","))
                         .map(r -> r.split("="))
                         .toList())
                 .map(l -> new Part(l.stream().collect(Collectors.toMap(k -> k[0].charAt(0),v -> Long.valueOf(v[1])))))
                 .toList();
    }
}
