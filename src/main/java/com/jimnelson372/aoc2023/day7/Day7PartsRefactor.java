package com.jimnelson372.aoc2023.day7;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day7PartsRefactor {

    enum HandType {
        HighCard(11),
        OnePair(21),
        TwoPair(22),
        ThreeOfKind(31),
        FullHouse(32),
        FourOfKind(41),
        FiveOfKind(50);

        public final int numericRep;

        HandType(int numericRep) {
            this.numericRep = numericRep;
        }

        static public HandType of(int numericRep) {
            // Didn't really need the enumerated types, give I can order by the intrinsic type,
            // but it depends on further uses if we need to see these types printed.
            return switch (numericRep) {
                case 50 -> HandType.FiveOfKind;
                case 41 -> HandType.FourOfKind;
                case 32 -> HandType.FullHouse;
                case 31 -> HandType.ThreeOfKind;
                case 22 -> HandType.TwoPair;
                case 21 -> HandType.OnePair;
                default -> HandType.HighCard;
            };
        }
    }

    record GenericCardHand(
            String cards,
            int bid) {
        public List<Integer> getCardValuesList(String orderOfCards) {
            return this.cards.codePoints()
                    .map(orderOfCards::indexOf)
                    .boxed()
                    .toList();
        }
    }

    record RankedCardHand(
            GenericCardHand hand,
            HandType type,
            long value) implements Comparable<RankedCardHand> {

        @Override
        public int compareTo(RankedCardHand o) {
            var typeOrder = this.type.compareTo(o.type);
            return typeOrder != 0
                    ? typeOrder
                    :
                            Long.compare(this.value, o.value);
        }
    }

    static class HandHelpers {

        private static Integer calculateIntrinsicValue(List<Integer> cardValues) {
            // Converting the hand as though it's a base 13 -> base 10 long.
            return cardValues.stream()
                    .reduce(0, (acc, v) -> acc * 13 + v);
        }

        private static int sumHandWinnings(List<RankedCardHand> rankOrderedHands) {
            return IntStream.range(0, rankOrderedHands.size())
                    .reduce(0, (acc, i) -> acc + getWinningsForHand(i, rankOrderedHands));
        }

        private static int getWinningsForHand(int i, List<RankedCardHand> rankOrderedHands) {
            return rankOrderedHands.get(i).hand.bid * (i + 1);
        }

        public static int compareByRanking(RankedCardHand a, RankedCardHand b) {
            var typeOrder = a.type.compareTo(b.type);
            return typeOrder != 0
                    ? typeOrder
                    :
                            Long.compare(a.value, b.value);
        }

        private static List<Integer> getCardValuesList(GenericCardHand hand, String orderOfCards) {
            return hand.cards.codePoints()
                    .map(orderOfCards::indexOf)
                    .boxed()
                    .toList();
        }
    }

    static class Rule1Helpers {

        static public RankedCardHand toRule1RankedHand(GenericCardHand hand) {
            @SuppressWarnings("SpellCheckingInspection")
            var cardValues = hand.getCardValuesList("23456789TJQKA");

            var intrinsicValue = HandHelpers.calculateIntrinsicValue(cardValues);
            var handType = getRule1HandType(cardValues);

            return new RankedCardHand(hand, handType, intrinsicValue);
        }

        private static HandType getRule1HandType(List<Integer> cardValues) {
            // Now group the cards, and sort the counts from highest to lowest
            var holding = cardValues.stream()
                    .collect(Collectors.groupingBy(a -> a, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue()
                                    .reversed())
                    .map(e -> e.getValue()
                            .intValue())
                    .toList();
            // We can create a comparable value from the highest 2 counts
            //   as seen in the switch statement below.
            return HandType.of(holding.get(0) * 10 + (holding.size() > 1
                    ? holding.get(1)
                    : 0));
        }
    }

    static class Rule2Helpers {

        @SuppressWarnings("SpellCheckingInspection")
        static final private String cardOrder = "J23456789TQKA";

        static public RankedCardHand toRule2RankedHand(GenericCardHand hand) {

            var cardValues = hand.getCardValuesList(cardOrder);

            var intrinsicValue = HandHelpers.calculateIntrinsicValue(cardValues);
            var handType = getRule2HandType(cardValues, 'J');

            return new RankedCardHand(hand, handType, intrinsicValue);
        }

        private static HandType getRule2HandType(List<Integer> cardValues, char wildcard) {
            // Now group the cards, and sort the counts from highest to lowest
            var countedCards = cardValues.stream()
                    .collect(Collectors.groupingBy(a -> a, Collectors.counting()));

            // Here I cut into my original stream processing
            //  to get the count of the Wild cards
            var wildCardNdx = cardOrder.indexOf(wildcard);
            var countWildcardsInHand = countedCards.getOrDefault(wildCardNdx, 0L);
            if (countWildcardsInHand == 5) {
                // if wildcard is the FiveOfKind, we can set the intrinsic type to 50 and return early
                return HandType.of(50);
            }

            var countedCardsBeforeApplyWild = countedCards
                    .entrySet()
                    .stream()
                    .filter(e -> e.getKey() != wildCardNdx)  //filter out wildcard
                    .sorted(Map.Entry.<Integer, Long>comparingByValue()
                                    .reversed()
                                    .thenComparing(Map.Entry.<Integer, Long>comparingByKey()
                                                           .reversed()))
                    .toList();

            if (countWildcardsInHand > 0) {
                // If we had wildcards in the hand before we filtered them out,
                //  add their count to the remaining card with the highest count
                var topEntry = countedCardsBeforeApplyWild.get(0);
                topEntry.setValue(topEntry.getValue() + countWildcardsInHand);
            }

            var cardGroupingCounts = countedCardsBeforeApplyWild.stream()
                    .map(e -> e.getValue()
                            .intValue())
                    .limit(2)  //we only care about the highest 2 multiple card counts in hand.
                    .toList();

            // We can create a comparable value from the highest 2 counts
            //   as seen in the switch statement below.
            return HandType.of(cardGroupingCounts.get(0) * 10
                                       + (cardGroupingCounts.size() > 1
                    ? cardGroupingCounts.get(1)
                    : 0));
        }

    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources")
                .toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day7-puzzle-input.txt"))) {

            var hands = br.lines()
                    .map(hand -> {
                        var split = hand.split("\\s");
                        return new GenericCardHand(split[0], Integer.parseInt(split[1]));
                    })
                    .toList();

            // How to process Day 7 Part 1 with this list of hands and helper methods.

            var rankOrderedHands = hands.stream()
                    .map(Rule1Helpers::toRule1RankedHand)
                    .sorted()
                    .toList(); // we end this stream since the sum switches to an IntStream to have ndx.

            var part1 = HandHelpers.sumHandWinnings(rankOrderedHands);
            System.out.println("Day 7 Part 1 Total winnings = " + part1);

            // How to process Day 7 Part 1 with this list of hands and helper methods.
            // The difference comes down to the function that maps to a RankedHand.

            var rankOrderedHandsPart2 = hands.stream()
                    .map(Rule2Helpers::toRule2RankedHand) // only diff from Part 1 at this level.
                    .sorted()
                    .toList(); // we end this stream since the sum switches to an IntStream to have ndx.

            var part2 = HandHelpers.sumHandWinnings(rankOrderedHandsPart2);
            System.out.println("Day 7 Part 2 Total winnings = " + part2);

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }
}
