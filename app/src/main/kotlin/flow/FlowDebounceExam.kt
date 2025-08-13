package com.purestation.app.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

@OptIn(FlowPreview::class)
suspend fun main() {
//fun main() = runBlocking {
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