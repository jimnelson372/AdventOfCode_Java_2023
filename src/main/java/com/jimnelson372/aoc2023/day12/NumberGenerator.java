package com.jimnelson372.aoc2023.day12;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// ChatGBT
public class NumberGenerator {

    record NumberInfo(int position, long currentSum, long currentNumber) {}

    public static List<Long> generateNumbers(int n, long targetSum) {
        System.out.println("Generate numbers for: " +  n + " digits and  targetsum=" + targetSum);
        List<Long> result = new ArrayList<>();
        Stack<NumberInfo> stack = new Stack<>();
        stack.push(new NumberInfo(0, 0, 0));

        while (!stack.isEmpty()) {
            NumberInfo info = stack.pop();

            if (info.position == n && info.currentSum == targetSum) {
                //System.out.println(info.currentNumber);
                result.add(info.currentNumber);
            } else if (info.position < n) {
                for (long digit = 0; digit <= targetSum; digit++) {
                    if (info.currentSum + digit <= targetSum) {
                        stack.push(new NumberInfo(info.position + 1,
                                info.currentSum + digit,
                                Math.multiplyExact(info.currentNumber, (targetSum+1)) + digit));
                    }
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();
        int n = 16;
        int targetSum = 9;
        List<Long> result = generateNumbers(n, targetSum);

        System.out.println(result);
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }
}
