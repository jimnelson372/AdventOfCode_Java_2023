package com.jimnelson372.aoc2023.day10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Day10Part1 {

    record Position(int x, int y) {
        public Position nextPosition(Direction dir) {
            return new Position(this.x + dir.xOff, this.y + dir.yOff);
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
        North(0,-1),
        West(-1,0),
        South(0,1),
        East(1,0),
        None(0,0),
        Any(0,0);

        final int xOff;
        final int yOff;

        Direction(int xOff, int yOff) {
            this.xOff=xOff;
            this.yOff=yOff;
        }

        public Direction reverse() {
            return switch(this) {
                case West -> East;
                case East -> West;
                case South -> North;
                case North -> South;
                default -> Any;
            };
        }
    }

    enum Path {
        NorthSouth(Direction.North, Direction.South),
        EastWest(Direction.East, Direction.West),
        NorthWest(Direction.North, Direction.West),
        NorthEast(Direction.North, Direction.East),
        SouthWest(Direction.South, Direction.West),
        SouthEast(Direction.South, Direction.East),
        None(Direction.None, Direction.None),
        Start(Direction.None, Direction.None);

        @Override
        public String toString() {
            return "Path{" +
                    "direction1=" + direction1 +
                    ", direction2=" + direction2 +
                    '}';
        }

        final Direction direction1;
        final Direction direction2;

        Path(Direction open1, Direction open2) {
            this.direction1 = open1;
            this.direction2 = open2;
        }

        static public Path of(Character c) {
            return switch(c) {
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

        public String out() {
            return switch(this) {
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
            return start == direction1 ? this.direction2
                    : start == direction2 ? this.direction1
                    : Direction.None;
        }


    }


    public static void main(String[] args) {
        String resourcesPath = Paths.get("src", "main", "resources").toString();
        try(BufferedReader br = Files.newBufferedReader(Paths.get(resourcesPath,"day10-puzzle-input.txt"))) {
            // For this problem, I loaded the full file into memory to
            // use it as like an array of arrays.
            var schematic = br.lines()
                    .map(l -> l.chars()
                            .mapToObj(c -> Path.of((char) c))
                            .toList())
                    .toList();
            Function<Position, Path> schematicGetPathAt = pos -> schematic.get(pos.y).get(pos.x);

            var rows = schematic.size();
            var cols = schematic.get(0).size();

            int[][] res = new int[rows][cols];
            Function<Position,Integer> resLookup =  (pos) -> res[pos.y][pos.x];
            BiFunction<Position,Integer,Integer> resAssign= (pos,val) -> res[pos.y][pos.x] = val;

            var startRow = IntStream.range(0,rows)
                            .filter(i -> schematic.get(i).contains(Path.Start)).findFirst().orElse(0);
            var startCol = schematic.get(startRow).indexOf(Path.Start);
            //System.out.println(startRow + ", " + startCol);

            //var directions = List.of(Direction.North,Direction.South,Direction.East,Direction.West);
            var curDirection = Direction.East;
            var curPath = Path.EastWest;
            var curPosition = new Position(startCol, startRow);
            var count = 0;
            do {
                count++;
                resAssign.apply(curPosition,1);

                curPosition = curPosition.nextPosition(curDirection);
                var path = schematicGetPathAt.apply(curPosition);

                curDirection = path.nextDirectionIfStarting(curDirection.reverse());
                //System.out.println(path + ": " + curPosition +" -- " + curDirection);
            } while(curPath.direction1 != Direction.None && resLookup.apply(curPosition) == 0);

            //Print Result Map
//            for (int i = 0; i < rows; i++) {
//                for (int j = 0; j < cols; j++) {
//                    curPosition = new Position(j,i);
//                    var cur = resLookup.apply(curPosition);
//                    if (cur == 0)
//                        System.out.print(".");
//                    else
//                        System.out.print(schematicGetPathAt.apply(curPosition).out());
//                }
//                System.out.println();
//            }
            System.out.println("Total Path Length: " + count + "; Halfway: " + (count/2));
        } catch (IOException e) {
            System.out.print("The puzzle input was not found at expected location.");
        }
    }

}