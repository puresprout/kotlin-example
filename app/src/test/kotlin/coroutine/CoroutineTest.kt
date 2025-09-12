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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.EmptyCoroutineContext
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
//    fun test4() = runTest {   // 출력이 잘 안 되는데,...
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
}