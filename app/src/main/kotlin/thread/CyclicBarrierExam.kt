package com.purestation.app.thread

import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread

fun main() {
    val parties = 3
    // barrier: 3개의 스레드가 모두 도착해야 다음 단계로 넘어갈 수 있음
    // barrier에 모두 도착하면 지정된 동작(println("모두 도착, 다음 단계로")) 실행
    val barrier = CyclicBarrier(parties) {
        println("모두 도착, 다음 단계로")
    }

    // 3개의 스레드를 반복해서 생성
    repeat(parties) { i ->
        thread(start = true) {
            // 1단계 작업 수행
            println("1단계 작업 $i")
            // barrier.await(): 다른 스레드들도 여기 도착할 때까지 대기
            barrier.await()

            // barrier가 열리면(모든 스레드가 도착하면) 2단계 시작
            println("2단계 작업 $i")
            // 다시 barrier.await(): 모든 스레드가 2단계 도착할 때까지 대기
            barrier.await()
        }
    }
}
