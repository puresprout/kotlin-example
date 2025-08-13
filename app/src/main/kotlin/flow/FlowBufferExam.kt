package com.purestation.app.flow

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

suspend fun main() {
//    bufferExam1()
    bufferExam2()
}

private suspend fun bufferExam1() {
    val producer = flow {
        (1..5).forEach {
            println("emit $it @${System.currentTimeMillis() % 100000}")
            emit(it)
            delay(100)
        }
    }

    producer
        .buffer()                 // 기본: Channel.BUFFERED(드랍 없음)
        .onEach { delay(300) }    // 느린 소비자
        .collect { println("collect $it @${System.currentTimeMillis() % 100000}") }
}

private suspend fun bufferExam2() {
    (1..10).asFlow()
        .onEach { delay(50) }                          // 빠른 생산
        .buffer(capacity = 2, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        .onEach { delay(200) }                         // 느린 소비
        .collect { println("collect $it") }
}