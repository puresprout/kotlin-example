package com.purestation.app.coroutine

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// 코루틴 내부에서 직접 try/catch
// 자식에서 예외를 소비해 버리면 부모로 안 올라가니 크래시가 안 나요.
//fun main() = runBlocking {
//    val job = launch {
//        try {
//            throw RuntimeException("boom")
//        } catch (e: Throwable) {
//            println("Caught: $e")
//        }
//    }
//}

// 핸들러를 자식 쪽에 불이기
// CoroutineExceptionHandler는 "루트 코루틴(부모가 없는 launch)"의 미처리 예외만 잡아줘.
// 아래 코드는 runBlocking { ... } 안에서 launch(handler)를 만들었으니, 그 launch는 자식 코루틴이야. 자식에서 던진 예외는 먼저 부모(runBlocking)로 전파되고, 자식에 붙인 handler는 무시돼.
//fun main() = runBlocking {
//    val handler = CoroutineExceptionHandler { _, e -> println("Caught: $e") }
//
//    val job = launch(handler) {
//        throw RuntimeException("boom")
//    }
//    job.join()
//}

// 핸들러를 부모(스코프) 쪽에 붙이기
// runBlocking은 루트 코루틴이지만, 자식 코루틴의 예외를 잡아두었다가 종료 시 재던지기 때문에 CoroutineExceptionHandler가 동작하지 않고 크래시가 발생한다. (runBlocking 자체는 루트 코루틴이 맞지만, 그 예외 처리 동작 방식 때문에 handler가 무력화되는 거야.)
// CoroutineExceptionHandler는 "루트 코루틴의 미처리 예외"만 잡음.
//fun main() = runBlocking(
//    CoroutineExceptionHandler { _, e -> println("Caught: $e") }
//) {
//    val job = launch {
//        throw RuntimeException("boom")
//    }
//    job.join() // 완료 대기; 위 예외는 부모의 handler가 처리됨
//}

// 루트 스코프 + Handler(추천)
// 별도 스코프를 만들어 그 안에서 launch하기. 그러면 예외를 핸들러가 처리하고 runBlocking까지 안 튀어요.
fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, e -> println("Caught: $e") }

    // runBlocking과 분리된 루트 스코프
    val scope = CoroutineScope(handler)

    val job = scope.launch {
        throw RuntimeException("boom")
    }
    job.join()         // 예외는 handler가 처리, main 크래시 안 됨
}