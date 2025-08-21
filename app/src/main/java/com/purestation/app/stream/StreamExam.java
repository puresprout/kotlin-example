package com.purestation.app.stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamExam {
    public static void main(String[] args) {
        List<Integer> src = Arrays.asList(1,2,3,4,5);
        List<Integer> out = src.stream()
                .map(x -> {
                    System.out.println("map: " + x);
                    return x * 2;
                })
                .filter(x -> {
                    System.out.println("filter: " + x);
                    return x % 3 == 0;
                })
                .collect(Collectors.toList());

        System.out.println("Result: " + out); // [6]

        /*
map: 1
filter: 2
map: 2
filter: 4
map: 3
filter: 6
map: 4
filter: 8
map: 5
filter: 10
Result: [6]
         */
    }
}
