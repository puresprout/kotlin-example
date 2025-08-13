package com.purestation.app.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

//fun main() = runBlocking {
suspend fun main() {
    val fastProducer = flow {
        (1..10).forEach {
            emit(it)            // 0ms,100ms,200ms,300ms,400ms
            delay(100)
        }
    }

    fastProducer
        .conflate()            // 최신 1개만 유지
        .onEach { println("collected $it at ${System.currentTimeMillis()%100000}") }
        .collect { delay(300) } // 느린 소비자
}