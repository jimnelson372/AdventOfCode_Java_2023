package com.jimnelson372.aoc2023.day15;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;


public class Day15Part2 {

    record LabeledLens(int labelId, int focalLen) {}
    record LabelInfo(int labelid, int hash) {}


    static AtomicInteger labelCounter = new AtomicInteger(1);
    static HashMap<String,LabelInfo> labels = new HashMap<>();
    static List<List<LabeledLens>> boxes = new ArrayList<>(Collections.nCopies(256,List.of()));
    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day15-puzzle-input.txt"))) {
            var sequence = br.readLine();
            var operators = sequence.split(",");


            for(var word : operators) {
                int wordend = word.length() - 1;
                var oper = (word.charAt(wordend) == '-') ? '-' : '=';
                var label = word.substring(0,wordend - ((oper=='-' ? 0 : 1)));
                var digit = (oper=='=') ? word.charAt(wordend)-'0' : 0;
                var labelInfo = labels.compute(label,
                        (k,v) -> (v == null)
                                ? new LabelInfo(labelCounter.getAndIncrement(),HASH(k))
                                : v
                );
                int labelHash = labelInfo.hash;

                var contentsOfBox = boxes.get(labelHash);
                if (oper == '-') {
                    contentsOfBox = contentsOfBox.stream()
                            .filter(ll -> ll.labelId != labelInfo.labelid)
                            .toList();
                }  else {
                    var hasLabel = contentsOfBox.stream()
                            .reduce(false, (acc, ll) -> acc || ll.labelId == labelInfo.labelid, (a,b)->a);
                    if (hasLabel)
                        contentsOfBox = contentsOfBox.stream()
                                        .map(ll -> (ll.labelId == labelInfo.labelid)
                                                    ? new LabeledLens(ll.labelId, digit)
                                                    : ll)
                                                .toList();
                    else {
                        var tmpArrayList = new ArrayList<LabeledLens>(contentsOfBox);
                        tmpArrayList.add(new LabeledLens(labelInfo.labelid, digit));
                        contentsOfBox = tmpArrayList;
                    }
                }
                boxes.set(labelHash,contentsOfBox);


                //System.out.println(label + " " + labelHash + " " + oper + " " + digit + ": " + labelInfo.labelid);
            };

            var scores =
                    IntStream.range(0,boxes.size())
                    .boxed()
                    .map(i -> {
                      var box = boxes.get(i);
                      var score = 0;
                      if (!box.isEmpty()) {
                          //System.out.println("here");
                          var boxPoints = i+1;

                          for(int j=0; j < box.size(); j++) {
                              var slotPoints = j+1;
                              score += boxPoints * slotPoints * box.get(j).focalLen;
                          }
                      }
                      return score;
                    })
                    .filter(i -> i > 0)
                    .reduce(0,Integer::sum);

            System.out.println(scores);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

    private static int HASH(String word) {
        var curCode = 0;
        for (int curNdx = 0; curNdx < word.length(); curNdx ++) {
            int ascii = word.charAt(curNdx);
            curCode += ascii;
            curCode = curCode * 17;
            curCode = curCode % 256;
        }
        return curCode;
    }


}