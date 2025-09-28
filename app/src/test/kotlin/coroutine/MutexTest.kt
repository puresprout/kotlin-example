package com.purestation.app.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class MutexTest {
    private var counter = 0
    private val nCoroutines = 100
    private val nIncrements = 1_000
    private lateinit var mutex: Mutex

    @BeforeTest
    fun setup() {
        counter = 0
        mutex = Mutex()
    }

    // 1) 호출한 쓰레드 + Mutex 미사용
    @Test
    fun calledThread_noMutex() = runTest {
        suspend fun increment() {
            repeat(nIncrements) { counter++ }
        }

        val jobs = List(nCoroutines) { launch { increment() } }
        jobs.joinAll()
        println("[calledThread_noMutex] counter=$counter (expected=${nCoroutines * nIncrements})")
    }

    // 2) 호출한 쓰레드 + Mutex 사용
    @Test
    fun calledThread_withMutex() = runTest {
        suspend fun increment() {
            repeat(nIncrements) { mutex.withLock { counter++ } }
        }

        val jobs = List(nCoroutines) { launch { increment() } }
        jobs.joinAll()
        println("[calledThread_withMutex] counter=$counter (expected=${nCoroutines * nIncrements})")
    }

    // 3) Dispatchers.Default + Mutex 미사용
    @Test
    fun defaultDispatcher_noMutex() = runTest {
        suspend fun increment() {
            repeat(nIncrements) { counter++ }
        }

        val jobs = List(nCoroutines) { launch(Dispatchers.Default) { increment() } }
        jobs.joinAll()
        println("[defaultDispatcher_noMutex] counter=$counter (expected=${nCoroutines * nIncrements})")
    }

    // 4) Dispatchers.Default + Mutex 사용
    @Test
    fun defaultDispatcher_withMutex() = runTest {
        suspend fun increment() {
            repeat(nIncrements) { mutex.withLock { counter++ } }
        }

        val jobs = List(nCoroutines) { launch(Dispatchers.Default) { increment() } }
        jobs.joinAll()
        println("[defaultDispatcher_withMutex] counter=$counter (expected=${nCoroutines * nIncrements})")
    }

    /**
     * 호출 쓰레드(runTest 기본) → 싱글 스레드 → 경합 없음 → Mutex 없어도 항상 기대값
     * Default 디스패처 → 멀티 스레드 풀 → 경합 발생 → Mutex 필요
     */
}
