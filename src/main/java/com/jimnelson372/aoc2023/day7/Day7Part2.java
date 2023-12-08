package com.jimnelson372.aoc2023.day7;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Day7Part2 {

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

        // Part 2 made J the lowest value, so it moved to the start of the string.
        static String cardOrder = "J23456789TQKA";
        static final char WildCardChar = 'J';

        public Hand(String cards, int bid) {
            this.cards = cards;
            this.bid = bid;
            computeHandTypeAndIntrinsicValue(cards);
        }

        private void computeHandTypeAndIntrinsicValue(String cards) {
            var cardValues = cards.codePoints().map(ch -> cardOrder.indexOf(ch))
                    .boxed()
                    .toList();

            // Converting the hand as though it's a base 13 -> base 10 long.
            this.intrinsicValue = cardValues.stream().reduce(0,(acc, v) -> acc*13 + v);

            // Now group the cards, and sort the counts from highest to lowest
            var countedCards = cardValues.stream()
                    .collect(Collectors.groupingBy(a -> a,Collectors.counting()));

            var wildCardNdx = cardOrder.indexOf(WildCardChar);
            // Here I cut into my original stream processing
            //  to get the count of J cards
            var numWilds = countedCards.getOrDefault(wildCardNdx, 0L); // J is 0 in this case.
            if (numWilds == 5) {
                // if J is the FiveOfKind, we can set the intrinsic type to 50 and return early
                this.intrinsicType = 50;
                return;
            }

            var countedCardsBeforeApplyWild = countedCards
                    .entrySet().stream()
                    .filter(e -> e.getKey() != wildCardNdx)  // we now filter out the remaining J's (key 0)
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()
                            .thenComparing(Map.Entry.<Integer, Long>comparingByKey().reversed()))
                    .toList();

            if (numWilds > 0) {
                // If we had counts of J (Or other wildcard) before we filtered them out,
                //  add those to the remaining card with the top count (index 0).

                var topEntry = countedCardsBeforeApplyWild.get(0);
                topEntry.setValue(topEntry.getValue() + numWilds); // add J's count to topCount;
            }

              var holding = countedCardsBeforeApplyWild.stream()
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

            var hands = br.lines().map(hand -> {
                var split = hand.split("\\s");
                return new Hand(split[0], Integer.parseInt(split[1]));
            }).sorted().toList();


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
