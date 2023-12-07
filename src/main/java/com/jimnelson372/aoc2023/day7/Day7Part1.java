package com.jimnelson372.aoc2023.day7;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Day7Part1 {

    enum HandType {
        UNKNOWN,
        HighCard,
        OnePair,
        TwoPair,
        ThreeOfKind,
        FullHouse,
        FourOfKind,
        FiveOfKind
    }



    static class Hand implements Comparable<Hand> {
        String cards;
        HandType handType = HandType.UNKNOWN;
        long intrinsicValue;
        int intrinsicType;
        int bid;

        static String cardOrder = "23456789TJQKA";

        public Hand(String cards, int bid) {
            this.cards = cards;
            this.bid = bid;
            computeHandTypeAndIntrinsicValue(cards);
        }

        private void computeHandTypeAndIntrinsicValue(String cards) {
            var cardValues = cards.codePoints().map(ch -> {
                return cardOrder.indexOf(ch);
            }).boxed().toList();
            // Converting the hand as though it's a base 13 -> base 10 long.
            this.intrinsicValue = cardValues.stream().reduce(0,(acc, v) -> acc*13 + v);

            // Now group the cards, and sort the counts from highest to lowest
            var holding = cardValues.stream()
                    .collect(Collectors.groupingBy(a -> a,Collectors.counting()))
                    .entrySet().stream().sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .map(e -> e.getValue().intValue())
                    .toList();
            // We can create a comparable value from the highest 2 counts
            //   as seen in the switch statement below.
            this.intrinsicType = holding.get(0)*10 + (holding.size() >1  ? holding.get(1) : 0);

            // Didn't really need the enumerated types, give I can order by the intrinsicetype
            // but it depends on further uses if we need to see these types printed.
            this.handType = switch(this.intrinsicType) {
               case 50 -> HandType.FiveOfKind;
               case 41 -> HandType.FourOfKind;
               case 32 -> HandType.FullHouse;
               case 31 -> HandType.ThreeOfKind;
               case 22 -> HandType.TwoPair;
               case 21 -> HandType.OnePair;
               default -> HandType.HighCard;
            };
        }

        @Override
        public int compareTo(Hand o) {
            var typeOrder = Integer.compare(this.intrinsicType, o.intrinsicType);
            return typeOrder != 0 ? typeOrder :
                            Long.compare(this.intrinsicValue,o.intrinsicValue);
        }
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day7-puzzle-input.txt"))) {

            var hands = new java.util.ArrayList<>(br.lines().map(hand -> {
                var split = hand.split("\\s");
                return new Hand(split[0], Integer.parseInt(split[1]));
            }).sorted().toList());


            var result = LongStream.range(1,hands.size()+1)
                    .reduce(0L, (acc,i) -> acc + hands.get((int) i - 1).bid * i);

            System.out.println("Total winnings = " + result);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }
}
