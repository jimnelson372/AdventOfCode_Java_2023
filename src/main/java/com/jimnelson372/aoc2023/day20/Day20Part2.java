package com.jimnelson372.aoc2023.day20;


import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Day20Part2 {

    static final String  broadcasterName = "broadcaster";

    enum ModuleType {FLIP_FLOP, BROADCASTER, CONJUNCTION}

    enum FlipFlopState { ON, OFF }
    enum PulseType { LOW, HIGH }
    interface Module {
        ModuleType getType();
        List<String> getSendToes();
        Module processPulse(PulseMessage pulse);
    }

    record FlipFlop(String name, List<String> sendTos, FlipFlopState state) implements Module {
        @Override
        public ModuleType getType() { return ModuleType.FLIP_FLOP; }

        @Override
        public List<String> getSendToes() {
            return sendTos;
        }

        @Override
        public FlipFlop processPulse(PulseMessage pulse) {
            if (pulse.type == PulseType.HIGH) return this;
            // overkill for this app, but I wanted to try it.
            var flipFlopState = (state == FlipFlopState.ON) ? FlipFlopState.OFF : FlipFlopState.ON;
            var typePulseToSend = switch (flipFlopState) {
                case ON -> PulseType.HIGH;
                case OFF -> PulseType.LOW;
            };
            broadCast(typePulseToSend, name, sendTos);

            return new FlipFlop(name, sendTos, flipFlopState);
        }


    }
    record Broadcaster(List<String> sendTos) implements Module{
        @Override
        public Broadcaster processPulse(PulseMessage pulse) {

            broadCast(pulse.type, broadcasterName, sendTos);
            return this;
        }

        @Override
        public List<String> getSendToes() {
            return sendTos;
        }

        @Override
        public ModuleType getType() {
            return ModuleType.BROADCASTER; }
    }
    record Conjunction(String name, List<String> sendTos, List<PulseMessage> mostResentPulses) implements Module {
        @Override
        public Conjunction processPulse(PulseMessage pulse) {
            List<PulseMessage> resentPulses = mostResentPulses;
            if (mostResentPulses.isEmpty()) {
                resentPulses = moduleMap.entrySet().stream()
                        .filter(m-> m.getValue().getSendToes().contains(name))
                        .map(m -> new PulseMessage(PulseType.LOW,m.getKey(),"initialize"))
                        .toList();
            }
            resentPulses = new ArrayList<>(resentPulses.stream()
                    .filter(pm -> !pm.sender.equals(pulse.sender))
                    .toList());

            resentPulses.add(pulse);
            // this use of reduce wouldn't work on an empty list, since even without a HIGH it would come back true.
            // but fortunately the previous statement ensures there is at least one item to test.
            var areAllHigh = resentPulses.stream()
                    .map(p -> p.type == PulseType.HIGH)
                    .reduce(true, (acc, isHigh) -> acc && isHigh);

            broadCast((areAllHigh) ? PulseType.LOW : PulseType.HIGH, name, sendTos);

            return new Conjunction(name, sendTos,resentPulses);
        }

        @Override
        public List<String> getSendToes() {
            return sendTos;
        }

        @Override
        public ModuleType getType() { return ModuleType.CONJUNCTION; }
    }

    record PulseMessage(PulseType type, String sender, String target) {
        @Override
        public String toString() {
            return sender + " -" + (type == PulseType.LOW ? "low" : "high")
                    + "-> " + target;
        }
    }

    static Queue<PulseMessage> messageQueue = new ArrayDeque<>();
    static Map<String,Module> moduleMap = new HashMap<>();


    static void pressButton() {
        buttonPressedCnt++;
        broadCast(PulseType.LOW,"button",List.of(broadcasterName));
        while (!messageQueue.isEmpty()){
            var message = messageQueue.poll();

            var module = moduleMap.getOrDefault(message.target,new Broadcaster(List.of()));

            // We get back an updated state on the module which we store in the map.
            //  replacing the prior state.
            var updated = module.processPulse(message);
            moduleMap.put(message.target,updated);
        }
    }

    static long lowPulseCnt = 0;
    static long highPulseCnt = 0;
    static long buttonPressedCnt = 0;



    private static void broadCast(PulseType typePulseToSend, String from, List<String> sendTos) {
        sendTos.forEach(st-> {
            if (typePulseToSend == PulseType.LOW)
                lowPulseCnt++;
            else
                highPulseCnt++;
            PulseMessage message = new PulseMessage(typePulseToSend, from, st);
            // Added this call to performMonitoring for the purpose of part 2.
            performMonitoring(message);

            messageQueue.add(message);
        });
    }

    // Global context needed for performMonitoring.
    static Map<String, ImmutablePair<Long,Long>> cycleCountMap = new HashMap<>();
    static List<String> monitorList = List.of();
    static Set<String> cyclingSet = new HashSet<>();
    static boolean allFound = false;
    private static void performMonitoring(PulseMessage message) {
        // check that it's only the modules we're monitoring and we must have a HIGH signal.
        if (!monitorList.contains(message.sender)) return;
        if (message.type == PulseType.LOW) return;

        var countsLastTime = cycleCountMap.getOrDefault(message.sender, new ImmutablePair<>(0L,0L));

        // We are waiting to confirm a cycle length per our monitored modules.
        var cycleLength = buttonPressedCnt - countsLastTime.left; // left has last times buttonPressedCnt
        if (cycleLength == countsLastTime.right) {
            cyclingSet.add(message.sender); // We found the cycle on this one.
        } else
            cycleCountMap.put(message.sender,new ImmutablePair<>(buttonPressedCnt,cycleLength));

        if (cyclingSet.size() == monitorList.size())
            allFound = true;  // now we know we've found them all.

    }

    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day20-puzzle-input.txt"))) {
            populateModuleMap(br);

            var requires = moduleMap.entrySet().stream()
                    .filter(kv -> kv.getValue().getSendToes().contains("rx"))
                    .toList();


            // Just confirming what I saw in the file.
            if (requires.size() == 1 && requires.get(0).getValue().getType() == ModuleType.CONJUNCTION) {

                String moduleName = requires.get(0).getKey();

                System.out.println("Yes, rx only depends on " + moduleName);

                // Get the module names that feed this module, and we'll monitor them for
                //   cycles in their HIGH signally.
                monitorList = moduleMap.entrySet().stream()
                        .filter(kv -> kv.getValue().getSendToes().contains(moduleName))
                        .map(Map.Entry::getKey)
                        .toList();

                while (!allFound) {
                    pressButton();
                }
                // Once we have all cycles, we can just get the LCM of them.  That's our answer.
                System.out.println("Needed cycles to reach rx: " + cycleCountMap.values().stream()
                        .map(ip -> BigInteger.valueOf(ip.right))
                        .reduce(BorrowedUtils::lcm).orElse(BigInteger.ZERO));

            } else {
                System.out.println("My assumptions about the file were wrong.");
            }

        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }


    static class BorrowedUtils {
        // this Least Common Multiple method is from https://www.baeldung.com/java-least-common-multiple
        // It is fast enough for my purposes here.
        public static BigInteger lcm(BigInteger number1, BigInteger number2) {
            BigInteger gcd = number1.gcd(number2);
            BigInteger absProduct = number1.multiply(number2).abs();
            return absProduct.divide(gcd);
        }
    }
    private static void populateModuleMap(BufferedReader br) {
        br.lines()
                .map(line -> List.of(line.split("->")))
                .map(l -> {
                    var name = l.get(0).trim();
                    var sendTos = Arrays.stream(l.get(1).split(","))
                                .map(String::trim)
                                .toList();
                    return switch (name.charAt(0)) {
                        case '%' -> new FlipFlop(name.substring(1),sendTos, FlipFlopState.OFF);
                        case '&' -> new Conjunction(name.substring(1),sendTos,List.of());
                        default -> new Broadcaster(sendTos);
                    };
                })
                .forEach(m -> {
                    if (m instanceof Broadcaster b)
                        moduleMap.put(broadcasterName, b);
                    else if (m instanceof FlipFlop f)
                        moduleMap.put(f.name, f);
                    else if (m instanceof Conjunction c) {
                        moduleMap.put(c.name, c);
                    }
                });
    }
}
