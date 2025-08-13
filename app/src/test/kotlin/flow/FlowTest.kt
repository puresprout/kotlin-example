package com.purestation.app.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class FlowTest {
    @Test
    fun conflateTest() = runTest {
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

    @OptIn(FlowPreview::class)
    @Test
    fun debounceTest() = runTest {
        data class Query(val text: String, val at: Long)

        val events = listOf(
            Query("a", 0),
            Query("ap", 120),
            Query("app", 220),
            Query("apple", 800) // 앞에서 멈췄다가 800ms에 새 입력
        )

        val queryFlow = flow {
            val start = System.currentTimeMillis()
            for (e in events) {
                delay(e.at - (System.currentTimeMillis() - start))
                emit(e.text)
            }
        }

        queryFlow
            .debounce(300)              // 300ms 동안 새 입력 없을 때만 방출
            .distinctUntilChanged()     // 같은 값 연속 방지(선택)
            .collect { println("search for: $it") }
    }

    @Test
    fun bufferTest1() = runTest {
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

    @Test
    fun bufferTest2() = runTest {
        (1..10).asFlow()
            .onEach { delay(50) }                          // 빠른 생산
            .buffer(capacity = 2, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .onEach { delay(200) }                         // 느린 소비
            .collect { println("collect $it") }
    }
}