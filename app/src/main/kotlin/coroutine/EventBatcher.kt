package com.purestation.app.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select

data class Event(val id: Int, val payload: String)

/** Channel로 받은 이벤트를 (개수/시간) 기준으로 모아 업로드하고, 결과 배치를 Flow로 방출 */
class EventBatcher(
    scope: CoroutineScope,
    private val batchSize: Int = 3,
    private val maxDelayMillis: Long = 500,
    private val uploader: suspend (List<Event>) -> Unit
) {
    private val inCh = Channel<Event>(Channel.BUFFERED)
    private val _reports = MutableSharedFlow<List<Event>>(extraBufferCapacity = 64)
    val reports = _reports.asSharedFlow()

    private val worker = scope.launch {
        val buf = mutableListOf<Event>()
        suspend fun flush() {
            if (buf.isEmpty()) return
            val batch = buf.toList()
            buf.clear()
            uploader(batch)
            _reports.emit(batch)
        }
        try {
            while (isActive) {
                select<Unit> {
                    // 이벤트 도착 → 버퍼에 추가, 사이즈 기준 즉시 flush
                    inCh.onReceive { e ->
                        buf += e
                        if (buf.size >= batchSize) flush()
                    }
                    // 버퍼가 비어있지 않으면 시간 기준 flush
                    if (buf.isNotEmpty()) {
                        onTimeout(maxDelayMillis) { flush() }
                    }
                }
            }
        } finally {
            // 종료 시 잔여 flush
            try { flush() } catch (_: Throwable) {}
        }
    }

    suspend fun submit(e: Event) = inCh.send(e)
    fun close() { inCh.close(); worker.cancel() }
}
