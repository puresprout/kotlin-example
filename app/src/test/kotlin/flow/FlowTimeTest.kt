package com.purestation.app.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        flow {
            emit("h"); delay(50)
            emit("he"); delay(50)
            emit("hel"); delay(50)
            emit("hell"); delay(300)   // 여기서 멈춤
            emit("hello")
        }
            .debounce(200)                 // 200ms 동안 추가 입력 없을 때만 방출
            .collect { println(it) }       // 출력: "hell", "hello"
    }



    @OptIn(FlowPreview::class)
    @Test
    fun test4() = runTest {
        (1..100).asFlow()
            .onEach { delay(10) }  // 10ms 간격으로 빠르게 emit
            .sample(100)           // 100ms마다 최근값 하나만
            .collect { println(it) }
    }



    // Flow 확장 함수: 주어진 windowMillis 동안 첫 번째 값만 방출
    fun <T> Flow<T>.throttleFirst(windowMillis: Long): Flow<T> = flow {
        var lastEmitTime = 0L
        collect { value ->
            val now = System.currentTimeMillis()
            // 마지막 방출 시점으로부터 windowMillis 이상 지난 경우만 방출
            if (now - lastEmitTime >= windowMillis) {
                lastEmitTime = now
                emit(value) // 첫 이벤트를 즉시 방출
            }
        }
    }

    // Flow 확장 함수: 주어진 windowMillis 동안 최신 값(latest)만 방출
    fun <T> Flow<T>.throttleLatest(windowMillis: Long): Flow<T> = channelFlow {
        var lastWindowStart = 0L
        var latest: T? = null

        // 주기적으로 latest 값을 방출하는 코루틴
        launch {
            while (isActive) {
                delay(windowMillis) // 지정된 윈도우 시간 동안 대기
                latest?.let { send(it) } // 최신 값이 있으면 방출
                latest = null
                lastWindowStart = System.currentTimeMillis() // 새로운 윈도우 시작 시점 갱신
            }
        }

        // 원본 Flow의 값 수집
        collect { value ->
            val now = System.currentTimeMillis()
            if (now - lastWindowStart >= windowMillis) {
                // 새 윈도우가 시작된 경우: 첫 이벤트를 즉시 방출
                lastWindowStart = now
                latest = null
                trySend(value) // 즉시 방출
            } else {
                // 현재 윈도우 내에서는 최신 값만 보관
                latest = value
            }
        }
    }

    @Test
    /*
    runTest에서는 가상 시간(virtual time) 으로 delay가 진행되는데, 지금 구현은 System.currentTimeMillis()(실시간)를 써서 윈도우 경과 여부를 판단하고 있어요.
    테스트에서는 가상 시간만 흐르고 실시간은 거의 안 지나므로, 첫 이벤트만 통과하고 그다음은 전부 막혀 1만 찍히는 현상이 납니다. runBlocking에선 실시간이 흐르니 기대대로 여러 값이 찍히구요.
    실시간 대신 코루틴의 지연(delay) 기반으로 윈도우를 관리하도록 바꾸면 테스트/실행 모두에서 일관되게 동작합니다(가상 시간 친화적).
     */
//    fun test5() = runTest {
    fun test5() = runBlocking {
        (1..20).asFlow().onEach { delay(50) }
            .throttleFirst(200)     // 200ms 윈도우 당 첫 값만
            .collect { println("first: $it") }
    }
}
