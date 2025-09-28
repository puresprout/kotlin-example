package com.purestation.app.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SemaphoreTest {
    @Test
    fun test1() = runTest {
        println(Thread.currentThread().name)

        val limiter = Semaphore(permits = 3)

        suspend fun fetch(id: Int) {
            limiter.withPermit {
                println("Start $id on ${Thread.currentThread().name}")
                delay(500) // 네트워크/디스크 같은 I/O를 가정
                println("End   $id")
            }
        }

        val jobs = List(10) { i -> launch(Dispatchers.IO) { fetch(i) } }
        jobs.joinAll()
        println("All done")
    }
}