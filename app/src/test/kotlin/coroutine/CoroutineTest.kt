package com.purestation.app.coroutine

import io.kotest.matchers.equality.compareUsingFields
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.test.Test
import kotlin.time.measureTime

class CoroutineTest {
    // launch와 async 차이 이해하기
    @Test
    fun test1() = runTest {  // 가상시간
//    fun test() = runBlocking {
        suspend fun f(): String {
            delay(300)
            return "f"
        }

        suspend fun g(): String {
            delay(400)
            return "g"
        }

        val elapsed = measureTime {
            val a = async { f() }
            val b = async { g() }
            val r = a.await() + b.await()
            println(r)
        }

        println("elapsed=${elapsed}")
    }

    // 구조적 동시성: 자식 취소 전파
    @Test
    fun test2() = runTest {
        try {
            coroutineScope {    // 범위로 묶으면 예외를 잡을수 있다.
                launch {
                    try {
                        delay(1000)
                        println("A done")
                    } finally {
                        println("A cancelled")
                    }
                }

                launch {
                    delay(200)
                    throw IllegalStateException("B failed")
                }

                launch {
                    delay(100)
                    println("C done")
                }
            }
        } catch (e: Exception) {
            println("caught: ${e.message}")
        }
    }

    // withTimeout과 타임아웃 예외 처리
    @Test
    fun test3() = runTest {
        suspend fun fetch(): String {
            delay(1000)
            return "OK"
        }

        suspend fun safeFetch() = try {
            withTimeout(500) {
                fetch()
            }
        } catch (e: TimeoutCancellationException) {
            null
        }

        println(safeFetch())
    }

    // 예외 전파와 SupervisorJob
    @Test
    fun test4(): Unit = runBlocking {
//    fun test4() = runTest {   // 출력이 안 되는데, supervisorScope 방식으로는 됨
        val scope = CoroutineScope(SupervisorJob())

        scope.launch {
            delay(1000)
            println("A done")
        }

        scope.launch {
            delay(200)
            throw IllegalStateException("B failed")
        }

        scope.launch {
            delay(100)
            println("C done")
        }

        delay(2000)

        scope.cancel()
    }

    // 콜백 통합: suspendCancellableCoroutine
    // 콜백 기반 API (가짜)
    interface NetworkCallback {
        fun onSuccess(data: String)
        fun onError(e: Throwable)
    }

    @Test
    fun test5() = runTest {
        fun fakeNetworkRequest(callback: NetworkCallback) {
            Thread {
                try {
                    Thread.sleep(1000) // 네트워크 대기 시뮬레이션
                    callback.onSuccess("📦 서버 응답 데이터")
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }.start()
        }

        // suspend 함수로 변환
        suspend fun fetchData(): String = suspendCancellableCoroutine { cont ->
            val callback = object : NetworkCallback {
                override fun onSuccess(data: String) {
                    if (cont.isActive) cont.resume(data)
                }
                override fun onError(e: Throwable) {
                    if (cont.isActive) cont.resumeWithException(e)
                }
            }

            // 요청 시작
            fakeNetworkRequest(callback)

            // 취소 시 정리 작업
            cont.invokeOnCancellation {
                println("❌ 코루틴 취소됨 → 네트워크 요청도 취소 가능")
            }

            // At this point the coroutine is suspended
            // by suspendCancellableCoroutine until callback fires
        }

        // 사용 예시
        val job = launch {
            try {
                val result = fetchData()
                println("결과: $result")
            } catch (e: Exception) {
                println("예외 발생: $e")
            }
        }

        delay(500)   // 반쯤 기다리다가
        job.cancel() // 취소해보기
    }
}