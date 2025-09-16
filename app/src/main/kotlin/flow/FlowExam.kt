package com.purestation.app.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

// flatMapLatest (최신만 유지)
@OptIn(ExperimentalCoroutinesApi::class)
fun main(): Unit = runBlocking {
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
