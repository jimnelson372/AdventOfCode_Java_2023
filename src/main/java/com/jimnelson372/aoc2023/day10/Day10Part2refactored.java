package com.jimnelson372.aoc2023.day10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day10Part2refactored {

    record Position(int x, int y) {
        public Position moveInDirection(Direction dir) {

            return new Position(this.x + dir.xOff, this.y + dir.yOff);
        }
        public boolean isValidPosition(int maxRow, int maxCol) {
            return x >= 0 && x < maxCol && y >= 0 && y < maxRow;
        }
    }

    enum Direction {
        North(0, -1),
        South(0, 1),
        East(1, 0),
        West(-1, 0),
        None(0, 0),
        Any(0, 0);

        final int xOff;
        final int yOff;

        Direction(int xOff, int yOff) {
            this.xOff = xOff;
            this.yOff = yOff;
        }

        public Direction reverse() {
            return switch (this) {
                case West -> East;
                case East -> West;
                case South -> North;
                case North -> South;
                default -> Any;
            };
        }
    }

    enum PipeType {
        NorthSouth(Direction.North, Direction.South),
        EastWest(Direction.East, Direction.West),
        NorthWest(Direction.North, Direction.West),
        NorthEast(Direction.North, Direction.East),
        SouthWest(Direction.South, Direction.West),
        SouthEast(Direction.South, Direction.East),
        None(Direction.None, Direction.None),
        Start(Direction.None, Direction.None);

        final Direction opening1;
        final Direction opening2;

        PipeType(Direction open1, Direction open2) {
            this.opening1 = open1;
            this.opening2 = open2;
        }

        static public PipeType of(Character c) {
            return switch (c) {
                case '|' -> NorthSouth;
                case '-' -> EastWest;
                case 'L' -> NorthEast;
                case 'J' -> NorthWest;
                case '7' -> SouthWest;
                case 'F' -> SouthEast;
                case '.' -> None;
                case 'S' -> Start;

                default -> throw new IllegalStateException("Unexpected value: " + c);
            };
        }

        static public PipeType of(Direction opening1, Direction opening2) {
            // find the PipeType that matches these direction of openings.
            var pathList = List.of(NorthSouth, NorthWest, NorthEast, EastWest, SouthWest, SouthEast);
            return pathList.stream()
                    .filter(p -> p.opening1 == opening1 & p.opening2 == opening2)
                    .findFirst().
                    orElse(None);
        }

        private int getVertCrossingIncrement(PipeType nextPipeType) {
            // To be a crossing, a vertical pipe must:
            //    1) Hava a vertical component.
            //    2) If a turn, it must continue along the same vertical direction, not turn back.
            //    3) NorthSouth always counts as a crossing.
            //    4) EastWest is ignored.
            return nextPipeType.hasVertical()
                    ? switch (this) {
                        case EastWest -> 0; // ignore eastwest
                        case NorthEast -> nextPipeType == SouthWest ? 0 : 1; // turned back
                        case SouthEast -> nextPipeType == NorthWest ? 0 : 1; // turned back.
                        default -> 1; // any other vertical is a crossing, i.e. default catches NorthSouth.
                    } : 0; // not a vertical.
        }


        public boolean hasVertical() {
            return (this.opening1 == Direction.North ||
                    this.opening1 == Direction.South ||
                    this.opening2 == Direction.North ||
                    this.opening2 == Direction.South);
        }

        public String out() {
            return switch (this) {
                case NorthSouth -> "|";
                case EastWest -> "-";
                case NorthEast -> "L";
                case NorthWest -> "J";
                case SouthEast -> "F";
                case SouthWest -> "7";
                case None -> ".";
                case Start -> "S";
            };
        }

        public Direction outputDirectionsFromThisInput(Direction start) {
            return start == opening1 ? this.opening2
                    : start == opening2 ? this.opening1
                    : Direction.None;
        }
    }

    static class Schematic {
        static List<List<PipeType>> schematic = null;
        static int maxRows = 0;
        static int maxCols = 0;
        static Position startPosition;
        static PipeType startPosPipeType;

        static int[][] pipeTracking;

        private static void initializeSchematicData(BufferedReader br) {
            // For this problem, I loaded the full file into memory to
            // use it as like an array of arrays.

            // Get the Pipe Type for every position in the originally loaded map.
            schematic = br.lines()
                    .map(l -> l.chars()
                            .mapToObj(c -> PipeType.of((char) c))
                            .toList())
                    .toList();
            maxRows = schematic.size();
            maxCols = schematic.get(0).size();

            // Initial Decision to use a two-dimensional array to track the positions of our pipe path.
            pipeTracking = new int[maxRows][maxCols];

            startPosition = findTheStartPosition();

            // And for later, I want the implied Pipe Type at the starting position.
            startPosPipeType = determineThePipeTypeAsTheStartPosition();
        }

        private static Position findTheStartPosition() {
            // First we need the starting position, and starting direction.
            var startRow = IntStream.range(0, schematic.size())
                    .filter(i -> schematic.get(i).contains(PipeType.Start))
                    .findFirst()
                    .orElse(0);
            var startCol = schematic.get(startRow).indexOf(PipeType.Start);
            return new Position(startCol, startRow);
        }
        private static PipeType determineThePipeTypeAsTheStartPosition() {
            var potentialStartDirections = Stream.of(Direction.North, Direction.South, Direction.East, Direction.West)
                    .filter(dir -> {
                        // two conditions to filter out...
                        //    1) an invalid position in that direction
                        Position pos = startPosition.moveInDirection(dir);
                        if (!pos.isValidPosition(maxRows, maxCols)) return false;

                        //    2) a path in that direction that won't move us forward.
                        var nextPath = schematic.get(pos.y).get(pos.x);
                        var nextDirection = nextPath.outputDirectionsFromThisInput(dir.reverse());
                        return (nextDirection != Direction.None);
                    })
                    .sorted()   // I've set this up so that vertical directions (North, South) are earlier in the sort.
                    .toList();
            // Make sure we have 2 directions associated with our Start position.
            if (potentialStartDirections.size() != 2) {
                throw new RuntimeException("Bad start position; should connect 2 pipes");
            }
            // If al those conditions work, we can figure out the implicit PipeType at the S location.
            return PipeType.of(potentialStartDirections.get(0), potentialStartDirections.get(1));
        }

        private static PipeType getCurPipeTypeOnPath(Position pos) {
            return (pos.equals(startPosition))
                    ? startPosPipeType
                    : schematic.get(pos.y).get(pos.x);
        }

        private static void printThePart1ResultMap() {
            Position curPosition;
            //Print Result Map -- just to see the positions of the pipes only in our path.
            for (int i = 0; i < maxRows; i++) {
                for (int j = 0; j < maxCols; j++) {
                    curPosition = new Position(j, i);
                    var cur = pipeTracking[curPosition.y][curPosition.x];
                    if (curPosition.equals(startPosition))
                        System.out.print(startPosPipeType.out());
                    else if (cur == 0)
                        System.out.print(".");
                    else
                        System.out.print(schematic.get(curPosition.y).get(curPosition.x).out());
                }
                System.out.println();
            }
        }
    }

    private static int computeDay10Part1Answer(boolean display) {
        var curDirection = Schematic.startPosPipeType.opening1;
        var curPosition = Schematic.startPosition;
        PipeType curPipe;
        var pipeCount = 0;
        do {
            pipeCount++;

            // I haven't attempted to convert this algorithm to streams()
            // since I'm mutating the following data structure as I loop.
            Schematic.pipeTracking[curPosition.y][curPosition.x] = 1;

            curPosition = curPosition.moveInDirection(curDirection);
            curPipe = Schematic.schematic.get(curPosition.y).get(curPosition.x);

            curDirection = curPipe.outputDirectionsFromThisInput(curDirection.reverse());

        } while (curPipe.opening1 != Direction.None && Schematic.pipeTracking[curPosition.y][curPosition.x] == 0);

        if (display)
            Schematic.printThePart1ResultMap();

        return pipeCount;
    }

    private static int computeDay10Part2Answer(boolean display) {
        Position curPosition;
        var tilesInLoop = 0;
        for (int i = 0; i < Schematic.maxRows; i++) {
            var countedPipeOnPath = PipeType.None;
            var curPipeOnPath = PipeType.None;
            var curCrossingCount = 0;
            for (int j = 0; j < Schematic.maxCols; j++) {
                curPosition = new Position(j, i);

                var isTile = Schematic.pipeTracking[curPosition.y][curPosition.x] == 0;

                curPipeOnPath = isTile
                                    ? PipeType.None
                                    : Schematic.getCurPipeTypeOnPath(curPosition);


                curCrossingCount += countedPipeOnPath.getVertCrossingIncrement(curPipeOnPath);
                // Ignore purely horizontal pipes, i.e. EastWest, in this algorithm.
                if (curPipeOnPath != PipeType.EastWest)
                    countedPipeOnPath = curPipeOnPath;

                // To know if we're surrounded, it's enough to test if we've crossed an odd number of vertical crossings.
                //   with the key to this counting being done in the "getVertCrossingIncrement" above.
                //    (not enough just to count pipes with vertical sections.)
                if (isTile && (curCrossingCount % 2 == 1 )) {
                    tilesInLoop += 1;
                    if (display) System.out.print("I");
                }
                else
                    if (display) System.out.print(".");
            }
            if (display) System.out.println();
        }
        return tilesInLoop;
    }


    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day10-puzzle-input.txt"))) {

            Schematic.initializeSchematicData(br);

            boolean display = true;
            var pipeDistance = computeDay10Part1Answer(display);
            var tilesInLoop = computeDay10Part2Answer(display);

            System.out.println();
            System.out.println("Total Path Length: " + pipeDistance + "; Halfway (furthest distance from start): " + (pipeDistance / 2));
            System.out.println("Tiles in loop: " + tilesInLoop);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }


}