package com.purestation.app.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
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
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.pow

// 초간단 asFlow: 1·2·3 제곱 출력
suspend fun main() {
    listOf(1,2,3)
        .asFlow()
        .onEach { delay(100) }
        .map { it * it }
        .collect(::println)
}



// 기초 수집/취소
//fun ticking() = flow {
//    (1..5).forEach {
//        emit(it)
//        delay(300)
//    }
//}
//
//fun main() = runBlocking {
//    val job = launch {
//        ticking().collect { println("got $it") }
//    }
//
//    delay(1000)
//
//    job.cancelAndJoin()
//
//    println("cancel")
//}



// mapLatest로 최신값만 처리
//fun fastFlow() : Flow<Int> = flow {
//    for (i in 1..5) {
//        emit(i)
//        delay(200)
//    }
//}
//
//suspend fun main() {
//    fastFlow().mapLatest {
////        println("$it")
//
//        delay(300)
//        "done $it"
//    }.collect { println(it) }
//}



// debounce + distinctUntilChanged
//suspend fun search(q: String) { println("Searching: $q") }
//
//@OptIn(FlowPreview::class)
//fun main() = runBlocking {
//    val inputs = flow {
//        emit("k") ; delay(50)
//        emit("ko"); delay(50)
//        emit("kot"); delay(50)
//        emit("kotlin"); delay(400)
//        emit("kotlin"); delay(350)
//        emit("kotlin"); delay(10)
//        emit("kotlin flow")
//    }
//
//    inputs
//        .debounce(300)
//        .distinctUntilChanged()
//        .collect { search(it) }
//}



// MutableStateFlow + debounce + distinctUntilChanged (검색창)
//suspend fun search(q: String): List<String> {
//    delay(200)
//    return listOf(q)
//}
//
//@ExperimentalCoroutinesApi
//@FlowPreview
//fun main() = runBlocking {
//    val query = MutableStateFlow("")
//
//    val job = launch {
//        query
//            .debounce(300)
////            .distinctUntilChanged() // 이 연산자 이전에 MutableStateFlow에서 같은 값을 넣으면 무시됨?
//            .flatMapLatest { q -> flow { emit(search(q)) } }
//            .collect {
////                println("result: ${search(it)}")
//
//                println("result: $it")
//            }
//    }
//
//    listOf("4","42","42d").forEach {
//        query.value = it
//        delay(150)
//    }
//    delay(300)
//    listOf("42do").forEach {
//        query.value = it
//        delay(150)
//    }
//
//    delay(1000)
//
//    job.cancel()
//}



// combine으로 UI 상태 만들기
//fun loginFlow() = flow {
//    emit(false)
//    delay(1000)
//    emit(true)
//}
//
//fun alramCountFlow() = flow {
//    emit(0)
//    delay(2000)
//    emit(2)
//    delay(1000)
//    emit(5)
//}
//
//suspend fun main() {
//    combine(loginFlow(), alramCountFlow()) {
//        loggedIn, count -> if (loggedIn) "User($count)" else "Guest"
//    }.collect(::println)
//}



// conflate로 중간 값 건너뛰기
//fun numbers() = flow {
//    for (i in 1..20) {
//        emit(i)
//        delay(50)
//    }
//}
//
//suspend fun main() {
//    numbers()
//        .conflate()
//        .collect {
//            delay(200)
//            println("processed $it")
//    }
//}



// retryWhen으로 지수 백오프
//suspend fun fetch(): String {
//    delay(100)
//    error("network")
//}
//
//suspend fun main() {
//    flow { emit(fetch()) }
//        .retryWhen { _, attempt ->
//            if (attempt < 3) {
//                val delay = 100L shl attempt.toInt()
////                val delay = (100 * 2.0.pow(attempt.toInt())).toLong()
////                println(delay)
//                delay(delay)
//                true
//            } else false
//        }
//        .catch { e -> emit("ERR:${e}") }
//        .collect { println(it) }
//}



// SharedFlow로 다중 구독 + 재생
//fun main() = runBlocking {
//    val bus = MutableSharedFlow<String>(replay = 1)
//
//    val early = launch {
//        bus.collect { println("early: $it") }
//    }
//
//    launch {
//        (1..3).forEach {
//            bus.emit("Tick$it")
//            delay(100)
//        }
//    }
//
//    delay(250)
//
//    val late = launch {
//        bus.collect { println("late: $it") }
//    }
//
//    delay(1000)
//
//    early.cancel()
//    late.cancel()
//}



// flatMapLatest vs flatMapMerge 비교
//fun upper() = flow {
//    listOf("A","B").forEach {
//        emit(it)
//        delay(1000)
//    }
//}
//
//fun subFlow(v: String) = flow {
//    repeat(3) {
//        emit("$v-$it")
//        delay(600)
//    }
//}
//
//@OptIn(ExperimentalCoroutinesApi::class)
//suspend fun main() {
//    println("== latest ==")
//    upper()
//        .flatMapLatest { subFlow(it) }
//        .collect { println(it) }
//
//    println("== merge ==")
//    upper()
//        .flatMapMerge { subFlow(it) }
//        .collect { println(it) }
//}



// timeout + onCompletion로 마무리 처리
//fun infinite() = flow {
//    var i = 0
//    while (true) {
//        emit(i++)
//        delay(200)
//    }
//}
//
//fun main() = runBlocking {
//    val r = withTimeoutOrNull(900) {
//        infinite().onCompletion { println("done") }.collect { println(it) }
//    }
//
//    if (r == null) println("timeout")
//}
