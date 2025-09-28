package com.purestation.app.thread

import kotlin.concurrent.thread

fun main() {
    val t = thread(start = true, name = "worker") {
        println("[${Thread.currentThread().name}] 작업 시작")
        Thread.sleep(200)
        println("[${Thread.currentThread().name}] 작업 끝")
    }

    println("[main] worker 합류 대기")
    t.join()
    println("[main] 종료")
}
