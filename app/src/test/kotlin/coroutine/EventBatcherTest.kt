package com.purestation.app.coroutine

import kotlinx.coroutines.test.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventBatcherTest {

    @Test
    fun `사이즈 기준 flush`() = runTest {
        val uploaded = mutableListOf<List<Event>>()
        val batcher = EventBatcher(this, batchSize = 3, maxDelayMillis = 10) { uploaded += it }

        batcher.submit(Event(1,"A"))
        batcher.submit(Event(2,"B"))
        batcher.submit(Event(3,"C"))

        advanceUntilIdle()
        assertEquals(1, uploaded.size)
        assertEquals(listOf(1,2,3), uploaded[0].map { it.id })

        batcher.close()
    }

    @Test
    fun `시간 기준 flush`() = runTest {
        val uploaded = mutableListOf<List<Event>>()
        val batcher = EventBatcher(this, batchSize = 10, maxDelayMillis = 500) {
            uploaded += it
        }

        batcher.submit(Event(1,"A"))
        batcher.submit(Event(2,"B"))

        advanceTimeBy(499)
//        advanceUntilIdle()
        runCurrent()    // 시간 전진 없이 현재 시각의 작업만 실행
        assertEquals(0, uploaded.size)

        advanceTimeBy(1)
        // 여기서는 flush와 후속 emit까지 다 돌려야 하므로
//        advanceUntilIdle()
        runCurrent()
        runCurrent()
        assertEquals(1, uploaded.size)
        assertEquals(listOf(1,2), uploaded[0].map { it.id })

        batcher.close()
    }
}
