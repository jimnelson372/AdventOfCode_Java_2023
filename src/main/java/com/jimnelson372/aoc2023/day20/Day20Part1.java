package com.jimnelson372.aoc2023.day20;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

public class Day20Part1 {

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
    private static void broadCast(PulseType typePulseToSend, String from, List<String> sendTos) {
        sendTos.forEach(st-> {
            if (typePulseToSend == PulseType.LOW)
                lowPulseCnt++;
            else
                highPulseCnt++;
            PulseMessage message = new PulseMessage(typePulseToSend, from, st);
            // System.out.println("" + message);
            messageQueue.add(message);
        });
    }
    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day20-puzzle-input.txt"))) {
            populateModuleMap(br);


            IntStream.range(0,1000).forEach(i -> {
                //System.out.println("Pressing Button...");
                pressButton();
//                moduleMap.values().stream()
//                        .filter(m -> m.getType() == ModuleType.FLIP_FLOP)
//                        .forEach(System.out::println);
//                System.out.println("-----------");
            });

            System.out.println("Lows: " + lowPulseCnt);
            System.out.println("Highs: " + highPulseCnt);
            System.out.println("Answer = " + lowPulseCnt*highPulseCnt);



        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
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
                        case '%' -> new FlipFlop(name.substring(1),sendTos,FlipFlopState.OFF);
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
