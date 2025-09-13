package com.purestation.app.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { delay(10) }
        } finally {
            withContext(NonCancellable) {
                println("정리 실행")
            }
        }
    }

    // delay없이 cancel하면 코루틴이 실행하기도 전에 취소되서 println으로 안 찍힐수 있음
    delay(10)

    job.cancelAndJoin()
}