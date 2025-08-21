package com.purestation.app.sequence

fun main() {
    val src = listOf(1, 2, 3, 4, 5)

    println("=== List(즉시) ===")
    val eager = src
        .map { println("map $it"); it * 2 }   // 즉시 모두 실행
        .filter { println("filter $it"); it % 3 == 0 }
    println("end of pipeline (List)")       // 여기까지 이미 map/filter 끝
    println(eager)                           // [6]

    /*
=== List(즉시) ===
map 1
map 2
map 3
map 4
map 5
filter 2
filter 4
filter 6
filter 8
filter 10
end of pipeline (List)
[6]
     */



    println("=== Sequence(지연) ===")
    val lazySeq = src.asSequence()
        .map { println("map $it"); it * 2 }    // 아직 실행 안 됨
        .filter { println("filter $it"); it % 3 == 0 }
    println("before terminal")               // 여기까지 아무 연산도 실행 안 됨
    println(lazySeq.toList())                // 이 시점에 요소가 1개씩 map→filter 순서로 흐르며 평가

    /*
=== Sequence(지연) ===
before terminal
map 1
filter 2
map 2
filter 4
map 3
filter 6
map 4
filter 8
map 5
filter 10
[6]
     */
}