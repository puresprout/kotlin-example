package com.purestation.app.channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Flow ↔ Channel 변환과 backpressure 제어
fun callbackToChannel(scope: CoroutineScope): ReceiveChannel<Int> {
    val ch = Channel<Int>(Channel.UNLIMITED)
    scope.launch {
        repeat(10) { i ->
            delay(50) // 외부 콜백처럼 빠르게 도착
            ch.send(i)
        }
        ch.close()
    }
    return ch
}

fun main(): Unit = runBlocking {
    val ch = callbackToChannel(this)
    ch
        .consumeAsFlow()
        .buffer(capacity = 2, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        .onEach { delay(200) } // 느린 처리
        .collect { println("Handled: $it") }
}