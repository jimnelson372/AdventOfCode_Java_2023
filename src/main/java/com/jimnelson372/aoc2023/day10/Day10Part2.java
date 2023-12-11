package com.jimnelson372.aoc2023.day10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

public class Day10Part2 {

    record Position(int x, int y) {
        public Position nextPosition(Direction dir) {
            return new Position(this.x + dir.xOff, this.y + dir.yOff);
        }

        public Position nextSafePosition(Direction dir, int rows, int cols) {
            var newX = Math.min(cols - 1, Math.max(0, this.x + dir.xOff));
            var newY = Math.min(rows - 1, Math.max(0, this.y + dir.yOff));
            return new Position(newX, newY);
        }

        @Override
        public String toString() {
            return "{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
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

        static public PipeType of(Direction d1, Direction d2) {
            var pathList = List.of(NorthSouth, NorthWest, NorthEast, EastWest, SouthWest, SouthEast);
            return pathList.stream()
                    .filter(p -> p.opening1 == d1 & p.opening2 == d2)
                    .findFirst().
                    orElse(None);
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

        public Direction nextDirectionIfStarting(Direction start) {
            return start == opening1 ? this.opening2
                    : start == opening2 ? this.opening1
                    : Direction.None;
        }
    }


    private static int getPipeDistance(int[][] pipeTracking, Position curPosition, Direction curDirection, List<List<PipeType>> schematic) {
        PipeType curPipe;
        var pipeCount = 0;
        do {
            pipeCount++;
            pipeTracking[curPosition.y][curPosition.x] = 1;

            curPosition = curPosition.nextPosition(curDirection);
            curPipe = schematic.get(curPosition.y).get(curPosition.x);

            curDirection = curPipe.nextDirectionIfStarting(curDirection.reverse());

        } while (curPipe.opening1 != Direction.None && pipeTracking[curPosition.y][curPosition.x] == 0);
        return pipeCount;
    }

    private static Position findPositionOfStart(List<List<PipeType>> schematic) {
        // First we need the starting position, and starting direction.
        var startRow = IntStream.range(0, schematic.size())
                .filter(i -> schematic.get(i).contains(PipeType.Start))
                .findFirst()
                .orElse(0);
        var startCol = schematic.get(startRow).indexOf(PipeType.Start);
        return new Position(startCol, startRow);
    }

    private static int getVertCrossingIncrement(PipeType curPipeType, PipeType nextPipeType) {
        return nextPipeType.hasVertical() ? switch (curPipeType) {
            case EastWest -> 0;
            case NorthEast -> nextPipeType == PipeType.SouthWest ? 0 : 1;
            case SouthEast -> nextPipeType == PipeType.NorthWest ? 0 : 1;
            default -> 1;
        } : 0;
    }

    private static PipeType getPipeTypeAtStart(List<List<PipeType>> schematic, Position curPosition) {
        var maxRows = schematic.size();
        var maxCols = schematic.get(0).size();


        var directions = List.of(Direction.North, Direction.South, Direction.East, Direction.West);
        for (var dir : directions) {
            Position pos = curPosition.nextSafePosition(dir, maxRows, maxCols);
            var nextPath = schematic.get(pos.y).get(pos.x);
            var nextDirection = nextPath.nextDirectionIfStarting(dir.reverse());
            if (nextDirection != Direction.None) break;
        }

        var potentialStartDirections = directions.stream()
                .filter(dir -> {
                    Position pos = curPosition.nextSafePosition(dir, maxRows, maxCols);
                    var nextPath = schematic.get(pos.y).get(pos.x);
                    var nextDirection = nextPath.nextDirectionIfStarting(dir.reverse());
                    return (nextDirection != Direction.None);
                })
                .sorted()
                .toList();
        if (potentialStartDirections.size() != 2) {
            throw new RuntimeException("Bad start position; should connect 2 pipes");
        }

        return PipeType.of(potentialStartDirections.get(0), potentialStartDirections.get(1));
    }
    public static void main(String[] args) {
        var startTime = System.nanoTime();

        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath, "day10-puzzle-input.txt"))) {
            // For this problem, I loaded the full file into memory to
            // use it as like an array of arrays.

            // Get the Pipe Type for every position
            var schematic = br.lines()
                    .map(l -> l.chars()
                            .mapToObj(c -> PipeType.of((char) c))
                            .toList())
                    .toList();


            var rows = schematic.size();
            var cols = schematic.get(0).size();

            // Initial Decision to use a two-dimensional array to track the positions of our pipe path.
            int[][] pipeTracking = new int[rows][cols];

            var startPosition = findPositionOfStart(schematic);

            // And for later, I want the implied PathType at the starting position.
            var startPosPipeType = getPipeTypeAtStart(schematic, startPosition);
            //System.out.println(startPosPipeType);

            var curDirection = startPosPipeType.opening1;
            //System.out.println("Starting direction:" + curDirection);

            // Map the pipe loop from Start and around.
            // Also count the pipes to solve Part 1.
            var curPosition = startPosition;
            //var curPipe = impliedPipeTypeAtStartPosition;

            var count = getPipeDistance(pipeTracking, curPosition, curDirection, schematic);

//            //Print Result Map -- just to see the positions of the pipes only in our path.
//            for (int i = 0; i < rows; i++) {
//                for (int j = 0; j < cols; j++) {
//                    curPosition = new Position(j, i);
//                    var cur = pipeTracking[curPosition.y][curPosition.x];
//                    if (curPosition.equals(startPosition))
//                        System.out.print(startPosPipeType.out());
//                    else if (cur == 0)
//                        System.out.print(".");
//                    else
//                        System.out.print(schematic.get(curPosition.y).get(curPosition.x).out());
//                }
//                System.out.println();
//            }

            // Now we can use this information to solve the AoC Part 2 question.
            //  We'll loop through each row.
            var tilesInLoop = 0;
            for (int i = 0; i < rows; i++) {
                int finalI = i;
                var pipesOnPathOnly = IntStream.range(0, cols)
                        .mapToObj(u -> {
                            var pos = new Position(u, finalI);
                            if (pos.equals(startPosition)) return startPosPipeType;
                            return pipeTracking[pos.y][pos.x] == 0 ? PipeType.None : schematic.get(pos.y).get(pos.x);
                        })
                        .toList();

                int vertCrossingCount = 0;
                var curTestPath = PipeType.None;
                for (PipeType p : pipesOnPathOnly) {
                    vertCrossingCount += getVertCrossingIncrement(curTestPath, p);
                    if (p != PipeType.EastWest)
                        curTestPath = p;
                }

                curTestPath = PipeType.None;
                var onPath = PipeType.None;
                var curCrossingCount = 0;
                for (int j = 0; j < cols; j++) {
                    //boolean quickNotInTest = curCrossingCount == 0 || curCrossingCount==vertCrossingCount;
                    curPosition = new Position(j, i);

                    if (curPosition.equals(startPosition))
                        onPath = startPosPipeType;
                    else
                        onPath = schematic.get(curPosition.y).get(curPosition.x);

                    var isTile = pipeTracking[curPosition.y][curPosition.x] == 0;

                    if (isTile)
                        onPath = PipeType.None;

                    curCrossingCount += getVertCrossingIncrement(curTestPath, onPath);
                    if (onPath != PipeType.EastWest)
                        curTestPath = onPath;

                    boolean otherSideOdd = (vertCrossingCount - curCrossingCount) % 2 == 1;
                    if (isTile && (curCrossingCount % 2 == 1 || otherSideOdd)) {
                        tilesInLoop += 1;
                        //System.out.print("I");
                    }
//                    else
//                        System.out.print(".");
                }
                //System.out.println();
            }

            System.out.println();
            System.out.println("Total Path Length: " + count + "; Halfway (furthest distance from start): " + (count / 2));
            System.out.println("Tiles in loop: " + tilesInLoop);
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
        System.out.println("---------------");
        System.out.println("Completed In: " +(System.nanoTime() - startTime)/ 1_000_000 + "ms");
    }

}