package com.purestation.app.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Context/백프레셔 (Context & Buffering)
 *
 * buffer
 * conflate
 * collectLatest
 * flowOn (디스패처 전환)
 */
class FlowBufferingTest {
    @Test
    fun test1() = runTest {
        (1..10).asFlow()
            .onEach { delay(50) }         // 빠르게 생산
            .buffer(capacity = 2)          // 중간에 버퍼
            .map { value ->
                // 값은 버려지지 않으며, 순서가 유지됨

                delay(150)                 // 느린 처리
                "processed $value"
            }
            .collect { println(it) }
    }

    @Test
    fun test2() = runTest {
        (1..10).asFlow()
            .onEach { delay(20) }   // 빠르게 값 발생
            .conflate()             // 최신값만 남기고 건너뜀
            .collect { value ->
                // 값들이 버려질 수 있음

                delay(100)          // 느린 수집
                println("got $value")
            }
    }

    @Test
    fun test3() = runTest {
        flowOf("q", "qu", "que", "query")
            .onEach { delay(50) }       // 사용자 입력 변화
            .collectLatest { q ->       // 새 입력 오면 이전 검색 취소
                // 업스트림 값 자체는 버려지지 않지만, “이전 값에 대한 처리”가 중단되어 결과는 나오지 않을 수 있음

                println("searching: $q")
                delay(200)              // 네트워크 호출 가정
                println("result for: $q")
            }
    }
}