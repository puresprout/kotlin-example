package com.purestation.app.channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull

// Channel로 생산자-소비자(팬아웃) 구현
//suspend fun main() = coroutineScope {
//    val ch = Channel<Int>(Channel.UNLIMITED)
//
//    val producer = launch {
//        for (i in 1..10) {
//            delay(50)
//            ch.send(i)
//        }
//        ch.close()
//    }
//
//    val workers = List(3) { w ->
//        launch {
//            for (x in ch) {
//                println("Worker$w handling $x")
//                delay(200)
//            }
//        }
//    }
//
//    producer.join()
//    workers.joinAll()
//}



// Flow ↔ Channel 변환과 backpressure 제어
//fun callbackToChannel(scope: CoroutineScope): ReceiveChannel<Int> {
//    val ch = Channel<Int>(Channel.UNLIMITED)
//    scope.launch {
//        repeat(10) { i ->
//            delay(50) // 외부 콜백처럼 빠르게 도착
//            ch.send(i)
//        }
//        ch.close()
//    }
//    return ch
//}
//
//fun main() = runBlocking {
//    val ch = callbackToChannel(this)
//    ch
//        .consumeAsFlow()
//        .buffer(capacity = 2, onBufferOverflow = BufferOverflow.DROP_OLDEST)
//        .onEach { delay(200) } // 느린 처리
//        .collect { println("Handled: $it") }
//}



// Fan-in (여러 프로듀서 → 하나의 채널)
fun main(): kotlin.Unit = runBlocking {
    val ch = Channel<String>()

    // producer1
    launch {
        repeat(5) {
            ch.send("🍎 from P1 [$it]")
            delay(200)
        }
    }

    // producer2
    launch {
        repeat(5) {
            ch.send("🍌 from P2 [$it]")
            delay(300)
        }
    }

    // consumer
    launch {
        repeat(10) {
            println("C <- ${ch.receive()}")
        }
        ch.close()
    }
}
