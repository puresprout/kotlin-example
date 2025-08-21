package com.purestation.app.sequence

fun main() {
    val result = (1..100_000).asSequence()
        .filter { println("filter $it"); it % 2 == 0 }
        .map { println("map $it"); it * it }
        .take(5)
        .toList()
    println(result)

    /*
filter 1
filter 2
map 2
filter 3
filter 4
map 4
filter 5
filter 6
map 6
filter 7
filter 8
map 8
filter 9
filter 10
map 10
[4, 16, 36, 64, 100]
     */
}