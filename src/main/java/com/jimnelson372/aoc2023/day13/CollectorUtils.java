package com.jimnelson372.aoc2023.day13;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// ChatGPT wrote this utility class for me, though I made some bug fixes.
// it is used to split incoming lists of strings into grouped List<Strings>
// whenever the original list had a blank.
public class CollectorUtils {

    // Custom Collector to split a stream of strings by blank lines
        public static Collector<String, ?, GroupingByBlankLines> splittingByBlankLines() {
            return Collector.of(
                    GroupingByBlankLines::new,
                    GroupingByBlankLines::accumulate,
                    GroupingByBlankLines::combine
            );
        }

    public static class GroupingByBlankLines {
        private final List<List<String>> groups = Stream.of(new ArrayList<String>()).collect(Collectors.toList());

        protected void accumulate(String line) {
            if (line.isEmpty()) {
                groups.add(new ArrayList<>());
            } else {
                groups.get(groups.size() - 1).add(line);
            }
        }

        protected  GroupingByBlankLines combine(GroupingByBlankLines other) {
            System.out.println(other.groups);
            groups.addAll(other.groups);
            return this;
        }

        public List<List<String>> toList() {
            return groups;
        }
    }
}
