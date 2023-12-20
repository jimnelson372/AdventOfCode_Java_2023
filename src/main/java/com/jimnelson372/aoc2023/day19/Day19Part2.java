package com.jimnelson372.aoc2023.day19;

import com.jimnelson372.aoc2023.day13.CollectorUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class Day19Part2 {

    record Part(Map<Character, Long> ratings) {
        public Long ratingsScore() {
            return ratings.values().stream()
                    .reduce(0L, Long::sum);
        }
    }

    record Rule(char rating, char oper, long value, String next) {

        Optional<String> eval(Part part) {
            var valueToTest = part.ratings.get(rating);
            var passes = switch (oper) {
                case '<' -> valueToTest < value;
                case '>' -> valueToTest > value;
                default -> false;
            };
            return passes ? Optional.of(next) : Optional.empty();
        }

        Rule inverse() {
            return switch (oper) {
                case '<' -> new Rule(rating, '>', value - 1, next);
                case '>' -> new Rule(rating, '<', value + 1, next);
                default -> this;
            };
        }
    }

    record RequiredRule(char rating, char oper, long value) {
        RequiredRule inverse() {
            return switch (oper) {
                case '<' -> new RequiredRule(rating, '>', value - 1);
                case '>' -> new RequiredRule(rating, '<', value + 1);
                default -> this;
            };
        }
    }

    record RequireAllRuleSet(List<RequiredRule> rules, String next) {
    }

    record FirstMatchRuleSet(List<Rule> rules, String defaultNext) {

        String eval(Part part) {
            var result = rules.stream()
                    .map(r -> r.eval(part))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

            return result.orElse(defaultNext);
        }
    }


    record Ratings(char rating, long min, long max) {

        long countOfPossibleValues() {
            return max - min + 1;
        }
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day19-puzzle-input.txt"))) {
            var data = br.lines()
                    .collect(CollectorUtils.splittingByBlankLines())
                    .toList();

            var workflows = getWorkflows(data.get(0));

            var solutionToPart2 = getSolutionToPart2(workflows);

            System.out.println("The number of possible combinations that will be accepted: " + solutionToPart2);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }

    private static Long getSolutionToPart2(Map<String, FirstMatchRuleSet> workflows) {
        // Generate the distinct set of ratings range value required to get to A.
        var partlyOrganized = getSetOfRatingRangesForPathsToA(workflows);

        // Now calculate the total # of possible solutions.
        // By multiplying the possible values in each ratings range for each path
        // then summing all these ranges together.
        return partlyOrganized.stream()
                .map(m -> m.stream()
                        .map(Ratings::countOfPossibleValues)
                        .reduce(1L, (acc, v) -> acc * v))
                .reduce(0L, Long::sum);
    }

    private static Set<List<Ratings>> getSetOfRatingRangesForPathsToA(Map<String, FirstMatchRuleSet> workflows) {
        // From the recursive function, we get a raw collection of the gathered rules for a path
        // that made it to A.
        var pathRulesUnorganized = recursiveGatherRequiredRulesToGetToA("in", workflows)
                .orElse(List.of());

        // This is the code that groups those rules into a simpler Ratings record for
        //    each of the rating types, x, m, a and s, with a min and max range value.
        return pathRulesUnorganized.stream()

                .map(r1 -> r1.rules.stream()
                        .collect(groupingBy(RequiredRule::rating,
                                groupingBy(RequiredRule::oper))))

                .map(groupedMap -> Stream.of('x', 'm', 'a', 's')
                        .map(name -> {
                            var e = groupedMap.getOrDefault(name, Map.of());

                            var higher = e.getOrDefault('<', List.of()).stream()
                                    .map(r -> r.value - 1)
                                    .min(Long::compare)
                                    .orElse(4000L);

                            var lower = e.getOrDefault('>', List.of()).stream()
                                    .map(r -> r.value + 1)
                                    .max(Long::compare)
                                    .orElse(1L);

                            if (lower > higher)
                                throw new RuntimeException("invalid range");

                            return new Ratings(name, lower, higher);
                        })
                        .toList()
                )
                .collect(Collectors.toSet());
    }

    private static Optional<List<RequireAllRuleSet>> recursiveGatherRequiredRulesToGetToA(String wf, Map<String, FirstMatchRuleSet> workflows) {

        if (wf.equals("A"))
            return Optional.of(List.of(new RequireAllRuleSet(List.of(), "A")));
        if (wf.equals("R"))
            return Optional.empty();

        // Instead of working with our original Workflow Sequential First Match Rule Set
        //  We'll convert that into what I call a Required All Rule Set, and return it
        //  as a stream for further processing.
        var currWf = workflows.get(wf);
        var requiredRuleSetWFStream = getRequireAllRuleSetStream(currWf);

        // Here is where our recursion happens to hunt down the WF paths that get to A
        //  and gather all the required rules to get there.
        var requiredRulesSetsToGetToA = requiredRuleSetWFStream
                .<RequireAllRuleSet>mapMulti((rule, consumer) -> {
                    var rulesToGetToAOpt = recursiveGatherRequiredRulesToGetToA(rule.next, workflows);
                    if (rulesToGetToAOpt.isEmpty()) return; // won't get passed along.

                    var rulesToGetToA = rulesToGetToAOpt.get();
                    for (var needed : rulesToGetToA) {
                        List<RequiredRule> reqRules = new ArrayList<>();
                        reqRules.addAll(rule.rules);
                        reqRules.addAll(needed.rules);

                        consumer.accept(new RequireAllRuleSet(reqRules, needed.next));
                    }
                })
                .toList();

        return Optional.of(requiredRulesSetsToGetToA);
    }

    private static Stream<RequireAllRuleSet> getRequireAllRuleSetStream(FirstMatchRuleSet firstMatchWF) {

        // Each rule after then first will now include the inverse of the prior rules,
        // since that's the only way for that rule to occur.
        List<RequiredRule> invertedRequiredRules = new ArrayList<>();

        // Convert our workflow sequential Rules and default path into RequiredRuleSets that
        //  can then be further processed independently.
        return Stream.concat(
                firstMatchWF.rules.stream()
                        .map(r -> {
                            var reqRule = new RequiredRule(r.rating, r.oper, r.value);

                            List<RequiredRule> reqRules = new ArrayList<>(invertedRequiredRules);
                            reqRules.add(reqRule);

                            invertedRequiredRules.add(reqRule.inverse());

                            return new RequireAllRuleSet(reqRules, r.next);
                        }),
                Stream.of(new RequireAllRuleSet(invertedRequiredRules, firstMatchWF.defaultNext))
        );
    }

    private static Map<String, FirstMatchRuleSet> getWorkflows(List<String> data) {
        return data.stream()
                .map(l -> {
                    var split = l.split("[{},]");
                    var workflowName = split[0];
                    var defaultNext = split[split.length - 1];
                    var rules = IntStream.range(1, split.length - 1)
                            .boxed()
                            .map(i -> split[i].split(":"))
                            .map(rs -> new Rule(rs[0].charAt(0),
                                    rs[0].charAt(1),
                                    Long.parseLong(rs[0].substring(2)),
                                    rs[1])
                            )
                            .toList();
                    return new ImmutablePair<>(workflowName, new FirstMatchRuleSet(rules, defaultNext));
                })
                .collect(Collectors.toMap(k -> k.left, v -> v.right));
    }

}
