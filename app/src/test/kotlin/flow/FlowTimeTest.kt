package com.purestation.app.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * 시간/속도 제어 (Time)
 *
 * debounce, sample (주기별 값 추출)
 * throttleFirst, throttleLatest (확장 라이브러리 제공)
 * timeout (일정 시간 안 들어오면 취소)
 */
class FlowTimeTest {
    @OptIn(FlowPreview::class)
    @Test
    fun test1() = runTest {
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
//            .distinctUntilChanged()     // 같은 값 연속 방지(선택)
            .collect { println("search for: $it") }
    }

    @OptIn(FlowPreview::class)
    @Test
    fun test2() = runTest {
        fun search(q: String) {
            println("Searching: $q")
        }

        val inputs = flow {
            emit("k"); delay(50)
            emit("ko"); delay(50)
            emit("kot"); delay(50)
            emit("kotlin"); delay(400)
            emit("kotlin"); delay(350)
            emit("kotlin"); delay(10)
            emit("kotlin flow")
        }

        inputs
            .debounce(300)
//            .distinctUntilChanged()
            .collect { search(it) }
    }

    @OptIn(FlowPreview::class)
    @Test
    fun test3() = runTest {
        (1..100).asFlow()
            .onEach { delay(10) }  // 10ms 간격으로 빠르게 emit
            .sample(100)           // 100ms마다 최근값 하나만
            .collect { println(it) }
    }
}
