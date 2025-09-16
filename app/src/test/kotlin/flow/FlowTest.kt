package com.purestation.app.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import kotlin.system.measureTimeMillis
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

    // 초간단 asFlow: 1·2·3 제곱 출력
    @Test
    fun test1() = runTest {
        listOf(1,2,3)
            .asFlow()
            .onEach { delay(100) }
            .map { it * it }
            .collect(::println)
    }

    // 기초 수집/취소
    @Test
    fun test2() = runTest {
        fun ticking() = flow {
            (1..5).forEach {
                emit(it)
                delay(300)
            }
        }

        val job = launch {
            ticking().collect { println("got $it") }
        }

        delay(1000)

        job.cancelAndJoin()

        println("cancel")
    }

    // mapLatest로 최신값만 처리
    @Test
    fun test3() = runTest {
        fun fastFlow() : Flow<Int> = flow {
            for (i in 1..5) {
                emit(i)
                delay(200)
            }
        }

        fastFlow().mapLatest {
//            println("$it")

            delay(300)
            "done $it"
        }.collect { println(it) }
    }

    // debounce + distinctUntilChanged
    @OptIn(FlowPreview::class)
    @Test
    fun test4() = runTest {
        fun search(q: String) { println("Searching: $q") }

        val inputs = flow {
            emit("k") ; delay(50)
            emit("ko"); delay(50)
            emit("kot"); delay(50)
            emit("kotlin"); delay(400)
            emit("kotlin"); delay(350)
            emit("kotlin"); delay(10)
            emit("kotlin flow")
        }

        inputs
            .debounce(300)
            .distinctUntilChanged()
            .collect { search(it) }
    }

    // MutableStateFlow + debounce + distinctUntilChanged (검색창)
    @ExperimentalCoroutinesApi
    @FlowPreview
    @Test
    fun test5() = runTest {
        suspend fun search(q: String): List<String> {
            delay(200)
            return listOf(q)
        }

        val query = MutableStateFlow("")

        val job = launch {
            query
                .debounce(300)
    //            .distinctUntilChanged() // MutableStateFlow는 같은 값(==) 설정 시 기본적으로 재방출하지 않는 “distinct until changed” 의미론
                .flatMapLatest { q -> flow { emit(search(q)) } }
                .collect {
    //                println("result: ${search(it)}")

                    println("result: $it")
                }
        }

        listOf("4","42","42d").forEach {
            query.value = it
            delay(150)
        }
        delay(300)
        listOf("42do").forEach {
            query.value = it
            delay(150)
        }

        delay(1000)

        job.cancel()
    }

    // combine으로 UI 상태 만들기
    @Test
    fun test6() = runTest {
        fun loginFlow() = flow {
            emit(false)
            delay(1000)
            emit(true)
        }

        fun alramCountFlow() = flow {
            emit(0)
            delay(2000)
            emit(2)
            delay(1000)
            emit(5)
        }

        combine(loginFlow(), alramCountFlow()) {
            loggedIn, count -> if (loggedIn) "User($count)" else "Guest"
        }.collect(::println)
    }

    // conflate로 중간 값 건너뛰기
    @Test
    fun test7() = runTest {
        fun numbers() = flow {
            for (i in 1..20) {
                emit(i)
                delay(50)
            }
        }

        numbers()
            .conflate()
            .collect {
                delay(200)
                println("processed $it")
        }
    }

    @Test
    fun test8() = runTest {
        // retryWhen으로 지수 백오프
        suspend fun fetch(): String {
            delay(100)
            error("network")
        }
        flow { emit(fetch()) }
            .retryWhen { _, attempt ->
                if (attempt < 3) {
                    val delay = 100L shl attempt.toInt()
    //                val delay = (100 * 2.0.pow(attempt.toInt())).toLong()
    //                println(delay)
                    delay(delay)
                    true
                } else false
            }
            .catch { e -> emit("ERR:${e}") }
            .collect { println(it) }
    }

    // SharedFlow로 다중 구독 + 재생
    @Test
    fun test9() = runTest {
        val bus = MutableSharedFlow<String>(replay = 1)

        val early = launch {
            bus.collect { println("early: $it") }
        }

        launch {
            (1..3).forEach {
                bus.emit("Tick$it")
                delay(100)
            }
        }

        delay(250)

        val late = launch {
            bus.collect { println("late: $it") }
        }

        delay(1000)

        early.cancel()
        late.cancel()
    }

    // flatMapLatest vs flatMapMerge 비교
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test10() = runTest {
        fun upper() = flow {
            listOf("A","B").forEach {
                emit(it)
                delay(1000)
            }
        }

        fun subFlow(v: String) = flow {
            repeat(3) {
                emit("$v-$it")
                delay(600)
            }
        }

        println("== latest ==")
        upper()
            .flatMapLatest { subFlow(it) }
            .collect { println(it) }

        println("== merge ==")
        upper()
            .flatMapMerge { subFlow(it) }
            .collect { println(it) }
    }

    // timeout + onCompletion로 마무리 처리
    @Test
    fun test11() = runTest {
        fun infinite() = flow {
            var i = 0
            while (true) {
                emit(i++)
                delay(200)
            }
        }

        val r = withTimeoutOrNull(900) {
            infinite().onCompletion { println("done") }.collect { println(it) }
        }

        if (r == null) println("timeout")
    }

    @Test
    fun test12() = runTest {
        // 느린 소비자와 buffer/conflate 비교
        fun ints(): Flow<Int> = flow {
            for (i in 1..5) {
                delay(100)
                emit(i)
            }
        }

        suspend fun slowCollect(f: Flow<Int>) {
            f.collect {
                delay(300)
                println("Got $it at ${System.currentTimeMillis()}")
            }
        }

        val tA = measureTimeMillis { slowCollect(ints()) }
        println()
        val tB = measureTimeMillis { slowCollect(ints().buffer()) }
        println()
        val tC = measureTimeMillis { slowCollect(ints().conflate()) }
        println()

        println("A: $tA ms, B(buffer): $tB ms, C(conflate): $tC ms")
    }

    @Test
    fun test13() = runTest {
        // 콜드 Flow → 핫 SharedFlow 브리지
        fun apiFlow() = flow {
            println("API call started")
            delay(300)
            emit("payload")
        }

        val shared = apiFlow()
            .shareIn(this, started = SharingStarted.Eagerly, replay = 1)

        val a = launch { shared.collect { println("A: $it") } }
        val b = launch { shared.collect { println("B: $it") } }

        delay(500)

        a.cancel()
        b.cancel()
    }

    @Test
    fun test14() = runTest {
        // Flow + 에러 & 재시도
        fun riskyFlow(): Flow<Int> = flow {
            emit(1);
            emit(2)
            error("bang")
        }

        riskyFlow()
            // retry(2): 업스트림에서 예외가 나면 최대 2번까지 처음부터 재수집(re-subscribe) 시도
            // 람다는 발생한 예외(it)를 받아서 true면 재시도, false면 재시도 중단
            // 여기선 IOException일 때만 재시도하며, 그 결과를 println으로 출력
            // (참고: error("bang")은 IllegalStateException이므로 실제로는 재시도되지 않음)
            .retry(2) { (it is IOException).also(::println) }
            .catch { emit(-1) }
            .collect { println(it) }
    }

    // flatMapLatest (최신만 유지)
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test15() = runTest {
        // 100ms마다 1,2,3 방출
        val upstream = flow {
            emit(1); delay(100)
            emit(2); delay(100)
            emit(3)
        }

        upstream
            .flatMapLatest { v ->
                flow {
                    emit("start $v")
                    delay(150)               // 다음 값이 오면 현재 Flow는 취소됨
                    emit("end $v")
                }
            }
            .collect { println(it) }        // start 1, start 2, start 3, end 3 (1/2의 end는 취소로 미방출)
    }
}