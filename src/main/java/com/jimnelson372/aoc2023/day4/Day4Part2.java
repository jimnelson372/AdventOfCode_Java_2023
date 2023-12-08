package com.jimnelson372.aoc2023.day4;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Day4Part2 {

    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day4-puzzle-input.txt"))) {
            Stream<String> lines = br.lines();

              var matchingPerCard = getMatchingPerCardList(lines);
              var result = getTotalScoreFromCards(matchingPerCard);

              System.out.println("result = " + result);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }

    private static Integer getTotalScoreFromCards(List<Integer> matchingPerCard) {
        var totalStartCards = matchingPerCard.size();

        var additionalCardsWonByThisCard = new ArrayList<>(Collections.nCopies(totalStartCards, 0));

        // Rather than recurse to the base case (which will be the final cards)
        // well do the processing from the last card and move backwards.
        // Note: we start at the end of the list with the iterator and use previous() to move backwards.

        // In other words, the algorithm is:
        //    Start beyond the last line of the provided "matchingPerCard" List.
        //    Iterating backwords, as long as we have a previous line:
        //       We get the current line's known count of matching numbers from the provided list.
        //       We'll be calculating the # of additional cards that will result by
        //          adding 1 more of this card.  And this calculation will be stored in
        //          a new ArrayList, "additionalCardsWonByThisCard" at the same ndx as this line.
        //       For the current card, we use its count to sum the counts for that # of
        //           following rows (capped by available rows) from the additionalCardsWonByThisCard.
        //           (By this time, we've already calculated those rows's values.)
        //           We add this sum to our current card's count and record this in the
        //           additionalCardsWonByThisCart array at the same index.
        //       When we've completed this process for all the rows,
        //          we now sum up all the values in the additionalCardsWonByThisCard array.
        //          And that's our answer.
        //    I believe this is much better than taking all the recursive steps to start at
        //          the top and have to keep track of which rows were already seen, etc.

        ListIterator<Integer> matchingPerCardIterator = matchingPerCard.listIterator(totalStartCards);
        while(matchingPerCardIterator.hasPrevious()) {
            var curNdx = matchingPerCardIterator.previousIndex();
            var numMatchesOnCard = matchingPerCardIterator.previous();

            // Calculate the # of cards we can add;
            var numNewCards = Math.min(numMatchesOnCard, totalStartCards-curNdx-1);
            // calculate the total cards these new cards will add;
            var totalNewCards = additionalCardsWonByThisCard.subList(curNdx+1,curNdx+numNewCards+1).stream()
                    .reduce(numNewCards,Integer::sum);
            // store that count for that card in the additionalCardsPerCard list.
            additionalCardsWonByThisCard.set(curNdx,totalNewCards);
        }

        //System.out.println(additionalCardsPerCard);
        return additionalCardsWonByThisCard.stream().reduce(totalStartCards,Integer::sum);
    }

    // This will give us a List of the # of matching #s for each card, in order.
    // I test for skipped card #s, but the input date doesn't skip, so I
    //   will continue on that assumption for now.
    private static List<Integer> getMatchingPerCardList(Stream<String> lines) {
        //AtomicInteger cnt = new AtomicInteger();
        return lines.map(card -> {
            //cnt.getAndIncrement();
            var sections = List.of(card.split("[:|]\\s+"));

            var winning = getWinningNumberSet(sections.get(1));
            var ourNumbers = getWinningNumberSet(sections.get(2));

            Set<Integer> intersect = intersectionSet(winning, ourNumbers);

            return intersect.size();
        }).toList();
    }

    // This didn't need to be generic, but it didn't seem to depend on the type, so I made it generic.
    // I'm surprised it's not part of the collection library already.
    private static <T> Set<T> intersectionSet(Set<T> set1, Set<T> set2) {
        Set<T> intersect = new HashSet<>(set1);
        intersect.retainAll(set2);
        return intersect;
    }

    private static Set<Integer> getWinningNumberSet(String winningPart) {
        return Arrays.stream(winningPart.split("\\s+")).map(Integer::valueOf).collect(Collectors.toSet());
    }
}